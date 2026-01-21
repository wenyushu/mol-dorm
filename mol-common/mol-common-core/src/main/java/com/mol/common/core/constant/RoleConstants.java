package com.mol.common.core.constant;

/**
 * 角色权限标识常量 (四级权限体系 - 最终对齐版)
 * <p>
 * 必须与数据库 sys_role 表的 role_key 字段严格一致
 * </p>
 */
public class RoleConstants {
    
    // ================== 1. 一级权限 (系统核心) ==================
    /** 超级管理员 */
    public static final String SUPER_ADMIN = "super_admin";
    
    // ================== 2. 二级权限 (部门/学院主管) ==================
    /** 部门/学院 の 管理员 (对应 Controller 中的 DEPT_ADMIN) */
    public static final String DEPT_ADMIN = "dept_admin";
    
    // ================== 3. 三级权限 (一线工作人员) ==================
    /** 宿管经理 */
    public static final String DORM_MANAGER = "dorm_manager";
    /** 维修工头 */
    public static final String REPAIR_MASTER = "repair_master";
    /** 行政辅导员 */
    public static final String COUNSELOR = "counselor";
    /** 学院的教职工或/兼职辅导员 (对应数据库 college_teacher,且值与数据库 sys_role 表一致) */
    public static final String COLLEGE_TEACHER = "college_teacher";
    
    // ================== 4. 四级权限 (普通用户) ==================
    /** 学生 */
    public static final String STUDENT = "student";
    /** 普通工勤人员 (食堂/保安) */
    public static final String STAFF = "staff";
}