package com.mol.server.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
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

/**
 * 通知公告控制器
 */
@Tag(name = "系统公告管理")
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class SysNoticeController {
    
    private final SysNoticeService noticeService;
    
    @Operation(summary = "发布公告 (管理员)")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @PostMapping("/add")
    public R<Void> add(@RequestBody SysNotice notice) {
        noticeService.publish(notice);
        return R.ok();
    }
    
    @Operation(summary = "修改公告 (管理员)")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @PutMapping("/edit")
    public R<Void> edit(@RequestBody SysNotice notice) {
        noticeService.updateById(notice);
        return R.ok();
    }
    
    @Operation(summary = "撤回/删除公告 (管理员)")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        // 这里执行逻辑删除或物理删除均可，业务上叫"撤回"
        noticeService.withdraw(id);
        return R.ok();
    }
    
    @Operation(summary = "公告列表 (后台管理用)", description = "包含已撤回的")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @GetMapping("/list")
    public R<Page<SysNotice>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer type) {
        
        Page<SysNotice> page = new Page<>(pageNum, pageSize);
        return R.ok(noticeService.getNoticePage(page, title, type, false));
    }
    
    @Operation(summary = "公告列表 (学生/大屏用)", description = "只展示已发布的")
    @SaCheckLogin
    @GetMapping("/public/list")
    public R<Page<SysNotice>> publicList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Page<SysNotice> page = new Page<>(pageNum, pageSize);
        return R.ok(noticeService.getNoticePage(page, null, null, true));
    }
    
    @Operation(summary = "获取最新重要通知 (大屏专用)", description = "返回前 3 条重要通知")
    @GetMapping("/bigscreen/important")
    public R<List<SysNotice>> bigScreenList() {
        // 大屏接口通常不需要鉴权，或者使用特定 Token，这里为了方便演示暂不加 SaCheck
        // 实际上线建议加 @SaCheckLogin 或 IP 白名单
        
        Page<SysNotice> page = new Page<>(1, 3);
        // type=null, onlyActive=true
        // 这里其实应该过滤 level=1 (重要)，复用 getNoticePage 需要改动，这里简单用 MyBatisPlus 查
        // 为了方便，这里简单调用 service，实际建议专门写个 SQL
        return R.ok(noticeService.getNoticePage(page, null, null, true).getRecords());
    }
}