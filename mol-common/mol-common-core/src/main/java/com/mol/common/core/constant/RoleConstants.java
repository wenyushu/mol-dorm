package com.mol.common.core.constant;

/**
 * 角色权限常量 (代码与数据库的唯一真理标准)
 * <p>
 * 1. 包含 role_key (用于注解鉴权 @SaCheckRole)
 * 2. 包含 role_id (用于业务逻辑绑定)
 * </p>
 */
public class RoleConstants {
    
    // 🚫 私有构造，防止 new RoleConstants()
    private RoleConstants() {}
    
    // ================== 1. 一级权限 (系统核心) ==================
    /** 超级管理员 */
    public static final String SUPER_ADMIN = "super_admin";
    public static final Long SUPER_ADMIN_ID = 1L;
    
    // ================== 2. 二级权限 (部门/学院主管) ==================
    /** 部门/学院 の 管理员 (对应 Controller 中的 DEPT_ADMIN) */
    public static final String DEPT_ADMIN = "dept_admin";
    public static final Long DEPT_ADMIN_ID = 2L;
    
    // ================== 3. 三级权限 (一线工作人员) ==================
    /** 宿管经理 */
    public static final String DORM_MANAGER = "dorm_manager";
    public static final Long DORM_MANAGER_ID = 3L;
    
    /** 维修工头 */
    public static final String REPAIR_MASTER = "repair_master";
    public static final Long REPAIR_MASTER_ID = 4L;
    
    /** 行政辅导员 */
    public static final String COUNSELOR = "counselor";
    public static final Long COUNSELOR_ID = 5L;
    
    // ================== 4. 四级权限 (普通用户) ==================
    /** 学院的教职工或/兼职辅导员 (对应数据库 college_teacher,且值与数据库 sys_role 表一致) */
    public static final String COLLEGE_TEACHER = "college_teacher";
    public static final Long COLLEGE_TEACHER_ID = 6L;
    
    /** 工勤人员 */
    public static final String STAFF = "staff";
    public static final Long STAFF_ID = 7L;
    
    /** 学生 (对应 user_category=0) */
    public static final String STUDENT = "student";
    public static final Long STUDENT_ID = 8L;
    
}