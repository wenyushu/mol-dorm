package com.mol.dorm.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.mapper.DormBedMapper;
import com.mol.dorm.biz.mapper.DormRoomMapper;
import com.mol.dorm.biz.service.DormBedService;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 宿舍床位业务实现类
 *
 * @author mol
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DormBedServiceImpl extends ServiceImpl<DormBedMapper, DormBed> implements DormBedService {
    
    private final SysOrdinaryUserMapper userMapper;
    private final DormRoomMapper roomMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserToBed(Long bedId, Long userId) {
        // 1. 基础校验
        DormBed bed = this.getById(bedId);
        if (bed == null) throw new ServiceException("目标床位不存在");
        if (bed.getOccupantId() != null) throw new ServiceException("该床位已被占用");
        
        DormRoom room = roomMapper.selectById(bed.getRoomId());
        if (room == null) throw new ServiceException("房间数据异常");
        
        SysOrdinaryUser user = userMapper.selectById(userId);
        if (user == null) throw new ServiceException("学生不存在");
        
        // 2. 性别校验
        if (room.getGender() != null && user.getSex() != null) {
            if (!room.getGender().equals(user.getSex())) {
                throw new ServiceException("性别不符，禁止入住！");
            }
        }
        
        // 3. 执行分配 (CAS乐观锁)
        boolean success = this.update(Wrappers.<DormBed>lambdaUpdate()
                .eq(DormBed::getId, bedId)
                .isNull(DormBed::getOccupantId)
                .set(DormBed::getOccupantId, userId));
        
        if (!success) throw new ServiceException("分配失败，床位已被抢占");
        
        // 4. 更新房间人数
        int currentNum = (room.getCurrentNum() == null) ? 0 : room.getCurrentNum();
        room.setCurrentNum(currentNum + 1);
        // 如果满了，更新状态
        if (room.getCurrentNum() >= room.getCapacity()) {
            room.setStatus(2); // 满员
        }
        roomMapper.updateById(room);
        
        // 5. 更新学生状态 (预分配状态)
        // 这里可以选择是否立即更新 residence_type，或者等学生点击确认入住再更新
        // 为了方便，分配即视为住校
        user.setResidenceType(0); // 0-住校
        userMapper.updateById(user);
        
        log.info("床位分配成功: {} -> {}", bed.getBedLabel(), user.getRealName());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseBed(Long bedId) {
        DormBed bed = this.getById(bedId);
        if (bed == null) throw new ServiceException("床位不存在");
        
        Long studentId = bed.getOccupantId();
        if (studentId == null) {
            log.warn("床位[{}]已是空闲状态", bed.getBedLabel());
            return;
        }
        
        // 1. 清空床位
        boolean success = this.update(Wrappers.<DormBed>lambdaUpdate()
                .eq(DormBed::getId, bedId)
                .set(DormBed::getOccupantId, null));
        
        if (!success) throw new ServiceException("退宿失败，请重试");
        
        // 2. 更新房间人数
        DormRoom room = roomMapper.selectById(bed.getRoomId());
        if (room != null) {
            int currentNum = (room.getCurrentNum() == null) ? 0 : room.getCurrentNum();
            if (currentNum > 0) {
                room.setCurrentNum(currentNum - 1);
                // 如果之前是满员(2)，现在变正常(1)
                if (room.getStatus() == 2) {
                    room.setStatus(1);
                }
                roomMapper.updateById(room);
            }
        }
        
        // 3. 更新学生状态
        SysOrdinaryUser user = userMapper.selectById(studentId);
        if (user != null) {
            user.setResidenceType(1); // 1-校外/未住
            userMapper.updateById(user);
        }
        
        log.info("退宿成功: 床位[{}]，原住户ID[{}]", bedId, studentId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmCheckIn(Long userId) {
        // 1. 检查该学生是否已分配床位
        DormBed bed = this.getOne(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getOccupantId, userId)
                .last("LIMIT 1"));
        
        if (bed == null) {
            throw new ServiceException("您尚未分配任何床位，无法办理入住！请联系宿管。");
        }
        
        // 2. 更新学生状态 (双重确认)
        SysOrdinaryUser user = userMapper.selectById(userId);
        if (user != null) {
            user.setResidenceType(0); // 确保标记为住校
            // 可以在这里扩展字段，比如 check_in_time = now()
            userMapper.updateById(user);
        }
        
        log.info("学生[{}]已确认入住床位[{}]", userId, bed.getBedLabel());
    }
}