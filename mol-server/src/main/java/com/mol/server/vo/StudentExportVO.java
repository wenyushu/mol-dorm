package com.mol.server.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 📤 导出专用 VO (全维度档案版)
 */
@Data
@ColumnWidth(25)
public class StudentExportVO {
    
    @ExcelProperty("完整归属链 (学生:全路径/教工:部门)")
    @ColumnWidth(60)
    private String classFullName;
    
    @ExcelProperty("姓名")
    private String realName;
    
    @ExcelProperty("性别")
    private String gender;
    
    @ExcelProperty("身份类型")
    private String userCategory;
    
    @ExcelProperty("所属校区") // ✨ 补全，解决报错
    private String campusName;
    
    @ExcelProperty("所属学院/部门") // ✨ 补全，解决报错
    private String collegeName;
    
    @ExcelProperty("所属专业")
    private String majorName;
    
    @ExcelProperty("培养层次")
    private String eduLevel;
    
    @ExcelProperty("学号/工号")
    private String username;
    
    @ExcelProperty("身份证号")
    private String idCard;
    
    @ExcelProperty("年龄")
    private Integer age;
    
    @ExcelProperty("手机号")
    private String phone;
    
    @ExcelProperty("入职/入学年份")
    private Integer entryYear;
    
    @ExcelProperty("家庭居住地址")
    private String homeAddress;
    
    @ExcelProperty("当前状态")
    private String status;
}