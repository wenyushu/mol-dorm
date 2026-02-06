package com.mol.server.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.server.entity.SysCollege;

/**
 * 【学院】业务接口
 * 管理学院信息 (如：网络安全学院、计算机学院)
 * 继承 IService 后，自动拥有 save, updateById, removeById 等标准方法定义
 */
public interface SysCollegeService extends IService<SysCollege> {
}