package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.DormFixedAsset;

/**
 * 固定资产服务接口
 * 🛡️ [防刁民设计]：
 * 资产状态直接联动房间可用性。严禁删除已报修但未结项的资产记录。
 */
public interface DormFixedAssetService extends IService<DormFixedAsset> {
    
    /**
     * 🟢 资产多条件分页查询
     * @param page 分页对象
     * @param roomId 房间ID
     * @param assetName 资产名称 (模糊查询)
     * @param category 资产分类
     * @return 分页结果
     */
    Page<DormFixedAsset> getAssetPage(Page<DormFixedAsset> page, Long roomId, String assetName, Integer category);
    
    /**
     * 安全更新资产状态 (基于 ID)
     * @param assetId 资产ID
     * @param status 20-正常, 50-维修中, 60-已损坏, 0-已报废
     */
    void updateAssetStatus(Long assetId, Integer status);
    
    /**
     * 基于资产条码更新状态 (用于扫码报修联动)
     * 🛡️ [防穿透]：强制校验 roomId，防止 A 房间的学生扫码修改 B 房间的资产状态
     */
    void updateAssetStatusByCode(Long roomId, String assetCode, Integer status);
    
    /**
     * 严苛入库审计
     * [逻辑]：强制条码(AssetCode)唯一性校验，初始化状态为 20。
     */
    void saveAssetStrict(DormFixedAsset asset);
    
    /**
     * 资产健康度审计 (联动熔断)
     * [逻辑]：当资产状态变更后，通知房间服务重新评估该房间是否还能住人。
     */
    void auditRoomAvailability(Long roomId);
}