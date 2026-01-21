package com.mol.common.core.handler;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.core.util.StrUtil;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.nio.charset.StandardCharsets;

/**
 * 敏感字段智能加密处理器 (AES)
 * <p>
 * 特性：
 * 1. 写入时：自动加密。
 * 2. 读取时：尝试解密。如果解密失败（说明数据库里存的是明文旧数据），则原样返回。
 * 这样可以完美兼容你的 SQL 测试数据！
 * </p>
 */
public class EncryptTypeHandler extends BaseTypeHandler<String> {
    
    // ⚠️ 生产环境请将密钥配置在 yml 中，不要硬编码！
    // 这里为了测试方便，使用一个固定的 16 位密钥
    private static final byte[] KEYS = "mol-dorm-secure1".getBytes(StandardCharsets.UTF_8);
    private static final AES aes = SecureUtil.aes(KEYS);
    
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        if (StrUtil.isBlank(parameter)) {
            ps.setString(i, parameter);
            return;
        }
        // 写入数据库前：加密
        ps.setString(i, aes.encryptHex(parameter));
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
            // 尝试解密
            return aes.decryptStr(value);
        } catch (Exception e) {
            // 🚨 兼容模式：如果解密失败（报错），说明数据库里存的是 SQL 初始化时的明文
            // 直接返回原文，保证测试数据能正常显示
            return value;
        }
    }
}