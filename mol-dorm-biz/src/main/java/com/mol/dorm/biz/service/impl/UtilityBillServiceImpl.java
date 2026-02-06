package com.mol.dorm.biz.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.*;
import com.mol.dorm.biz.enums.BillStatusEnum;
import com.mol.dorm.biz.mapper.*;
import com.mol.dorm.biz.service.UtilityBillService;
import com.mol.server.entity.SysCampus;
import com.mol.server.mapper.SysCampusMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 水电费账单业务核心实现类 - 严苛防刁民版
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UtilityBillServiceImpl extends ServiceImpl<UtilityBillMapper, UtilityBill> implements UtilityBillService {
    
    private final DormRoomMapper roomMapper;
    private final DormBuildingMapper buildingMapper;
    private final SysCampusMapper campusMapper;
    private final DormRoomWalletMapper walletMapper;
    
    // === [严苛风控配置] ===
    private static final BigDecimal SUBSIDY_ELEC = new BigDecimal("6.0"); // 补贴
    private static final BigDecimal MAX_USAGE_WATER = new BigDecimal("500");
    private static final BigDecimal MAX_USAGE_ELEC = new BigDecimal("3000");
    private static final BigDecimal ALERT_THRESHOLD = new BigDecimal("10.0"); // 10元预警线
    private static final Pattern MONTH_PATTERN = Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])$");
    
    // 前置操作：检查选中的账单中是否存在已支付的记录
    @Override
    public boolean checkHasPaid(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return false;
        
        // 🛡️ [防刁民逻辑]：在数据库中直接 count 状态为 1 (已支付) 的记录
        // 假设 1 代表已支付，请根据你数据库的真实 payment_status 对应
        long count = this.query()
                .in("id", ids)
                .eq("payment_status", 1)
                .count();
        
        return count > 0;
    }
    
    /**
     * 1. 账单生成（含严苛校验）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateBill(UtilityBill bill) {
        // [防刁民 A]：基础参数审计
        auditBillBasic(bill);
        
        // [防刁民 B]：用量风控审计（负数、超大数拦截）
        validateUsage("冷水", bill.getWaterCold(), MAX_USAGE_WATER);
        validateUsage("热水", bill.getWaterHot(), MAX_USAGE_WATER);
        validateUsage("用电", bill.getElectricUsage(), MAX_USAGE_ELEC);
        
        // 定价与计算
        SysCampus campus = lookupCampusInfo(bill.getRoomId());
        calculateCosts(bill, campus);
        
        bill.setPaymentStatus(BillStatusEnum.UNPAID.getCode()).setStatus(1);
        
        if (!this.save(bill)) throw new ServiceException("数据库繁忙，账单保存失败");
        
        // [防刁民 C]：生成后立即触发余额清算与预警
        this.autoPayBill(bill.getId());
    }
    
    /**
     * 2. 账单自动清算
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoPayBill(Long billId) {
        UtilityBill bill = this.getById(billId);
        if (bill == null || BillStatusEnum.PAID.getCode().equals(bill.getPaymentStatus())) return;
        
        // 尝试自动支付
        try {
            this.pay(billId);
        } catch (ServiceException e) {
            // 支付失败不回滚，仅记录并触发余额不足提示
            log.warn(">>> [清算提醒] 房间 {} 余额不足，无法自动扣费：{}", bill.getRoomId(), e.getMessage());
            this.checkWalletSufficiency(bill.getRoomId());
        }
    }
    
    /**
     * 3. 核心原子支付
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(Long billId) {
        UtilityBill bill = this.getById(billId);
        if (bill == null) throw new ServiceException("账单记录丢失");
        
        // [防刁民 D]：幂等性拦截
        if (BillStatusEnum.PAID.getCode().equals(bill.getPaymentStatus())) {
            throw new ServiceException("操作冲突：账单已支付。");
        }
        
        // 0元账单逻辑
        if (bill.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            finishBillPayment(bill);
            return;
        }
        
        // [防刁民 E]：金融级原子扣款
        // 此处必须使用我们在 Mapper 里写的带有 balance >= amount 校验的 SQL
        int rows = walletMapper.deductBalance(bill.getRoomId(), bill.getTotalAmount());
        
        if (rows == 0) {
            throw new ServiceException(StrUtil.format("支付失败：房间 {} 余额不足！应付：{}", bill.getRoomId(), bill.getTotalAmount()));
        }
        
        finishBillPayment(bill);
        // 支付成功后也要体检，看看剩多少钱，是否需要充值
        this.checkWalletSufficiency(bill.getRoomId());
    }
    
    /**
     * 4. 🟢 余额健康审计（低余额预警）
     */
    @Override
    public void checkWalletSufficiency(Long roomId) {
        DormRoomWallet wallet = walletMapper.selectOne(Wrappers.<DormRoomWallet>lambdaQuery().eq(DormRoomWallet::getRoomId, roomId));
        if (wallet == null) return;
        
        BigDecimal balance = wallet.getBalance();
        
        // 统计房间所有未缴费账单
        List<UtilityBill> unpaid = this.list(Wrappers.<UtilityBill>lambdaQuery()
                .eq(UtilityBill::getRoomId, roomId)
                .eq(UtilityBill::getPaymentStatus, BillStatusEnum.UNPAID.getCode()));
        
        BigDecimal totalDebt = unpaid.stream().map(UtilityBill::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // [防刁民 F]：差异化预警
        if (balance.compareTo(totalDebt) < 0) {
            log.error("🛑 [停机预警] 房间 {} 余额 {} 已不足以支付历史欠费 {}！", roomId, balance, totalDebt);
            // 这里可以根据实际情况 throw 异常或发送站内信
        } else if (balance.compareTo(ALERT_THRESHOLD) <= 0) {
            log.warn("🚩 [充值提醒] 房间 {} 余额 {} 低于安全水位（10元），请通知学生充值。", roomId, balance);
        }
    }
    
    // ==========================================
    // 🛡️ [防刁民核心逻辑细节]
    // ==========================================
    
    private void auditBillBasic(UtilityBill bill) {
        if (bill.getRoomId() == null || StrUtil.isBlank(bill.getMonth())) {
            throw new ServiceException("非法参数：房间与月份缺失");
        }
        // 1. 月份格式审计（拦截 2026-14）
        if (!MONTH_PATTERN.matcher(bill.getMonth()).matches()) {
            throw new ServiceException("月份格式错误！示例：2026-02");
        }
        // 2. 时空一致性审计（禁止预支未来）
        Date billDate = DateUtil.parse(bill.getMonth(), "yyyy-MM");
        if (DateUtil.beginOfMonth(billDate).isAfter(DateUtil.beginOfMonth(new Date()))) {
            throw new ServiceException("操作拦截：禁止生成未来月份的账单！");
        }
        // 3. 财务审计（禁止重复入账）
        Long count = this.count(Wrappers.<UtilityBill>lambdaQuery()
                .eq(UtilityBill::getRoomId, bill.getRoomId())
                .eq(UtilityBill::getMonth, bill.getMonth()));
        if (count > 0) throw new ServiceException("计费重复：该房间当月账单已锁定，请先作废旧账单。");
    }
    
    private void validateUsage(String type, BigDecimal value, BigDecimal max) {
        if (value == null) return;
        // 4. 负数审计（防止通过反向数值刷余额）
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException(type + "用量不能为负数！");
        }
        // 5. 巨额审计（防录入失误或接口攻击）
        if (value.compareTo(max) > 0) {
            throw new ServiceException(StrUtil.format("熔断拦截：{}用量异常（{}），超过系统阈值！", type, value));
        }
    }
    
    private void calculateCosts(UtilityBill bill, SysCampus campus) {
        // 水费计算
        bill.setCostWaterCold(NumberUtil.mul(bill.getWaterCold(), campus.getPriceWaterCold()));
        bill.setCostWaterHot(NumberUtil.mul(bill.getWaterHot(), campus.getPriceWaterHot()));
        
        // 电费计算（带 6度 补贴逻辑，且补贴不能导致费用为负）
        BigDecimal billableElec = bill.getElectricUsage().subtract(SUBSIDY_ELEC);
        billableElec = billableElec.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : billableElec;
        bill.setCostElectric(NumberUtil.mul(billableElec, campus.getPriceElectric()));
        
        // 汇总（四舍五入保留2位）
        BigDecimal total = bill.getCostWaterCold().add(bill.getCostWaterHot()).add(bill.getCostElectric());
        bill.setTotalAmount(NumberUtil.round(total, 2));
    }
    
    private SysCampus lookupCampusInfo(Long roomId) {
        DormRoom room = roomMapper.selectById(roomId);
        if (room == null) throw new ServiceException("档案缺失：目标房间不存在");
        DormBuilding building = buildingMapper.selectById(room.getBuildingId());
        SysCampus campus = campusMapper.selectById(building.getCampusId());
        if (campus == null) throw new ServiceException("财务异常：校区定价未配置");
        return campus;
    }
    
    private void finishBillPayment(UtilityBill bill) {
        bill.setPaymentStatus(BillStatusEnum.PAID.getCode());
        bill.setPayTime(LocalDateTime.now());
        this.updateById(bill);
    }
    
    @Override
    public Page<UtilityBill> getBillPage(Page<UtilityBill> page, Long roomId, String month, Integer status) {
        return this.page(page, Wrappers.<UtilityBill>lambdaQuery()
                .eq(roomId != null, UtilityBill::getRoomId, roomId)
                .eq(StrUtil.isNotBlank(month), UtilityBill::getMonth, month)
                .eq(status != null, UtilityBill::getPaymentStatus, status)
                .orderByAsc(UtilityBill::getPaymentStatus)
                .orderByDesc(UtilityBill::getMonth));
    }
}