package com.mol.dorm.biz.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormBuilding;
import com.mol.dorm.biz.mapper.DormBuildingMapper;
import com.mol.server.event.CampusDeleteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 监听校区删除事件
 * 负责检查该校区下是否有宿舍楼
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DormCampusDeleteListener {
    
    private final DormBuildingMapper buildingMapper;
    
    @EventListener
    public void handleCampusDelete(CampusDeleteEvent event) {
        Long campusId = event.getCampusId();
        log.info("收到校区删除事件，开始校验... CampusID: {}", campusId);
        
        // 检查是否有楼栋
        Long count = buildingMapper.selectCount(new LambdaQueryWrapper<DormBuilding>()
                .eq(DormBuilding::getCampusId, campusId));
        
        if (count > 0) {
            log.warn("校区删除被拦截：存在 {} 栋楼宇", count);
            // 🚨 直接抛出异常！这会中断 Controller 的执行流程，并返回错误信息给前端
            throw new ServiceException("无法删除：该校区下仍有 " + count + " 栋楼宇，请先清理楼宇数据！");
        }
    }
}