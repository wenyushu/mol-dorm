package com.mol.dorm.biz.service;

import com.mol.dorm.biz.vo.DashboardVO;

/**
 * 数据大屏业务接口
 */
public interface DashboardService {
    
    /**
     * 获取大屏全量数据
     * @return 聚合统计信息
     */
    DashboardVO getBigScreenData();
}