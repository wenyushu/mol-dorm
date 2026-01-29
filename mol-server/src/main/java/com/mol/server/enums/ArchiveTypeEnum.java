package com.mol.server.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 归档/异动类型枚举
 * <p>
 * 💡 防刁民设计：
 * 使用枚举限制入参，防止前端瞎传一个 "999" 这种不存在的状态导致系统逻辑崩溃。
 * </p>
 */
@Getter
@AllArgsConstructor
public enum ArchiveTypeEnum {
    
    // --- ✅ 正常流转 ---
    GRADUATION(10, "正常毕业"),       // 学生毕业离校
    RESIGNATION(20, "教工离职"),      // 宿管/辅导员离职
    
    // --- 🤖 系统自动处理 ---
    INACTIVE_FREEZE(30, "长期不活跃冻结"),   // 僵尸号清理
    SUSPENSION_EXPIRED(52, "休学期满自动退学"), // 休学超过2年未复学 (System Task 触发)
    
    // --- ⚠️ 人工干预 (不可逆) ---
    DROP_OUT_VOLUNTARY(40, "主动退学"),      // 学生申请退学
    DROP_OUT_EXPELLED(41, "勒令退学"),       // 违纪开除 (严重)
    
    // --- ⏸️ 人工干预 (可恢复 - 需保留学籍) ---
    SUSPENSION_MEDICAL(50, "因病休学"),      // 身体原因
    SUSPENSION_PERSONAL(51, "因事休学");     // 创业/家庭原因

    
    private final Integer code;
    private final String desc;
    
    /**
     * 根据 code 获取枚举 (用于 Controller 参数转换)
     */
    public static ArchiveTypeEnum getByCode(Integer code) {
        for (ArchiveTypeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}