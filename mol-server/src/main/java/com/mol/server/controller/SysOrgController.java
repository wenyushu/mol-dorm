package com.mol.server.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
 * 🔒 权限策略：
 * 1. 查询接口 (GET) -> @SaCheckLogin (所有登录用户可查，用于下拉框联动)
 * 2. 管理接口 (POST/PUT/DELETE) -> @SaCheckRole(SUPER_ADMIN) (仅限超管操作，防止数据被乱改)
 *
 * @author mol
 */
@Tag(name = "组织架构", description = "学院-专业-班级管理及联动查询")
@RestController
@RequestMapping("/org")
@RequiredArgsConstructor
public class SysOrgController {
    
    private final SysCollegeService collegeService;
    private final SysMajorService majorService;
    private final SysClassService classService;
    
    // =================================================================================
    //                                  1. 学院管理 (College)
    // =================================================================================
    
    /**
     * 查询所有学院列表 (用于下拉框)
     */
    @SaCheckLogin
    @Operation(summary = "查询所有学院", description = "返回所有未删除的学院，用于前端选择")
    @GetMapping("/college/list")
    public R<List<SysCollege>> listCollege() {
        return R.ok(collegeService.lambdaQuery()
                .eq(SysCollege::getDelFlag, "0")
                .orderByAsc(SysCollege::getSort) // 按照设定的排序号排序
                .list());
    }
    
    /**
     * 新增学院
     */
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 仅超管
    @Operation(summary = "新增学院 (Admin)", description = "创建新的二级学院")
    @PostMapping("/college")
    public R<Boolean> saveCollege(@RequestBody SysCollege college) {
        return R.ok(collegeService.save(college));
    }
    
    /**
     * 修改学院
     */
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 仅超管
    @Operation(summary = "修改学院 (Admin)", description = "修改名称、代码、负责人等")
    @PutMapping("/college")
    public R<Boolean> updateCollege(@RequestBody SysCollege college) {
        return R.ok(collegeService.updateById(college));
    }
    
    /**
     * 删除学院
     */
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 仅超管
    @Operation(summary = "删除学院 (Admin)", description = "逻辑删除学院。注意：若学院下有专业，建议先清理专业。")
    @DeleteMapping("/college/{id}")
    public R<Boolean> removeCollege(@PathVariable Long id) {
        // 这里只是逻辑删除，实际业务中最好先 checkMajorCount > 0 则抛异常
        return R.ok(collegeService.removeById(id));
    }
    
    // =================================================================================
    //                                  2. 专业管理 (Major)
    // =================================================================================
    
    /**
     * 级联查询：查某学院下的专业
     */
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
    
    /**
     * 新增专业
     */
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 仅超管
    @Operation(summary = "新增专业 (Admin)")
    @PostMapping("/major")
    public R<Boolean> saveMajor(@RequestBody SysMajor major) {
        return R.ok(majorService.save(major));
    }
    
    /**
     * 修改专业
     */
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 仅超管
    @Operation(summary = "修改专业 (Admin)", description = "修改专业名称、层次(本科/专科)、学制等")
    @PutMapping("/major")
    public R<Boolean> updateMajor(@RequestBody SysMajor major) {
        return R.ok(majorService.updateById(major));
    }
    
    /**
     * 删除专业
     */
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 仅超管
    @Operation(summary = "删除专业 (Admin)")
    @DeleteMapping("/major/{id}")
    public R<Boolean> removeMajor(@PathVariable Long id) {
        return R.ok(majorService.removeById(id));
    }
    
    // =================================================================================
    //                                  3. 班级管理 (Class)
    // =================================================================================
    
    /**
     * 级联查询：查某专业下的班级 (基础列表)
     */
    @SaCheckLogin
    @Operation(summary = "查询某专业下的班级", description = "根据专业ID查询班级，用于三级联动")
    @GetMapping("/class/list/{majorId}")
    public R<List<SysClass>> listClassByMajor(
            @Parameter(description = "专业 ID") @PathVariable Long majorId) {
        return R.ok(classService.lambdaQuery()
                .eq(SysClass::getMajorId, majorId)
                .eq(SysClass::getDelFlag, "0")
                .orderByDesc(SysClass::getGrade) // 2024级排在2023级前面
                .list());
    }
    
    /**
     * ✅ 增强功能：分页查询班级详情 (返回 VO)
     * 解决前端展示 "网络安全学院 网络安全 本科 24级网络安全1班" 的需求
     */
    @SaCheckLogin
    @Operation(summary = "分页查询班级列表 (含全名)", description = "返回结果包含：学院名、专业名、层次、以及拼接好的全名")
    @GetMapping("/class/page")
    public R<IPage<SysClassVO>> pageClassVo(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "年级筛选") @RequestParam(required = false) Integer grade,
            @Parameter(description = "班级名模糊搜") @RequestParam(required = false) String name) {
        
        // 构造查询参数实体
        SysClass queryParam = new SysClass();
        queryParam.setGrade(grade);
        queryParam.setName(name);
        
        // 调用 Service 的增强查询方法
        // 注意：你需要在 SysClassService 中实现这个方法，调用 Mapper 的 selectClassVoPage
        // return R.ok(classService.getClassVoPage(new Page<>(pageNum, pageSize), queryParam));
        
        // ⚠️ 临时代码（如果你还没写 Service 实现，请先用这个占位，否则报错）：
        return R.ok(null);
    }
    
    /**
     * 新增班级
     */
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 仅超管
    @Operation(summary = "新增班级 (Admin)")
    @PostMapping("/class")
    public R<Boolean> saveClass(@RequestBody SysClass clazz) {
        return R.ok(classService.save(clazz));
    }
    
    /**
     * 修改班级
     */
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 仅超管
    @Operation(summary = "修改班级 (Admin)", description = "修改班级名称、所属专业、年级等")
    @PutMapping("/class")
    public R<Boolean> updateClass(@RequestBody SysClass clazz) {
        return R.ok(classService.updateById(clazz));
    }
    
    /**
     * 删除班级
     */
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 仅超管
    @Operation(summary = "删除班级 (Admin)")
    @DeleteMapping("/class/{id}")
    public R<Boolean> removeClass(@PathVariable Long id) {
        return R.ok(classService.removeById(id));
    }
}