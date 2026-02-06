package com.mol.dorm.biz.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.UserPreference;
import com.mol.dorm.biz.mapper.UserPreferenceMapper;
import com.mol.dorm.biz.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 用户画像业务实现类 - 最终终极加固版
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserPreferenceServiceImpl extends ServiceImpl<UserPreferenceMapper, UserPreference> implements UserPreferenceService {
    
    private final UserPreferenceMapper preferenceMapper;
    
    private static final List<String> VALID_MBTI = Arrays.asList(
            "INTJ", "INTP", "ENTJ", "ENTP", "INFJ", "INFP", "ENFJ", "ENFP",
            "ISTJ", "ISFJ", "ESTJ", "ESFJ", "ISTP", "ISFP", "ESTP", "ESFP"
    );
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdatePreference(UserPreference pref) {
        validateAndAudit(pref);
        
        if (StrUtil.isNotBlank(pref.getMbtiResult())) {
            pref.setMbtiEI(pref.getMbtiResult().substring(0, 1));
        }
        
        pref.setProfileStatus(1);
        
        // 🛡️ [优化点]：利用 MyBatis-Plus 的 saveOrUpdate 简化代码
        this.saveOrUpdate(pref);
    }
    
    /**
     * 🟢 核心修正：将参数 String gender 改为 Integer gender
     * 这样就完美实现了 UserPreferenceService 接口中的 abstract 方法
     */
    @Override
    public List<UserPreference> getFullProfilesForAllocation(Long campusId, Integer gender) {
        // 调用 Mapper，此时 Mapper 接口也应该是接收 Integer gender
        return preferenceMapper.selectFullProfileForAllocation(campusId, gender);
    }
    
    /**
     * 🛡️ 核心审计方法：逻辑悖论与数据完整性检查
     */
    private void validateAndAudit(UserPreference p) {
        if (p.getUserId() == null) throw new ServiceException("用户 ID 不能为空");
        
        // --- A. 数值越界校验 ---
        checkRange(p.getBedTime(), 1, 6, "就寝时间");
        checkRange(p.getWakeTime(), 1, 6, "起床时间");
        checkRange(p.getSleepQuality(), 1, 4, "睡眠质量");
        checkRange(p.getSnoringLevel(), 0, 3, "打呼噜等级");
        checkRange(p.getPersonalHygiene(), 1, 5, "卫生自评");
        checkRange(p.getOdorTolerance(), 1, 3, "异味容忍度");
        checkRange(p.getGameHabit(), 0, 2, "游戏习惯");
        checkRange(p.getKeyboardAxis(), 1, 3, "键盘轴体");
        
        if (p.getAcTemp() != null && (p.getAcTemp() < 16 || p.getAcTemp() > 30)) {
            throw new ServiceException("空调温度超出 16-30 度生理舒适区间！");
        }
        
        // --- B. 逻辑一致性悖论审计 (核心防御) ---
        if (isVal(p.getPersonalHygiene(), 5) && isVal(p.getCleanFreq(), 4)) {
            throw new ServiceException("检测到逻辑矛盾：您自评'重度洁癖'，但打扫频率却是'随缘'？请诚实填写！");
        }
        
        boolean hasOdorHabit = isVal(p.getEatLuosifen(), 2) || isVal(p.getEatDurian(), 2);
        if (hasOdorHabit && isVal(p.getOdorTolerance(), 1)) {
            throw new ServiceException("生活习惯冲突：您爱吃螺蛳粉/榴莲，但无法容忍异味？室友将无法与您相处。");
        }
        
        if (isVal(p.getSmoking(), 2) && isVal(p.getSmokeTolerance(), 0)) {
            throw new ServiceException("逻辑崩塌：您要在室内抽烟，却拒绝烟味？请重新评估您的忍受度。");
        }
        
        if (isVal(p.getGameHabit(), 0) && isVal(p.getKeyboardAxis(), 3)) {
            throw new ServiceException("键盘选择异常：不玩游戏却选用了干扰性极强的青轴键盘？");
        }
        
        if (isVal(p.getSocialBattery(), 1) && p.getBringGuest() != null && p.getBringGuest() >= 2) {
            throw new ServiceException("社交模式矛盾：深度社恐通常不希望频繁有访客。");
        }
        
        if (isVal(p.getSnoringLevel(), 3) && isVal(p.getSleepQuality(), 4)) {
            throw new ServiceException("分配预警：您打呼噜严重且睡眠极度敏感，建议申请特殊单间。");
        }
        
        // --- C. 数据清洗与标准化 ---
        if (StrUtil.isNotBlank(p.getMbtiResult())) {
            String mbti = p.getMbtiResult().trim().toUpperCase();
            if (!VALID_MBTI.contains(mbti)) {
                throw new ServiceException("MBTI 类型无效：[" + mbti + "] 必须是标准的 16 种类型之一。");
            }
            p.setMbtiResult(mbti);
        }
        
        if (p.getTeamCode() != null) {
            p.setTeamCode(p.getTeamCode().trim());
        }
    }
    
    private void checkRange(Integer val, int min, int max, String fieldName) {
        if (val != null && (val < min || val > max)) {
            throw new ServiceException(fieldName + "选项越界（应在" + min + "-" + max + "之间）");
        }
    }
    
    private boolean isVal(Integer val, int target) {
        return val != null && val == target;
    }
}