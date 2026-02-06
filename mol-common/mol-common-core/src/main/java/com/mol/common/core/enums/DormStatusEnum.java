package com.mol.common.core.enums;

import com.mol.common.core.constant.DormConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 宿舍系统标准化状态枚举 - 语义化对齐版
 */
@Getter
@AllArgsConstructor
public enum DormStatusEnum {
    
    // --- 生命周期 (LC) - 决定了能不能“进去” ---
    STOP(DormConstants.LC_STOP, "停止使用", "gray"),
    UNUSED(DormConstants.LC_UNUSED, "基建未启用", "blue"),
    NORMAL(DormConstants.LC_NORMAL, "正常运行", "green"),
    PAUSE(DormConstants.LC_PAUSE, "行政暂停使用", "orange"),
    DECORATING(DormConstants.LC_DECORATING, "内部装修中", "orange"),
    REPAIRING(DormConstants.LC_REPAIRING, "设施维修中", "red"),
    DAMAGED(DormConstants.LC_DAMAGED, "物理损坏报废", "dark-red"),
    RESERVED(DormConstants.LC_RESERVED, "特殊行政预留", "purple"),
    
    // --- 资源状态 (RES) - 决定了有没有“位置” ---
    RES_EMPTY(DormConstants.RES_EMPTY, "完全空闲", "success"),
    RES_USING(DormConstants.RES_USING, "正常使用", "primary"),
    RES_NOT_FULL(DormConstants.RES_NOT_FULL, "尚有空位", "warning"),
    RES_SUFFICIENT(24, "资源充裕", "success"), // 对应计算逻辑
    RES_TENSE(25, "资源紧张", "danger"),      // 对应计算逻辑
    RES_FULL(DormConstants.RES_FULL, "全员满额", "danger");
    
    private final int code;
    private final String desc;
    private final String color;
    
    /**
     * 安全转换：根据代码获取枚举
     */
    public static DormStatusEnum fromCode(Integer code) {
        if (code == null) return STOP;
        for (DormStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        return STOP;
    }
}