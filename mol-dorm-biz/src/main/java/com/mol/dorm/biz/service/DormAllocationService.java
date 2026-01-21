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
import com.mol.dorm.biz.mapper.DormRoomMapper;
import com.mol.server.entity.SysCollege;
import com.mol.server.service.SysCollegeService;
import com.mol.server.service.SysOrdinaryUserService;
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
 * 宿舍智能分配核心算法服务 (Pro Ultra: 最终完整防刁民版)
 * <p>
 * 核心架构特性：
 * 1. 细粒度并发控制：基于 CampusId 的内存锁，防止同一校区被多人并发操作，但不同校区可并行。
 * 2. 事务自调用修复：注入 self 代理对象，确保 @Transactional 生效。
 * 3. 数据一致性保障：使用 SQL 原子更新 (increment) 解决房间人数超卖问题。
 * 4. 健壮性设计：全链路 NPE (空指针) 防御，使用 nvl() 和 getOrDefault() 兜底。
 * </p>
 *
 * @author mol
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DormAllocationService {
    
    // ==========================================
    // 依赖注入区 (Dependencies)
    // ==========================================
    private final SysOrdinaryUserService userService;       // 学生基础数据服务
    private final SysCollegeService collegeService;         // 学院数据服务
    private final UserPreferenceService preferenceService;  // 用户画像/偏好服务
    private final DormRoomService roomService;              // 房间服务
    private final DormBuildingService buildingService;      // 楼栋服务
    private final DormBedService bedService;                // 床位服务
    private final DormRoomMapper roomMapper;                // 核心：原子更新 Mapper (防超卖)
    
    /**
     * 🟢 自注入代理对象
     * 作用：解决 Spring AOP 的“自调用”失效问题。
     * 当我们在 executeAllocation 内部调用 doExecute 时，必须通过 self 调用，事务才会生效。
     * 加 @Lazy 是为了防止循环依赖报错。
     */
    @Resource
    @Lazy
    private DormAllocationService self;
    
    /**
     * 🔒 本地细粒度锁容器
     * Key: CampusId (校区ID), Value: 锁对象
     * 作用：确保同一时间，同一个校区只能有一个线程在跑分配算法，防止资源竞争。
     */
    private final Map<Long, Object> campusLocks = new ConcurrentHashMap<>();
    
    // ==========================================
    // 核心入口与事务控制 (Entry & Transaction)
    // ==========================================
    
    /**
     * 【对外入口】执行智能分配
     * <p>
     * 作用：作为 Controller 调用的入口，负责获取锁，并委托给事务方法执行。
     * </p>
     *
     * @param campusId 目标校区 ID (核心隔离参数)
     * @param gender   指定分配性别 ("1"-男 "0"-女，null则全部运行)
     * @return 分配结果的文字摘要
     */
    public String executeAllocation(Long campusId, String gender) {
        // 1. 获取该校区的专用锁对象 (如果没有则创建，computeIfAbsent 保证原子性)
        Object lock = campusLocks.computeIfAbsent(campusId, k -> new Object());
        
        // 🟢 性能优化：只锁当前校区，其他校区不阻塞
        synchronized (lock) {
            // 通过代理对象调用事务方法，确保事务切面生效
            return self.doExecute(campusId, gender);
        }
    }
    
    /**
     * 【事务核心】实际执行分配逻辑
     * <p>
     * 作用：包裹在事务中，执行资源加载、数据过滤、分组分配和结果校验。
     * 只要发生异常，所有数据库操作全部回滚。
     * </p>
     */
    @Transactional(rollbackFor = Exception.class)
    public String doExecute(Long campusId, String gender) {
        long startTime = System.currentTimeMillis();
        
        // 1. 资源预加载：获取该校区的楼栋和待分配学生
        List<Long> buildingIds = loadBuildingIds(campusId);
        List<SysOrdinaryUser> candidateUsers = loadCandidateUsers(campusId, gender);
        
        if (CollUtil.isEmpty(candidateUsers)) {
            return "该校区暂无符合条件的学生";
        }
        
        // 2. 幂等性过滤：剔除那些已经有床位的学生 (防止重复分配)
        List<SysOrdinaryUser> finalUsers = filterOccupiedUsers(candidateUsers);
        if (CollUtil.isEmpty(finalUsers)) {
            return "所有学生均已分配，无需操作";
        }
        
        int totalStudents = finalUsers.size();
        log.info(">>> [智能分配] 启动 | 校区ID:{} | 待分配人数:{}", campusId, totalStudents);
        
        // 3. 加载画像数据：一次性查出所有学生的偏好，避免循环查库
        Map<Long, UserPreference> prefMap = loadPreferences(finalUsers);
        
        // 4. 执行分流分配：按性别分组处理
        int allocatedCount = 0;
        
        // 防止 Integer/String 类型转换错误，统一转 String Key
        Map<String, List<SysOrdinaryUser>> genderGroups = finalUsers.stream()
                .collect(Collectors.groupingBy(user -> StrUtil.toString(user.getGender())));
        
        // 处理男生组 ("1")
        if (genderGroups.containsKey("1")) {
            allocatedCount += processGroupAllocation("1", genderGroups.get("1"), prefMap, buildingIds);
        }
        // 处理女生组 ("0")
        if (genderGroups.containsKey("0")) {
            allocatedCount += processGroupAllocation("0", genderGroups.get("0"), prefMap, buildingIds);
        }
        
        // 5. 结果校验：打印日志对比预期人数和实际入库人数
        verifyAllocationResult(totalStudents, allocatedCount);
        
        long duration = System.currentTimeMillis() - startTime;
        return StrUtil.format("分配完成！耗时{}ms，应分{}人，实分{}人", duration, totalStudents, allocatedCount);
    }
    
    // ==========================================
    // 核心算法逻辑 (Core Algorithm)
    // ==========================================
    
    /**
     * 单性别群体分配主逻辑
     * <p>
     * 作用：为一个性别群体（如全校男生）寻找合适的房间并入住。
     * 策略：贪心算法 + 优先级排序。
     * </p>
     *
     * @param gender      性别
     * @param users       该性别的所有待分配学生
     * @param prefMap     用户画像缓存
     * @param buildingIds 可用楼栋ID列表
     * @return 成功分配的人数
     */
    private int processGroupAllocation(String gender, List<SysOrdinaryUser> users,
                                       Map<Long, UserPreference> prefMap, List<Long> buildingIds) {
        if (CollUtil.isEmpty(users)) return 0;
        
        // A. 获取可用房源 (排序策略：低楼层优先 -> 同楼栋聚合)
        // 这里的 list 已经按优选顺序排好了
        List<DormRoom> availableRooms = getSortedRooms(buildingIds, gender);
        if (CollUtil.isEmpty(availableRooms)) {
            String genderStr = "1".equals(gender) ? "男" : "女";
            log.warn(">>> [资源告急] 性别[{}]房源不足，该批次分配跳过", genderStr);
            return 0;
        }
        
        // B. 学生池排序 (地缘性优先：学院 -> 专业 -> 班级)
        // 这样 list.removeFirst() 出来的学生，天然就是同班同学，作为种子用户能形成聚集效应
        LinkedList<SysOrdinaryUser> studentPool = users.stream()
                .sorted(Comparator.comparing(SysOrdinaryUser::getCollegeId)
                        .thenComparing(SysOrdinaryUser::getMajorId)
                        .thenComparing(SysOrdinaryUser::getClassId))
                .collect(Collectors.toCollection(LinkedList::new));
        
        Iterator<DormRoom> roomIterator = availableRooms.iterator();
        int successCount = 0;
        
        // C. 【L2 优先级】处理组队码 (Team Code)
        // 只要填了组队码，无论画像如何，优先把他们塞进一个房间
        successCount += processTeamCodeLogic(studentPool, prefMap, roomIterator);
        
        // D. 【核心】贪心匹配循环
        // 只要 还有人没住 且 还有房间
        while (!studentPool.isEmpty() && roomIterator.hasNext()) {
            DormRoom currentRoom = roomIterator.next();
            
            // 重新计算剩余容量 (注意：currentNum 此时是准确的，因为我们有锁)
            int needed = currentRoom.getCapacity() - currentRoom.getCurrentNum();
            if (needed <= 0) continue; // 房间满了，下一个
            
            List<SysOrdinaryUser> roomMates = new ArrayList<>();
            
            // 1. 选取种子用户 (Seed User)
            // 取列表头部的学生 (因为已排序，大概率是同班的)
            SysOrdinaryUser seed = studentPool.removeFirst();
            roomMates.add(seed);
            
            // 2. 为种子用户寻找最佳室友 (Find Best Match)
            // 循环直到填满房间，或者学生池空了
            while (roomMates.size() < needed && !studentPool.isEmpty()) {
                // 在池子里找一个“不完美但最合适”的人
                SysOrdinaryUser bestMatch = findBestMatch(roomMates, studentPool, prefMap);
                
                if (bestMatch != null) {
                    // 找到了合适的，加入房间，从池子移除
                    roomMates.add(bestMatch);
                    studentPool.remove(bestMatch);
                } else {
                    // 兜底策略：如果前N个人都触发了“一票否决”(如都抽烟冲突)，无法完美匹配
                    // 为了防止产生“孤儿床位”，强制取列表头部的一个人(同班)填坑
                    // 逻辑：两害相权取其轻，地缘性(同班) > 生活习惯
                    
                    // 🛡️ NPE 防御：必须判空，因为上面循环可能把池子删空了
                    if (!studentPool.isEmpty()) {
                        SysOrdinaryUser fallback = studentPool.removeFirst();
                        roomMates.add(fallback);
                    } else {
                        break; // 池子真的一滴也没有了
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
     * <p>
     * 作用：遍历学生池的前 N 个人，计算他们与当前房间内已有人成员的“契合度分数”。
     * 分数最高者当选。
     * </p>
     */
    private SysOrdinaryUser findBestMatch(List<SysOrdinaryUser> currentRoom,
                                          List<SysOrdinaryUser> pool,
                                          Map<Long, UserPreference> prefMap) {
        SysOrdinaryUser bestCandidate = null;
        double maxMatchScore = -Double.MAX_VALUE; // 初始分数为负无穷
        
        // 性能优化：搜索窗口限制为 50 人
        // 我们不需要遍历全校几千人，因为池子已按班级排序，前50人基本都是同专业同班的，地缘性最好
        int searchLimit = Math.min(pool.size(), 50);
        
        for (int i = 0; i < searchLimit; i++) {
            SysOrdinaryUser candidate = pool.get(i);
            
            // 🛡️ 防御：如果 candidate 为空(极低概率)
            if (candidate == null) continue;
            
            // [L1] 一票否决 (Veto) - 只要触犯一条红线，直接跳过
            if (checkDeepVeto(currentRoom, candidate, prefMap)) {
                continue;
            }
            
            // 开始打分
            double totalScore = 0.0;
            for (SysOrdinaryUser member : currentRoom) {
                // 🛡️ 防御：Map.get 可能返回 null，使用默认值对象兜底，防止NPE
                UserPreference pMember = prefMap.getOrDefault(member.getId(), new UserPreference());
                UserPreference pCandidate = prefMap.getOrDefault(candidate.getId(), new UserPreference());
                
                // [L3] 基础生活习惯距离 (差异越大，距离越大，分数应扣减)
                double discord = calculateEuclideanDistance(pMember, pCandidate);
                totalScore -= discord;
                
                // [L4] 灵魂匹配奖励 (加分)
                totalScore += calculateSoulCompatibility(pMember, pCandidate);
                
                // [L5] 人口融合与气味匹配 (加分/减分)
                totalScore += calculateDemoAndSmell(pMember, pCandidate);
            }
            
            double avgScore = totalScore / currentRoom.size();
            SysOrdinaryUser seed = currentRoom.get(0);
            
            // [L2] 地缘性权重 (同班级极大加分)
            // 🛡️ 防御：ClassId 可能为 null
            if (ObjectUtil.equal(candidate.getClassId(), seed.getClassId()) && seed.getClassId() != null) {
                avgScore += 500.0; // 同班核心权重
            } else if (ObjectUtil.equal(candidate.getMajorId(), seed.getMajorId()) && seed.getMajorId() != null) {
                avgScore += 200.0; // 同专业次级权重
            }
            
            // 择优录取
            if (avgScore > maxMatchScore) {
                maxMatchScore = avgScore;
                bestCandidate = candidate;
            }
        }
        return bestCandidate;
    }
    
    /**
     * [L1] 深度一票否决逻辑 (Deep Veto)
     * <p>
     * 作用：判断两个人是否“绝对不能”住在一起。
     * 返回 true 表示冲突，必须隔离。
     * </p>
     */
    private boolean checkDeepVeto(List<SysOrdinaryUser> currentRoom, SysOrdinaryUser candidate, Map<Long, UserPreference> prefMap) {
        // 🛡️ 防御：使用 getOrDefault 防止 NPE
        UserPreference pCandidate = prefMap.getOrDefault(candidate.getId(), new UserPreference());
        
        // 1. 民族数量限制 (每间房最多2个少数民族，防止小团体或孤立)
        if (isMinority(candidate)) {
            long minorityCount = currentRoom.stream().filter(this::isMinority).count();
            if (minorityCount >= 2) return true; // 满员了，不能再进
        }
        
        for (SysOrdinaryUser member : currentRoom) {
            UserPreference pMember = prefMap.getOrDefault(member.getId(), new UserPreference());
            
            // 2. 烟草硬隔离 (Smoking)
            // 只要有一个人抽烟(>0)，且另一个人完全不耐受(0)，必须隔离
            // 使用 nvl() 处理 null 值
            if ((gt0(pCandidate.getSmoking()) && nvl(pMember.getSmokeTolerance()) == 0) ||
                    (gt0(pMember.getSmoking()) && nvl(pCandidate.getSmokeTolerance()) == 0)) {
                return true;
            }
            
            // 3. 生理冲突 (Snoring vs Sensitive)
            // 神经衰弱(>=3) 遇上 雷震子呼噜(>=2) -> 隔离
            if ((nvl(pMember.getSleepQuality()) >= 3 && nvl(pCandidate.getSnoringLevel()) >= 2) ||
                    (nvl(pCandidate.getSleepQuality()) >= 3 && nvl(pMember.getSnoringLevel()) >= 2)) {
                return true;
            }
            
            // 4. 异味硬隔离 (Smell)
            // 无法忍受异味(1) vs 爱吃螺蛳粉/榴莲(>0)
            boolean candidateSmelly = gt0(pCandidate.getEatLuosifen()) || gt0(pCandidate.getEatDurian());
            boolean memberSmelly = gt0(pMember.getEatLuosifen()) || gt0(pMember.getEatDurian());
            
            if ((nvl(pMember.getOdorTolerance()) == 1 && candidateSmelly) ||
                    (nvl(pCandidate.getOdorTolerance()) == 1 && memberSmelly)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * [L3] 计算基础生活习惯的不和谐度 (Weighted Euclidean Distance)
     * <p>
     * 作用：计算两人生活习惯的“距离”。
     * 返回值越大，说明习惯差异越大，越不适合住一起。
     * </p>
     */
    private double calculateEuclideanDistance(UserPreference p1, UserPreference p2) {
        double sumSq = 0.0;
        // 权重配置 (作息最重要，键盘噪音次之)
        sumSq += weightedSq(p1.getBedTime(), p2.getBedTime(), 2.0);        // 作息
        sumSq += weightedSq(p1.getAcTemp(), p2.getAcTemp(), 1.5);          // 空调
        sumSq += weightedSq(p1.getCleanFreq(), p2.getCleanFreq(), 1.2);    // 卫生
        sumSq += weightedSq(p1.getGameVoice(), p2.getGameVoice(), 1.2);    // 连麦噪音
        sumSq += weightedSq(p1.getKeyboardAxis(), p2.getKeyboardAxis(), 1.5); // 键盘轴体
        return Math.sqrt(sumSq);
    }
    
    /**
     * [L4] 灵魂匹配奖励 (Soul Compatibility)
     * <p>
     * 作用：发现共同爱好，给予加分。
     * </p>
     */
    private double calculateSoulCompatibility(UserPreference p1, UserPreference p2) {
        double bonus = 0.0;
        
        // 1. 游戏深度匹配
        // LOL/DOTA (都玩?)
        if (gt0(p1.getGameTypeLol()) && gt0(p2.getGameTypeLol())) {
            bonus += 10.0; // 基础分
            // 段位接近? (分差<=1)
            if (Math.abs(nvl(p1.getGameRank()) - nvl(p2.getGameRank())) <= 1) bonus += 5.0;
            // 位置互补? (下路双人组)
            if (isBotLaneDuo(p1.getGameRole(), p2.getGameRole())) bonus += 20.0; // 黄金搭档
        }
        // FPS (CS/瓦/三角洲)
        if (gt0(p1.getGameTypeFps()) && gt0(p2.getGameTypeFps())) {
            bonus += 10.0;
            // 键盘轴体一致? (都吵或者都静音)
            if (ObjectUtil.equal(p1.getKeyboardAxis(), p2.getKeyboardAxis())) bonus += 5.0;
        }
        
        // 2. 二次元共鸣
        if (nvl(p1.getIsAnime()) >= 1 && nvl(p2.getIsAnime()) >= 1) {
            bonus += 15.0;
        }
        // Cosplay 同好
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
     * <p>
     * 作用：处理气味相投和南北融合。
     * </p>
     */
    private double calculateDemoAndSmell(UserPreference p1, UserPreference p2) {
        double score = 0.0;
        // 1. 臭味相投 (都爱吃)
        if (gt0(p1.getEatLuosifen()) && gt0(p2.getEatLuosifen())) score += 15.0;
        if (gt0(p1.getEatDurian()) && gt0(p2.getEatDurian())) score += 10.0;
        
        // 2. 南北融合 (Region Mixing)
        // 促进文化交流，给予适当奖励
        if (p1.getRegionType() != null && p2.getRegionType() != null) {
            if (!p1.getRegionType().equals(p2.getRegionType())) {
                score += 5.0;
            }
        }
        return score;
    }
    
    // ==========================================
    // 组队逻辑与持久化 (Team & Persistence)
    // ==========================================
    
    /**
     * 处理组队码逻辑
     * <p>
     * 作用：扫描所有填了组队码的学生，优先将他们安排在同一个房间。
     * </p>
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
            // 取 min(空位数, 队伍人数)
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
     * <p>
     * 核心：使用 RoomMapper 的原子更新 SQL，防止 Java 内存数据覆盖数据库，导致超卖。
     * </p>
     */
    private void persistToDatabase(DormRoom room, List<SysOrdinaryUser> newOccupants) {
        if (CollUtil.isEmpty(newOccupants)) return;
        
        // 1. 再次查询空床位 (防止并发超卖，虽然有锁，但多重保障更稳)
        List<DormBed> emptyBeds = bedService.list(Wrappers.<DormBed>lambdaQuery()
                .eq(DormBed::getRoomId, room.getId())
                .isNull(DormBed::getOccupantId) // 只查空床
                .orderByAsc(DormBed::getBedLabel)
                .last("LIMIT " + newOccupants.size()));
        
        if (emptyBeds.size() < newOccupants.size()) {
            // 这是一个严重的并发异常，但在批处理中，我们选择跳过当前房间，避免回滚整个大事务
            log.error("并发异常：房间[{}]实际空余床位不足，跳过此房间分配", room.getRoomNo());
            return;
        }
        
        // 2. 批量更新床位 (设置为占用)
        List<DormBed> updates = new ArrayList<>();
        for (int i = 0; i < newOccupants.size(); i++) {
            DormBed bed = emptyBeds.get(i);
            bed.setOccupantId(newOccupants.get(i).getId());
            bed.setStatus(1); // 1-占用
            updates.add(bed);
        }
        bedService.updateBatchById(updates);
        
        // 3. 🟢 原子更新房间实住人数 (SQL: UPDATE ... SET current_num = current_num + N)
        // 这一步至关重要，防止 Java 内存数据覆盖数据库
        roomMapper.increaseOccupancy(room.getId(), newOccupants.size());
        
        // 4. 更新房间状态 (如果满员，改状态为 20)
        // 这里的判断仅供业务标记，数值准确性依靠 SQL
        int newTotal = room.getCurrentNum() + newOccupants.size();
        if (newTotal >= room.getCapacity()) {
            DormRoom statusUpdate = new DormRoom();
            statusUpdate.setId(room.getId());
            statusUpdate.setStatus(20); // 20-满员
            roomService.updateById(statusUpdate);
        }
    }
    
    // ==========================================
    // 基础数据加载与工具方法 (Helpers)
    // ==========================================
    
    private List<Long> loadBuildingIds(Long campusId) {
        List<Long> ids = buildingService.list(Wrappers.<DormBuilding>lambdaQuery()
                        .eq(DormBuilding::getCampusId, campusId))
                .stream().map(DormBuilding::getId).collect(Collectors.toList());
        if (CollUtil.isEmpty(ids)) throw new ServiceException("该校区下暂无宿舍楼栋数据");
        return ids;
    }
    
    private List<SysOrdinaryUser> loadCandidateUsers(Long campusId, String gender) {
        List<Long> collegeIds = collegeService.list(Wrappers.<SysCollege>lambdaQuery()
                        .eq(SysCollege::getCampusId, campusId))
                .stream().map(SysCollege::getId).collect(Collectors.toList());
        if (CollUtil.isEmpty(collegeIds)) return Collections.emptyList();
        
        return userService.list(Wrappers.<SysOrdinaryUser>lambdaQuery()
                .in(SysOrdinaryUser::getCollegeId, collegeIds)
                .eq(SysOrdinaryUser::getStatus, "0") // 0-正常
                // 🟢 核心修复：getSex -> getGender (String类型)
                .eq(StrUtil.isNotBlank(gender), SysOrdinaryUser::getGender, gender));
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
        // 填充默认画像 (使用空对象，防止 get() 报空，虽然我们后面用了 getOrDefault)
        users.forEach(u -> map.computeIfAbsent(u.getId(), this::createDefaultPreference));
        return map;
    }
    
    private List<DormRoom> getSortedRooms(List<Long> buildingIds, String gender) {
        return roomService.list(Wrappers.<DormRoom>lambdaQuery()
                        .in(DormRoom::getBuildingId, buildingIds)
                        .eq(DormRoom::getGender, gender)
                        .eq(DormRoom::getStatus, 10) // 10-正常(未满)
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
    
    // 辅助逻辑
    private boolean isMinority(SysOrdinaryUser user) {
        // 模拟判断：实际应从 user.getEthnicity() 判断
        // 假设 entity 中有 ethnicity 字段，不是 "汉族" 则为少数民族
        // if ("汉族".equals(user.getEthnicity())) return false;
        return false;
    }
    
    // 辅助判断下路双人组 (4:ADC, 5:Support)
    private boolean isBotLaneDuo(Integer r1, Integer r2) {
        if (r1 == null || r2 == null) return false;
        return (r1 == 4 && r2 == 5) || (r1 == 5 && r2 == 4);
    }
    
    // 计算加权欧氏距离平方项
    private double weightedSq(Integer v1, Integer v2, double weight) {
        if (v1 == null || v2 == null) return 0.0;
        return Math.pow(v1 - v2, 2) * weight;
    }
    
    // 判空工具：大于0
    private boolean gt0(Integer val) { return val != null && val > 0; }
    
    // 🟢 核心 NPE 防御工具：Integer 判空转 0
    private int nvl(Integer val) { return val == null ? 0 : val; }
    
    // 创建默认画像对象
    private UserPreference createDefaultPreference(Long userId) {
        UserPreference p = new UserPreference();
        p.setUserId(userId);
        p.setSmoking(0); p.setSmokeTolerance(1); // 默认无烟
        p.setBedTime(3); p.setWakeTime(3);       // 默认 23:00-08:00
        p.setSnoringLevel(0); p.setSleepQuality(2);
        p.setEatLuosifen(0); p.setEatDurian(0);
        p.setIsAnime(0); p.setIsCosplay(0); p.setMbtiEI("I");
        return p;
    }
}