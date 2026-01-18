package com.mol.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "管理员修改学生信息参数")
public class AdminUpdateStudentBody {
    
    @NotNull(message = "学生 ID 不能为空")
    @Schema(description = "学生 ID", example = "1001")
    private Long id;
    
    // 🛡️ 防刁民设计：不包含 username 字段，确保学号无法被修改
    
    @Schema(description = "真实姓名 (改名需谨慎)", example = "李四")
    private String realName;
    
    @Schema(description = "性别 (0-男, 1-女, 2-未知)", example = "0")
    private String gender; // 仅限管理员操作
    
    @Schema(description = "学院 ID", example = "101")
    private Long collegeId;
    
    @Schema(description = "专业 ID", example = "201")
    private Long majorId;
    
    @Schema(description = "班级 ID", example = "301")
    private Long classId;
    
    @Schema(description = "状态 (0-正常 1-停用)", example = "0")
    private String status;
}