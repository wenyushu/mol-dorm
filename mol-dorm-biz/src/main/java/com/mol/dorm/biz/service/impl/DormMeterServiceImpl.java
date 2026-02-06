package com.mol.dorm.biz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mol.dorm.biz.entity.DormMeter;
import com.mol.dorm.biz.mapper.DormMeterMapper;
import com.mol.dorm.biz.service.DormMeterService;
import org.springframework.stereotype.Service;

@Service
public class DormMeterServiceImpl extends ServiceImpl<DormMeterMapper, DormMeter> implements DormMeterService {
    // 后续可以在这里写：远程抄表、远程合闸/跳闸 的逻辑
}