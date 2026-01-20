package com.mol.dorm.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormBuilding;
import com.mol.dorm.biz.entity.DormFloor;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.mapper.DormBuildingMapper;
import com.mol.dorm.biz.mapper.DormFloorMapper;
import com.mol.dorm.biz.mapper.DormRoomMapper;
import com.mol.dorm.biz.service.DormFloorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 宿舍楼层业务实现类
 * <p>
 * 核心职责：
 * 1. 楼层层级管理 (承上启下：上有楼栋，下有房间)。
 * 2. 严格的性别限制校验 (配合混合楼/单性别楼)。
 * </p>
 *
 * @author mol
 */
@Service
@RequiredArgsConstructor
public class DormFloorServiceImpl extends ServiceImpl<DormFloorMapper, DormFloor> implements DormFloorService {
    
    private final DormBuildingMapper buildingMapper;
    private final DormRoomMapper roomMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveFloor(DormFloor floor) {
        // 1. 查上级：楼栋是否存在且启用
        DormBuilding building = buildingMapper.selectById(floor.getBuildingId());
        
        if (building == null) {
            throw new ServiceException("防刁民拦截：所属楼栋不存在！");
        }
        
        // 🛡️ 状态拦截升级：不仅拦截 0(停用)，还要拦截 41(装修)
        // 只有状态为 1 (启用) 的楼栋才允许搞基建
        if (building.getStatus() != 1) {
            throw new ServiceException("操作拦截：所属楼栋处于 [停用/装修] 状态，禁止新增楼层！");
        }
        
        // 2. 🛡️ 性别熔断机制 (Anti-Diaomin)
        // Building: 1-男, 2-女, 3-混合
        // Floor: 1-男, 2-女, 0-无限制(通常不允许)
        
        // 场景A：男楼里建女层 -> ❌
        if (building.getGenderLimit() == 1 && floor.getGenderLimit() == 2) {
            throw new ServiceException("逻辑冲突：[纯男楼] 内禁止创建 [女层]");
        }
        // 场景B：女楼里建男层 -> ❌
        if (building.getGenderLimit() == 2 && floor.getGenderLimit() == 1) {
            throw new ServiceException("逻辑冲突：[纯女楼] 内禁止创建 [男层]");
        }
        // 场景C：混合楼 -> ✅ (允许创建男层或女层)
        
        // 3. 冗余字段填充 (加速查询)
        floor.setCampusId(building.getCampusId());
        
        // 4. 默认值兜底
        if (floor.getStatus() == null) {
            floor.setStatus(1); // 默认启用
        }
        
        // 5. 查重 (防止同一栋楼出现两个 "3楼")
        // 这是一个物理层面的重复校验
        boolean exists = this.exists(new LambdaQueryWrapper<DormFloor>()
                .eq(DormFloor::getBuildingId, floor.getBuildingId())
                .eq(DormFloor::getFloorNum, floor.getFloorNum()));
        if (exists) {
            throw new ServiceException("该楼栋已存在 " + floor.getFloorNum() + " 楼，请勿重复创建");
        }
        
        return super.save(floor);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeFloor(Long floorId) {
        // 🛡️ 防孤儿数据：删除楼层前，先看有没有房间
        // 注意：这里我们不允许直接删有房间的楼层，必须先去清空房间。
        Long count = roomMapper.selectCount(new LambdaQueryWrapper<DormRoom>()
                .eq(DormRoom::getFloorId, floorId));
        
        if (count > 0) {
            throw new ServiceException("操作拒绝：该楼层下仍有 " + count + " 个房间，请先删除或清空房间！");
        }
        
        return super.removeById(floorId);
    }
}