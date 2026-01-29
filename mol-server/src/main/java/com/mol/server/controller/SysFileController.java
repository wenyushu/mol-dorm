package com.mol.server.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.mol.common.core.exception.ServiceException;
import com.mol.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;

@Slf4j
@Tag(name = "文件管理")
@RestController
@RequestMapping("/system/file")
public class SysFileController {
    
    @Value("${mol.profile:D:/mol/upload}")
    private String basePath;
    
    // 允许的扩展名白名单 (防 Webshell)
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "pdf"};
    
    @Operation(summary = "通用文件上传")
    @SaCheckLogin // 必须登录才能传
    @PostMapping("/upload")
    public R<String> upload(@RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            return R.fail("上传文件不能为空");
        }
        
        // 1. 校验文件类型 (简单后缀校验)
        String originalFilename = file.getOriginalFilename();
        String ext = FileUtil.extName(originalFilename);
        if (!isAllowedExtension(ext)) {
            throw new ServiceException("不支持的文件类型: " + ext);
        }
        
        // 2. 生成新文件名 (UUID + 日期分类)
        // 结构: /data/mol-dorm/upload/2026/01/28/uuid.jpg
        String datePath = DateUtil.format(new Date(), "yyyy/MM/dd");
        String fileName = IdUtil.fastSimpleUUID() + "." + ext;
        String absolutePath = basePath + File.separator + datePath + File.separator + fileName;
        
        // 3. 执行保存
        try {
            File dest = new File(absolutePath);
            FileUtil.touch(dest); // 自动创建父目录
            file.transferTo(dest);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return R.fail("文件上传失败: " + e.getMessage());
        }
        
        // 4. 返回可访问的 Web 路径 (供前端保存到 DB)
        // 格式: /profile/2026/01/28/abc.jpg
        String url = "/profile/" + datePath + "/" + fileName;
        return R.ok(url);
    }
    
    private boolean isAllowedExtension(String extension) {
        for (String str : ALLOWED_EXTENSIONS) {
            if (str.equalsIgnoreCase(extension)) return true;
        }
        return false;
    }
}