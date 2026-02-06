package com.mol.dorm.biz.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.constant.DormConstants;
import com.mol.common.core.enums.DormStatusEnum;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.*;
import com.mol.dorm.biz.mapper.*;
import com.mol.dorm.biz.service.DormRoomService;
import com.mol.dorm.biz.vo.MyRoomVO;
import com.mol.server.entity.SysNotice;
import com.mol.server.mapper.SysNoticeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 房间管理服务实现类 - 工业级严苛模式 (警告清理版)
 * 🛡️ [修正点]：
 * 1. 修复类型比对错误：Integer 与 String 的 equals 比较改为 Objects.equals 或类型转换。
 * 2. 激活注入组件：确保 fixedAssetMapper 在安全评估逻辑中被正确调用。
 * 3. 增强房型算法：支持北方 1-20+ 人寝室动态映射。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DormRoomServiceImpl extends ServiceImpl<DormRoomMapper, DormRoom> implements DormRoomService {
    
    private final DormFloorMapper floorMapper;
    private final DormBuildingMapper buildingMapper;
    private final DormBedMapper bedMapper;
    private final DormFixedAssetMapper fixedAssetMapper; // 👈 警告已消除：将在 evaluateRoomSafety 中使用
    private final DormRoomWalletMapper walletMapper;
    private final UtilityBillMapper billMapper;
    private final SysNoticeMapper noticeMapper;
    
    /**
     * 1. 严格保存房间 (含语义算法对齐)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRoomStrict(DormRoom room) {
        DormFloor floor = floorMapper.selectById(room.getFloorId());
        if (floor == null) throw new ServiceException("录入失败：关联楼层不存在");
        
        DormBuilding building = buildingMapper.selectById(floor.getBuildingId());
        if (building == null) throw new ServiceException("录入失败：关联楼栋不存在");
        
        // 性别隔离校验 (Integer 转 String 进行比对)
        if (!Objects.equals(room.getGender(), String.valueOf(floor.getGenderLimit())) && floor.getGenderLimit() != 3) {
            throw new ServiceException(StrUtil.format("性别合规拦截：第 {} 层非混合层，禁止跨性别创建房间！", floor.getFloorNum()));
        }
        
        // 🚀 语义算法自动映射
        room.setApartmentType(convertToApartmentType(room.getCapacity()));
        
        room.setUsageType(building.getUsageType());
        room.setBuildingId(building.getId());
        room.setCampusId(building.getCampusId());
        
        if (room.getId() == null) {
            room.setStatus(DormConstants.LC_NORMAL);
            room.setSafetyLevel(1);
            room.setCurrentNum(0);
            room.setResStatus(DormConstants.RES_EMPTY);
        } else {
            DormRoom oldRoom = this.getById(room.getId());
            if (!Objects.equals(oldRoom.getGender(), room.getGender()) || !Objects.equals(oldRoom.getCapacity(), room.getCapacity())) {
                checkRoomOccupancy(room.getId(), "修改核心属性");
            }
        }
        
        if (!this.saveOrUpdate(room)) {
            throw new ServiceException("并发冲突：房间数据已被修改");
        }
        
        this.refreshResourceStatus(room.getId());
        this.evaluateRoomSafety(room.getId());
    }
    
    /**
     * 2. [核心功能] 获取移动端聚合看板数据
     */
    @Override
    public MyRoomVO getMyRoomDashboard(Long studentId) {
        DormRoom room = baseMapper.selectByStudentId(studentId);
        if (room == null) throw new ServiceException("看板加载失败：未检测到您的入住信息");
        
        MyRoomVO vo = new MyRoomVO();
        vo.setRoomNo(room.getRoomNo());
        vo.setApartmentType(room.getApartmentType());
        vo.setStatusDesc(DormStatusEnum.fromCode(room.getStatus()).getDesc());
        
        // 钱包聚合
        DormRoomWallet wallet = walletMapper.selectOne(Wrappers.<DormRoomWallet>lambdaQuery().eq(DormRoomWallet::getRoomId, room.getId()));
        if (wallet != null) {
            vo.setWalletBalance(wallet.getBalance());
            vo.setWalletStatus(wallet.getStatus());
            vo.setPowerOffWarning(wallet.getBalance().compareTo(new BigDecimal("10")) < 0);
        }
        
        // 账单快照
        UtilityBill lastBill = billMapper.selectOne(Wrappers.<UtilityBill>lambdaQuery()
                .eq(UtilityBill::getRoomId, room.getId()).orderByDesc(UtilityBill::getMonth).last("LIMIT 1"));
        if (lastBill != null) {
            vo.setLastBillMonth(lastBill.getMonth());
            vo.setLastBillAmount(lastBill.getTotalAmount());
        }
        
        // 舍友矩阵
        List<DormBed> beds = bedMapper.selectList(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getRoomId, room.getId()));
        vo.setBedList(beds.stream().map(b -> {
            MyRoomVO.BedInfoVO bedVo = new MyRoomVO.BedInfoVO();
            bedVo.setBedId(b.getId());
            bedVo.setBedLabel(b.getBedLabel());
            bedVo.setIsOccupied(b.getOccupantId() != null);
            bedVo.setIsMe(studentId.equals(b.getOccupantId()));
            return bedVo;
        }).collect(Collectors.toList()));
        
        // 🛡️ 修正点：公告类型比对警告消除
        SysNotice latest = noticeMapper.selectOne(Wrappers.<SysNotice>lambdaQuery()
                .eq(SysNotice::getStatus, "0").orderByDesc(SysNotice::getLevel, SysNotice::getCreateTime).last("LIMIT 1"));
        if (latest != null) {
            MyRoomVO.NoticeSnippetVO nVo = new MyRoomVO.NoticeSnippetVO();
            nVo.setTitle(latest.getTitle());
            // 💡 修正：使用整数比对，或 Objects.equals(latest.getType(), 3)
            nVo.setTypeDesc(Objects.equals(latest.getType(), 3) ? "欠费通知" : "常规公告");
            vo.setLatestNotice(nVo);
        }
        
        return vo;
    }
    
    /**
     * 3. 房间安全评估 (资产联动)
     * 🛡️ 修正点：正确使用 fixedAssetMapper 消除警告
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void evaluateRoomSafety(Long roomId) {
        DormRoom room = this.getById(roomId);
        if (room == null) return;
        
        // 💡 显式使用 fixedAssetMapper 进行统计
        List<Map<String, Object>> stats = fixedAssetMapper.countStatusByRoom(roomId);
        long brokenCount = 0;
        for (Map<String, Object> stat : stats) {
            Integer status = (Integer) stat.get("status");
            Long count = Long.valueOf(stat.get("total_count").toString());
            if (Objects.equals(status, 50) || Objects.equals(status, 60)) {
                brokenCount += count;
            }
        }
        
        if (brokenCount >= 3) {
            room.setSafetyLevel(3);
            room.setStatus(50); // 触发维修下架
        } else {
            room.setSafetyLevel(brokenCount > 0 ? 2 : 1);
            if (brokenCount == 0 && Objects.equals(room.getStatus(), 50)) room.setStatus(20);
        }
        this.updateById(room);
    }
    
    /**
     * 4. 动态调整容量 (含算法联动)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustRoomCapacity(Long roomId, Integer newCapacity) {
        DormRoom room = this.getById(roomId);
        if (room == null) throw new ServiceException("房间档案丢失");
        if (newCapacity < room.getCurrentNum()) throw new ServiceException("缩减容量拦截：在住人数超标");
        
        room.setCapacity(newCapacity);
        room.setApartmentType(convertToApartmentType(newCapacity)); // 🚀 语义自动对齐
        
        this.updateById(room);
        this.refreshResourceStatus(roomId);
    }
    
    /**
     * 🛡️ 辅助：动态房型语义转换算法
     */
    private String convertToApartmentType(Integer capacity) {
        if (capacity == null || capacity <= 0) return "未知房型";
        String[] units = {"", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
        StringBuilder sb = new StringBuilder();
        if (capacity < 10) sb.append(units[capacity]);
        else if (capacity < 20) sb.append("十").append(units[capacity % 10]);
        else {
            sb.append(units[capacity / 10]).append("十").append(units[capacity % 10]);
        }
        return sb.append("人间").toString();
    }
    
    /**
     * 其他方法 (updateRoomStatus, refreshResourceStatus, removeRoomStrict 等) 保持原有的严苛逻辑
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoomStatus(Long roomId, Integer status) {
        if (!Objects.equals(status, DormConstants.LC_NORMAL)) checkRoomOccupancy(roomId, "状态变更");
        DormRoom room = new DormRoom();
        room.setId(roomId);
        room.setStatus(status);
        this.updateById(room);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshResourceStatus(Long roomId) {
        DormRoom room = this.getById(roomId);
        if (room == null) return;
        Long realNum = Optional.ofNullable(bedMapper.selectCount(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getRoomId, roomId).isNotNull(DormBed::getOccupantId))).orElse(0L);
        room.setCurrentNum(realNum.intValue());
        if (realNum == 0) room.setResStatus(21);
        else if (realNum >= room.getCapacity()) room.setResStatus(26);
        else room.setResStatus(24);
        this.updateById(room);
    }
    
    @Override public List<DormRoom> getByFloor(Long floorId) { return this.list(Wrappers.<DormRoom>lambdaQuery().eq(DormRoom::getFloorId, floorId).orderByAsc(DormRoom::getRoomNo)); }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeRoomStrict(Long roomId) {
        checkRoomOccupancy(roomId, "物理注销");
        this.removeById(roomId);
    }
    
    private void checkRoomOccupancy(Long roomId, String action) {
        Long count = bedMapper.selectCount(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getRoomId, roomId).isNotNull(DormBed::getOccupantId));
        if (count > 0) throw new ServiceException(StrUtil.format("安全熔断：[{}] 失败！房间内仍有人员。", action));
    }
}