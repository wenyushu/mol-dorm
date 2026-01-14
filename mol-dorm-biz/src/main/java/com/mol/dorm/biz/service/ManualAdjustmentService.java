package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员手动调宿/原子操作服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ManualAdjustmentService {
    
    private final DormBedService bedService;
    private final DormRoomService roomService;
    private final SysOrdinaryUserMapper userMapper;
    
    /**
     * 场景一：两人互换床位 (原子操作)
     * 互换是 ID 对调，不涉及 NULL，所以 updateById 没问题
     */
    @Transactional(rollbackFor = Exception.class)
    public void swapBeds(Long studentIdA, Long studentIdB) {
        DormBed bedA = getBedByStudent(studentIdA);
        DormBed bedB = getBedByStudent(studentIdB);
        
        checkGenderConstraint(studentIdA, bedB.getRoomId());
        checkGenderConstraint(studentIdB, bedA.getRoomId());
        
        bedA.setOccupantId(studentIdB);
        bedB.setOccupantId(studentIdA);
        
        bedService.updateById(bedA);
        bedService.updateById(bedB);
        
        log.info("执行互换: 学生[{}] <-> 学生[{}]", studentIdA, studentIdB);
    }
    
    /**
     * 场景二：单人强制搬迁 / 退宿
     */
    @Transactional(rollbackFor = Exception.class)
    public void moveUserToBed(Long studentId, Long targetBedId) {
        // 1. 释放原床位
        DormBed oldBed = bedService.getOne(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getOccupantId, studentId));
        if (oldBed != null) {
            // [Fix] 显式更新为 NULL
            bedService.update(null, Wrappers.<DormBed>lambdaUpdate()
                    .eq(DormBed::getId, oldBed.getId())
                    .set(DormBed::getOccupantId, null)); // 强制设为 NULL
            
            updateRoomCount(oldBed.getRoomId(), -1);
        }
        
        if (targetBedId == null) {
            log.info("管理员执行退宿: 学生[{}]", studentId);
            return;
        }
        
        // 2. 占用新床位
        DormBed newBed = bedService.getById(targetBedId);
        if (newBed == null) throw new ServiceException("目标床位不存在");
        if (newBed.getOccupantId() != null) throw new ServiceException("目标床位已被占用");
        
        checkGenderConstraint(studentId, newBed.getRoomId());
        
        newBed.setOccupantId(studentId);
        bedService.updateById(newBed);
        updateRoomCount(newBed.getRoomId(), 1);
        
        log.info("管理员执行搬迁: 学生[{}] -> 床位[{}]", studentId, targetBedId);
    }
    
    /**
     * 场景三：批量毕业生退宿
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchGraduate(int year) {
        List<Long> studentIds = userMapper.selectList(Wrappers.<SysOrdinaryUser>lambdaQuery()
                .apply("YEAR(entry_date) = {0}", year)
        ).stream().map(SysOrdinaryUser::getId).toList();
        
        if (studentIds.isEmpty()) return;
        
        List<DormBed> beds = bedService.list(Wrappers.<DormBed>lambdaQuery()
                .in(DormBed::getOccupantId, studentIds));
        
        if (beds.isEmpty()) return;
        
        Map<Long, Long> roomCountDelta = beds.stream()
                .collect(Collectors.groupingBy(DormBed::getRoomId, Collectors.counting()));
        
        // [Fix] 批量强制置空
        List<Long> bedIdsToClear = beds.stream().map(DormBed::getId).toList();
        bedService.update(null, Wrappers.<DormBed>lambdaUpdate()
                .in(DormBed::getId, bedIdsToClear)
                .set(DormBed::getOccupantId, null));
        
        for (Map.Entry<Long, Long> entry : roomCountDelta.entrySet()) {
            roomService.update(Wrappers.<DormRoom>lambdaUpdate()
                    .eq(DormRoom::getId, entry.getKey())
                    .setSql("current_num = current_num - " + entry.getValue()));
        }
        
        log.info("批量毕业处理完成: 年级[{}], 释放床位[{}]个", year, beds.size());
    }
    
    private DormBed getBedByStudent(Long userId) {
        DormBed bed = bedService.getOne(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getOccupantId, userId));
        if (bed == null) throw new ServiceException("学生[" + userId + "]当前未分配床位");
        return bed;
    }
    
    private void updateRoomCount(Long roomId, int delta) {
        roomService.update(Wrappers.<DormRoom>lambdaUpdate()
                .eq(DormRoom::getId, roomId)
                .setSql("current_num = current_num + " + delta));
    }
    
    private void checkGenderConstraint(Long userId, Long roomId) {
        SysOrdinaryUser user = userMapper.selectById(userId);
        DormRoom room = roomService.getById(roomId);
        if (room.getGender() != null && user.getSex() != null && !room.getGender().equals(user.getSex())) {
            throw new ServiceException("性别冲突");
        }
    }
}