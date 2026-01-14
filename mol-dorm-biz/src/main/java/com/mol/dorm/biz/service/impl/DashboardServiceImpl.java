package com.mol.dorm.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mol.dorm.biz.entity.DormBed;
import com.mol.dorm.biz.entity.DormBuilding;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.entity.RepairOrder;
import com.mol.dorm.biz.mapper.DormBedMapper;
import com.mol.dorm.biz.mapper.DormBuildingMapper;
import com.mol.dorm.biz.mapper.DormRoomMapper;
import com.mol.dorm.biz.mapper.RepairOrderMapper;
import com.mol.dorm.biz.service.DashboardService;
import com.mol.dorm.biz.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    
    private final DormBuildingMapper buildingMapper;
    private final DormRoomMapper roomMapper;
    private final DormBedMapper bedMapper;
    private final RepairOrderMapper repairMapper;
    
    @Override
    public DashboardVO getBigScreenData() {
        DashboardVO vo = new DashboardVO();
        
        // 1. 基础卡片数据
        vo.setSummary(buildSummary());
        
        // 2. 入住率饼图 (已住 vs 空闲)
        vo.setOccupancyPie(buildOccupancyPie(vo.getSummary()));
        
        // 3. 楼栋分布柱状图 (X轴楼栋，Y轴人数)
        vo.setBuildingBar(buildBuildingBar());
        
        // 4. 报修状态分布
        vo.setRepairPie(buildRepairPie());
        
        return vo;
    }
    
    /**
     * 构建顶部数字卡片
     */
    private DashboardVO.SummaryCard buildSummary() {
        Long totalBuildings = buildingMapper.selectCount(null);
        Long totalRooms = roomMapper.selectCount(null);
        Long totalBeds = bedMapper.selectCount(null);
        
        // 统计已分配的床位 (occupant_id 不为空)
        Long usedBeds = bedMapper.selectCount(Wrappers.<DormBed>lambdaQuery()
                .isNotNull(DormBed::getOccupantId));
        
        // 计算入住率 (保留1位小数)
        String rate = "0%";
        if (totalBeds > 0) {
            double percent = (double) usedBeds / totalBeds * 100;
            // [Fix 1] 使用 Java 原生 String.format 替代 Hutool，避免版本兼容问题
            rate = String.format("%.1f%%", percent);
        }
        
        return DashboardVO.SummaryCard.builder()
                .totalBuildings(totalBuildings)
                .totalRooms(totalRooms)
                .totalBeds(totalBeds)
                .usedBeds(usedBeds)
                .occupancyRate(rate)
                .build();
    }
    
    /**
     * 构建入住对比饼图
     */
    private List<DashboardVO.NameValue> buildOccupancyPie(DashboardVO.SummaryCard summary) {
        List<DashboardVO.NameValue> list = new ArrayList<>();
        list.add(DashboardVO.NameValue.builder()
                .name("已入住")
                .value(summary.getUsedBeds())
                .build());
        
        list.add(DashboardVO.NameValue.builder()
                .name("空闲床位")
                .value(summary.getTotalBeds() - summary.getUsedBeds())
                .build());
        return list;
    }
    
    /**
     * 构建楼栋人数柱状图
     */
    private DashboardVO.AxisChart buildBuildingBar() {
        // 1. 查出所有楼栋
        List<DormBuilding> buildings = buildingMapper.selectList(Wrappers.<DormBuilding>lambdaQuery()
                .orderByAsc(DormBuilding::getName));
        
        List<String> xData = new ArrayList<>();
        List<Long> yData = new ArrayList<>();
        
        if (buildings != null && !buildings.isEmpty()) {
            // A. 获取所有已占用的床位 (只查 roomId)
            List<DormBed> usedBeds = bedMapper.selectList(Wrappers.<DormBed>lambdaQuery()
                    .isNotNull(DormBed::getOccupantId)
                    .select(DormBed::getRoomId));
            
            // B. 获取所有房间映射 (RoomId -> BuildingId)
            // [Fix 2] 修正泛型：roomMapper 必须接收 DormRoom 类型的 Wrapper
            Map<Long, Long> roomBuildingMap = roomMapper.selectList(Wrappers.<DormRoom>query()
                            .select("id", "building_id"))
                    .stream()
                    .collect(Collectors.toMap(DormRoom::getId, DormRoom::getBuildingId));
            
            // C. 统计每个楼栋的人数 Map<BuildingId, Count>
            Map<Long, Long> buildingCountMap = usedBeds.stream()
                    .map(bed -> roomBuildingMap.get(bed.getRoomId()))
                    .filter(bid -> bid != null)
                    .collect(Collectors.groupingBy(bid -> bid, Collectors.counting()));
            
            // D. 组装数据
            for (DormBuilding b : buildings) {
                xData.add(b.getName());
                yData.add(buildingCountMap.getOrDefault(b.getId(), 0L));
            }
        }
        
        return DashboardVO.AxisChart.builder()
                .categories(xData)
                .seriesData(yData)
                .build();
    }
    
    /**
     * 构建报修状态分布饼图
     */
    private List<DashboardVO.NameValue> buildRepairPie() {
        QueryWrapper<RepairOrder> wrapper = new QueryWrapper<>();
        wrapper.select("status", "count(*) as total");
        wrapper.groupBy("status");
        
        List<Map<String, Object>> result = repairMapper.selectMaps(wrapper);
        
        List<DashboardVO.NameValue> list = new ArrayList<>();
        String[] statusNames = {"待处理", "维修中", "已修复", "已评价", "已驳回"};
        
        for (Map<String, Object> map : result) {
            // 注意：不同数据库返回的 count 类型可能不同 (Long/BigDecimal)，这里做个兼容
            Integer status = (Integer) map.get("status");
            Number countNum = (Number) map.get("total");
            Long count = countNum.longValue();
            
            String name = "未知状态";
            if (status != null && status >= 0 && status < statusNames.length) {
                name = statusNames[status];
            }
            
            list.add(DashboardVO.NameValue.builder()
                    .name(name)
                    .value(count)
                    .build());
        }
        
        return list;
    }
}