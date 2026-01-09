package com.mol.dorm.biz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.mapper.DormBedMapper;
import com.mol.dorm.biz.service.DormBedService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 宿舍床位业务实现类
 */
@Slf4j
@Service
public class DormBedServiceImpl extends ServiceImpl<DormBedMapper, DormBed> implements DormBedService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserToBed(Long bedId, Long userId) {
        log.info("开始分配床位: bedId={}, userId={}", bedId, userId);
        
        // 1. 基础校验：检查床位是否存在、是否处于正常状态
        DormBed bed = this.getById(bedId);
        if (bed == null) {
            throw new ServiceException("目标床位不存在");
        }
        if (bed.getStatus() != 0) { // 假设 0 是正常状态
            throw new ServiceException("该床位当前不可用（可能维修中或已停用）");
        }
        if (bed.getOccupantId() != null) {
            throw new ServiceException("该床位已被占用，请刷新数据");
        }
        
        // 2. 执行 CAS 更新 (关键并发控制)
        // 使用自定义 Mapper 方法，确保原子性
        int rows = baseMapper.assignBed(bedId, userId);
        
        if (rows == 0) {
            // 如果影响行数为0，说明在查询后的一瞬间，床位被别人抢占了
            log.warn("床位分配并发冲突: bedId={}, userId={}", bedId, userId);
            throw new ServiceException("手慢了！该床位刚刚已被其他人占用，请重试");
        }
        
        log.info("床位分配成功");
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseBed(Long bedId) {
        // 释放床位比较简单，直接将 occupant_id 置空
        boolean update = this.update(Wrappers.<DormBed>lambdaUpdate()
                .set(DormBed::getOccupantId, null) // 设置为空
                .eq(DormBed::getId, bedId));
        
        if (!update) {
            throw new ServiceException("释放床位失败，请检查床位ID");
        }
    }
}