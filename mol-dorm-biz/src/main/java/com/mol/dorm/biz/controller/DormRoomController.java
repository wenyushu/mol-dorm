package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.service.DormRoomService;
import com.mol.dorm.biz.vo.DormRoomVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 宿舍房间管理控制器
 * <p>
 * 权限更加灵活，支持宿管和辅导员参与日常运维
 */
@Tag(name = "宿舍房间管理", description = "房间状态及容量运维")
@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class DormRoomController {
    
    private final DormRoomService roomService;
    
    // 修改返回类型为 Page<DormRoomVO>
    @Operation(summary = "查询某楼栋的所有房间", description = "所有人可查，包含居住人姓名")
    @GetMapping("/list/{buildingId}")
    public R<Page<DormRoomVO>> listByBuilding(
            @Parameter(description = "楼栋 ID") @PathVariable Long buildingId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        
        // 调用 Service 的 VO 增强查询
        Page<DormRoom> pageParam = new Page<>(pageNum, pageSize);
        return R.ok(roomService.getRoomVoPage(pageParam, buildingId));
    }
    
    
    // ================== 高风险操作：仅 Admin ==================
    
    @Operation(summary = "新增房间 (仅 Admin)", description = "物理建设房间，通常在建楼时批量导入")
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 仅超管
    @PostMapping
    public R<Boolean> save(@RequestBody DormRoom room) {
        return R.ok(roomService.save(room));
    }
    
    @Operation(summary = "删除房间 (仅 Admin)", description = "物理拆除房间")
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 仅超管
    @DeleteMapping("/{id}")
    public R<Boolean> remove(@PathVariable Long id) {
        return R.ok(roomService.removeById(id));
    }
    
    
    // ================== 运维操作：多人共有 ==================
    
    /**
     * 修改房间信息
     * 场景举例：
     * 1. 宿管发现水管爆裂 -> 将 status 改为 0 (封寝)
     * 2. 辅导员将 4 人间改为 6 人间 -> 将 capacity 改为 6
     * 3. Admin 修正门牌号错误
     */
    @Operation(summary = "修改房间信息 (Admin/宿管/辅导员)", description = "支持修改状态(封寝)、容量等日常运维信息")
    @SaCheckRole(
            value = {
                    RoleConstants.SUPER_ADMIN,   // 超管
                    RoleConstants.DORM_MANAGER,  // 宿管
                    RoleConstants.COUNSELOR      // 辅导员
            },
            mode = SaMode.OR // 🔓 核心配置：OR 模式表示只要具备列表中的【任意一个】角色即可通过
    )
    @PutMapping
    public R<Boolean> update(@RequestBody DormRoom room) {
        // 注意：实际生产中，可能需要限制宿管不能改 room_no，只能改 status。
        // 这里为了简化，暂未做细粒度的字段级控制。
        return R.ok(roomService.updateById(room));
    }
    
    // ================== 4. 紧急应急操作 ==================
    
    /**
     * 紧急腾退 (如火灾、设施损坏)
     * 操作后果：房间状态变为封寝，所有人员自动退宿(变成无床位状态)，需重新分配
     */
    @Operation(summary = "紧急腾退/封寝", description = "用于火灾、维修等紧急情况。会强制移出所有人员并封锁房间。")
    @SaCheckRole(
            value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, // 宿管也可以操作，因为他们是现场第一响应人
            mode = SaMode.OR
    )
    @PostMapping("/evacuate")
    public R<Void> evacuate(
            @Parameter(description = "房间 ID") @RequestParam Long roomId,
            @Parameter(description = "原因") @RequestParam(required = false) String reason) {
        
        roomService.evacuateRoom(roomId, reason);
        return R.ok(null, "紧急腾退成功！人员已全部移出，房间已封锁。请尽快为学生重新分配床位。");
    }
}