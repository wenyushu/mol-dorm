/*
SQLyog Community v13.3.1 (64 bit)
MySQL - 8.4.6 : Database - mol-dorm
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`mol-dorm` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `mol-dorm`;

/*Table structure for table `biz_class` */

DROP TABLE IF EXISTS `biz_class`;

CREATE TABLE `biz_class` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `major_id` bigint NOT NULL COMMENT '所属专业ID',
  `grade` int NOT NULL COMMENT '年级 (如: 2024)',
  `class_name` varchar(100) NOT NULL COMMENT '班级名称',
  `education_level` varchar(32) DEFAULT NULL COMMENT '培养层次 (专科,本科,硕士研究生,博士研究生)',
  `counselor_id` bigint DEFAULT NULL COMMENT '辅导员ID',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号(0开始)',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_major` (`major_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2302 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-班级表';

/*Data for the table `biz_class` */

insert  into `biz_class`(`id`,`major_id`,`grade`,`class_name`,`education_level`,`counselor_id`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1005,2024,'系统结构 2401 班','本科',NULL,0,'system','2026-01-31 17:03:52','1','2026-02-03 14:54:36','0',NULL),
(2,1001,2023,'软工 2301 班','本科',NULL,0,'system','2026-01-31 17:03:52','','2026-02-03 14:38:27','0',NULL),
(3,3001,2024,'数媒 2401 班','专科',NULL,0,'system','2026-01-31 17:03:52','','2026-02-03 14:38:28','0',NULL),
(4,1004,2023,'计科 2301 班','硕士研究生',NULL,0,'system','2026-01-31 17:03:52','','2026-02-03 14:38:30','0',NULL),
(5,2002,2024,'MBA 2401 班','硕士研究生',NULL,0,'system','2026-01-31 17:03:52','','2026-02-03 14:40:35','0',NULL),
(6,1005,2026,'计算机系统结构 2601 博士班(直博)',NULL,NULL,0,'1','2026-02-03 22:44:53','1','2026-02-03 22:44:53','0',NULL),
(301,1001,2024,'24级软工1班','本科',3,0,'admin','2026-02-04 15:09:19','','2026-02-04 16:28:03','0',NULL),
(302,1001,2024,'24级软工2班','本科',3,0,'admin','2026-02-04 15:09:19','','2026-02-04 16:28:03','0',NULL),
(303,2001,2024,'24级大数据专科1班','专科',3,0,'admin','2026-02-04 15:09:19','','2026-02-04 16:28:03','0',NULL),
(304,2002,2024,'24级软专升本1班','专升本',3,0,'admin','2026-02-04 15:09:19','','2026-02-04 16:28:03','0',NULL),
(305,2003,2024,'24级医研1班','硕士研究生',8,0,'admin','2026-02-04 15:09:19','','2026-02-04 16:28:03','0',NULL),
(306,2004,2024,'24级医学博士班','博士研究生',8,0,'admin','2026-02-04 15:09:19','','2026-02-04 16:28:03','0',NULL),
(307,2005,2024,'24级法学硕士班','硕士研究生',8,0,'admin','2026-02-04 15:09:19','','2026-02-04 16:28:03','0',NULL),
(308,2008,2024,'24级海洋博1班','博士研究生',8,0,'admin','2026-02-04 15:09:19','','2026-02-04 16:28:03','0',NULL),
(2301,1001,2023,'软工2301班','本科',5001,0,'system','2026-02-03 14:57:07','',NULL,'0',NULL);

/*Table structure for table `biz_electric_violation_log` */

DROP TABLE IF EXISTS `biz_electric_violation_log`;

CREATE TABLE `biz_electric_violation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `violation_type` int NOT NULL COMMENT '违规类型',
  `power_val` decimal(10,2) DEFAULT NULL COMMENT '功率值',
  `detected_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号(0开始)',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物联-违规日志';

/*Data for the table `biz_electric_violation_log` */

insert  into `biz_electric_violation_log`(`id`,`room_id`,`violation_type`,`power_val`,`detected_time`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,101,1,1200.50,'2026-02-04 15:18:49',0,'system','2026-02-04 15:18:49','',NULL,'0','系统检测到大功率电器(疑似吹风机/热得快)，已自动断电'),
(2,104,2,2500.00,'2026-02-04 15:18:49',0,'system','2026-02-04 15:18:49','',NULL,'0','空调回路电流异常，疑似私接排插'),
(3,101,1,950.00,'2026-02-04 15:18:49',0,'system','2026-02-04 15:18:49','',NULL,'0','二次违规使用大功率电器');

/*Table structure for table `biz_recharge_log` */

DROP TABLE IF EXISTS `biz_recharge_log`;

CREATE TABLE `biz_recharge_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(64) NOT NULL COMMENT '流水号 (唯一, 防止重复到账)',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `user_id` bigint NOT NULL COMMENT '操作人ID (谁充的钱)',
  `amount` decimal(10,2) NOT NULL COMMENT '充值金额',
  `before_balance` decimal(10,2) NOT NULL COMMENT '充值前余额快照',
  `after_balance` decimal(10,2) NOT NULL COMMENT '充值后余额快照',
  `pay_type` varchar(20) DEFAULT 'WXPAY' COMMENT '支付方式 (WXPAY/ALIPAY/CASH)',
  `status` tinyint DEFAULT '1' COMMENT '状态 (1-成功 0-失败/处理中)',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '充值时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注 (存入外部流水号)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_room` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='财务-充值记录审计表';

/*Data for the table `biz_recharge_log` */

/*Table structure for table `biz_repair_order` */

DROP TABLE IF EXISTS `biz_repair_order`;

