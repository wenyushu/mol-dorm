package com.mol.common.core.config;

import java.beans.PropertyEditorSupport;

/**
 * 自定义字符串编辑器
 * 功能：
 * 1. 去除首尾空格
 * 2. 空字符串转 null
 * 3. [核心] 中英文数字之间自动加空格 (盘古之白)
 */
public class AutoFormatStringEditor extends PropertyEditorSupport {
    
    private final boolean emptyAsNull;
    
    public AutoFormatStringEditor(boolean emptyAsNull) {
        this.emptyAsNull = emptyAsNull;
    }
    
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null) {
            setValue(null);
            return;
        }
        
        // 1. 去除首尾空格
        String value = text.trim();
        
        // 2. 处理空字符串
        if (this.emptyAsNull && "".equals(value)) {
            setValue(null);
            return;
        }
        
        /* 3. 执行 “盘古之白” 自动加空格逻辑
         正则含义：在 [汉字] 和 [字母数字] 之间，或者 [字母数字] 和 [汉字] 之间插入空格。
         \\p{IsHan} 代表所有汉字。
         注意：在标准的 Java 正则表达式（java.util.regex.Pattern）中，Script（脚本）名称必须加上 Is 前缀。
         直接写 \p{Han} 在某些 JDK 版本下会被识别为“未知的字符类别”，正确的写法应该是 \p{IsHan}。*/
        value = value.replaceAll("([\\p{IsHan}])([a-zA-Z0-9])", "$1 $2")
                .replaceAll("([a-zA-Z0-9])([\\p{IsHan}])", "$1 $2");
        
        setValue(value);
    }
}