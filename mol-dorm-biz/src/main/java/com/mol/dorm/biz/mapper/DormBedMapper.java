package com.mol.dorm.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.dorm.biz.entity.DormBed;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 宿舍床位 Mapper 接口
 */
@Mapper
public interface DormBedMapper extends BaseMapper<DormBed> {
    
    /**
     * CAS (Compare-And-Swap) 乐观锁更新床位
     * 核心逻辑：只有当 occupant_id 为 NULL (即当前为空闲) 时，才执行更新。
     * 这能有效防止两个管理员同时把两个人分配到同一个床位的并发问题。
     *
     * @param bedId  床位主键 ID
     * @param userId 入住用户 ID
     * @return 受影响行数 (1表示成功抢到，0表示已被别人抢先占用或床位不存在)
     */
    @Update("UPDATE dorm_bed SET occupant_id = #{userId} WHERE id = #{bedId} AND occupant_id IS NULL")
    int assignBed(@Param("bedId") Long bedId, @Param("userId") Long userId);
}