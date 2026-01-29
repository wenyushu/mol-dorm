package com.mol.dorm.biz.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.entity.UtilityBill;
import com.mol.dorm.biz.enums.BillStatusEnum;
import com.mol.dorm.biz.mapper.UtilityBillMapper;
import com.mol.dorm.biz.service.DormRoomService;
import com.mol.dorm.biz.service.UtilityBillService;
import com.mol.server.entity.SysCampus;
import com.mol.server.service.SysCampusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 水电费账单服务实现 (动态计价版)
 *
 * @author mol
 */
@Service
@RequiredArgsConstructor // 🟢 注入 Service 依赖
public class UtilityBillServiceImpl extends ServiceImpl<UtilityBillMapper, UtilityBill> implements UtilityBillService {
    
    private final DormRoomService roomService;
    private final SysCampusService campusService;
    
    // 兜底单价 (防止校区未配置时计算报错，也可选择直接报错)
    private static final BigDecimal DEFAULT_PRICE_COLD = new BigDecimal("3.5");
    private static final BigDecimal DEFAULT_PRICE_HOT = new BigDecimal("18.0");
    private static final BigDecimal DEFAULT_PRICE_ELEC = new BigDecimal("0.58");
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calculateAndSave(UtilityBill bill) {
        // 1. 获取计费上下文 (房间 -> 校区 -> 单价)
        if (bill.getRoomId() == null) {
            throw new ServiceException("生成账单失败：未关联房间 ID");
        }
        
        DormRoom room = roomService.getById(bill.getRoomId());
        if (room == null) {
            throw new ServiceException("生成账单失败：房间不存在");
        }
        
        // 获取校区配置
        SysCampus campus = campusService.getById(room.getCampusId());
        if (campus == null) {
            throw new ServiceException("生成账单失败：房间所属校区数据缺失");
        }
        
        // 2. 确定单价 (优先用校区配置，无配置则用兜底)
        BigDecimal priceCold = ObjectUtil.defaultIfNull(campus.getPriceWaterCold(), DEFAULT_PRICE_COLD);
        BigDecimal priceHot = ObjectUtil.defaultIfNull(campus.getPriceWaterHot(), DEFAULT_PRICE_HOT);
        BigDecimal priceElec = ObjectUtil.defaultIfNull(campus.getPriceElectric(), DEFAULT_PRICE_ELEC);
        
        // 3. 计算各项费用 (使用 Hutool 防止精度丢失)
        BigDecimal costCold = NumberUtil.mul(bill.getWaterCold(), priceCold);
        BigDecimal costHot = NumberUtil.mul(bill.getWaterHot(), priceHot);
        BigDecimal costElec = NumberUtil.mul(bill.getElectricUsage(), priceElec);
        
        bill.setCostWaterCold(costCold);
        bill.setCostWaterHot(costHot);
        bill.setCostElectric(costElec);
        
        // 4. 计算总价并保留两位小数 (ROUND_HALF_UP: 四舍五入)
        BigDecimal total = costCold.add(costHot).add(costElec);
        bill.setTotalCost(NumberUtil.round(total, 2));
        
        // 5. 设置初始状态
        if (bill.getPaymentStatus() == null) {
            bill.setPaymentStatus(BillStatusEnum.UNPAID.getCode());
        }
        
        // 保存或更新
        this.saveOrUpdate(bill);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payBill(Long billId, boolean success) {
        // 1. 查询账单
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
        
        // 3. 支付处理
        if (success) {
            bill.setPaymentStatus(BillStatusEnum.PAID.getCode());
            bill.setPayTime(LocalDateTime.now());
            // TODO: 这里未来可以扩展：扣除学生账户余额、发送通知等
            
        } else {
            bill.setPaymentStatus(BillStatusEnum.FAILED.getCode());
        }
        
        // 4. 更新数据库 (乐观锁 version 控制并发)
        if (!this.updateById(bill)) {
            throw new ServiceException("支付并发冲突，请刷新后重试");
        }
    }
}