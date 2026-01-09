package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.DormBed;

/**
 * 宿舍床位业务接口
 */
public interface DormBedService extends IService<DormBed> {
    
    /**
     * 为用户分配床位
     *
     * @param bedId  床位 ID
     * @param userId 用户 ID
     * @throws RuntimeException 如果分配失败（如床位已满）抛出异常
     */
    void assignUserToBed(Long bedId, Long userId);
    
    /**
     * 释放床位（用户退宿）
     *
     * @param bedId 床位 ID
     */
    void releaseBed(Long bedId);
}