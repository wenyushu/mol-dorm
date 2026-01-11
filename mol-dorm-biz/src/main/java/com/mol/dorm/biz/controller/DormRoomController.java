package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.service.DormRoomService;
import com.mol.dorm.biz.vo.DormRoomVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "宿舍房间管理")
@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class DormRoomController {
    
    private final DormRoomService roomService;
    
    // ==================== 1. 公共查询 ====================
    @SaCheckLogin // 🔒 需要登录
    @Operation(summary = "查询某楼栋房间列表")
    @GetMapping("/list/{buildingId}")
    public R<Page<DormRoomVO>> listByBuilding(
            @PathVariable Long buildingId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<DormRoom> pageParam = new Page<>(pageNum, pageSize);
        return R.ok(roomService.getRoomVoPage(pageParam, buildingId));
    }
    
    @GetMapping("/{id}")
    public R<DormRoomVO> getDetail(@PathVariable Long id) {
        return R.ok(roomService.getRoomDetail(id));
    }
    
    
    // ==================== 2. 高危物理操作 (仅 Super Admin) ====================
    
    @Operation(summary = "新增房间 (物理)", description = "仅限超管")
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @PostMapping
    public R<Void> add(@RequestBody DormRoom room) {
        roomService.addRoom(room);
        return R.ok();
    }
    
    @Operation(summary = "删除房间 (物理)", description = "仅限超管")
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return R.ok();
    }
    
    @Operation(summary = "删除整层楼 (物理)", description = "仅限超管")
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @DeleteMapping("/floor")
    public R<Void> deleteFloor(@RequestParam Long buildingId, @RequestParam Integer floor) {
        roomService.deleteFloor(buildingId, floor);
        return R.ok();
    }
    
    // ==================== 3. 运维与应急操作 (Admin + 宿管 + 辅导员) ====================
    
    @Operation(summary = "修改房间 (扩容/缩容/封寝)", description = "一线管理人员可操作")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER, RoleConstants.COUNSELOR}, mode = SaMode.OR)
    @PutMapping
    public R<Void> update(@RequestBody DormRoom room) {
        roomService.updateRoom(room);
        return R.ok();
    }
    
    @Operation(summary = "停用整层楼", description = "一线管理人员可操作")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER, RoleConstants.COUNSELOR}, mode = SaMode.OR)
    @PutMapping("/floor/disable")
    public R<Void> disableFloor(@RequestParam Long buildingId, @RequestParam Integer floor) {
        roomService.disableFloor(buildingId, floor);
        return R.ok();
    }
    
    @Operation(summary = "紧急腾退/封寝", description = "一线管理人员可操作")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER, RoleConstants.COUNSELOR}, mode = SaMode.OR)
    @PostMapping("/evacuate")
    public R<Void> evacuate(@RequestParam Long roomId, @RequestParam(required = false) String reason) {
        roomService.evacuateRoom(roomId, reason);
        return R.ok();
    }
    
    @Operation(summary = "紧急转移人员", description = "一线管理人员可操作")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER, RoleConstants.COUNSELOR}, mode = SaMode.OR)
    @PostMapping("/transfer")
    public R<Void> emergencyTransfer(Long sourceId, Long targetId) {
        roomService.emergencyTransfer(sourceId, targetId);
        return R.ok();
    }
}