package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.dorm.biz.MolDormApplication;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.DormBuilding;
import com.mol.dorm.biz.entity.DormFloor;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.entity.UserPreference;
import com.mol.server.service.SysCampusService;
import com.mol.server.service.SysOrdinaryUserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 宿舍智能分配算法集成测试
 * <p>
 * 包含：
 * 1. 物理删除清理数据 (JdbcTemplate)
 * 2. 基础设施构建 (校区-楼-层-房-床)
 * 3. 核心算法验证 (组队优先、兴趣匹配、防刁民隔离)
 * </p>
 */
@SpringBootTest(classes = MolDormApplication.class)
@ActiveProfiles("dorm")
public class DormAllocationTest {
    
    @Autowired private DormAllocationService allocationService;
    @Autowired private SysOrdinaryUserService userService;
    @Autowired private UserPreferenceService preferenceService;
    @Autowired private DormRoomService roomService;
    @Autowired private DormBedService bedService;
    @Autowired private DormBuildingService buildingService;
    @Autowired private DormFloorService floorService;
    @Autowired private SysCampusService campusService;
    
    // 注入 JdbcTemplate 进行暴力物理删除
    @Autowired private JdbcTemplate jdbcTemplate;
    
    // --- 模拟角色 ID ---
    private final Long idSmoker = 1001L; // 刁民1: 室内抽烟
    private final Long idHater = 1002L;  // 刁民2: 厌恶烟味 (这两人绝对不能在一起)
    
    private final Long idGamer1 = 1003L; // 游戏党1 (修仙+青轴)
    private final Long idGamer2 = 1004L; // 游戏党2 (修仙+青轴) -> 应该在一起
    
    private final Long idTeam1 = 1005L;  // 死党A (填了组队码)
    private final Long idTeam2 = 1006L;  // 死党B (填了组队码) -> 必须在一起
    
    @BeforeEach
    public void init() {
        System.out.println(">>> [Init]正在清理测试环境...");
        
        // === 1. 暴力物理清理 (严格按照外键依赖的逆序) ===
        // 依赖链A: Bed -> Room -> Floor -> Building -> Campus
        // 依赖链B: Student -> Class -> Major -> College -> Campus
        
        // 先清空所有可能引用别的表的数据
        jdbcTemplate.execute("DELETE FROM dorm_bed");
        jdbcTemplate.execute("DELETE FROM dorm_room");
        jdbcTemplate.execute("DELETE FROM dorm_floor");
        jdbcTemplate.execute("DELETE FROM dorm_building");
        
        // 清理学生相关链条
        jdbcTemplate.execute("DELETE FROM biz_user_preference");
        jdbcTemplate.execute("DELETE FROM sys_ordinary_user"); // 学生
        jdbcTemplate.execute("DELETE FROM biz_class");         // 班级
        jdbcTemplate.execute("DELETE FROM sys_major");         // 专业
        jdbcTemplate.execute("DELETE FROM sys_college");       // 学院 (它引用了 Campus)
        
        // 最后才能删校区
        jdbcTemplate.execute("DELETE FROM sys_campus");
        
        System.out.println(">>> [Init] 清理完毕，开始重建...");
        
        // === 2. 重建基础设施 ===
        createInfrastructure();
        
        // === 3. 重建用户和画像 ===
        // 冲突组 (一票否决测试)
        createUser(idSmoker, "老烟枪");
        // 室内抽烟(2), 耐受(1), 晚睡(3), 早起(3), 键盘(0)
        createPref(idSmoker, null, 2, 1, 3, 3, 0);
        
        createUser(idHater, "养生哥");
        // 不抽(0), 厌烟(0) -> 触发 Smokng Veto
        createPref(idHater, null, 0, 0, 1, 1, 0);
        
        // 匹配组 (欧氏距离测试)
        createUser(idGamer1, "五杀王");
        // 凌晨2点睡(6), 青轴(3)
        createPref(idGamer1, null, 0, 1, 6, 6, 3);
        
        createUser(idGamer2, "辅助哥");
        // 习惯完全一致
        createPref(idGamer2, null, 0, 1, 6, 6, 3);
        
        // 组队组 (TeamCode测试)
        createUser(idTeam1, "死党A");
        createPref(idTeam1, "TEAM_XB", 0, 1, 3, 3, 1);
        
        createUser(idTeam2, "死党B");
        createPref(idTeam2, "TEAM_XB", 0, 1, 3, 3, 1);
        
        System.out.println(">>> [Init] 环境准备完毕");
    }
    
