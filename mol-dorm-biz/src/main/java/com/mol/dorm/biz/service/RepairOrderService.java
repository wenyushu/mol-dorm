package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.RepairOrder;

import java.math.BigDecimal;

/**
 * 报修工单服务接口
 * 🛡️ [防刁民逻辑矩阵]：
 * 1. 状态流转：待处理(0) -> 维修中(1) -> 已修复(2) -> 已评价(3) -> 已驳回(4)
 * 2. 物理联动：基于 AssetCode 实现报修即锁定资产，完工即释放资源。
 * 3. 熔断降级：重大故障自动下架房间分配权。
 */
public interface RepairOrderService extends IService<RepairOrder> {
    
    /**
     * 1. 提交报修申请
     * [联动]：自动将关联资产设为 50(维修中)，并触发房间可用性评估。
     */
    void submitRepair(RepairOrder order);
    
    /**
     * 2. 接单/开始维修
     * [逻辑]：维修师傅确认工单，状态由待处理转为维修中。
     */
    void startRepair(Long orderId, Long repairmanId);
    
    /**
     * 3. 完成维修并反馈
     * @param isHumanDamage 是否判定为人为损坏 (决定是否触发自动扣费)
     */
    void finishRepair(Long orderId, String comment, Integer rating, BigDecimal materialCost, Boolean isHumanDamage);
    
    /**
     * 4. 挂起工单 (转为待大修)
     * [场景]：现场核实后发现需寒暑假施工或外协，状态转为 5。
     */
    void suspendRepair(Long orderId, String reason);
    
    /**
     * 5. 驳回工单
     * [场景]：信息错误或非报修范畴，状态转为 4。
     */
    void rejectRepair(Long orderId, String reason);
}