package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.DormFloor;

/**
 * 宿舍楼层服务接口
 */
public interface DormFloorService extends IService<DormFloor> {
    
    /**
     * 新增楼层 (带防刁民校验)
     * @param floor 楼层实体
     * @return 是否成功
     */
    boolean saveFloor(DormFloor floor);
    
    /**
     * 删除楼层 (带防孤儿校验)
     * @param floorId 楼层 ID
     * @return 是否成功
     */
    boolean removeFloor(Long floorId);
}