package com.mol.sys.biz.vo;

import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private Long userId;
    private String realName;
    private String role;   // 角色标识，用于前端控制菜单
    private String avatar;
}