    @Test
    public void testDeepAntiTrollAllocation() {
        System.out.println(">>> [测试开始] 模拟智能分配...");
        
        // 待分配名单
        List<Long> targets = Arrays.asList(idSmoker, idHater, idGamer1, idGamer2, idTeam1, idTeam2);
        
        // 执行核心算法
        allocationService.executeAllocation(targets);
        
        // === 验证结果 ===
        // 获取所有有人的床位: Map<UserId, RoomId>
        Map<Long, Long> resultMap = bedService.list(Wrappers.<DormBed>lambdaQuery().isNotNull(DormBed::getOccupantId))
                .stream()
                .collect(Collectors.toMap(DormBed::getOccupantId, DormBed::getRoomId));
        
        System.out.println(">>> 分配结果详情: " + resultMap);
        
        // 1. 验证组队逻辑 (Team Code)
        // 两人必须在同一个房间
        Assertions.assertNotNull(resultMap.get(idTeam1), "死党A未分配到床位");
        Assertions.assertEquals(resultMap.get(idTeam1), resultMap.get(idTeam2),
                "❌ 失败：填写了相同TeamCode的学生应该分在同一间宿舍");
        System.out.println("✅ 组队逻辑 (TeamCode) 验证通过");
        
        // 2. 验证兴趣匹配 (Soul Mate)
        // 两个游戏党应该分在一起，因为我们只有2个房间，如果不分在一起说明算法瞎分配
        Assertions.assertEquals(resultMap.get(idGamer1), resultMap.get(idGamer2),
                "❌ 失败：两个生活习惯高度相似的游戏党应该分在一起");
        System.out.println("✅ 兴趣匹配逻辑 (Euclidean) 验证通过");
        
        // 3. 验证防刁民/一票否决 (Anti-Troll)
        // 抽烟的和厌烟的绝对不能在同一个房间
        Assertions.assertNotEquals(resultMap.get(idSmoker), resultMap.get(idHater),
                "❌ 失败：严重冲突(抽烟vs厌烟)的学生绝对不能分在同一间宿舍！");
        System.out.println("✅ 防刁民(一票否决) 验证通过");
    }
    
    // ================= 辅助构建方法 =================
    
    private void createInfrastructure() {
// --- 1. 基础数据 (校区/学院/专业/班级) ---
        // 1.1 校区
        com.mol.server.entity.SysCampus campus = new com.mol.server.entity.SysCampus();
        campus.setCampusName("测试校区");
        campus.setCampusCode("TEST");
        campusService.save(campus);
        
        // 1.2 学院 (如果不创建，createUser 里设置 collegeId=100 也会报错)
        com.mol.server.entity.SysCollege college = new com.mol.server.entity.SysCollege();
        college.setId(100L); // 显式指定ID
        college.setCampusId(campus.getId());
        college.setName("测试学院");
        college.setCode("TEST_COL");
        // 注意：这里需要注入 CollegeService，或者用 jdbcTemplate 插入
        // 为了简单，我们直接用 SQL 插，因为没注入 Service
        jdbcTemplate.update("INSERT INTO sys_college (id, campus_id, name, code) VALUES (?, ?, ?, ?)",
                100L, campus.getId(), "测试学院", "TC");
        
        // 1.3 专业
        jdbcTemplate.update("INSERT INTO sys_major (id, college_id, name) VALUES (?, ?, ?)",
                200L, 100L, "测试专业");
        
        // 1.4 班级 (关键！解决报错)
        jdbcTemplate.update("INSERT INTO biz_class (id, major_id, grade, class_name) VALUES (?, ?, ?, ?)",
                300L, 200L, "2024", "测试班级");
        
        // 2. 楼栋
        DormBuilding b = new DormBuilding();
        b.setCampusId(campus.getId());
        b.setName("智能分配楼");
        b.setType(1); // 男生楼
        b.setFloors(1);
        buildingService.save(b);
        
        // 3. 楼层 (关键步骤)
        DormFloor f = new DormFloor();
        f.setBuildingId(b.getId());
        f.setFloorNum(1);
        f.setGenderLimit(1);
        floorService.save(f);
        
        // 4. 房间 (建2个房间: 101, 102)
        // 只有2个房间，每间4人，足够容纳6个测试用户
        for (int i = 1; i <= 2; i++) {
            DormRoom r = new DormRoom();
            r.setBuildingId(b.getId());
            r.setFloorId(f.getId()); // 关联楼层ID
            r.setRoomNo("10" + i);
            r.setFloorNo(1);
            r.setCapacity(4);
            r.setCurrentNum(0);
            r.setGender(1);
            r.setStatus(1);
            roomService.save(r);
            
            // 5. 床位
            for (int j = 1; j <= 4; j++) {
                DormBed bed = new DormBed();
                bed.setRoomId(r.getId());
                bed.setBedLabel(r.getRoomNo() + "-" + j);
                bedService.save(bed);
            }
        }
    }
    
    private void createUser(Long id, String name) {
        SysOrdinaryUser u = new SysOrdinaryUser();
        u.setId(id); // 强制指定ID方便断言
        u.setUsername(String.valueOf(id));
        u.setRealName(name);
        u.setPassword("123");
        u.setSex(1); // 全是男生
        u.setCollegeId(100L);
        u.setMajorId(200L);
        u.setClassId(300L);   // 同班级
        userService.save(u);
    }
    
    private void createPref(Long userId, String teamCode, int smoking, int tolerance, int bedTime, int wakeTime, int kb) {
        UserPreference p = new UserPreference();
        p.setUserId(userId);
        p.setTeamCode(teamCode);
        p.setSmoking(smoking);
        p.setSmokeTolerance(tolerance);
        p.setBedTime(bedTime);
        p.setWakeTime(wakeTime);
        p.setKeyboardAxis(kb);
        
        // 填充默认值，防止空指针
        p.setSleepQuality(2);
        p.setSnoringLevel(0);
        p.setPersonalHygiene(3);
        p.setAcDuration(1);
        p.setAcTemp(26);
        p.setBringGuest(1);
        p.setCleanFreq(2);
        p.setSockWash(0);
        p.setTrashHabit(1);
        p.setOutLateFreq(0);
        
        preferenceService.save(p);
    }
}