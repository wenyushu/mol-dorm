package com.mol.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * 用户角色授权 DTO
 * 用于接收前端传来的“给谁(userId)授权什么角色(roleIds)”参数
 */
@Data // 必须加这个注解，否则无法解析 getUserId() 和 getRoleIds()
@Schema(description = "用户角色授权参数")
public class UserRoleGrantDTO {
    
    @Schema(description = "目标用户 ID (如李小牧的 userId)")
    private Long userId;
    
    @Schema(description = "角色 ID 列表 (例如 [8] 代表辅导员)")
    private List<Long> roleIds;
}