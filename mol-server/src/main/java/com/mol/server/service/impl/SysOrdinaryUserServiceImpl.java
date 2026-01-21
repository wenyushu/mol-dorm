package com.mol.server.service.impl;

import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.entity.SysUserRole;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.component.UsernameGenerator;
import com.mol.server.entity.SysMajor;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import com.mol.server.mapper.SysUserRoleMapper;
import com.mol.server.service.SysMajorService;
import com.mol.server.service.SysOrdinaryUserService;
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
 * 核心功能：
 * 1. 用户档案管理 (增删改查)
 * 2. 账号自动生成 (基于 Redis 原子计数)
 * 3. 密码安全管理 (BCrypt 加密)
 * 4. 身份证信息智能解析
 * </p>
 *
 * @author mol
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysOrdinaryUserServiceImpl extends ServiceImpl<SysOrdinaryUserMapper, SysOrdinaryUser> implements SysOrdinaryUserService {
    
    // 注入自定义的 ID 生成器组件 (非静态)
    private final UsernameGenerator usernameGenerator;
    // 注入专业服务，用于查询专业所属学院
    private final SysMajorService majorService;
    // 注入角色关联 Mapper，用于分配初始角色
    private final SysUserRoleMapper userRoleMapper;
    
    // 🔒 账号格式防火墙正则
    // 允许 10 到 30 位的数字和大写字母组合，拒绝特殊符号
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^[0-9A-Z]{10,30}$");
    
    // =================================================================================
    // 1. 新增用户 (核心入口)
    // =================================================================================
    
    /**
     * 新增用户 (学生或教工)
     * 包含完整的校验、生成、加密、赋权流程
     *
     * @param user 前端提交的用户实体
     * @return 是否保存成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 开启事务，任何一步失败都回滚
    public boolean saveUser(SysOrdinaryUser user) {
        // --- 1. 基础参数防刁民校验 ---
        // 必须指定是学生还是老师
        if (user.getUserCategory() == null) {
            throw new ServiceException("非法请求：必须指定用户类别 (0-学生, 1-教工)");
        }
        
        // 年份合理性校验 (防止录入 1900 年或 3000 年的数据)
        Integer year = user.getEntryYear();
        if (year == null) year = user.getEnrollmentYear(); // 学生取入学年份
        if (year != null) {
            int currentYear = Year.now().getValue();
            // 限制年份范围，防止脏数据
            if (year < 2000 || year > currentYear + 1) {
                throw new ServiceException("年份异常：只能录入2000年至今的学生/教工");
            }
        }
        
        // 身份证强校验 (利用 Hutool 工具包)
        if (StrUtil.isNotBlank(user.getIdCard()) && !IdcardUtil.isValidCard(user.getIdCard())) {
            throw new ServiceException("身份证号码无效，请核对后重新输入");
        }
        
        // --- 2. 智能填充 (从身份证解析生日、籍贯、性别) ---
        // 减少人工录入工作量，提高数据准确性
        parseIdCardInfo(user);
        
        // --- 3. 账号生成与安全审查 ---
        if (StrUtil.isBlank(user.getUsername())) {
            // A. 自动生成模式：调用 Redis 生成器生成唯一学号/工号
            String generatedAccount = generateUniqueAccount(user);
            
            // B. 正则防火墙审查 (防止生成器出现异常字符)
            if (!ACCOUNT_PATTERN.matcher(generatedAccount).matches()) {
                log.error("账号生成安全阻断 -> 生成结果: {}", generatedAccount);
                throw new ServiceException("系统内部安全拦截：生成的账号格式异常，请联系管理员");
            }
            user.setUsername(generatedAccount);
        } else {
            // C. 手动输入模式：严格校验格式
            if (!ACCOUNT_PATTERN.matcher(user.getUsername()).matches()) {
                throw new ServiceException("账号格式错误！仅允许10-30位数字和大写字母组合");
            }
            // D. 查重校验 (防止学号冲突)
            if (checkUsernameExists(user.getUsername())) {
                throw new ServiceException("该账号已存在: " + user.getUsername());
            }
        }
        
        // --- 4. 密码安全兜底 ---
        String rawPwd = user.getPassword();
        if (StrUtil.isBlank(rawPwd)) {
            // 默认密码策略：有身份证取后6位，无身份证默认为 123456
            rawPwd = "123456";
            if (StrUtil.isNotBlank(user.getIdCard()) && user.getIdCard().length() >= 6) {
                rawPwd = StrUtil.subSuf(user.getIdCard(), user.getIdCard().length() - 6);
            }
        }
        // 🟢 核心安全：使用 BCrypt 进行哈希加密，数据库不存明文
        user.setPassword(BCrypt.hashpw(rawPwd, BCrypt.gensalt()));
        
        // 🟢 标记为初始密码 (登录后会强制要求修改)
        user.setIsInitialPwd(1);
        
        // --- 5. 设置默认状态 ---
        if (user.getStatus() == null) user.setStatus("0"); // 0-正常
        if (user.getEntryDate() == null) user.setEntryDate(LocalDate.now()); // 默认今日入校
        
        // 执行数据库保存
        boolean result = super.save(user);
        
        // --- 6. 自动分配角色 ---
        if (result) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(user.getId());
            // 简单映射策略：0(学生)->角色ID:5, 1(教工)->角色ID:6
            ur.setRoleId(user.getUserCategory() == 0 ? 5L : 6L);
            userRoleMapper.insert(ur);
        }
        
        return result;
    }
    
    // =================================================================================
    // 2. 修改用户
    // =================================================================================
    
    /**
     * 更新用户信息
     * 注意：此方法不直接处理"改密"业务，但会处理密码字段的加密逻辑
     *
     * @param user 修改后的用户对象
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysOrdinaryUser user) {
        // 如果前端传了新密码，需要加密后存入
        if (StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        } else {
            // 如果没传密码，置为 null，MyBatisPlus 更新时会忽略此字段，保持原密码不变
            user.setPassword(null);
        }
        
        // 如果修改了身份证，需要重新校验并重新解析生日等信息
        if (StrUtil.isNotBlank(user.getIdCard())) {
            if (!IdcardUtil.isValidCard(user.getIdCard())) {
                throw new ServiceException("身份证格式错误");
            }
            parseIdCardInfo(user); // 重新解析
        }
        return super.updateById(user);
    }
    
    // =================================================================================
    // 3. 密码管理
    // =================================================================================
    
    /**
     * 管理员强制重置密码
     *
     * @param userId      目标用户 ID
     * @param newPassword 新密码 (明文)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, String newPassword) {
        if (StrUtil.isBlank(newPassword) || newPassword.length() < 6) {
            throw new ServiceException("密码长度不能少于 6 位");
        }
        
        // 加密
        String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        
        // 更新数据库，并将其标记为"初始密码"，迫使用户下次登录修改
        this.lambdaUpdate()
                .eq(SysOrdinaryUser::getId, userId) // 使用 Getter 定位 ID 字段
                .set(SysOrdinaryUser::getPassword, hash) // 使用 Getter 定位 Password 字段
                .set(SysOrdinaryUser::getIsInitialPwd, 1) // 必须用 Getter (getIsInitialPwd)，不能用 Setter
                .update();
    }
    
    /**
     * 用户自行修改密码
     *
     * @param userId      用户 ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        SysOrdinaryUser user = this.getById(userId);
        if (user == null) throw new ServiceException("用户不存在");
        
        // 1. 校验旧密码是否正确
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new ServiceException("原密码错误");
        }
        // 2. 校验新密码长度
        if (newPassword.length() < 6) throw new ServiceException("新密码长度不能少于6位");
        
        // 3. 加密新密码
        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        // 4. 解除初始密码状态 (说明用户已经改过了)
        user.setIsInitialPwd(0);
        
        this.updateById(user);
    }
    
    // =================================================================================
    // 私有辅助方法 (必须去掉 static 关键字，否则无法调用 Bean)
    // =================================================================================
    
    /**
     * 🟢 生成唯一账号 (调用 UsernameGenerator 组件)
     * * 逻辑：
     * 1. 校验生成所需的必要参数 (校区、学院/部门等)
     * 2. 根据用户类型 (学生/教工) 调用不同的生成规则
     * 3. 返回生成的唯一 ID
     */
    private String generateUniqueAccount(SysOrdinaryUser user) {
        // 默认取当前年份
        Integer year = user.getEnrollmentYear();
        if (year == null) year = Year.now().getValue();
        
        // 必填项检查
        if (user.getCampusId() == null) {
            throw new ServiceException("生成账号失败：必须选择 [校区]");
        }
        
        // === 分支 A: 学生生成逻辑 ===
        if (user.getUserCategory() == 0) {
            // 智能补全：如果只传了 MajorId，自动查出 CollegeId 和 培养层次
            if (user.getCollegeId() == null && user.getMajorId() != null) {
                // majorService 是注入的实例 Bean，所以此方法不能是 static
                SysMajor major = majorService.getById(user.getMajorId());
                if (major != null) {
                    user.setCollegeId(major.getCollegeId());
                    user.setEduLevel(convertLevelToCode(major.getLevel()));
                }
            }
            
            // 再次校验完整性
            if (user.getCollegeId() == null) throw new ServiceException("生成学号失败：必须选择 [学院] 或 [专业]");
            if (user.getMajorId() == null) throw new ServiceException("生成学号失败：必须选择 [专业]");
            
            // 调用 ID 生成器组件
            return usernameGenerator.generateStudentAccount(
                    year,
                    user.getEduLevel(),
                    user.getCollegeId(),
                    user.getCampusId(),
                    user.getMajorId()
            );
        }
        // === 分支 B: 教工生成逻辑 ===
        else {
            if (user.getDeptId() == null) {
                throw new ServiceException("生成工号失败：必须选择 [所属部门]");
            }
            
            // 默认合同年限 1 年
            Integer contractYear = user.getContractYear();
            if (contractYear == null) contractYear = 1;
            
            return usernameGenerator.generateStaffAccount(
                    year,
                    contractYear,
                    user.getCampusId(),
                    user.getDeptId()
            );
        }
    }
    
    /**
     * 🟢 从身份证解析元数据
     * 自动填充：出生日期、籍贯、性别
     */
    private void parseIdCardInfo(SysOrdinaryUser user) {
        String idCard = user.getIdCard();
        // 必须有效才解析
        if (StrUtil.isBlank(idCard) || !IdcardUtil.isValidCard(idCard)) return;
        
        try {
            // 1. 解析生日 (yyyy MM dd)
            String birth = IdcardUtil.getBirthByIdCard(idCard);
            user.setBirthDate(LocalDate.parse(birth, DateTimeFormatter.ofPattern("yyyyMMdd")));
            
            // 2. 解析籍贯 (仅在为空时填充，防止覆盖用户手填的详细地址)
            if (StrUtil.isBlank(user.getHometown())) {
                user.setHometown(IdcardUtil.getProvinceByIdCard(idCard));
            }
            
            // 3. 解析性别 (1男 0女)
            int genderVal = IdcardUtil.getGenderByIdCard(idCard);
            user.setGender(String.valueOf(genderVal));
            
        } catch (Exception ignored) {
            // 解析失败不阻断流程，仅打印日志
            log.warn("身份证解析失败: {}", idCard);
        }
    }
    
    /**
     * 辅助工具：将中文学历转为代码
     * 本科->B, 专科->Z, 专升本->ZB, 研究生->Y, 博士->D
     */
    private String convertLevelToCode(String levelName) {
        if (levelName == null) return "B"; // 默认本科
        if (levelName.contains("专科")) return "Z";
        if (levelName.contains("专升本")) return "ZB";
        if (levelName.contains("研究生") || levelName.contains("硕士")) return "Y";
        if (levelName.contains("博士")) return "D";
        return "B";
    }
    
    /**
     * 检查账号是否重复 (数据库查询)
     */
    private boolean checkUsernameExists(String username) {
        return this.lambdaQuery().eq(SysOrdinaryUser::getUsername, username).exists();
    }
}