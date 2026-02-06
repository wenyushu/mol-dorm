package com.mol.server.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.entity.SysCampus;
import com.mol.server.event.CampusDeleteEvent;
import com.mol.server.mapper.SysCampusMapper;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import com.mol.server.service.SysCampusService;
import com.mol.server.vo.SysCampusTreeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 校区管理业务实现类
 *
 * @author mol
 */
@Slf4j // 必须加上这个，log.info() 才能正常使用
@Service
@RequiredArgsConstructor // 自动注入 final 字段
public class SysCampusServiceImpl extends ServiceImpl<SysCampusMapper, SysCampus> implements SysCampusService {
    
    // ❌ 注意：不要在这里注入 DormBuildingMapper！
    // 因为 SysCampus 在 server 模块，DormBuilding 在 dorm 模块。
    // server 模块不应该知道 dorm 模块的存在（下层不能依赖上层）。
    
    // 如果非要检查，建议在 Controller 层先调用 buildingService.count() 检查，再调用这里的 remove。
    // 或者，定义一个通用的 CheckService 接口注入进来。
    
    private final SysCampusMapper campusMapper;
    private final SysOrdinaryUserMapper userMapper;
    // 注入 Spring 事件发布器，用于通知其他模块 (如 Dorm 模块)
    private final ApplicationEventPublisher eventPublisher;

    // =================================================================================
    // 0. 核心聚合查询：校区 -> 楼栋 资产树
    // =================================================================================
    
    /**
     * 实现树形查询接口
     * 💡 利用 MyBatis 联表嵌套查询，实现 1:N 数据的一次性拉取，比循环查询性能提升数倍。
     */
    @Override
    public List<SysCampusTreeVO> getCampusBuildingTree(Integer status) {
        log.info(">>> [驾驶舱穿透] 正在全量扫描校区资源树，过滤状态: {}", status);
        // 调用我们刚在 XML 中定义的 selectCampusBuildingTree
        return campusMapper.selectCampusBuildingTree(status);
    }
    
    // =================================================================================
    // 1. 新增校区 (save)
    // =================================================================================
    @Override
    public boolean save(SysCampus campus) {
        // A. 基础非空校验
        if (StrUtil.isBlank(campus.getCampusName())) throw new ServiceException("新增失败：校区名称不能为空");
        if (StrUtil.isBlank(campus.getCampusCode())) throw new ServiceException("新增失败：校区编码不能为空");
        
        // B. 🛡️ 防刁民：水电费单价校验
        checkPrice(campus.getPriceWaterCold(), "冷水");
        checkPrice(campus.getPriceWaterHot(), "热水");
        checkPrice(campus.getPriceElectric(), "电费");
        
        // C. 唯一性校验
        checkUnique(null, campus.getCampusName(), campus.getCampusCode());
        
        // D. 默认状态
        if (campus.getStatus() == null) {
            campus.setStatus(1); // 默认启用
        }
        
        log.info("✅ 成功创建新校区：{} ({})", campus.getCampusName(), campus.getCampusCode());
        
        return super.save(campus);
    }
    
    // =================================================================================
    // 2. 修改校区 (updateById)
    // =================================================================================
    @Override
    public boolean updateById(SysCampus campus) {
        // A. ID 校验
        if (campus.getId() == null) throw new ServiceException("修改失败：ID 不能为空");
        
        // B. 唯一性校验 (排除自己)
        if (StrUtil.isNotBlank(campus.getCampusName()) || StrUtil.isNotBlank(campus.getCampusCode())) {
            checkUnique(campus.getId(), campus.getCampusName(), campus.getCampusCode());
        }
        
        // C. 🛡️ 防刁民：如果修改了价格，必须校验非负
        if (campus.getPriceWaterCold() != null) checkPrice(campus.getPriceWaterCold(), "冷水");
        if (campus.getPriceWaterHot() != null) checkPrice(campus.getPriceWaterHot(), "热水");
        if (campus.getPriceElectric() != null) checkPrice(campus.getPriceElectric(), "电费");
        
        log.info("🔄 更新校区信息：ID = {}, 名称 = {}", campus.getId(), campus.getCampusName());
        
        return super.updateById(campus);
    }
    

    // =================================================================================
    // 3. 删除校区 (removeById) - 监听器模式
    // =================================================================================
     /* 策略：
     * 1. Service层负责检查【人员】占用。
     * 2. Controller层负责检查【楼栋】占用 (跨模块协调)。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        Long campusId = (Long) id;

        // 1. 核心种子数据物理锁
        if (Long.valueOf(1).equals(campusId)) {
            throw new ServiceException("系统警告：默认校区(ID:1)属于全局公共资源，禁止物理删除！");
        }
        
        // 2. 本地检查：是否有人员 (Server 模块内部校验)
        Long userCount = userMapper.selectCount(Wrappers.<SysOrdinaryUser>lambdaQuery()
                .eq(SysOrdinaryUser::getCampusId, campusId));
        if (userCount > 0) {
            throw new ServiceException("删除失败：该校区下尚有 " + userCount + " 名人员！请先进行人员调动。");
        }
        
        // 3. 📢 广播事件：通知其他模块 (Dorm 模块) 进行检查
        // 如果 Dorm 模块发现该校区下有楼栋，监听器会直接抛出 ServiceException，打断当前事务
        log.warn("🚨 触发校区删除审计事件，目标校区 ID: {}", campusId);
        eventPublisher.publishEvent(new CampusDeleteEvent(this, campusId));
        
        log.info("🗑️ 校区数据清理成功：ID={}", campusId);
        
        // 4. 执行删除
        return super.removeById(campusId);
    }
    
    // =================================================================================
    // 🛡️ 私有辅助方法
    // =================================================================================
    
    /**
     * 价格边界审计
     */
    private void checkPrice(BigDecimal price, String typeName) {
        if (price != null) {
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                throw new ServiceException("单价异常：" + typeName + "单价不能设置为负数！");
            }
            // 设定一个合理的单价上限 (例如 100元)，防止手滑多打 0 导致天价账单
            if (price.compareTo(new BigDecimal("100")) > 0) {
                throw new ServiceException("风险警告：" + typeName + "单价超出合理阈值(100)，请核实数据！");
            }
        }
    }
    
    /**
     * 唯一性审计：名称与编码
     */
    private void checkUnique(Long id, String name, String code) {
        if (StrUtil.isNotBlank(name)) {
            long count = this.count(new LambdaQueryWrapper<SysCampus>()
                    .eq(SysCampus::getCampusName, name)
                    .ne(id != null, SysCampus::getId, id));
            if (count > 0) throw new ServiceException("冲突：校区名称 [" + name + "] 在系统中已存在");
        }
        if (StrUtil.isNotBlank(code)) {
            long count = this.count(new LambdaQueryWrapper<SysCampus>()
                    .eq(SysCampus::getCampusCode, code)
                    .ne(id != null, SysCampus::getId, id));
            if (count > 0) throw new ServiceException("冲突：校区编码 [" + code + "] 在系统中已存在");
        }
    }
}