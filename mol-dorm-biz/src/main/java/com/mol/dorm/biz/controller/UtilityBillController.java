package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.UtilityBill;
import com.mol.dorm.biz.service.UtilityBillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "水电费管理")
@RestController
@RequestMapping("/utility")
@RequiredArgsConstructor
public class UtilityBillController {
    
    private final UtilityBillService utilityBillService;
    
    @Operation(summary = "录入/生成账单 (宿管用)")
    @SaCheckRole(RoleConstants.DORM_MANAGER)
    @PostMapping("/generate")
    public R<Void> generate(@RequestBody UtilityBill bill) {
        utilityBillService.calculateAndSave(bill);
        return R.ok();
    }
    
    @Operation(summary = "模拟支付 (学生用)")
    @SaCheckRole(RoleConstants.STUDENT)
    @PostMapping("/pay/{billId}")
    public R<String> pay(@PathVariable Long billId, @RequestParam(defaultValue = "true") Boolean simulateSuccess) {
        try {
            utilityBillService.payBill(billId, simulateSuccess);
            if (simulateSuccess) {
                return R.ok("支付成功！");
            } else {
                return R.fail("模拟支付失败：余额不足或银行拒绝");
            }
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }
    
    @Operation(summary = "查看我的账单")
    @GetMapping("/{id}")
    public R<UtilityBill> getInfo(@PathVariable Long id) {
        return R.ok(utilityBillService.getById(id));
    }
}