package com.mol.server.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.idev.excel.FastExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.entity.SysAdminUser;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.entity.SysUserRole;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.entity.SysCampus;
import com.mol.server.entity.SysCollege;
import com.mol.server.entity.SysDept;
import com.mol.server.mapper.SysAdminUserMapper;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import com.mol.server.mapper.SysUserRoleMapper;
import com.mol.server.service.SysAdminUserService;
import com.mol.server.service.SysCampusService;
import com.mol.server.service.SysCollegeService;
import com.mol.server.service.SysDeptService;
import com.mol.server.vo.AdminExportVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统管理员业务实现类
 * <p>
 * 核心职责：
 * 1. 管理员账号的增删改查。
 * 2. 密码安全管理（加密、重置、初始密码标记）。
 * 3. 管理员名单导出（支持 ID -> 名称翻译）。
 * 4. 角色绑定 (新增时强制绑定角色，修改时支持变更角色)。
 * </p>
 *
 * @author mol
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysAdminUserServiceImpl extends ServiceImpl<SysAdminUserMapper, SysAdminUser> implements SysAdminUserService {
    
    // 注入必要的字典服务
    private final SysCampusService campusService;
    private final SysDeptService deptService;
    private final SysCollegeService collegeService;
    
    // 核心引入：需要查隔壁的学生表 (跨表查重)
    private final SysOrdinaryUserMapper ordinaryUserMapper;
    
    // 🟢 新增引入：角色关联 Mapper (用于赋权)
    private final SysUserRoleMapper userRoleMapper;
    
    // =================================================================================
    // 1. 新增管理员
    // =================================================================================
    
    /**
     * 新增管理员 (宿管、后勤、辅导员)
     * 🟢 必须绑定一个角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveAdmin(SysAdminUser admin) {
        // --- 1. 参数完整性与格式校验 ---
        if (StrUtil.isBlank(admin.getUsername())) {
            throw new ServiceException("管理员账号不能为空");
        }
        if (StrUtil.isBlank(admin.getRealName())) {
            throw new ServiceException("真实姓名不能为空");
        }
        
        // 🟢 核心约束：必须指定角色
        if (admin.getRoleId() == null) {
            throw new ServiceException("新增失败：必须为管理员指定一个角色");
        }
        
        // 🔒 手机号校验
        if (StrUtil.isNotBlank(admin.getPhone())) {
            if (admin.getPhone().length() != 11) {
                throw new ServiceException("手机号格式错误，请输入 11 位号码");
            }
        } else {
            admin.setPhone("");
        }
        
        // --- 2. 🛡️ 账号唯一性检查 (双表互斥) ---
        // A. 查 Admin 表
        long countSelf = this.count(new LambdaQueryWrapper<SysAdminUser>()
                .eq(SysAdminUser::getUsername, admin.getUsername()));
        if (countSelf > 0) {
            throw new ServiceException("新增失败：管理员账号[" + admin.getUsername() + "]已存在");
        }
        
        // B. 查 Student 表
        Long countOther = ordinaryUserMapper.selectCount(Wrappers.<SysOrdinaryUser>lambdaQuery()
                .eq(SysOrdinaryUser::getUsername, admin.getUsername()));
        if (countOther > 0) {
            throw new ServiceException("新增失败：该账号已被【普通用户/学生】占用，请更换账号！");
        }
        
        // --- 3. 密码加密 ---
        String rawPwd = StrUtil.isBlank(admin.getPassword()) ? "123456" : admin.getPassword();
        admin.setPassword(BCrypt.hashpw(rawPwd, BCrypt.gensalt()));
        admin.setIsInitialPwd(1);
        
        // --- 4. 默认状态 ---
        if (StrUtil.isBlank(admin.getStatus())) admin.setStatus("0");
        if (admin.getResidenceType() == null) admin.setResidenceType(1);
        
        // --- 5. 落库 (先存人，再存角色) ---
        boolean saveResult = this.save(admin);
        
        if (saveResult) {
            // 🟢 插入角色关联表
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(admin.getId());
            userRole.setRoleId(admin.getRoleId()); // 前端传来的角色ID
            userRoleMapper.insert(userRole);
        }
        
        return saveResult;
    }
    
    // =================================================================================
    // 2. 修改管理员
    // =================================================================================
    
    /**
     * 修改管理员资料
     * 🟢 支持修改角色 (先删旧，再插新)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAdmin(SysAdminUser admin) {
        // 🛡️ 安全防御：禁止修改敏感字段
        admin.setPassword(null);
        admin.setUsername(null);
        
        // 手机号校验
        if (StrUtil.isNotBlank(admin.getPhone()) && admin.getPhone().length() != 11) {
            throw new ServiceException("手机号格式错误");
        }
        
        // 1. 更新基本信息
        boolean updateResult = this.updateById(admin);
        
        // 2. 🟢 角色变更逻辑
        // 如果前端传了 roleId，说明要修改角色
        if (admin.getRoleId() != null) {
            // 先清理旧角色
            userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, admin.getId()));
            
            // 再赋予新角色
            SysUserRole newUserRole = new SysUserRole();
            newUserRole.setUserId(admin.getId());
            newUserRole.setRoleId(admin.getRoleId());
            userRoleMapper.insert(newUserRole);
        }
        
        return updateResult;
    }
    
    // =================================================================================
    // 3. 密码重置 (逻辑不变)
    // =================================================================================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, String newPassword) {
        if (StrUtil.length(newPassword) < 6) {
            throw new ServiceException("密码长度不能少于6位");
        }
        String encodePwd = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        this.update(Wrappers.<SysAdminUser>lambdaUpdate()
                .eq(SysAdminUser::getId, userId)
                .set(SysAdminUser::getPassword, encodePwd)
                .set(SysAdminUser::getIsInitialPwd, 1));
    }
    
    // =================================================================================
    // 4. Excel 导出 (逻辑不变)
    // =================================================================================
    
    @Override
    public void exportData(HttpServletResponse response, SysAdminUser queryParams) {
        try {
            List<SysAdminUser> list = this.lambdaQuery()
                    .like(StrUtil.isNotBlank(queryParams.getRealName()), SysAdminUser::getRealName, queryParams.getRealName())
                    .like(StrUtil.isNotBlank(queryParams.getUsername()), SysAdminUser::getUsername, queryParams.getUsername())
                    .orderByDesc(SysAdminUser::getCreateTime)
                    .list();
            
            if (CollUtil.isEmpty(list)) {
                throw new ServiceException("当前筛选条件下无数据，无法导出");
            }
            
            log.info("导出管理员：正在加载基础数据字典...");
            Map<Long, String> campusMap = campusService.list().stream().collect(Collectors.toMap(SysCampus::getId, SysCampus::getCampusName));
            Map<Long, String> deptMap = deptService.list().stream().collect(Collectors.toMap(SysDept::getId, SysDept::getName));
            Map<Long, String> collegeMap = collegeService.list().stream().collect(Collectors.toMap(SysCollege::getId, SysCollege::getName));
            
            List<AdminExportVO> exportList = list.stream().map(user -> {
                AdminExportVO vo = new AdminExportVO();
                vo.setUsername(user.getUsername());
                vo.setRealName(user.getRealName());
                vo.setPhone(user.getPhone());
                vo.setStatus("0".equals(user.getStatus()) ? "正常" : "停用");
                vo.setResidenceType(user.getResidenceType() == 0 ? "住校" : "校外");
                vo.setCampusName(campusMap.getOrDefault(user.getCampusId(), ""));
                vo.setDeptName(deptMap.getOrDefault(user.getDeptId(), ""));
                vo.setCollegeName(collegeMap.getOrDefault(user.getCollegeId(), ""));
                return vo;
            }).collect(Collectors.toList());
            
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("管理员名单_" + System.currentTimeMillis(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            
            FastExcel.write(response.getOutputStream(), AdminExportVO.class).sheet("管理员列表").doWrite(exportList);
            
        } catch (Exception e) {
            log.error("导出管理员失败", e);
            throw new ServiceException("文件生成失败: " + e.getMessage());
        }
    }
}