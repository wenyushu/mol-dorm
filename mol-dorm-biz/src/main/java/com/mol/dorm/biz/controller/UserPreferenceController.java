package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.UserPreference;
import com.mol.dorm.biz.service.UserPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户画像管理")
@RestController
@RequestMapping("/preference")
@RequiredArgsConstructor
public class UserPreferenceController {
    
    private final UserPreferenceService preferenceService;
    
    @Operation(summary = "获取我的画像")
    @SaCheckRole(RoleConstants.STUDENT)
    @GetMapping("/my")
    public R<UserPreference> getMyInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        UserPreference pref = preferenceService.getById(userId);
        if (pref == null) {
            // 如果还没填过，返回一个空对象，前端识别 ID 为空则显示 "去填写"
            return R.ok(new UserPreference());
        }
        return R.ok(pref);
    }
    
    @Operation(summary = "保存/更新画像")
    @SaCheckRole(RoleConstants.STUDENT)
    @PostMapping("/save")
    public R<Void> save(@RequestBody UserPreference preference) {
        // 强制绑定当前登录用户 ID (防止篡改他人数据)
        Long userId = StpUtil.getLoginIdAsLong();
        preference.setUserId(userId);
        
        preferenceService.saveOrUpdatePreference(preference);
        return R.ok();
    }
}