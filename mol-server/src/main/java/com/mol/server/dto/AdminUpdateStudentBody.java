package com.mol.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员修改学生信息参数对象
 * <p>
 * 🛡️ 防刁民设计说明：
 * 1. 字段阉割：不包含 username，防止管理员误操作或恶意修改学生学号（唯一标识）。
 * 2. 权限分级：字段虽然在此定义，但 Service 层会对性别等核心字段进行角色二次校验。
 * 3. 长度约束：地址等字段设置了最大长度，防止恶意超长字符串填充数据库。
 * </p>
 */
@Data
@Schema(description = "管理员修改学生/教工档案参数 (特权操作)")
public class AdminUpdateStudentBody {
    
    @NotNull(message = "操作失败：目标 ID 不能为空")
    @Schema(description = "用户主键 ID (数据库自增 ID)", example = "1001")
    private Long id;
    
    @Schema(description = "真实姓名 (改名需经过教务处核实，系统会记录操作人)", example = "李四")
    private String realName;
    
    @Pattern(regexp = "[01]", message = "非法参数：性别只能是 0(女) 或 1(男)")
    @Schema(description = "性别 (0-女 1-男)", example = "1")
    private String gender;
    
    @Schema(description = "学院 ID (关联 sys_college 表)", example = "101")
    private Long collegeId;
    
    @Schema(description = "专业 ID (关联 sys_major 表)", example = "201")
    private Long majorId;
    
    @Schema(description = "班级 ID (关联 sys_class 表)", example = "301")
    private Long classId;
    
    @Schema(description = "状态 (0-正常 1-停用 2-已归档)", example = "0")
    private String status;
    
    /**
     * 🛡️ 强制备案核心字段：校外居住地址
     * 当此字段有值时，Service 层会联动修改 residence_type 为校外。
     */
    @Size(max = 500, message = "操作失败：校外地址描述过长")
    @Schema(description = "校外居住地址 (管理员强制备案使用)", example = "广东省广州市白云区XX大道XX小区XX号")
    private String outsideAddress;
    
    /**
     * 家庭居住地址 (对应身份证地址)
     */
    @Size(max = 500, message = "操作失败：家庭地址描述过长")
    @Schema(description = "家庭居住地址 (身份证原始地址)", example = "广东省广州市白云区XX大道XX小区XX号")
    private String homeAddress;
    
    // ✨ 建议预留：备注字段，管理员可以手动输入修改原因
    @Schema(description = "修改备注 (说明修改原因，如：根据教务处证明更名)", example = "该生已办理校外住宿手续")
    private String remark;
}