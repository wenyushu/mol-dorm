package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 📝 宿舍床位异动流水日志 (全维度存证版)
 * 🛡️ [防刁民设计]：全字段快照化。
 * 不再依赖 ID 关联，而是直接固化当时的所有归属信息，确保历史溯源无死角。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_dorm_log")
public class SysDormLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // ==================== 1. 人员维度快照 ====================
    private Long userId;
    private String username;
    private String realName;
    private String campusName;   // 所属校区
    private String collegeName;  // 所属学院
    private String majorName;    // 所属专业
    private String grade;        // 年级 (如: 25级)
    private String className;    // 班级
    
    // ==================== 2. 宿舍维度快照 ====================
    private Long oldDormId;      // 原物理 ID
    private String dormCampus;   // 宿舍所在校区
    private String buildingName; // 楼栋 (如: 慎思楼/12栋)
    private Integer floorNum;    // 楼层
    private String roomName;     // 房号 (如: 302)
    private String bedName;      // 床位号 (如: 1号床)
    
    // ==================== 3. 变动核心信息 ====================
    /** 异动类型 (AUTO_RELEASE: 自动释放, MANUAL_OUT: 手动退宿, EXCHANGE: 调宿) */
    private String logType;
    
    /** 详细备注 */
    private String content;
    
    /** 记录时间 */
    private LocalDateTime createTime;
}