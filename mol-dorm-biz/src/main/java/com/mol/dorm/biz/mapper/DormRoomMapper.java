package com.mol.dorm.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.dorm.biz.entity.DormRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 宿舍房间 Mapper
 * 核心功能：提供基于 SQL 的原子更新能力，防止并发超卖。
 */
@Mapper
public interface DormRoomMapper extends BaseMapper<DormRoom> {
    
    /**
     * 原子增加当前人数 (支持批量)
     * 场景：分配宿舍时，一次性增加 N 人
     * SQL: UPDATE dorm_room SET current_num = current_num + #{count} WHERE id = #{id}
     */
    @Update("UPDATE dorm_room SET current_num = current_num + #{count} WHERE id = #{id}")
    int increaseOccupancy(@Param("id") Long id, @Param("count") Integer count);
    
    /**
     * 原子减少当前人数 (支持批量，且防止减成负数)
     * 场景：退宿/调宿时，一次性减少 N 人
     * SQL: UPDATE dorm_room SET current_num = current_num - #{count} WHERE id = #{id} AND current_num >= #{count}
     */
    @Update("UPDATE dorm_room SET current_num = current_num - #{count} WHERE id = #{id} AND current_num >= #{count}")
    int decreaseOccupancy(@Param("id") Long id, @Param("count") Integer count);
}