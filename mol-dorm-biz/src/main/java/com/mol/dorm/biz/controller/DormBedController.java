package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.exception.ServiceException;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.service.DormBedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "床位管理")
@RestController
@RequestMapping("/bed")
@RequiredArgsConstructor
public class DormBedController {
    
    private final DormBedService bedService;
    
    // ==================== 1. 公共查询 (需登录) ====================
    
    @Operation(summary = "查询我的床位信息", description = "学生端使用，查询自己分到了哪里")
    @SaCheckLogin // 🔒 只要登录就能查
    @GetMapping("/my")
    public R<DormBed> getMyBed() {
        Long userId = StpUtil.getLoginIdAsLong();
        DormBed bed = bedService.getOne(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getOccupantId, userId)
                .last("LIMIT 1"));
        if (bed == null) {
            return R.fail("您当前未分配任何床位");
        }
        return R.ok(bed);
    }
    
    @Operation(summary = "查询房间内的所有床位", description = "展示房间详情时使用")
    @SaCheckLogin // 🔒 只要登录就能查
    @GetMapping("/list/{roomId}")
    public R<List<DormBed>> listByRoom(@PathVariable Long roomId) {
        return R.ok(bedService.lambdaQuery()
                .eq(DormBed::getRoomId, roomId)
                .orderByAsc(DormBed::getBedLabel)
                .list());
    }
    
    // ==================== 2. 学生操作 ====================
    
    @Operation(summary = "学生确认入住", description = "学生到达宿舍后，点击此按钮确认入住")
    @SaCheckLogin // 🔒 必须登录
    @PostMapping("/check-in")
    public R<Void> checkIn() {
        Long userId = StpUtil.getLoginIdAsLong();
        bedService.confirmCheckIn(userId);
        return R.ok(null, "入住办理成功！");
    }
    
    // ==================== 3. 管理员操作 (分配/退宿) ====================
    
    @Operation(summary = "分配床位 (手动)", description = "管理员手动指定某人住某床")
    // 🔒 权限：超管 或 宿管 或 辅导员 (OR模式，满足其一即可)
    @SaCheckRole(value = {
            RoleConstants.SUPER_ADMIN,
            RoleConstants.DORM_MANAGER,
            RoleConstants.COUNSELOR
    }, mode = SaMode.OR)
    @PostMapping("/assign")
    public R<Void> assignUser(
            @Parameter(description = "床位 ID") @RequestParam Long bedId,
            @Parameter(description = "学生 ID") @RequestParam Long userId) {
        bedService.assignUserToBed(bedId, userId);
        return R.ok(null, "分配成功");
    }
    
    @Operation(summary = "一键退宿 (释放床位)", description = "学生毕业或离校时，释放其床位")
    // 🔒 权限：超管 或 宿管 (辅导员通常只负责学生管理，退宿这种资源操作建议留给宿管)
    @SaCheckRole(value = {
            RoleConstants.SUPER_ADMIN,
            RoleConstants.DORM_MANAGER
    }, mode = SaMode.OR)
    @PostMapping("/release")
    public R<Void> releaseBed(@Parameter(description = "床位 ID") @RequestParam Long bedId) {
        bedService.releaseBed(bedId);
        return R.ok(null, "退宿成功，床位已释放");
    }
}