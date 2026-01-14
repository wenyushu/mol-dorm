package com.mol.dorm.biz.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.HolidayStay;
import com.mol.dorm.biz.mapper.HolidayStayMapper;
import com.mol.dorm.biz.service.HolidayStayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HolidayStayServiceImpl extends ServiceImpl<HolidayStayMapper, HolidayStay> implements HolidayStayService {
    
    // 状态常量
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_APPROVED = 1;
    private static final int STATUS_REJECTED = 2;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(HolidayStay stay) {
        // 1. 必填项校验 (防刁民)
        if (stay.getStartDate() == null || stay.getEndDate() == null) {
            throw new ServiceException("留校时间段不能为空");
        }
        if (stay.getEndDate().isBefore(stay.getStartDate())) {
            throw new ServiceException("结束日期不能早于开始日期");
        }
        if (StrUtil.isBlank(stay.getEmergencyName()) ||
                StrUtil.isBlank(stay.getEmergencyPhone()) ||
                StrUtil.isBlank(stay.getEmergencyRelation())) {
            throw new ServiceException("为了您的安全，紧急联系人信息必须填写完整");
        }
        
        // 2. 重复申请校验
        // 检查该学生是否已有 "待审批" 或 "已通过" 的申请，防止重复提交
        Long count = this.baseMapper.selectCount(Wrappers.<HolidayStay>lambdaQuery()
                .eq(HolidayStay::getStudentId, stay.getStudentId())
                .in(HolidayStay::getStatus, STATUS_PENDING, STATUS_APPROVED));
        
        if (count > 0) {
            throw new ServiceException("您已有正在处理或已通过的留校申请，请勿重复提交");
        }
        
        // 3. 初始化并保存
        stay.setStatus(STATUS_PENDING);
        this.save(stay);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, Boolean pass, String msg) {
        HolidayStay stay = this.getById(id);
        if (stay == null) throw new ServiceException("申请不存在");
        
        if (stay.getStatus() != STATUS_PENDING) {
            throw new ServiceException("该申请已被处理，无需重复操作");
        }
        
        if (pass) {
            stay.setStatus(STATUS_APPROVED);
            // 可以在这里联动其他逻辑，比如给门禁系统发白名单
        } else {
            stay.setStatus(STATUS_REJECTED);
        }
        
        stay.setAuditMsg(msg);
        this.updateById(stay);
    }
    
    @Override
    public Page<HolidayStay> getPage(Page<HolidayStay> page, Long userId, Integer status) {
        LambdaQueryWrapper<HolidayStay> wrapper = Wrappers.lambdaQuery();
        
        // 如果传了 userId，说明是学生查自己；不传则是管理员查所有
        if (userId != null) {
            wrapper.eq(HolidayStay::getStudentId, userId);
        }
        
        if (status != null) {
            wrapper.eq(HolidayStay::getStatus, status);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(HolidayStay::getCreateTime);
        
        return this.page(page, wrapper);
    }
}