package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 房间实体类 - 资源树 Level 4
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("dorm_room")
@Schema(description = "房间层级：资源饱和度计算与费用载体")
public class DormRoom extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 🔗 冗余链路设计：提升大屏数据穿透速度
    private Long campusId;
    private Long buildingId;
    private Long floorId;
    
    @Schema(description = "物理楼层号")
    @TableField("floor_num") // 确保与数据库字段名一致
    private Integer floorNum;
    
    @Schema(description = "物理房间号 (如: 305)")
    private String roomNo;
    
    @Schema(description = "房型描述 (如: 四人间)")
    @TableField("apartment_type")
    private String apartmentType;
    
    @Schema(description = "核定总床位容量")
    private Integer capacity;
    
    @Schema(description = "当前实际入住人数")
    private Integer currentNum;
    
    @Schema(description = "住宿费标准(元/学年)")
    private BigDecimal accommodationFee;
    
    /**
     * 🛡️ 房间性别限制 (与用户表 gender 保持一致的 String 类型)
     * "1": 男寝, "0": 女寝 (🛡️防刁民：代码中强制校验此值与 Floor 的一致性)
     */
    @Schema(description = "性别: 0-女, 1-男")
    private String gender;
    
    /**
     * 🛡️ 冗余字段：房间的用途 (0-学生, 1-教工)
     * [防刁民] 虽然属于楼栋的属性，但在房间层冗余，可极大提升校验性能。
     */
    @Schema(description = "用途: 0-学生, 1-教工")
    private Integer usageType;
    
    /**
     * 🚦 生命周期 (Lifecycle):
     * 20: 正常使用, 50: 故障维修, 80: 行政预留(不分配)
     */
    @Schema(description = "生命周期: 20正常, 50维修, 80预留")
    private Integer status;
    
    /**
     * 📊 资源饱和度码 (Resource Status):
     * 21: 空闲, 23: 未满员, 24: 资源充裕, 25: 资源紧张, 26: 已满员
     */
    @TableField("resource_status")
    @Schema(description = "饱和度状态码")
    private Integer resStatus;
    
    /**
     * 安全等级 (1-安全, 2-警告, 3-危险)
     * 对应数据库新增的 safety_level 字段
     */
    @TableField("safety_level") // 显式指定数据库列名
    private Integer safetyLevel;
    
    @Version
    private Integer version;
    
}