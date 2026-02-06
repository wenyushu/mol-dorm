package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.DormBuilding;
import com.mol.dorm.biz.service.DormBuildingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 楼栋资源管控控制器
 * 🛡️ [防刁民策略]：
 * 1. 权限隔离：初始化与注销锁定 SUPER_ADMIN。
 * 2. 状态关联：切换状态强制触发“净楼审计”。
 * 3. 算法闭环：通过专有 DTO 接收初始化参数，确保房型语义算法获得准确输入。
 */
@Tag(name = "楼栋管理")
@RestController
@RequestMapping("/dorm/building")
@RequiredArgsConstructor
public class DormBuildingController {
    
    private final DormBuildingService buildingService;
    
    // =================================================================================
    // 1. 创世/毁灭级操作 (仅限 SUPER_ADMIN)
    // =================================================================================
    
    @Operation(summary = "全自动资源初始化引擎", description = "一键生成楼层、房间、床位全链路数据。")
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @PostMapping("/init-full")
    public R<String> initFullBuilding(@RequestBody BuildingInitDTO dto) {
        // [毕设亮点]：调用 Service 层算法引擎
        buildingService.createFullBuilding(
                dto.getBuilding(),
                dto.getFloorGenders(),
                dto.getRoomsPerFloor(),
                dto.getCapacityPerRoom()
        );
        return R.okMsg("楼栋资源树初始化成功！已根据算法自动生成房型描述。");
    }
    
    @Operation(summary = "严格级联删除楼栋", description = "必须先清空在住人员，否则触发安全熔断。")
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @DeleteMapping("/{id}")
    public R<Void> removeBuildingStrict(@PathVariable Long id) {
        buildingService.removeBuildingStrict(id);
        return R.okMsg("楼栋及其关联房间、床位档案已安全注销");
    }
    
    // ==================== 2. 核心运维 (限 SUPER_ADMIN / DEPT_ADMIN) ====================
    
    @Operation(summary = "楼栋性质强制改造", description = "修改用途或性别。若有人在住则禁止修改。")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DEPT_ADMIN}, mode = SaMode.OR)
    @PutMapping("/edit-strict")
    public R<Void> editStrict(@RequestBody DormBuilding building) {
        buildingService.updateBuildingStrict(building);
        return R.okMsg("核心性质已变更，下属资源档案已完成同步");
    }
    
    /**
     * 🟢 数据自愈入口 (炫技接口)
     */
    @Operation(summary = "一键校准全楼入住数据", description = "穿透床位表执行物理计票，修正房间 currentNum 统计误差。")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @PostMapping("/{id}/sync-occupancy")
    public R<Void> syncOccupancy(@PathVariable Long id) {
        buildingService.syncRoomOccupancy(id);
        return R.okMsg("校准引擎运行结束，楼栋数据已恢复物理一致性");
    }
    
    // ==================== 3. 日常状态维护 ====================
    
    @Operation(summary = "切换楼栋生命周期状态")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        buildingService.updateStatus(id, status);
        return R.okMsg("楼栋状态已切换，审计通过");
    }
    
    @Operation(summary = "修改楼栋基础信息")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @PutMapping("/basic-info")
    public R<Void> updateBasicInfo(@RequestBody DormBuilding building) {
        buildingService.updateById(building);
        return R.ok();
    }
    
    // ==================== 4. 信息查询 ====================
    
    @Operation(summary = "楼栋资源分页查询")
    @GetMapping("/page")
    public R<IPage<DormBuilding>> listPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long campusId,
            @RequestParam(required = false) String buildingName) {
        
        Page<DormBuilding> page = new Page<>(pageNum, pageSize);
        IPage<DormBuilding> result = buildingService.page(page,
                Wrappers.<DormBuilding>lambdaQuery()
                        .eq(campusId != null, DormBuilding::getCampusId, campusId)
                        .like(StrUtil.isNotBlank(buildingName), DormBuilding::getBuildingName, buildingName)
                        .orderByAsc(DormBuilding::getBuildingNo));
        return R.ok(result);
    }
    
    // ==================== 5. 内部参数载体 ====================
    
    /**
     * 🛡️ 初始化专用 DTO
     */
    @Data
    public static class BuildingInitDTO {
        private DormBuilding building;
        private List<Integer> floorGenders; // 各楼层性别 0-女 1-男
        private Integer roomsPerFloor;
        private Integer capacityPerRoom;
    }
}