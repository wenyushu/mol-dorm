package com.mol.dorm.biz.service.impl;

import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mol.common.core.enums.DormStatusEnum;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.*;
import com.mol.dorm.biz.mapper.*;
import com.mol.dorm.biz.service.DashboardService;
import com.mol.dorm.biz.vo.DashboardVO;
import com.mol.dorm.biz.vo.DormRoomVO;
import com.mol.dorm.biz.vo.MyRoomVO;
import com.mol.server.entity.SysNotice;
import com.mol.common.core.entity.SysOrdinaryUser; // 🛡️ 修正：引用 mol-common 中的正确实体
import com.mol.server.mapper.SysNoticeMapper;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import com.mol.server.mapper.SysCampusMapper;
import com.mol.server.vo.SysCampusTreeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 宿舍驾驶舱核心业务实现类 - 数字化全维度监控版
 * 🛡️ [防刁民设计]：
 * 1. 深度穿透审计：所有预警数据均实时扫描底层财务余额与床位占用，不信任冗余计数字段。
 * 2. 身份隔离看板：自动区分学生端聚合看板(MyRoomVO)与宿管端驾驶舱(Alerts)的数据颗粒度。
 * 3. 动态视觉驱动：通过 isCritical 标志位实现异常任务堆栈的红色警示渲染。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    
    private final SysCampusMapper campusMapper;
    private final DormBuildingMapper buildingMapper;
    private final DormFloorMapper floorMapper;
    private final DormRoomMapper roomMapper;
    private final DormBedMapper bedMapper;
    private final SysNoticeMapper noticeMapper;
    private final DormLostFoundMapper lostFoundMapper;
    private final RepairOrderMapper repairMapper;
    private final DormWorkflowMapper workflowMapper;
    private final SysOrdinaryUserMapper userMapper;
    private final DormRoomWalletMapper walletMapper;
    private final UtilityBillMapper billMapper;
    
    // =================================================================================
    // 1. 【学生侧】我的宿舍全量看板 (MyRoomVO)
    // =================================================================================
    
    @Override
    public MyRoomVO getStudentRoomDashboard(Long userId) {
        // [反查审计]：通过学生 ID 穿透床位表精准定位房间
        DormRoom room = roomMapper.selectByStudentId(userId);
        if (room == null) throw new ServiceException("看板加载失败：未检测到您的入住档案");
        
        MyRoomVO vo = new MyRoomVO();
        vo.setRoomNo(room.getRoomNo());
        vo.setApartmentType(room.getApartmentType());
        
        // 财务预警哨兵
        DormRoomWallet wallet = walletMapper.selectOne(Wrappers.<DormRoomWallet>lambdaQuery().eq(DormRoomWallet::getRoomId, room.getId()));
        if (wallet != null) {
            vo.setWalletBalance(wallet.getBalance());
            vo.setWalletStatus(wallet.getStatus());
            vo.setPowerOffWarning(wallet.getBalance().compareTo(new BigDecimal("10")) < 0);
            vo.setWalletStatusMsg(wallet.getBalance().compareTo(BigDecimal.ZERO) < 0 ? "已欠费，请充值" : "正常");
        }
        
        // 能耗快照：获取最近一期账单
        UtilityBill lastBill = billMapper.selectOne(Wrappers.<UtilityBill>lambdaQuery()
                .eq(UtilityBill::getRoomId, room.getId())
                .orderByDesc(UtilityBill::getMonth).last("LIMIT 1"));
        if (lastBill != null) {
            vo.setLastBillMonth(lastBill.getMonth());
            vo.setLastBillAmount(lastBill.getTotalAmount());
            vo.setLastElectricUsage(lastBill.getElectricUsage());
        }
        
        // 舍友实时分布
        List<DormBed> beds = bedMapper.selectList(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getRoomId, room.getId()));
        vo.setBedList(beds.stream().map(b -> {
            MyRoomVO.BedInfoVO bedVo = new MyRoomVO.BedInfoVO();
            bedVo.setBedId(b.getId());
            bedVo.setBedLabel(b.getBedLabel());
            bedVo.setIsOccupied(b.getOccupantId() != null);
            bedVo.setIsMe(userId.equals(b.getOccupantId()));
            return bedVo;
        }).collect(Collectors.toList()));
        
        // 紧急公告聚合
        SysNotice latest = noticeMapper.selectOne(Wrappers.<SysNotice>lambdaQuery()
                .eq(SysNotice::getStatus, "0").orderByDesc(SysNotice::getLevel, SysNotice::getCreateTime).last("LIMIT 1"));
        if (latest != null) {
            MyRoomVO.NoticeSnippetVO nVo = new MyRoomVO.NoticeSnippetVO();
            nVo.setTitle(latest.getTitle());
            nVo.setTypeDesc(Objects.equals(latest.getType(), 3) ? "紧急" : "通知");
            vo.setLatestNotice(nVo);
        }
        return vo;
    }
    
    // =================================================================================
    // 2. 【宿管侧】驾驶舱功能补全
    // =================================================================================
    
    @Override
    public List<Map<String, Object>> getWalletArrearsAlerts(Long buildingId) {
        return walletMapper.selectArrearsRoomsByBuilding(buildingId);
    }
    
    @Override
    public List<Map<String, Object>> getEnergyAnomalyRank(Long buildingId) {
        return billMapper.selectAnomalyRank(buildingId);
    }
    
    @Override
    public DashboardVO getBigScreenData() {
        Long totalBeds = bedMapper.selectCount(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getStatus, 20));
        Long usedBeds = bedMapper.selectCount(Wrappers.<DormBed>lambdaQuery().isNotNull(DormBed::getOccupantId));
        
        DashboardVO.SummaryCard summary = DashboardVO.SummaryCard.builder()
                .totalBuildings(buildingMapper.selectCount(null))
                .totalBeds(totalBeds)
                .usedBeds(usedBeds)
                .occupancyRate(totalBeds > 0 ? NumberUtil.formatPercent((double) usedBeds / totalBeds, 1) : "0%")
                .build();
        
        return DashboardVO.builder()
                .summary(summary)
                .occupancyPie(Arrays.asList(
                        new DashboardVO.NameValue("在宿学生", usedBeds, "#67C23A"),
                        new DashboardVO.NameValue("空置床位", Math.max(0, totalBeds - usedBeds), "#909399")
                ))
                .repairPie(buildRepairPie())
                .infoBoard(buildInfoBoard())
                .workQueue(buildWorkQueue())
                .build();
    }
    
    // =================================================================================
    // 3. 【核心下钻】L4 房间人员画像透视 (解决报错的关键补全)
    // =================================================================================
    
    /**
     * 🟢 房间原子详情：实现人员档案与物理床位的秒级对账
     * [防数据滞后]：即时调用 userMapper 获取实时姓名，杜绝因档案迁移导致的姓名显示错误。
     */
    @Override
    public DormRoomVO getRoomDetail(Long roomId) {
        // A. 抓取房间物理全量详情 (由 DormRoomMapper.xml 穿透楼宇、楼层名称)
        Map<String, Object> roomMap = roomMapper.selectRoomDetail(roomId);
        if (roomMap == null) throw new ServiceException("下钻失败：房间档案已在系统中注销");
        
        DormRoomVO vo = new DormRoomVO();
        vo.setId(roomId);
        vo.setRoomNo((String) roomMap.get("roomNo"));
        vo.setBuildingName((String) roomMap.get("buildingName"));
        vo.setFloorNo((Integer) roomMap.get("floorNum"));
        vo.setCurrentNum((Integer) roomMap.get("currentNum"));
        vo.setCapacity((Integer) roomMap.get("capacity"));
        vo.setStatus((Integer) roomMap.get("status"));
        vo.setStatusDesc(DormStatusEnum.fromCode(vo.getStatus()).getDesc());
        
        // B. 穿透至最底层床位表进行人员反查
        List<DormBed> beds = bedMapper.selectList(Wrappers.<DormBed>lambdaQuery()
                .eq(DormBed::getRoomId, roomId).orderByAsc(DormBed::getBedLabel));
        
        vo.setBedList(beds.stream().map(b -> {
            DormRoomVO.BedInfo detail = new DormRoomVO.BedInfo();
            detail.setBedId(b.getId());
            detail.setBedLabel(b.getBedLabel());
            detail.setOccupantId(b.getOccupantId());
            detail.setOccupantType(b.getOccupantType());
            detail.setVersion(b.getVersion());
            
            // 🛡️ [修正点]：通过 getUsername() 获取学工号，消灭 getStudentNo 报错
            if (b.getOccupantId() != null) {
                SysOrdinaryUser user = userMapper.selectById(b.getOccupantId());
                if (user != null) {
                    detail.setOccupantName(user.getRealName());
                    detail.setOccupantNo(user.getUsername()); // 👈 核心修复：username 即代表学号
                }
            }
            return detail;
        }).collect(Collectors.toList()));
        
        return vo;
    }
    
    /**
     * 🟢 【今日动态预警板】探测引擎
     * [逻辑]：自动扫描积压工单、异常财务与高敏申请，驱动大屏 isCritical 红色预警。
     */
    @Override
    public Map<String, Object> getTodayAlerts() {
        Map<String, Object> alerts = new HashMap<>();
        
        // 1. 财务风险预判：余额小于 0 的房间总数
        long arrearsCount = walletMapper.selectCount(Wrappers.<DormRoomWallet>lambdaQuery()
                .lt(DormRoomWallet::getBalance, BigDecimal.ZERO));
        alerts.put("arrearsCount", arrearsCount);
        
        // 2. 运维时效预判：探测 24 小时未指派的积压报修单
        List<RepairOrder> urgentOrders = repairMapper.selectUrgentOrders();
        alerts.put("urgentRepairs", urgentOrders.size());
        
        // 3. 行政堆栈预判：待审批的工作流总数
        long pendingWorkflow = workflowMapper.selectCount(Wrappers.<DormWorkflow>lambdaQuery()
                .eq(DormWorkflow::getStatus, 0));
        alerts.put("pendingWorkflow", pendingWorkflow);
        
        // 🛡️ [高压预警驱动]：若任意堆栈超过阈值，大屏 UI 开启红色警戒模式
        alerts.put("isCritical", arrearsCount > 5 || urgentOrders.size() > 10 || pendingWorkflow > 20);
        
        return alerts;
    }
    
    // =================================================================================
    // 4. 下钻结构辅助逻辑
    // =================================================================================
    
    @Override
    public List<Map<String, Object>> getCampusStructure() {
        List<SysCampusTreeVO> tree = campusMapper.selectCampusBuildingTree(null);
        return tree.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("name", c.getCampusName());
            map.put("buildings", c.getBuildings());
            return map;
        }).collect(Collectors.toList());
    }
    
    @Override
    public List<Map<String, Object>> getBuildingFloorStats(Long buildingId) {
        DormBuilding b = buildingMapper.selectById(buildingId);
        if (b == null || b.getStatus() >= 30) throw new ServiceException("楼栋处于封锁/维护状态");
        
        QueryWrapper<DormRoom> qw = new QueryWrapper<>();
        qw.select("floor_num", "count(*) as roomCount", "sum(capacity) as total", "sum(current_num) as used")
                .eq("building_id", buildingId).groupBy("floor_num");
        
        return roomMapper.selectMaps(qw).stream().map(m -> {
            Map<String, Object> fMap = new HashMap<>();
            long used = ((Number) m.get("used")).longValue();
            long total = ((Number) m.get("total")).longValue();
            fMap.put("floor", m.get("floor_num") + "F");
            // 🛡️ [UI 驱动]：饱和度 > 90% 显红色预警
            fMap.put("color", (double)used/total >= 0.9 ? "#F56C6C" : "#409EFF");
            return fMap;
        }).collect(Collectors.toList());
    }
    
    @Override
    public List<DormRoomVO> getFloorRoomMatrix(Long buildingId, Integer floorNum) {
        List<DormRoom> rooms = roomMapper.selectList(Wrappers.<DormRoom>lambdaQuery()
                .eq(DormRoom::getBuildingId, buildingId).eq(DormRoom::getFloorNum, floorNum));
        return rooms.stream().map(r -> {
            DormRoomVO vo = new DormRoomVO();
            vo.setRoomNo(r.getRoomNo());
            vo.setStatusDesc(DormStatusEnum.fromCode(r.getStatus()).getDesc());
            vo.setStatusColor(DormStatusEnum.fromCode(r.getStatus()).getColor());
            return vo;
        }).collect(Collectors.toList());
    }
    
    private DashboardVO.InfoBoard buildInfoBoard() {
        return DashboardVO.InfoBoard.builder()
                .notices(noticeMapper.selectList(Wrappers.<SysNotice>lambdaQuery().eq(SysNotice::getStatus, "0").last("LIMIT 5"))
                        .stream().map(n -> DashboardVO.NoticeItem.builder().title(n.getTitle()).build()).collect(Collectors.toList()))
                .build();
    }
    
    private DashboardVO.WorkQueue buildWorkQueue() {
        long pendingRepairs = repairMapper.selectCount(Wrappers.<RepairOrder>lambdaQuery().eq(RepairOrder::getStatus, 0));
        long pendingWf = workflowMapper.selectCount(Wrappers.<DormWorkflow>lambdaQuery().eq(DormWorkflow::getStatus, 0));
        return DashboardVO.WorkQueue.builder()
                .activeRepairs(pendingRepairs)
                .pendingWorkflow(pendingWf)
                .isCritical(pendingRepairs > 10 || pendingWf > 20)
                .build();
    }
    
    private List<DashboardVO.NameValue> buildRepairPie() {
        QueryWrapper<RepairOrder> qw = new QueryWrapper<>();
        qw.select("status", "count(*) as total").groupBy("status");
        return repairMapper.selectMaps(qw).stream().map(m ->
                new DashboardVO.NameValue(m.get("status").toString(), m.get("total"), "#409EFF")
        ).collect(Collectors.toList());
    }
}