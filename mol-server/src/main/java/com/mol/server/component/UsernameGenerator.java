package com.mol.server.component;

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
 * 业务规则账号生成器 (Redis 分布式版) - 精简版
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
    
    // Redis Key 前缀
    private static final String ID_GEN_KEY_PREFIX = "mol:id:gen:";
    
    /**
     * 生成学生学号 (精简版)
     * 规则: 入学年份后2位 + 培养层次(1-2位) + 校区尾号(1位) + 学院尾号(1位) + 专业尾号(1位) + 流水号(4位)
     * 示例: 2024年入学, 本科(B), 北校区(ID=1), 计院(ID=1), 软工(ID=101)
     * 结果: 24 B 1 1 1 0001 -> 24B1110001 (共10位)
     *
     * @param year      入学年份
     * @param eduLevel  培养层次 (B/Z/Y/D)
     * @param collegeId 学院 ID
     * @param campusId  校区 ID
     * @param majorId   专业 ID
     * @return 唯一学号
     */
    public String generateStudentAccount(Integer year, String eduLevel, Long collegeId, Long campusId, Long majorId) {
        // 1. 参数校验与默认值
        if (year == null) year = Year.now().getValue();
        if (StrUtil.isBlank(eduLevel)) eduLevel = "B";
        if (collegeId == null) throw new ServiceException("生成学号失败：必须指定学院");
        if (campusId == null) throw new ServiceException("生成学号失败：必须指定校区");
        if (majorId == null) throw new ServiceException("生成学号失败：必须指定专业");
        
        // 2. 构造精简前缀
        // 年份后两位: 2024 -> "24"
        String yearCode = String.format("%02d", year % 100);
        
        // 校区ID最后一位: ID=1 -> "1", ID=12 -> "2"
        String campusCode = String.valueOf(campusId % 10);
        
        // 学院ID最后一位: ID=5 -> "5", ID=15 -> "5"
        String collegeCode = String.valueOf(collegeId % 10);
        
        // 专业ID最后一位: ID=101 -> "1"
        String majorCode = String.valueOf(majorId % 10);
        
        // 前缀: 24 + B + 1 + 1 + 1
        String prefix = yearCode + eduLevel.toUpperCase() + campusCode + collegeCode + majorCode;
        
        // 3. 获取原子流水号 (流水号长度 4)
        long sequence = getSequenceFromRedis(prefix, 4);
        
        // 4. 拼接 (补齐 4 位)
        return prefix + String.format("%04d", sequence);
    }
    
    /**
     * 生成教职工工号 (精简版)
     * 规则: 入职年份后2位 + "JZG" + 合同年限(1位) + 校区尾号(1位) + 部门后2位 + 流水号(6位)
     * 示例: 2020年入职, 3年合同, 北校区(ID=1), 后勤处(ID=2)
     * 结果: 20 JZG 3 1 02 000001 -> 20JZG3102000001 (共15位)
     *
     * @param year          入职年份
     * @param contractYears 合同年限
     * @param campusId      校区 ID
     * @param deptId        部门 ID
     * @return 唯一工号
     */
    public String generateStaffAccount(Integer year, Integer contractYears, Long campusId, Long deptId) {
        if (year == null) year = Year.now().getValue();
        if (contractYears == null) contractYears = 1;
        if (campusId == null) throw new ServiceException("生成工号失败：必须指定校区");
        if (deptId == null) throw new ServiceException("生成工号失败：必须指定部门");
        
        // 1. 构造精简前缀
        // 年份后两位
        String yearCode = String.format("%02d", year % 100);
        
        // 合同年限取最后一位 (防止输入 10 年导致格式错乱)
        String contractCode = String.valueOf(contractYears % 10);
        
        // 校区ID最后一位
        String campusCode = String.valueOf(campusId % 10);
        
        // 部门ID后两位: ID=2 -> "02", ID=105 -> "05"
        String deptCode = String.format("%02d", deptId % 100);
        
        // 前缀: 20 + JZG + 3 + 1 + 02
        String prefix = yearCode + "JZG" + contractCode + campusCode + deptCode;
        
        // 2. 获取原子流水号 (流水号长度 6)
        long sequence = getSequenceFromRedis(prefix, 6);
        
        // 3. 拼接 (补齐 6 位)
        return prefix + String.format("%06d", sequence);
    }
    
    /**
     * 【核心算法】从 Redis 获取分布式原子序列
     */
    private long getSequenceFromRedis(String prefix, int length) {
        String key = ID_GEN_KEY_PREFIX + prefix;
        
        Long seq = redisTemplate.opsForValue().increment(key);
        
        // 数据库兜底逻辑
        if (seq != null && seq == 1) {
            String maxUsername = userMapper.selectMaxUsernameByPrefix(prefix);
            
            if (StrUtil.isNotBlank(maxUsername)) {
                try {
                    // 动态截取后缀长度
                    String suffix = StrUtil.subSuf(maxUsername, maxUsername.length() - length);
                    long dbMaxSeq = Long.parseLong(suffix);
                    long nextSeq = dbMaxSeq + 1;
                    
                    redisTemplate.opsForValue().set(key, String.valueOf(nextSeq));
                    redisTemplate.expire(key, 365, TimeUnit.DAYS);
                    
                    log.info("Redis 缓存校准: {} -> 数据库最大值 {}, 重置为 {}", prefix, dbMaxSeq, nextSeq);
                    return nextSeq;
                } catch (Exception e) {
                    log.error("账号流水号解析异常: {} (期望后缀长度: {})", maxUsername, length);
                }
            }
        }
        
        redisTemplate.expire(key, 365, TimeUnit.DAYS);
        return seq != null ? seq : 1;
    }
}