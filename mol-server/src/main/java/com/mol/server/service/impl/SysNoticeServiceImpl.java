package com.mol.server.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.entity.SysNotice;
import com.mol.server.mapper.SysNoticeMapper;
import com.mol.server.service.SysNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 通知公告业务实现
 */
@Service
@RequiredArgsConstructor
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements SysNoticeService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(SysNotice notice) {
        if (StrUtil.isBlank(notice.getTitle())) {
            throw new ServiceException("标题不能为空");
        }
        if (StrUtil.isBlank(notice.getContent())) {
            throw new ServiceException("内容不能为空");
        }
        
        // 默认状态
        notice.setStatus("0"); // 发布
        if (notice.getType() == null) notice.setType(1); // 默认通知
        if (notice.getLevel() == null) notice.setLevel(0); // 默认普通
        
        this.save(notice);
    }
    
    @Override
    public void withdraw(Long id) {
        SysNotice notice = this.getById(id);
        if (notice == null) throw new ServiceException("公告不存在");
        
        notice.setStatus("1"); // 撤回
        this.updateById(notice);
    }
    
    @Override
    public Page<SysNotice> getNoticePage(Page<SysNotice> page, String title, Integer type, boolean onlyActive) {
        LambdaQueryWrapper<SysNotice> wrapper = Wrappers.lambdaQuery();
        
        // 动态条件
        wrapper.like(StrUtil.isNotBlank(title), SysNotice::getTitle, title)
                .eq(type != null, SysNotice::getType, type);
        
        // 如果是学生端展示，只查状态为 "0" (正常)的
        if (onlyActive) {
            wrapper.eq(SysNotice::getStatus, "0");
        }
        
        // 按创建时间倒序 (最新的在前面)
        wrapper.orderByDesc(SysNotice::getCreateTime);
        
        return this.page(page, wrapper);
    }
}