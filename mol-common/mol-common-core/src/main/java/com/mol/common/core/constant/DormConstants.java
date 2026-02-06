package com.mol.common.core.constant;

/**
 * 宿舍系统标准化状态码集 - 协议层
 * 🛡️ 极其严格设计：生命周期、性别、资源、用途 四维解耦
 * 注：此处定义为常量，主要用于实体类字段注释、Swagger注解及注解参数。
 *
 * @author mol
 */
public interface DormConstants {
    
    /** 1. 生命周期 (Lifecycle - 对应数据库 status 字段) */
    int LC_STOP = 0;           // 停止使用 (已废弃/逻辑注销)
    int LC_UNUSED = 10;        // 未使用 (初始化完成，尚未上线)
    int LC_NORMAL = 20;        // 正常使用 (业务承载中)
    int LC_PAUSE = 30;         // 暂停使用 (如暑假封楼、临时征用)
    int LC_DECORATING = 40;    // 装修中 (暂停使用)
    int LC_REPAIRING = 50;     // 维修中 (暂停使用)
    int LC_DAMAGED = 60;       // 已损坏 (暂停使用)
    int LC_RESERVED = 80;      // 预留/保留 (行政预留，不参与自动分配)
    
    /** 2. 性别区分 (Gender - 对应 genderLimit/gender 字段) */
    int GENDER_MALE = 1;       // 男
    int GENDER_FEMALE = 2;     // 女
    int GENDER_MIXED = 3;      // 混合 (仅用于楼栋级)
    
    /** 3. 资源状态码 (Resource Status - 对应 resource_status 字段) */
    int RES_EMPTY = 21;        // 完全空闲 (当前人数 = 0)
    int RES_USING = 22;        // 正常使用 (专门用于床位：已住人)
    int RES_NOT_FULL = 23;     // 未满员 (针对房间：0 < 人数 < 满员)
    int RES_SUFFICIENT = 24;   // 资源充裕 (入住率 < 60%)
    int RES_TENSE = 25;        // 资源紧张 (入住率 >= 85%)
    int RES_FULL = 26;         // 已满员 (入住率 = 100%)
    
    /** 4. 用途类型 (Usage - 对应 usageType 字段) */
    int USAGE_STUDENT = 0;     // 学生宿舍
    int USAGE_TEACHER = 1;     // 教职工公寓
}