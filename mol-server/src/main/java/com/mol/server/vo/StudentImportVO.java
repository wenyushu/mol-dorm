package com.mol.server.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 人员批量导入 Excel 模板对象
 * <p>
 * 对应 Excel 表格中的每一列。
 * 支持同时导入 "学生" 和 "教职工"。
 * 逻辑区分：
 * 1. 如果填了【班级名称】，则自动识别为【学生】。
 * 2. 如果没填班级，但填了【部门名称】，则识别为【行政教职工】。
 * 3. 如果没填班级，但填了【学院名称】，则识别为【教学教职工】。
 * </p>
 *
 * @author mol
 */
@Data
@ColumnWidth(20) // 设置全局列宽为 20
public class StudentImportVO {
    
    // ==================== 1. 基础身份信息 (必填) ====================
    
    @ExcelProperty("学号/工号") // 对应 Excel 第一行表头
    private String username;
    
    @ExcelProperty("姓名")
    private String realName;
    
    @ExcelProperty("性别") // 填写 "男" 或 "女" -> 代码里转 1/0
    private String genderStr;
    
    @ExcelProperty("身份证号") // 必填，用于提取初始密码或生日
    private String idCard;
    
    @ExcelProperty("手机号") // 必填
    private String phone;
    
    
    // ==================== 2. 归属链式信息 (核心外键) ====================
    // 这些字段存的是"名称"，后台导入时需要查库转成 ID
    
    @ExcelProperty("校区名称") // 必填 (如: "南校区", "北校区")
    private String campusName;
    
    @ExcelProperty("学院名称") // 学生/教师必填 (如: "计算机学院")
    private String collegeName;
    
    @ExcelProperty("专业名称") // 学生必填 (如: "软件工程")，教工留空
    private String majorName;
    
    @ExcelProperty("班级名称") // 学生必填 (如: "软工2401")，教工留空 -> 区分身份的关键
    private String className;
    
    @ExcelProperty("部门名称") // 行政教工必填 (如: "后勤处")，学生留空
    private String deptName;
    
    
    // ==================== 3. 详细档案信息 (建议填写) ====================
    
    @ExcelProperty("紧急联系人") // 建议必填 (如: "张父")
    private String emergencyContact;
    
    @ExcelProperty("紧急电话") // 建议必填
    private String emergencyPhone;
    
    @ExcelProperty("关系") // 选填 (如: "父子")
    private String emergencyRelation;
    
    @ExcelProperty("籍贯") // 选填 (如: "江苏南京")
    private String hometown;
    
    @ExcelProperty("民族") // 选填 (如: "汉族")
    private String ethnicity;
}