package com.mol.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.server.entity.SysCampus;
import com.mol.server.vo.SysCampusTreeVO;
import java.util.List;

/**
 * 校区基础信息服务接口
 */
public interface SysCampusService extends IService<SysCampus> {
    
    /**
     * 获取全校区资源树 (含解耦后的楼栋简项)
     * 🛡️ [防刁民设计]：支持状态过滤，默认仅返回运营中的校区。
     */
    List<SysCampusTreeVO> getCampusBuildingTree(Integer status);
}