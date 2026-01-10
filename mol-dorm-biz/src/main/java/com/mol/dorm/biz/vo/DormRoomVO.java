package com.mol.dorm.biz.vo;

import com.mol.dorm.biz.entity.DormRoom;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 宿舍房间详情视图对象 (View Object)
 * <p>
 * 用途：
 * 1. 在列表查询时，携带“床位列表”和“居住人姓名”等额外信息返回给前端。
 * 2. 避免直接修改实体类 DormRoom，保持 Entity 的纯洁性。
 * </p>
 *
 * @author mol
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DormRoomVO extends DormRoom implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 该房间内的床位列表
     * (包含：床位号、居住人 ID、居住人姓名、学号)
     */
    private List<BedInfo> bedList;
    
    /**
     * 床位详情内部类
     */
    @Data
    public static class BedInfo implements Serializable {
        
        @Serial
        private static final long serialVersionUID = 1L;
        
        /** 床位 ID */
        private Long bedId;
        
        /** 床位号 (如: 101-1) */
        private String bedLabel;
        
        /** 居住人 ID (空闲则为 null) */
        private Long studentId;
        
        /** 居住人姓名 (空闲则为 null) */
        private String studentName;
        
        /** 居住人学号 (空闲则为 null) */
        private String studentNo;
    }
}