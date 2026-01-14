package com.mol.dorm.biz.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.entity.UtilityBill;
import com.mol.dorm.biz.enums.BillStatusEnum;
import com.mol.dorm.biz.mapper.DormRoomMapper;
import com.mol.dorm.biz.mapper.UtilityBillMapper;
import com.mol.dorm.biz.service.UtilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class UtilityServiceImpl extends ServiceImpl<UtilityBillMapper, UtilityBill> implements UtilityService {
    
    private final DormRoomMapper roomMapper;
    
    // === 计费常量配置 ===
    private static final BigDecimal PRICE_WATER_COLD = new BigDecimal("3.5");  // 冷水 3.5元/吨
    private static final BigDecimal PRICE_WATER_HOT = new BigDecimal("15.0");  // 热水 15元/吨
    private static final BigDecimal PRICE_ELEC = new BigDecimal("0.6");        // 电费 0.6元/度
    private static final BigDecimal SUBSIDY_ELEC = new BigDecimal("6.0");      // 补贴: 每月赠送6度
    
    // [防刁民] 阈值设置：防止输入错误或恶意攻击
    private static final BigDecimal MAX_USAGE_LIMIT = new BigDecimal("5000"); // 单月单项用量不可能超过5000
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateBill(UtilityBill bill) {
        log.info(">>> 开始生成账单: 房间ID={}, 月份={}", bill.getRoomId(), bill.getMonth());
        
        // 1. [防刁民] 基础参数校验：防止空指针或非法格式
        if (bill.getRoomId() == null || StrUtil.isBlank(bill.getMonth())) {
            throw new ServiceException("房间 ID 和账单月份不能为空");
        }
        
        // 2. [防刁民] 校验房间是否存在且状态正常
        DormRoom room = roomMapper.selectById(bill.getRoomId());
        if (room == null) {
            throw new ServiceException("非法操作：房间不存在");
        }
        // 如果房间正在维修中，是否允许生成账单？通常允许（因为维修也可能用电），视业务而定。
        
        // 3. [防刁民] 数据合法性校验：禁止负数（反向发电）和 异常巨量（手抖多输个0）
        validateUsage("冷水", bill.getWaterCold());
        validateUsage("热水", bill.getWaterHot());
        validateUsage("用电", bill.getElectricUsage());
        
        // 4. [防刁民] 禁止“未来穿越”：不能生成下个月的账单
        String currentMonth = DateUtil.format(new Date(), "yyyy-MM");
        if (bill.getMonth().compareTo(currentMonth) > 0) {
            throw new ServiceException("无法生成未来月份的账单，请检查服务器时间或月份参数");
        }
        
        // 5. [防刁民] 防止“无限补贴”漏洞：严格限制【一房一月一单】
        // 如果允许一个月生成多张单子，学生就会把100度电拆成20张5度的单子，每张都扣减6度补贴，最后电费为0。
        Long count = this.baseMapper.selectCount(Wrappers.<UtilityBill>lambdaQuery()
                .eq(UtilityBill::getRoomId, bill.getRoomId())
                .eq(UtilityBill::getMonth, bill.getMonth()));
        if (count > 0) {
            throw new ServiceException("该房间本月账单已存在！如需修改，请先撤销旧账单。");
        }
        
        // === 核心计费逻辑 ===
        
        // 计算水费
        BigDecimal coldCost = NumberUtil.mul(bill.getWaterCold(), PRICE_WATER_COLD);
        BigDecimal hotCost = NumberUtil.mul(bill.getWaterHot(), PRICE_WATER_HOT);
        bill.setCostWaterCold(coldCost);
        bill.setCostWaterHot(hotCost);
        
        // 计算电费
        BigDecimal usageElec = bill.getElectricUsage();
        
        // 6. [防刁民] 补贴逻辑守门员：扣减补贴后不能为负数
        // 即使只用了 3 度电 (补贴6度)，费用也是 0，而不是系统倒贴钱 (-3 * 0.6)
        BigDecimal billableElec = usageElec.subtract(SUBSIDY_ELEC);
        if (billableElec.compareTo(BigDecimal.ZERO) < 0) {
            billableElec = BigDecimal.ZERO;
        }
        
        BigDecimal elecCost = NumberUtil.mul(billableElec, PRICE_ELEC);
        bill.setCostElectric(elecCost);
        
        // 计算总价
        BigDecimal total = coldCost.add(hotCost).add(elecCost);
        bill.setTotalCost(total);
        
        // 初始化状态
        bill.setPaymentStatus(BillStatusEnum.UNPAID.getCode());
        
        // 保存入库
        boolean saveResult = this.save(bill);
        if (!saveResult) {
            throw new ServiceException("系统繁忙，账单生成失败");
        }
        
        log.info("<<< 账单生成完毕: 总额={}, 电费补贴已扣除", total);
    }
    
    /**
     * [防刁民] 内部校验方法：检查数值是否合法
     */
    private void validateUsage(String type, BigDecimal value) {
        if (value == null) {
            // 如果为空，默认设为0，防止后续空指针
            value = BigDecimal.ZERO;
        }
        // 1. 禁止负数
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException(String.format("%s用量不能为负数！请检查读数。", type));
        }
        // 2. 禁止异常巨量 (比如把 100.5 输成了 1005)
        if (value.compareTo(MAX_USAGE_LIMIT) > 0) {
            throw new ServiceException(String.format("%s用量数值异常过大(>%s)，请确认是否输入错误！", type, MAX_USAGE_LIMIT));
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(Long billId) {
        // 1. [防刁民] 乐观锁前置查询
        UtilityBill bill = this.getById(billId);
        if (bill == null) {
            throw new ServiceException("账单不存在，可能已被删除");
        }
        
        // 2. [防刁民] 幂等性检查：防止重复支付
        // 如果状态已经是 PAID，直接拦截。防止前端连点两次导致扣两次余额（如果接入了钱包系统）。
        if (BillStatusEnum.PAID.getCode().equals(bill.getPaymentStatus())) {
            throw new ServiceException("该账单已支付，请勿重复操作");
        }
        
        // 3. [防刁民] 状态检查：作废账单不能支付
        if (BillStatusEnum.CANCELLED.getCode().equals(bill.getPaymentStatus())) {
            throw new ServiceException("该账单已作废，无法支付");
        }
        
        // 更新状态
        bill.setPaymentStatus(BillStatusEnum.PAID.getCode());
        bill.setPayTime(LocalDateTime.now());
        
        // 4. [防刁民] 乐观锁更新 (需配合实体类的 @Version 注解)
        // 这一步是最后的防线。如果两个线程同时通过了上面的检查，updateById 会因为 version 不匹配而失败一个。
        boolean update = this.updateById(bill);
        if (!update) {
            throw new ServiceException("支付状态冲突，请刷新后重试");
        }
    }
    
    @Override
    public Page<UtilityBill> getBillPage(Page<UtilityBill> page, Long roomId, String month, Integer status) {
        LambdaQueryWrapper<UtilityBill> wrapper = Wrappers.lambdaQuery();
        
        // 动态拼接条件
        wrapper.eq(roomId != null, UtilityBill::getRoomId, roomId);
        wrapper.eq(StrUtil.isNotBlank(month), UtilityBill::getMonth, month);
        wrapper.eq(status != null, UtilityBill::getPaymentStatus, status);
        
        // 排序：未支付的优先，然后按月份倒序
        // 这样学生一进来就能看到要交钱的单子
        wrapper.orderByAsc(UtilityBill::getPaymentStatus)
                .orderByDesc(UtilityBill::getMonth);
        
        return this.page(page, wrapper);
    }
}