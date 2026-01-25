package com.mol.server.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.idev.excel.FastExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.entity.SysAdminUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.entity.SysCampus;
import com.mol.server.entity.SysCollege;
import com.mol.server.entity.SysDept;
import com.mol.server.mapper.SysAdminUserMapper;
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
 * </p>
 *
 * @author mol
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysAdminUserServiceImpl extends ServiceImpl<SysAdminUserMapper, SysAdminUser> implements SysAdminUserService {
    
    // 🟢 注入必要的字典服务 (用于导出时的 "ID -> 名称" 翻译)
    private final SysCampusService campusService;
    private final SysDeptService deptService;
    private final SysCollegeService collegeService;
    
    // =================================================================================
    // 1. 新增管理员
    // =================================================================================
    
    /**
     * 新增管理员 (宿管、后勤、辅导员)
     *
     * @param admin 管理员实体
     * @return 是否成功
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
        
        // 🔒 防刁民：手机号简单校验 (11位)
        if (StrUtil.isNotBlank(admin.getPhone())) {
            if (admin.getPhone().length() != 11) {
                throw new ServiceException("手机号格式错误，请输入 11 位号码");
            }
        } else {
            // 如果没填，给个默认空串，避免数据库存 NULL
            admin.setPhone("");
        }
        
        // --- 2. 账号查重 (Double Check) ---
        long count = this.count(new LambdaQueryWrapper<SysAdminUser>()
                .eq(SysAdminUser::getUsername, admin.getUsername()));
        if (count > 0) {
            throw new ServiceException("该管理员账号[" + admin.getUsername() + "]已存在，请更换");
        }
        
        // --- 3. 密码加密 ---
        // 默认密码 123456
        String rawPwd = StrUtil.isBlank(admin.getPassword()) ? "123456" : admin.getPassword();
        admin.setPassword(BCrypt.hashpw(rawPwd, BCrypt.gensalt()));
        
        // 🟢 强制标记为初始密码 (安全合规，强迫首次登录改密)
        admin.setIsInitialPwd(1);
        
        // --- 4. 默认状态 ---
        if (StrUtil.isBlank(admin.getStatus())) {
            admin.setStatus("0"); // 0=正常
        }
        // 居住类型默认校外(1)
        if (admin.getResidenceType() == null) {
            admin.setResidenceType(1);
        }
        
        return this.save(admin);
    }
    
    // =================================================================================
    // 2. 修改管理员
    // =================================================================================
    
    /**
     * 修改管理员资料
     * 注意：此方法严禁修改密码或账号，防止越权。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAdmin(SysAdminUser admin) {
        // 🛡️ 安全防御：
        // 绝对禁止通过此接口修改 [密码] 或 [账号]，防止前端恶意传参篡改。
        admin.setPassword(null);
        admin.setUsername(null);
        
        // 手机号变更时的格式校验
        if (StrUtil.isNotBlank(admin.getPhone()) && admin.getPhone().length() != 11) {
            throw new ServiceException("手机号格式错误");
        }
        
        return this.updateById(admin);
    }
    
    // =================================================================================
    // 3. 密码重置
    // =================================================================================
    
    /**
     * 强制重置管理员密码 (由超管操作)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, String newPassword) {
        if (StrUtil.length(newPassword) < 6) {
            throw new ServiceException("密码长度不能少于6位");
        }
        
        String encodePwd = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        
        // 更新并重置 "初始密码" 标记
        this.update(Wrappers.<SysAdminUser>lambdaUpdate()
                .eq(SysAdminUser::getId, userId)
                .set(SysAdminUser::getPassword, encodePwd)
                .set(SysAdminUser::getIsInitialPwd, 1)); // 重置后，该用户下次登录需强制改密
    }
    
    // =================================================================================
    // 4. Excel 导出 (新增功能)
    // =================================================================================
    
    /**
     * 导出管理员名单 (含 ID->Name 翻译)
     */
    @Override
    public void exportData(HttpServletResponse response, SysAdminUser queryParams) {
        try {
            // 🟢 1. 查询数据
            List<SysAdminUser> list = this.lambdaQuery()
                    .like(StrUtil.isNotBlank(queryParams.getRealName()), SysAdminUser::getRealName, queryParams.getRealName())
                    .like(StrUtil.isNotBlank(queryParams.getUsername()), SysAdminUser::getUsername, queryParams.getUsername())
                    .orderByDesc(SysAdminUser::getCreateTime)
                    .list();
            
            // 防呆：无数据报错
            if (CollUtil.isEmpty(list)) {
                throw new ServiceException("当前筛选条件下无数据，无法导出");
            }
            
            log.info("导出管理员：正在加载基础数据字典...");
            
            // 🟢 2. 准备字典 (ID -> Name)
            // 避免循环查库，一次性查出所有基础信息
            Map<Long, String> campusMap = campusService.list().stream()
                    .collect(Collectors.toMap(SysCampus::getId, SysCampus::getCampusName));
            
            Map<Long, String> deptMap = deptService.list().stream()
                    .collect(Collectors.toMap(SysDept::getId, SysDept::getName));
            
            Map<Long, String> collegeMap = collegeService.list().stream()
                    .collect(Collectors.toMap(SysCollege::getId, SysCollege::getName));
            
            // 🟢 3. 转换数据 (Entity -> VO)
            List<AdminExportVO> exportList = list.stream().map(user -> {
                AdminExportVO vo = new AdminExportVO();
                // 基础拷贝
                vo.setUsername(user.getUsername());
                vo.setRealName(user.getRealName());
                vo.setPhone(user.getPhone());
                
                // 逻辑转换
                vo.setStatus("0".equals(user.getStatus()) ? "正常" : "停用");
                vo.setResidenceType(user.getResidenceType() == 0 ? "住校" : "校外");
                
                // ID 转名称 (使用 getOrDefault 防止空指针)
                vo.setCampusName(campusMap.getOrDefault(user.getCampusId(), ""));
                vo.setDeptName(deptMap.getOrDefault(user.getDeptId(), ""));
                vo.setCollegeName(collegeMap.getOrDefault(user.getCollegeId(), ""));
                
                return vo;
            }).collect(Collectors.toList());
            
            // 🟢 4. 写出 Excel
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("管理员名单_" + System.currentTimeMillis(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            
            FastExcel.write(response.getOutputStream(), AdminExportVO.class)
                    .sheet("管理员列表")
                    .doWrite(exportList);
            
        } catch (Exception e) {
            log.error("导出管理员失败", e);
            throw new ServiceException("文件生成失败: " + e.getMessage());
        }
    }
}