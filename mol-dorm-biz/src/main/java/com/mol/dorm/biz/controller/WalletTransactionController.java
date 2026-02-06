package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.util.LoginHelper;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.WalletTransaction;
import com.mol.dorm.biz.service.DormBedService;
import com.mol.dorm.biz.service.WalletTransactionService;
import com.mol.dorm.biz.vo.BuildingBillSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 财务流水/账单管理
 */
@Tag(name = "账单管理")
@RestController
@RequestMapping("/api/dorm/bill")
@RequiredArgsConstructor
public class WalletTransactionController {
    
    private final WalletTransactionService trxService;
    private final DormBedService bedService;
    
    /**
     * 分页查询账单流水
     */
    @Operation(summary = "分页查询月度账单/流水")
    @SaCheckLogin
    @GetMapping("/page")
    public R<Page<WalletTransaction>> getBillPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Integer trxType) {
        
        Page<WalletTransaction> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WalletTransaction> wrapper = new LambdaQueryWrapper<>();
        
        // 🛡️ [权限熔断]：非管理员角色，强制锁定只能查询本寝室账单
        if (!LoginHelper.isAdmin()) {
            DormBed bed = bedService.getOne(new LambdaQueryWrapper<DormBed>()
                    .eq(DormBed::getOccupantId, LoginHelper.getUserId())
                    .last("LIMIT 1"));
            if (bed == null) return R.failMsg("未找到您的入住信息");
            roomId = bed.getRoomId();
        }
        
        wrapper.eq(roomId != null, WalletTransaction::getRoomId, roomId)
                .eq(trxType != null, WalletTransaction::getTrxType, trxType)
                .orderByDesc(WalletTransaction::getCreateTime);
        
        return R.ok(trxService.page(page, wrapper));
    }
    
    /**
     * 【楼栋收支汇总】
     * 🛡️ [权限穿透]：自动识别宿管身份，锁定其管理的楼栋
     */
    @Operation(summary = "楼栋月度收支汇总", description = "展示宿管辖区内所有房间的月度财务总览")
    @GetMapping("/summary")
    public R<BuildingBillSummaryVO> getBuildingSummary( // 修改泛型为 BuildingBillSummaryVO
                                                        @RequestParam(required = false) Long buildingId,
                                                        @RequestParam String month) {
        
        // 🛡️ [权限锁定逻辑精简版]
        if (!LoginHelper.isAdmin()) {
            Long managedId = LoginHelper.getManagedBuildingId();
            if (managedId == null) {
                return R.failMsg("操作失败：当前管理账户未绑定负责楼栋");
            }
            // 强制覆盖前端参数，防止横向越权查其他楼
            buildingId = managedId;
        }
        
        if (buildingId == null) {
            return R.failMsg("无法确定查询楼栋，请联系系统管理员");
        }
        
        // 调用 Service 返回 VO，红号彻底消失
        return R.ok(trxService.getBuildingMonthlyStats(buildingId, month));
    }
}