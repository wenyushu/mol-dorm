package com.mol.dorm.biz.service;

/**
 * 人工调宿/强制调整服务接口 (管理员专用)
 * <p>
 * 🛡️ 防刁民设计原则：
 * 1. 所有的 ID 参数必须校验存在性。
 * 2. 所有的状态变更必须加 @Transactional 事务。
 * 3. 涉及多人操作（如互换）必须防止“左右互搏”（自己换自己）。
 * </p>
 *
 * @author mol
 */
public interface ManualAdjustmentService {
    
    /**
     * 强制双人互换床位
     * <p>
     * ⚠️ 警告：此操作会无视目标房间的额定人数限制（因为总人数不变）。
     * </p>
     * @param studentIdA 学生A ID
     * @param studentIdB 学生B ID
     */
    void swapBeds(Long studentIdA, Long studentIdB);
    
    /**
     * 强制搬迁 或 强制退宿
     * <p>
     * 如果 targetBedId 不为空，必须确保该床位当前【空闲】。
     * </p>
     * @param studentId 学生 ID
     * @param targetBedId 目标床位 ID。如果为 null，则表示【强制退宿】
     */
    void moveUserToBed(Long studentId, Long targetBedId);
    
    /**
     * 批量毕业生离校
     * <p>
     * ⚠️ 高危操作：将清空指定年份入学的所有学生的床位。
     * </p>
     * @param year 入学年份 (例如 2021)
     */
    void batchGraduate(Integer year);
}