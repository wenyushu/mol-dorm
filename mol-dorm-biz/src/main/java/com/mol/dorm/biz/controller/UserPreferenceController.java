package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.mol.common.core.constant.RoleConstants;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.UserPreference;
import com.mol.dorm.biz.service.UserPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户画像偏好控制层
 */
@Tag(name = "用户画像", description = "学生生活习惯与教工偏好录入")
@RestController
@RequestMapping("/dorm/preference")
@RequiredArgsConstructor
public class UserPreferenceController {
    
    private final UserPreferenceService preferenceService;
    
    /**
     * 获取当前登录人的画像
     */
    @Operation(summary = "获取个人偏好详情")
    @GetMapping("/my")
    public R<UserPreference> getMyPreference() {
        Long userId = StpUtil.getLoginIdAsLong();
        UserPreference pref = preferenceService.getById(userId);
        // 🛡️ [防崩溃逻辑]：如果用户第一次进还没填，返回一个初始对象，带上 ID 即可
        if (pref == null) {
            pref = new UserPreference();
            pref.setUserId(userId);
            pref.setProfileStatus(0);
        }
        return R.ok(pref);
    }
    
    /**
     * 提交/更新个人画像
     * 🛡️ [防刁民逻辑]：
     * 调用 saveOrUpdatePreference 触发 Service 层的【悖论审计】。
     * 防止出现“社恐却要带异性回寝”这种恶意填报。
     */
    @Operation(summary = "提交个人画像")
    @PostMapping("/submit")
    public R<Void> submitMyPreference(@RequestBody UserPreference preference) {
        // 强制锁定当前用户ID，防止抓包修改他人 ID
        preference.setUserId(StpUtil.getLoginIdAsLong());
        
        // 🟢 修改点：使用咱们加固后的 Service 方法，执行深度审计
        preferenceService.saveOrUpdatePreference(preference);
        return R.okMsg("画像已通过悖论审计，您的习惯已被收录");
    }
    
    /**
     * 管理员代填/修改画像
     */
    @Operation(summary = "管理员代填画像")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @PutMapping("/admin-update")
    public R<Void> adminUpdate(@RequestBody UserPreference preference) {
        if (preference.getUserId() == null) {
            return R.fail("操作拦截：必须指定目标用户 ID");
        }
        // 🟢 管理员修改也必须经过审计，防止管理员录入自相矛盾的数据
        preferenceService.saveOrUpdatePreference(preference);
        return R.okMsg("管理员强制修正画像成功");
    }
    
    /**
     * 重置画像状态
     */
    @Operation(summary = "重置画像状态", description = "将画像设为未完成，使其暂时退出智能分配池。")
    @SaCheckRole(value = {RoleConstants.SUPER_ADMIN, RoleConstants.DORM_MANAGER}, mode = SaMode.OR)
    @DeleteMapping("/reset/{userId}")
    public R<Void> resetPreference(@PathVariable Long userId) {
        UserPreference p = new UserPreference();
        p.setUserId(userId);
        p.setProfileStatus(0); // 设为未完成，算法将自动跳过此人
        preferenceService.updateById(p);
        return R.okMsg("该用户画像已被管理员打回，需重新填报");
    }
}