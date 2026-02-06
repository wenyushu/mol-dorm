package com.mol.server.task;

import cn.hutool.core.io.FileUtil;
import com.mol.common.core.file.IFileUrlProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 孤儿文件清理任务 (Pro Ultra: 解耦 & 防误删版)
 * <p>
 * 核心逻辑：
 * 1. 依赖倒置：通过 IFileUrlProvider 接口收集所有模块的“白名单”。
 * 2. 宽限期：只删除 24小时前 的文件，防止用户刚上传还没提交就被删了。
 * 3. 忽略大小写：防止 Windows/Linux 文件系统差异导致误判。
 * </p>
 *
 * @author mol
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileCleanupTask {
    
    // 读取配置文件中的上传根目录
    @Value("${mol.profile:D:/mol/upload}")
    private String basePath;
    
    /**
     * 核心解耦点：
     * Spring 会自动将所有实现了 IFileUrlProvider 接口的 Bean 注入到这个列表中。
     * 包括：
     * 1. mol-dorm-biz 里的 RepairOrderFileProvider (报修图)
     * 2. mol-server 里的 UserAvatarFileProvider (头像)
     * 3. 未来其他模块的 Provider...
     */
    private final List<IFileUrlProvider> fileProviders;
    
    /**
     * 定时任务：每天凌晨 03:30 执行
     */
    @Scheduled(cron = "0 30 3 * * ?")
    public void cleanOrphanFiles() {
        log.info(">>> [垃圾清理] 开始扫描孤儿文件...");
        long startTime = System.currentTimeMillis();
        
        // 1. 🛡️ 聚合白名单 (收集所有正在使用的文件名)
        Set<String> validFileNames = new HashSet<>();
        
        if (fileProviders != null) {
            for (IFileUrlProvider provider : fileProviders) {
                try {
                    Set<String> urls = provider.getUsedFileUrls();
                    if (urls != null && !urls.isEmpty()) {
                        validFileNames.addAll(urls);
                    }
                } catch (Exception e) {
                    log.error("获取文件白名单失败，Provider: {}", provider.getClass().getSimpleName(), e);
                    // 即使某个模块报错，也不中断整个清理流程，只是该模块的文件暂时不清理
                }
            }
        }
        
        // 添加系统预置图片到白名单 (防止误删默认头像等)
        validFileNames.add("default_avatar.png");
        validFileNames.add("default_cover.jpg");
        
        log.info("当前系统有效文件白名单总数: {}", validFileNames.size());
        
        // 2. 🛡️ 扫描磁盘
        File rootDir = new File(basePath);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            log.warn("文件上传根目录不存在或不可读: {}", basePath);
            return;
        }
        
        // 递归获取目录下所有文件 (使用 Hutool 工具)
        List<File> allFiles = FileUtil.loopFiles(rootDir);
        int deletedCount = 0;
        int skippedCount = 0;
        long releaseSpace = 0L;
        
        // 计算过期时间节点 (当前时间 - 24小时)
        // 🛡️ 防刁民核心：给用户留 24 小时操作时间，防止上传一半被删
        long expireTimeThreshold = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        
        // 3. 🛡️ 执行清理
        for (File file : allFiles) {
            // A. 跳过 "新文件" (创建/修改时间在24小时内)
            if (file.lastModified() > expireTimeThreshold) {
                skippedCount++;
                continue;
            }
            
            String fileName = file.getName();
            
            // B. 检查是否在白名单中 (忽略大小写比对)
            // 提示：Set.contains 是区分大小写的，为了稳健，我们这里用流式 filter
            boolean isUsed = validFileNames.stream()
                    .anyMatch(validName -> validName.equalsIgnoreCase(fileName));
            
            if (!isUsed) {
                // C. 既然既老旧又没被引用，那就是垃圾 -> 删除
                long size = file.length();
                boolean success = FileUtil.del(file);
                if (success) {
                    deletedCount++;
                    releaseSpace += size;
                    log.debug("已删除垃圾文件: {}", file.getAbsolutePath());
                } else {
                    log.warn("垃圾文件删除失败(可能被占用): {}", file.getAbsolutePath());
                }
            }
        }
        
        // 4. 清理空文件夹 (深度清理)
        try {
            FileUtil.cleanEmpty(rootDir);
        } catch (Exception e) {
            // 忽略文件夹清理异常
        }
        
        long duration = System.currentTimeMillis() - startTime;
        log.info("<<< [垃圾清理] 完成。耗时:{}ms, 扫描:{}个, 跳过新文件:{}个, 删除:{}个, 释放空间:{}",
                duration, allFiles.size(), skippedCount, deletedCount, FileUtil.readableFileSize(releaseSpace));
    }
}