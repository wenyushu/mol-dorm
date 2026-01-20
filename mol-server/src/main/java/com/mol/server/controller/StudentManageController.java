package com.mol.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode; // 👈 1. 必须导入这个枚举
import com.mol.common.core.util.R;
import com.mol.server.dto.AdminUpdateStudentBody;
import com.mol.server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "学生管理", description = "仅限管理员使用")
@RestController
@RequestMapping("/system/student")
@RequiredArgsConstructor
public class StudentManageController {
    
    private final UserService userService;
    
    @Operation(summary = "修改学生学籍信息", description = "转专业、换班级等")
    // 🛡️ 防刁民：只有 super_admin 或 teacher 角色才能调用
    @SaCheckRole(value = {"super_admin", "teacher"}, mode = SaMode.OR)
    @PutMapping
    public R<String> updateStudent(@Validated @RequestBody AdminUpdateStudentBody body) {
        userService.updateStudentByAdmin(body);
        return R.ok("学生信息更新成功");
    }
}