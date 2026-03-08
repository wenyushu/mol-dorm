package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.DormWorkflow;
import com.mol.dorm.biz.mapper.DormWorkflowMapper;
import com.mol.dorm.biz.service.DormWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 宿舍管理中心 (管理员上帝模式)
 * 🛡️ [审计标准]：
 * 1. 审批流：开放给管理层和辅导员。
 * 2. 物理干预：仅限核心管理员。
 * 3. 批量清退：仅限超级管理员。
 */
@Tag(name = "宿管管理中心", description = "包含审批流程和行政强制干预接口")
@RestController
@RequestMapping("/admin/dorm")
@RequiredArgsConstructor
public class DormAdminController {
    
    private final DormWorkflowService workflowService;
    private final DormWorkflowMapper workflowMapper;
    
    // ==================== 1. 审批流管理 ====================
    
    @Operation(summary = "分页获取申请单列表")
    @GetMapping("/application/page")
    // 🛡️ [放宽审计]：允许辅导员和部门管理员查看，方便他们掌握本院学生动态
    @SaCheckRole(value = {
            RoleConstants.SUPER_ADMIN,
            RoleConstants.DEPT_ADMIN,
            RoleConstants.DORM_MANAGER,
            RoleConstants.COUNSELOR
    }, mode = SaMode.OR)
    public R<IPage<DormWorkflow>> getApplicationPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer type) {
        
        Page<DormWorkflow> page = new Page<>(pageNum, pageSize);
        IPage<DormWorkflow> result = workflowMapper.selectPage(page,
                Wrappers.<DormWorkflow>lambdaQuery()
                        .eq(status != null, DormWorkflow::getStatus, status)
                        .eq(type != null, DormWorkflow::getType, type)
                        .orderByDesc(DormWorkflow::getCreateTime));
        
        return R.ok(result);
    }
    
    @Operation(summary = "统一审批接口")
    @PostMapping("/application/audit")
    // 🛡️ [审批权限]：仅限有决策权的管理者
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DEPT_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    public R<String> audit(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Boolean agree = (Boolean) params.get("agree");
        String remark = (String) params.get("remark");
        
        workflowService.handleApproval(id, agree, remark);
        return R.okMsg(agree ? "审批通过，业务已生效" : "申请已驳回");
    }
    
    // ==================== 2. 行政强制干预 (上帝模式) ====================
    
    @Operation(summary = "[行政强制] 双方床位互换")
    @PostMapping("/manual/swap")
    // 🛡️ [高权审计]：强制互换属于行政命令，排除辅导员，仅限宿管经理及以上
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    public R<String> forceSwap(@RequestParam Long userIdA, @RequestParam Long userIdB) {
        workflowService.forceSwap(userIdA, userIdB);
        return R.okMsg("行政干预成功：床位已互换");
    }
    
    @Operation(summary = "[行政强制] 搬迁/退宿")
    @PostMapping("/manual/move")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    public R<String> forceMove(
            @RequestParam Long userId,
            @RequestParam(required = false) Long targetBedId) {
        workflowService.forceMove(userId, targetBedId);
        String msg = targetBedId == null ? "该用户已被强制清退并释放床位" : "搬迁指令已执行";
        return R.okMsg(msg);
    }
    
    @Operation(summary = "[灭世级] 批量毕业生离校")
    @PostMapping("/manual/graduate")
    // 🛡️ [终极权限]：必须锁在 SUPER_ADMIN，防止误操作导致全校原地 “毕业”
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    public R<String> batchGraduate(
            @Parameter(name = "grade", description = "年级，如 2022", example = "2022")
            @RequestParam Integer grade) {
        
        int count = workflowService.batchGraduate(grade);
        if (count == 0) {
            return R.failMsg(grade + " 级暂无可处理的在校生数据");
        }
        return R.okMsg(grade + " 级毕业生手续已批量处理：共释放床位 " + count + " 张。");
    }
}