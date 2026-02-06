package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 综合住宿申请实体 (全能版)
 * 涵盖：学生入住、教工入住、调宿、退宿、假期留校
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("dorm_application")
@Schema(description = "综合住宿申请单")
public class DormWorkflow extends BaseEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    
    // === 业务类型常量 ===
    public static final int TYPE_CHECK_IN_NEW     = 10; // 新生/新教工入住
    public static final int TYPE_EXCHANGE_ADMIN   = 20; // 调宿-向宿管申请(分配空床)
    public static final int TYPE_EXCHANGE_MUTUAL  = 21; // 调宿-互换(A与B互换)
    public static final int TYPE_CHECK_OUT        = 30; // 退宿申请
    public static final int TYPE_HOLIDAY_STAY     = 40; // 假期留校
    
    
    // ========== 1. 申请人信息 ==========
    
    @Schema(description = "申请人ID")
    private Long userId;
    
    @Schema(description = "学号/工号")
    private String username;
    
    @Schema(description = "姓名")
    private String realName;
    
    @Schema(description = "人员类型: 0-学生 1-教工 2-宿管")
    private Integer userType;
    
    @Schema(description = "性别: 1男 2女")
    private String gender;
    
    @Schema(description = "所属校区ID (冗余方便查询)")
    private Long campusId;
    
    // ========== 2. 核心类型与状态 ==========
    
    /**
     * 10: 入住申请 (含学生回校、教工新入职)
     * 20: 调宿申请 (换寝)
     * 30: 退宿申请
     * 40: 假期留校
     */
    @Schema(description = "申请类型: 10-入住 20-调宿 30-退宿 40-留校")
    private Integer type;
    
    @Schema(description = "状态: 0-待审核, 1-通过, 2-驳回, 3-撤销")
    private Integer status;
    
    @Schema(description = "申请原因")
    private String reason;
    
    // ========== 3. 业务差异化字段 (按需取值) ==========
    
    @Schema(description = "目标房间ID (调宿/入住用)")
    private Long targetRoomId;
    
    @Schema(description = "目标床位ID (精准调宿用)")
    private Long targetBedId;
    
    @Schema(description = "期望户型 (教工申请用)")
    private String expectType;
    
    @Schema(description = "开始日期 (留校用)")
    private LocalDate startDate;
    
    @Schema(description = "结束日期 (留校用)")
    private LocalDate endDate;
    
    // ========== 4. 紧急联系人 (留校必填) ==========
    
    @Schema(description = "紧急联系人姓名")
    private String emergencyName;
    
    @Schema(description = "与本人关系")
    private String emergencyRelation;
    
    @Schema(description = "紧急联系人电话")
    private String emergencyPhone;
    
    // ========== 5. 审批审计 ==========
    
    @Schema(description = "审批人")
    private String handleBy;
    
    @Schema(description = "审批时间")
    private LocalDateTime handleTime;
    
    @Schema(description = "审批意见/驳回原因")
    private String handleNote;
}