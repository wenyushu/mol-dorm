package com.mol.common.core.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * 全局 JSON 请求体自动格式化配置
 * 针对 @RequestBody 生效
 */
@Configuration
public class JacksonTrimConfig {
    
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder.deserializerByType(String.class, new AutoFormatStringDeserializer());
    }
    
    /**
     * 内部类：自定义 JSON 反序列化逻辑
     */
    public static class AutoFormatStringDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getValueAsString();
            if (text == null) {
                return null;
            }
            
            // 1. 去除首尾空格
            String value = text.trim();
            
            // 2. 空串转 null (可选，如果不想要这个功能可以删掉这行)
            if ("".equals(value)) {
                return null;
            }
            
            /* 3. 执行 “盘古之白” 自动加空格逻辑
             正则含义：在 [汉字] 和 [字母数字] 之间，或者 [字母数字] 和 [汉字] 之间插入空格。
             \\p{IsHan} 代表所有汉字。
             注意：在标准的 Java 正则表达式（java.util.regex.Pattern）中，Script（脚本）名称必须加上 Is 前缀。
             直接写 \p{Han} 在某些 JDK 版本下会被识别为“未知的字符类别”，正确的写法应该是 \p{IsHan}。*/
            value = value.replaceAll("([\\p{IsHan}])([a-zA-Z0-9])", "$1 $2")
                    .replaceAll("([a-zA-Z0-9])([\\p{IsHan}])", "$1 $2");
            
            return value;
        }
    }
}