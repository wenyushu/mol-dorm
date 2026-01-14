package com.mol.dorm.biz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.dorm.biz.entity.DormFloor;
import com.mol.dorm.biz.mapper.DormFloorMapper;
import com.mol.dorm.biz.service.DormFloorService;
import org.springframework.stereotype.Service;

@Service
public class DormFloorServiceImpl extends ServiceImpl<DormFloorMapper, DormFloor> implements DormFloorService {
}