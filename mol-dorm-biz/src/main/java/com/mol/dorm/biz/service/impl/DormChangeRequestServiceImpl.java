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
import com.mol.server.mapper.SysOrdinaryUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 宿舍调宿申请业务实现类
 * <p>
 * 业务流程：
 * 1. 学生提交申请 -> 状态 0 (待辅导员审批)
 * 2. 辅导员审批通过 -> 状态 1 (待宿管审批)
 * 3. 宿管审批通过 -> 执行实际换房操作(改床位、改人数) -> 状态 2 (已完成)
 * 4. 任意环节驳回 -> 状态 3 (已驳回)
 * </p>
 *
 * @author mol
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DormChangeRequestServiceImpl extends ServiceImpl<DormChangeRequestMapper, DormChangeRequest> implements DormChangeRequestService {
    
    // 注入必要的 Mapper
    private final SysOrdinaryUserMapper userMapper; // 用于查询学生信息
    private final DormRoomMapper roomMapper;        // 用于查询和更新房间信息
    private final DormBedMapper bedMapper;          // 用于查询和更新床位信息
    
    // 状态常量定义 (与数据库注释一致)
    private static final int STATUS_WAIT_COUNSELOR = 0; // 待辅导员审批
    private static final int STATUS_WAIT_MANAGER = 1;   // 待宿管审批
    private static final int STATUS_FINISH = 2;         // 已完成
    private static final int STATUS_REJECT = 3;         // 已驳回
    
    /**
     * 提交调宿申请
     *
     * @param userId       申请学生ID
     * @param targetRoomId 目标房间ID
     * @param reason       申请原因
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitRequest(Long userId, Long targetRoomId, String reason) {
        // 1. 校验用户是否存在
        SysOrdinaryUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        
        // 2. 查找用户当前所在的床位 (确定原房间)
        DormBed currentBed = bedMapper.selectOne(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getOccupantId, userId)
                .last("LIMIT 1"));
        
        if (currentBed == null) {
            throw new ServiceException("您当前未分配床位，无法申请调宿，请先联系宿管分配。");
        }
        Long currentRoomId = currentBed.getRoomId();
        
        // 3. 基础校验：不能换到自己现在的房间
        if (currentRoomId.equals(targetRoomId)) {
            throw new ServiceException("不能申请调换到当前居住的房间");
        }
        
        // 4. 校验目标房间是否存在及状态
        DormRoom targetRoom = roomMapper.selectById(targetRoomId);
        if (targetRoom == null) {
            throw new ServiceException("目标房间不存在");
        }
        if (targetRoom.getStatus() != null && targetRoom.getStatus() == 0) {
            throw new ServiceException("目标房间处于维护或封禁状态，无法申请");
        }
        
        // 5. 校验性别匹配 (简单逻辑：1男2女)
        // 假设 SysOrdinaryUser.sex: 1-男, 0/2-女; DormRoom.gender: 1-男, 2-女
        // 这里做简单的映射转换，具体根据你的业务字典调整
        int userGender = (user.getSex() != null && user.getSex() == 1) ? 1 : 2;
        if (targetRoom.getGender() != null && targetRoom.getGender() != 0) { // 0为混合楼，不校验
            if (!targetRoom.getGender().equals(userGender)) {
                throw new ServiceException("无法申请与您性别不符的宿舍楼栋");
            }
        }
        
        // 6. 校验目标房间容量 (防止满员还申请)
        if (targetRoom.getCurrentNum() >= targetRoom.getCapacity()) {
            throw new ServiceException("目标房间已满员，暂时无法申请");
        }
        
        // 7. 防重复提交校验 (检查是否有未结束的流程)
        Long pendingCount = this.baseMapper.selectCount(new LambdaQueryWrapper<DormChangeRequest>()
                .eq(DormChangeRequest::getStudentId, userId)
                .in(DormChangeRequest::getStatus, STATUS_WAIT_COUNSELOR, STATUS_WAIT_MANAGER));
        
        if (pendingCount > 0) {
            throw new ServiceException("您已有正在审核中的申请，请勿重复提交");
        }
        
        // 8. 构造并保存申请单
        DormChangeRequest request = new DormChangeRequest();
        request.setStudentId(userId);
        request.setCurrentRoomId(currentRoomId);
        request.setTargetRoomId(targetRoomId);
        request.setReason(reason);
        request.setStatus(STATUS_WAIT_COUNSELOR); // 初始状态：待辅导员审批
        // createTime 由 Mybatis-Plus 自动填充
        
        this.save(request);
    }
    
    /**
     * 通用审批入口 (核心修复方法)
     * <p>
     * 自动根据申请单当前状态，路由到具体的审批逻辑(辅导员 or 宿管)。
     * 解决了 Controller 无法确定调用哪个 Service 方法的问题。
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveRequest(Long requestId, Boolean pass, String msg) {
        DormChangeRequest request = this.getById(requestId);
        if (request == null) {
            throw new ServiceException("申请记录不存在");
        }
        
        // 路由逻辑
        if (request.getStatus() == STATUS_WAIT_COUNSELOR) {
            // 当前是待辅导员审批 -> 调用辅导员审批逻辑
            auditByCounselor(requestId, pass, msg);
        } else if (request.getStatus() == STATUS_WAIT_MANAGER) {
            // 当前是待宿管审批 -> 调用宿管审批逻辑
            auditByManager(requestId, pass, msg);
        } else {
            // 其他状态不可操作
            throw new ServiceException("当前状态不可审批 (可能已完成或已驳回)");
        }
    }
    
    /**
     * 辅导员审批 (第一级)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditByCounselor(Long requestId, Boolean pass, String msg) {
        DormChangeRequest request = this.getById(requestId);
        
        // 双重校验状态
        if (request.getStatus() != STATUS_WAIT_COUNSELOR) {
            throw new ServiceException("状态已变更，辅导员审批失败");
        }
        
        if (pass) {
            // 通过 -> 进入下一级
            request.setStatus(STATUS_WAIT_MANAGER);
        } else {
            // 驳回 -> 结束
            request.setStatus(STATUS_REJECT);
        }
        
        // 追加审批意见
        request.setAuditMsg("【辅导员】: " + (msg == null ? "同意" : msg));
        this.updateById(request);
    }
    
    /**
     * 宿管经理审批 (第二级/终审)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditByManager(Long requestId, Boolean pass, String msg) {
        DormChangeRequest request = this.getById(requestId);
        
        // 双重校验状态
        if (request.getStatus() != STATUS_WAIT_MANAGER) {
            throw new ServiceException("状态已变更，宿管审批失败");
        }
        
        if (pass) {
            // 通过 -> 核心：执行换房原子操作
            executeRoomChange(request);
            
            // 操作成功后，更新状态为已完成
            request.setStatus(STATUS_FINISH);
        } else {
            // 驳回
            request.setStatus(STATUS_REJECT);
        }
        
        // 追加审批意见 (保留之前的意见)
        String oldMsg = request.getAuditMsg() == null ? "" : request.getAuditMsg();
        request.setAuditMsg(oldMsg + " | 【宿管】: " + (msg == null ? "同意" : msg));
        
        this.updateById(request);
    }
    
    /**
     * 分页查询申请列表
     */
    @Override
    public Page<DormChangeRequest> getRequestList(Page<DormChangeRequest> page, Long userId, Integer status) {
        LambdaQueryWrapper<DormChangeRequest> wrapper = new LambdaQueryWrapper<>();
        
        // 动态拼接查询条件
        if (userId != null) {
            wrapper.eq(DormChangeRequest::getStudentId, userId);
        }
        if (status != null) {
            wrapper.eq(DormChangeRequest::getStatus, status);
        }
        
        // 按时间倒序排列
        wrapper.orderByDesc(DormChangeRequest::getCreateTime);
        
        return this.page(page, wrapper);
    }
    
    /**
     * 私有辅助方法：执行换房的原子操作
     * <p>
     * 包含：
     * 1. 释放旧床位 (更新 bed, 更新 oldRoom.currentNum)
     * 2. 占用新床位 (更新 bed, 更新 newRoom.currentNum)
     * </p>
     */
    private void executeRoomChange(DormChangeRequest request) {
        Long userId = request.getStudentId();
        Long newRoomId = request.getTargetRoomId();
        
        // --- 1. 释放旧床位 ---
        // 查找学生当前占用的床位
        DormBed oldBed = bedMapper.selectOne(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getOccupantId, userId));
        
        if (oldBed != null) {
            // 清空床位上的学生 ID
            oldBed.setOccupantId(null);
            bedMapper.updateById(oldBed);
            
            // 旧房间人数 -1
            DormRoom oldRoom = roomMapper.selectById(oldBed.getRoomId());
            if (oldRoom != null && oldRoom.getCurrentNum() > 0) {
                oldRoom.setCurrentNum(oldRoom.getCurrentNum() - 1);
                roomMapper.updateById(oldRoom);
            }
        }
        
        // --- 2. 占用新床位 ---
        // 再次校验目标房间容量 (防止审批期间被抢占)
        DormRoom newRoom = roomMapper.selectById(newRoomId);
        if (newRoom.getCurrentNum() >= newRoom.getCapacity()) {
            throw new ServiceException("换房失败：目标房间刚刚已满员");
        }
        
        // 查找一个空闲床位
        DormBed newBed = bedMapper.selectOne(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getRoomId, newRoomId)
                .isNull(DormBed::getOccupantId) // 必须是空的
                .last("LIMIT 1"));
        
        if (newBed == null) {
            throw new ServiceException("换房失败：目标房间数据异常，未找到空床位记录");
        }
        
        // 绑定学生
        newBed.setOccupantId(userId);
        bedMapper.updateById(newBed);
        
        // 新房间人数 +1
        newRoom.setCurrentNum(newRoom.getCurrentNum() + 1);
        roomMapper.updateById(newRoom);
        
        log.info("换房成功：学生[{}] 从房间[{}] 移动到 [{}]", userId, request.getCurrentRoomId(), newRoomId);
    }
}