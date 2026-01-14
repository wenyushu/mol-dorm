package com.mol.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.server.entity.SysNotice;

/**
 * 通知公告业务接口
 */
public interface SysNoticeService extends IService<SysNotice> {
    
    /**
     * 发布公告
     */
    void publish(SysNotice notice);
    
    /**
     * 撤回公告
     */
    void withdraw(Long id);
    
    /**
     * 分页查询 (前台/后台通用)
     * @param page 分页对象
     * @param title 标题关键词 (可选)
     * @param type 类型 (可选)
     * @param onlyActive 是否只查已发布的 (学生端传true)
     */
    Page<SysNotice> getNoticePage(Page<SysNotice> page, String title, Integer type, boolean onlyActive);
}