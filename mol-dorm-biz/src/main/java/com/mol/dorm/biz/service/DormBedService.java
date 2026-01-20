package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.DormBed;

/**
 * 宿舍床位服务接口
 */
public interface DormBedService extends IService<DormBed> {
    
    /**
     * 分配床位 (入住)
     * @param bedId 床位 ID
     * @param userId 用户 ID
     * @param userType 用户类型 (0-普通用户 1-管理员)
     */
    void assignBed(Long bedId, Long userId, Integer userType);
    
    /**
     * 释放床位 (退宿)
     * @param bedId 床位 ID
     */
    void releaseBed(Long bedId);
    
    /**
     * 获取床位详情
     */
    DormBed getBedDetail(Long bedId);
}