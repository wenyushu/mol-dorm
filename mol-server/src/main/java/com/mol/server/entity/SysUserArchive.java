package com.mol.server.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.handler.EncryptTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 🎓 用户归档实体 (人员档案黑匣子)
 * <p>
 * 🛡️ [防刁民设计 - 冷数据隔离与备份策略]：
 * 1. 瘦身主表：将离校/离职等不活跃用户移出业务主表，确保万级学生数据下的查询性能依然“秒开”。
 * 2. 历史快照化：存储名称(如学院名)而非 ID，防止多年后组织架构调整导致历史档案归属“不可考”。
 * 3. 无损化核销：核心字段 originalDataJson 封存了用户归档瞬时的所有原始数据。
 * 4. 责任可追溯：记录操作人与原因，杜绝“系统自动清算”与“人工误操作”之间的责任推诿。
 * </p>
 *
 * @author mol
 */
@Data
@Builder
@NoArgsConstructor  // ✨ 核心：MyBatis 结果集映射需要无参构造函数
@AllArgsConstructor // ✨ 核心：Builder 模式需要全参构造函数
@TableName(value = "sys_user_archive", autoResultMap = true) // 必须开启以支持下方的 EncryptTypeHandler
public class SysUserArchive {
    
    /**
     * 原始用户 ID
     * 🛡️ [设计意图]：复用原普通用户表的 ID，确保在审计日志、水电费缴费历史等关联表中，依然能通过 ID 串联历史。
     */
    @TableId
    private Long id;
    
    /**
     * 账号快照 (学号/工号)
     */
    private String username;
    
    /**
     * 姓名快照
     */
    private String realName;
    
    /**
     * 用户类别 (0-学生, 1-教工)
     */
    private Integer userCategory;
    
    /**
     * 学院名称快照
     * 🛡️ [防刁民设计]：这里存储字符串“XX学院”而非外键 ID。
     * 因为 10 年后学院可能撤并改名，存 ID 会导致档案归属丢失，存“名称”才是对历史的真实固化。
     */
    private String collegeName;
    
    /**
     * 手机号 (数据脱敏及加密存储)
     * 🛡️ [安全性]：即便档案库被拖库，加密后的手机号也无法被直接利用。
     */
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String phone;
    
    /**
     * 身份证号 (数据脱敏及加密存储)
     */
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String idCard;
    
    /**
     * 入学/入职年份
     * 用于统计分析，例如：计算某一届学生的平均毕业率或休学比例。
     */
    private Integer entryYear;
    
    /**
     * 归档类型
     * 对应 ArchiveTypeEnum (10-正常毕业, 40-勒令退学, 52-休学超时, 60-系统清算等)
     */
    private Integer archiveType;
    
    /**
     * 归档原因
     * 🛡️ [防推诿设计]：详细记录异动原因，作为档案存证的核心。
     */
    private String archiveReason;
    
    /**
     * 归档执行时间
     */
    private LocalDateTime archiveTime;
    
    /**
     * 归档操作人
     * 记录是哪个管理员操作，或者是“SYSTEM_AUTO_TASK”自动执行。
     */
    private String operator;
    
    /**
     * 原始数据全量备份 (JSON)
     * 🛡️ [核心防刁民设计 - 后悔药机制]：
     * 1. 存储 SysOrdinaryUser 对象归档时的全量 JSON 字符串。
     * 2. 万一出现误操作（如：学生在当兵期间被误以为延毕清算），管理员可根据此 JSON 瞬间恢复所有原始资料。
     * 3. 存储容量建议：数据库字段应设为 LONGTEXT。
     */
    private String originalDataJson;
}