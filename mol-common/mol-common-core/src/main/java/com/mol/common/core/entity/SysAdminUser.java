package com.mol.common.core.entity;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.mol.common.core.handler.EncryptTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

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
    
    // 默认头像
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
    
    // 🟢 ================== 核心归属字段 (新增) ==================
    
    @Schema(description = "所属校区 ID (用于宿管/维修工划分区域)")
    private Long campusId;
    
    @Schema(description = "所属部门 ID (用于后勤/行政归属)")
    private Long deptId;
    
    @Schema(description = "所属学院 ID (专用于辅导员)")
    private Long collegeId;
    
    // =========================================================
    
    /**
     * 0: 住校(如宿管住值班室), 1: 校外
     */
    @Schema(description = "居住类型 (0:住校 1:校外)")
    private Integer residenceType;
    
    @Schema(description = "帐号状态 (0:正常 1:停用)")
    private String status;
    
    @Schema(description = "逻辑删除标志")
    @TableLogic
    private String delFlag;
    
    @Schema(description = "备注")
    private String remark;
}