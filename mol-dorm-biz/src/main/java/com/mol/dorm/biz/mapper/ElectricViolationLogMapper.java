package com.mol.dorm.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.server.entity.ElectricViolationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 违规电器日志 Mapper
 */
@Mapper
public interface ElectricViolationLogMapper extends BaseMapper<ElectricViolationLog> {
}