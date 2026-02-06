package com.mol.dorm.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.dorm.biz.entity.RechargeLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper
public interface RechargeLogMapper extends BaseMapper<RechargeLog> {
    // 基础 CRUD 即可，核心逻辑在 Service 层的原子操作
    
    /**
     * 财务统计：指定时间内充值成功的总金额
     */
    @Select("SELECT SUM(amount) FROM biz_recharge_log " +
            "WHERE status = 1 " +
            "AND create_time BETWEEN #{begin} AND #{end}")
    BigDecimal sumRechargeAmount(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);
}