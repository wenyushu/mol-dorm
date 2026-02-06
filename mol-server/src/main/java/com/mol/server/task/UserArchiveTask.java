package com.mol.server.task;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.server.entity.SysCollege;
import com.mol.server.entity.SysUserArchive;
import com.mol.server.enums.ArchiveTypeEnum;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import com.mol.server.mapper.SysUserArchiveMapper;
import com.mol.server.service.SysCollegeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学籍状态自动化维护任务
 * <p>
 * 🛡️ 防刁民设计 - 核心逻辑：
 * 自动识别并清理“僵尸学籍”。针对休学超过 2 年未复学的用户，
 * 执行“休学期满自动退学”流程，并进行全量数据归档。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserArchiveTask {
    
    private final SysOrdinaryUserMapper userMapper;
    private final SysUserArchiveMapper archiveMapper;
    // 注入学院服务，用于获取学院名称快照
    private final SysCollegeService collegeService;
    
    /**
     * 每天凌晨 4 点执行扫描
     */
    @Scheduled(cron = "0 0 4 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void autoCheckSuspension() {
        log.info("【系统任务】开始扫描休学超时用户...");
        
        // 1. 计算截止日期 (2年前的今天)
        LocalDate deadline = LocalDate.now().minusYears(2);
        
        // 2. 查询目标：在校状态=2(休学) 且 休学开始日期 <= 2年前
        List<SysOrdinaryUser> expiredUsers = userMapper.selectList(Wrappers.<SysOrdinaryUser>lambdaQuery()
                .eq(SysOrdinaryUser::getCampusStatus, 2) // 2=休学
                .isNotNull(SysOrdinaryUser::getSuspensionStartDate)
                .le(SysOrdinaryUser::getSuspensionStartDate, deadline));
        
        if (expiredUsers.isEmpty()) {
            log.info("【系统任务】今日无超时休学人员。");
            return;
        }
        
        log.warn("【系统任务】监测到 {} 名休学超时人员，正在执行自动退学...", expiredUsers.size());
        
        // 3. 预加载学院字典 (ID -> Name)，避免循环查库
        Map<Long, String> collegeMap = collegeService.list().stream()
                .collect(Collectors.toMap(SysCollege::getId, SysCollege::getName));
        
        // 4. 批量处理
        for (SysOrdinaryUser user : expiredUsers) {
            try {
                // A. 构建归档记录 (SysUserArchive)
                SysUserArchive archive = SysUserArchive.builder()
                        .id(user.getId()) // 复用原ID
                        .username(user.getUsername())
                        .realName(user.getRealName())
                        .userCategory(user.getUserCategory())
                        
                        // 核心字段快照
                        .phone(user.getPhone())
                        .idCard(user.getIdCard())
                        // 优先取入学年份，没有则取入职年份
                        .entryYear(user.getEnrollmentYear() != null ? user.getEnrollmentYear() : user.getEntryYear())
                        
                        // 学院名称快照 (从字典取，取不到填未知)
                        .collegeName(collegeMap.getOrDefault(user.getCollegeId(), "未知学院(ID:" + user.getCollegeId() + ")"))
                        
                        // 归档类型：休学期满自动退学 (52)
                        .archiveType(ArchiveTypeEnum.SUSPENSION_EXPIRED.getCode())
                        .archiveReason("【系统自动】休学期限已满 2 年(730天)，用户未申请复学，触发自动退学机制。")
                        
                        .operator("SYSTEM_AUTO_TASK") // 操作人：系统任务
                        .archiveTime(LocalDateTime.now())
                        
                        // 💊 后悔药：全量数据备份
                        .originalDataJson(JSONUtil.toJsonStr(user))
                        .build();
                
                // B. 插入归档表
                archiveMapper.insert(archive);
                
                // C. 更新原用户状态
                // status: 2 (已归档/删除)
                // campus_status: 4 (肄业/退学 - 假设4代表退学)
                user.setStatus("2");
                user.setCampusStatus(4);
                // 清空休学时间，防止下次重复扫描
                user.setSuspensionStartDate(null);
                user.setRemark(user.getRemark() + " -> [System]超时退学");
                
                userMapper.updateById(user);
                
                log.info("用户 [{} {}] 已成功归档。", user.getUsername(), user.getRealName());
                
            } catch (Exception e) {
                log.error("用户 [{} {}] 归档失败！", user.getUsername(), user.getRealName(), e);
                // 捕获异常，防止一个人失败导致整个任务回滚，影响其他人处理
            }
        }
        
        log.info("【系统任务】休学超时扫描结束。");
    }
}