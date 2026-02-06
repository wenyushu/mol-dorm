package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.DormLostFound;

/**
 * 失物招领业务接口
 * 🛡️ [防刁民设计]：
 * 1. 状态流转审计：确保只有发布人或管理员能结案。
 * 2. 物理隔离：区分“寻找失物”与“拾获招领”的逻辑展示。
 */
public interface DormLostFoundService extends IService<DormLostFound> {
    
    /**
     * 分页查询失物招领信息
     */
    Page<DormLostFound> getLostFoundPage(Page<DormLostFound> page, Integer type, String itemName);
    
    /**
     * 发布信息 (含自动脱敏与归属锁定)
     */
    void publish(DormLostFound info);
    
    /**
     * 结案 (标记为已寻回/已领回)
     */
    void complete(Long id, Long currentUserId);
    
    /**
     * 安全删除
     */
    void safeDelete(Long id, Long currentUserId);
}