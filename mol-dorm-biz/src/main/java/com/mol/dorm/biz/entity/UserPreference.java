package com.mol.dorm.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mol.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 用户宿舍分配偏好画像实体
 * <p>
 * 对应表: biz_user_preference
 * 包含50+个维度的生活习惯特征
 * </p>
 *
 * @author mol
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_user_preference")
@Schema(description = "用户分配偏好画像")
public class UserPreference extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "用户ID (主键, 与sys_user_id一致)")
    @TableId(type = IdType.INPUT) // 手动输入ID，非自增
    private Long userId;
    
    @Schema(description = "组队码 (有相同码的优先分一起)")
    private String teamCode;
    
    // === 1. 核心生活习惯 (权重: 高) ===
    
    @Schema(description = "是否抽烟: 0-不抽, 1-偶尔, 2-经常")
    private Integer smoking;
    
    @Schema(description = "能否接受烟味: 0-绝不, 1-可以")
    private Integer smokeTolerance;
    
    @Schema(description = "是否喝酒: 0-不喝, 1-偶尔, 2-经常")
    private Integer drinking;
    
    @Schema(description = "就寝时间: 1-22点前, 2-23点, 3-24点, 4-凌晨1点, 5-凌晨2点+")
    private Integer bedTime;
    
    @Schema(description = "起床时间: 1-6点前, 2-7点, 3-8点, 4-9点, 5-10点+")
    private Integer wakeTime;
    
    @Schema(description = "睡眠深浅: 0-雷打不动, 1-普通, 2-轻度敏感, 3-神经衰弱")
    private Integer sleepLight;
    
    @Schema(description = "是否打鼾: 0-无, 1-轻微, 2-严重")
    private Integer snoring;
    
    // === 2. 卫生与环境 (权重: 中) ===
    
    @Schema(description = "夏季空调: 1-<20度, 2-20~24度, 3-25~27度, 4-28度+, 5-不用")
    private Integer acTempSummer;
    
    @Schema(description = "打扫频率: 1-每天, 2-每周, 3-每月, 4-随缘")
    private Integer cleanFreq;
    
    @Schema(description = "轮流刷厕所: 1-接受, 0-拒绝")
    private Integer toiletClean;
    
    @Schema(description = "个人卫生评价: 1-5分")
    private Integer personalHygiene;
    
    @Schema(description = "异味容忍度: 1-5分")
    private Integer odorTolerance;
    
    // === 3. 兴趣与娱乐 (权重: 低) ===
    
    @Schema(description = "游戏习惯: 0-不玩, 1-手游, 2-端游(键鼠), 3-主机")
    private Integer gameHabit;
    
    @Schema(description = "连麦音量: 0-静音, 1-小声, 2-激动")
    private Integer gameVoice;
    
    @Schema(description = "键盘类型: 0-静音/薄膜, 1-机械(青轴等吵闹型)")
    private Integer keyboardType;
    
    @Schema(description = "二次元浓度: 0-现充, 1-路人, 2-老二刺螈(Cos/追番)")
    private Integer isAcg;
    
    @Schema(description = "健身习惯: 0-无, 1-偶尔, 2-狂热")
    private Integer gymHabit;
    
    // === 4. 性格与社交 (权重: 中) ===
    
    @Schema(description = "E/I维度 (E/I)")
    private String mbtiEI;
    
    @Schema(description = "完整 MBTI (如 INTJ)")
    private String mbtiType;
    
    @Schema(description = "社交电量: 1(社恐)-5(社牛)")
    private Integer socialBattery;
    
    @Schema(description = "带人回寝: 0-绝不, 1-偶尔同性, 2-经常同性, 3-带异性(打死)")
    private Integer visitors;
    
    // === 5. 特殊需求 (硬性约束) ===
    
    @Schema(description = "特殊疾病(如糖尿病需冰箱)")
    private String specialDisease;
    
    @Schema(description = "残疾辅助: 0-无, 1-需低楼层/电梯")
    private Integer disability;
    
    @Schema(description = "宗教习惯")
    private String religionHabit;
}