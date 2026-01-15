package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 宿舍楼栋实体类
 * <p>
 * 映射数据库表: dorm_building
 * 该表统一管理全校所有住宿楼宇，通过 usageType 区分学生宿舍与教职工公寓。
 * </p>
 *
 * @author mol
 */
@Data
@TableName("dorm_building")
@Schema(description = "宿舍楼栋信息")
public class DormBuilding implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键 ID
     */
    @Schema(description = "楼栋ID (主键)")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 所属校区 ID
     * 用于多校区物理隔离，分配时通过此字段筛选
     */
    @Schema(description = "所属校区 ID")
    private Long campusId;
    
    /**
     * 楼栋名称
     * 例如："海棠苑1号楼", "教工公寓A座"
     */
    @Schema(description = "楼栋名称")
    private String buildingName;
    
    /**
     * 楼栋编号/代码
     * 例如："HT-01", "JG-A"
     */
    @Schema(description = "楼栋编号")
    private String buildingNo;
    
    /**
     * 总层数
     * 用于前端楼层选择器展示
     */
    @Schema(description = "楼层总数")
    private Integer floorCount;
    
    /**
     * 性别限制
     * 0-混合 (通常用于特殊的高层次人才夫妻房，或整栋楼分层混住但房间独立)
     * 1-男 (男寝/男教工宿舍)
     * 2-女 (女寝/女教工宿舍)
     */
    @Schema(description = "性别限制: 0-混合 1-男 2-女")
    private Integer gender;
    
    /**
     * 宿管负责人 ID
     * 关联 sys_ordinary_user 表
     */
    @Schema(description = "宿管负责人 ID")
    private Long managerId;
    
    /**
     * 地理位置
     * 存储经纬度或具体的文字描述
     */
    @Schema(description = "地理位置")
    private String location;
    
    /**
     * 用途分类 (核心业务字段)
     * 0-学生宿舍 (Student Dorm) -> 走智能分配算法
     * 1-教职工公寓 (Staff Apartment) -> 走申请审批流程
     */
    @Schema(description = "用途: 0-学生宿舍 1-教职工公寓")
    private Integer usageType;
    
    /**
     * 状态
     * 1-启用 (正常分配)
     * 0-停用 (装修中/封楼/废弃)
     */
    @Schema(description = "状态: 1-启用 0-停用")
    private Integer status;
    
    /**
     * 备注信息
     */
    @Schema(description = "备注")
    private String remark;
    
    /**
     * 逻辑删除标识
     * MyBatis-Plus 自动处理：查询时自动带上 del_flag='0'
     */
    @TableLogic
    @Schema(description = "逻辑删除: 0-正常 1-已删除")
    private String delFlag;
    
    /**
     * 创建时间
     * 自动填充
     */
    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     * 自动填充
     */
    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}