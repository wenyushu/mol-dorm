package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.DormWorkflow;

/**
 * 宿舍业务流转服务接口 - 数字化调度中心
 * 🛡️ [防刁民设计]：
 * 1. 物理强隔离：定义了从申请到物理执行的完整闭环，严禁绕过流程直接篡改床位。
 * 2. 状态原子性：确保入住、调宿、退宿、毕业清算的每一个动作都具备物理一致性。
 */
public interface DormWorkflowService extends IService<DormWorkflow> {
    
    // =================================================================================
    // 1. C端申请方法 (学生/教工发起)
    // =================================================================================
    
    /**
     * 提交入住申请
     * [逻辑]：校验用户是否有在途申请，防止重复提交。
     */
    void submitCheckIn(DormWorkflow app);
    
    /**
     * 提交调宿/换房申请
     * [联动]：自动锁定目标房间意向，防止审批期间被他人抢占。
     */
    void submitRoomChange(DormWorkflow app);
    
    /**
     * 提交互换床位申请
     * 🛡️ [防刁民逻辑]：必须双方均同意且性别、年级符合换宿规则。
     */
    void submitMutualExchange(DormWorkflow app);
    
    /**
     * 提交退宿申请
     */
    void submitCheckOut(DormWorkflow app);
    
    /**
     * 提交假期留校申请
     */
    void submitHolidayStay(DormWorkflow app);
    
    // =================================================================================
    // 2. B端管理方法 (宿管/管理员执行)
    // =================================================================================
    
    /**
     * 统一审批处理出口
     */
    void handleApproval(Long applicationId, Boolean agree, String reply);
    
    /**
     * [物理引擎] 强制搬迁/退宿
     * 🛡️ [防逻辑死锁]：自动维护资源状态码，同步刷新房间的饱和度。
     * @param targetBedId 为 null 时执行物理退宿
     */
    void forceMove(Long userId, Long targetBedId);
    
    /**
     * [上帝模式] 强制互换双方床位
     * [演示亮点]：原子性交换，确保不会出现“一个人两个床位”的中间态。
     */
    void forceSwap(Long userIdA, Long userIdB);
    
    /**
     * [归档引擎] 数字化毕业一键清退 (灭世级增强版)
     * 🛡️ [动态学制算法]：(入学年 + 学制 + 异动年限) <= 当前年 则清退。
     * @return 返回成功处理的总人数
     */
    int batchGraduate(Integer currentYear);
}