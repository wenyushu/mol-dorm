package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.DormBed;

/**
 * 床位业务接口
 * <p>
 * 负责处理具体的入住分配和退宿逻辑。
 * </p>
 *
 * @author mol
 */
public interface DormBedService extends IService<DormBed> {
    
    /**
     * 分配床位 (入住)
     * <p>
     * 核心校验：
     * 1. 目标床位必须为空。
     * 2. 性别门禁：学生性别必须与房间/楼栋要求一致。
     * 3. 并发安全：使用乐观锁防止多人同时抢占同一床位。
     * </p>
     * @param bedId 目标床位 ID
     * @param userId 学生 ID
     */
    void assignUserToBed(Long bedId, Long userId);
    
    /**
     * 释放床位 (退宿)
     * <p>
     * 清空床位上的学生信息，并更新房间的当前居住人数。
     * </p>
     * @param bedId 床位 ID
     */
    void releaseBed(Long bedId);
}