package com.mol.server.vo;

import cn.hutool.core.util.StrUtil;
import com.mol.server.entity.SysClass;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 班级视图对象 - 拼接版
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysClassVO extends SysClass {
    
    @Schema(description = "所属学院名称")
    private String collegeName;
    
    @Schema(description = "所属专业名称")
    private String majorName;
    
    @Schema(description = "培养层次，例如：本科")
    private String eduLevel;
    
    @Schema(description = "学制年限，例如：4")
    private Integer duration;
    
    /**
     * 🛡️ 终极全名拼接算法 [对齐李毅乐样式]
     * 格式：[校区名]-[学院名]-[专业名]-[层次]-[学制]年-[年级]级-[班级名]
     * 示例：广州校区南校园-计算机学院-人工智能-本科-4年-25级-人工1班
     * * @param campusName 外部传入的校区名称
     * @return 完整归属链条
     */
    public String getFullName(String campusName) {
        StringBuilder sb = new StringBuilder();
        
        // 1. 校区-学院-专业 (核心三段)
        sb.append(StrUtil.blankToDefault(campusName, "未知校区")).append("-");
        sb.append(StrUtil.blankToDefault(collegeName, "未知学院")).append("-");
        sb.append(StrUtil.blankToDefault(majorName, "未知专业")).append("-");
        
        // 2. 层次-学制 (生命周期段)
        sb.append(StrUtil.blankToDefault(eduLevel, "层次未知")).append("-");
        sb.append(duration != null ? duration : "X").append("年-");
        
        // 3. 年级-班级 (具体定位段)
        sb.append(getGrade() != null ? getGrade() : "XX").append("级-");
        sb.append(getClassName() != null ? getClassName() : "未知班级");
        
        return sb.toString();
    }
}