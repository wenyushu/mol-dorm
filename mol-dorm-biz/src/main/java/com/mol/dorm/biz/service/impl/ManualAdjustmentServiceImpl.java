package com.mol.dorm.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.mapper.DormBedMapper;
import com.mol.dorm.biz.mapper.DormRoomMapper;
import com.mol.dorm.biz.service.ManualAdjustmentService;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 人工调宿服务实现类 (修复版)
 * <p>
 * 🛡️ 包含完整的防御性编程逻辑
 * </p>
 *
 * @author mol
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ManualAdjustmentServiceImpl implements ManualAdjustmentService {
    
    private final DormBedMapper bedMapper;
    private final DormRoomMapper roomMapper;
    private final SysOrdinaryUserMapper userMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void swapBeds(Long studentIdA, Long studentIdB) {
        // 🛡️ 防刁民1：防手滑
        if (ObjectUtil.equal(studentIdA, studentIdB)) {
            throw new ServiceException("不能和自己互换床位，请检查参数");
        }
        
        // 🛡️ 防刁民2：防幽灵ID
        checkUserExist(studentIdA);
        checkUserExist(studentIdB);
        
        // 1. 查询两人的当前床位
        DormBed bedA = getBedByUserId(studentIdA);
        if (bedA == null) throw new ServiceException("操作失败：学生A当前未入住，无法互换");
        
        DormBed bedB = getBedByUserId(studentIdB);
        if (bedB == null) throw new ServiceException("操作失败：学生B当前未入住，无法互换");
        
        // 🛡️ 防刁民3：性别校验 (交叉校验)
        checkGenderMatch(studentIdA, bedB.getRoomId());
        checkGenderMatch(studentIdB, bedA.getRoomId());
        
        // 2. 交换 OccupantId (原子操作)
        bedA.setOccupantId(studentIdB);
        bedB.setOccupantId(studentIdA);
        
        bedMapper.updateById(bedA);
        bedMapper.updateById(bedB);
        
        log.info("管理员[强制互换]成功: 学生[{}] <-> 学生[{}]", studentIdA, studentIdB);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveUserToBed(Long studentId, Long targetBedId) {
        // 校验学生存在性
        checkUserExist(studentId);
        
        DormBed currentBed = getBedByUserId(studentId);
        
        if (targetBedId == null) {
            // ================== 强制退宿 ==================
            if (currentBed == null) {
                throw new ServiceException("该学生当前名下无床位，无需退宿");
            }
            
            currentBed.setOccupantId(null);
            bedMapper.updateById(currentBed);
            updateRoomCount(currentBed.getRoomId());
            
            log.info("管理员[强制退宿]成功: 学生[{}]", studentId);
            
        } else {
            // ================== 强制搬迁/入住 ==================
            
            // 1. 校验目标床位
            DormBed targetBed = bedMapper.selectById(targetBedId);
            if (targetBed == null) {
                throw new ServiceException("目标床位不存在");
            }
            
            // 🛡️ 防刁民4：防性别错误
            checkGenderMatch(studentId, targetBed.getRoomId());
            
            // 🛡️ 防刁民5：防数据覆盖
            if (targetBed.getOccupantId() != null) {
                if (targetBed.getOccupantId().equals(studentId)) {
                    return; // 已经在该床位了
                }
                throw new ServiceException("目标床位已有其他学生(" + targetBed.getOccupantId() + ")入住，请先清空");
            }
            
            // 2. 腾退旧床位 (如果有)
            if (currentBed != null) {
                currentBed.setOccupantId(null);
                bedMapper.updateById(currentBed);
                updateRoomCount(currentBed.getRoomId());
            }
            
            // 3. 入住新床位
            targetBed.setOccupantId(studentId);
            bedMapper.updateById(targetBed);
            updateRoomCount(targetBed.getRoomId());
            
            log.info("管理员[强制搬迁]成功: 学生[{}] -> 床位[{}]", studentId, targetBedId);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchGraduate(Integer year) {
        if (year == null || year < 2000 || year > 2100) {
            throw new ServiceException("年份输入错误");
        }
        
        // 1. 查找学生 (注意：UserCategory 字段需确保实体类中有，如无请自行调整)
        List<SysOrdinaryUser> graduates = userMapper.selectList(new LambdaQueryWrapper<SysOrdinaryUser>()
                .likeRight(SysOrdinaryUser::getUsername, String.valueOf(year)));
        
        if (CollUtil.isEmpty(graduates)) {
            throw new ServiceException(year + "级未找到任何学生记录");
        }
        
        List<Long> studentIds = graduates.stream().map(SysOrdinaryUser::getId).collect(Collectors.toList());
        
        // 2. 查找床位
        List<DormBed> beds = bedMapper.selectList(new LambdaQueryWrapper<DormBed>()
                .in(DormBed::getOccupantId, studentIds));
        
        if (CollUtil.isEmpty(beds)) {
            return;
        }
        
        // 3. 批量清空
        Set<Long> affectedRoomIds = beds.stream().map(DormBed::getRoomId).collect(Collectors.toSet());
        
        // 使用 UpdateWrapper 进行批量更新
        bedMapper.update(null, Wrappers.<DormBed>lambdaUpdate()
                .in(DormBed::getOccupantId, studentIds)
                .set(DormBed::getOccupantId, null));
        
        // 4. 重算人数
        for (Long roomId : affectedRoomIds) {
            updateRoomCount(roomId);
        }
        
        log.info("批量离校完成: 清退[{}]级学生床位共[{}]个", year, beds.size());
    }
    
    // =========================================================
    // 私有辅助方法 (全部在 Class 内部)
    // =========================================================
    
    /**
     * 校验性别是否匹配 (已修复为 String 类型)
     */
    private void checkGenderMatch(Long userId, Long roomId) {
        // 1. 获取学生性别 (修改为 getGender)
        SysOrdinaryUser user = userMapper.selectOne(Wrappers.<SysOrdinaryUser>lambdaQuery()
                .select(SysOrdinaryUser::getGender, SysOrdinaryUser::getRealName)
                .eq(SysOrdinaryUser::getId, userId));
        
        if (user == null) throw new ServiceException("学生ID[" + userId + "]不存在");
        
        // 🟢 修复：获取 String 类型的性别
        String userGender = user.getGender(); // "1"-男, "0"-女
        
        // 2. 获取房间限制 (修改为 getGender)
        DormRoom room = roomMapper.selectOne(Wrappers.<DormRoom>lambdaQuery()
                .select(DormRoom::getGender, DormRoom::getRoomNo)
                .eq(DormRoom::getId, roomId));
        
        if (room == null) throw new ServiceException("房间ID[" + roomId + "]不存在");
        
        // 🟢 修复：获取 String 类型的房间性别
        String roomGender = room.getGender(); // "1"-男, "0"-女
        
        // 3. 校验逻辑 (字符串比较)
        if (StrUtil.isNotBlank(roomGender)) {
            if (!StrUtil.equals(roomGender, userGender)) {
                String userSexStr = "1".equals(userGender) ? "男" : "女";
                String roomLimitStr = "1".equals(roomGender) ? "男寝" : "女寝";
                
                throw new ServiceException("性别不匹配！学生[" + user.getRealName() +
                        "]是" + userSexStr + "性，无法入住" + room.getRoomNo() + "[" + roomLimitStr + "]");
            }
        }
    }
    
    private void checkUserExist(Long userId) {
        if (userId == null) throw new ServiceException("学生 ID 不能为空");
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysOrdinaryUser>()
                .eq(SysOrdinaryUser::getId, userId));
        if (count == 0) {
            throw new ServiceException("学生ID[" + userId + "]不存在");
        }
    }
    
    private DormBed getBedByUserId(Long userId) {
        return bedMapper.selectOne(Wrappers.<DormBed>lambdaQuery()
                .eq(DormBed::getOccupantId, userId)
                .last("LIMIT 1"));
    }
    
    private void updateRoomCount(Long roomId) {
        Long count = bedMapper.selectCount(Wrappers.<DormBed>lambdaQuery()
                .eq(DormBed::getRoomId, roomId)
                .isNotNull(DormBed::getOccupantId));
        
        DormRoom room = new DormRoom();
        room.setId(roomId);
        room.setCurrentNum(count.intValue());
        roomMapper.updateById(room);
    }
}