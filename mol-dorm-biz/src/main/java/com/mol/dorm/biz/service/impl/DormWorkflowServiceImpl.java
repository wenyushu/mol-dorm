package com.mol.dorm.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.constant.DormConstants;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.*;
import com.mol.dorm.biz.mapper.*;
import com.mol.dorm.biz.service.DormRoomService;
import com.mol.dorm.biz.service.DormWorkflowService;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 宿舍业务流转服务实现类 - 资源调度核心引擎
 * 🛡️ [防刁民设计手册]：
 * 1. 物理强一致性：任何退宿、搬迁、毕业操作后，强制驱动 evaluateRoomSafety 体检，防止人数统计腐化。
 * 2. 状态机防伪：不信任前端传入的状态，所有操作前必须查库校验当前是否有房或是否有在途申请。
 * 3. AOP事务隔离：审批流中调用物理执行方法时，使用代理对象调用，确保大规模搬迁时事务不失效。
 * 4. 动态清算：结合 (入学+学制+异动) 算法，实现对超期占用资源的精准“灭世”打击。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DormWorkflowServiceImpl extends ServiceImpl<DormWorkflowMapper, DormWorkflow> implements DormWorkflowService {
    
    private final DormWorkflowMapper workflowMapper;
    private final SysOrdinaryUserMapper userMapper;
    private final DormRoomMapper roomMapper;
    private final DormBedMapper bedMapper;
    
    @Lazy
    private final DormRoomService roomService;
    
    // =================================================================================
    // 1. C端申请逻辑 (学生/教工发起)
    // =================================================================================
    
    /**
     * [提交入住申请]
     * 用途：新生或新入职员工申请宿舍。
     * 🛡️ 防刁民：1. 严禁已在住人员申请；2. 严禁重复提交多份申请导致流程堆积。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitCheckIn(DormWorkflow app) {
        SysOrdinaryUser user = getUserAndCheckStatus(app.getUserId());
        checkUserHasNoBed(user.getId());
        checkDuplicateApplication(user.getId(), DormWorkflow.TYPE_CHECK_IN_NEW);
        
        app.setType(DormWorkflow.TYPE_CHECK_IN_NEW);
        fillBaseInfo(app, user);
        this.save(app);
    }
    
    /**
     * [提交调宿申请]
     * 用途：在住学生申请更换房间。
     * 🛡️ 防刁民：1. 没房的人不准换房；2. 目标房间必须是 LC_NORMAL(20) 正常态，禁止申请维修或预留房。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitRoomChange(DormWorkflow app) {
        SysOrdinaryUser user = getUserAndCheckStatus(app.getUserId());
        checkUserHasBed(user.getId());
        
        if (app.getTargetRoomId() != null) {
            DormRoom target = roomMapper.selectById(app.getTargetRoomId());
            // 🛡️ 修正：int 类型使用 != 比对，防止“无法调用 int 方法”报错
            if (target == null || target.getStatus() != DormConstants.LC_NORMAL) {
                throw new ServiceException("申请拦截：意向房间目前处于非分配状态（维修/预留）");
            }
        }
        
        checkDuplicateApplication(user.getId(), DormWorkflow.TYPE_EXCHANGE_ADMIN);
        app.setType(DormWorkflow.TYPE_EXCHANGE_ADMIN);
        fillBaseInfo(app, user);
        this.save(app);
    }
    
    /**
     * [提交互换申请]
     * 用途：两名在住学生私下商量好后，提交系统备案互换。
     * 🛡️ 防刁民：1. 目标床位必须有人；2. 强校验双方性别，严禁异性互换申请绕过规则。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitMutualExchange(DormWorkflow app) {
        SysOrdinaryUser me = getUserAndCheckStatus(app.getUserId());
        checkUserHasBed(me.getId());
        
        DormBed targetBed = bedMapper.selectById(app.getTargetBedId());
        if (targetBed == null || targetBed.getOccupantId() == null) {
            throw new ServiceException("互换失败：目标床位目前无人居住，请走普通调宿流程");
        }
        
        SysOrdinaryUser other = userMapper.selectById(targetBed.getOccupantId());
        // 🛡️ 性对齐：me.getGender() 是 "0"/"1"，与 Integer 强转比对，对齐 0女1男
        if (!Integer.valueOf(me.getGender()).equals(Integer.valueOf(other.getGender()))) {
            throw new ServiceException("安全警告：严禁提交异性换宿申请！");
        }
        
        checkDuplicateApplication(me.getId(), DormWorkflow.TYPE_EXCHANGE_MUTUAL);
        app.setType(DormWorkflow.TYPE_EXCHANGE_MUTUAL);
        fillBaseInfo(app, me);
        this.save(app);
    }
    
    /**
     * [提交退宿申请]
     * 🛡️ 防刁民：没房的人禁止提交。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitCheckOut(DormWorkflow app) {
        SysOrdinaryUser user = getUserAndCheckStatus(app.getUserId());
        checkUserHasBed(user.getId());
        checkDuplicateApplication(user.getId(), DormWorkflow.TYPE_CHECK_OUT);
        app.setType(DormWorkflow.TYPE_CHECK_OUT);
        fillBaseInfo(app, user);
        this.save(app);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitHolidayStay(DormWorkflow app) {
        SysOrdinaryUser user = getUserAndCheckStatus(app.getUserId());
        app.setType(DormWorkflow.TYPE_HOLIDAY_STAY);
        fillBaseInfo(app, user);
        this.save(app);
    }
    
    // =================================================================================
    // 2. B端管理方法 (宿管执行)
    // =================================================================================
    
    /**
     * [审批处理出口]
     * 🛡️ AOP 代理调用：通过 AopContext.currentProxy() 调用物理搬迁，确保事务不因内部自调用失效。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleApproval(Long applicationId, Boolean agree, String reply) {
        DormWorkflow app = this.getById(applicationId);
        if (app == null || app.getStatus() != 0) throw new ServiceException("申请单已失效或已处理");
        
        if (Boolean.TRUE.equals(agree)) {
            app.setStatus(1); // 通过
            // 🚀 [核弹级加固]：获取代理对象执行物理指令
            DormWorkflowService proxy = (DormWorkflowService) AopContext.currentProxy();
            if (app.getType() == DormWorkflow.TYPE_CHECK_OUT) {
                proxy.forceMove(app.getUserId(), null);
            } else if (app.getTargetBedId() != null) {
                proxy.forceMove(app.getUserId(), app.getTargetBedId());
            }
        } else {
            app.setStatus(2); // 驳回
        }
        app.setHandleTime(LocalDateTime.now());
        app.setHandleNote(reply);
        this.updateById(app);
    }
    
    /**
     * [物理搬迁引擎]
     * 用途：强制修改床位占用情况，是系统中最重的数据操作。
     * 🛡️ 资源自愈：搬迁后立即触发 evaluateRoomSafety，刷新房间 RES_FULL(26) 等状态码。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceMove(Long userId, Long targetBedId) {
        DormBed currentBed = bedMapper.selectOne(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getOccupantId, userId));
        Long oldRoomId = (currentBed != null) ? currentBed.getRoomId() : null;
        
        // A. 强制退宿场景
        if (targetBedId == null) {
            if (currentBed != null) {
                executeCheckOut(currentBed);
                roomService.evaluateRoomSafety(oldRoomId);
            }
            return;
        }
        
        // B. 强制搬迁场景
        DormBed target = bedMapper.selectById(targetBedId);
        if (target == null || target.getOccupantId() != null) throw new ServiceException("目标床位不可用");
        
        if (currentBed != null) {
            executeCheckOut(currentBed);
            roomService.evaluateRoomSafety(oldRoomId);
        }
        
        target.setOccupantId(userId);
        target.setResStatus(DormConstants.RES_USING); // 22-已占用
        bedMapper.updateById(target);
        
        userMapper.update(null, Wrappers.<SysOrdinaryUser>lambdaUpdate()
                .eq(SysOrdinaryUser::getId, userId)
                .set(SysOrdinaryUser::getDormId, target.getRoomId()));
        
        roomService.evaluateRoomSafety(target.getRoomId());
    }
    
    /**
     * [灭世级清算引擎]
     * 用途：针对毕业生执行一键清退。
     * 🛡️ 动态算法：(入学年 + 学制 + 异动年) <= 审计年。精准剔除留级生，清理到期生。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchGraduate(Integer currentYear) {
        log.info("🔥 [审计清算] 启动 {} 年度数字化清算引擎...", currentYear);
        
        List<SysOrdinaryUser> overdueStudents = userMapper.selectOverdueStudents(currentYear);
        if (CollUtil.isEmpty(overdueStudents)) return 0;
        
        List<Long> userIds = overdueStudents.stream().map(SysOrdinaryUser::getId).collect(Collectors.toList());
        List<DormBed> occupiedBeds = bedMapper.selectList(Wrappers.<DormBed>lambdaQuery().in(DormBed::getOccupantId, userIds));
        List<Long> affectedRoomIds = occupiedBeds.stream().map(DormBed::getRoomId).distinct().toList();
        
        // 1. 物理释放床位
        int rows = bedMapper.update(null, Wrappers.<DormBed>lambdaUpdate()
                .in(DormBed::getOccupantId, userIds)
                .set(DormBed::getOccupantId, null)
                .set(DormBed::getResStatus, DormConstants.RES_EMPTY)); // 21-空闲
        
        // 2. 档案强制归档
        userMapper.update(null, Wrappers.<SysOrdinaryUser>lambdaUpdate()
                .in(SysOrdinaryUser::getId, userIds)
                .set(SysOrdinaryUser::getStatus, "2") // 2-已归档
                .set(SysOrdinaryUser::getDormId, null));
        
        // 3. 房间自愈
        affectedRoomIds.forEach(roomService::evaluateRoomSafety);
        
        log.info("✅ 清算结束：归档档案 {} 份，物理释放席位 {} 张。", userIds.size(), rows);
        return rows;
    }
    
    /**
     * [上帝模式-对调交换]
     * 🛡️ 原子交换：采用“临时归零”法，防止在对调时因为 ID 冲突导致数据丢失。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceSwap(Long userIdA, Long userIdB) {
        DormBed bedA = bedMapper.selectOne(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getOccupantId, userIdA));
        DormBed bedB = bedMapper.selectOne(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getOccupantId, userIdB));
        
        if (bedA == null || bedB == null) throw new ServiceException("对调失败：双方必须都在住且床位档案完整");
        
        bedA.setOccupantId(null); bedMapper.updateById(bedA);
        bedB.setOccupantId(userIdA); bedMapper.updateById(bedB);
        bedA.setOccupantId(userIdB); bedMapper.updateById(bedA);
        
        log.info("⚖️ 行政干预：用户 {} 与 {} 已完成床位互换", userIdA, userIdB);
    }
    
    // =================================================================================
    // 🛡️ 私有辅助审计逻辑 (不省略)
    // =================================================================================
    
    private void executeCheckOut(DormBed bed) {
        bed.setOccupantId(null);
        bed.setResStatus(DormConstants.RES_EMPTY); // 21-空闲
        bedMapper.updateById(bed);
    }
    
    private SysOrdinaryUser getUserAndCheckStatus(Long userId) {
        SysOrdinaryUser user = userMapper.selectById(userId);
        if (user == null || "1".equals(user.getStatus())) throw new ServiceException("非法用户：档案不存在或已停用");
        return user;
    }
    
    private void checkUserHasBed(Long userId) {
        if (bedMapper.selectCount(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getOccupantId, userId)) == 0) {
            throw new ServiceException("拦截：您当前名下无床位资源");
        }
    }
    
    private void checkUserHasNoBed(Long userId) {
        if (bedMapper.selectCount(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getOccupantId, userId)) > 0) {
            throw new ServiceException("拦截：您已经在住，请勿重复申请");
        }
    }
    
    private void checkDuplicateApplication(Long userId, Integer type) {
        Long count = workflowMapper.selectCount(Wrappers.<DormWorkflow>lambdaQuery()
                .eq(DormWorkflow::getUserId, userId)
                .eq(DormWorkflow::getType, type)
                .eq(DormWorkflow::getStatus, 0)); // 0-待审批
        if (count > 0) throw new ServiceException("重复操作：您已有一笔同类申请正在审批中");
    }
    
    private void fillBaseInfo(DormWorkflow app, SysOrdinaryUser user) {
        app.setUsername(user.getUsername());
        app.setRealName(user.getRealName());
        app.setGender(user.getGender());
        app.setUserType(user.getUserCategory());
        app.setStatus(0); // 待审批
        app.setCreateTime(LocalDateTime.now());
    }
}