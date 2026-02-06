package com.mol.dorm.biz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.mapper.DormBedMapper;
import com.mol.dorm.biz.service.DormBedService;
import com.mol.dorm.biz.service.DormRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 床位资源服务实现类
 * 🛡️ [防刁民设计手册]：
 * 1. 唯一性死守：利用 checkUserHasBed 确保全校“一人一床”。
 * 2. 高并发 CAS：通过 version 字段实现乐观锁，解决管理员“抢房”冲突。
 * 3. 资源树同步：床位变动后，强制触发房间 evaluateRoomSafety 进行“资源体检”。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DormBedServiceImpl extends ServiceImpl<DormBedMapper, DormBed> implements DormBedService {
    
    private final DormBedMapper bedMapper;
    
    @Lazy // 🛡️ [防止循环依赖]：Bed 联动 Room，Room 有时也需要查询 Bed 详情
    private final DormRoomService roomService;
    
    /**
     * 1. 严格保存/修改床位
     * [逻辑]：防止手动修改导致悬空床位，且有人在住时禁止修改床位标签。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBedStrict(DormBed bed) {
        if (bed.getId() != null) {
            DormBed oldBed = this.getById(bed.getId());
            // 🛡️ [防刁民逻辑]：如果床位有人，严禁修改床位编号（Label），防止资产对账混乱
            if (oldBed.getOccupantId() != null && !oldBed.getBedLabel().equals(bed.getBedLabel())) {
                throw new ServiceException("审计拦截：床位在住期间，禁止修改床位物理标签/编号");
            }
        }
        this.saveOrUpdate(bed);
    }
    
    /**
     * 2. 物理熔断：修改床位生命周期
     * [逻辑]：20-正常, 50-维修, 80-保留。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBedStatus(Long bedId, Integer status) {
        DormBed bed = this.getById(bedId);
        if (bed == null) throw new ServiceException("床位不存在");
        
        // 🛡️ [安全拦截]：有人在住的床位，严禁切换至“维修”或“保留”状态
        if (bed.getOccupantId() != null && status != 20) {
            throw new ServiceException("熔断拦截：该床位仍有学生在住，无法变更为非正常状态");
        }
        
        bed.setStatus(status);
        this.updateById(bed);
        
        // [联动]：状态变更可能影响房间饱和度，触发体检
        roomService.evaluateRoomSafety(bed.getRoomId());
    }
    
    /**
     * 3. [核心引擎] 执行入住/退宿动作
     * 🛡️ [高并发设计]：采用 version 乐观锁 CAS 机制。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOccupant(Long id, Long userId, Integer userCategory, Integer version) {
        // A. 获取当前最真实的数据库记录
        DormBed bed = this.getById(id);
        if (bed == null) throw new ServiceException("床位档案已丢失");
        
        // B. 🛡️ [CAS 并发校验]：校验前端传入的版本号是否与数据库一致
        if (version == null || !bed.getVersion().equals(version)) {
            throw new ServiceException("并发冲突：该床位信息已被他人修改，请刷新列表后重新操作");
        }
        
        // C. [业务逻辑：入住 vs 退宿]
        if (userId == null) {
            // 执行退宿
            bed.setOccupantId(null);
            bed.setOccupantType(null);
            bed.setResStatus(21); // 21-空闲
        } else {
            // 执行入住
            // 🛡️ [一人一床死守]：查询该用户是否在全校其他地方有房
            Long existingBedId = bedMapper.checkUserHasBed(userId);
            if (existingBedId != null && !existingBedId.equals(id)) {
                throw new ServiceException("分配失败：该用户在系统中已有在住床位，禁止重复占用资源！");
            }
            bed.setOccupantId(userId);
            bed.setOccupantType(userCategory);
            bed.setResStatus(22); // 22-已占用
        }
        
        bed.setUpdateTime(LocalDateTime.now());
        
        // D. 🛡️ [原子更新]：SET version = version + 1 WHERE id = ? AND version = ?
        boolean success = this.update(bed, Wrappers.<DormBed>lambdaUpdate()
                .eq(DormBed::getId, id)
                .eq(DormBed::getVersion, version)
                .set(DormBed::getVersion, version + 1));
        
        if (!success) {
            throw new ServiceException("抢占失败：资源由于高并发竞争已被锁定，请重试");
        }
        
        // E. 🛡️ [核心联动]：床位变动后，强制触发房间“资源体检”
        // 引擎会自动根据最新的床位快照，重算房间的 currentNum 和 resStatus(饱和度)
        roomService.evaluateRoomSafety(bed.getRoomId());
        
        log.info("✅ 床位事务完成：ID={}, 用户ID={}, 房间 {} 已同步校准", id, userId, bed.getRoomId());
    }
    
    /**
     * 4. 安全删除床位
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBedStrict(Long bedId) {
        DormBed bed = this.getById(bedId);
        if (bed == null) return;
        
        // 🛡️ [物理保护]：有人在住的床位严禁物理删除
        if (bed.getOccupantId() != null) {
            throw new ServiceException("删除失败：该床位当前有在住人员，请先执行退宿搬迁");
        }
        
        this.removeById(bedId);
        
        // [联动]：床位消失，房间容量发生变化，触发体检
        roomService.evaluateRoomSafety(bed.getRoomId());
    }
    
    /**
     * 5. 获取房间关联列表
     */
    @Override
    public List<DormBed> getByRoom(Long roomId) {
        return this.list(Wrappers.<DormBed>lambdaQuery()
                .eq(DormBed::getRoomId, roomId)
                .orderByAsc(DormBed::getId));
    }
    
    /**
     * 🛡️ 兼容旧接口 (如果有些地方还没改 version，暂时引向报错提示)
     */
    @Override
    @Deprecated
    public void updateOccupant(Long bedId, Long userId, Integer userCategory) {
        throw new ServiceException("系统警告：请使用带 version 校验的高阶 updateOccupant 接口以确保并发安全");
    }
}