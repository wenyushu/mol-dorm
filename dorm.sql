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
  `grade` int DEFAULT NULL COMMENT '年级(如2024)',
  `class_name` varchar(100) NOT NULL COMMENT '班级名称',
  `counselor_id` bigint DEFAULT NULL COMMENT '辅导员ID',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_major` (`major_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-班级表';

/*Data for the table `biz_class` */

insert  into `biz_class`(`id`,`major_id`,`grade`,`class_name`,`counselor_id`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,2024,'软工2401',NULL,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(2,1,2024,'软工2402',NULL,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(3,2,2024,'AI2401',NULL,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(4,3,2024,'英语2401',NULL,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(5,3,2024,'英语2402',NULL,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(6,5,2024,'机自2401',NULL,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(7,5,2024,'机自2402',NULL,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(8,7,2024,'土木2401',NULL,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(9,1,2023,'软工2301',NULL,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(10,3,2023,'英语2301',NULL,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(11,1,2025,'软工2501',NULL,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(12,1,2025,'软工2502',NULL,'admin','2026-01-18 18:30:34','',NULL,'0',NULL);

/*Table structure for table `biz_electric_violation_log` */

DROP TABLE IF EXISTS `biz_electric_violation_log`;

CREATE TABLE `biz_electric_violation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `violation_type` int NOT NULL COMMENT '1超载 2恶性负载',
  `power_val` decimal(10,2) DEFAULT NULL,
  `detected_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'system',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物联-违规用电日志';

/*Data for the table `biz_electric_violation_log` */

/*Table structure for table `biz_repair_order` */

DROP TABLE IF EXISTS `biz_repair_order`;

CREATE TABLE `biz_repair_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL,
  `room_id` bigint NOT NULL,
  `applicant_id` bigint NOT NULL,
  `description` text NOT NULL,
  `images` varchar(1000) DEFAULT NULL,
  `status` int DEFAULT '0' COMMENT '0待处理 1维修中 2已完成',
  `repairman_id` bigint DEFAULT NULL,
  `finish_time` datetime DEFAULT NULL,
  `rating` tinyint DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-报修工单';

/*Data for the table `biz_repair_order` */

insert  into `biz_repair_order`(`id`,`order_no`,`room_id`,`applicant_id`,`description`,`images`,`status`,`repairman_id`,`finish_time`,`rating`,`comment`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,'REP202401001',2,14,'空调不制冷',NULL,0,NULL,NULL,NULL,NULL,'0','admin','2026-01-18 18:30:34','',NULL,NULL),
(2,'REP202401002',1,10,'灯泡坏了',NULL,2,NULL,NULL,NULL,NULL,'0','admin','2026-01-15 18:30:34','',NULL,NULL),
(3,'REP_TEST_001',1,60,'插座冒火星了',NULL,1,14,NULL,NULL,NULL,'0','admin','2026-01-18 18:34:56','','2026-01-18 18:41:29',NULL);

/*Table structure for table `biz_staff_profile` */

DROP TABLE IF EXISTS `biz_staff_profile`;

CREATE TABLE `biz_staff_profile` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  `job_title` varchar(50) NOT NULL COMMENT '职称',
  `contract_type` varchar(20) DEFAULT '长期' COMMENT '合同类型',
  `housing_intent` tinyint DEFAULT '0' COMMENT '住宿意向:0无 1有',
  `current_bed_id` bigint DEFAULT NULL COMMENT '当前床位ID',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-教工档案扩展';

/*Data for the table `biz_staff_profile` */

insert  into `biz_staff_profile`(`user_id`,`dept_id`,`job_title`,`contract_type`,`housing_intent`,`current_bed_id`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(2,2,'职员','长期',0,NULL,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(3,2,'职员','长期',0,NULL,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(4,1,'职员','长期',0,30,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(5,1,'职员','长期',0,NULL,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(6,1,'职员','长期',0,NULL,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(56,1,'教授','长期',1,NULL,'admin','2026-01-18 18:34:56','',NULL,'0',NULL),
(57,1,'讲师','长期',1,NULL,'admin','2026-01-18 18:34:56','',NULL,'0',NULL);

/*Table structure for table `biz_user_preference` */

DROP TABLE IF EXISTS `biz_user_preference`;

CREATE TABLE `biz_user_preference` (
  `user_id` bigint NOT NULL COMMENT '用户ID (sys_ordinary_user.id)',
  `team_code` varchar(32) DEFAULT NULL COMMENT '组队码',
  `bed_time` tinyint DEFAULT '3' COMMENT '就寝:1-21点..6-2点+',
  `wake_time` tinyint DEFAULT '3' COMMENT '起床:1-6点..6-11点+',
  `siesta_habit` tinyint DEFAULT '0' COMMENT '午睡:0无 1偶尔 2必须',
  `out_late_freq` tinyint DEFAULT '0' COMMENT '晚归:0无 1偶尔 2经常',
  `sleep_quality` tinyint DEFAULT '2' COMMENT '深浅:1雷打不动 4神经衰弱',
  `snoring_level` tinyint DEFAULT '0' COMMENT '呼噜:0无 3严重',
  `grinding_teeth` tinyint DEFAULT '0' COMMENT '磨牙:0无 1有',
  `sleep_talk` tinyint DEFAULT '0' COMMENT '梦话:0无 1有',
  `climb_bed_noise` tinyint DEFAULT '1' COMMENT '上床动静:0轻 2拆迁队',
  `shower_freq` tinyint DEFAULT '1' COMMENT '洗澡:1每天 3不定',
  `sock_wash` tinyint DEFAULT '0' COMMENT '袜子:0当天 1攒一堆',
  `trash_habit` tinyint DEFAULT '2' COMMENT '倒垃圾:1满倒 2轮流',
  `clean_freq` tinyint DEFAULT '2' COMMENT '打扫:1天 2周 3月',
  `toilet_clean` tinyint DEFAULT '1' COMMENT '刷厕所:1接受 0拒绝',
  `desk_messy` tinyint DEFAULT '2' COMMENT '桌面:1极简 2乱中有序',
  `personal_hygiene` tinyint DEFAULT '3' COMMENT '自评:1邋遢 5洁癖',
  `odor_tolerance` tinyint DEFAULT '2' COMMENT '异味容忍:1不可 3强',
  `smoking` tinyint DEFAULT '0' COMMENT '抽烟:0不 1阳台 2室内',
  `smoke_tolerance` tinyint DEFAULT '0' COMMENT '接受烟味:0不可 1可',
  `drinking` tinyint DEFAULT '0' COMMENT '喝酒:0无 1小酌 2酗酒',
  `ac_temp` tinyint DEFAULT '26' COMMENT '空调温度:16-30',
  `ac_duration` tinyint DEFAULT '2' COMMENT '空调时长:1整晚 2定时',
  `game_type_lol` tinyint DEFAULT '0' COMMENT 'LOL/DOTA: 0否 1是',
  `game_type_fps` tinyint DEFAULT '0' COMMENT 'FPS: 0否 1是',
  `game_type_3a` tinyint DEFAULT '0' COMMENT '3A大作: 0否 1是',
  `game_type_mmo` tinyint DEFAULT '0' COMMENT 'MMO: 0否 1是',
  `game_type_mobile` tinyint DEFAULT '0' COMMENT '手游: 0否 1是',
  `game_habit` tinyint DEFAULT '0' COMMENT '游戏程度:0无 1轻 2重',
  `game_voice` tinyint DEFAULT '1' COMMENT '连麦:0静音 2咆哮',
  `keyboard_axis` tinyint DEFAULT '1' COMMENT '键盘:1静音 3青轴',
  `is_cosplay` tinyint DEFAULT '0' COMMENT 'Cosplay: 0否 1是',
  `is_anime` tinyint DEFAULT '0' COMMENT '二次元: 0现充 2老二刺螈',
  `mbti_ei` char(1) DEFAULT NULL COMMENT 'MBTI: E/I',
  `mbti_result` varchar(4) DEFAULT NULL COMMENT 'MBTI完整结果',
  `social_battery` tinyint DEFAULT '3' COMMENT '社交意愿:1社恐 5社牛',
  `share_items` tinyint DEFAULT '1' COMMENT '共享:0不可 2随意',
  `bring_guest` tinyint DEFAULT '0' COMMENT '带人回寝:0绝不 3带异性',
  `visitors` tinyint DEFAULT '0' COMMENT '接受访客:同上',
  `relationship_status` tinyint DEFAULT '0' COMMENT '恋爱:0单 1恋',
  `has_disability` tinyint DEFAULT '0' COMMENT '残疾:0无 1有',
  `has_insulin` tinyint DEFAULT '0' COMMENT '胰岛素:0无 1有',
  `has_infectious` tinyint DEFAULT '0' COMMENT '传染病:0无 1有',
  `religion_taboo` varchar(255) DEFAULT NULL COMMENT '宗教禁忌',
  `special_disease` varchar(255) DEFAULT NULL COMMENT '特殊疾病',
  `game_rank` tinyint DEFAULT '0' COMMENT '段位:0低 3高',
  `game_role` tinyint DEFAULT '0' COMMENT '位置:0全能 5辅助',
  `eat_luosifen` tinyint DEFAULT '0' COMMENT '螺蛳粉:0拒 2爱',
  `eat_durian` tinyint DEFAULT '0' COMMENT '榴莲:0拒 1吃',
  `region_type` tinyint DEFAULT NULL COMMENT '籍贯:0南 1北',
  `create_by` varchar(64) DEFAULT 'system',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-用户全维度画像表';

/*Data for the table `biz_user_preference` */

insert  into `biz_user_preference`(`user_id`,`team_code`,`bed_time`,`wake_time`,`siesta_habit`,`out_late_freq`,`sleep_quality`,`snoring_level`,`grinding_teeth`,`sleep_talk`,`climb_bed_noise`,`shower_freq`,`sock_wash`,`trash_habit`,`clean_freq`,`toilet_clean`,`desk_messy`,`personal_hygiene`,`odor_tolerance`,`smoking`,`smoke_tolerance`,`drinking`,`ac_temp`,`ac_duration`,`game_type_lol`,`game_type_fps`,`game_type_3a`,`game_type_mmo`,`game_type_mobile`,`game_habit`,`game_voice`,`keyboard_axis`,`is_cosplay`,`is_anime`,`mbti_ei`,`mbti_result`,`social_battery`,`share_items`,`bring_guest`,`visitors`,`relationship_status`,`has_disability`,`has_insulin`,`has_infectious`,`religion_taboo`,`special_disease`,`game_rank`,`game_role`,`eat_luosifen`,`eat_durian`,`region_type`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(10,NULL,2,2,0,0,2,0,0,0,1,1,0,2,2,1,2,3,2,0,0,0,26,2,0,0,0,0,0,1,1,1,0,0,'E','ESFP',3,1,0,0,0,0,0,0,NULL,NULL,0,0,0,0,NULL,'system','2026-01-18 18:30:34','',NULL,'0',NULL),
(11,NULL,5,5,0,0,2,0,0,0,1,1,0,2,2,1,2,3,2,1,0,0,26,2,0,0,0,0,0,2,1,1,0,0,'I','INTP',3,1,0,0,0,0,0,0,NULL,NULL,0,0,0,0,NULL,'system','2026-01-18 18:30:34','',NULL,'0',NULL),
(12,NULL,3,3,0,0,2,0,0,0,1,1,0,2,2,1,2,3,2,0,0,0,26,2,0,0,0,0,0,0,1,1,0,0,'I','ISFJ',3,1,0,0,0,0,0,0,NULL,NULL,0,0,0,0,NULL,'system','2026-01-18 18:30:34','',NULL,'0',NULL),
(20,NULL,1,1,0,0,2,0,0,0,1,1,0,2,2,1,2,3,2,0,0,0,26,2,0,0,0,0,0,1,1,1,0,0,'E','ENFJ',3,1,0,0,0,0,0,0,NULL,NULL,0,0,0,0,NULL,'system','2026-01-18 18:30:34','',NULL,'0',NULL),
(56,NULL,3,3,0,0,2,0,0,0,1,1,0,2,2,1,2,3,2,0,0,0,26,2,0,0,0,0,0,0,1,1,0,0,'I',NULL,3,1,0,0,0,0,0,0,NULL,NULL,0,0,0,0,NULL,'system','2026-01-18 18:34:56','',NULL,'0',NULL),
(60,NULL,2,3,0,0,2,0,0,0,1,1,0,2,2,1,2,3,2,0,0,0,26,2,0,0,0,0,0,0,1,1,0,0,'I',NULL,3,1,0,0,0,0,0,0,NULL,NULL,0,0,0,0,NULL,'system','2026-01-18 18:34:56','',NULL,'0',NULL),
(61,NULL,6,3,0,0,2,0,0,0,1,1,0,2,2,1,2,3,2,2,0,0,26,2,0,0,0,0,0,2,1,1,0,0,'E',NULL,3,1,0,0,0,0,0,0,NULL,NULL,0,0,0,0,NULL,'system','2026-01-18 18:34:56','',NULL,'0',NULL);

/*Table structure for table `biz_utility_bill` */

DROP TABLE IF EXISTS `biz_utility_bill`;

CREATE TABLE `biz_utility_bill` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `month` varchar(7) NOT NULL COMMENT 'YYYY-MM',
  `total_amount` decimal(10,2) DEFAULT '0.00',
  `status` int DEFAULT '0' COMMENT '0未付 1已付',
  `version` int DEFAULT '1',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_month` (`room_id`,`month`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-水电账单';

/*Data for the table `biz_utility_bill` */

insert  into `biz_utility_bill`(`id`,`room_id`,`month`,`total_amount`,`status`,`version`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,1,'2024-01',120.50,1,1,'0','admin','2026-01-18 18:30:34','',NULL,NULL),
(2,1,'2024-02',88.00,0,1,'0','admin','2026-01-18 18:30:34','',NULL,NULL),
(3,2,'2024-02',45.00,0,1,'0','admin','2026-01-18 18:30:34','',NULL,NULL);

/*Table structure for table `dorm_bed` */

DROP TABLE IF EXISTS `dorm_bed`;

CREATE TABLE `dorm_bed` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `campus_id` bigint DEFAULT NULL,
  `building_id` bigint DEFAULT NULL,
  `floor_id` bigint DEFAULT NULL,
  `room_id` bigint NOT NULL,
  `bed_label` varchar(32) DEFAULT NULL COMMENT '床号(1号)',
  `sort_order` int NOT NULL DEFAULT '1',
  `occupant_id` bigint DEFAULT NULL COMMENT '入住人ID',
  `occupant_type` int DEFAULT '0' COMMENT '0学生 1教工',
  `status` int NOT NULL DEFAULT '0' COMMENT '0空闲 1占用',
  `version` int DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_room` (`room_id`),
  KEY `idx_occupant` (`occupant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=155 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-床位表';

/*Data for the table `dorm_bed` */

insert  into `dorm_bed`(`id`,`campus_id`,`building_id`,`floor_id`,`room_id`,`bed_label`,`sort_order`,`occupant_id`,`occupant_type`,`status`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,1,1,1,'101-1',1,10,0,1,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(2,1,1,1,2,'102-1',1,14,0,1,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(3,1,1,1,3,'103-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(4,1,1,2,4,'201-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(5,1,1,2,5,'202-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(6,1,1,2,6,'203-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(7,1,1,3,7,'301-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(8,1,1,3,8,'302-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(9,1,1,3,9,'303-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(10,1,1,4,10,'401-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(11,1,1,4,11,'402-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(12,1,1,5,12,'501-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(13,1,2,6,13,'101-1',1,20,0,1,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(14,1,2,6,14,'102-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(15,1,2,6,15,'103-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(16,1,2,7,16,'201-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(17,1,2,7,17,'202-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(18,1,2,8,18,'301-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(19,1,2,8,19,'302-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(20,1,2,9,20,'401-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(21,1,2,10,21,'501-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(22,2,3,11,22,'101-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(23,2,3,11,23,'102-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(24,2,3,12,24,'201-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(25,2,3,12,25,'202-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(26,2,3,13,26,'301-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(27,2,3,13,27,'302-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(28,2,3,14,28,'401-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(29,2,3,15,29,'501-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(30,1,4,16,30,'101-1',1,4,1,1,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(31,1,4,16,31,'102-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(32,1,4,17,32,'201-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(33,1,4,17,33,'202-1',1,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(64,1,1,1,1,'101-2',2,11,0,1,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(65,1,1,1,2,'102-2',2,15,0,1,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(66,1,1,1,3,'103-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(67,1,1,2,4,'201-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(68,1,1,2,5,'202-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(69,1,1,2,6,'203-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(70,1,1,3,7,'301-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(71,1,1,3,8,'302-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(72,1,1,3,9,'303-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(73,1,1,4,10,'401-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(74,1,1,4,11,'402-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(75,1,1,5,12,'501-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(76,1,2,6,13,'101-2',2,21,0,1,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(77,1,2,6,14,'102-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(78,1,2,6,15,'103-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(79,1,2,7,16,'201-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(80,1,2,7,17,'202-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(81,1,2,8,18,'301-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(82,1,2,8,19,'302-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(83,1,2,9,20,'401-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(84,1,2,10,21,'501-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(85,2,3,11,22,'101-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(86,2,3,11,23,'102-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(87,2,3,12,24,'201-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(88,2,3,12,25,'202-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(89,2,3,13,26,'301-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(90,2,3,13,27,'302-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(91,2,3,14,28,'401-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(92,2,3,15,29,'501-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(93,1,4,16,31,'102-2',2,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(95,1,1,1,1,'101-3',3,12,0,1,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(96,1,1,1,2,'102-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(97,1,1,1,3,'103-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(98,1,1,2,4,'201-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(99,1,1,2,5,'202-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(100,1,1,2,6,'203-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(101,1,1,3,7,'301-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(102,1,1,3,8,'302-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(103,1,1,3,9,'303-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(104,1,1,4,10,'401-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(105,1,1,4,11,'402-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(106,1,1,5,12,'501-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(107,1,2,6,13,'101-3',3,22,0,1,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(108,1,2,6,14,'102-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(109,1,2,6,15,'103-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(110,1,2,7,16,'201-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(111,1,2,7,17,'202-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(112,1,2,8,18,'301-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(113,1,2,8,19,'302-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(114,1,2,9,20,'401-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(115,1,2,10,21,'501-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(116,2,3,11,22,'101-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(117,2,3,11,23,'102-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(118,2,3,12,24,'201-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(119,2,3,12,25,'202-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(120,2,3,13,26,'301-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(121,2,3,13,27,'302-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(122,2,3,14,28,'401-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(123,2,3,15,29,'501-3',3,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(126,1,1,1,1,'101-4',4,13,0,1,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(127,1,1,1,2,'102-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(128,1,1,1,3,'103-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(129,1,1,2,4,'201-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(130,1,1,2,5,'202-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(131,1,1,2,6,'203-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(132,1,1,3,7,'301-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(133,1,1,3,8,'302-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(134,1,1,3,9,'303-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(135,1,1,4,10,'401-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(136,1,1,4,11,'402-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(137,1,1,5,12,'501-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(138,1,2,6,13,'101-4',4,23,0,1,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(139,1,2,6,14,'102-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(140,1,2,6,15,'103-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(141,1,2,7,16,'201-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(142,1,2,7,17,'202-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(143,1,2,8,18,'301-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(144,1,2,8,19,'302-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(145,1,2,9,20,'401-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(146,1,2,10,21,'501-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(147,2,3,11,22,'101-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(148,2,3,11,23,'102-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(149,2,3,12,24,'201-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(150,2,3,12,25,'202-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(151,2,3,13,26,'301-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(152,2,3,13,27,'302-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(153,2,3,14,28,'401-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(154,2,3,15,29,'501-4',4,NULL,0,0,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL);

/*Table structure for table `dorm_building` */

DROP TABLE IF EXISTS `dorm_building`;

CREATE TABLE `dorm_building` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint NOT NULL COMMENT '校区ID',
  `building_name` varchar(64) NOT NULL COMMENT '楼栋名',
  `building_no` varchar(32) DEFAULT NULL COMMENT '编号',
  `floor_count` int DEFAULT NULL COMMENT '层数',
  `gender_limit` int NOT NULL DEFAULT '3' COMMENT '限制:1男 2女 3混合',
  `usage_type` int NOT NULL DEFAULT '0' COMMENT '用途:0学生 1教工',
  `manager_id` bigint DEFAULT NULL COMMENT '宿管ID',
  `location` varchar(255) DEFAULT NULL,
  `status` int NOT NULL DEFAULT '1' COMMENT '1启用 0停用 41装修',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_campus` (`campus_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-楼栋表';

/*Data for the table `dorm_building` */

insert  into `dorm_building`(`id`,`campus_id`,`building_name`,`building_no`,`floor_count`,`gender_limit`,`usage_type`,`manager_id`,`location`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,'海棠苑1号楼','HT-01',5,1,0,NULL,NULL,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(2,1,'海棠苑2号楼','HT-02',5,2,0,NULL,NULL,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(3,2,'丁香苑1号楼','DX-01',5,3,0,NULL,NULL,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(4,1,'教师公寓A座','TE-A',5,3,1,NULL,NULL,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL);

/*Table structure for table `dorm_change_request` */

DROP TABLE IF EXISTS `dorm_change_request`;

CREATE TABLE `dorm_change_request` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `current_room_id` bigint NOT NULL,
  `target_room_id` bigint DEFAULT NULL,
  `type` int DEFAULT '0' COMMENT '0换房 1退宿 2互换',
  `reason` varchar(500) DEFAULT NULL,
  `status` int DEFAULT '0' COMMENT '0待审 1通过 2驳回',
  `swap_student_id` bigint DEFAULT NULL,
  `audit_msg` varchar(255) DEFAULT NULL,
  `apply_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-调宿申请';

/*Data for the table `dorm_change_request` */

insert  into `dorm_change_request`(`id`,`student_id`,`current_room_id`,`target_room_id`,`type`,`reason`,`status`,`swap_student_id`,`audit_msg`,`apply_time`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,15,2,NULL,0,'和室友作息不合',0,NULL,NULL,'2026-01-18 18:30:34','0','admin','2026-01-18 18:30:34','',NULL,NULL),
(2,31,0,NULL,1,'休学退宿',1,NULL,NULL,'2026-01-18 18:30:34','0','admin','2026-01-18 18:30:34','',NULL,NULL);

/*Table structure for table `dorm_fixed_asset` */

DROP TABLE IF EXISTS `dorm_fixed_asset`;

CREATE TABLE `dorm_fixed_asset` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `asset_name` varchar(100) NOT NULL,
  `asset_code` varchar(64) NOT NULL,
  `category` int NOT NULL COMMENT '1家具 2电器',
  `room_id` bigint NOT NULL,
  `price` decimal(10,2) DEFAULT '0.00',
  `status` int DEFAULT '1' COMMENT '1正常 2报修 3丢失',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_asset_code` (`asset_code`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资产表';

/*Data for the table `dorm_fixed_asset` */

insert  into `dorm_fixed_asset`(`id`,`asset_name`,`asset_code`,`category`,`room_id`,`price`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,'空调','AC-101-01',2,1,0.00,1,'0','admin','2026-01-18 18:30:34','',NULL,NULL),
(2,'书桌','TB-101-01',1,1,0.00,1,'0','admin','2026-01-18 18:30:34','',NULL,NULL),
(3,'空调','AC-102-01',2,2,0.00,2,'0','admin','2026-01-18 18:30:34','',NULL,NULL),
(4,'热水器','WH-201-01',2,4,0.00,3,'0','admin','2026-01-18 18:30:34','',NULL,NULL);

/*Table structure for table `dorm_floor` */

DROP TABLE IF EXISTS `dorm_floor`;

CREATE TABLE `dorm_floor` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `campus_id` bigint DEFAULT NULL,
  `building_id` bigint NOT NULL,
  `floor_num` int NOT NULL COMMENT '楼层号',
  `gender_limit` int NOT NULL DEFAULT '0' COMMENT '0混合 1男 2女',
  `status` int NOT NULL DEFAULT '1',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_building` (`building_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-楼层表';

/*Data for the table `dorm_floor` */

insert  into `dorm_floor`(`id`,`campus_id`,`building_id`,`floor_num`,`gender_limit`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,1,1,1,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(2,1,1,2,1,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(3,1,1,3,1,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(4,1,1,4,1,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(5,1,1,5,1,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(6,1,2,1,2,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(7,1,2,2,2,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(8,1,2,3,2,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(9,1,2,4,2,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(10,1,2,5,2,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(11,2,3,1,1,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(12,2,3,2,1,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(13,2,3,3,2,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(14,2,3,4,2,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(15,2,3,5,2,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(16,1,4,1,0,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(17,1,4,2,0,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(18,1,4,3,0,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(19,1,4,4,0,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(20,1,4,5,0,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL);

/*Table structure for table `dorm_meter_electric` */

DROP TABLE IF EXISTS `dorm_meter_electric`;

CREATE TABLE `dorm_meter_electric` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `meter_no` varchar(50) NOT NULL,
  `current_reading` decimal(10,2) DEFAULT '0.00',
  `balance` decimal(10,2) DEFAULT '0.00',
  `status` int DEFAULT '1',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物联-电表';

/*Data for the table `dorm_meter_electric` */

/*Table structure for table `dorm_meter_water` */

DROP TABLE IF EXISTS `dorm_meter_water`;

CREATE TABLE `dorm_meter_water` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `meter_no` varchar(50) NOT NULL,
  `current_reading` decimal(10,2) DEFAULT '0.00',
  `status` int DEFAULT '1',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物联-水表';

/*Data for the table `dorm_meter_water` */

/*Table structure for table `dorm_room` */

DROP TABLE IF EXISTS `dorm_room`;

CREATE TABLE `dorm_room` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `campus_id` bigint DEFAULT NULL,
  `building_id` bigint DEFAULT NULL,
  `floor_id` bigint NOT NULL,
  `floor_no` int DEFAULT NULL COMMENT '冗余层号',
  `room_no` varchar(32) NOT NULL COMMENT '房号(101)',
  `apartment_type` varchar(32) DEFAULT '四人间',
  `capacity` int DEFAULT '4' COMMENT '床位数',
  `current_num` int DEFAULT '0' COMMENT '实住人数',
  `gender` char(1) NOT NULL DEFAULT '1' COMMENT '0女 1男',
  `status` int NOT NULL DEFAULT '10' COMMENT '10正常 20满员 40维修',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_hierarchy` (`campus_id`,`building_id`,`floor_id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-房间表';

/*Data for the table `dorm_room` */

insert  into `dorm_room`(`id`,`campus_id`,`building_id`,`floor_id`,`floor_no`,`room_no`,`apartment_type`,`capacity`,`current_num`,`gender`,`status`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,1,1,1,'101','四人间',4,4,'1',20,0,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(2,1,1,1,1,'102','四人间',4,2,'1',10,0,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(3,1,1,1,1,'103','四人间',4,0,'1',10,0,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(4,1,1,2,2,'201','四人间',4,0,'1',40,0,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(5,1,1,2,2,'202','四人间',4,0,'1',20,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(6,1,1,2,2,'203','四人间',4,0,'1',20,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(7,1,1,3,3,'301','四人间',4,0,'1',20,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(8,1,1,3,3,'302','四人间',4,0,'1',20,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(9,1,1,3,3,'303','四人间',4,0,'1',20,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(10,1,1,4,4,'401','四人间',4,0,'1',20,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(11,1,1,4,4,'402','四人间',4,0,'1',20,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(12,1,1,5,5,'501','四人间',4,0,'1',20,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(13,1,2,6,1,'101','四人间',4,4,'0',20,0,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(14,1,2,6,1,'102','四人间',4,0,'0',20,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(15,1,2,6,1,'103','四人间',4,0,'0',10,0,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(16,1,2,7,2,'201','四人间',4,0,'0',20,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(17,1,2,7,2,'202','四人间',4,0,'0',20,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(18,1,2,8,3,'301','四人间',4,0,'0',20,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(19,1,2,8,3,'302','四人间',4,0,'0',20,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(20,1,2,9,4,'401','四人间',4,0,'0',20,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(21,1,2,10,5,'501','四人间',4,0,'0',20,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(22,2,3,11,1,'101','四人间',4,0,'1',20,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(23,2,3,11,1,'102','四人间',4,0,'1',20,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(24,2,3,12,2,'201','四人间',4,0,'1',20,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(25,2,3,12,2,'202','四人间',4,0,'1',20,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(26,2,3,13,3,'301','四人间',4,0,'0',20,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(27,2,3,13,3,'302','四人间',4,0,'0',20,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(28,2,3,14,4,'401','四人间',4,0,'0',20,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(29,2,3,15,5,'501','四人间',4,0,'0',20,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(30,1,4,16,1,'101','四人间',1,1,'1',20,0,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(31,1,4,16,1,'102','四人间',2,0,'0',10,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(32,1,4,17,2,'201','四人间',1,0,'1',20,0,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(33,1,4,17,2,'202','四人间',1,0,'0',10,0,'admin','2026-01-18 18:30:34','',NULL,'0',NULL);

/*Table structure for table `dorm_staff_application` */

DROP TABLE IF EXISTS `dorm_staff_application`;

CREATE TABLE `dorm_staff_application` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `apply_type` tinyint DEFAULT '0' COMMENT '0入住 1退宿 2换房',
  `target_room_type` varchar(50) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT '0',
  `remark` varchar(255) DEFAULT NULL,
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-教工住宿申请';

/*Data for the table `dorm_staff_application` */

insert  into `dorm_staff_application`(`id`,`user_id`,`apply_type`,`target_room_type`,`reason`,`status`,`remark`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`) values 
(1,56,0,'单人间','新入职需要周转房',0,NULL,'0','admin','2026-01-18 18:34:56','',NULL);

/*Table structure for table `stu_profile` */

DROP TABLE IF EXISTS `stu_profile`;

CREATE TABLE `stu_profile` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `entry_year` int NOT NULL DEFAULT '2024' COMMENT '入学年份',
  `status` tinyint DEFAULT '0' COMMENT '学籍:0在读 1休学 2毕业',
  `current_bed_id` bigint DEFAULT NULL COMMENT '当前床位ID',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-学生档案扩展';

/*Data for the table `stu_profile` */

insert  into `stu_profile`(`user_id`,`entry_year`,`status`,`current_bed_id`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(10,2024,0,1,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(11,2024,0,64,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(12,2024,0,95,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(13,2024,0,126,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(14,2024,0,2,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(15,2024,0,65,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(16,2024,0,NULL,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(17,2024,0,NULL,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(20,2024,0,13,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(21,2024,0,76,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(22,2024,0,107,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(23,2024,0,138,'admin','2026-01-18 18:30:34','','2026-01-18 18:30:34','0',NULL),
(24,2024,0,NULL,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(25,2024,0,NULL,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(26,2024,0,NULL,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(27,2024,0,NULL,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(30,2024,0,NULL,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(31,2024,0,NULL,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(60,2024,0,NULL,'admin','2026-01-18 18:34:56','',NULL,'0',NULL),
(61,2024,0,NULL,'admin','2026-01-18 18:34:56','',NULL,'0',NULL),
(62,2024,0,NULL,'admin','2026-01-18 18:34:56','',NULL,'0',NULL);

/*Table structure for table `sys_admin_user` */

DROP TABLE IF EXISTS `sys_admin_user`;

CREATE TABLE `sys_admin_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(64) NOT NULL COMMENT '账号',
  `password` varchar(100) NOT NULL COMMENT '加密密码',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
  `gender` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '1' COMMENT '性别:0女 1男',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(500) DEFAULT '' COMMENT '头像',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '状态:0正常 1停用',
  `create_by` varchar(64) DEFAULT 'system',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  `residence_type` int DEFAULT '1' COMMENT '居住类型(默认1校外)',
  `emergency_contact` varchar(64) DEFAULT '',
  `emergency_phone` varchar(20) DEFAULT '',
  `emergency_relation` varchar(32) DEFAULT '',
  `current_address` varchar(255) DEFAULT '',
  `id_card` varchar(20) DEFAULT '',
  `ethnicity` varchar(20) DEFAULT '汉族',
  `hometown` varchar(64) DEFAULT '未知',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_name` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-后台管理员表';

/*Data for the table `sys_admin_user` */

insert  into `sys_admin_user`(`id`,`username`,`password`,`real_name`,`nickname`,`gender`,`phone`,`email`,`avatar`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`,`residence_type`,`emergency_contact`,`emergency_phone`,`emergency_relation`,`current_address`,`id_card`,`ethnicity`,`hometown`) values 
(1,'admin','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','超级管理员','Admin','1','',NULL,'','0','system','2026-01-18 18:30:34','','2026-01-20 11:16:25','0',NULL,1,'','','','','','汉族','未知'),
(2,'monitor','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','审计专员','Monitor','0','',NULL,'','0','system','2026-01-18 18:34:56','','2026-01-20 11:16:25','0','只读权限管理员',1,'','','','','','汉族','未知'),
(10,'admin_wang','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','王建国','海棠苑宿管','1','13800138001',NULL,'','0','system','2026-01-18 18:41:29','','2026-01-20 11:16:25','0','负责北校区海棠苑',1,'','','','','','汉族','未知'),
(11,'admin_zhao','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','赵秀英','丁香苑宿管','0','13800138002',NULL,'','0','system','2026-01-18 18:41:29','','2026-01-20 11:16:25','0','负责南校区丁香苑',1,'','','','','','汉族','未知'),
(12,'admin_chen','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','陈思思','计算机辅导员','0','13900139001',NULL,'','0','system','2026-01-18 18:41:29','','2026-01-20 11:16:25','0','计算机学院学工办',1,'','','','','','汉族','未知'),
(13,'admin_li','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','李强','机械辅导员','1','13900139002',NULL,'','0','system','2026-01-18 18:41:29','','2026-01-20 11:16:25','0','机械学院学工办',1,'','','','','','汉族','未知'),
(14,'worker_zhou','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','周师傅','高级电工','1','13700137001',NULL,'','0','system','2026-01-18 18:41:29','','2026-01-20 11:16:25','0','后勤维修组',1,'','','','','','汉族','未知');

/*Table structure for table `sys_algorithm_config` */

DROP TABLE IF EXISTS `sys_algorithm_config`;

CREATE TABLE `sys_algorithm_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(100) NOT NULL,
  `config_value` varchar(100) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='配置-算法参数';

/*Data for the table `sys_algorithm_config` */

insert  into `sys_algorithm_config`(`id`,`config_key`,`config_value`,`description`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,'weight_sleep','0.8','作息权重','0','admin','2026-01-18 18:30:34','',NULL,NULL),
(2,'weight_game','0.5','游戏权重','0','admin','2026-01-18 18:30:34','',NULL,NULL),
(3,'allocate_open','true','开放分配系统','0','admin','2026-01-18 18:30:34','',NULL,NULL);

/*Table structure for table `sys_campus` */

DROP TABLE IF EXISTS `sys_campus`;

CREATE TABLE `sys_campus` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_name` varchar(64) NOT NULL COMMENT '校区名称',
  `campus_code` varchar(32) NOT NULL COMMENT '校区编码',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态: 1-启用 0-停用',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-校区表';

/*Data for the table `sys_campus` */

insert  into `sys_campus`(`id`,`campus_name`,`campus_code`,`address`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,'北校区','CAMP-N','北京市海淀区XX路1号',1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(2,'南校区','CAMP-S','北京市昌平区XX路2号',1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL);

/*Table structure for table `sys_college` */

DROP TABLE IF EXISTS `sys_college`;

CREATE TABLE `sys_college` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint NOT NULL COMMENT '所属校区ID',
  `college_name` varchar(64) NOT NULL COMMENT '学院名称',
  `short_name` varchar(32) DEFAULT NULL COMMENT '简称',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_campus` (`campus_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-学院表';

/*Data for the table `sys_college` */

insert  into `sys_college`(`id`,`campus_id`,`college_name`,`short_name`,`sort`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,'计算机学院','CS',0,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(2,1,'外国语学院','FL',0,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(3,2,'机械工程学院','ME',0,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(4,2,'土木建筑学院','CE',0,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL);

/*Table structure for table `sys_dept` */

DROP TABLE IF EXISTS `sys_dept`;

CREATE TABLE `sys_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint NOT NULL COMMENT '所属校区ID',
  `dept_name` varchar(64) NOT NULL COMMENT '部门名称',
  `parent_id` bigint DEFAULT '0' COMMENT '父部门ID',
  `sort` int DEFAULT '0',
  `status` tinyint DEFAULT '1',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_campus` (`campus_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-部门表';

/*Data for the table `sys_dept` */

insert  into `sys_dept`(`id`,`campus_id`,`dept_name`,`parent_id`,`sort`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,'学工处',0,0,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(2,1,'后勤处',0,0,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(3,2,'保卫处',0,0,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(4,2,'财务处',0,0,1,'admin','2026-01-18 18:30:34','',NULL,'0',NULL);

/*Table structure for table `sys_login_log` */

DROP TABLE IF EXISTS `sys_login_log`;

CREATE TABLE `sys_login_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) DEFAULT '',
  `ipaddr` varchar(128) DEFAULT '',
  `status` char(1) DEFAULT '0',
  `msg` varchar(255) DEFAULT '',
  `access_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='日志-登录';

/*Data for the table `sys_login_log` */

/*Table structure for table `sys_major` */

DROP TABLE IF EXISTS `sys_major`;

CREATE TABLE `sys_major` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `college_id` bigint NOT NULL COMMENT '所属学院ID',
  `name` varchar(50) NOT NULL COMMENT '专业名称',
  `level` varchar(20) DEFAULT '本科' COMMENT '层次',
  `duration` int DEFAULT '4' COMMENT '学制',
  `sort` int DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_college` (`college_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-专业表';

/*Data for the table `sys_major` */

insert  into `sys_major`(`id`,`college_id`,`name`,`level`,`duration`,`sort`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,'软件工程','本科',4,0,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(2,1,'人工智能','本科',4,0,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(3,2,'英语','本科',4,0,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(4,2,'日语','本科',4,0,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(5,3,'机械自动化','本科',4,0,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(6,3,'车辆工程','本科',4,0,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(7,4,'土木工程','本科',4,0,'admin','2026-01-18 18:30:34','',NULL,'0',NULL),
(8,4,'工程造价','本科',4,0,'admin','2026-01-18 18:30:34','',NULL,'0',NULL);

/*Table structure for table `sys_notice` */

DROP TABLE IF EXISTS `sys_notice`;

CREATE TABLE `sys_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(128) NOT NULL,
  `content` text NOT NULL,
  `type` int DEFAULT '1' COMMENT '1通知 2公告',
  `status` char(1) DEFAULT '0',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-通知公告';

/*Data for the table `sys_notice` */

insert  into `sys_notice`(`id`,`title`,`content`,`type`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,'2024寒假封楼通知','请各位同学于...',1,'0','0','admin','2026-01-18 18:30:34','',NULL,NULL),
(2,'关于严禁使用违规电器的提醒','近期发现...',2,'0','0','admin','2026-01-18 18:30:34','',NULL,NULL);

/*Table structure for table `sys_oper_log` */

DROP TABLE IF EXISTS `sys_oper_log`;

CREATE TABLE `sys_oper_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(50) DEFAULT '',
  `business_type` int DEFAULT '0',
  `method` varchar(100) DEFAULT '',
  `request_method` varchar(10) DEFAULT '',
  `oper_name` varchar(50) DEFAULT '',
  `oper_url` varchar(255) DEFAULT '',
  `oper_ip` varchar(128) DEFAULT '',
  `oper_param` varchar(2000) DEFAULT '',
  `json_result` varchar(2000) DEFAULT '',
  `status` int DEFAULT '0',
  `error_msg` varchar(2000) DEFAULT '',
  `oper_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='日志-操作';

/*Data for the table `sys_oper_log` */

/*Table structure for table `sys_ordinary_user` */

DROP TABLE IF EXISTS `sys_ordinary_user`;

CREATE TABLE `sys_ordinary_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(64) NOT NULL COMMENT '学号/工号',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `real_name` varchar(50) NOT NULL COMMENT '姓名',
  `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
  `user_category` int NOT NULL DEFAULT '0' COMMENT '类别:0学生 1教职工',
  `gender` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '1' COMMENT '性别:0女 1男',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '手机号',
  `id_card` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '身份证号',
  `ethnicity` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '汉族' COMMENT '民族（如：汉族）',
  `hometown` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '未知' COMMENT '籍贯（如：广东广州）',
  `avatar` varchar(500) DEFAULT '' COMMENT '头像',
  `campus_id` bigint DEFAULT NULL COMMENT '校区ID',
  `college_id` bigint DEFAULT NULL COMMENT '学院ID',
  `major_id` bigint DEFAULT NULL COMMENT '专业ID',
  `class_id` bigint DEFAULT NULL COMMENT '班级ID',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `residence_type` int DEFAULT '0' COMMENT '居住:0住校 1走读',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '状态:0正常 1停用',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  `emergency_contact` varchar(64) DEFAULT '',
  `emergency_phone` varchar(20) DEFAULT '',
  `emergency_relation` varchar(32) DEFAULT '',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=63 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-普通用户表(学生/教工)';

/*Data for the table `sys_ordinary_user` */

insert  into `sys_ordinary_user`(`id`,`username`,`password`,`real_name`,`nickname`,`user_category`,`gender`,`phone`,`id_card`,`ethnicity`,`hometown`,`avatar`,`campus_id`,`college_id`,`major_id`,`class_id`,`dept_id`,`residence_type`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`,`emergency_contact`,`emergency_phone`,`emergency_relation`) values 
(2,'S001','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','宿管阿姨',NULL,1,'0','','','汉族','未知','',NULL,NULL,NULL,NULL,2,0,'0','admin','2026-01-18 18:30:34','','2026-01-20 11:16:25','0',NULL,'','',''),
(3,'S002','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','维修大叔',NULL,1,'1','','','汉族','未知','',NULL,NULL,NULL,NULL,2,0,'0','admin','2026-01-18 18:30:34','','2026-01-20 11:16:25','0',NULL,'','',''),
(4,'T001','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','王教授',NULL,1,'1','','','汉族','未知','',NULL,NULL,NULL,NULL,1,0,'0','admin','2026-01-18 18:30:34','','2026-01-20 11:16:25','0',NULL,'','',''),
(5,'T002','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','李讲师',NULL,1,'0','','','汉族','未知','',NULL,NULL,NULL,NULL,1,0,'0','admin','2026-01-18 18:30:34','','2026-01-20 11:16:25','0',NULL,'','',''),
(6,'T003','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','张辅导员',NULL,1,'1','','','汉族','未知','',NULL,NULL,NULL,NULL,1,0,'0','admin','2026-01-18 18:30:34','','2026-01-20 11:16:25','0',NULL,'','',''),
(10,'2024001','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','学生A1',NULL,0,'1','','','汉族','未知','',1,1,1,1,NULL,0,'0','admin','2026-01-18 18:30:34','','2026-01-20 11:16:25','0',NULL,'','',''),
(11,'2024002','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','学生A2',NULL,0,'1','','','汉族','未知','',1,1,1,1,NULL,0,'0','admin','2026-01-18 18:30:34','','2026-01-20 11:16:25','0',NULL,'','',''),
(12,'2024003','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','学生A3',NULL,0,'1','','','汉族','未知','',1,1,1,1,NULL,0,'0','admin','2026-01-18 18:30:34','','2026-01-20 11:16:25','0',NULL,'','',''),
(13,'2024004','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','学生A4',NULL,0,'1','','','汉族','未知','',1,1,1,1,NULL,0,'0','admin','2026-01-18 18:30:34','','2026-01-20 11:16:25','0',NULL,'','',''),
(14,'2024005','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','学生A5',NULL,0,'1','','','汉族','未知','',1,1,1,1,NULL,0,'0','admin','2026-01-18 18:30:34','','2026-01-20 11:16:25','0',NULL,'','',''),
(15,'2024006','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','学生A6',NULL,0,'1','','','汉族','未知','',1,1,1,1,NULL,0,'0','admin','2026-01-18 18:30:34','','2026-01-20 11:16:25','0',NULL,'','',''),
(16,'2024007','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','学生A7',NULL,0,'1','','','汉族','未知','',1,1,1,1,NULL,0,'0','admin','2026-01-18 18:30:34','','2026-01-20 11:16:25','0',NULL,'','',''),
(17,'2024008','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','学生A8',NULL,0,'1','','','汉族','未知','',1,1,1,1,NULL,0,'0','admin','2026-01-18 18:30:34','','2026-01-20 11:16:25','0',NULL,'','',''),
(20,'2024011','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','学生B1',NULL,0,'0','','','汉族','未知','',1,2,3,4,NULL,0,'0','admin','2026-01-18 18:30:34','','2026-01-20 11:16:25','0',NULL,'','',''),
(21,'2024012','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','学生B2',NULL,0,'0','','','汉族','未知','',1,2,3,4,NULL,0,'0','admin','2026-01-18 18:30:34','','2026-01-20 11:16:25','0',NULL,'','',''),
(22,'2024013','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','学生B3',NULL,0,'0','','','汉族','未知','',1,2,3,4,NULL,0,'0','admin','2026-01-18 18:30:34','','2026-01-20 11:16:25','0',NULL,'','',''),
(23,'2024014','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','学生B4',NULL,0,'0','','','汉族','未知','',1,2,3,4,NULL,0,'0','admin','2026-01-18 18:30:34','','2026-01-20 11:16:25','0',NULL,'','',''),
(24,'2024015','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','学生B5',NULL,0,'0','','','汉族','未知','',1,2,3,4,NULL,0,'0','admin','2026-01-18 18:30:34','','2026-01-20 11:16:25','0',NULL,'','',''),
(25,'2024016','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','学生B6',NULL,0,'0','','','汉族','未知','',1,2,3,4,NULL,0,'0','admin','2026-01-18 18:30:34','','2026-01-20 11:16:25','0',NULL,'','',''),
(26,'2024017','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','学生B7',NULL,0,'0','','','汉族','未知','',1,2,3,4,NULL,0,'0','admin','2026-01-18 18:30:34','','2026-01-20 11:16:25','0',NULL,'','',''),
(27,'2024018','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','学生B8',NULL,0,'0','','','汉族','未知','',1,2,3,4,NULL,0,'0','admin','2026-01-18 18:30:34','','2026-01-20 11:16:25','0',NULL,'','',''),
(30,'2024030','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','走读生C',NULL,0,'1','','','汉族','未知','',1,1,1,1,NULL,1,'0','admin','2026-01-18 18:30:34','','2026-01-20 11:16:25','0',NULL,'','',''),
(31,'2024031','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','休学生D',NULL,0,'1','','','汉族','未知','',1,1,1,1,NULL,0,'1','admin','2026-01-18 18:30:34','','2026-01-20 11:16:25','0',NULL,'','',''),
(56,'TEA_PROF','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','孙教授',NULL,1,'1','13600136001','','汉族','未知','',1,1,NULL,NULL,NULL,0,'0','admin','2026-01-18 18:34:56','','2026-01-20 11:16:25','0',NULL,'','',''),
(57,'TEA_LEC','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','钱讲师',NULL,1,'0','13600136002','','汉族','未知','',1,2,NULL,NULL,NULL,0,'0','admin','2026-01-18 18:34:56','','2026-01-20 11:16:25','0',NULL,'','',''),
(60,'STU_2024_A','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','学霸小明',NULL,0,'1','15000150001','','汉族','未知','',1,1,1,1,NULL,0,'0','admin','2026-01-18 18:34:56','','2026-01-20 11:16:25','0',NULL,'','',''),
(61,'STU_2024_B','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','捣蛋鬼小强',NULL,0,'1','15000150002','','汉族','未知','',1,1,1,1,NULL,0,'0','admin','2026-01-18 18:34:56','','2026-01-20 11:16:25','0',NULL,'','',''),
(62,'STU_2024_C','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','富二代小刚',NULL,0,'1','15000150003','','汉族','未知','',1,1,1,1,NULL,1,'0','admin','2026-01-18 18:34:56','','2026-01-20 11:16:25','0',NULL,'','','');

/*Table structure for table `sys_role` */

DROP TABLE IF EXISTS `sys_role`;

CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(30) NOT NULL,
  `role_key` varchar(100) NOT NULL,
  `sort` int DEFAULT '0',
  `status` char(1) DEFAULT '0',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-角色表';

/*Data for the table `sys_role` */

insert  into `sys_role`(`id`,`role_name`,`role_key`,`sort`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,'超级管理员','super_admin',1,'0','0','admin','2026-01-18 18:30:34','',NULL,NULL),
(2,'宿管经理','dorm_manager',2,'0','0','admin','2026-01-18 18:30:34','',NULL,NULL),
(3,'辅导员','counselor',3,'0','0','admin','2026-01-18 18:30:34','',NULL,NULL),
(4,'维修人员','repair_staff',4,'0','0','admin','2026-01-18 18:30:34','',NULL,NULL),
(5,'学生','student',5,'0','0','admin','2026-01-18 18:30:34','',NULL,NULL),
(6,'教职工','teacher',6,'0','0','admin','2026-01-18 18:30:34','',NULL,NULL);

/*Table structure for table `sys_user_role` */

DROP TABLE IF EXISTS `sys_user_role`;

CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-用户角色关联';

/*Data for the table `sys_user_role` */

insert  into `sys_user_role`(`user_id`,`role_id`,`create_time`) values 
(1,1,'2026-01-18 18:30:34'),
(2,1,'2026-01-18 18:34:56'),
(2,2,'2026-01-18 18:30:34'),
(3,4,'2026-01-18 18:30:34'),
(4,6,'2026-01-18 18:30:34'),
(5,6,'2026-01-18 18:30:34'),
(6,3,'2026-01-18 18:30:34'),
(10,2,'2026-01-18 18:41:29'),
(10,5,'2026-01-18 18:30:34'),
(11,2,'2026-01-18 18:41:29'),
(11,5,'2026-01-18 18:30:34'),
(12,3,'2026-01-18 18:41:29'),
(12,5,'2026-01-18 18:30:34'),
(13,3,'2026-01-18 18:41:29'),
(13,5,'2026-01-18 18:30:34'),
(14,4,'2026-01-18 18:41:29'),
(14,5,'2026-01-18 18:30:34'),
(15,5,'2026-01-18 18:30:34'),
(16,5,'2026-01-18 18:30:34'),
(17,5,'2026-01-18 18:30:34'),
(20,5,'2026-01-18 18:30:34'),
(21,5,'2026-01-18 18:30:34'),
(22,5,'2026-01-18 18:30:34'),
(23,5,'2026-01-18 18:30:34'),
(24,5,'2026-01-18 18:30:34'),
(25,5,'2026-01-18 18:30:34'),
(26,5,'2026-01-18 18:30:34'),
(27,5,'2026-01-18 18:30:34'),
(30,5,'2026-01-18 18:30:34'),
(31,5,'2026-01-18 18:30:34'),
(56,6,'2026-01-18 18:34:56'),
(57,6,'2026-01-18 18:34:56'),
(60,5,'2026-01-18 18:34:56'),
(61,5,'2026-01-18 18:34:56'),
(62,5,'2026-01-18 18:34:56');

/*Table structure for table `sys_utility_price` */

DROP TABLE IF EXISTS `sys_utility_price`;

CREATE TABLE `sys_utility_price` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` int NOT NULL COMMENT '1水 2电',
  `name` varchar(50) NOT NULL,
  `price` decimal(10,4) NOT NULL,
  `unit` varchar(10) DEFAULT NULL,
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='配置-基础水电价';

/*Data for the table `sys_utility_price` */

insert  into `sys_utility_price`(`id`,`type`,`name`,`price`,`unit`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,1,'生活用水',3.5000,'吨','0','admin','2026-01-18 18:30:34','',NULL,NULL),
(2,2,'生活用电',0.5600,'度','0','admin','2026-01-18 18:30:34','',NULL,NULL);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
