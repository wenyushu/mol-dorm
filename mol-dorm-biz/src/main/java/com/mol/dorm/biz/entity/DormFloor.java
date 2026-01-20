package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 宿舍楼层实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dorm_floor")
@Schema(description = "宿舍楼层")
public class DormFloor extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键 ID")
    private Long id;
    
    // 🔗 归属：校区 + 楼栋 (全链路冗余，查询飞快)
    @Schema(description = "所属校区 ID (冗余)")
    private Long campusId;
    
    @NotNull(message = "所属楼栋不能为空")
    @Schema(description = "所属楼栋 ID")
    private Long buildingId;
    
    @NotNull(message = "楼层号不能为空")
    @Schema(description = "楼层号 (物理层数)")
    private Integer floorNum;
    
    /**
     * 🛡️ 楼层性别防线:
     * - 若 Building 是男楼，这里必填 1
     * - 若 Building 是女楼，这里必填 2
     * - 若 Building 是混合，这里按需填 1 或 2
     */
    @NotNull(message = "楼层性别限制不能为空")
    @Schema(description = "性别限制: 1-男 2-女 (混合楼需指定每一层的性别)")
    private Integer genderLimit;
    
    /**
     * 1: 启用
     * 0: 停用
     * 41: 装修/封层 (层级维修)
     */
    @NotNull(message = "状态不能为空")
    @Schema(description = "状态: 1-启用 0-停用 41-装修")
    private Integer status;
}