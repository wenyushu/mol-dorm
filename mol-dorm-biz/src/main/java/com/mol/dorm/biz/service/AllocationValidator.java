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

import java.util.*;
import java.util.stream.Collectors;

/**
 * 分配数据完整性校验器 (性能优化 & 性别隔离增强版)
 * <p>
 * 核心职责：
 * 1. 📊 统计大盘：使用 SQL 聚合查询代替内存计算，提升万级数据下的性能。
 * 2. 🕵️ 异常扫雷：检测幽灵床位、超卖房间、性别混住及跨校区异常。
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
     * @return 健康报告 VO
     */
    public AllocationStatsVO analyzeCampus(Long campusId) {
        // 1. 基础数据获取
        SysCampus campus = campusService.getById(campusId);
        if (campus == null) return null;
        
        AllocationStatsVO report = new AllocationStatsVO();
        report.setCampusName(campus.getCampusName());
        
        // 获取该校区下的所有学院 ID (用于限定统计范围)
        List<Long> collegeIds = collegeService.list(Wrappers.<SysCollege>lambdaQuery()
                        .select(SysCollege::getId) // ⚡ 性能优化：只查 ID
                        .eq(SysCollege::getCampusId, campusId))
                .stream()
                .map(SysCollege::getId)
                .toList();
        
        if (CollUtil.isEmpty(collegeIds)) {
            return emptyReport(report);
        }
        
        // =========================================================================
        // Step 1: 基础统计 (SQL Aggregation) - ⚡ 性能优化点
        // 不再将几万名学生全部加载到内存，而是直接查 count
        // =========================================================================
        
        // 1.1 总学生数
        long totalStudents = userService.count(Wrappers.<SysOrdinaryUser>lambdaQuery()
                .in(SysOrdinaryUser::getCollegeId, collegeIds));
        report.setTotalStudents(totalStudents);
        
        // 1.2 异常状态 (休学/停用) count(status != '0')
        long suspended = userService.count(Wrappers.<SysOrdinaryUser>lambdaQuery()
                .in(SysOrdinaryUser::getCollegeId, collegeIds)
                .ne(SysOrdinaryUser::getStatus, "0"));
        report.setSuspendedCount(suspended);
        
        // 1.3 走读生 (status='0' and residence_type=1)
        long offCampus = userService.count(Wrappers.<SysOrdinaryUser>lambdaQuery()
                .in(SysOrdinaryUser::getCollegeId, collegeIds)
                .eq(SysOrdinaryUser::getStatus, "0")
                .eq(SysOrdinaryUser::getResidenceType, 1));
        report.setOffCampusCount(offCampus);
        
        // 1.4 需住校总人数 (status='0' and residence_type=0)
        long needDorm = userService.count(Wrappers.<SysOrdinaryUser>lambdaQuery()
                .in(SysOrdinaryUser::getCollegeId, collegeIds)
                .eq(SysOrdinaryUser::getStatus, "0")
                .eq(SysOrdinaryUser::getResidenceType, 0));
        
        // =========================================================================
        // Step 2: 资源与占用数据加载 (Resource Loading)
        // =========================================================================
        
        List<Long> buildingIds = buildingService.list(Wrappers.<DormBuilding>lambdaQuery()
                        .select(DormBuilding::getId)
                        .eq(DormBuilding::getCampusId, campusId))
                .stream().map(DormBuilding::getId).toList();
        
        List<DormRoom> allRooms = new ArrayList<>();
        List<DormBed> allBeds = new ArrayList<>();
        
        if (CollUtil.isNotEmpty(buildingIds)) {
            allRooms = roomService.list(Wrappers.<DormRoom>lambdaQuery()
                    .in(DormRoom::getBuildingId, buildingIds));
            if (CollUtil.isNotEmpty(allRooms)) {
                List<Long> roomIds = allRooms.stream().map(DormRoom::getId).toList();
                allBeds = bedService.list(Wrappers.<DormBed>lambdaQuery()
                        .in(DormBed::getRoomId, roomIds));
            }
        }
        
        // =========================================================================
        // Step 3: 深度校验与性别检测 (Deep Validation)
        // =========================================================================
        
        // 提取所有"已占位"的床位上的 occupantId
        Set<Long> occupantIds = allBeds.stream()
                .map(DormBed::getOccupantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        // ⚡ 性能优化：只查询"住在床上"的那些用户的必要字段 (ID, 性别, 姓名, 状态, 校区)
        // 即使校区有 2万人，如果只住了 5000人，也只查这 5000条数据
        Map<Long, SysOrdinaryUser> activeUserMap = new HashMap<>();
        if (CollUtil.isNotEmpty(occupantIds)) {
            List<SysOrdinaryUser> activeUsers = userService.list(Wrappers.<SysOrdinaryUser>lambdaQuery()
                    .select(SysOrdinaryUser::getId, SysOrdinaryUser::getRealName,
                            SysOrdinaryUser::getGender, SysOrdinaryUser::getStatus,
                            SysOrdinaryUser::getCampusId, SysOrdinaryUser::getResidenceType)
                    .in(SysOrdinaryUser::getId, occupantIds));
            activeUsers.forEach(u -> activeUserMap.put(u.getId(), u));
        }
        
        // 计算"有效分配数": 住在床上的用户必须是 (状态正常 + 确实申请了住校)
        long allocatedReal = activeUserMap.values().stream()
                .filter(u -> "0".equals(u.getStatus()) && u.getResidenceType() == 0)
                .count();
        
        report.setAllocatedCount(allocatedReal);
        report.setUnallocatedCount(Math.max(0, needDorm - allocatedReal));
        report.setProgressRate(needDorm == 0 ? "100%" :
                NumberUtil.formatPercent((double) allocatedReal / needDorm, 1));
        
        // =========================================================================
        // Step 4: 异常检测 (Anomaly Detection)
        // =========================================================================
        
        List<String> details = report.getErrorDetails();
        int errorCount = 0;
        
        // 🚨 检测 1: 性别混住 (Gender Conflict) - [新增功能]
        // 逻辑：按房间分组，检查同一房间内的用户性别是否一致
        Map<Long, List<DormBed>> bedsByRoom = allBeds.stream()
                .collect(Collectors.groupingBy(DormBed::getRoomId));
        
        for (Map.Entry<Long, List<DormBed>> entry : bedsByRoom.entrySet()) {
            Long roomId = entry.getKey();
            List<DormBed> beds = entry.getValue();
            
            Set<String> genders = new HashSet<>();
            for (DormBed bed : beds) {
                if (bed.getOccupantId() != null) {
                    SysOrdinaryUser u = activeUserMap.get(bed.getOccupantId());
                    // 只检查已识别的用户
                    if (u != null && u.getGender() != null) {
                        genders.add(u.getGender());
                    }
                }
            }
            
            // 如果一个房间里出现了 > 1 种性别 (即 0 和 1 同时存在)
            if (genders.size() > 1) {
                errorCount++;
                DormRoom room = allRooms.stream().filter(r -> r.getId().equals(roomId)).findFirst().orElse(null);
                String roomNo = room != null ? room.getRoomNo() : "Unknown";
                details.add(StrUtil.format("👫 性别混住: 房间[{}] 同时存在男女生，请立即处理！", roomNo));
            }
        }
        
        // 🚨 检测 2: 幽灵床位 & 跨校区异常 (Ghost Bed)
        for (DormBed bed : allBeds) {
            // 仅校验学生 (occupantType == 0 或 null)
            if (bed.getOccupantId() != null && (bed.getOccupantType() == null || bed.getOccupantType() == 0)) {
                SysOrdinaryUser user = activeUserMap.get(bed.getOccupantId());
                
                if (user == null) {
                    // Case A: 查无此人
                    errorCount++;
                    report.setGhostBedCount(defaultValue(report.getGhostBedCount()) + 1);
                    details.add(StrUtil.format("👻 幽灵床位: 房间[{}-{}] 用户ID[{}] 不存在或非本校区",
                            bed.getRoomId(), bed.getBedLabel(), bed.getOccupantId()));
                } else {
                    // Case B: 跨校区数据错乱 (学生档案属于 A 校区，却住在 B 校区)
                    if (user.getCampusId() != null && !user.getCampusId().equals(campusId)) {
                        errorCount++;
                        details.add(StrUtil.format("⚠️ 跨校区: 房间[{}-{}] 学生[{}] 档案属于其他校区",
                                bed.getRoomId(), bed.getBedLabel(), user.getRealName()));
                    }
                    // Case C: 状态异常 (休学的住在宿舍里)
                    if (!"0".equals(user.getStatus())) {
                        errorCount++;
                        details.add(StrUtil.format("⚠️ 状态异常: 房间[{}-{}] 学生[{}] 已停用/休学",
                                bed.getRoomId(), bed.getBedLabel(), user.getRealName()));
                    }
                }
            }
        }
        
        // 🚨 检测 3: 超卖房间 (Oversold)
        for (DormRoom room : allRooms) {
            if (room.getCurrentNum() > room.getCapacity()) {
                errorCount++;
                report.setOversoldRoomCount(defaultValue(report.getOversoldRoomCount()) + 1);
                details.add(StrUtil.format("💥 严重超卖: 房间[{}] 容量{}人, 记录实住{}人",
                        room.getRoomNo(), room.getCapacity(), room.getCurrentNum()));
            }
        }
        
        // 🚨 检测 4: 计数不同步 (Sync Error)
        // 实时统计 Bed 表里的实际人数
        Map<Long, Long> realOccupancyMap = allBeds.stream()
                .filter(b -> b.getOccupantId() != null)
                .collect(Collectors.groupingBy(DormBed::getRoomId, Collectors.counting()));
        
        for (DormRoom room : allRooms) {
            long realCount = realOccupancyMap.getOrDefault(room.getId(), 0L);
            if (room.getCurrentNum() != realCount) {
                errorCount++;
                report.setSyncErrorCount(defaultValue(report.getSyncErrorCount()) + 1);
                details.add(StrUtil.format("⚠️ 计数不同步: 房间[{}] Room表记{}人, Bed表实占{}人",
                        room.getRoomNo(), room.getCurrentNum(), realCount));
            }
        }
        
        report.setErrorCount(errorCount);
        // 日志截断
        if (details.size() > 50) {
            List<String> subList = new ArrayList<>(details.subList(0, 50));
            subList.add("... (异常数据过多，仅展示前50条)");
            report.setErrorDetails(subList);
        }
        
        return report;
    }
    
    private AllocationStatsVO emptyReport(AllocationStatsVO report) {
        report.setTotalStudents(0L);
        report.setProgressRate("0%");
        return report;
    }
    
    private int defaultValue(Integer val) {
        return val == null ? 0 : val;
    }
}