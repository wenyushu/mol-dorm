package com.mol.sys.biz.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.sys.biz.entity.SysClass;
import com.mol.sys.biz.mapper.SysClassMapper;
import com.mol.sys.biz.service.SysClassService;
import org.springframework.stereotype.Service;

/**
 * 【班级】业务实现
 */
@Service
public class SysClassServiceImpl extends ServiceImpl<SysClassMapper, SysClass> implements SysClassService {
}