package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R; // 🟢 引用真理 R 类
import com.mol.dorm.biz.entity.DormFloor;
import com.mol.dorm.biz.service.DormFloorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 楼层层级资源控制器
 * 🛡️ [防刁民策略]：
 * 1. 性别硬熔断：禁止在运行中的楼层随意切换性别。
 * 2. 物理保护：楼层的删除操作仅限超级管理员，防止因楼层断层导致的树形资源崩溃。
 */
@Tag(name = "楼层管理")
@RestController
@RequestMapping("/dorm/floor")
@RequiredArgsConstructor
public class DormFloorController {
    
    private final DormFloorService floorService;
    
    // =================================================================================
    // 1. 核心资源维护 (限 SUPER_ADMIN / DEPT_ADMIN)
    // =================================================================================
    
    @Operation(summary = "严格保存/修改楼层", description = "调整性别前会强制执行人员在住审计。")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DEPT_ADMIN}, mode = SaMode.OR)
    @PostMapping("/save-strict")
    public R<Void> saveFloorStrict(@RequestBody DormFloor floor) {
        floorService.saveFloorStrict(floor);
        return R.okMsg("楼层配置已安全更新");
    }
    
    @Operation(summary = "安全物理清理楼层")
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @DeleteMapping("/{id}")
    public R<Void> removeFloorStrict(@PathVariable Long id) {
        floorService.removeFloorStrict(id);
        return R.okMsg("楼层资源已彻底从物理架构中移除");
    }
    
    // =================================================================================
    // 2. 运维状态管理 (限管理员级别)
    // =================================================================================
    
    @Operation(summary = "切换楼层生命周期状态")
    @SaCheckRole(value = {
            RoleConstants.SUPER_ADMIN,
            RoleConstants.DEPT_ADMIN,
            RoleConstants.DORM_MANAGER
    }, mode = SaMode.OR)
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(
            @Parameter(name = "id", description = "楼层主键ID", example = "1001")
            @PathVariable Long id,
            @Parameter(name = "status", description = "状态码: 20-正常, 40-装修, 0-停用", example = "40")
            @RequestParam Integer status) {
        
        floorService.updateFloorStatus(id, status);
        return R.okMsg("楼层状态已成功切换为: " + status);
    }
    
    // =================================================================================
    // 3. 资源检索 (登录即可访问)
    // =================================================================================
    
    @Operation(summary = "查询楼栋关联楼层")
    @GetMapping("/list-by-building")
    public R<List<DormFloor>> listByBuilding(
            @Parameter(name = "buildingId", description = "所属楼栋ID", example = "50")
            @RequestParam Long buildingId) {
        return R.ok(floorService.getByBuilding(buildingId));
    }
    
    @Operation(summary = "获取楼层详情档案")
    @GetMapping("/{id}")
    public R<DormFloor> getInfo(@PathVariable Long id) {
        return R.ok(floorService.getById(id));
    }
}