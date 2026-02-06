package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 房间统一钱包实体 - 财务审计增强版
 * 🛡️ [金融级保障]：
 * 1. 乐观锁：通过 @Version 字段防止高并发下的金额覆盖风险。
 * 2. 动账审计：通过继承 BaseEntity，自动记录每次变更的操作人和时间。
 * 3. 计费追踪：新增 lastBillingTime 字段，防止月度账单重复生成。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("dorm_room_wallet")
@Schema(description = "房间统一钱包 - 财务核心表")
public class DormRoomWallet extends BaseEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "房间 ID")
    private Long roomId;
    
    @Schema(description = "账户实时余额 (元)")
    private BigDecimal balance;
    
    @Schema(description = "累计充值金额 (只增不减，用于用户画像统计)")
    private BigDecimal totalRecharge;
    
    @Schema(description = "累计消费金额 (只增不减)")
    private BigDecimal totalConsume;
    
    /**
     * 🚦 状态标准：
     * 1-正常运营
     * 2-欠费冻结 (余额 < 0 自动触发)
     * 3-行政停用 (如整栋楼清空)
     */
    @Schema(description = "状态: 1正常, 2欠费冻结, 3停用")
    private Integer status;
    
    /**
     * 🌙 计费锚点：
     * 每次执行“模拟月度账单”后更新此时间。
     * 作用：防止定时任务因为重启或异常触发重复扣费。
     */
    @Schema(description = "上次月度账单计费时间")
    private LocalDateTime lastBillingTime;
    
    /**
     * 🛡️ 乐观锁：
     * MyBatis-Plus 插件会自动处理。
     * 逻辑：UPDATE ... SET balance = balance - 10, version = version + 1 WHERE id = 1 AND version = 5
     */
    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer version;
}