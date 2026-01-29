package com.mol.common.core.entity;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.mol.common.core.handler.EncryptTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 系统管理员实体
 * <p>
 * 包含：超管、宿管经理、辅导员、维修工头等
 * 对应表: sys_admin_user
 * </p>
 *
 * @author mol
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_admin_user", autoResultMap = true)
@Schema(description = "系统管理员对象")
public class SysAdminUser extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    public static final String DEFAULT_AVATAR = "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png";
    
    @Schema(description = "主键 ID")
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @NotBlank(message = "账号不能为空")
    @Schema(description = "登录账号 (工号/admin)")
    private String username;
    
    @Schema(description = "加密密码")
    private String password;
    
    @Schema(description = "是否为初始密码 (1:是 0:否)")
    private Integer isInitialPwd;
    
    @NotBlank(message = "真实姓名不能为空")
    @Schema(description = "真实姓名")
    private String realName;
    
    @Schema(description = "昵称")
    private String nickname;
    
    @Schema(description = "性别 (0-女 1-男)")
    private String gender;
    
    @Schema(description = "头像")
    private String avatar;
    public String getAvatar() {
        return StrUtil.isBlank(this.avatar) ? DEFAULT_AVATAR : this.avatar;
    }
    
    @Schema(description = "手机号")
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String phone;
    
    @Schema(description = "电子邮箱")
    private String email;
    
    // 🟢 ================== 核心归属字段 ==================
    
    @Schema(description = "所属校区 ID")
    private Long campusId;
    
    @Schema(description = "所属部门 ID")
    private Long deptId;
    
    @Schema(description = "所属学院 ID")
    private Long collegeId;
    
    // 🟢 ================== 档案补充字段 ==================
    
    @Schema(description = "身份证号")
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String idCard;
    
    @Schema(description = "民族")
    private String ethnicity;
    
    @Schema(description = "籍贯")
    private String hometown;
    
    @Schema(description = "居住地址")
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String currentAddress;
    
    @Schema(description = "紧急联系人")
    private String emergencyContact;
    
    @Schema(description = "紧急电话")
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String emergencyPhone;
    
    @Schema(description = "关系")
    private String emergencyRelation;
    
    // =========================================================
    
    @Schema(description = "居住类型 (0:住校 1:校外)")
    private Integer residenceType;
    
    @Schema(description = "帐号状态 (0:正常 1:停用)")
    private String status;
    
    @Schema(description = "逻辑删除标志")
    @TableLogic
    private String delFlag;
    
    @Schema(description = "备注")
    private String remark;
    
    // =========== ✨ 新增：防刁民/审计核心字段 ===========
    
    /**
     * 在岗/在校状态 (1:在岗/在校 0:休假/离校)
     * <p>
     * 🛡️ 业务场景：
     * 对于维修工(RepairMaster)和宿管(DormManager)：
     * 0 表示正在休假或下班回家，系统派单时应自动过滤掉这些人。
     * </p>
     */
    @Schema(description = "在岗状态: 1在岗 0休假/离校")
    private Integer campusStatus;
    
    /**
     * 最后登录时间
     * <p>
     * 🛡️ 防刁民设计：
     * 用于审计管理员账号的活跃度。
     * 如果一个管理员账号超过 180 天未登录，说明该人员可能已离职但权限未收回，
     * 系统应在后台高亮显示，提示超级管理员进行封禁处理。
     * </p>
     */
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;
}