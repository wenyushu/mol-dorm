package com.mol.dorm.biz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.dorm.biz.entity.UserPreference;
import com.mol.dorm.biz.mapper.UserPreferenceMapper;
import com.mol.dorm.biz.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户画像服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserPreferenceServiceImpl extends ServiceImpl<UserPreferenceMapper, UserPreference> implements UserPreferenceService {
    
    @Override
    public void saveOrUpdatePreference(UserPreference preference) {
        // 这里可以添加业务校验，比如校验 TeamCode 是否存在等
        this.saveOrUpdate(preference);
    }
    
    @Override
    public UserPreference getByUserId(Long userId) {
        UserPreference pref = this.getById(userId);
        if (pref == null) {
            // 如果用户没填，返回一个安全的默认对象
            pref = new UserPreference();
            pref.setUserId(userId);
            
            // 赋默认值，避免算法计算距离时除以0或空指针
            pref.setSmoking(0); // 默认不抽烟
            pref.setSmokeTolerance(1); // 默认能忍受(随和)
            pref.setBedTime(2); // 默认23点
            pref.setWakeTime(2); // 默认7点
            pref.setAcTempSummer(3); // 默认26度
            pref.setCleanFreq(2); // 每周
            pref.setSleepLight(1); // 睡眠普通
            pref.setGameVoice(1); // 声音普通
            pref.setKeyboardType(0); // 普通键盘
            pref.setMbtiEI("E"); // 默认 E 人
            pref.setSocialBattery(3);
            pref.setVisitors(1);
            pref.setIsAcg(1); // 路人
        }
        return pref;
    }
}