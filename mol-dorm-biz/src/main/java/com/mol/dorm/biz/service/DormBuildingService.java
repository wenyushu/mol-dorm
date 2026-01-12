package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.bto.BuildingInitDto;
import com.mol.dorm.biz.entity.DormBuilding;

/**
 * 楼栋业务接口
 * <p>
 * 提供楼栋的生命周期管理及快捷初始化功能。
 * 所有删除/停用操作均包含“居住人员检测”安全逻辑。
 * </p>
 *
 * @author mol
 */
public interface DormBuildingService extends IService<DormBuilding> {
    
    /**
     * 新增楼栋
     * @param building 楼栋信息
     * @return 是否成功
     */
    boolean saveBuilding(DormBuilding building);
    
    /**
     * 修改楼栋信息 (带安全校验)
     * <p>
     * 如果尝试封禁楼栋 (status=0)，系统会检查楼内是否仍有人居住。
     * 若有人，则抛出异常，禁止封禁。
     * </p>
     * @param building 修改后的楼栋信息
     * @return 是否成功
     */
    boolean updateBuilding(DormBuilding building);
    
    /**
     * 删除楼栋 (级联删除 + 安全校验)
     * <p>
     * 1. 检查楼内是否有人居住，有则报错。
     * 2. 级联删除该楼下的所有房间。
     * 3. 级联删除该楼下的所有床位。
     * </p>
     * @param buildingId 楼栋 ID
     */
    void deleteBuilding(Long buildingId);
    
    /**
     * 一键初始化楼栋
     * <p>
     * 自动完成：建楼 -> 批量建房 -> 批量建床。
     * 极大降低初始化工作量。
     * </p>
     * @param initDto 初始化参数 (层数、每层房间数、容量等)
     */
    void initBuilding(BuildingInitDto initDto);
}