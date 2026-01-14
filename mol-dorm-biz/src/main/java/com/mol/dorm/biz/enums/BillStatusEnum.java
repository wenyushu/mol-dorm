package com.mol.dorm.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 水电费账单状态枚举
 */
@Getter
@AllArgsConstructor
public enum BillStatusEnum {
    
    UNPAID(0, "待支付/未缴"),
    PAID(1, "支付成功/已缴"),
    OVERDUE(2, "已逾期"),
    CANCELLED(3, "已取消/作废"),
    FAILED(4, "支付失败"); // 模拟支付异常
    
    private final Integer code;
    private final String msg;
}