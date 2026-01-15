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

/**
 * 首页/大屏数据服务实现类
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    
    private final DormBuildingMapper buildingMapper;
    private final DormRoomMapper roomMapper;
    private final DormBedMapper bedMapper;
    private final RepairOrderMapper repairMapper;
    
    /**
     * 获取大屏聚合数据
     */
    @Override
    public DashboardVO getBigScreenData() {
        DashboardVO vo = new DashboardVO();
        
        // 1. 基础卡片数据 (楼栋数、房间数、床位数、入住率)
        vo.setSummary(buildSummary());
        
        // 2. 入住率饼图 (已住 vs 空闲)
        vo.setOccupancyPie(buildOccupancyPie(vo.getSummary()));
        
        // 3. 楼栋分布柱状图 (X轴楼栋名，Y轴入住人数)
        vo.setBuildingBar(buildBuildingBar());
        
        // 4. 报修状态分布饼图
        vo.setRepairPie(buildRepairPie());
        
        return vo;
    }
    
    /**
     * 构建顶部数字卡片
     */
    private DashboardVO.SummaryCard buildSummary() {
        // 统计总数
        Long totalBuildings = buildingMapper.selectCount(null);
        Long totalRooms = roomMapper.selectCount(null);
        Long totalBeds = bedMapper.selectCount(null);
        
        // 统计已分配的床位 (occupant_id 不为空即为占用)
        Long usedBeds = bedMapper.selectCount(Wrappers.<DormBed>lambdaQuery()
                .isNotNull(DormBed::getOccupantId));
        
        // 计算入住率 (保留1位小数)
        String rate = "0%";
        if (totalBeds > 0) {
            double percent = (double) usedBeds / totalBeds * 100;
            // 使用 String.format 格式化百分比
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
        
        // 数据项1：已入住
        list.add(DashboardVO.NameValue.builder()
                .name("已入住")
                .value(summary.getUsedBeds())
                .build());
        
        // 数据项2：空闲床位
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
        // 1. 查出所有楼栋，按名称排序
        // ✅ [修复点] 使用 getBuildingName 代替 getName
        List<DormBuilding> buildings = buildingMapper.selectList(Wrappers.<DormBuilding>lambdaQuery()
                .orderByAsc(DormBuilding::getBuildingName));
        
        List<String> xData = new ArrayList<>();
        List<Long> yData = new ArrayList<>();
        
        if (buildings != null && !buildings.isEmpty()) {
            // A. 获取所有已占用的床位 (只查 roomId 以减少数据量)
            List<DormBed> usedBeds = bedMapper.selectList(Wrappers.<DormBed>lambdaQuery()
                    .isNotNull(DormBed::getOccupantId)
                    .select(DormBed::getRoomId));
            
            // B. 获取所有房间映射 (RoomId -> BuildingId)
            // 查出 id 和 building_id 两个字段
            Map<Long, Long> roomBuildingMap = roomMapper.selectList(Wrappers.<DormRoom>query()
                            .select("id", "building_id"))
                    .stream()
                    .collect(Collectors.toMap(DormRoom::getId, DormRoom::getBuildingId));
            
            // C. 统计每个楼栋的人数 Map<BuildingId, Count>
            // 遍历已占用的床位 -> 找到对应房间 -> 找到对应楼栋 -> 计数
            Map<Long, Long> buildingCountMap = usedBeds.stream()
                    .map(bed -> roomBuildingMap.get(bed.getRoomId()))
                    .filter(bid -> bid != null) // 过滤掉找不到楼栋的异常数据
                    .collect(Collectors.groupingBy(bid -> bid, Collectors.counting()));
            
            // D. 组装数据，确保 X轴和 Y轴 一一对应
            for (DormBuilding b : buildings) {
                // ✅ [修复点] 使用 getBuildingName
                xData.add(b.getBuildingName());
                // 如果该楼栋没人住，默认为 0
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
        // 使用 MyBatis-Plus 的 selectMaps 进行聚合查询
        // SQL 类似: SELECT status, count(*) as total FROM repair_order GROUP BY status
        QueryWrapper<RepairOrder> wrapper = new QueryWrapper<>();
        wrapper.select("status", "count(*) as total");
        wrapper.groupBy("status");
        
        List<Map<String, Object>> result = repairMapper.selectMaps(wrapper);
        
        List<DashboardVO.NameValue> list = new ArrayList<>();
        // 对应 RepairOrder 实体中的状态定义: 0-待处理, 1-维修中, 2-已修复, 3-已评价, 4-已驳回
        String[] statusNames = {"待处理", "维修中", "已修复", "已评价", "已驳回"};
        
        for (Map<String, Object> map : result) {
            // 注意：不同数据库/驱动返回的数字类型可能不同 (Integer/Long/BigDecimal)
            // 状态通常是 Integer
            Integer status = (Integer) map.get("status");
            // 总数通常是 Number 的子类
            Number countNum = (Number) map.get("total");
            Long count = countNum.longValue();
            
            // 将数字状态转为中文名称
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