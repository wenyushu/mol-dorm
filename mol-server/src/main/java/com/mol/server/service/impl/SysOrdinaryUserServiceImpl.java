package com.mol.server.service.impl;

import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.entity.SysMajor;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import com.mol.server.service.SysMajorService;
import com.mol.server.service.SysOrdinaryUserService;
import com.mol.server.service.UsernameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * 普通用户(学生/教职工) 核心业务实现类
 * <p>
 * 🛡️ 安全特性 (防刁民版):
 * 1. 入参清洗: 严格校验身份证、年份合理性
 * 2. 格式白名单: 账号生成后进行正则匹配，拒绝一切特殊字符
 * 3. 密码兜底: 强制加密存储
 * </p>
 *
 * @author mol
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysOrdinaryUserServiceImpl extends ServiceImpl<SysOrdinaryUserMapper, SysOrdinaryUser> implements SysOrdinaryUserService {
    
    private final UsernameGenerator usernameGenerator;
    private final SysMajorService majorService;
    
    // 🔒 核心正则防火墙：只允许 数字 和 大写字母，其他一切符号滚蛋
    // 格式解释：4位数字 + 1到3位大写字母 + 2位数字 + 4位数字
    // 例子匹配：2026B010001 (匹配), 2026JZG020005 (匹配), 2026' OR 1=1 (拦截)
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^[0-9]{4}[A-Z]{1,3}[0-9]{2}[0-9]{4}$");
    
    // =================================================================================
    // 1. 新增用户 (防刁民核心入口)
    // =================================================================================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveUser(SysOrdinaryUser user) {
        // --- 1. 基础防刁民校验 ---
        if (user.getUserCategory() == null) {
            throw new ServiceException("非法请求：必须指定用户类别");
        }
        
        // 年份合理性校验 (防止录入公元前或未来的穿越者)
        Integer year = user.getEntryYear();
        if (year == null) year = user.getEnrollmentYear(); // 兼容字段
        if (year != null) {
            int currentYear = Year.now().getValue();
            if (year < 2000 || year > currentYear + 1) {
                throw new ServiceException("年份异常：只能录入2000年至今的学生/教工");
            }
        }
        
        // 身份证强校验
        if (StrUtil.isNotBlank(user.getIdCard()) && !IdcardUtil.isValidCard(user.getIdCard())) {
            throw new ServiceException("身份证号码无效，请核对后重新输入");
        }
        
        // --- 2. 智能填充 ---
        parseIdCardInfo(user);
        
        // --- 3. 账号生成与安全审查 ---
        if (StrUtil.isBlank(user.getUsername())) {
            // A. 自动生成
            String generatedAccount = generateUniqueAccount(user);
            
            // B. 🔥【核心】正则防火墙审查
            // 虽然是系统生成的，但为了防止上游脏数据污染（比如 eduLevel 混入了特殊字符），必须在这里做最后一道防线
            if (!ACCOUNT_PATTERN.matcher(generatedAccount).matches()) {
                log.error("账号生成安全阻断 -> 生成结果: {}", generatedAccount);
                throw new ServiceException("系统内部安全拦截：生成的账号格式异常，请联系管理员");
            }
            
            user.setUsername(generatedAccount);
        } else {
            // C. 如果是前端手填的，必须接受更严格的审查
            if (!ACCOUNT_PATTERN.matcher(user.getUsername()).matches()) {
                throw new ServiceException("学号/工号格式错误！仅允许数字和大写字母组合 (如: 2026B010001)");
            }
            // D. 重复性校验
            if (checkUsernameExists(user.getUsername())) {
                throw new ServiceException("该账号已存在: " + user.getUsername());
            }
        }
        
        // --- 4. 密码安全兜底 ---
        if (StrUtil.isBlank(user.getPassword())) {
            String defaultPwd = "123456";
            if (StrUtil.isNotBlank(user.getIdCard())) {
                defaultPwd = StrUtil.subSuf(user.getIdCard(), user.getIdCard().length() - 6);
            }
            user.setPassword(BCrypt.hashpw(defaultPwd));
        } else {
            user.setPassword(BCrypt.hashpw(user.getPassword()));
        }
        
        // --- 5. 状态默认值 ---
        if (user.getStatus() == null) user.setStatus("0");
        if (user.getEntryDate() == null) user.setEntryDate(LocalDate.now());
        
        return super.save(user);
    }
    
    // =================================================================================
    // 2. 修改用户
    // =================================================================================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysOrdinaryUser user) {
        if (StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(BCrypt.hashpw(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        
        if (StrUtil.isNotBlank(user.getIdCard())) {
            if (!IdcardUtil.isValidCard(user.getIdCard())) {
                throw new ServiceException("身份证格式错误");
            }
            parseIdCardInfo(user);
        }
        return super.updateById(user);
    }
    
    // =================================================================================
    // 3. 密码管理
    // =================================================================================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, String newPassword) {
        if (StrUtil.isBlank(newPassword) || newPassword.length() < 6) {
            throw new ServiceException("密码长度不能少于6位");
        }
        SysOrdinaryUser user = new SysOrdinaryUser();
        user.setId(userId);
        user.setPassword(BCrypt.hashpw(newPassword));
        this.updateById(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        SysOrdinaryUser user = this.getById(userId);
        if (user == null) throw new ServiceException("用户不存在");
        
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new ServiceException("原密码错误");
        }
        if (newPassword.length() < 6) throw new ServiceException("新密码长度不能少于6位");
        
        user.setPassword(BCrypt.hashpw(newPassword));
        this.updateById(user);
    }
    
    // =================================================================================
    // 私有辅助方法
    // =================================================================================
    
    private String generateUniqueAccount(SysOrdinaryUser user) {
        // 优先使用传入的 enrollmentYear，没有则用当前年份
        Integer year = user.getEnrollmentYear();
        if (year == null) year = Year.now().getValue();
        
        // --- 学生逻辑 ---
        if (user.getUserCategory() == 0) {
            // 自动补全学院 の 逻辑
            if (user.getCollegeId() == null && user.getMajorId() != null) {
                SysMajor major = majorService.getById(user.getMajorId());
                if (major != null) {
                    user.setCollegeId(major.getCollegeId());
                    user.setEduLevel(convertLevelToCode(major.getLevel()));
                }
            }
            if (user.getCollegeId() == null) {
                throw new ServiceException("生成学号失败：请选择学院或专业");
            }
            
            return usernameGenerator.generateStudentAccount(
                    year,
                    user.getEduLevel(),
                    user.getCollegeId()
            );
        }
        // --- 教职工逻辑 ---
        else {
            if (user.getDeptId() == null) {
                throw new ServiceException("生成工号失败：请选择所属部门");
            }
            return usernameGenerator.generateStaffAccount(year, user.getDeptId());
        }
    }
    
    private void parseIdCardInfo(SysOrdinaryUser user) {
        String idCard = user.getIdCard();
        if (StrUtil.isBlank(idCard) || !IdcardUtil.isValidCard(idCard)) return;
        try {
            // 1. 解析生日
            String birth = IdcardUtil.getBirthByIdCard(idCard);
            user.setBirthDate(LocalDate.parse(birth, DateTimeFormatter.ofPattern("yyyyMMdd")));
            
            // 2. 解析籍贯
            if (StrUtil.isBlank(user.getHometown())) {
                user.setHometown(IdcardUtil.getProvinceByIdCard(idCard));
            }
            
            // 3. 解析性别 (Hutool 标准: 1男 0女)
            // 🟢 直接转 String 即可，完美对齐 "0-女 1-男"
            int genderVal = IdcardUtil.getGenderByIdCard(idCard);
            user.setGender(String.valueOf(genderVal));
            
        } catch (Exception ignored) {
            log.warn("身份证解析失败: {}", idCard);
        }
    }
    
    private String convertLevelToCode(String levelName) {
        if (levelName == null) return "B";
        // 规范化代码，防止中文混入
        if (levelName.contains("专科")) return "Z";
        if (levelName.contains("专升本")) return "ZB";
        if (levelName.contains("研究生") || levelName.contains("硕士")) return "Y";
        if (levelName.contains("博士")) return "D";
        return "B";
    }
    
    private boolean checkUsernameExists(String username) {
        return this.lambdaQuery().eq(SysOrdinaryUser::getUsername, username).exists();
    }
}