package com.mol.dorm.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.dorm.biz.entity.UserPreference;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 用户全维度画像 Mapper 接口
 * 🛡️ [算法弹药库]：支撑智能分配算法的深度联表查询
 */
@Mapper
public interface UserPreferenceMapper extends BaseMapper<UserPreference> {
    
    /**
     * [算法核心] 加载全维度画像及用户信息
     * 对齐系统标准 1男 2女
     */
    List<UserPreference> selectFullProfileForAllocation(@Param("campusId") Long campusId,
                                                        @Param("gender") Integer gender);
    
    /**
     * 适配学生(0)与教工(1)的未分配画像查询
     */
    List<UserPreference> selectUnallocatedByRole(@Param("campusId") Long campusId,
                                                 @Param("gender") Integer gender,
                                                 @Param("userCategory") Integer userCategory);
    
    /**
     * 专项查询：教职工特殊需求画像
     * 🛡️ [防刁民逻辑]：专门检索有[胰岛素需求]、[残疾]、[传染病]等红线特征的人群。
     */
    List<UserPreference> selectStaffWithSpecialNeeds(@Param("campusId") Long campusId);
}