package com.mol.dorm.biz.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum RequestStatus {
    PENDING(0, "待审核"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已驳回");
    
    @EnumValue // 配合 MyBatisPlus 写入数据库的值
    private final int code;
    private final String msg;
    
    RequestStatus(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}