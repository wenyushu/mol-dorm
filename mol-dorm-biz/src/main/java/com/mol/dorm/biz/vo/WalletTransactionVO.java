package com.mol.dorm.biz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "账单流水视图对象")
public class WalletTransactionVO {
    @Schema(description = "流水号")
    private String trxNo;
    
    @Schema(description = "房间号")
    private String roomNo;
    
    @Schema(description = "业务类型描述")
    private String typeDesc; // 如：电费扣除、充值入账
    
    @Schema(description = "变动金额")
    private BigDecimal amount;
    
    @Schema(description = "动账后余额")
    private BigDecimal postBalance;
    
    @Schema(description = "消费时间")
    private LocalDateTime createTime;
    
    @Schema(description = "备注")
    private String remark;
}