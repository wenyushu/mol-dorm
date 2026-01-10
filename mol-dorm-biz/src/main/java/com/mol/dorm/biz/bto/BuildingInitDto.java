package com.mol.dorm.biz.bto;

import lombok.Data;
import java.io.Serializable;

/**
 * 楼栋初始化参数 DTO
 * 用于接收前端“一键建楼”的参数
 */
@Data
public class BuildingInitDto implements Serializable {
    
    // === 楼栋基本信息 ===
    private String name;       // 楼号名称 (如: "海棠 1 号楼")
    private String type;       // 楼宇类型 (如: "本科生公寓")
    private String location;   // 位置
    private String manager;    // 宿管姓名 (可选)
    
    // === 自动生成规则 ===
    private Integer floors;        // 楼层数 (如: 7)
    private Integer roomsPerFloor; // 每层房间数 (如: 100)
    private Integer defaultCapacity; // 默认每间房容量 (如: 4)
    
    /**
     * 默认房间的性别
     * 1-男, 2-女, 0-混合
     * (如果是混合楼，通常默认设为混合，后续再单独调整某层为男/女)
     */
    private Integer defaultGender;
}