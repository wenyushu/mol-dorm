package com.mol.dorm.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.dorm.biz.entity.DormChangeRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * 调宿申请 Mapper 接口
 * * 必须继承 BaseMapper 才能使用 selectCount, insert 等内置方法
 */
@Mapper
public interface DormChangeRequestMapper extends BaseMapper<DormChangeRequest> {
}