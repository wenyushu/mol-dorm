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
  `grade` int DEFAULT NULL COMMENT '年级 (如: 2024)',
  `class_name` varchar(100) NOT NULL COMMENT '班级名称',
  `counselor_id` bigint DEFAULT NULL COMMENT '辅导员ID',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除: 0-未删 1-已删',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `fk_class_major` (`major_id`),
  CONSTRAINT `fk_class_major` FOREIGN KEY (`major_id`) REFERENCES `sys_major` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础信息-班级表';

/*Data for the table `biz_class` */

insert  into `biz_class`(`id`,`major_id`,`grade`,`class_name`,`counselor_id`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,1,2024,'英语2401班',NULL,'0','admin','2026-01-15 12:22:12','',NULL,NULL),
(2,1,2024,'英语2402班',NULL,'0','admin','2026-01-15 12:22:12','',NULL,NULL),
(3,2,2025,'视传2501班',NULL,'0','admin','2026-01-15 12:22:12','',NULL,NULL),
(4,4,2023,'机械2301班',NULL,'0','admin','2026-01-15 12:22:12','',NULL,NULL),
(5,5,2024,'化工研2401班',NULL,'0','admin','2026-01-15 12:22:12','',NULL,NULL);

/*Table structure for table `biz_electric_violation_log` */

DROP TABLE IF EXISTS `biz_electric_violation_log`;

CREATE TABLE `biz_electric_violation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `violation_type` int NOT NULL COMMENT '违规类型: 1-功率超载 2-恶性负载(阻性) 3-夜间不归',
  `power_val` decimal(10,2) DEFAULT NULL COMMENT '违规时的功率(W)',
  `detected_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '处理结果备注',
  PRIMARY KEY (`id`),
  KEY `fk_violation_room` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='违规用电/安全警报日志';

/*Data for the table `biz_electric_violation_log` */

/*Table structure for table `biz_holiday_stay` */

DROP TABLE IF EXISTS `biz_holiday_stay`;

CREATE TABLE `biz_holiday_stay` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '申请学生ID',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `reason` varchar(500) NOT NULL COMMENT '留校原因',
  `emergency_name` varchar(64) NOT NULL COMMENT '紧急联系人',
  `emergency_relation` varchar(32) NOT NULL COMMENT '关系',
  `emergency_phone` varchar(20) NOT NULL COMMENT '联系电话',
  `status` int DEFAULT '0' COMMENT '状态: 0-待审批 1-通过 2-驳回',
  `audit_msg` varchar(255) DEFAULT NULL COMMENT '审批意见',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-假期留校表';

/*Data for the table `biz_holiday_stay` */

/*Table structure for table `biz_meter_reading` */

DROP TABLE IF EXISTS `biz_meter_reading`;

CREATE TABLE `biz_meter_reading` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `utility_type` tinyint NOT NULL COMMENT '类型 (1-电 2-水)',
  `reading_val` decimal(10,2) NOT NULL COMMENT '当前读数',
  `usage_val` decimal(10,2) NOT NULL COMMENT '本次用量增量',
  `record_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '抄表时间',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除 (0:未删 1:已删)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='抄表历史记录';

/*Data for the table `biz_meter_reading` */

/*Table structure for table `biz_repair_order` */

DROP TABLE IF EXISTS `biz_repair_order`;

CREATE TABLE `biz_repair_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(32) NOT NULL COMMENT '工单号',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID',
  `description` text NOT NULL COMMENT '故障描述',
  `status` int DEFAULT '0' COMMENT '状态: 0-待处理 1-维修中 2-已完成',
  `images` varchar(1000) DEFAULT NULL COMMENT '图片URL',
  `repairman_id` bigint DEFAULT NULL COMMENT '维修工ID',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `rating` tinyint DEFAULT NULL COMMENT '评分',
  `comment` varchar(255) DEFAULT NULL COMMENT '评价内容',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-报修工单表';

/*Data for the table `biz_repair_order` */

/*Table structure for table `biz_staff_profile` */

DROP TABLE IF EXISTS `biz_staff_profile`;

