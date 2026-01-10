package com.mol.dorm.biz.vo;

import com.mol.dorm.biz.entity.DormRoom;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * 宿舍房间详情视图对象
 * 包含：房间基本信息 + 床位列表(含学生姓名)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DormRoomVo extends DormRoom {
    // 继承 DormRoom 的所有属性
    
    // 额外包含该房间的床位列表
    private List<BedInfo> bedList;
    
    @Data
    public static class BedInfo {
        private Long bedId;
        private String bedLabel;   // 床位号 (如 1号床)
        private Long studentId;    // 居住人 ID
        private String studentName;// 居住人姓名
        private String studentNo;  // 学号
    }
}