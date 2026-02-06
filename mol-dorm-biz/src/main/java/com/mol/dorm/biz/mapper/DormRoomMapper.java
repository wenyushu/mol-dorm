package com.mol.dorm.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.dorm.biz.entity.DormRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 房间 Mapper 接口
 * 🛡️ [防刁民]：支持资源饱和度优先分配算法
 */
@Mapper
public interface DormRoomMapper extends BaseMapper<DormRoom> {
    
    /**
     * 智能搜索可用房源
     * @param gender 房间限定性别
     * @param usageType 用途类型 (学生/教工)
     * 🛡️ 排序规则：资源饱和度降序，优先填满已有人的房间
     */
    List<DormRoom> findAvailableRooms(@Param("buildingId") Long buildingId,
                                      @Param("gender") String gender,
                                      @Param("usageType") Integer usageType);
    
    /**
     * 统计指定范围内的饱和度状态分布 (用于可视化大屏)
     */
    List<Map<String, Object>> selectRoomStatusStats(@Param("buildingId") Long buildingId,
                                                    @Param("campusId") Long campusId);
    
    /**
     * 🛡️ [反查逻辑]：根据学生 ID 穿透床位表获取其所属房间信息
     * 用于学生端小程序“我的宿舍”看板
     */
    DormRoom selectByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 获取房间物理详情 (带楼栋名称)
     */
    Map<String, Object> selectRoomDetail(@Param("roomId") Long roomId);
    
    /**
     * 📊 [资产看板]：统计指定房间内各状态资产的数量
     * 用于 evaluateRoomSafety 判定是否需要熔断下架
     */
    List<Map<String, Object>> countStatusByRoom(@Param("roomId") Long roomId);
    
    /**
     * SQL 原子更新房间的人数
     * 防止 Java 内存计算导致的人数超卖
     */
    @Update("UPDATE dorm_room SET current_num = current_num + #{count} " +
            "WHERE id = #{id} AND (current_num + #{count}) <= capacity")
    int increaseOccupancy(@Param("id") Long id, @Param("count") Integer count);
}