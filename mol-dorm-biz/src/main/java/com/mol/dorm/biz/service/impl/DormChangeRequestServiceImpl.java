package com.mol.dorm.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.DormChangeRequest;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.mapper.DormBedMapper;
import com.mol.dorm.biz.mapper.DormChangeRequestMapper;
import com.mol.dorm.biz.mapper.DormRoomMapper;
import com.mol.dorm.biz.service.DormChangeRequestService;
import com.mol.sys.biz.mapper.SysOrdinaryUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 宿舍调换申请业务实现类
 * <p>
 * 核心逻辑：
 * 1. 学生提交 -> 状态 0 (待辅导员审批)
 * 2. 辅导员通过 -> 状态 1 (待宿管审批)
 * 3. 宿管通过 -> 执行换房(改床位、改人数) -> 状态 2 (已完成)
 * 4. 任意环节驳回 -> 状态 3 (已驳回)
 * </p>
 *
 * @author mol
 */
@Service
@RequiredArgsConstructor
public class DormChangeRequestServiceImpl extends ServiceImpl<DormChangeRequestMapper, DormChangeRequest> implements DormChangeRequestService {
    
    // 注入所需的 Mapper
    private final SysOrdinaryUserMapper userMapper;
    private final DormRoomMapper roomMapper;
    private final DormBedMapper bedMapper;
    
    // 状态常量定义 (对应数据库注释)
    private static final int STATUS_WAIT_COUNSELOR = 0; // 待辅导员审批
    private static final int STATUS_WAIT_MANAGER = 1;   // 待宿管审批
    private static final int STATUS_FINISH = 2;         // 已完成 (已换房)
    private static final int STATUS_REJECT = 3;         // 已驳回
    
    /**
     * 提交调宿申请
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitRequest(Long userId, Long targetRoomId, String reason) {
        // 1. 校验用户是否存在
        SysOrdinaryUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        
        // 2. 查找用户当前所在的床位 (以此确定原房间)
        DormBed currentBed = bedMapper.selectOne(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getOccupantId, userId)
                .last("LIMIT 1"));
        
        if (currentBed == null) {
            throw new ServiceException("您当前未分配床位，无法申请调宿，请先联系宿管分配。");
        }
        Long currentRoomId = currentBed.getRoomId();
        
        // 3. 校验目标房间
        if (currentRoomId.equals(targetRoomId)) {
            throw new ServiceException("不能申请调换到当前居住的房间");
        }
        DormRoom targetRoom = roomMapper.selectById(targetRoomId);
        if (targetRoom == null) {
            throw new ServiceException("目标房间不存在");
        }
        
        // 4. 校验性别匹配 (用户 1男0女 vs 房间 1男2女)
        // 简单转换逻辑：用户的1对应房间1，用户的0对应房间2
        int userGenderMap = (user.getSex() != null && user.getSex() == 1) ? 1 : 2;
        // 如果房间有性别限制(不为null) 且 不匹配
        if (targetRoom.getGender() != null && !targetRoom.getGender().equals(userGenderMap)) {
            throw new ServiceException("无法申请与您性别不符的宿舍楼栋");
        }
        
        // 5. 校验目标房间容量 (当前人数 >= 最大容量)
        if (targetRoom.getCurrentNum() >= targetRoom.getCapacity()) {
            throw new ServiceException("目标房间已满员，无法申请");
        }
        
        // 6. 避免重复提交 (检查是否有未结束的申请: 状态 0 或 1)
        Long pendingCount = this.baseMapper.selectCount(new LambdaQueryWrapper<DormChangeRequest>()
                .eq(DormChangeRequest::getStudentId, userId)
                .in(DormChangeRequest::getStatus, STATUS_WAIT_COUNSELOR, STATUS_WAIT_MANAGER));
        
        if (pendingCount > 0) {
            throw new ServiceException("您已有正在审核中的申请，请勿重复提交");
        }
        
        // 7. 构造并保存申请记录
        DormChangeRequest request = new DormChangeRequest();
        request.setStudentId(userId);
        request.setCurrentRoomId(currentRoomId);
        request.setTargetRoomId(targetRoomId);
        request.setReason(reason);
        request.setStatus(STATUS_WAIT_COUNSELOR); // 初始状态：待辅导员审批
        // createTime 由 BaseEntity 和 MybatisPlusConfig 自动填充，无需 set
        
        this.save(request);
    }
    
    /**
     * 辅导员审批 (第一级审批)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditByCounselor(Long requestId, Boolean pass, String msg) {
        // 1. 获取申请单
        DormChangeRequest request = this.getById(requestId);
        if (request == null) {
            throw new ServiceException("申请记录不存在");
        }
        
        // 2. 校验状态：必须是 "待辅导员审批"
        if (request.getStatus() != STATUS_WAIT_COUNSELOR) {
            throw new ServiceException("该申请状态已变更，无法进行辅导员审批");
        }
        
        // 3. 处理审批结果
        if (pass) {
            // 通过 -> 流转到下一级 (宿管)
            request.setStatus(STATUS_WAIT_MANAGER);
        } else {
            // 驳回 -> 流程结束
            request.setStatus(STATUS_REJECT);
        }
        
        // 4. 记录审批意见
        request.setAuditMsg("【辅导员】: " + (msg == null ? "无" : msg));
        
        this.updateById(request);
    }
    
    /**
     * 宿管经理审批 (第二级审批，终审)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditByManager(Long requestId, Boolean pass, String msg) {
        // 1. 获取申请单
        DormChangeRequest request = this.getById(requestId);
        if (request == null) {
            throw new ServiceException("申请记录不存在");
        }
        
        // 2. 校验状态：必须是 "待宿管审批"
        if (request.getStatus() != STATUS_WAIT_MANAGER) {
            throw new ServiceException("该申请未通过辅导员审批或已处理，无法操作");
        }
        
        // 3. 处理审批结果
        if (pass) {
            // === 核心：执行实际换房逻辑 ===
            executeRoomChange(request);
            
            // 只有换房成功后，状态才改为已完成
            request.setStatus(STATUS_FINISH);
        } else {
            // 驳回
            request.setStatus(STATUS_REJECT);
        }
        
        // 4. 追加审批意见
        String oldMsg = request.getAuditMsg() == null ? "" : request.getAuditMsg();
        request.setAuditMsg(oldMsg + " | 【宿管】: " + (msg == null ? "无" : msg));
        
        this.updateById(request);
    }
    
    /**
     * 分页查询申请列表
     */
    @Override
    public Page<DormChangeRequest> getRequestList(Page<DormChangeRequest> page, Long userId, Integer status) {
        LambdaQueryWrapper<DormChangeRequest> wrapper = new LambdaQueryWrapper<>();
        
        // 如果传入了 studentId，则查询该学生的
        if (userId != null) {
            wrapper.eq(DormChangeRequest::getStudentId, userId);
        }
        // 如果传入了状态，则筛选状态
        if (status != null) {
            wrapper.eq(DormChangeRequest::getStatus, status);
        }
        
        // 按时间倒序
        wrapper.orderByDesc(DormChangeRequest::getCreateTime); // 使用 BaseEntity 的 createTime
        
        return this.page(page, wrapper);
    }
    
