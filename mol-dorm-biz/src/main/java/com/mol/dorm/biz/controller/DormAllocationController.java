package com.mol.dorm.biz.controller;


import com.mol.common.core.util.R;
import com.mol.dorm.biz.service.impl.DormAllocationService;
import com.mol.server.service.SysOrdinaryUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "智能分配管理")
@RestController
@RequestMapping("/allocation")
@RequiredArgsConstructor
public class DormAllocationController {
    
    private final DormAllocationService allocationService;
    private final SysOrdinaryUserService userService;
    
    @Operation(summary = "执行一键智能分配")
    @PostMapping("/execute")
    public R<String> executeAllocation(@RequestBody(required = false) List<Long> studentIds) {
        // 1. 如果前端没传ID，则默认查找所有 “未分配床位” 的学生（模拟一键全员分配）
        // 这里的逻辑是：查 sys_ordinary_user 表，且不在 dorm_bed 表里的人
        // 为了演示方便，如果参数为空，我们直接查所有学生ID
        if (studentIds == null || studentIds.isEmpty()) {
            // 这里简单粗暴一点，获取前 100 个学生用于测试
            studentIds = userService.list().stream().map(u -> u.getId()).toList();
        }
        
        if (studentIds.isEmpty()) {
            return R.failed("没有可分配的学生");
        }
        
        // 2. 调用核心算法
        allocationService.executeAllocation(studentIds);
        
        return R.ok("分配计算完成，请查看后台日志或数据库结果");
    }
}