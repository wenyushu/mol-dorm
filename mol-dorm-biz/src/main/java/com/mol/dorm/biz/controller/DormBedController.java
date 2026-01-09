package com.mol.dorm.biz.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.service.DormBedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 宿舍床位管理控制器
 */
@Tag(name = "床位管理", description = "宿舍床位的增删改查及分配")
@RestController
@RequestMapping("/bed")
@RequiredArgsConstructor
public class DormBedController {
    
    private final DormBedService bedService;
    
    @Operation(summary = "分页查询床位")
    @GetMapping("/page")
    public R<IPage<DormBed>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "房间 ID (可选)") @RequestParam(required = false) Long roomId,
            @Parameter(description = "是否空闲 (可选, true查空闲)") @RequestParam(required = false) Boolean isFree) {
        
        return R.ok(bedService.lambdaQuery()
                .eq(roomId != null, DormBed::getRoomId, roomId)
                .isNull(isFree != null && isFree, DormBed::getOccupantId) // 如果 isFree=true，查 occupantId 为空的
                .isNotNull(isFree != null && !isFree, DormBed::getOccupantId) // 如果 isFree=false，查被占用的
                .page(new Page<>(pageNum, pageSize)));
    }
    
    @Operation(summary = "查询某房间的所有床位")
    @GetMapping("/list/{roomId}")
    public R<List<DormBed>> listByRoom(@PathVariable Long roomId) {
        return R.ok(bedService.lambdaQuery().eq(DormBed::getRoomId, roomId).list());
    }
    
    @Operation(summary = "新增床位")
    @PostMapping
    public R<Boolean> save(@RequestBody DormBed bed) {
        return R.ok(bedService.save(bed));
    }
    
    @Operation(summary = "修改床位信息")
    @PutMapping
    public R<Boolean> update(@RequestBody DormBed bed) {
        return R.ok(bedService.updateById(bed));
    }
    
    @Operation(summary = "删除床位")
    @DeleteMapping("/{id}")
    public R<Boolean> remove(@PathVariable Long id) {
        return R.ok(bedService.removeById(id));
    }
    
    // ------------------- 核心业务接口 -------------------
    
    @Operation(summary = "分配床位 (入住)", description = "将指定用户分配到指定床位，具备并发保护")
    @PostMapping("/assign")
    public R<Void> assignBed(
            @Parameter(description = "床位 ID", required = true) @RequestParam Long bedId,
            @Parameter(description = "用户 ID", required = true) @RequestParam Long userId) {
        
        bedService.assignUserToBed(bedId, userId);
        return R.ok(null, "分配成功");
    }
    
    @Operation(summary = "释放床位 (退宿)")
    @PostMapping("/release")
    public R<Void> releaseBed(@Parameter(description = "床位 ID") @RequestParam Long bedId) {
        bedService.releaseBed(bedId);
        return R.ok(null, "释放成功");
    }
}