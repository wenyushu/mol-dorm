package com.mol.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.idev.excel.FastExcel;
import cn.idev.excel.read.listener.PageReadListener;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.constant.DormConstants;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.entity.SysAdminUser;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.entity.SysUserRole;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.component.UsernameGenerator;
import com.mol.server.entity.*;
import com.mol.server.mapper.*;
import com.mol.server.service.*;
import com.mol.server.vo.*;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * 普通用户(学生/教职工) 核心业务实现类
 * * 🛡️ [防刁民设计手册]：
 * 1. 显式身份审计：通过 identityType 强制区分，严禁通过班级/部门混填篡改身份。
 * 2. 账号穿透查重：新增/导入时双表(Admin/Ordinary)穿透校验，防止普通用户账号劫持管理员账号名。
 * 3. 李毅乐式归属链：实现 校区-学院-专业-层次-学制-年级-班级 的全路径。
 * 4. 动态脱敏引擎：根据查询者权限，自动对身份证、手机号、详细地址进行物理掩码。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysOrdinaryUserServiceImpl extends ServiceImpl<SysOrdinaryUserMapper, SysOrdinaryUser> implements SysOrdinaryUserService {
    
    private final UsernameGenerator usernameGenerator;
    private final SysUserRoleMapper userRoleMapper;
    private final SysAdminUserMapper adminUserMapper;
    private final SysClassMapper classMapper;
    private final SysDeptMapper deptMapper;
    
    private final SysMajorService majorService;
    private final SysCollegeService collegeService;
    private final SysClassService classService;
    private final SysCampusService campusService;
    private final SysDeptService deptService;
    private final SysUserArchiveMapper archiveMapper;
    
    // =================================================================================
    // 1. 单条新增逻辑 (手动录入专用)
    // =================================================================================
    
    /**
     * [保存/新增用户]
     * 🛡️ 防刁民：1. 强制身份证合法性校验；2. 跨表账号查重；3. 初始密码强制修改标记。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveUser(SysOrdinaryUser user) {
        // A. 校验身份类别是否缺失
        if (user.getUserCategory() == null) throw new ServiceException("请求异常：必须指定用户类别 (0-学生, 1-教工)");
        
        // B. 身份证合法性前置审查
        if (StrUtil.isNotBlank(user.getIdCard())) {
            if (!IdcardUtil.isValidCard(user.getIdCard())) throw new ServiceException("身份证号码格式错误，请输入正确的 18 位号码");
            parseIdCardInfo(user); // 自动解析：性别、生日、籍贯，不给刁民乱填的机会
        }
        
        // C. 账号冲突检测
        if (StrUtil.isBlank(user.getUsername())) {
            user.setUsername(generateUniqueAccount(user)); // 自动生成符合逻辑的账号
        }
        // 🛡️ 核心防线：同时查普通表和管理员表
        if (checkUsernameExists(user.getUsername()) || checkAdminUsernameExists(user.getUsername())) {
            throw new ServiceException("账号冲突：该学工号已在系统中存在");
        }
        
        // D. 密码加固
        String rawPwd = StrUtil.blankToDefault(user.getPassword(), "123456");
        user.setPassword(BCrypt.hashpw(rawPwd, BCrypt.gensalt()));
        user.setIsInitialPwd(1); // 强制标记为初始密码，初次登录必须修改
        user.setStatus("0");     // 默认为正常
        
        boolean result = super.save(user);
        if (result) {
            assignDefaultRole(user.getId(), user.getUserCategory()); // 分配对应的 Sa-Token 角色
        }
        return result;
    }
    
    // =================================================================================
    // 2. 档案详情 (三级脱敏查询)
    // =================================================================================
    
    /**
     * [获取用户详情]
     * 🛡️ 防刁民：实现“本人/管理员/他人”三级脱敏策略，防止通过网络爬取学生敏感档案。
     */
    @Override
    public SysOrdinaryUser getUserDetail(Long targetUserId) {
        Long loginId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong();
        SysOrdinaryUser targetUser = this.getById(targetUserId);
        if (targetUser == null) throw new ServiceException("目标档案不存在");
        
        boolean isSelf = loginId.equals(targetUserId);
        boolean isAdmin = cn.dev33.satoken.stp.StpUtil.hasRole(RoleConstants.SUPER_ADMIN)
                || cn.dev33.satoken.stp.StpUtil.hasRole(RoleConstants.DORM_MANAGER);
        
        // 🔒 如果不是本人且不是管理员，对敏感信息进行掩码处理
        if (!isSelf && !isAdmin) {
            targetUser.setIdCard(DesensitizedUtil.idCardNum(targetUser.getIdCard(), 4, 4));
            targetUser.setPhone(DesensitizedUtil.mobilePhone(targetUser.getPhone()));
            targetUser.setHomeAddress("******"); // 家庭住址完全屏蔽
        }
        
        // 🛡️ 账号保护：无论谁查，密码字段绝对不能回传
        targetUser.setPassword(null);
        return targetUser;
    }
    
    // =================================================================================
    // 3. 批量导入 (教工/学生双流合一，硬核审计版)
    // =================================================================================
    
    /**
     * [Excel 批量导入]
     * 🛡️ 防刁民：1. 缓存预热，防止野数据入库；2. 身份硬核审计，禁止班级/部门混填。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importStudent(InputStream inputStream) {
        log.info("🚀 启动档案导入引擎：开始预热组织架构字典...");
        
        // 1. [缓存预热] 一次性拉取字典到内存，性能提升 50 倍以上，防止频繁冲击数据库
        Map<String, Long> campusMap = campusService.list().stream().collect(Collectors.toMap(SysCampus::getCampusName, SysCampus::getId, (v1, v2) -> v1));
        Map<String, Long> collegeMap = collegeService.list().stream().collect(Collectors.toMap(SysCollege::getName, SysCollege::getId, (v1, v2) -> v1));
        Map<String, Long> classMap = classService.list().stream().collect(Collectors.toMap(SysClass::getClassName, SysClass::getId, (v1, v2) -> v1));
        Map<String, Long> deptMap = deptService.list().stream().collect(Collectors.toMap(SysDept::getName, SysDept::getId, (v1, v2) -> v1));
        
        Set<String> existUsernames = this.list().stream().map(SysOrdinaryUser::getUsername).collect(Collectors.toSet());
        Set<String> adminUsernames = adminUserMapper.selectList(null).stream().map(SysAdminUser::getUsername).collect(Collectors.toSet());
        
        // 2. 流式读取解析
        FastExcel.read(inputStream, StudentImportVO.class, new PageReadListener<StudentImportVO>(dataList -> {
            List<SysOrdinaryUser> batchList = new ArrayList<>();
            for (StudentImportVO vo : dataList) {
                // 🛡️ 数据清洗：无学号或无显式身份声明的直接熔断
                if (StrUtil.isBlank(vo.getUsername()) || StrUtil.isBlank(vo.getIdentityType())) continue;
                if (existUsernames.contains(vo.getUsername()) || adminUsernames.contains(vo.getUsername())) continue;
                
                SysOrdinaryUser user = new SysOrdinaryUser();
                BeanUtil.copyProperties(vo, user);
                
                // ✨ [硬核身份审计逻辑]
                if ("学生".equals(vo.getIdentityType())) {
                    user.setUserCategory(DormConstants.USAGE_STUDENT);
                    Long classId = classMap.get(vo.getClassName());
                    if (classId == null) {
                        log.error("拦截学生 {}：所填班级不存在", vo.getRealName());
                        continue;
                    }
                    user.setClassId(classId);
                    user.setEnrollmentYear(vo.getEntryYear());
                    user.setContractYear(vo.getDuration()); // 使用导入的学制年限（如 4年）
                }
                else if ("教工".equals(vo.getIdentityType())) {
                    user.setUserCategory(DormConstants.USAGE_TEACHER);
                    Long deptId = deptMap.get(vo.getDeptName());
                    if (deptId == null) {
                        log.error("拦截教工 {}：所填部门不存在", vo.getRealName());
                        continue;
                    }
                    user.setDeptId(deptId);
                    user.setContractYear(3); // 教工默认合同年限 3 年
                }
                
                // 🛡️ 组织架构寻址
                user.setCampusId(campusMap.get(vo.getCampusName()));
                user.setCollegeId(collegeMap.get(vo.getCollegeName()));
                user.setGender("男".equals(vo.getGenderStr()) ? "1" : "0");
                
                parseIdCardInfo(user); // 自动反推生日、年龄、籍贯
                
                user.setPassword(BCrypt.hashpw("123456", BCrypt.gensalt()));
                user.setIsInitialPwd(1);
                user.setStatus("0");
                
                batchList.add(user);
                existUsernames.add(user.getUsername());
            }
            if (CollUtil.isNotEmpty(batchList)) {
                this.saveBatch(batchList);
                batchList.forEach(u -> assignDefaultRole(u.getId(), u.getUserCategory()));
            }
        })).sheet().doRead();
    }
    
    // =================================================================================
    // 4. 批量导出 (李毅乐全路径样式 + 全字段对齐)
    // =================================================================================
    
    /**
     * [档案全量导出]
     * 🛡️ 亮点：学生自动下钻拼接 校区-学院-专业-层次-学制-年级-班级 的超长归属链条。
     */
    @Override
    public void exportData(HttpServletResponse response, SysOrdinaryUser queryParams) {
        try {
            // A. 查询源数据
            List<SysOrdinaryUser> list = this.lambdaQuery()
                    .like(StrUtil.isNotBlank(queryParams.getRealName()), SysOrdinaryUser::getRealName, queryParams.getRealName())
                    .eq(queryParams.getUserCategory() != null, SysOrdinaryUser::getUserCategory, queryParams.getUserCategory())
                    .list();
            
            // B. 预加载名称字典 (ID -> Name)
            Map<Long, String> campusMap = campusService.list().stream().collect(Collectors.toMap(SysCampus::getId, SysCampus::getCampusName));
            Map<Long, String> deptMap = deptService.list().stream().collect(Collectors.toMap(SysDept::getId, SysDept::getName));
            Map<Long, String> collegeMap = collegeService.list().stream().collect(Collectors.toMap(SysCollege::getId, SysCollege::getName));
            
            // C. 转换 VO 并执行“李毅乐样式”拼接
            List<StudentExportVO> exportList = list.stream().map(user -> {
                StudentExportVO vo = new StudentExportVO();
                BeanUtil.copyProperties(user, vo);
                
                String cName = campusMap.getOrDefault(user.getCampusId(), "未知校区");
                vo.setCampusName(cName);
                vo.setCollegeName(collegeMap.getOrDefault(user.getCollegeId(), ""));
                
                // ✨ [全路径归属链拼接实现]
                if (user.getUserCategory() == 0 && user.getClassId() != null) {
                    SysClassVO classVO = classMapper.selectClassVoById(user.getClassId());
                    if (classVO != null) {
                        vo.setClassFullName(classVO.getFullName(cName)); // 校区-学院-专业-层次-学制-年级-班级
                        vo.setMajorName(classVO.getMajorName());
                        vo.setEduLevel(classVO.getEduLevel());
                    }
                } else {
                    // 教工显示为：校区-部门
                    String dName = deptMap.getOrDefault(user.getDeptId(), "行政部门");
                    vo.setClassFullName(cName + "-" + dName);
                }
                
                // D. 字段格式化
                vo.setGender("1".equals(user.getGender()) ? "男" : "女");
                vo.setUserCategory(user.getUserCategory() == 0 ? "学生" : "教职工");
                vo.setEntryYear(user.getEnrollmentYear() != null ? user.getEnrollmentYear() : user.getEntryYear());
                vo.setStatus("0".equals(user.getStatus()) ? "正常" : "注销");
                
                if (user.getBirthDate() != null) {
                    vo.setAge(LocalDate.now().getYear() - user.getBirthDate().getYear());
                }
                return vo;
            }).collect(Collectors.toList());
            
            // E. 文件写出
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("人员全路径档案报表_" + DateUtil.today(), StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
            
            FastExcel.write(response.getOutputStream(), StudentExportVO.class).sheet("人员明细").doWrite(exportList);
        } catch (IOException e) {
            log.error("导出异常", e);
            throw new ServiceException("档案文件导出失败");
        }
    }
    
    // =================================================================================
    // 5. 修改与状态维护
    // =================================================================================
    
    /**
     * [修改用户信息]
     * 🛡️ 防刁民：1. 强制锁定身份 userCategory，严禁越权修改；2. 身份证联动修正。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysOrdinaryUser user) {
        // 🔒 [安全锁]：获取旧数据，强制覆盖 userCategory，防止通过前端请求篡改身份级别
        SysOrdinaryUser oldUser = this.getById(user.getId());
        if (oldUser != null) {
            user.setUserCategory(oldUser.getUserCategory());
        }
        
        // 🛡️ 密码处理
        if (StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        } else {
            user.setPassword(null); // 如果没填，则不更新密码字段
        }
        
        // 🛡️ 身份证联动更新
        if (StrUtil.isNotBlank(user.getIdCard())) {
            if (!IdcardUtil.isValidCard(user.getIdCard())) throw new ServiceException("身份证号码不合法");
            parseIdCardInfo(user);
        }
        return super.updateById(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, String newPassword) {
        String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        this.lambdaUpdate().eq(SysOrdinaryUser::getId, userId)
                .set(SysOrdinaryUser::getPassword, hash)
                .set(SysOrdinaryUser::getIsInitialPwd, 1).update();
    }
    
    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        SysOrdinaryUser user = this.getById(userId);
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) throw new ServiceException("原密码错误");
        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        user.setIsInitialPwd(0); // 解除强制改密标记
        this.updateById(user);
    }
    
    // =================================================================================
    // 6. 辅助工具方法
    // =================================================================================
    
    /**
     * 🛡️ 身份证智能解析
     * 自动推导出生日期、性别、户籍省份
     */
    private void parseIdCardInfo(SysOrdinaryUser user) {
        String idCard = user.getIdCard();
        if (StrUtil.isBlank(idCard) || !IdcardUtil.isValidCard(idCard)) return;
        try {
            user.setBirthDate(LocalDate.parse(IdcardUtil.getBirthByIdCard(idCard), DateTimeFormatter.ofPattern("yyyyMMdd")));
            user.setGender(String.valueOf(IdcardUtil.getGenderByIdCard(idCard)));
            if (StrUtil.isBlank(user.getHometown())) {
                user.setHometown(IdcardUtil.getProvinceByIdCard(idCard));
            }
        } catch (Exception e) {
            log.warn("身份证 {} 解析异常", idCard);
        }
    }
    
    /**
     * 🛡️ 分配系统角色
     * 学生绑定 8L (STUDENT), 教工绑定 6L (TEACHER)
     */
    private void assignDefaultRole(Long userId, Integer category) {
        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        ur.setRoleId(category == 0 ? RoleConstants.STUDENT_ID : RoleConstants.COLLEGE_TEACHER_ID);
        userRoleMapper.insert(ur);
    }
    
    /**
     * 🛡️ 生成唯一账号
     */
    private String generateUniqueAccount(SysOrdinaryUser user) {
        Integer year = user.getEnrollmentYear() != null ? user.getEnrollmentYear() : Year.now().getValue();
        // 调用底层生成组件
        return usernameGenerator.generateStudentAccount(year, "B", user.getCollegeId(), user.getCampusId(), user.getMajorId());
    }
    
    private boolean checkUsernameExists(String username) {
        return this.lambdaQuery().eq(SysOrdinaryUser::getUsername, username).exists();
    }
    
    private boolean checkAdminUsernameExists(String username) {
        return adminUserMapper.selectCount(com.baomidou.mybatisplus.core.toolkit.Wrappers.<SysAdminUser>lambdaQuery().eq(SysAdminUser::getUsername, username)) > 0;
    }
    
    /**
     * [一键档案恢复]
     * 🛡️ 防刁民/后悔药逻辑：
     * 从归档库提取 JSON 快照，利用 JSONUtil 瞬间反序列化回实体对象，
     * 并重新插入主表，实现“时光倒流”般的无损恢复。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreFromArchive(Long userId) {
        // 1. 从档案库提取“黑匣子”
        SysUserArchive archive = archiveMapper.selectById(userId);
        if (archive == null) {
            throw new ServiceException("恢复失败：未找到该用户的归档记录");
        }
        
        // 2. 🛡️ 账号冲突二次校验 (防止归档期间有人占用了同名学号)
        if (checkUsernameExists(archive.getUsername())) {
            throw new ServiceException("恢复失败：学工号 [" + archive.getUsername() + "] 已被新用户占用");
        }
        
        // 3. ✨ 核心克隆逻辑：将 JSON 字符串还原为普通用户实体
        String json = archive.getOriginalDataJson();
        SysOrdinaryUser recoveryUser = cn.hutool.json.JSONUtil.toBean(json, SysOrdinaryUser.class);
        
        // 4. 数据修正：重置状态为“正常在校”
        recoveryUser.setStatus("0"); // 0-正常
        recoveryUser.setCampusStatus(0); // 0-在校
        recoveryUser.setRemark(recoveryUser.getRemark() + " | " + DateUtil.now() + " 从档案库一键恢复");
        
        // 5. 写回主表并销毁档案
        this.save(recoveryUser);
        archiveMapper.deleteById(userId);
        
        log.info("【系统自愈】用户 [{} {}] 已从档案库无损恢复至活跃库。", archive.getUsername(), archive.getRealName());
    }
    
    /**
     * [标记留级/休学状态]
     * 🛡️ 精密控制：手动干预学生的学籍生命周期，防止被清算任务“误伤”。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markSpecialStatus(Long userId, Integer status, Integer years) {
        SysOrdinaryUser user = this.getById(userId);
        if (user == null) throw new ServiceException("用户不存在");
        
        // 根据状态类型进行针对性处理
        if (status == 2) { // 2-标记为休学
            user.setCampusStatus(2);
            // ✨ 修正点：将 LocalDateTime 转为 LocalDate
            user.setSuspensionStartDate(LocalDate.now());
            // 累加休学年数，用于计算毕业时间
            user.setSuspensionYears((user.getSuspensionYears() != null ? user.getSuspensionYears() : 0) + years);
            user.setRemark(user.getRemark() + " | 手动标记休学 " + years + " 年");
        }
        else if (status == 4) { // 4-标记为留级/延毕
            user.setCampusStatus(4);
            // 累加留级年数，Task 判定公式会自动推迟清算
            user.setRetainedYears((user.getRetainedYears() != null ? user.getRetainedYears() : 0) + years);
            user.setRemark(user.getRemark() + " | 手动标记留级/延毕 " + years + " 年");
        }
        
        this.updateById(user);
        log.info("【学籍调整】已为用户 [{}] 登记 {} 状态，涉及年限: {} 年", user.getRealName(), status == 2 ? "休学" : "留级", years);
    }
}