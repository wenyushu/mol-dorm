package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.dorm.biz.MolDormApplication;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.DormBuilding;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.vo.DashboardVO;
import com.mol.server.entity.SysCampus;
import com.mol.server.service.SysCampusService;
import com.mol.server.service.SysOrdinaryUserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = MolDormApplication.class)
@ActiveProfiles("dorm")
public class DormDashboardTest {
    
    @Autowired private DashboardService dashboardService;
    @Autowired private DormBuildingService buildingService;
    @Autowired private DormRoomService roomService;
    @Autowired private DormBedService bedService;
    @Autowired private SysCampusService campusService;
    @Autowired private SysOrdinaryUserService userService; // [Fix] 注入用户服务
    
    @BeforeEach
    public void init() {
        // 1. 清理数据
        bedService.remove(Wrappers.emptyWrapper());
        roomService.remove(Wrappers.emptyWrapper());
        buildingService.remove(Wrappers.emptyWrapper());
        campusService.remove(Wrappers.emptyWrapper());
        // 注意：userService 也要考虑清理，或者只管新建
        
        // 2. 建校区
        SysCampus campus = new SysCampus();
        campus.setCampusName("Dash校区");
        campus.setCampusCode("DASH_001");
        campus.setStatus("0");
        campusService.save(campus);
        
        // 3. 建楼
        DormBuilding b1 = new DormBuilding();
        b1.setName("Dashboard楼");
        b1.setCampusId(campus.getId());
        b1.setType(1);
        b1.setFloors(6);
        b1.setStatus(1);
        buildingService.save(b1);
        
        // 4. 建房
        DormRoom r1 = new DormRoom();
        r1.setBuildingId(b1.getId());
        r1.setRoomNo("101");
        r1.setFloorNo(1);
        r1.setCapacity(4);
        r1.setCurrentNum(1);
        r1.setGender(1);
        r1.setStatus(1);
        roomService.save(r1);
        
        // 5. [Fix] 创建真实学生用户
        SysOrdinaryUser student = new SysOrdinaryUser();
        student.setUsername("dash_stu");
        student.setRealName("图表测试员");
        student.setPassword("123456");
        student.setUserCategory(0);
        student.setStatus("0");
        userService.save(student);
        
        // 6. 建床并住人 (关联真实学生ID)
        DormBed bed1 = new DormBed();
        bed1.setRoomId(r1.getId());
        bed1.setBedLabel("101-1");
        bed1.setOccupantId(student.getId()); // 使用真实ID
        bedService.save(bed1);
        
        DormBed bed2 = new DormBed();
        bed2.setRoomId(r1.getId());
        bed2.setBedLabel("101-2");
        bed2.setOccupantId(null); // 空闲
        bedService.save(bed2);
    }
    
    @Test
    @Transactional
    public void testGetDashboardData() {
        DashboardVO vo = dashboardService.getBigScreenData();
        
        System.out.println(">>> 大屏数据: " + vo);
        
        // 验证基础数据
        Assertions.assertEquals(1, vo.getSummary().getTotalBuildings());
        Assertions.assertEquals(2, vo.getSummary().getTotalBeds());
        Assertions.assertEquals(1, vo.getSummary().getUsedBeds()); // 1人入住
        Assertions.assertEquals("50.0%", vo.getSummary().getOccupancyRate());
        
        // 验证饼图
        Assertions.assertEquals(2, vo.getOccupancyPie().size());
        
        // 验证楼栋柱状图
        Assertions.assertTrue(vo.getBuildingBar().getCategories().contains("Dashboard楼"));
        // 验证数据不为空即可，具体顺序可能因数据库实现略有不同
        Assertions.assertFalse(vo.getBuildingBar().getSeriesData().isEmpty());
        Assertions.assertEquals(1, vo.getBuildingBar().getSeriesData().get(0)); // 该楼有1人
    }
}