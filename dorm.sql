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
  `create_by` varchar(64) DEFAULT 'admin',
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
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
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='抄表历史记录';

/*Data for the table `biz_meter_reading` */

/*Table structure for table `biz_repair_order` */

DROP TABLE IF EXISTS `biz_repair_order`;

CREATE TABLE `biz_repair_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` varchar(32) NOT NULL COMMENT '工单号(R2024...)',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID',
  `description` text NOT NULL COMMENT '故障描述',
  `status` int DEFAULT '0' COMMENT '0-待处理, 1-维修中, 2-已完成',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='报修工单';

/*Data for the table `biz_repair_order` */

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
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `biz_user_preference` */

/*Table structure for table `biz_utility_bill` */

DROP TABLE IF EXISTS `biz_utility_bill`;

CREATE TABLE `biz_utility_bill` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `month` varchar(7) NOT NULL COMMENT '月份(2024-06)',
  `water_usage` decimal(10,2) DEFAULT '0.00' COMMENT '用水量',
  `electric_usage` decimal(10,2) DEFAULT '0.00' COMMENT '用电量',
  `total_cost` decimal(10,2) NOT NULL COMMENT '总费用',
  `status` int DEFAULT '0' COMMENT '0-未缴, 1-已缴',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='水电费账单';

/*Data for the table `biz_utility_bill` */

/*Table structure for table `dorm_bed` */

DROP TABLE IF EXISTS `dorm_bed`;

CREATE TABLE `dorm_bed` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '所属房间ID',
  `bed_label` varchar(20) NOT NULL COMMENT '床位显示标签 (如: 101-1号床)',
  `occupant_id` bigint DEFAULT NULL COMMENT '当前居住者ID (关联学生表，空则为闲置)',
  `status` tinyint DEFAULT '0' COMMENT '床位状态 (0:正常 1:报修)',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除 (0:未删 1:已删)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `fk_bed_room` (`room_id`),
  KEY `fk_bed_student` (`occupant_id`),
  CONSTRAINT `fk_bed_room` FOREIGN KEY (`room_id`) REFERENCES `dorm_room` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_bed_student` FOREIGN KEY (`occupant_id`) REFERENCES `sys_ordinary_user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=20007 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍床位表';

/*Data for the table `dorm_bed` */

insert  into `dorm_bed`(`id`,`room_id`,`bed_label`,`occupant_id`,`status`,`del_flag`,`create_time`,`create_by`,`update_by`,`update_time`,`remark`) values 
(20001,101,'101-1',10001,0,'0','2026-01-10 16:02:33','admin','',NULL,NULL),
(20002,101,'101-2',10002,0,'0','2026-01-10 16:02:33','admin','',NULL,NULL),
(20003,101,'101-3',NULL,0,'0','2026-01-10 16:02:33','admin','',NULL,NULL),
(20004,101,'101-4',NULL,0,'0','2026-01-10 16:02:33','admin','',NULL,NULL),
(20005,201,'201-1',10003,0,'0','2026-01-10 16:02:33','admin','',NULL,NULL),
(20006,201,'201-2',NULL,0,'0','2026-01-10 16:02:33','admin','',NULL,NULL);

/*Table structure for table `dorm_building` */

DROP TABLE IF EXISTS `dorm_building`;

CREATE TABLE `dorm_building` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint NOT NULL COMMENT '所属校区ID',
  `name` varchar(50) NOT NULL COMMENT '楼栋名称 (如: 南苑1号楼)',
  `type` int DEFAULT '1' COMMENT '楼宇类型 (1:男生楼 2:女生楼 3:混合楼)',
  `floors` int DEFAULT '6' COMMENT '总层数',
  `manager` varchar(50) DEFAULT NULL COMMENT '宿管负责人姓名',
  `status` int DEFAULT '1' COMMENT '状态 (1:启用 0:停用/维修)',
  `location` varchar(100) DEFAULT NULL COMMENT '具体位置描述',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除 (0:未删 1:已删)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `fk_building_campus` (`campus_id`),
  CONSTRAINT `fk_building_campus` FOREIGN KEY (`campus_id`) REFERENCES `sys_campus` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍楼栋表';

/*Data for the table `dorm_building` */

