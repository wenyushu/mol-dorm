package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 宿舍楼栋实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dorm_building")
@Schema(description = "宿舍楼栋信息")
public class DormBuilding extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键 ID")
    private Long id;
    
    // 🔗 归属：校区
    @NotNull(message = "所属校区不能为空")
    @Schema(description = "所属校区 ID")
    private Long campusId;
    
    @NotBlank(message = "楼栋名称不能为空")
    @Schema(description = "楼栋名称 (如: 海棠苑1号楼)")
    private String buildingName;
    
    @Schema(description = "楼栋编号 (如: HT-01)")
    private String buildingNo;
    
    @Schema(description = "总层数 (用于前端生成楼层)")
    private Integer floorCount;
    
    /**
     * 🛡️ 性别熔断核心:
     * 1: 男楼 (全楼纯爷们，禁止创建女层)
     * 2: 女楼 (全楼女生，禁止创建男层)
     * 3: 混合楼 (需结合楼层限制，如 1-3层男，4-6层女)
     */
    @NotNull(message = "性别限制不能为空")
    @Schema(description = "性别限制: 1-男楼 2-女楼 3-混合楼")
    private Integer genderLimit;
    
    /**
     * 🛡️ 用途隔离:
     * 防止把学生分到教工楼，或者教工分到学生楼。
     */
    @NotNull(message = "楼栋用途不能为空")
    @Schema(description = "用途: 0-学生宿舍 1-教职工公寓")
    private Integer usageType;
    
    @Schema(description = "宿管负责人 ID")
    private Long managerId;
    
    @Schema(description = "地理位置")
    private String location;
    
    /**
     * 1: 启用
     * 0: 停用
     * 41: 装修中 (整栋楼封锁)
     */
    @NotNull(message = "状态不能为空")
    @Schema(description = "状态: 1-启用(正常) 0-停用(废弃) 41-装修(停用)")
    private Integer status;
    
    @Schema(description = "备注")
    private String remark;
    
    @TableLogic
    private String delFlag;
}