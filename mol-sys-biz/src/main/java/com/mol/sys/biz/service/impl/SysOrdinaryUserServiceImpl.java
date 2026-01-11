package com.mol.sys.biz.service.impl;

import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.common.core.exception.ServiceException;
import com.mol.sys.biz.mapper.SysOrdinaryUserMapper;
import com.mol.sys.biz.service.SysOrdinaryUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 普通用户业务实现
 * @author mol
 */
@Service
public class SysOrdinaryUserServiceImpl extends ServiceImpl<SysOrdinaryUserMapper, SysOrdinaryUser> implements SysOrdinaryUserService {
    
    // ================= 1. 新增用户 =================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveUser(SysOrdinaryUser user) {
        // 初始化默认密码
        if (StrUtil.isBlank(user.getPassword())) {
            user.setPassword(BCrypt.hashpw("123456"));
        } else {
            user.setPassword(BCrypt.hashpw(user.getPassword()));
        }
        
        // 解析身份证
        parseIdCardInfo(user);
        
        // 设置默认值
        if (user.getStatus() == null) user.setStatus("0");
        if (user.getEntryDate() == null && user.getUserCategory() != null && user.getUserCategory() == 0) {
            user.setEntryDate(LocalDate.now());
        }
        
        return super.save(user);
    }
    
    // ================= 2. 修改用户 =================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysOrdinaryUser user) {
        // 密码逻辑：如果前端传了值，就加密修改；没传(空串)，设为null，MyBatis就不会动数据库里的旧密码
        if (StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(BCrypt.hashpw(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        
        // 解析身份证
        parseIdCardInfo(user);
        
        return super.updateById(user);
    }
    
    // ================= 3. 强制重置密码 (新增) =================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, String newPassword) {
        SysOrdinaryUser user = new SysOrdinaryUser();
        user.setId(userId);
        user.setPassword(BCrypt.hashpw(newPassword));
        this.updateById(user);
    }
    
    // ================= 4. 自行修改密码 (新增) =================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        // 1. 查出当前用户
        SysOrdinaryUser user = this.getById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        
        // 2. 校验旧密码
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new ServiceException("原密码错误");
        }
        
        // 3. 更新新密码
        user.setPassword(BCrypt.hashpw(newPassword));
        this.updateById(user);
    }
    
    /**
     * 辅助方法：身份证解析
     */
    private void parseIdCardInfo(SysOrdinaryUser user) {
        String idCard = user.getIdCard();
        if (!IdcardUtil.isValidCard(idCard)) return;
        try {
            String birth = IdcardUtil.getBirthByIdCard(idCard);
            user.setBirthDate(LocalDate.parse(birth, DateTimeFormatter.ofPattern("yyyyMMdd")));
            if (StrUtil.isBlank(user.getHometown())) {
                user.setHometown(IdcardUtil.getProvinceByIdCard(idCard));
            }
            int gender = IdcardUtil.getGenderByIdCard(idCard);
            user.setSex(gender == 1 ? 1 : 2);
        } catch (Exception ignored) {}
    }
}