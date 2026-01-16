package com.mol.server.service;

import cn.hutool.core.util.StrUtil;
import com.mol.common.core.exception.ServiceException;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.concurrent.TimeUnit;

/**
 * 业务规则账号生成器 (Redis 分布式版)
 * <p>
 * 负责生成全局唯一的学号和工号。
 * 策略：Redis AtomicIncrement (高性能) + DB Max Query (高可靠)
 * </p>
 *
 * @author mol
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UsernameGenerator {
    
    private final SysOrdinaryUserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    
    // Redis Key 前缀，用于隔离业务数据
    private static final String ID_GEN_KEY_PREFIX = "mol:id:gen:";
    
    /**
     * 生成学生学号
     * 规则: 入学年份(4) + 培养层次(1-2) + 学院ID(2) + 流水号(4)
     * 示例: 2026B050001
     *
     * @param year      入学年份 (如 2026)
     * @param eduLevel  培养层次 (Z/B/ZB/Y/D)
     * @param collegeId 学院 ID (如 1, 2, 5)
     * @return 11位+ 的唯一学号
     */
    public String generateStudentAccount(Integer year, String eduLevel, Long collegeId) {
        // 1. 参数校验与默认值
        if (year == null) year = Year.now().getValue();
        if (StrUtil.isBlank(eduLevel)) eduLevel = "B"; // 默认为本科
        if (collegeId == null) throw new ServiceException("生成学号失败：必须指定学院");
        
        // 2. 构造业务前缀
        // 格式化学院ID为2位，例如 ID=5 -> "05", ID=12 -> "12"
        String collegeCode = String.format("%02d", collegeId % 100);
        
        // 前缀 = 2026 + B + 05 -> "2026B05"
        String prefix = year + eduLevel.toUpperCase() + collegeCode;
        
        // 3. 获取原子流水号
        long sequence = getSequenceFromRedis(prefix);
        
        // 4. 拼接最终结果 (流水号补齐4位，如 1 -> 0001)
        return prefix + String.format("%04d", sequence);
    }
    
    /**
     * 生成教职工工号
     * 规则: 入职年份(4) + "JZG" + 部门ID(2) + 流水号(4)
     * 示例: 2020JZG010002
     *
     * @param year   入职年份
     * @param deptId 部门 ID
     * @return 唯一工号
     */
    public String generateStaffAccount(Integer year, Long deptId) {
        if (year == null) year = Year.now().getValue();
        if (deptId == null) throw new ServiceException("生成工号失败：必须指定部门");
        
        // 1. 构造前缀
        String deptCode = String.format("%02d", deptId % 100);
        
        // 前缀 = 2020 + JZG + 01 -> "2020JZG01"
        String prefix = year + "JZG" + deptCode;
        
        // 2. 获取原子流水号
        long sequence = getSequenceFromRedis(prefix);
        
        // 3. 拼接
        return prefix + String.format("%04d", sequence);
    }
    
    /**
     * 【核心算法】从 Redis 获取分布式原子序列
     * 包含"数据库兜底"逻辑，防止 Redis 数据丢失导致 ID 重复
     *
     * @param prefix 业务前缀 (如 2026B05)
     * @return 下一个可用的序列号
     */
    private long getSequenceFromRedis(String prefix) {
        String key = ID_GEN_KEY_PREFIX + prefix;
        
        // 1. Redis 原子自增
        // 如果 key 不存在，Redis 会将其初始化为 0 再加 1，返回 1
        Long seq = redisTemplate.opsForValue().increment(key);
        
        // 2. 安全检查：如果是 1，说明 Key 是新创建的（或者是 Redis 重启/过期了）
        // 此时必须查数据库，确认是不是真的没有旧数据，防止 ID 重突
        if (seq != null && seq == 1) {
            // 查库：SELECT max(username) ... LIKE '2026B05%'
            String maxUsername = userMapper.selectMaxUsernameByPrefix(prefix);
            
            if (StrUtil.isNotBlank(maxUsername)) {
                // 数据库里已经有数据了 (例如 2026B050045)
                // 提取后4位流水号 -> 45
                // 注意：这里要处理可能的解析异常
                try {
                    // 截取最后4位
                    String suffix = StrUtil.subSuf(maxUsername, maxUsername.length() - 4);
                    long dbMaxSeq = Long.parseLong(suffix);
                    
                    // 修正 Redis 的值：设为 dbMaxSeq + 1 (即 46)
                    long nextSeq = dbMaxSeq + 1;
                    redisTemplate.opsForValue().set(key, String.valueOf(nextSeq));
                    
                    log.info("检测到 Redis 缓存丢失，已根据数据库修正流水号: {} -> {}", prefix, nextSeq);
                    return nextSeq;
                } catch (NumberFormatException e) {
                    log.error("现有账号格式异常，无法解析流水号: {}", maxUsername);
                    // 如果解析失败，维持 1，可能存在风险，但在规范数据下不会发生
                }
            }
        }
        
        // 3. 设置过期时间
        // 设为 365 天，避免永久占用内存。明年会有新的前缀，旧前缀可以过期。
        redisTemplate.expire(key, 365, TimeUnit.DAYS);
        
        return seq != null ? seq : 1;
    }
}