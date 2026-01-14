package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.DormFixedAsset;
import com.mol.dorm.biz.service.DormFixedAssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "固定资产管理")
@RestController
@RequestMapping("/asset")
@RequiredArgsConstructor
public class DormFixedAssetController {
    
    private final DormFixedAssetService assetService;
    
    @Operation(summary = "资产列表查询")
    // 宿管、超管、辅导员可看
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER, RoleConstants.COUNSELOR}, mode = SaMode.OR)
    @GetMapping("/list")
    public R<Page<DormFixedAsset>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String assetName,
            @RequestParam(required = false) Integer category) {
        
        Page<DormFixedAsset> page = new Page<>(pageNum, pageSize);
        return R.ok(assetService.getAssetPage(page, roomId, assetName, category));
    }
    
    @Operation(summary = "登记新资产")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @PostMapping("/add")
    public R<Void> add(@RequestBody DormFixedAsset asset) {
        assetService.addAsset(asset);
        return R.ok();
    }
    
    @Operation(summary = "更新资产信息")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @PutMapping("/edit")
    public R<Void> edit(@RequestBody DormFixedAsset asset) {
        assetService.updateById(asset);
        return R.ok();
    }
    
    @Operation(summary = "删除资产")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        assetService.removeById(id);
        return R.ok();
    }
}