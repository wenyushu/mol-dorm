package com.mol.server.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 人员批量导入 Excel 模板对象
 */
@Data
@ColumnWidth(20)
public class StudentImportVO {
    
    @ExcelProperty("身份(学生/教工)") // 🛡️ 核心：刁民判定第一哨
    private String identityType;
    
    // ==================== 1. 通用基础信息 ====================
    @ExcelProperty("学号/工号")
    private String username;
    
    @ExcelProperty("姓名")
    private String realName;
    
    @ExcelProperty("性别(男/女)")
    private String genderStr;
    
    @ExcelProperty("身份证号")
    private String idCard;
    
    @ExcelProperty("手机号")
    private String phone;
    
    @ExcelProperty("校区名称")
    private String campusName;
    
    // ==================== 2. 学生专用字段 (教工可留空) ====================
    @ExcelProperty("学院名称")
    private String collegeName;
    
    @ExcelProperty("专业名称")
    private String majorName;
    
    @ExcelProperty("培养层次(专科/本科/研究生)")
    private String eduLevel;
    
    @ExcelProperty("学制年限(如:4)")
    private Integer duration;
    
    @ExcelProperty("班级名称")
    private String className;
    
    // ==================== 3. 教工专用字段 (学生可留空) ====================
    @ExcelProperty("所属部门") // ✨ 适配教职工的核心归属
    private String deptName;
    
    @ExcelProperty("职称/职位") // ✨ 适配教职工的档案深度
    private String jobTitle;
    
    // ==================== 4. 详细档案信息 (通用) ====================
    @ExcelProperty("入学/入职年份(2026)")
    private Integer entryYear;
    
    @ExcelProperty("籍贯")
    private String hometown;
    
    @ExcelProperty("家庭居住地址")
    private String homeAddress;
    
    @ExcelProperty("紧急联系人")
    private String emergencyContact;
    
    @ExcelProperty("紧急电话")
    private String emergencyPhone;
}