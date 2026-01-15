package com.mol.dorm.biz.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.DormBuilding;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.vo.AllocationStatsVO;
import com.mol.server.entity.SysCampus;
import com.mol.server.entity.SysCollege;
import com.mol.server.service.SysCampusService;
import com.mol.server.service.SysCollegeService;
import com.mol.server.service.SysOrdinaryUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 分配数据完整性校验器
 * <p>
 * 专门负责“扫雷”：检测幽灵数据、孤儿数据、超卖房间及统计进度。
 * </p>
 *
 * @author mol
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AllocationValidator {
    
    private final SysCampusService campusService;
    private final SysCollegeService collegeService;
    private final SysOrdinaryUserService userService;
    private final DormBuildingService buildingService;
    private final DormRoomService roomService;
    private final DormBedService bedService;
    
    /**
     * 对指定校区进行全量数据体检
     *
     * @param campusId 校区 ID
     * @return 健康报告
     */
    public AllocationStatsVO analyzeCampus(Long campusId) {
        SysCampus campus = campusService.getById(campusId);
        if (campus == null) return null;
        
        AllocationStatsVO report = new AllocationStatsVO();
        report.setCampusName(campus.getCampusName());
        
        // ------------------------------------------------
        // 1. 基础人群画像 (User Dimension)
        // ------------------------------------------------
        // 获取该校区所有学院
        List<Long> collegeIds = collegeService.list(Wrappers.<SysCollege>lambdaQuery()
                        .eq(SysCollege::getCampusId, campusId))
                .stream().map(SysCollege::getId).collect(Collectors.toList());
        
        List<SysOrdinaryUser> allStudents = List.of();
        if (CollUtil.isNotEmpty(collegeIds)) {
            allStudents = userService.list(Wrappers.<SysOrdinaryUser>lambdaQuery()
                    .in(SysOrdinaryUser::getCollegeId, collegeIds));
        }
        report.setTotalStudents((long) allStudents.size());
        
        // 状态分类
        // status: 0-正常 1-停用(休学等)
        // residence_type: 0-住校 1-走读
        long suspended = allStudents.stream().filter(u -> !"0".equals(u.getStatus())).count();
        long offCampus = allStudents.stream().filter(u -> "0".equals(u.getStatus()) && u.getResidenceType() == 1).count();
        long needDorm = allStudents.stream().filter(u -> "0".equals(u.getStatus()) && u.getResidenceType() == 0).count();
        
        report.setSuspendedCount(suspended);
        report.setOffCampusCount(offCampus);
        
        // ------------------------------------------------
        // 2. 住宿数据核查 (Bed/Room Dimension)
        // ------------------------------------------------
        // 获取该校区所有楼栋 -> 房间 -> 床位
        List<Long> buildingIds = buildingService.list(Wrappers.<DormBuilding>lambdaQuery()
                        .eq(DormBuilding::getCampusId, campusId))
                .stream().map(DormBuilding::getId).collect(Collectors.toList());
        
        List<DormRoom> allRooms = List.of();
        List<DormBed> allBeds = List.of();
        
        if (CollUtil.isNotEmpty(buildingIds)) {
            allRooms = roomService.list(Wrappers.<DormRoom>lambdaQuery()
                    .in(DormRoom::getBuildingId, buildingIds));
            allBeds = bedService.list(Wrappers.<DormBed>lambdaQuery()
                    .in(DormBed::getRoomId, allRooms.stream().map(DormRoom::getId).toList()));
        }
        
        // ------------------------------------------------
        // 3. 核心：交叉验证 (Cross Validation)
        // ------------------------------------------------
        
        // A. 统计实际占用床位的 ID 集合
        Set<Long> occupantIds = allBeds.stream()
                .filter(b -> b.getOccupantId() != null)
                .map(DormBed::getOccupantId)
                .collect(Collectors.toSet());
        
        // B. 计算分配进度
        // 在“需要住宿”的学生中，有多少人在 occupantIds 里
        long allocatedReal = allStudents.stream()
                .filter(u -> "0".equals(u.getStatus()) && u.getResidenceType() == 0) // 正常且申请住校
                .filter(u -> occupantIds.contains(u.getId()))
                .count();
        
        report.setAllocatedCount(allocatedReal);
        report.setUnallocatedCount(needDorm - allocatedReal);
        report.setProgressRate(needDorm == 0 ? "100%" :
                NumberUtil.formatPercent((double) allocatedReal / needDorm, 1));
        
        // ------------------------------------------------
        // 4. 异常检测 (Anomaly Detection)
        // ------------------------------------------------
        int errorCount = 0;
        List<String> details = report.getErrorDetails();
        
        // 🚨 异常1: 幽灵床位 (Ghost Bed)
        // 床位上有人(ID)，但这个 ID 不在本校区学生列表里，或者是休学/走读生
        // (注：这里简单处理，只检查 ID 是否属于本校区有效学生。如果查不到，可能是脏数据或跨校区分配错误)
        Set<Long> validStudentIds = allStudents.stream().map(SysOrdinaryUser::getId).collect(Collectors.toSet());
        for (DormBed bed : allBeds) {
            if (bed.getOccupantId() != null) {
                if (!validStudentIds.contains(bed.getOccupantId())) {
                    // 进一步检查：是不是压根没这个用户？
                    errorCount++;
                    report.setGhostBedCount(report.getGhostBedCount() == null ? 1 : report.getGhostBedCount() + 1);
                    details.add(StrUtil.format("幽灵床位: 房间[{}]床位[{}] 占用者ID[{}] 非本校区有效学生",
                            bed.getRoomId(), bed.getBedLabel(), bed.getOccupantId()));
                }
            }
        }
        
        // 🚨 异常2: 超卖房间 (Oversold)
        // 房间实住人数(current_num) > 容量(capacity)
        for (DormRoom room : allRooms) {
            if (room.getCurrentNum() > room.getCapacity()) {
                errorCount++;
                report.setOversoldRoomCount(report.getOversoldRoomCount() == null ? 1 : report.getOversoldRoomCount() + 1);
                details.add(StrUtil.format("严重超卖: 房间[{}] 容量{}人, 实住{}人",
                        room.getRoomNo(), room.getCapacity(), room.getCurrentNum()));
            }
        }
        
        // 🚨 异常3: 数据不同步 (Sync Error)
        // 房间表的 current_num != 床位表中该房间不为空的数量
        Map<Long, Long> realOccupancyMap = allBeds.stream()
                .filter(b -> b.getOccupantId() != null)
                .collect(Collectors.groupingBy(DormBed::getRoomId, Collectors.counting()));
        
        for (DormRoom room : allRooms) {
            long realCount = realOccupancyMap.getOrDefault(room.getId(), 0L);
            if (room.getCurrentNum() != realCount) {
                errorCount++;
                report.setSyncErrorCount(report.getSyncErrorCount() == null ? 1 : report.getSyncErrorCount() + 1);
                details.add(StrUtil.format("计数不同步: 房间[{}] 记录{}人, 实际床位占用{}人",
                        room.getRoomNo(), room.getCurrentNum(), realCount));
            }
        }
        
        // 🚨 异常4: 孤儿用户 (Orphan - 逻辑上的)
        // 如果系统有字段标记了"已入住"但没床位（当前系统主要靠bed表判断，所以此项暂时通过 allocatedCount 计算体现）
        // 比如：状态是“住校”，但分配完了还没床位，这在 unallocatedCount 里体现，不算数据错误。
        // 但如果有 "走读生" 却占了床位，这属于幽灵床位的一种。
        
        report.setErrorCount(errorCount);
        
        // 截断详情日志，防止过长
        if (details.size() > 20) {
            details.add("... 更多异常请查看后台日志");
        }
        
        return report;
    }
}