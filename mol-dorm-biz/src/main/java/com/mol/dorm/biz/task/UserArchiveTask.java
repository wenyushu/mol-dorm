package com.mol.dorm.biz.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.dorm.biz.entity.DormRoom;
import com.mol.dorm.biz.entity.SysDormLog;
import com.mol.dorm.biz.mapper.DormRoomMapper;
import com.mol.dorm.biz.mapper.SysDormLogMapper;
import com.mol.server.entity.SysCollege;
import com.mol.server.entity.SysUserArchive;
import com.mol.server.mapper.SysClassMapper;
import com.mol.server.mapper.SysOrdinaryUserMapper;
import com.mol.server.mapper.SysUserArchiveMapper;
import com.mol.server.service.SysCollegeService;
import com.mol.server.vo.SysClassVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 🎓 宿舍人员档案自动化清算引擎
 * 🛡️ [防刁民/全链路审计版]：
 * 1. 业务审计：区分“休学超时”与“正常毕业”。
 * 2. 资料封存：全量 JSON 快照保存到归档表。
 * 3. 资产回收：物理腾空宿舍 ID，并在日志表记录流水。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserArchiveTask {
    
    private final SysOrdinaryUserMapper userMapper;
    private final SysUserArchiveMapper archiveMapper;
    private final SysCollegeService collegeService;
    private final SysDormLogMapper dormLogMapper; // ✨ 新注入：床位异动流水日志
    
    private final DormRoomMapper dormRoomMapper;
    private final SysClassMapper classMapper;
    
    /**
     * [总控制台] 每天凌晨 4 点执行
     */
    @Scheduled(cron = "0 0 4 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void runArchiveEngine() {
        
        log.info("【清算引擎】开始审计... 🔍");
        
        // 1. 预加载学院字典快照
        Map<Long, String> collegeMap = collegeService.list().stream()
                .collect(Collectors.toMap(SysCollege::getId, SysCollege::getName));
        
        // 2. 业务 A：扫描休学超时人员 (满2年未复学)
        checkSuspensionExpired(collegeMap);
        
        // 3. 业务 B：扫描应届毕业生 (预计毕业年 <= 今年)
        checkGraduationWithBuffer(collegeMap);
        
        log.info("【清算引擎】审计结束。 ✅");
    }
    
    /**
     * 🛡️ 业务 A：休学超时自动核销
     */
    private void checkSuspensionExpired(Map<Long, String> collegeMap) {
        
        LocalDateTime twoYearsAgo = LocalDateTime.now().minusYears(2);
        List<SysOrdinaryUser> suspensionExpired = userMapper.selectList(Wrappers.<SysOrdinaryUser>lambdaQuery()
                .eq(SysOrdinaryUser::getCampusStatus, 2) // 2-休学
                .le(SysOrdinaryUser::getSuspensionStartDate, twoYearsAgo));
        
        if (CollUtil.isNotEmpty(suspensionExpired)) {
            doProcessArchive(suspensionExpired, "休学逾期自动核销", 52, collegeMap);
        }
    }
    
    /**
     * 🛡️ 业务 B：应届毕业动态核销
     */
    private void checkGraduationWithBuffer(Map<Long, String> collegeMap) {
        
        int currentYear = LocalDateTime.now().getYear();
        
        // 排除掉入伍(3)、延迟毕业(4)等正在受保护的状态
        List<SysOrdinaryUser> students = userMapper.selectList(Wrappers.<SysOrdinaryUser>lambdaQuery()
                .eq(SysOrdinaryUser::getUserCategory, 0)
                .eq(SysOrdinaryUser::getStatus, "0")
                .eq(SysOrdinaryUser::getCampusStatus, 0)
                .isNotNull(SysOrdinaryUser::getEnrollmentYear)
                .isNotNull(SysOrdinaryUser::getContractYear));
        
        List<SysOrdinaryUser> realGraduates = students.stream().filter(u -> {
            int retained = u.getRetainedYears() != null ? u.getRetainedYears() : 0;
            int suspension = u.getSuspensionYears() != null ? u.getSuspensionYears() : 0;
            // 公式：入学 + 学制 + 留级补偿 + 休学补偿 <= 今年
            return (u.getEnrollmentYear() + u.getContractYear() + retained + suspension) <= currentYear;
        }).collect(Collectors.toList());
        
        if (CollUtil.isNotEmpty(realGraduates)) {
            doProcessArchive(realGraduates, "学制年限届满清算", 60, collegeMap);
        }
    }
    
    /**
     * [归档核心处理器]
     * 🛡️ 逻辑：归档资料 -> 记录床位日志 -> 物理清算
     */
    private void doProcessArchive(List<SysOrdinaryUser> users, String reason, Integer typeCode, Map<Long, String> collegeMap) {
        
        for (SysOrdinaryUser user : users) {
            try {
                // 1. ✨ 生成全量档案快照 (JSON 后悔药)
                SysUserArchive archive = SysUserArchive.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .realName(user.getRealName())
                        .idCard(user.getIdCard())
                        .phone(user.getPhone())
                        .userCategory(user.getUserCategory())
                        .collegeName(collegeMap.getOrDefault(user.getCollegeId(), "未知学院"))
                        .archiveType(typeCode)
                        .archiveReason("【系统审计】" + reason)
                        .archiveTime(LocalDateTime.now())
                        .operator("SYSTEM_AUTO_TASK")
                        .originalDataJson(JSONUtil.toJsonStr(user))
                        .build();
                archiveMapper.insert(archive);
                
                // 2. ✨ 全维度宿舍日志 (精准适配 DormRoom 版)
                if (user.getDormId() != null) {
                    // 🛡️ 适配你的实体名 DormRoom
                    DormRoom room = dormRoomMapper.selectById(user.getDormId());
                    SysClassVO classVO = classMapper.selectClassVoById(user.getClassId());
                    
                    SysDormLog dormLog = SysDormLog.builder()
                            .userId(user.getId())
                            .username(user.getUsername())
                            .realName(user.getRealName())
                            .campusName(collegeMap.getOrDefault(user.getCampusId(), "未知校区"))
                            .collegeName(collegeMap.getOrDefault(user.getCollegeId(), "未知学院"))
                            .majorName(classVO != null ? classVO.getMajorName() : "未知专业")
                            // ✨ 修正点：使用 String.valueOf() 将 Integer 转为 String，并拼接“级”字
                            .grade(classVO != null && classVO.getGrade() != null ? classVO.getGrade() + "级" : "未知年级")
                            .className(classVO != null ? classVO.getClassName() : "未知班级")
                            
                            // 🏨 宿舍快照：精准对齐 DormRoom 实体字段
                            .oldDormId(user.getDormId())
                            .dormCampus(collegeMap.getOrDefault(room != null ? room.getCampusId() : null, "未知"))
                            .buildingName(room != null ? String.valueOf(room.getBuildingId()) : "未知")
                            .floorNum(room != null ? room.getFloorNum() : 0)
                            .roomName(room != null ? room.getRoomNo() : "未知")
                            
                            // 🛡️ 床位处理：解决类型不兼容。无论 user 里的字段叫 bedId 还是 bedNum，统一转 String
                            // 如果报错，请将 getBedId 改为你 SysOrdinaryUser 里真实的床位字段名
                            .bedName(user.getDormId() != null ? "已分配床位" : "无")
                            
                            .logType("AUTO_RELEASE")
                            .content("系统清算退宿。原因：" + reason)
                            .createTime(LocalDateTime.now())
                            .build();
                    
                    dormLogMapper.insert(dormLog);
                    
                    // 🛡️ [资产同步]：既然人走了，记得调用 Mapper 里的原子更新，把房间的人数 -1
                    dormRoomMapper.increaseOccupancy(user.getDormId(), -1);
                }
                
                // 3. ✨ 物理核销用户状态
                user.setStatus("2");      // 2-已离校归档
                user.setCampusStatus(4); // 4-已核销注销
                user.setDormId(null);    // 腾空床位！
                user.setRemark(user.getRemark() + " | " + DateUtil.now() + " 清算: " + reason);
                
                userMapper.updateById(user);
                
                log.info("【清算成功】人员 [{} {}] 已归档并释放宿舍资源。", user.getUsername(), user.getRealName());
            } catch (Exception e) {
                log.error("【清算失败】处理用户 {} 时发生异常", user.getRealName(), e);
            }
        }
    }
}