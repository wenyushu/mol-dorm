package com.mol.server.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.entity.SysClass;
import com.mol.server.entity.SysMajor;
import com.mol.server.mapper.SysClassMapper;
import com.mol.server.mapper.SysMajorMapper;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import com.mol.server.service.SysClassService;
import com.mol.server.vo.SysClassVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

/**
 * 【班级】业务实现
 */
@Service
@RequiredArgsConstructor
public class SysClassServiceImpl extends ServiceImpl<SysClassMapper, SysClass> implements SysClassService {
    
    // 注入 UserMapper (用于检查学生依赖)
    private final SysOrdinaryUserMapper userMapper;
    // 注入 MajorMapper (用于查询专业，回填培养层次)
    private final SysMajorMapper majorMapper;
    
    // =================================================================================
    // 1. 分页查询 (VO模式)
    // =================================================================================
    @Override
    public IPage<SysClassVO> getClassVoPage(Page<SysClassVO> page, SysClass queryParams) {
        QueryWrapper<SysClass> wrapper = new QueryWrapper<>();
        // 1. 年级查询
        if (queryParams.getGrade() != null) {
            wrapper.eq("c.grade", queryParams.getGrade());
        }
        // 2. 班级名称查询
        if (StrUtil.isNotBlank(queryParams.getClassName())) {
            wrapper.like("c.class_name", queryParams.getClassName());
        }
        // 调用 Mapper 的自定义 SQL
        return baseMapper.selectClassVoPage(page, wrapper);
    }
    
    // =================================================================================
    // 2. 新增班级 (save) - 自动填充层次 + 查重
    // =================================================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(SysClass sysClass) {
        // A. 基础校验
        if (sysClass.getMajorId() == null) throw new ServiceException("新增失败：必须指定所属专业");
        if (sysClass.getGrade() == null) throw new ServiceException("新增失败：必须指定年级");
        if (StrUtil.isBlank(sysClass.getClassName())) throw new ServiceException("新增失败：班级名称不能为空");
        
        // B. ⚡️ 核心：自动填充培养层次 (从专业表拿)
        fillEducationLevel(sysClass);
        
        // C. 查重逻辑 (同一专业下，年级+班级名不能重复)
        // 例如：软件工程下不能有两个 "2024级1班"
        checkUnique(null, sysClass);
        
        return super.save(sysClass);
    }
    
    // =================================================================================
    // 3. 修改班级 (updateById) - 同步层次
    // =================================================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(SysClass sysClass) {
        // A. ID校验
        if (sysClass.getId() == null) throw new ServiceException("修改失败：ID 不能为空");
        
        // B. ⚡️ 核心：如果修改了专业，需要重新同步培养层次
        if (sysClass.getMajorId() != null) {
            fillEducationLevel(sysClass);
        }
        
        // C. 查重逻辑 (排除自己)
        // 只有当修改了 专业/年级/班名 时才检查
        if (sysClass.getMajorId() != null || sysClass.getGrade() != null || StrUtil.isNotBlank(sysClass.getClassName())) {
            // 注意：这里需要去数据库查出旧数据拼装，或者假设前端传的是全量数据。
            // 简单起见，我们假设前端传了关键字段，直接校验。
            checkUnique(sysClass.getId(), sysClass);
        }
        
        return super.updateById(sysClass);
    }
    
    // =================================================================================
    // 4. 删除班级 (removeById) - 依赖检查
    // =================================================================================
    @Override
    public boolean removeById(Serializable id) {
        // 1. 检查该班级下是否有学生
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysOrdinaryUser>()
                .eq(SysOrdinaryUser::getClassId, id));
        
        if (count > 0) {
            throw new ServiceException("删除失败：该班级下尚有 " + count + " 名学生！请先将学生转移或删除。");
        }
        
        // 2. 确认没人后，执行删除
        return super.removeById(id);
    }
    
    // =================================================================================
    // 🛡️ 私有辅助方法
    // =================================================================================
    
    /**
     * 自动查找并回填培养层次
     */
    private void fillEducationLevel(SysClass sysClass) {
        if (sysClass.getMajorId() != null) {
            SysMajor major = majorMapper.selectById(sysClass.getMajorId());
            if (major != null) {
                // 自动继承专业的层次 (例如 "本科", "硕士研究生")
                sysClass.setEducationLevel(major.getLevel());
            } else {
                // 防刁民：传了一个不存在的 majorId
                throw new ServiceException("操作失败：指定的专业ID不存在");
            }
        }
    }
    
    /**
     * 唯一性校验
     * 规则：同一个专业(Major) + 同一个年级(Grade) + 同一个班名(ClassName) 必须唯一
     */
    private void checkUnique(Long id, SysClass sysClass) {
        // 如果字段不全，可能无法进行完整校验，视业务情况决定是否跳过或强制查库
        // 这里假设是关键字段校验
        if (sysClass.getMajorId() != null && sysClass.getGrade() != null && StrUtil.isNotBlank(sysClass.getClassName())) {
            long count = this.count(new LambdaQueryWrapper<SysClass>()
                    .eq(SysClass::getMajorId, sysClass.getMajorId())
                    .eq(SysClass::getGrade, sysClass.getGrade())
                    .eq(SysClass::getClassName, sysClass.getClassName())
                    .ne(id != null, SysClass::getId, id)); // 修改时排除自己
            
            if (count > 0) {
                throw new ServiceException("操作失败：该专业年级下已存在同名班级 [" + sysClass.getClassName() + "]");
            }
        }
    }
}