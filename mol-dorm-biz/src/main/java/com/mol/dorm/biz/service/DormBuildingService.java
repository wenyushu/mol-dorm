package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.bto.BuildingInitDto;
import com.mol.dorm.biz.entity.DormBuilding;

/**
 * 宿舍楼栋服务接口
 */
public interface DormBuildingService extends IService<DormBuilding> {
    
    /**
     * 新增楼栋 (带逻辑外键校验)
     * @param building 楼栋实体
     * @return 是否成功
     */
    boolean saveBuilding(DormBuilding building);
    
    /**
     * 修改楼栋 (带封禁安全校验)
     * @param building 楼栋实体
     * @return 是否成功
     */
    boolean updateBuilding(DormBuilding building);
    
    /**
     * 删除楼栋 (级联删除房间与床位)
     * @param buildingId 楼栋 ID
     */
    void deleteBuilding(Long buildingId);
    
    /**
     * 一键初始化楼栋 (生成房间和床位)
     * @param dto 初始化参数
     */
    void initBuilding(BuildingInitDto dto);
    
    /**
     * 根据校区 ID 统计楼栋数量 (用于删除校区前的校验)
     * @param campusId 校区ID
     * @return 该校区下的楼栋数
     */
    long countByCampusId(Long campusId);
}