insert  into `dorm_building`(`id`,`campus_id`,`name`,`type`,`floors`,`manager`,`status`,`location`,`del_flag`,`create_time`,`create_by`,`update_by`,`update_time`,`remark`) values 
(1,1,'北苑1号楼',1,6,'宿管阿姨',1,'北区东侧','0','2026-01-10 16:02:32','admin','',NULL,NULL),
(2,1,'北苑2号楼',2,6,'宿管阿姨',1,'北区西侧','0','2026-01-10 16:02:32','admin','',NULL,NULL);

/*Table structure for table `dorm_change_request` */

DROP TABLE IF EXISTS `dorm_change_request`;

CREATE TABLE `dorm_change_request` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请单ID',
  `student_id` bigint NOT NULL COMMENT '申请学生ID',
  `current_room_id` bigint NOT NULL COMMENT '原房间ID',
  `target_room_id` bigint NOT NULL COMMENT '目标房间ID',
  `reason` varchar(500) DEFAULT NULL COMMENT '申请原因',
  `status` int DEFAULT '0' COMMENT '审批状态 (0:待辅导员审 1:待宿管审 2:已完成 3:已驳回)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `audit_msg` varchar(255) DEFAULT NULL COMMENT '审批意见记录',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='调宿申请单表';

/*Data for the table `dorm_change_request` */

insert  into `dorm_change_request`(`id`,`student_id`,`current_room_id`,`target_room_id`,`reason`,`status`,`create_time`,`audit_msg`,`create_by`,`update_by`,`update_time`,`del_flag`) values 
(1,10001,101,102,'宿舍太吵，想换个安静的',0,'2026-01-10 16:02:33',NULL,'admin','',NULL,'0');

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
  `create_by` varchar(64) DEFAULT 'admin',
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='固定资产表';

/*Data for the table `dorm_fixed_asset` */

