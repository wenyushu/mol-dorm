package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
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

@Tag(name = "智能分配-管理员", description = "基于画像算法的一键分配")
@RestController
@RequestMapping("/allocation/smart")
@RequiredArgsConstructor
public class SmartAllocationController {
    
    private final DormAllocationService allocationService;
    private final AllocationValidator validator; // 注入校验器
    
    @Operation(summary = "一键智能分配", description = "根据校区隔离，自动将该校区未分配的学生填入该校区的宿舍")
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 只有超管能做这个操作，因为影响范围极大
    @PostMapping("/execute")
    
    public R<String> execute(
            @Parameter(description = "目标校区 ID (必选)", required = true)
            @RequestParam Long campusId,
            @Parameter(description = "仅分配特定性别 (可选, 0-女 1-男, 不填则全跑)")
            @RequestParam(required = false) String gender) {
        
        // 核心：直接透传 String 类型的 gender 给 Service，服务层会自动查找该校区下的学院学生和宿舍楼
        String result = allocationService.executeAllocation(campusId, gender);
        
        return R.ok(result);
    }
    
    @Operation(summary = "分配数据校验与监控", description = "实时检测影子用户、超卖房间及分配进度")
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 只有超管能做这个操作，因为影响范围极大
    @GetMapping("/validate")
    public R<AllocationStatsVO> validate(
            @Parameter(description = "校区 ID (必填)", required = true) @RequestParam Long campusId) {
        
        AllocationStatsVO stats = validator.analyzeCampus(campusId);
        if (stats == null) {
            return R.failed("校区不存在");
        }
        return R.ok(stats);
    }
}