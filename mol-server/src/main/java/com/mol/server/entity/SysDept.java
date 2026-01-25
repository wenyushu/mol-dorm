package com.mol.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门实体
 * (存放于 mol-server 业务模块)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class SysDept extends BaseEntity {
    
    /**
     * 部门 ID (主键)
     * 必须显式定义，否则报错 getId 找不到
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 部门名称 (如：后勤处、保卫科)
     */
    private String name;
    
    /**
     * 部门编码
     */
    private String code;
    
    /**
     * 部门简介
     */
    private String intro;
    
    /**
     * 显示排序
     */
    private Integer sort;
    
    /**
     * 状态 (0正常 1停用)
     */
    private String status;
}