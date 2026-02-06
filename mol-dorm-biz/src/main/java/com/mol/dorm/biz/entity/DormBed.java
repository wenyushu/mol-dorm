package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * 床位实体类 - 资源树 Level 5 (末端)
 */
@Data
@TableName("dorm_bed")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "床位层级：人员绑定的原子节点")
public class DormBed extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 🔗 全链路冗余：支持从任意维度直接定位床位
    private Long campusId;
    private Long buildingId;
    private Long floorId;
    private Long roomId;
    
    @Schema(description = "床位显示标签 (如: 305-1)")
    private String bedLabel;
    
    @Schema(description = "排序/方位序号 (1-左上, 2-左下...)")
    private Integer sortOrder;
    
    @Schema(description = "入住人主键ID (sys_ordinary_user)")
    private Long occupantId;
    
    /**
     * 🛡️ 居住者类型隔离:
     * 0: 学生, 1: 教职工 (🛡️防刁民：禁止跨类别分配)
     */
    @Schema(description = "居住类型: 0-学生, 1-教工")
    private Integer occupantType;
    
    /**
     * 🚦 生命周期 (Lifecycle):
     * 20: 正常, 50: 维修(床板坏了), 80: 保留(放杂物)
     */
    @Schema(description = "生命周期状态码")
    private Integer status;
    
    /**
     * 📊 业务状态码 (Resource Status):
     * 21: 空闲, 22: 正常使用(已住人)
     */
    @TableField("res_status")
    @Schema(description = "状态码: 21空闲, 22已住")
    private Integer resStatus;
    
    @Version
    private Integer version;
}