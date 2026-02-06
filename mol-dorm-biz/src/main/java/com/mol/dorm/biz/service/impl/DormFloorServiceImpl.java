package com.mol.dorm.biz.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.constant.DormConstants;
import com.mol.common.core.enums.DormStatusEnum;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.DormBuilding;
import com.mol.dorm.biz.entity.DormFloor;
import com.mol.dorm.biz.mapper.DormBedMapper;
import com.mol.dorm.biz.mapper.DormBuildingMapper;
import com.mol.dorm.biz.mapper.DormFloorMapper;
import com.mol.dorm.biz.service.DormFloorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 楼层管理服务实现类 - 严苛防御版
 * 🛡️ [补全说明]：
 * 1. 状态闭环：禁止对有人的楼层执行 40(装修) 或 0(停用) 操作。
 * 2. 深度穿透：通过 bedMapper 实时检索该楼层下所有房间的居住情况。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DormFloorServiceImpl extends ServiceImpl<DormFloorMapper, DormFloor> implements DormFloorService {
    
    private final DormBuildingMapper buildingMapper;
    private final DormBedMapper bedMapper;
    
    /**
     * 安全保存/修改楼层
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveFloorStrict(DormFloor floor) {
        DormBuilding building = buildingMapper.selectById(floor.getBuildingId());
        if (building == null) throw new ServiceException("录入失败：关联楼栋不存在");
        
        // [防刁民] 校验楼层高度是否超出楼栋定义
        if (floor.getFloorNum() > building.getFloorCount()) {
            throw new ServiceException(StrUtil.format("逻辑错误：楼栋 [{}] 仅有 {} 层，无法操作第 {} 层",
                    building.getBuildingName(), building.getFloorCount(), floor.getFloorNum()));
        }
        
        // [性别防火墙]
        if (building.getGenderLimit() != 3 && !Objects.equals(building.getGenderLimit(), floor.getGenderLimit())) {
            throw new ServiceException("性别隔离拦截：楼层性别必须与所属单性别楼栋保持一致！");
        }
        
        // [修改校验]
        if (floor.getId() != null) {
            DormFloor old = this.getById(floor.getId());
            // 如果改性别，必须先清空人
            if (!Objects.equals(old.getGenderLimit(), floor.getGenderLimit())) {
                checkFloorOccupancy(floor.getId(), "变更楼层性别");
            }
        } else {
            floor.setStatus(DormConstants.LC_NORMAL);
        }
        
        this.saveOrUpdate(floor);
    }
    
    /**
     * 物理熔断：整层状态切换
     * 🛡️ [补全逻辑]：封闭楼层前必须强制进行活人审计。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFloorStatus(Long floorId, Integer status) {
        DormFloor floor = this.getById(floorId);
        if (floor == null) throw new ServiceException("操作失败：楼层不存在");
        
        // 🛡️ [防刁民核心]：非正常状态切换校验
        // 如果状态不是 20 (正常)，即变为 40(装修) 或 0(停止)，必须确保该层没人
        if (!Objects.equals(status, DormConstants.LC_NORMAL)) {
            String statusName = DormStatusEnum.fromCode(status).getDesc();
            // 强制执行深度审计，有人则抛出异常回滚
            checkFloorOccupancy(floorId, "执行 [" + statusName + "] 操作");
        }
        
        floor.setStatus(status);
        this.updateById(floor);
        log.warn("🚨 [行政干预] 楼层 ID: {} 状态已强制切换为: {}", floorId, DormStatusEnum.fromCode(status).getDesc());
    }
    
    /**
     * 物理清理：安全删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFloorStrict(Long floorId) {
        // 🛡️ [强制审计]
        checkFloorOccupancy(floorId, "彻底删除楼层");
        
        this.removeById(floorId);
        log.info("🗑️ [资源回收] 楼层 ID: {} 已从系统中物理注销", floorId);
    }
    
    @Override
    public List<DormFloor> getByBuilding(Long buildingId) {
        return this.list(Wrappers.<DormFloor>lambdaQuery()
                .eq(DormFloor::getBuildingId, buildingId)
                .orderByAsc(DormFloor::getFloorNum));
    }
    
    // =================================================================================
    // 🛡️ 辅助方法：楼层深度审计引擎
    // =================================================================================
    
    /**
     * 深度审计该层是否有活人
     * [原理]：通过 Bed Mapper 穿透查询该 Floor 下所有关联床位的 occupant_id。
     */
    private void checkFloorOccupancy(Long floorId, String action) {
        // 🛡️ 严苛查询：统计该楼层下 occupant_id 不为空的记录数
        Long count = bedMapper.selectCount(Wrappers.<DormBed>lambdaQuery()
                .eq(DormBed::getFloorId, floorId) // 穿透到床位级检查
                .isNotNull(DormBed::getOccupantId));
        
        if (count > 0) {
            log.error("🛑 拦截违规操作：试图在有人居住（{}人）的情况下对楼层 ID {} 执行 {}", count, floorId, action);
            throw new ServiceException(StrUtil.format("安全拦截：[{}] 失败！该楼层目前仍有 {} 名人员未搬离，请先处理人员去向。",
                    action, count));
        }
    }
}