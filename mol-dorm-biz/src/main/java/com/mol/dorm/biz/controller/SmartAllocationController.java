package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode; // 🟢 引入 Mode
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.service.AllocationValidator;
import com.mol.dorm.biz.service.DormAllocationService;
import com.mol.dorm.biz.vo.AllocationStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "智能分配-管理端", description = "核心业务：画像算法分配与数据监控")
@RestController
@RequestMapping("/allocation/smart")
@RequiredArgsConstructor
public class SmartAllocationController {
    
    private final DormAllocationService allocationService;
    private final AllocationValidator validator;
    
    @Operation(summary = "执行一键智能分配", description = "基于贪心算法，根据校区隔离，自动将未分配学生填入宿舍")
    // 🛡️ 权限升级：允许 超管 OR 宿管经理 操作
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @PostMapping("/execute")
    public R<String> execute(
            @Parameter(description = "目标校区 ID (必选)", required = true)
            @RequestParam Long campusId,
            @Parameter(description = "仅分配特定性别 (可选, 0-女 1-男, 不填则全跑)")
            @RequestParam(required = false) String gender) {
        
        if (campusId == null) {
            return R.fail("校区 ID 不能为空");
        }
        
        // 核心：调用 Pro Ultra 级防超卖算法
        String result = allocationService.executeAllocation(campusId, gender);
        
        return R.ok(result);
    }
    
    @Operation(summary = "分配数据校验与监控", description = "实时检测幽灵床位、超卖房间及性别混住异常")
    // 🛡️ 权限：校验功能只读不写，允许更多角色查看（如辅导员关心自己班级分配情况）
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER, RoleConstants.COUNSELOR}, mode = SaMode.OR)
    @GetMapping("/validate")
    public R<AllocationStatsVO> validate(
            @Parameter(description = "校区 ID (必填)", required = true) @RequestParam Long campusId) {
        
        AllocationStatsVO stats = validator.analyzeCampus(campusId);
        if (stats == null) {
            return R.fail("校区不存在或数据异常");
        }
        return R.ok(stats);
    }
}