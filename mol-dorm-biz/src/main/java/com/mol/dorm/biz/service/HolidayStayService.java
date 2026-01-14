package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.HolidayStay;

public interface HolidayStayService extends IService<HolidayStay> {
    
    /**
     * 提交留校申请
     * @param stay 包含时间、原因、联系人信息
     */
    void submit(HolidayStay stay);
    
    /**
     * 审批申请 (辅导员/管理员)
     * @param id 申请 ID
     * @param pass 是否通过
     * @param msg 审批意见
     */
    void audit(Long id, Boolean pass, String msg);
    
    /**
     * 分页查询
     */
    Page<HolidayStay> getPage(Page<HolidayStay> page, Long userId, Integer status);
}