package com.mol.dorm.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.dorm.biz.entity.DormBed;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 床位 Mapper 接口
 * 🛡️ [防刁民]：实现全校范围内的“一人一床”与“毕业清算”审计
 */
@Mapper
public interface DormBedMapper extends BaseMapper<DormBed> {
    
    /**
     * [物理审计] 统计房间内真实在住人数
     * 🛡️ 绕过冗余字段，直接查 occupant_id 的物理计数
     */
    Integer countRealOccupants(@Param("roomId") Long roomId);
    
    /**
     * [毕业季脚本] 根据入学年级找床位
     * 用于一键清退，防止毕业学生继续占用床位
     */
    List<Long> selectBedIdsByGraduateGrade(@Param("grade") Integer grade);
    
    /**
     * 查询房间内可分配的“净空”床位
     */
    List<DormBed> findEmptyBedsInRoom(@Param("roomId") Long roomId);
    
    /**
     * [防刁民审计]：全校范围扫描用户是否已占用床位
     * @param userId 用户ID
     * @return 冲突的床位ID，若无则返回 null
     */
    @Select("SELECT id FROM dorm_bed WHERE occupant_id = #{userId} AND del_flag = '0' LIMIT 1")
    Long checkUserHasBed(@Param("userId") Long userId);
}