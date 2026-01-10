package com.mol.common.core.constant;

/**
 * 角色权限标识常量
 */
public class RoleConstants {
    
    // --- 管理员体系 (Admin) ---
    /** 超级管理员 (最高权限) */
    public static final String SUPER_ADMIN = "super_admin";
    
    /** 一级管理员：部门负责人 (后勤部长/行政部长) */
    public static final String DEPT_HEAD = "dept_head";
    
    /** 二级管理员：具体执行者 (宿管/辅导员) */
    public static final String DORM_MANAGER = "dorm_manager";
    public static final String COUNSELOR = "counselor";
    
    // --- 普通用户体系 (Ordinary) ---
    /** 学生 */
    public static final String STUDENT = "student";
    
    /** 教职工 (一线人员：食堂/保安) */
    public static final String STAFF = "staff";
}