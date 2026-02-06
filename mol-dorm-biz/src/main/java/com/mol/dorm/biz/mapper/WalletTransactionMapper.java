package com.mol.dorm.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.dorm.biz.entity.WalletTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 钱包交易流水 Mapper
 * 🛡️ [财务审计准则]：
 * 1. 禁止物理删除：所有查询必须默认过滤 del_flag = '0'。
 * 2. 统计透视：支持按月、按类型聚合，支撑大屏财务看板。
 */
@Mapper
public interface WalletTransactionMapper extends BaseMapper<WalletTransaction> {
    
    /**
     * [财务对账]：查询指定房间在特定时间段内的收支汇总
     */
    Map<String, Object> selectRoomFinancialSummary(@Param("roomId") Long roomId,
                                                   @Param("beginTime") String beginTime,
                                                   @Param("endTime") String endTime);
    
    /**
     * [防刁民审计]：检测是否存在重复的业务单号入账
     */
    int countByBizNo(@Param("bizNo") String bizNo, @Param("roomId") Long roomId);
    
    /**
     * [大屏联动]：查询全校能耗排行 (水费+电费)
     */
    List<Map<String, Object>> selectTopEnergyConsumingRooms(@Param("limit") Integer limit);
    
    /**
     * 🏢 [楼栋财务报表]：按楼栋维度统计收支
     * 🚨 【核心修正点】返回值必须是 Map<String, Object>
     * 理由：MyBatis 聚合查询结果由 Service 层通过 toString() 转换为 BigDecimal 才是最稳健的，
     * 这样可以规避泛型不协变导致的编译报错。
     */
    Map<String, Object> selectBuildingBillSummary(
            @Param("buildingId") Long buildingId,
            @Param("month") String month // 格式: 2026-02
    );
}