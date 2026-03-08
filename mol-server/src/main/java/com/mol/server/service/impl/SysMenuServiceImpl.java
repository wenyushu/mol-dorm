package com.mol.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.vo.RouteVO;
import com.mol.server.entity.SysMenu;
import com.mol.server.mapper.SysMenuMapper;
import com.mol.server.service.SysMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 系统菜单服务实现类
 * <p>
 * 核心升级：
 * 1. 强化了递归过程中的“防崩坏”过滤，确保不会向前端发送包含 null 的路由数组。
 * 2. 严格校验 path 和 name 字段，防止前端排序函数解构失败导致白屏。
 * </p>
 */
@Slf4j
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {
    
    /**
     * 获取用户动态路由菜单
     * @param userId 当前登录用户ID
     * @return 树形路由列表
     */
    @Override
    public List<RouteVO> getAsyncRoutes(Long userId) {
        log.info("🚀 正在为用户 [{}] 构建路由树...", userId);
        
        // 1. 查询所有待显示的目录(0)和菜单(1)
        // 🛡️ 防刁民：只查未删除的数据，且按 sort_order 升序
        List<SysMenu> allMenus = this.list(new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getMenuType, 0, 1)
                .orderByAsc(SysMenu::getSortOrder));
        
        if (allMenus == null || allMenus.isEmpty()) {
            log.warn("⚠️ 数据库 sys_menu 表中没有有效的菜单配置");
            return Collections.emptyList();
        }
        
        // 2. 执行递归构建（从父ID为 0 的顶级节点开始）
        return buildTree(allMenus, 0L);
    }
    
    /**
     * 递归构建路由树逻辑
     * @param menus    数据库原始扁平集合
     * @param parentId 当前父节点ID
     * @return 过滤后的树形 JSON
     */
    private List<RouteVO> buildTree(List<SysMenu> menus, Long parentId) {
        return menus.stream()
                // 筛选属于当前父节点的子项
                .filter(m -> Objects.equals(m.getParentId(), parentId))
                .map(m -> {
                    // 🛡️ 防崩坏设计：如果关键字段缺失，则放弃此节点，避免前端解构 name 报错
                    if (!StringUtils.hasText(m.getPath()) || !StringUtils.hasText(m.getName())) {
                        log.error("❌ 菜单数据配置不全，已自动忽略：ID={}, Title={}", m.getId(), m.getTitle());
                        return null;
                    }
                    
                    // 开始适配前端协议
                    RouteVO vo = new RouteVO();
                    vo.setPath(m.getPath());
                    vo.setName(m.getName());
                    
                    // 映射 Meta 信息
                    RouteVO.MetaVO meta = RouteVO.MetaVO.builder()
                            .title(m.getTitle())
                            .icon(m.getIcon())
                            // 数据库字段 sortOrder -> 前端 rank
                            .rank(m.getSortOrder() != null ? m.getSortOrder() : 99)
                            // 权限码处理
                            .auths(StringUtils.hasText(m.getAuths()) ?
                                    Arrays.asList(m.getAuths().split(",")) : null)
                            .showLink(true)
                            .build();
                    
                    vo.setMeta(meta);
                    
                    // 递归查找子节点
                    List<RouteVO> children = buildTree(menus, m.getId());
                    if (children != null && !children.isEmpty()) {
                        vo.setChildren(children);
                    }
                    
                    return vo;
                })
                // ✨ 核心保护：过滤掉所有 null 节点，防止返回 [null, null] 的数组
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}