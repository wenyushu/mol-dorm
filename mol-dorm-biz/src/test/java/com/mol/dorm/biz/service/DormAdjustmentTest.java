package com.mol.dorm.biz.service;

import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.dorm.biz.MolDormApplication;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.DormBuilding;
import com.mol.dorm.biz.entity.DormFloor;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.server.service.SysOrdinaryUserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * 宿舍调宿申请流程测试
 */
@SpringBootTest(classes = MolDormApplication.class)
@ActiveProfiles("dorm")
public class DormAdjustmentTest {
    
    @Autowired private DormAdjustmentService adjustmentService;
    @Autowired private SysOrdinaryUserService userService;
    @Autowired private DormRoomService roomService;
    @Autowired private DormBedService bedService;
    @Autowired private DormBuildingService buildingService;
    @Autowired private DormFloorService floorService; // 必须注入楼层Service
    
    // 注入 JdbcTemplate 进行暴力物理清理
    @Autowired private JdbcTemplate jdbcTemplate;
    
    // 测试数据 ID
    private Long buildingId;
    private Long floorId;
    private Long roomId;
    private Long bedId;
    private Long userId = 10086L;
    
    @BeforeEach
    public void initData() {
        System.out.println(">>> [Init] 清理并重建测试数据...");
        
        // === 1. 物理清理 (严格逆序：申请单 -> 床 -> 房 -> 层 -> 楼 -> 人) ===
        // 任何引用了 building_id 或 room_id 的表都必须先删掉
        jdbcTemplate.execute("DELETE FROM dorm_change_request"); // 调宿申请表
        jdbcTemplate.execute("DELETE FROM dorm_bed");
        jdbcTemplate.execute("DELETE FROM dorm_room");
        
        // [关键修复] 必须先删楼层，再删楼栋！之前的报错就是因为缺了这步或顺序错了
        jdbcTemplate.execute("DELETE FROM dorm_floor");
        
        jdbcTemplate.execute("DELETE FROM dorm_building");
        jdbcTemplate.execute("DELETE FROM sys_ordinary_user");
        
        // === 2. 重建基础设施 ===
        // 2.1 楼栋
        DormBuilding building = new DormBuilding();
        building.setName("测试楼A");
        building.setType(1); // 男生楼
        building.setFloors(1);
        buildingService.save(building);
        this.buildingId = building.getId();
        
        // 2.2 [关键] 楼层 (必须显式创建，否则房间无法关联)
        DormFloor floor = new DormFloor();
        floor.setBuildingId(buildingId);
        floor.setFloorNum(1);
        floor.setGenderLimit(1);
        floorService.save(floor);
        this.floorId = floor.getId();
        
        // 2.3 房间
        DormRoom room = new DormRoom();
        room.setBuildingId(buildingId);
        room.setFloorId(floorId); // 关联楼层
        room.setRoomNo("101");
        room.setCapacity(4);
        room.setCurrentNum(1);
        room.setGender(1);
        room.setStatus(1);
        roomService.save(room);
        this.roomId = room.getId();
        
        // 2.4 床位
        DormBed bed = new DormBed();
        bed.setRoomId(roomId);
        bed.setBedLabel("101-1");
        bed.setOccupantId(userId); // 用户当前住在这里
        bed.setStatus(1);
        bedService.save(bed);
        this.bedId = bed.getId();
        
        // === 3. 创建用户 ===
        SysOrdinaryUser user = new SysOrdinaryUser();
        user.setId(userId);
        user.setUsername("test_student");
        user.setRealName("张三");
        user.setSex(1);
        // 为了防止外键报错，这里先不设班级学院，或者设为 null
        user.setCollegeId(null);
        user.setMajorId(null);
        user.setClassId(null);
        userService.save(user);
        
        System.out.println(">>> [Init] 数据准备完毕");
    }
    
    @Test
    public void testApplyAdjustment() {
        System.out.println(">>> [Test] 开始测试调宿申请...");
        
        // 1. 模拟用户申请换宿
        // 参数：用户ID, 申请原因, 目标房间ID(null表示不指定)
        String reason = "室友打呼噜太大声";
        Long targetRoomId = null;
        
        // 调用核心业务方法
        boolean result = adjustmentService.applyForAdjustment(userId, reason, targetRoomId);
        
        // 2. 验证结果
        Assertions.assertTrue(result, "调宿申请提交应该成功");
        
        System.out.println("✅ 调宿申请测试通过");
    }
}