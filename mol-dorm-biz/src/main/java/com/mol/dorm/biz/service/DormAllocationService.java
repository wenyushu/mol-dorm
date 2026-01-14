package com.mol.dorm.biz.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.entity.UserPreference;
import com.mol.server.service.SysOrdinaryUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 宿舍智能分配核心算法服务 (Pro版)
 * <p>
 * 本算法采用 "基于约束满足的贪心策略 (Constraint Satisfaction Greedy)"，
 * 结合 "加权欧几里得距离 (Weighted Euclidean Distance)" 进行相似度计算。
 * </p>
 *
 * <h3>核心防刁民机制 (Deep Anti-Troll):</h3>
 * <ul>
 * <li><strong>一级防御 (硬性隔离):</strong> 性别、吸烟习惯、极端作息差异。</li>
 * <li><strong>二级防御 (生理冲突):</strong> 浅睡眠者 vs 打呼噜/磨牙者。</li>
 * <li><strong>三级防御 (环境冲突):</strong> 空调时长偏好 (整晚 vs 定时)。</li>
 * <li><strong>四级防御 (领地冲突):</strong> 社恐 vs 频繁带客者。</li>
 * </ul>
 *
 * @author mol
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DormAllocationService {
    
    private final SysOrdinaryUserService userService;
    private final UserPreferenceService preferenceService;
    private final DormRoomService roomService;
    private final DormBedService bedService;
    
    /**
     * 执行智能分配的主入口
     * 使用 synchronized 锁住方法，防止多管理员并发点击导致超卖或分配冲突
     *
     * @param targetUserIds 待分配的学生ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void executeAllocation(List<Long> targetUserIds) {
        long startTime = System.currentTimeMillis();
        
        // ---------------------------------------------------------
        // 1. 幂等性与前置校验 (Idempotency Check)
        // ---------------------------------------------------------
        // 目的：防止管理员手抖点了两次，或者列表中混入了已经有床位的学生
        List<Long> alreadyAssignedIds = bedService.list(Wrappers.<DormBed>lambdaQuery()
                .in(DormBed::getOccupantId, targetUserIds)
                .isNotNull(DormBed::getOccupantId)
        ).stream().map(DormBed::getOccupantId).toList();
        
        List<Long> finalUserIds = new ArrayList<>(targetUserIds);
        if (!alreadyAssignedIds.isEmpty()) {
            log.warn(">>> [智能分配] 自动剔除 {} 名已有床位的学生，防止重复分配", alreadyAssignedIds.size());
            finalUserIds.removeAll(alreadyAssignedIds);
        }
        
        if (finalUserIds.isEmpty()) {
            throw new ServiceException("所选学生均已分配床位，系统无需操作");
        }
        
        log.info(">>> [智能分配] 引擎启动，实际待分配人数: {}", finalUserIds.size());
        
        // ---------------------------------------------------------
        // 2. 数据加载与预处理 (Data Loading)
        // ---------------------------------------------------------
        List<SysOrdinaryUser> users = userService.listByIds(finalUserIds);
        if (CollUtil.isEmpty(users)) return;
        
        // 加载用户画像偏好
        List<UserPreference> prefList = preferenceService.listByIds(finalUserIds);
        Map<Long, UserPreference> prefMap = prefList.stream()
                .collect(Collectors.toMap(UserPreference::getUserId, p -> p));
        
        // 兜底逻辑：如果学生没填画像，生成一个"中庸"的默认画像，防止算法空指针异常
        for (SysOrdinaryUser u : users) {
            prefMap.computeIfAbsent(u.getId(), this::createDefaultPreference);
        }
        
        // ---------------------------------------------------------
        // 3. 核心分流 (Core Flow)
        // ---------------------------------------------------------
        // 将学生按性别分组，男生分男寝，女生分女寝，互不干扰
        Map<Integer, List<SysOrdinaryUser>> genderGroups = users.stream()
                .collect(Collectors.groupingBy(SysOrdinaryUser::getSex));
        
        if (genderGroups.containsKey(1)) {
            processGroupAllocation(1, genderGroups.get(1), prefMap);
        }
        if (genderGroups.containsKey(2)) {
            processGroupAllocation(2, genderGroups.get(2), prefMap);
        }
        
        log.info(">>> [智能分配] 全部完成，总耗时: {}ms", System.currentTimeMillis() - startTime);
    }
    
    /**
     * 处理单性别群体的分配逻辑
     *
     * @param gender 性别 (1男 2女)
     * @param users  该性别的学生列表
     * @param prefMap 画像字典
     */
    private void processGroupAllocation(Integer gender, List<SysOrdinaryUser> users, Map<Long, UserPreference> prefMap) {
        if (CollUtil.isEmpty(users)) return;
        String genderStr = (gender == 1 ? "男" : "女");
        
        // Step A: 获取可用房源
        // 排序策略：楼栋 -> 楼层 -> 房间号。这样能保证同一个班级的学生优先填满低楼层，形成聚集效应。
        List<DormRoom> availableRooms = getSortedAvailableRooms(gender);
        if (CollUtil.isEmpty(availableRooms)) {
            throw new ServiceException("资源严重不足：系统未找到可用的[" + genderStr + "]生宿舍！");
        }
        
        // Step B: 准备学生池 (Student Pool)
        // 排序策略：学院 -> 专业 -> 班级。保证“地缘性”，同班同学排在一起，作为种子用户优先入驻。
        LinkedList<SysOrdinaryUser> studentPool = users.stream()
                .sorted(Comparator.comparing(SysOrdinaryUser::getCollegeId)
                        .thenComparing(SysOrdinaryUser::getMajorId)
                        .thenComparing(SysOrdinaryUser::getClassId))
                .collect(Collectors.toCollection(LinkedList::new));
        
        Iterator<DormRoom> roomIterator = availableRooms.iterator();
        
        // Step C: 【优先】处理组队逻辑 (Team Code)
        // 拥有相同 TeamCode 的人，说明他们在现实中已经商量好了，算法必须无条件满足（优先级最高）
        processTeamCodeLogic(studentPool, prefMap, roomIterator);
        
        // Step D: 【核心】处理散户匹配 (Greedy Strategy)
        while (!studentPool.isEmpty() && roomIterator.hasNext()) {
            DormRoom currentRoom = roomIterator.next();
            
            // 计算该房间还能住几人
            int neededCount = currentRoom.getCapacity() - currentRoom.getCurrentNum();
            if (neededCount <= 0) continue;
            
            List<SysOrdinaryUser> roomMates = new ArrayList<>();
            
            // 1. 选取种子用户 (Seed User)
            // 取列表第一个人作为基准。因为列表已按班级排序，所以基准通常是同班同学。
            SysOrdinaryUser seed = studentPool.removeFirst();
            roomMates.add(seed);
            
            // 2. 为种子用户寻找最匹配的室友 (Fill the room)
            while (roomMates.size() < neededCount && !studentPool.isEmpty()) {
                SysOrdinaryUser bestMatch = findBestMatch(roomMates, studentPool, prefMap);
                
                if (bestMatch != null) {
                    roomMates.add(bestMatch);
                    studentPool.remove(bestMatch); // 从池子中移除已选中的人
                } else {
                    // 情况：池子里剩下的人都和当前房间的人有“一票否决”冲突
                    // 策略：为了不让房间空着，尝试强制塞入一个冲突最小的（Break Veto）
                    // 实际操作：简单起见，这里跳出当前房间循环，留空该床位，等待后续人工处理或下一轮
                    // 或者：这里演示强制分配同班同学（Fallback）
                    if (!studentPool.isEmpty()) {
                        // 实在没得选，优先塞同班的，避免无限循环
                        SysOrdinaryUser fallback = studentPool.removeFirst();
                        roomMates.add(fallback);
                    } else {
                        break;
                    }
                }
            }
            
            // 3. 落库保存
            persistToDatabase(currentRoom, roomMates);
        }
        
        // Step E: 异常报告
        if (!studentPool.isEmpty()) {
            log.warn("警告：仍有 {} 名[{}]学生因房源不足未分配到床位", studentPool.size(), genderStr);
            // 实际上线时不建议抛异常回滚，而是记录日志提示管理员手动分配剩余人员
            // 这里为了毕设演示严谨性，选择抛出
            throw new ServiceException(genderStr + "生宿舍房源不足，分配中断，剩余 " + studentPool.size() + " 人");
        }
    }
    
    /**
     * 核心算法：寻找最佳室友
     * 遍历池中前N个人，计算与当前室友的平均"不和谐度"，选最低的。
     * * @param currentRoom 当前房间已有的成员
     * @param pool 待分配的学生池
     * @param prefMap 画像数据
     */
    private SysOrdinaryUser findBestMatch(List<SysOrdinaryUser> currentRoom,
                                          List<SysOrdinaryUser> pool,
                                          Map<Long, UserPreference> prefMap) {
        SysOrdinaryUser bestCandidate = null;
        double minDiscordScore = Double.MAX_VALUE;
        
        // 搜索窗口限制：为了性能，只看池子头部的前50个人（大概率是同班或隔壁班）
        // 如果全校混选，这里可以设大一点
        int searchLimit = Math.min(pool.size(), 50);
        
        for (int i = 0; i < searchLimit; i++) {
            SysOrdinaryUser candidate = pool.get(i);
            
            // --- 1. 深层一票否决检查 (Deep Anti-Troll) ---
            if (checkDeepVetoPower(currentRoom, candidate, prefMap)) {
                continue; // 触发否决，直接跳过此人，寻找下一个
            }
            
            // --- 2. 计算不和谐度分数 (越高越不合) ---
            double totalScore = 0.0;
            for (SysOrdinaryUser member : currentRoom) {
                totalScore += calculateWeightedEuclideanDiscord(
                        prefMap.get(member.getId()),
                        prefMap.get(candidate.getId())
                );
            }
            double avgScore = totalScore / currentRoom.size();
            
            // --- 3. 地缘性惩罚 (Geographic Penalty) ---
            // 如果不是同一个班级，分数 +300。
            // 这意味着：我们宁愿容忍一点点生活习惯差异，也要优先把同班同学分在一起。
            SysOrdinaryUser seed = currentRoom.get(0);
            if (!candidate.getClassId().equals(seed.getClassId())) {
                avgScore += 300.0;
            }
            
            // --- 4. 择优 ---
            if (avgScore < minDiscordScore) {
                minDiscordScore = avgScore;
                bestCandidate = candidate;
            }
        }
        return bestCandidate;
    }
    
    /**
     * 【防刁民设计核心】深层一票否决逻辑
     * @return true 表示 "绝对不能住一起"
     */
    private boolean checkDeepVetoPower(List<SysOrdinaryUser> currentRoom, SysOrdinaryUser candidate, Map<Long, UserPreference> prefMap) {
        UserPreference pCandidate = prefMap.get(candidate.getId());
        if (pCandidate == null) return false;
        
        for (SysOrdinaryUser member : currentRoom) {
            UserPreference pMember = prefMap.get(member.getId());
            if (pMember == null) continue;
            
            // === 1. 烟草隔离 (Smoke Firewall) ===
            // 只要有一个人抽烟(>0)，且另一个人完全不耐受(0)，必须隔离
            if ((pCandidate.getSmoking() > 0 && pMember.getSmokeTolerance() == 0) ||
                    (pMember.getSmoking() > 0 && pCandidate.getSmokeTolerance() == 0)) {
                return true;
            }
            
            // === 2. 生理冲突 (Physiological Conflict) ===
            // 浅睡眠者(3或4) vs 呼噜震天响(2或3)
            // 这是一个非常严重的冲突，必须避免
            if ((pMember.getSleepQuality() >= 3 && pCandidate.getSnoringLevel() >= 2) ||
                    (pCandidate.getSleepQuality() >= 3 && pMember.getSnoringLevel() >= 2)) {
                return true;
            }
            
            // === 3. 空调战争 (AC War) ===
            // 一个要整晚开(1)，一个要定时关(2)，且温度偏好差超过5度
            // 这种组合100%会吵架
            if (pCandidate.getAcDuration() != null && pMember.getAcDuration() != null) {
                if (!pCandidate.getAcDuration().equals(pMember.getAcDuration()) &&
                        Math.abs(pCandidate.getAcTemp() - pMember.getAcTemp()) > 5) {
                    return true;
                }
            }
            
            // === 4. 领地意识冲突 (Territory Conflict) ===
            // 绝不接受访客(0) vs 经常带人(2)或带异性(3)
            // 社恐 vs 社牛的极端体现
            if ((pMember.getBringGuest() == 0 && pCandidate.getBringGuest() >= 2) ||
                    (pCandidate.getBringGuest() == 0 && pMember.getBringGuest() >= 2)) {
                return true;
            }
            
            // === 5. 作息极端冲突 (Schedule Clash) ===
            // 晚睡差异超过 3 个档位 (例如: 21点睡 vs 凌晨1点睡)
            if (Math.abs(pCandidate.getBedTime() - pMember.getBedTime()) >= 3) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 【数学核心】计算两个人的不和谐度 (加权欧几里得距离)
     * 公式: sqrt( sum( (x1-x2)^2 * weight ) )
     * 分数越低越匹配
     */
    private double calculateWeightedEuclideanDiscord(UserPreference p1, UserPreference p2) {
        if (p1 == null || p2 == null) return 500.0; // 缺失数据的默认惩罚
        
        double sumSq = 0.0;
        
        // 1. 作息维度 (权重: 2.0 - 最高)
        // 晚归频率: 经常晚归 vs 从不
        sumSq += weightedSq(p1.getOutLateFreq(), p2.getOutLateFreq(), 2.0);
        // 起床/睡觉时间
        sumSq += weightedSq(p1.getBedTime(), p2.getBedTime(), 2.0);
        sumSq += weightedSq(p1.getWakeTime(), p2.getWakeTime(), 1.5);
        
        // 2. 卫生维度 (权重: 1.2 - 中)
        sumSq += weightedSq(p1.getCleanFreq(), p2.getCleanFreq(), 1.2);
        sumSq += weightedSq(p1.getTrashHabit(), p2.getTrashHabit(), 1.0);
        sumSq += weightedSq(p1.getPersonalHygiene(), p2.getPersonalHygiene(), 1.0);
        
        // 3. 噪音与干扰 (权重: 1.5 - 中高)
        // 键盘轴体: 青轴(3) vs 静音(1) 差异很大，必须加重权
        sumSq += weightedSq(p1.getKeyboardAxis(), p2.getKeyboardAxis(), 1.5);
        // 连麦音量
        sumSq += weightedSq(p1.getGameVoice(), p2.getGameVoice(), 1.2);
        
        // 4. 性格维度 (MBTI 简单匹配 - 权重: 0.8 - 低)
        // 社交意愿差异
        sumSq += weightedSq(p1.getSocialBattery(), p2.getSocialBattery(), 0.8);
        
        return Math.sqrt(sumSq);
    }
    
    /**
     * 辅助计算：加权平方差
     */
    private double weightedSq(Integer v1, Integer v2, double weight) {
        if (v1 == null || v2 == null) return 0.0; // 如果数据缺失，暂不计入惩罚
        double diff = v1 - v2;
        return Math.pow(diff, 2) * weight; // 差的平方 * 权重
    }
    
    /**
     * 数据库持久化操作：将学生分配到指定房间的床位
     */
    private void persistToDatabase(DormRoom room, List<SysOrdinaryUser> newOccupants) {
        if (CollUtil.isEmpty(newOccupants)) return;
        
        // 1. 找该房间的空床位
        List<DormBed> emptyBeds = bedService.list(Wrappers.<DormBed>lambdaQuery()
                .eq(DormBed::getRoomId, room.getId())
                .isNull(DormBed::getOccupantId) // 找occupant_id为NULL的
                .orderByAsc(DormBed::getBedLabel)
                .last("LIMIT " + newOccupants.size())
        );
        
        if (emptyBeds.size() < newOccupants.size()) {
            throw new ServiceException(StrUtil.format(
                    "分配并发异常：房间[{}]实际空余床位({})少于预期({})，事务已自动回滚。",
                    room.getRoomNo(), emptyBeds.size(), newOccupants.size()));
        }
        
        // 2. 更新床位表
        List<DormBed> bedsToUpdate = new ArrayList<>();
        for (int i = 0; i < newOccupants.size(); i++) {
            SysOrdinaryUser student = newOccupants.get(i);
            DormBed bed = emptyBeds.get(i);
            bed.setOccupantId(student.getId());
            bedsToUpdate.add(bed);
            
            log.info(">> 成功分配: 房间[{}]-{} -> 学生[{}]", room.getRoomNo(), bed.getBedLabel(), student.getRealName());
        }
        bedService.updateBatchById(bedsToUpdate);
        
        // 3. 更新房间实住人数
        roomService.update(Wrappers.<DormRoom>lambdaUpdate()
                .eq(DormRoom::getId, room.getId())
                .setSql("current_num = current_num + " + newOccupants.size())
        );
    }
    
    /**
     * 优先处理组队码逻辑 (Team Code)
     */
    private void processTeamCodeLogic(List<SysOrdinaryUser> studentPool,
                                      Map<Long, UserPreference> prefMap,
                                      Iterator<DormRoom> roomIterator) {
        // 按组队码分组
        Map<String, List<SysOrdinaryUser>> teams = studentPool.stream()
                .filter(u -> {
                    UserPreference p = prefMap.get(u.getId());
                    return p != null && StrUtil.isNotBlank(p.getTeamCode());
                })
                .collect(Collectors.groupingBy(u -> prefMap.get(u.getId()).getTeamCode()));
        
        // 遍历每个小队
        for (Map.Entry<String, List<SysOrdinaryUser>> entry : teams.entrySet()) {
            List<SysOrdinaryUser> teamMembers = entry.getValue();
            if (teamMembers.size() < 2) continue; // 只有1个人的不算组队，回退到散户池
            
            if (!roomIterator.hasNext()) {
                log.warn(">>> 房源耗尽，停止组队分配");
                break;
            }
            
            // 找一个能塞下这个小队的房间
            DormRoom room = roomIterator.next();
            int space = room.getCapacity() - room.getCurrentNum();
            
            // 简单的组队逻辑：直接塞入当前房间，塞不下就拆分(实际业务可能需要搜索空房)
            List<SysOrdinaryUser> movingIn = new ArrayList<>();
            for (int i = 0; i < Math.min(space, teamMembers.size()); i++) {
                movingIn.add(teamMembers.get(i));
            }
            
            persistToDatabase(room, movingIn);
            
            // 从散户池中移除这些已分配的人，防止重复分配
            studentPool.removeAll(movingIn);
            log.info(">>> 组队分配成功: 队伍[{}] {}人 入住 {}", entry.getKey(), movingIn.size(), room.getRoomNo());
        }
    }
    
    /**
     * 生成默认画像 (针对没填表的懒学生)
     */
    private UserPreference createDefaultPreference(Long userId) {
        UserPreference p = new UserPreference();
        p.setUserId(userId);
        p.setSmoking(0); // 默认不抽烟
        p.setSmokeTolerance(1); // 默认能忍
        p.setBedTime(3); // 默认23点睡
        p.setWakeTime(3); // 默认8点起
        p.setCleanFreq(2); // 每周打扫
        p.setPersonalHygiene(3); // 卫生普通
        p.setAcTemp(26);
        p.setAcDuration(1); // 整晚
        p.setKeyboardAxis(1); // 默认静音键盘
        p.setBringGuest(0); // 默认不带人
        p.setSnoringLevel(0); // 默认不打呼
        p.setSleepQuality(2); // 睡眠质量普通
        return p;
    }
    
    /**
     * 获取按优先级排序的可用房源
     */
    private List<DormRoom> getSortedAvailableRooms(Integer gender) {
        return roomService.list(Wrappers.<DormRoom>lambdaQuery()
                        .eq(DormRoom::getGender, gender)
                        .eq(DormRoom::getStatus, 1) // 启用状态
                        .apply("current_num < capacity") // 还有空位
                ).stream()
                // 排序优化：先填满低楼层，再填高楼层 (模拟真实生活偏好)
                .sorted(Comparator.comparing(DormRoom::getBuildingId)
                        .thenComparing(DormRoom::getFloorNo)
                        .thenComparing(DormRoom::getRoomNo))
                .collect(Collectors.toList());
    }
}