package com.mol.server.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.server.entity.SysCampus;
import com.mol.server.event.CampusDeleteEvent;
import com.mol.server.service.SysCampusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 校区管理控制器
 * 提供校区的增删改查功能
 * <p>
 * 🛡️ 架构升级：在此处协调 Server 模块和 Dorm 模块，解决级联删除校验问题。
 */
@Tag(name = "校区管理", description = "管理学校的各个校区信息")
@RestController
@RequestMapping("/campus")
@RequiredArgsConstructor
public class SysCampusController {
    
    private final SysCampusService campusService;
    
    // ✅ 注入事件发布器 (Spring 自带)
    private final ApplicationEventPublisher eventPublisher;
    
    @SaCheckLogin
    @Operation(summary = "获取所有校区", description = "查询系统中的所有校区列表（不分页）")
    @GetMapping("/list")
    public R<List<SysCampus>> list() {
        // 使用 LambdaQueryWrapper 稍微排个序，比如按ID倒序
        return R.ok(campusService.list(new LambdaQueryWrapper<SysCampus>()
                .orderByDesc(SysCampus::getId)));
    }
    
    @SaCheckLogin
    @Operation(summary = "分页查询校区")
    @GetMapping("/page")
    public R<IPage<SysCampus>> page(
            @Parameter(description = "页码", example = "1")
            @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        
        return R.ok(campusService.page(new Page<>(pageNum, pageSize)));
    }
    
    @SaCheckLogin
    @Operation(summary = "根据 ID 获取详情")
    @GetMapping("/{id}")
    public R<SysCampus> getInfo(@PathVariable Long id) {
        return R.ok(campusService.getById(id));
    }
    
    // 🔒 权限锁：只有超级管理员能执行
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @Operation(summary = "新增校区")
    @PostMapping
    public R<Boolean> save(@RequestBody SysCampus campus) {
        // 🟢 切换：使用我们重写的 addCampus (带编码查重)
        return R.ok(campusService.addCampus(campus));
    }
    
    // 🔒 权限锁：只有超级管理员能执行
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @Operation(summary = "修改校区")
    @PutMapping
    public R<Boolean> update(@RequestBody SysCampus campus) {
        // 🟢 切换：使用我们重写的 updateCampus (带编码查重)
        return R.ok(campusService.updateCampus(campus));
    }
    
    // 🔒 权限锁：只有超级管理员能执行
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @Operation(summary = "删除校区", description = "删除前会自动校验该校区下是否存在楼栋")
    @DeleteMapping("/{id}")
    public R<Boolean> remove(@PathVariable Long id) {
        // =================================================
        // 🛡️ 级联检查 (防刁民核心防线)
        // =================================================

        // 1. 发布“准备删除”事件
        // 如果 Dorm 模块监听到这个校区还有楼，它会直接抛出异常，打断后续代码
        eventPublisher.publishEvent(new CampusDeleteEvent(this, id));
        
        // 2. 如果上面没报错，说明校验通过，执行删除
        return R.ok(campusService.removeCampus(id));
    }
}