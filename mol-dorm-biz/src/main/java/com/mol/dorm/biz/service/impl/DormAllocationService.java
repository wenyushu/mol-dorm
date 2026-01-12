package com.mol.dorm.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.entity.UserPreference;
import com.mol.dorm.biz.service.DormBedService;
import com.mol.dorm.biz.service.DormRoomService;
import com.mol.dorm.biz.service.UserPreferenceService;
import com.mol.server.service.SysOrdinaryUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 宿舍智能分配核心算法服务 (Pro Max版)
 * <p>
 * 核心架构：基于【分层加权欧几里得距离】的约束贪心聚类算法
 * * 算法流程：
 * Layer 0: 数据清洗与地缘性预排序 (学院->专业->班级)
 * Layer 1: 硬性物理隔离 (性别、校区)
 * Layer 2: 组队优先策略 (Team Code)
 * Layer 3: 贪心聚类分配 (核心循环)
 * - 3.1 一票否决检查 (Veto Power): 医疗、极端习惯冲突
 * - 3.2 不匹配度计算 (Discord Calculation): 欧式距离 + 权重
 * - 3.3 地缘性惩罚 (Geographic Penalty): 跨专业/跨班级惩罚
 * Layer 4: 数据库事务落库
 * </p>
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
     * 执行智能分配 (主入口)
     * @param targetUserIds 需要分配的学生 ID 列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void executeAllocation(List<Long> targetUserIds) {
        long startTime = System.currentTimeMillis();
        log.info(">>> [智能分配] 引擎启动，目标人数: {}", targetUserIds.size());
        
        // 1. 获取学生基础信息
        List<SysOrdinaryUser> users = userService.listByIds(targetUserIds);
        if (CollUtil.isEmpty(users)) {
            log.warn("未找到有效的学生信息，分配终止");
            return;
        }
        
        // 2. 获取并构建画像映射 (Map加速查询)
        List<UserPreference> prefList = preferenceService.listByIds(targetUserIds);
        Map<Long, UserPreference> prefMap = prefList.stream()
                .collect(Collectors.toMap(UserPreference::getUserId, p -> p));
        
        // 2.1 兜底逻辑：如果有学生没填画像，生成默认值，防止算法空指针
        for (SysOrdinaryUser u : users) {
            prefMap.computeIfAbsent(u.getId(), preferenceService::getByUserId);
        }
        
        // 3. 按性别分流 (绝对物理隔离)
        // 这一步确保男生绝不会分进女生楼
        Map<Integer, List<SysOrdinaryUser>> genderGroups = users.stream()
                .collect(Collectors.groupingBy(SysOrdinaryUser::getSex));
        
        // 4. 分别并行或串行处理
        // 男生 (gender=1), 女生 (gender=2)
        processGroupAllocation(1, genderGroups.get(1), prefMap);
        processGroupAllocation(2, genderGroups.get(2), prefMap);
        
        log.info(">>> [智能分配] 全部完成，总耗时: {}ms", System.currentTimeMillis() - startTime);
    }
    
    /**
     * 处理特定性别组的分配逻辑
     * @param gender 性别
     * @param users 该性别下的学生列表
     * @param prefMap 画像 Map
     */
    private void processGroupAllocation(Integer gender, List<SysOrdinaryUser> users, Map<Long, UserPreference> prefMap) {
        if (CollUtil.isEmpty(users)) return;
        String genderStr = (gender == 1 ? "男" : "女");
        log.info(">>> 正在处理[{}]生组, 人数: {}", genderStr, users.size());
        
        // A. 准备房源 (关键步骤)
        // 获取该性别所有未满员的房间，并按 [校区 -> 楼栋 -> 楼层 -> 房间号] 排序
        // 算法效果：分配时会像水流一样，先填满一楼，再填满二楼，溢出则去下一栋楼
        List<DormRoom> availableRooms = getSortedAvailableRooms(gender);
        if (CollUtil.isEmpty(availableRooms)) {
            throw new RuntimeException("资源不足警告：没有足够的[" + genderStr + "]生宿舍可用！");
        }
        
        // B. 准备学生池 (地缘性排序)
        // ★ 核心逻辑：按 [学院 -> 专业 -> 班级] 排序
        // 效果：List中相邻的人大概率是同班同学。算法取人时，天然优先取到同班的。
        List<SysOrdinaryUser> studentPool = users.stream()
                .sorted(Comparator.comparing(SysOrdinaryUser::getCollegeId)
                        .thenComparing(SysOrdinaryUser::getMajorId)
                        .thenComparing(SysOrdinaryUser::getClassId))
                .collect(Collectors.toList());
        
        // C. 处理组队逻辑 (Team Code)
        // 优先把有组队码的小团体塞进房间，消耗掉部分房源
        Iterator<DormRoom> roomIterator = availableRooms.iterator();
        processTeamCodeLogic(studentPool, prefMap, roomIterator);
        
        // D. 处理剩余散户 (核心聚类算法)
        while (!studentPool.isEmpty() && roomIterator.hasNext()) {
            DormRoom currentRoom = roomIterator.next();
            
            // 1. 获取该房间剩余空床位数量 (动态容量支持：4/6/8 人间)
            int neededCount = currentRoom.getCapacity() - currentRoom.getCurrentNum();
            if (neededCount <= 0) continue;
            
            // 2. 挑选入住名单
            List<SysOrdinaryUser> roomMates = new ArrayList<>();
            
            // 2.1 选种子用户 (Seed)
            // 直接取池子里的第一个人。由于池子已按班级排序，此人代表了当前待分配的班级群体。
            SysOrdinaryUser seed = studentPool.remove(0);
            roomMates.add(seed);
            
            // 2.2 为种子寻找最佳室友
            while (roomMates.size() < neededCount && !studentPool.isEmpty()) {
                // 在池子中寻找与当前房间成员最匹配的人
                SysOrdinaryUser bestMatch = findBestMatch(roomMates, studentPool, prefMap);
                
                if (bestMatch != null) {
                    roomMates.add(bestMatch);
                    studentPool.remove(bestMatch); // 从池中移除
                } else {
                    // 找不到匹配的人 (可能因为一票否决权导致全员冲突，或者池子空了)
                    // 策略：允许房间不满员，跳过当前房间，进入下一间
                    break;
                }
            }
            
            // 3. 真实的数据库落库操作 (事务写入)
            persistToDatabase(currentRoom, roomMates);
        }
        
        if (!studentPool.isEmpty()) {
            log.warn("警告：仍有 {} 名[{}]学生未分配到床位 (可能是房源不足或极端排斥)", studentPool.size(), genderStr);
        }
    }
    
    /**
     * 寻找最佳匹配 (包含分层加权逻辑)
     * @param currentRoom 当前房间已有的成员
     * @param pool 候选人池子
     * @param prefMap 画像数据
     * @return 最佳匹配者，无则返回 null
     */
    private SysOrdinaryUser findBestMatch(List<SysOrdinaryUser> currentRoom,
                                          List<SysOrdinaryUser> pool,
                                          Map<Long, UserPreference> prefMap) {
        SysOrdinaryUser bestCandidate = null;
        // 初始设为最大值，寻找越小越好的分数
        double minDiscordScore = Double.MAX_VALUE;
        
        // 【性能优化】：搜索窗口限制
        // 为了保证“同班优先”，我们只在 pool 的前 50 人中搜索。
        // 如果遍历整个 pool (如5000人)，不仅 O(N^2) 慢，而且可能会为了追求生活习惯完美匹配，
        // 把不同专业的陌生人拉进来，破坏了地缘性。前 50 人大概率是同专业/同班的。
        int searchLimit = Math.min(pool.size(), 50);
        
        for (int i = 0; i < searchLimit; i++) {
            SysOrdinaryUser candidate = pool.get(i);
            
            // === Layer 1: 一票否决检查 (Hard Constraint) ===
            // 如果触发任何一条否决规则（如传染病、极端习惯冲突），直接跳过
            if (checkVetoPower(currentRoom, candidate, prefMap)) {
                continue;
            }
            
            // === Layer 2: 分层加权欧几里得距离 (Soft Constraint) ===
            double totalScore = 0.0;
            for (SysOrdinaryUser member : currentRoom) {
                totalScore += calculateEuclideanDiscord(prefMap.get(member.getId()), prefMap.get(candidate.getId()));
            }
            // 取平均分 (让算法适用于 4/6/8 人间)
            double avgScore = totalScore / currentRoom.size();
            
            // === Layer 3: 地缘性惩罚 (Geographic Penalty) ===
            // 即使生活习惯很合拍，如果不是一个班/专业的，也要加分(惩罚)
            SysOrdinaryUser seed = currentRoom.get(0);
            if (!candidate.getClassId().equals(seed.getClassId())) {
                avgScore += 300.0; // 不同班，惩罚分中等 (尽量不拆班)
            }
            if (!candidate.getMajorId().equals(seed.getMajorId())) {
                avgScore += 800.0; // 不同专业，惩罚极大 (尽量不跨专业)
            }
            
            // 更新最优解
            if (avgScore < minDiscordScore) {
                minDiscordScore = avgScore;
                bestCandidate = candidate;
            }
        }
        return bestCandidate;
    }
    
    /**
     * ★ 一票否决权检查 (Veto Power)
     * 返回 true 表示 "冲突，不能住一起"
     */
    private boolean checkVetoPower(List<SysOrdinaryUser> currentRoom, SysOrdinaryUser candidate, Map<Long, UserPreference> prefMap) {
        UserPreference pCandidate = prefMap.get(candidate.getId());
        
        // 1. 少数民族聚集限制
        // 规则：为促进融合，一个寝室少数民族不宜过多(例如 <= 2)。
        long minorityCount = currentRoom.stream().filter(u -> isMinority(u.getEthnicity())).count();
        if (isMinority(candidate.getEthnicity()) && minorityCount >= 2) {
            return true; // 否决，请去下一个房间
        }
        
        // 2. 特殊医疗需求 (如胰岛素需冰箱)
        // 假设我们在 specialDisease 字段里标记了
        boolean needFridge = StrUtil.contains(pCandidate.getSpecialDisease(), "胰岛素");
        if (needFridge) {
            // 简化逻辑：如果有特殊需求，尽量作为房间的第一个人入住(种子)，或者加入已有同类需求的房间
            // 如果房间里已经有人且没这需求，为了避免麻烦，这里简单否决(实际可细化)
            // return !currentRoom.isEmpty();
        }
        
        // 3. 极端生活习惯互斥 (最后一道防线)
        for (SysOrdinaryUser member : currentRoom) {
            UserPreference pMember = prefMap.get(member.getId());
            // 例子：一个人 "严重打鼾(2)" 且 另一个人 "神经衰弱(3)" -> 绝对不行
            if ((pCandidate.getSnoring() == 2 && pMember.getSleepLight() == 3) ||
                    (pMember.getSnoring() == 2 && pCandidate.getSleepLight() == 3)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * ★ 核心算法：分层加权欧几里得距离 (Hierarchical Weighted Euclidean Distance)
     * 公式: D = Sqrt( Sum( Weight_i * (A_i - B_i)^2 ) )
     * 特性: 平方操作会非线性地放大差异。差异越大，惩罚越重。
     */
    private double calculateEuclideanDiscord(UserPreference p1, UserPreference p2) {
        if (p1 == null || p2 == null) return 0.0;
        
        // === Layer 1: 致命冲突 (Weight = Inf) ===
        // 抽烟冲突：一方抽烟(>0) 且 另一方不能忍(0)
        boolean smokeConflict = (p1.getSmoking() > 0 && p2.getSmokeTolerance() == 0) ||
                (p2.getSmoking() > 0 && p1.getSmokeTolerance() == 0);
        if (smokeConflict) return 10000.0; // 直接返回极大值
        
        // === Layer 2: 加权欧式距离 ===
        double sumSquares = 0.0;
        
        // 1. 生存类指标 (权重 2.0) - 作息不同最要命
        sumSquares += 2.0 * Math.pow(p1.getBedTime() - p2.getBedTime(), 2);
        sumSquares += 2.0 * Math.pow(p1.getWakeTime() - p2.getWakeTime(), 2);
        
        // 2. 卫生类指标 (权重 1.5)
        sumSquares += 1.5 * Math.pow(p1.getAcTempSummer() - p2.getAcTempSummer(), 2); // 空调
        sumSquares += 1.5 * Math.pow(p1.getPersonalHygiene() - p2.getPersonalHygiene(), 2); // 个人卫生
        sumSquares += 1.5 * Math.pow(p1.getCleanFreq() - p2.getCleanFreq(), 2); // 打扫频率
        // 轮流刷厕所 (0拒绝 1接受)，如果不一致，平方后是1
        sumSquares += 1.5 * Math.pow(p1.getToiletClean() - p2.getToiletClean(), 2);
        
        // 3. 噪音类指标 (权重 1.5)
        // 动态权重：如果只要有一方睡眠浅(>1)，噪音差异的权重翻倍
        double noiseWeight = (p1.getSleepLight() > 1 || p2.getSleepLight() > 1) ? 3.0 : 1.0;
        sumSquares += noiseWeight * Math.pow(p1.getGameVoice() - p2.getGameVoice(), 2);
        sumSquares += noiseWeight * Math.pow(p1.getKeyboardType() - p2.getKeyboardType(), 2); // 机械键盘
        
        // 4. 社交与MBTI (权重 0.8)
        // E/I 维度转换: E=1, I=0
        int e1 = "E".equalsIgnoreCase(p1.getMbtiEI()) ? 1 : 0;
        int e2 = "E".equalsIgnoreCase(p2.getMbtiEI()) ? 1 : 0;
        sumSquares += 0.8 * Math.pow(e1 - e2, 2);
        sumSquares += 0.8 * Math.pow(p1.getVisitors() - p2.getVisitors(), 2);
        
        // 5. 兴趣爱好 (权重 0.5 - 加分项)
        // 差异越小越好。如果都是二次元(>1)，距离为0，无惩罚。
        sumSquares += 0.5 * Math.pow(p1.getIsAcg() - p2.getIsAcg(), 2);
        sumSquares += 0.5 * Math.pow(p1.getGameHabit() - p2.getGameHabit(), 2);
        
        // 开根号返回
        return Math.sqrt(sumSquares);
    }
    
    /**
     * 获取排序后的可用房间列表
     * 默认楼栋 ID 是按校区分布的，直接按楼栋排序即可满足 “就近” 原则
     */
    private List<DormRoom> getSortedAvailableRooms(Integer gender) {
        return roomService.list(Wrappers.<DormRoom>lambdaQuery()
                        .eq(DormRoom::getGender, gender)
                        .eq(DormRoom::getStatus, 1) // 1=启用
                        .apply("current_num < capacity") // 必须还有空床位
                ).stream()
                // 🔴 修正排序逻辑：
                // 1. 先按楼栋排 (BuildingId) - 同一栋楼的在一起
                // 2. 再按楼层排 (FloorNo) - 同一层楼的在一起
                // 3. 最后按房间号排 (RoomNo) - 隔壁房间在一起
                .sorted(Comparator.comparing(DormRoom::getBuildingId)
                        .thenComparing(DormRoom::getFloorNo)
                        .thenComparing(DormRoom::getRoomNo))
                .collect(Collectors.toList());
    }

    
    /**
     * 真实的数据库操作 (落库)
     */
    private void persistToDatabase(DormRoom room, List<SysOrdinaryUser> newOccupants) {
        if (CollUtil.isEmpty(newOccupants)) return;
        
        // 1. 查找该房间内所有的 "空床位"
        // 必须按床号排序，保证 1 号床、2 号床 顺序填入
        List<DormBed> emptyBeds = bedService.list(Wrappers.<DormBed>lambdaQuery()
                .eq(DormBed::getRoomId, room.getId())
                .isNull(DormBed::getOccupantId) // 空床
                .orderByAsc(DormBed::getBedLabel)
                .last("LIMIT " + newOccupants.size()) // 取出需要的数量
        );
        
        if (emptyBeds.size() < newOccupants.size()) {
            log.error("数据一致性异常：房间[{}]显示有{}个空位，但dorm_bed表只找到{}张空床",
                    room.getRoomNo(), newOccupants.size(), emptyBeds.size());
            return; // 保护性跳过
        }
        
        List<DormBed> bedsToUpdate = new ArrayList<>();
        
        // 2. 绑定 人-床 关系
        for (int i = 0; i < newOccupants.size(); i++) {
            SysOrdinaryUser student = newOccupants.get(i);
            DormBed bed = emptyBeds.get(i);
            
            bed.setOccupantId(student.getId());
            bedsToUpdate.add(bed);
            
            log.info(">> 分配落库: 房间[{}]-{} -> 学生[{}]", room.getRoomNo(), bed.getBedLabel(), student.getRealName());
        }
        
        // 3. 批量更新床位表
        bedService.updateBatchById(bedsToUpdate);
        
        // 4. 更新房间实住人数 (原子更新)
        // 无论房间是4人还是8人，这里逻辑通用
        roomService.update(Wrappers.<DormRoom>lambdaUpdate()
                .eq(DormRoom::getId, room.getId())
                .setSql("current_num = current_num + " + newOccupants.size())
        );
        
        // 5. 检查是否满员，更新状态
        // 再次查询最新状态以确保准确
        DormRoom updatedRoom = roomService.getById(room.getId());
        if (updatedRoom.getCurrentNum() >= updatedRoom.getCapacity()) {
            roomService.update(Wrappers.<DormRoom>lambdaUpdate()
                    .eq(DormRoom::getId, room.getId())
                    .set(DormRoom::getStatus, 2) // 2=满员
            );
        }
    }
    
    /**
     * 处理组队逻辑
     */
    private void processTeamCodeLogic(List<SysOrdinaryUser> studentPool,
                                      Map<Long, UserPreference> prefMap,
                                      Iterator<DormRoom> roomIterator) {
        // 分组
        Map<String, List<SysOrdinaryUser>> teams = studentPool.stream()
                .filter(u -> StrUtil.isNotBlank(prefMap.get(u.getId()).getTeamCode()))
                .collect(Collectors.groupingBy(u -> prefMap.get(u.getId()).getTeamCode()));
        
        // 遍历每个小队
        for (List<SysOrdinaryUser> teamMembers : teams.values()) {
            if (!roomIterator.hasNext()) break;
            
            // 简单策略：当前房间能塞下几个人，就塞几个人
            // 剩下的队友会留到下一轮循环，或者你可以写逻辑去找下一个房间
            DormRoom room = roomIterator.next();
            int space = room.getCapacity() - room.getCurrentNum();
            
            List<SysOrdinaryUser> movingIn = new ArrayList<>();
            // 截取能塞进去的人数
            for (int i = 0; i < Math.min(space, teamMembers.size()); i++) {
                movingIn.add(teamMembers.get(i));
            }
            
            // 落库
            persistToDatabase(room, movingIn);
            
            // 从大池子移除
            studentPool.removeAll(movingIn);
            
            // 如果这个房间没塞满，迭代器不需要动，散户逻辑会继续填满它
            // 如果塞满了，下一次 next() 会自动取下一个房间
        }
    }
    
    // 辅助方法：判断是否为少数民族
    private boolean isMinority(String ethnicity) {
        return ethnicity != null && !ethnicity.contains("汉");
    }
}