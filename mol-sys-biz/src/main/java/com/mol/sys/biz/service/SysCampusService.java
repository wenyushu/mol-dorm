package com.mol.sys.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mol.sys.biz.entity.SysCampus;

/**
 * 校区管理 业务接口
 * 继承 IService 可以直接使用 MP 提供的常用业务方法
 *
 * @author mol
 */
public interface SysCampusService extends IService<SysCampus> {
    // 这里可以定义复杂的业务逻辑方法，例如：根据编码查询校区
    // 目前用 MyBatis-Plus 自带的 CRUD 就够了
}