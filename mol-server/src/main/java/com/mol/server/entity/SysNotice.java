package com.mol.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 系统通知公告实体 (mol-server 底层模块)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice")
@Schema(description = "全局通知公告")
public class SysNotice extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "公告标题")
    private String title;
    
    @Schema(description = "公告内容 (支持富文本HTML)")
    private String content;
    
    @Schema(description = "类型: 1-通知, 2-公告, 3-安全提醒")
    private Integer type;
    
    @Schema(description = "级别: 0-普通, 1-重要(大屏红色高亮)")
    private Integer level;
    
    @Schema(description = "状态: 0-发布, 1-撤回")
    private String status;
    
    @Schema(description = "发布人展示名")
    private String publisherName;
    
    @Schema(description = "发布时间")
    private LocalDateTime releaseTime;
}