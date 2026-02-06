package com.mol.common.core.config;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * 全局 URL 参数/表单 自动格式化配置
 * 防刁民设计：自动去首尾空格 + 自动给中英文之间加空格
 */
@ControllerAdvice
public class WebMvcTrimConfig {
    
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // 使用我们需要自定义的编辑器
        // true 表示：如果结果是空字符串 ""，则转换为 null
        AutoFormatStringEditor autoEditor = new AutoFormatStringEditor(true);
        
        // 注册到 String 类型上
        binder.registerCustomEditor(String.class, autoEditor);
    }
}