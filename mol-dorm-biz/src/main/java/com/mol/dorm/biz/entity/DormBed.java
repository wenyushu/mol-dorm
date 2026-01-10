package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 宿舍床位实体类
 * <p>
 * 对应数据库表：dorm_bed
 * 核心逻辑：一个房间(DormRoom)有多个床位(DormBed)，每个床位绑定一个学生(occupantId)
 * </p>
 *
 * @author mol
 */
@Data // 自动生成 getter, setter, toString 等方法
@EqualsAndHashCode(callSuper = true) // 包含父类 BaseEntity 字段的比较
@TableName("dorm_bed") // 映射数据库表名
public class DormBed extends BaseEntity implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 所属房间 ID
     * 关联 dorm_room.id
     */
    private Long roomId;
    
    /**
     * 床位标签/编号 (核心修复字段)
     * 例如: "305-1", "305-2"
     * <p>
     * 说明：此字段用于前端展示和后台排序。
     * 之前报错是因为缺少这个字段，导致 Lombok 无法生成 getBedLabel() 方法。
     * </p>
     */
    @TableField("bed_label") // 指定数据库列名，如果数据库是 bed_label，这行其实可以省略，MyBatis-Plus会自动驼峰映射
    private String bedLabel;
    
    /**
     * 当前居住者 ID
     * 关联 sys_ordinary_user.id
     * <p>
     * 如果为 null，表示该床位为空，可以分配。
     * </p>
     */
    private Long occupantId;
    
    // 注意：
    // createTime, updateTime, createBy, updateBy, delFlag 等审计字段
    // 已经通过继承 BaseEntity 自动获得，无需在此重复定义。
}