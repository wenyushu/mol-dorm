package com.mol.dorm.biz.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormFixedAsset;
import com.mol.dorm.biz.mapper.DormFixedAssetMapper;
import com.mol.dorm.biz.service.DormFixedAssetService;
import com.mol.dorm.biz.service.DormRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DormFixedAssetServiceImpl extends ServiceImpl<DormFixedAssetMapper, DormFixedAsset> implements DormFixedAssetService {
    
    private final DormFixedAssetMapper assetMapper;
    
    @Lazy // 防止循环依赖：Asset 调 Room，Room 有时也调 Asset
    private final DormRoomService roomService;
    
    // 查询
    @Override
    public Page<DormFixedAsset> getAssetPage(Page<DormFixedAsset> page, Long roomId, String assetName, Integer category) {
        // 🛡️ [防刁民设计]：使用 LambdaQuery 确保字段名安全，避免手写 SQL 注入
        return this.page(page, Wrappers.<DormFixedAsset>lambdaQuery()
                // 只有当参数不为空时才拼接条件
                .eq(roomId != null, DormFixedAsset::getRoomId, roomId)
                .eq(category != null, DormFixedAsset::getCategory, category)
                .like(StrUtil.isNotBlank(assetName), DormFixedAsset::getAssetName, assetName)
                // 按 ID 降序，保证新登记的资产排在前面
                .orderByDesc(DormFixedAsset::getId));
    }
    
    /**
     * 1. 基于 ID 更新状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAssetStatus(Long assetId, Integer status) {
        DormFixedAsset asset = this.getById(assetId);
        if (asset == null) throw new ServiceException("资产档案不存在");
        
        asset.setStatus(status);
        this.updateById(asset);
        
        // 变更后立即触发房间体检
        this.auditRoomAvailability(asset.getRoomId());
    }
    
    /**
     * 2. 基于条码更新状态 (扫码报修核心)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAssetStatusByCode(Long roomId, String assetCode, Integer status) {
        // 使用 Mapper 中的原子 SQL，带 roomId 校验，防止越权修改
        int rows = assetMapper.updateAssetStatus(roomId, assetCode, status);
        
        if (rows > 0) {
            // 变更后立即触发房间体检
            this.auditRoomAvailability(roomId);
        } else {
            log.warn("⚠️ 状态更新未命中：房间 {} 下无条码为 {} 的资产", roomId, assetCode);
        }
    }
    
    /**
     * 3. 严苛入库审计
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAssetStrict(DormFixedAsset asset) {
        // A. 唯一性校验 (防刁民录入重复条码)
        Long existingId = assetMapper.checkCodeUnique(asset.getAssetCode());
        if (existingId != null && !existingId.equals(asset.getId())) {
            throw new ServiceException("录入拦截：资产条码 [" + asset.getAssetCode() + "] 已存在，请勿重复创建！");
        }
        
        // B. 默认状态初始化
        if (asset.getId() == null) {
            asset.setStatus(20); // 正常
        }
        
        this.saveOrUpdate(asset);
    }
    
    /**
     * 4. 资产变动审计 (熔断触发器)
     */
    @Override
    public void auditRoomAvailability(Long roomId) {
        if (roomId == null) return;
        // 核心联动：直接调用我们之前写在 DormRoomService 里的体检引擎
        // 引擎会自动根据资产表里的 status 统计，决定房间是 20(正常) 还是 50(维修)
        roomService.evaluateRoomSafety(roomId);
    }
}