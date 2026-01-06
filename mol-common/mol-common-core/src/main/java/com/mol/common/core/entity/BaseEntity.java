package com.mol.common.core.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类 (BaseEntity)
 * 封装审计字段。
 */
@Getter
@Setter
public class BaseEntity implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 创建人
     * accessMode = READ_ONLY: 告诉 Apifox/Swagger，此字段前端不可改，仅由后端返回
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人", accessMode = Schema.AccessMode.READ_ONLY)
    private String createBy;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createTime;
    
    /**
     * 修改人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "修改人", accessMode = Schema.AccessMode.READ_ONLY)
    private String updateBy;
    
    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "修改时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除标记 (0-正常, 1-已删除)
     * hidden = true: 在 Apifox 的接口文档界面中隐藏此字段，因为前端不需要关心逻辑删除位
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "逻辑删除标记", hidden = true)
    private String delFlag;
}