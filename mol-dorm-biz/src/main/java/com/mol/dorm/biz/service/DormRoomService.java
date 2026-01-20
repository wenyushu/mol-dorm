package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.vo.DormRoomVO;

/**
 * 宿舍房间服务接口
 */
public interface DormRoomService extends IService<DormRoom> {
    
    // --- 单个管理 ---
    
    /** 新增房间 (自动生成床位) */
    void addRoom(DormRoom room);
    
    /** 修改房间 (带封寝校验、扩缩容) */
    void updateRoom(DormRoom room);
    
    /** 删除房间 (带防孤儿校验) */
    void deleteRoom(Long roomId);
    
    // --- 楼层批量操作 ---
    
    /** 批量停用楼层 */
    void disableFloor(Long buildingId, Integer floorNo);
    
    /** 批量删除楼层 */
    void deleteFloor(Long buildingId, Integer floorNo);
    
    // --- 查询 ---
    
    /** 获取详情 (含床位和人名) */
    DormRoomVO getRoomDetail(Long roomId);
    
    /** 分页查询 (VO增强版) */
    Page<DormRoomVO> getRoomVoPage(Page<DormRoom> page, Long buildingId);
    
    // --- 应急处理 ---
    
    /** 紧急转移人员 (从源房间挪到目标房间) */
    void emergencyTransfer(Long sourceRoomId, Long targetRoomId);
    
    /** 紧急腾退/封寝 (强制清空并封锁) */
    void evacuateRoom(Long roomId, String reason);
}