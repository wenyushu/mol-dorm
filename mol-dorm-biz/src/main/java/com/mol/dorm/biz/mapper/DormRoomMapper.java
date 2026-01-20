package com.mol.dorm.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.dorm.biz.entity.DormRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 宿舍房间 Mapper
 * * 增加原子更新方法，防止并发下人数统计错误。
 */
@Mapper
public interface DormRoomMapper extends BaseMapper<DormRoom> {
    
    /**
     * 原子增加当前人数
     * SQL: UPDATE dorm_room SET current_num = current_num + 1 WHERE id = ?
     */
    @Update("UPDATE dorm_room SET current_num = current_num + 1 WHERE id = #{id}")
    void incrementCurrentNum(@Param("id") Long id);
    
    /**
     * 原子减少当前人数 (防止减成负数)
     * SQL: UPDATE dorm_room SET current_num = current_num - 1 WHERE id = ? AND current_num > 0
     */
    @Update("UPDATE dorm_room SET current_num = current_num - 1 WHERE id = #{id} AND current_num > 0")
    void decrementCurrentNum(@Param("id") Long id);
}