CREATE TABLE `biz_repair_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(32) NOT NULL COMMENT '工单号 (唯一)',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `asset_code` varchar(64) DEFAULT NULL COMMENT '关联资产条码',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID',
  `description` text NOT NULL COMMENT '报修描述',
  `asset_snapshot` varchar(500) DEFAULT NULL COMMENT '报修时资产状态快照',
  `images` varchar(1000) DEFAULT NULL COMMENT '图片地址',
  `status` tinyint DEFAULT '0' COMMENT '状态: 0待指派, 1维修中, 2已完成, 3已评价, 4已驳回, 5待大修',
  `repairman_id` bigint DEFAULT NULL COMMENT '维修工ID',
  `repairman_name` varchar(64) DEFAULT NULL COMMENT '维修工姓名',
  `repairman_phone` varchar(20) DEFAULT NULL COMMENT '维修工电话',
  `assign_time` datetime DEFAULT NULL COMMENT '指派时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完工时间',
  `rating` tinyint DEFAULT NULL COMMENT '评分',
  `material_cost` decimal(10,2) DEFAULT '0.00' COMMENT '维修耗材费用',
  `is_human_damage` tinyint(1) DEFAULT '0' COMMENT '是否人为损坏 (0-否, 1-是)',
  `comment` varchar(255) DEFAULT NULL COMMENT '评价内容',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号(0开始)',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_room_status` (`room_id`,`status`),
  KEY `idx_asset_code` (`asset_code`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-报修工单';

/*Data for the table `biz_repair_order` */

insert  into `biz_repair_order`(`id`,`order_no`,`room_id`,`asset_code`,`applicant_id`,`description`,`asset_snapshot`,`images`,`status`,`repairman_id`,`repairman_name`,`repairman_phone`,`assign_time`,`finish_time`,`rating`,`material_cost`,`is_human_damage`,`comment`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,'REP20260204001',101,NULL,10001,'阳台洗手盆漏水',NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0,NULL,0,'system','2026-02-04 14:58:30','',NULL,'0',NULL),
(2,'REP20260204002',103,NULL,10003,'空调不制冷',NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0,NULL,0,'system','2026-02-04 14:58:30','',NULL,'0',NULL),
(3,'REP20260204003',101,NULL,10001,'宿舍门锁舌卡死',NULL,NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0,NULL,0,'system','2026-02-04 14:58:30','',NULL,'0',NULL),
(4,'REP20260204004',105,NULL,10005,'厕所灯泡闪烁',NULL,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0,NULL,0,'system','2026-02-04 14:58:30','',NULL,'0',NULL),
(5,'REP20260204005',108,NULL,10008,'窗户推拉不畅',NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0,NULL,0,'system','2026-02-04 14:58:30','',NULL,'0',NULL),
(6,'REP20260204006',101,NULL,10001,'网络插座松动',NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0,NULL,0,'system','2026-02-04 14:58:30','',NULL,'0',NULL),
(7,'REP20260204007',107,NULL,10004,'热水供应不稳定',NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0,NULL,0,'system','2026-02-04 14:58:30','',NULL,'0',NULL),
(8,'REP20260204008',102,NULL,10002,'天花板有轻微裂缝',NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0,NULL,0,'system','2026-02-04 14:58:30','',NULL,'0',NULL),
(9,'REP20260204009',101,NULL,10001,'椅子腿松动，无法正常坐立',NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0,NULL,0,'梁梓豪','2026-02-04 16:28:03','',NULL,'0',NULL),
(10,'REP20260206999',101,'RM-101-CHR-BROKEN',10001,'椅腿快掉了，坐着晃荡',NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,0.00,0,NULL,0,'梁梓豪','2026-02-06 15:32:30','',NULL,'0',NULL);

/*Table structure for table `biz_user_preference` */

DROP TABLE IF EXISTS `biz_user_preference`;

CREATE TABLE `biz_user_preference` (
  `user_id` bigint NOT NULL COMMENT '用户 ID (关联学生表)',
  `team_code` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '组队码 (相同码强制分一间)',
  `bed_time` tinyint DEFAULT NULL COMMENT '就寝时间: 1-21点, 2-22点, 3-23点, 4-24点, 5-1点, 6-2点+',
  `wake_time` tinyint DEFAULT NULL COMMENT '起床时间: 1-6点, 2-7点, 3-8点, 4-9点, 5-10点, 6-11点+',
  `siesta_habit` tinyint DEFAULT '0' COMMENT '午睡习惯: 0-无, 1-偶尔, 2-必须午睡(需安静)',
  `out_late_freq` tinyint DEFAULT '0' COMMENT '晚归频率: 0-从不, 1-偶尔, 2-经常',
  `sleep_quality` tinyint DEFAULT '2' COMMENT '睡眠深浅: 1-雷打不动, 2-普通, 3-轻度敏感, 4-神经衰弱',
  `snoring_level` tinyint DEFAULT '0' COMMENT '打呼噜等级: 0-无, 1-轻微, 2-中度, 3-雷震子(严重)',
  `grinding_teeth` tinyint DEFAULT '0' COMMENT '磨牙: 0-无, 1-有',
  `sleep_talk` tinyint DEFAULT '0' COMMENT '说梦话: 0-无, 1-有',
  `climb_bed_noise` tinyint DEFAULT '1' COMMENT '上下床动静: 0-轻盈, 1-普通, 2-拆迁队(动静大)',
  `shower_freq` tinyint DEFAULT '1' COMMENT '洗澡频率: 1-每天, 2-两天一次, 3-不定期',
  `sock_wash` tinyint DEFAULT '0' COMMENT '袜子清洗: 0-当天洗, 1-攒一堆洗',
  `trash_habit` tinyint DEFAULT '1' COMMENT '倒垃圾习惯: 1-满了就倒, 2-轮流倒, 3-谁看不下去谁倒',
  `clean_freq` tinyint DEFAULT '2' COMMENT '打扫频率: 1-每天, 2-每周, 3-每月, 4-随缘',
  `toilet_clean` tinyint DEFAULT '1' COMMENT '接受轮流刷厕所: 1-完全接受, 0-拒绝',
  `desk_messy` tinyint DEFAULT '2' COMMENT '桌面整洁度: 1-极简, 2-乱中有序, 3-垃圾堆',
  `personal_hygiene` tinyint DEFAULT '3' COMMENT '个人卫生自评: 1-5分',
  `odor_tolerance` tinyint DEFAULT '2' COMMENT '异味容忍度: 1-无法忍受, 2-普通, 3-毒气室也能住',
  `smoking` tinyint DEFAULT '0' COMMENT '抽烟: 0-不抽, 1-阳台抽, 2-室内抽',
  `smoke_tolerance` tinyint DEFAULT '0' COMMENT '接受烟味: 0-不可, 1-可以',
  `drinking` tinyint DEFAULT '0' COMMENT '喝酒: 0-无, 1-小酌, 2-酗酒',
  `ac_temp` tinyint DEFAULT '26' COMMENT '空调温度习惯: 16-30度',
  `ac_duration` tinyint DEFAULT '1' COMMENT '空调时长: 1-整晚, 2-定时关闭',
  `game_type_lol` tinyint DEFAULT '0' COMMENT '玩LOL/DOTA: 0-否 1-是',
  `game_type_fps` tinyint DEFAULT '0' COMMENT '玩FPS: 0-否 1-是',
  `game_type_3a` tinyint DEFAULT '0' COMMENT '玩3A大作: 0-否 1-是',
  `game_type_mmo` tinyint DEFAULT '0' COMMENT '玩MMO: 0-否 1-是',
  `game_type_mobile` tinyint DEFAULT '0' COMMENT '玩手游: 0-否 1-是',
  `game_habit` tinyint DEFAULT '0' COMMENT '游戏习惯综合: 0-不玩 1-轻度 2-重度',
  `game_voice` tinyint DEFAULT '1' COMMENT '连麦音量: 0-静音, 1-正常, 2-咆哮',
  `keyboard_axis` tinyint DEFAULT '1' COMMENT '键盘轴体: 1-薄膜/静音, 2-红/茶轴, 3-青轴(吵)',
  `is_cosplay` tinyint DEFAULT '0' COMMENT '玩Cosplay: 0-否 1-是',
  `is_anime` tinyint DEFAULT '0' COMMENT '二次元浓度: 0-现充, 1-看番, 2-老二刺螈',
  `mbti_e_i` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'MBTI E/I维度: E或I',
  `mbti_result` char(4) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'MBTI结果 (如 INTJ)',
  `social_battery` tinyint DEFAULT '3' COMMENT '社交意愿: 1-社恐 -> 5-社牛',
  `share_items` tinyint DEFAULT '1' COMMENT '物品共享意愿: 0-皆不可, 1-部分, 2-随意',
  `bring_guest` tinyint DEFAULT '0' COMMENT '带人回寝: 0-绝不, 1-偶尔, 2-经常, 3-带异性',
  `visitors` tinyint DEFAULT '0' COMMENT '接受访客频率',
  `relationship_status` tinyint DEFAULT '0' COMMENT '恋爱状态: 0-单身, 1-恋爱中',
  `has_disability` tinyint DEFAULT '0' COMMENT '身体残疾: 0-无, 1-腿部残疾',
  `has_insulin` tinyint DEFAULT '0' COMMENT '胰岛素需求: 0-无, 1-需要冰箱',
  `has_infectious` tinyint DEFAULT '0' COMMENT '传染性疾病: 0-无, 1-有',
  `religion_taboo` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '宗教禁忌',
  `special_disease` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '特殊疾病描述',
  `game_rank` tinyint DEFAULT '0' COMMENT '游戏段位: 0-黑铁, 1-黄金, 2-钻石, 3-王者',
  `game_role` tinyint DEFAULT '0' COMMENT 'MOBA位置: 0-全能, 1-上, 2-野, 3-中, 4-射, 5-辅',
  `eat_luosifen` tinyint DEFAULT '1' COMMENT '吃螺蛳粉: 0-拒绝, 1-偶尔, 2-重度',
  `eat_durian` tinyint DEFAULT '1' COMMENT '吃榴莲: 0-拒绝, 1-吃',
  `region_type` tinyint DEFAULT NULL COMMENT '南北方: 0-南方, 1-北方',
  `use_blackout_curtain` tinyint DEFAULT '0' COMMENT '遮光帘依赖: 0-不用, 1-必装',
  `ventilation_habit` tinyint DEFAULT '1' COMMENT '通风习惯: 0-怕冷关窗, 1-常开窗',
  `light_sensitivity` tinyint DEFAULT '2' COMMENT '光线敏感度: 1-全黑, 2-普通, 3-开灯睡',
  `electricity_budget` tinyint DEFAULT '2' COMMENT '电费承受力: 1-省电, 2-普通, 3-空调自由',
  `profile_status` tinyint DEFAULT '0' COMMENT '画像完成度: 0-未完成, 1-已完成',
  `match_locked` tinyint DEFAULT '0' COMMENT '匹配锁定: 0-开放, 1-锁定',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`),
  KEY `idx_team_code` (`team_code`),
  KEY `idx_mbti` (`mbti_result`),
  KEY `idx_pref_status` (`profile_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户全维度画像偏好表';

/*Data for the table `biz_user_preference` */

insert  into `biz_user_preference`(`user_id`,`team_code`,`bed_time`,`wake_time`,`siesta_habit`,`out_late_freq`,`sleep_quality`,`snoring_level`,`grinding_teeth`,`sleep_talk`,`climb_bed_noise`,`shower_freq`,`sock_wash`,`trash_habit`,`clean_freq`,`toilet_clean`,`desk_messy`,`personal_hygiene`,`odor_tolerance`,`smoking`,`smoke_tolerance`,`drinking`,`ac_temp`,`ac_duration`,`game_type_lol`,`game_type_fps`,`game_type_3a`,`game_type_mmo`,`game_type_mobile`,`game_habit`,`game_voice`,`keyboard_axis`,`is_cosplay`,`is_anime`,`mbti_e_i`,`mbti_result`,`social_battery`,`share_items`,`bring_guest`,`visitors`,`relationship_status`,`has_disability`,`has_insulin`,`has_infectious`,`religion_taboo`,`special_disease`,`game_rank`,`game_role`,`eat_luosifen`,`eat_durian`,`region_type`,`use_blackout_curtain`,`ventilation_habit`,`light_sensitivity`,`electricity_budget`,`profile_status`,`match_locked`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1001,'BRO666',4,3,0,0,2,0,0,0,1,1,0,1,2,1,2,3,2,0,0,0,26,1,0,0,0,0,0,2,1,1,0,0,NULL,NULL,3,1,0,0,0,0,0,0,NULL,NULL,0,0,1,1,NULL,0,1,2,2,1,0,NULL,'2026-02-05 16:43:05',NULL,'2026-02-05 16:43:05','我是梁梓豪的好兄弟1号'),
(1002,'BRO666',4,3,0,0,2,0,0,0,1,1,0,1,2,1,2,3,2,0,0,0,26,1,0,0,0,0,0,2,1,1,0,0,NULL,NULL,3,1,0,0,0,0,0,0,NULL,NULL,0,0,1,1,NULL,0,1,2,2,1,0,NULL,'2026-02-05 16:43:05',NULL,'2026-02-05 16:43:05','我是梁梓豪的好兄弟2号'),
(1003,NULL,6,6,0,0,2,1,0,0,1,3,0,1,2,1,2,3,2,0,0,0,26,1,0,0,0,0,0,2,2,3,0,0,NULL,NULL,3,1,0,0,0,0,0,0,NULL,NULL,0,0,1,1,NULL,0,1,2,2,1,0,NULL,'2026-02-05 16:43:05',NULL,'2026-02-05 16:43:05','电竞死宅：青轴键盘+深夜咆哮'),
(1004,NULL,1,1,0,0,2,0,0,0,1,1,0,1,1,1,2,3,2,0,0,0,26,1,0,0,0,0,0,0,1,1,0,0,NULL,NULL,3,1,0,0,0,0,0,0,NULL,NULL,0,0,1,1,NULL,0,1,2,2,1,0,NULL,'2026-02-05 16:43:05',NULL,'2026-02-05 16:43:05','自律强迫症：5点起床，每天打扫'),
(1005,NULL,NULL,NULL,0,0,4,0,0,0,1,1,0,1,2,1,2,3,1,0,0,0,26,1,0,0,0,0,0,0,1,1,0,0,NULL,NULL,3,1,0,0,0,0,0,0,NULL,NULL,0,0,1,1,NULL,1,1,1,2,1,0,NULL,'2026-02-05 16:43:05',NULL,'2026-02-05 16:43:05','睡眠极度敏感，别吵我'),
(1006,NULL,NULL,NULL,0,0,2,0,0,0,1,1,0,1,2,1,2,3,3,0,0,0,26,1,0,0,0,0,0,0,1,1,0,0,NULL,NULL,3,1,0,0,0,0,0,0,NULL,NULL,0,0,2,2,NULL,0,1,2,2,1,0,NULL,'2026-02-05 16:43:05',NULL,'2026-02-05 16:43:05','生化武器爱好者：螺蛳粉+榴莲'),
(1007,NULL,3,2,0,0,2,0,0,0,1,1,0,1,2,1,2,3,2,0,0,0,26,1,0,0,0,0,0,0,1,1,0,0,NULL,NULL,3,1,0,0,0,0,0,0,NULL,NULL,0,0,1,1,NULL,0,1,2,2,1,0,NULL,'2026-02-05 16:43:05',NULL,'2026-02-05 16:43:05',NULL);

/*Table structure for table `biz_utility_bill` */

DROP TABLE IF EXISTS `biz_utility_bill`;

CREATE TABLE `biz_utility_bill` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `month` varchar(7) NOT NULL COMMENT '账单月份',
  `total_amount` decimal(10,2) DEFAULT '0.00' COMMENT '总金额',
  `water_cold` decimal(10,2) DEFAULT '0.00' COMMENT '冷水用量',
  `water_hot` decimal(10,2) DEFAULT '0.00' COMMENT '热水用量',
  `electric_usage` decimal(10,2) DEFAULT '0.00' COMMENT '用电量',
  `cost_water_cold` decimal(10,2) DEFAULT '0.00' COMMENT '冷水费',
  `cost_water_hot` decimal(10,2) DEFAULT '0.00' COMMENT '热水费',
  `cost_electric` decimal(10,2) DEFAULT '0.00' COMMENT '电费',
  `status` int DEFAULT '0' COMMENT '状态',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `payment_status` int DEFAULT '0' COMMENT '支付状态',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号(0开始)',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_month` (`room_id`,`month`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-水电账单';

/*Data for the table `biz_utility_bill` */

insert  into `biz_utility_bill`(`id`,`room_id`,`month`,`total_amount`,`water_cold`,`water_hot`,`electric_usage`,`cost_water_cold`,`cost_water_hot`,`cost_electric`,`status`,`pay_time`,`payment_status`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,101,'2026-01',156.50,12.50,5.00,120.00,0.00,0.00,0.00,0,NULL,1,0,'system','2026-02-04 15:18:49','',NULL,'0',NULL),
(2,102,'2026-01',94.80,8.20,2.00,85.00,0.00,0.00,0.00,0,NULL,1,0,'system','2026-02-04 15:18:49','',NULL,'0',NULL),
(3,104,'2026-01',212.30,15.00,8.50,180.00,0.00,0.00,0.00,0,NULL,0,0,'system','2026-02-04 15:18:49','',NULL,'0',NULL),
(4,101,'2026-02',52.40,4.10,1.20,45.00,0.00,0.00,0.00,0,NULL,0,0,'system','2026-02-04 15:18:49','',NULL,'0',NULL),
(5,105,'2026-01',112.00,10.00,3.00,100.00,0.00,0.00,0.00,0,NULL,1,0,'system','2026-02-04 15:18:49','',NULL,'0',NULL),
(6,108,'2026-01',54.40,5.50,0.00,60.00,0.00,0.00,0.00,0,NULL,1,0,'system','2026-02-04 15:18:49','',NULL,'0',NULL);

/*Table structure for table `biz_wallet_transaction` */

DROP TABLE IF EXISTS `biz_wallet_transaction`;

CREATE TABLE `biz_wallet_transaction` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `trx_no` varchar(64) NOT NULL COMMENT '流水单号',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `trx_type` tinyint NOT NULL COMMENT '业务类型: 1充值, 2电费, 3水费, 4维修, 5清算',
  `amount` decimal(10,2) NOT NULL COMMENT '变动金额',
  `post_balance` decimal(10,2) NOT NULL COMMENT '动账后余额',
  `biz_no` varchar(64) DEFAULT NULL COMMENT '关联业务号',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trx_no` (`trx_no`),
  KEY `idx_room_type` (`room_id`,`trx_type`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='钱包交易流水表';

/*Data for the table `biz_wallet_transaction` */

insert  into `biz_wallet_transaction`(`id`,`trx_no`,`room_id`,`trx_type`,`amount`,`post_balance`,`biz_no`,`remark`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`) values 
(1,'TRX202601010001',101,1,500.00,500.00,'PAY-INIT','系统初始化充值',NULL,'2026-01-01 10:00:00',NULL,NULL,'0'),
(2,'TRX202601310001',101,2,-349.50,150.50,'UTIL202601','1月份电费结算',NULL,'2026-01-31 23:59:59',NULL,NULL,'0');

/*Table structure for table `dorm_allocation_log` */

DROP TABLE IF EXISTS `dorm_allocation_log`;

CREATE TABLE `dorm_allocation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学生/教工ID',
  `name` varchar(64) NOT NULL COMMENT '姓名(冗余字段,防用户被删)',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `room_no` varchar(32) NOT NULL COMMENT '房号(冗余字段)',
  `bed_label` varchar(32) DEFAULT NULL COMMENT '床号',
  `action_type` int NOT NULL COMMENT '动作: 10办理入住 20调宿换入 21调宿换出 30办理退宿',
  `action_date` date NOT NULL COMMENT '变动日期',
  `reason` varchar(255) DEFAULT NULL COMMENT '变动原因(如: 考研调宿, 毕业离校)',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号(0开始)',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_student` (`student_id`),
  KEY `idx_room` (`room_id`),
  KEY `idx_date` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-入住历史记录(审计)';

/*Data for the table `dorm_allocation_log` */

insert  into `dorm_allocation_log`(`id`,`student_id`,`name`,`room_id`,`room_no`,`bed_label`,`action_type`,`action_date`,`reason`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,10001,'梁梓豪',101,'101','1号床',10,'2026-02-04','24级新生统一分配',0,'admin','2026-02-04 15:14:38','',NULL,'0',NULL),
(2,10005,'郭展鹏',101,'101','2号床',10,'2026-02-04','24级新生统一分配',0,'admin','2026-02-04 15:14:38','',NULL,'0',NULL),
(3,10011,'王志豪',102,'102','1号床',10,'2026-02-04','24级新生统一分配',0,'admin','2026-02-04 15:14:38','',NULL,'0',NULL),
(4,10002,'何嘉欣',104,'201','1号床',10,'2026-02-04','24级新生统一分配',0,'admin','2026-02-04 15:14:38','',NULL,'0',NULL),
(5,10004,'陈晓彤',104,'201','2号床',10,'2026-02-04','24级新生统一分配',0,'admin','2026-02-04 15:14:38','',NULL,'0',NULL);

/*Table structure for table `dorm_application` */

DROP TABLE IF EXISTS `dorm_application`;

CREATE TABLE `dorm_application` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '申请人ID',
  `username` varchar(64) NOT NULL COMMENT '学号/工号',
  `real_name` varchar(64) NOT NULL COMMENT '姓名',
  `user_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '人员类型: 0-学生 1-教工 2-宿管',
  `gender` char(1) DEFAULT '0' COMMENT '性别(1男 2女)',
  `campus_id` bigint DEFAULT NULL COMMENT '所属校区ID',
  `type` tinyint(1) NOT NULL COMMENT '申请类型',
  `status` tinyint(1) DEFAULT '0' COMMENT '状态: 0-待审核 1-通过 2-驳回 3-撤销',
  `reason` varchar(500) DEFAULT NULL COMMENT '申请原因',
  `target_room_id` bigint DEFAULT NULL COMMENT '目标房间ID (调宿/入住具体房间用)',
  `target_bed_id` bigint DEFAULT NULL COMMENT '目标床位ID (精准调宿用)',
  `expect_type` varchar(64) DEFAULT NULL COMMENT '期望户型 (教工申请用，如: 单人间)',
  `start_date` date DEFAULT NULL COMMENT '开始日期 (假期留校用)',
  `end_date` date DEFAULT NULL COMMENT '结束日期 (假期留校用)',
  `emergency_name` varchar(64) DEFAULT NULL COMMENT '紧急联系人姓名',
  `emergency_relation` varchar(32) DEFAULT NULL COMMENT '与本人关系',
  `emergency_phone` varchar(20) DEFAULT NULL COMMENT '紧急联系人电话',
  `handle_by` varchar(64) DEFAULT NULL COMMENT '审批人姓名',
  `handle_time` datetime DEFAULT NULL COMMENT '审批时间',
  `handle_note` varchar(255) DEFAULT NULL COMMENT '审批意见/驳回原因',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `version` int DEFAULT '0' COMMENT '乐观锁版本',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_status_type` (`status`,`type`)
) ENGINE=InnoDB AUTO_INCREMENT=1006 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='综合住宿申请表';

