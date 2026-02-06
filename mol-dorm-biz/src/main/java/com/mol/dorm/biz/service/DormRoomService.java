package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.vo.MyRoomVO; // 🟢 引入移动端聚合模型
import java.util.List;

/**
 * 房间管理服务接口 - 资源树 Level 4 (工业级严苛契约)
 * 🛡️ [防刁民设计契约]：
 * 1. 物理强一致：房间的性别、用途必须绝对服从于楼层行政定义。
 * 2. 状态自动化：禁止手动干预人数与饱和度，一切以底层床位物理统计为准。
 * 3. 语义自对齐：房型描述由算法动态派生，确保 capacity 与 apartmentType 绝对对齐。
 */
public interface DormRoomService extends IService<DormRoom> {
    
    /**
     * 安全保存房间
     * [校验]：1. 性别必须与所属楼层对齐；2. 强制触发算法生成语义房型（如 12 -> 十二人间）。
     */
    void saveRoomStrict(DormRoom room);
    
    /**
     * 修改房间生命周期
     * [拦截]：严禁对有人的房间执行“维修、停用”等非正常状态切换，防止在住学生档案悬空。
     */
    void updateRoomStatus(Long roomId, Integer status);
    
    /**
     * [核心引擎] 刷新房间资源饱和度状态码
     * [逻辑]：穿透床位表统计真实人数，自动修正 21(空闲) 至 26(满员) 状态码。
     */
    void refreshResourceStatus(Long roomId);
    
    /**
     * 安全删除房间
     * [拦截]：执行“物理净空”审计，只要房间内有一个床位有人，即刻触发熔断，拒绝注销。
     */
    void removeRoomStrict(Long roomId);
    
    /**
     * 获取楼层下属房间列表
     */
    List<DormRoom> getByFloor(Long floorId);
    
    /**
     * 🛡️ [高阶引擎] 房间容量动态调整 (如：4人寝变5人寝)
     * [逻辑]：调整后自动重算语义房型，适配南北方不同规模寝室场景。
     */
    void adjustRoomCapacity(Long roomId, Integer newCapacity);
    
    /**
     * 房间安全评估 (资产熔断机制)
     * [联动]：根据资产损坏程度（FixedAsset 状态），自动判定房间安全等级并决定是否下架。
     */
    void evaluateRoomSafety(Long roomId);
    
    /**
     * 🟢 [移动端聚合] 获取学生个人宿舍看版数据
     * [逻辑]：聚合财务余额、上月能耗快照、舍友画像及紧急公告。
     * @param studentId 当前登录学生ID
     */
    MyRoomVO getMyRoomDashboard(Long studentId);
}