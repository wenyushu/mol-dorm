package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.UserPreference;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户全维度画像服务接口
 */
public interface UserPreferenceService extends IService<UserPreference> {
    
    /**
     * 安全保存或更新画像 (含深度审计)
     */
    void saveOrUpdatePreference(UserPreference pref);
    
    /**
     * 加载全维度分配画像
     */
    List<UserPreference> getFullProfilesForAllocation(Long campusId, Integer gender);
}