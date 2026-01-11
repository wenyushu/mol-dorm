package com.mol.common.core.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体通用父类 (审计字段)
 * <p>
 * 包含: 创建/更新时间、创建/更新人、逻辑删除、备注
 * JDK 17 兼容
 * </p>
 *
 * @author mol
 */
@Data
public class BaseEntity implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "创建者")
    @TableField(fill = FieldFill.INSERT)
    private String createBy;
    
    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    @Schema(description = "更新者")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
    
    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
    
    @Schema(description = "备注")
    private String remark;
    
    @Schema(description = "逻辑删除 (0:未删 1:已删)")
    @TableLogic(value = "0", delval = "1") // 配合 yml 配置，默认不做物理删除
    @TableField(select = false) // 查询时通常不需要把这个字段查出来显示
    private String delFlag;
}