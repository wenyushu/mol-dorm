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
import com.mol.server.mapper.SysOrdinaryUserMapper;
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
 * 包含：房间增删改查、楼层批量操作、VO 组装、应急事务处理。
 * <p>
 * 核心原则：任何【删除】或【停用】操作，必须先校验【是否有人居住】。
 * 这是为了防止产生“孤儿数据”（即学生有床位号，但对应的房间/楼栋已不存在），
 * 保证系统数据的一致性和安全性。
 * </p>
 *
 * @author mol
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DormRoomServiceImpl extends ServiceImpl<DormRoomMapper, DormRoom> implements DormRoomService {
    
    // 注入床位 Mapper，用于操作床位数据 (DormBed)
    private final DormBedMapper bedMapper;
    // 注入系统用户 Mapper，用于跨模块查询学生姓名 (SysOrdinaryUser)
    private final SysOrdinaryUserMapper userMapper;
    
    // =========================== 1. 单个房间管理 (增删改) ===========================
    
    /**
     * 新增房间
     * <p>
     * 1. 校验必填项。
     * 2. 校验同一楼栋下房间号是否重复。
     * 3. 保存房间并自动生成配套床位。
     * </p>
     *
     * @param room 房间信息实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRoom(DormRoom room) {
        // 1. 基础参数校验
        // 楼栋ID和房间号是必须的，容量也不能为空
        if (room.getBuildingId() == null || StrUtil.isBlank(room.getRoomNo())) {
            throw new ServiceException("楼栋和房间号不能为空");
        }
        if (room.getCapacity() == null || room.getCapacity() <= 0) {
            throw new ServiceException("房间容量必须大于0");
        }
        
        // 2. 唯一性校验 (同一楼栋下房间号唯一)
        // 防止出现两个 "1号楼-101" 这种数据错误
        long count = this.count(new LambdaQueryWrapper<DormRoom>()
                .eq(DormRoom::getBuildingId, room.getBuildingId())
                .eq(DormRoom::getRoomNo, room.getRoomNo()));
        if (count > 0) {
            throw new ServiceException("该楼栋下已存在房间号：" + room.getRoomNo());
        }
        
        // 3. 初始化默认值并保存
        // 刚创建的房间人数肯定为 0，状态默认为 1 (正常)
        room.setCurrentNum(0);
        room.setStatus(1);
        this.save(room);
        
        // 4. 自动生成配套床位 (如 101-1, 101-2)
        // 这一步是为了减轻管理员负担，不需要再手动去创建床位
        createBeds(room.getId(), room.getRoomNo(), room.getCapacity());
    }
    
    /**
     * 修改房间信息 (带安全校验)
     * <p>
     * 核心逻辑：
     * 1. 封寝校验：如果修改状态为封寝，必须确保没人住。
     * 2. 查重校验：修改房间号不能和现有重复。
     * 3. 扩缩容逻辑：修改容量时，自动联动增删床位。
     * </p>
     *
     * @param room 包含修改后信息的房间实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoom(DormRoom room) {
        // 先查出旧数据，用于对比
        DormRoom oldRoom = this.getById(room.getId());
        if (oldRoom == null) {
            throw new ServiceException("房间不存在");
        }
        
        // ✅ 安全校验1：封寝安全检查
        // 如果状态改为 0 (停用/封寝)，且原来是正常的
        if (room.getStatus() != null && room.getStatus() == 0) {
            // 必须确保当前没人住，否则禁止封寝
            if (oldRoom.getCurrentNum() > 0) {
                throw new ServiceException("操作失败：该房间仍有 " + oldRoom.getCurrentNum() + " 人居住，请先清退人员！");
            }
        }
        
        // 校验2：修改房间号查重
        // 如果改了房间号，要检查新号码是不是已经有了
        if (!oldRoom.getRoomNo().equals(room.getRoomNo())) {
            long count = this.count(new LambdaQueryWrapper<DormRoom>()
                    .eq(DormRoom::getBuildingId, oldRoom.getBuildingId())
                    .eq(DormRoom::getRoomNo, room.getRoomNo())
                    .ne(DormRoom::getId, room.getId())); // 排除自己
            if (count > 0) {
                throw new ServiceException("新房间号已存在");
            }
        }
        
        // 校验3：容量变更逻辑 (扩容/缩容)
        Integer oldCap = oldRoom.getCapacity();
        Integer newCap = room.getCapacity();
        
        // 只有当新容量 != 旧容量时才触发
        if (newCap != null && !newCap.equals(oldCap)) {
            if (newCap < oldCap) {
                // --- 缩容逻辑 (变小) ---
                // 安全检查：如果当前实际居住人数 > 新容量，禁止操作，防止把住着的人“挤没了”
                if (oldRoom.getCurrentNum() > newCap) {
                    throw new ServiceException("缩容失败：当前居住人数(" + oldRoom.getCurrentNum() +
                            ")超过新容量(" + newCap + ")，请先移出部分学生");
                }
                // 调用私有方法，删除多余的空床位
                removeExcessBeds(room.getId(), oldCap - newCap);
            } else {
                // --- 扩容逻辑 (变大) ---
                // 调用私有方法，追加新床位
                addMoreBeds(room.getId(), room.getRoomNo(), oldCap + 1, newCap);
            }
        }
        
        // 最后执行 MyBatis-Plus 的更新操作
        this.updateById(room);
    }
    
    /**
     * 删除单个房间 (带安全校验)
     * <p>
     * 安全策略：只有空房间才能被删除。
     * </p>
     *
     * @param roomId 待删除的房间ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoom(Long roomId) {
        DormRoom room = this.getById(roomId);
        if (room == null) return;
        
        // ✅ 安全校验：有人绝对不能删
        if (room.getCurrentNum() > 0) {
            throw new ServiceException("删除失败：该房间仍有 " + room.getCurrentNum() + " 人居住！");
        }
        
        // 1. 级联删除：先删关联的空床位
        bedMapper.delete(new LambdaQueryWrapper<DormBed>().eq(DormBed::getRoomId, roomId));
        // 2. 删除房间本身
        this.removeById(roomId);
    }
    
    // =========================== 2. 楼层批量操作 (核心新增) ===========================
    
    /**
     * 停用整层楼
     * <p>
     * 场景：某层楼水管爆裂或装修，需要批量封锁。
     * </p>
     *
     * @param buildingId 楼栋ID
     * @param floor      楼层号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableFloor(Long buildingId, Integer floor) {
        // 1. 检查该层是否有人居住 (只要有一间房有人，就报错)
        // 🔴 修复点：使用 getFloorNo 匹配实体类字段
        Long occupiedCount = this.baseMapper.selectCount(new LambdaQueryWrapper<DormRoom>()
                .eq(DormRoom::getBuildingId, buildingId)
                .eq(DormRoom::getFloorNo, floor)
                .gt(DormRoom::getCurrentNum, 0));
        
        if (occupiedCount > 0) {
            throw new ServiceException("停用失败：该楼层仍有 " + occupiedCount + " 间房有人居住！");
        }
        
        // 2. 批量更新状态为 0 (停用)
        DormRoom updateEntity = new DormRoom();
        updateEntity.setStatus(0);
        
        // 🔴 修复点：使用 getFloorNo
        this.update(updateEntity, new LambdaQueryWrapper<DormRoom>()
                .eq(DormRoom::getBuildingId, buildingId)
                .eq(DormRoom::getFloorNo, floor));
        
        log.info("楼层停用成功：楼栋ID={}, 楼层={}", buildingId, floor);
    }
    
    /**
     * 删除整层楼
     * <p>
     * 场景：楼层规划变更，物理拆除。
     * </p>
     *
     * @param buildingId 楼栋ID
     * @param floor      楼层号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFloor(Long buildingId, Integer floor) {
        // 1. 检查该层是否有人
        // 🔴 修复点：使用 getFloorNo
        Long occupiedCount = this.baseMapper.selectCount(new LambdaQueryWrapper<DormRoom>()
                .eq(DormRoom::getBuildingId, buildingId)
                .eq(DormRoom::getFloorNo, floor)
                .gt(DormRoom::getCurrentNum, 0));
        
        if (occupiedCount > 0) {
            throw new ServiceException("删除失败：该楼层仍有 " + occupiedCount + " 间房有人居住！");
        }
        
        // 2. 查出该层所有房间ID (用于后续删床位)
        // 🔴 修复点：使用 getFloorNo
        List<DormRoom> rooms = this.list(new LambdaQueryWrapper<DormRoom>()
                .select(DormRoom::getId)
                .eq(DormRoom::getBuildingId, buildingId)
                .eq(DormRoom::getFloorNo, floor));
        
        if (CollUtil.isEmpty(rooms)) return;
        // 提取 ID 列表
        List<Long> roomIds = rooms.stream().map(DormRoom::getId).collect(Collectors.toList());
        
        // 3. 级联删除所有床位
        // DELETE FROM dorm_bed WHERE room_id IN (1, 2, 3...)
        bedMapper.delete(new LambdaQueryWrapper<DormBed>().in(DormBed::getRoomId, roomIds));
        
        // 4. 级联删除所有房间 (使用新版 removeByIds)
        this.removeByIds(roomIds);
        
        log.info("楼层删除成功：楼栋ID={}, 楼层={}, 共删除房间 {} 间", buildingId, floor, roomIds.size());
    }
    
    // =========================== 3. 高级查询 (VO封装) ===========================
    
    /**
     * 获取单个房间详情 (含人名)
     *
     * @param roomId 房间ID
     * @return VO 对象，包含床位列表和学生姓名
     */
    @Override
    public DormRoomVO getRoomDetail(Long roomId) {
        DormRoom room = this.getById(roomId);
        if (room == null) return null;
        
        // 转换 Entity 为 VO
        DormRoomVO vo = new DormRoomVO();
        BeanUtils.copyProperties(room, vo);
        
        // 查询该房间的所有床位，按床号排序
        List<DormBed> beds = bedMapper.selectList(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getRoomId, roomId)
                .orderByAsc(DormBed::getBedLabel));
        
        // 填充人员信息 (调用辅助方法)
        fillStudentInfo(beds, vo);
        return vo;
    }
    
    /**
     * 分页查询房间列表 (VO增强版)
     * <p>
     * 解决 N+1 问题：
     * 1. 先查出当前页的房间列表。
     * 2. 提取所有房间ID，一次性查出所有床位。
     * 3. 提取所有学生ID，一次性查出所有学生姓名。
     * 4. 在内存中进行组装。
     * </p>
     */
    @Override
    public Page<DormRoomVO> getRoomVoPage(Page<DormRoom> page, Long buildingId) {
        // 1. 查房间分页数据
        // 🔴 修复点：使用 getFloorNo
        Page<DormRoom> roomPage = this.page(page, new LambdaQueryWrapper<DormRoom>()
                .eq(DormRoom::getBuildingId, buildingId)
                .orderByAsc(DormRoom::getFloorNo) // 先按楼层排
                .orderByAsc(DormRoom::getRoomNo)); // 再按房号排
        
        if (CollUtil.isEmpty(roomPage.getRecords())) {
            return new Page<>(page.getCurrent(), page.getSize(), 0);
        }
        
        // 2. 提取房间ID列表，批量查床位
        List<Long> roomIds = roomPage.getRecords().stream().map(DormRoom::getId).collect(Collectors.toList());
        List<DormBed> allBeds = bedMapper.selectList(new LambdaQueryWrapper<DormBed>()
                .in(DormBed::getRoomId, roomIds)
                .orderByAsc(DormBed::getBedLabel));
        
        // 3. 提取居住人ID列表，批量查学生
        Set<Long> studentIds = allBeds.stream()
                .map(DormBed::getOccupantId)
                .filter(Objects::nonNull) // 过滤掉空床位
                .collect(Collectors.toSet());
        
        Map<Long, SysOrdinaryUser> studentMap = new HashMap<>();
        if (CollUtil.isNotEmpty(studentIds)) {
            // 使用 selectByIds 批量查询
            List<SysOrdinaryUser> students = userMapper.selectByIds(studentIds);
            // 转为 Map 方便后续查找 (key: userId, value: User对象)
            for (SysOrdinaryUser s : students) studentMap.put(s.getId(), s);
        }
        
        // 4. 内存组装数据 (将床位按房间ID分组)
        Map<Long, List<DormBed>> roomBedMap = allBeds.stream().collect(Collectors.groupingBy(DormBed::getRoomId));
        
        // 遍历房间列表，组装 VO
        List<DormRoomVO> voList = roomPage.getRecords().stream().map(room -> {
            DormRoomVO vo = new DormRoomVO();
            BeanUtils.copyProperties(room, vo);
            
            // 获取属于该房间的床位
            List<DormBed> myBeds = roomBedMap.getOrDefault(room.getId(), Collections.emptyList());
            
            // 转换床位信息，填入学生姓名
            List<DormRoomVO.BedInfo> bedInfos = myBeds.stream().map(bed -> {
                DormRoomVO.BedInfo info = new DormRoomVO.BedInfo();
                info.setBedId(bed.getId());
                info.setBedLabel(bed.getBedLabel());
                info.setStudentId(bed.getOccupantId());
                
                // 如果有人住，从 Map 里取名字
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
    
    // =========================== 4. 应急处理 ===========================
    
    /**
     * 紧急转移人员
     * <p>
     * 将源房间 (source) 的所有居住人员，批量移动到目标房间 (target) 的空床位上。
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void emergencyTransfer(Long sourceRoomId, Long targetRoomId) {
        DormRoom source = this.getById(sourceRoomId);
        DormRoom target = this.getById(targetRoomId);
        
        if (source == null || target == null) throw new ServiceException("房间不存在");
        if (target.getStatus() != null && target.getStatus() == 0) {
            throw new ServiceException("目标房间不可用");
        }
        
        // 容量检查
        int peopleCount = source.getCurrentNum();
        int targetAvailable = target.getCapacity() - target.getCurrentNum();
        if (peopleCount > targetAvailable) {
            throw new ServiceException("目标房间床位不足");
        }
        
        if (peopleCount == 0) {
            source.setStatus(0); // 没人住直接封源房间
            this.updateById(source);
            return;
        }
        
        // 获取源房间有人的床位
        List<DormBed> sourceBeds = bedMapper.selectList(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getRoomId, sourceRoomId).isNotNull(DormBed::getOccupantId));
        
        // 获取目标房间的空床位
        List<DormBed> targetEmptyBeds = bedMapper.selectList(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getRoomId, targetRoomId).isNull(DormBed::getOccupantId).last("LIMIT " + peopleCount));
        
        // 执行“挪人”
        for (int i = 0; i < sourceBeds.size(); i++) {
            DormBed src = sourceBeds.get(i);
            DormBed tgt = targetEmptyBeds.get(i);
            
            // 移动学生ID到新床
            tgt.setOccupantId(src.getOccupantId());
            bedMapper.updateById(tgt);
            
            // 清空旧床
            src.setOccupantId(null);
            bedMapper.updateById(src);
        }
        
        // 更新状态
        source.setCurrentNum(0);
        source.setStatus(0); // 源房间封锁
        this.updateById(source);
        
        target.setCurrentNum(target.getCurrentNum() + peopleCount); // 目标房间人数增加
        this.updateById(target);
    }
    
    /**
     * 紧急腾退/封寝
     * <p>
     * 强制清空某房间的所有床位 (occupant_id 置空)，并将房间设为不可用状态。
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void evacuateRoom(Long roomId, String reason) {
        DormRoom room = this.getById(roomId);
        if (room == null) throw new ServiceException("房间不存在");
        
        // 1. 强制清空该房间所有床位的人员
        bedMapper.update(null, Wrappers.<DormBed>lambdaUpdate()
                .eq(DormBed::getRoomId, roomId).set(DormBed::getOccupantId, null));
        
        // 2. 更新房间状态
        room.setCurrentNum(0);
        room.setStatus(0); // 0-维修/不可用
        this.updateById(room);
        
        log.warn("房间[{}]执行紧急腾退，原因：{}", room.getRoomNo(), reason);
    }
    
    // =========================== 5. 私有辅助方法 ===========================
    
    /**
     * 辅助方法：为单个房间详情填充学生信息
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
     * 批量创建床位 (新增房间时调用)
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
     * 扩容：追加新床位
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
        // 优先删除床位号较大的空床 (如 101-4)
        List<DormBed> beds = bedMapper.selectList(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getRoomId, roomId).isNull(DormBed::getOccupantId)
                .orderByDesc(DormBed::getBedLabel).last("LIMIT " + count));
        
        if (beds.size() < count) {
            throw new ServiceException("缩容失败：空床位不足，请先检查是否有人居住");
        }
        
        // 批量删除
        // ⚠️ 修复点：deleteBatchIds -> deleteByIds
        bedMapper.deleteByIds(beds.stream().map(DormBed::getId).toList());
    }
}