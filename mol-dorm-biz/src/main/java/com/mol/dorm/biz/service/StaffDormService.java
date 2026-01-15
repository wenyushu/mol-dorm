package com.mol.dorm.biz.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.*;
import com.mol.dorm.biz.mapper.DormStaffApplicationMapper;
import com.mol.server.service.SysOrdinaryUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 教职工住宿管理服务
 * <p>
 * 核心逻辑：
 * 1. 资源严格隔离：教职工只能住 usage_type=1 的楼栋。
 * 2. 申请审批制：不自动分配，必须人工审核资格。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StaffDormService {
    
    private final SysOrdinaryUserService userService;
    private final DormBuildingService buildingService;
    private final DormRoomService roomService;
    private final DormBedService bedService;
    private final DormStaffApplicationMapper applicationMapper;
    
    /**
     * 1. 教职工提交入住/换房申请
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitApplication(Long userId, Integer applyType, String reason, String targetRoomType) {
        // A. 身份校验 (防刁民：防止学生冒充教职工申请公寓)
        SysOrdinaryUser user = userService.getById(userId);
        if (user == null || user.getUserCategory() != 1) { // 1代表教职工
            throw new ServiceException("非法操作：仅限教职工身份申请人才公寓");
        }
        
        // B. 重复校验
        Long count = applicationMapper.selectCount(Wrappers.<DormStaffApplication>lambdaQuery()
                .eq(DormStaffApplication::getUserId, userId)
                .eq(DormStaffApplication::getStatus, 0)); // 0待审批
        if (count > 0) {
            throw new ServiceException("您已有一条待审批的申请，请勿重复提交");
        }
        
        // C. 状态校验 (如果是新入住，必须当前无房)
        if (applyType == 0) {
            if (checkUserHasBed(userId)) throw new ServiceException("您名下已有宿舍资源，请先退宿或申请换房");
        }
        
        // D. 入库
        DormStaffApplication app = new DormStaffApplication();
        app.setUserId(userId);
        app.setApplyType(applyType);
        app.setReason(reason);
        app.setTargetRoomType(targetRoomType);
        app.setStatus(0);
        applicationMapper.insert(app);
    }
    
    /**
     * 2. 管理员审批并分配房间 (人工分配，因为教职工通常需要挑房)
     *
     * @param applicationId 申请单 ID
     * @param roomId 指定分配的房间 ID (必须是教工楼的房间)
     * @param agree 是否同意
     */
    @Transactional(rollbackFor = Exception.class)
    public void approveAndAssign(Long applicationId, Long roomId, boolean agree, String rejectReason) {
        DormStaffApplication app = applicationMapper.selectById(applicationId);
        if (app == null || app.getStatus() != 0) throw new ServiceException("申请单不存在或已处理");
        
        if (!agree) {
            app.setStatus(2); // 拒绝
            app.setRemark(rejectReason);
            applicationMapper.updateById(app);
            return;
        }
        
        // --- 同意逻辑 ---
        
        // 1. 校验目标房间是否合法 (防刁民核心)
        if (roomId == null) throw new ServiceException("同意申请时必须指定房间");
        DormRoom room = roomService.getById(roomId);
        DormBuilding building = buildingService.getById(room.getBuildingId());
        
        // 获取申请人的详细信息（为了拿性别）
        SysOrdinaryUser applicant = userService.getById(app.getUserId());
        if (applicant == null) throw new ServiceException("申请人账号异常");
        
        // ---------------------------------------------------------
        // 🛡️ 校验核心：防刁民、防混住
        // ---------------------------------------------------------

        // 1. 资源隔离校验：教工不能住学生楼
        if (building.getUsageType() != 1) {
            throw new ServiceException("违规操作：该房间位于学生宿舍区，无法分配给教职工！");
        }
        
        // 2. 容量校验
        if (room.getCurrentNum() >= room.getCapacity()) {
            throw new ServiceException("该房间已满员");
        }
        
        
        // 3. 【新增】性别强校验 (Gender Check)
        // 逻辑：如果房间设定了性别（非混合宿舍），则必须匹配
        // 1-男, 2-女, 0-混合(家庭房/夫妻房)
        Integer roomGender = room.getGender();
        Integer userSex = applicant.getSex();
        
        if (roomGender != null && roomGender != 0) {
            // 如果房间不是混合宿舍，且性别不匹配，直接拦截
            if (!roomGender.equals(userSex)) {
                String roomSexStr = (roomGender == 1) ? "男教工" : "女教工";
                String userSexStr = (userSex == 1) ? "男" : "女";
                throw new ServiceException(String.format(
                        "性别冲突：该房间仅限[%s]入住，申请人为[%s]性", roomSexStr, userSexStr));
            }
        }
        // 如果 roomGender == 0，视为家庭房或特殊混合房，允许任何性别入住
        
        
        // ---------------------------------------------------------
        // 执行分配 (保持不变)
        // ---------------------------------------------------------
        
        // 4. 查找空床位
        List<DormBed> emptyBeds = bedService.list(Wrappers.<DormBed>lambdaQuery()
                .eq(DormBed::getRoomId, roomId)
                .isNull(DormBed::getOccupantId));
        if (CollUtil.isEmpty(emptyBeds)) throw new ServiceException("数据异常：房间未满但无空床位");
        
        DormBed targetBed = emptyBeds.get(0);
        
        // 5. 如果是换房，先清理旧床位
        if (app.getApplyType() == 2) {
            clearUserBed(app.getUserId());
        }
        
        // 6. 分配新床位
        targetBed.setOccupantId(app.getUserId());
        bedService.updateById(targetBed);
        
        // 7. 更新房间内人数
        room.setCurrentNum(room.getCurrentNum() + 1);
        roomService.updateById(room);
        
        // 8. 更新申请单
        app.setStatus(1); // 通过
        app.setRemark("已分配至: " + building.getBuildingName() + " - " + room.getRoomNo());
        applicationMapper.updateById(app);
        
        log.info("教职工入住成功: 用户[{}] -> 房间[{}]", app.getUserId(), room.getRoomNo());
    }
    
    /**
     * 3. 教职工退宿 (离职或外住)
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkOut(Long userId) {
        if (!checkUserHasBed(userId)) throw new ServiceException("该教职工当前未入住");
        clearUserBed(userId);
    }
    
    // ================= 辅助方法 =================
    
    private boolean checkUserHasBed(Long userId) {
        return bedService.count(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getOccupantId, userId)) > 0;
    }
    
    private void clearUserBed(Long userId) {
        DormBed bed = bedService.getOne(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getOccupantId, userId));
        if (bed != null) {
            bed.setOccupantId(null);
            bedService.updateById(bed);
            
            // 更新房间计数
            DormRoom room = roomService.getById(bed.getRoomId());
            room.setCurrentNum(room.getCurrentNum() - 1);
            roomService.updateById(room);
        }
    }
}