CREATE TABLE `biz_staff_profile` (
  `user_id` bigint NOT NULL COMMENT '用户ID (主键, 关联 sys_ordinary_user.id)',
  `dept_id` bigint NOT NULL COMMENT '所属部门/学院ID',
  `job_title` varchar(50) NOT NULL COMMENT '职称/职务',
  `contract_type` varchar(20) DEFAULT '长期' COMMENT '合同类型',
  `housing_intent` tinyint DEFAULT '0' COMMENT '是否有住宿意向 (0:无 1:有)',
  `current_bed_id` bigint DEFAULT NULL COMMENT '当前床位ID (冗余字段)',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='教职工档案扩展表';

/*Data for the table `biz_staff_profile` */

insert  into `biz_staff_profile`(`user_id`,`dept_id`,`job_title`,`contract_type`,`housing_intent`,`current_bed_id`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(10005,1,'讲师','长期',1,NULL,'admin','2026-01-15 12:05:31','',NULL,'0',NULL),
(10006,1,'讲师','长期',1,NULL,'admin','2026-01-15 12:05:31','',NULL,'0',NULL),
(10007,1,'讲师','长期',1,NULL,'admin','2026-01-15 12:05:31','',NULL,'0',NULL),
(10008,1,'讲师','长期',1,NULL,'admin','2026-01-15 12:05:31','',NULL,'0',NULL),
(10009,1,'讲师','长期',1,NULL,'admin','2026-01-15 12:05:31','',NULL,'0',NULL),
(80001,2,'教授','长期',1,NULL,'admin','2026-01-15 11:26:31','',NULL,'0',NULL),
(80002,2,'讲师','长期',0,NULL,'admin','2026-01-15 11:26:31','',NULL,'0',NULL),
(80003,1,'科员','固定期限',0,NULL,'admin','2026-01-15 11:26:31','',NULL,'0',NULL),
(80004,4,'技工','劳务派遣',1,NULL,'admin','2026-01-15 11:26:31','',NULL,'0',NULL),
(80005,5,'队长','长期',0,NULL,'admin','2026-01-15 11:26:31','',NULL,'0',NULL);

/*Table structure for table `biz_user_preference` */

DROP TABLE IF EXISTS `biz_user_preference`;

CREATE TABLE `biz_user_preference` (
  `user_id` bigint NOT NULL COMMENT '用户ID (关联 sys_ordinary_user.id)',
  `team_code` varchar(32) DEFAULT NULL COMMENT '组队码 (有相同码的学生强制分在一起)',
  `bed_time` tinyint DEFAULT '3' COMMENT '就寝时间: 1-21点, 2-22点, 3-23点, 4-24点, 5-1点, 6-2点+',
  `wake_time` tinyint DEFAULT '3' COMMENT '起床时间: 1-6点, 2-7点, 3-8点, 4-9点, 5-10点, 6-11点+',
  `siesta_habit` tinyint DEFAULT '0' COMMENT '午睡习惯: 0-无, 1-偶尔, 2-必须午睡(需安静)',
  `out_late_freq` tinyint DEFAULT '0' COMMENT '晚归频率: 0-从不, 1-偶尔, 2-经常',
  `sleep_quality` tinyint DEFAULT '2' COMMENT '睡眠深浅: 1-雷打不动, 2-普通, 3-轻度敏感, 4-神经衰弱',
  `snoring_level` tinyint DEFAULT '0' COMMENT '打呼噜: 0-无, 1-轻微, 2-中度, 3-雷震子',
  `grinding_teeth` tinyint DEFAULT '0' COMMENT '磨牙: 0-无, 1-有',
  `sleep_talk` tinyint DEFAULT '0' COMMENT '说梦话: 0-无, 1-有',
  `climb_bed_noise` tinyint DEFAULT '0' COMMENT '上下床动静: 0-轻盈, 1-普通, 2-拆迁队',
  `shower_freq` tinyint DEFAULT '1' COMMENT '洗澡频率: 1-每天, 2-两天一次, 3-不定期',
  `sock_wash` tinyint DEFAULT '0' COMMENT '袜子清洗: 0-当天洗, 1-攒一堆洗',
  `trash_habit` tinyint DEFAULT '1' COMMENT '倒垃圾: 1-满了就倒, 2-轮流倒, 3-谁看不下去谁倒',
  `clean_freq` tinyint DEFAULT '2' COMMENT '打扫频率: 1-每天, 2-每周, 3-每月, 4-随缘',
  `toilet_clean` tinyint DEFAULT '1' COMMENT '轮流刷厕所: 1-完全接受, 0-拒绝',
  `desk_messy` tinyint DEFAULT '2' COMMENT '桌面整洁度: 1-极简, 2-乱中有序, 3-垃圾堆',
  `personal_hygiene` tinyint DEFAULT '3' COMMENT '个人卫生自评: 1-5分',
  `odor_tolerance` tinyint DEFAULT '2' COMMENT '异味容忍度: 1-无法忍受, 2-普通, 3-强悍',
  `smoking` tinyint DEFAULT '0' COMMENT '抽烟: 0-不抽, 1-阳台抽, 2-室内抽',
  `smoke_tolerance` tinyint DEFAULT '0' COMMENT '接受烟味: 0-不可, 1-可以',
  `drinking` tinyint DEFAULT '0' COMMENT '喝酒: 0-无, 1-小酌, 2-酗酒',
  `ac_temp` tinyint DEFAULT '26' COMMENT '空调温度习惯: 18-30',
  `ac_duration` tinyint DEFAULT '1' COMMENT '空调时长: 1-整晚, 2-定时关闭',
  `game_type_lol` tinyint DEFAULT '0' COMMENT '玩LOL/DOTA: 0-否 1-是',
  `game_type_fps` tinyint DEFAULT '0' COMMENT '玩FPS(CS/瓦/三角洲): 0-否 1-是',
  `game_type_3a` tinyint DEFAULT '0' COMMENT '玩3A大作: 0-否 1-是',
  `game_type_mmo` tinyint DEFAULT '0' COMMENT '玩MMO: 0-否 1-是',
  `game_type_mobile` tinyint DEFAULT '0' COMMENT '玩手游: 0-否 1-是',
  `game_habit` tinyint DEFAULT '0' COMMENT '游戏习惯综合: 0-不玩 1-轻度 2-重度',
  `game_voice` tinyint DEFAULT '1' COMMENT '连麦音量: 0-静音, 1-正常, 2-咆哮',
  `keyboard_axis` tinyint DEFAULT '1' COMMENT '键盘轴体: 1-薄膜/静音, 2-红/茶轴, 3-青轴(吵)',
  `is_cosplay` tinyint DEFAULT '0' COMMENT '玩Cosplay: 0-否 1-是',
  `is_anime` tinyint DEFAULT '0' COMMENT '二次元浓度: 0-现充, 1-看番, 2-老二刺螈',
  `mbti_e_i` char(1) DEFAULT 'E' COMMENT 'MBTI维度: E/I',
  `mbti_result` varchar(4) DEFAULT NULL COMMENT 'MBTI结果(如INTJ)',
  `social_battery` tinyint DEFAULT '3' COMMENT '社交意愿: 1-社恐(别理我) -> 5-社牛',
  `share_items` tinyint DEFAULT '1' COMMENT '物品共享意愿: 0-皆不可, 1-部分可借, 2-随意用',
  `bring_guest` tinyint DEFAULT '0' COMMENT '带人回寝: 0-绝不, 1-偶尔同性, 2-经常',
  `visitors` tinyint DEFAULT '0' COMMENT '访客频率: 同上',
  `relationship_status` tinyint DEFAULT '0' COMMENT '恋爱状态: 0-单身, 1-恋爱中(煲电话粥风险)',
  `has_disability` tinyint DEFAULT '0' COMMENT '身体残疾: 0-无, 1-腿部残疾(需低层)',
  `has_insulin` tinyint DEFAULT '0' COMMENT '胰岛素需求: 0-无, 1-需要冰箱',
  `has_infectious` tinyint DEFAULT '0' COMMENT '传染性疾病: 0-无, 1-有(需单独处理)',
  `religion_taboo` varchar(50) DEFAULT NULL COMMENT '宗教禁忌(如清真)',
  `special_disease` varchar(255) DEFAULT NULL COMMENT '特殊疾病描述',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `game_rank` tinyint DEFAULT '0' COMMENT '游戏段位: 0-青铜/白银 1-黄金/铂金 2-钻石/大师 3-王者',
  `game_role` tinyint DEFAULT '0' COMMENT '游戏位置: 0-全能 1-上单 2-打野 3-中单 4-射手 5-辅助',
  `eat_luosifen` tinyint DEFAULT '0' COMMENT '吃螺蛳粉: 0-拒绝 1-偶尔 2-重度',
  `eat_durian` tinyint DEFAULT '0' COMMENT '吃榴莲: 0-拒绝 1-吃',
  `region_type` tinyint DEFAULT NULL COMMENT '籍贯类型: 0-南方 1-北方 (由系统根据省份自动计算)',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户全维度画像表';

/*Data for the table `biz_user_preference` */

insert  into `biz_user_preference`(`user_id`,`team_code`,`bed_time`,`wake_time`,`siesta_habit`,`out_late_freq`,`sleep_quality`,`snoring_level`,`grinding_teeth`,`sleep_talk`,`climb_bed_noise`,`shower_freq`,`sock_wash`,`trash_habit`,`clean_freq`,`toilet_clean`,`desk_messy`,`personal_hygiene`,`odor_tolerance`,`smoking`,`smoke_tolerance`,`drinking`,`ac_temp`,`ac_duration`,`game_type_lol`,`game_type_fps`,`game_type_3a`,`game_type_mmo`,`game_type_mobile`,`game_habit`,`game_voice`,`keyboard_axis`,`is_cosplay`,`is_anime`,`mbti_e_i`,`mbti_result`,`social_battery`,`share_items`,`bring_guest`,`visitors`,`relationship_status`,`has_disability`,`has_insulin`,`has_infectious`,`religion_taboo`,`special_disease`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`,`game_rank`,`game_role`,`eat_luosifen`,`eat_durian`,`region_type`) values 
(10000,NULL,2,3,0,0,2,0,0,0,0,1,0,1,2,1,2,3,2,0,0,0,26,1,1,0,0,0,0,0,1,1,0,0,'E',NULL,3,1,0,0,0,0,0,0,NULL,NULL,'system','2026-01-15 12:05:31','',NULL,'0',NULL,2,0,0,0,NULL),
(10001,NULL,4,3,0,0,2,0,0,0,0,1,0,1,2,1,2,3,2,1,0,0,26,1,1,0,0,0,0,0,1,1,0,0,'I',NULL,3,1,0,0,0,0,0,0,NULL,NULL,'system','2026-01-15 12:05:31','',NULL,'0',NULL,1,0,0,0,NULL),
(10002,NULL,2,3,0,0,2,0,0,0,0,1,0,1,2,1,2,3,2,0,0,0,26,1,0,0,0,0,0,0,1,1,0,0,'E',NULL,3,1,0,0,0,0,0,0,NULL,NULL,'system','2026-01-15 12:05:31','',NULL,'0',NULL,0,0,1,0,NULL),
(10003,NULL,1,3,0,0,2,0,0,0,0,1,0,1,2,1,2,3,2,0,0,0,26,1,0,0,0,0,0,0,1,1,0,0,'I',NULL,3,1,0,0,0,0,0,0,NULL,NULL,'system','2026-01-15 12:05:31','',NULL,'0',NULL,0,0,0,0,NULL),
(10004,NULL,3,3,0,0,2,0,0,0,0,1,0,1,2,1,2,3,2,2,0,0,26,1,1,0,0,0,0,0,1,1,0,0,'E',NULL,3,1,0,0,0,0,0,0,NULL,NULL,'system','2026-01-15 12:05:31','',NULL,'0',NULL,3,0,1,0,NULL),
(80001,NULL,1,3,0,0,2,0,0,0,0,1,0,1,2,1,2,3,2,0,0,0,26,1,0,0,0,0,0,0,1,1,0,0,'I',NULL,3,1,0,0,0,0,0,0,NULL,NULL,'system','2026-01-15 11:26:31','',NULL,'0',NULL,0,0,0,0,NULL),
(80002,NULL,2,3,0,0,2,0,0,0,0,1,0,1,2,1,2,3,2,0,0,0,26,1,0,0,0,0,0,0,1,1,0,0,'E',NULL,3,1,0,0,0,0,0,0,NULL,NULL,'system','2026-01-15 11:26:31','',NULL,'0',NULL,0,0,0,0,NULL),
(80003,NULL,2,3,0,0,2,0,0,0,0,1,0,1,2,1,2,3,2,1,0,0,26,1,0,0,0,0,0,0,1,1,0,0,'E',NULL,3,1,0,0,0,0,0,0,NULL,NULL,'system','2026-01-15 11:26:31','',NULL,'0',NULL,0,0,0,0,NULL),
(80004,NULL,2,3,0,0,2,0,0,0,0,1,0,1,2,1,2,3,2,2,0,0,26,1,0,0,0,0,0,0,1,1,0,0,'E',NULL,3,1,0,0,0,0,0,0,NULL,NULL,'system','2026-01-15 11:26:31','',NULL,'0',NULL,0,0,1,0,NULL),
(80005,NULL,2,3,0,0,2,0,0,0,0,1,0,1,2,1,2,3,2,0,0,0,26,1,0,0,0,0,0,0,1,1,0,0,'I',NULL,3,1,0,0,0,0,0,0,NULL,NULL,'system','2026-01-15 11:26:31','',NULL,'0',NULL,0,0,0,0,NULL),
(2024001,NULL,3,3,0,0,2,0,0,0,0,1,0,1,2,1,2,3,2,0,0,0,26,1,0,0,0,0,0,0,1,1,0,0,'E',NULL,3,1,0,0,0,0,0,0,NULL,NULL,'system','2026-01-15 11:26:31','',NULL,'0',NULL,3,0,2,0,NULL),
(2024002,NULL,1,3,0,0,2,0,0,0,0,1,0,1,2,1,2,3,2,0,0,0,26,1,0,0,0,0,0,0,1,1,0,0,'I',NULL,3,1,0,0,0,0,0,0,NULL,NULL,'system','2026-01-15 11:26:31','',NULL,'0',NULL,0,0,0,0,NULL),
(2024003,NULL,3,3,0,0,2,0,0,0,0,1,0,1,2,1,2,3,2,2,0,0,26,1,0,0,0,0,0,0,1,1,0,0,'E',NULL,3,1,0,0,0,0,0,0,NULL,NULL,'system','2026-01-15 11:26:31','',NULL,'0',NULL,3,0,0,0,NULL),
(2024004,NULL,3,3,0,0,2,0,0,0,0,1,0,1,2,1,2,3,2,0,0,0,26,1,0,0,0,0,0,0,1,1,0,0,'E',NULL,3,1,0,0,0,0,0,0,NULL,NULL,'system','2026-01-15 11:26:31','',NULL,'0',NULL,3,0,0,0,NULL),
(2024005,NULL,2,3,0,0,2,0,0,0,0,1,0,1,2,1,2,3,2,0,0,0,26,1,0,0,0,0,0,0,1,1,0,0,'I',NULL,3,1,0,0,0,0,0,0,NULL,NULL,'system','2026-01-15 11:26:31','',NULL,'0',NULL,1,0,1,0,NULL);

/*Table structure for table `biz_utility_bill` */

DROP TABLE IF EXISTS `biz_utility_bill`;

CREATE TABLE `biz_utility_bill` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `month` varchar(7) NOT NULL COMMENT '月份 (YYYY-MM)',
  `water_cold` decimal(10,2) DEFAULT '0.00' COMMENT '冷水用量',
  `water_hot` decimal(10,2) DEFAULT '0.00' COMMENT '热水用量',
  `elec_light` decimal(10,2) DEFAULT '0.00' COMMENT '照明电量',
  `elec_ac` decimal(10,2) DEFAULT '0.00' COMMENT '空调电量',
  `cost_water` decimal(10,2) DEFAULT '0.00' COMMENT '水费',
  `cost_elec` decimal(10,2) DEFAULT '0.00' COMMENT '电费',
  `total_amount` decimal(10,2) DEFAULT '0.00' COMMENT '总金额',
  `status` int DEFAULT '0' COMMENT '状态: 0-未付 1-已付',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `version` int DEFAULT '1' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_month` (`room_id`,`month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-水电账单表';

/*Data for the table `biz_utility_bill` */

/*Table structure for table `dorm_bed` */

DROP TABLE IF EXISTS `dorm_bed`;

CREATE TABLE `dorm_bed` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '所属房间ID',
  `bed_label` varchar(20) NOT NULL COMMENT '床位号(如:1号床)',
  `occupant_id` bigint DEFAULT NULL COMMENT '居住者ID',
  `status` tinyint DEFAULT '0' COMMENT '状态: 0-空闲 1-占用',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `version` int DEFAULT '1' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  KEY `fk_bed_room` (`room_id`),
  KEY `fk_bed_user` (`occupant_id`),
  CONSTRAINT `fk_bed_room` FOREIGN KEY (`room_id`) REFERENCES `dorm_room` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_bed_user` FOREIGN KEY (`occupant_id`) REFERENCES `sys_ordinary_user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-床位表';

/*Data for the table `dorm_bed` */

insert  into `dorm_bed`(`id`,`room_id`,`bed_label`,`occupant_id`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`,`version`) values 
(1,1,'1号床',3,1,'0','admin','2026-01-15 12:22:12','',NULL,NULL,1),
(2,1,'2号床',NULL,0,'0','admin','2026-01-15 12:22:12','',NULL,NULL,1);

/*Table structure for table `dorm_building` */

DROP TABLE IF EXISTS `dorm_building`;

CREATE TABLE `dorm_building` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint NOT NULL COMMENT '所属校区ID',
  `name` varchar(50) NOT NULL COMMENT '楼栋名称',
  `type` int DEFAULT '1' COMMENT '类型 (1:男 2:女 3:混合)',
  `floors` int DEFAULT '6' COMMENT '总层数',
  `has_elevator` tinyint DEFAULT '0' COMMENT '有无电梯 (0:无 1:有)',
  `power_limit` int DEFAULT '1000' COMMENT '限电功率(W)',
  `status` int DEFAULT '1' COMMENT '状态 (1:启用 0:维修)',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `usage_type` tinyint DEFAULT '0' COMMENT '用途: 0-学生宿舍 1-教职工公寓',
  PRIMARY KEY (`id`),
  KEY `fk_building_campus` (`campus_id`),
  CONSTRAINT `fk_building_campus` FOREIGN KEY (`campus_id`) REFERENCES `sys_campus` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-楼栋表';

/*Data for the table `dorm_building` */

insert  into `dorm_building`(`id`,`campus_id`,`name`,`type`,`floors`,`has_elevator`,`power_limit`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`,`usage_type`) values 
(1,1,'海棠苑1号楼',2,6,0,1000,1,'0','admin','2026-01-15 12:22:12','',NULL,NULL,0),
(2,1,'海棠苑2号楼',1,6,0,1000,1,'0','admin','2026-01-15 12:22:12','',NULL,NULL,0),
(3,2,'铁西教工公寓A座',3,12,0,1000,1,'0','admin','2026-01-15 12:22:12','',NULL,NULL,1),
(4,2,'铁西学生公寓3号',1,6,0,1000,1,'0','admin','2026-01-15 12:22:12','',NULL,NULL,0),
(5,3,'抚顺留学生楼',3,5,0,1000,1,'0','admin','2026-01-15 12:22:12','',NULL,NULL,0);

/*Table structure for table `dorm_change_request` */

DROP TABLE IF EXISTS `dorm_change_request`;

CREATE TABLE `dorm_change_request` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '申请学生ID',
  `current_room_id` bigint NOT NULL COMMENT '原房间ID',
  `target_room_id` bigint DEFAULT NULL COMMENT '目标房间ID',
  `type` int DEFAULT '0' COMMENT '类型: 0-换房 1-退宿 2-互换',
  `reason` varchar(500) DEFAULT NULL COMMENT '原因',
  `status` int DEFAULT '0' COMMENT '状态: 0-待审批 1-通过 2-驳回',
  `swap_student_id` bigint DEFAULT NULL COMMENT '互换目标学生ID',
  `audit_msg` varchar(255) DEFAULT NULL COMMENT '审批意见',
  `apply_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-调宿申请表';

/*Data for the table `dorm_change_request` */

/*Table structure for table `dorm_fixed_asset` */

DROP TABLE IF EXISTS `dorm_fixed_asset`;

CREATE TABLE `dorm_fixed_asset` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `asset_name` varchar(100) NOT NULL COMMENT '资产名称 (如: 书桌, 空调)',
  `asset_code` varchar(64) NOT NULL COMMENT '资产编号 (唯一标识, 如: AD-101-01)',
  `category` int NOT NULL COMMENT '分类: 1-家具(桌椅床柜) 2-电器(热水器空调) 3-基建(门窗洗漱台)',
  `room_id` bigint NOT NULL COMMENT '所属房间ID',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '价值(元)',
  `status` int DEFAULT '1' COMMENT '状态: 1-正常 2-报修中 3-损坏/丢失 4-报废',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_asset_code` (`asset_code`),
  KEY `fk_asset_room` (`room_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资产管理表';

/*Data for the table `dorm_fixed_asset` */

insert  into `dorm_fixed_asset`(`id`,`asset_name`,`asset_code`,`category`,`room_id`,`price`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,'空调','AC-001',2,1,0.00,1,'admin','2026-01-15 12:22:12','',NULL,'0',NULL),
(2,'书桌','DK-001',1,1,0.00,1,'admin','2026-01-15 12:22:12','',NULL,'0',NULL);

/*Table structure for table `dorm_floor` */

DROP TABLE IF EXISTS `dorm_floor`;

CREATE TABLE `dorm_floor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `building_id` bigint NOT NULL COMMENT '所属楼栋ID',
  `floor_num` int NOT NULL COMMENT '楼层号',
  `gender_limit` tinyint DEFAULT '0' COMMENT '性别限制 (0:无 1:男 2:女)',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `fk_floor_building` (`building_id`),
  CONSTRAINT `fk_floor_building` FOREIGN KEY (`building_id`) REFERENCES `dorm_building` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-楼层表';

/*Data for the table `dorm_floor` */

insert  into `dorm_floor`(`id`,`building_id`,`floor_num`,`gender_limit`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,1,1,2,'0','admin','2026-01-15 12:22:12','',NULL,NULL),
(2,1,2,2,'0','admin','2026-01-15 12:22:12','',NULL,NULL),
(3,2,1,1,'0','admin','2026-01-15 12:22:12','',NULL,NULL);

/*Table structure for table `dorm_meter_electric` */

DROP TABLE IF EXISTS `dorm_meter_electric`;

CREATE TABLE `dorm_meter_electric` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `meter_no` varchar(50) NOT NULL COMMENT '电表编号',
  `current_reading` decimal(10,2) DEFAULT '0.00' COMMENT '当前读数',
  `balance` decimal(10,2) DEFAULT '0.00' COMMENT '余额',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_e` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='电表实时信息表';

/*Data for the table `dorm_meter_electric` */

/*Table structure for table `dorm_meter_water` */

DROP TABLE IF EXISTS `dorm_meter_water`;

CREATE TABLE `dorm_meter_water` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `meter_no` varchar(50) NOT NULL COMMENT '水表编号',
  `current_reading` decimal(10,2) DEFAULT '0.00' COMMENT '当前读数',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_w` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='水表实时信息表';

/*Data for the table `dorm_meter_water` */

/*Table structure for table `dorm_power_rule` */

DROP TABLE IF EXISTS `dorm_power_rule`;

CREATE TABLE `dorm_power_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `max_watt` decimal(10,2) NOT NULL DEFAULT '2000.00' COMMENT '最大功率限制(W)',
  `auto_trip` tinyint DEFAULT '1' COMMENT '自动跳闸开关 (1:开 0:关)',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_rule` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='房间用电规则表';

/*Data for the table `dorm_power_rule` */

/*Table structure for table `dorm_room` */

DROP TABLE IF EXISTS `dorm_room`;

CREATE TABLE `dorm_room` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `building_id` bigint NOT NULL COMMENT '所属楼栋ID',
  `floor_id` bigint DEFAULT NULL COMMENT '所属楼层ID',
  `floor_no` int NOT NULL COMMENT '楼层号(冗余)',
  `room_no` varchar(20) NOT NULL COMMENT '房间号',
  `capacity` int DEFAULT '4' COMMENT '床位数',
  `current_num` int DEFAULT '0' COMMENT '实住人数',
  `gender` int DEFAULT '1' COMMENT '性别 (1:男 2:女 0:混合)',
  `status` int DEFAULT '1' COMMENT '状态 (1:正常 0:维修 2:满员)',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `version` int DEFAULT '1' COMMENT '乐观锁版本号',
  `apartment_type` varchar(50) DEFAULT NULL COMMENT '户型: 单间/一室一厅/两室一厅',
  PRIMARY KEY (`id`),
  KEY `fk_room_building` (`building_id`),
  KEY `fk_room_floor` (`floor_id`),
  CONSTRAINT `fk_room_building` FOREIGN KEY (`building_id`) REFERENCES `dorm_building` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_room_floor` FOREIGN KEY (`floor_id`) REFERENCES `dorm_floor` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-房间表';

/*Data for the table `dorm_room` */

insert  into `dorm_room`(`id`,`building_id`,`floor_id`,`floor_no`,`room_no`,`capacity`,`current_num`,`gender`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`,`version`,`apartment_type`) values 
(1,1,1,1,'101',4,0,2,1,'0','admin','2026-01-15 12:22:12','',NULL,NULL,1,NULL),
(2,1,1,1,'102',4,0,2,1,'0','admin','2026-01-15 12:22:12','',NULL,NULL,1,NULL),
(3,2,3,1,'101',4,0,1,1,'0','admin','2026-01-15 12:22:12','',NULL,NULL,1,NULL);

/*Table structure for table `dorm_staff_application` */

DROP TABLE IF EXISTS `dorm_staff_application`;

CREATE TABLE `dorm_staff_application` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '教工ID',
  `apply_type` tinyint DEFAULT '0' COMMENT '0-新入住 1-退宿 2-换房',
  `target_room_type` varchar(50) DEFAULT NULL COMMENT '期望户型',
  `reason` varchar(255) DEFAULT NULL COMMENT '申请原因',
  `status` tinyint DEFAULT '0' COMMENT '0-待审批 1-通过 2-驳回',
  `remark` varchar(255) DEFAULT NULL COMMENT '审批备注',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='教职工住宿申请单';

/*Data for the table `dorm_staff_application` */

/*Table structure for table `stu_profile` */

DROP TABLE IF EXISTS `stu_profile`;

CREATE TABLE `stu_profile` (
  `user_id` bigint NOT NULL COMMENT '用户ID (主键, 关联 sys_ordinary_user.id)',
  `entry_year` int NOT NULL DEFAULT '2024' COMMENT '入学年份',
  `status` tinyint DEFAULT '0' COMMENT '学籍状态 (0:在读 1:休学 2:毕业 3:退学)',
  `current_bed_id` bigint DEFAULT NULL COMMENT '当前入住床位ID (冗余查询字段)',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生学籍档案扩展表';

/*Data for the table `stu_profile` */

insert  into `stu_profile`(`user_id`,`entry_year`,`status`,`current_bed_id`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(10000,2024,0,NULL,'admin','2026-01-15 12:05:31','',NULL,'0',NULL),
(10001,2024,0,NULL,'admin','2026-01-15 12:05:31','',NULL,'0',NULL),
(10002,2024,0,NULL,'admin','2026-01-15 12:05:31','',NULL,'0',NULL),
(10003,2024,0,NULL,'admin','2026-01-15 12:05:31','',NULL,'0',NULL),
(10004,2024,0,NULL,'admin','2026-01-15 12:05:31','',NULL,'0',NULL),
(2024001,2024,0,NULL,'admin','2026-01-15 11:26:31','',NULL,'0',NULL),
(2024002,2024,0,NULL,'admin','2026-01-15 11:26:31','',NULL,'0',NULL),
(2024003,2024,0,NULL,'admin','2026-01-15 11:26:31','',NULL,'0',NULL),
(2024004,2024,0,NULL,'admin','2026-01-15 11:26:31','',NULL,'0',NULL),
(2024005,2024,0,NULL,'admin','2026-01-15 11:26:31','',NULL,'0',NULL);

/*Table structure for table `sys_admin_user` */

DROP TABLE IF EXISTS `sys_admin_user`;

CREATE TABLE `sys_admin_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(64) NOT NULL COMMENT '登录账号',
  `password` varchar(100) NOT NULL COMMENT '加密密码',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像路径',
  `status` char(1) DEFAULT '0' COMMENT '状态: 0-正常 1-停用',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_name` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-管理员表';

/*Data for the table `sys_admin_user` */

insert  into `sys_admin_user`(`id`,`username`,`password`,`real_name`,`avatar`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,'admin','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','超级管理员',NULL,'0','0','admin','2026-01-15 11:26:31','',NULL,NULL),
(2,'manager_zhang','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','张宿管',NULL,'0','0','admin','2026-01-15 11:26:31','',NULL,NULL),
(3,'manager_li','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','李宿管',NULL,'0','0','admin','2026-01-15 11:26:31','',NULL,NULL),
(4,'counselor_wang','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','王辅导员',NULL,'0','0','admin','2026-01-15 11:26:31','',NULL,NULL),
(5,'repair_master','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','赵维修工',NULL,'0','0','admin','2026-01-15 11:26:31','',NULL,NULL),
(13,'test_admin1','$2a$10$xxx','测试超管',NULL,'0','0','admin','2026-01-15 12:05:31','',NULL,NULL),
(14,'test_dorm1','$2a$10$xxx','海棠宿管',NULL,'0','0','admin','2026-01-15 12:05:31','',NULL,NULL),
(15,'test_repair','$2a$10$xxx','维修工头',NULL,'0','0','admin','2026-01-15 12:05:31','',NULL,NULL),
(16,'test_audit','$2a$10$xxx','审批员',NULL,'0','0','admin','2026-01-15 12:05:31','',NULL,NULL),
(17,'test_view','$2a$10$xxx','观察员',NULL,'0','0','admin','2026-01-15 12:05:31','',NULL,NULL);

/*Table structure for table `sys_algorithm_config` */

DROP TABLE IF EXISTS `sys_algorithm_config`;

CREATE TABLE `sys_algorithm_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键 (如: weight_sleep_time)',
  `config_value` varchar(100) NOT NULL COMMENT '配置值 (如: 0.8)',
  `description` varchar(255) DEFAULT NULL COMMENT '配置说明',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统核心参数配置表';

/*Data for the table `sys_algorithm_config` */

insert  into `sys_algorithm_config`(`id`,`config_key`,`config_value`,`description`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(6,'weight.schedule','0.3','作息时间权重','admin','2026-01-15 11:26:31','',NULL,'0',NULL),
(7,'weight.hygiene','0.2','卫生习惯权重','admin','2026-01-15 11:26:31','',NULL,'0',NULL),
(8,'weight.smoke','100','抽烟一票否决阈值','admin','2026-01-15 11:26:31','',NULL,'0',NULL),
(9,'weight.game','0.1','游戏兴趣权重','admin','2026-01-15 11:26:31','',NULL,'0',NULL),
(10,'allocate.open','true','系统是否开放分配','admin','2026-01-15 11:26:31','',NULL,'0',NULL),
(11,'weight_smoke','0.9','吸烟回避权重','admin','2026-01-15 12:05:31','',NULL,'0',NULL),
(12,'weight_sleep','0.8','作息匹配权重','admin','2026-01-15 12:05:31','',NULL,'0',NULL),
(13,'weight_game','0.5','游戏同好权重','admin','2026-01-15 12:05:31','',NULL,'0',NULL),
(14,'weight_mbti','0.3','性格互补权重','admin','2026-01-15 12:05:31','',NULL,'0',NULL),
(15,'weight_region','0.2','地域融合权重','admin','2026-01-15 12:05:31','',NULL,'0',NULL);

/*Table structure for table `sys_campus` */

DROP TABLE IF EXISTS `sys_campus`;

CREATE TABLE `sys_campus` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '校区ID',
  `campus_name` varchar(64) NOT NULL COMMENT '校区名称 (如: 主校区, 浑南校区)',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '校区联系电话',
  `status` tinyint DEFAULT '1' COMMENT '状态: 1-启用 0-停用',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='校区基础信息表';

/*Data for the table `sys_campus` */

insert  into `sys_campus`(`id`,`campus_name`,`address`,`contact_phone`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,'浑南新校区','沈阳市浑南区智慧大街100号',NULL,1,'admin','2026-01-15 12:22:12','',NULL,'0',NULL),
(2,'铁西校区','沈阳市铁西区建设大路5号',NULL,1,'admin','2026-01-15 12:22:12','',NULL,'0',NULL),
(3,'抚顺分校','抚顺市望花区和平路',NULL,1,'admin','2026-01-15 12:22:12','',NULL,'0',NULL),
(4,'朝阳校区','朝阳市双塔区文化路',NULL,0,'admin','2026-01-15 12:22:12','',NULL,'0',NULL),
(5,'国际交流中心','沈阳市沈河区青年大街',NULL,1,'admin','2026-01-15 12:22:12','',NULL,'0',NULL);

/*Table structure for table `sys_college` */

DROP TABLE IF EXISTS `sys_college`;

CREATE TABLE `sys_college` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '学院ID (对应学号中的两位学院码)',
  `campus_id` bigint NOT NULL COMMENT '所属校区ID',
  `college_name` varchar(64) NOT NULL COMMENT '学院名称 (如: 计算机学院)',
  `short_name` varchar(32) DEFAULT NULL COMMENT '简称',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态: 1-启用 0-停用',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_campus` (`campus_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='二级学院表';

/*Data for the table `sys_college` */

insert  into `sys_college`(`id`,`campus_id`,`college_name`,`short_name`,`sort`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,'外国语学院',NULL,1,1,'admin','2026-01-15 12:22:12','',NULL,'0',NULL),
(2,1,'艺术设计学院',NULL,2,1,'admin','2026-01-15 12:22:12','',NULL,'0',NULL),
(3,2,'机械工程学院',NULL,3,1,'admin','2026-01-15 12:22:12','',NULL,'0',NULL),
(4,2,'材料科学学院',NULL,4,1,'admin','2026-01-15 12:22:12','',NULL,'0',NULL),
(5,3,'石油化工学院',NULL,5,1,'admin','2026-01-15 12:22:12','',NULL,'0',NULL);

/*Table structure for table `sys_dept` */

DROP TABLE IF EXISTS `sys_dept`;

CREATE TABLE `sys_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门ID (对应工号中的两位部门码)',
  `campus_id` bigint NOT NULL COMMENT '所属校区ID',
  `dept_name` varchar(64) NOT NULL COMMENT '部门名称 (如: 后勤处, 财务处)',
  `parent_id` bigint DEFAULT '0' COMMENT '父部门ID (0为顶级)',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态: 1-启用 0-停用',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_campus` (`campus_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='行政部门表';

/*Data for the table `sys_dept` */

insert  into `sys_dept`(`id`,`campus_id`,`dept_name`,`parent_id`,`sort`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,'保卫处',0,0,1,'admin','2026-01-15 12:22:12','',NULL,'0',NULL),
(2,1,'学生工作处',0,0,1,'admin','2026-01-15 12:22:12','',NULL,'0',NULL),
(3,2,'财务处',0,0,1,'admin','2026-01-15 12:22:12','',NULL,'0',NULL),
(4,2,'图书馆',0,0,1,'admin','2026-01-15 12:22:12','',NULL,'0',NULL),
(5,3,'后勤服务中心',0,0,1,'admin','2026-01-15 12:22:12','',NULL,'0',NULL);

/*Table structure for table `sys_major` */

DROP TABLE IF EXISTS `sys_major`;

CREATE TABLE `sys_major` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `college_id` bigint NOT NULL COMMENT '所属学院ID',
  `name` varchar(50) NOT NULL COMMENT '专业名称',
  `level` varchar(20) DEFAULT '本科' COMMENT '培养层次',
  `duration` int DEFAULT '4' COMMENT '学制',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `sort` int DEFAULT '0' COMMENT '排序优先级',
  PRIMARY KEY (`id`),
  KEY `fk_major_college` (`college_id`),
  CONSTRAINT `fk_major_college` FOREIGN KEY (`college_id`) REFERENCES `sys_college` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础信息-专业表';

/*Data for the table `sys_major` */

insert  into `sys_major`(`id`,`college_id`,`name`,`level`,`duration`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`,`sort`) values 
(1,1,'英语','本科',4,'0','admin','2026-01-15 12:22:12','',NULL,NULL,0),
(2,2,'视觉传达设计','本科',4,'0','admin','2026-01-15 12:22:12','',NULL,NULL,0),
(3,2,'环境设计','本科',4,'0','admin','2026-01-15 12:22:12','',NULL,NULL,0),
(4,3,'机械制造及其自动化','本科',4,'0','admin','2026-01-15 12:22:12','',NULL,NULL,0),
(5,5,'化学工程','研究生',3,'0','admin','2026-01-15 12:22:12','',NULL,NULL,0);

/*Table structure for table `sys_notice` */

DROP TABLE IF EXISTS `sys_notice`;

CREATE TABLE `sys_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(128) NOT NULL COMMENT '标题',
  `content` text NOT NULL COMMENT '内容',
  `type` int DEFAULT '1' COMMENT '类型',
  `level` int DEFAULT '0' COMMENT '级别',
  `status` char(1) DEFAULT '0' COMMENT '状态',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-通知公告表';

/*Data for the table `sys_notice` */

insert  into `sys_notice`(`id`,`title`,`content`,`type`,`level`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(12,'2026年寒假离校通知','请各位同学于1月15日前离校...',1,0,'0','0','admin','2026-01-15 11:26:31','',NULL,NULL),
(13,'关于严禁使用违规电器的通知','近期发现多起违规用电...',2,0,'0','0','admin','2026-01-15 11:26:31','',NULL,NULL),
(14,'宿舍卫生大检查安排','本周三下午进行...',1,0,'0','0','admin','2026-01-15 11:26:31','',NULL,NULL),
(15,'教职工选房系统开放通知','新一轮选房将于...',1,0,'0','0','admin','2026-01-15 11:26:31','',NULL,NULL),
(16,'失物招领：海棠苑食堂捡到饭卡','请失主到...',1,0,'0','0','admin','2026-01-15 11:26:31','',NULL,NULL),
(17,'2026春季开学通知','请各位同学按时返校...',1,0,'0','0','admin','2026-01-15 12:05:31','',NULL,NULL),
(18,'关于严禁使用违规电器的警告','近期发现多起热得快...',2,0,'0','0','admin','2026-01-15 12:05:31','',NULL,NULL),
(19,'宿舍网费调整公告','自下月起网费下调...',1,0,'0','0','admin','2026-01-15 12:05:31','',NULL,NULL),
(20,'停水通知','因管道维修，明日停水...',2,0,'0','0','admin','2026-01-15 12:05:31','',NULL,NULL),
(21,'文明寝室评选结果','恭喜101等寝室获奖...',1,0,'0','0','admin','2026-01-15 12:05:31','',NULL,NULL);

/*Table structure for table `sys_ordinary_user` */

DROP TABLE IF EXISTS `sys_ordinary_user`;

CREATE TABLE `sys_ordinary_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(64) NOT NULL COMMENT '学号/工号 (登录账号)',
  `password` varchar(100) NOT NULL COMMENT '加密密码',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像路径',
  `user_category` int DEFAULT '0' COMMENT '类别: 0-学生 1-教职工',
  `sex` tinyint DEFAULT '1' COMMENT '性别: 1-男 2-女',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `college_id` bigint DEFAULT NULL COMMENT '学院ID',
  `major_id` bigint DEFAULT NULL COMMENT '专业ID',
  `class_id` bigint DEFAULT NULL COMMENT '班级ID',
  `residence_type` int DEFAULT '0' COMMENT '居住类型: 0-住校 1-校外',
  `status` char(1) DEFAULT '0' COMMENT '状态: 0-正常 1-停用',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `id_card` varchar(20) DEFAULT NULL COMMENT '身份证号',
  `ethnicity` varchar(20) DEFAULT '汉族' COMMENT '民族',
  `campus_id` bigint DEFAULT NULL COMMENT '冗余校区ID (方便查询)',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID (教工用)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `fk_user_class` (`class_id`),
  CONSTRAINT `fk_user_class` FOREIGN KEY (`class_id`) REFERENCES `biz_class` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-普通用户表';

/*Data for the table `sys_ordinary_user` */

insert  into `sys_ordinary_user`(`id`,`username`,`password`,`real_name`,`avatar`,`user_category`,`sex`,`phone`,`college_id`,`major_id`,`class_id`,`residence_type`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`,`id_card`,`ethnicity`,`campus_id`,`dept_id`) values 
(1,'2024001','$2a$10$xxx','张三',NULL,0,1,NULL,1,NULL,NULL,0,'0','0','admin','2026-01-15 12:22:12','',NULL,NULL,NULL,'汉族',NULL,NULL),
(2,'2024002','$2a$10$xxx','李四',NULL,0,1,NULL,1,NULL,NULL,0,'0','0','admin','2026-01-15 12:22:12','',NULL,NULL,NULL,'汉族',NULL,NULL),
(3,'2024003','$2a$10$xxx','王五',NULL,0,2,NULL,2,NULL,NULL,0,'0','0','admin','2026-01-15 12:22:12','',NULL,NULL,NULL,'汉族',NULL,NULL),
(4,'2024004','$2a$10$xxx','赵六',NULL,0,2,NULL,2,NULL,NULL,0,'0','0','admin','2026-01-15 12:22:12','',NULL,NULL,NULL,'汉族',NULL,NULL),
(5,'2024005','$2a$10$xxx','钱七',NULL,0,1,NULL,3,NULL,NULL,1,'0','0','admin','2026-01-15 12:22:12','',NULL,NULL,NULL,'汉族',NULL,NULL),
(6,'80001','$2a$10$xxx','王教授',NULL,1,1,NULL,NULL,NULL,NULL,1,'0','0','admin','2026-01-15 12:22:12','',NULL,NULL,NULL,'汉族',NULL,1),
(7,'80002','$2a$10$xxx','李老师',NULL,1,2,NULL,NULL,NULL,NULL,0,'0','0','admin','2026-01-15 12:22:12','',NULL,NULL,NULL,'汉族',NULL,1);

/*Table structure for table `sys_role` */

DROP TABLE IF EXISTS `sys_role`;

CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) NOT NULL COMMENT '角色标识',
  `status` char(1) DEFAULT '0' COMMENT '状态',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `sys_role` */

insert  into `sys_role`(`id`,`role_name`,`role_key`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,'超级管理员','super_admin','0','0','admin','2026-01-13 14:52:42','',NULL,NULL),
(2,'宿管经理','dorm_manager','0','0','admin','2026-01-13 14:52:42','',NULL,NULL),
(3,'辅导员','counselor','0','0','admin','2026-01-13 14:52:42','',NULL,NULL),
(4,'学生','student','0','0','admin','2026-01-13 14:52:42','',NULL,NULL),
(5,'维修人员','repair_staff','0','0','admin','2026-01-15 12:05:31','',NULL,NULL),
(6,'财务人员','finance_staff','0','0','admin','2026-01-15 12:05:31','',NULL,NULL),
(7,'保卫人员','security_staff','0','0','admin','2026-01-15 12:05:31','',NULL,NULL),
(8,'学院辅导员','college_teacher','0','0','admin','2026-01-15 12:05:31','',NULL,NULL),
(9,'访客','visitor','0','0','admin','2026-01-15 12:05:31','',NULL,NULL);

/*Table structure for table `sys_user_role` */

DROP TABLE IF EXISTS `sys_user_role`;

CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `sys_user_role` */

insert  into `sys_user_role`(`user_id`,`role_id`,`create_time`) values 
(1001,4,'2026-01-13 16:02:37'),
(1002,4,'2026-01-13 16:02:37'),
(1003,4,'2026-01-13 16:02:37'),
(1004,4,'2026-01-13 16:02:37'),
(1005,4,'2026-01-13 16:02:37'),
(1006,4,'2026-01-13 16:02:37');

/*Table structure for table `sys_utility_price` */

DROP TABLE IF EXISTS `sys_utility_price`;

CREATE TABLE `sys_utility_price` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` int NOT NULL COMMENT '类型: 1-水 2-电',
  `name` varchar(50) NOT NULL COMMENT '费用名称 (如: 居民用电)',
  `price` decimal(10,4) NOT NULL COMMENT '单价 (元)',
  `unit` varchar(10) DEFAULT NULL COMMENT '单位 (度/吨)',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='水电费单价配置';

/*Data for the table `sys_utility_price` */

insert  into `sys_utility_price`(`id`,`type`,`name`,`price`,`unit`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(5,1,'居民生活用水',3.5000,'吨','admin','2026-01-15 11:26:31','',NULL,'0',NULL),
(6,1,'非居民用水',5.0000,'吨','admin','2026-01-15 11:26:31','',NULL,'0',NULL),
(7,2,'基础电价',0.5000,'度','admin','2026-01-15 11:26:31','',NULL,'0',NULL),
(8,2,'第二阶梯电价',0.6000,'度','admin','2026-01-15 11:26:31','',NULL,'0',NULL),
(9,2,'商业用电',1.0000,'度','admin','2026-01-15 11:26:31','',NULL,'0',NULL),
(10,1,'居民生活用水',3.5000,'吨','admin','2026-01-15 12:05:31','',NULL,'0',NULL),
(11,1,'商业用水',5.5000,'吨','admin','2026-01-15 12:05:31','',NULL,'0',NULL),
(12,2,'基础电价',0.5500,'度','admin','2026-01-15 12:05:31','',NULL,'0',NULL),
(13,2,'峰时电价',0.8500,'度','admin','2026-01-15 12:05:31','',NULL,'0',NULL),
(14,2,'谷时电价',0.3500,'度','admin','2026-01-15 12:05:31','',NULL,'0',NULL);

/*Table structure for table `sys_utility_price_rule` */

DROP TABLE IF EXISTS `sys_utility_price_rule`;

CREATE TABLE `sys_utility_price_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rule_name` varchar(100) NOT NULL COMMENT '规则名称',
  `utility_type` tinyint NOT NULL COMMENT '1-电, 2-水',
  `tier_start` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '阶梯起始量',
  `tier_end` decimal(10,2) DEFAULT '9999.99' COMMENT '阶梯结束量',
  `unit_price` decimal(10,3) NOT NULL COMMENT '单价',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='水电价位表';

/*Data for the table `sys_utility_price_rule` */

insert  into `sys_utility_price_rule`(`id`,`rule_name`,`utility_type`,`tier_start`,`tier_end`,`unit_price`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(4,'一阶水费',2,0.00,10.00,3.500,'admin','2026-01-15 11:26:31',NULL,NULL,'0',NULL),
(5,'二阶水费',2,10.01,20.00,4.500,'admin','2026-01-15 11:26:31',NULL,NULL,'0',NULL),
(6,'三阶水费',2,20.01,9999.00,6.000,'admin','2026-01-15 11:26:31',NULL,NULL,'0',NULL),
(7,'一阶电费',1,0.00,100.00,0.500,'admin','2026-01-15 11:26:31',NULL,NULL,'0',NULL),
(8,'二阶电费',1,100.01,9999.00,0.600,'admin','2026-01-15 11:26:31',NULL,NULL,'0',NULL),
(9,'一阶水费',2,0.00,10.00,3.500,'admin','2026-01-15 12:05:31',NULL,NULL,'0',NULL),
(10,'二阶水费',2,10.00,20.00,4.500,'admin','2026-01-15 12:05:31',NULL,NULL,'0',NULL),
(11,'三阶水费',2,20.00,9999.00,6.000,'admin','2026-01-15 12:05:31',NULL,NULL,'0',NULL),
(12,'一阶电费',1,0.00,100.00,0.550,'admin','2026-01-15 12:05:31',NULL,NULL,'0',NULL),
(13,'二阶电费',1,100.00,300.00,0.650,'admin','2026-01-15 12:05:31',NULL,NULL,'0',NULL);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
