package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.vo.DormRoomVo;

public interface DormRoomService extends IService<DormRoom> {
    
    // 新增房间
    void addRoom(DormRoom room);
    
    // 修改房间 (含扩容/缩容)
    void updateRoom(DormRoom room);
    
    // 删除房间
    void deleteRoom(Long roomId);
    
    // 获取详情 (含居住人)
    DormRoomVo getRoomDetail(Long roomId);
    
    // 紧急转移
    void emergencyTransfer(Long sourceRoomId, Long targetRoomId);
}