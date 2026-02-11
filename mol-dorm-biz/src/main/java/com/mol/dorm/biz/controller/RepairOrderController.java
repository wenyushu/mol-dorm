package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.LoginHelper;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.RepairOrder;
import com.mol.dorm.biz.service.RepairOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 报修管理控制层 - 数字化运维中心
 * 🛡️ [排错加固点]：
 * 1. 权限精简：利用 LoginHelper 全面替代手动 ID 校验，防止纵向越权。
 * 2. 调度联动：提交接口已集成自动指派引擎，实现“报修即指派”。
 * 3. 财务对账：结项接口强校验人为损坏标识，直接驱动 Service 层的自动扣赔事务。
 */
@Tag(name = "报修管理")
@RestController
@RequestMapping("/dorm/repair")
@RequiredArgsConstructor
public class RepairOrderController {
    
    private final RepairOrderService repairService;
    
    /**
     * 1. 提交报修申请
     * [联动逻辑]：后端自动绑定申请人 -> 触发资产锁定 -> 触发负载均衡自动派单。
     */
    @Operation(summary = "提交报修申请")
    @SaCheckLogin
    @PostMapping("/submit")
    public R<Void> submit(@RequestBody RepairOrder order) {
        // [审计保护]：从安全上下文获取 ID，防止通过 Postman 伪造他人报修
        order.setApplicantId(LoginHelper.getUserId());
        
        // 执行 Service (内部含 autoAllocate 自动指派逻辑)
        repairService.submitRepair(order);
        
        return R.okMsg("报修已受理，系统已为您自动匹配最闲师傅。");
    }
    
    /**
     * 2. 维修完工确认 (师傅端/管理端)
     * 🛡️ [核心业务]：判定人为损坏性质，若为 true 则触发学生账户资产追偿。
     */
    @Operation(summary = "维修完工确认")
    @SaCheckRole(value = {RoleConstants.REPAIR_MASTER, RoleConstants.SUPER_ADMIN}, mode = SaMode.OR)
    @PostMapping("/finish")
    public R<Void> finish(@RequestBody Map<String, Object> params) {
        Long orderId = Long.valueOf(params.get("orderId").toString());
        String comment = (String) params.get("comment");
        Integer rating = (Integer) params.get("rating");
        
        // A. 处理材料费 (支持 null 保护)
        Object costObj = params.get("materialCost");
        BigDecimal materialCost = costObj != null ? new BigDecimal(costObj.toString()) : BigDecimal.ZERO;
        
        // B. 🛠️ 语义对齐：获取“是否为人为损坏”标识
        // 该标识由师傅在现场判断，直接决定 Service 层是否执行 WalletTransaction 扣费
        Object humanObj = params.get("isHumanDamage");
        Boolean isHumanDamage = humanObj != null && Boolean.parseBoolean(humanObj.toString());
        
        // C. 调用具备“保底与事务”加固的 Service 方法
        repairService.finishRepair(orderId, comment, rating, materialCost, isHumanDamage);
        
        return R.okMsg("维修记录已归档，资产状态与房态已实时自愈。");
    }
    
    /**
     * 3. 挂起工单 (待大修处理)
     * 场景：现场发现需封锁宿舍进行长周期维修，调用此接口将资源强制下架。
     */
    @Operation(summary = "挂起/待大修处理")
    @SaCheckRole(value = {RoleConstants.REPAIR_MASTER, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @PostMapping("/suspend")
    public R<Void> suspend(@RequestBody Map<String, Object> params) {
        Long orderId = Long.valueOf(params.get("orderId").toString());
        String reason = (String) params.get("reason");
        
        repairService.suspendRepair(orderId, reason);
        return R.okMsg("工单已挂起，该房间资源已在系统中强制停用并锁定。");
    }
    
    /**
     * 4. 驳回无效报修
     * 场景：经核实为误报或恶意报修，释放资源锁定。
     */
    @Operation(summary = "驳回无效报修")
    @SaCheckRole(value = {RoleConstants.DORM_MANAGER, RoleConstants.SUPER_ADMIN}, mode = SaMode.OR)
    @PostMapping("/reject")
    public R<Void> reject(@RequestBody Map<String, Object> params) {
        Long orderId = Long.valueOf(params.get("orderId").toString());
        String reason = (String) params.get("reason");
        
        repairService.rejectRepair(orderId, reason);
        return R.okMsg("工单已驳回，相关资产已恢复正常状态。");
    }
    
    /**
     * 5. 用户评价 (学生端)
     * 🛡️ [防伪设计]：利用 LoginHelper 锁定评价人 ID，锁死越权评价路径。
     */
    @Operation(summary = "用户服务评价")
    @SaCheckLogin
    @PostMapping("/rate")
    public R<Void> rate(@RequestBody Map<String, Object> params) {
        Long orderId = Long.valueOf(params.get("orderId").toString());
        Integer rating = (Integer) params.get("rating");
        String comment = (String) params.get("comment");
        
        // 传入当前登录人 ID 进行防伪比对
        repairService.rateOrder(orderId, rating, comment, LoginHelper.getUserId());
        return R.okMsg("感谢您的评价，我们将不断提升服务质量！");
    }
    
    /**
     * 6. 角色感知分页查询
     * [数据隔离]：后端自动感应身份，实现“各司其职”的视图隔离。
     */
    @Operation(summary = "分页查询报修列表")
    @SaCheckLogin
    @GetMapping("/page")
    public R<Page<RepairOrder>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            RepairOrder query) {
        
        Page<RepairOrder> page = new Page<>(pageNum, pageSize);
        // 调用具备角色过滤逻辑的分页方法
        return R.ok((Page<RepairOrder>) repairService.selectOrderPage(page, query, LoginHelper.getUserId()));
    }
}