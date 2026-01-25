package com.mol.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.entity.SysDept;
import com.mol.server.mapper.SysDeptMapper;
import com.mol.server.mapper.SysOrdinaryUserMapper; // 👈 引入用户 Mapper
import com.mol.server.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * 【部门】业务实现 (如后勤处、教务处)
 */
@Service
@RequiredArgsConstructor //自动注入 final 字段
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {
    
    // 注入用户 Mapper，用于检查是否有教职工归属该部门
    private final SysOrdinaryUserMapper userMapper;
    
    /**
     * 删除部门
     * 🛡️ 防刁民：如果部门下还有职工，禁止删除，防止数据孤儿。
     */
    @Override
    public boolean removeById(Serializable id) {
        // 1. 检查该部门下是否有教职工 (UserCategory=1 的用户)
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysOrdinaryUser>()
                .eq(SysOrdinaryUser::getDeptId, id));
        
        if (count > 0) {
            throw new ServiceException("删除失败：该部门下尚有 " + count + " 名教职工！请先进行人员调动。");
        }
        
        // 2. 安全删除
        return super.removeById(id);
    }
}