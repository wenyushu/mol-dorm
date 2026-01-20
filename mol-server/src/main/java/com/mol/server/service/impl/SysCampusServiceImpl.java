package com.mol.server.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.entity.SysCampus;
import com.mol.server.mapper.SysCampusMapper;
import com.mol.server.service.SysCampusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 校区管理业务实现类
 *
 * @author mol
 */
@Service
@RequiredArgsConstructor
public class SysCampusServiceImpl extends ServiceImpl<SysCampusMapper, SysCampus> implements SysCampusService {
    
    // ❌ 注意：不要在这里注入 DormBuildingMapper！
    // 因为 SysCampus 在 server 模块，DormBuilding 在 dorm 模块。
    // server 模块不应该知道 dorm 模块的存在（下层不能依赖上层）。
    
    // 如果非要检查，建议在 Controller 层先调用 buildingService.count() 检查，再调用这里的 remove。
    // 或者，定义一个通用的 CheckService 接口注入进来。
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addCampus(SysCampus campus) {
        // 1. 校验编码唯一性
        boolean exists = this.exists(new LambdaQueryWrapper<SysCampus>()
                .eq(SysCampus::getCampusCode, campus.getCampusCode()));
        if (exists) {
            throw new ServiceException("校区编码 " + campus.getCampusCode() + " 已存在");
        }
        
        // 2. 默认启用
        if (campus.getStatus() == null) {
            campus.setStatus(1);
        }
        
        return this.save(campus);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCampus(SysCampus campus) {
        // 校验编码唯一性 (排除自己)
        if (StrUtil.isNotBlank(campus.getCampusCode())) {
            boolean exists = this.exists(new LambdaQueryWrapper<SysCampus>()
                    .eq(SysCampus::getCampusCode, campus.getCampusCode())
                    .ne(SysCampus::getId, campus.getId()));
            if (exists) {
                throw new ServiceException("校区编码 " + campus.getCampusCode() + " 已存在");
            }
        }
        return this.updateById(campus);
    }
    
    /**
     * 删除校区
     * 注意：这里只负责删校区本身。
     * "检查楼栋" 的逻辑应当在 Controller 层组装，或者通过 Spring Event 机制解耦。
     * * 如果你强行要在这里检查，你需要引入 Dorm 模块的 Mapper，但这会破坏分层架构。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeCampus(Long campusId) {
        // 建议：此处只做最基础的检查，比如系统默认校区不能删
        // 🛡️ 基础防刁民：系统默认数据保护 (假设 ID 1 是本部)
        if (campusId == 1L) {
            throw new ServiceException("系统默认校区禁止删除");
        }
        
        // 注意：关于 "该校区下是否有楼栋" 的检查，
        // 请在 Controller 层调用 DormBuildingService 进行检查，
        // 避免在此处引入 Dorm 模块的 Mapper 导致循环依赖。
        return super.removeById(campusId);
    }
}