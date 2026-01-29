package com.mol.server.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.event.UserArchiveEvent;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.entity.SysUserArchive;
import com.mol.server.enums.ArchiveTypeEnum;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import com.mol.server.mapper.SysUserArchiveMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserArchiveService {
    
    private final SysOrdinaryUserMapper userMapper;
    private final SysUserArchiveMapper archiveMapper;
    // 注入事件发布器
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * 执行人员异动/归档处理
     */
    @Transactional(rollbackFor = Exception.class)
    public void executeUserArchive(Long userId, ArchiveTypeEnum typeEnum, String reason, String operator) {
        // 1. 🛡️ 参数与状态检查
        if (userId == null || typeEnum == null) throw new ServiceException("归档参数缺失");
        if (StrUtil.isBlank(reason)) throw new ServiceException("必须填写异动/归档原因");
        
        SysOrdinaryUser user = userMapper.selectById(userId);
        if (user == null) throw new ServiceException("用户不存在");
        if ("2".equals(user.getStatus())) throw new ServiceException("该用户已处于归档状态");
        
        log.info("管理员[{}] 对用户[{}] 执行 [{}] 操作", operator, user.getRealName(), typeEnum.getDesc());
        
        // 2. 💾 数据备份 (存入归档表)
        SysUserArchive archive = SysUserArchive.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .userCategory(user.getUserCategory())
                .phone(user.getPhone())
                .idCard(user.getIdCard())
                .entryYear(user.getEnrollmentYear())
                .archiveType(typeEnum.getCode())
                .archiveReason(reason)
                .operator(operator)
                .archiveTime(LocalDateTime.now())
                .originalDataJson(JSONUtil.toJsonStr(user))
                .build();
        archiveMapper.insert(archive);
        
        // 3. 📢 发送事件广播 (核心解耦点)
        // 通知其他模块：这个用户归档了，你们该清理资源的赶紧清理 (比如宿舍模块释放床位)
        eventPublisher.publishEvent(new UserArchiveEvent(this, userId, typeEnum.getCode()));
        
        // 4. 🔒 冻结原账号
        user.setStatus("2"); // 2: 已归档/停用
        user.setCampusId(null);
        user.setClassId(null);
        user.setMajorId(null);
        user.setRemark(StrUtil.format("【{}】{}", typeEnum.getDesc(), reason));
        
        // 休学特殊处理：记录开始时间
        if (typeEnum == ArchiveTypeEnum.SUSPENSION_MEDICAL || typeEnum == ArchiveTypeEnum.SUSPENSION_PERSONAL) {
            user.setSuspensionStartDate(LocalDate.now());
        } else {
            user.setSuspensionStartDate(null);
        }
        
        userMapper.updateById(user);
    }
}