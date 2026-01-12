package com.mol.dorm.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.bto.BuildingInitDto;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.DormBuilding;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.mapper.DormBuildingMapper;
import com.mol.dorm.biz.mapper.DormRoomMapper;
import com.mol.dorm.biz.service.DormBedService;
import com.mol.dorm.biz.service.DormBuildingService;
import com.mol.dorm.biz.service.DormRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 宿舍楼栋业务实现类
 * <p>
 * 核心功能：
 * 1. 楼栋的生命周期管理（建、改、删）。
 * 2. 一键初始化楼栋（自动生成成百上千个房间和床位）。
 * 3. 严格的安全校验（封楼、删除前必须确保楼内无人）。
 * </p>
 *
 * @author mol
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DormBuildingServiceImpl extends ServiceImpl<DormBuildingMapper, DormBuilding> implements DormBuildingService {
    
    // 注入房间 Mapper，用于核心的人数统计校验
    private final DormRoomMapper roomMapper;
    
    // 注入房间和床位 Service，用于级联删除和批量插入
    private final DormRoomService roomService;
    private final DormBedService bedService;
    
    // =========================== 1. 基础管理 (带安全校验) ===========================
    
    @Override
    public boolean saveBuilding(DormBuilding building) {
        // 设置默认状态
        if (building.getStatus() == null) {
            building.setStatus(1); // 1-正常启用
        }
        return this.save(building);
    }
    
    /**
     * 修改楼栋信息
     * <p>
     * 安全逻辑：
     * 如果尝试将状态改为 0 (封禁/停用)，系统会检查该楼栋下是否有房间仍有人居住。
     * 如果有，则禁止封禁，抛出异常。
     * </p>
     */
    @Override
    public boolean updateBuilding(DormBuilding building) {
        DormBuilding oldBuilding = this.getById(building.getId());
        if (oldBuilding == null) {
            throw new ServiceException("楼栋不存在");
        }
        
        // 核心校验：如果准备封楼 (old=1 -> new=0)
        if (building.getStatus() != null && building.getStatus() == 0 && oldBuilding.getStatus() == 1) {
            checkIfBuildingHasPeople(building.getId(), "封禁失败");
        }
        
        return this.updateById(building);
    }
    
    /**
     * 删除楼栋 (级联删除)
     * <p>
     * 逻辑顺序：
     * 1. 安全检查：确认全楼无人居住。
     * 2. 删除该楼所有床位。
     * 3. 删除该楼所有房间。
     * 4. 删除楼栋本身。
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBuilding(Long buildingId) {
        // 1. 安全检测：是否有人居住
        checkIfBuildingHasPeople(buildingId, "删除失败");
        
        // 2. 查出该楼所有房间ID (用于级联删除)
        List<DormRoom> rooms = roomMapper.selectList(new LambdaQueryWrapper<DormRoom>()
                .select(DormRoom::getId) // 只查ID，性能更好
                .eq(DormRoom::getBuildingId, buildingId));
        
        if (CollUtil.isNotEmpty(rooms)) {
            List<Long> roomIds = rooms.stream().map(DormRoom::getId).toList();
            
            // A. 级联删除所有床位
            // DELETE FROM dorm_bed WHERE room_id IN (...)
            bedService.remove(new LambdaQueryWrapper<DormBed>().in(DormBed::getRoomId, roomIds));
            
            // B. 级联删除所有房间
            // DELETE FROM dorm_room WHERE id IN (...)
            roomService.removeByIds(roomIds);
            
            log.info("级联删除成功：已清理楼栋[{}]下的 {} 间房间及其床位", buildingId, roomIds.size());
        }
        
        // 3. 删除楼栋主体
        this.removeById(buildingId);
    }
    
    // =========================== 2. 一键初始化 (核心功能) ===========================
    
    /**
     * 一键初始化楼栋
     * <p>
     * 根据参数自动创建：楼栋 -> 房间 (Floor * RoomPerFloor) -> 床位 (Capacity)。
     * 例如：7层 * 100间/层 = 700间房，700 * 4床 = 2800个床位。
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initBuilding(BuildingInitDto dto) {
        // 1. 参数防御性校验
        if (dto.getFloors() == null || dto.getFloors() <= 0) throw new ServiceException("楼层数必须大于0");
        if (dto.getRoomsPerFloor() == null || dto.getRoomsPerFloor() <= 0) throw new ServiceException("每层房间数必须大于0");
        if (dto.getDefaultCapacity() == null || dto.getDefaultCapacity() <= 0) throw new ServiceException("默认床位数必须大于0");
        
        // 2. 创建并保存楼栋对象
        DormBuilding building = new DormBuilding();
        BeanUtils.copyProperties(dto, building);
        building.setStatus(1); // 默认启用
        this.save(building);   // MP 插入后自动回填 ID
        
        Long buildingId = building.getId();
        log.info("初始化楼栋成功，ID: {}, 开始生成房间数据...", buildingId);
        
        // 3. 内存中批量生成房间对象
        List<DormRoom> roomList = new ArrayList<>();
        int floors = dto.getFloors();
        int roomsPerFloor = dto.getRoomsPerFloor();
        int capacity = dto.getDefaultCapacity();
        int gender = dto.getDefaultGender() == null ? 0 : dto.getDefaultGender(); // 0-混合/未定
        
        for (int f = 1; f <= floors; f++) {
            for (int r = 1; r <= roomsPerFloor; r++) {
                DormRoom room = new DormRoom();
                room.setBuildingId(buildingId);
                room.setFloorNo(f); // 设置楼层
                
                // 智能生成房间号：
                // 如果单层房间少于100 -> 101, 102
                // 如果单层房间多于100 -> 1001, 1002
                String roomNo = (roomsPerFloor < 100)
                        ? String.format("%d%02d", f, r)
                        : String.format("%d%03d", f, r);
                
                room.setRoomNo(roomNo);
                room.setCapacity(capacity);
                room.setCurrentNum(0);
                room.setGender(gender);
                room.setStatus(1);
                
                roomList.add(room);
            }
        }
        
        // 4. 批量插入房间 (Batch Insert)
        // 这一步至关重要：MP 的 saveBatch 会在插入后将生成的 ID 回填到 roomList 的对象中
        roomService.saveBatch(roomList);
        log.info("房间批量创建完成，共 {} 间，开始生成床位...", roomList.size());
        
        // 5. 内存中批量生成床位对象
        List<DormBed> bedList = new ArrayList<>();
        for (DormRoom room : roomList) {
            // 根据每个房间的容量生成对应数量的床位
            for (int i = 1; i <= room.getCapacity(); i++) {
                DormBed bed = new DormBed();
                bed.setRoomId(room.getId()); // 使用回填的 ID
                bed.setBedLabel(room.getRoomNo() + "-" + i); // 例如: 101-1
                bed.setOccupantId(null); // 初始为空
                bedList.add(bed);
            }
        }
        
        // 6. 批量插入床位 (分批处理防止 SQL 过长)
        int batchSize = 1000;
        if (bedList.size() > batchSize) {
            for (int i = 0; i < bedList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, bedList.size());
                bedService.saveBatch(bedList.subList(i, end));
            }
        } else {
            bedService.saveBatch(bedList);
        }
        
        log.info("床位初始化完成，共生成 {} 个床位。", bedList.size());
    }
    
    // ================= 私有辅助方法 =================
    
    /**
     * 检测楼栋内是否有人居住
     * @param buildingId 楼栋ID
     * @param opName 操作名称 (用于异常提示)
     * @throws ServiceException 如果有人居住则抛出
     */
    private void checkIfBuildingHasPeople(Long buildingId, String opName) {
        // 查询该楼栋下，current_num > 0 的房间数量
        Long occupiedCount = roomMapper.selectCount(new LambdaQueryWrapper<DormRoom>()
                .eq(DormRoom::getBuildingId, buildingId)
                .gt(DormRoom::getCurrentNum, 0)); // 只有人数 > 0 才算有人
        
        if (occupiedCount != null && occupiedCount > 0) {
            throw new ServiceException(opName + "：该楼栋内仍有 " + occupiedCount + " 间宿舍有人居住！请先清退人员。");
        }
    }
}