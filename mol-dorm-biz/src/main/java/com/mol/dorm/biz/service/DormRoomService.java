package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.vo.DormRoomVO;

/**
 * 房间业务接口
 * <p>
 * 包含：房间增删改查、楼层级批量操作、应急事务处理。
 * </p>
 *
 * @author mol
 */
public interface DormRoomService extends IService<DormRoom> {
    
    // ================== 单个房间操作 ==================
    
    /**
     * 新增房间 (自动生成床位)
     * @param room 房间信息
     */
    void addRoom(DormRoom room);
    
    /**
     * 修改房间 (带安全校验 & 扩缩容逻辑)
     * <p>
     * 1. 若修改容量：自动触发床位追加或删除。
     * 2. 若修改状态为封寝：检查是否有人居住。
     * </p>
     * @param room 房间信息
     */
    void updateRoom(DormRoom room);
    
    /**
     * 删除房间 (带安全校验)
     * <p>
     * 若房间内有人居住，禁止删除。
     * </p>
     * @param roomId 房间 ID
     */
    void deleteRoom(Long roomId);
    
    // ================== 楼层批量操作 ==================
    
    /**
     * 停用整层楼
     * <p>
     * 将该楼层所有房间状态置为 0 (不可用)。
     * 前置条件：该楼层所有房间均无人居住。
     * </p>
     * @param buildingId 楼栋 ID
     * @param floor 楼层号
     */
    void disableFloor(Long buildingId, Integer floor);
    
    /**
     * 删除整层楼
     * <p>
     * 物理删除该楼层所有房间及关联床位。
     * 前置条件：该楼层所有房间均无人居住。
     * </p>
     * @param buildingId 楼栋 ID
     * @param floor 楼层号
     */
    void deleteFloor(Long buildingId, Integer floor);
    
    // ================== 查询业务 (VO) ==================
    
    /**
     * 获取房间详情
     * @param roomId 房间 ID
     * @return 包含床位列表和居住人姓名的 VO 对象
     */
    DormRoomVO getRoomDetail(Long roomId);
    
    /**
     * 分页查询房间列表
     * <p>
     * 解决 N+1 问题，批量装配人员信息。
     * </p>
     * @param page 分页参数
     * @param buildingId 楼栋 ID
     * @return VO 分页对象
     */
    Page<DormRoomVO> getRoomVoPage(Page<DormRoom> page, Long buildingId);
    
    // ================== 应急处理 ==================
    
    /**
     * 紧急转移人员
     * <p>
     * 将源房间的所有人员移动到目标房间，并封锁源房间。
     * </p>
     * @param sourceRoomId 源房间 ID
     * @param targetRoomId 目标房间 ID
     */
    void emergencyTransfer(Long sourceRoomId, Long targetRoomId);
    
    /**
     * 紧急腾退/封寝
     * <p>
     * 强制清空房间内所有人员，并将房间状态设为不可用。
     * </p>
     * @param roomId 房间 ID
     * @param reason 原因
     */
    void evacuateRoom(Long roomId, String reason);
}