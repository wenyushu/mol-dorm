package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.DormBed;
import java.util.List;

/**
 * 床位管理服务接口
 * 🛡️ [防刁民契约]：
 * 1. 物理唯一性：严格禁止一名用户同时出现在两个床位上。
 * 2. 状态强关联：床位的占用/释放必须联动触发房间的饱和度重新计算。
 */
public interface DormBedService extends IService<DormBed> {
    
    /**
     * 安全保存床位
     * 🛡️ 校验全链路冗余 ID 的正确性，防止“悬空床位”。
     */
    void saveBedStrict(DormBed bed);
    
    /**
     * 物理熔断：修改床位生命周期
     * @param bedId 床位ID
     * @param status 目标状态 (20-正常, 50-维修, 80-保留)
     */
    void updateBedStatus(Long bedId, Integer status);
    
    /**
     * 安全删除床位
     */
    void removeBedStrict(Long bedId);
    
    /**
     * 获取房间下的所有床位
     */
    List<DormBed> getByRoom(Long roomId);
    
    /**
     * 【核心】床位人员变更
     * @param id 床位ID
     * @param userId 用户ID (为 null 则退宿)
     * @param userCategory 用户类型
     * @param version 乐观锁版本号 (防并发冲突)
     */
    void updateOccupant(Long id, Long userId, Integer userCategory, Integer version);
    
    /**
     * 兼容：3参数版本 (为了防止其他旧代码报错，可以在接口也留着)
     */
    void updateOccupant(Long bedId, Long userId, Integer userCategory);
}