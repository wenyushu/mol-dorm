package com.mol.dorm.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.entity.SysAdminUser;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.DormFloor;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.mapper.DormBedMapper;
import com.mol.dorm.biz.mapper.DormFloorMapper;
import com.mol.dorm.biz.mapper.DormRoomMapper;
import com.mol.dorm.biz.service.DormRoomService;
import com.mol.dorm.biz.vo.DormRoomVO;
import com.mol.server.mapper.SysAdminUserMapper;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 宿舍房间业务核心实现类 (终极防刁民 + 混合居住版)
 * <p>
 * 核心职责：
 * 1. 维护房间生命周期，确保数据一致性。
 * 2. 处理混合居住逻辑 (学生+教职工)。
 * 3. 执行严格的业务规则拦截 (防误删、防违规分配)。
 * </p>
 *
 * @author mol
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DormRoomServiceImpl extends ServiceImpl<DormRoomMapper, DormRoom> implements DormRoomService {
    
    private final DormFloorMapper floorMapper;
    private final DormBedMapper bedMapper;
    
    // 注入两张用户表的 Mapper，用于混合居住查询
    private final SysOrdinaryUserMapper ordinaryUserMapper; // 学生
    private final SysAdminUserMapper adminUserMapper;       // 教工/宿管
    
    // =================================================================================================
    // 1. 单个房间管理 (增删改)
    // =================================================================================================
    
    /**
     * 新增房间
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRoom(DormRoom room) {
        // --- 1. 参数防御 ---
        if (room.getFloorId() == null) throw new ServiceException("必须指定所属楼层");
        if (StrUtil.isBlank(room.getRoomNo())) throw new ServiceException("房间号不能为空");
        if (room.getCapacity() == null || room.getCapacity() <= 0) throw new ServiceException("房间容量必须大于0");
        if (StrUtil.isBlank(room.getGender())) throw new ServiceException("必须指定房间性别限制");
        
        // --- 2. 上级状态校验 ---
        DormFloor floor = floorMapper.selectById(room.getFloorId());
        if (floor == null) throw new ServiceException("防刁民拦截：所属楼层不存在");
        
        // 🛡️ 状态拦截：楼层停用(0)或装修(41)时，禁止操作
        if (floor.getStatus() == 0 || floor.getStatus() == 41) {
            throw new ServiceException("操作拦截：所属楼层已停用或正在装修，禁止新增房间");
        }
        
        // --- 3. 性别熔断机制 ---
        // Floor(Int): 1-男, 2-女 | Room(Str): "1"-男, "0"-女
        if (floor.getGenderLimit() == 1 && "0".equals(room.getGender())) {
            throw new ServiceException("规则拦截：[男层] 禁止创建 [女寝]");
        }
        if (floor.getGenderLimit() == 2 && "1".equals(room.getGender())) {
            throw new ServiceException("规则拦截：[女层] 禁止创建 [男寝]");
        }
        
        // --- 4. 唯一性查重 ---
        boolean exists = this.exists(new LambdaQueryWrapper<DormRoom>()
                .eq(DormRoom::getFloorId, floor.getId())
                .eq(DormRoom::getRoomNo, room.getRoomNo()));
        if (exists) {
            throw new ServiceException("该楼层已存在房间号：" + room.getRoomNo());
        }
        
        // --- 5. 全链路冗余填充 ---
        room.setCampusId(floor.getCampusId());
        room.setBuildingId(floor.getBuildingId());
        room.setFloorNo(floor.getFloorNum());
        
        // --- 6. 初始化并保存 ---
        room.setCurrentNum(0);
        room.setStatus(10); // 10-正常(未满)
        this.save(room);
        
        // --- 7. 级联创建床位 ---
        createBeds(room, room.getCapacity());
    }
    
    /**
     * 修改房间
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoom(DormRoom room) {
        DormRoom oldRoom = this.getById(room.getId());
        if (oldRoom == null) throw new ServiceException("房间不存在");
        
        // ✅ 防刁民：有人住时禁止封寝
        if (isStopStatus(room.getStatus()) && oldRoom.getCurrentNum() > 0) {
            throw new ServiceException("操作失败：房间仍有人员居住，请先清退人员再执行封停/维修操作！");
        }
        
        // 校验：房间号查重
        if (StrUtil.isNotBlank(room.getRoomNo()) && !oldRoom.getRoomNo().equals(room.getRoomNo())) {
            boolean exists = this.exists(new LambdaQueryWrapper<DormRoom>()
                    .eq(DormRoom::getFloorId, oldRoom.getFloorId())
                    .eq(DormRoom::getRoomNo, room.getRoomNo())
                    .ne(DormRoom::getId, room.getId()));
            if (exists) throw new ServiceException("新房间号已存在");
        }
        
        // 校验：容量变更 (扩缩容)
        Integer oldCap = oldRoom.getCapacity();
        Integer newCap = room.getCapacity();
        
        if (newCap != null && !newCap.equals(oldCap)) {
            if (newCap < oldCap) {
                // 缩容：先检查人会不会被挤出去
                if (oldRoom.getCurrentNum() > newCap) {
                    throw new ServiceException("缩容失败：当前人数(" + oldRoom.getCurrentNum() +
                            ") > 新容量(" + newCap + ")，请先移出部分人员");
                }
                removeExcessBeds(room.getId(), oldCap - newCap);
            } else {
                // 扩容
                addMoreBeds(oldRoom, oldCap + 1, newCap);
            }
        }
        
        this.updateById(room);
    }
    
    /**
     * 删除房间
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoom(Long roomId) {
        DormRoom room = this.getById(roomId);
        if (room == null) return;
        
        // ✅ 防孤儿：检查是否有“已入住”的床位
        Long occupiedBeds = bedMapper.selectCount(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getRoomId, roomId)
                .eq(DormBed::getStatus, 1)); // 1-已入住
        
        if (occupiedBeds > 0) {
            throw new ServiceException("删除失败：房间内仍有人员居住，禁止删除！");
        }
        
        // 级联删除空床位
        bedMapper.delete(new LambdaQueryWrapper<DormBed>().eq(DormBed::getRoomId, roomId));
        
        // 删除房间
        this.removeById(roomId);
    }
    
    // =================================================================================================
    // 2. 楼层批量操作
    // =================================================================================================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableFloor(Long buildingId, Integer floorNo) {
        // 检查是否有人
        Long occupiedCount = this.baseMapper.selectCount(new LambdaQueryWrapper<DormRoom>()
                .eq(DormRoom::getBuildingId, buildingId)
                .eq(DormRoom::getFloorNo, floorNo)
                .gt(DormRoom::getCurrentNum, 0));
        
        if (occupiedCount > 0) {
            throw new ServiceException("停用失败：该楼层仍有 " + occupiedCount + " 间房有人居住！");
        }
        
        // 批量置为 40-维修停用
        DormRoom updateEntity = new DormRoom();
        updateEntity.setStatus(40);
        this.update(updateEntity, new LambdaQueryWrapper<DormRoom>()
                .eq(DormRoom::getBuildingId, buildingId)
                .eq(DormRoom::getFloorNo, floorNo));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFloor(Long buildingId, Integer floorNo) {
        // 检查是否有人
        Long occupiedCount = this.baseMapper.selectCount(new LambdaQueryWrapper<DormRoom>()
                .eq(DormRoom::getBuildingId, buildingId)
                .eq(DormRoom::getFloorNo, floorNo)
                .gt(DormRoom::getCurrentNum, 0));
        
        if (occupiedCount > 0) {
            throw new ServiceException("删除失败：该楼层仍有 " + occupiedCount + " 间房有人居住！");
        }
        
        // 查ID -> 删床 -> 删房
        List<DormRoom> rooms = this.list(new LambdaQueryWrapper<DormRoom>()
                .select(DormRoom::getId)
                .eq(DormRoom::getBuildingId, buildingId)
                .eq(DormRoom::getFloorNo, floorNo));
        
        if (CollUtil.isEmpty(rooms)) return;
        List<Long> roomIds = rooms.stream().map(DormRoom::getId).collect(Collectors.toList());
        
        bedMapper.delete(new LambdaQueryWrapper<DormBed>().in(DormBed::getRoomId, roomIds));
        this.removeByIds(roomIds);
    }
    
    // =================================================================================================
    // 3. 高级查询 (支持混合居住 VO)
    // =================================================================================================
    
    @Override
    public DormRoomVO getRoomDetail(Long roomId) {
        DormRoom room = this.getById(roomId);
        if (room == null) return null;
        
        DormRoomVO vo = new DormRoomVO();
        BeanUtils.copyProperties(room, vo);
        
        // 查床位 (按物理方位排序)
        List<DormBed> beds = bedMapper.selectList(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getRoomId, roomId)
                .orderByAsc(DormBed::getSortOrder));
        
        // 填充人员信息 (改用 Occupant 逻辑)
        fillOccupantInfo(beds, vo);
        return vo;
    }
    
    @Override
    public Page<DormRoomVO> getRoomVoPage(Page<DormRoom> page, Long buildingId) {
        // 1. 分页查房
        Page<DormRoom> roomPage = this.page(page, new LambdaQueryWrapper<DormRoom>()
                .eq(DormRoom::getBuildingId, buildingId)
                .orderByAsc(DormRoom::getFloorNo)
                .orderByAsc(DormRoom::getRoomNo));
        
        if (CollUtil.isEmpty(roomPage.getRecords())) {
            return new Page<>(page.getCurrent(), page.getSize(), 0);
        }
        
        // 2. 批量查床位
        List<Long> roomIds = roomPage.getRecords().stream().map(DormRoom::getId).collect(Collectors.toList());
        List<DormBed> allBeds = bedMapper.selectList(new LambdaQueryWrapper<DormBed>()
                .in(DormBed::getRoomId, roomIds)
                .orderByAsc(DormBed::getSortOrder));
        
        // 3. 内存分组
        Map<Long, List<DormBed>> roomBedMap = allBeds.stream().collect(Collectors.groupingBy(DormBed::getRoomId));
        
        // 4. 提取所有人员ID (需区分类型)
        // 这一步比较复杂，我们放在 fillOccupantInfo 的批量逻辑里处理，
        // 但为了分页查询性能，我们需要把所有涉及的床位一起传进去处理，或者在这里预处理。
        // 为了代码复用，我们在下面独立写一个 "批量填充" 的逻辑。
        
        // 此处为了逻辑简单，循环调用单次填充逻辑 (性能略有损耗但逻辑清晰)
        // 优化方案：写一个 batchFillOccupantInfo，这里演示单次调用的结构
        List<DormRoomVO> voList = roomPage.getRecords().stream().map(room -> {
            DormRoomVO vo = new DormRoomVO();
            BeanUtils.copyProperties(room, vo);
            
            List<DormBed> myBeds = roomBedMap.getOrDefault(room.getId(), Collections.emptyList());
            fillOccupantInfo(myBeds, vo); // 复用填充逻辑
            
            return vo;
        }).collect(Collectors.toList());
        
        Page<DormRoomVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), roomPage.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }
    
    // =================================================================================================
    // 4. 应急处理
    // =================================================================================================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void emergencyTransfer(Long sourceRoomId, Long targetRoomId) {
        DormRoom source = this.getById(sourceRoomId);
        DormRoom target = this.getById(targetRoomId);
        
        if (source == null || target == null) throw new ServiceException("房间不存在");
        
        // 目标必须可用 (10/20)
        if (target.getStatus() >= 40) throw new ServiceException("目标房间不可用");
        
        int peopleCount = source.getCurrentNum();
        int targetAvailable = target.getCapacity() - target.getCurrentNum();
        if (peopleCount > targetAvailable) throw new ServiceException("目标房间床位不足");
        
        if (peopleCount > 0) {
            // 源已住
            List<DormBed> sourceBeds = bedMapper.selectList(new LambdaQueryWrapper<DormBed>()
                    .eq(DormBed::getRoomId, sourceRoomId)
                    .eq(DormBed::getStatus, 1));
            // 目标空闲
            List<DormBed> targetEmptyBeds = bedMapper.selectList(new LambdaQueryWrapper<DormBed>()
                    .eq(DormBed::getRoomId, targetRoomId)
                    .eq(DormBed::getStatus, 0)
                    .orderByAsc(DormBed::getSortOrder)
                    .last("LIMIT " + peopleCount));
            
            for (int i = 0; i < sourceBeds.size(); i++) {
                DormBed src = sourceBeds.get(i);
                DormBed tgt = targetEmptyBeds.get(i);
                // 完整迁移数据
                tgt.setOccupantId(src.getOccupantId());
                tgt.setOccupantType(src.getOccupantType()); // ✅ 迁移类型
                tgt.setStatus(1);
                
                src.setOccupantId(null);
                src.setOccupantType(null);
                src.setStatus(0);
                
                bedMapper.updateById(tgt);
                bedMapper.updateById(src);
            }
        }
        
        // 更新状态
        source.setCurrentNum(0);
        source.setStatus(40);
        this.updateById(source);
        
        target.setCurrentNum(target.getCurrentNum() + peopleCount);
        if (target.getCurrentNum() >= target.getCapacity()) target.setStatus(20);
        this.updateById(target);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void evacuateRoom(Long roomId, String reason) {
        DormRoom room = this.getById(roomId);
        if (room == null) throw new ServiceException("房间不存在");
        
        bedMapper.update(null, Wrappers.<DormBed>lambdaUpdate()
                .eq(DormBed::getRoomId, roomId)
                .set(DormBed::getOccupantId, null)
                .set(DormBed::getOccupantType, null)
                .set(DormBed::getStatus, 0));
        
        room.setCurrentNum(0);
        room.setStatus(42); // 42-损坏
        this.updateById(room);
        log.warn("🚨 房间[{}] 紧急腾退，原因：{}", room.getRoomNo(), reason);
    }
    
    // =================================================================================================
    // 5. 私有辅助方法 (核心逻辑)
    // =================================================================================================
    
    private boolean isStopStatus(Integer status) {
        return status != null && status >= 40;
    }
    
    /**
     * 核心方法：填充床位入住者信息 (支持学生+教工)
     */
    private void fillOccupantInfo(List<DormBed> beds, DormRoomVO vo) {
        if (CollUtil.isEmpty(beds)) {
            vo.setBedList(Collections.emptyList());
            return;
        }
        
        // 1. 分离 ID：学生 vs 教工
        List<Long> studentIds = new ArrayList<>();
        List<Long> teacherIds = new ArrayList<>();
        
        for (DormBed bed : beds) {
            if (bed.getOccupantId() != null && bed.getOccupantType() != null) {
                if (bed.getOccupantType() == 0) {
                    studentIds.add(bed.getOccupantId());
                } else if (bed.getOccupantType() == 1) {
                    teacherIds.add(bed.getOccupantId());
                }
            }
        }
        
        // 2. 批量查询学生
        Map<Long, SysOrdinaryUser> studentMap = new HashMap<>();
        if (CollUtil.isNotEmpty(studentIds)) {
            List<SysOrdinaryUser> students = ordinaryUserMapper.selectByIds(studentIds);
            for (SysOrdinaryUser s : students) studentMap.put(s.getId(), s);
        }
        
        // 3. 批量查询教工
        Map<Long, SysAdminUser> teacherMap = new HashMap<>();
        if (CollUtil.isNotEmpty(teacherIds)) {
            List<SysAdminUser> teachers = adminUserMapper.selectByIds(teacherIds);
            for (SysAdminUser t : teachers) teacherMap.put(t.getId(), t);
        }
        
        // 4. 组装 BedInfo
        List<DormRoomVO.BedInfo> list = new ArrayList<>();
        for (DormBed bed : beds) {
            DormRoomVO.BedInfo info = new DormRoomVO.BedInfo();
            info.setBedId(bed.getId());
            info.setBedLabel(bed.getBedLabel());
            info.setSortOrder(bed.getSortOrder());
            
            // 填充通用字段
            Long uid = bed.getOccupantId();
            Integer type = bed.getOccupantType();
            
            info.setOccupantId(uid);
            info.setOccupantType(type);
            
            if (uid != null && type != null) {
                if (type == 0) { // 学生
                    SysOrdinaryUser s = studentMap.get(uid);
                    if (s != null) {
                        info.setOccupantName(s.getRealName());
                        info.setOccupantNo(s.getUsername()); // 假设 username 是学号
                    }
                } else if (type == 1) { // 教工
                    SysAdminUser t = teacherMap.get(uid);
                    if (t != null) {
                        info.setOccupantName(t.getRealName());
                        info.setOccupantNo(t.getUsername()); // 假设 username 是工号
                    }
                }
            }
            list.add(info);
        }
        vo.setBedList(list);
    }
    
    private void createBeds(DormRoom room, int count) {
        for (int i = 1; i <= count; i++) {
            DormBed bed = new DormBed();
            bed.setCampusId(room.getCampusId());
            bed.setBuildingId(room.getBuildingId());
            bed.setFloorId(room.getFloorId());
            bed.setRoomId(room.getId());
            bed.setBedLabel(room.getRoomNo() + "-" + i);
            bed.setSortOrder(i);
            bed.setStatus(0);
            bedMapper.insert(bed);
        }
    }
    
    private void addMoreBeds(DormRoom room, int start, int end) {
        for (int i = start; i <= end; i++) {
            DormBed bed = new DormBed();
            bed.setCampusId(room.getCampusId());
            bed.setBuildingId(room.getBuildingId());
            bed.setFloorId(room.getFloorId());
            bed.setRoomId(room.getId());
            bed.setBedLabel(room.getRoomNo() + "-" + i);
            bed.setSortOrder(i);
            bed.setStatus(0);
            bedMapper.insert(bed);
        }
    }
    
    private void removeExcessBeds(Long roomId, int count) {
        List<DormBed> beds = bedMapper.selectList(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getRoomId, roomId)
                .isNull(DormBed::getOccupantId)
                .orderByDesc(DormBed::getSortOrder)
                .last("LIMIT " + count));
        
        if (beds.size() < count) {
            throw new ServiceException("缩容失败：空床位不足");
        }
        
        // 推荐写法：使用 deleteByIds 替代 deleteBatchIds
        List<Long> ids = beds.stream().map(DormBed::getId).collect(Collectors.toList());
        bedMapper.deleteByIds(ids);
    }
}