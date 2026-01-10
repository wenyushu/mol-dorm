package com.mol.dorm.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormBuilding;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.mapper.DormBuildingMapper;
import com.mol.dorm.biz.mapper.DormRoomMapper;
import com.mol.dorm.biz.service.DormBuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

/**
 * 宿舍楼业务实现类
 * <p>
 * 负责处理宿舍楼的业务逻辑。
 * 虽然大部分 CRUD 功能由 MyBatis-Plus 自动实现，
 * 但我们在删除操作中增加了"安全校验"，防止误删导致的数据不一致。
 *
 * @author mol
 */
@Service
@RequiredArgsConstructor
public class DormBuildingServiceImpl extends ServiceImpl<DormBuildingMapper, DormBuilding> implements DormBuildingService {
    
    // 注入房间 Mapper，用于删除前的关联检查
    private final DormRoomMapper roomMapper;
    
    /**
     * 重写删除逻辑 (增强版)
     * 删除楼栋前，强制检查楼内是否还有未删除的房间。
     *
     * @param id 楼栋 ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        // 1. 安全校验：检查该楼栋下是否存在有效房间
        // 如果楼里还有房间，禁止删除，防止出现"幽灵房间" (关联的楼 ID 失效)
        Long count = roomMapper.selectCount(new LambdaQueryWrapper<DormRoom>()
                .eq(DormRoom::getBuildingId, id)
                .eq(DormRoom::getDelFlag, "0")); // 只统计未被逻辑删除的房间
        
        if (count > 0) {
            throw new ServiceException("该楼栋下仍有 " + count + " 个房间数据，禁止删除！请先清空或移除所有房间。");
        }
        
        // 2. 校验通过，调用父类默认实现进行逻辑删除
        return super.removeById(id);
    }
}