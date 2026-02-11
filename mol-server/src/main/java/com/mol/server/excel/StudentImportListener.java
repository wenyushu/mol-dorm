package com.mol.server.excel;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.read.listener.ReadListener;
import cn.idev.excel.util.ListUtils;
import com.mol.common.core.constant.DormConstants;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.entity.SysUserRole;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.entity.*;
import com.mol.server.mapper.*;
import com.mol.server.service.SysOrdinaryUserService;
import com.mol.server.vo.StudentImportVO;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生/教工批量导入监听器 - 高性能寻址版
 */
@Slf4j
public class StudentImportListener implements ReadListener<StudentImportVO> {
    
    private final SysOrdinaryUserService userService;
    private final SysUserRoleMapper userRoleMapper;
    
    // 缓存池 (预热数据)
    // ✨ 加上 final，解决字段可能为 final 的警告
    private final Map<String, Long> campusCache;
    private final Map<String, Long> collegeCache;
    private final Map<String, Long> majorCache;
    private final Map<String, Long> classCache;
    private final Map<String, Long> deptCache;
    
    private final List<StudentImportVO> cachedDataList = ListUtils.newArrayListWithExpectedSize(100);
    
    /**
     * 构造函数中注入 Mapper 进行缓存预热
     */
    public StudentImportListener(
            SysOrdinaryUserService userService,
            SysUserRoleMapper userRoleMapper,
            SysCampusMapper campusMapper,
            SysCollegeMapper collegeMapper,
            SysMajorMapper majorMapper,
            SysClassMapper classMapper,
            SysDeptMapper deptMapper) {
        
        this.userService = userService;
        this.userRoleMapper = userRoleMapper;
        
        // 🔥 [缓存预热] 一次性加载所有基础档案名称与 ID 的映射
        log.info("🚀 正在预热导入缓存，下钻审计组织架构...");
        
        // 1. 校区缓存 (字段是 campusName，保持不变)
        this.campusCache = campusMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysCampus::getCampusName, SysCampus::getId, (k1, k2) -> k1));
        
        // 2. 学院缓存 (修正：字段是 name)
        this.collegeCache = collegeMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysCollege::getName, SysCollege::getId, (k1, k2) -> k1));
        
        // 3. 专业缓存 (字段是 name，保持不变)
        this.majorCache = majorMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysMajor::getName, SysMajor::getId, (k1, k2) -> k1));
        
        // 4. 班级缓存 (字段是 className，保持不变)
        this.classCache = classMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysClass::getClassName, SysClass::getId, (k1, k2) -> k1));
        
        // 5. 部门缓存 (修正：字段是 name)
        this.deptCache = deptMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysDept::getName, SysDept::getId, (k1, k2) -> k1));
    }
    
    @Override
    public void invoke(StudentImportVO data, AnalysisContext context) {
        cachedDataList.add(data);
        if (cachedDataList.size() >= 100) {
            saveData();
            cachedDataList.clear();
        }
    }
    
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
    }
    
    private void saveData() {
        if (CollUtil.isEmpty(cachedDataList)) return;
        
        List<SysOrdinaryUser> usersToSave = new ArrayList<>();
        String defaultPwd = BCrypt.hashpw("123456", BCrypt.gensalt());
        
        for (StudentImportVO vo : cachedDataList) {
            if (StrUtil.isBlank(vo.getUsername())) continue;
            
            SysOrdinaryUser user = new SysOrdinaryUser();
            BeanUtil.copyProperties(vo, user);
            
            // 1. 寻址校验 (防刁民：名称对不上直接熔断)
            user.setCampusId(getCacheId(campusCache, vo.getCampusName(), "校区"));
            user.setCollegeId(getCacheId(collegeCache, vo.getCollegeName(), "学院"));
            
            // 2. 身份与角色判定逻辑
            Long roleId;
            if (StrUtil.isNotBlank(vo.getClassName())) {
                // 判定为学生
                user.setUserCategory(DormConstants.USAGE_STUDENT); // 0
                user.setMajorId(getCacheId(majorCache, vo.getMajorName(), "专业"));
                user.setClassId(getCacheId(classCache, vo.getClassName(), "班级"));
                roleId = RoleConstants.STUDENT_ID; // 8L
            } else {
                // 判定为教工
                user.setUserCategory(DormConstants.USAGE_TEACHER); // 1
                user.setDeptId(getCacheId(deptCache, vo.getDeptName(), "部门"));
                roleId = RoleConstants.COLLEGE_TEACHER_ID; // 6L
            }
            
            user.setGender("男".equals(vo.getGenderStr()) ? "1" : "0");
            user.setPassword(defaultPwd);
            user.setIsInitialPwd(1);
            user.setStatus("0");
            
            // 执行保存并绑定角色
            userService.save(user);
            
            SysUserRole ur = new SysUserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
    }
    
    private Long getCacheId(Map<String, Long> cache, String name, String type) {
        if (StrUtil.isBlank(name)) return null;
        Long id = cache.get(name);
        if (id == null) {
            throw new ServiceException("导入中断：基础档案中不存在名为 [" + name + "] 的" + type);
        }
        return id;
    }
}