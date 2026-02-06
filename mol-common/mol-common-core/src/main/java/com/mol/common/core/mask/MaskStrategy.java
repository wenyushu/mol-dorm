package com.mol.common.core.mask;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 宿舍系统脱敏字段策略
 * 🛡️ 仅针对身份证、联系方式、详细住址进行隐私保护
 */
@Getter
@AllArgsConstructor
public enum MaskStrategy {
    /** 身份证号 (4201 ********** 1234) */
    ID_CARD,
    
    /** 手机号 / 紧急联系人电话 (138****5678) */
    PHONE,
    
    /** 家庭居住地址 (仅保留省市，详细门牌号脱敏) */
    HOME_ADDRESS,
    
    /** 校外居住地址 (完全脱敏，非特权阶级不可见) */
    OUTSIDE_ADDRESS
}