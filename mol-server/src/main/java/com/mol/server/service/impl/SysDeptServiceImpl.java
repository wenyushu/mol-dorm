package com.mol.server.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.entity.SysDept;
import com.mol.server.mapper.SysDeptMapper;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import com.mol.server.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

/**
 * 【部门】业务实现 (如后勤处、教务处)
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {
    
    // 注入用户 Mapper，用于检查是否有教职工归属该部门
    private final SysOrdinaryUserMapper userMapper;
    
    // =================================================================================
    // 1. 新增部门 (带校验)
    // =================================================================================
    @Override
    public boolean save(SysDept dept) {
        // A. 非空校验
        if (StrUtil.isBlank(dept.getName())) {
            throw new ServiceException("新增失败：部门名称不能为空");
        }
        if (dept.getCampusId() == null) {
            throw new ServiceException("新增失败：必须指定所属校区");
        }
        
        // B. 唯一性校验
        checkUnique(null, dept.getName());
        
        return super.save(dept);
    }
    
    // =================================================================================
    // 2. 修改部门 (带校验)
    // =================================================================================
    @Override
    public boolean updateById(SysDept dept) {
        // A. 校验 ID
        if (dept.getId() == null) {
            throw new ServiceException("修改失败：ID 不能为空");
        }
        
        // B. 唯一性校验 (排除自己)
        if (StrUtil.isNotBlank(dept.getName())) {
            checkUnique(dept.getId(), dept.getName());
        }
        
        return super.updateById(dept);
    }
    
    // =================================================================================
    // 3. 删除部门 (带依赖检查)
    // =================================================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        // 1. 检查部门是否存在
        SysDept dept = this.getById(id);
        if (dept == null) {
            throw new ServiceException("删除失败：ID " + id + " 不存在");
        }
        
        // 2. 检查该部门下是否有教职工 (UserCategory=1 的用户, 或任意用户)
        // 使用 Wrappers 工厂类更简洁
        Long count = userMapper.selectCount(Wrappers.<SysOrdinaryUser>lambdaQuery()
                .eq(SysOrdinaryUser::getDeptId, id));
        
        if (count > 0) {
            throw new ServiceException("删除失败：该部门下尚有 " + count + " 名教职工！请先进行人员调动。");
        }
        
        // 3. 安全删除
        return super.removeById(id);
    }
    
    // =================================================================================
    // 🛡️ 私有辅助方法
    // =================================================================================
    /**
     * 检查名称是否重复
     * @param id 当前ID (排除自己用)
     * @param name 部门名称
     */
    private void checkUnique(Long id, String name) {
        long count = this.count(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getName, name)
                .ne(id != null, SysDept::getId, id)); // 修改时排除自己
        
        if (count > 0) {
            throw new ServiceException("操作失败：部门名称 [" + name + "] 已存在");
        }
    }
}