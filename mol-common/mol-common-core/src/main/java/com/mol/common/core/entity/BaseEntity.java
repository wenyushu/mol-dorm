package com.mol.common.core.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类
 * <p>所有业务表都应该继承此类，从而自动拥有审计字段</p>
 */
@Data
public class BaseEntity implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 创建者
     * 对应 Handler 里的 "createBy"
     */
    @TableField(fill = FieldFill.INSERT) // 关键：告诉 MP 在插入时调用 Handler
    private String createBy;
    
    /**
     * 创建时间
     * 对应 Handler 里的 "createTime"
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新者
     * 对应 Handler 里的 "updateBy"
     */
    @TableField(fill = FieldFill.INSERT_UPDATE) // 关键：插入和更新时都调用
    private String updateBy;
    
    /**
     * 更新时间
     * 对应 Handler 里的 "updateTime"
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除标志
     * 对应 Handler 里的 "delFlag"
     */
    @TableLogic(value = "0", delval = "2") // 逻辑删除核心注解
    @TableField(fill = FieldFill.INSERT)   // 插入时自动填充为 "0"
    private String delFlag;
    
    /**
     * 备注
     * (这个字段一般不需要自动填充，由前端传过来，所以不用加 fill)
     */
    private String remark;
}