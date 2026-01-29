package com.mol.server.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.LoginHelper;
import com.mol.common.core.util.R;
import com.mol.server.enums.ArchiveTypeEnum;
import com.mol.server.service.SysUserArchiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "人员异动/归档管理")
@RestController
@RequestMapping("/system/archive")
@RequiredArgsConstructor
public class SysUserArchiveController {
    
    private final SysUserArchiveService archiveService;
    
    @Operation(summary = "执行人员异动(休学/退学/毕业)")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.COUNSELOR}, mode = SaMode.OR)
    @PostMapping("/execute")
    public R<Void> execute(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "异动类型(10毕业, 40退学, 50病休, 51事休)", required = true) @RequestParam Integer type,
            @Parameter(description = "详细原因", required = true) @RequestParam String reason
    ) {
        // 1. 查找枚举
        ArchiveTypeEnum typeEnum = null;
        for (ArchiveTypeEnum e : ArchiveTypeEnum.values()) {
            if (e.getCode().equals(type)) {
                typeEnum = e;
                break;
            }
        }
        if (typeEnum == null) {
            // 这里要用 fail，因为没有 data
            return R.fail("非法的异动类型代码");
        }
        
        // 2. 获取当前操作人ID (用于审计)
        String operatorName = "Admin-" + LoginHelper.getUserId();
        
        // 3. 执行核心逻辑
        archiveService.executeUserArchive(userId, typeEnum, reason, operatorName);
        
        // 🟢 修复点：调用 R.ok(data, msg)，将 data 设为 null，消息设为自定义字符串
        return R.ok(null, "异动处理成功");
    }
}