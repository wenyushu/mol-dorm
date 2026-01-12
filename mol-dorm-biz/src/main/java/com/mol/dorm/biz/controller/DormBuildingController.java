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
    
    // ==================== 1. 公共查询 ====================
    @Operation(summary = "分页查询楼栋列表")
    @SaCheckLogin // 🔒 登录就能查询
    @GetMapping("/list")
    public R<Page<DormBuilding>> list(Page<DormBuilding> page) {
        return R.ok(buildingService.page(page));
    }
    
    // ==================== 2. 超管操作 (基建) ====================
    
    @Operation(summary = "一键初始化楼栋", description = "建楼+建房+建床，仅限超管")
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 创世级操作，仅限超管
    @PostMapping("/init")
    public R<Void> initBuilding(@RequestBody BuildingInitDto initDto) {
        buildingService.initBuilding(initDto);
        return R.ok(null, "楼栋初始化成功");
    }
    
    @Operation(summary = "删除楼栋", description = "级联删除，仅限超管")
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 创世级操作，仅限超管
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        buildingService.deleteBuilding(id);
        return R.ok();
    }
    
    // ==================== 3. 运维操作 ====================
    @Operation(summary = "修改楼栋信息")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    // 🔒 放开权限，管理员即可
    @PutMapping
    public R<Boolean> update(@RequestBody DormBuilding building) {
        return R.ok(buildingService.updateBuilding(building));
    }
}
