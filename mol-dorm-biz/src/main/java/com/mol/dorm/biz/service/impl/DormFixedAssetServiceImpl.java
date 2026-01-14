package com.mol.dorm.biz.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormFixedAsset;
import com.mol.dorm.biz.mapper.DormFixedAssetMapper;
import com.mol.dorm.biz.service.DormFixedAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DormFixedAssetServiceImpl extends ServiceImpl<DormFixedAssetMapper, DormFixedAsset> implements DormFixedAssetService {
    
    @Override
    public Page<DormFixedAsset> getAssetPage(Page<DormFixedAsset> page, Long roomId, String assetName, Integer category) {
        LambdaQueryWrapper<DormFixedAsset> wrapper = Wrappers.lambdaQuery();
        
        if (roomId != null) wrapper.eq(DormFixedAsset::getRoomId, roomId);
        if (StrUtil.isNotBlank(assetName)) wrapper.like(DormFixedAsset::getAssetName, assetName);
        if (category != null) wrapper.eq(DormFixedAsset::getCategory, category);
        
        wrapper.orderByDesc(DormFixedAsset::getCreateTime);
        return this.page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAsset(DormFixedAsset asset) {
        // 1. 必填校验
        if (asset.getRoomId() == null) throw new ServiceException("必须绑定房间");
        if (StrUtil.isBlank(asset.getAssetName())) throw new ServiceException("资产名称不能为空");
        
        // 2. 自动生成编号 (如果前端没填)
        // 格式: ASSET-时间戳
        if (StrUtil.isBlank(asset.getAssetCode())) {
            asset.setAssetCode("AST-" + IdUtil.getSnowflakeNextIdStr());
        }
        
        // 3. 查重
        Long count = this.baseMapper.selectCount(Wrappers.<DormFixedAsset>lambdaQuery()
                .eq(DormFixedAsset::getAssetCode, asset.getAssetCode()));
        if (count > 0) throw new ServiceException("资产编号已存在");
        
        // 4. 默认值
        if (asset.getStatus() == null) asset.setStatus(1); // 正常
        
        this.save(asset);
    }
}