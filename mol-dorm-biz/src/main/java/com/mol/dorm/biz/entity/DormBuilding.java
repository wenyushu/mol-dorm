package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 宿舍楼实体类
 * <p>
 * 对应数据库表：dorm_building
 * 包含楼栋的基础信息、状态控制及物理属性
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dorm_building")
@Schema(description = "宿舍楼信息对象")
public class DormBuilding extends BaseEntity implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "楼栋主键 ID")
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "所属校区 ID")
    private Long campusId;
    
    @Schema(description = "楼栋名称 (如: 北苑3号楼)")
    private String name;
    
    /**
     * 楼宇类型
     * 1-男生楼, 2-女生楼, 3-混合楼
     * (对应前端下拉框选项)
     */
    @Schema(description = "楼宇类型: 1-男生楼, 2-女生楼, 3-混合楼")
    private Integer type;
    
    /**
     * 楼层总数
     * 用于“一键建楼”时的循环上限
     */
    @Schema(description = "总层数")
    private Integer floors;
    
    @Schema(description = "是否有电梯 (true-有, false-无)")
    private Boolean hasElevator;
    
    /**
     * 宿管负责人姓名
     * (从 DTO 复制属性时需要此字段)
     */
    @Schema(description = "宿管负责人")
    private String manager;
    
    /**
     * 地理位置
     * (如: "北校区东侧", 从 DTO 复制属性时需要此字段)
     */
    @Schema(description = "地理位置")
    private String location;
    
    /**
     * 状态 (核心字段)
     * 1-正常/启用, 0-停用/封禁
     * <p>
     * ⚠️ 修复报错: 无法解析 'getStatus' / 'setStatus'
     * Service 中 updateBuilding 方法依赖此字段进行封楼校验
     * </p>
     */
    @Schema(description = "状态: 1-启用, 0-封禁")
    private Integer status;
    
    /**
     * 逻辑删除标志
     * 0-正常, 1-删除
     * (MyBatis-Plus 全局配置通常已处理，显式写出可增强可读性)
     */
    @Schema(description = "逻辑删除标志")
    private String delFlag;
}