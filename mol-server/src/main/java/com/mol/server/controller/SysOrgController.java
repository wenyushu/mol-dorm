package com.mol.server.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.server.entity.SysClass;
import com.mol.server.entity.SysCollege;
import com.mol.server.entity.SysMajor;
import com.mol.server.service.SysClassService;
import com.mol.server.service.SysCollegeService;
import com.mol.server.service.SysMajorService;
import com.mol.server.vo.SysClassVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织架构管理控制器
 * <p>
 * 包含：学院(College)、专业(Major)、班级(Class) 的全生命周期管理。
 * </p>
 *
 * @author mol
 */
@Tag(name = "组织架构", description = "学院-专业-班级管理及联动查询")
@RestController
@RequestMapping("/sys/org") // 建议加 /sys 前缀规范路径
@RequiredArgsConstructor
public class SysOrgController {
    
    private final SysCollegeService collegeService;
    private final SysMajorService majorService;
    private final SysClassService classService;
    
    // =================================================================================
    //                                  1. 学院管理 (College)
    // =================================================================================
    
    @SaCheckLogin
    @Operation(summary = "查询所有学院", description = "返回所有未删除的学院，用于前端选择")
    @GetMapping("/college/list")
    public R<List<SysCollege>> listCollege() {
        return R.ok(collegeService.lambdaQuery()
                .eq(SysCollege::getDelFlag, "0")
                .orderByAsc(SysCollege::getSort)
                .list());
    }
    
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @Operation(summary = "新增学院 (Admin)")
    @PostMapping("/college")
    public R<Boolean> saveCollege(@RequestBody SysCollege college) {
        return R.ok(collegeService.save(college));
    }
    
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @Operation(summary = "修改学院 (Admin)")
    @PutMapping("/college")
    public R<Boolean> updateCollege(@RequestBody SysCollege college) {
        return R.ok(collegeService.updateById(college));
    }
    
    /**
     * 修复点：删除学院
     * 显式判断结果，防止假成功
     */
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @Operation(summary = "删除学院 (Admin)", description = "逻辑删除学院")
    @DeleteMapping("/college/{id}")
    public R<Void> removeCollege(@PathVariable Long id) {
        boolean result = collegeService.removeById(id);
        
        if (result) {
            // 🟢 修改处：使用 R.ok()，不要传字符串，否则变成 R<String>
            return R.ok();
        } else {
            // 失败时返回错误信息，R.fail 可以适配 R<Void>
            return R.fail("删除失败：ID [" + id + "] 不存在或已被删除");
        }
    }
    
    // =================================================================================
    //                                  2. 专业管理 (Major)
    // =================================================================================
    
    @SaCheckLogin
    @Operation(summary = "查询某学院下的专业", description = "根据学院ID查询其下属专业，用于二级联动")
    @GetMapping("/major/list/{collegeId}")
    public R<List<SysMajor>> listMajorByCollege(
            @Parameter(description = "学院 ID") @PathVariable Long collegeId) {
        return R.ok(majorService.lambdaQuery()
                .eq(SysMajor::getCollegeId, collegeId)
                .eq(SysMajor::getDelFlag, "0")
                .list());
    }
    
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @Operation(summary = "新增专业 (Admin)")
    @PostMapping("/major")
    public R<Boolean> saveMajor(@RequestBody SysMajor major) {
        return R.ok(majorService.save(major));
    }
    
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @Operation(summary = "修改专业 (Admin)")
    @PutMapping("/major")
    public R<Boolean> updateMajor(@RequestBody SysMajor major) {
        return R.ok(majorService.updateById(major));
    }
    
    /**
     * 修复点：删除专业
     */
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @Operation(summary = "删除专业 (Admin)")
    @DeleteMapping("/major/{id}")
    public R<Void> removeMajor(@PathVariable Long id) {
        boolean result = majorService.removeById(id);
        if (result) {
            // 🟢 修改处
            return R.ok();
        } else {
            return R.fail("删除失败：ID [" + id + "] 不存在");
        }
    }
    
    // =================================================================================
    //                                  3. 班级管理 (Class)
    // =================================================================================
    
    @SaCheckLogin
    @Operation(summary = "查询某专业下的班级", description = "根据专业ID查询班级，用于三级联动")
    @GetMapping("/class/list/{majorId}")
    public R<List<SysClass>> listClassByMajor(
            @Parameter(description = "专业 ID") @PathVariable Long majorId) {
        return R.ok(classService.lambdaQuery()
                .eq(SysClass::getMajorId, majorId)
                .eq(SysClass::getDelFlag, "0")
                .orderByDesc(SysClass::getGrade)
                .list());
    }
    
    @SaCheckLogin
    @Operation(summary = "分页查询班级列表 (含全名)", description = "返回结果包含：学院名、专业名、层次、以及拼接好的全名")
    @GetMapping("/class/page")
    public R<IPage<SysClassVO>> pageClassVo(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "年级筛选") @RequestParam(required = false) Integer grade,
            @Parameter(description = "班级名模糊搜") @RequestParam(required = false) String name) {
        
        SysClass queryParam = new SysClass();
        queryParam.setGrade(grade);
        queryParam.setClassName(name);
        
        return R.ok(classService.getClassVoPage(new Page<>(pageNum, pageSize), queryParam));
    }
    
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @Operation(summary = "新增班级 (Admin)")
    @PostMapping("/class")
    public R<Boolean> saveClass(@RequestBody SysClass clazz) {
        return R.ok(classService.save(clazz));
    }
    
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @Operation(summary = "修改班级 (Admin)")
    @PutMapping("/class")
    public R<Boolean> updateClass(@RequestBody SysClass clazz) {
        return R.ok(classService.updateById(clazz));
    }
    
    /**
     * 修复点：删除班级
     */
    @SaCheckRole(RoleConstants.SUPER_ADMIN)
    @Operation(summary = "删除班级 (Admin)")
    @DeleteMapping("/class/{id}")
    public R<Void> removeClass(@PathVariable Long id) {
        boolean result = classService.removeById(id);
        if (result) {
            // 🟢 修改处
            return R.ok();
        } else {
            return R.fail("删除失败：ID [" + id + "] 不存在");
        }
    }
}