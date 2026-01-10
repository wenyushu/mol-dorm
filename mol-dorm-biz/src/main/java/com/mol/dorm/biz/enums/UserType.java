package com.mol.dorm.biz.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum UserType {
    STUDENT(1, "学生"),
    TEACHER(2, "教职工"), // 对应之前的逻辑
    ADMIN(0, "管理员");
    
    @EnumValue
    private final int code;
    private final String desc;
    
    UserType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}