package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户全维度画像实体
 * <p>
 * 对应表: biz_user_preference
 * 包含 50+ 个细分维度，用于宿舍智能分配算法。
 * </p>
 *
 * @author mol
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_user_preference")
@Schema(description = "用户分配偏好画像")
public class UserPreference extends BaseEntity implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "用户ID (手动输入，非自增)")
    @TableId(type = IdType.INPUT)
    private Long userId;
    
    @Schema(description = "组队码")
    private String teamCode;
    
    // === 1. 基础作息 (权重: 2.0) ===
    @Schema(description = "就寝时间: 1-21点...6-2点+")
    private Integer bedTime;
    
    @Schema(description = "起床时间: 1-6点...6-11点+")
    private Integer wakeTime;
    
    @Schema(description = "午睡习惯: 0-无 1-有")
    private Integer siestaHabit;
    
    @Schema(description = "晚归频率")
    private Integer outLateFreq;
    
    // === 2. 睡眠质量 (权重: 2.0) ===
    @Schema(description = "睡眠深浅 1-4")
    private Integer sleepQuality;
    
    @Schema(description = "打呼噜 0-3")
    private Integer snoringLevel; // 对应数据库 snoring
    
    @Schema(description = "磨牙")
    private Integer grindingTeeth;
    
    @Schema(description = "说梦话")
    private Integer sleepTalk;
    
    @Schema(description = "上下床动静")
    private Integer climbBedNoise;
    
    // === 3. 卫生习惯 (权重: 1.5) ===
    @Schema(description = "洗澡频率")
    private Integer showerFreq;
    
    @Schema(description = "袜子清洗")
    private Integer sockWash;
    
    @Schema(description = "倒垃圾习惯")
    private Integer trashHabit;
    
    @Schema(description = "打扫频率: 1-每天...4-随缘")
    @TableField("clean_freq") // 显式指定，防止驼峰转换歧义
    private Integer cleanFreq;
    
    @Schema(description = "刷厕所意愿 1-是 0-否")
    private Integer toiletClean;
    
    @Schema(description = "桌面整洁度")
    private Integer deskMessy;
    
    @Schema(description = "个人卫生评分 1-5")
    @TableField("personal_hygiene") // 显式指定，确保 getPersonalHygiene() 生成正确
    private Integer personalHygiene;
    
    @Schema(description = "异味容忍度")
    private Integer odorTolerance;
    
    // === 4. 生活习惯 (权重: 1.5) ===
    @Schema(description = "抽烟 0-不 1-阳台 2-室内")
    private Integer smoking;
    
    @Schema(description = "烟味容忍 0-否 1-是")
    private Integer smokeTolerance;
    
    @Schema(description = "喝酒")
    private Integer drinking;
    
    @Schema(description = "空调温度")
    private Integer acTemp; // 对应 ac_temp_summer 或 ac_temp
    
    @Schema(description = "空调时长")
    private Integer acDuration;
    
    // === 5. 游戏与娱乐 (权重: 1.0) ===
    @Schema(description = "玩LOL/DOTA")
    private Integer gameTypeLol;
    @Schema(description = "玩FPS")
    private Integer gameTypeFps;
    @Schema(description = "玩3A大作")
    private Integer gameType3a;
    @Schema(description = "玩MMO")
    private Integer gameTypeMmo;
    @Schema(description = "玩手游")
    private Integer gameTypeMobile;
    
    @Schema(description = "游戏习惯 (综合)")
    private Integer gameHabit;
    
    @Schema(description = "连麦音量")
    private Integer gameVoice;
    
    @Schema(description = "键盘轴体 (3-青轴吵)")
    private Integer keyboardAxis; // 对应 keyboard_type 或 keyboard_axis
    
    @Schema(description = "是否Cosplay")
    private Integer isCosplay;
    
    @Schema(description = "二次元浓度")
    private Integer isAnime; // 对应 is_acg 或 is_anime
    
    // === 6. 性格与社交 (权重: 0.8) ===
    @Schema(description = "MBTI E/I维度")
    private String mbtiEI;
    
    @Schema(description = "MBTI完整结果")
    private String mbtiResult;
    
    @Schema(description = "社交电量 1-5")
    private Integer socialBattery;
    
    @Schema(description = "物品共享")
    private Integer shareItems;
    
    @Schema(description = "带人回寝")
    private Integer visitors; // 对应 visitors 或 bring_guest
    
    @Schema(description = "恋爱状态")
    private Integer relationshipStatus;
    
    // === 7. 健康与特殊 (一票否决) ===
    @Schema(description = "残疾需求 0-无 1-低层")
    private Integer hasDisability; // 对应 disability
    
    @Schema(description = "胰岛素需求 0-无 1-有")
    @TableField("special_disease") // 临时映射到 special_disease 字段，如果数据库有 has_insulin 更好
    private String hasInsulinStr; // 这里处理一下类型转换，或者数据库统一用 special_disease
    
    // 为了兼容代码逻辑，手动增加一个 getter，如果 specialDisease 包含 "胰岛素" 返回 1
    public Integer getHasInsulin() {
        if (specialDisease != null && specialDisease.contains("胰岛素")) {
            return 1;
        }
        return 0;
    }
    
    @Schema(description = "传染病 0-无 1-有")
    private Integer hasInfectious; // 如果数据库没这个字段，代码逻辑里先默认0
    
    @Schema(description = "特殊疾病描述")
    private String specialDisease;
    
    @Schema(description = "宗教禁忌")
    private String religionTaboo; // 对应 religion_habit
}