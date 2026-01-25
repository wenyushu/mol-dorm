package com.mol.server.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.mol.common.core.util.R;
import com.mol.server.entity.*;
import com.mol.server.service.*;
import com.mol.server.vo.OptionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 基础信息控制器 (用于前端下拉框数据源)
 * 包含：校区、学院、专业、班级、部门的列表查询
 */
@Tag(name = "通用服务-基础选项", description = "提供前端下拉框所需的 Option 数据")
@RestController
@RequestMapping("/option")
@RequiredArgsConstructor
public class SysBaseInfoController {
    
    private final SysCampusService campusService;
    private final SysCollegeService collegeService;
    private final SysMajorService majorService;
    private final SysClassService classService;
    private final SysDeptService deptService;
    
    @Operation(summary = "获取校区列表")
    @SaCheckLogin
    @GetMapping("/campus")
    public R<List<OptionVO>> listCampus() {
        return R.ok(campusService.list().stream()
                .map(item -> new OptionVO(item.getId(), item.getCampusName()))
                .collect(Collectors.toList()));
    }
    
    @Operation(summary = "获取学院列表")
    @SaCheckLogin
    @GetMapping("/college")
    public R<List<OptionVO>> listCollege() {
        return R.ok(collegeService.list().stream()
                .map(item -> new OptionVO(item.getId(), item.getName()))
                .collect(Collectors.toList()));
    }
    
    @Operation(summary = "获取专业列表 (支持按学院筛选)")
    @SaCheckLogin
    @GetMapping("/major")
    public R<List<OptionVO>> listMajor(@RequestParam(required = false) Long collegeId) {
        // 如果传了 collegeId，就查该学院下的专业；否则查所有
        return R.ok(majorService.lambdaQuery()
                .eq(collegeId != null, SysMajor::getCollegeId, collegeId)
                .list().stream()
                .map(item -> new OptionVO(item.getId(), item.getName()))
                .collect(Collectors.toList()));
    }
    
    @Operation(summary = "获取班级列表 (支持按专业筛选)")
    @SaCheckLogin
    @GetMapping("/class")
    public R<List<OptionVO>> listClass(@RequestParam(required = false) Long majorId) {
        // 如果传了 majorId，就查该专业下的班级
        return R.ok(classService.lambdaQuery()
                .eq(majorId != null, SysClass::getMajorId, majorId)
                .list().stream()
                .map(item -> new OptionVO(item.getId(), item.getClassName()))
                .collect(Collectors.toList()));
    }
    
    @Operation(summary = "获取部门列表 (用于教工)")
    @SaCheckLogin
    @GetMapping("/dept")
    public R<List<OptionVO>> listDept() {
        return R.ok(deptService.list().stream()
                .map(item -> new OptionVO(item.getId(), item.getName()))
                .collect(Collectors.toList()));
    }
}