package com.mol.dorm.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.mapper.DormBedMapper;
import com.mol.dorm.biz.mapper.DormRoomMapper;
import com.mol.dorm.biz.service.DormRoomService;
import com.mol.dorm.biz.vo.DormRoomVO;
import com.mol.sys.biz.mapper.SysOrdinaryUserMapper; // 引用系统模块 Mapper
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 宿舍房间业务核心实现类
 * <p>
 * 负责房间的生命周期管理（建、改、删、查）以及突发事件处理（转移、封寝）。
 * </p>
 *
 * @author mol
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DormRoomServiceImpl extends ServiceImpl<DormRoomMapper, DormRoom> implements DormRoomService {
    
    private final DormBedMapper bedMapper;
    private final SysOrdinaryUserMapper userMapper;
    
    // =========================== 1. 单个房间管理 (增删改) ===========================
    
    /**
     * 新增房间
     * <p>
     * 1. 校验必填项。
     * 2. 校验同一楼栋下房间号是否重复。
     * 3. 保存房间并自动生成配套床位。
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRoom(DormRoom room) {
        // 1. 基础参数校验
        if (room.getBuildingId() == null || StrUtil.isBlank(room.getRoomNo())) {
            throw new ServiceException("楼栋和房间号不能为空");
        }
        if (room.getCapacity() == null || room.getCapacity() <= 0) {
            throw new ServiceException("房间容量必须大于0");
        }
        
        // 2. 唯一性校验 (同一楼栋下房间号唯一)
        long count = this.count(new LambdaQueryWrapper<DormRoom>()
                .eq(DormRoom::getBuildingId, room.getBuildingId())
                .eq(DormRoom::getRoomNo, room.getRoomNo()));
        if (count > 0) {
            throw new ServiceException("该楼栋下已存在房间号：" + room.getRoomNo());
        }
        
        // 3. 初始化默认值并保存
        room.setCurrentNum(0);
        room.setStatus(1); // 默认 1 - 正常
        this.save(room);
        
        // 4. 自动生成配套床位 (如 101-1, 101-2)
        createBeds(room.getId(), room.getRoomNo(), room.getCapacity());
    }
    
    /**
     * 修改房间信息
     * <p>
     * 核心逻辑：
     * 如果修改了容量 (Capacity)，需要联动处理床位。
     * - 扩容：追加新床位。
     * - 缩容：删除空床位 (如果人数超标则禁止缩容)。
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoom(DormRoom room) {
        DormRoom oldRoom = this.getById(room.getId());
        if (oldRoom == null) {
            throw new ServiceException("房间不存在");
        }
        
        // 校验1：如果状态改为 0 (停用/封禁)，必须确保没人住
        if (room.getStatus() != null && room.getStatus() == 0) {
            // 如果原来是正常的，且当前有人，则禁止停用
            if (oldRoom.getCurrentNum() > 0) {
                throw new ServiceException("停用失败：该房间仍有 " + oldRoom.getCurrentNum() + " 人居住！");
            }
        }
        
        // 2. 如果修改了房间号，需查重
        if (!oldRoom.getRoomNo().equals(room.getRoomNo())) {
            long count = this.count(new LambdaQueryWrapper<DormRoom>()
                    .eq(DormRoom::getBuildingId, oldRoom.getBuildingId())
                    .eq(DormRoom::getRoomNo, room.getRoomNo())
                    .ne(DormRoom::getId, room.getId()));
            if (count > 0) {
                throw new ServiceException("新房间号已存在");
            }
        }
        
        // 3. 处理扩容/缩容逻辑
        Integer oldCap = oldRoom.getCapacity();
        Integer newCap = room.getCapacity();
        
        if (newCap != null && !newCap.equals(oldCap)) {
            if (newCap < oldCap) {
                // --- 缩容逻辑 ---
                // 安全检查：如果当前实际居住人数 > 新容量，禁止操作，防止数据不一致
                if (oldRoom.getCurrentNum() > newCap) {
                    throw new ServiceException("缩容失败：当前居住人数(" + oldRoom.getCurrentNum() +
                            ")超过新容量(" + newCap + ")，请先移出部分学生");
                }
                // 删除多余的空床位
                removeExcessBeds(room.getId(), oldCap - newCap);
            } else {
                // --- 扩容逻辑 ---
                // 追加新床位
                addMoreBeds(room.getId(), room.getRoomNo(), oldCap + 1, newCap);
            }
        }
        
        // 3. 更新房间基本信息
        this.updateById(room);
    }
    
    /**
     * 删除房间
     * <p>
     * 安全策略：只有空房间才能被删除。
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoom(Long roomId) {
        DormRoom room = this.getById(roomId);
        if (room == null) return;
        
        // 安全校验,有人绝对不能删
        if (room.getCurrentNum() > 0) {
            throw new ServiceException("删除失败：该房间仍有 " + room.getCurrentNum() + " 人居住，请先清退人员。");
        }
        
        // 级联删除关联的空床位
        bedMapper.delete(new LambdaQueryWrapper<DormBed>().eq(DormBed::getRoomId, roomId));
        // 删除房间本身
        this.removeById(roomId);
    }
    
    // =========================== 2. 楼层批量操作 (核心新增) ===========================
    
    /**
     * 停用整层楼
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableFloor(Long buildingId, Integer floor) {
        // 1. 检查该层是否有人居住
        Long occupiedCount = this.baseMapper.selectCount(new LambdaQueryWrapper<DormRoom>()
                .eq(DormRoom::getBuildingId, buildingId)
                .eq(DormRoom::getFloor, floor) // 对应 Entity 中的 floor 字段
                .gt(DormRoom::getCurrentNum, 0));
        
        if (occupiedCount > 0) {
            throw new ServiceException("停用失败：该楼层仍有 " + occupiedCount + " 间房有人居住！");
        }
        
        // 2. 批量更新状态为 0 (停用)
        DormRoom updateEntity = new DormRoom();
        updateEntity.setStatus(0);
        
        this.update(updateEntity, new LambdaQueryWrapper<DormRoom>()
                .eq(DormRoom::getBuildingId, buildingId)
                .eq(DormRoom::getFloor, floor));
        
        log.info("楼层停用成功：楼栋ID={}, 楼层={}", buildingId, floor);
    }
    
    /**
     * 删除整层楼
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFloor(Long buildingId, Integer floor) {
        // 1. 检查该层是否有人
        Long occupiedCount = this.baseMapper.selectCount(new LambdaQueryWrapper<DormRoom>()
                .eq(DormRoom::getBuildingId, buildingId)
                .eq(DormRoom::getFloor, floor)
                .gt(DormRoom::getCurrentNum, 0));
        
        if (occupiedCount > 0) {
            throw new ServiceException("删除失败：该楼层仍有 " + occupiedCount + " 间房有人居住！");
        }
        
        // 2. 查出该层所有房间ID
        List<DormRoom> rooms = this.list(new LambdaQueryWrapper<DormRoom>()
                .select(DormRoom::getId)
                .eq(DormRoom::getBuildingId, buildingId)
                .eq(DormRoom::getFloor, floor));
        
        if (CollUtil.isEmpty(rooms)) return;
        List<Long> roomIds = rooms.stream().map(DormRoom::getId).collect(Collectors.toList());
        
        // 3. 级联删除床位
        bedMapper.delete(new LambdaQueryWrapper<DormBed>().in(DormBed::getRoomId, roomIds));
        
        // 4. 级联删除房间
        this.removeBatchByIds(roomIds);
        
        log.info("楼层删除成功：楼栋ID={}, 楼层={}, 共删除房间 {} 间", buildingId, floor, roomIds.size());
    }
    
    // =========================== 3. 高级查询逻辑 (VO封装) ===========================
    
    /**
     * 获取单条房间详情
     * <p>
     * 返回数据包含：房间基础信息 + 床位列表 + 居住人姓名/学号。
     * </p>
     */
    @Override
    public DormRoomVO getRoomDetail(Long roomId) {
        DormRoom room = this.getById(roomId);
        if (room == null) return null;
        
        DormRoomVO vo = new DormRoomVO();
        BeanUtils.copyProperties(room, vo);
        
        // 查询该房间的所有床位
        List<DormBed> beds = bedMapper.selectList(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getRoomId, roomId)
                .orderByAsc(DormBed::getBedLabel));
        
        // 填充人员信息 (避免返回纯 ID)
        fillStudentInfo(beds, vo);
        return vo;
    }
    
    /**
     * 分页查询房间列表 (增强版)
     * <p>
     * 解决 N+1 问题：
     * 先查出当前页的所有房间，再批量查询这些房间的床位和学生信息，
     * 最后在内存中进行组装。
     * </p>
     */
    @Override
    public Page<DormRoomVO> getRoomVoPage(Page<DormRoom> page, Long buildingId) {
        
        // 1. 查房间分页
        Page<DormRoom> roomPage = this.page(page, new LambdaQueryWrapper<DormRoom>()
                .eq(DormRoom::getBuildingId, buildingId)
                .orderByAsc(DormRoom::getFloor) // 先按楼层
                .orderByAsc(DormRoom::getRoomNo)); // 再按房号
        
        if (CollUtil.isEmpty(roomPage.getRecords())) {
            return new Page<>(page.getCurrent(), page.getSize(), 0);
        }

        // 2. 批量查床位
        List<Long> roomIds = roomPage.getRecords().stream().map(DormRoom::getId).collect(Collectors.toList());
        List<DormBed> allBeds = bedMapper.selectList(new LambdaQueryWrapper<DormBed>()
                .in(DormBed::getRoomId, roomIds)
                .orderByAsc(DormBed::getBedLabel));
        
        // 3. 提取所有居住人ID，批量查学生信息
        Set<Long> studentIds = allBeds.stream()
                .map(DormBed::getOccupantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Map<Long, SysOrdinaryUser> studentMap = new HashMap<>();
        
        if (CollUtil.isNotEmpty(studentIds)) {
            // 注意：MyBatis-Plus 3.5.10+ 使用 selectByIds
            List<SysOrdinaryUser> students = userMapper.selectByIds(studentIds);
            for (SysOrdinaryUser s : students) {
                studentMap.put(s.getId(), s);
            }
        }
        
        // 4. 内存组装数据 (将床位按房间分组)
        Map<Long, List<DormBed>> roomBedMap = allBeds.stream()
                .collect(Collectors.groupingBy(DormBed::getRoomId));
        
        // 将 Entity 转换为 VO
        List<DormRoomVO> voList = roomPage.getRecords().stream().map(room -> {
            DormRoomVO vo = new DormRoomVO();
            BeanUtils.copyProperties(room, vo);
            
            // 获取该房间的床位列表
            List<DormBed> myBeds = roomBedMap.getOrDefault(room.getId(), Collections.emptyList());
            
            // 转换床位信息
            List<DormRoomVO.BedInfo> bedInfos = myBeds.stream().map(bed -> {
                DormRoomVO.BedInfo info = new DormRoomVO.BedInfo();
                info.setBedId(bed.getId());
                info.setBedLabel(bed.getBedLabel());
                info.setStudentId(bed.getOccupantId());
                
                // 填充学生姓名
                if (bed.getOccupantId() != null) {
                    SysOrdinaryUser u = studentMap.get(bed.getOccupantId());
                    if (u != null) {
                        info.setStudentName(u.getRealName());
                        info.setStudentNo(u.getUsername());
                    }
                }
                return info;
            }).collect(Collectors.toList());
            
            vo.setBedList(bedInfos);
            return vo;
        }).collect(Collectors.toList());
        
        // 5. 构造结果页并返回
        Page<DormRoomVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), roomPage.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }
    
    // =========================== 4. 应急处理逻辑 ===========================
    
    /**
     * 紧急转移人员
     * <p>
     * 将源房间 (source) 的所有居住人员，批量移动到目标房间 (target) 的空床位上。
     * 转移后，源房间会被封锁 (status=0)。
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void emergencyTransfer(Long sourceRoomId, Long targetRoomId) {
        
        DormRoom source = this.getById(sourceRoomId);
        DormRoom target = this.getById(targetRoomId);
        
        // 防刁民
        if (source == null || target == null) throw new ServiceException("房间不存在");
        if (sourceRoomId.equals(targetRoomId)) throw new ServiceException("目标房间不能与原房间相同");
        
        
        // 检查目标房间是否可用
        if (target.getStatus() != null && target.getStatus() == 0) {
            throw new ServiceException("目标房间当前不可用 (维修/封寝中)");
        }
        
        // 容量检查
        int peopleCount = source.getCurrentNum();
        int targetAvailable = target.getCapacity() - target.getCurrentNum();
        if (peopleCount > targetAvailable) {
            throw new ServiceException("目标房间床位不足 (需" + peopleCount + "，余" + targetAvailable + ")");
        }
        
        // 如果原房间没人，直接封锁即可
        if (peopleCount == 0) {
            source.setStatus(0);
            this.updateById(source);
            return;
        }
        
        // 获取原房间有人的床位
        List<DormBed> sourceBeds = bedMapper.selectList(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getRoomId, sourceRoomId)
                .isNotNull(DormBed::getOccupantId));
        
        // 获取目标房间的空床位 (只取需要的数量)
        List<DormBed> targetEmptyBeds = bedMapper.selectList(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getRoomId, targetRoomId)
                .isNull(DormBed::getOccupantId)
                .last("LIMIT " + peopleCount));
        
        // 执行 “挪人” 操作
        for (int i = 0; i < sourceBeds.size(); i++) {
            DormBed src = sourceBeds.get(i);
            DormBed tgt = targetEmptyBeds.get(i);
            
            // 移动学生 ID
            tgt.setOccupantId(src.getOccupantId());
            bedMapper.updateById(tgt);
            
            // 清空旧床位
            src.setOccupantId(null);
            bedMapper.updateById(src);
        }
        
        // 更新源房间状态：清零、封锁
        source.setCurrentNum(0);
        source.setStatus(0); // 0-维修/封寝
        this.updateById(source);
        
        // 更新目标房间人数
        target.setCurrentNum(target.getCurrentNum() + peopleCount);
        this.updateById(target);
        
        log.info("紧急转移完成: 从 [{}] 转移 {} 人到 [{}]", source.getRoomNo(), peopleCount, target.getRoomNo());
    }
    
    /**
     * 紧急腾退/封寝
     * <p>
     * 强制清空某房间的所有床位 (occupant_id 置空)，并将房间设为不可用状态。
     * 通常用于火灾、房屋结构损坏等极端情况。
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void evacuateRoom(Long roomId, String reason) {
        DormRoom room = this.getById(roomId);
        if (room == null) throw new ServiceException("房间不存在");
        
        // 1. 强制清空该房间所有床位的人员 (update dorm_bed set occupant_id = null where room_id = ?)
        bedMapper.update(null, Wrappers.<DormBed>lambdaUpdate()
                .eq(DormBed::getRoomId, roomId)
                .set(DormBed::getOccupantId, null));
        
        // 2. 更新房间状态，并封锁房间
        room.setCurrentNum(0);
        room.setStatus(0); // 0-维修/不可用
        this.updateById(room);
        
        log.warn("房间[{}]执行了紧急腾退，所有人员已被移出系统床位。原因：{}", room.getRoomNo(), reason);
    }
    
    
    // =========================== 5. 私有辅助方法 ===========================
    
    /**
     * 填充单条详情的人员信息
     */
    private void fillStudentInfo(List<DormBed> beds, DormRoomVO vo) {
        List<Long> ids = beds.stream().map(DormBed::getOccupantId).filter(Objects::nonNull).toList();
        Map<Long, SysOrdinaryUser> map = new HashMap<>();
        if (CollUtil.isNotEmpty(ids)) {
            List<SysOrdinaryUser> users = userMapper.selectByIds(ids);
            for (SysOrdinaryUser u : users) map.put(u.getId(), u);
        }
        List<DormRoomVO.BedInfo> list = new ArrayList<>();
        for (DormBed bed : beds) {
            DormRoomVO.BedInfo info = new DormRoomVO.BedInfo();
            info.setBedId(bed.getId());
            info.setBedLabel(bed.getBedLabel());
            info.setStudentId(bed.getOccupantId());
            if (bed.getOccupantId() != null) {
                SysOrdinaryUser u = map.get(bed.getOccupantId());
                if (u != null) {
                    info.setStudentName(u.getRealName());
                    info.setStudentNo(u.getUsername());
                }
            }
            list.add(info);
        }
        vo.setBedList(list);
    }
    
    /**
     * 初始化创建床位
     */
    private void createBeds(Long roomId, String roomNo, int count) {
        for (int i = 1; i <= count; i++) {
            DormBed bed = new DormBed();
            bed.setRoomId(roomId);
            bed.setBedLabel(roomNo + "-" + i);
            bedMapper.insert(bed);
        }
    }
    
    /**
     * 扩容：追加床位
     */
    private void addMoreBeds(Long roomId, String roomNo, int start, int end) {
        for (int i = start; i <= end; i++) {
            DormBed bed = new DormBed();
            bed.setRoomId(roomId);
            bed.setBedLabel(roomNo + "-" + i);
            bedMapper.insert(bed);
        }
    }
    
    /**
     * 缩容：删除多余的空床位
     */
    private void removeExcessBeds(Long roomId, int count) {
        List<DormBed> beds = bedMapper.selectList(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getRoomId, roomId).isNull(DormBed::getOccupantId)
                .orderByDesc(DormBed::getBedLabel).last("LIMIT " + count));
        if (beds.size() < count) throw new ServiceException("缩容失败：空床位不足");
        bedMapper.deleteByIds(beds.stream().map(DormBed::getId).toList());
    }
}