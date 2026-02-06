package com.mol.common.core.entity;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.mol.common.core.annotation.SensitiveMask;
import com.mol.common.core.handler.EncryptTypeHandler;
import com.mol.common.core.mask.MaskStrategy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDate;
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
@Accessors(chain = true) // ✨ 父类及子类支持链式返回
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
    @SensitiveMask(MaskStrategy.PHONE)   // ✨ 新增脱敏注解
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String phone;
    
    @Schema(description = "电子邮箱")
    private String email;
    
    // ================== 核心归属字段 ==================
    
    @Schema(description = "所属校区 ID")
    private Long campusId;
    
    @Schema(description = "所属部门 ID")
    private Long deptId;
    
    @Schema(description = "所属学院 ID")
    private Long collegeId;
    
    /**
     * 🏢 负责楼栋 ID
     * 🛡️ [网格化管理核心]：
     * 1. 权限锚点：宿管、楼栋辅导员通过此 ID 实现数据权限自动隔离。
     * 2. 业务锁定：报修指派、房间体检等业务流自动根据此字段过滤管辖范围。
     */
    @Schema(description = "负责楼栋 ID (宿管/辅导员管辖权属性)")
    @TableField(value = "building_id") // 显式指定数据库字段，防止映射偏差
    private Long buildingId;
    
    // ================== 档案补充字段 ==================
    
    @Schema(description = "身份证号")
    @TableField(typeHandler = EncryptTypeHandler.class)
    @SensitiveMask(MaskStrategy.ID_CARD) // ✨ 新增脱敏注解
    private String idCard;
    
    @Schema(description = "民族")
    private String ethnicity;
    
    @Schema(description = "籍贯")
    private String hometown;
    
    /**
     * 家庭居住地址 (身份证上的原始地址)
     * 🛡️ 防刁民：用于核实人员真实背景，通常不随入职变动。
     */
    @Schema(description = "家庭居住地址")
    @TableField(typeHandler = EncryptTypeHandler.class)
    @SensitiveMask(MaskStrategy.HOME_ADDRESS) // ✨ 新增脱敏
    private String homeAddress;
    
    /**
     * 校外居住地址 (实际目前的居住地)
     * 🛡️ 防刁民：仅管理员可见，防止非相关人员顺藤摸瓜。
     */
    @Schema(description = "校外居住地址")
    @TableField(typeHandler = EncryptTypeHandler.class)
    @SensitiveMask(MaskStrategy.OUTSIDE_ADDRESS) // ✨ 新增脱敏
    private String outsideAddress;
    
    
    @Schema(description = "紧急联系人")
    private String emergencyContact;
    
    @Schema(description = "紧急电话")
    @TableField(typeHandler = EncryptTypeHandler.class)
    @SensitiveMask(MaskStrategy.PHONE) // ✨ 新增脱敏
    private String emergencyPhone;
    
    @Schema(description = "关系")
    private String emergencyRelation;
    
    // =========================================================
    
    @Schema(description = "入职日期")
    private LocalDate entryDate;  // 对应数据库 entry_date
    
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
    
    // ✨✨✨【关键修改点】添加下面这个字段 ✨✨✨
    @TableField(exist = false) // 👈 告诉 MyBatis-Plus：数据库表里没这个列，别报错
    @Schema(description = "角色ID (仅用于接收前端参数，保存时存入关联表)")
    private Long roleId;
    
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