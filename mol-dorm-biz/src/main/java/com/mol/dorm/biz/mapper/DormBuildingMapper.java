package com.mol.dorm.biz.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mol.dorm.biz.entity.DormBuilding;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宿舍楼 Mapper
 * 继承 MyBatis-Plus BaseMapper，自动拥有基础 CRUD 能力
 */
@Mapper
public interface DormBuildingMapper extends BaseMapper<DormBuilding> {

}