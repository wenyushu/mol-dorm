package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * 楼栋实体类 - 资源树 Level 2
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("dorm_building")
@Schema(description = "楼栋层级：定义人群用途隔离与整体生命周期")
public class DormBuilding extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "所属校区ID")
    private Long campusId;
    
    @Schema(description = "楼栋名称 (如: 慎思园 1 号楼)")
    private String buildingName;
    
    @Schema(description = "楼栋编号 (如: SSY-01)")
    private String buildingNo;
    
    @Schema(description = "总层数")
    private Integer floorCount;
    
    /**
     * 🛡️ 用途隔离 (Usage):
     * 0: 学生宿舍, 1: 教职工公寓 (🛡️防刁民：严禁混合分配)
     */
    @Schema(description = "用途: 0-学生, 1-教工")
    private Integer usageType;
    
    /**
     * 🛡️ 性别熔断 (Gender):
     * 1: 男楼, 2: 女楼, 3: 混合楼
     */
    @Schema(description = "性别限制: 1-男, 2-女, 3-混合")
    private Integer genderLimit;
    
    /**
     * 🚦 生命周期 (Lifecycle):
     * 20: 正常, 40: 装修, 50: 维修, 60: 损坏, 0: 停止
     */
    @Schema(description = "生命周期: 20正常, 40装修, 50维修, 0停止")
    private Integer status;
    
    @Schema(description = "楼宇位置描述/坐标")
    private String location;
    
    @Version
    private Integer version;
}