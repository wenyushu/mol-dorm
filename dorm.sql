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
  `grade` int DEFAULT NULL COMMENT '年级',
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
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-班级表';

/*Data for the table `biz_class` */

insert  into `biz_class`(`id`,`major_id`,`grade`,`class_name`,`counselor_id`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,101,2024,'软工2401',12,'admin','2026-01-20 17:01:47','',NULL,'0',NULL),
(2,101,2024,'软工2402',12,'admin','2026-01-20 17:01:47','',NULL,'0',NULL),
(4,101,2023,'软工2301',11,'admin','2026-01-20 17:20:22','',NULL,'0',NULL),
(5,101,2025,'软工2501',11,'admin','2026-01-20 17:20:22','',NULL,'0',NULL),
(6,201,2024,'英语2401',14,'admin','2026-01-20 17:20:22','',NULL,'0',NULL),
(7,301,2024,'机自2401',NULL,'admin','2026-01-20 17:20:22','',NULL,'0',NULL);

/*Table structure for table `biz_electric_violation_log` */

DROP TABLE IF EXISTS `biz_electric_violation_log`;

CREATE TABLE `biz_electric_violation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL,
  `violation_type` int NOT NULL,
  `power_val` decimal(10,2) DEFAULT NULL,
  `detected_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'system',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物联-违规日志';

/*Data for the table `biz_electric_violation_log` */

insert  into `biz_electric_violation_log`(`id`,`room_id`,`violation_type`,`power_val`,`detected_time`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,4,2,2500.00,'2026-01-20 17:20:22','0','system','2026-01-20 17:20:22','',NULL,'检测到大功率电器(电磁炉)');

/*Table structure for table `biz_repair_order` */

DROP TABLE IF EXISTS `biz_repair_order`;

