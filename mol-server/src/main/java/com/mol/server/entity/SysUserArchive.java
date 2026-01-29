package com.mol.server.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.handler.EncryptTypeHandler;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户归档实体
 * <p>
 * 🛡️ 防刁民设计 - 冷数据隔离与备份：
 * 1. **瘦身主表**：将不活跃/已离开的用户移出主表，保证 `sys_ordinary_user` 查询速度。
 * 2. **数据快照**：`originalDataJson` 字段保存了用户归档那一刻的**全量数据**。
 * 万一辅导员手滑误点了“勒令退学”，管理员可以从这个 JSON 字段无损恢复所有信息（包括偏好、家庭住址等），
 * 避免了“删库跑路”式的不可逆灾难。
 * 3. **责任追溯**：记录了 `operator` (操作人) 和 `archiveReason` (原因)，防止推诿扯皮。
 * </p>
 *
 * @author mol
 */
@Data
@Builder
@TableName(value = "sys_user_archive", autoResultMap = true) // 必须开启自动映射以支持 TypeHandler
public class SysUserArchive {
    
    /**
     * 原始用户ID
     * 🛡️ 设计：复用原 ID，方便在日志表中追溯该用户以前的操作记录。
     */
    @TableId
    private Long id;
    
    /**
     * 账号快照
     */
    private String username;
    
    /**
     * 姓名快照
     */
    private String realName;
    
    /**
     * 用户类别 (0学生 1教工)
     */
    private Integer userCategory;
    
    /**
     * 学院名称快照
     * 🛡️ 设计：存储“计算机学院”字符串，而不是 college_id。
     * 因为 10 年后“计算机学院”可能改名或合并，存 ID 可能导致未来查不到归属，存名称则是历史的真实记录。
     */
    private String collegeName;
    
    /**
     * 手机号 (加密存储)
     */
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String phone;
    
    /**
     * 身份证 (加密存储)
     */
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String idCard;
    
    /**
     * 入学/入职年份
     * 用于统计各级学生的流失率/毕业率。
     */
    private Integer entryYear;
    
    /**
     * 归档类型
     * 对应 ArchiveTypeEnum (10毕业, 40退学, 52休学超时等)
     */
    private Integer archiveType;
    
    /**
     * 归档/异动原因
     * 必填项，记录为何进行此操作。
     */
    private String archiveReason;
    
    /**
     * 归档操作时间
     */
    private LocalDateTime archiveTime;
    
    /**
     * 操作人
     * 记录是谁执行了归档操作 (系统任务或具体管理员)，用于审计。
     */
    private String operator;
    
    /**
     * 原始数据全量备份 (JSON)
     * 🛡️ 核心防刁民设计：后悔药。
     * 包含了 SysOrdinaryUser 对象的所有字段。
     */
    private String originalDataJson;
}