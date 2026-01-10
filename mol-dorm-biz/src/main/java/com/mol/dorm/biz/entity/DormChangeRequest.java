package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 调宿申请表
 * 对应数据库表：dorm_change_request
 */
@Data
@EqualsAndHashCode(callSuper = true) // 告诉 Lombok 也要处理父类的字段
@TableName("dorm_change_request")
public class DormChangeRequest extends BaseEntity { // 必须继承 BaseEntity
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 申请人ID (学生或教职工)
     * 对应 SQL: student_id
     */
    private Long studentId;
    
    /**
     * 原房间ID
     * 对应 SQL: current_room_id
     */
    private Long currentRoomId;
    
    /**
     * 目标房间ID
     * 对应 SQL: target_room_id
     */
    private Long targetRoomId;
    
    /**
     * 换宿原因
     */
    private String reason;
    
    /**
     * 状态
     * 0-待辅导员审批, 1-待宿管审批, 2-已完成, 3-已驳回
     */
    private Integer status;
    
    /**
     * 审批意见
     * 对应 SQL: audit_msg
     */
    private String auditMsg;
    
    /**
     * 申请时间 (业务字段)
     * 对应 SQL: apply_time
     * 注意：BaseEntity 里还有 create_time (系统审计时间)。
     * 通常 applyTime 和 createTime 值是一样的，你可以保留这个字段作为业务展示用。
     */
    private LocalDateTime applyTime;
}