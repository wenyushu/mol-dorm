package com.mol.server.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.mol.common.core.util.R;
import com.mol.server.dto.UpdatePasswordBody;
import com.mol.server.dto.UserProfileBody;
import com.mol.server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "个人中心", description = "修改资料/密码")
@RestController
@RequestMapping("/user/profile")
@RequiredArgsConstructor
public class UserProfileController {
    
    private final UserService userService;
    
    @Operation(summary = "修改个人资料", description = "修改昵称、头像、手机号等")
    @SaCheckLogin // 🔒 必须登录
    @PutMapping
    public R<String> updateProfile(@Validated @RequestBody UserProfileBody body) {
        userService.updateProfile(body);
        return R.ok("修改成功");
    }
    
    @Operation(summary = "修改登录密码")
    @SaCheckLogin // 🔒 必须登录
    @PutMapping("/password")
    public R<String> updatePassword(@Validated @RequestBody UpdatePasswordBody body) {
        userService.updatePassword(body);
        return R.ok("密码修改成功，请重新登录");
    }
}