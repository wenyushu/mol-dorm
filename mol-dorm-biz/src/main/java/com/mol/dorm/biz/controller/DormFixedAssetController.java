package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.constant.DormConstants; // 🟢 引入标准化状态码
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.DormFixedAsset;
import com.mol.dorm.biz.service.DormFixedAssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 固定资产管理控制器
 * 🛡️ [防刁民策略]：
 * 1. 状态对齐：资产状态必须符合 DormConstants 定义（20正常, 50维修, 60损坏）。
 * 2. 录入拦截：通过 saveAssetStrict 强制校验 AssetCode 唯一性。
 * 3. 联动审计：修改或删除资产后，自动触发房间 evaluateRoomSafety 审计。
 */
@Tag(name = "固定资产管理")
@RestController
@RequestMapping("/dorm/asset") // 建议路径对齐模块名
@RequiredArgsConstructor
public class DormFixedAssetController {
    
    private final DormFixedAssetService assetService;
    
    /**
     * 资产列表查询
     */
    @Operation(summary = "资产列表查询")
    @SaCheckRole(value = {
            RoleConstants.SUPER_ADMIN,
            RoleConstants.DORM_MANAGER,
            RoleConstants.COUNSELOR
    }, mode = SaMode.OR)
    @GetMapping("/list")
    public R<Page<DormFixedAsset>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String assetName,
            @RequestParam(required = false) Integer category) {
        
        Page<DormFixedAsset> page = new Page<>(pageNum, pageSize);
        // 调用 Service 层的分页查询，内部包含对房间、名称的模糊匹配
        return R.ok(assetService.getAssetPage(page, roomId, assetName, category));
    }
    
    /**
     * 登记新资产
     * 🛡️ [防刁民点]：必须使用 saveAssetStrict，防止重复 AssetCode 导致的报修错乱。
     */
    @Operation(summary = "登记新资产")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @PostMapping("/add")
    public R<Void> add(@RequestBody DormFixedAsset asset) {
        // 默认设置为正常使用状态 (20)
        asset.setStatus(DormConstants.LC_NORMAL);
        assetService.saveAssetStrict(asset);
        return R.okMsg("资产登记成功并已初始化为正常状态");
    }
    
    /**
     * 更新资产信息
     * 🛡️ [防刁民点]：如果资产正在维修中(50)，部分核心字段应禁止修改。
     */
    @Operation(summary = "更新资产信息")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @PutMapping("/edit")
    public R<Void> edit(@RequestBody DormFixedAsset asset) {
        // 使用严格更新逻辑，更新后自动触发房间可用性评估
        assetService.updateAssetStatus(asset.getId(), asset.getStatus());
        assetService.updateById(asset);
        return R.okMsg("资产信息已同步，所属房间状态已重新审计");
    }
    
    /**
     * 资产状态切换 (专门用于手动标记损坏)
     * 🟢 场景：宿管巡检发现凳子坏了，手动切为 60-已损坏
     */
    @Operation(summary = "资产状态手动切换")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @PutMapping("/{id}/status")
    public R<Void> changeStatus(
            @PathVariable Long id,
            @Parameter(description = "状态码: 20正常, 50维修, 60损坏") @RequestParam Integer status) {
        assetService.updateAssetStatus(id, status);
        return R.okMsg("资产状态已变更为: " + status);
    }
    
    /**
     * 删除资产
     * 🛡️ [防刁民点]：严禁删除正在“维修中”的资产，防止工单变成孤儿数据。
     */
    @Operation(summary = "删除资产")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        DormFixedAsset asset = assetService.getById(id);
        if (asset != null && asset.getStatus() == DormConstants.LC_REPAIRING) {
            return R.failMsg("操作拦截：该资产处于报修维护中，禁止删除！");
        }
        assetService.removeById(id);
        // 删除后触发房间审计
        if (asset != null) {
            assetService.auditRoomAvailability(asset.getRoomId());
        }
        return R.okMsg("资产移除成功，房间资源已校准");
    }
}