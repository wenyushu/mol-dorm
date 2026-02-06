package com.mol.server.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * 校区资源树视图对象 (底层解耦版)
 * 🛡️ [架构防线]：不引用业务模块实体，通过 DTO 承载，防止循环依赖。
 */
@Data
@Schema(description = "校区-楼栋树状结构")
public class SysCampusTreeVO {
    
    @Schema(description = "校区 ID")
    private Long id;
    
    @Schema(description = "校区名称")
    private String campusName;
    
    @Schema(description = "校区状态")
    private Integer status;
    
    @Schema(description = "下属楼栋简要信息")
    private List<BuildingItemVO> buildings;
    
    /**
     * 内部类：楼栋简要信息
     */
    @Data
    public static class BuildingItemVO {
        private Long id;
        private String name;
        @Schema(description = "用途: 0-学生, 1-教工")
        private Integer usageType;
        @Schema(description = "生命周期状态: 20正常等")
        private Integer status;
    }
}