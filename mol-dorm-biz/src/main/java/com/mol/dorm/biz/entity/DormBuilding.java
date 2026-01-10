package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 宿舍楼实体类
 * <p>
 * 对应数据库表：dorm_building
 * 继承 BaseEntity 以自动处理 create_time, update_time 等字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dorm_building")
@Schema(description = "宿舍楼信息对象")
public class DormBuilding extends BaseEntity {
    
    @Schema(description = "楼栋主键 ID")
    @TableId(type = IdType.AUTO) // 数据库自增 ID
    private Long id;
    
    @Schema(description = "所属校区 ID")
    private Long campusId;
    
    @Schema(description = "楼栋名称 (如: 北苑 3 号楼)")
    private String name;
    
    @Schema(description = "楼宇类型: 1-男生楼, 2-女生楼, 3-混合楼")
    private Integer type;
    
    @Schema(description = "总层数")
    private Integer floors;
    
    @Schema(description = "是否有电梯 (true-有, false-无)")
    private Boolean hasElevator;
    
    @Schema(description = "逻辑删除标志 (0-正常, 1-删除)")
    private String delFlag;
}