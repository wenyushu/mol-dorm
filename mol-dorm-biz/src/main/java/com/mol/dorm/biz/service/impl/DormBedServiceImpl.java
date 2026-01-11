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
// 👇 关键 Import：引用系统模块的 UserMapper
import com.mol.server.mapper.SysOrdinaryUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 宿舍床位业务实现类
 * <p>
 * 核心职责：
 * 1. 处理床位的分配 (入住) 与释放 (退宿)。
 * 2. 维护房间的当前居住人数 (current_num)。
 * 3. 执行严格的业务校验 (性别、满员、重复分配等)。
 * </p>
 *
 * @author mol
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DormBedServiceImpl extends ServiceImpl<DormBedMapper, DormBed> implements DormBedService {
    
    // 注入系统模块 Mapper，用于查询学生性别
    private final SysOrdinaryUserMapper userMapper;
    // 注入房间 Mapper，用于更新房间人数和校验房间性别
    private final DormRoomMapper roomMapper;
    
    /**
     * 分配床位 (入住核心逻辑)
     *
     * @param bedId  目标床位 ID
     * @param userId 学生 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserToBed(Long bedId, Long userId) {
        // ================= 1. 基础数据校验 =================
        // 查床位
        DormBed bed = this.getById(bedId);
        if (bed == null) {
            throw new ServiceException("目标床位不存在");
        }
        // 预检：如果床位已有 occupantId，直接报错
        if (bed.getOccupantId() != null) {
            throw new ServiceException("手慢了！该床位已被占用");
        }
        
        // 查房间
        DormRoom room = roomMapper.selectById(bed.getRoomId());
        if (room == null) {
            throw new ServiceException("数据异常：床位所属的房间不存在");
        }
        
        // 查学生
        SysOrdinaryUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("学生信息不存在");
        }
        
        // ================= 2. 业务规则校验 =================
        
        // A. 性别门禁校验 (Gender Gate)
        // 规则：房间性别必须与学生性别一致 (1-男, 2-女)
        if (room.getGender() != null && user.getSex() != null) {
            if (!room.getGender().equals(user.getSex())) {
                String roomSex = (room.getGender() == 1) ? "男寝" : "女寝";
                String userSex = (user.getSex() == 1) ? "男" : "女";
                throw new ServiceException(String.format("性别不符！该房间是[%s]，学生性别为[%s]，禁止入住。", roomSex, userSex));
            }
        }
        
        // B. 房间容量校验 (双重保险)
        // 防止 current_num 数据已满但床位看起来还是空的极端情况
        if (room.getCapacity() != null && room.getCurrentNum() >= room.getCapacity()) {
            throw new ServiceException("该房间已满员，无法继续分配");
        }
        
        // ================= 3. 执行分配 (原子操作) =================
        
        // 核心：使用 update ... set occupant_id = ? where id = ? AND occupant_id IS NULL
        // 利用数据库行锁防止并发冲突 (两个管理员同时给同一个空床位分人)
        boolean updateResult = this.update(Wrappers.<DormBed>lambdaUpdate()
                .eq(DormBed::getId, bedId)
                .isNull(DormBed::getOccupantId) // CAS 乐观锁条件：必须是空床
                .set(DormBed::getOccupantId, userId));
        
        if (!updateResult) {
            throw new ServiceException("分配失败：该床位刚刚已被其他人抢占");
        }
        
        // ================= 4. 联动更新 =================
        
        // 房间当前人数 +1
        // 考虑到 null 值情况，赋默认值 0
        int currentNum = (room.getCurrentNum() == null) ? 0 : room.getCurrentNum();
        room.setCurrentNum(currentNum + 1);
        roomMapper.updateById(room);
        
        log.info("床位分配成功：床位[{}] -> 学生[{}]，房间[{}]人数更新为 {}",
                bed.getBedLabel(), user.getRealName(), room.getRoomNo(), room.getCurrentNum());
    }
    
    /**
     * 释放床位 (退宿核心逻辑)
     *
     * @param bedId 床位 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseBed(Long bedId) {
        DormBed bed = this.getById(bedId);
        if (bed == null) {
            throw new ServiceException("床位不存在");
        }
        
        // 如果本来就是空的，无需操作，直接返回或提示
        if (bed.getOccupantId() == null) {
            log.warn("重复操作：床位[{}]已经是空闲状态", bed.getBedLabel());
            return;
        }
        
        Long roomId = bed.getRoomId();
        
        // 1. 清空床位上的学生ID
        boolean updateResult = this.update(Wrappers.<DormBed>lambdaUpdate()
                .eq(DormBed::getId, bedId)
                .set(DormBed::getOccupantId, null)); // 置空
        
        if (!updateResult) {
            throw new ServiceException("释放床位失败，请重试");
        }
        
        // 2. 房间当前人数 -1
        DormRoom room = roomMapper.selectById(roomId);
        if (room != null) {
            int currentNum = (room.getCurrentNum() == null) ? 0 : room.getCurrentNum();
            if (currentNum > 0) {
                room.setCurrentNum(currentNum - 1);
                roomMapper.updateById(room);
            }
        }
        
        log.info("床位释放成功：床位ID[{}]，房间ID[{}]", bedId, roomId);
    }
}