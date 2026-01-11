package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.bto.BuildingInitDto;
import com.mol.dorm.biz.entity.DormBuilding;
import com.mol.dorm.biz.service.DormBuildingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "楼栋管理")
@RestController
@RequestMapping("/building")
@RequiredArgsConstructor
public class DormBuildingController {
    
    private final DormBuildingService buildingService;
    
    // ==================== 1. 公共查询 (所有人可查) ====================
    @SaCheckLogin // 🔒 需要登录
    @Operation(summary = "分页查询楼栋列表")
    @GetMapping("/list")
    public R<Page<DormBuilding>> list(Page<DormBuilding> page, DormBuilding building) {
        // Service 中可自行实现 page 查询，此处略
        return R.ok(buildingService.page(page));
    }
    
    @SaCheckLogin // 🔒 需要登录
    @Operation(summary = "获取楼栋详情")
    @GetMapping("/{id}")
    public R<DormBuilding> getInfo(@PathVariable Long id) {
        return R.ok(buildingService.getById(id));
    }
    
    
    // ==================== 2. 高危操作 (仅 Super Admin) ====================
    
    @Operation(summary = "一键初始化楼栋", description = "建楼+建房+建床，仅限超管")
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 只有超管能初始化
    @PostMapping("/init")
    public R<Void> initBuilding(@RequestBody BuildingInitDto initDto) {
        buildingService.initBuilding(initDto);
        return R.ok(null, "楼栋初始化成功");
    }
    
    
    @Operation(summary = "新增楼栋 (手动)", description = "仅限超管")
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 只有超管能建楼
    @PostMapping
    public R<Boolean> save(@RequestBody DormBuilding building) {
        return R.ok(buildingService.saveBuilding(building));
    }
    
    
    @Operation(summary = "删除楼栋", description = "级联删除，仅限超管")
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 只有超管能拆楼
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        buildingService.deleteBuilding(id);
        return R.ok();
    }
    
    // ==================== 3. 运维操作 (Admin + 宿管 + 辅导员) ====================
    
    @Operation(summary = "修改楼栋信息", description = "宿管可修改状态(封楼)、名称等")
    @SaCheckRole(value = {
            RoleConstants.SUPER_ADMIN,
            RoleConstants.DORM_MANAGER,
            RoleConstants.COUNSELOR
    }, mode = SaMode.OR) // 🔓 只要有其中一个角色即可
    @PutMapping
    public R<Boolean> update(@RequestBody DormBuilding building) {
        return R.ok(buildingService.updateBuilding(building));
    }
}