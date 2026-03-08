package com.mol.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.common.core.vo.RouteVO;
import com.mol.server.entity.SysMenu;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {
    /** 获取当前用户的异步路由树 */
    List<RouteVO> getAsyncRoutes(Long userId);
}
