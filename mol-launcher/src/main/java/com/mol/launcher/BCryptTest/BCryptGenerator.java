package com.mol.launcher.BCryptTest;

import cn.hutool.crypto.digest.BCrypt;

/**
 * BCrypt 密码生成工具
 * 用于手动生成加密后的字符串，随后手动更新到数据库中
 */
public class BCryptGenerator {
    public static void main(String[] args) {
        // 1. 设置你想要设定的明文密码
        String plainPassword = "123456";
        
        // 2. 使用 Sa-Token 封装的 BCrypt 进行加密
        // hashpw 会自动生成一个随机盐并混入其中
        String hashedParams = BCrypt.hashpw(plainPassword);
        
        System.out.println("\n-------------------------------------------");
        System.out.println("原始密码: " + plainPassword);
        System.out.println("加密后的字符串 (用于写入数据库): ");
        System.out.println(hashedParams);
        System.out.println("-------------------------------------------\n");
        
        // 3. 校验演示 (模拟登录时的匹配逻辑)
        boolean isMatch = BCrypt.checkpw(plainPassword, hashedParams);
        System.out.println("验证测试结果: " + (isMatch ? "匹配成功 ✅" : "匹配失败 ❌"));
    }
    
//    UPDATE `sys_admin_user`
//    SET `password` = '$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO'
//    WHERE `id` > 0;
//
//        -- 2. 更新普通用户表
//    UPDATE `sys_ordinary_user`
//    SET `password` = '$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO'
//    WHERE `id` > 0;
    
}
