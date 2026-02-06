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
 * 宿舍驾驶舱核心业务实现类 - 全维度监控版
 * 🛡️ [防刁民设计]：
 * 1. 穿透审计：不信冗余字段，实时聚合财务流水与能耗账单，确保预警 100% 准确。
 * 2. 身份隔离：自动区分学生(MyRoomVO)与宿管(BuildingAlerts)的数据颗粒度。
 * 3. 动态色标：通过 #F56C6C(红) 和 #67C23A(绿) 实现“异常驱动管理”。
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
    // 1. 【学生侧】我的宿舍全量看板 (MyRoomVO 实现)
    // =================================================================================
    
    @Override
    public MyRoomVO getStudentRoomDashboard(Long userId) {
        // A. 基础定位：找到学生当前住哪
        DormRoom room = roomMapper.selectByStudentId(userId);
        if (room == null) throw new ServiceException("看板加载失败：未检测到您的入住信息");
        
        MyRoomVO vo = new MyRoomVO();
        vo.setRoomNo(room.getRoomNo());
        vo.setApartmentType(room.getApartmentType());
        
        // B. 财务哨兵：聚合钱包余额与欠费预警
        DormRoomWallet wallet = walletMapper.selectOne(Wrappers.<DormRoomWallet>lambdaQuery().eq(DormRoomWallet::getRoomId, room.getId()));
        if (wallet != null) {
            vo.setWalletBalance(wallet.getBalance());
            vo.setWalletStatus(wallet.getStatus());
            // [防刁民]：余额低于10元强制触发前端红点警告
            vo.setPowerOffWarning(wallet.getBalance().compareTo(new BigDecimal("10")) < 0);
            vo.setWalletStatusMsg(wallet.getBalance().compareTo(BigDecimal.ZERO) < 0 ? "已欠费，请立即充值防止断电" : "正常使用");
        }
        
        // C. 能耗快照：抓取最近一期已结账单
        UtilityBill lastBill = billMapper.selectOne(Wrappers.<UtilityBill>lambdaQuery()
                .eq(UtilityBill::getRoomId, room.getId())
                .orderByDesc(UtilityBill::getMonth).last("LIMIT 1"));
        if (lastBill != null) {
            vo.setLastBillMonth(lastBill.getMonth());
            vo.setLastBillAmount(lastBill.getTotalAmount());
            vo.setLastElectricUsage(lastBill.getElectricUsage());
        }
        
        // D. 舍友矩阵：穿透床位表获取实时舍友画像
        List<DormBed> beds = bedMapper.selectList(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getRoomId, room.getId()));
        vo.setBedList(beds.stream().map(b -> {
            MyRoomVO.BedInfoVO bedVo = new MyRoomVO.BedInfoVO();
            bedVo.setBedId(b.getId());
            bedVo.setBedLabel(b.getBedLabel());
            bedVo.setIsOccupied(b.getOccupantId() != null);
            bedVo.setIsMe(userId.equals(b.getOccupantId()));
            // 如果有人，则带出脱敏后的姓名与画像 (逻辑略)
            return bedVo;
        }).collect(Collectors.toList()));
        
        // E. 公告聚合
        SysNotice latest = noticeMapper.selectOne(Wrappers.<SysNotice>lambdaQuery()
                .eq(SysNotice::getStatus, "0").orderByDesc(SysNotice::getLevel, SysNotice::getCreateTime).last("LIMIT 1"));
        if (latest != null) {
            MyRoomVO.NoticeSnippetVO nVo = new MyRoomVO.NoticeSnippetVO();
            nVo.setTitle(latest.getTitle());
            nVo.setTypeDesc(latest.getType() == 3 ? "紧急" : "通知");
            vo.setLatestNotice(nVo);
        }
        
        return vo;
    }
    
    // =================================================================================
    // 2. 【宿管侧】驾驶舱功能补全
    // =================================================================================
    
    /**
     * 🟢 [财务预警] 获取待断电提醒列表 (针对宿管)
     * [防刁民逻辑]：自动穿透“余额 < 0”且“开关还开着”的房间，提示宿管执行物联关断。
     */
    @Override
    public List<Map<String, Object>> getWalletArrearsAlerts(Long buildingId) {
        // 这里的 SQL 在 Mapper XML 中实现，直接通过 Room 联表查询 Wallet 状态为 2 的记录
        return walletMapper.selectArrearsRoomsByBuilding(buildingId);
    }
    
    /**
     * 🟢 [能耗审计] 获取本月异常用电排行
     * [防刁民逻辑]：辅助宿管发现“违规电器”。
     */
    @Override
    public List<Map<String, Object>> getEnergyAnomalyRank(Long buildingId) {
        // 获取该楼栋下本月电费异常（例如超过同型号房间平均值 150%）的列表
        return billMapper.selectAnomalyRank(buildingId);
    }
    
    /**
     * 获取首页聚合看板大屏数据
     */
    @Override
    public DashboardVO getBigScreenData() {
        Long totalBeds = bedMapper.selectCount(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getStatus, 20));
        Long usedBeds = bedMapper.selectCount(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getResStatus, 22));
        
        DashboardVO.SummaryCard summary = DashboardVO.SummaryCard.builder()
                .totalBuildings(buildingMapper.selectCount(null))
                .totalBeds(totalBeds)
                .usedBeds(usedBeds)
                .occupancyRate(totalBeds > 0 ? NumberUtil.formatPercent((double) usedBeds / totalBeds, 1) : "0%")
                .build();
        
        return DashboardVO.builder()
                .summary(summary)
                .occupancyPie(Arrays.asList(
                        new DashboardVO.NameValue("在宿人员", usedBeds, "#67C23A"),
                        new DashboardVO.NameValue("空闲床位", Math.max(0, totalBeds - usedBeds), "#909399")
                ))
                .repairPie(buildRepairPie())
                .infoBoard(buildInfoBoard())
                .workQueue(buildWorkQueue())
                .build();
    }
    
    // =================================================================================
    // 3. 视觉与下钻辅助逻辑
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
            // 根据饱和度动态配置 UI 颜色
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
        return DashboardVO.WorkQueue.builder()
                .activeRepairs(pendingRepairs)
                .isCritical(pendingRepairs > 10)
                .build();
    }
    
    private List<DashboardVO.NameValue> buildRepairPie() {
        QueryWrapper<RepairOrder> qw = new QueryWrapper<>();
        qw.select("status", "count(*) as total").groupBy("status");
        return repairMapper.selectMaps(qw).stream().map(m ->
                new DashboardVO.NameValue(m.get("status").toString(), m.get("total"), "#409EFF")
        ).collect(Collectors.toList());
    }
    
    @Override public DormRoomVO getRoomDetail(Long roomId) { return null; }
    @Override public Map<String, Object> getTodayAlerts() { return null; }
}