package com.mol.dorm.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.dorm.biz.entity.SysDormLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 📝 宿舍日志 Mapper (业务层)
 */
@Mapper
public interface SysDormLogMapper extends BaseMapper<SysDormLog> {
}