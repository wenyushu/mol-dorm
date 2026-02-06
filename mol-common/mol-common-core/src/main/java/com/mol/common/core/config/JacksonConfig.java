package com.mol.common.core.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 全局配置 (三合一增强版)
 * 1. 序列化: Long 转 String (解决前端精度丢失)
 * 2. 序列化/反序列化: 统一时间格式 (yyyy-MM-dd HH:mm:ss)
 * 3. 反序列化: String 自动去空格 (防刁民/防手误)
 */
@Configuration
public class JacksonConfig {
    
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            // ================================================
            // 1. Long 类型转 String (防止前端精度丢失)
            // ================================================
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
            
            // ================================================
            // 2. 统一时间格式
            // ================================================
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
            builder.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
            
            // ================================================
            // 3. String 类型自动去空格 (防刁民设计)
            // ================================================
            builder.deserializerByType(String.class, new StringTrimDeserializer());
        };
    }
    
    /**
     * 内部类：自定义字符串反序列化器
     * 逻辑：如果前端传来的 JSON 字段是字符串，自动执行 trim()
     */
    public static class StringTrimDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            // 获取字段的原始字符串值
            String value = p.getValueAsString();
            if (value != null) {
                // 核心：去除首尾空格
                return value.trim();
            }
            return null;
        }
    }
}