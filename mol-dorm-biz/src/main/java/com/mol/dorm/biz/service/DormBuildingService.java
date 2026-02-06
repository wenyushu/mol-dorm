package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.DormBuilding;
import java.util.List;

/**
 * 楼栋资源引擎服务接口
 * 🛡️ [防刁民设计契约]：
 * 1. 自动化：拒绝手动录入零散资源，强制要求通过引擎一键生成资源树，从源头杜绝“幽灵房间”。
 * 2. 强一致性：确保楼栋、楼层、房间、床位的用途与性别属性在初始化时通过逻辑锁死。
 * 3. 物理隔离：通过代码强制执行性别防火墙，严禁任何可能导致混住的配置产生。
 */
public interface DormBuildingService extends IService<DormBuilding> {
    
    /**
     * 🚀 1. 一键全自动创建楼栋资源树
     * [功能]：输入楼栋基本信息后，系统自动铺设下属所有楼层、房间、床位。
     * [防刁民红线]：
     * - 唯一性校验：拦截重复的楼栋名及编号。
     * - 性别隔离：混合楼层强制执行“单断点”校验（严禁男-女-男这种交替式排布）。
     * - 初始同步：强制所有子资源继承楼栋的 usageType（学生/教工）。
     */
    void createFullBuilding(DormBuilding building, List<Integer> floorGenders, Integer roomsPerFloor, Integer capacityPerRoom);
    
    /**
     * 🛡️ 2. 严格模式修改楼栋
     * [防刁民红线]：
     * - 属性溯源：若修改了性别限制或用途，系统将触发“深度审计”，强制遍历全楼床位。
     * - 熔断保护：只要楼内有 1 名在住人员，禁止修改核心属性，防止产生“性质冲突”。
     */
    void updateBuildingStrict(DormBuilding building);
    
    /**
     * 🗑️ 3. 严格模式删除楼栋
     * [防刁民红线]：
     * - 级联清理：物理/逻辑清理整棵资源树（楼层->房间->床位）。
     * - 强制校验：执行“清空审计”，若检测到任何在住人员，即刻熔断并报错。
     */
    void removeBuildingStrict(Long buildingId);
    
    /**
     * 🚦 4. 修改楼栋生命周期状态
     * [功能]：切换正常/维修/停用状态。
     * [逻辑点]：当状态从“正常”切为“停用/装修”时，自动校验楼内是否清空。
     */
    void updateStatus(Long id, Integer status);
    
    /**
     * 🛠️ 5. 一键校准房间人数 (数据自愈引擎)
     * [功能]：强制以“床位表”真实占位数为准，刷回“房间表”的冗余计数值。
     * [场景]：用于修复并发异常导致的计数不同步、饱和度状态码（resStatus）偏差。
     * 🛡️ [数据防线]：确保大屏看板展示的人数与物理真实情况 100% 吻合。
     */
    void syncRoomOccupancy(Long buildingId);
}