package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.DormFixedAsset;

public interface DormFixedAssetService extends IService<DormFixedAsset> {
    
    /**
     * 分页查询资产列表
     * @param page 分页对象
     * @param roomId 房间ID (可选)
     * @param assetName 资产名称 (可选)
     * @param category 分类 (可选)
     */
    Page<DormFixedAsset> getAssetPage(Page<DormFixedAsset> page, Long roomId, String assetName, Integer category);
    
    /**
     * 登记新资产
     */
    void addAsset(DormFixedAsset asset);
}