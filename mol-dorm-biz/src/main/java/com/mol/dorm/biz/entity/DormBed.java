package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 宿舍床位实体类
 * 分配系统的最小原子单位。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dorm_bed")
@Schema(description = "宿舍床位")
public class DormBed extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 🔗 归属：全家桶 (查询性能 Max)
    // 场景：查询 "海棠苑1号楼" 所有空床位，不需要 JOIN 房间表，直接 WHERE building_id = ?
    @Schema(description = "所属校区 ID")
    private Long campusId;
    @Schema(description = "所属楼栋 ID")
    private Long buildingId;
    @Schema(description = "所属楼层 ID")
    private Long floorId;
    @Schema(description = "所属房间 ID")
    private Long roomId;
    
    @Schema(description = "床位标签 (如: 305-1)")
    private String bedLabel;
    
    /**
     * 🧭 可视化方位:
     * 1: 左上(靠门)
     * 2: 左下(靠窗)
     * 3: 右上(靠门)
     * 4: 右下(靠窗)
     * 前端根据房间户型图 + 此字段渲染 ICON。
     */
    @NotNull(message = "床位排序/方位不能为空")
    @Schema(description = "床位序号/方位 (1-4)")
    private Integer sortOrder;
    
    // 🛡️ 入住多态性 (老师/学生/宿管)
    @Schema(description = "居住者 ID")
    private Long occupantId;
    
    @Schema(description = "入住者类型: 0-普通用户 1-管理员")
    private Integer occupantType;
    
    /**
     * 🚦 床位状态机 (语义化升级):
     * 0: 正常(空闲) - 可分配
     * 1: 正常(已住) - 已分配
     * 30: 保留(占用) - 比如放杂物，不可分
     * 40: 维修(停用) - 床板断了
     * 42: 损坏(停用) - 彻底报废
     */
    @NotNull(message = "床位状态不能为空")
    @Schema(description = "状态: 0-正常(空闲) 1-正常(已住) 30-保留(占用) 40-维修(停用) 42-损坏(停用)")
    private Integer status;
    
    @Version
    private Integer version;
    
    @TableLogic
    private String delFlag;
}