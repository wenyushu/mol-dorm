package com.mol.dorm.biz.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
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
 * 3. 换房逻辑：支持原有资源自动腾退。
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
     *
     * @param userId         教职工 ID
     * @param applyType      申请类型 (0-入住申请, 1-退宿申请, 2-换房申请)
     * @param reason         申请原因
     * @param targetRoomType 期望房型 (如：单人间/家庭房)
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitApplication(Long userId, Integer applyType, String reason, String targetRoomType) {
        // A. 身份校验
        SysOrdinaryUser user = userService.getById(userId);
        // 假设 UserCategory=1 为教职工
        if (user == null || (user.getUserCategory() != null && user.getUserCategory() != 1)) {
            throw new ServiceException("非法操作：仅限教职工身份申请人才公寓");
        }
        
        // B. 重复校验 (防止同时提交两张单子)
        Long count = applicationMapper.selectCount(Wrappers.<DormStaffApplication>lambdaQuery()
                .eq(DormStaffApplication::getUserId, userId)
                .eq(DormStaffApplication::getStatus, 0)); // 0待审批
        if (count > 0) {
            throw new ServiceException("您已有一条待审批的申请，请勿重复提交");
        }
        
        // C. 状态校验 (核心防刁民逻辑)
        boolean hasBed = checkUserHasBed(userId);
        
        if (applyType == 0) {
            // [入住申请]：必须当前无房
            if (hasBed) throw new ServiceException("您名下已有宿舍资源，无需申请入住，请申请[换房]");
        } else if (applyType == 2) {
            // [换房申请]：必须当前有房
            if (!hasBed) throw new ServiceException("您当前未入住任何房间，无法申请换房，请申请[入住]");
        } else if (applyType == 1) {
            // [退宿申请]：必须当前有房
            if (!hasBed) throw new ServiceException("您当前未入住，无法申请退宿");
        }
        
        // D. 入库
        DormStaffApplication app = new DormStaffApplication();
        app.setUserId(userId);
        app.setApplyType(applyType);
        app.setReason(reason);
        app.setTargetRoomType(targetRoomType);
        app.setStatus(0); // 0-待审批
        applicationMapper.insert(app);
    }
    
    /**
     * 2. 管理员审批并分配房间 (完美支持换房自动腾退)
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
        
        // 如果是退宿申请 (applyType=1)，直接同意并清理资源
        if (app.getApplyType() == 1) {
            checkOut(app.getUserId());
            app.setStatus(1);
            app.setRemark("同意退宿");
            applicationMapper.updateById(app);
            return;
        }
        
        // --- 以下为 入住(0) 或 换房(2) 的同意逻辑 ---
        
        // 1. 校验目标房间
        if (roomId == null) throw new ServiceException("同意申请时必须指定房间");
        DormRoom room = roomService.getById(roomId);
        DormBuilding building = buildingService.getById(room.getBuildingId());
        SysOrdinaryUser applicant = userService.getById(app.getUserId());
        
        // 2. 资源隔离校验
        if (building.getUsageType() != 1) {
            throw new ServiceException("违规操作：该房间位于学生宿舍区，无法分配给教职工！");
        }
        
        // 3. 容量校验
        if (room.getCurrentNum() >= room.getCapacity()) {
            throw new ServiceException("该房间已满员");
        }
        
        // 4. 性别强校验 (String类型)
        String roomGender = room.getGender();
        String userGender = applicant.getGender();
        if (StrUtil.isNotBlank(roomGender)) {
            if (!StrUtil.equals(roomGender, userGender)) {
                String roomSexStr = "1".equals(roomGender) ? "男教工" : "女教工";
                String userSexStr = "1".equals(userGender) ? "男" : "女";
                throw new ServiceException(String.format("性别冲突：该房间仅限[%s]入住，申请人为[%s]性", roomSexStr, userSexStr));
            }
        }
        
        // 5. 查找新房间的空床位
        List<DormBed> emptyBeds = bedService.list(Wrappers.<DormBed>lambdaQuery()
                .eq(DormBed::getRoomId, roomId)
                .isNull(DormBed::getOccupantId));
        if (CollUtil.isEmpty(emptyBeds)) throw new ServiceException("数据异常：房间未满但无空床位");
        DormBed targetBed = emptyBeds.get(0);
        
        // 🟢 6. 换房核心：先清理旧床位
        // 如果是换房申请(2)，必须先把原来占用的坑释放出来，否则一个人占两个坑
        if (app.getApplyType() == 2) {
            clearUserBed(app.getUserId());
            log.info("换房操作：已自动腾退用户[{}]的旧床位", app.getUserId());
        }
        
        // 7. 分配新床位
        targetBed.setOccupantId(app.getUserId());
        bedService.updateById(targetBed);
        
        // 8. 更新房间内人数
        room.setCurrentNum(room.getCurrentNum() + 1);
        roomService.updateById(room);
        
        // 9. 更新申请单
        app.setStatus(1); // 通过
        app.setRemark("已分配至: " + building.getBuildingName() + " - " + room.getRoomNo());
        applicationMapper.updateById(app);
        
        log.info("教职工安置成功: 用户[{}] -> 房间[{}]", app.getUserId(), room.getRoomNo());
    }
    
    // ... checkOut, checkUserHasBed, clearUserBed 保持不变 ...
    
    @Transactional(rollbackFor = Exception.class)
    public void checkOut(Long userId) {
        if (!checkUserHasBed(userId)) throw new ServiceException("该教职工当前未入住");
        clearUserBed(userId);
    }
    
    private boolean checkUserHasBed(Long userId) {
        return bedService.count(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getOccupantId, userId)) > 0;
    }
    
    private void clearUserBed(Long userId) {
        DormBed bed = bedService.getOne(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getOccupantId, userId));
        if (bed != null) {
            bed.setOccupantId(null);
            bedService.updateById(bed);
            DormRoom room = roomService.getById(bed.getRoomId());
            if (room.getCurrentNum() > 0) {
                room.setCurrentNum(room.getCurrentNum() - 1);
                roomService.updateById(room);
            }
        }
    }
}