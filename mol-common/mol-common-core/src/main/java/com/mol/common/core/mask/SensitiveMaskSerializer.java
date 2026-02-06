package com.mol.common.core.mask;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.mol.common.core.annotation.SensitiveMask;
import com.mol.common.core.context.SecurityContext;

import java.io.IOException;

/**
 * 动态脱敏序列化器
 */
public class SensitiveMaskSerializer extends JsonSerializer<String> implements ContextualSerializer {
    
    private MaskStrategy strategy;
    
    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // 🛡️ 判定：上帝模式（Admin/Manager）或业务授权（同寝室等）开启时返回明文, 否则执行脱敏
        if (SecurityContext.canViewFullDetail() || StrUtil.isBlank(value)) {
            gen.writeString(value);
        } else {
            switch (strategy) {
                // 根据策略执行不同的脱敏逻辑 (借助 Hutool 工具类)
                case ID_CARD:
                    // 身份证：保留前 4 和后 4 位，中间隐藏
                    gen.writeString(DesensitizedUtil.idCardNum(value, 4, 4));
                    break;
                
                case PHONE:
                    // 手机号/紧急电话：138 **** 5678
                    gen.writeString(DesensitizedUtil.mobilePhone(value));
                    break;
                
                case HOME_ADDRESS:
                    // 家庭地址：保留前 6 位（行政区划），后面全部掩盖
                    // 广东省深圳市******
                    gen.writeString(StrUtil.hide(value, 6, value.length()));
                    break;
                
                case OUTSIDE_ADDRESS:
                    // 校外地址：极其敏感，只保留前3位，其余全部抹除
                    // 某某小区******
                    gen.writeString(StrUtil.hide(value, 3, value.length()));
                    break;
            }
        }
    }
    
    /**
     * 获取注解上下文
     */
    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
            throws JsonMappingException { // 抛出异常解决报错
        
        SensitiveMask annotation = property.getAnnotation(SensitiveMask.class);
        if (annotation != null) {
            this.strategy = annotation.value();
            return this;
        }
        return prov.findValueSerializer(property.getType(), property);
    }
}