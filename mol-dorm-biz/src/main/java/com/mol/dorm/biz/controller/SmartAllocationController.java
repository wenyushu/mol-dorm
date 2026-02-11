package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.service.AllocationValidator;
import com.mol.dorm.biz.service.DormAllocationService;
import com.mol.dorm.biz.service.DormWorkflowService; // 🟢 引入流程服务，复用毕业清算逻辑
import com.mol.dorm.biz.vo.AllocationStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 智能分配与全校清算控制中心
 * 🛡️ [安全防线]：
 * 1. 创世权限：一键入住属于高熵操作，仅限 SUPER_ADMIN。
 * 2. 状态原子：清算后强制触发 evaluateRoomSafety。
 */
@Tag(name = "智能分配-管理端", description = "核心业务：创世分配、批量清退与资源审计")
@RestController
@RequestMapping("/api/dorm/allocation/smart") // 规范路径
@RequiredArgsConstructor
public class SmartAllocationController {
    
    private final DormAllocationService allocationService;
    private final DormWorkflowService workflowService; // 🟢 引入毕业清算核心实现
    private final AllocationValidator validator;
    
    // =================================================================================
    // 1. 创世级操作：一键入住 (仅限 SUPER_ADMIN)
    // =================================================================================
    
    /**
     * 批量执行新生分配
     * 🛡️ [防刁民逻辑]：贪心算法执行前先调用 validator 预检，防止数据冲突。
     */
    @Operation(summary = "【创世】执行一键智能分配")
    @Parameters({
            @Parameter(name = "campusId", description = "校区ID", required = true),
            @Parameter(name = "gender", description = "指定性别(1-男, 2-女)")
    })
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @PostMapping("/execute")
    public R<String> execute(@RequestParam Long campusId, @RequestParam(required = false) Integer gender) {
        // [防刁民点]：gender 改为 Integer，对齐 DormConstants.GENDER_MALE/FEMALE
        String result = allocationService.executeAllocation(campusId, gender);
        return R.ok(result, "一键分配指令已完成");
    }
    
    // =================================================================================
    // 2. 终结级操作：一键清退 (仅限 SUPER_ADMIN)
    // =================================================================================
    
    /**
     * 毕业生批量强制退宿
     * 🛡️ [防刁民点]：复用 workflowService.batchGraduate 以触发全量资源体检。
     */
    @Operation(summary = "【终结】毕业生一键批量退宿")
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @PostMapping("/terminate-graduate")
    public R<String> terminateByGrade(
            @Parameter(description = "入学年份(如2022)", required = true) @RequestParam Integer graduateYear) {
        
        // 对齐 Service 返回值，返回处理人数
        int count = workflowService.batchGraduate(graduateYear);
        
        if (count == 0) {
            return R.failMsg(graduateYear + " 级暂无可清算的在校生数据");
        }
        
        return R.okMsg("毕业季大规模清算结束：共释放床位 " + count + " 张，涉及房间已自动完成资源体检。");
    }
    
    /**
     * 影子重置：一键撤销刚才的分配（用于模拟失败后的回滚）
     */
    @Operation(summary = "【危险】重置校区分配状态")
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @DeleteMapping("/reset-campus")
    public R<Void> resetCampus(@RequestParam Long campusId) {
        allocationService.resetCampusAllocation(campusId);
        return R.okMsg("校区预分配数据已清空，资源回归初始态");
    }
    
    // =================================================================================
    // 3. 运维与监控 (限管理员级别)
    // =================================================================================
    
    @Operation(summary = "分配数据审计监控", description = "检测幽灵床位、异性混住等异常。")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER, RoleConstants.COUNSELOR}, mode = SaMode.OR)
    @GetMapping("/validate")
    public R<AllocationStatsVO> validate(@RequestParam Long campusId) {
        AllocationStatsVO stats = validator.analyzeCampus(campusId);
        return R.ok(stats);
    }
    
    @Operation(summary = "分配模拟测算")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @GetMapping("/simulate")
    public R<String> simulate(@RequestParam Long campusId) {
        String report = allocationService.simulateAllocation(campusId);
        return R.ok(report, "模拟测算报告已生成");
    }
}