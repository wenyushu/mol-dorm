package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.RepairOrder;
import java.math.BigDecimal;

/**
 * 报修管理服务接口 - 工业级全周期闭环
 */
public interface RepairOrderService extends IService<RepairOrder> {
    
    // 1. 提交与自愈
    void submitRepair(RepairOrder order);
    
    // 2. 接单与分配
    void startRepair(Long orderId, Long repairmanId);
    
    /**
     * 🟢 [新增] 师傅侧自动巡航引擎：系统自动寻找空闲师傅并指派
     */
    void autoAllocate(Long orderId);
    
    // 3. 完工与财务
    void finishRepair(Long orderId, String comment, Integer rating, BigDecimal materialCost, Boolean isHumanDamage);
    
    // 4. 运维状态流转
    void suspendRepair(Long orderId, String reason);
    
    void rejectRepair(Long orderId, String reason);
    
    /**
     * 🟢 [新增] 学生评价防伪入口
     * @param userId 评价人ID (需校验身份)
     */
    void rateOrder(Long orderId, Integer rating, String comment, Long userId);
    
    /**
     * 🟢 [新增] 角色感知分页查询
     * 逻辑：学生看本人，师傅看名下，管理员看全区
     */
    IPage<RepairOrder> selectOrderPage(Page<RepairOrder> page, RepairOrder query, Long userId);
}