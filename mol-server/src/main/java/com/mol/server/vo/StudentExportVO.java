package com.mol.server.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 📤 导出专用 VO
 * <p>
 * 负责将数据库里的 ID 和 代码 翻译成人类可读的中文。
 * </p>
 */
@Data
@ColumnWidth(25)
public class StudentExportVO {
    
    @ExcelProperty("学号/工号")
    private String username;
    
    @ExcelProperty("姓名")
    private String realName;
    
    @ExcelProperty("性别") // 导出时显示 "男/女"
    private String gender;
    
    @ExcelProperty("身份") // 导出时显示 "学生/教工"
    private String userCategory;
    
    @ExcelProperty("手机号")
    private String phone;
    
    @ExcelProperty("身份证号")
    private String idCard;
    
    // --- 核心归属 (导出时显示具体名称，而不是 ID) ---
    
    @ExcelProperty("所属校区")
    private String campusName;
    
    @ExcelProperty("所属学院")
    private String collegeName;
    
    @ExcelProperty("所属专业")
    private String majorName;
    
    @ExcelProperty("所属班级")
    private String className;
    
    @ExcelProperty("所属部门")
    private String deptName;
    
    // --- 补充信息 ---
    @ExcelProperty("入学/入职年份")
    private Integer entryYear;
    
    @ExcelProperty("状态") // 正常/停用
    private String status;
}