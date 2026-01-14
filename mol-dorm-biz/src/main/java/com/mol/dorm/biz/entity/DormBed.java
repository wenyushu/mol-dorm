package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 宿舍床位实体类
 * <p>
 * 对应表: dorm_bed
 * 这是分配系统的最小原子单位。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dorm_bed")
@Schema(description = "宿舍床位")
public class DormBed extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "主键 ID")
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "所属房间 ID")
    private Long roomId;
    
    @Schema(description = "床位标签 (如: 305-1, 305-A)")
    private String bedLabel;
    
    @Schema(description = "当前居住者 ID (空闲则为 NULL)")
    private Long occupantId;
    
    @Schema(description = "床位状态: 1-正常 0-报修/不可用")
    private Integer status;
    
    @Schema(description = "排序/方位 (1-靠窗 2-靠门...)")
    private Integer sortOrder;
    
    @Schema(description = "乐观锁版本号")
    @Version
    private Integer version;
    
    @Schema(description = "逻辑删除标志")
    @TableLogic
    private String delFlag;
}