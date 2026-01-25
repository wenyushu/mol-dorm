package com.mol.server.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
@ColumnWidth(25)
public class AdminExportVO {
    
    @ExcelProperty("工号/账号")
    private String username;
    
    @ExcelProperty("真实姓名")
    private String realName;
    
    @ExcelProperty("手机号")
    private String phone;
    
    // --- 核心归属 ---
    
    @ExcelProperty("所属校区")
    private String campusName;
    
    @ExcelProperty("所属部门") // 行政人员用
    private String deptName;
    
    @ExcelProperty("负责学院") // 辅导员用
    private String collegeName;
    
    @ExcelProperty("居住类型") // 住校/校外
    private String residenceType;
    
    @ExcelProperty("账号状态")
    private String status;
}