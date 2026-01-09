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
  `id` bigint NOT NULL COMMENT '主键ID',
  `major_id` bigint NOT NULL,
  `grade` varchar(10) NOT NULL,
  `class_name` varchar(100) NOT NULL,
  `counselor_id` bigint DEFAULT NULL,
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='班级表';

/*Data for the table `biz_class` */

insert  into `biz_class`(`id`,`major_id`,`grade`,`class_name`,`counselor_id`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(10301,10201,'2023','软工2301班',NULL,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10302,10201,'2023','软工2302班',NULL,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10303,10201,'2024','软工2401班',NULL,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10304,10202,'2023','计科2301班',NULL,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10305,10202,'2024','计科2401班',NULL,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10306,10205,'2023','机械2301班',NULL,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10307,10207,'2023','英语2301班',NULL,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10308,10209,'2023','会计2301班',NULL,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10309,10209,'2022','会计2201班',NULL,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10310,10204,'2024','AI2401班',NULL,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL);

/*Table structure for table `biz_electric_violation_log` */

DROP TABLE IF EXISTS `biz_electric_violation_log`;

CREATE TABLE `biz_electric_violation_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `room_id` bigint NOT NULL,
  `violation_type` tinyint NOT NULL COMMENT '1-超载, 2-禁用电器',
  `description` varchar(255) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='违规告警表';

/*Data for the table `biz_electric_violation_log` */

/*Table structure for table `biz_meter_reading` */

DROP TABLE IF EXISTS `biz_meter_reading`;

CREATE TABLE `biz_meter_reading` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `room_id` bigint NOT NULL,
  `utility_type` tinyint NOT NULL COMMENT '1-电, 2-水',
  `reading_val` decimal(10,2) NOT NULL COMMENT '读数',
  `usage_val` decimal(10,2) NOT NULL COMMENT '增量',
  `record_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='抄表历史记录';

/*Data for the table `biz_meter_reading` */

/*Table structure for table `biz_repair_order` */

DROP TABLE IF EXISTS `biz_repair_order`;

CREATE TABLE `biz_repair_order` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `order_no` varchar(32) NOT NULL,
  `room_id` bigint NOT NULL,
  `asset_id` bigint DEFAULT NULL COMMENT '关联dorm_fixed_asset',
  `applicant_id` bigint NOT NULL,
  `handler_id` bigint DEFAULT NULL COMMENT '维修工ID',
  `description` text NOT NULL,
  `damage_level` tinyint DEFAULT '1' COMMENT '1-轻微, 2-严重, 3-需报废',
  `status` tinyint DEFAULT '0' COMMENT '0-待派, 1-维修中, 2-完成',
  `result_remark` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `biz_repair_order` */

insert  into `biz_repair_order`(`id`,`order_no`,`room_id`,`asset_id`,`applicant_id`,`handler_id`,`description`,`damage_level`,`status`,`result_remark`,`create_time`) values 
(81001,'R202601010001',40001,NULL,60001,NULL,'空调不制冷',1,0,NULL,'2026-01-08 10:32:49'),
(81002,'R202601010002',40005,NULL,60003,NULL,'门锁坏了',1,1,NULL,'2026-01-08 10:32:49'),
(81003,'R202601010003',40002,NULL,60008,NULL,'灯管闪烁',1,2,NULL,'2026-01-08 10:32:49'),
(81004,'R202601010004',40001,NULL,60002,NULL,'水龙头漏水',1,0,NULL,'2026-01-08 10:32:49'),
(81005,'R202601010005',40006,NULL,60004,NULL,'下水道堵塞',1,0,NULL,'2026-01-08 10:32:49'),
(81006,'R202601010006',40003,NULL,60005,NULL,'椅子腿断了',1,2,NULL,'2026-01-08 10:32:49'),
(81007,'R202601010007',40004,NULL,60006,NULL,'网口不通',1,0,NULL,'2026-01-08 10:32:49'),
(81008,'R202601010008',40001,NULL,60001,NULL,'阳台玻璃裂缝',1,0,NULL,'2026-01-08 10:32:49'),
(81009,'R202601010009',40002,NULL,60009,NULL,'天花板渗水',1,1,NULL,'2026-01-08 10:32:49'),
(81010,'R202601010010',40005,NULL,60007,NULL,'衣柜合页坏了',1,0,NULL,'2026-01-08 10:32:49');

/*Table structure for table `biz_room_change` */

DROP TABLE IF EXISTS `biz_room_change`;

CREATE TABLE `biz_room_change` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `applicant_id` bigint NOT NULL,
  `current_bed_id` bigint NOT NULL,
  `target_bed_id` bigint DEFAULT NULL,
  `reason` text NOT NULL,
  `approver_id` bigint DEFAULT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0-审核中, 1-通过, 2-驳回',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `biz_room_change` */

/*Table structure for table `biz_room_change_request` */

DROP TABLE IF EXISTS `biz_room_change_request`;

CREATE TABLE `biz_room_change_request` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '申请人sys_ordinary_user.id',
  `current_bed_id` bigint NOT NULL,
  `target_bed_id` bigint DEFAULT NULL COMMENT '期望床位',
  `reason` text NOT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0-审核中, 1-通过, 2-驳回, 3-已搬入',
  `auditor_id` bigint DEFAULT NULL COMMENT '审批人sys_admin_user.id',
  `audit_remark` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='调宿申请表';

/*Data for the table `biz_room_change_request` */

/*Table structure for table `biz_staff_profile` */

DROP TABLE IF EXISTS `biz_staff_profile`;

CREATE TABLE `biz_staff_profile` (
  `user_id` bigint NOT NULL COMMENT '用户ID(主键)',
  `dept_id` bigint NOT NULL,
  `job_title` varchar(50) NOT NULL,
  `contract_type` varchar(20) DEFAULT '长期',
  `housing_intent` tinyint DEFAULT '0',
  `current_bed_id` bigint DEFAULT NULL,
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `biz_staff_profile` */

insert  into `biz_staff_profile`(`user_id`,`dept_id`,`job_title`,`contract_type`,`housing_intent`,`current_bed_id`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(70001,10101,'教授','长期',0,NULL,'admin','2026-01-08 10:32:50',NULL,NULL,NULL),
(70002,10103,'讲师','长期',0,NULL,'admin','2026-01-08 10:32:50',NULL,NULL,NULL);

/*Table structure for table `biz_user_preference` */

DROP TABLE IF EXISTS `biz_user_preference`;

CREATE TABLE `biz_user_preference` (
  `user_id` bigint NOT NULL,
  `smoke_drink` tinyint DEFAULT '0' COMMENT '0-无, 1-烟, 2-酒, 3-双',
  `sleep_time` tinyint DEFAULT '2' COMMENT '1-早(22点), 2-正常(23-24), 3-晚(1点+)',
  `is_snoring` tinyint DEFAULT '0',
  `game_habit` tinyint DEFAULT '0' COMMENT '0-不玩, 1-手游, 2-端游',
  `hygiene_level` tinyint DEFAULT '3' COMMENT '1-5分',
  `special_needs` text,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `biz_user_preference` */

/*Table structure for table `biz_utility_bill` */

DROP TABLE IF EXISTS `biz_utility_bill`;

CREATE TABLE `biz_utility_bill` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `room_id` bigint NOT NULL,
  `month` varchar(7) NOT NULL COMMENT '2025-06',
  `water_usage` decimal(10,2) DEFAULT '0.00',
  `electric_usage` decimal(10,2) DEFAULT '0.00',
  `total_cost` decimal(10,2) NOT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0-未缴, 1-已缴',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `biz_utility_bill` */

insert  into `biz_utility_bill`(`id`,`room_id`,`month`,`water_usage`,`electric_usage`,`total_cost`,`status`) values 
(83001,40001,'2025-12',0.00,0.00,150.00,1),
(83002,40002,'2025-12',0.00,0.00,120.00,0),
(83003,40003,'2025-12',0.00,0.00,90.00,1),
(83004,40005,'2025-12',0.00,0.00,200.00,0),
(83005,40001,'2026-01',0.00,0.00,50.00,0),
(83006,40002,'2026-01',0.00,0.00,60.00,0),
(83007,40003,'2026-01',0.00,0.00,70.00,0),
(83008,40005,'2026-01',0.00,0.00,80.00,0),
(83009,40006,'2026-01',0.00,0.00,40.00,1),
(83010,40007,'2026-01',0.00,0.00,110.00,1);

/*Table structure for table `biz_vacation_stay` */

DROP TABLE IF EXISTS `biz_vacation_stay`;

CREATE TABLE `biz_vacation_stay` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `student_id` bigint NOT NULL,
  `vacation_type` tinyint NOT NULL COMMENT '1-寒假, 2-暑假',
  `year` int NOT NULL COMMENT '2025',
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `reason` varchar(255) NOT NULL,
  `status` tinyint DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `biz_vacation_stay` */

/*Table structure for table `dorm_bed` */

DROP TABLE IF EXISTS `dorm_bed`;

CREATE TABLE `dorm_bed` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `room_id` bigint NOT NULL,
  `bed_no` varchar(10) NOT NULL,
  `occupant_id` bigint DEFAULT NULL COMMENT '占用者ID，为空表示空闲',
  `status` tinyint DEFAULT '0',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `dorm_bed` */

insert  into `dorm_bed`(`id`,`room_id`,`bed_no`,`occupant_id`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(50001,40001,'1',60001,0,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(50002,40001,'2',60002,0,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(50003,40001,'3',60005,0,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(50004,40001,'4',60006,0,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(50005,40002,'1',60008,0,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(50006,40002,'2',60009,0,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(50007,40002,'3',NULL,0,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(50008,40002,'4',NULL,0,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(50009,40005,'1',60003,0,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(50010,40005,'2',60004,0,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(50011,40005,'3',60007,0,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(50012,40005,'4',NULL,0,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL);

/*Table structure for table `dorm_building` */

DROP TABLE IF EXISTS `dorm_building`;

CREATE TABLE `dorm_building` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `campus_id` bigint NOT NULL,
  `building_no` varchar(50) NOT NULL,
  `building_name` varchar(100) NOT NULL,
  `total_floors` int NOT NULL,
  `manager_id` bigint DEFAULT NULL,
  `status` char(1) DEFAULT '0',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `dorm_building` */

insert  into `dorm_building`(`id`,`campus_id`,`building_no`,`building_name`,`total_floors`,`manager_id`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(20001,10001,'N1','北一栋',6,NULL,'0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(20002,10001,'N2','北二栋',6,NULL,'0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(20003,10001,'S1','南一栋',7,NULL,'0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(20004,10001,'S2','南二栋',7,NULL,'0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(20005,10002,'B1','滨海一号楼',10,NULL,'0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(20006,10002,'B2','滨海二号楼',10,NULL,'0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(20007,10001,'T1','教师公寓A座',15,NULL,'0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(20008,10001,'T2','教师公寓B座',15,NULL,'0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(20009,10001,'L1','留学生公寓',8,NULL,'0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(20010,10001,'G1','研究生楼',8,NULL,'0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL);

/*Table structure for table `dorm_fixed_asset` */

DROP TABLE IF EXISTS `dorm_fixed_asset`;

CREATE TABLE `dorm_fixed_asset` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `asset_name` varchar(100) NOT NULL,
  `asset_code` varchar(64) DEFAULT NULL,
  `level_type` tinyint NOT NULL COMMENT '1-房间级, 2-层级, 3-楼栋级',
  `target_id` bigint NOT NULL,
  `status` tinyint DEFAULT '1' COMMENT '1-正常, 0-损坏',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='固定资产表';

/*Data for the table `dorm_fixed_asset` */

insert  into `dorm_fixed_asset`(`id`,`asset_name`,`asset_code`,`level_type`,`target_id`,`status`,`create_time`) values 
(80001,'格力空调',NULL,1,40001,1,'2026-01-08 10:32:49'),
(80002,'格力空调',NULL,1,40002,1,'2026-01-08 10:32:49'),
(80003,'格力空调',NULL,1,40005,1,'2026-01-08 10:32:49'),
(80004,'公用饮水机',NULL,2,30001,1,'2026-01-08 10:32:49'),
(80005,'公用洗衣机',NULL,2,30001,1,'2026-01-08 10:32:49'),
(80006,'吹风机',NULL,2,30004,1,'2026-01-08 10:32:49'),
(80007,'门禁闸机',NULL,3,20001,1,'2026-01-08 10:32:49'),
(80008,'人脸识别终端',NULL,3,20002,1,'2026-01-08 10:32:49'),
(80009,'电梯左',NULL,3,20001,1,'2026-01-08 10:32:49'),
(80010,'电梯右',NULL,3,20001,1,'2026-01-08 10:32:49');

/*Table structure for table `dorm_floor` */

DROP TABLE IF EXISTS `dorm_floor`;

CREATE TABLE `dorm_floor` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `building_id` bigint NOT NULL,
  `floor_num` int NOT NULL,
  `gender_limit` tinyint DEFAULT '0' COMMENT '0-无限制 1-男 2-女',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `dorm_floor` */

insert  into `dorm_floor`(`id`,`building_id`,`floor_num`,`gender_limit`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(30001,20001,1,1,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(30002,20001,2,1,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(30003,20001,3,1,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(30004,20002,1,2,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(30005,20002,2,2,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(30006,20002,3,2,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(30007,20003,1,1,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(30008,20003,2,1,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(30009,20004,1,2,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(30010,20004,2,2,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL);

/*Table structure for table `dorm_meter_electric` */

DROP TABLE IF EXISTS `dorm_meter_electric`;

CREATE TABLE `dorm_meter_electric` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `room_id` bigint NOT NULL,
  `meter_no` varchar(50) NOT NULL,
  `current_reading` decimal(10,2) DEFAULT '0.00',
  `balance` decimal(10,2) DEFAULT '0.00',
  `is_tripped` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_e` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='电表表';

/*Data for the table `dorm_meter_electric` */

insert  into `dorm_meter_electric`(`id`,`room_id`,`meter_no`,`current_reading`,`balance`,`is_tripped`) values 
(82001,40001,'E-40001',0.00,50.00,0),
(82002,40002,'E-40002',0.00,12.50,0),
(82003,40003,'E-40003',0.00,100.00,0),
(82004,40004,'E-40004',0.00,0.00,0),
(82005,40005,'E-40005',0.00,23.80,0),
(82006,40006,'E-40006',0.00,45.00,0),
(82007,40007,'E-40007',0.00,88.88,0),
(82008,40008,'E-40008',0.00,10.00,0),
(82009,40009,'E-40009',0.00,200.00,0),
(82010,40010,'E-40010',0.00,150.00,0);

/*Table structure for table `dorm_meter_water` */

DROP TABLE IF EXISTS `dorm_meter_water`;

CREATE TABLE `dorm_meter_water` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `room_id` bigint NOT NULL,
  `meter_no` varchar(50) NOT NULL,
  `current_reading` decimal(10,2) DEFAULT '0.00',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_w` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='水表表';

/*Data for the table `dorm_meter_water` */

/*Table structure for table `dorm_power_rule` */

DROP TABLE IF EXISTS `dorm_power_rule`;

CREATE TABLE `dorm_power_rule` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `room_id` bigint NOT NULL,
  `max_watt` decimal(10,2) NOT NULL DEFAULT '2000.00',
  `auto_trip` tinyint DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_rule` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用电规则表';

/*Data for the table `dorm_power_rule` */

/*Table structure for table `dorm_room` */

DROP TABLE IF EXISTS `dorm_room`;

CREATE TABLE `dorm_room` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `building_id` bigint NOT NULL,
  `floor_id` bigint NOT NULL,
  `room_no` varchar(20) NOT NULL,
  `capacity` int DEFAULT '4',
  `current_gender` tinyint DEFAULT '0',
  `status` char(1) DEFAULT '1',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `dorm_room` */

insert  into `dorm_room`(`id`,`building_id`,`floor_id`,`room_no`,`capacity`,`current_gender`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(40001,20001,30001,'101',4,1,'1','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(40002,20001,30001,'102',4,1,'1','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(40003,20001,30002,'201',4,1,'1','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(40004,20001,30002,'202',4,1,'1','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(40005,20002,30004,'101',4,2,'1','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(40006,20002,30004,'102',4,2,'1','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(40007,20002,30005,'201',4,2,'1','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(40008,20002,30005,'202',4,2,'1','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(40009,20003,30007,'101',6,1,'1','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(40010,20004,30009,'101',6,2,'1','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL);

/*Table structure for table `staff_profile` */

DROP TABLE IF EXISTS `staff_profile`;

CREATE TABLE `staff_profile` (
  `user_id` bigint NOT NULL,
  `dept_id` bigint NOT NULL,
  `job_title` varchar(50) NOT NULL,
  `contract_type` varchar(20) DEFAULT '3年' COMMENT '1年/3年/长期',
  `hire_date` date NOT NULL,
  `expect_leave_date` date DEFAULT NULL,
  `work_status` tinyint DEFAULT '0' COMMENT '0-在职, 1-请假, 2-退休',
  `housing_intent` tinyint NOT NULL DEFAULT '0' COMMENT '0-外住, 1-住校',
  `current_bed_id` bigint DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `staff_profile` */

insert  into `staff_profile`(`user_id`,`dept_id`,`job_title`,`contract_type`,`hire_date`,`expect_leave_date`,`work_status`,`housing_intent`,`current_bed_id`) values 
(20,21,'讲师','3年','2015-09-01','2024-09-01',0,0,NULL),
(22,12,'高级电工','1年','2023-01-01','2024-01-01',0,1,NULL),
(25,11,'宿管员','长期','2010-05-01',NULL,0,1,NULL);

/*Table structure for table `stu_leave_status` */

DROP TABLE IF EXISTS `stu_leave_status`;

CREATE TABLE `stu_leave_status` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `student_id` bigint NOT NULL,
  `status_type` tinyint NOT NULL COMMENT '0-在校, 1-请假离校, 2-假期离校, 3-实习离校',
  `start_time` datetime NOT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `end_time` datetime DEFAULT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0-活跃, 1-已销假归校',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生考勤请假表';

/*Data for the table `stu_leave_status` */

/*Table structure for table `stu_profile` */

DROP TABLE IF EXISTS `stu_profile`;

CREATE TABLE `stu_profile` (
  `user_id` bigint NOT NULL,
  `class_id` bigint NOT NULL,
  `tutor_id` bigint DEFAULT NULL,
  `counselor_id` bigint DEFAULT NULL,
  `entry_year` int NOT NULL,
  `duration` int NOT NULL COMMENT '学制',
  `retention_years` int DEFAULT '0',
  `expect_grad_date` date DEFAULT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0-在读, 1-休学, 2-延毕, 3-毕业',
  `status_remark` varchar(255) DEFAULT NULL,
  `current_bed_id` bigint DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `stu_profile` */

insert  into `stu_profile`(`user_id`,`class_id`,`tutor_id`,`counselor_id`,`entry_year`,`duration`,`retention_years`,`expect_grad_date`,`status`,`status_remark`,`current_bed_id`) values 
(60001,10301,NULL,NULL,2023,4,0,NULL,0,NULL,50001),
(60002,10301,NULL,NULL,2023,4,0,NULL,0,NULL,50002),
(60003,10302,NULL,NULL,2023,4,0,NULL,0,NULL,50009),
(60004,10302,NULL,NULL,2023,4,0,NULL,0,NULL,50010),
(60005,10303,NULL,NULL,2023,4,0,NULL,0,NULL,50003),
(60006,10303,NULL,NULL,2023,4,0,NULL,0,NULL,50004),
(60007,10304,NULL,NULL,2023,4,0,NULL,0,NULL,50011),
(60008,10304,NULL,NULL,2023,4,0,NULL,0,NULL,50005),
(60009,10305,NULL,NULL,2023,4,0,NULL,0,NULL,50006),
(60010,10305,NULL,NULL,2023,4,0,NULL,0,NULL,NULL);

/*Table structure for table `stu_student_profile` */

DROP TABLE IF EXISTS `stu_student_profile`;

CREATE TABLE `stu_student_profile` (
  `user_id` bigint NOT NULL,
  `class_id` bigint NOT NULL,
  `edu_type` varchar(20) DEFAULT '本科',
  `current_bed_id` bigint DEFAULT NULL,
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `stu_student_profile` */

insert  into `stu_student_profile`(`user_id`,`class_id`,`edu_type`,`current_bed_id`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(60001,10301,'本科',50001,'admin','2026-01-08 10:32:49',NULL,NULL,NULL);

/*Table structure for table `sys_admin_user` */

DROP TABLE IF EXISTS `sys_admin_user`;

CREATE TABLE `sys_admin_user` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `username` varchar(64) NOT NULL,
  `password` varchar(100) NOT NULL,
  `real_name` varchar(50) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `role_type` tinyint NOT NULL COMMENT '0-超管, 1-宿管...',
  `dept_id` bigint DEFAULT NULL,
  `status` char(1) DEFAULT '0',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_adm_user` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员表';

/*Data for the table `sys_admin_user` */

insert  into `sys_admin_user`(`id`,`username`,`password`,`real_name`,`phone`,`role_type`,`dept_id`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,'admin','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','系统超管','13800000000',0,NULL,'0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(2,'manager','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','宿管经理','13800000001',1,NULL,'0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(3,'aunt1','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','王阿姨','13800000002',1,NULL,'0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(4,'uncle1','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','李大叔','13800000003',1,NULL,'0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(5,'repair1','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','维修工张三','13800000004',1,NULL,'0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(6,'repair2','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','维修工李四','13800000005',1,NULL,'0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(7,'security1','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','保安队长','13800000006',1,NULL,'0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(8,'counselor1','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','张辅导员','13800000007',1,NULL,'0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(9,'counselor2','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','王辅导员','13800000008',1,NULL,'0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10,'logistics','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','后勤主管','13800000009',1,NULL,'0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL);

/*Table structure for table `sys_algorithm_config` */

DROP TABLE IF EXISTS `sys_algorithm_config`;

CREATE TABLE `sys_algorithm_config` (
  `id` bigint NOT NULL COMMENT '主键ID(统一为Long)',
  `config_key` varchar(100) NOT NULL,
  `config_value` varchar(100) NOT NULL,
  `remark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='算法参数表';

/*Data for the table `sys_algorithm_config` */

insert  into `sys_algorithm_config`(`id`,`config_key`,`config_value`,`remark`) values 
(1,'max_introverts_per_room','2','内向者最大比例'),
(2,'max_minority_per_room','2','少数民族最大聚集数'),
(3,'game_clustering_weight','0.5','游戏发烧友聚合权重'),
(4,'sleep_time_weight','0.8','作息时间权重'),
(5,'smoke_free_policy','strict','禁烟策略'),
(6,'bed_allocation_mode','auto','床位分配模式'),
(7,'priority_freshman','true','新生优先'),
(8,'priority_disabled','true','残障人士优先'),
(9,'system_open_date','2026-08-20','系统开放时间'),
(10,'system_close_date','2026-09-01','系统关闭时间');

/*Table structure for table `sys_campus` */

DROP TABLE IF EXISTS `sys_campus`;

CREATE TABLE `sys_campus` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `campus_name` varchar(100) NOT NULL,
  `campus_code` varchar(20) NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `status` char(1) DEFAULT '0',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_campus_code` (`campus_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='校区表';

/*Data for the table `sys_campus` */

insert  into `sys_campus`(`id`,`campus_name`,`campus_code`,`address`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(10001,'君山本部','JS-01','君山市学府路1号','0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10002,'滨海校区','BH-02','滨海大道88号','0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10003,'东城校区','DC-03','东城区科技园路5号','0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10004,'国际学院','GJ-04','自贸区环岛路10号','0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10005,'南山分校','NS-05','南山区创新大道1号','0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10006,'医学院区','YX-06','健康路120号','0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10007,'软件园实训基地','RJ-07','高新南四道','0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10008,'老校区','OLD-08','解放路1949号','1','0','admin','2026-01-08 10:32:49',NULL,NULL,'已废弃'),
(10009,'在建新校区','NEW-09','未来城核心区','1','0','admin','2026-01-08 10:32:49',NULL,NULL,'建设中'),
(10010,'虚拟校区','VR-10','线上教学','0','0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(2009251957457895426,'测试新增校区','TEST-001','测试地址888号','0','0','0:1','2026-01-08 21:12:51','0:1','2026-01-08 21:12:51',NULL);

/*Table structure for table `sys_department` */

DROP TABLE IF EXISTS `sys_department`;

CREATE TABLE `sys_department` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `parent_id` bigint DEFAULT '0',
  `campus_id` bigint NOT NULL,
  `dept_name` varchar(100) NOT NULL,
  `dept_type` tinyint DEFAULT '1',
  `leader_id` bigint DEFAULT NULL,
  `sort_order` int DEFAULT '0',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';

/*Data for the table `sys_department` */

insert  into `sys_department`(`id`,`parent_id`,`campus_id`,`dept_name`,`dept_type`,`leader_id`,`sort_order`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(10101,0,10001,'计算机学院',1,NULL,1,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10102,0,10001,'机械工程学院',1,NULL,2,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10103,0,10001,'外国语学院',1,NULL,3,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10104,0,10001,'经济管理学院',1,NULL,4,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10105,0,10001,'教务处',2,NULL,5,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10106,0,10001,'学生工作处',2,NULL,6,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10107,0,10001,'后勤保障处',2,NULL,7,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10108,0,10002,'滨海分院',1,NULL,8,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10109,0,10002,'海洋科学系',1,NULL,9,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10110,0,10001,'保卫处',2,NULL,10,'0','admin','2026-01-08 10:32:49',NULL,NULL,NULL);

/*Table structure for table `sys_major` */

DROP TABLE IF EXISTS `sys_major`;

CREATE TABLE `sys_major` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `dept_id` bigint NOT NULL,
  `major_name` varchar(100) NOT NULL,
  `duration` int DEFAULT '4',
  `degree_type` varchar(20) DEFAULT '本科',
  `status` tinyint DEFAULT '1',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='专业表';

/*Data for the table `sys_major` */

insert  into `sys_major`(`id`,`dept_id`,`major_name`,`duration`,`degree_type`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(10201,10101,'软件工程',4,'本科',1,'admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10202,10101,'计算机科学与技术',4,'本科',1,'admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10203,10101,'网络安全',4,'本科',1,'admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10204,10101,'人工智能',4,'本科',1,'admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10205,10102,'机械自动化',4,'本科',1,'admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10206,10102,'工业设计',4,'本科',1,'admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10207,10103,'英语',4,'本科',1,'admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10208,10103,'日语',4,'本科',1,'admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10209,10104,'会计学',4,'本科',1,'admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10210,10104,'国际贸易',4,'本科',1,'admin','2026-01-08 10:32:49',NULL,NULL,NULL);

/*Table structure for table `sys_ordinary_user` */

DROP TABLE IF EXISTS `sys_ordinary_user`;

CREATE TABLE `sys_ordinary_user` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `username` varchar(64) NOT NULL,
  `password` varchar(100) NOT NULL,
  `real_name` varchar(50) NOT NULL,
  `id_card` varchar(50) NOT NULL,
  `sex` tinyint NOT NULL COMMENT '1男0女',
  `user_identity` tinyint NOT NULL COMMENT '0-学生, 1-教工',
  `phone` varchar(20) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `nickname` varchar(50) DEFAULT NULL,
  `account_status` tinyint DEFAULT '1' COMMENT '1-活跃, 0-禁用',
  `status` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ord_user` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='普通用户表';

/*Data for the table `sys_ordinary_user` */

insert  into `sys_ordinary_user`(`id`,`username`,`password`,`real_name`,`id_card`,`sex`,`user_identity`,`phone`,`avatar`,`nickname`,`account_status`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(60001,'2023001','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','张伟','420101200501010001',1,0,NULL,NULL,NULL,1,'0','SYSTEM','2026-01-08 10:32:49',NULL,NULL,NULL),
(60002,'2023002','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','李强','420101200501010002',1,0,NULL,NULL,NULL,1,'0','SYSTEM','2026-01-08 10:32:49',NULL,NULL,NULL),
(60003,'2023003','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','王芳','420101200501010003',0,0,NULL,NULL,NULL,1,'0','SYSTEM','2026-01-08 10:32:49',NULL,NULL,NULL),
(60004,'2023004','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','赵敏','420101200501010004',0,0,NULL,NULL,NULL,1,'0','SYSTEM','2026-01-08 10:32:49',NULL,NULL,NULL),
(60005,'2023005','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','孙悟空','420101200501010005',1,0,NULL,NULL,NULL,1,'0','SYSTEM','2026-01-08 10:32:49',NULL,NULL,NULL),
(60006,'2023006','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','猪八戒','420101200501010006',1,0,NULL,NULL,NULL,1,'0','SYSTEM','2026-01-08 10:32:49',NULL,NULL,NULL),
(60007,'2023007','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','白骨精','420101200501010007',0,0,NULL,NULL,NULL,1,'0','SYSTEM','2026-01-08 10:32:49',NULL,NULL,NULL),
(60008,'2023008','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','沙僧','420101200501010008',1,0,NULL,NULL,NULL,1,'0','SYSTEM','2026-01-08 10:32:49',NULL,NULL,NULL),
(60009,'2023009','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','唐僧','420101200501010009',1,0,NULL,NULL,NULL,1,'0','SYSTEM','2026-01-08 10:32:49',NULL,NULL,NULL),
(60010,'2023010','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','女儿国王','420101200501010010',0,0,NULL,NULL,NULL,1,'0','SYSTEM','2026-01-08 10:32:49',NULL,NULL,NULL),
(70001,'T0001','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','风清扬','420101198001010001',1,1,NULL,NULL,NULL,1,'0','SYSTEM','2026-01-08 10:32:49',NULL,NULL,NULL),
(70002,'T0002','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','灭绝师太','420101198001010002',0,1,NULL,NULL,NULL,1,'0','SYSTEM','2026-01-08 10:32:49',NULL,NULL,NULL);

/*Table structure for table `sys_role` */

DROP TABLE IF EXISTS `sys_role`;

CREATE TABLE `sys_role` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `role_name` varchar(50) NOT NULL,
  `role_key` varchar(50) NOT NULL,
  `status` tinyint DEFAULT '1',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `sys_role` */

insert  into `sys_role`(`id`,`role_name`,`role_key`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,'超级管理员','admin',1,'admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(2,'宿管经理','dorm_manager',1,'admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(3,'辅导员','counselor',1,'admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(4,'宿管员','housemaster',1,'admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(5,'维修工','repairman',1,'admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(6,'学生','student',1,'admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(7,'教职工','teacher',1,'admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(8,'访客','visitor',1,'admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(9,'保卫处','security',1,'admin','2026-01-08 10:32:49',NULL,NULL,NULL),
(10,'后勤处','logistics',1,'admin','2026-01-08 10:32:49',NULL,NULL,NULL);

/*Table structure for table `sys_user_role` */

DROP TABLE IF EXISTS `sys_user_role`;

CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `user_type` tinyint NOT NULL COMMENT '0-管理员, 1-普通用户',
  PRIMARY KEY (`user_id`,`role_id`,`user_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `sys_user_role` */

insert  into `sys_user_role`(`user_id`,`role_id`,`user_type`) values 
(1,1,0),
(2,2,0),
(3,4,0),
(4,4,0),
(5,5,0),
(6,5,0),
(8,3,0),
(60001,6,1),
(60002,6,1),
(60003,6,1),
(60004,6,1),
(60005,6,1),
(70001,7,1);

/*Table structure for table `sys_utility_price_rule` */

DROP TABLE IF EXISTS `sys_utility_price_rule`;

CREATE TABLE `sys_utility_price_rule` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `rule_name` varchar(100) NOT NULL,
  `utility_type` tinyint NOT NULL COMMENT '1-电, 2-水',
  `tier_start` decimal(10,2) NOT NULL DEFAULT '0.00',
  `tier_end` decimal(10,2) DEFAULT '9999.99',
  `unit_price` decimal(10,3) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='水电价位表';

/*Data for the table `sys_utility_price_rule` */

insert  into `sys_utility_price_rule`(`id`,`rule_name`,`utility_type`,`tier_start`,`tier_end`,`unit_price`) values 
(1,'电费一阶',1,0.00,100.00,0.550),
(2,'电费二阶',1,100.01,9999.00,0.850),
(3,'标准水费',2,0.00,9999.00,3.200);

/*Table structure for table `sys_utility_rule` */

DROP TABLE IF EXISTS `sys_utility_rule`;

CREATE TABLE `sys_utility_rule` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `rule_name` varchar(50) NOT NULL,
  `type` tinyint NOT NULL COMMENT '1-水, 2-电',
  `tier_1_limit` decimal(10,2) NOT NULL COMMENT '一阶上限',
  `tier_1_price` decimal(10,3) NOT NULL,
  `tier_2_price` decimal(10,3) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `sys_utility_rule` */

insert  into `sys_utility_rule`(`id`,`rule_name`,`type`,`tier_1_limit`,`tier_1_price`,`tier_2_price`) values 
(1,'居民用电',2,200.00,0.588,0.888),
(2,'生活用水',1,10.00,2.800,4.500);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
