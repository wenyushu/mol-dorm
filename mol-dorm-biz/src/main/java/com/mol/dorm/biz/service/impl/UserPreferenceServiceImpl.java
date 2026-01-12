package com.mol.dorm.biz.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.UserPreference;
import com.mol.dorm.biz.mapper.UserPreferenceMapper;
import com.mol.dorm.biz.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户画像服务实现类
 * <p>
 * 负责用户生活习惯、偏好的录入与查询。
 * 包含严格的数据校验，防止“刁民”乱填数据干扰算法。
 * </p>
 *
 * @author mol
 */
@Service
@RequiredArgsConstructor
public class UserPreferenceServiceImpl extends ServiceImpl<UserPreferenceMapper, UserPreference> implements UserPreferenceService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdatePreference(UserPreference preference) {
        // 1. 基础校验：防止ID为空
        if (preference.getUserId() == null) {
            throw new ServiceException("用户 ID 不能为空");
        }
        
        // 2. 防刁民校验：数值范围检查
        // 比如：睡眠质量只能是 1-4，如果填了 0 或 100，说明是乱填的
        checkRange(preference.getBedTime(), 1, 6, "就寝时间");
        checkRange(preference.getWakeTime(), 1, 6, "起床时间");
        checkRange(preference.getCleanFreq(), 1, 4, "打扫频率");
        checkRange(preference.getPersonalHygiene(), 1, 5, "个人卫生评分");
        
        // 3. 组队码校验 (可选)
        // 如果填了组队码，长度限制一下，防止恶意长串
        if (StrUtil.isNotBlank(preference.getTeamCode()) && preference.getTeamCode().length() > 8) {
            throw new ServiceException("组队码长度不能超过 8 位");
        }
        
        // 4. 执行保存或更新
        this.saveOrUpdate(preference);
    }
    
    @Override
    public UserPreference getByUserId(Long userId) {
        UserPreference pref = this.getById(userId);
        if (pref == null) {
            // 如果用户没填，返回一个安全的默认对象，避免算法空指针
            pref = new UserPreference();
            pref.setUserId(userId);
            
            // 赋默认值 (大众化配置)
            pref.setSmoking(0); // 默认不抽烟
            pref.setSmokeTolerance(1); // 默认能忍受(随和)
            pref.setBedTime(2); // 默认23点
            pref.setWakeTime(2); // 默认7点
            pref.setAcTemp(26); // 默认26度
            pref.setCleanFreq(2); // 每周
            pref.setSleepQuality(2); // 睡眠普通
            pref.setGameVoice(1); // 声音普通
            pref.setKeyboardAxis(1); // 普通键盘
            // ...其他可赋默认值
        }
        return pref;
    }
    
    /**
     * 辅助校验方法
     */
    private void checkRange(Integer value, int min, int max, String fieldName) {
        if (value != null && (value < min || value > max)) {
            throw new ServiceException(fieldName + "数值异常，请按规定选项填写");
        }
    }
}