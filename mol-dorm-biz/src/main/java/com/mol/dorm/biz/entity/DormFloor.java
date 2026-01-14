package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 宿舍楼层实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dorm_floor")
@Schema(description = "宿舍楼层")
public class DormFloor extends BaseEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "所属楼栋 ID")
    private Long buildingId;
    
    @Schema(description = "楼层号 (如: 1, 2, 3)")
    private Integer floorNum;
    
    @Schema(description = "性别限制: 0-无 1-男 2-女")
    private Integer genderLimit;
}