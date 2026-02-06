package com.mol.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.server.entity.SysClass;
import com.mol.server.vo.SysClassVO;

/**
 * 班级服务接口
 */
public interface SysClassService extends IService<SysClass> {
    
    /**
     * 分页查询班级列表 (VO模式，包含专业名、学院名)
     * @param page 分页参数
     * @param queryParams 查询参数(年级、班名)
     * @return 分页结果
     */
    IPage<SysClassVO> getClassVoPage(Page<SysClassVO> page, SysClass queryParams);
    
//    /**
//     * 新增班级 (包含自动填充培养层次 + 查重)
//     * @param sysClass 班级实体
//     * @return 是否成功
//     */
//    boolean addClass(SysClass sysClass);
//
//    /**
//     * 修改班级 (包含重新同步层次)
//     * @param sysClass 班级实体
//     * @return 是否成功
//     */
//    boolean updateClass(SysClass sysClass);
}