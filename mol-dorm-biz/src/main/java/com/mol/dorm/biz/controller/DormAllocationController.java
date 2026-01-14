package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.service.DormBedService;
import com.mol.dorm.biz.service.DormAllocationService;
import com.mol.server.service.SysOrdinaryUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "智能分配管理")
@RestController
@RequestMapping("/allocation")
@RequiredArgsConstructor
@Slf4j
public class DormAllocationController {
    
    private final DormAllocationService allocationService;
    private final SysOrdinaryUserService userService;
    private final DormBedService bedService; // [Fix] 注入床位服务
    
    @Operation(summary = "执行一键智能分配 (仅超管)", description = "高危操作！根据画像算法批量分配床位。")
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @PostMapping("/execute")
    public R<String> executeAllocation(@RequestBody(required = false) List<Long> studentIds) {
        
        // 1. 获取待处理名单
        if (studentIds == null || studentIds.isEmpty()) {
            // [Fix] 仅查询状态为"在读"且"住校"的学生 (这里简化逻辑，实际应结合 stu_profile)
            studentIds = userService.list(Wrappers.<SysOrdinaryUser>lambdaQuery()
                    .eq(SysOrdinaryUser::getStatus, "0") // 在读
                    .eq(SysOrdinaryUser::getResidenceType, 0) // 住校
            ).stream().map(SysOrdinaryUser::getId).collect(Collectors.toList());
        }
        
        if (studentIds.isEmpty()) {
            return R.failed("没有可分配的学生");
        }
        
        // 2. [Fix] 关键步骤：过滤掉已经分配了床位的学生！防止重复分配
        // 查询 dorm_bed 表中 occupant_id 在目标列表中的记录
        List<Long> assignedIds = bedService.list(Wrappers.<DormBed>lambdaQuery()
                .in(DormBed::getOccupantId, studentIds)
        ).stream().map(DormBed::getOccupantId).toList();
        
        if (!assignedIds.isEmpty()) {
            log.info("检测到 {} 名学生已有床位，自动跳过。", assignedIds.size());
            // 从待分配列表中移除这些 ID
            studentIds = studentIds.stream()
                    .filter(id -> !assignedIds.contains(id))
                    .collect(Collectors.toList());
        }
        
        if (studentIds.isEmpty()) {
            return R.failed("所选学生均已分配床位，无需操作。");
        }
        
        // 3. 调用核心算法
        allocationService.executeAllocation(studentIds);
        
        return R.ok(String.format("智能分配任务已完成，成功处理 %d 人。", studentIds.size()));
    }
}