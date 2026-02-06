package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.service.DashboardService;
import com.mol.dorm.biz.vo.DashboardVO;
import com.mol.dorm.biz.vo.DormRoomVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 宿舍大屏驾驶舱控制层 - 数字化看板核心入口
 * 🛡️ [设计规范与安全矩阵]：
 * 1. 角色熔断：严禁学生角色嗅探校区资源分布。
 * 2. 数据驱动：Controller 仅作为协议转发，核心逻辑在 Service 聚合。
 * 3. 预警下钻：新增能耗与财务维度的下钻入口，支撑宿管主动干预。
 */
@Tag(name = "驾驶舱管理", description = "提供大屏看板聚合数据及四级钻取穿透接口")
@RestController
@RequestMapping("/api/dorm/dashboard")
@RequiredArgsConstructor
@SaCheckRole(value = {
        RoleConstants.SUPER_ADMIN,
        RoleConstants.DEPT_ADMIN,
        RoleConstants.DORM_MANAGER,
        RoleConstants.COUNSELOR
}, mode = SaMode.OR)
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    /**
     * 【主看板聚合数据】
     */
    @Operation(summary = "首页看板聚合数据", description = "卡片、饼图、公告、工作流积压数据汇总")
    @GetMapping("/main")
    public R<DashboardVO> getMainData() {
        return R.ok(dashboardService.getBigScreenData());
    }
    
    /**
     * 【L1 下钻：校区资源树】
     */
    @Operation(summary = "L1-校区-楼栋资源树")
    @GetMapping("/campus/tree")
    public R<List<Map<String, Object>>> getCampusTree() {
        return R.ok(dashboardService.getCampusStructure());
    }
    
    /**
     * 【L2 下钻：楼层压力透视】
     */
    @Operation(summary = "L2-楼层饱和度透视")
    @GetMapping("/building/{id}/floors")
    public R<List<Map<String, Object>>> getFloors(
            @Parameter(description = "楼栋唯一 ID") @PathVariable("id") Long buildingId) {
        return R.ok(dashboardService.getBuildingFloorStats(buildingId));
    }
    
    /**
     * 【L3 下钻：房间分布矩阵】
     */
    @Operation(summary = "L3-房间状态矩阵")
    @GetMapping("/building/{id}/floor/{num}/rooms")
    public R<List<DormRoomVO>> getRoomMatrix(
            @Parameter(description = "楼栋 ID") @PathVariable("id") Long buildingId,
            @Parameter(description = "楼层号") @PathVariable("num") Integer floorNum) {
        return R.ok(dashboardService.getFloorRoomMatrix(buildingId, floorNum));
    }
    
    /**
     * 【L4 下钻：房间原子详情】
     */
    @Operation(summary = "L4-床位详情与人员画像")
    @GetMapping("/room/{id}/detail")
    public R<DormRoomVO> getRoomDetail(
            @Parameter(description = "房间唯一 ID") @PathVariable("id") Long roomId) {
        return R.ok(dashboardService.getRoomDetail(roomId));
    }
    
    /**
     * 🟢 【财务预警下钻】欠费房间实时清单
     * 🛡️ [防刁民]：辅助宿管精准定位恶意欠费或遗忘缴费的宿舍。
     */
    @Operation(summary = "L2-楼栋欠费预警清单", description = "获取指定楼栋内所有余额为负数的房间及金额")
    @GetMapping("/building/{id}/arrears-alerts")
    public R<List<Map<String, Object>>> getArrearsAlerts(@PathVariable("id") Long buildingId) {
        return R.ok(dashboardService.getWalletArrearsAlerts(buildingId));
    }
    
    /**
     * 🟢 【能耗异常审计】高能耗排行
     * 🛡️ [防刁民]：自动识别出用电量偏离常数的房间（排查违规电器）。
     */
    @Operation(summary = "L2-楼栋能耗异常监控", description = "获取指定楼栋内用电量 Top 排行及环比异常数据")
    @GetMapping("/building/{id}/energy-anomaly")
    public R<List<Map<String, Object>>> getEnergyAnomaly(@PathVariable("id") Long buildingId) {
        return R.ok(dashboardService.getEnergyAnomalyRank(buildingId));
    }
    
    /**
     * 【今日动态提醒】
     */
    @Operation(summary = "今日驾驶舱综合动态预警", description = "包含报修积压、待审工作流及实时断电信号")
    @GetMapping("/today-alerts")
    public R<Map<String, Object>> getTodayAlerts() {
        return R.ok(dashboardService.getTodayAlerts());
    }
}