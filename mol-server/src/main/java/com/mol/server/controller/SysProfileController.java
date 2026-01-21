package com.mol.server.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.map.MapUtil;
import com.mol.common.core.entity.SysAdminUser;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.util.LoginHelper;
import com.mol.common.core.util.R;
import com.mol.server.mapper.SysAdminUserMapper;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 个人信息控制器
 *
 * @author mol
 */
@Tag(name = "个人信息管理")
@RestController
@RequestMapping("/system/user/profile")
@RequiredArgsConstructor
public class SysProfileController {
    
    private final SysAdminUserMapper adminUserMapper;
    private final SysOrdinaryUserMapper ordinaryUserMapper;
    
    /**
     * 获取当前登录用户信息
     * 核心测试点：验证 LoginHelper 是否能正确解析 Token
     */
    @SaCheckLogin // 🟢 只有登录后才能访问
    @Operation(summary = "获取个人信息")
    @GetMapping
    public R<Map<String, Object>> getProfile() {
        // 1. 利用 LoginHelper 获取当前 Token 对应的 UserID 和 UserType
        Long userId = LoginHelper.getUserId();
        String userType = LoginHelper.getUserType();
        
        // 2. 准备返回结果
        Map<String, Object> result = MapUtil.newHashMap();
        result.put("id", userId);
        result.put("type", userType);
        result.put("role", LoginHelper.getRoleKey()); // 获取角色权限字符
        
        // 3. 根据类型去查不同的表
        if ("admin".equals(userType)) {
            // --- A. 管理员 ---
            SysAdminUser admin = adminUserMapper.selectById(userId);
            if (admin != null) {
                // 脱敏处理 (不返回密码)
                admin.setPassword(null);
                result.put("userInfo", admin);
                result.put("identity", "管理员/宿管/辅导员");
            }
        } else {
            // --- B. 普通用户 (学生/教工) ---
            // 注意：student 和 staff 都查这张表
            SysOrdinaryUser user = ordinaryUserMapper.selectById(userId);
            if (user != null) {
                user.setPassword(null);
                result.put("userInfo", user);
                // 这里的 user_category 是数据库里的字段 (0:学生, 1:教工)
                result.put("identity", user.getUserCategory() == 1 ? "教职工" : "学生");
            }
        }
        
        return R.ok(result);
    }
}