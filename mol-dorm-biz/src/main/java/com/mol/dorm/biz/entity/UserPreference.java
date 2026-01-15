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
 * 用户全维度画像实体类
 * <p>
 * 用于存储学生的作息、卫生、性格等详细偏好，是智能分配算法的核心依据。
 * 继承 BaseEntity 以自动包含 create_time, update_time 等审计字段。
 * </p>
 *
 * @author mol
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_user_preference")
@Schema(description = "用户画像/生活习惯偏好")
public class UserPreference extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     * 注意：这里不能用 AUTO，必须是 INPUT，因为它是跟 sys_ordinary_user 的 ID 一一对应的
     */
    @TableId(type = IdType.INPUT)
    @Schema(description = "用户ID (关联学生表)")
    private Long userId;
    
    @Schema(description = "组队码 (拥有相同组队码的学生将被强制分配到同一宿舍)")
    private String teamCode;
    
    // ==========================================
    // 1. 作息习惯 (Schedule)
    // ==========================================
    
    @Schema(description = "就寝时间: 1-21点, 2-22点, 3-23点, 4-24点, 5-1点, 6-2点+")
    private Integer bedTime;
    
    @Schema(description = "起床时间: 1-6点, 2-7点, 3-8点, 4-9点, 5-10点, 6-11点+")
    private Integer wakeTime;
    
    @Schema(description = "午睡习惯: 0-无, 1-偶尔, 2-必须午睡(需安静)")
    private Integer siestaHabit;
    
    @Schema(description = "晚归频率: 0-从不, 1-偶尔, 2-经常")
    private Integer outLateFreq;
    
    // ==========================================
    // 2. 睡眠质量 (Sleep Quality)
    // ==========================================
    
    @Schema(description = "睡眠深浅: 1-雷打不动, 2-普通, 3-轻度敏感, 4-神经衰弱")
    private Integer sleepQuality;
    
    @Schema(description = "打呼噜等级: 0-无, 1-轻微, 2-中度, 3-雷震子(严重)")
    private Integer snoringLevel;
    
    @Schema(description = "磨牙: 0-无, 1-有")
    private Integer grindingTeeth;
    
    @Schema(description = "说梦话: 0-无, 1-有")
    private Integer sleepTalk;
    
    @Schema(description = "上下床动静: 0-轻盈, 1-普通, 2-拆迁队(动静大)")
    private Integer climbBedNoise;
    
    // ==========================================
    // 3. 卫生习惯 (Hygiene)
    // ==========================================
    
    @Schema(description = "洗澡频率: 1-每天, 2-两天一次, 3-不定期")
    private Integer showerFreq;
    
    @Schema(description = "袜子清洗: 0-当天洗, 1-攒一堆洗")
    private Integer sockWash;
    
    @Schema(description = "倒垃圾习惯: 1-满了就倒, 2-轮流倒, 3-谁看不下去谁倒")
    private Integer trashHabit;
    
    @Schema(description = "打扫频率: 1-每天, 2-每周, 3-每月, 4-随缘")
    private Integer cleanFreq;
    
    @Schema(description = "接受轮流刷厕所: 1-完全接受, 0-拒绝")
    private Integer toiletClean;
    
    @Schema(description = "桌面整洁度: 1-极简, 2-乱中有序, 3-垃圾堆")
    private Integer deskMessy;
    
    @Schema(description = "个人卫生自评: 1-5分 (1分邋遢, 5分洁癖)")
    private Integer personalHygiene;
    
    @Schema(description = "异味容忍度: 1-无法忍受, 2-普通, 3-毒气室也能住")
    private Integer odorTolerance;
    
    // ==========================================
    // 4. 生活嗜好 (Habits)
    // ==========================================
    
    @Schema(description = "抽烟: 0-不抽, 1-阳台抽, 2-室内抽")
    private Integer smoking;
    
    @Schema(description = "接受烟味: 0-不可, 1-可以")
    private Integer smokeTolerance;
    
    @Schema(description = "喝酒: 0-无, 1-小酌, 2-酗酒")
    private Integer drinking;
    
    @Schema(description = "空调温度习惯: 16-30度")
    private Integer acTemp;
    
    @Schema(description = "空调时长: 1-整晚, 2-定时关闭")
    private Integer acDuration;
    
    // ==========================================
    // 5. 游戏与外设 (Gaming & Tech)
    // ==========================================
    
    @Schema(description = "玩LOL/DOTA: 0-否 1-是")
    private Integer gameTypeLol;
    
    @Schema(description = "玩FPS(CS/瓦/三角洲): 0-否 1-是")
    private Integer gameTypeFps;
    
    @Schema(description = "玩3A大作: 0-否 1-是")
    private Integer gameType3a;
    
    @Schema(description = "玩MMO(剑三/魔兽): 0-否 1-是")
    private Integer gameTypeMmo;
    
    @Schema(description = "玩手游(王者/原神): 0-否 1-是")
    private Integer gameTypeMobile;
    
    @Schema(description = "游戏习惯综合: 0-不玩 1-轻度 2-重度")
    private Integer gameHabit;
    
    @Schema(description = "连麦音量: 0-静音, 1-正常, 2-咆哮")
    private Integer gameVoice;
    
    @Schema(description = "键盘轴体: 1-薄膜/静音, 2-红/茶轴, 3-青轴(吵)")
    private Integer keyboardAxis;
    
    // ==========================================
    // 6. 性格与社交 (Personality)
    // ==========================================
    
    @Schema(description = "玩Cosplay: 0-否 1-是")
    private Integer isCosplay;
    
    @Schema(description = "二次元浓度: 0-现充, 1-看番, 2-老二刺螈")
    private Integer isAnime;
    
    @Schema(description = "MBTI E/I维度: E或I")
    private String mbtiEI;
    
    @Schema(description = "MBTI 完整结果 (如 INTJ)")
    private String mbtiResult;
    
    @Schema(description = "社交意愿: 1-社恐(别理我) -> 5-社牛")
    private Integer socialBattery;
    
    @Schema(description = "物品共享意愿: 0-皆不可, 1-部分可借(纸/伞), 2-随意用")
    private Integer shareItems;
    
    @Schema(description = "带人回寝: 0-绝不, 1-偶尔同性, 2-经常, 3-带异性")
    private Integer bringGuest;
    
    @Schema(description = "接受访客频率: 同带人回寝")
    private Integer visitors;
    
    @Schema(description = "恋爱状态: 0-单身, 1-恋爱中(可能会煲电话粥)")
    private Integer relationshipStatus;
    
    // ==========================================
    // 7. 特殊需求 (Special Needs) - 一票否决项
    // ==========================================
    
    @Schema(description = "身体残疾: 0-无, 1-腿部残疾(需低层)")
    private Integer hasDisability;
    
    @Schema(description = "胰岛素需求: 0-无, 1-需要冰箱")
    private Integer hasInsulin;
    
    @Schema(description = "传染性疾病: 0-无, 1-有(需单独处理)")
    private Integer hasInfectious;
    
    @Schema(description = "宗教禁忌 (如清真)")
    private String religionTaboo;
    
    @Schema(description = "特殊疾病描述")
    private String specialDisease;
    
    // ==========================================
    // 8. 游戏详细数据 (Game Details)
    // ==========================================
    @Schema(description = "游戏段位: 0-黑铁/青铜, 1-白银/黄金, 2-铂金/钻石, 3-大师/王者")
    private Integer gameRank;
    
    @Schema(description = "MOBA 位置: 0-全能, 1-上单, 2-打野, 3-中单, 4-射手, 5-辅助")
    private Integer gameRole;
    
    // ==========================================
    // 9. 饮食偏好 (Dietary & Smell)
    // ==========================================
    @Schema(description = "吃螺蛳粉: 0-拒绝/闻不了, 1-偶尔吃, 2-重度爱好者")
    private Integer eatLuosifen;
    
    @Schema(description = "吃榴莲: 0-拒绝/闻不了, 1-吃")
    private Integer eatDurian;
    
    // ==========================================
    // 10. 籍贯/民族 (通常在User表，若为了方便计算可冗余在此)
    // ==========================================
    @Schema(description = "南北方: 0-南方, 1-北方 (根据籍贯自动计算)")
    private Integer regionType;
}