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
import java.time.LocalDateTime;

/**
 * 普通用户实体 (学生/教职工)
 * <p>
 * 对应表: sys_ordinary_user
 * 核心业务表，记录在校活跃用户的档案信息。
 * </p>
 *
 * @author mol
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_ordinary_user", autoResultMap = true)
@Schema(description = "普通用户(学生/教工)对象")
public class SysOrdinaryUser extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    public static final String DEFAULT_AVATAR = "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png";
    
    @Schema(description = "主键 ID")
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "学号/工号 (登录账号)")
    private String username;
    
    @Schema(description = "加密密码")
    private String password;
    
    @Schema(description = "是否为初始密码 (1:是 0:否)")
    private Integer isInitialPwd;
    
    @Schema(description = "真实姓名")
    private String realName;
    
    @Schema(description = "用户昵称")
    private String nickname;
    
    @Schema(description = "头像地址")
    private String avatar;
    
    public String getAvatar() {
        if (StrUtil.isBlank(this.avatar)) {
            return DEFAULT_AVATAR;
        }
        return this.avatar;
    }
    
    @NotBlank(message = "身份证号不能为空")
    @Schema(description = "身份证号")
    @TableField(typeHandler = EncryptTypeHandler.class) // 🛡️ 敏感字段加密
    private String idCard;
    
    @NotBlank(message = "手机号不能为空")
    @Schema(description = "本人手机号")
    @TableField(typeHandler = EncryptTypeHandler.class) // 🛡️ 敏感字段加密
    private String phone;
    
    @NotNull(message = "人员类别不能为空")
    @Schema(description = "人员类别 (0:学生 1:教职工)")
    private Integer userCategory;
    
    @NotNull(message = "性别不能为空")
    @Pattern(regexp = "[01]", message = "性别格式错误 (0-女 1-男)")
    @Schema(description = "性别 (0-女 1-男)")
    private String gender;
    
    // ----------- 归属信息 -----------
    
    @Schema(description = "所属校区 ID")
    private Long campusId;
    
    @Schema(description = "合同年限 (仅教职工)")
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
    @Schema(description = "民族")
    private String ethnicity;
    
    @NotBlank(message = "籍贯不能为空")
    @Schema(description = "籍贯")
    private String hometown;
    
    @Schema(description = "出生日期")
    private LocalDate birthDate;
    
    @Schema(description = "政治面貌")
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
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String emergencyPhone;
    
    @NotBlank(message = "紧急联系人关系不能为空")
    @Schema(description = "紧急联系人关系")
    private String emergencyRelation;
    
    // ----------- 居住与时间 -----------
    
    @Schema(description = "居住类型 (0:住校 1:校外)")
    private Integer residenceType;
    
    @Schema(description = "校外居住地址")
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String currentAddress;
    
    @Schema(description = "入学/入职时间")
    private LocalDate entryDate;
    
    @NotBlank(message = "账号状态不能为空")
    @Schema(description = "帐号状态 (0:正常 1:停用 2:已归档)")
    private String status;
    
    @Schema(description = "逻辑删除标志")
    @TableLogic
    private String delFlag;
    
    @Schema(description = "入学年份(学生)")
    private Integer enrollmentYear;
    
    @Schema(description = "入职年份(教工)")
    private Integer entryYear;
    
    public Integer getEnrollmentYear() {
        return this.entryYear;
    }
    
    @TableField(exist = false)
    private String eduLevel;
    
    // =========== ✨ 新增字段：防刁民/假期管理核心 ===========
    
    /**
     * 在校状态 (1:在校 0:离校)
     * <p>
     * 🛡️ 防刁民设计：
     * 寒暑假或请假离校时，学生需打卡将此状态置为 0。
     * 状态为 0 时，可以限制部分校园网功能（如报修、订场），但保留基础查询功能。
     * 这与 status (账号状态) 不同，campus_status 只是物理位置标记，不影响账号登录。
     * </p>
     */
    @Schema(description = "在校状态: 1在校 0离校(假期)")
    private Integer campusStatus;
    
    /**
     * 最后登录时间
     * <p>
     * 🛡️ 防刁民设计：
     * 用于识别“僵尸账号”。如果一个学生 status=0 (正常)，但 lastLoginTime 停留在 1 年前，
     * 系统会自动将其识别为异常（可能已退学但未走流程），并触发归档预警。
     * </p>
     */
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;
    
    /**
     * 休学开始日期
     * <p>
     * 🛡️ 防刁民设计：
     * 针对“无限期休学赖着不走”的情况。
     * 系统每日定时任务会检查此字段，如果 (当前时间 - 休学开始时间) > 2年，
     * 且用户未复学，系统将自动执行“超时退学”归档流程，释放学籍和资源。
     * </p>
     */
    @Schema(description = "休学开始日期(用于计算2年期限)")
    private LocalDate suspensionStartDate;
}