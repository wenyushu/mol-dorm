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
import java.time.LocalDateTime;

/**
 * 通知公告业务实现 (mol-server)
 */
@Service
@RequiredArgsConstructor
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements SysNoticeService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(SysNotice notice) {
        // 🛡️ [防刁民逻辑]：基础字段强制校验
        if (StrUtil.isBlank(notice.getTitle())) throw new ServiceException("发布失败：标题不能为空");
        if (StrUtil.isBlank(notice.getContent())) throw new ServiceException("发布失败：内容不能为空");
        
        // 🛡️ [状态锁定]
        notice.setStatus("0");
        notice.setReleaseTime(LocalDateTime.now()); // 强制记录当前发布时间
        if (notice.getType() == null) notice.setType(1);
        if (notice.getLevel() == null) notice.setLevel(0);
        
        this.save(notice);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(Long id) {
        SysNotice notice = this.getById(id);
        if (notice == null) throw new ServiceException("操作失败：公告已不存在");
        
        // 🛡️ [逻辑删除/撤回]
        notice.setStatus("1");
        this.updateById(notice);
    }
    
    @Override
    public Page<SysNotice> getNoticePage(Page<SysNotice> page, String title, Integer type, boolean onlyActive) {
        LambdaQueryWrapper<SysNotice> wrapper = Wrappers.lambdaQuery();
        
        wrapper.like(StrUtil.isNotBlank(title), SysNotice::getTitle, title)
                .eq(type != null, SysNotice::getType, type);
        
        // 🛡️ [多态查询]：学生端(onlyActive=true)严禁查到撤回的内容
        if (onlyActive) {
            wrapper.eq(SysNotice::getStatus, "0");
        }
        
        // 💡 排序逻辑：重要(level)优先，时间(releaseTime)次之
        wrapper.orderByDesc(SysNotice::getLevel)
                .orderByDesc(SysNotice::getReleaseTime);
        
        return this.page(page, wrapper);
    }
}