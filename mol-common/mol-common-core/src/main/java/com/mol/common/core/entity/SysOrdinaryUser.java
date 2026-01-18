package com.mol.common.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDate;

/**
 * 普通用户实体 (学生/教职工)
 * <p>
 * 对应表: sys_ordinary_user
 * 包含完整的档案信息和基础审计字段
 * </p>
 *
 * @author mol
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_ordinary_user")
@Schema(description = "普通用户(学生/教工)对象")
public class SysOrdinaryUser extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "主键 ID")
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "学号/工号 (登录账号)")
    private String username;
    
    @Schema(description = "加密密码")
    private String password;
    
    @Schema(description = "真实姓名")
    private String realName;
    
    // 🟢 新增字段
    @Schema(description = "用户昵称")
    private String nickname;
    
    @Schema(description = "头像地址")
    private String avatar;
    
    @Schema(description = "身份证号")
    private String idCard;
    
    @Schema(description = "本人手机号")
    private String phone;
    
    @Schema(description = "人员类别 (0:学生 1:教职工)")
    private Integer userCategory;
    
    @Schema(description = "性别 (0:男 1:女 2:未知)")
    private String gender;
    
    // ----------- 归属信息 -----------
    
    @Schema(description = "所属学院 ID")
    private Long collegeId;
    
    @Schema(description = "所属专业 ID (仅学生)")
    private Long majorId;
    
    @Schema(description = "所属班级 ID (仅学生)")
    private Long classId;
    
    @Schema(description = "部门 ID (教职工用)")
    private Long deptId;
    
    // ----------- 详细档案信息 -----------
    
    @Schema(description = "民族 (如: 汉族)")
    private String ethnicity;
    
    @Schema(description = "籍贯 (如: 江苏南京)")
    private String hometown;
    
    @Schema(description = "出生日期")
    private LocalDate birthDate;
    
    @Schema(description = "政治面貌 (党员/团员/群众)")
    private String politicalStatus;
    
    @Schema(description = "电子邮箱")
    private String email;
    
    @Schema(description = "家庭座机")
    private String landline;
    
    // ----------- 紧急联系人 -----------
    
    @Schema(description = "紧急联系人姓名")
    private String emergencyContact;
    
    @Schema(description = "紧急联系人电话")
    private String emergencyPhone;
    
    @Schema(description = "紧急联系人关系 (如: 父子)")
    private String emergencyRelation;
    
    // ----------- 居住与时间 -----------
    
    /**
     * 0: 住校, 1: 校外
     */
    @Schema(description = "居住类型 (0:住校 1:校外)")
    private Integer residenceType;
    
    @Schema(description = "校外居住地址")
    private String currentAddress;
    
    @Schema(description = "入学/入职时间")
    private LocalDate entryDate;
    
    /**
     * 帐号状态 (0:正常 1:停用)
     */
    @Schema(description = "帐号状态 (0:正常 1:停用)")
    private String status;
    
    @Schema(description = "逻辑删除标志")
    @TableLogic
    private String delFlag;
    
    /**
     * 入学/入职年份 (YYYY)
     * 用于生成学号前缀，不存入 sys_ordinary_user 表 (存入 stu_profile)
     */
    @TableField(exist = false)
    @Schema(description = "入学/入职年份 (前端传参用)")
    private Integer entryYear;
    
    /**
     * 为了兼容性，增加 getEnrollmentYear 方法别名
     */
    public Integer getEnrollmentYear() {
        return this.entryYear;
    }
    
    /**
     * 培养层次代码 (Z/B/Y/D)
     * 业务过程变量，不存库
     */
    @TableField(exist = false)
    private String eduLevel;
}