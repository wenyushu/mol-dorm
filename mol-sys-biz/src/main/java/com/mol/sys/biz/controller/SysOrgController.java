package com.mol.sys.biz.controller;

import com.mol.common.core.util.R;
import com.mol.sys.biz.entity.SysClass;
import com.mol.sys.biz.entity.SysCollege;
import com.mol.sys.biz.entity.SysMajor;
import com.mol.sys.biz.service.SysClassService;
import com.mol.sys.biz.service.SysCollegeService;
import com.mol.sys.biz.service.SysMajorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织架构管理 (学院/专业/班级)
 * 提供级联查询接口，用于前端"选择学院->选择专业->选择班级"的联动
 */
@Tag(name = "组织架构", description = "学院-专业-班级管理")
@RestController
@RequestMapping("/org")
@RequiredArgsConstructor
public class SysOrgController {
    
    private final SysCollegeService collegeService;
    private final SysMajorService majorService;
    private final SysClassService classService;
    
    // ================== 1. 学院 (College) ==================
    
    @Operation(summary = "查询所有学院", description = "用于下拉框选择")
    @GetMapping("/college/list")
    public R<List<SysCollege>> listCollege() {
        // 查所有未删除的
        return R.ok(collegeService.lambdaQuery().eq(SysCollege::getDelFlag, "0").list());
    }
    
    @Operation(summary = "新增学院")
    @PostMapping("/college")
    public R<Boolean> saveCollege(@RequestBody SysCollege college) {
        return R.ok(collegeService.save(college));
    }
    
    // ================== 2. 专业 (Major) ==================
    
    @Operation(summary = "查询某学院下的专业", description = "根据学院 ID 级联查询")
    @GetMapping("/major/list/{collegeId}")
    public R<List<SysMajor>> listMajorByCollege(@Parameter(description = "学院 ID") @PathVariable Long collegeId) {
        return R.ok(majorService.lambdaQuery()
                .eq(SysMajor::getCollegeId, collegeId)
                .eq(SysMajor::getDelFlag, "0")
                .list());
    }
    
    @Operation(summary = "新增专业")
    @PostMapping("/major")
    public R<Boolean> saveMajor(@RequestBody SysMajor major) {
        return R.ok(majorService.save(major));
    }
    
    // ================== 3. 班级 (Class) ==================
    
    @Operation(summary = "查询某专业下的班级", description = "根据专业 ID 级联查询")
    @GetMapping("/class/list/{majorId}")
    public R<List<SysClass>> listClassByMajor(@Parameter(description = "专业 ID") @PathVariable Long majorId) {
        return R.ok(classService.lambdaQuery()
                .eq(SysClass::getMajorId, majorId)
                .eq(SysClass::getDelFlag, "0")
                .list());
    }
    
    @Operation(summary = "新增班级")
    @PostMapping("/class")
    public R<Boolean> saveClass(@RequestBody SysClass clazz) {
        return R.ok(classService.save(clazz));
    }
}