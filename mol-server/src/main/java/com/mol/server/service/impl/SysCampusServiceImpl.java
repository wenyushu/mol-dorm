package com.mol.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.server.entity.SysCampus;
import com.mol.server.mapper.SysCampusMapper;
import com.mol.server.service.SysCampusService;
import org.springframework.stereotype.Service;

/**
 * 校区管理业务实现类
 *
 * @author mol
 */
@Service
public class SysCampusServiceImpl extends ServiceImpl<SysCampusMapper, SysCampus> implements SysCampusService {
    // 继承 ServiceImpl 后，自动拥有了 save, remove, getById, page 等几十个方法
    // 具体的业务逻辑在此处实现
}