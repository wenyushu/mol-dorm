package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 宿舍固定资产实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dorm_fixed_asset")
@Schema(description = "固定资产")
public class DormFixedAsset extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "资产名称 (书桌/空调)")
    private String assetName;
    
    @Schema(description = "资产编号 (唯一标识)")
    private String assetCode;
    
    @Schema(description = "分类: 1-家具 2-电器 3-基建")
    private Integer category;
    
    @Schema(description = "所属房间 ID")
    private Long roomId;
    
    @Schema(description = "价值")
    private BigDecimal price;
    
    @Schema(description = "状态: 1-正常 2-报修中 3-损坏 4-报废")
    private Integer status;
}