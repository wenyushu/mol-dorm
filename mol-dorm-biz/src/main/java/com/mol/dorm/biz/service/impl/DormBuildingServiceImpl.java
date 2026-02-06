package com.mol.dorm.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.constant.DormConstants;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.*;
import com.mol.dorm.biz.mapper.*;
import com.mol.dorm.biz.service.DormBuildingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 楼栋资源引擎实现类 - 工业级自动化生成引擎
 * 🛡️ [防刁民逻辑矩阵]：
 * 1. 动态语义引擎：利用算法自动将 1-20+ 数字转为中文房型（如：十二人间），杜绝硬编码，适配全国寝室规模。
 * 2. 物理占用审计：在修改楼栋性质、注销楼栋或切换停用状态时，强制穿透到底层床位核验人员，防止“暴力删库”。
 * 3. 冗余 ID 注入：在生成资源时强制注入 campusId 等冗余字段，确保大数据分析时无需多表 JOIN。
 * 4. 性别安全红线：针对混合楼栋，强制执行“单断点”排布审计，防止男女楼层交叉错乱。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DormBuildingServiceImpl extends ServiceImpl<DormBuildingMapper, DormBuilding> implements DormBuildingService {
    
    private final DormFloorMapper floorMapper;
    private final DormRoomMapper roomMapper;
    private final DormBedMapper bedMapper;
    
    /**
     * 🚀 1. 楼栋资源全链条初始化 (一键生成引擎)
     * @param building 楼栋基础信息
     * @param floorGenders 各楼层性别定义 (List长度需等于总楼层数)
     * @param roomsPerFloor 每层核定房间数
     * @param capacityPerRoom 房间核定床位数 (支持 1-20+ 人大寝室)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createFullBuilding(DormBuilding building, List<Integer> floorGenders, Integer roomsPerFloor, Integer capacityPerRoom) {
        // [审计 1]：校内唯一性校验，防止楼号、楼名重复
        checkUniqueness(building);
        
        // [审计 2]：针对混合性别楼栋，校验楼层排布逻辑（低层男/高层女，严禁交替）
        if (Objects.equals(building.getGenderLimit(), 3)) {
            validateMixedGenderLogic(floorGenders);
        }
        
        // [算法 3]：调用动态语义转换算法，将数字容量转为中文描述 (如 12 -> 十二人间)
        String apartmentType = convertToApartmentType(capacityPerRoom);
        
        building.setStatus(DormConstants.LC_NORMAL);
        building.setDelFlag("0");
        this.save(building);
        
        log.info("📢 资源引擎启动：开始初始化楼栋 [{}], 识别房型为: [{}]", building.getBuildingName(), apartmentType);
        
        // [执行 4]：三级资源递归生成 (Floor -> Room -> Bed)
        for (int i = 1; i <= building.getFloorCount(); i++) {
            // A. 生成楼层
            DormFloor floor = new DormFloor();
            floor.setBuildingId(building.getId());
            floor.setFloorNum(i);
            // 混合楼取列表对应性别，纯色楼取楼栋全局定义性别
            Integer currentGender = (building.getGenderLimit() == 3) ? floorGenders.get(i - 1) : building.getGenderLimit();
            floor.setGenderLimit(currentGender);
            floor.setStatus(DormConstants.LC_NORMAL);
            floorMapper.insert(floor);
            
            for (int j = 1; j <= roomsPerFloor; j++) {
                // B. 生成房间 (编码规范：层号+2位流水，如 101, 1205)
                String roomNo = String.format("%d%02d", i, j);
                DormRoom room = new DormRoom();
                room.setBuildingId(building.getId());
                room.setFloorId(floor.getId());
                room.setCampusId(building.getCampusId()); // 🛡️ 注入全链路冗余 ID，保障大数据穿透效率
                room.setRoomNo(roomNo);
                room.setCapacity(capacityPerRoom);
                room.setApartmentType(apartmentType);    // 🛡️ 算法自动生成的中文描述
                room.setCurrentNum(0);
                room.setGender(String.valueOf(currentGender));
                room.setUsageType(building.getUsageType());
                room.setStatus(DormConstants.LC_NORMAL);
                room.setResStatus(DormConstants.RES_EMPTY);
                roomMapper.insert(room);
                
                for (int k = 1; k <= capacityPerRoom; k++) {
                    // C. 生成床位 (编码规范：房号-床号，如 101-4, 1205-12)
                    DormBed bed = new DormBed();
                    bed.setBuildingId(building.getId());
                    bed.setFloorId(floor.getId());
                    bed.setRoomId(room.getId());
                    bed.setCampusId(building.getCampusId());
                    bed.setBedLabel(roomNo + "-" + k);
                    bed.setOccupantType(building.getUsageType());
                    bed.setStatus(DormConstants.LC_NORMAL);
                    bed.setResStatus(DormConstants.RES_EMPTY);
                    bedMapper.insert(bed);
                }
            }
        }
        log.info("🏁 楼栋资源生成完毕，总计生成房间数: {}", building.getFloorCount() * roomsPerFloor);
    }
    
    /**
     * 🛡️ 2. 楼栋状态变更 (解决接口契约报错的关键方法)
     * [防刁民逻辑]：如果要将楼栋设为非正常状态 (维修/封锁/注销)，必须物理审计在住人数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        // [审计]：除了切回“正常(20)”，其他状态切换必须确保“净楼”
        if (!Objects.equals(status, DormConstants.LC_NORMAL)) {
            manualTraverseCheck(id, "执行状态切换至 [" + status + "]");
        }
        
        DormBuilding b = new DormBuilding();
        b.setId(id);
        b.setStatus(status);
        
        if (!this.updateById(b)) {
            throw new ServiceException("并发冲突：楼栋状态更新失败。");
        }
    }
    
    /**
     * 🛡️ 3. 动态房型语义转换算法 (兼容 1-20+ 人寝)
     * [设计背景]：北方供暖区存在大容量寝室，本算法根据数值权位动态解析中文。
     */
    private String convertToApartmentType(Integer capacity) {
        if (capacity == null || capacity <= 0) return "未知房型";
        String[] units = {"", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
        StringBuilder sb = new StringBuilder();
        if (capacity < 10) {
            sb.append(units[capacity]);
        } else if (capacity < 20) {
            // 处理 10-19: 如 12 -> 十二
            sb.append("十").append(units[capacity % 10]);
        } else {
            // 处理 20 及以上: 如 20 -> 二十, 24 -> 二十四
            int shi = capacity / 10;
            int ge = capacity % 10;
            sb.append(units[shi]).append("十").append(units[ge]);
        }
        return sb.append("人间").toString();
    }
    
    /**
     * 🛡️ 4. 严格修改楼栋核心属性
     * [拦截]：严禁在有人居住时修改楼栋性别上限或用途（如：学生楼变教工楼）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBuildingStrict(DormBuilding building) {
        checkUniqueness(building);
        DormBuilding old = this.getById(building.getId());
        
        if (!Objects.equals(old.getUsageType(), building.getUsageType()) ||
                !Objects.equals(old.getGenderLimit(), building.getGenderLimit())) {
            manualTraverseCheck(building.getId(), "核心性质改造");
            syncResourceProperties(building);
        }
        
        this.updateById(building);
    }
    
    /**
     * 🛡️ 5. 安全注销楼栋
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBuildingStrict(Long buildingId) {
        // 确保楼内无人才允许清理资源树
        manualTraverseCheck(buildingId, "注销/拆除楼栋");
        
        bedMapper.delete(Wrappers.<DormBed>lambdaQuery().eq(DormBed::getBuildingId, buildingId));
        roomMapper.delete(Wrappers.<DormRoom>lambdaQuery().eq(DormRoom::getBuildingId, buildingId));
        floorMapper.delete(Wrappers.<DormFloor>lambdaQuery().eq(DormFloor::getBuildingId, buildingId));
        this.removeById(buildingId);
    }
    
    /**
     * 🛠️ 6. 数据自愈引擎：一键校准房间人数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncRoomOccupancy(Long buildingId) {
        // 物理统计：通过原子级床位表计票
        List<Map<String, Object>> realCounts = bedMapper.selectMaps(Wrappers.<DormBed>query()
                .select("room_id as roomId", "count(occupant_id) as realCount")
                .eq("building_id", buildingId).isNotNull("occupant_id").groupBy("room_id"));
        
        Map<Long, Long> realCountMap = realCounts.stream().collect(Collectors.toMap(
                m -> Long.valueOf(m.get("roomId").toString()),
                m -> (Long) m.get("realCount")
        ));
        
        List<DormRoom> rooms = roomMapper.selectList(Wrappers.<DormRoom>lambdaQuery().eq(DormRoom::getBuildingId, buildingId));
        for (DormRoom room : rooms) {
            long realNum = realCountMap.getOrDefault(room.getId(), 0L);
            if (!Objects.equals(room.getCurrentNum(), (int)realNum)) {
                room.setCurrentNum((int)realNum);
                roomMapper.updateById(room);
            }
        }
    }
    
    // =================================================================================
    // 🛡️ 内部审计防火墙
    // =================================================================================
    
    private void validateMixedGenderLogic(List<Integer> genders) {
        if (CollUtil.isEmpty(genders)) throw new ServiceException("配置异常：缺少楼层性别映射表");
        int switches = 0;
        for (int i = 0; i < genders.size() - 1; i++) {
            if (!genders.get(i).equals(genders.get(i + 1))) switches++;
        }
        // [防刁民点]：严禁 男-女-男 这种非法交叉排布
        if (switches > 1) throw new ServiceException("性别隔离审计失败：楼层性别排布存在多次交替断点！");
    }
    
    private void checkUniqueness(DormBuilding b) {
        Long count = baseMapper.selectCount(Wrappers.<DormBuilding>lambdaQuery()
                .and(qw -> qw.eq(DormBuilding::getBuildingName, b.getBuildingName()).or().eq(DormBuilding::getBuildingNo, b.getBuildingNo()))
                .ne(b.getId() != null, DormBuilding::getId, b.getId()));
        if (count > 0) throw new ServiceException("录入冲突：楼栋名称或编号已占用");
    }
    
    /**
     * 深度审计：穿透床位层，确认当前楼栋是否处于“净空”状态。
     * [防刁民逻辑]：直接从原子级的床位表统计，哪怕房间表数据由于并发出错了，这里也能守住底线。
     */
    private void manualTraverseCheck(Long buildingId, String action) {
        // 🛡️ 注意这里必须引用 DormBed 自身的字段 getBuildingId
        Long occupants = bedMapper.selectCount(Wrappers.<DormBed>lambdaQuery()
                .eq(DormBed::getBuildingId, buildingId) // 👈 修正这里：由 DormBuilding::getId 改为 DormBed::getBuildingId
                .isNotNull(DormBed::getOccupantId));
        
        if (occupants > 0) {
            log.warn("🚨 安全拦截：试图对有人员在住的楼栋执行 [{}]，当前在住人数：{}", action, occupants);
            throw new ServiceException(StrUtil.format("安全拦截：[{}] 失败！楼内尚有 {} 名人员在住，请先搬迁人员。", action, occupants));
        }
    }
    
    private void syncResourceProperties(DormBuilding b) {
        roomMapper.update(null, Wrappers.<DormRoom>lambdaUpdate()
                .eq(DormRoom::getBuildingId, b.getId())
                .set(DormRoom::getUsageType, b.getUsageType())
                .set(DormRoom::getGender, String.valueOf(b.getGenderLimit())));
    }
}