/*Data for the table `dorm_application` */

insert  into `dorm_application`(`id`,`user_id`,`username`,`real_name`,`user_type`,`gender`,`campus_id`,`type`,`status`,`reason`,`target_room_id`,`target_bed_id`,`expect_type`,`start_date`,`end_date`,`emergency_name`,`emergency_relation`,`emergency_phone`,`handle_by`,`handle_time`,`handle_note`,`create_time`,`update_by`,`update_time`,`remark`,`del_flag`,`version`,`create_by`) values 
(1001,10011,'2024101','王志豪',0,'1',1,1,1,'24级新生入校申请',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'admin',NULL,'准予入住','2026-02-01 09:00:00','',NULL,NULL,'0',0,'system'),
(1002,10012,'2024102','李悦欣',0,'0',1,1,0,'24级新生入校申请',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-02-04 10:00:00','',NULL,NULL,'0',0,'system'),
(1003,10013,'2024103','张一鸣',0,'1',1,2,0,'宿舍空调漏水，申请调换',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-02-04 11:00:00','',NULL,NULL,'0',0,'system'),
(1004,20001,'T001','张铁柱',1,'1',1,1,1,'新入职教师单身公寓申请',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'admin',NULL,'已分配教工区','2026-02-01 08:30:00','',NULL,NULL,'0',0,'system'),
(1005,10021,'2024111','郑若男',0,'0',2,1,0,'研究生入校住宿申请',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-02-04 14:00:00','',NULL,NULL,'0',0,'system');

/*Table structure for table `dorm_bed` */

DROP TABLE IF EXISTS `dorm_bed`;

CREATE TABLE `dorm_bed` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '床位主键ID',
  `campus_id` bigint NOT NULL COMMENT '所属校区ID',
  `building_id` bigint NOT NULL COMMENT '所属楼宇ID',
  `floor_id` bigint DEFAULT NULL COMMENT '所属楼层 ID',
  `room_id` bigint NOT NULL COMMENT '所属房间ID',
  `bed_label` varchar(32) NOT NULL COMMENT '床号 (如: 1号床)',
  `sort_order` int DEFAULT '0' COMMENT '排序号',
  `occupant_id` bigint DEFAULT NULL COMMENT '当前占用者ID (关联ordinary_user)',
  `occupant_type` int DEFAULT '0' COMMENT '居住者类型',
  `res_status` int NOT NULL DEFAULT '21' COMMENT '状态码: 21空闲, 22正常使用',
  `status` int NOT NULL DEFAULT '20' COMMENT '生命周期: 20正常, 50维修, 80保留',
  `version` int DEFAULT '0' COMMENT '乐观锁版本',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  PRIMARY KEY (`id`),
  KEY `idx_room_status` (`room_id`,`status`),
  KEY `idx_occupant` (`occupant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-床位表';

/*Data for the table `dorm_bed` */

insert  into `dorm_bed`(`id`,`campus_id`,`building_id`,`floor_id`,`room_id`,`bed_label`,`sort_order`,`occupant_id`,`occupant_type`,`res_status`,`status`,`version`,`create_time`,`update_time`,`del_flag`,`remark`,`create_by`,`update_by`) values 
(1,1,1,1,101,'101-1',1,10001,0,22,20,0,'2026-02-04 18:53:09',NULL,'0',NULL,'system',''),
(2,1,1,1,101,'101-2',2,10005,0,22,20,0,'2026-02-04 18:53:09',NULL,'0',NULL,'system',''),
(3,1,1,1,101,'101-3',3,10011,0,22,20,0,'2026-02-04 18:53:09',NULL,'0',NULL,'system',''),
(4,1,1,1,101,'101-4',4,10020,0,22,20,0,'2026-02-04 18:53:09',NULL,'0',NULL,'system',''),
(5,1,1,1,102,'102-1',1,10025,0,22,20,0,'2026-02-04 18:53:09',NULL,'0',NULL,'system',''),
(6,1,1,1,102,'102-2',2,NULL,0,21,50,0,'2026-02-04 18:53:09',NULL,'0',NULL,'system',''),
(7,1,1,1,102,'102-3',3,NULL,0,21,20,0,'2026-02-04 18:53:09',NULL,'0',NULL,'system',''),
(8,1,1,1,102,'102-4',4,NULL,0,21,20,0,'2026-02-04 18:53:09',NULL,'0',NULL,'system',''),
(9,1,1,3,301,'301-1',1,NULL,1,21,80,0,'2026-02-04 18:53:09',NULL,'0',NULL,'system','');

/*Table structure for table `dorm_building` */

DROP TABLE IF EXISTS `dorm_building`;

CREATE TABLE `dorm_building` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint NOT NULL COMMENT '校区ID',
  `building_name` varchar(64) NOT NULL COMMENT '楼栋名',
  `building_no` varchar(32) NOT NULL COMMENT '楼栋编号',
  `floor_count` int NOT NULL COMMENT '总层数',
  `gender_limit` int NOT NULL DEFAULT '3' COMMENT '性别限制: 1男, 2女, 3混合',
  `usage_type` int NOT NULL DEFAULT '0' COMMENT '用途: 0学生, 1教职工',
  `manager_id` bigint DEFAULT NULL COMMENT '宿管负责人ID',
  `location` varchar(255) DEFAULT NULL COMMENT '楼宇地理位置/经纬度坐标',
  `status` int NOT NULL DEFAULT '20' COMMENT '生命周期: 0停止, 20正常, 40装修, 50维修, 60损坏',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号(0开始)',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_campus` (`campus_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-楼栋表';

/*Data for the table `dorm_building` */

insert  into `dorm_building`(`id`,`campus_id`,`building_name`,`building_no`,`floor_count`,`gender_limit`,`usage_type`,`manager_id`,`location`,`status`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,'慎思园 1 号楼','SSY-01',6,1,0,NULL,NULL,20,0,'system','2026-02-04 18:53:09','',NULL,'0',NULL),
(2,1,'慎思园 2 号楼','SSY-02',6,2,0,NULL,NULL,20,0,'system','2026-02-04 18:53:09','',NULL,'0',NULL),
(3,1,'格致园 3 号楼','GZY-03',12,3,1,NULL,NULL,20,0,'system','2026-02-04 18:53:09','',NULL,'0',NULL),
(4,2,'北校园 1 号楼','BXY-01',5,1,0,NULL,NULL,40,0,'system','2026-02-04 18:53:09','',NULL,'0',NULL);

/*Table structure for table `dorm_change_request` */

DROP TABLE IF EXISTS `dorm_change_request`;

CREATE TABLE `dorm_change_request` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '申请学生ID',
  `current_room_id` bigint NOT NULL COMMENT '当前房间ID',
  `target_room_id` bigint DEFAULT NULL COMMENT '目标房间ID',
  `type` int DEFAULT '0' COMMENT '类型: 0换房 1退宿 2互换',
  `reason` varchar(500) DEFAULT NULL COMMENT '申请原因',
  `status` int DEFAULT '0' COMMENT '状态: 0待审 1通过 2驳回',
  `swap_student_id` bigint DEFAULT NULL COMMENT '互换对象ID',
  `audit_msg` varchar(255) DEFAULT NULL COMMENT '审核意见',
  `apply_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号(0开始)',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-调宿申请';

/*Data for the table `dorm_change_request` */

insert  into `dorm_change_request`(`id`,`student_id`,`current_room_id`,`target_room_id`,`type`,`reason`,`status`,`swap_student_id`,`audit_msg`,`apply_time`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,10002,101,NULL,0,'室友打呼噜声音太大，严重影响睡眠，申请调换到安静的宿舍。',0,NULL,NULL,'2026-02-02 16:44:55',0,'admin','2026-02-02 16:44:55','',NULL,'0',NULL),
(2,10010,201,NULL,0,'想和同班同学住在一起。',1,NULL,'同意申请，已安排至 102 宿舍。','2026-01-28 16:44:55',0,'admin','2026-02-02 16:44:55','',NULL,'0',NULL);

/*Table structure for table `dorm_fixed_asset` */

DROP TABLE IF EXISTS `dorm_fixed_asset`;

CREATE TABLE `dorm_fixed_asset` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `asset_name` varchar(100) NOT NULL COMMENT '资产名称',
  `asset_code` varchar(64) NOT NULL COMMENT '资产编号',
  `category` int NOT NULL COMMENT '类型: 1家具 2电器',
  `room_id` bigint NOT NULL COMMENT '所属房间ID',
  `price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '价格',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '资产数量',
  `status` int DEFAULT '20' COMMENT '状态: 20-正常, 50-维修中, 60-已损坏, 0-已报废',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号(0开始)',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_asset_code` (`asset_code`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资产表';

/*Data for the table `dorm_fixed_asset` */

insert  into `dorm_fixed_asset`(`id`,`asset_name`,`asset_code`,`category`,`room_id`,`price`,`quantity`,`status`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,'标准学生床','RM-101-BED',1,101,850.00,4,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','含爬梯和护栏'),
(2,'标准学生床','RM-102-BED',1,102,850.00,4,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','含爬梯和护栏'),
(3,'标准学生床','RM-103-BED',1,103,850.00,4,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','含爬梯和护栏'),
(4,'书桌','RM-101-DSK',1,101,450.00,4,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','钢木组合'),
(5,'书桌','RM-102-DSK',1,102,450.00,4,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','钢木组合'),
(6,'书桌','RM-103-DSK',1,103,450.00,4,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','钢木组合'),
(7,'靠背椅','RM-101-CHR',1,101,120.00,3,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','实木'),
(8,'靠背椅','RM-102-CHR',1,102,120.00,4,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','实木'),
(9,'靠背椅','RM-103-CHR',1,103,120.00,4,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','实木'),
(10,'储物书柜','RM-101-CAB',1,101,300.00,4,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','带锁'),
(11,'储物书柜','RM-102-CAB',1,102,300.00,4,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','带锁'),
(12,'储物书柜','RM-103-CAB',1,103,300.00,4,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','带锁'),
(13,'宿舍门钥匙','RM-101-KEY',3,101,10.00,3,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','每人一把'),
(14,'宿舍门钥匙','RM-102-KEY',3,102,10.00,4,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','每人一把'),
(15,'宿舍门钥匙','RM-103-KEY',3,103,10.00,4,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','每人一把'),
(16,'变频空调','RM-101-AC',2,101,3200.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','1.5匹'),
(17,'变频空调','RM-102-AC',2,102,3200.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','遥控器缺失'),
(18,'变频空调','RM-103-AC',2,103,3200.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','1.5匹'),
(19,'铝合金窗户','RM-101-WIN',3,101,1500.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','含玻璃'),
(20,'铝合金窗户','RM-102-WIN',3,102,1500.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','含玻璃'),
(21,'铝合金窗户','RM-103-WIN',3,103,1500.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','含玻璃'),
(22,'防尘纱窗','RM-101-SCR',3,101,150.00,1,4,0,'system','2026-02-02 17:34:38','','2026-02-02 17:34:38','0','纱网破大洞，需更换'),
(23,'防尘纱窗','RM-102-SCR',3,102,150.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','金刚网'),
(24,'防尘纱窗','RM-103-SCR',3,103,150.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','金刚网'),
(25,'淋浴花洒','RM-101-SHW',3,101,200.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','增压出水'),
(26,'淋浴花洒','RM-102-SHW',3,102,200.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','增压出水'),
(27,'淋浴花洒','RM-103-SHW',3,103,200.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','增压出水'),
(28,'浴室镜','RM-101-MIR',1,101,100.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','防雾'),
(29,'浴室镜','RM-102-MIR',1,102,100.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','防雾'),
(30,'浴室镜','RM-103-MIR',1,103,100.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','防雾'),
(31,'公牛插座面板','RM-101-SOC',3,101,35.00,6,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','86型五孔'),
(32,'公牛插座面板','RM-102-SOC',3,102,35.00,6,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','86型五孔'),
(33,'公牛插座面板','RM-103-SOC',3,103,35.00,6,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','86型五孔'),
(34,'给排水管网','RM-101-PIPE',3,101,800.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','PPR/PVC材质'),
(35,'给排水管网','RM-102-PIPE',3,102,800.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','PPR/PVC材质'),
(36,'给排水管网','RM-103-PIPE',3,103,800.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','PPR/PVC材质'),
(37,'室内电路','RM-101-WIRE',3,101,1000.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','含开关和灯具'),
(38,'室内电路','RM-102-WIRE',3,102,1000.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','含开关和灯具'),
(39,'室内电路','RM-103-WIRE',3,103,1000.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','含开关和灯具'),
(40,'智能水表','RM-101-M-W',2,101,250.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','远传水表'),
(41,'智能水表','RM-102-M-W',2,102,250.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','远传水表'),
(42,'智能水表','RM-103-M-W',2,103,250.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','远传水表'),
(43,'智能电表','RM-101-M-E',2,101,450.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','预付费电表'),
(44,'智能电表','RM-102-M-E',2,102,450.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','预付费电表'),
(45,'智能电表','RM-103-M-E',2,103,450.00,1,20,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','预付费电表'),
(46,'宿舍门钥匙','RM-101-KEY-LOST',3,101,10.00,1,60,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','学生上报丢失，需补配'),
(47,'靠背椅','RM-101-CHR-BROKEN',1,101,120.00,1,50,0,'system','2026-02-02 17:34:38','','2026-02-05 17:48:14','0','椅腿松动，报修中');

/*Table structure for table `dorm_floor` */

DROP TABLE IF EXISTS `dorm_floor`;

CREATE TABLE `dorm_floor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `building_id` bigint NOT NULL COMMENT '楼栋ID',
  `floor_num` int NOT NULL COMMENT '楼层号',
  `gender_limit` int DEFAULT '0' COMMENT '性别限制: 0混合 1男 2女',
  `usage_type` int NOT NULL DEFAULT '0' COMMENT '用途: 同楼栋',
  `status` int NOT NULL DEFAULT '20' COMMENT '生命周期: 同楼栋',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号(0开始)',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_building` (`building_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-楼层表';

/*Data for the table `dorm_floor` */

insert  into `dorm_floor`(`id`,`building_id`,`floor_num`,`gender_limit`,`usage_type`,`status`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,1,1,0,20,0,'system','2026-02-04 18:53:09','',NULL,'0',NULL),
(2,2,1,2,0,20,0,'system','2026-02-04 18:53:09','',NULL,'0',NULL),
(3,3,1,1,1,20,0,'system','2026-02-04 18:53:09','',NULL,'0',NULL),
(4,3,2,2,1,20,0,'system','2026-02-04 18:53:09','',NULL,'0',NULL),
(5,1,2,1,0,50,0,'system','2026-02-04 18:53:09','',NULL,'0',NULL),
(6,4,1,1,0,40,0,'system','2026-02-04 18:53:09','',NULL,'0',NULL);

/*Table structure for table `dorm_lost_found` */

DROP TABLE IF EXISTS `dorm_lost_found`;

CREATE TABLE `dorm_lost_found` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `item_name` varchar(64) NOT NULL COMMENT '物品名称',
  `description` varchar(500) DEFAULT NULL COMMENT '详细特征 (如: 蓝色外壳、带挂坠)',
  `location` varchar(128) DEFAULT NULL COMMENT '发现/丢失地点',
  `image_url` varchar(512) DEFAULT NULL COMMENT '物品照片URL (支持长路径)',
  `type` tinyint(1) DEFAULT '1' COMMENT '类型 (1:捡到物品 2:丢失物品)',
  `gender_limit` int DEFAULT '0' COMMENT '可见性别限制: 0不限 1男 2女',
  `status` tinyint(1) DEFAULT '0' COMMENT '状态 (0:寻找中 1:已认领/已找回)',
  `version` int DEFAULT '0' COMMENT '版本号',
  `claim_method` tinyint(1) DEFAULT '1' COMMENT '领取方式 (1:联系发布人 2:前往宿管站)',
  `claim_location` varchar(255) DEFAULT NULL COMMENT '具体领取地点',
  `contact_name` varchar(64) DEFAULT NULL COMMENT '联系人称呼',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话 (脱敏显示)',
  `publish_user_id` bigint DEFAULT NULL COMMENT '发布人用户ID',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者账号',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志(0正常 2删除)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_type_status` (`type`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-失物招领表';

/*Data for the table `dorm_lost_found` */

insert  into `dorm_lost_found`(`id`,`item_name`,`description`,`location`,`image_url`,`type`,`gender_limit`,`status`,`version`,`claim_method`,`claim_location`,`contact_name`,`contact_phone`,`publish_user_id`,`create_by`,`update_by`,`create_time`,`update_time`,`del_flag`,`remark`) values 
(1,'华为手机','黑色 P60, 屏幕有钢化膜破损','南苑食堂',NULL,1,0,0,0,2,NULL,'张宿管','138****8888',3,'admin','','2026-02-04 10:00:00',NULL,'0','已交至值班室'),
(5,'小天才手表','粉红色，屏幕有划痕','慎思园1号楼大厅',NULL,1,0,0,0,1,NULL,'值班宿管','13922223333',NULL,'admin','','2026-02-04 16:28:03',NULL,'0',NULL),
(6,'校园卡','学号2024105, 姓名陈冠宇','东苑二食堂','/profile/lost_found/example_card.png',1,0,1,0,1,NULL,'刘同学','135****4444',10001,'admin','','2026-02-04 16:28:03','2026-02-06 15:32:37','0',NULL),
(7,'英语课本','书名《大学英语》，扉页写有周梦琪','格致园3号楼自习室',NULL,1,0,0,0,1,NULL,'王同学','136****5555',NULL,'admin','','2026-02-04 16:28:03',NULL,'0',NULL),
(8,'雨伞','天堂伞，黑色大号','教学楼A区101',NULL,2,0,0,0,1,NULL,'何嘉欣','13811110002',NULL,'2024002','','2026-02-04 16:28:03',NULL,'0',NULL);

/*Table structure for table `dorm_meter` */

DROP TABLE IF EXISTS `dorm_meter`;

CREATE TABLE `dorm_meter` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `meter_no` varchar(64) NOT NULL COMMENT '设备编号(物理ID)',
  `name` varchar(50) DEFAULT NULL COMMENT '设备名称(如:空调电表)',
  `type` int NOT NULL COMMENT '类型: 10冷水 11热水 20照明电 21空调电',
  `current_reading` decimal(10,2) DEFAULT '0.00' COMMENT '当前读数',
  `last_reading` decimal(10,2) DEFAULT '0.00' COMMENT '上月读数',
  `power_limit` int DEFAULT NULL COMMENT '功率限制(W),仅电表有效',
  `is_online` tinyint DEFAULT '1' COMMENT '在线状态: 1在线 0离线',
  `switch_status` tinyint DEFAULT '1' COMMENT '阀门状态: 1通 0断',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号(0开始)',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meter_no` (`meter_no`),
  KEY `idx_room` (`room_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物联-统一仪表';

/*Data for the table `dorm_meter` */

insert  into `dorm_meter`(`id`,`room_id`,`meter_no`,`name`,`type`,`current_reading`,`last_reading`,`power_limit`,`is_online`,`switch_status`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,101,'M-101-E','101照明电表',20,1245.50,0.00,800,1,1,0,'system','2026-02-04 15:18:49','',NULL,'0',NULL),
(2,104,'M-201-E','201空调电表',21,3560.20,0.00,2000,1,1,0,'system','2026-02-04 15:18:49','',NULL,'0',NULL),
(3,102,'M-102-E','102通用电表',20,450.80,0.00,800,0,1,0,'system','2026-02-04 15:18:49','',NULL,'0',NULL);

/*Table structure for table `dorm_room` */

DROP TABLE IF EXISTS `dorm_room`;

CREATE TABLE `dorm_room` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint DEFAULT NULL COMMENT '所属校区ID (冗余字段,用于加速查询)',
  `building_id` bigint DEFAULT NULL COMMENT '所属楼宇ID (冗余字段,用于加速查询)',
  `floor_id` bigint NOT NULL COMMENT '楼层ID',
  `floor_num` int DEFAULT NULL COMMENT '物理楼层号 (如: 3层)',
  `room_no` varchar(32) NOT NULL COMMENT '房号',
  `apartment_type` varchar(32) DEFAULT '四人间' COMMENT '房型',
  `capacity` int DEFAULT '4' COMMENT '核定人数',
  `accommodation_fee` decimal(10,2) DEFAULT '0.00' COMMENT '住宿费(元/年)',
  `current_num` int DEFAULT '0' COMMENT '当前人数',
  `gender` char(1) NOT NULL DEFAULT '1' COMMENT '性别: 0女, 1男',
  `status` int NOT NULL DEFAULT '20' COMMENT '生命周期: 20正常, 50维修, 60损坏, 80保留',
  `safety_level` tinyint DEFAULT '1' COMMENT '安全等级 (1-安全, 2-警告, 3-危险禁止分配)',
  `resource_status` int NOT NULL DEFAULT '21' COMMENT '资源码: 21空闲, 23未满, 24充裕, 25紧张, 26已满',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号(0开始)',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room` (`building_id`,`room_no`)
) ENGINE=InnoDB AUTO_INCREMENT=402 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-房间表';

/*Data for the table `dorm_room` */

insert  into `dorm_room`(`id`,`campus_id`,`building_id`,`floor_id`,`floor_num`,`room_no`,`apartment_type`,`capacity`,`accommodation_fee`,`current_num`,`gender`,`status`,`safety_level`,`resource_status`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(101,1,1,1,NULL,'101','四人间',4,0.00,4,'1',20,1,26,0,'system','2026-02-04 18:53:09','',NULL,'0',NULL),
(102,1,1,1,NULL,'102','四人间',4,0.00,1,'1',20,1,24,0,'system','2026-02-04 18:53:09','',NULL,'0',NULL),
(103,1,1,1,NULL,'103','四人间',4,0.00,3,'1',20,1,25,0,'system','2026-02-04 18:53:09','',NULL,'0',NULL),
(104,1,1,1,NULL,'104','四人间',4,0.00,0,'1',20,1,21,0,'system','2026-02-04 18:53:09','',NULL,'0',NULL),
(201,1,2,2,NULL,'101','四人间',4,0.00,2,'0',20,1,23,0,'system','2026-02-04 18:53:09','',NULL,'0',NULL),
(301,1,3,3,NULL,'101','四人间',1,0.00,0,'1',80,1,21,0,'system','2026-02-04 18:53:09','',NULL,'0',NULL),
(401,2,4,6,NULL,'101','四人间',4,0.00,0,'1',40,1,21,0,'system','2026-02-04 18:53:09','',NULL,'0',NULL);

/*Table structure for table `dorm_room_wallet` */

DROP TABLE IF EXISTS `dorm_room_wallet`;

CREATE TABLE `dorm_room_wallet` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `balance` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '账户余额',
  `total_recharge` decimal(10,2) DEFAULT '0.00' COMMENT '累计充值',
  `total_consume` decimal(10,2) DEFAULT '0.00' COMMENT '累计消费',
  `status` int DEFAULT '1' COMMENT '状态: 1正常 2欠费冻结 3停用',
  `last_billing_time` datetime DEFAULT NULL COMMENT '上次月度计费时间',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号(0开始)',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room` (`room_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-房间钱包';

/*Data for the table `dorm_room_wallet` */

insert  into `dorm_room_wallet`(`id`,`room_id`,`balance`,`total_recharge`,`total_consume`,`status`,`last_billing_time`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,101,150.50,500.00,349.50,1,NULL,0,'system','2026-02-04 15:18:49','',NULL,'0',NULL),
(2,102,0.50,100.00,94.80,1,NULL,0,'system','2026-02-04 15:18:49','','2026-02-06 15:32:24','0',NULL),
(3,104,-12.30,200.00,212.30,2,NULL,0,'system','2026-02-04 15:18:49','',NULL,'0',NULL),
(4,103,100.00,100.00,0.00,1,NULL,0,'system','2026-02-04 15:18:49','',NULL,'0',NULL),
(5,105,88.00,200.00,112.00,1,NULL,0,'system','2026-02-04 15:18:49','',NULL,'0',NULL),
(6,106,0.00,0.00,0.00,1,NULL,0,'system','2026-02-04 15:18:49','',NULL,'0',NULL),
(7,107,200.00,200.00,0.00,1,NULL,0,'system','2026-02-04 15:18:49','',NULL,'0',NULL),
(8,108,45.60,100.00,54.40,1,NULL,0,'system','2026-02-04 15:18:49','',NULL,'0',NULL);

/*Table structure for table `sys_admin_user` */

DROP TABLE IF EXISTS `sys_admin_user`;

CREATE TABLE `sys_admin_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(64) NOT NULL COMMENT '登录账号',
  `password` varchar(100) NOT NULL COMMENT '加密密码',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
  `gender` char(1) NOT NULL DEFAULT '1' COMMENT '性别:0女 1男',
  `phone` varchar(255) NOT NULL DEFAULT '',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `landline` varchar(20) DEFAULT '' COMMENT '座机/固定电话',
  `id_card` varchar(255) NOT NULL DEFAULT '',
  `ethnicity` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '汉族' COMMENT '民族',
  `hometown` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '未知' COMMENT '籍贯',
  `outside_address` varchar(500) DEFAULT '' COMMENT '校外居住地址(加密存储)',
  `entry_date` date DEFAULT NULL COMMENT '入职日期',
  `entry_year` int DEFAULT NULL COMMENT '入职年份',
  `emergency_contact` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '紧急联系人',
  `emergency_phone` varchar(255) NOT NULL DEFAULT '',
  `emergency_relation` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '关系(限枚举值)',
  `residence_type` int NOT NULL DEFAULT '1' COMMENT '居住类型(0住校 1校外)',
  `avatar` varchar(500) DEFAULT '' COMMENT '头像URL',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态:0正常 1停用',
  `campus_status` int DEFAULT '1' COMMENT '在岗状态: 1在岗 0休假',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `is_initial_pwd` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否初始密码',
  `campus_id` bigint DEFAULT NULL COMMENT '所属校区ID',
  `dept_id` bigint DEFAULT NULL COMMENT '所属部门ID',
  `college_id` bigint DEFAULT NULL COMMENT '所属学院ID',
  `building_id` bigint DEFAULT NULL COMMENT '负责楼栋ID',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号(0开始)',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `home_address` varchar(500) DEFAULT '' COMMENT '家庭居住地址/身份证地址(加密存储)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_name` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-后台管理员表';

/*Data for the table `sys_admin_user` */

insert  into `sys_admin_user`(`id`,`username`,`password`,`real_name`,`nickname`,`gender`,`phone`,`email`,`landline`,`id_card`,`ethnicity`,`hometown`,`outside_address`,`entry_date`,`entry_year`,`emergency_contact`,`emergency_phone`,`emergency_relation`,`residence_type`,`avatar`,`status`,`campus_status`,`last_login_time`,`is_initial_pwd`,`campus_id`,`dept_id`,`college_id`,`building_id`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`,`home_address`) values 
(1,'admin','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','问榆树','超级管理员','1','13800138000',NULL,'','11010119800101001X','汉族','未知','广州市海珠区中山大学教工公寓','2024-01-01',NULL,'陈夫人','13911112222','配偶',1,'','0',1,NULL,1,1,101,NULL,NULL,0,'system','2026-02-04 14:57:16','','2026-02-04 15:28:11','0',NULL,'广东省广州市越秀区新港西路135号'),
(2,'li_manager','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','李淑珍',NULL,'0','13900000002',NULL,'','44010119750520002X','汉族','未知','','2024-01-01',NULL,'','','',1,'','0',1,NULL,1,1,102,NULL,NULL,0,'system','2026-02-04 14:57:16','','2026-02-04 15:28:11','0',NULL,''),
(3,'huang_counselor','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','黄志强',NULL,'1','13900000003',NULL,'','44010119900910003X','汉族','未知','','2024-01-01',NULL,'','','',1,'','0',1,NULL,1,3,101,NULL,NULL,0,'system','2026-02-04 14:57:16','','2026-02-04 15:28:11','0',NULL,''),
(4,'zhang_master','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','张大为',NULL,'1','13900000004',NULL,'','44010119850808004X','汉族','未知','','2024-01-01',NULL,'','','',1,'','0',1,NULL,1,1,102,NULL,NULL,0,'system','2026-02-04 14:57:16','','2026-02-04 15:28:11','0',NULL,''),
(5,'liang_adm','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','梁美玲',NULL,'0','13900000005',NULL,'','44010119820315005X','汉族','未知','','2024-01-01',NULL,'','','',1,'','0',1,NULL,1,1,101,NULL,NULL,0,'system','2026-02-04 14:57:16','','2026-02-04 15:28:11','0',NULL,''),
(6,'wu_security','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','吴广德',NULL,'1','13900000006',NULL,'','44010119781212006X','汉族','未知','','2024-01-01',NULL,'','','',1,'','0',1,NULL,1,2,103,NULL,NULL,0,'system','2026-02-04 14:57:16','','2026-02-04 15:28:11','0',NULL,''),
(7,'zhao_dorm','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','赵爱华',NULL,'0','13900000007',NULL,'','44010119760707007X','汉族','未知','','2024-01-01',NULL,'','','',1,'','0',1,NULL,1,1,102,NULL,1,0,'system','2026-02-04 14:57:16','','2026-02-06 15:32:42','0',NULL,''),
(8,'lin_counselor','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','林思源',NULL,'0','13900000008',NULL,'','44010119951111008X','汉族','未知','','2024-01-01',NULL,'','','',1,'','0',1,NULL,1,3,101,NULL,NULL,0,'system','2026-02-04 14:57:16','','2026-02-04 15:28:11','0',NULL,'');

/*Table structure for table `sys_algorithm_config` */

DROP TABLE IF EXISTS `sys_algorithm_config`;

CREATE TABLE `sys_algorithm_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` varchar(100) NOT NULL COMMENT '配置值',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号(0开始)',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='配置-算法与参数';

/*Data for the table `sys_algorithm_config` */

insert  into `sys_algorithm_config`(`id`,`config_key`,`config_value`,`description`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,'WEIGHT_SLEEP_TIME','0.4','分配权重：作息时间一致性',0,'system','2026-02-02 16:44:55','',NULL,'0',NULL),
(2,'WEIGHT_HYGIENE','0.3','分配权重：卫生习惯一致性',0,'system','2026-02-02 16:44:55','',NULL,'0',NULL),
(3,'WEIGHT_GAME','0.1','分配权重：游戏习惯',0,'system','2026-02-02 16:44:55','',NULL,'0',NULL),
(4,'WEIGHT_MAJOR','0.2','分配权重：专业聚集度',0,'system','2026-02-02 16:44:55','',NULL,'0',NULL),
(5,'WEIGHT_SOCIAL_BREAKING','0.15','破冰权重：二次元/COS同好加分',0,'system','2026-02-05 17:08:12','',NULL,'0',NULL),
(6,'WEIGHT_MBTI_BALANCE','0.25','平衡权重：MBTI 社交 E/I 平衡分',0,'system','2026-02-05 17:08:12','',NULL,'0',NULL);

/*Table structure for table `sys_campus` */

DROP TABLE IF EXISTS `sys_campus`;

CREATE TABLE `sys_campus` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '校区主键ID',
  `campus_name` varchar(64) NOT NULL COMMENT '校区名称 (如: 北校区)',
  `campus_code` varchar(32) NOT NULL COMMENT '校区编码 (唯一, 如: CAMP-N)',
  `address` varchar(255) NOT NULL COMMENT '校区地理位置',
  `status` int NOT NULL DEFAULT '20' COMMENT '生命周期: 20正常, 30暂停',
  `price_water_cold` decimal(10,2) NOT NULL DEFAULT '3.50' COMMENT '冷水单价(元/吨)',
  `price_water_hot` decimal(10,2) NOT NULL DEFAULT '18.00' COMMENT '热水单价(元/吨)',
  `price_electric` decimal(10,2) NOT NULL DEFAULT '0.58' COMMENT '电费单价(元/度)',
  `version` int DEFAULT '0' COMMENT '乐观锁版本',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`campus_code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-校区表';

/*Data for the table `sys_campus` */

insert  into `sys_campus`(`id`,`campus_name`,`campus_code`,`address`,`status`,`price_water_cold`,`price_water_hot`,`price_electric`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,'广州校区南校园','SYSU-GZ-S','广州市海珠区新港西路135号',20,3.50,18.00,0.58,0,'system','2026-02-04 18:53:09','',NULL,'0',NULL),
(2,'广州校区北校园','SYSU-GZ-N','广州市越秀区中山二路74号',20,3.50,18.00,0.58,0,'system','2026-02-04 18:53:09','',NULL,'0',NULL),
(3,'珠海校区','SYSU-ZH','珠海市唐家湾镇',30,3.20,15.00,0.60,0,'system','2026-02-04 18:53:09','',NULL,'0',NULL);

/*Table structure for table `sys_college` */

DROP TABLE IF EXISTS `sys_college`;

CREATE TABLE `sys_college` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint NOT NULL COMMENT '所属校区ID',
  `name` varchar(64) NOT NULL COMMENT '学院名称',
  `code` varchar(32) NOT NULL COMMENT '学院代码',
  `sort` int DEFAULT '0' COMMENT '排序',
  `description` varchar(500) DEFAULT NULL COMMENT '学院简介',
  `status` tinyint DEFAULT '1' COMMENT '状态: 1正常 0停用',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号(0开始)',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_campus` (`campus_id`)
) ENGINE=InnoDB AUTO_INCREMENT=109 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-学院表';

/*Data for the table `sys_college` */

insert  into `sys_college`(`id`,`campus_id`,`name`,`code`,`sort`,`description`,`status`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(101,1,'计算机学院','CSE',1,NULL,1,0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(102,1,'电子与通信工程学院','ECE',2,NULL,1,0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(103,3,'传播与设计学院','SCD',3,NULL,1,0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(104,2,'中山医学院','SMS',4,NULL,1,0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(105,1,'岭南学院','LNM',5,NULL,1,0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(106,1,'法学院','LAW',6,NULL,1,0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(107,2,'心理学系','PSY',7,NULL,1,0,'system','2026-02-04 14:57:16','','2026-02-04 18:52:05','0',NULL),
(108,3,'海洋科学学院','MAR',8,NULL,1,0,'system','2026-02-04 14:57:16','','2026-02-04 18:51:48','0',NULL);

/*Table structure for table `sys_dept` */

DROP TABLE IF EXISTS `sys_dept`;

CREATE TABLE `sys_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint NOT NULL COMMENT '所属校区ID',
  `name` varchar(64) NOT NULL COMMENT '部门名称',
  `code` varchar(64) DEFAULT NULL COMMENT '部门编码',
  `intro` varchar(500) DEFAULT NULL COMMENT '部门简介',
  `parent_id` bigint DEFAULT '0' COMMENT '父部门ID',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` char(1) DEFAULT '0' COMMENT '状态: 0正常 1停用',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号(0开始)',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_campus` (`campus_id`)
) ENGINE=InnoDB AUTO_INCREMENT=109 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-部门表';

/*Data for the table `sys_dept` */

insert  into `sys_dept`(`id`,`campus_id`,`name`,`code`,`intro`,`parent_id`,`sort`,`status`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(101,1,'学生工作部','XGB',NULL,0,0,'1',0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(102,1,'总务部(后勤)','ZWB',NULL,0,0,'1',0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(103,1,'校园安全管理处','BWC',NULL,0,0,'1',0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(104,1,'团委','TW',NULL,0,0,'1',0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(105,1,'网络与信息中心','WXC',NULL,0,0,'1',0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(106,2,'北校园管委会','BXY',NULL,0,0,'1',0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(107,3,'东校园物业中心','DXY',NULL,0,0,'1',0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(108,1,'教务部','JWB',NULL,0,0,'1',0,'system','2026-02-04 14:57:16','',NULL,'0',NULL);

/*Table structure for table `sys_login_log` */

DROP TABLE IF EXISTS `sys_login_log`;

CREATE TABLE `sys_login_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) DEFAULT '' COMMENT '登录账号',
  `ipaddr` varchar(128) DEFAULT '' COMMENT 'IP地址',
  `status` char(1) DEFAULT '0' COMMENT '状态: 0成功 1失败',
  `msg` varchar(255) DEFAULT '' COMMENT '提示信息',
  `access_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号(0开始)',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='日志-登录';

/*Data for the table `sys_login_log` */

insert  into `sys_login_log`(`id`,`username`,`ipaddr`,`status`,`msg`,`access_time`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,'admin','127.0.0.1','0','登录成功','2026-02-02 14:44:55',0,'system','2026-02-02 16:44:55','',NULL,'0',NULL),
(2,'admin','127.0.0.1','1','密码错误','2026-02-02 14:44:55',0,'system','2026-02-02 16:44:55','',NULL,'0',NULL),
(3,'dorm_manager','192.168.1.5','0','登录成功','2026-02-01 16:44:55',0,'system','2026-02-02 16:44:55','',NULL,'0',NULL);

/*Table structure for table `sys_major` */

DROP TABLE IF EXISTS `sys_major`;

CREATE TABLE `sys_major` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `college_id` bigint NOT NULL COMMENT '所属学院ID',
  `name` varchar(50) NOT NULL COMMENT '专业名称',
  `short_name` varchar(32) DEFAULT NULL COMMENT '专业简称/代码',
  `level` varchar(20) DEFAULT '本科' COMMENT '层次',
  `duration` int DEFAULT '4' COMMENT '学制',
  `sort` int DEFAULT '0' COMMENT '排序',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号(0开始)',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_college` (`college_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2009 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-专业表';

/*Data for the table `sys_major` */

insert  into `sys_major`(`id`,`college_id`,`name`,`short_name`,`level`,`duration`,`sort`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1001,101,'软件工程',NULL,'本科',4,0,0,'system','2026-02-04 14:58:30','',NULL,'0',NULL),
(1002,101,'计算机科学与技术',NULL,'本科',4,0,0,'system','2026-02-04 14:58:30','',NULL,'0',NULL),
(1003,101,'人工智能',NULL,'本科',4,0,0,'system','2026-02-04 14:58:30','',NULL,'0',NULL),
(1004,102,'电子信息工程',NULL,'本科',4,0,0,'system','2026-02-04 14:58:30','',NULL,'0',NULL),
(1005,102,'通信工程',NULL,'本科',4,0,0,'system','2026-02-04 14:58:30','',NULL,'0',NULL),
(1006,103,'传播学',NULL,'本科',4,0,0,'system','2026-02-04 14:58:30','',NULL,'0',NULL),
(1007,104,'临床医学',NULL,'本科',5,0,0,'system','2026-02-04 14:58:30','',NULL,'0',NULL),
(1008,105,'经济学',NULL,'本科',4,0,0,'system','2026-02-04 14:58:30','',NULL,'0',NULL),
(2001,101,'大数据技术',NULL,'专科',3,0,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(2002,101,'软件工程',NULL,'专升本',2,0,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(2003,104,'基础医学',NULL,'硕士研究生',3,0,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(2004,104,'内科学',NULL,'博士研究生',3,0,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(2005,106,'民商法学',NULL,'硕士研究生',3,0,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(2006,105,'工商管理',NULL,'硕士研究生',2,0,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(2007,107,'应用心理学',NULL,'本科',4,0,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(2008,108,'海洋生物资源',NULL,'博士研究生',4,0,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL);

/*Table structure for table `sys_notice` */

DROP TABLE IF EXISTS `sys_notice`;

CREATE TABLE `sys_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公告主键ID',
  `title` varchar(128) NOT NULL COMMENT '标题',
  `type` tinyint(1) DEFAULT '1' COMMENT '类型 (1:常规通知 2:放假安排 3:安全预警)',
  `level` tinyint(1) DEFAULT '0' COMMENT '紧急程度 (0:普通 1:重要)',
  `content` longtext COMMENT '富文本内容',
  `status` char(1) DEFAULT '0' COMMENT '状态 (0:发布中 1:已撤回)',
  `version` int DEFAULT '0' COMMENT '版本号',
  `create_by` varchar(64) DEFAULT '' COMMENT '发布人账号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-通知公告表';

/*Data for the table `sys_notice` */

insert  into `sys_notice`(`id`,`title`,`type`,`level`,`content`,`status`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`,`del_flag`) values 
(1,'关于2026年春季开学的通知',1,1,'各位同学，请于2月25日前返校...','0',0,'admin','2026-02-01 09:00:00','',NULL,NULL,'0');

/*Table structure for table `sys_oper_log` */

DROP TABLE IF EXISTS `sys_oper_log`;

CREATE TABLE `sys_oper_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(50) DEFAULT '' COMMENT '模块标题',
  `business_type` int DEFAULT '0' COMMENT '业务类型',
  `method` varchar(100) DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) DEFAULT '' COMMENT '请求方式',
  `oper_name` varchar(50) DEFAULT '' COMMENT '操作人员',
  `oper_url` varchar(255) DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) DEFAULT '' COMMENT '主机地址',
  `oper_param` varchar(2000) DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) DEFAULT '' COMMENT '返回参数',
  `status` int DEFAULT '0' COMMENT '操作状态',
  `error_msg` varchar(2000) DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号(0开始)',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='日志-操作';

/*Data for the table `sys_oper_log` */

insert  into `sys_oper_log`(`id`,`title`,`business_type`,`method`,`request_method`,`oper_name`,`oper_url`,`oper_ip`,`oper_param`,`json_result`,`status`,`error_msg`,`oper_time`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,'房间管理',1,'addRoom','POST','admin','/dorm/room','127.0.0.1','','',0,'','2026-02-02 15:44:55',0,'system','2026-02-02 16:44:55','',NULL,'0',NULL),
(2,'分配床位',2,'assignBed','POST','admin','/dorm/bed/assign','127.0.0.1','','',0,'','2026-02-02 16:14:55',0,'system','2026-02-02 16:44:55','',NULL,'0',NULL),
(3,'强制退宿',3,'removeUser','DELETE','admin','/dorm/bed/remove','127.0.0.1','','',1,'','2026-02-02 16:34:55',0,'system','2026-02-02 16:44:55','',NULL,'0',NULL),
(4,'水电充值',1,'recharge','','梁梓豪','','','{\"amount\":200, \"type\":\"electric\"}','{\"code\":200}',0,'','2026-02-04 15:18:49',0,'system','2026-02-04 15:18:49','',NULL,'0','101宿舍充值电费'),
(5,'水电充值',1,'recharge','','何嘉欣','','','{\"amount\":100, \"type\":\"water\"}','{\"code\":200}',0,'','2026-02-04 15:18:49',0,'system','2026-02-04 15:18:49','',NULL,'0','201宿舍充值水费'),
(6,'水电充值',1,'recharge','','王志豪','','','{\"amount\":50, \"type\":\"all\"}','{\"code\":200}',0,'','2026-02-04 15:18:49',0,'system','2026-02-04 15:18:49','',NULL,'0','102宿舍充值');

/*Table structure for table `sys_ordinary_user` */

DROP TABLE IF EXISTS `sys_ordinary_user`;

CREATE TABLE `sys_ordinary_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户主键ID',
  `username` varchar(64) NOT NULL COMMENT '学号/工号 (登录账号)',
  `password` varchar(100) NOT NULL COMMENT '加密密码 (BCrypt)',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
  `user_category` int NOT NULL DEFAULT '0' COMMENT '人员类别 (0:学生 1:教职工)',
  `gender` char(1) NOT NULL DEFAULT '1' COMMENT '性别 (0:女 1:男)',
  `birth_date` date DEFAULT NULL COMMENT '出生日期',
  `phone` varchar(255) DEFAULT NULL COMMENT '手机号 (加密存储)',
  `email` varchar(100) DEFAULT NULL COMMENT '电子邮箱',
  `id_card` varchar(255) DEFAULT NULL COMMENT '身份证号 (加密存储)',
  `ethnicity` varchar(20) NOT NULL DEFAULT '汉族' COMMENT '民族',
  `hometown` varchar(64) NOT NULL DEFAULT '未知' COMMENT '籍贯',
  `political_status` varchar(20) DEFAULT '群众' COMMENT '政治面貌',
  `avatar` varchar(500) DEFAULT '' COMMENT '头像URL',
  `home_address` varchar(500) DEFAULT NULL COMMENT '家庭居住地址/身份证地址 (加密存储)',
  `outside_address` varchar(500) DEFAULT NULL COMMENT '校外居住地址 (加密存储)',
  `entry_date` date DEFAULT NULL COMMENT '入学日期',
  `dorm_id` bigint DEFAULT NULL COMMENT '当前入住房间ID (关联dorm_room)',
  `campus_id` bigint DEFAULT NULL COMMENT '所属校区ID',
  `college_id` bigint DEFAULT NULL COMMENT '所属学院ID',
  `major_id` bigint DEFAULT NULL COMMENT '所属专业ID',
  `class_id` bigint DEFAULT NULL COMMENT '所属班级ID',
  `dept_id` bigint DEFAULT NULL COMMENT '所属部门ID (教工用)',
  `job_title_level` int DEFAULT '0' COMMENT '职称/行政级别 (教工专用: 1-助教, 2-讲师, 3-副教授, 4-教授)',
  `enrollment_year` int DEFAULT NULL COMMENT '入学年份 (学生)',
  `contract_year` int DEFAULT NULL COMMENT '合同年限 (仅教职工)',
  `entry_year` int DEFAULT NULL COMMENT '入职年份 (教工)',
  `residence_type` int NOT NULL DEFAULT '0' COMMENT '居住类型 (0:住校 1:走读)',
  `is_initial_pwd` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否为初始密码 (1:是 0:否)',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '帐号状态 (0:正常 1:停用 2:已归档)',
  `campus_status` int DEFAULT '1' COMMENT '在校状态 (1:在校 0:离校/假期)',
  `emergency_contact` varchar(64) DEFAULT NULL COMMENT '紧急联系人姓名',
  `emergency_phone` varchar(255) DEFAULT NULL COMMENT '紧急联系人电话 (加密存储)',
  `emergency_relation` varchar(32) DEFAULT '' COMMENT '与本人关系',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `suspension_start_date` date DEFAULT NULL COMMENT '休学开始日期',
  `version` int DEFAULT '0' COMMENT '乐观锁版本',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_dorm` (`dorm_id`),
  KEY `idx_class` (`class_id`)
) ENGINE=InnoDB AUTO_INCREMENT=20011 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-普通用户表';

/*Data for the table `sys_ordinary_user` */

insert  into `sys_ordinary_user`(`id`,`username`,`password`,`real_name`,`nickname`,`user_category`,`gender`,`birth_date`,`phone`,`email`,`id_card`,`ethnicity`,`hometown`,`political_status`,`avatar`,`home_address`,`outside_address`,`entry_date`,`dorm_id`,`campus_id`,`college_id`,`major_id`,`class_id`,`dept_id`,`job_title_level`,`enrollment_year`,`contract_year`,`entry_year`,`residence_type`,`is_initial_pwd`,`status`,`campus_status`,`emergency_contact`,`emergency_phone`,`emergency_relation`,`last_login_time`,`suspension_start_date`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`,`del_flag`) values 
(10001,'2024001','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','梁梓豪',NULL,0,'1',NULL,'13811110001',NULL,'440105200601010011','汉族','未知','群众','','湖北省武汉市洪山区民族大道XX小区','广州市城外环东路132号',NULL,101,1,101,NULL,NULL,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 14:57:16','','2026-02-04 15:28:11',NULL,'0'),
(10002,'2024002','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','何嘉欣',NULL,0,'0',NULL,'13811110002',NULL,'440105200602020022','汉族','未知','群众','','广东省佛山市顺德区大良街道',NULL,NULL,104,1,101,NULL,NULL,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 14:57:16','','2026-02-04 15:14:38',NULL,'0'),
(10003,'2024003','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','钟俊杰',NULL,0,'1',NULL,'13811110003',NULL,'440105200603030033','汉族','未知','群众','','广东省深圳市南山区粤海街道',NULL,NULL,NULL,3,103,NULL,NULL,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 14:57:16','',NULL,NULL,'0'),
(10004,'2024004','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','陈晓彤',NULL,0,'0',NULL,'13811110004',NULL,'440105200604040044','汉族','未知','群众','','广东省中山市石岐街道',NULL,NULL,104,2,104,NULL,NULL,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 14:57:16','','2026-02-04 15:14:38',NULL,'0'),
(10005,'2024005','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','郭展鹏',NULL,0,'1',NULL,'13811110005',NULL,'440105200605050055','汉族','未知','群众','','广东省东莞市虎门镇',NULL,NULL,101,1,101,NULL,NULL,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 14:57:16','','2026-02-04 15:14:38',NULL,'0'),
(10006,'2024006','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','苏锦荣',NULL,0,'1',NULL,'13811110006',NULL,'440105200606060066','汉族','未知','群众','','广东省江门市新会区',NULL,NULL,NULL,1,105,NULL,NULL,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 14:57:16','',NULL,NULL,'0'),
(10007,'2024007','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','谭婉仪',NULL,0,'0',NULL,'13811110007',NULL,'440105200607070077','汉族','未知','群众','','广东省珠海市香洲区',NULL,NULL,NULL,1,106,NULL,NULL,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 14:57:16','',NULL,NULL,'0'),
(10008,'2024008','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','冯子晴',NULL,0,'0',NULL,'13811110008',NULL,'440105200608080088','汉族','未知','群众','','广东省肇庆市端州区',NULL,NULL,NULL,3,107,NULL,NULL,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 14:57:16','',NULL,NULL,'0'),
(10011,'2024101','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','王志豪',NULL,0,'1',NULL,'13700010001',NULL,'110101200501011001','汉族','未知','群众','',NULL,NULL,NULL,102,1,101,NULL,301,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','','2026-02-04 15:14:38',NULL,'0'),
(10012,'2024102','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','李悦欣',NULL,0,'0',NULL,'13700010002',NULL,'110101200501011002','汉族','未知','群众','',NULL,NULL,NULL,NULL,1,101,NULL,301,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10013,'2024103','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','张一鸣',NULL,0,'1',NULL,'13700010003',NULL,'110101200501011003','汉族','未知','群众','',NULL,NULL,NULL,NULL,1,101,NULL,302,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10014,'2024104','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','刘思琪',NULL,0,'0',NULL,'13700010004',NULL,'110101200501011004','汉族','未知','群众','',NULL,NULL,NULL,NULL,1,101,NULL,303,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10015,'2024105','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','陈冠宇',NULL,0,'1',NULL,'13700010005',NULL,'110101200501011005','汉族','未知','群众','',NULL,NULL,NULL,NULL,1,101,NULL,304,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10016,'2024106','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','杨曼文',NULL,0,'0',NULL,'13700010006',NULL,'110101200501011006','汉族','未知','群众','',NULL,NULL,NULL,NULL,3,103,NULL,307,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10017,'2024107','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','赵锦坤',NULL,0,'1',NULL,'13700010007',NULL,'110101200501011007','汉族','未知','群众','',NULL,NULL,NULL,NULL,2,104,NULL,306,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10018,'2024108','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','黄子健',NULL,0,'1',NULL,'13700010008',NULL,'110101200501011008','汉族','未知','群众','',NULL,NULL,NULL,NULL,4,108,NULL,308,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10019,'2024109','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','周梦琪',NULL,0,'0',NULL,'13700010009',NULL,'110101200501011009','汉族','未知','群众','',NULL,NULL,NULL,NULL,1,101,NULL,301,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10020,'2024110','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','吴磊',NULL,0,'1',NULL,'13700010010',NULL,'110101200501011010','汉族','未知','群众','',NULL,NULL,NULL,NULL,1,105,NULL,301,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10021,'2024111','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','郑若男',NULL,0,'0',NULL,'13700010011',NULL,'110101200501011011','汉族','未知','群众','',NULL,NULL,NULL,NULL,2,104,NULL,305,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10022,'2024112','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','谢文强',NULL,0,'1',NULL,'13700010012',NULL,'110101200501011012','汉族','未知','群众','',NULL,NULL,NULL,NULL,3,103,NULL,307,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10023,'2024113','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','郭靖宇',NULL,0,'1',NULL,'13700010013',NULL,'110101200501011013','汉族','未知','群众','',NULL,NULL,NULL,NULL,1,106,NULL,307,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10024,'2024114','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','徐诗嘉',NULL,0,'0',NULL,'13700010014',NULL,'110101200501011014','汉族','未知','群众','',NULL,NULL,NULL,NULL,1,101,NULL,302,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10025,'2024115','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','邓超群',NULL,0,'1',NULL,'13700010015',NULL,'110101200501011015','汉族','未知','群众','',NULL,NULL,NULL,NULL,5,101,NULL,301,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10026,'2024116','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','宋承宪',NULL,0,'1',NULL,'13700010016',NULL,'110101200501011016','汉族','未知','群众','',NULL,NULL,NULL,NULL,1,102,NULL,301,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10027,'2024117','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','韩美美',NULL,0,'0',NULL,'13700010017',NULL,'110101200501011017','汉族','未知','群众','',NULL,NULL,NULL,NULL,2,104,NULL,306,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10028,'2024118','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','沈腾龙',NULL,0,'1',NULL,'13700010018',NULL,'110101200501011018','汉族','未知','群众','',NULL,NULL,NULL,NULL,3,103,NULL,307,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10029,'2024119','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','冯小刚',NULL,0,'1',NULL,'13700010019',NULL,'110101200501011019','汉族','未知','群众','',NULL,NULL,NULL,NULL,4,108,NULL,308,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10030,'2024120','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','蔡雅琪',NULL,0,'0',NULL,'13700010020',NULL,'110101200501011020','汉族','未知','群众','',NULL,NULL,NULL,NULL,1,107,NULL,301,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10031,'2024121','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','丁一博',NULL,0,'1',NULL,'13700010021',NULL,'110101200501011021','汉族','未知','群众','',NULL,NULL,NULL,NULL,1,101,NULL,301,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10032,'2024122','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','叶清泉',NULL,0,'0',NULL,'13700010022',NULL,'110101200501011022','汉族','未知','群众','',NULL,NULL,NULL,NULL,1,101,NULL,302,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10033,'2024123','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','马嘉祺',NULL,0,'1',NULL,'13700010023',NULL,'110101200501011023','汉族','未知','群众','',NULL,NULL,NULL,NULL,2,104,NULL,305,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10034,'2024124','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','白浅浅',NULL,0,'0',NULL,'13700010024',NULL,'110101200501011024','汉族','未知','群众','',NULL,NULL,NULL,NULL,1,106,NULL,307,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10035,'2024125','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','曾小贤',NULL,0,'1',NULL,'13700010025',NULL,'110101200501011025','汉族','未知','群众','',NULL,NULL,NULL,NULL,1,101,NULL,301,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10036,'2024126','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','胡一菲',NULL,0,'0',NULL,'13700010026',NULL,'110101200501011026','汉族','未知','群众','',NULL,NULL,NULL,NULL,1,105,NULL,301,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10037,'2024127','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','唐悠悠',NULL,0,'0',NULL,'13700010027',NULL,'110101200501011027','汉族','未知','群众','',NULL,NULL,NULL,NULL,3,103,NULL,307,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10038,'2024128','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','关谷奇',NULL,0,'1',NULL,'13700010028',NULL,'110101200501011028','汉族','未知','群众','',NULL,NULL,NULL,NULL,1,102,NULL,301,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10039,'2024129','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','吕子乔',NULL,0,'1',NULL,'13700010029',NULL,'110101200501011029','汉族','未知','群众','',NULL,NULL,NULL,NULL,5,101,NULL,301,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(10040,'2024130','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','林宛瑜',NULL,0,'0',NULL,'13700010030',NULL,'110101200501011030','汉族','未知','群众','',NULL,NULL,NULL,NULL,2,104,NULL,306,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(20001,'T001','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','张铁柱',NULL,1,'1',NULL,'13510002001',NULL,'34010119780101101X','汉族','未知','群众','',NULL,NULL,NULL,NULL,1,101,NULL,NULL,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(20002,'T002','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','王爱莲',NULL,1,'0',NULL,'13510002002',NULL,'34010119820202102X','汉族','未知','群众','',NULL,NULL,NULL,NULL,1,102,NULL,NULL,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(20003,'S001','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','刘大勇',NULL,1,'1',NULL,'13510002003',NULL,'34010119750303103X','汉族','未知','群众','',NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(20004,'S002','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','马冬梅',NULL,1,'0',NULL,'13510002004',NULL,'34010119800404104X','汉族','未知','群众','',NULL,NULL,NULL,NULL,3,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(20005,'S003','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','赵卫国',NULL,1,'1',NULL,'13510002005',NULL,'34010119700505105X','汉族','未知','群众','',NULL,NULL,NULL,NULL,2,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(20006,'S004','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','孙利民',NULL,1,'1',NULL,'13510002006',NULL,'34010119850606106X','汉族','未知','群众','',NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(20007,'T003','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','周晓琳',NULL,1,'0',NULL,'13510002007',NULL,'34010119920707107X','汉族','未知','群众','',NULL,NULL,NULL,NULL,3,103,NULL,NULL,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(20008,'S005','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','郭大婶',NULL,1,'0',NULL,'13510002008',NULL,'34010119720808108X','汉族','未知','群众','',NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(20009,'S006','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','钱多多',NULL,1,'1',NULL,'13510002009',NULL,'34010119880909109X','汉族','未知','群众','',NULL,NULL,NULL,NULL,5,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0'),
(20010,'S007','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','胡一刀',NULL,1,'1',NULL,'13510002010',NULL,'34010119811010110X','汉族','未知','群众','',NULL,NULL,NULL,NULL,4,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,0,1,'0',1,NULL,NULL,'',NULL,NULL,0,'system','2026-02-04 15:08:01','',NULL,NULL,'0');

/*Table structure for table `sys_role` */

DROP TABLE IF EXISTS `sys_role`;

CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` varchar(30) NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) NOT NULL COMMENT '角色权限字符',
  `sort` int DEFAULT '0' COMMENT '显示顺序',
  `status` char(1) DEFAULT '0' COMMENT '状态: 0正常 1停用',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号(0开始)',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_key` (`role_key`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-角色表';

/*Data for the table `sys_role` */

insert  into `sys_role`(`id`,`role_name`,`role_key`,`sort`,`status`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,'超级管理员','super_admin',1,'0',0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(2,'部门/学院管理员','dept_admin',2,'0',0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(3,'宿管经理','dorm_manager',3,'0',0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(4,'维修工头','repair_master',4,'0',0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(5,'行政辅导员','counselor',5,'0',0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(6,'教职工','college_teacher',6,'0',0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(7,'工勤人员','staff',7,'0',0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(8,'学生','student',8,'0',0,'system','2026-02-04 14:57:16','',NULL,'0',NULL);

/*Table structure for table `sys_user_archive` */

DROP TABLE IF EXISTS `sys_user_archive`;

CREATE TABLE `sys_user_archive` (
  `id` bigint NOT NULL COMMENT '原始用户ID(保留原ID)',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账号',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `user_category` int NOT NULL COMMENT '0学生 1教工',
  `phone` varchar(255) DEFAULT NULL COMMENT '联系电话(可能含密文)',
  `id_card` varchar(255) DEFAULT NULL COMMENT '身份证号(可能含密文)',
  `college_name` varchar(64) DEFAULT NULL COMMENT '学院快照',
  `archive_type` int NOT NULL COMMENT '归档类型',
  `archive_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '详细原因',
  `archive_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
  `original_data_json` longtext COMMENT '原始数据备份',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号(0开始)',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `operator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-用户归档表';

/*Data for the table `sys_user_archive` */

insert  into `sys_user_archive`(`id`,`username`,`real_name`,`user_category`,`phone`,`id_card`,`college_name`,`archive_type`,`archive_reason`,`archive_time`,`original_data_json`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`,`operator`) values 
(90001,'20200512','林耀东',0,'13811119001','440105200201019001','计算机学院',10,'2024届本科生正常毕业离校','2024-06-30 10:00:00',NULL,0,'system','2026-02-04 14:58:39','',NULL,'0',NULL,'admin'),
(90002,'20210344','张志豪',0,'13811119002','440105200305059002','岭南学院',51,'【休学】学生本人因病申请休学一年（保留学籍）','2025-09-15 14:30:00',NULL,0,'system','2026-02-04 14:58:39','',NULL,'0',NULL,'huang_counselor'),
(90003,'1998JZG102','陈美玲',1,'13900009003','440105197508089003','后勤管理处',20,'【退休】达到法定退休年龄，办理退休归档','2025-12-01 09:00:00',NULL,0,'system','2026-02-04 14:58:39','',NULL,'0',NULL,'li_manager'),
(90004,'20220911','周永乐',0,'13811119004','440105200411119004','法学院',40,'【劝退】多次严重违反宿舍用电安全及学校纪律','2025-11-20 16:45:00',NULL,0,'system','2026-02-04 14:58:39','',NULL,'0',NULL,'admin'),
(90005,'20230788','何婉莹',0,'13811119005','440105200507079005','中山医学院',11,'【结业】学分未修满，按结业处理，释放宿舍资源','2025-07-10 11:20:00',NULL,0,'system','2026-02-04 14:58:39','',NULL,'0',NULL,'admin'),
(90006,'20240102','陆大为',0,'13811119006','440105200610109006','传播与设计学院',60,'【入伍】应征入伍，保留学籍两年','2025-09-01 08:30:00',NULL,0,'system','2026-02-04 14:58:39','',NULL,'0',NULL,'lin_counselor'),
(90007,'20210255','苏小妹',0,'13811119007','440105200312129007','计算机学院',12,'【出国】办理自费留学，申请注销在校学籍','2025-10-18 15:00:00',NULL,0,'system','2026-02-04 14:58:39','',NULL,'0',NULL,'admin'),
(90008,'20190001','刁一鸣',0,'13811119008','440105200101019008','法学院',99,'【超时退学】休学逾期未复学且无法取得联系','2026-02-01 10:00:00',NULL,0,'system','2026-02-04 14:58:39','',NULL,'0',NULL,'admin');

/*Table structure for table `sys_user_role` */

DROP TABLE IF EXISTS `sys_user_role`;

CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号(0开始)',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';

/*Data for the table `sys_user_role` */

insert  into `sys_user_role`(`id`,`user_id`,`role_id`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,1,0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(2,2,3,0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(3,3,5,0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(4,4,4,0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(5,5,2,0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(6,6,7,0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(7,7,3,0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(8,8,5,0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(9,10001,8,0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(10,10002,8,0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(11,10003,8,0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(12,10004,8,0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(13,10005,8,0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(14,10006,8,0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(15,10007,8,0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(16,10008,8,0,'system','2026-02-04 14:57:16','',NULL,'0',NULL),
(17,20001,6,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(18,20002,6,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(19,20003,7,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(20,20004,7,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(21,20005,7,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(22,20006,7,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(23,20007,5,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(24,20008,7,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(25,20009,7,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(26,20010,7,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(27,10011,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(28,10012,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(29,10013,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(30,10014,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(31,10015,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(32,10016,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(33,10017,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(34,10018,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(35,10019,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(36,10020,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(37,10021,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(38,10022,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(39,10023,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(40,10024,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(41,10025,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(42,10026,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(43,10027,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(44,10028,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(45,10029,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(46,10030,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(47,10031,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(48,10032,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(49,10033,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(50,10034,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(51,10035,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(52,10036,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(53,10037,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(54,10038,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(55,10039,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL),
(56,10040,8,0,'system','2026-02-04 15:08:01','',NULL,'0',NULL);

/*Table structure for table `sys_utility_price` */

DROP TABLE IF EXISTS `sys_utility_price`;

CREATE TABLE `sys_utility_price` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type` int NOT NULL COMMENT '类型: 1水 2电',
  `name` varchar(50) NOT NULL COMMENT '名称',
  `price` decimal(10,4) NOT NULL COMMENT '单价',
  `unit` varchar(10) DEFAULT NULL COMMENT '单位',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号(0开始)',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='配置-水电价';

/*Data for the table `sys_utility_price` */

insert  into `sys_utility_price`(`id`,`type`,`name`,`price`,`unit`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,'生活用水',4.3200,'吨',0,'admin','2026-01-31 16:42:43','',NULL,'0',NULL),
(2,2,'生活用电',0.5600,'度',0,'admin','2026-01-31 16:42:43','',NULL,'0',NULL);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
