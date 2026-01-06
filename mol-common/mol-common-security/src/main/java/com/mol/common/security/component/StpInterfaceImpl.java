package com.mol.common.security.component;

import cn.dev33.satoken.stp.StpInterface;
import com.mol.common.mybatis.mapper.SysUserRoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义权限验证接口扩展
 * 使用 ObjectProvider 解决多模块下 Mapper 注入顺序或找不到 Bean 的问题
 */
@Slf4j
@Component
public class StpInterfaceImpl implements StpInterface {
    
    private final ObjectProvider<SysUserRoleMapper> sysUserRoleMapperProvider;
    
    // 使用构造红注入，ObjectProvider 可以有效缓解“无法自动装配”的报错
    public StpInterfaceImpl(ObjectProvider<SysUserRoleMapper> sysUserRoleMapperProvider) {
        this.sysUserRoleMapperProvider = sysUserRoleMapperProvider;
        System.out.println("DEBUG >>> StpInterfaceImpl 加载成功！");
    }
    
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 1. 获取 Mapper (如果此时容器中还没有，则返回 null)
        SysUserRoleMapper mapper = sysUserRoleMapperProvider.getIfAvailable();
        if (mapper == null) {
            log.error("致命错误：无法从 Spring 容器中获取 SysUserRoleMapper，请检查 @MapperScan 配置！");
            return new ArrayList<>();
        }
        
        try {
            // 2. 解析 loginId (格式为 "userType:userId")
            String[] parts = ((String) loginId).split(":");
            int userType = Integer.parseInt(parts[0]);
            long userId = Long.parseLong(parts[1]);
            
            // 3. 查询角色
            return mapper.getRoleKeys(userId, userType);
        } catch (Exception e) {
            log.error("权限加载异常: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 权限码暂时留空
        return new ArrayList<>();
    }
}