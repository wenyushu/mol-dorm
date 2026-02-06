package com.mol.dorm.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.dorm.biz.entity.RepairOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 报修工单持久层 - 财务资产联动 & 高并发优化版
 * 🛡️ [设计规范]：
 * 1. 原子操作：利用 SQL 状态机实现接单。
 * 2. 穿透统计：跨表聚合房间、建筑、用户及财务流水数据。
 */
@Mapper
public interface RepairOrderMapper extends BaseMapper<RepairOrder> {
    
    /**
     * 🛡️ [防重复报修校验]
     * 判定同一资产是否已在“待指派、维修中、挂起”这三个生命周期内。
     */
    Long checkAssetUnderRepair(@Param("assetCode") String assetCode);
    
    /**
     * 📊 [大屏看板]：报修大户楼栋排行 (按单量)
     */
    List<Map<String, Object>> selectTopRepairBuildings(@Param("limit") Integer limit);
    
    /**
     * 🚨 [时效催办]：查询超过 24 小时未指派的工单
     */
    List<RepairOrder> selectUrgentOrders();
    
    /**
     * 👤 [工效统计]：统计维修工的结案总数与平均评分
     */
    Map<String, Object> selectRepairmanStats(@Param("repairmanId") Long repairmanId);
    
    /**
     * 🧩 [全要素穿透查询]：获取工单详情 (联动建筑、房间、用户、资产、财务流水)
     */
    Map<String, Object> selectOrderWithDetail(@Param("orderId") Long orderId);
    
    /**
     * ⚡ [高并发原子抢单核心]
     * 🛡️ 为什么不先 Select 后 Update？
     * 在高并发点击时，Select 到 Status=0 的线程可能有多个。
     * 直接使用 UPDATE ... WHERE status = 0 可以在数据库行锁层面确保“只有一个师傅能赢”。
     * * @return 返回 1 表示抢单成功；返回 0 表示已被别人抢走或工单已失效。
     */
    int takeOrder(@Param("orderId") Long orderId, @Param("repairmanId") Long repairmanId);
}