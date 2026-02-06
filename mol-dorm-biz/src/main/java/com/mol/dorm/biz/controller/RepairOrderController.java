package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.LoginHelper;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.RepairOrder;
import com.mol.dorm.biz.service.DormBedService;
import com.mol.dorm.biz.service.RepairOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 报修管理控制层 - 资产联动增强版
 */
@Tag(name = "报修管理")
@RestController
@RequestMapping("/dorm/repair")
@RequiredArgsConstructor
public class RepairOrderController {
    
    private final RepairOrderService repairService;
    private final DormBedService bedService;
    
    /**
     * 1. 提交报修 (学生/教工扫码)
     */
    @Operation(summary = "提交报修申请")
    @SaCheckLogin
    @PostMapping("/submit")
    public R<Void> submit(@RequestBody RepairOrder order) {
        Long currentUserId = LoginHelper.getUserId();
        
        // A. 自动寻址保护
        if (order.getRoomId() == null) {
            DormBed bed = bedService.getOne(new LambdaQueryWrapper<DormBed>()
                    .eq(DormBed::getOccupantId, currentUserId)
                    .last("LIMIT 1"));
            
            if (bed == null) return R.failMsg("报修失败：未检测到您的入住信息，请联系宿管");
            order.setRoomId(bed.getRoomId());
        }
        
        // B. 强制设定申请人为当前登录人
        order.setApplicantId(currentUserId);
        
        repairService.submitRepair(order);
        return R.okMsg("报修受理成功，相关资产已锁定");
    }
    
    /**
     * 2. 师傅接单
     * 🛡️ [同步修改]：调用 Service 逻辑，确保状态锁原子更新
     */
    @Operation(summary = "维修师傅接单")
    @SaCheckRole(value = {RoleConstants.REPAIR_MASTER, RoleConstants.SUPER_ADMIN}, mode = SaMode.OR)
    @PutMapping("/take/{orderId}")
    public R<Void> takeOrder(@PathVariable Long orderId) {
        Long repairmanId = LoginHelper.getUserId();
        // 建议在 LoginHelper 中扩展获取姓名和手机号的方法，此处模拟传入
        repairService.startRepair(orderId, repairmanId);
        return R.okMsg("接单成功");
    }
    
    /**
     * 3. 完工结项
     * 🛡️ [修正说明]：补全 isAssetBroken 参数，用于判定资产是否需强制报废
     */
    @Operation(summary = "维修完工确认")
    @SaCheckRole(value = {RoleConstants.REPAIR_MASTER, RoleConstants.SUPER_ADMIN}, mode = SaMode.OR)
    @PostMapping("/finish")
    public R<Void> finish(@RequestBody Map<String, Object> params) {
        Long orderId = Long.valueOf(params.get("orderId").toString());
        String comment = (String) params.get("comment");
        Integer rating = (Integer) params.get("rating");
        
        // A. 处理材料费
        Object costObj = params.get("materialCost");
        BigDecimal materialCost = costObj != null ? new BigDecimal(costObj.toString()) : BigDecimal.ZERO;
        
        // B. 🛠️ 补全关键参数：资产是否彻底损坏/报废
        // 前端通常通过一个 Switch 开关传来，如果不传则默认为 false (已修复)
        Object brokenObj = params.get("isAssetBroken");
        Boolean isAssetBroken = brokenObj != null ? Boolean.valueOf(brokenObj.toString()) : false;
        
        // C. 调用 Service (现在参数长度对齐了：Long, String, Integer, BigDecimal, Boolean)
        repairService.finishRepair(orderId, comment, rating, materialCost, isAssetBroken);
        
        return R.okMsg("维修记录已归档，资产状态已同步更新");
    }
    
    /**
     * 4. 挂起报修 (新增入口)
     * 场景：需等待大修或配件暂缺
     */
    @Operation(summary = "挂起/待大修处理")
    @SaCheckRole(value = {RoleConstants.REPAIR_MASTER, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @PostMapping("/suspend")
    public R<Void> suspend(@RequestBody Map<String, Object> params) {
        Long orderId = Long.valueOf(params.get("orderId").toString());
        String reason = (String) params.get("reason");
        repairService.suspendRepair(orderId, reason);
        return R.okMsg("工单已转入待大修状态");
    }
    
    /**
     * 5. 驳回报修 (新增入口)
     * 场景：恶意报修或信息完全错误
     */
    @Operation(summary = "驳回无效报修")
    @SaCheckRole(value = {RoleConstants.DORM_MANAGER, RoleConstants.SUPER_ADMIN}, mode = SaMode.OR)
    @PostMapping("/reject")
    public R<Void> reject(@RequestBody Map<String, Object> params) {
        Long orderId = Long.valueOf(params.get("orderId").toString());
        String reason = (String) params.get("reason");
        repairService.rejectRepair(orderId, reason);
        return R.okMsg("工单已驳回并释放相关资源");
    }
    
    /**
     * 6. 用户评价 (结项后的动作)
     */
    @Operation(summary = "用户服务评价")
    @SaCheckLogin
    @PostMapping("/rate")
    public R<Void> rate(@RequestBody RepairOrder order) {
        // 校验是否为本人评价
        RepairOrder dbOrder = repairService.getById(order.getId());
        if (!dbOrder.getApplicantId().equals(LoginHelper.getUserId())) {
            return R.failMsg("权限拦截：您不能评价他人的报修单");
        }
        
        dbOrder.setRating(order.getRating());
        dbOrder.setComment(order.getComment());
        dbOrder.setStatus(3); // 状态流转到 3-已评价
        repairService.updateById(dbOrder);
        return R.okMsg("感谢评价！");
    }
    
    /**
     * 7. 分页查询
     */
    @Operation(summary = "分页查询报修列表")
    @SaCheckLogin
    @GetMapping("/page")
    public R<Page<RepairOrder>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            RepairOrder query) {
        
        Page<RepairOrder> page = new Page<>(pageNum, pageSize);
        // 此处逻辑建议后续在 Service 中根据角色进行 QueryWrapper 的动态拼接
        return R.ok(repairService.page(page, new LambdaQueryWrapper<>(query)
                .orderByDesc(RepairOrder::getCreateTime)));
    }
}