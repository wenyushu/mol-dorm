package com.mol.sys.biz.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.util.R;
import com.mol.sys.biz.entity.SysCampus;
import com.mol.sys.biz.service.SysCampusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 校区管理控制器
 * 提供校区的增删改查功能
 */
@Tag(name = "校区管理", description = "管理学校的各个校区信息")
@RestController
@RequestMapping("/campus")
@RequiredArgsConstructor
public class SysCampusController {
    
    private final SysCampusService campusService;
    
    @Operation(summary = "获取所有校区", description = "查询系统中的所有校区列表（不分页）")
    @GetMapping("/list")
    public R<List<SysCampus>> list() {
        return R.ok(campusService.list());
    }
    
    @Operation(summary = "分页查询校区")
    @GetMapping("/page")
    public R<IPage<SysCampus>> page(
            @Parameter(description = "页码", example = "1")
            @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum, // 显式添加 name = "pageNum"
            
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) { // 显式添加 name = "pageSize"
        
        return R.ok(campusService.page(new Page<>(pageNum, pageSize)));
    }
    
    @Operation(summary = "根据 ID 获取详情")
    @GetMapping("/{id}")
    public R<SysCampus> getInfo(@PathVariable Long id) {
        return R.ok(campusService.getById(id));
    }
    
    @Operation(summary = "新增校区")
    @PostMapping
    public R<Boolean> save(@RequestBody SysCampus campus) {
        return R.ok(campusService.save(campus));
    }
    
    @Operation(summary = "修改校区")
    @PutMapping
    public R<Boolean> update(@RequestBody SysCampus campus) {
        return R.ok(campusService.updateById(campus));
    }
    
    @Operation(summary = "删除校区")
    @DeleteMapping("/{id}")
    public R<Boolean> remove(@PathVariable Long id) {
        return R.ok(campusService.removeById(id));
    }
}