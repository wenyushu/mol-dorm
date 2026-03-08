package com.mol.common.core.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 路由动态菜单响应对象 (对接 Pure Admin 协议)
 * <p>
 * 防刁民设计：
 * 1. 使用 @JsonInclude(Include.NON_EMPTY)：
 * 当 children 为空或字段为 null 时，不返回给前端。
 * 理由：防止前端递归空数组导致渲染死循环，同时减少网络传输 payload，增加黑客逆向成本。
 * </p>
 *
 * @author mol
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "动态路由响应对象")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RouteVO {
    
    @Schema(description = "路由访问路径 (如: /system/user)", example = "/system/user")
    private String path;
    
    @Schema(description = "路由名称 (需与前端组件 defineOptions 中的 name 严格一致)", example = "SysUser")
    private String name;
    
    @Schema(description = "路由元信息 (前端控制显示的核心)")
    private MetaVO meta;
    
    @Schema(description = "子路由列表 (递归结构)")
    private List<RouteVO> children;
    
    /**
     * 路由元信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "路由元信息")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class MetaVO {
        
        @Schema(description = "菜单标题 (展示在侧边栏的文字)", example = "用户管理")
        private String title;
        
        @Schema(description = "菜单图标 (支持 Iconify 或本地 SVG)", example = "ri:admin-line")
        private String icon;
        
        /**
         * ✨ 备注：此处的 rank 对应后端实体类中的 sortOrder。
         * 保持 rank 命名是为了无缝兼容 Pure Admin 的底层排序逻辑。
         */
        @Schema(description = "前端排序优先级 (数值越小越靠前)", example = "1")
        private Integer rank;
        
        @Schema(description = "按钮级别权限标识集合", example = "['sys:user:add', 'sys:user:edit']")
        private List<String> auths;
        
        @Schema(description = "是否隐藏菜单 (0:显示, 1:隐藏)", example = "false")
        private Boolean showLink;
    }
}