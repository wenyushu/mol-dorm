package com.mol.dorm.biz.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BuildingBillSummaryVO {
    private BigDecimal totalIncome = BigDecimal.ZERO;  // 总收入
    private BigDecimal totalOutcome = BigDecimal.ZERO; // 总支出
    private String month;                              // 统计月份
}
