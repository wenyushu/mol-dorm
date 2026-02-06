package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.DormRoomWallet;
import com.mol.dorm.biz.entity.RechargeLog;
import com.mol.dorm.biz.service.DormRoomWalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 房间钱包管理控制层
 * 🛡️ [财务审计版]：
 * 1. 动账存证：每一笔充值都会在 biz_recharge_log 产生快照（记录充值前后的 balance）。
 * 2. 幂等校验：利用 orderNo 锁死重复提交。
 */
@Tag(name = "房间钱包管理")
@RestController
@RequestMapping("/dorm/wallet")
@RequiredArgsConstructor
public class DormRoomWalletController {
    
    private final DormRoomWalletService walletService;
    
    /**
     * 查询房间的余额
     */
    @Operation(summary = "查询房间余额及钱包详情")
    @SaCheckLogin // 🔒 登录即可查询
    @GetMapping("/{roomId}")
    public R<DormRoomWallet> getBalance(@PathVariable Long roomId) {
        return R.ok(walletService.getWalletByRoomId(roomId));
    }
    
    /**
     * 新增：查询房间充值流水明细
     * 场景：学生在手机端查看“我的账单”，管理员在后台进行财务核对。
     */
    @Operation(summary = "查询房间充值流水明细", description = "获取指定房间的历史充值记录，包含充值前后的金额变化。")
    @SaCheckLogin
    @GetMapping("/logs/{roomId}")
    public R<Page<RechargeLog>> getRechargeLogs(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Page<RechargeLog> page = new Page<>(pageNum, pageSize);
        // 此方法需要在 DormRoomWalletService 中定义
        return R.ok(walletService.getRechargePage(page, roomId));
    }
    
    /**
     * 房间充值
     * 🛡️ [防刁民逻辑]：自动抓取操作人 ID，严禁前端伪造。
     */
    @Operation(summary = "房间在线充值")
    @SaCheckLogin
    @PostMapping("/recharge")
    public R<Void> recharge(@RequestBody Map<String, Object> params) {
        Long roomId = Long.valueOf(params.get("roomId").toString());
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String orderNo = (String) params.get("orderNo");
        
        // 🛡️ [安全点]：直接通过 Token 获取当前操作人 ID
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        walletService.recharge(roomId, amount, orderNo, currentUserId);
        return R.okMsg("充值指令已处理，资金已实时到账");
    }
}