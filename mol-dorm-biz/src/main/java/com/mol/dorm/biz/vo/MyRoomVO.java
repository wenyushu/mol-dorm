package com.mol.dorm.biz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 移动端/小程序：我的宿舍全量看板 (一站式聚合模型)
 * 🛡️ [聚合模型]：位置 + 财务审计 + 能源账单 + 团队 + 舍友 + 公告。
 * 🛡️ [防刁民设计]：前端通过此 VO 实现“欠费自动弹窗”和“最新公告强制置顶”。
 */
@Data
@Schema(description = "我的宿舍个人一站式看板")
public class MyRoomVO {
    
    // === 1. 基础物理空间信息 ===
    @Schema(description = "校区-楼宇-房间 完整描述")
    private String fullLocation;
    
    @Schema(description = "楼宇名称")
    private String buildingName;
    
    @Schema(description = "房间号")
    private String roomNo;
    
    @Schema(description = "房型描述 (如: 六人间)")
    private String apartmentType;
    
    @Schema(description = "房间状态描述 (如: 正常使用)")
    private String statusDesc;
    
    // === 2. 核心财务与余额预警 ===
    @Schema(description = "房间水电余额")
    private BigDecimal walletBalance;
    
    @Schema(description = "钱包状态 (1-正常, 2-欠费预警, 3-停用)")
    private Integer walletStatus;
    
    @Schema(description = "余额状态描述 (如: 余额充足、请及时充值)")
    private String walletStatusMsg;
    
    @Schema(description = "是否触发停电预警 (余额 < 10元时返回 true)")
    private Boolean powerOffWarning;
    
    // === 3. 能源账单快照 (最近一期) ===
    @Schema(description = "上月账单月份 (如: 2026-01)")
    private String lastBillMonth;
    
    @Schema(description = "上月总能耗费用")
    private BigDecimal lastBillAmount;
    
    @Schema(description = "上月用电量 (度)")
    private BigDecimal lastElectricUsage;
    
    @Schema(description = "上月用电费 (元)")
    private BigDecimal lastElectricCost;
    
    // === 4. 服务团队联系方式 ===
    @Schema(description = "楼栋长/宿管姓名")
    private String managerName;
    
    @Schema(description = "宿管紧急联系电话")
    private String managerPhone;
    
    @Schema(description = "班级辅导员姓名")
    private String counselorName;
    
    @Schema(description = "辅导员电话")
    private String counselorPhone;
    
    // === 5. 校园/宿舍公告矩阵 ===
    @Schema(description = "最新系统公告 (置顶)")
    private NoticeSnippetVO latestNotice;
    
    // === 6. 舍友画像列表 ===
    @Schema(description = "舍友列表 (包含我自己在内)")
    private List<BedInfoVO> bedList;
    
    // --- 内部静态模型 1：床位与舍友详情 ---
    @Data
    public static class BedInfoVO {
        @Schema(description = "床位ID")
        private Long bedId;
        
        @Schema(description = "床位标签")
        private String bedLabel;
        
        @Schema(description = "是否有人居住")
        private Boolean isOccupied;
        
        @Schema(description = "是否是当前登录人 (前端高亮显示自己)")
        private Boolean isMe;
        
        @Schema(description = "舍友姓名")
        private String name;
        
        @Schema(description = "头像 URL")
        private String avatar;
        
        @Schema(description = "身份标签 (如: 计算机学院/24级/学生)")
        private String identityTag;
        
        @Schema(description = "班级专业信息")
        private String classMajorDesc;
    }
    
    // --- 内部静态模型 2：公告摘要 ---
    @Data
    public static class NoticeSnippetVO {
        @Schema(description = "公告ID")
        private Long noticeId;
        
        @Schema(description = "标题")
        private String title;
        
        @Schema(description = "公告类型 (1常规 2放假 3欠费通知)")
        private String typeDesc;
        
        @Schema(description = "发布时间")
        private String createTime;
    }
}