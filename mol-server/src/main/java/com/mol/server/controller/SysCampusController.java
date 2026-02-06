package com.mol.server.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.server.entity.SysCampus;
import com.mol.server.service.SysCampusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 校区管理控制器
 */
@Tag(name = "校区管理", description = "管理学校的各个校区信息")
@RestController
@RequestMapping("/sys/campus") // 建议统一加 /sys 前缀
@RequiredArgsConstructor
public class SysCampusController {
    
    private final SysCampusService campusService;
    
    // ❌ 移除：ApplicationEventPublisher 不需要在这里注入了
    // 因为我们在 ServiceImpl 里已经注入并使用了它
    
    @SaCheckLogin
    @Operation(summary = "获取所有校区")
    @GetMapping("/list")
    public R<List<SysCampus>> list() {
        return R.ok(campusService.list(new LambdaQueryWrapper<SysCampus>()
                .orderByAsc(SysCampus::getId))); // 通常ID正序或自定义Sort字段
    }
    
    @SaCheckLogin
    @Operation(summary = "分页查询校区")
    @GetMapping("/page")
    public R<IPage<SysCampus>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(campusService.page(new Page<>(pageNum, pageSize)));
    }
    
    @SaCheckLogin
    @Operation(summary = "根据 ID 获取详情")
    @GetMapping("/{id}")
    public R<SysCampus> getInfo(@PathVariable Long id) {
        return R.ok(campusService.getById(id));
    }
    
    // ==========================================================
    // 🟢 修正点 1：新增接口
    // ==========================================================
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @Operation(summary = "新增校区")
    @PostMapping
    public R<Boolean> save(@RequestBody @Validated SysCampus campus) {
        // 以前叫 addCampus，现在统一用标准方法 save
        // ServiceImpl 里已经重写了 save 方法，包含了查重逻辑
        return R.ok(campusService.save(campus));
    }
    
    // ==========================================================
    // 🟢 修正点 2：修改接口
    // ==========================================================
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @Operation(summary = "修改校区")
    @PutMapping
    public R<Boolean> update(@RequestBody SysCampus campus) {
        // 以前叫 updateCampus，现在统一用 standard updateById
        return R.ok(campusService.updateById(campus));
    }
    
    // ==========================================================
    // 🟢 修正点 3：删除接口
    // ==========================================================
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @Operation(summary = "删除校区")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        // 1. 这里不需要再 publishEvent 了！
        // 因为调用 campusService.removeById(id) 时，
        // Service 内部会自动触发 CampusDeleteEvent 事件并通知 Dorm 模块。
        
        boolean result = campusService.removeById(id);
        
        if (result) {
            return R.ok();
        } else {
            return R.fail("删除失败：ID 不存在");
        }
    }
}