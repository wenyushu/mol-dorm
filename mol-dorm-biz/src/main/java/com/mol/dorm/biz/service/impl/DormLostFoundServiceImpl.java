package com.mol.dorm.biz.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormLostFound;
import com.mol.dorm.biz.mapper.DormLostFoundMapper;
import com.mol.dorm.biz.service.DormLostFoundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 失物招领业务实现类
 * 🛡️ [防刁民设计]：
 * 1. 物理归属锁定：发布时强制注入当前 Token 对应的 UserId，防止前端篡改发布者。
 * 2. 权限双重校验：结案与删除操作通过 checkPermission 强行比对 Ownership 或 Admin 身份。
 * 3. 状态快照：查询列表默认只显示 status=0 (进行中) 的信息，确保看板时效性。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DormLostFoundServiceImpl extends ServiceImpl<DormLostFoundMapper, DormLostFound> implements DormLostFoundService {
    
    /**
     * 🔍 1. 分页查询 (看板逻辑)
     */
    @Override
    public Page<DormLostFound> getLostFoundPage(Page<DormLostFound> page, Integer type, String itemName) {
        return this.page(page, Wrappers.<DormLostFound>lambdaQuery()
                .eq(type != null, DormLostFound::getType, type)
                .like(StrUtil.isNotBlank(itemName), DormLostFound::getItemName, itemName)
                .eq(DormLostFound::getStatus, 0) // 🛡️ 只看“寻找中”或“待领取”的，结案后的默认隐藏
                .orderByDesc(DormLostFound::getCreateTime));
    }
    
    /**
     * 📢 2. 发布信息 (带归属强制锁定)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(DormLostFound info) {
        // 🛡️ [防刁民逻辑 1]：内容审计
        if (StrUtil.isBlank(info.getItemName()) || StrUtil.isBlank(info.getContactPhone())) {
            throw new ServiceException("发布失败：物品名称和联系方式是必填项，防止失主联系不上");
        }
        
        // 🛡️ [防刁民逻辑 2]：强制锁定归属
        // 哪怕前端传了 publishUserId 也要直接覆盖，防止冒充他人
        info.setPublishUserId(StpUtil.getLoginIdAsLong());
        info.setStatus(0); // 强制初始状态为进行中
        
        if (!this.save(info)) {
            throw new ServiceException("系统繁忙，发布失败");
        }
        log.info("📢 失物招领发布：用户 [{}] 发布了物品 [{}], 图片路径: [{}]",
                info.getPublishUserId(), info.getItemName(), info.getImageUrl());
    }
    
    /**
     * ✅ 3. 结案 (标记为已找回)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id, Long currentUserId) {
        DormLostFound item = this.getById(id);
        if (item == null) throw new ServiceException("该信息已失效或不存在");
        
        // 🛡️ [权限审计]
        checkPermission(item, currentUserId);
        
        // 检查是否已经结案
        if (item.getStatus() == 1) {
            throw new ServiceException("操作重复：该信息早已结案");
        }
        
        item.setStatus(1); // 标记已完成
        this.updateById(item);
        log.info("✅ 失物招领结案：物品 [{}] 已被认领/找回，操作人: [{}]", item.getItemName(), currentUserId);
    }
    
    /**
     * 🗑️ 4. 安全删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void safeDelete(Long id, Long currentUserId) {
        DormLostFound item = this.getById(id);
        if (item == null) return;
        
        // 🛡️ [权限审计]
        checkPermission(item, currentUserId);
        
        this.removeById(id);
        log.warn("🗑️ 失物招领删除：用户 [{}] 删除了条目 [{}]", currentUserId, item.getItemName());
    }
    
    /**
     * 🛡️ 5. 私有权限防火墙
     * 逻辑：只有信息的“发布者本人”或者是“管理员（宿管/系统管理员）”才能进行敏感操作。
     */
    private void checkPermission(DormLostFound item, Long currentUserId) {
        // A. 本人校验
        boolean isOwner = item.getPublishUserId().equals(currentUserId);
        
        // B. 管理员身份校验 (包含超级管理员和宿管角色)
        boolean isAdmin = StpUtil.hasRole(RoleConstants.SUPER_ADMIN) || StpUtil.hasRole(RoleConstants.DORM_MANAGER);
        
        if (!isOwner && !isAdmin) {
            log.warn("🚨 非法越权尝试：用户 [{}] 尝试修改/删除用户 [{}] 的信息", currentUserId, item.getPublishUserId());
            throw new ServiceException("权限不足：只有发布者本人或宿管老师有权操作");
        }
    }
}