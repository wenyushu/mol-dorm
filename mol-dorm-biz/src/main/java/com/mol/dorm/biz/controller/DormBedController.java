package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.service.DormBedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "床位管理")
@RestController
@RequestMapping("/bed")
@RequiredArgsConstructor
public class DormBedController {
    
    private final DormBedService bedService;
    
    // ==================== 1. 公共查询 ====================
    // 床位列表通常由 Room 接口带出，这里提供单查或统计接口
    @GetMapping("/{id}")
    public R<DormBed> getInfo(@PathVariable Long id) {
        return R.ok(bedService.getById(id));
    }
    
    // ==================== 2. 物理增删 (仅 Super Admin) ====================
    // 注：床位通常跟随房间自动生成，手动单独加床属于特殊物理变更
    
    @Operation(summary = "新增床位 (物理)", description = "仅限超管")
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @PostMapping
    public R<Boolean> save(@RequestBody DormBed bed) {
        return R.ok(bedService.save(bed));
    }
    
    @Operation(summary = "删除床位 (物理)", description = "仅限超管")
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @DeleteMapping("/{id}")
    public R<Boolean> remove(@PathVariable Long id) {
        return R.ok(bedService.removeById(id));
    }
    
    // ==================== 3. 分配与日常管理 (Admin + 宿管 + 辅导员) ====================
    
    @Operation(summary = "分配床位 (入住)", description = "一线管理人员操作")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER, RoleConstants.COUNSELOR}, mode = SaMode.OR)
    @PostMapping("/assign")
    public R<Void> assignUser(@RequestParam Long bedId, @RequestParam Long userId) {
        bedService.assignUserToBed(bedId, userId);
        return R.ok();
    }
    
    @Operation(summary = "释放床位 (退宿)", description = "一线管理人员操作")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER, RoleConstants.COUNSELOR}, mode = SaMode.OR)
    @PostMapping("/release")
    public R<Void> releaseBed(@RequestParam Long bedId) {
        bedService.releaseBed(bedId);
        return R.ok();
    }
}