insert  into `dorm_fixed_asset`(`id`,`asset_name`,`asset_code`,`level_type`,`target_id`,`status`,`create_time`,`create_by`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(80001,'格力空调',NULL,1,40001,1,'2026-01-08 10:32:49','admin',NULL,NULL,'0',NULL),
(80002,'格力空调',NULL,1,40002,1,'2026-01-08 10:32:49','admin',NULL,NULL,'0',NULL),
(80003,'格力空调',NULL,1,40005,1,'2026-01-08 10:32:49','admin',NULL,NULL,'0',NULL),
(80004,'公用饮水机',NULL,2,30001,1,'2026-01-08 10:32:49','admin',NULL,NULL,'0',NULL),
(80005,'公用洗衣机',NULL,2,30001,1,'2026-01-08 10:32:49','admin',NULL,NULL,'0',NULL),
(80006,'吹风机',NULL,2,30004,1,'2026-01-08 10:32:49','admin',NULL,NULL,'0',NULL),
(80007,'门禁闸机',NULL,3,20001,1,'2026-01-08 10:32:49','admin',NULL,NULL,'0',NULL),
(80008,'人脸识别终端',NULL,3,20002,1,'2026-01-08 10:32:49','admin',NULL,NULL,'0',NULL),
(80009,'电梯左',NULL,3,20001,1,'2026-01-08 10:32:49','admin',NULL,NULL,'0',NULL),
(80010,'电梯右',NULL,3,20001,1,'2026-01-08 10:32:49','admin',NULL,NULL,'0',NULL);

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
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_e` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='电表表';

/*Data for the table `dorm_meter_electric` */

insert  into `dorm_meter_electric`(`id`,`room_id`,`meter_no`,`current_reading`,`balance`,`is_tripped`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(82001,40001,'E-40001',0.00,50.00,0,'admin','2026-01-09 15:41:18',NULL,NULL,'0',NULL),
(82002,40002,'E-40002',0.00,12.50,0,'admin','2026-01-09 15:41:18',NULL,NULL,'0',NULL),
(82003,40003,'E-40003',0.00,100.00,0,'admin','2026-01-09 15:41:18',NULL,NULL,'0',NULL),
(82004,40004,'E-40004',0.00,0.00,0,'admin','2026-01-09 15:41:18',NULL,NULL,'0',NULL),
(82005,40005,'E-40005',0.00,23.80,0,'admin','2026-01-09 15:41:18',NULL,NULL,'0',NULL),
(82006,40006,'E-40006',0.00,45.00,0,'admin','2026-01-09 15:41:18',NULL,NULL,'0',NULL),
(82007,40007,'E-40007',0.00,88.88,0,'admin','2026-01-09 15:41:18',NULL,NULL,'0',NULL),
(82008,40008,'E-40008',0.00,10.00,0,'admin','2026-01-09 15:41:18',NULL,NULL,'0',NULL),
(82009,40009,'E-40009',0.00,200.00,0,'admin','2026-01-09 15:41:18',NULL,NULL,'0',NULL),
(82010,40010,'E-40010',0.00,150.00,0,'admin','2026-01-09 15:41:18',NULL,NULL,'0',NULL);

/*Table structure for table `dorm_meter_water` */

DROP TABLE IF EXISTS `dorm_meter_water`;

CREATE TABLE `dorm_meter_water` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `room_id` bigint NOT NULL,
  `meter_no` varchar(50) NOT NULL,
  `current_reading` decimal(10,2) DEFAULT '0.00',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
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
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_rule` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用电规则表';

/*Data for the table `dorm_power_rule` */

/*Table structure for table `dorm_room` */

DROP TABLE IF EXISTS `dorm_room`;

CREATE TABLE `dorm_room` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `building_id` bigint NOT NULL COMMENT '所属楼栋ID',
  `floor_no` int NOT NULL COMMENT '所在楼层 (如: 3)',
  `room_no` varchar(20) NOT NULL COMMENT '房间号/门牌号 (如: 305)',
  `capacity` int DEFAULT '4' COMMENT '核定床位数',
  `current_num` int DEFAULT '0' COMMENT '当前实住人数',
  `gender` int DEFAULT '1' COMMENT '性别限制 (0:混合 1:男 2:女)',
  `status` int DEFAULT '1' COMMENT '房间状态 (1:正常 0:封寝 2:满员)',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除 (0:未删 1:已删)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `fk_room_building` (`building_id`),
  CONSTRAINT `fk_room_building` FOREIGN KEY (`building_id`) REFERENCES `dorm_building` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_room_gender` CHECK ((`gender` in (0,1,2)))
) ENGINE=InnoDB AUTO_INCREMENT=202 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍房间表';

/*Data for the table `dorm_room` */

insert  into `dorm_room`(`id`,`building_id`,`floor_no`,`room_no`,`capacity`,`current_num`,`gender`,`status`,`del_flag`,`create_time`,`create_by`,`update_by`,`update_time`,`remark`) values 
(101,1,1,'101',4,2,1,1,'0','2026-01-10 16:02:33','admin','',NULL,NULL),
(102,1,1,'102',4,0,1,1,'0','2026-01-10 16:02:33','admin','',NULL,NULL),
(201,2,1,'201',4,1,2,1,'0','2026-01-10 16:02:33','admin','',NULL,NULL);

/*Table structure for table `stu_profile` */

DROP TABLE IF EXISTS `stu_profile`;

CREATE TABLE `stu_profile` (
  `user_id` bigint NOT NULL COMMENT '用户ID (与 sys_ordinary_user.id 一致)',
  `entry_year` int NOT NULL DEFAULT '2024' COMMENT '入学年份',
  `status` tinyint DEFAULT '0' COMMENT '学籍状态 (0:在读 1:休学 2:毕业)',
  `current_bed_id` bigint DEFAULT NULL COMMENT '当前入住床位ID (冗余查询字段)',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生学籍档案表';

/*Data for the table `stu_profile` */

insert  into `stu_profile`(`user_id`,`entry_year`,`status`,`current_bed_id`) values 
(10001,2024,0,20001),
(10002,2024,0,20002),
(10003,2024,0,20005);

/*Table structure for table `sys_admin_user` */

DROP TABLE IF EXISTS `sys_admin_user`;

CREATE TABLE `sys_admin_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(64) NOT NULL COMMENT '登录账号',
  `password` varchar(100) NOT NULL COMMENT '加密密码 (BCrypt)',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `status` char(1) DEFAULT '0' COMMENT '帐号状态 (0:正常 1:停用)',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除 (0:未删 1:已删)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `email` varchar(50) DEFAULT NULL COMMENT '电子邮箱',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像地址',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注信息',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='后台管理员表 (宿管/后勤)';

/*Data for the table `sys_admin_user` */

insert  into `sys_admin_user`(`id`,`username`,`password`,`real_name`,`phone`,`status`,`del_flag`,`create_time`,`email`,`avatar`,`remark`,`create_by`,`update_by`,`update_time`) values 
(1,'admin','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','系统超管',NULL,'0','0','2026-01-10 16:02:32',NULL,NULL,NULL,'admin','',NULL),
(2,'manager','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','宿管阿姨',NULL,'0','0','2026-01-10 16:02:32',NULL,NULL,NULL,'admin','',NULL);

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
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_name` varchar(100) NOT NULL COMMENT '校区名称 (如: 君山本部)',
  `campus_code` varchar(20) NOT NULL COMMENT '校区唯一编码',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `status` char(1) DEFAULT '0' COMMENT '状态 (0:正常 1:停用)',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除 (0:未删 1:已删)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='校区信息表';

/*Data for the table `sys_campus` */

insert  into `sys_campus`(`id`,`campus_name`,`campus_code`,`address`,`status`,`del_flag`,`create_time`,`create_by`,`update_by`,`update_time`,`remark`) values 
(1,'君山本部','JS01','学府路1号','0','0','2026-01-10 16:02:32','admin','',NULL,NULL),
(2,'滨海校区','BH02','滨海大道88号','0','0','2026-01-10 16:02:32','admin','',NULL,NULL);

/*Table structure for table `sys_class` */

DROP TABLE IF EXISTS `sys_class`;

CREATE TABLE `sys_class` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `major_id` bigint NOT NULL COMMENT '所属专业ID (关联 sys_major)',
  `name` varchar(50) NOT NULL COMMENT '班级名称 (如: 软工2401班)',
  `grade` int NOT NULL COMMENT '入学年级 (如: 2024)',
  `adviser` varchar(50) DEFAULT NULL COMMENT '班主任/辅导员姓名',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除 (0:未删 1:已删)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=304 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='行政班级表';

/*Data for the table `sys_class` */

insert  into `sys_class`(`id`,`major_id`,`name`,`grade`,`adviser`,`del_flag`,`create_time`,`create_by`,`update_by`,`update_time`,`remark`) values 
(301,201,'软工2401班',2024,'张老师','0','2026-01-10 16:02:32','admin','',NULL,NULL),
(302,202,'网安2401班',2024,'李老师','0','2026-01-10 16:02:32','admin','',NULL,NULL),
(303,203,'会计2401班',2024,'王老师','0','2026-01-10 16:02:32','admin','',NULL,NULL);

/*Table structure for table `sys_college` */

DROP TABLE IF EXISTS `sys_college`;

CREATE TABLE `sys_college` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint DEFAULT NULL COMMENT '所属校区ID (关联 sys_campus)',
  `name` varchar(50) NOT NULL COMMENT '学院名称 (如: 计算机学院)',
  `code` varchar(20) NOT NULL COMMENT '学院代码 (如: CS01)',
  `sort` int DEFAULT '0' COMMENT '显示排序 (数值越小越靠前)',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除 (0:未删 1:已删)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=104 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='二级学院表';

/*Data for the table `sys_college` */

insert  into `sys_college`(`id`,`campus_id`,`name`,`code`,`sort`,`del_flag`,`create_time`,`create_by`,`update_by`,`update_time`,`remark`) values 
(101,1,'计算机科学与技术学院','CS',1,'0','2026-01-10 16:02:32','admin','',NULL,NULL),
(102,1,'网络空间安全学院','SEC',2,'0','2026-01-10 16:02:32','admin','',NULL,NULL),
(103,2,'经济管理学院','ECO',3,'0','2026-01-10 16:02:32','admin','',NULL,NULL);

/*Table structure for table `sys_major` */

DROP TABLE IF EXISTS `sys_major`;

CREATE TABLE `sys_major` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `college_id` bigint NOT NULL COMMENT '所属学院ID (关联 sys_college)',
  `name` varchar(50) NOT NULL COMMENT '专业名称',
  `level` varchar(20) DEFAULT '本科' COMMENT '培养层次 (本科/专科/硕士/博士)',
  `duration` int DEFAULT '4' COMMENT '学制年份 (如: 4年)',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除 (0:未删 1:已删)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=204 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='专业信息表';

/*Data for the table `sys_major` */

insert  into `sys_major`(`id`,`college_id`,`name`,`level`,`duration`,`del_flag`,`create_time`,`create_by`,`update_by`,`update_time`,`remark`) values 
(201,101,'软件工程','本科',4,'0','2026-01-10 16:02:32','admin','',NULL,NULL),
(202,102,'网络安全','本科',4,'0','2026-01-10 16:02:32','admin','',NULL,NULL),
(203,103,'会计学','本科',4,'0','2026-01-10 16:02:32','admin','',NULL,NULL);

/*Table structure for table `sys_ordinary_user` */

DROP TABLE IF EXISTS `sys_ordinary_user`;

CREATE TABLE `sys_ordinary_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(64) NOT NULL COMMENT '学号/工号 (登录账号)',
  `password` varchar(100) NOT NULL COMMENT '加密密码 (BCrypt)',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '本人手机号',
  `user_category` int DEFAULT '0' COMMENT '人员类别 (0:学生 1:教职工)',
  `sex` tinyint DEFAULT '1' COMMENT '性别 (1:男 2:女)',
  `college_id` bigint DEFAULT NULL COMMENT '所属学院ID',
  `major_id` bigint DEFAULT NULL COMMENT '所属专业ID (仅学生)',
  `class_id` bigint DEFAULT NULL COMMENT '所属班级ID (仅学生)',
  `id_card` varchar(20) DEFAULT NULL COMMENT '身份证号 (用于实名)',
  `status` char(1) DEFAULT '0' COMMENT '帐号状态 (0:正常 1:停用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
  `ethnicity` varchar(20) DEFAULT NULL COMMENT '民族 (如: 汉族)',
  `hometown` varchar(50) DEFAULT NULL COMMENT '籍贯 (如: 江苏南京)',
  `birth_date` date DEFAULT NULL COMMENT '出生日期',
  `political_status` varchar(20) DEFAULT '群众' COMMENT '政治面貌 (党员/团员/群众)',
  `email` varchar(50) DEFAULT NULL COMMENT '电子邮箱',
  `landline` varchar(20) DEFAULT NULL COMMENT '座机/家庭电话',
  `emergency_contact` varchar(50) DEFAULT NULL COMMENT '紧急联系人姓名',
  `emergency_phone` varchar(20) DEFAULT NULL COMMENT '紧急联系人电话',
  `emergency_relation` varchar(20) DEFAULT NULL COMMENT '与本人关系 (父子/母子等)',
  `residence_type` int DEFAULT '0' COMMENT '居住类型: 0-住校 1-走读/校外租房',
  `current_address` varchar(255) DEFAULT NULL COMMENT '当前居住地址 (校外居住时必填)',
  `entry_date` date DEFAULT NULL COMMENT '入学/入职时间',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志 (之前可能漏了)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stuno` (`username`),
  CONSTRAINT `chk_user_sex` CHECK ((`sex` in (1,2)))
) ENGINE=InnoDB AUTO_INCREMENT=10004 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='普通用户表 (学生/教职工)';

/*Data for the table `sys_ordinary_user` */

insert  into `sys_ordinary_user`(`id`,`username`,`password`,`real_name`,`phone`,`user_category`,`sex`,`college_id`,`major_id`,`class_id`,`id_card`,`status`,`create_time`,`ethnicity`,`hometown`,`birth_date`,`political_status`,`email`,`landline`,`emergency_contact`,`emergency_phone`,`emergency_relation`,`residence_type`,`current_address`,`entry_date`,`create_by`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(10001,'2024001','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','张三',NULL,0,1,101,201,301,NULL,'0','2026-01-10 16:02:32',NULL,NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,'admin','',NULL,'0',NULL),
(10002,'2024002','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','李四',NULL,0,1,101,201,301,NULL,'0','2026-01-10 16:02:32',NULL,NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,'admin','',NULL,'0',NULL),
(10003,'2024003','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','王小美',NULL,0,2,102,202,302,NULL,'0','2026-01-10 16:02:32',NULL,NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,'admin','',NULL,'0',NULL);

/*Table structure for table `sys_role` */

DROP TABLE IF EXISTS `sys_role`;

CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) NOT NULL COMMENT '角色名称 (如: 超级管理员)',
  `role_key` varchar(100) NOT NULL COMMENT '角色权限字符 (如: super_admin)',
  `status` char(1) DEFAULT '0' COMMENT '状态 (0:正常 1:停用)',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统角色表';

/*Data for the table `sys_role` */

insert  into `sys_role`(`id`,`role_name`,`role_key`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,'超级管理员','super_admin','0','admin','2026-01-10 16:47:57','',NULL,'0',NULL),
(2,'宿管经理','dorm_manager','0','admin','2026-01-10 16:47:57','',NULL,'0',NULL),
(3,'辅导员','counselor','0','admin','2026-01-10 16:47:57','',NULL,'0',NULL),
(4,'学生','student','0','admin','2026-01-10 16:47:57','',NULL,'0',NULL);

/*Table structure for table `sys_user_role` */

DROP TABLE IF EXISTS `sys_user_role`;

CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户与角色关联表 (多对多)';

/*Data for the table `sys_user_role` */

insert  into `sys_user_role`(`user_id`,`role_id`) values 
(1,1),
(2,2),
(10001,4),
(10002,4),
(10003,4);

/*Table structure for table `sys_utility_price_rule` */

DROP TABLE IF EXISTS `sys_utility_price_rule`;

CREATE TABLE `sys_utility_price_rule` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `rule_name` varchar(100) NOT NULL,
  `utility_type` tinyint NOT NULL COMMENT '1-电, 2-水',
  `tier_start` decimal(10,2) NOT NULL DEFAULT '0.00',
  `tier_end` decimal(10,2) DEFAULT '9999.99',
  `unit_price` decimal(10,3) NOT NULL,
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='水电价位表';

/*Data for the table `sys_utility_price_rule` */

insert  into `sys_utility_price_rule`(`id`,`rule_name`,`utility_type`,`tier_start`,`tier_end`,`unit_price`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,'电费一阶',1,0.00,100.00,0.550,'admin','2026-01-09 15:41:19',NULL,NULL,'0',NULL),
(2,'电费二阶',1,100.01,9999.00,0.850,'admin','2026-01-09 15:41:19',NULL,NULL,'0',NULL),
(3,'标准水费',2,0.00,9999.00,3.200,'admin','2026-01-09 15:41:19',NULL,NULL,'0',NULL);

/*Table structure for table `sys_utility_rule` */

DROP TABLE IF EXISTS `sys_utility_rule`;

CREATE TABLE `sys_utility_rule` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `rule_name` varchar(50) NOT NULL,
  `type` tinyint NOT NULL COMMENT '1-水, 2-电',
  `tier_1_limit` decimal(10,2) NOT NULL COMMENT '一阶上限',
  `tier_1_price` decimal(10,3) NOT NULL,
  `tier_2_price` decimal(10,3) NOT NULL,
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `sys_utility_rule` */

insert  into `sys_utility_rule`(`id`,`rule_name`,`type`,`tier_1_limit`,`tier_1_price`,`tier_2_price`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,'居民用电',2,200.00,0.588,0.888,'admin','2026-01-09 15:41:19',NULL,NULL,'0',NULL),
(2,'生活用水',1,10.00,2.800,4.500,'admin','2026-01-09 15:41:19',NULL,NULL,'0',NULL);

/* Trigger structure for table `dorm_bed` */

DELIMITER $$

/*!50003 DROP TRIGGER*//*!50032 IF EXISTS */ /*!50003 `trg_check_gender_before_update` */$$

/*!50003 CREATE */ /*!50017 DEFINER = 'root'@'localhost' */ /*!50003 TRIGGER `trg_check_gender_before_update` BEFORE UPDATE ON `dorm_bed` FOR EACH ROW BEGIN
    -- 定义变量来存储学生性别和房间性别
    DECLARE student_sex INT;
    DECLARE room_gender INT;
    
    -- 只有当这次操作是“分配人”(即 occupant_id 不为空) 时才校验
    -- 如果是“清空床位”(即 occupant_id 设为 NULL)，则不校验，直接通过
    IF NEW.occupant_id IS NOT NULL THEN
        
        -- 1. 查即将入住的学生的性别
        SELECT sex INTO student_sex 
        FROM sys_ordinary_user 
        WHERE id = NEW.occupant_id;
        
        -- 2. 查这张床所在房间的限制性别
        SELECT gender INTO room_gender 
        FROM dorm_room 
        WHERE id = NEW.room_id;
        
        -- 3. 开始校验逻辑
        -- 房间性别 0=混合(不限), 1=男, 2=女
        -- 如果房间有限制 (即 != 0)，且 学生性别 != 房间性别，则报错
        IF room_gender != 0 AND student_sex != room_gender THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = '【数据库拦截】严禁异性混住：学生的性别与宿舍房间规定的性别不符！';
        END IF;
    END IF;
END */$$


DELIMITER ;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
