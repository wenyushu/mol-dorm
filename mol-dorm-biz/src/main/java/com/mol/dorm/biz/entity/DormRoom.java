package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.mol.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 宿舍房间实体类
 * 对应数据库表：dorm_room
 */
@Data // 自动生成 getFloorNo(), setStatus() 等方法
@EqualsAndHashCode(callSuper = true) // 建议加上：生成的 equals 方法包含父类字段
@TableName("dorm_room")
public class DormRoom extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 所属楼栋 ID
     */
    private Long buildingId;
    
    /**
     * 所在楼层 (例如: 3)
     * 对应数据库字段: floor_no
     * 解决报错: 无法解析方法 'getFloorNo'
     */
    @TableField("floor_no") // 明确指定映射数据库的 floor_no 字段
    private Integer floorNo;
    
    /**
     * 房间号 (例如: 305)
     * 对应数据库字段: room_no
     */
    @TableField("room_no")
    private String roomNo;
    
    /**
     * 核定床位数
     * 对应数据库字段: capacity
     */
    private Integer capacity;
    
    /**
     * 当前居住人数
     * 对应数据库字段: current_num
     */
    @TableField("current_num")
    private Integer currentNum;
    
    /**
     * 房间性别限制 (1:男, 2:女)
     * 对应数据库字段: gender
     */
    private Integer gender;
    
    /**
     * 房间状态
     * 对应数据库字段: status (1-正常, 0-维修, 2-已满)
     * 解决报错: 无法解析方法 'setStatus'
     */
    private Integer status;
    
    /**
     * 逻辑删除标志
     * 对应数据库字段: del_flag
     */
    @TableLogic(value = "0", delval = "1") // 假设你数据库注释里没写默认值，这里按通用配置，如果有变请修改
    @TableField("del_flag")
    private String delFlag;
}