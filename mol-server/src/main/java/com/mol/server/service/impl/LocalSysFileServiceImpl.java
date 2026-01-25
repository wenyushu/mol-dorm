package com.mol.server.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.service.SysFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;

/**
 * 本地文件存储实现
 */
@Slf4j
@Service
public class LocalSysFileServiceImpl implements SysFileService {
    
    @Value("${mol.profile}")
    private String localFilePath; // D:/mol/uploadPath
    
    @Override
    public String uploadFile(MultipartFile file, String subPath) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上传文件不能为空");
        }
        
        // 1. 获取原文件名和后缀
        String originalFilename = file.getOriginalFilename();
        String extName = FileUtil.extName(originalFilename);
        
        // 🛡️ 防刁民：校验文件类型 (只允许图片)
        if (!StrUtil.equalsAnyIgnoreCase(extName, "jpg", "jpeg", "png", "gif", "bmp")) {
            throw new ServiceException("仅支持 JPG, PNG, GIF 格式的图片");
        }
        
        // 2. 生成新文件名 (UUID 防止重名)
        // 结果如：avatar/20260124/a1b2c3d4.png
        String fileName = IdUtil.fastSimpleUUID() + "." + extName;
        String datePath = DateUtil.today(); // 2026-01-24
        
        // 最终相对路径：/avatar/2026-01-24/uuid.png
        String relativePath = "/" + subPath + "/" + datePath + "/" + fileName;
        
        // 最终绝对路径：D:/mol/uploadPath/avatar/2026-01-24/uuid.png
        File dest = new File(localFilePath + relativePath);
        
        // 3. 创建父目录并保存
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new ServiceException("文件上传失败: " + e.getMessage());
        }
        
        // 4. 生成访问 URL
        // 动态获取当前域名端口 (如 http://localhost:9090)
        String domain = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        
        // 拼接映射路径 /profile/...
        return domain + "/profile" + relativePath;
    }
}