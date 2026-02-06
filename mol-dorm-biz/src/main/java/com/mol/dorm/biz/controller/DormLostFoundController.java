package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.DormLostFound;
import com.mol.dorm.biz.service.DormLostFoundService;
import com.mol.server.service.SysFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 失物招领管理
 * 🛡️ [防刁民策略]：
 * 1. 匿名发布拦截：SaCheckLogin 强制实名。
 * 2. 物理存证：图片直接存储并关联 URL，解决“口说无凭”的招领纠纷。
 */
@Tag(name = "失物招领管理")
@RestController
@RequestMapping("/lost-found")
@RequiredArgsConstructor
public class DormLostFoundController {
    
    private final DormLostFoundService lostFoundService;
    private final SysFileService fileService;
    
    @Operation(summary = "失物招领列表")
    @SaCheckLogin
    @GetMapping("/list")
    public R<Page<DormLostFound>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String itemName) {
        
        Page<DormLostFound> page = new Page<>(pageNum, pageSize);
        return R.ok(lostFoundService.getLostFoundPage(page, type, itemName));
    }
    
    /**
     * 🚀 发布招领/寻物信息 (带图片上传版)
     * [防刁民设计]：
     * 1. 自动注入 publishUserId，防止通过 Postman 冒充他人发帖。
     * 2. 分类存储：所有失物招领图片强制存入 /lost_found 子目录。
     */
    @Operation(summary = "发布招领/寻物信息")
    @SaCheckLogin
    @PostMapping(value = "/publish", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<String> publish(
            @RequestPart("info") DormLostFound info,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        
        // 🛡️ [安全性增强]：强制绑定当前登录用户
        info.setPublishUserId(StpUtil.getLoginIdAsLong());
        info.setStatus(0); // 默认为进行中
        
        // 🖼️ [图片存储逻辑]
        if (file != null && !file.isEmpty()) {
            // 调用系统通用文件服务，子路径设为 "lost_found"
            String imageUrl = fileService.uploadFile(file, "lost_found");
            info.setImageUrl(imageUrl);
        }
        
        lostFoundService.publish(info);
        return R.okMsg("信息已在全校看板发布");
    }
    
    @Operation(summary = "结案 (标记为已领回/已找到)")
    @SaCheckLogin
    @PostMapping("/complete/{id}")
    public R<String> complete(@PathVariable Long id) {
        lostFoundService.complete(id, StpUtil.getLoginIdAsLong());
        return R.okMsg("操作成功，感谢您的热心帮助！");
    }
    
    @Operation(summary = "删除信息")
    @SaCheckLogin
    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Long id) {
        lostFoundService.safeDelete(id, StpUtil.getLoginIdAsLong());
        return R.okMsg("删除成功");
    }
}