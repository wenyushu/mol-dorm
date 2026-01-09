package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 宿舍床位实体类
 * 对应数据库表：dorm_bed
 *
 * @author mol
 */
@Data
@EqualsAndHashCode(callSuper = true) // 生成 equals/hashCode 时包含父类字段
@TableName("dorm_bed")
@Schema(description = "宿舍床位信息")
public class DormBed extends BaseEntity {
    
    @Schema(description = "床位主键 ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    @Schema(description = "所属房间 ID")
    private Long roomId;
    
    @Schema(description = "床位号 (如: 1号床, A床)")
    private String bedNo;
    
    @Schema(description = "占用者ID (为空表示空闲)")
    private Long occupantId;
    
    @Schema(description = "床位状态 (0-正常, 1-报修, 2-停用)")
    private Integer status;
}