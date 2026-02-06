package com.mol.dorm.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.dorm.biz.entity.DormWorkflow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宿舍业务流转 Mapper
 */
@Mapper
public interface DormWorkflowMapper extends BaseMapper<DormWorkflow> {
}