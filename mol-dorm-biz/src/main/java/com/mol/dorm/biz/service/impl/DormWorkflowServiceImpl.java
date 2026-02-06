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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 宿舍业务流转服务实现类
 * 🛡️ [防刁民设计手册]：
 * 1. 物理强一致性：所有床位变动后，必须强制触发房间体检引擎（evaluateRoomSafety）。
 * 2. 状态码防伪：拒绝在 Java 内存中拍脑袋决定房间是否已满，必须实时 count 数据库。
 * 3. 互换原子性：防止在互换过程中由于网络中断导致两个学生共享一张床。
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
    private final DormRoomService roomService; // 引入房间服务用于触发体检引擎
    
    // =================================================================================
    // 1. C端申请逻辑 (学生/教工发起)
    // =================================================================================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitCheckIn(DormWorkflow app) {
        SysOrdinaryUser user = getUserAndCheckStatus(app.getUserId());
        checkUserHasNoBed(user.getId()); // [防刁民] 一个人不能占两张床
        checkDuplicateApplication(user.getId(), DormWorkflow.TYPE_CHECK_IN_NEW);
        
        app.setType(DormWorkflow.TYPE_CHECK_IN_NEW);
        fillBaseInfo(app, user);
        this.save(app);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitRoomChange(DormWorkflow app) {
        SysOrdinaryUser user = getUserAndCheckStatus(app.getUserId());
        checkUserHasBed(user.getId()); // [防刁民] 没房的人不能申请换房
        
        if (app.getTargetRoomId() != null) {
            DormRoom target = roomMapper.selectById(app.getTargetRoomId());
            // [逻辑防线] 只有 20-正常 状态的房间才允许申请
            if (target == null || target.getStatus() != DormConstants.LC_NORMAL) {
                throw new ServiceException("申请拦截：意向房间目前处于非分配状态（维修/锁定）");
            }
        }
        
        checkDuplicateApplication(user.getId(), DormWorkflow.TYPE_EXCHANGE_ADMIN);
        app.setType(DormWorkflow.TYPE_EXCHANGE_ADMIN);
        fillBaseInfo(app, user);
        this.save(app);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitMutualExchange(DormWorkflow app) {
        SysOrdinaryUser me = getUserAndCheckStatus(app.getUserId());
        // 🛡️ [准入审计] ID < 2 为系统保留账号（如 admin），严禁参与换寝
        if (me.getId() < 2) throw new ServiceException("系统保护：预置管理账号禁止参与互换业务");
        checkUserHasBed(me.getId());
        
        if (app.getTargetBedId() == null) throw new ServiceException("请选择互换目标床位");
        DormBed targetBed = bedMapper.selectById(app.getTargetBedId());
        if (targetBed == null || targetBed.getOccupantId() == null) {
            throw new ServiceException("互换失败：目标床位目前无人居住");
        }
        
        SysOrdinaryUser other = userMapper.selectById(targetBed.getOccupantId());
        // [性别防范] 毕设重点：严禁异性互换申请绕过逻辑
        if (!me.getGender().equals(other.getGender())) {
            throw new ServiceException("安全警告：严禁提交异性换宿申请！");
        }
        
        checkDuplicateApplication(me.getId(), DormWorkflow.TYPE_EXCHANGE_MUTUAL);
        app.setType(DormWorkflow.TYPE_EXCHANGE_MUTUAL);
        fillBaseInfo(app, me);
        this.save(app);
    }
    
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
        checkUserHasBed(user.getId());
        checkDuplicateApplication(user.getId(), DormWorkflow.TYPE_HOLIDAY_STAY);
        app.setType(DormWorkflow.TYPE_HOLIDAY_STAY);
        fillBaseInfo(app, user);
        this.save(app);
    }
    
    // =================================================================================
    // 2. B端强制干预 (管理员执行)
    // =================================================================================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleApproval(Long applicationId, Boolean agree, String reply) {
        DormWorkflow app = this.getById(applicationId);
        if (app == null || app.getStatus() != 0) throw new ServiceException("申请单已失效");
        
        if (Boolean.TRUE.equals(agree)) {
            app.setStatus(1); // 通过
            if (app.getType() == DormWorkflow.TYPE_CHECK_OUT) {
                this.forceMove(app.getUserId(), null);
            } else if (app.getTargetBedId() != null) {
                this.forceMove(app.getUserId(), app.getTargetBedId());
            }
        } else {
            app.setStatus(2); // 驳回
        }
        app.setHandleTime(LocalDateTime.now());
        app.setHandleNote(reply);
        this.updateById(app);
    }
    
    /**
     * 2.2 物理搬迁引擎
     * 🛡️ [防故障逻辑]：
     * 1. 搬迁前先释放旧床位。
     * 2. 搬迁后立即触发房间体检引擎（evaluateRoomSafety），确保实时人数和饱和度状态码（21-26）绝对准确。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceMove(Long userId, Long targetBedId) {
        DormBed currentBed = bedMapper.selectOne(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getOccupantId, userId));
        Long oldRoomId = (currentBed != null) ? currentBed.getRoomId() : null;
        
        // A. 执行退宿 (targetBedId 为空)
        if (targetBedId == null) {
            if (currentBed != null) {
                executeCheckOut(currentBed);
                roomService.evaluateRoomSafety(oldRoomId); // [联动] 释放后体检
            }
            return;
        }
        
        // B. 执行入住/搬迁
        DormBed target = bedMapper.selectById(targetBedId);
        if (target == null || target.getOccupantId() != null) throw new ServiceException("目标床位不可用");
        
        // 1. 如果有旧房，先退宿
        if (currentBed != null) {
            executeCheckOut(currentBed);
            roomService.evaluateRoomSafety(oldRoomId);
        }
        
        // 2. 进驻新房
        SysOrdinaryUser user = userMapper.selectById(userId);
        target.setOccupantId(userId);
        target.setResStatus(DormConstants.RES_USING); // 22-已占用
        target.setUpdateTime(LocalDateTime.now());
        bedMapper.updateById(target);
        
        // 3. [核心联动] 触发新房间的体检引擎，自动更新 current_num 和 res_status
        roomService.evaluateRoomSafety(target.getRoomId());
    }
    
    /**
     * 2.3 强制对调
     * 🛡️ [原子性保证]：防止在对调时因为 ID 冲突导致数据丢失。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceSwap(Long userIdA, Long userIdB) {
        DormBed bedA = bedMapper.selectOne(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getOccupantId, userIdA));
        DormBed bedB = bedMapper.selectOne(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getOccupantId, userIdB));
        
        if (bedA == null || bedB == null) throw new ServiceException("对调失败：双方必须都在住");
        
        // 临时清空 A，腾出位置
        bedA.setOccupantId(null);
        bedMapper.updateById(bedA);
        
        // B 的人去 A
        bedA.setOccupantId(userIdB);
        bedMapper.updateById(bedA);
        
        // A 的人去 B
        bedB.setOccupantId(userIdA);
        bedMapper.updateById(bedB);
        
        log.info("⚖️ 行政干预：用户 {} 与 {} 已完成床位互换", userIdA, userIdB);
    }
    
    /**
     * 2.4 批量毕业处理
     * 🟢 修复返回值：现在返回处理的总人数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchGraduate(Integer grade) {
        // 1. 找到该年级所有在住的学生 ID
        List<SysOrdinaryUser> students = userMapper.selectList(Wrappers.<SysOrdinaryUser>lambdaQuery()
                .eq(SysOrdinaryUser::getEnrollmentYear, grade));
        if (CollUtil.isEmpty(students)) return 0;
        
        List<Long> userIds = students.stream().map(SysOrdinaryUser::getId).collect(Collectors.toList());
        
        // 2. 找出这些学生占用的房间 ID（用于后续体检）
        List<DormBed> occupiedBeds = bedMapper.selectList(Wrappers.<DormBed>lambdaQuery()
                .in(DormBed::getOccupantId, userIds));
        List<Long> roomIds = occupiedBeds.stream().map(DormBed::getRoomId).distinct().collect(Collectors.toList());
        
        // 3. [原子批量释放]
        int rows = bedMapper.update(null, Wrappers.<DormBed>lambdaUpdate()
                .in(DormBed::getOccupantId, userIds)
                .set(DormBed::getOccupantId, null)
                .set(DormBed::getResStatus, DormConstants.RES_EMPTY));
        
        // 4. [防数据腐化] 对涉及的所有房间重新执行体检
        roomIds.forEach(roomService::evaluateRoomSafety);
        
        return rows;
    }
    
    // =================================================================================
    // 🛡️ 私有辅助审计逻辑
    // =================================================================================
    
    private void executeCheckOut(DormBed bed) {
        bed.setOccupantId(null);
        bed.setResStatus(DormConstants.RES_EMPTY);
        bedMapper.updateById(bed);
    }
    
    private SysOrdinaryUser getUserAndCheckStatus(Long userId) {
        SysOrdinaryUser user = userMapper.selectById(userId);
        if (user == null) throw new ServiceException("非法用户：档案不存在");
        return user;
    }
    
    private void checkUserHasBed(Long userId) {
        Long count = bedMapper.selectCount(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getOccupantId, userId));
        if (count == 0) throw new ServiceException("拦截：您当前名下无床位，无法执行此操作");
    }
    
    private void checkUserHasNoBed(Long userId) {
        Long count = bedMapper.selectCount(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getOccupantId, userId));
        if (count > 0) throw new ServiceException("拦截：您已经在住，请勿重复申请");
    }
    
    private void checkDuplicateApplication(Long userId, Integer type) {
        Long count = workflowMapper.selectCount(Wrappers.<DormWorkflow>lambdaQuery()
                .eq(DormWorkflow::getUserId, userId)
                .eq(DormWorkflow::getType, type)
                .eq(DormWorkflow::getStatus, 0));
        if (count > 0) throw new ServiceException("重复操作：您有一笔同类申请正在审批中");
    }
    
    private void fillBaseInfo(DormWorkflow app, SysOrdinaryUser user) {
        app.setUsername(user.getUsername());
        app.setRealName(user.getRealName());
        app.setGender(String.valueOf(user.getGender()));
        app.setUserType(user.getUserCategory());
        app.setStatus(0); // 待审批
        app.setCreateTime(LocalDateTime.now());
    }
}