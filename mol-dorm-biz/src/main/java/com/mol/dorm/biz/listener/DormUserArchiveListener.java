package com.mol.dorm.biz.listener;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mol.common.core.event.UserArchiveEvent;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.mapper.DormBedMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 监听用户归档事件，自动释放床位
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DormUserArchiveListener {
    
    private final DormBedMapper bedMapper;
    
    /**
     * 监听到用户归档事件后，执行逻辑
     */
    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void handleUserArchive(UserArchiveEvent event) {
        Long userId = event.getUserId();
        Integer type = event.getArchiveType();
        
        log.info("监听到用户归档事件: userId={}, type={}, 正在检查并释放床位...", userId, type);
        
        // 强制释放该用户占用的床位
        // Update dorm_bed set status=0, occupant_id=null where occupant_id = {userId}
        int rows = bedMapper.update(null, new LambdaUpdateWrapper<DormBed>()
                .set(DormBed::getStatus, 0) // 变为空闲
                .set(DormBed::getOccupantId, null) // 清除占用人
                .set(DormBed::getOccupantType, null)
                .eq(DormBed::getOccupantId, userId));
        
        if (rows > 0) {
            log.info("成功释放归档用户的床位资源，释放数量: {}", rows);
        } else {
            log.info("该归档用户未占用任何床位，无需释放。");
        }
    }
}