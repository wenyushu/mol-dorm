package com.mol.common.core.handler;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.nio.charset.StandardCharsets;

/**
 * 敏感字段智能加密处理器 (AES 增强版)
 * <p>
 * 🛡️ 防刁民设计：
 * 1. 容错读取：解密失败则返回原值（兼容 SQL 明文初始化数据）。
 * 2. 幂等写入：如果字符串已经是 Hex 格式且符合加密特征，可以跳过再次加密（可选）。
 * 3. 密钥对齐：对密钥进行处理，防止长度变动导致崩溃。
 * </p>
 */
@Slf4j
public class EncryptTypeHandler extends BaseTypeHandler<String> {
    
    // ⚠️ 建议：此处密钥虽然是硬编码，但我们通过工具类确保其合法性
    private static final String KEY_STR = "mol-dorm-secure1";
    private static final AES aes;
    
    static {
        // 🛡️ 防刁民：即使 KEY_STR 长度被改，也强制截取或填充为 16 位，防止启动失败
        byte[] keyBytes = StrUtil.fillAfter(KEY_STR, '0', 16).substring(0, 16).getBytes(StandardCharsets.UTF_8);
        aes = SecureUtil.aes(keyBytes);
    }
    
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        if (StrUtil.isBlank(parameter)) {
            ps.setString(i, parameter);
            return;
        }
        
        try {
            // 🛡️ 写入：将明文加密后存入
            ps.setString(i, aes.encryptHex(parameter));
        } catch (Exception e) {
            log.warn("字段加密失败，原样存入数据库。字段值前缀: {}", StrUtil.subPre(parameter, 3));
            ps.setString(i, parameter);
        }
    }
    
    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return decrypt(rs.getString(columnName));
    }
    
    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return decrypt(rs.getString(columnIndex));
    }
    
    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return decrypt(cs.getString(columnIndex));
    }
    
    /**
     * 智能解密逻辑
     */
    private String decrypt(String value) {
        if (StrUtil.isBlank(value)) {
            return value;
        }
        try {
            // 尝试解密。如果是 SQL 初始化数据（明文），这里会抛出异常
            return aes.decryptStr(value);
        } catch (Exception e) {
            // 🚨 兼容模式：解密失败说明是明文，直接返回，确保 SQL 测试数据能看得到
            return value;
        }
    }
}