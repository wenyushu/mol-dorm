package com.mol.dorm.biz.service;

import com.mol.dorm.biz.vo.DashboardVO;
import com.mol.dorm.biz.vo.DormRoomVO;
import com.mol.dorm.biz.vo.MyRoomVO; // 🟢 引入你刚才定义的聚合模型
import java.util.List;
import java.util.Map;

/**
 * 宿管驾驶舱业务接口
 * 🛡️ [下钻体系]：校区 -> 楼层 -> 房间矩阵 -> 财务/人员原子详情
 */
public interface DashboardService {
    
    /**
     * 获取大屏首页聚合看板数据
     */
    DashboardVO getBigScreenData();
    
    /**
     * 【学生端核心】获取“我的房间”全量动态看板
     * 🛡️ [实现点]：聚合了 MyRoomVO 中要求的水电余额、账单快照和最新公告。
     * @param userId 当前登录学生ID
     */
    MyRoomVO getStudentRoomDashboard(Long userId);
    
    /**
     * 【下钻 L1】获取校区及楼栋树
     */
    List<Map<String, Object>> getCampusStructure();
    
    /**
     * 【下钻 L2】获取指定楼栋下的房态概览
     */
    List<Map<String, Object>> getBuildingFloorStats(Long buildingId);
    
    /**
     * 【下钻 L3】获取指定楼层下的房间矩阵 (含房态色块)
     */
    List<DormRoomVO> getFloorRoomMatrix(Long buildingId, Integer floorNo);
    
    /**
     * 【下钻 L4】获取指定房间的原子详情
     */
    DormRoomVO getRoomDetail(Long roomId);
    
    /**
     * [财务预警] 获取待断电提醒列表 (针对宿管)
     * 逻辑：筛选当前管理楼栋下，余额 < 0 且状态为“正常”的房间，提示宿管手动或自动干预。
     */
    List<Map<String, Object>> getWalletArrearsAlerts(Long buildingId);
    
    /**
     * [能耗审计] 获取本月异常用电排行
     * 逻辑：辅助宿管发现“违规电器”或“漏电异常”。
     */
    List<Map<String, Object>> getEnergyAnomalyRank(Long buildingId);
    
    /**
     * 获取驾驶舱今日动态提醒
     * 包含：今日新增报修、今日待审核申请、异常断电提醒等。
     */
    Map<String, Object> getTodayAlerts();
}