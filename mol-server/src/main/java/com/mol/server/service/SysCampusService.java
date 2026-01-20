package com.mol.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.server.entity.SysCampus;

/**
 * 校区服务接口
 */
public interface SysCampusService extends IService<SysCampus> {
    
    /**
     * 新增校区 (带编码查重)
     */
    boolean addCampus(SysCampus campus);
    
    /**
     * 修改校区 (带编码查重)
     */
    boolean updateCampus(SysCampus campus);
    
    /**
     * 删除校区
     */
    boolean removeCampus(Long campusId);
}