package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R; // 🟢 引用真理 R 类
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.service.DormBedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 床位原子资源控制器
 */
@Tag(name = "床位管理")
@RestController
@RequestMapping("/dorm/bed")
@RequiredArgsConstructor
public class DormBedController {
    
    private final DormBedService bedService;
    
    // =================================================================================
    // 1. 核心资源维护 (限 SUPER_ADMIN / DEPT_ADMIN)
    // =================================================================================
    
    @Operation(summary = "严格保存/修改床位", description = "若床位已住人，禁止修改 bedLabel。")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DEPT_ADMIN}, mode = SaMode.OR)
    @PostMapping("/save-strict")
    public R<Void> saveBedStrict(@RequestBody DormBed bed) {
        bedService.saveBedStrict(bed);
        return R.okMsg("床位物理档案同步成功");
    }
    
    @Operation(summary = "安全物理注销床位")
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @DeleteMapping("/{id}")
    public R<Void> removeBedStrict(@PathVariable Long id) {
        bedService.removeBedStrict(id);
        return R.ok();
    }
    
    // =================================================================================
    // 2. 状态与人员控制 (限管理员级别)
    // =================================================================================
    
    @Operation(summary = "切换床位生命周期状态")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DEPT_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        bedService.updateBedStatus(id, status);
        return R.okMsg("床位状态切换成功");
    }
    
    /**
     * 【核心入口】床位人员变更 (入住/退宿)
     * 🛡️ [防并发逻辑]：
     * 引入 version 参数。如果两个管理员同时点击“分配”，只有版本匹配的那个能成功，另一个会报“数据已被他人修改”。
     */
    @Operation(summary = "分配/释放床位人员", description = "办理入住或退宿。强制执行全校一人一床审计。")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DEPT_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @PutMapping("/{id}/occupant")
    public R<String> updateOccupant(
            @Parameter(name = "id", description = "床位主键 ID") @PathVariable Long id,
            @Parameter(name = "userId", description = "用户 ID(传空则执行退宿)") @RequestParam(required = false) Long userId,
            @Parameter(name = "userCategory", description = "用户类型(0学生, 1教工)") @RequestParam(required = false) Integer userCategory,
            @Parameter(name = "version", description = "乐观锁版本号", required = true) @RequestParam Integer version) {
        
        bedService.updateOccupant(id, userId, userCategory, version);
        String msg = (userId == null) ? "退宿手续办理成功" : "入住成功，床位资源已锁定";
        return R.okMsg(msg);
    }
    
    // =================================================================================
    // 3. 资源检索 (全员登录可见)
    // =================================================================================
    
    @Operation(summary = "查询房间下属床位")
    @GetMapping("/list-by-room")
    public R<List<DormBed>> listByRoom(@RequestParam Long roomId) {
        return R.ok(bedService.getByRoom(roomId));
    }
    
    @Operation(summary = "获取床位详情档案")
    @GetMapping("/{id}")
    public R<DormBed> getInfo(@PathVariable Long id) {
        return R.ok(bedService.getById(id));
    }
}