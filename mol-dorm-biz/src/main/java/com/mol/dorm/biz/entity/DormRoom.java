package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 宿舍房间实体类
 * <p>
 * 对应数据库表：dorm_room
 * 记录房间的物理位置、容量、当前居住情况及状态
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dorm_room")
@Schema(description = "宿舍房间对象")
public class DormRoom extends BaseEntity implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "主键 ID")
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "所属楼栋 ID")
    private Long buildingId;
    
    /**
     * 所在楼层 (核心修复字段)
     * <p>
     * 1. 字段名改为 `floor`，是为了匹配 Service 中 `room.setFloor()` 的调用。
     * 2. 使用 @TableField("floor_no") 依然映射到你数据库里的 `floor_no` 列。
     * <p>
     * ⚠️ 修复报错: 无法解析方法 'setFloor'
     */
    @Schema(description = "所在楼层")
    @TableField("floor_no") // 数据库列名保持 floor_no 不变
    private Integer floor;
    
    @Schema(description = "房间号 (如: 305)")
    @TableField("room_no")
    private String roomNo;
    
    @Schema(description = "核定床位数/容量")
    private Integer capacity;
    
    @Schema(description = "当前居住人数")
    @TableField("current_num")
    private Integer currentNum;
    
    @Schema(description = "房间性别限制 (1:男, 2:女, 0:混合)")
    private Integer gender;
    
    /**
     * 房间状态 (核心字段)
     * 1-正常, 0-维修/封寝
     * <p>
     * ⚠️ 修复报错: 无法解析方法 'setStatus'
     * Service 中 emergencyTransfer 等方法强依赖此字段
     */
    @Schema(description = "状态: 1-正常, 0-维修")
    private Integer status;
    
    @Schema(description = "逻辑删除标志")
    @TableLogic(value = "0", delval = "1")
    @TableField("del_flag")
    private String delFlag;
}