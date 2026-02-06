package com.mol.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 普通用户修改个人资料 DTO
 * 核心策略：只包含允许修改的字段，其他字段（如ID、学号）后端不接收，防止恶意篡改
 */
@Data
@Schema(description = "用户修改资料请求参数")
public class UserProfileEditDTO {
    
    @Schema(description = "头像 URL (最大500字符)")
    @Size(max = 500, message = "头像链接过长")
    private String avatar;
    
    @Schema(description = "昵称 (2-50 字符，防恶意长名)")
    @Size(min = 2, max = 50, message = "昵称长度需在 2-50 个字符之间")
    // 禁止包含特殊字符，只允许中英文数字下划线
    @Pattern(regexp = "^[\\u4e00-\\u9fa5_a-zA-Z0-9]+$", message = "昵称包含非法字符")
    private String nickname;
    
    @Schema(description = "手机号 (必须是11位大陆手机号)")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入有效的 11 位手机号码")
    private String phone;
    
    @Schema(description = "电子邮箱 (格式校验)")
    // 防刁民：必须符合邮箱格式，且长度不能太离谱
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过 100 字符")
    private String email;
    
    @Schema(description = "密码 (留空则不修改)")
    @Size(min = 6, max = 20, message = "密码长度需在 6-20 位之间")
    private String password;
    
    // ================= 扩展信息 =================
    
    @Schema(description = "民族 (例如：汉族)")
    @Size(max = 20, message = "民族名称过长")
    private String ethnicity;
    
    @Schema(description = "籍贯 (例如：北京市海淀区)")
    @Size(max = 64, message = "籍贯地址过长")
    private String hometown;
    
    @Schema(description = "家庭居住地址")
    @Size(max = 500, message = "家庭地址过长")
    private String homeAddress;
    
    @Schema(description = "校外居住地址(学生或教职工自行备案)")
    @Size(max = 500, message = "校外地址过长")
    private String outsideAddress;
    
    @Schema(description = "政治面貌 (群众/团员/党员)")
    @Pattern(regexp = "^(群众|共青团员|中共预备党员|中共党员|其它)$", message = "政治面貌选择不合法")
    private String politicalStatus;
    
    @Schema(description = "个人简介/备注 (可选)")
    @Size(max = 200, message = "备注不能超过 200 字")
    private String remark; // ✨ 建议加上，方便用户填一些特殊说明
    
    // ================= 紧急联系人 (重点防刁民) =================
    
    @Schema(description = "紧急联系人姓名 (必须是中文，2-50字)")
    @NotBlank(message = "紧急联系人不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,50}$", message = "紧急联系人姓名必须是 2-50 位中文")
    private String emergencyContact;
    
    @Schema(description = "紧急联系人电话 (必须有效)")
    @NotBlank(message = "紧急联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入有效的紧急联系人手机号")
    private String emergencyPhone;
    
    @Schema(description = "与联系人关系 (限制枚举值，防止填'隔壁老王')")
    @NotBlank(message = "关系不能为空")
    // 这里建议前端做下拉框，后端做白名单校验
    @Pattern(regexp = "^(父母|祖父母|兄弟姐妹|配偶|其他亲属|辅导员)$", message = "关系只能选择：父母/祖父母/兄弟姐妹/配偶/其他亲属/辅导员")
    private String emergencyRelation;
}