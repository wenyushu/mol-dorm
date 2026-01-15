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
 * 宿舍床位业务实现类 (最终增强版)
 * <p>
 * 包含完整的“防刁民”校验逻辑、并发锁控制以及数据一致性维护。
 * </p>
 *
 * @author mol
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DormBedServiceImpl extends ServiceImpl<DormBedMapper, DormBed> implements DormBedService {
    
    private final SysOrdinaryUserMapper userMapper;
    private final DormRoomMapper roomMapper;
    
    /**
     * 分配床位 (预分配)
     * 核心逻辑：
     * 1. 校验人：是否存在、是否已有床位(防多占)、性别是否匹配。
     * 2. 校验床：是否存在、是否已有人。
     * 3. 并发写：使用乐观锁抢占床位。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserToBed(Long bedId, Long userId) {
        // ================== 1. 防刁民：参数与身份校验 ==================
        if (bedId == null || userId == null) {
            throw new ServiceException("参数错误：床位ID或用户ID不能为空");
        }
        
        SysOrdinaryUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("学生不存在，无法分配");
        }
        
        // [核心防御] 检查该学生是否已经在别的床上躺着了
        // 防止出现 “同一个ID占两个坑” 的数据腐坏情况
        Long count = this.baseMapper.selectCount(Wrappers.<DormBed>lambdaQuery()
                .eq(DormBed::getOccupantId, userId));
        if (count > 0) {
            throw new ServiceException("分配失败：该学生已分配其他床位，请先退宿！");
        }
        
        // ================== 2. 校验目标床位与房间 ==================
        DormBed bed = this.getById(bedId);
        if (bed == null) {
            throw new ServiceException("目标床位不存在");
        }
        if (bed.getOccupantId() != null) {
            throw new ServiceException("手慢了！该床位已被占用");
        }
        
        DormRoom room = roomMapper.selectById(bed.getRoomId());
        if (room == null) {
            throw new ServiceException("数据异常：床位所属房间不存在");
        }
        
        // [核心防御] 性别门禁校验
        // 1=男, 2=女。如果房间有性别限制，必须匹配。
        if (room.getGender() != null && room.getGender() != 0 && user.getSex() != null) {
            if (!room.getGender().equals(user.getSex())) {
                throw new ServiceException("严重警告：性别不符，禁止分配！");
            }
        }
        
        // ================== 3. 执行分配 (CAS乐观锁) ==================
        // SQL: UPDATE dorm_bed SET occupant_id = ? WHERE id = ? AND occupant_id IS NULL
        // 只有当 occupant_id 为空时才更新，防止并发覆盖
        boolean success = this.update(Wrappers.<DormBed>lambdaUpdate()
                .eq(DormBed::getId, bedId)
                .isNull(DormBed::getOccupantId)
                .set(DormBed::getOccupantId, userId));
        
        if (!success) {
            throw new ServiceException("分配失败，床位可能刚刚被抢占，请刷新重试");
        }
        
        // ================== 4. 维护房间与学生状态 ==================
        // 更新房间当前人数
        int currentNum = (room.getCurrentNum() == null) ? 0 : room.getCurrentNum();
        room.setCurrentNum(currentNum + 1);
        
        // 如果人数达到上限，标记为满员(2)
        if (room.getCurrentNum() >= room.getCapacity()) {
            room.setStatus(2);
        }
        roomMapper.updateById(room);
        
        // 更新学生状态为“住校”
        user.setResidenceType(0);
        userMapper.updateById(user);
        
        log.info("床位分配成功: 床位[{}-{}] -> 学生[{}]", room.getRoomNo(), bed.getBedLabel(), user.getRealName());
    }
    
    /**
     * 释放床位 (退宿)
     * 核心逻辑：
     * 1. 强制将 occupant_id 置为 NULL。
     * 2. 减少房间人数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseBed(Long bedId) {
        if (bedId == null) throw new ServiceException("未指定床位 ID");
        
        DormBed bed = this.getById(bedId);
        if (bed == null) throw new ServiceException("床位不存在");
        
        Long studentId = bed.getOccupantId();
        if (studentId == null) {
            log.warn("床位[{}]已是空闲状态，无需重复退宿", bed.getBedLabel());
            return;
        }
        
        // ================== 1. 执行退宿 (强制置空) ==================
        // [修复] 使用 UpdateWrapper 显式设置 NULL，防止 MyBatis-Plus 忽略 null 更新
        boolean success = this.update(null, Wrappers.<DormBed>lambdaUpdate()
                .eq(DormBed::getId, bedId)
                .set(DormBed::getOccupantId, null)); // 关键：强制设为 NULL
        
        if (!success) {
            throw new ServiceException("退宿失败，数据可能已被修改");
        }
        
        // ================== 2. 维护房间数据 ==================
        DormRoom room = roomMapper.selectById(bed.getRoomId());
        if (room != null && room.getCurrentNum() > 0) {
            room.setCurrentNum(room.getCurrentNum() - 1);
            // 如果之前是满员(2)，现在变回正常(1)
            if (room.getStatus() == 2) {
                room.setStatus(1);
            }
            roomMapper.updateById(room);
        }
        
        // ================== 3. 维护学生状态 ==================
        SysOrdinaryUser user = userMapper.selectById(studentId);
        if (user != null) {
            user.setResidenceType(1); // 1-校外/未住
            userMapper.updateById(user);
        }
        
        log.info("退宿成功: 床位[{}]，原住户ID[{}]", bed.getBedLabel(), studentId);
    }
    
    /**
     * 学生确认入住 (Check-in)
     * 通常用于开学报到场景
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmCheckIn(Long userId) {
        if (userId == null) throw new ServiceException("用户 ID 为空");
        
        // 1. 检查该学生是否真的有床位
        DormBed bed = this.getOne(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getOccupantId, userId)
                .last("LIMIT 1"));
        
        if (bed == null) {
            throw new ServiceException("系统检测到您尚未分配床位，请先联系宿管或辅导员！");
        }
        
        // 2. 更新学生状态 (双重确认)
        SysOrdinaryUser user = userMapper.selectById(userId);
        if (user != null) {
            user.setResidenceType(0); // 确保标记为住校
            userMapper.updateById(user);
        }
        
        log.info("学生[{}]已确认入住床位[{}]", userId, bed.getBedLabel());
    }
    
    /**
     * 根据学生ID查询当前床位
     * 用于前端展示“我的床位”或后端逻辑判断
     */
    @Override
    public DormBed getBedByStudentId(Long studentId) {
        if (studentId == null) return null;
        
        // 使用 last("LIMIT 1") 防止脏数据导致报错
        return this.getOne(Wrappers.<DormBed>lambdaQuery()
                .eq(DormBed::getOccupantId, studentId)
                .last("LIMIT 1"));
    }
}