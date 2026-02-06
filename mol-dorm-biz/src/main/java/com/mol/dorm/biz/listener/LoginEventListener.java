package com.mol.dorm.biz.listener;

import cn.dev33.satoken.listener.SaTokenListener;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.mol.common.core.entity.SysAdminUser;
import com.mol.server.mapper.SysAdminUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 业务模块专属：登录增强监听器
 * 🛡️ [解耦逻辑]：
 * 1. 自动识别管理员/宿管身份，动态注入 buildingId。
 * 2. 自动审计：记录管理员最后一次登录时间，用于账号活跃度分析。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginEventListener implements SaTokenListener {
    
    private final SysAdminUserMapper adminUserMapper;
    
    /** 登录成功回调 */
    @Override
    public void doLogin(String loginType, Object loginId, String tokenValue, SaLoginParameter loginParameter) {
        String idStr = loginId.toString();
        
        // 判定是否为管理员/宿管 (ID 前缀为 0:)
        if (idStr.startsWith("0:")) {
            Long userId = Long.parseLong(idStr.split(":")[1]);
            SysAdminUser admin = adminUserMapper.selectById(userId);
            
            if (admin != null) {
                // A. 注入管辖权：供 LoginHelper 跨模块调用
                if (admin.getBuildingId() != null) {
                    SaSession session = StpUtil.getSessionByLoginId(loginId);
                    session.set("buildingId", admin.getBuildingId());
                }
                
                // B. 动态审计：更新最后登录时间 (对应实体类中的 lastLoginTime 字段)
                admin.setLastLoginTime(LocalDateTime.now());
                adminUserMapper.updateById(admin);
                
                log.info("🚀 [权限注入&审计] 管理员 {} 登录成功，最后登录时间已记录", admin.getRealName());
            }
        }
    }
    
    /** 修正：Sa-Token 新版要求的 4 参数签名 */
    @Override
    public void doRenewTimeout(String loginType, Object loginId, String tokenValue, long timeout) {
        // 续期操作通常不需要额外业务逻辑，保持空实现即可
    }
    
    // --- 补全其他方法 ---
    @Override public void doLogout(String loginType, Object loginId, String tokenValue) {}
    @Override public void doKickout(String loginType, Object loginId, String tokenValue) {}
    @Override public void doReplaced(String loginType, Object loginId, String tokenValue) {}
    @Override public void doDisable(String loginType, Object loginId, String service, int level, long disableTime) {}
    @Override public void doUntieDisable(String loginType, Object loginId, String service) {}
    @Override public void doOpenSafe(String loginType, String tokenValue, String service, long safeTime) {}
    @Override public void doCloseSafe(String loginType, String tokenValue, String service) {}
    @Override public void doCreateSession(String id) {}
    @Override public void doLogoutSession(String id) {}
}