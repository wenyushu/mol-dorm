package com.mol.server.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.idev.excel.FastExcel;
import cn.idev.excel.read.listener.PageReadListener;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.entity.*;
import com.mol.common.core.entity.SysUserRole;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.component.UsernameGenerator;
import com.mol.server.entity.*;
import com.mol.server.mapper.SysAdminUserMapper;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import com.mol.server.mapper.SysUserRoleMapper;
import com.mol.server.service.*;
import com.mol.server.vo.StudentExportVO;
import com.mol.server.vo.StudentImportVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 普通用户(学生/教职工) 核心业务实现类
 * <p>
 * 核心职责：
 * 1. 用户档案的全生命周期管理（新增、修改、重置密码）。
 * 2. 批量数据导入/导出（基于 FastExcel，支持名称与ID互转）。
 * 3. 账号安全策略（自动生成学号、BCrypt加密、初始密码强制修改）。
 * </p>
 *
 * @author mol
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysOrdinaryUserServiceImpl extends ServiceImpl<SysOrdinaryUserMapper, SysOrdinaryUser> implements SysOrdinaryUserService {
    
    // --- 核心组件注入 ---
    
    // ID 生成器 (按规则生成学号/工号)
    private final UsernameGenerator usernameGenerator;
    // 角色关联 Mapper (分配默认角色)
    private final SysUserRoleMapper userRoleMapper;
    
    // --- 基础数据服务注入 (用于 Excel 导入导出的 "名称 <-> ID" 字典翻译) ---
    private final SysMajorService majorService;     // 专业
    private final SysCollegeService collegeService; // 学院
    private final SysClassService classService;     // 班级
    private final SysCampusService campusService;   // 校区
    private final SysDeptService deptService;       // 部门 (行政教工)
    
    // 🔒 账号格式防火墙正则
    // 规则：仅允许 10 到 30 位的数字和大写字母组合 (拒绝特殊符号和中文)
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^[0-9A-Z]{10,30}$");
    
    // 新增：注入管理员 Mapper，用于跨表查重
    private final SysAdminUserMapper adminUserMapper;
    
    
    // =================================================================================
    // 1. 新增用户 (单条录入 - 防呆版)
    // =================================================================================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveUser(SysOrdinaryUser user) {
        
        // --- A. 基础参数校验 ---
        if (user.getUserCategory() == null) {
            throw new ServiceException("非法请求：必须指定用户类别 (0-学生, 1-教工)");
        }
        
        // 🛡️ 兜底策略：防止数据库必填字段报错 (Anti-Null)
        if (StrUtil.isBlank(user.getEmergencyContact())) user.setEmergencyContact("未知");
        if (StrUtil.isBlank(user.getEmergencyPhone())) user.setEmergencyPhone("无");
        if (StrUtil.isBlank(user.getEmergencyRelation())) user.setEmergencyRelation("亲属");
        if (StrUtil.isBlank(user.getEthnicity())) user.setEthnicity("汉族");
        if (StrUtil.isBlank(user.getHometown())) user.setHometown("未知");
        
        // 🛡️ 年份校验 (2000年 ~ 明年)
        Integer year = user.getEntryYear();
        if (year == null) year = user.getEnrollmentYear();
        if (year != null) {
            int currentYear = Year.now().getValue();
            if (year < 2000 || year > currentYear + 1) {
                throw new ServiceException("年份异常：只能录入 2000 年至今的数据");
            }
        }
        
        // 🛡️ 身份证算法校验
        if (StrUtil.isNotBlank(user.getIdCard()) && !IdcardUtil.isValidCard(user.getIdCard())) {
            throw new ServiceException("身份证号码无效，请核对后重新输入");
        }
        
        // --- B. 智能填充 ---
        parseIdCardInfo(user);
        
        // --- C. 账号生成与审查（🛡️ 防刁民） ---
        if (StrUtil.isBlank(user.getUsername())) {
            // C1. 自动生成
            String generatedAccount = generateUniqueAccount(user);
            // 防火墙二重校验
            if (!ACCOUNT_PATTERN.matcher(generatedAccount).matches()) {
                log.error("账号生成异常: {}", generatedAccount);
                throw new ServiceException("系统生成账号格式异常，请联系管理员");
            }
            // [新增] 即使是生成的账号，也稍微查一下管理员表 (万一有管理员故意取了个像学号的名字)
            if (checkAdminUsernameExists(generatedAccount)) {
                throw new ServiceException("生成失败：账号[" + generatedAccount + "]与现有管理员冲突");
            }
            user.setUsername(generatedAccount);
        } else {
            // C2. 手动录入
            if (!ACCOUNT_PATTERN.matcher(user.getUsername()).matches()) {
                throw new ServiceException("账号格式错误！仅允许 10-30 位数字和大写字母组合");
            }
            // 1. 查自己表
            if (checkUsernameExists(user.getUsername())) {
                throw new ServiceException("账号已存在: " + user.getUsername());
            }
            // 2. 查管理员表 (防刁民核心逻辑)
            if (checkAdminUsernameExists(user.getUsername())) {
                throw new ServiceException("新增失败：该账号[" + user.getUsername() + "]已被【管理员】占用，请更换！");
            }
        }
        
        // --- D. 🛡️ 密码处理 ---
        String rawPwd = user.getPassword();
        if (StrUtil.isBlank(rawPwd)) {
            // 默认密码：身份证后 6 位，或 123456
            rawPwd = "123456";
            if (StrUtil.isNotBlank(user.getIdCard()) && user.getIdCard().length() >= 6) {
                rawPwd = StrUtil.subSuf(user.getIdCard(), user.getIdCard().length() - 6);
            }
        }
        user.setPassword(BCrypt.hashpw(rawPwd, BCrypt.gensalt()));
        user.setIsInitialPwd(1); // 强制改密标记
        
        // --- E. 落库与赋权 ---
        if (user.getStatus() == null) user.setStatus("0");
        if (user.getEntryDate() == null) user.setEntryDate(LocalDate.now());
        
        boolean result = super.save(user);
        if (result) {
            assignDefaultRole(user.getId(), user.getUserCategory());
        }
        return result;
    }
    
    // =================================================================================
    // 2. Excel 批量导入 (FastExcel - 读)
    // =================================================================================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importStudent(InputStream inputStream) {
        log.info("导入开始：正在加载基础数据字典...");
        
        // 🛡️ 1. 字典预加载 (Name -> ID)
        // 避免在循环中查库，极大提升性能
        Map<String, Long> campusMap = campusService.list().stream()
                .collect(Collectors.toMap(SysCampus::getCampusName, SysCampus::getId, (v1, v2) -> v1));
        
        Map<String, Long> collegeMap = collegeService.list().stream()
                .collect(Collectors.toMap(SysCollege::getName, SysCollege::getId, (v1, v2) -> v1));
        
        Map<String, Long> majorMap = majorService.list().stream()
                .collect(Collectors.toMap(SysMajor::getName, SysMajor::getId, (v1, v2) -> v1));
        
        Map<String, Long> classMap = classService.list().stream()
                .collect(Collectors.toMap(SysClass::getClassName, SysClass::getId, (v1, v2) -> v1));
        
        Map<String, Long> deptMap = deptService.list().stream()
                .collect(Collectors.toMap(SysDept::getName, SysDept::getId, (v1, v2) -> v1));
        
        // 🛡️ 预查已有学号，(学生表)，防止唯一键冲突
        Set<String> existUsernames = this.list().stream()
                .map(SysOrdinaryUser::getUsername)
                .collect(Collectors.toSet());
        
        // 🛡️  [新增] 预查管理员账号 (一次性查出，避免在循环里查库)
        Set<String> existAdminUsernames = adminUserMapper.selectList(null).stream()
                .map(SysAdminUser::getUsername) // 假设 Admin 实体也有 getUsername
                .collect(Collectors.toSet());
        
        log.info("字典加载完成，开始解析...");
        
        //  2. 流式读取与转换
        FastExcel.read(inputStream, StudentImportVO.class, new PageReadListener<StudentImportVO>(dataList -> {
            List<SysOrdinaryUser> saveList = new ArrayList<>();
            List<SysUserRole> roleList = new ArrayList<>();
            
            for (StudentImportVO vo : dataList) {
                // 1. 跳过空行
                if (StrUtil.isBlank(vo.getUsername())) continue;
                
                // 2. 查重 (数据库已有的 + Excel前面行已经出现过的)
                if (existUsernames.contains(vo.getUsername())) {
                    log.warn("导入跳过：账号[{}]已存在 (数据库或Excel重复)", vo.getUsername());
                    continue;
                }
                
                // 3. 查管理员表冲突
                if (existAdminUsernames.contains(vo.getUsername())) {
                    log.warn("导入跳过：账号[{}]与管理员冲突", vo.getUsername());
                    continue;
                }
                
                SysOrdinaryUser user = new SysOrdinaryUser();
                
                // 基础拷贝
                user.setUsername(vo.getUsername());
                user.setRealName(vo.getRealName());
                user.setPhone(vo.getPhone());
                user.setIdCard(vo.getIdCard());
                // 空值防御
                user.setEmergencyContact(StrUtil.blankToDefault(vo.getEmergencyContact(), "未知"));
                user.setEmergencyPhone(StrUtil.blankToDefault(vo.getEmergencyPhone(), "无"));
                user.setEmergencyRelation(StrUtil.blankToDefault(vo.getEmergencyRelation(), "亲属"));
                user.setHometown(StrUtil.blankToDefault(vo.getHometown(), "未知"));
                user.setEthnicity(StrUtil.blankToDefault(vo.getEthnicity(), "汉族"));
                
                // 逻辑转换
                user.setGender("女".equals(vo.getGenderStr()) ? "0" : "1");
                
                String rawPwd = "123456";
                if (StrUtil.isNotBlank(vo.getIdCard()) && vo.getIdCard().length() >= 6) {
                    rawPwd = StrUtil.subSuf(vo.getIdCard(), vo.getIdCard().length() - 6);
                }
                user.setPassword(BCrypt.hashpw(rawPwd, BCrypt.gensalt()));
                user.setIsInitialPwd(1);
                user.setStatus("0");
                user.setEntryDate(LocalDate.now());
                
                // 核心：名称转ID
                user.setCampusId(campusMap.getOrDefault(vo.getCampusName(), 0L));
                
                boolean isStudent = StrUtil.isNotBlank(vo.getClassName());
                user.setUserCategory(isStudent ? 0 : 1);
                
                if (isStudent) {
                    user.setCollegeId(collegeMap.getOrDefault(vo.getCollegeName(), 0L));
                    user.setMajorId(majorMap.getOrDefault(vo.getMajorName(), 0L));
                    user.setClassId(classMap.getOrDefault(vo.getClassName(), 0L));
                    // 尝试从学号解析入学年份
                    if (user.getUsername().length() >= 4 && StrUtil.isNumeric(user.getUsername().substring(0, 4))) {
                        user.setEnrollmentYear(Integer.parseInt(user.getUsername().substring(0, 4)));
                    }
                } else {
                    if (StrUtil.isNotBlank(vo.getDeptName())) {
                        user.setDeptId(deptMap.getOrDefault(vo.getDeptName(), 0L));
                    } else if (StrUtil.isNotBlank(vo.getCollegeName())) {
                        user.setCollegeId(collegeMap.getOrDefault(vo.getCollegeName(), 0L));
                    }
                    user.setContractYear(3);
                }

                // 4. 解析身份证信息
                parseIdCardInfo(user);
                
                // 5. 加入待保存列表
                saveList.add(user);
                
                // 注意：在循环末尾把新加入的也放进 Set，防止 Excel 内部自己重复
                // 将当前处理完的学号加入 Set。
                // 作用：如果 Excel 下一行又是这个学号，上面的第 2 步判断就会拦截住它。
                existUsernames.add(user.getUsername());
            }
            
            // 批量落库
            if (CollUtil.isNotEmpty(saveList)) {
                this.saveBatch(saveList);
                // 构造角色关联
                for (SysOrdinaryUser u : saveList) {
                    SysUserRole ur = new SysUserRole();
                    ur.setUserId(u.getId());
                    // 使用 RoleConstants 常量 (8L 和 6L)
                    ur.setRoleId(u.getUserCategory() == 0 ? RoleConstants.STUDENT_ID : RoleConstants.COLLEGE_TEACHER_ID);
                    roleList.add(ur);
                }
                // 批量插角色
                for (SysUserRole ur : roleList) {
                    userRoleMapper.insert(ur);
                }
            }
        })).sheet().doRead();
    }
    
    // =================================================================================
    // 3. Excel 批量导出 (FastExcel - 写)
    // =================================================================================
    
    @Override
    public void exportData(HttpServletResponse response, SysOrdinaryUser queryParams) {
        try {
            //  1. 查询源数据
            // 根据 queryParams 进行筛选，复用 MyBatisPlus 逻辑
            List<SysOrdinaryUser> userList = this.lambdaQuery()
                    .like(StrUtil.isNotBlank(queryParams.getRealName()), SysOrdinaryUser::getRealName, queryParams.getRealName())
                    .eq(StrUtil.isNotBlank(queryParams.getUsername()), SysOrdinaryUser::getUsername, queryParams.getUsername())
                    .eq(queryParams.getUserCategory() != null, SysOrdinaryUser::getUserCategory, queryParams.getUserCategory())
                    .orderByDesc(SysOrdinaryUser::getCreateTime)
                    .list();
            
            // 防呆：无数据时直接报错，避免生成空 Excel 让用户困惑
            if (CollUtil.isEmpty(userList)) {
                throw new ServiceException("当前筛选条件下无数据，无法导出");
            }
            
            log.info("导出准备：加载反向字典...");
            
            //  2. 字典预加载 (ID -> Name)
            Map<Long, String> campusMap = campusService.list().stream()
                    .collect(Collectors.toMap(SysCampus::getId, SysCampus::getCampusName));
            
            Map<Long, String> collegeMap = collegeService.list().stream()
                    .collect(Collectors.toMap(SysCollege::getId, SysCollege::getName));
            
            Map<Long, String> majorMap = majorService.list().stream()
                    .collect(Collectors.toMap(SysMajor::getId, SysMajor::getName));
            
            Map<Long, String> classMap = classService.list().stream()
                    .collect(Collectors.toMap(SysClass::getId, SysClass::getClassName));
            
            Map<Long, String> deptMap = deptService.list().stream()
                    .collect(Collectors.toMap(SysDept::getId, SysDept::getName));
            
            //  3. 实体转换 (Entity -> ExportVO)
            List<StudentExportVO> exportList = userList.stream().map(user -> {
                StudentExportVO vo = new StudentExportVO();
                
                // 基础拷贝
                vo.setUsername(user.getUsername());
                vo.setRealName(user.getRealName());
                vo.setPhone(user.getPhone());
                vo.setIdCard(user.getIdCard());
                vo.setEntryYear(user.getEntryYear() != null ? user.getEntryYear() : user.getEnrollmentYear());
                
                // 逻辑转换
                vo.setGender("1".equals(user.getGender()) ? "男" : "女");
                vo.setUserCategory(user.getUserCategory() == 0 ? "学生" : "教职工");
                vo.setStatus("0".equals(user.getStatus()) ? "正常" : "停用");
                
                // ID 转名称 (使用 getOrDefault 防止 ID 无效导致空指针)
                vo.setCampusName(campusMap.getOrDefault(user.getCampusId(), ""));
                vo.setCollegeName(collegeMap.getOrDefault(user.getCollegeId(), ""));
                vo.setMajorName(majorMap.getOrDefault(user.getMajorId(), ""));
                vo.setClassName(classMap.getOrDefault(user.getClassId(), ""));
                vo.setDeptName(deptMap.getOrDefault(user.getDeptId(), ""));
                
                return vo;
            }).collect(Collectors.toList());
            
            // 4. 写出响应流
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("人员数据表_" + System.currentTimeMillis(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            
            FastExcel.write(response.getOutputStream(), StudentExportVO.class)
                    .sheet("人员名单")
                    .doWrite(exportList);
            
        } catch (IOException e) {
            log.error("IO异常", e);
            throw new ServiceException("文件生成失败");
        }
    }
    
    // =================================================================================
    // 4. 修改与密码管理
    // =================================================================================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysOrdinaryUser user) {
        // 🛡️ 密码加密：只有当 password 字段不为空时才加密更新，否则忽略
        if (StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        } else {
            user.setPassword(null);
        }
        
        // 🛡️ 身份证校验
        if (StrUtil.isNotBlank(user.getIdCard())) {
            if (!IdcardUtil.isValidCard(user.getIdCard())) {
                throw new ServiceException("身份证格式错误");
            }
            parseIdCardInfo(user);
        }
        return super.updateById(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, String newPassword) {
        if (StrUtil.isBlank(newPassword) || newPassword.length() < 6) {
            throw new ServiceException("密码长度不能少于 6 位");
        }
        String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        
        // 强制重置标记
        this.lambdaUpdate()
                .eq(SysOrdinaryUser::getId, userId)
                .set(SysOrdinaryUser::getPassword, hash)
                .set(SysOrdinaryUser::getIsInitialPwd, 1)
                .update();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        SysOrdinaryUser user = this.getById(userId);
        if (user == null) throw new ServiceException("用户不存在");
        
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new ServiceException("原密码错误");
        }
        if (newPassword.length() < 6) throw new ServiceException("新密码长度不能少于 6 位");
        
        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        user.setIsInitialPwd(0); // 解除初始密码状态
        this.updateById(user);
    }
    
    
    // =================================================================================
    // 5. 详情查询与隐私权限控制 (防刁民核心)
    // =================================================================================
    
    @Override
    public SysOrdinaryUser getUserDetail(Long targetUserId) {
        // 1. 获取当前登录者 ID (通过 Sa-Token)
        Long loginId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong();
        
        // 2. 查询目标用户信息
        SysOrdinaryUser targetUser = this.getById(targetUserId);
        if (targetUser == null) {
            throw new ServiceException("目标用户不存在");
        }
        
        // 3. 判定权限级别
        // A. 本人查看
        boolean isSelf = loginId.equals(targetUserId);
        
        // B. 管理员查看 (由 DataAuditAspect 切面已经预设了一部分，这里可以二次确认)
        boolean isAdmin = cn.dev33.satoken.stp.StpUtil.hasRole(RoleConstants.SUPER_ADMIN)
                || cn.dev33.satoken.stp.StpUtil.hasRole(RoleConstants.DORM_MANAGER);
        
        // C. 室友判定：两人都在住且 dormId 相同
        SysOrdinaryUser currentUser = this.getById(loginId);
        boolean isRoommate = false;
        if (currentUser != null && currentUser.getDormId() != null && targetUser.getDormId() != null) {
            isRoommate = currentUser.getDormId().equals(targetUser.getDormId());
        }
        
        // 4. 设置脱敏上下文标识
        if (isSelf || isAdmin || isRoommate) {
            // 符合“上帝模式”或“室友互助模式”，Jackson 序列化时将显示明文
            com.mol.common.core.context.SecurityContext.setCanViewFullDetail(true);
        } else {
            // 陌生人查看，强制开启脱敏
            com.mol.common.core.context.SecurityContext.setCanViewFullDetail(false);
        }
        
        // 5. 特殊隔离：校外居住地址 (outside_address) 仅限管理员可见
        // 即便是室友，也不能看对方校外住哪，防止顺藤摸瓜
        if (!isAdmin && !isSelf && StrUtil.isNotBlank(targetUser.getOutsideAddress())) {
            // 策略：如果是室友但不是管理员，直接在返回前对该字段脱敏
            targetUser.setOutsideAddress(StrUtil.hide(targetUser.getOutsideAddress(), 3, targetUser.getOutsideAddress().length()));
        }
        
        return targetUser;
    }
    
    
    // =================================================================================
    // 私有辅助方法
    // =================================================================================
    
    /**
     * 🛡️ 分配默认角色 (修正版)
     * @param category 0-学生, 1-教工
     */
    private void assignDefaultRole(Long userId, Integer category) {
        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        
        // 修正：直接使用参数 category 判断
        ur.setRoleId(category == 0 ? RoleConstants.STUDENT_ID : RoleConstants.COLLEGE_TEACHER_ID);
        
        userRoleMapper.insert(ur);
    }
    
    /**
     * 🛡️ 生成唯一账号
     */
    private String generateUniqueAccount(SysOrdinaryUser user) {
        Integer year = user.getEnrollmentYear();
        if (year == null) year = Year.now().getValue();
        
        if (user.getCampusId() == null) {
            throw new ServiceException("生成账号失败：必须选择 [校区]");
        }
        
        if (user.getUserCategory() == 0) {
            if (user.getCollegeId() == null && user.getMajorId() != null) {
                SysMajor major = majorService.getById(user.getMajorId());
                if (major != null) {
                    user.setCollegeId(major.getCollegeId());
                    user.setEduLevel(convertLevelToCode(major.getLevel()));
                }
            }
            if (user.getCollegeId() == null) throw new ServiceException("生成学号失败：必须选择 [学院] 或 [专业]");
            if (user.getMajorId() == null) throw new ServiceException("生成学号失败：必须选择 [专业]");
            
            return usernameGenerator.generateStudentAccount(
                    year,
                    user.getEduLevel(),
                    user.getCollegeId(),
                    user.getCampusId(),
                    user.getMajorId()
            );
        } else {
            if (user.getDeptId() == null) {
                throw new ServiceException("生成工号失败：必须选择 [所属部门]");
            }
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
     * 🛡️ 身份证元数据解析
     */
    private void parseIdCardInfo(SysOrdinaryUser user) {
        String idCard = user.getIdCard();
        if (StrUtil.isBlank(idCard) || !IdcardUtil.isValidCard(idCard)) return;
        try {
            String birth = IdcardUtil.getBirthByIdCard(idCard);
            user.setBirthDate(LocalDate.parse(birth, DateTimeFormatter.ofPattern("yyyyMMdd")));
            
            if (StrUtil.isBlank(user.getHometown())) {
                user.setHometown(IdcardUtil.getProvinceByIdCard(idCard));
            }
            
            int genderVal = IdcardUtil.getGenderByIdCard(idCard);
            user.setGender(String.valueOf(genderVal));
        } catch (Exception ignored) {
            log.warn("身份证解析失败: {}", idCard);
        }
    }
    
    private String convertLevelToCode(String levelName) {
        if (levelName == null) return "B";
        if (levelName.contains("专科")) return "Z";
        if (levelName.contains("专升本")) return "ZB";
        if (levelName.contains("研究生") || levelName.contains("硕士")) return "Y";
        if (levelName.contains("博士")) return "D";
        return "B";
    }
    
    private boolean checkUsernameExists(String username) {
        return this.lambdaQuery().eq(SysOrdinaryUser::getUsername, username).exists();
    }
    
    /**
     *  检查管理员表是否存在该账号
     */
    private boolean checkAdminUsernameExists(String username) {
        return adminUserMapper.exists(com.baomidou.mybatisplus.core.toolkit.Wrappers.<SysAdminUser>lambdaQuery()
                .eq(SysAdminUser::getUsername, username));
    }
}