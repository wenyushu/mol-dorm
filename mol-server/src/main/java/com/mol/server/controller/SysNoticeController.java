package com.mol.server.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.server.entity.SysNotice;
import com.mol.server.service.SysNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "系统公告管理")
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class SysNoticeController {
    
    private final SysNoticeService noticeService;
    
    @Operation(summary = "发布公告 (仅限管理人员)")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @PostMapping("/add")
    public R<String> add(@RequestBody SysNotice notice) {
        // 🛡️ [身份存证]：利用 StpUtil 获取登录名
        notice.setCreateBy(StpUtil.getLoginIdAsString());
        // 建议从 User 缓存中获取 realName 填入 publisherName，此处暂略
        
        noticeService.publish(notice);
        return R.okMsg("公告已正式面向全校发布");
    }
    
    @Operation(summary = "撤回/作废公告")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @DeleteMapping("/{id}")
    public R<String> remove(@PathVariable Long id) {
        noticeService.withdraw(id);
        return R.okMsg("公告已进入撤回库，学生端已同步隐藏");
    }
    
    @Operation(summary = "公告列表 (学生/首页端)")
    @SaCheckLogin
    @GetMapping("/public/list")
    public R<Page<SysNotice>> publicList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Page<SysNotice> page = new Page<>(pageNum, pageSize);
        // onlyActive = true 保证数据安全性
        return R.ok(noticeService.getNoticePage(page, null, null, true));
    }
    
    @Operation(summary = "大屏重要通知穿透", description = "获取前 3 条紧急公告")
    @GetMapping("/public/important")
    public R<List<SysNotice>> getImportant() {
        Page<SysNotice> page = new Page<>(1, 3);
        // 排序逻辑已在 Service 层处理：Level 1 的重要公告会排在最前
        return R.ok(noticeService.getNoticePage(page, null, null, true).getRecords());
    }
}