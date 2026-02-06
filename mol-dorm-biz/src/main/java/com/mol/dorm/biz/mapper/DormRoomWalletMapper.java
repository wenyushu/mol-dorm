package com.mol.dorm.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.dorm.biz.entity.DormRoomWallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 房间钱包 Mapper 接口
 * * 对应表: dorm_room_wallet
 */
@Mapper
public interface DormRoomWalletMapper extends BaseMapper<DormRoomWallet> {
    
    /**
     * 🟢 [财务预警]：查询指定楼栋内欠费房间清单
     * 关联房间表，获取房间号与具体余额
     */
    @Select("SELECT r.room_no as roomNo, w.balance, w.status " +
            "FROM dorm_room_wallet w " +
            "INNER JOIN dorm_room r ON w.room_id = r.id " +
            "WHERE r.building_id = #{buildingId} AND w.balance < 0")
    List<Map<String, Object>> selectArrearsRoomsByBuilding(@Param("buildingId") Long buildingId);
    
    /** * 原子扣款 (保留你原来的金融级设计)
     */
    @Update("UPDATE dorm_room_wallet SET balance = balance - #{amount}, " +
            "total_consume = total_consume + #{amount}, " +
            "status = CASE WHEN (balance - #{amount}) < 0 THEN 2 ELSE status END " +
            "WHERE room_id = #{roomId} AND balance >= #{amount} AND status != 3")
    int deductBalance(@Param("roomId") Long roomId, @Param("amount") BigDecimal amount);
    
    /**
     * 原子充值 (保留你原来的自愈逻辑)
     */
    @Update("UPDATE dorm_room_wallet SET " +
            "balance = balance + #{amount}, " +
            "total_recharge = total_recharge + #{amount}, " +
            "status = CASE WHEN (balance + #{amount}) >= 0 AND status = 2 THEN 1 ELSE status END " +
            "WHERE room_id = #{roomId} AND status != 3")
    int rechargeBalance(@Param("roomId") Long roomId, @Param("amount") BigDecimal amount);
}