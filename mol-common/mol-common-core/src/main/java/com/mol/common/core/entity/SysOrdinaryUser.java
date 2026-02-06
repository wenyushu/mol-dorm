package com.mol.common.core.entity;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.mol.common.core.annotation.SensitiveMask;
import com.mol.common.core.handler.EncryptTypeHandler;

import com.mol.common.core.mask.MaskStrategy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 普通用户实体 (学生/教职工)
 * <p>
 * 对应表: sys_ordinary_user
 * </p>
 */
@Data
@Accessors(chain = true) // ✨ 父类及子类支持链式返回
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
        return StrUtil.isBlank(this.avatar) ? DEFAULT_AVATAR : this.avatar;
    }
    
    @NotBlank(message = "身份证号不能为空")
    @Schema(description = "身份证号")
    @TableField(typeHandler = EncryptTypeHandler.class)
    @SensitiveMask(MaskStrategy.ID_CARD) // ✨ 动态脱敏
    private String idCard;
    
    @NotBlank(message = "手机号不能为空")
    @Schema(description = "本人手机号")
    @TableField(typeHandler = EncryptTypeHandler.class)
    @SensitiveMask(MaskStrategy.PHONE)   // ✨ 动态脱敏
    private String phone;
    
    @NotNull(message = "人员类别不能为空")
    @Schema(description = "人员类别 (0:学生 1:教职工)")
    private Integer userCategory;
    
    @NotNull(message = "性别不能为空")
    @Schema(description = "性别 (0-女 1-男)")
    private String gender;
    
    // ----------- 宿舍关联 (用于判定室友关系) -----------
    
    @Schema(description = "当前所属宿舍房间 ID")
    private Long dormId; // ✨ 补全此字段，解决 getDormId 报错
    
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
    
    @Schema(description = "民族")
    private String ethnicity;
    
    @Schema(description = "籍贯")
    private String hometown;
    
    @Schema(description = "出生日期")
    private LocalDate birthDate;
    
    @Schema(description = "政治面貌")
    private String politicalStatus;
    
    @Schema(description = "电子邮箱")
    private String email;
    
    // ----------- 紧急联系人 -----------
    
    @Schema(description = "紧急联系人姓名")
    private String emergencyContact;
    
    @Schema(description = "紧急联系人电话")
    @TableField(typeHandler = EncryptTypeHandler.class)
    @SensitiveMask(MaskStrategy.PHONE) // ✨ 动态脱敏
    private String emergencyPhone;
    
    @Schema(description = "紧急联系人关系")
    private String emergencyRelation;
    
    // ----------- 🟢 居住与地址 (核心修改点) -----------
    
    @Schema(description = "居住类型 (0:住校 1:校外)")
    private Integer residenceType;
    
    /**
     * 家庭居住地址 (身份证地址)
     */
    @Schema(description = "家庭居住地址")
    @TableField(typeHandler = EncryptTypeHandler.class)
    @SensitiveMask(MaskStrategy.HOME_ADDRESS) // ✨ 动态脱敏
    private String homeAddress;
    
    /**
     * 校外居住地址 (实际目前的居住地)
     * 🛡️ 防刁民：由原 currentAddress 更名而来
     */
    @Schema(description = "校外居住地址")
    @TableField(typeHandler = EncryptTypeHandler.class)
    @SensitiveMask(MaskStrategy.OUTSIDE_ADDRESS) // ✨ 动态脱敏
    private String outsideAddress; // ✨ 解决 getOutsideAddress 报错
    
    @Schema(description = "入学/入职时间")
    private LocalDate entryDate;
    
    @Schema(description = "帐号状态 (0:正常 1:停用 2:已归档)")
    private String status;
    
    @TableLogic
    private String delFlag;
    
    @Schema(description = "入学年份(学生)")
    private Integer enrollmentYear;
    
    @Schema(description = "入职年份(教工)")
    private Integer entryYear;
    
    @TableField(exist = false)
    private String eduLevel;
    
    @Schema(description = "在校状态: 1在校 0离校(假期)")
    private Integer campusStatus;
    
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;
    
    @Schema(description = "休学开始日期")
    private LocalDate suspensionStartDate;
}