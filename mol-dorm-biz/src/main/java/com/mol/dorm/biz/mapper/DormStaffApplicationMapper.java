package com.mol.dorm.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.dorm.biz.entity.DormStaffApplication;
import org.apache.ibatis.annotations.Mapper;

/**
 * 教职工住宿申请 Mapper 接口
 */
@Mapper
public interface DormStaffApplicationMapper extends BaseMapper<DormStaffApplication> {
}