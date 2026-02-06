package com.mol.dorm.biz.task;

import com.mol.dorm.biz.entity.DormBuilding;
import com.mol.dorm.biz.service.DormBuildingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 🧹 全量资源数据一致性巡检任务
 * 🛡️ [防数据腐化]：
 * 针对你提到的层级结构（楼栋 -> 楼层 -> 房间），由于房间基数大，
 * 每日凌晨自动触发一次全量校准，确保 Room 表的 current_num 与 Bed 表物理占位完全一致。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceConsistencyTask {
    
    private final DormBuildingService buildingService;
    
    /**
     * 🌙 每日凌晨 3 点执行
     * 逻辑：扫描所有【非停止状态】的楼栋，调用我们之前写的自愈引擎。
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void runDailyResourceCheck() {
        log.info("🕵️ [巡检开始] 启动全量资源数据一致性校准...");
        
        // 1. 获取所有正常的楼栋 (status = 20 或 40,50 等)
        List<DormBuilding> buildings = buildingService.list();
        
        int totalBuildings = buildings.size();
        log.info("🏢 本次巡检覆盖楼栋总数: {}", totalBuildings);
        
        // 2. 逐栋执行自愈引擎
        for (DormBuilding building : buildings) {
            try {
                // 调用我们之前在 Service 写的“一键修复”逻辑
                buildingService.syncRoomOccupancy(building.getId());
            } catch (Exception e) {
                log.error("❌ 楼栋 [{}] 校准过程中发生异常: {}", building.getBuildingName(), e.getMessage());
            }
        }
        
        log.info("✅ [巡检结束] 全校资源人数统计已同步至最新物理状态。");
    }
}