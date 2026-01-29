package com.mol.server.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.server.enums.ArchiveTypeEnum;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import com.mol.server.entity.SysUserArchive;
import cn.hutool.json.JSONUtil;
import com.mol.server.mapper.SysUserArchiveMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserArchiveTask {
    
    private final SysOrdinaryUserMapper userMapper;
    private final SysUserArchiveMapper archiveMapper;
    
    /**
     * 🛡️ 防刁民自动任务：每天凌晨4点扫描休学超时用户
     * 逻辑：如果休学时间超过 2 年(730天)，自动转为"自动退学"，彻底断绝保留学籍的可能。
     */
    @Scheduled(cron = "0 0 4 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void autoCheckSuspension() {
        log.info("开始扫描休学超时用户...");
        
        // 1. 计算2年前的日期
        LocalDate twoYearsAgo = LocalDate.now().minusYears(2);
        
        // 2. 查询：状态为归档(2) AND 有休学开始时间 AND 开始时间早于2年前
        List<SysOrdinaryUser> expiredUsers = userMapper.selectList(Wrappers.<SysOrdinaryUser>lambdaQuery()
                .eq(SysOrdinaryUser::getStatus, "2")
                .isNotNull(SysOrdinaryUser::getSuspensionStartDate)
                .le(SysOrdinaryUser::getSuspensionStartDate, twoYearsAgo));
        
        for (SysOrdinaryUser user : expiredUsers) {
            log.warn("用户[{}]休学已超过 2 年，执行自动退学处理。", user.getRealName());
            
            // A. 追加归档记录
            SysUserArchive archive = SysUserArchive.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .realName(user.getRealName())
                    .archiveType(ArchiveTypeEnum.SUSPENSION_EXPIRED.getCode()) // 52
                    .archiveReason("系统自动处理：休学超过 2 年未复学")
                    .operator("SYSTEM_TASK")
                    .archiveTime(LocalDateTime.now())
                    .originalDataJson(JSONUtil.toJsonStr(user))
                    .build();
            archiveMapper.insert(archive);
            
            // B. 清除休学时间标记 (意味着流程终结，不可自动恢复)
            user.setSuspensionStartDate(null);
            user.setRemark(user.getRemark() + " -> [系统]休学超时退学");
            
            userMapper.updateById(user);
        }
        
        if (!expiredUsers.isEmpty()) {
            log.info("休学超时扫描结束，共处理 {} 人", expiredUsers.size());
        }
    }
}