package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.DormFloor;

import java.util.List;

/**
 * 楼层管理服务接口
 * 🛡️ [防刁民设计]：
 * 1. 性别一致性：强制校验楼层性别是否符合所属楼栋的全局性别限制。
 * 2. 状态级联：提供整层封闭/开启的快速熔断接口。
 */
public interface DormFloorService extends IService<DormFloor> {
    
    /**
     * 安全保存/修改楼层
     * @param floor 楼层实体
     */
    void saveFloorStrict(DormFloor floor);
    
    /**
     * 物理熔断：整层封闭/装修切换
     * @param floorId 楼层ID
     * @param status 目标状态 (20-正常, 40-整层装修, 0-停止)
     */
    void updateFloorStatus(Long floorId, Integer status);
    
    /**
     * 物理清理：安全删除楼层
     * @param floorId 楼层ID
     */
    void removeFloorStrict(Long floorId);
    
    /**
     * 获取指定楼栋下的所有楼层
     */
    List<DormFloor> getByBuilding(Long buildingId);
}