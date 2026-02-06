package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 宿舍固定资产实体
 * 🛡️ [状态对齐版]：严格对齐全系统生命周期标准
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("dorm_fixed_asset")
@Schema(description = "固定资产 - 资源树末端资产节点")
public class DormFixedAsset extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "资产名称 (如: 变频空调、实木书桌)")
    private String assetName;
    
    @Schema(description = "资产编号 (唯一固资条码)")
    private String assetCode;
    
    @Schema(description = "分类: 1-家具 2-电器 3-基建")
    private Integer category;
    
    @Schema(description = "所属房间 ID")
    private Long roomId;
    
    @Schema(description = "资产原值")
    private BigDecimal price;
    
    /**
     * 🚦 状态对齐标准 (生命周期):
     * 20: 正常使用 (NORMAL)
     * 50: 报修中/维修中 (REPAIRING)
     * 60: 已损坏 (DAMAGED) - 指无法修好，等待鉴定
     * 0:  已报废 (STOP) - 指实物已注销/清理出库
     */
    @Schema(description = "状态: 20-正常, 50-维修中, 60-已损坏, 0-已报废")
    private Integer status;
}