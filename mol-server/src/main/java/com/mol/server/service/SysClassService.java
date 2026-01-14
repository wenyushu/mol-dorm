package com.mol.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.server.entity.SysClass;
import com.mol.server.vo.SysClassVO;

public interface SysClassService extends IService<SysClass> {
    
    /**
     * 获取班级 VO 分页列表
     * @param page 分页参数
     * @param queryParams 查询参数
     * @return 分页结果
     */
    IPage<SysClassVO> getClassVoPage(Page<SysClassVO> page, SysClass queryParams);
}