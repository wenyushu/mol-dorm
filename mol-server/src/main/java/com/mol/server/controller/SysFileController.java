package com.mol.server.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.mol.common.core.util.R;
import com.mol.server.service.SysFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "通用服务-文件上传", description = "头像、文档等文件上传")
@RestController
@RequestMapping("/common/file")
@RequiredArgsConstructor
public class SysFileController {
    
    private final SysFileService fileService;
    
    @Operation(summary = "上传头像", description = "上传成功后返回图片的访问 URL")
    @SaCheckLogin // 只要登录就能上传
    @PostMapping("/upload/avatar")
    public R<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        
        // 调用 Service，指定子目录为 "avatar"
        String fileUrl = fileService.uploadFile(file, "avatar");
        
        // 返回给前端
        Map<String, String> map = new HashMap<>();
        map.put("url", fileUrl); // 图片完整链接
        map.put("name", file.getOriginalFilename()); // 原文件名
        
        return R.ok(map);
    }
}