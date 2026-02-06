package com.mol.dorm.biz.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mol.common.core.constant.DormConstants;
import com.mol.dorm.biz.entity.*;
import com.mol.dorm.biz.mapper.DormRoomMapper;
import com.mol.dorm.biz.mapper.UserPreferenceMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 宿舍智能分配核心引擎
 * 🛡️ [算法精髓]：
 * 1. 地缘聚合：同班/同专业优先成团，降低管理摩擦力。
 * 2. 社交平衡：MBTI 社交破冰算法，防止寝室变成沉默孤岛。
 * 3. 动态熔断：分配后自动触发 evaluateRoomSafety，确保饱和度状态码(21-26)实时校准。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DormAllocationService {
    
    private final UserPreferenceMapper preferenceMapper;
    private final DormBuildingService buildingService;
    private final DormBedService bedService;
    private final DormRoomMapper roomMapper;
    
    @Lazy
    private final DormRoomService roomService; // 🟢 用于联动体检引擎
    
    @Resource
    @Lazy
    private DormAllocationService self;
    
    // 🔒 校区级细粒度并发锁，防止多名管理员同时对同一校区执行“创世分配”
    private final Map<Long, Object> campusLocks = new ConcurrentHashMap<>();
    
    // =================================================================================
    // 1. 核心分配入口 (Controller 直接调用)
    // =================================================================================
    
    public String executeAllocation(Long campusId, Integer gender) {
        Object lock = campusLocks.computeIfAbsent(campusId, k -> new Object());
        synchronized (lock) {
            return self.doExecute(campusId, gender);
        }
    }
    
    @Transactional(rollbackFor = Exception.class)
    public String doExecute(Long campusId, Integer gender) {
        long startTime = System.currentTimeMillis();
        
        // A. 加载目标校区的物理楼栋资源
        List<Long> buildingIds = loadBuildingIds(campusId);
        
        // B. 抓取“待分配”池：一次性加载用户基础档案与画像习惯
        // [逻辑点]：SQL 内部已按照地缘性（班级、专业）排好序，保证了地缘聚合的底色
        List<UserPreference> candidateProfiles = preferenceMapper.selectFullProfileForAllocation(campusId, gender);
        
        if (CollUtil.isEmpty(candidateProfiles)) {
            return "当前校区暂无符合条件（已填画像且未分配）的人员数据。";
        }
        
        // C. 按性别执行物理隔离分配
        int totalAllocated = 0;
        Map<Integer, List<UserPreference>> genderGroups = candidateProfiles.stream()
                .collect(Collectors.groupingBy(UserPreference::getGender));
        
        for (Map.Entry<Integer, List<UserPreference>> entry : genderGroups.entrySet()) {
            totalAllocated += processGroup(entry.getKey(), entry.getValue(), buildingIds);
        }
        
        long duration = System.currentTimeMillis() - startTime;
        return StrUtil.format("【分配完成】耗时:{}ms, 待处理:{}人, 成功分配:{}人", duration, candidateProfiles.size(), totalAllocated);
    }
    
    // =================================================================================
    // 2. 核心算法分组处理逻辑
    // =================================================================================
    
    private int processGroup(Integer gender, List<UserPreference> profiles, List<Long> buildingIds) {
        // 1. 获取该性别对应的可用房间池 (必须是 LC_NORMAL-20 状态)
        List<DormRoom> roomPool = getSortedRooms(buildingIds, gender);
        if (CollUtil.isEmpty(roomPool)) return 0;
        
        // 2. 待分配池使用 LinkedList 保持 SQL 给出的原始地缘排序
        LinkedList<UserPreference> studentPool = new LinkedList<>(profiles);
        int successCount = 0;
        Iterator<DormRoom> roomIterator = roomPool.iterator();
        
        // [算法阶段 A]：组队码逻辑优先 (处理强制捆绑入住需求)
        successCount += processTeamLogic(studentPool, roomIterator);
        
        // [算法阶段 B]：智能画像匹配 (MBTI + 生活习惯加权)
        while (!studentPool.isEmpty() && roomIterator.hasNext()) {
            DormRoom room = roomIterator.next();
            int needed = room.getCapacity() - room.getCurrentNum();
            if (needed <= 0) continue;
            
            List<UserPreference> roomMates = new ArrayList<>();
            // 取出当前地缘性最优的“种子用户”
            UserPreference seed = studentPool.removeFirst();
            roomMates.add(seed);
            
            // 为种子寻找灵魂契合的室友
            while (roomMates.size() < needed && !studentPool.isEmpty()) {
                UserPreference bestMatch = findBestMatch(roomMates, studentPool);
                
                if (bestMatch != null) {
                    roomMates.add(bestMatch);
                    studentPool.remove(bestMatch);
                } else {
                    // 若无理想匹配，则直接按班级地缘性顺位补齐
                    roomMates.add(studentPool.removeFirst());
                }
            }
            
            // 物理写入床位并通知审计引擎
            persistAllocation(room, roomMates);
            successCount += roomMates.size();
        }
        return successCount;
    }
    
    /**
     * 3. 灵魂匹配算法：MBTI 社交平衡 + 冲突计算
     */
    private UserPreference findBestMatch(List<UserPreference> currentRoom, LinkedList<UserPreference> pool) {
        UserPreference bestOne = null;
        double maxMatchScore = -Double.MAX_VALUE;
        UserPreference seed = currentRoom.get(0);
        
        // 社交破冰：统计当前寝室中 E 人（外向型）数量
        long eCount = currentRoom.stream().filter(p -> "E".equalsIgnoreCase(p.getMbtiEI())).count();
        
        // 滑动窗口搜索：仅在池中前 50 人中寻找，兼顾地缘聚集性与匹配度
        int searchLimit = Math.min(pool.size(), 50);
        for (int i = 0; i < searchLimit; i++) {
            UserPreference cand = pool.get(i);
            
            // 🛡️ [一票否决红线]：吸烟、呼噜、重度冲突
            if (checkDeepVeto(currentRoom, cand)) continue;
            
            double score = 0.0;
            
            // A. 社交奖励分：全 I 寝室（沉默孤岛）急需 E 人带头（奖励 300 分）
            if (eCount == 0 && "E".equalsIgnoreCase(cand.getMbtiEI())) score += 300.0;
            
            // B. 计算与已有成员的共鸣与冲突
            for (UserPreference m : currentRoom) {
                score += calculateSocialBreaking(m, cand); // 兴趣同好加分
                score -= calculateConflict(m, cand);       // 作息/卫生习惯扣分
            }
            
            // C. 地缘行政分 (班级 > 专业)
            if (ObjectUtil.equal(cand.getClassId(), seed.getClassId())) score += 500.0;
            else if (ObjectUtil.equal(cand.getMajorId(), seed.getMajorId())) score += 200.0;
            
            if (score > maxMatchScore) {
                maxMatchScore = score;
                bestOne = cand;
            }
        }
        return bestOne;
    }
    
    // =================================================================================
    // 3. 辅助计算逻辑 (一票否决、冲突系数、同好加分)
    // =================================================================================
    
    private boolean checkDeepVeto(List<UserPreference> room, UserPreference cand) {
        for (UserPreference m : room) {
            // 烟草隔离红线
            if ((gt0(cand.getSmoking()) && nvl(m.getSmokeTolerance()) == 0) ||
                    (gt0(m.getSmoking()) && nvl(cand.getSmokeTolerance()) == 0)) return true;
            // 噪音红线 (呼噜 vs 神经衰弱)
            if ((nvl(cand.getSnoringLevel()) >= 2 && nvl(m.getSleepQuality()) >= 3) ||
                    (nvl(m.getSnoringLevel()) >= 2 && nvl(cand.getSleepQuality()) >= 3)) return true;
        }
        return false;
    }
    
    private double calculateSocialBreaking(UserPreference p1, UserPreference p2) {
        double bonus = 0.0;
        // 破冰标签加分
        if (nvl(p1.getIsAnime()) > 0 && nvl(p2.getIsAnime()) > 0) bonus += 60.0;
        if (gt0(p1.getIsCosplay()) && gt0(p2.getIsCosplay())) bonus += 120.0;
        return bonus;
    }
    
    private double calculateConflict(UserPreference p1, UserPreference p2) {
        // 加权欧氏距离：作息习惯权重最高 (3.5)
        double sum = Math.pow(nvl(p1.getBedTime()) - nvl(p2.getBedTime()), 2) * 3.5;
        return Math.sqrt(sum);
    }
    
    private int processTeamLogic(LinkedList<UserPreference> pool, Iterator<DormRoom> roomIt) {
        Map<String, List<UserPreference>> teams = pool.stream()
                .filter(p -> StrUtil.isNotBlank(p.getTeamCode()))
                .collect(Collectors.groupingBy(UserPreference::getTeamCode));
        
        int count = 0;
        for (List<UserPreference> members : teams.values()) {
            if (members.size() < 2 || !roomIt.hasNext()) continue;
            DormRoom room = roomIt.next();
            int space = Math.min(room.getCapacity() - room.getCurrentNum(), members.size());
            if (space < 2) continue;
            
            List<UserPreference> joiners = members.subList(0, space);
            persistAllocation(room, joiners);
            pool.removeAll(joiners);
            count += joiners.size();
        }
        return count;
    }
    
    // =================================================================================
    // 4. 高级维护方法 (重置与模拟)
    // =================================================================================
    
    /**
     * 重置校区分配状态
     * 🛡️ [防逻辑残留]：重置后强制触发房间全量体检。
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetCampusAllocation(Long campusId) {
        List<Long> bIds = loadBuildingIds(campusId);
        if (CollUtil.isEmpty(bIds)) return;
        
        // 批量清空床位占用 (ResStatus 回归 21-空闲)
        bedService.update(Wrappers.<DormBed>lambdaUpdate()
                .in(DormBed::getBuildingId, bIds)
                .set(DormBed::getOccupantId, null)
                .set(DormBed::getResStatus, DormConstants.RES_EMPTY));
        
        // [联动]：让受影响的房间重置 current_num 并刷新状态码
        roomService.list(Wrappers.<DormRoom>lambdaQuery().in(DormRoom::getBuildingId, bIds))
                .forEach(room -> roomService.evaluateRoomSafety(room.getId()));
    }
    
    /**
     * 分配模拟测算报告 (只读逻辑)
     */
    public String simulateAllocation(Long campusId) {
        long candidates = preferenceMapper.selectFullProfileForAllocation(campusId, null).size();
        List<Long> bIds = loadBuildingIds(campusId);
        long available = bedService.count(Wrappers.<DormBed>lambdaQuery()
                .in(DormBed::getBuildingId, bIds)
                .isNull(DormBed::getOccupantId)
                .eq(DormBed::getStatus, DormConstants.LC_NORMAL));
        
        return StrUtil.format("【模拟报告】待分新生:{}人, 可用床位:{}张。资源覆盖率:{}%",
                candidates, available, (available == 0 ? 0 : (available * 100 / candidates)));
    }
    
    // =================================================================================
    // 5. 辅助工具逻辑
    // =================================================================================
    
    private void persistAllocation(DormRoom room, List<UserPreference> users) {
        if (CollUtil.isEmpty(users)) return;
        List<DormBed> beds = bedService.list(Wrappers.<DormBed>lambdaQuery()
                .eq(DormBed::getRoomId, room.getId()).isNull(DormBed::getOccupantId)
                .last("LIMIT " + users.size()));
        
        for (int i = 0; i < Math.min(beds.size(), users.size()); i++) {
            DormBed bed = beds.get(i);
            bed.setOccupantId(users.get(i).getUserId());
            bed.setResStatus(DormConstants.RES_USING); // 22-已占用
            bedService.updateById(bed);
        }
        // 🟢 触发核心体检引擎，同步 currentNum 和饱和度状态码
        roomService.evaluateRoomSafety(room.getId());
    }
    
    private List<DormRoom> getSortedRooms(List<Long> bIds, Integer gender) {
        return roomService.list(Wrappers.<DormRoom>lambdaQuery()
                        .in(DormRoom::getBuildingId, bIds)
                        .eq(DormRoom::getGender, gender)
                        .eq(DormRoom::getStatus, DormConstants.LC_NORMAL))
                .stream().sorted(Comparator.comparing(DormRoom::getBuildingId).thenComparing(DormRoom::getRoomNo))
                .collect(Collectors.toList());
    }
    
    private List<Long> loadBuildingIds(Long cid) {
        return buildingService.list(Wrappers.<DormBuilding>lambdaQuery().eq(DormBuilding::getCampusId, cid))
                .stream().map(DormBuilding::getId).collect(Collectors.toList());
    }
    
    private int nvl(Integer v) { return v == null ? 0 : v; }
    private boolean gt0(Integer v) { return v != null && v > 0; }
}