CREATE TABLE `biz_repair_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(32) NOT NULL COMMENT '工单号',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID',
  `description` text NOT NULL COMMENT '描述',
  `images` varchar(1000) DEFAULT NULL,
  `status` int DEFAULT '0' COMMENT '状态: 0待处理 1维修中 2已完成',
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-报修工单';

/*Data for the table `biz_repair_order` */

insert  into `biz_repair_order`(`id`,`order_no`,`room_id`,`applicant_id`,`description`,`images`,`status`,`repairman_id`,`finish_time`,`rating`,`comment`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,'REP20240121001',4,501,'空调遥控器没反应，疑似没电',NULL,0,NULL,NULL,NULL,NULL,'0','admin','2026-01-20 17:20:22','',NULL,NULL),
(2,'REP20240121002',1,1,'厕所灯泡闪烁',NULL,1,13,NULL,NULL,NULL,'0','admin','2026-01-20 17:20:22','',NULL,NULL);

/*Table structure for table `biz_user_preference` */

DROP TABLE IF EXISTS `biz_user_preference`;

CREATE TABLE `biz_user_preference` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `team_code` varchar(32) DEFAULT NULL,
  `bed_time` tinyint DEFAULT '3',
  `wake_time` tinyint DEFAULT '3',
  `siesta_habit` tinyint DEFAULT '0',
  `out_late_freq` tinyint DEFAULT '0',
  `sleep_quality` tinyint DEFAULT '2',
  `snoring_level` tinyint DEFAULT '0',
  `grinding_teeth` tinyint DEFAULT '0',
  `sleep_talk` tinyint DEFAULT '0',
  `climb_bed_noise` tinyint DEFAULT '1',
  `shower_freq` tinyint DEFAULT '1',
  `sock_wash` tinyint DEFAULT '0',
  `trash_habit` tinyint DEFAULT '2',
  `clean_freq` tinyint DEFAULT '2',
  `toilet_clean` tinyint DEFAULT '1',
  `desk_messy` tinyint DEFAULT '2',
  `personal_hygiene` tinyint DEFAULT '3',
  `odor_tolerance` tinyint DEFAULT '2',
  `smoking` tinyint DEFAULT '0',
  `smoke_tolerance` tinyint DEFAULT '0',
  `drinking` tinyint DEFAULT '0',
  `ac_temp` tinyint DEFAULT '26',
  `ac_duration` tinyint DEFAULT '2',
  `game_type_lol` tinyint DEFAULT '0',
  `game_type_fps` tinyint DEFAULT '0',
  `game_type_3a` tinyint DEFAULT '0',
  `game_type_mmo` tinyint DEFAULT '0',
  `game_type_mobile` tinyint DEFAULT '0',
  `game_habit` tinyint DEFAULT '0',
  `game_voice` tinyint DEFAULT '1',
  `keyboard_axis` tinyint DEFAULT '1',
  `is_cosplay` tinyint DEFAULT '0',
  `is_anime` tinyint DEFAULT '0',
  `mbti_ei` char(1) DEFAULT NULL,
  `mbti_result` varchar(4) DEFAULT NULL,
  `social_battery` tinyint DEFAULT '3',
  `share_items` tinyint DEFAULT '1',
  `bring_guest` tinyint DEFAULT '0',
  `visitors` tinyint DEFAULT '0',
  `relationship_status` tinyint DEFAULT '0',
  `has_disability` tinyint DEFAULT '0',
  `has_insulin` tinyint DEFAULT '0',
  `has_infectious` tinyint DEFAULT '0',
  `religion_taboo` varchar(255) DEFAULT NULL,
  `special_disease` varchar(255) DEFAULT NULL,
  `game_rank` tinyint DEFAULT '0',
  `game_role` tinyint DEFAULT '0',
  `eat_luosifen` tinyint DEFAULT '0',
  `eat_durian` tinyint DEFAULT '0',
  `region_type` tinyint DEFAULT NULL,
  `create_by` varchar(64) DEFAULT 'system',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-用户画像';

/*Data for the table `biz_user_preference` */

insert  into `biz_user_preference`(`user_id`,`team_code`,`bed_time`,`wake_time`,`siesta_habit`,`out_late_freq`,`sleep_quality`,`snoring_level`,`grinding_teeth`,`sleep_talk`,`climb_bed_noise`,`shower_freq`,`sock_wash`,`trash_habit`,`clean_freq`,`toilet_clean`,`desk_messy`,`personal_hygiene`,`odor_tolerance`,`smoking`,`smoke_tolerance`,`drinking`,`ac_temp`,`ac_duration`,`game_type_lol`,`game_type_fps`,`game_type_3a`,`game_type_mmo`,`game_type_mobile`,`game_habit`,`game_voice`,`keyboard_axis`,`is_cosplay`,`is_anime`,`mbti_ei`,`mbti_result`,`social_battery`,`share_items`,`bring_guest`,`visitors`,`relationship_status`,`has_disability`,`has_insulin`,`has_infectious`,`religion_taboo`,`special_disease`,`game_rank`,`game_role`,`eat_luosifen`,`eat_durian`,`region_type`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(501,NULL,6,6,0,0,2,0,0,0,1,1,0,2,3,1,2,3,2,1,0,0,26,2,1,0,0,0,0,2,1,1,0,0,NULL,NULL,3,1,0,0,0,0,0,0,NULL,NULL,0,0,0,0,NULL,'system','2026-01-20 17:20:22','',NULL,'0',NULL),
(505,NULL,1,1,0,0,2,0,0,0,1,1,0,2,1,1,2,3,2,0,0,0,26,2,0,0,0,0,0,0,1,1,0,0,NULL,NULL,3,1,0,0,0,0,0,0,NULL,NULL,0,0,0,0,NULL,'system','2026-01-20 17:20:22','',NULL,'0',NULL);

/*Table structure for table `biz_utility_bill` */

DROP TABLE IF EXISTS `biz_utility_bill`;

CREATE TABLE `biz_utility_bill` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL,
  `month` varchar(7) NOT NULL,
  `total_amount` decimal(10,2) DEFAULT '0.00',
  `status` int DEFAULT '0' COMMENT '状态: 0未缴费 1已缴费',
  `version` int DEFAULT '1',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_month` (`room_id`,`month`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-水电账单';

/*Data for the table `biz_utility_bill` */

insert  into `biz_utility_bill`(`id`,`room_id`,`month`,`total_amount`,`status`,`version`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,4,'2026-01',156.50,0,1,'0','admin','2026-01-20 17:20:22','',NULL,NULL),
(2,4,'2025-12',120.00,1,1,'0','admin','2026-01-20 17:20:22','',NULL,NULL);

/*Table structure for table `dorm_bed` */

DROP TABLE IF EXISTS `dorm_bed`;

CREATE TABLE `dorm_bed` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint DEFAULT NULL,
  `building_id` bigint DEFAULT NULL,
  `floor_id` bigint DEFAULT NULL,
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `bed_label` varchar(32) DEFAULT NULL COMMENT '床号',
  `sort_order` int NOT NULL DEFAULT '1',
  `occupant_id` bigint DEFAULT NULL,
  `occupant_type` int DEFAULT '0' COMMENT '入住类型: 0学生 1教工',
  `status` int DEFAULT '0' COMMENT '状态: 0空闲 1占用',
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
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-床位表';

/*Data for the table `dorm_bed` */

insert  into `dorm_bed`(`id`,`campus_id`,`building_id`,`floor_id`,`room_id`,`bed_label`,`sort_order`,`occupant_id`,`occupant_type`,`status`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,1,1,1,'101-1',1,1,0,1,0,'admin','2026-01-20 17:01:47','',NULL,'0',NULL),
(2,1,1,1,1,'101-2',2,2,0,1,0,'admin','2026-01-20 17:01:47','',NULL,'0',NULL),
(3,1,1,1,1,'101-3',3,NULL,0,0,0,'admin','2026-01-20 17:01:47','',NULL,'0',NULL),
(4,1,1,1,1,'101-4',4,NULL,0,0,0,'admin','2026-01-20 17:01:47','',NULL,'0',NULL),
(10,NULL,NULL,NULL,4,'201-1',1,501,0,1,0,'admin','2026-01-20 17:20:22','',NULL,'0',NULL),
(11,NULL,NULL,NULL,4,'201-2',1,502,0,1,0,'admin','2026-01-20 17:20:22','',NULL,'0',NULL),
(12,NULL,NULL,NULL,4,'201-3',1,503,0,1,0,'admin','2026-01-20 17:20:22','',NULL,'0',NULL),
(13,NULL,NULL,NULL,4,'201-4',1,504,0,1,0,'admin','2026-01-20 17:20:22','',NULL,'0',NULL),
(14,NULL,NULL,NULL,5,'202-1',1,505,0,1,0,'admin','2026-01-20 17:20:22','',NULL,'0',NULL),
(15,NULL,NULL,NULL,5,'202-2',1,506,0,1,0,'admin','2026-01-20 17:20:22','',NULL,'0',NULL),
(16,NULL,NULL,NULL,5,'202-3',1,NULL,0,0,0,'admin','2026-01-20 17:20:22','',NULL,'0',NULL),
(17,NULL,NULL,NULL,5,'202-4',1,NULL,0,0,0,'admin','2026-01-20 17:20:22','',NULL,'0',NULL);

/*Table structure for table `dorm_building` */

DROP TABLE IF EXISTS `dorm_building`;

CREATE TABLE `dorm_building` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint NOT NULL COMMENT '校区ID',
  `building_name` varchar(64) NOT NULL COMMENT '楼栋名',
  `building_no` varchar(32) DEFAULT NULL COMMENT '编号',
  `floor_count` int DEFAULT NULL COMMENT '层数',
  `gender_limit` int DEFAULT '3' COMMENT '限制: 1男 2女 3混合',
  `usage_type` int DEFAULT '0' COMMENT '用途: 0学生 1教工',
  `manager_id` bigint DEFAULT NULL COMMENT '宿管ID',
  `location` varchar(255) DEFAULT NULL COMMENT '位置',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态',
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
(1,1,'海棠苑1号楼','HT-01',5,1,0,10,NULL,1,'admin','2026-01-20 17:01:47','',NULL,'0',NULL),
(2,1,'海棠苑2号楼',NULL,NULL,2,0,NULL,NULL,1,'admin','2026-01-20 17:20:22','',NULL,'0',NULL);

/*Table structure for table `dorm_change_request` */

DROP TABLE IF EXISTS `dorm_change_request`;

CREATE TABLE `dorm_change_request` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL,
  `current_room_id` bigint NOT NULL,
  `target_room_id` bigint DEFAULT NULL,
  `type` int DEFAULT '0' COMMENT '类型: 0换房 1退宿 2互换',
  `reason` varchar(500) DEFAULT NULL,
  `status` int DEFAULT '0' COMMENT '状态: 0待审 1通过 2驳回',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-调宿申请';

/*Data for the table `dorm_change_request` */

/*Table structure for table `dorm_fixed_asset` */

DROP TABLE IF EXISTS `dorm_fixed_asset`;

CREATE TABLE `dorm_fixed_asset` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `asset_name` varchar(100) NOT NULL,
  `asset_code` varchar(64) NOT NULL,
  `category` int NOT NULL COMMENT '类型: 1家具 2电器',
  `room_id` bigint NOT NULL,
  `price` decimal(10,2) DEFAULT '0.00',
  `status` int DEFAULT '1' COMMENT '状态: 1正常 2报修 3丢失',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_asset_code` (`asset_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资产表';

/*Data for the table `dorm_fixed_asset` */

/*Table structure for table `dorm_floor` */

DROP TABLE IF EXISTS `dorm_floor`;

CREATE TABLE `dorm_floor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint DEFAULT NULL,
  `building_id` bigint NOT NULL COMMENT '楼栋ID',
  `floor_num` int NOT NULL COMMENT '楼层号',
  `gender_limit` int DEFAULT '0' COMMENT '限制: 0混合 1男 2女',
  `status` int NOT NULL DEFAULT '1',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_building` (`building_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-楼层表';

/*Data for the table `dorm_floor` */

insert  into `dorm_floor`(`id`,`campus_id`,`building_id`,`floor_num`,`gender_limit`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,1,1,1,1,'admin','2026-01-20 17:01:47','',NULL,'0',NULL),
(6,1,1,2,1,1,'admin','2026-01-20 17:20:22','',NULL,'0',NULL),
(7,1,1,3,1,1,'admin','2026-01-20 17:20:22','',NULL,'0',NULL),
(8,1,2,1,2,1,'admin','2026-01-20 17:20:22','',NULL,'0',NULL),
(9,1,2,2,2,1,'admin','2026-01-20 17:20:22','',NULL,'0',NULL);

/*Table structure for table `dorm_meter_electric` */

DROP TABLE IF EXISTS `dorm_meter_electric`;

CREATE TABLE `dorm_meter_electric` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL,
  `meter_no` varchar(50) NOT NULL,
  `current_reading` decimal(10,2) DEFAULT '0.00',
  `balance` decimal(10,2) DEFAULT '0.00',
  `status` int DEFAULT '1' COMMENT '状态: 1正常 2故障',
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
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL,
  `meter_no` varchar(50) NOT NULL,
  `current_reading` decimal(10,2) DEFAULT '0.00',
  `status` int DEFAULT '1' COMMENT '状态: 1正常 2故障',
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
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint DEFAULT NULL,
  `building_id` bigint DEFAULT NULL,
  `floor_id` bigint NOT NULL COMMENT '楼层ID',
  `floor_no` int DEFAULT NULL,
  `room_no` varchar(32) NOT NULL COMMENT '房号',
  `apartment_type` varchar(32) DEFAULT '四人间' COMMENT '房型: 四人间/二人间',
  `capacity` int DEFAULT '4',
  `current_num` int DEFAULT '0',
  `gender` char(1) DEFAULT '1' COMMENT '性别限制: 0女 1男',
  `status` int DEFAULT '10' COMMENT '状态: 10正常 20满员 40维修',
  `version` int DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_hierarchy` (`campus_id`,`building_id`,`floor_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-房间表';

/*Data for the table `dorm_room` */

insert  into `dorm_room`(`id`,`campus_id`,`building_id`,`floor_id`,`floor_no`,`room_no`,`apartment_type`,`capacity`,`current_num`,`gender`,`status`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,1,1,1,'101','四人间',4,3,'1',10,0,'admin','2026-01-20 17:01:47','',NULL,'0',NULL),
(10,1,1,2,NULL,'201','四人间',4,4,'1',20,0,'admin','2026-01-20 17:20:22','',NULL,'0',NULL),
(11,1,1,2,NULL,'202','四人间',4,2,'1',10,0,'admin','2026-01-20 17:20:22','',NULL,'0',NULL),
(12,1,1,2,NULL,'203','四人间',4,0,'1',10,0,'admin','2026-01-20 17:20:22','',NULL,'0',NULL),
(13,1,1,2,NULL,'204','四人间',4,0,'1',40,0,'admin','2026-01-20 17:20:22','',NULL,'0',NULL),
(14,1,2,6,NULL,'101','四人间',4,4,'0',20,0,'admin','2026-01-20 17:20:22','',NULL,'0',NULL),
(15,1,2,6,NULL,'102','四人间',2,0,'0',10,0,'admin','2026-01-20 17:20:22','',NULL,'0',NULL);

/*Table structure for table `dorm_staff_application` */

DROP TABLE IF EXISTS `dorm_staff_application`;

CREATE TABLE `dorm_staff_application` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL,
  `apply_type` tinyint DEFAULT '0' COMMENT '类型: 0入住 1退宿 2换房',
  `target_room_type` varchar(50) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT '0' COMMENT '状态: 0待审 1通过 2驳回',
  `remark` varchar(255) DEFAULT NULL,
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-教工住宿申请';

/*Data for the table `dorm_staff_application` */

/*Table structure for table `sys_admin_user` */

DROP TABLE IF EXISTS `sys_admin_user`;

CREATE TABLE `sys_admin_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(64) NOT NULL COMMENT '登录账号 (工号/admin)',
  `password` varchar(100) NOT NULL COMMENT '加密密码 (BCrypt)',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
  `gender` char(1) NOT NULL DEFAULT '1' COMMENT '性别:0女 1男',
  `phone` varchar(255) NOT NULL DEFAULT '' COMMENT '手机号(密文) - 必填',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(500) DEFAULT '' COMMENT '头像URL',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态:0正常 1停用',
  `residence_type` int NOT NULL DEFAULT '1' COMMENT '居住类型(0住校 1校外)',
  `is_initial_pwd` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否为初始密码 (1:是 0:否)',
  `campus_id` bigint DEFAULT NULL COMMENT '所属校区ID (用于宿管/维修工划分区域)',
  `dept_id` bigint DEFAULT NULL COMMENT '所属部门ID (用于行政归属)',
  `college_id` bigint DEFAULT NULL COMMENT '所属学院ID (专用于辅导员)',
  `emergency_contact` varchar(64) DEFAULT '' COMMENT '紧急联系人',
  `emergency_phone` varchar(255) DEFAULT '' COMMENT '紧急电话(密文)',
  `emergency_relation` varchar(32) DEFAULT '' COMMENT '关系',
  `current_address` varchar(500) DEFAULT '' COMMENT '居住地址(密文)',
  `id_card` varchar(255) DEFAULT '' COMMENT '身份证(密文)',
  `ethnicity` varchar(20) DEFAULT '汉族' COMMENT '民族',
  `hometown` varchar(64) DEFAULT '未知' COMMENT '籍贯',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除标志',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_name` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-后台管理员表';

/*Data for the table `sys_admin_user` */

insert  into `sys_admin_user`(`id`,`username`,`password`,`real_name`,`nickname`,`gender`,`phone`,`email`,`avatar`,`status`,`residence_type`,`is_initial_pwd`,`campus_id`,`dept_id`,`college_id`,`emergency_contact`,`emergency_phone`,`emergency_relation`,`current_address`,`id_card`,`ethnicity`,`hometown`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,'admin','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','超级管理员','SuperAdmin','1','13800000000',NULL,'','0',1,0,NULL,NULL,NULL,'','','','','','汉族','未知','system','2026-01-20 17:09:57','','2026-01-21 06:56:56','0','拥有所有权限'),
(10,'20JZG3102000001','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','王建国','北校区宿管','1','13800000001',NULL,'','0',1,1,1,2,NULL,'','','','','','汉族','未知','system','2026-01-20 17:09:57','','2026-01-21 06:56:56','0','负责北校区海棠苑'),
(11,'21JZG3101000002','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','李思思','CS辅导员','0','13800000002',NULL,'','0',1,1,1,1,1,'','','','','','汉族','未知','system','2026-01-20 17:09:57','','2026-01-21 06:56:56','0','计算机学院辅导员'),
(12,'19JZG5202000003','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','赵秀英','南校区宿管','0','13800000003',NULL,'','0',1,1,2,2,NULL,'','','','','','汉族','未知','system','2026-01-20 17:09:57','','2026-01-21 06:56:56','0','负责南校区丁香苑'),
(13,'18JZG5102000004','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','周大力','金牌电工','1','13800000004',NULL,'','0',1,1,1,2,NULL,'','','','','','汉族','未知','system','2026-01-20 17:09:57','','2026-01-21 06:56:56','0','负责全校水电维修'),
(14,'22JZG1101000005','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','陈晨','FL辅导员','0','13800000005',NULL,'','0',1,1,1,1,2,'','','','','','汉族','未知','system','2026-01-20 17:09:57','','2026-01-21 06:56:56','0','外国语学院辅导员'),
(20,'23JZG3204000099','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','钱算盘',NULL,'0','13800900001',NULL,'','0',1,1,2,4,NULL,'','','','','','汉族','未知','system','2026-01-20 17:20:22','','2026-01-21 06:56:56','0','负责学生缴费审核'),
(21,'24JZG3106000088','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','美羊羊',NULL,'0','13800900002',NULL,'','0',1,1,1,NULL,6,'','','','','','汉族','未知','system','2026-01-20 17:20:22','','2026-01-21 06:56:56','0','负责艺术学院学生'),
(22,'suguan001','$2a$10$snUzTQn..mMYe/zN97T2Lu4I66oBmclFViZDizyh3eG8M0kHAf9jm','张阿姨','一号楼宿管','0','ced3a46566ecbf95a9e95a605ab06264','zhang@mol.com','https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png','1',1,1,NULL,NULL,NULL,'','','','','','汉族','未知','1','2026-01-22 22:04:45','1','2026-01-22 22:04:45','0',NULL);

/*Table structure for table `sys_algorithm_config` */

DROP TABLE IF EXISTS `sys_algorithm_config`;

CREATE TABLE `sys_algorithm_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='配置-算法';

/*Data for the table `sys_algorithm_config` */

insert  into `sys_algorithm_config`(`id`,`config_key`,`config_value`,`description`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,'weight_sleep','0.8','作息权重','0','admin','2026-01-20 17:01:48','',NULL,NULL);

/*Table structure for table `sys_campus` */

DROP TABLE IF EXISTS `sys_campus`;

CREATE TABLE `sys_campus` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_name` varchar(64) NOT NULL COMMENT '校区名称',
  `campus_code` varchar(32) NOT NULL COMMENT '校区编码',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态: 1启用 0停用',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-校区表';

/*Data for the table `sys_campus` */

insert  into `sys_campus`(`id`,`campus_name`,`campus_code`,`address`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,'北校区','CAMP-N','北京市海淀区XX路1号',1,'admin','2026-01-20 17:01:47','',NULL,'0',NULL),
(2,'南校区','CAMP-S','北京市昌平区XX路2号',1,'admin','2026-01-20 17:01:47','',NULL,'0',NULL);

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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-学院表';

/*Data for the table `sys_college` */

insert  into `sys_college`(`id`,`campus_id`,`college_name`,`short_name`,`sort`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,'计算机学院','CS',0,1,'admin','2026-01-20 17:01:47','',NULL,'0',NULL),
(2,1,'外国语学院','FL',0,1,'admin','2026-01-20 17:01:47','',NULL,'0',NULL),
(3,2,'机械工程学院','ME',0,1,'admin','2026-01-20 17:01:47','',NULL,'0',NULL),
(4,2,'土木建筑学院','CE',0,1,'admin','2026-01-20 17:01:47','',NULL,'0',NULL),
(5,1,'经济管理学院','SEM',5,1,'admin','2026-01-20 17:20:22','',NULL,'0',NULL),
(6,2,'艺术设计学院','ART',6,1,'admin','2026-01-20 17:20:22','',NULL,'0',NULL);

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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-部门表';

/*Data for the table `sys_dept` */

insert  into `sys_dept`(`id`,`campus_id`,`dept_name`,`parent_id`,`sort`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,'学工处',0,0,1,'admin','2026-01-20 17:01:47','',NULL,'0',NULL),
(2,1,'后勤处',0,0,1,'admin','2026-01-20 17:01:47','',NULL,'0',NULL),
(3,2,'保卫处',0,0,1,'admin','2026-01-20 17:01:47','',NULL,'0',NULL),
(4,2,'财务处',0,0,1,'admin','2026-01-20 17:01:47','',NULL,'0',NULL),
(5,2,'饮食中心',2,1,1,'admin','2026-01-20 17:13:22','',NULL,'0','负责食堂管理'),
(6,1,'物业管理科',2,2,1,'admin','2026-01-20 17:13:22','',NULL,'0','负责保洁与维修');

/*Table structure for table `sys_login_log` */

DROP TABLE IF EXISTS `sys_login_log`;

CREATE TABLE `sys_login_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
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
) ENGINE=InnoDB AUTO_INCREMENT=805 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-专业表';

/*Data for the table `sys_major` */

insert  into `sys_major`(`id`,`college_id`,`name`,`level`,`duration`,`sort`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(101,1,'软件工程','本科',4,0,'admin','2026-01-20 17:01:47','',NULL,'0',NULL),
(102,1,'人工智能','本科',4,0,'admin','2026-01-20 17:01:47','',NULL,'0',NULL),
(201,2,'英语','本科',4,0,'admin','2026-01-20 17:01:47','',NULL,'0',NULL),
(301,3,'机械自动化','本科',4,0,'admin','2026-01-20 17:01:47','',NULL,'0',NULL),
(501,3,'数控技术','专科',3,0,'admin','2026-01-20 17:13:22','',NULL,'0',NULL),
(601,4,'土木工程(专升本)','专升本',2,0,'admin','2026-01-20 17:13:22','',NULL,'0',NULL),
(701,1,'计算机技术','硕士',3,0,'admin','2026-01-20 17:13:22','',NULL,'0',NULL),
(801,1,'软件工程','博士',4,0,'admin','2026-01-20 17:13:22','',NULL,'0',NULL),
(802,5,'会计学','本科',4,0,'admin','2026-01-20 17:20:22','',NULL,'0',NULL),
(803,5,'工商管理','硕士',3,0,'admin','2026-01-20 17:20:22','',NULL,'0',NULL),
(804,6,'视觉传达','本科',4,0,'admin','2026-01-20 17:20:22','',NULL,'0',NULL);

/*Table structure for table `sys_notice` */

DROP TABLE IF EXISTS `sys_notice`;

CREATE TABLE `sys_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(128) NOT NULL,
  `content` text NOT NULL,
  `type` int DEFAULT '1' COMMENT '类型: 1通知 2公告',
  `status` char(1) DEFAULT '0' COMMENT '状态: 0正常 1关闭',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-通知公告';

/*Data for the table `sys_notice` */

/*Table structure for table `sys_oper_log` */

DROP TABLE IF EXISTS `sys_oper_log`;

CREATE TABLE `sys_oper_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
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
  `password` varchar(100) NOT NULL COMMENT '加密密码',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
  `user_category` int NOT NULL DEFAULT '0' COMMENT '类别: 0学生 1教职工',
  `gender` char(1) NOT NULL DEFAULT '1' COMMENT '性别: 0女 1男',
  `phone` varchar(255) NOT NULL COMMENT '手机号(密文)',
  `id_card` varchar(255) NOT NULL COMMENT '身份证(密文)',
  `ethnicity` varchar(20) NOT NULL DEFAULT '汉族' COMMENT '民族',
  `hometown` varchar(64) NOT NULL DEFAULT '未知' COMMENT '籍贯',
  `avatar` varchar(500) DEFAULT '' COMMENT '头像',
  `campus_id` bigint DEFAULT NULL COMMENT '校区ID',
  `college_id` bigint DEFAULT NULL COMMENT '学院ID',
  `major_id` bigint DEFAULT NULL COMMENT '专业ID',
  `class_id` bigint DEFAULT NULL COMMENT '班级ID',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `contract_year` int DEFAULT '1' COMMENT '合同年限',
  `enrollment_year` int DEFAULT NULL COMMENT '入学年份',
  `entry_year` int DEFAULT NULL COMMENT '入职年份',
  `residence_type` int NOT NULL DEFAULT '0' COMMENT '居住: 0住校 1走读',
  `is_initial_pwd` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否初始密码',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态',
  `emergency_contact` varchar(64) NOT NULL COMMENT '紧急联系人',
  `emergency_phone` varchar(255) NOT NULL COMMENT '紧急电话',
  `emergency_relation` varchar(32) NOT NULL COMMENT '关系',
  `current_address` varchar(500) DEFAULT '' COMMENT '居住地址',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  `birth_date` date DEFAULT NULL COMMENT '出生日期',
  `political_status` varchar(20) DEFAULT '群众' COMMENT '政治面貌',
  `email` varchar(100) DEFAULT NULL COMMENT '电子邮箱',
  `landline` varchar(32) DEFAULT NULL COMMENT '固定电话',
  `entry_date` date DEFAULT NULL COMMENT '入学/入职日期',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2011 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-普通用户表(学生/教工)';

/*Data for the table `sys_ordinary_user` */

insert  into `sys_ordinary_user`(`id`,`username`,`password`,`real_name`,`nickname`,`user_category`,`gender`,`phone`,`id_card`,`ethnicity`,`hometown`,`avatar`,`campus_id`,`college_id`,`major_id`,`class_id`,`dept_id`,`contract_year`,`enrollment_year`,`entry_year`,`residence_type`,`is_initial_pwd`,`status`,`emergency_contact`,`emergency_phone`,`emergency_relation`,`current_address`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`,`birth_date`,`political_status`,`email`,`landline`,`entry_date`) values 
(1,'24B1110001','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','张三',NULL,0,'1','13800010001','110101200401011234','汉族','未知','',1,1,101,1,NULL,1,2024,NULL,0,1,'0','张父','13900000001','父子','','admin','2026-01-20 17:01:47','',NULL,'0',NULL,NULL,'群众',NULL,NULL,NULL),
(2,'24B1110002','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','李四',NULL,0,'1','13800010002','110101200402021234','汉族','未知','',1,1,101,1,NULL,1,2024,NULL,0,1,'0','李母','13900000002','母子','','admin','2026-01-20 17:01:47','',NULL,'0',NULL,NULL,'群众',NULL,NULL,NULL),
(100,'20JZG3102000001','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','赵教授',NULL,1,'1','13800020001','110101198001011234','汉族','未知','',1,NULL,NULL,NULL,1,3,NULL,2020,0,1,'0','赵夫人','13900000004','夫妻','','admin','2026-01-20 17:01:47','',NULL,'0',NULL,NULL,'群众',NULL,NULL,NULL),
(501,'24B1110010','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','李逍遥',NULL,0,'1','13800100501','110101200401010501','汉族','未知','',1,1,101,1,NULL,1,NULL,NULL,0,1,'0','李大娘','13900000501','亲属','','admin','2026-01-20 17:20:22','','2026-01-21 06:56:56','0',NULL,NULL,'群众',NULL,NULL,NULL),
(502,'24B1110011','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','景天',NULL,0,'1','13800100502','110101200401010502','汉族','未知','',1,1,101,1,NULL,1,NULL,NULL,0,1,'0','唐雪见','13900000502','朋友','','admin','2026-01-20 17:20:22','','2026-01-21 06:56:56','0',NULL,NULL,'群众',NULL,NULL,NULL),
(503,'24B1110012','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','徐长卿',NULL,0,'1','13800100503','110101200401010503','汉族','未知','',1,1,101,1,NULL,1,NULL,NULL,0,1,'0','紫萱','13900000503','朋友','','admin','2026-01-20 17:20:22','','2026-01-21 06:56:56','0',NULL,NULL,'群众',NULL,NULL,NULL),
(504,'24B1110013','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','重楼',NULL,0,'1','13800100504','110101200401010504','汉族','未知','',1,1,101,1,NULL,1,NULL,NULL,0,1,'0','飞蓬','13900000504','对手','','admin','2026-01-20 17:20:22','','2026-01-21 06:56:56','0',NULL,NULL,'群众',NULL,NULL,NULL),
(505,'24B1110014','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','陈景润',NULL,0,'1','13800100505','110101200401010505','汉族','未知','',1,1,101,1,NULL,1,NULL,NULL,0,1,'0','陈父','13900000505','父子','','admin','2026-01-20 17:20:22','','2026-01-21 06:56:56','0',NULL,NULL,'群众',NULL,NULL,NULL),
(506,'24B1110015','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','华罗庚',NULL,0,'1','13800100506','110101200401010506','汉族','未知','',1,1,101,1,NULL,1,NULL,NULL,0,1,'0','华父','13900000506','父子','','admin','2026-01-20 17:20:22','','2026-01-21 06:56:56','0',NULL,NULL,'群众',NULL,NULL,NULL),
(507,'25B1110001','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','萌新A',NULL,0,'1','13800100507','110101200501010507','汉族','未知','',1,1,101,2,NULL,1,NULL,NULL,0,1,'0','家长A','13900000507','父子','','admin','2026-01-20 17:20:22','','2026-01-21 06:56:56','0',NULL,NULL,'群众',NULL,NULL,NULL),
(508,'25B1110002','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','萌新B',NULL,0,'1','13800100508','110101200501010508','汉族','未知','',1,1,101,2,NULL,1,NULL,NULL,0,1,'0','家长B','13900000508','父子','','admin','2026-01-20 17:20:22','','2026-01-21 06:56:56','0',NULL,NULL,'群众',NULL,NULL,NULL),
(601,'20JZG1105000010','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','张大厨',NULL,1,'1','13800200601','110101198001010601','汉族','未知','',1,NULL,NULL,NULL,5,1,NULL,2020,0,1,'0','张妻','13900000601','夫妻','','admin','2026-01-20 17:20:22','','2026-01-21 06:56:56','0',NULL,NULL,'群众',NULL,NULL,NULL),
(602,'22JZG1105000011','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','王晓晓',NULL,1,'0','13800200602','110101199001010602','汉族','未知','',1,NULL,NULL,NULL,5,1,NULL,2022,0,1,'0','王夫','13900000602','夫妻','','admin','2026-01-20 17:20:22','','2026-01-21 06:56:56','0',NULL,NULL,'群众',NULL,NULL,NULL),
(603,'18JZG5102000012','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','鲁班七号',NULL,1,'1','13800200603','110101197501010603','汉族','未知','',1,NULL,NULL,NULL,2,5,NULL,2018,0,1,'0','鲁子','13900000603','父子','','admin','2026-01-20 17:20:22','','2026-01-21 06:56:56','0',NULL,NULL,'群众',NULL,NULL,NULL),
(2000,'22D1110001','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','博一鸣',NULL,0,'1','13900001001','110101199501010001','汉族','未知','',1,1,801,1,NULL,1,2022,NULL,0,1,'0','博父','13900009999','父子','博士公寓A座','admin','2026-01-20 17:13:58','',NULL,'0',NULL,NULL,'群众',NULL,NULL,NULL),
(2001,'23Y1110001','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','硕小研',NULL,0,'0','13900001002','110101199802020002','汉族','未知','',1,1,701,1,NULL,1,2023,NULL,0,1,'0','硕母','13900009998','母女','研究生楼302','admin','2026-01-20 17:13:58','',NULL,'0',NULL,NULL,'群众',NULL,NULL,NULL),
(2002,'24ZB2410001','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','升本强',NULL,0,'1','13900001003','110101200203030003','汉族','未知','',2,4,601,1,NULL,1,2024,NULL,0,1,'0','升父','13900009997','父子','南区宿舍5号楼','admin','2026-01-20 17:13:58','',NULL,'0',NULL,NULL,'群众',NULL,NULL,NULL),
(2003,'24Z2310001','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','专小科',NULL,0,'1','13900001004','110101200504040004','汉族','未知','',2,3,501,1,NULL,1,2024,NULL,0,1,'0','专母','13900009996','母子','南区宿舍6号楼','admin','2026-01-20 17:13:58','',NULL,'0',NULL,NULL,'群众',NULL,NULL,NULL),
(2004,'20B1210005','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','延毕王',NULL,0,'1','13900001005','110101200105050005','汉族','未知','',1,2,201,1,NULL,1,2020,NULL,0,1,'0','王父','13900009995','父子','北区宿舍1号楼','admin','2026-01-20 17:13:58','',NULL,'0',NULL,NULL,'群众',NULL,NULL,NULL),
(2005,'10JZG9101000001','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','钱晓民',NULL,1,'1','13800002001','110101197001010001','汉族','未知','',1,NULL,NULL,NULL,1,10,NULL,2010,0,1,'0','钱夫人','13800008881','夫妻','教师公寓A座101','admin','2026-01-20 17:14:46','',NULL,'0',NULL,NULL,'群众',NULL,NULL,NULL),
(2006,'22JZG3101000002','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','孙讲师',NULL,1,'0','13800002002','110101199002020002','汉族','未知','',1,NULL,NULL,NULL,1,3,NULL,2022,0,1,'0','孙先生','13800008882','夫妻','教师公寓B座302','admin','2026-01-20 17:14:47','',NULL,'0',NULL,NULL,'群众',NULL,NULL,NULL),
(2007,'23JZG1205000003','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','刘阿姨',NULL,1,'0','13800002003','110101197503030003','汉族','未知','',2,NULL,NULL,NULL,5,1,NULL,2023,0,1,'0','刘师傅','13800008883','夫妻','南区食堂后勤房','admin','2026-01-20 17:14:47','',NULL,'0',NULL,NULL,'群众',NULL,NULL,NULL),
(2008,'19JZG5106000004','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','赵电工',NULL,1,'1','13800002004','110101198504040004','汉族','未知','',1,NULL,NULL,NULL,6,5,NULL,2019,0,1,'0','赵弟','13800008884','兄弟','北区配电室值班室','admin','2026-01-20 17:14:47','',NULL,'0',NULL,NULL,'群众',NULL,NULL,NULL),
(2009,'24JZG3203000005','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','郑保安',NULL,1,'1','13800002005','110101200005050005','汉族','未知','',2,NULL,NULL,NULL,3,3,NULL,2024,0,1,'0','郑父','13800008885','父子','校外租房','admin','2026-01-20 17:14:47','',NULL,'0',NULL,NULL,'群众',NULL,NULL,NULL),
(2010,'2401200243','$2a$10$0RRvgxvz3zzNii1fd8.p7OrIppzPK.rLh8GJXPyckJ81HA.73.smW','李长生',NULL,0,'1','841ec00dfd0cf315c121439ffbff8772','a2177ccf30f9d234f22637f785762f24b6b28b6ff42fad91434bf5782671a53d','汉族','北京市海淀区','https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',NULL,1,1,1,NULL,1,NULL,NULL,0,1,'1','李老爹','e1135e6aa0816760db0e3dc3b790750f','父子','','1','2026-01-22 22:42:27','1','2026-01-22 22:52:38','0',NULL,'1990-03-07','群众',NULL,NULL,'2026-01-22');

/*Table structure for table `sys_role` */

DROP TABLE IF EXISTS `sys_role`;

CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` varchar(30) NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) NOT NULL COMMENT '角色权限字符',
  `sort` int DEFAULT '0',
  `status` char(1) DEFAULT '0',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-角色表';

/*Data for the table `sys_role` */

insert  into `sys_role`(`id`,`role_name`,`role_key`,`sort`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,'超级管理员','super_admin',1,'0','0','admin','2026-01-20 17:01:47','',NULL,NULL),
(2,'宿管经理','dorm_manager',2,'0','0','admin','2026-01-20 17:01:47','',NULL,NULL),
(3,'行政辅导员','counselor',3,'0','0','admin','2026-01-20 17:01:47','',NULL,NULL),
(4,'维修工头','repair_master',4,'0','0','admin','2026-01-20 17:01:47','',NULL,NULL),
(5,'普通学生','student',5,'0','0','admin','2026-01-20 17:01:47','',NULL,NULL),
(6,'教职工','college_teacher',6,'0','0','admin','2026-01-20 17:01:47','',NULL,NULL),
(7,'部门管理员','dept_admin',7,'0','0','admin','2026-01-20 17:01:47','',NULL,NULL),
(8,'工勤人员','staff',8,'0','0','admin','2026-01-20 17:01:47','',NULL,NULL);

/*Table structure for table `sys_user_role` */

DROP TABLE IF EXISTS `sys_user_role`;

CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除标志(0正常 1删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-用户角色关联';

/*Data for the table `sys_user_role` */

insert  into `sys_user_role`(`user_id`,`role_id`,`create_time`,`create_by`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,'2026-01-20 17:09:57','system','',NULL,'0',NULL),
(1,5,'2026-01-20 17:09:57','system','',NULL,'0',NULL),
(2,5,'2026-01-20 17:09:57','system','',NULL,'0',NULL),
(3,5,'2026-01-20 17:09:57','system','',NULL,'0',NULL),
(10,2,'2026-01-20 17:09:57','system','',NULL,'0',NULL),
(11,3,'2026-01-20 17:09:57','system','',NULL,'0',NULL),
(12,2,'2026-01-20 17:09:57','system','',NULL,'0',NULL),
(13,4,'2026-01-20 17:09:57','system','',NULL,'0',NULL),
(14,3,'2026-01-20 17:09:57','system','',NULL,'0',NULL),
(20,7,'2026-01-20 17:20:22','system','',NULL,'0',NULL),
(21,3,'2026-01-20 17:20:22','system','',NULL,'0',NULL),
(100,6,'2026-01-20 17:09:57','system','',NULL,'0',NULL),
(501,5,'2026-01-20 17:20:22','system','',NULL,'0',NULL),
(502,5,'2026-01-20 17:20:22','system','',NULL,'0',NULL),
(503,5,'2026-01-20 17:20:22','system','',NULL,'0',NULL),
(504,5,'2026-01-20 17:20:22','system','',NULL,'0',NULL),
(505,5,'2026-01-20 17:20:22','system','',NULL,'0',NULL),
(506,5,'2026-01-20 17:20:22','system','',NULL,'0',NULL),
(507,5,'2026-01-20 17:20:22','system','',NULL,'0',NULL),
(508,5,'2026-01-20 17:20:22','system','',NULL,'0',NULL),
(601,8,'2026-01-20 17:20:22','system','',NULL,'0',NULL),
(602,8,'2026-01-20 17:20:22','system','',NULL,'0',NULL),
(603,8,'2026-01-20 17:20:22','system','',NULL,'0',NULL),
(2000,5,'2026-01-20 17:17:33','system','',NULL,'0',NULL),
(2001,5,'2026-01-20 17:17:33','system','',NULL,'0',NULL),
(2002,5,'2026-01-20 17:17:33','system','',NULL,'0',NULL),
(2003,5,'2026-01-20 17:17:33','system','',NULL,'0',NULL),
(2004,5,'2026-01-20 17:17:33','system','',NULL,'0',NULL),
(2005,6,'2026-01-20 17:17:33','system','',NULL,'0',NULL),
(2006,6,'2026-01-20 17:15:39','system','',NULL,'0',NULL),
(2007,8,'2026-01-20 17:15:39','system','',NULL,'0',NULL),
(2008,8,'2026-01-20 17:15:39','system','',NULL,'0',NULL),
(2009,7,'2026-01-20 17:15:39','system','',NULL,'0',NULL),
(2010,5,'2026-01-22 14:42:27','system','',NULL,'0',NULL);

/*Table structure for table `sys_utility_price` */

DROP TABLE IF EXISTS `sys_utility_price`;

CREATE TABLE `sys_utility_price` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type` int NOT NULL COMMENT '类型: 1水 2电',
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='配置-水电价';

/*Data for the table `sys_utility_price` */

insert  into `sys_utility_price`(`id`,`type`,`name`,`price`,`unit`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,1,'生活用水',3.5000,'吨','0','admin','2026-01-20 17:01:48','',NULL,NULL),
(2,2,'生活用电',0.5600,'度','0','admin','2026-01-20 17:01:48','',NULL,NULL);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
