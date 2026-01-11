package com.mol.sys.biz.vo;

import com.mol.sys.biz.entity.SysClass;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 班级视图对象 (用于前端展示)
 * <p>
 * 包含：基础班级信息 + 学院名称 + 专业名称 + 完整显示名称
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "班级列表展示对象")
public class SysClassVO extends SysClass {
    
    @Schema(description = "所属学院名称")
    private String collegeName;
    
    @Schema(description = "所属专业名称")
    private String majorName;
    
    @Schema(description = "培养层次 (如: 本科, 专科)")
    private String eduLevel; // 对应 Major 表的 level 字段
    
    /**
     * 获取组合后的完整名称
     * 格式示例：网络安全学院 网络安全 本科 24级网络安全1班
     */
    @Schema(description = "班级全名 (自动拼接)")
    public String getFullName() {
        // 防止空指针的防御性编程
        String cName = collegeName == null ? "未知学院" : collegeName;
        String mName = majorName == null ? "未知专业" : majorName;
        String level = eduLevel == null ? "" : eduLevel;
        Integer gradeVal = getGrade() == null ? 0 : getGrade();
        String className = getName() == null ? "未知班级" : getName();
        
        // 格式：[学院] [专业] [层次] [年级]级[班级名]
        return String.format("%s %s %s %d级%s", cName, mName, level, gradeVal, className);
    }
}