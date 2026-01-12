package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.UserPreference;

/**
 * 用户宿舍偏好画像服务接口
 * * @author mol
 */
public interface UserPreferenceService extends IService<UserPreference> {
    
    /**
     * 初始化或更新用户偏好
     * @param preference 用户提交的表单数据
     */
    void saveOrUpdatePreference(UserPreference preference);
    
    /**
     * 根据用户ID获取画像 (如果没有则返回默认值)
     * 保证算法层不会遇到 NullPointerException
     * @param userId 用户ID
     * @return 画像实体
     */
    UserPreference getByUserId(Long userId);
}