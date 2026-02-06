package com.mol.server.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.entity.SysCollege;
import com.mol.server.entity.SysMajor;
import com.mol.server.mapper.SysCollegeMapper;
import com.mol.server.service.SysCollegeService;
import com.mol.server.service.SysMajorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

/**
 * 【学院】业务实现
 */
@Service
@RequiredArgsConstructor
public class SysCollegeServiceImpl extends ServiceImpl<SysCollegeMapper, SysCollege> implements SysCollegeService {
    
    private final SysMajorService majorService;
    
    // =================================================================================
    // 1. 新增学院 (带校验)
    // =================================================================================
    @Override
    public boolean save(SysCollege college) {
        // --- A. 非空校验 ---
        if (StrUtil.isBlank(college.getName())) {
            throw new ServiceException("新增失败：学院名称不能为空");
        }
        if (StrUtil.isBlank(college.getCode())) {
            throw new ServiceException("新增失败：学院代码不能为空");
        }
        if (college.getCampusId() == null) {
            throw new ServiceException("新增失败：必须指定所属校区");
        }
        
        // --- B. 唯一性校验 (查重) ---
        checkUnique(null, college.getName(), college.getCode());
        
        // --- C. 执行保存 ---
        return super.save(college);
    }
    
    // =================================================================================
    // 2. 修改学院 (带校验)
    // =================================================================================
    @Override
    public boolean updateById(SysCollege college) {
        // --- A. 基础校验 ---
        if (college.getId() == null) {
            throw new ServiceException("修改失败：ID 不能为空");
        }
        
        // --- B. 唯一性校验 (排除自己) ---
        // 只有当名称或代码被修改时，才需要查重。但简单起见，每次都查一下也无妨。
        // 关键点：checkUnique 的第一个参数传当前 ID，表示“查重时忽略我自己”
        if (StrUtil.isNotBlank(college.getName()) || StrUtil.isNotBlank(college.getCode())) {
            checkUnique(college.getId(), college.getName(), college.getCode());
        }
        
        // --- C. 执行更新 ---
        return super.updateById(college);
    }
    
    // =================================================================================
    // 3. 删除学院 (带依赖检查)
    // =================================================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        // 1. 检查是否存在
        SysCollege college = this.getById(id);
        if (college == null) {
            // 这里抛异常比返回 false 更明确，能告诉用户具体原因
            throw new ServiceException("删除失败：ID " + id + " 不存在");
        }
        
        // 2. 检查依赖 (该学院下是否有专业)
        long majorCount = majorService.count(new LambdaQueryWrapper<SysMajor>()
                .eq(SysMajor::getCollegeId, id));
        
        if (majorCount > 0) {
            throw new ServiceException("删除失败：该学院下包含 " + majorCount + " 个专业，请先删除或迁移专业！");
        }
        
        // 3. 执行删除
        return super.removeById(id);
    }
    
    // =================================================================================
    // 🛡️ 私有辅助方法：通用唯一性校验
    // =================================================================================
    /**
     * 检查名称和代码是否重复
     * @param id 当前操作的ID (新增时为null，修改时为当前ID)
     * @param name 学院名称
     * @param code 学院代码
     */
    private void checkUnique(Long id, String name, String code) {
        // 1. 检查名称重复
        if (StrUtil.isNotBlank(name)) {
            long count = this.count(new LambdaQueryWrapper<SysCollege>()
                    .eq(SysCollege::getName, name)
                    .ne(id != null, SysCollege::getId, id)); // 修改时排除自己
            if (count > 0) {
                throw new ServiceException("操作失败：学院名称 [" + name + "] 已存在");
            }
        }
        
        // 2. 检查代码重复
        if (StrUtil.isNotBlank(code)) {
            long count = this.count(new LambdaQueryWrapper<SysCollege>()
                    .eq(SysCollege::getCode, code)
                    .ne(id != null, SysCollege::getId, id)); // 修改时排除自己
            if (count > 0) {
                throw new ServiceException("操作失败：学院代码 [" + code + "] 已存在");
            }
        }
    }
}