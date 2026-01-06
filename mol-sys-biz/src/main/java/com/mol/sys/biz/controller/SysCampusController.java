package com.mol.sys.biz.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
 * 校区管理
 * <p>提供校区的增删改查功能</p>
 *
 * @author mol
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/campus")
@Tag(name = "校区管理接口", description = "校区基础信息的维护与查询")
public class SysCampusController {
    
    private final SysCampusService sysCampusService;
    
    /**
     * 分页查询校区列表
     * @param page 分页对象（Apifox 会自动识别 current 和 size 参数）
     * @return 分页后的数据
     */
    @Operation(summary = "分页查询校区", description = "根据页码和条数获取校区列表")
    @GetMapping("/page")
    public R<Page<SysCampus>> getCampusPage(Page<SysCampus> page) {
        // 使用前端传进来的 page 对象，这样 current (页码) 和 size (每页条数) 才会生效
        return R.ok(sysCampusService.page(page));
    }
    
    /**
     * 获取所有校区列表
     * <p>常用于下拉框选择</p>
     */
    @Operation(summary = "获取所有校区列表", description = "获取所有校区数据，不进行分页")
    @GetMapping("/list")
    public R<List<SysCampus>> getList() {
        return R.ok(sysCampusService.list(Wrappers.emptyWrapper()));
    }
    
    /**
     * 新增校区
     * @param sysCampus 校区信息对象
     */
    @Operation(summary = "新增校区")
    @PostMapping
    public R<Boolean> save(@RequestBody SysCampus sysCampus) {
        return R.ok(sysCampusService.save(sysCampus));
    }
    
    /**
     * 根据 ID 删除校区
     * @param id 校区主键 ID
     */
    @Operation(summary = "删除校区")
    @Parameter(name = "id", description = "校区 ID", required = true)
    @DeleteMapping("/{id}")
    public R<Boolean> removeById(@PathVariable Long id) {
        return R.ok(sysCampusService.removeById(id));
    }
}