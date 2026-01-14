package com.mol.dorm.biz.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.exception.ServiceException;
import com.mol.dorm.biz.entity.UserPreference;
import com.mol.dorm.biz.mapper.UserPreferenceMapper;
import com.mol.dorm.biz.service.UserPreferenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 用户画像业务实现类
 * <p>
 * 集成了严格的数据校验逻辑，防止学生填写无效或矛盾的数据影响分配算法的准确性。
 * </p>
 */
@Service
public class UserPreferenceServiceImpl extends ServiceImpl<UserPreferenceMapper, UserPreference> implements UserPreferenceService {
    
    // MBTI 16种类型白名单
    private static final List<String> VALID_MBTI = Arrays.asList(
            "INTJ", "INTP", "ENTJ", "ENTP",
            "INFJ", "INFP", "ENFJ", "ENFP",
            "ISTJ", "ISFJ", "ESTJ", "ESFJ",
            "ISTP", "ISFP", "ESTP", "ESFP"
    );
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdatePreference(UserPreference pref) {
        // 1. 数据清洗与防刁民校验
        validateAndClean(pref);
        
        // 2. 检查数据库中是否已存在该用户的记录
        // 注意：这里用 getById 是因为 userId 设置了 @TableId(type = IdType.INPUT)
        UserPreference existing = this.getById(pref.getUserId());
        
        if (existing == null) {
            // 新增
            this.save(pref);
        } else {
            // 更新
            // 强制保证 ID 不变
            pref.setUserId(existing.getUserId());
            this.updateById(pref);
        }
    }
    
    /**
     * 核心校验方法：防止离谱数据
     */
    private void validateAndClean(UserPreference p) {
        if (p.getUserId() == null) {
            throw new ServiceException("用户 ID 不能为空");
        }
        
        // ==========================================
        // 1. 基础范围校验 (防止数值越界/恶意修改报文)
        // ==========================================
        
        // 作息 (1-6)
        checkRange(p.getBedTime(), 1, 6, "就寝时间选项无效");
        checkRange(p.getWakeTime(), 1, 6, "起床时间选项无效");
        
        // 睡眠质量 (1-4)
        // 防止刁民填 100 分
        checkRange(p.getSleepQuality(), 1, 4, "睡眠质量只能选1-4");
        
        // 呼噜等级 (0-3)
        checkRange(p.getSnoringLevel(), 0, 3, "打呼噜等级只能选0-3");
        
        // 个人卫生 (1-5)
        checkRange(p.getPersonalHygiene(), 1, 5, "个人卫生自评只能选1-5分");
        
        // 空调温度 (16-30)
        // 防止刁民填 -100 度或者 1000 度
        if (p.getAcTemp() != null) {
            if (p.getAcTemp() < 16 || p.getAcTemp() > 30) {
                throw new ServiceException("空调温度设置不合理，请设置在 16-30 度之间");
            }
        }
        
        // ==========================================
        // 2. 逻辑一致性校验 (防止自相矛盾)
        // ==========================================
        
        // 【卫生悖论】：自评卫生满分(5分)，但打扫频率却是"随缘"(4)
        if (isVal(p.getPersonalHygiene(), 5) && isVal(p.getCleanFreq(), 4)) {
            throw new ServiceException("检测到逻辑矛盾：您自评'重度洁癖'，但打扫频率却是'随缘'？请诚实填写以确保分配准确！");
        }
        
        // 【吸烟悖论】：要在室内抽烟(2)，却无法忍受烟味(0)
        if (isVal(p.getSmoking(), 2) && isVal(p.getSmokeTolerance(), 0)) {
            throw new ServiceException("检测到逻辑矛盾：您选择在室内抽烟，却无法忍受烟味？这将导致无法为您匹配室友。");
        }
        
        // 【作息悖论】：凌晨2点后睡(6)，却要在早上6点起(1)，且不午睡(0)
        // 虽然有这种超人，但为了身体健康和数据准确性，给予警告
        if (isVal(p.getBedTime(), 6) && isVal(p.getWakeTime(), 1) && isVal(p.getSiestaHabit(), 0)) {
            // 这里可以抛异常，也可以只在日志记录。为了防乱填，我们选择抛异常提示。
            throw new ServiceException("您的作息时间设置过于极端（睡4小时且不午休），请确认是否填写有误。");
        }
        
        // ==========================================
        // 3. 格式化清洗
        // ==========================================
        
        // MBTI 清洗：转大写，去空格
        if (StrUtil.isNotBlank(p.getMbtiResult())) {
            String mbti = p.getMbtiResult().trim().toUpperCase();
            if (!VALID_MBTI.contains(mbti)) {
                throw new ServiceException("MBTI类型无效，请输入标准的4位字母(如 INTJ, ENFP)");
            }
            p.setMbtiResult(mbti);
        }
        
        // 组队码清洗
        if (StrUtil.isNotBlank(p.getTeamCode())) {
            p.setTeamCode(p.getTeamCode().trim());
        }
    }
    
    /**
     * 辅助方法：检查数值范围
     */
    private void checkRange(Integer val, int min, int max, String errorMsg) {
        if (val != null) {
            if (val < min || val > max) {
                throw new ServiceException(errorMsg + " (非法值:" + val + ")");
            }
        }
    }
    
    /**
     * 辅助方法：判断 Integer 是否等于某个值 (处理 null 安全)
     */
    private boolean isVal(Integer val, int target) {
        return val != null && val == target;
    }
}