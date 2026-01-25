package com.mol.server.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.entity.SysCampus;
import com.mol.server.mapper.SysCampusMapper;
import com.mol.server.mapper.SysOrdinaryUserMapper;
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
@RequiredArgsConstructor // 自动注入 final 字段
public class SysCampusServiceImpl extends ServiceImpl<SysCampusMapper, SysCampus> implements SysCampusService {
    
    // ❌ 注意：不要在这里注入 DormBuildingMapper！
    // 因为 SysCampus 在 server 模块，DormBuilding 在 dorm 模块。
    // server 模块不应该知道 dorm 模块的存在（下层不能依赖上层）。
    
    // 如果非要检查，建议在 Controller 层先调用 buildingService.count() 检查，再调用这里的 remove。
    // 或者，定义一个通用的 CheckService 接口注入进来。
    
    
    // 注入用户 Mapper (同在 server 模块，可以依赖)
    private final SysOrdinaryUserMapper userMapper;
    
    
    /**
     * 增加校区
     */
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
    
    /**
     * 更新校区
     */
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
     * 策略：
     * 1. Service层负责检查【人员】占用。
     * 2. Controller层负责检查【楼栋】占用 (跨模块协调)。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeCampus(Long campusId) {
        // 1. 基础防刁民：系统默认校区禁止删除 (假设 ID=1 是本部)
        if (campusId == 1L) {
            throw new ServiceException("系统默认校区, 禁止删除");
        }
        
        // 2. 检查该校区下是否有人员 (学生/教工)
        Long userCount = userMapper.selectCount(new LambdaQueryWrapper<SysOrdinaryUser>()
                .eq(SysOrdinaryUser::getCampusId, campusId));
        
        if (userCount > 0) {
            throw new ServiceException("删除失败：该校区下尚有 " + userCount + " 名人员！请先进行人员调动。");
        }
        
        // 3. 执行删除
        return super.removeById(campusId);
    }
}