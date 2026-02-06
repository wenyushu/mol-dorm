package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 钱包交易流水表 - 财务审计核心
 * 🛡️ [防刁民审计]：记录每一分钱的去向，支持水电费、维修费、充值等全业务溯源。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("biz_wallet_transaction")
@Schema(description = "钱包交易流水")
public class WalletTransaction extends BaseEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "流水单号 (全局唯一)")
    private String trxNo;
    
    @Schema(description = "房间 ID")
    private Long roomId;
    
    /**
     * 🚦 业务类型：
     * 1-充值入账, 2-电费扣除, 3-水费扣除, 4-维修耗材, 5-退宿清算
     */
    @Schema(description = "业务类型: 1充值, 2电费, 3水费, 4维修, 5清算")
    private Integer trxType;
    
    @Schema(description = "变动金额 (正数为入账，负数为支出)")
    private BigDecimal amount;
    
    @Schema(description = "动账后余额快照")
    private BigDecimal postBalance;
    
    @Schema(description = "关联业务单号 (如报修单号或充值订单号)")
    private String bizNo;
}