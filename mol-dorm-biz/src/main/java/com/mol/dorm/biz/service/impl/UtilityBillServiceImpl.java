package com.mol.dorm.biz.service.impl;

import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.UtilityBill;
import com.mol.dorm.biz.enums.BillStatusEnum;
import com.mol.dorm.biz.mapper.UtilityBillMapper;
import com.mol.dorm.biz.service.UtilityBillService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class UtilityBillServiceImpl extends ServiceImpl<UtilityBillMapper, UtilityBill> implements UtilityBillService {
    
    // 模拟单价配置 (实际项目中应从 sys_config 表读取)
    private static final BigDecimal PRICE_WATER_COLD = new BigDecimal("3.5"); // 冷水 3.5元/吨
    private static final BigDecimal PRICE_WATER_HOT = new BigDecimal("18.0"); // 热水 18元/吨 (通常比较贵)
    private static final BigDecimal PRICE_ELEC = new BigDecimal("0.58");      // 电费 0.58元/度
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calculateAndSave(UtilityBill bill) {
        // 1. 计算各项费用
        // NumberUtil.mul 是 Hutool 工具类，防止精度丢失，也可以直接用 BigDecimal.multiply
        BigDecimal costCold = NumberUtil.mul(bill.getWaterCold(), PRICE_WATER_COLD);
        BigDecimal costHot = NumberUtil.mul(bill.getWaterHot(), PRICE_WATER_HOT);
        BigDecimal costElec = NumberUtil.mul(bill.getElectricUsage(), PRICE_ELEC);
        
        bill.setCostWaterCold(costCold);
        bill.setCostWaterHot(costHot);
        bill.setCostElectric(costElec);
        
        // 2. 计算总价
        BigDecimal total = costCold.add(costHot).add(costElec);
        bill.setTotalCost(total);
        
        // 3. 初始状态
        if (bill.getPaymentStatus() == null) {
            bill.setPaymentStatus(BillStatusEnum.UNPAID.getCode());
        }
        
        this.saveOrUpdate(bill);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payBill(Long billId, boolean success) {
        // 1. 查询账单 (利用乐观锁 version)
        UtilityBill bill = this.getById(billId);
        if (bill == null) {
            throw new ServiceException("账单不存在");
        }
        
        // 2. 状态检查
        if (BillStatusEnum.PAID.getCode().equals(bill.getPaymentStatus())) {
            throw new ServiceException("该账单已支付，请勿重复操作");
        }
        if (BillStatusEnum.CANCELLED.getCode().equals(bill.getPaymentStatus())) {
            throw new ServiceException("账单已作废");
        }
        
        // 3. 模拟支付逻辑
        if (success) {
            // 支付成功
            bill.setPaymentStatus(BillStatusEnum.PAID.getCode());
            bill.setPayTime(LocalDateTime.now());
        } else {
            // 支付失败 (演示用)
            bill.setPaymentStatus(BillStatusEnum.FAILED.getCode());
            // 失败时不更新支付时间
        }
        
        // 4. 更新数据库 (乐观锁生效)
        boolean updateResult = this.updateById(bill);
        if (!updateResult) {
            throw new ServiceException("支付并发冲突，请刷新后重试");
        }
    }
}