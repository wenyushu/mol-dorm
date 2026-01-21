package com.mol.common.core.entity;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.mol.common.core.handler.EncryptTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
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
@TableName(value = "sys_ordinary_user", autoResultMap = true) // 🟢 必须加 autoResultMap = true
@Schema(description = "普通用户(学生/教工)对象")
public class SysOrdinaryUser extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    // 定义一个常量(头像 url)，方便以后统一修改
    public static final String DEFAULT_AVATAR = "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png";
    
    @Schema(description = "主键 ID")
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "学号/工号 (登录账号)")
    private String username;
    
    @Schema(description = "加密密码")
    private String password;
    
    // 默认初始密码为：123456
    @Schema(description = "是否为初始密码 (1:是 0:否)")
    private Integer isInitialPwd;
    
    @Schema(description = "真实姓名")
    private String realName;
    
    @Schema(description = "用户昵称")
    private String nickname;
    
    @Schema(description = "头像地址")
    private String avatar;
    /**
     * 重写 getAvatar 方法 (Lombok 的 @Data 会生成默认的，我们需要覆盖它)
     * 作用：如果数据库里存的是 null 或 空串，获取时自动返回默认头像
     */
    public String getAvatar() {
        if (StrUtil.isBlank(this.avatar)) {
            return DEFAULT_AVATAR;
        }
        return this.avatar;
    }
    
    
    // 🟢 1. 身份证 (非空)
    @NotBlank(message = "身份证号不能为空")
    @Schema(description = "身份证号")
    @TableField(typeHandler = EncryptTypeHandler.class) // 🔒 加密
    private String idCard;
    
    // 🟢 2. 手机号 (非空)
    @NotBlank(message = "手机号不能为空")
    @Schema(description = "本人手机号")
    @TableField(typeHandler = EncryptTypeHandler.class) // 🔒 加密
    private String phone;
    
    // 🟢 3. 账户类别 (非空)
    @NotNull(message = "人员类别不能为空")
    @Schema(description = "人员类别 (0:学生 1:教职工)")
    private Integer userCategory;
    
    // 🟢 4. 性别：强制非空，只能是 0 或 1
    @NotNull(message = "性别不能为空")
    @Pattern(regexp = "[01]", message = "性别格式错误 (0-女 1-男)")
    @Schema(description = "性别 (0-女 1-男)")
    private String gender;
    
    // ----------- 归属信息 -----------
    
    @Schema(description = "所属校区 ID", example = "1")
    private Long campusId;
    
    @Schema(description = "合同年限 (仅教职工)", example = "3")
    private Integer contractYear;
    
    @Schema(description = "学院 ID")
    private Long collegeId;
    
    @Schema(description = "部门 ID")
    private Long deptId;
    
    @Schema(description = "所属专业 ID (仅学生)")
    private Long majorId;
    
    @Schema(description = "所属班级 ID (仅学生)")
    private Long classId;
    
    
    // ----------- 详细档案信息 -----------
    
    @NotBlank(message = "民族不能为空")
    @Schema(description = "民族 (如: 汉族)")
    private String ethnicity;
    
    @NotBlank(message = "籍贯不能为空")
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
    
    @NotBlank(message = "紧急联系人不能为空")
    @Schema(description = "紧急联系人姓名")
    private String emergencyContact;
    
    @NotBlank(message = "紧急联系电话不能为空")
    @Schema(description = "紧急联系人电话")
    @TableField(typeHandler = EncryptTypeHandler.class) // 🔒 加密
    private String emergencyPhone;
    
    @NotBlank(message = "紧急联系人关系不能为空")
    @Schema(description = "紧急联系人关系 (如: 父子)")
    private String emergencyRelation;
    
    // ----------- 居住与时间 -----------
    
    /**
     * 0: 住校, 1: 校外
     */
    @Schema(description = "居住类型 (0:住校 1:校外)")
    private Integer residenceType;
    
    @Schema(description = "校外居住地址")
    @TableField(typeHandler = EncryptTypeHandler.class) // 🔒 加密
    private String currentAddress;
    
    @Schema(description = "入学/入职时间")
    private LocalDate entryDate;
    
    /**
     * 帐号状态 (0:正常 1:停用)
     */
    @NotBlank(message = "账号状态不能为空")
    @Schema(description = "帐号状态 (0:正常 1:停用)")
    private String status;
    
    @Schema(description = "逻辑删除标志")
    @TableLogic
    private String delFlag;
    
    @Schema(description = "入学年份")
    private Integer enrollmentYear;
    
    @Schema(description = "入职年份")
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