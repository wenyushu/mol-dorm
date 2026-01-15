package com.mol.dorm.biz.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.DormBuilding;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.entity.UserPreference;
import com.mol.server.entity.SysCollege;
import com.mol.server.service.SysCollegeService;
import com.mol.server.service.SysOrdinaryUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 宿舍智能分配核心算法服务 (Pro Ultra: 细节狂魔版)
 * <p>
 * 本算法采用 "基于约束满足的贪心策略 (Constraint Satisfaction Greedy)"，
 * 旨在解决高校宿舍分配中的"生存矛盾"与"社交需求"。
 * </p>
 *
 * <h3>核心逻辑五层金字塔：</h3>
 * <ol>
 * <li><strong>L1 硬性隔离 (Hard Veto):</strong> 排除绝对无法共存的情况（如：严重呼噜vs神经衰弱、厌烟vs抽烟、螺蛳粉厌恶vs爱好者）。</li>
 * <li><strong>L2 优先聚合 (Priority):</strong> 组队码 > 同班 > 同专业 > 同学院（地缘性原则）。</li>
 * <li><strong>L3 生活匹配 (Lifestyle):</strong> 基于加权欧几里得距离计算作息、卫生、空调习惯的契合度。</li>
 * <li><strong>L4 灵魂匹配 (Soul Mate):</strong> 游戏段位/位置互补（下路双人组）、二次元共鸣、MBTI E/I 互补。</li>
 * <li><strong>L5 人口融合 (Demographics):</strong> 南北籍贯搭配、少数民族数量限制。</li>
 * </ol>
 *
 * @author mol
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DormAllocationService {
    
    // ----------- 依赖服务 -----------
    private final SysOrdinaryUserService userService;       // 学生数据
    private final SysCollegeService collegeService;         // 学院数据
    private final UserPreferenceService preferenceService;  // 画像数据
    private final DormRoomService roomService;              // 房间数据
    private final DormBuildingService buildingService;      // 楼栋数据
    private final DormBedService bedService;                // 床位数据
    
    /**
     * 【入口】执行智能分配
     *
     * @param campusId 目标校区ID (核心隔离参数，防止跨校区分配)
     * @param gender   指定分配性别 (1男 2女，null则全部运行)
     * @return 分配结果摘要
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized String executeAllocation(Long campusId, Integer gender) {
        long startTime = System.currentTimeMillis();
        
        // 1. 资源预加载 (校区物理隔离)
        // ---------------------------------------------------------
        List<Long> buildingIds = loadBuildingIds(campusId);
        List<SysOrdinaryUser> candidateUsers = loadCandidateUsers(campusId, gender);
        
        if (CollUtil.isEmpty(candidateUsers)) {
            return "该校区暂无符合条件的学生";
        }
        
        // 2. 幂等性过滤 (剔除已有床位的学生，防止重复分配)
        // ---------------------------------------------------------
        List<SysOrdinaryUser> finalUsers = filterOccupiedUsers(candidateUsers);
        if (CollUtil.isEmpty(finalUsers)) {
            return "所有学生均已分配，无需操作";
        }
        
        int totalStudents = finalUsers.size();
        log.info(">>> [智能分配] 启动 | 校区ID:{} | 待分配人数:{}", campusId, totalStudents);
        
        // 3. 加载画像数据 (Profile Loading)
        // ---------------------------------------------------------
        Map<Long, UserPreference> prefMap = loadPreferences(finalUsers);
        
        // 4. 执行分流分配 (Execution)
        // ---------------------------------------------------------
        int allocatedCount = 0;
        Map<Integer, List<SysOrdinaryUser>> genderGroups = finalUsers.stream()
                .collect(Collectors.groupingBy(SysOrdinaryUser::getSex));
        
        // 男生组
        if (genderGroups.containsKey(1)) {
            allocatedCount += processGroupAllocation(1, genderGroups.get(1), prefMap, buildingIds);
        }
        // 女生组
        if (genderGroups.containsKey(2)) {
            allocatedCount += processGroupAllocation(2, genderGroups.get(2), prefMap, buildingIds);
        }
        
        // 5. 最终结果校验
        verifyAllocationResult(totalStudents, allocatedCount);
        
        long duration = System.currentTimeMillis() - startTime;
        return StrUtil.format("分配完成！耗时{}ms，应分{}人，实分{}人", duration, totalStudents, allocatedCount);
    }
    
    /**
     * 单性别群体分配主逻辑
     */
    private int processGroupAllocation(Integer gender, List<SysOrdinaryUser> users,
                                       Map<Long, UserPreference> prefMap, List<Long> buildingIds) {
        if (CollUtil.isEmpty(users)) return 0;
        
        // A. 获取可用房源 (排序策略：低楼层优先 -> 同楼栋聚合)
        List<DormRoom> availableRooms = getSortedRooms(buildingIds, gender);
        if (CollUtil.isEmpty(availableRooms)) {
            log.warn(">>> [资源告急] 性别[{}]房源不足，该批次分配跳过", gender == 1 ? "男" : "女");
            return 0;
        }
        
        // B. 学生池排序 (地缘性优先：学院 -> 专业 -> 班级)
        // 这样 list.pop() 出来的学生，天然就是同班同学，作为种子用户能形成聚集效应
        LinkedList<SysOrdinaryUser> studentPool = users.stream()
                .sorted(Comparator.comparing(SysOrdinaryUser::getCollegeId)
                        .thenComparing(SysOrdinaryUser::getMajorId)
                        .thenComparing(SysOrdinaryUser::getClassId))
                .collect(Collectors.toCollection(LinkedList::new));
        
        Iterator<DormRoom> roomIterator = availableRooms.iterator();
        int successCount = 0;
        
        // C. 【L2 优先级】处理组队码 (Team Code)
        // 自选室友优先级最高，直接锁定房间
        successCount += processTeamCodeLogic(studentPool, prefMap, roomIterator);
        
        // D. 【核心】贪心匹配循环
        while (!studentPool.isEmpty() && roomIterator.hasNext()) {
            DormRoom currentRoom = roomIterator.next();
            int needed = currentRoom.getCapacity() - currentRoom.getCurrentNum();
            if (needed <= 0) continue;
            
            List<SysOrdinaryUser> roomMates = new ArrayList<>();
            
            // 1. 选取种子用户 (Seed User)
            // 取列表头部的学生 (因为已排序，大概率是同班的)
            SysOrdinaryUser seed = studentPool.removeFirst();
            roomMates.add(seed);
            
            // 2. 为种子用户寻找最佳室友 (Find Best Match)
            while (roomMates.size() < needed && !studentPool.isEmpty()) {
                // 在池子里找一个“不完美但最合适”的人
                SysOrdinaryUser bestMatch = findBestMatch(roomMates, studentPool, prefMap);
                
                if (bestMatch != null) {
                    roomMates.add(bestMatch);
                    studentPool.remove(bestMatch);
                } else {
                    // 兜底策略：如果前N个人都触发了“一票否决”(如都抽烟)，无法完美匹配
                    // 为了防止产生“孤儿床位”，强制取列表头部的一个人(同班)填坑
                    // 逻辑：两害相权取其轻，地缘性(同班) > 生活习惯
                    if (!studentPool.isEmpty()) {
                        SysOrdinaryUser fallback = studentPool.removeFirst();
                        roomMates.add(fallback);
                    } else {
                        break; // 池子空了
                    }
                }
            }
            
            // 3. 落库保存 (原子性操作)
            persistToDatabase(currentRoom, roomMates);
            successCount += roomMates.size();
        }
        
        // E. 孤儿数据检查
        if (!studentPool.isEmpty()) {
            log.error(">>> 警告：性别[{}]有 {} 人因房源不足未分配！", gender, studentPool.size());
        }
        
        return successCount;
    }
    
    /**
     * 🧠 核心匹配算法：寻找最佳室友
     * 遍历池中前N人，计算与当前室友的平均契合度，分高者得。
     */
    private SysOrdinaryUser findBestMatch(List<SysOrdinaryUser> currentRoom,
                                          List<SysOrdinaryUser> pool,
                                          Map<Long, UserPreference> prefMap) {
        SysOrdinaryUser bestCandidate = null;
        double maxMatchScore = -Double.MAX_VALUE; // 分数越高越好
        
        // 性能优化：搜索窗口限制为 50 人
        // 因为池子已按班级排序，前50人基本都是同专业同班的，地缘性最好
        int searchLimit = Math.min(pool.size(), 50);
        
        for (int i = 0; i < searchLimit; i++) {
            SysOrdinaryUser candidate = pool.get(i);
            
            // [L1] 一票否决 (Veto) - 只要触犯一条红线，直接跳过
            if (checkDeepVeto(currentRoom, candidate, prefMap)) {
                continue;
            }
            
            double totalScore = 0.0;
            
            for (SysOrdinaryUser member : currentRoom) {
                UserPreference pMember = prefMap.get(member.getId());
                UserPreference pCandidate = prefMap.get(candidate.getId());
                
                // [L3] 基础生活习惯距离 (越小越好，所以用负数累加)
                double discord = calculateEuclideanDistance(pMember, pCandidate);
                totalScore -= discord;
                
                // [L4] 灵魂匹配奖励 (加分)
                // 游戏深度匹配 (段位、位置)、二次元、MBTI
                totalScore += calculateSoulCompatibility(pMember, pCandidate);
                
                // [L5] 人口融合与气味匹配 (加分/减分)
                // 南北融合、螺蛳粉/榴莲共存
                totalScore += calculateDemoAndSmell(pMember, pCandidate);
            }
            
            double avgScore = totalScore / currentRoom.size();
            
            // [L2] 地缘性权重 (同班级极大加分)
            SysOrdinaryUser seed = currentRoom.get(0);
            if (ObjectUtil.equal(candidate.getClassId(), seed.getClassId())) {
                avgScore += 500.0; // 同班核心权重
            } else if (ObjectUtil.equal(candidate.getMajorId(), seed.getMajorId())) {
                avgScore += 200.0; // 同专业次级权重
            }
            
            // 择优
            if (avgScore > maxMatchScore) {
                maxMatchScore = avgScore;
                bestCandidate = candidate;
            }
        }
        return bestCandidate;
    }
    
    /**
     * [L1] 深度一票否决逻辑 (Deep Veto)
     * 返回 true 表示 "绝对不能住一起"
     */
    private boolean checkDeepVeto(List<SysOrdinaryUser> currentRoom, SysOrdinaryUser candidate, Map<Long, UserPreference> prefMap) {
        UserPreference pCandidate = prefMap.get(candidate.getId());
        if (pCandidate == null) return false;
        
        // 1. 民族数量限制 (每间房最多2个少数民族，防止小团体或孤立)
        if (isMinority(candidate)) {
            long minorityCount = currentRoom.stream().filter(this::isMinority).count();
            if (minorityCount >= 2) return true; // 满员了，不能再进
        }
        
        for (SysOrdinaryUser member : currentRoom) {
            UserPreference pMember = prefMap.get(member.getId());
            if (pMember == null) continue;
            
            // 2. 烟草硬隔离 (Smoking)
            // 只要有一个人抽烟(>0)，且另一个人完全不耐受(0)，必须隔离
            if ((gt0(pCandidate.getSmoking()) && pMember.getSmokeTolerance() == 0) ||
                    (gt0(pMember.getSmoking()) && pCandidate.getSmokeTolerance() == 0)) {
                return true;
            }
            
            // 3. 生理冲突 (Snoring vs Sensitive)
            // 神经衰弱(>=3) 遇上 雷震子呼噜(>=2) -> 隔离
            if ((pMember.getSleepQuality() >= 3 && pCandidate.getSnoringLevel() >= 2) ||
                    (pCandidate.getSleepQuality() >= 3 && pMember.getSnoringLevel() >= 2)) {
                return true;
            }
            
            // 4. 异味硬隔离 (Smell)
            // 无法忍受异味(1) vs 爱吃螺蛳粉/榴莲(>0)
            boolean candidateSmelly = gt0(pCandidate.getEatLuosifen()) || gt0(pCandidate.getEatDurian());
            boolean memberSmelly = gt0(pMember.getEatLuosifen()) || gt0(pMember.getEatDurian());
            
            if ((pMember.getOdorTolerance() != null && pMember.getOdorTolerance() == 1 && candidateSmelly) ||
                    (pCandidate.getOdorTolerance() != null && pCandidate.getOdorTolerance() == 1 && memberSmelly)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * [L3] 计算基础生活习惯的不和谐度 (Weighted Euclidean Distance)
     * 返回值越大，差异越大
     */
    private double calculateEuclideanDistance(UserPreference p1, UserPreference p2) {
        double sumSq = 0.0;
        // 权重配置 (可根据实际反馈调整)
        sumSq += weightedSq(p1.getBedTime(), p2.getBedTime(), 2.0);        // 作息 (最重要)
        sumSq += weightedSq(p1.getAcTemp(), p2.getAcTemp(), 1.5);          // 空调 (易吵架)
        sumSq += weightedSq(p1.getCleanFreq(), p2.getCleanFreq(), 1.2);    // 卫生
        sumSq += weightedSq(p1.getGameVoice(), p2.getGameVoice(), 1.2);    // 噪音
        sumSq += weightedSq(p1.getKeyboardAxis(), p2.getKeyboardAxis(), 1.5); // 机械键盘
        return Math.sqrt(sumSq);
    }
    
    /**
     * [L4] 灵魂匹配奖励 (Soul Compatibility)
     * 游戏、二次元、MBTI
     */
    private double calculateSoulCompatibility(UserPreference p1, UserPreference p2) {
        double bonus = 0.0;
        
        // 1. 游戏深度匹配
        // LOL/DOTA
        if (gt0(p1.getGameTypeLol()) && gt0(p2.getGameTypeLol())) {
            bonus += 10.0; // 基础同好
            // 段位接近 (分差<=1)
            if (Math.abs(nvl(p1.getGameRank()) - nvl(p2.getGameRank())) <= 1) bonus += 5.0;
            // 位置互补 (如下路双人组：射手4 + 辅助5)
            if (isBotLaneDuo(p1.getGameRole(), p2.getGameRole())) bonus += 20.0; // 黄金搭档！
        }
        // FPS (CS/瓦/三角洲)
        if (gt0(p1.getGameTypeFps()) && gt0(p2.getGameTypeFps())) {
            bonus += 10.0;
            // 键盘轴体一致 (都吵或者都静音)
            if (ObjectUtil.equal(p1.getKeyboardAxis(), p2.getKeyboardAxis())) bonus += 5.0;
        }
        
        // 2. 二次元共鸣 (Anime)
        // 0-现充, 1-看番, 2-老二刺螈。只要都>=1，大幅奖励
        if (nvl(p1.getIsAnime()) >= 1 && nvl(p2.getIsAnime()) >= 1) {
            bonus += 15.0;
        }
        // Cosplay 同好 (极强社交纽带)
        if (gt0(p1.getIsCosplay()) && gt0(p2.getIsCosplay())) {
            bonus += 20.0;
        }
        
        // 3. MBTI 互补 (E/I)
        // 防止"全员自闭"。一E一I，奖励 8 分
        String e1 = p1.getMbtiEI();
        String e2 = p2.getMbtiEI();
        if (StrUtil.isNotBlank(e1) && StrUtil.isNotBlank(e2) && !e1.equals(e2)) {
            bonus += 8.0;
        }
        
        return bonus;
    }
    
    /**
     * [L5] 人口融合与特殊饮食 (Demo & Smell)
     */
    private double calculateDemoAndSmell(UserPreference p1, UserPreference p2) {
        double score = 0.0;
        
        // 1. 臭味相投 (Luosifen/Durian)
        // 都爱吃螺蛳粉
        if (gt0(p1.getEatLuosifen()) && gt0(p2.getEatLuosifen())) score += 15.0;
        // 都爱吃榴莲
        if (gt0(p1.getEatDurian()) && gt0(p2.getEatDurian())) score += 10.0;
        
        // 2. 南北融合 (Region Mixing)
        // regionType: 0-南, 1-北
        if (p1.getRegionType() != null && p2.getRegionType() != null) {
            // 一南一北，给予适当奖励，促进文化交流
            if (!p1.getRegionType().equals(p2.getRegionType())) {
                score += 5.0;
            }
        }
        return score;
    }
    
    // ==========================================
    // 组队逻辑与持久化 (Utils)
    // ==========================================
    
    /**
     * 处理组队码逻辑
     */
    private int processTeamCodeLogic(List<SysOrdinaryUser> studentPool,
                                     Map<Long, UserPreference> prefMap,
                                     Iterator<DormRoom> roomIterator) {
        // 找出所有填了 TeamCode 的学生
        Map<String, List<SysOrdinaryUser>> teams = studentPool.stream()
                .filter(u -> {
                    UserPreference p = prefMap.get(u.getId());
                    return p != null && StrUtil.isNotBlank(p.getTeamCode());
                })
                .collect(Collectors.groupingBy(u -> prefMap.get(u.getId()).getTeamCode()));
        
        int count = 0;
        for (Map.Entry<String, List<SysOrdinaryUser>> entry : teams.entrySet()) {
            List<SysOrdinaryUser> members = entry.getValue();
            if (members.size() < 2) continue; // 单人不算组队，回退到散户池
            
            if (!roomIterator.hasNext()) break;
            DormRoom room = roomIterator.next();
            
            // 简单逻辑：直接塞入当前房间，如果塞不下就拆分(实际业务可优化为寻找空房)
            int space = room.getCapacity() - room.getCurrentNum();
            List<SysOrdinaryUser> movingIn = new ArrayList<>();
            for (int i = 0; i < Math.min(space, members.size()); i++) {
                movingIn.add(members.get(i));
            }
            
            persistToDatabase(room, movingIn);
            studentPool.removeAll(movingIn); // 从散户池移除
            count += movingIn.size();
            
            log.info(">>> 组队分配成功: 队伍[{}] {}人 入住 {}", entry.getKey(), movingIn.size(), room.getRoomNo());
        }
        return count;
    }
    
    /**
     * 数据库持久化：更新床位表和房间表
     */
    private void persistToDatabase(DormRoom room, List<SysOrdinaryUser> newOccupants) {
        if (CollUtil.isEmpty(newOccupants)) return;
        
        // 1. 再次查询空床位 (防止并发超卖)
        List<DormBed> emptyBeds = bedService.list(Wrappers.<DormBed>lambdaQuery()
                .eq(DormBed::getRoomId, room.getId())
                .isNull(DormBed::getOccupantId)
                .orderByAsc(DormBed::getBedLabel)
                .last("LIMIT " + newOccupants.size()));
        
        if (emptyBeds.size() < newOccupants.size()) {
            // 这是一个严重的并发异常，但在批处理中，我们选择跳过当前房间，避免回滚整个大事务
            log.error("并发异常：房间[{}]实际空余床位不足，跳过此房间分配", room.getRoomNo());
            return;
        }
        
        // 2. 批量更新床位
        List<DormBed> updates = new ArrayList<>();
        for (int i = 0; i < newOccupants.size(); i++) {
            DormBed bed = emptyBeds.get(i);
            bed.setOccupantId(newOccupants.get(i).getId());
            updates.add(bed);
        }
        bedService.updateBatchById(updates);
        
        // 3. 更新房间实住人数
        room.setCurrentNum(room.getCurrentNum() + newOccupants.size());
        roomService.updateById(room);
    }
    
    // ==========================================
    // 辅助工具方法
    // ==========================================
    
    private List<Long> loadBuildingIds(Long campusId) {
        List<Long> ids = buildingService.list(Wrappers.<DormBuilding>lambdaQuery()
                        .eq(DormBuilding::getCampusId, campusId))
                .stream().map(DormBuilding::getId).collect(Collectors.toList());
        if (CollUtil.isEmpty(ids)) throw new ServiceException("该校区下暂无宿舍楼栋数据");
        return ids;
    }
    
    private List<SysOrdinaryUser> loadCandidateUsers(Long campusId, Integer gender) {
        List<Long> collegeIds = collegeService.list(Wrappers.<SysCollege>lambdaQuery()
                        .eq(SysCollege::getCampusId, campusId))
                .stream().map(SysCollege::getId).collect(Collectors.toList());
        if (CollUtil.isEmpty(collegeIds)) return Collections.emptyList();
        
        return userService.list(Wrappers.<SysOrdinaryUser>lambdaQuery()
                .in(SysOrdinaryUser::getCollegeId, collegeIds)
                .eq(SysOrdinaryUser::getStatus, "0")
                .eq(gender != null, SysOrdinaryUser::getSex, gender));
    }
    
    private List<SysOrdinaryUser> filterOccupiedUsers(List<SysOrdinaryUser> users) {
        // 查出所有已占用的床位中的 occupant_id
        Set<Long> occupied = bedService.list(Wrappers.<DormBed>lambdaQuery()
                        .isNotNull(DormBed::getOccupantId))
                .stream().map(DormBed::getOccupantId).collect(Collectors.toSet());
        return users.stream().filter(u -> !occupied.contains(u.getId())).collect(Collectors.toList());
    }
    
    private Map<Long, UserPreference> loadPreferences(List<SysOrdinaryUser> users) {
        if (CollUtil.isEmpty(users)) return new HashMap<>();
        List<Long> ids = users.stream().map(SysOrdinaryUser::getId).collect(Collectors.toList());
        Map<Long, UserPreference> map = preferenceService.listByIds(ids).stream()
                .collect(Collectors.toMap(UserPreference::getUserId, p -> p));
        // 填充默认画像
        users.forEach(u -> map.computeIfAbsent(u.getId(), this::createDefaultPreference));
        return map;
    }
    
    private List<DormRoom> getSortedRooms(List<Long> buildingIds, Integer gender) {
        return roomService.list(Wrappers.<DormRoom>lambdaQuery()
                        .in(DormRoom::getBuildingId, buildingIds)
                        .eq(DormRoom::getGender, gender)
                        .eq(DormRoom::getStatus, 1)
                        .apply("current_num < capacity"))
                .stream()
                // 排序：先填满低楼层，便于管理
                .sorted(Comparator.comparing(DormRoom::getBuildingId)
                        .thenComparing(DormRoom::getFloorNo)
                        .thenComparing(DormRoom::getRoomNo))
                .collect(Collectors.toList());
    }
    
    private void verifyAllocationResult(int expected, int actual) {
        if (expected != actual) {
            log.warn(">>> 分配非闭环！预期 {} 人，实际入库 {} 人 (可能是房源不足或并发跳过)", expected, actual);
        } else {
            log.info(">>> 分配校验通过，数据完美闭环。");
        }
    }
    
    private boolean isMinority(SysOrdinaryUser user) {
        // 模拟判断：实际应从 user.getEthnicity() 判断
        // 假设 entity 中有 ethnicity 字段，不是 "汉族" 则为少数民族
        // return !"汉族".equals(user.getEthnicity());
        return false; // 暂且返回 false
    }
    
    // 辅助判断下路双人组 (4:ADC, 5:Support)
    private boolean isBotLaneDuo(Integer r1, Integer r2) {
        if (r1 == null || r2 == null) return false;
        return (r1 == 4 && r2 == 5) || (r1 == 5 && r2 == 4);
    }
    
    private double weightedSq(Integer v1, Integer v2, double weight) {
        if (v1 == null || v2 == null) return 0.0;
        return Math.pow(v1 - v2, 2) * weight;
    }
    
    private boolean gt0(Integer val) { return val != null && val > 0; }
    private int nvl(Integer val) { return val == null ? 0 : val; }
    
    private UserPreference createDefaultPreference(Long userId) {
        UserPreference p = new UserPreference();
        p.setUserId(userId);
        p.setSmoking(0); p.setSmokeTolerance(1); // 默认无烟
        p.setBedTime(3); p.setWakeTime(3);       // 默认 23:00-08:00
        p.setSnoringLevel(0); p.setSleepQuality(2);
        p.setEatLuosifen(0); p.setEatDurian(0);  // 默认不吃异味食品
        p.setIsAnime(0); p.setIsCosplay(0); p.setMbtiEI("I");
        return p;
    }
}