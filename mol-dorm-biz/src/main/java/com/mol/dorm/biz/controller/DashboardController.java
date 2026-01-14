package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.service.DashboardService;
import com.mol.dorm.biz.vo.DashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "数据大屏接口")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    @Operation(summary = "获取全量大屏数据", description = "包含顶部统计、饼图、柱状图数据")
    @SaCheckLogin // 登录即可访问，或者改为 @SaCheckRole 限制管理员
    @GetMapping("/data")
    public R<DashboardVO> getData() {
        return R.ok(dashboardService.getBigScreenData());
    }
}