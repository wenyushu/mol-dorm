package com.mol.server.service;
import org.springframework.web.multipart.MultipartFile;

public interface SysFileService {
    /**
     * 上传文件
     * @param file 前端传来的文件
     * @param subPath 子路径 (如 "avatar", "document")
     * @return 文件的访问 URL
     */
    String uploadFile(MultipartFile file, String subPath);
}