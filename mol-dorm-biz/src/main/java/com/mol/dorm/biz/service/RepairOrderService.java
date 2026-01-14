package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.RepairOrder;

public interface RepairOrderService extends IService<RepairOrder> {
    
    /**
     * 学生提交报修
     */
    void submit(Long studentId, Long roomId, String desc, String images);
    
    /**
     * 宿管/维修工 接单或派单
     */
    void assign(Long orderId, Long repairmanId);
    
    /**
     * 维修工完成维修
     */
    void complete(Long orderId, String remark);
    
    /**
     * 学生评价
     */
    void rate(Long orderId, Integer rating, String comment);
    
    /**
     * 分页查询 (根据角色自动过滤)
     */
    Page<RepairOrder> getPage(Page<RepairOrder> page, RepairOrder query, Long currentUserId, String userRole);
}