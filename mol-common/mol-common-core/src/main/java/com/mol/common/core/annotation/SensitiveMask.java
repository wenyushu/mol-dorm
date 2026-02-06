package com.mol.common.core.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mol.common.core.mask.MaskStrategy;
import com.mol.common.core.mask.SensitiveMaskSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段脱敏注解
 * 🛡️ 挂载此注解后，Jackson 序列化时会自动根据权限判定是否脱敏
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveMaskSerializer.class)
public @interface SensitiveMask {
    /**
     * 脱敏策略
     */
    MaskStrategy value();
}