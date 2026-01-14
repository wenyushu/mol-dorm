package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.RepairOrder;
import com.mol.dorm.biz.service.DormBedService;
import com.mol.dorm.biz.service.RepairOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "报修管理")
@RestController
@RequestMapping("/repair")
@RequiredArgsConstructor
public class RepairOrderController {
    
    private final RepairOrderService repairService;
    private final DormBedService bedService; // 用于查找学生当前房间
    
    @Operation(summary = "学生提交报修")
    @SaCheckRole(RoleConstants.STUDENT)
    @PostMapping("/submit")
    public R<Void> submit(@RequestBody RepairOrder vo) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 自动查找学生当前所在房间
        Long roomId = vo.getRoomId();
        if (roomId == null) {
            DormBed bed = bedService.getBedByStudentId(userId);
            if (bed == null) return R.failed("您当前未入住，无法报修");
            roomId = bed.getRoomId();
        }
        
        repairService.submit(userId, roomId, vo.getDescription(), vo.getImages());
        return R.ok();
    }
    
    @Operation(summary = "指派维修工 (宿管)")
    @SaCheckRole(value = {RoleConstants.DORM_MANAGER, RoleConstants.SUPER_ADMIN}, mode = SaMode.OR)
    @PostMapping("/assign")
    public R<Void> assign(@RequestParam Long orderId, @RequestParam Long repairmanId) {
        repairService.assign(orderId, repairmanId);
        return R.ok();
    }
    
    @Operation(summary = "完成维修 (维修工/宿管)")
    @SaCheckLogin
    @PostMapping("/complete")
    public R<Void> complete(@RequestParam Long orderId, @RequestParam(required = false) String remark) {
        repairService.complete(orderId, remark);
        return R.ok();
    }
    
    @Operation(summary = "评价工单 (学生)")
    @SaCheckRole(RoleConstants.STUDENT)
    @PostMapping("/rate")
    public R<Void> rate(@RequestBody RepairOrder vo) {
        repairService.rate(vo.getId(), vo.getRating(), vo.getComment());
        return R.ok();
    }
    
    @Operation(summary = "分页查询报修列表")
    @SaCheckLogin
    @GetMapping("/list")
    public R<Page<RepairOrder>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            RepairOrder query) {
        
        Page<RepairOrder> page = new Page<>(pageNum, pageSize);
        // 获取当前用户角色，用于 Service 层做数据隔离
        String role = (String) StpUtil.getSession().get("role");
        
        return R.ok(repairService.getPage(page, query, StpUtil.getLoginIdAsLong(), role));
    }
}