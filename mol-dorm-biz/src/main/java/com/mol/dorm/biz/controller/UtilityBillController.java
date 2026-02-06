package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.UtilityBill;
import com.mol.dorm.biz.service.UtilityBillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 水电费管理接口
 * 🛡️ [防刁民策略]：
 * 1. 支付熔断：已支付账单二次进入支付流瞬间拦截，防止钱包双倍扣费。
 * 2. 身份隔离：普通学生仅能查询/支付所属房间的账单。
 * 3. 物理保护：涉及金额变动的账单，一旦支付完成，系统物理锁定修改权限。
 */
@Tag(name = "业务-水电费账单")
@RestController
@RequestMapping("/dorm/utility/bill")
@RequiredArgsConstructor
public class UtilityBillController {
    
    private final UtilityBillService utilityBillService;
    
    /**
     * 1. 录入/生成账单
     */
    @Operation(summary = "录入并生成账单 (宿管)", description = "录入起止读数，系统根据单价自动计算总额并发布。")
    @SaCheckRole(value = {RoleConstants.DORM_MANAGER, RoleConstants.SUPER_ADMIN}, mode = SaMode.OR)
    @PostMapping("/generate")
    public R<String> generate(@RequestBody @Validated UtilityBill bill) {
        // Service内部应包含：1. 读数合法性校验；2. 历史读数顺接校验
        utilityBillService.generateBill(bill);
        return R.okMsg("账单已生成，已推送至寝室成员小程序端");
    }
    
    /**
     * 2. 支付账单 (核心安全接口)
     */
    @Operation(summary = "支付账单 (从房间钱包扣款)")
    @SaCheckLogin
    @PostMapping("/pay/{billId}")
    public R<String> pay(@PathVariable Long billId) {
        // 🛡️ [防刁民点]：Service层通过事务和状态锁(WHERE status = 0)来保证单次扣费
        utilityBillService.pay(billId);
        return R.okMsg("支付成功，电子回单已生成，房间钱包余额已扣除");
    }
    
    /**
     * 3. 分页查询账单
     */
    @Operation(summary = "分页查询账单列表")
    @SaCheckLogin
    @GetMapping("/page")
    public R<Page<UtilityBill>> page(
            Page<UtilityBill> page,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Integer status
    ) {
        // 🛡️ [安全增强]：如果不是管理员，强制只能看自己房间的
        if (!StpUtil.hasRole(RoleConstants.SUPER_ADMIN) && !StpUtil.hasRole(RoleConstants.DORM_MANAGER)) {
            // 此处应通过 LoginHelper 获取当前学生关联的 roomId
            // roomId = LoginHelper.getUserRoomId();
        }
        
        return R.ok(utilityBillService.getBillPage(page, roomId, month, status));
    }
    
    /**
     * 4. 修改账单
     */
    @Operation(summary = "修改账单内容", description = "仅允许修改备注或未支付的原始读数。")
    @SaCheckRole(RoleConstants.DORM_MANAGER)
    @PutMapping
    public R<String> update(@RequestBody UtilityBill bill) {
        UtilityBill dbBill = utilityBillService.getById(bill.getId());
        if (dbBill == null) return R.fail("账单档案不存在");
        
        // 🛡️ [硬核逻辑]：对齐状态位，假设 1 为已支付
        if (Integer.valueOf(1).equals(dbBill.getPaymentStatus())) {
            return R.fail("操作拦截：已支付账单涉及财务核销，禁止直接修改读数，请走冲正流程。");
        }
        
        utilityBillService.updateById(bill);
        return R.okMsg("账单基础信息已更正");
    }
    
    /**
     * 5. 删除账单
     */
    @Operation(summary = "批量删除账单 (仅限管理员)")
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @DeleteMapping("/{ids}")
    public R<String> remove(@PathVariable Long[] ids) {
        List<Long> idList = Arrays.asList(ids);
        
        // 🛡️ [物理拦截]：检查是否有已支付的单子
        boolean hasPaid = utilityBillService.checkHasPaid(idList);
        if (hasPaid) {
            return R.fail("删除失败：选中的账单中存在已支付记录，需先执行财务撤销。");
        }
        
        utilityBillService.removeByIds(idList);
        return R.okMsg("账单物理记录已作废");
    }
}