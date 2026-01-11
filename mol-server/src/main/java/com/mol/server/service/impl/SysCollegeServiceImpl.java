package com.mol.server.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.server.entity.SysCollege;
import com.mol.server.mapper.SysCollegeMapper;
import com.mol.server.service.SysCollegeService;
import org.springframework.stereotype.Service;

/**
 * 【学院】业务实现
 */
@Service
public class SysCollegeServiceImpl extends ServiceImpl<SysCollegeMapper, SysCollege> implements SysCollegeService {
}