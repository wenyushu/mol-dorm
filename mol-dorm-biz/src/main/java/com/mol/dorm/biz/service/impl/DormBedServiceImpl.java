package com.mol.dorm.biz.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.entity.SysAdminUser;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.mapper.DormBedMapper;
import com.mol.dorm.biz.mapper.DormRoomMapper;
import com.mol.dorm.biz.service.DormBedService;
import com.mol.server.mapper.SysAdminUserMapper;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 宿舍床位业务核心实现类
 *
 * @author mol
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DormBedServiceImpl extends ServiceImpl<DormBedMapper, DormBed> implements DormBedService {
    
    private final DormRoomMapper roomMapper;
    private final SysOrdinaryUserMapper ordinaryUserMapper;
    private final SysAdminUserMapper adminUserMapper;
    
    // =================================================================================================
    // 核心业务：分配床位 (入住)
    // =================================================================================================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignBed(Long bedId, Long userId, Integer userType) {
        // --- 1. 基础参数校验 ---
        if (bedId == null || userId == null || userType == null) {
            throw new ServiceException("分配失败：关键参数缺失");
        }
        
        // --- 2. 用户身份核验 & 性别获取 ---
        String userGender; // "0"-女, "1"-男
        String userName;
        
        if (userType == 0) {
            SysOrdinaryUser user = ordinaryUserMapper.selectById(userId);
            if (user == null) throw new ServiceException("用户不存在 (ID: " + userId + ")");
            userGender = user.getGender();
            userName = user.getRealName();
            if ("1".equals(user.getStatus())) throw new ServiceException("该账号已被停用，无法办理入住");
            
        } else if (userType == 1) {
            SysAdminUser admin = adminUserMapper.selectById(userId);
            if (admin == null) throw new ServiceException("管理员不存在 (ID: " + userId + ")");
            userGender = admin.getGender();
            userName = admin.getRealName();
        } else {
            throw new ServiceException("不支持的用户类型");
        }
        
        // --- 3. 防重入校验 ---
        Long count = this.baseMapper.selectCount(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getOccupantId, userId)
                .eq(DormBed::getOccupantType, userType));
        if (count > 0) {
            throw new ServiceException("分配失败：该用户已分配其他床位，请先执行退宿操作！");
        }
        
        // --- 4. 床位与房间状态校验 ---
        DormBed bed = this.getById(bedId);
        if (bed == null) throw new ServiceException("目标床位不存在");
        
        if (bed.getStatus() != 0) {
            throw new ServiceException("操作拦截：该床位当前不可分配 (状态码: " + bed.getStatus() + ")");
        }
        
        DormRoom room = roomMapper.selectById(bed.getRoomId());
        if (room == null) throw new ServiceException("床位所属房间不存在");
        
        if (room.getStatus() >= 40) {
            throw new ServiceException("操作拦截：所属房间处于维修/装修封锁状态，禁止入住");
        }
        
        // --- 5. 性别熔断机制 ---
        if (!StrUtil.equals(room.getGender(), userGender)) {
            String roomLimit = "1".equals(room.getGender()) ? "男寝" : "女寝";
            // 🟢 修复点：直接使用局部变量 userGender，而不是调用那个 dummy 方法
            String userSex = "1".equals(userGender) ? "男" : "女";
            throw new ServiceException("性别严重不符：试图将 [" + userSex + "] 性用户分配至 [" + roomLimit + "]");
        }
        
        // --- 6. 执行分配 ---
        boolean updateBed = this.update(Wrappers.<DormBed>lambdaUpdate()
                .eq(DormBed::getId, bedId)
                .eq(DormBed::getStatus, 0)
                .set(DormBed::getOccupantId, userId)
                .set(DormBed::getOccupantType, userType)
                .set(DormBed::getStatus, 1));
        
        if (!updateBed) {
            throw new ServiceException("手慢了！该床位刚刚被抢占或状态已变更");
        }
        
        // --- 7. 联动维护房间数据 ---
        synchronized (this) {
            roomMapper.incrementCurrentNum(room.getId());
            DormRoom updatedRoom = roomMapper.selectById(room.getId());
            if (updatedRoom.getCurrentNum() >= updatedRoom.getCapacity()) {
                updatedRoom.setStatus(20);
                roomMapper.updateById(updatedRoom);
            }
        }
        
        // --- 8. 联动维护用户状态 ---
        if (userType == 0) {
            SysOrdinaryUser updateStu = new SysOrdinaryUser();
            updateStu.setId(userId);
            updateStu.setResidenceType(0);
            ordinaryUserMapper.updateById(updateStu);
        } else {
            SysAdminUser updateAdmin = new SysAdminUser();
            updateAdmin.setId(userId);
            updateAdmin.setResidenceType(0);
            adminUserMapper.updateById(updateAdmin);
        }
        
        log.info("✅ 入住成功: 床位[{}] -> 用户[{}-{}]", bed.getBedLabel(), userType, userName);
    }
    
    // =================================================================================================
    // 核心业务：释放床位 (退宿)
    // =================================================================================================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseBed(Long bedId) {
        if (bedId == null) throw new ServiceException("未指定床位 ID");
        
        DormBed bed = this.getById(bedId);
        if (bed == null) throw new ServiceException("床位不存在");
        
        Long occupantId = bed.getOccupantId();
        Integer occupantType = bed.getOccupantType();
        
        // 幂等性处理：如果 occupantId 是 null，这里就会返回
        if (occupantId == null || bed.getStatus() == 0) {
            log.warn("床位[{}]已是空闲状态，无需重复退宿", bed.getBedLabel());
            return;
        }
        
        // --- 1. 执行退宿 ---
        boolean success = this.update(null, Wrappers.<DormBed>lambdaUpdate()
                .eq(DormBed::getId, bedId)
                .set(DormBed::getOccupantId, null)
                .set(DormBed::getOccupantType, null)
                .set(DormBed::getStatus, 0));
        
        if (!success) {
            throw new ServiceException("退宿失败，数据可能已被并发修改");
        }
        
        // --- 2. 维护房间数据 ---
        DormRoom room = roomMapper.selectById(bed.getRoomId());
        if (room != null) {
            if (room.getCurrentNum() > 0) {
                roomMapper.decrementCurrentNum(room.getId());
            }
            DormRoom latestRoom = roomMapper.selectById(room.getId());
            if (latestRoom.getStatus() == 20 && latestRoom.getCurrentNum() < latestRoom.getCapacity()) {
                latestRoom.setStatus(10);
                roomMapper.updateById(latestRoom);
            }
        }
        
        // --- 3. 维护用户状态 ---
        // 🟢 修复点：移除了 occupantId != null 的冗余判断
        // 因为如果 occupantId 为 null，代码在上面就已经 return 了，能走到这里说明它一定有值
        if (occupantType != null) {
            if (occupantType == 0) {
                SysOrdinaryUser user = new SysOrdinaryUser();
                user.setId(occupantId);
                user.setResidenceType(1); // 1-校外/未住
                ordinaryUserMapper.updateById(user);
            } else {
                SysAdminUser admin = new SysAdminUser();
                admin.setId(occupantId);
                admin.setResidenceType(1); // 1-校外/未住
                adminUserMapper.updateById(admin);
            }
        }
        
        log.info("👋 退宿成功: 床位[{}]，原住户[{}]", bed.getBedLabel(), occupantId);
    }
    
    // =================================================================================================
    // 辅助查询
    // =================================================================================================
    
    @Override
    public DormBed getBedDetail(Long bedId) {
        return this.getById(bedId);
    }
}