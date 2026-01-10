package com.mol.dorm.biz.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.mapper.DormBedMapper;
import com.mol.dorm.biz.mapper.DormRoomMapper;
import com.mol.dorm.biz.service.DormRoomService;
import com.mol.dorm.biz.vo.DormRoomVo;
import com.mol.sys.biz.mapper.SysOrdinaryUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DormRoomServiceImpl extends ServiceImpl<DormRoomMapper, DormRoom> implements DormRoomService {
    
    private final DormBedMapper bedMapper;
    private final SysOrdinaryUserMapper userMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRoom(DormRoom room) {
        if (room.getBuildingId() == null || StrUtil.isBlank(room.getRoomNo())) {
            throw new ServiceException("楼栋和房间号不能为空");
        }
        if (room.getCapacity() == null || room.getCapacity() <= 0) {
            throw new ServiceException("房间容量必须大于0");
        }
        
        long count = this.count(new LambdaQueryWrapper<DormRoom>()
                .eq(DormRoom::getBuildingId, room.getBuildingId())
                .eq(DormRoom::getRoomNo, room.getRoomNo()));
        if (count > 0) {
            throw new ServiceException("该楼栋下已存在房间号：" + room.getRoomNo());
        }
        
        room.setCurrentNum(0);
        room.setStatus(1);
        this.save(room);
        
        createBeds(room.getId(), room.getRoomNo(), room.getCapacity());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoom(DormRoom room) {
        DormRoom oldRoom = this.getById(room.getId());
        if (oldRoom == null) throw new ServiceException("房间不存在");
        
        if (!oldRoom.getRoomNo().equals(room.getRoomNo())) {
            long count = this.count(new LambdaQueryWrapper<DormRoom>()
                    .eq(DormRoom::getBuildingId, oldRoom.getBuildingId())
                    .eq(DormRoom::getRoomNo, room.getRoomNo())
                    .ne(DormRoom::getId, room.getId()));
            if (count > 0) throw new ServiceException("新房间号已存在");
        }
        
        Integer oldCap = oldRoom.getCapacity();
        Integer newCap = room.getCapacity();
        
        if (newCap != null && !newCap.equals(oldCap)) {
            if (newCap < oldCap) {
                if (oldRoom.getCurrentNum() > newCap) {
                    throw new ServiceException("缩容失败：当前居住人数已超标，请先移出部分学生");
                }
                removeExcessBeds(room.getId(), oldCap - newCap);
            } else {
                addMoreBeds(room.getId(), room.getRoomNo(), oldCap + 1, newCap);
            }
        }
        this.updateById(room);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoom(Long roomId) {
        DormRoom room = this.getById(roomId);
        if (room == null) return;
        
        if (room.getCurrentNum() > 0) {
            throw new ServiceException("删除失败：该房间仍有人居住");
        }
        
        bedMapper.delete(new LambdaQueryWrapper<DormBed>().eq(DormBed::getRoomId, roomId));
        this.removeById(roomId);
    }
    
    @Override
    public DormRoomVo getRoomDetail(Long roomId) {
        DormRoom room = this.getById(roomId);
        if (room == null) return null;
        
        DormRoomVo vo = new DormRoomVo();
        BeanUtils.copyProperties(room, vo);
        
        List<DormBed> beds = bedMapper.selectList(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getRoomId, roomId)
                .orderByAsc(DormBed::getBedLabel));
        
        List<Long> studentIds = beds.stream()
                .map(DormBed::getOccupantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        Map<Long, SysOrdinaryUser> studentMap = null;
        if (CollUtil.isNotEmpty(studentIds)) {
            // 修复点：selectBatchIds -> selectByIds
            List<SysOrdinaryUser> students = userMapper.selectByIds(studentIds);
            studentMap = students.stream().collect(Collectors.toMap(SysOrdinaryUser::getId, s -> s));
        }
        
        List<DormRoomVo.BedInfo> bedInfos = new ArrayList<>();
        for (DormBed bed : beds) {
            DormRoomVo.BedInfo info = new DormRoomVo.BedInfo();
            info.setBedId(bed.getId());
            info.setBedLabel(bed.getBedLabel()); // 这里的红线应该随着第一步 DormBed 的修改而消失
            info.setStudentId(bed.getOccupantId());
            
            if (bed.getOccupantId() != null && studentMap != null) {
                SysOrdinaryUser student = studentMap.get(bed.getOccupantId());
                if (student != null) {
                    info.setStudentName(student.getRealName());
                    info.setStudentNo(student.getUsername());
                }
            }
            bedInfos.add(info);
        }
        vo.setBedList(bedInfos);
        return vo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void emergencyTransfer(Long sourceRoomId, Long targetRoomId) {
        DormRoom source = this.getById(sourceRoomId);
        DormRoom target = this.getById(targetRoomId);
        
        if (source == null || target == null) throw new ServiceException("房间不存在");
        if (sourceRoomId.equals(targetRoomId)) throw new ServiceException("目标房间不能与源房间相同");
        
        if (target.getStatus() != null && target.getStatus() == 0) {
            throw new ServiceException("目标房间不可用");
        }
        
        int peopleToMove = source.getCurrentNum();
        int targetAvailable = target.getCapacity() - target.getCurrentNum();
        if (peopleToMove > targetAvailable) {
            throw new ServiceException("目标房间床位不足");
        }
        
        if (peopleToMove == 0) {
            source.setStatus(0);
            this.updateById(source);
            return;
        }
        
        List<DormBed> sourceBeds = bedMapper.selectList(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getRoomId, sourceRoomId)
                .isNotNull(DormBed::getOccupantId));
        
        List<DormBed> targetEmptyBeds = bedMapper.selectList(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getRoomId, targetRoomId)
                .isNull(DormBed::getOccupantId)
                .last("LIMIT " + peopleToMove));
        
        for (int i = 0; i < sourceBeds.size(); i++) {
            DormBed srcBed = sourceBeds.get(i);
            DormBed tgtBed = targetEmptyBeds.get(i);
            tgtBed.setOccupantId(srcBed.getOccupantId());
            bedMapper.updateById(tgtBed);
            srcBed.setOccupantId(null);
            bedMapper.updateById(srcBed);
        }
        
        source.setCurrentNum(0);
        source.setStatus(0);
        this.updateById(source);
        
        target.setCurrentNum(target.getCurrentNum() + peopleToMove);
        this.updateById(target);
    }
    
    private void createBeds(Long roomId, String roomNo, int count) {
        for (int i = 1; i <= count; i++) {
            DormBed bed = new DormBed();
            bed.setRoomId(roomId);
            bed.setBedLabel(roomNo + "-" + i); // 确保 DormBed 有 setBedLabel 方法
            bedMapper.insert(bed);
        }
    }
    
    private void addMoreBeds(Long roomId, String roomNo, int startSeq, int endSeq) {
        for (int i = startSeq; i <= endSeq; i++) {
            DormBed bed = new DormBed();
            bed.setRoomId(roomId);
            bed.setBedLabel(roomNo + "-" + i);
            bedMapper.insert(bed);
        }
    }
    
    private void removeExcessBeds(Long roomId, int countToRemove) {
        List<DormBed> emptyBeds = bedMapper.selectList(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getRoomId, roomId)
                .isNull(DormBed::getOccupantId)
                .orderByDesc(DormBed::getBedLabel) // 依赖 bedLabel 字段
                .last("LIMIT " + countToRemove));
        
        if (emptyBeds.size() < countToRemove) {
            throw new ServiceException("缩容失败：空床位不足");
        }
        
        List<Long> idsToDelete = emptyBeds.stream().map(DormBed::getId).collect(Collectors.toList());
        // 修复点：deleteBatchIds -> deleteByIds
        bedMapper.deleteByIds(idsToDelete);
    }
}