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
 * 分配数据完整性校验器
 * <p>
 * 专门负责“扫雷”：检测幽灵数据、超卖房间及统计进度。
 * 本类使用了 JDK 17 特性 (如 .toList() )。
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
        // 1. 基础数据获取：校区信息
        SysCampus campus = campusService.getById(campusId);
        if (campus == null) return null;
        
        AllocationStatsVO report = new AllocationStatsVO();
        report.setCampusName(campus.getCampusName());
        
        // =========================================================================
        // Step 1: 基础人群画像 (User Dimension) - 统计有多少人需要住
        // =========================================================================
        
        // A. 获取该校区下的所有学院 ID
        List<Long> collegeIds = collegeService.list(Wrappers.<SysCollege>lambdaQuery()
                        .eq(SysCollege::getCampusId, campusId))
                .stream()
                .map(SysCollege::getId)
                .toList(); // JDK 17: 直接转为不可变列表
        
        List<SysOrdinaryUser> allStudents = new ArrayList<>();
        if (CollUtil.isNotEmpty(collegeIds)) {
            // B. 查询这些学院下的所有学生 (不分状态，全量查)
            allStudents = userService.list(Wrappers.<SysOrdinaryUser>lambdaQuery()
                    .in(SysOrdinaryUser::getCollegeId, collegeIds));
        }
        report.setTotalStudents((long) allStudents.size());
        
        // C. 维度统计
        // Status: "0"-正常, "1"-停用/休学
        // ResidenceType: 0-住校, 1-走读
        long suspended = allStudents.stream().filter(u -> !"0".equals(u.getStatus())).count();
        long offCampus = allStudents.stream().filter(u -> "0".equals(u.getStatus()) && u.getResidenceType() == 1).count();
        // 核心关注群体：正常状态且申请住校的学生
        long needDorm = allStudents.stream().filter(u -> "0".equals(u.getStatus()) && u.getResidenceType() == 0).count();
        
        report.setSuspendedCount(suspended);
        report.setOffCampusCount(offCampus);
        
        // =========================================================================
        // Step 2: 住宿资源核查 (Resource Dimension) - 统计有多少床位
        // =========================================================================
        
        // A. 获取该校区所有楼栋
        List<Long> buildingIds = buildingService.list(Wrappers.<DormBuilding>lambdaQuery()
                        .eq(DormBuilding::getCampusId, campusId))
                .stream()
                .map(DormBuilding::getId)
                .toList();
        
        List<DormRoom> allRooms = new ArrayList<>();
        List<DormBed> allBeds = new ArrayList<>();
        
        if (CollUtil.isNotEmpty(buildingIds)) {
            // B. 获取所有房间
            allRooms = roomService.list(Wrappers.<DormRoom>lambdaQuery()
                    .in(DormRoom::getBuildingId, buildingIds));
            
            if (CollUtil.isNotEmpty(allRooms)) {
                List<Long> roomIds = allRooms.stream().map(DormRoom::getId).toList();
                // C. 获取所有床位
                allBeds = bedService.list(Wrappers.<DormBed>lambdaQuery()
                        .in(DormBed::getRoomId, roomIds));
            }
        }
        
        // =========================================================================
        // Step 3: 核心交叉验证 (Cross Validation) - 计算“分配率”
        // =========================================================================
        
        // A. 提取出所有“确实有人住”的床位上的 OccupantID
        Set<Long> occupantIdsInBeds = allBeds.stream()
                .map(DormBed::getOccupantId)
                .filter(occupantId -> occupantId != null)
                .collect(Collectors.toSet());
        
        // B. 计算逻辑：在【需要住校】的学生名单中，有多少人的 ID 出现在了【床位表】里
        long allocatedReal = allStudents.stream()
                .filter(u -> "0".equals(u.getStatus()) && u.getResidenceType() == 0)
                .filter(u -> occupantIdsInBeds.contains(u.getId()))
                .count();
        
        report.setAllocatedCount(allocatedReal);
        report.setUnallocatedCount(needDorm - allocatedReal);
        // 计算百分比，保留1位小数
        report.setProgressRate(needDorm == 0 ? "100%" :
                NumberUtil.formatPercent((double) allocatedReal / needDorm, 1));
        
        // =========================================================================
        // Step 4: 异常检测 (Anomaly Detection) - 寻找脏数据
        // =========================================================================
        
        int errorCount = 0;
        List<String> details = report.getErrorDetails();
        
        // 🚨 异常检测 1: 幽灵床位 (Ghost Bed)
        // 定义：床位上记录了 occupant_id，但这个 ID 在本校区的有效学生列表里找不到。
        // 可能原因：
        // 1. 学生转校区了，但床位没退。
        // 2. 学生休学/退学了，但床位没退。
        // 3. 数据库手动删了学生，忘删床位。
        
        // 制作本校区有效学生 ID 集合 (Set 查询快)
        Set<Long> validStudentIds = allStudents.stream()
                .map(SysOrdinaryUser::getId)
                .collect(Collectors.toSet());
        
        for (DormBed bed : allBeds) {
            // 只有当床位有人(occupantId != null) 且 住的是学生(Type=0或null) 时才校验
            // 如果住的是教职工(Type=1)，则跳过校验，否则会误报
            if (bed.getOccupantId() != null && (bed.getOccupantType() == null || bed.getOccupantType() == 0)) {
                if (!validStudentIds.contains(bed.getOccupantId())) {
                    errorCount++;
                    report.setGhostBedCount(defaultValue(report.getGhostBedCount()) + 1);
                    details.add(StrUtil.format("👻 幽灵床位: 房间[{}] 床位[{}] 占用者ID[{}] 非本校区在籍学生",
                            bed.getRoomId(), bed.getBedLabel(), bed.getOccupantId()));
                }
            }
        }
        
        // 🚨 异常检测 2: 超卖房间 (Oversold Room)
        // 定义：房间的 current_num (实住人数) > capacity (物理容量)。
        // 原因：并发控制失效，导致多个人抢到了同一个床位，或者数据手动改错了。
        for (DormRoom room : allRooms) {
            if (room.getCurrentNum() > room.getCapacity()) {
                errorCount++;
                report.setOversoldRoomCount(defaultValue(report.getOversoldRoomCount()) + 1);
                details.add(StrUtil.format("💥 严重超卖: 房间[{}] 容量{}人, 记录实住{}人",
                        room.getRoomNo(), room.getCapacity(), room.getCurrentNum()));
            }
        }
        
        // 🚨 异常检测 3: 数据计数不同步 (Sync Error)
        // 定义：Room 表里的 current_num 字段，不等于 Bed 表里该房间实际占用的数量。
        // 原因：分配或退宿时，事务未完全提交，或者直接操作了 Bed 表没更新 Room 表。
        
        // 实时计算每个房间的实际床位占用数
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
        
        // 日志截断：如果异常太多，只展示前 20 条，防止前端炸裂
        if (details.size() > 20) {
            details.add("... (异常数据过多，请查看后台详细日志)");
        }
        
        return report;
    }
    
    /**
     * 辅助方法：处理 Integer null 值为 0
     */
    private int defaultValue(Integer val) {
        return val == null ? 0 : val;
    }
}