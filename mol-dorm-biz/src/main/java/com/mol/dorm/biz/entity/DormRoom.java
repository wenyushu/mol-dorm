package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 宿舍房间实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dorm_room")
@Schema(description = "宿舍房间对象")
public class DormRoom extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 🔗 归属：校区 + 楼栋 + 楼层 (全链路冗余)
    @Schema(description = "所属校区 ID (冗余)")
    private Long campusId;
    @Schema(description = "所属楼栋 ID (冗余)")
    private Long buildingId;
    @Schema(description = "所属楼层 ID (关联 dorm_floor)")
    private Long floorId;
    
    // 冗余字段：方便不连表直接显示 "3 楼"
    @Schema(description = "所在楼层号 (如: 3)")
    private Integer floorNo;
    
    @NotBlank(message = "房间号不能为空")
    @Schema(description = "房间号 (如: 305)")
    private String roomNo;
    
    @Schema(description = "户型")
    private String apartmentType;
    
    @Schema(description = "核定床位数")
    private Integer capacity;
    
    @Schema(description = "当前居住人数")
    private Integer currentNum;
    
    /**
     * 🛡️ 房间性别:
     * 必须严格对应 SysOrdinaryUser.gender
     * 0: 女
     * 1: 男
     * 这里的 String 类型是为了匹配数据库 char(1) 和身份证标准。
     */
    @NotBlank(message = "房间性别限制不能为空")
    @Pattern(regexp = "[01]", message = "房间性别数据异常 (0-女 1-男)")
    @Schema(description = "房间性别: 0-女 1-男")
    private String gender;
    
    /**
     * 🚦 房间状态机 (语义化升级):
     * 10: 正常(未满) - 绿色，可分配
     * 20: 正常(满员) - 黄色，不可分配
     * 30: 保留(占用) - 灰色，被征用
     * 40: 维修(停用) - 红色，临时故障
     * 41: 装修(停用) - 红色，封闭施工
     * 42: 损坏(停用) - 红色，危房/严重损坏
     */
    @NotNull(message = "房间状态不能为空")
    @Schema(description = "状态: 10-正常(未满) 20-正常(满员) 30-保留(占用) 40-维修(停用) 41-装修(停用) 42-损坏(停用)")
    private Integer status;
    
    @Version
    private Integer version;
    
    @TableLogic
    private String delFlag;
}