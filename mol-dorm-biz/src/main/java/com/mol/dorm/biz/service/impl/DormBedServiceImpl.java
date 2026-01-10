package com.mol.dorm.biz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.mapper.DormBedMapper;
import com.mol.dorm.biz.mapper.DormRoomMapper;
import com.mol.dorm.biz.service.DormBedService;
import com.mol.sys.biz.mapper.SysOrdinaryUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DormBedServiceImpl extends ServiceImpl<DormBedMapper, DormBed> implements DormBedService {
    
    private final SysOrdinaryUserMapper userMapper; // 用于查用户性别
    private final DormRoomMapper roomMapper;        // 用于查房间的性别和更新人数
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserToBed(Long bedId, Long userId) {
        // 1. 基础校验
        DormBed bed = this.getById(bedId);
        if (bed == null) throw new ServiceException("床位不存在");
        if (bed.getOccupantId() != null) throw new ServiceException("该床位已被占用");
        
        // 2. 获取房间的信息 & 用户信息
        DormRoom room = roomMapper.selectById(bed.getRoomId());
        if (room == null) throw new ServiceException("关联房间不存在");
        
        SysOrdinaryUser user = userMapper.selectById(userId);
        if (user == null) throw new ServiceException("用户不存在");
        
        // ---------------------------------------------------------
        // 3. 【核心】性别门禁与混合楼逻辑
        // ---------------------------------------------------------
        // 无论是不是混合楼，房间本身必须有性别属性 (1-男, 2-女)
        // 规则：用户的性别 必须等于 房间的性别
        if (room.getGender() != null && user.getSex() != null) {
            if (!room.getGender().equals(user.getSex())) {
                String roomSexStr = (room.getGender() == 1) ? "男生宿舍" : "女生宿舍";
                String userSexStr = (user.getSex() == 1) ? "男" : "女";
                throw new ServiceException("性别不符！该房间为 [" + roomSexStr + "]，学生性别为 [" + userSexStr + "]，禁止入住。");
            }
        }
        
        // 4. 校验是否超员 (双重保险，防止 current_num 不准)
        if (room.getCurrentNum() != null && room.getCapacity() != null) {
            if (room.getCurrentNum() >= room.getCapacity()) {
                throw new ServiceException("该房间已满员");
            }
        }
        
        // 5. 执行 CAS 抢占床位
        int rows = baseMapper.assignBed(bedId, userId);
        if (rows == 0) {
            throw new ServiceException("手慢了！该床位刚刚已被其他人占用");
        }
        
        // 6. 房间当前人数 +1
        room.setCurrentNum(room.getCurrentNum() == null ? 1 : room.getCurrentNum() + 1);
        roomMapper.updateById(room);
        
        log.info("分配成功: 房间[{}] 当前人数更新为 {}", room.getRoomNo(), room.getCurrentNum());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseBed(Long bedId) {
        DormBed bed = this.getById(bedId);
        if (bed == null || bed.getOccupantId() == null) {
            throw new ServiceException("该床位原本就是空闲的");
        }
        Long roomId = bed.getRoomId();
        
        // 1. 释放床位
        boolean update = this.update(Wrappers.<DormBed>lambdaUpdate()
                .set(DormBed::getOccupantId, null)
                .eq(DormBed::getId, bedId));
        if (!update) throw new ServiceException("释放失败");
        
        // 2. 房间当前人数 -1
        DormRoom room = roomMapper.selectById(roomId);
        if (room != null && room.getCurrentNum() > 0) {
            room.setCurrentNum(room.getCurrentNum() - 1);
            roomMapper.updateById(room);
        }
    }
}