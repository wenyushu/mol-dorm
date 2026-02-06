package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.service.DormRoomService;
import com.mol.dorm.biz.vo.MyRoomVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 房间资源管理控制器 - 资源树末梢管控 (聚合看板版)
 */
@Tag(name = "房间管理")
@RestController
@RequestMapping("/dorm/room")
@RequiredArgsConstructor
public class DormRoomController {
    
    private final DormRoomService roomService;
    
    // =================================================================================
    // 1. 小程序/学生端聚合入口 (毕设最核心接口)
    // =================================================================================
    
    @Operation(summary = "获取我的宿舍聚合看板", description = "学生专用接口：一键获取房间位置、舍友画像、水电余额、最近账单及系统公告。")
    @SaCheckLogin // 🛡️ 只要登录即可访问，后端会自动根据当前 Session ID 识别学生身份
    @GetMapping("/my-dashboard")
    public R<MyRoomVO> getMyDashboard() {
        // 从 Sa-Token 获取当前登录人的 ID (Ordinary User ID)
        Long studentId = StpUtil.getLoginIdAsLong();
        return R.ok(roomService.getMyRoomDashboard(studentId));
    }
    
    // =================================================================================
    // 2. 核心资源维护 (限 SUPER_ADMIN / DEPT_ADMIN)
    // =================================================================================
    
    @Operation(summary = "严格保存/修改房间", description = "若修改容量，将触发算法自动对齐房型描述（如 12 -> 十二人间）。")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DEPT_ADMIN}, mode = SaMode.OR)
    @PostMapping("/save-strict")
    public R<Void> saveRoomStrict(@RequestBody DormRoom room) {
        roomService.saveRoomStrict(room);
        return R.okMsg("房间资源档案已安全同步，语义房型已算法校准。");
    }
    
    @Operation(summary = "调整房间核定容量", description = "动态增减房间床位。缩减时若实住人数超过目标容量，则操作拦截。")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @PutMapping("/{id}/adjust-capacity")
    public R<Void> adjustCapacity(
            @Parameter(name = "id", description = "房间ID") @PathVariable Long id,
            @Parameter(name = "newCapacity", description = "新定员数") @RequestParam Integer newCapacity) {
        roomService.adjustRoomCapacity(id, newCapacity);
        return R.okMsg("房间容量已成功调整为 " + newCapacity + " 人，房型描述已自动重算。");
    }
    
    @Operation(summary = "安全物理注销房间")
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @DeleteMapping("/{id}")
    public R<Void> removeRoomStrict(@PathVariable Long id) {
        roomService.removeRoomStrict(id);
        return R.ok();
    }
    
    // =================================================================================
    // 3. 运维与熔断控制 (限管理员级别)
    // =================================================================================
    
    @Operation(summary = "切换房间生命周期状态")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        roomService.updateRoomStatus(id, status);
        return R.okMsg("房间状态已成功切换。");
    }
    
    /**
     * 🟢 核心体检入口 (毕设炫技接口)
     */
    @Operation(summary = "触发房间安全审计与体检", description = "执行“三维审计”：1. 资产安全性评估 2. 床位物理核账 3. 资源饱和度重置。")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @PostMapping("/{id}/evaluate")
    public R<String> evaluate(@PathVariable Long id) {
        // 同时触发两套核心审计逻辑
        roomService.evaluateRoomSafety(id);      // 审计资产与安全性
        roomService.refreshResourceStatus(id);  // 审计人员与饱和度
        return R.okMsg("房间体检完成，安全等级与在住人数已实时同步。");
    }
    
    // =================================================================================
    // 4. 资源检索
    // =================================================================================
    
    @Operation(summary = "查询楼层下属房间")
    @GetMapping("/list-by-floor")
    public R<List<DormRoom>> listByFloor(@RequestParam Long floorId) {
        return R.ok(roomService.getByFloor(floorId));
    }
    
    @Operation(summary = "获取房间详情档案")
    @GetMapping("/{id}")
    public R<DormRoom> getInfo(@PathVariable Long id) {
        return R.ok(roomService.getById(id));
    }
}