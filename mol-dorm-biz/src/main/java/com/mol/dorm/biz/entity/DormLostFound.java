package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 失物招领表 dorm_lost_found
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("dorm_lost_found")
@Schema(description = "失物招领")
public class DormLostFound extends BaseEntity {
    
    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Schema(description = "物品名称")
    private String itemName;
    
    @Schema(description = "物品描述/特征")
    private String description;
    
    @Schema(description = "性别限制: 0-不限, 1-男, 2-女 (例如仅限女生宿舍寻找)")
    private String genderLimit;
    
    @Schema(description = "发现/丢失的大致位置")
    private String location;
    
    @Schema(description = "领取方式: 1-联系个人, 2-宿管站自取")
    private Integer claimMethod;
    
    @Schema(description = "领取具体地点 (如: 南苑A栋值班室)")
    private String claimLocation;
    
    @Schema(description = "图片地址")
    private String imageUrl;
    
    @Schema(description = "类型: 1-拾物(捡到), 2-失物(寻找)")
    private Integer type;
    
    @Schema(description = "状态: 0-进行中, 1-已完成")
    private Integer status;
    
    @Schema(description = "联系人称呼")
    private String contactName;
    
    @Schema(description = "联系电话")
    private String contactPhone;
    
    @Schema(description = "发布人 ID")
    private Long publishUserId;
}