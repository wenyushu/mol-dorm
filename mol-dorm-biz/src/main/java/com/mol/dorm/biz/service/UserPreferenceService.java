package com.mol.dorm.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.dorm.biz.entity.UserPreference;

public interface UserPreferenceService extends IService<UserPreference> {
    
    /**
     * 保存或更新用户画像 (包含防刁民校验)
     */
    void saveOrUpdatePreference(UserPreference preference);
    
    // 注意：这里不需要写 getByUserId，因为 IService 里已经有 getById 了
}