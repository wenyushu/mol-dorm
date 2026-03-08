package com.mol.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 菜单权限表 - 实体类
 * 防刁民设计：采用逻辑删除，避免误删导致系统路由崩溃
 */
@Data
@TableName("sys_menu")
@Schema(description = "菜单权限实体")
public class SysMenu {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "父级ID (0为顶级)")
    private Long parentId;
    
    @Schema(description = "菜单标题")
    private String title;
    
    @Schema(description = "路由名称 (需与前端组件name对应)")
    private String name;
    
    @Schema(description = "路由路径")
    private String path;
    
    @Schema(description = "组件路径")
    private String component;
    
    @Schema(description = "图标")
    private String icon;
    
    @Schema(description = "排序 (越小越靠前)")
    private Integer sortOrder;
    
    @Schema(description = "菜单类型 (0-目录 1-菜单 2-按钮)")
    private Integer menuType;
    
    @Schema(description = "权限标识 (如 sys:user:add)")
    private String auths;
    
    @Schema(description = "备注")
    private String remark;
    
    /** 审计字段 - 自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /** 逻辑删除 (0-存在 1-已删除) - 防刁民：物理删除不可恢复 */
    @TableLogic
    private Integer delFlag;
}