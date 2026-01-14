package com.mol.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 系统通知公告实体
 * <p>
 * 对应表: sys_notice
 * </p>
 *
 * @author mol
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice")
@Schema(description = "通知公告")
public class SysNotice extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "公告标题")
    private String title;
    
    @Schema(description = "公告内容 (支持HTML)")
    private String content;
    
    /**
     * 1:通知 2:公告 3:安全提醒
     */
    @Schema(description = "类型: 1-通知, 2-公告, 3-安全提醒")
    private Integer type;
    
    /**
     * 0:普通 1:重要 (大屏红色高亮)
     */
    @Schema(description = "级别: 0-普通, 1-重要")
    private Integer level;
    
    /**
     * 0:发布 1:撤回
     */
    @Schema(description = "状态: 0-发布, 1-撤回")
    private String status;
}