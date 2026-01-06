package com.mol.common.core.constant;

/**
 * 全局公共常量类
 * 定义系统中通用的状态标识，减少硬编码
 *
 * @author mol
 */
public interface CommonConstants {
    
    /**
     * 业务执行成功标识：0
     * 遵循多数国产框架约定，0 代表成功
     */
    Integer SUCCESS = 0;
    
    /**
     * 业务执行失败标识：1
     */
    Integer FAIL = 1;
    
    /**
     * 逻辑删除标识：0-正常
     */
    String DB_NOT_DELETED = "0";
    
    /**
     * 逻辑删除标识：1-已删除
     */
    String DB_DELETED = "1";
}