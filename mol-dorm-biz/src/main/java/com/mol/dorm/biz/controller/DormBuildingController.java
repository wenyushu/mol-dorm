package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.DormBuilding;
import com.mol.dorm.biz.service.DormBuildingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 宿舍楼管理控制器
 * <p>
 * 权限说明：
 * 1. 查询 (GET) -> 对所有登录用户开放
 * 2. 增删改 (POST/PUT/DELETE) -> 仅限超级管理员 (Super Admin)
 */
@Tag(name = "宿舍楼管理", description = "楼宇的增删改查 (固定资产)")
@RestController
@RequestMapping("/building")
@RequiredArgsConstructor
public class DormBuildingController {
    
    private final DormBuildingService buildingService;
    
    @Operation(summary = "分页查询宿舍楼", description = "查询所有有效楼栋 (不包含已删除的)")
    @GetMapping("/page")
    public R<Page<DormBuilding>> page(Page<DormBuilding> page) {
        // 自动过滤逻辑删除数据 (MyBatis-Plus 默认行为，前提是配置生效)
        // 这里显式加上 del_flag=0 也是一种保险写法
        return R.ok(buildingService.lambdaQuery()
                .eq(DormBuilding::getDelFlag, "0")
                .page(page));
    }
    
    @Operation(summary = "新增宿舍楼 (仅 Admin)", description = "建设新的宿舍楼，涉及校区规划")
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 权限锁：只有超管能进
    @PostMapping
    public R<Boolean> save(@RequestBody DormBuilding building) {
        return R.ok(buildingService.save(building));
    }
    
    @Operation(summary = "修改宿舍楼信息 (仅 Admin)", description = "如：修改楼名、加装电梯状态")
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 权限锁
    @PutMapping
    public R<Boolean> update(@RequestBody DormBuilding building) {
        return R.ok(buildingService.updateById(building));
    }
    
    @Operation(summary = "删除宿舍楼 (仅 Admin)", description = "拆除或废弃楼栋")
    @SaCheckRole(RoleConstants.SUPER_ADMIN) // 🔒 权限锁
    @DeleteMapping("/{id}")
    public R<Boolean> remove(@PathVariable Long id) {
        return R.ok(buildingService.removeById(id));
    }
}