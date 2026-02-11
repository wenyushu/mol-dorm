package com.mol.dorm.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.dorm.biz.entity.RepairOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 报修工单持久层 - 数字化运维核心
 */
@Mapper
public interface RepairOrderMapper extends BaseMapper<RepairOrder> {
    
    /**
     * 🛡️ [防重复报修校验]
     */
    Long checkAssetUnderRepair(@Param("assetCode") String assetCode);
    
    /**
     * 🤖 [自动调度算法核心]：查找负载最低的师傅
     * 逻辑：筛选拥有“维修师傅”角色的账户，统计其“处理中(1)”和“挂起(5)”的单量，按单量升序排列。
     * @return 师傅ID列表（首位即是最闲师傅）
     */
    List<Long> selectRepairmanByTaskCount();
    
    /**
     * ⚡ [高并发原子抢单核心]
     */
    int takeOrder(@Param("orderId") Long orderId, @Param("repairmanId") Long repairmanId);
    
    /**
     * 📊 [大屏看板]：报修排行
     */
    List<Map<String, Object>> selectTopRepairBuildings(@Param("limit") Integer limit);
    
    /**
     * 🚨 [时效预警]：查询 24h 未响应工单
     */
    List<RepairOrder> selectUrgentOrders();
    
    /**
     * 👤 [工效统计]：统计结案数与评分
     */
    Map<String, Object> selectRepairmanStats(@Param("repairmanId") Long repairmanId);
    
    /**
     * 🧩 [穿透详情查询]
     */
    Map<String, Object> selectOrderWithDetail(@Param("orderId") Long orderId);
}