    /**
     * 私有辅助方法：执行换房的原子操作
     */
    private void executeRoomChange(DormChangeRequest request) {
        Long userId = request.getStudentId();
        Long oldRoomId = request.getCurrentRoomId();
        Long newRoomId = request.getTargetRoomId();
        
        // 1. Double Check: 目标房间是否刚刚已满 (防止并发超卖)
        DormRoom newRoom = roomMapper.selectById(newRoomId);
        if (newRoom.getCurrentNum() >= newRoom.getCapacity()) {
            throw new ServiceException("审核中断：目标房间刚刚已满员，无法分配");
        }
        
        // 2. 释放旧床位
        // 找到该学生当前占用的床位
        DormBed oldBed = bedMapper.selectOne(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getOccupantId, userId));
        
        if (oldBed != null) {
            oldBed.setOccupantId(null); // 清空占用者
            bedMapper.updateById(oldBed); // 更新数据库
            
            // 更新旧房间的统计人数 -1
            DormRoom oldRoom = roomMapper.selectById(oldRoomId);
            if (oldRoom != null && oldRoom.getCurrentNum() > 0) {
                oldRoom.setCurrentNum(oldRoom.getCurrentNum() - 1);
                roomMapper.updateById(oldRoom);
            }
        }
        
        // 3. 占用新床位
        // 在目标房间里找一个 occupant_id 为空的床位
        DormBed newBed = bedMapper.selectOne(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getRoomId, newRoomId)
                .isNull(DormBed::getOccupantId)
                .last("LIMIT 1")); // 只要一个
        
        if (newBed == null) {
            throw new ServiceException("数据异常：目标房间显示有余量，但未找到空闲床位记录");
        }
        
        // 绑定用户
        newBed.setOccupantId(userId);
        bedMapper.updateById(newBed);
        
        // 更新新房间的统计人数 +1
        newRoom.setCurrentNum(newRoom.getCurrentNum() + 1);
        roomMapper.updateById(newRoom);
    }
}