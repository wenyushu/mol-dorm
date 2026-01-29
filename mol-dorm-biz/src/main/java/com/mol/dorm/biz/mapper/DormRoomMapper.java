package com.mol.dorm.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.dorm.biz.entity.DormRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 宿舍房间 Mapper (CAS 防超卖加强版)
 */
@Mapper
public interface DormRoomMapper extends BaseMapper<DormRoom> {
    
    /**
     * 🟢 [修复] 原子增加当前人数 (带容量熔断保护)
     * 原理：利用 MySQL 行锁，在更新时同时校验 (current_num + count <= capacity)
     * @return 影响行数。如果返回 0，说明容量不足，更新失败。
     */
    @Update("UPDATE dorm_room SET current_num = current_num + #{count} " +
            "WHERE id = #{id} AND (current_num + #{count}) <= capacity")
    int increaseOccupancy(@Param("id") Long id, @Param("count") Integer count);
    
    /**
     * 原子减少当前人数 (防止负数)
     */
    @Update("UPDATE dorm_room SET current_num = current_num - #{count} " +
            "WHERE id = #{id} AND current_num >= #{count}")
    int decreaseOccupancy(@Param("id") Long id, @Param("count") Integer count);
}