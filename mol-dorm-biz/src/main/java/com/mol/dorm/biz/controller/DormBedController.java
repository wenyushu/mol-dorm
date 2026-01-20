package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.LoginHelper;
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
    
    @Operation(summary = "查询我的床位信息", description = "查询当前登录用户（无论是学生还是教工）的床位")
    @SaCheckLogin // 🔒 只要登录就能查
    @GetMapping("/my")
    public R<DormBed> getMyBed() {
        Long userId = LoginHelper.getUserId();
        
        // 🛡️ 通用查询：不管你是学生(0)还是教工(1)，只要 ID 对得上就行
        // 如果需要严格区分，可以加上 occupantType 的判断
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
        return R.ok(bedService.list(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getRoomId, roomId)
                .orderByAsc(DormBed::getBedLabel)));
    }
    
    // ==================== 2. 学生/教工 操作 ====================
    
    @Operation(summary = "自助办理入住 (Check-In)", description = "确认入住状态（通常配合现场扫码）")
    @SaCheckLogin // 🔒 必须登录
    @PostMapping("/check-in")
    public R<Void> checkIn() {
        // 我们的 Service 逻辑里，分配时状态已经是 1(已入住) 了。
        // 这个接口通常用于“二次确认”或者“激活门禁权限”。
        // 这里简单实现：检查是否有床位即可。
        Long userId = LoginHelper.getUserId();
        long count = bedService.count(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getOccupantId, userId));
        
        if (count == 0) {
            return R.fail("系统检测到您尚未分配床位，请先联系宿管！");
        }
        return R.ok(null, "入住状态确认成功！");
    }
    
    // ==================== 3. 管理员操作 (分配/退宿) ====================
    
    @Operation(summary = "分配床位 (手动)", description = "管理员手动指定某人住某床")
    // 🔒 权限：超管 或 宿管 或 辅导员
    @SaCheckRole(value = {
            RoleConstants.SUPER_ADMIN,
            RoleConstants.DORM_MANAGER,
            RoleConstants.COUNSELOR
    }, mode = SaMode.OR)
    @PostMapping("/assign")
    public R<Void> assignUser(
            @Parameter(description = "床位 ID") @RequestParam Long bedId,
            @Parameter(description = "用户 ID") @RequestParam Long userId,
            @Parameter(description = "用户类型: 0-学生 1-教工") @RequestParam(defaultValue = "0") Integer userType) {
        
        // 🟢 适配新方法：assignBed(bedId, userId, userType)
        bedService.assignBed(bedId, userId, userType);
        return R.ok(null, "分配成功");
    }
    
    @Operation(summary = "一键退宿 (释放床位)", description = "释放指定床位")
    @SaCheckRole(value = {
            RoleConstants.SUPER_ADMIN,
            RoleConstants.DORM_MANAGER
    }, mode = SaMode.OR)
    @PostMapping("/release")
    public R<Void> releaseBed(@Parameter(description = "床位 ID") @RequestParam Long bedId) {
        // 🟢 适配新方法：releaseBed(bedId)
        bedService.releaseBed(bedId);
        return R.ok(null, "退宿成功，床位已释放");
    }
}