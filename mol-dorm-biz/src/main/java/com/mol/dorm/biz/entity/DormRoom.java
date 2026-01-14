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
    
    // --- 冗余字段，方便查询 ---
    @Schema(description = "所在楼层号 (如: 3)")
    private Integer floorNo;
    
    // --- 核心外键，关联逻辑 ---
    @Schema(description = "所属楼层记录 ID (关联 dorm_floor 表)")
    private Long floorId;
    
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
    
    @Schema(description = "状态: 1-正常, 0-维修")
    private Integer status;
    
    @Schema(description = "乐观锁版本号")
    @Version
    private Integer version;
    
    @Schema(description = "逻辑删除标志")
    @TableLogic(value = "0", delval = "1")
    @TableField("del_flag")
    private String delFlag;
}