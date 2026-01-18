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
  `counselor_id` bigint DEFAULT NULL COMMENT '辅导员ID(关联sys_ordinary_user)',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `fk_class_major` (`major_id`),
  CONSTRAINT `fk_class_major` FOREIGN KEY (`major_id`) REFERENCES `sys_major` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-班级表';

/*Data for the table `biz_class` */

insert  into `biz_class`(`id`,`major_id`,`grade`,`class_name`,`counselor_id`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,1,2024,'软工2401',NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(2,1,2024,'软工2402',NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(3,2,2023,'AI研2301',NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(4,3,2025,'英语2501',NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(5,3,2025,'英语2502',NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(6,4,2024,'机械2401',NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(7,4,2024,'机械2402',NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(8,5,2024,'土木2401',NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(9,1,2022,'软工2201(毕业班)',NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(10,1,2026,'软工2601(新生)',NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL);

/*Table structure for table `biz_electric_violation_log` */

DROP TABLE IF EXISTS `biz_electric_violation_log`;

CREATE TABLE `biz_electric_violation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `violation_type` int NOT NULL COMMENT '类型: 1-功率超载 2-恶性负载',
  `power_val` decimal(10,2) DEFAULT NULL COMMENT '当时功率(W)',
  `detected_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'system',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_room` (`room_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物联-违规用电日志';

/*Data for the table `biz_electric_violation_log` */

insert  into `biz_electric_violation_log`(`id`,`room_id`,`violation_type`,`power_val`,`detected_time`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,19,2,2500.00,'2026-01-15 18:43:32','0','system','2026-01-15 18:43:32','',NULL,'使用热得快'),
(2,1,1,1200.00,'2026-01-15 18:43:32','0','system','2026-01-15 18:43:32','',NULL,'吹风机功率过大');

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
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
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
  `order_no` varchar(32) NOT NULL COMMENT '工单号(唯一)',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID',
  `description` text NOT NULL COMMENT '故障描述',
  `images` varchar(1000) DEFAULT NULL COMMENT '图片URL(逗号分隔)',
  `status` int DEFAULT '0' COMMENT '状态: 0-待处理 1-维修中 2-已完成',
  `repairman_id` bigint DEFAULT NULL COMMENT '维修工ID',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `rating` tinyint DEFAULT NULL COMMENT '评分(1-5)',
  `comment` varchar(255) DEFAULT NULL COMMENT '评价内容',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-报修工单表';

/*Data for the table `biz_repair_order` */

insert  into `biz_repair_order`(`id`,`order_no`,`room_id`,`applicant_id`,`description`,`images`,`status`,`repairman_id`,`finish_time`,`rating`,`comment`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,'REP20240101001',1,1,'空调漏水',NULL,0,NULL,NULL,NULL,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(2,'REP20240101002',19,5,'门锁坏了',NULL,1,NULL,NULL,NULL,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(3,'REP20240101003',37,8,'马桶堵了',NULL,2,NULL,NULL,NULL,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL);

/*Table structure for table `biz_staff_profile` */

DROP TABLE IF EXISTS `biz_staff_profile`;

CREATE TABLE `biz_staff_profile` (
  `user_id` bigint NOT NULL COMMENT '用户ID (关联 sys_ordinary_user.id)',
  `dept_id` bigint NOT NULL COMMENT '所属部门ID(冗余)',
  `job_title` varchar(50) NOT NULL COMMENT '职称/职务',
  `contract_type` varchar(20) DEFAULT '长期' COMMENT '合同类型',
  `housing_intent` tinyint DEFAULT '0' COMMENT '是否有住宿意向: 0-无 1-有',
  `current_bed_id` bigint DEFAULT NULL COMMENT '当前床位ID (冗余)',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='教职工档案扩展表';

/*Data for the table `biz_staff_profile` */

insert  into `biz_staff_profile`(`user_id`,`dept_id`,`job_title`,`contract_type`,`housing_intent`,`current_bed_id`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,1,'讲师','长期',1,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(2,1,'讲师','长期',1,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(3,1,'讲师','长期',1,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(4,1,'讲师','长期',1,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(5,1,'讲师','长期',1,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(6,1,'讲师','长期',1,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(7,1,'讲师','长期',1,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(8,1,'讲师','长期',1,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(9,1,'讲师','长期',1,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(10,1,'讲师','长期',1,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL);

/*Table structure for table `biz_user_preference` */

DROP TABLE IF EXISTS `biz_user_preference`;

CREATE TABLE `biz_user_preference` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `team_code` varchar(32) DEFAULT NULL COMMENT '组队码(三人行)',
  `bed_time` tinyint DEFAULT '3' COMMENT '就寝:1-21点..6-2点+',
  `wake_time` tinyint DEFAULT '3' COMMENT '起床:1-6点..6-11点+',
  `smoke_tolerance` tinyint DEFAULT '0' COMMENT '烟味容忍:0-不可 1-可',
  `smoking` tinyint DEFAULT '0' COMMENT '抽烟:0-不 1-阳台 2-室内',
  `game_habit` tinyint DEFAULT '0' COMMENT '游戏:0-不玩 1-轻度 2-重度',
  `region_type` tinyint DEFAULT NULL COMMENT '籍贯类型:0-南 1-北',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'system',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-用户画像表';

/*Data for the table `biz_user_preference` */

insert  into `biz_user_preference`(`user_id`,`team_code`,`bed_time`,`wake_time`,`smoke_tolerance`,`smoking`,`game_habit`,`region_type`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,NULL,5,3,0,0,2,0,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(2,NULL,6,3,0,1,1,0,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(3,NULL,3,3,0,1,2,0,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(4,NULL,2,3,0,1,1,1,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(5,NULL,5,3,0,0,0,0,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(6,NULL,5,3,0,1,1,0,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(7,NULL,4,3,0,0,2,1,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(8,NULL,4,3,0,0,1,1,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(9,NULL,1,3,0,0,1,1,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(10,NULL,4,3,0,1,1,0,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(11,NULL,3,3,0,0,2,1,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(12,NULL,5,3,0,0,1,0,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(13,NULL,5,3,0,0,0,0,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(14,NULL,2,3,0,0,2,1,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(15,NULL,2,3,0,0,1,1,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(16,NULL,4,3,0,1,2,1,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(17,NULL,2,3,0,1,0,0,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(18,NULL,5,3,0,0,0,1,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(19,NULL,5,3,0,0,0,0,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(20,NULL,2,3,0,0,2,1,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(21,NULL,4,3,0,1,1,0,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(22,NULL,5,3,0,0,0,0,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(23,NULL,6,3,0,1,2,1,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(24,NULL,2,3,0,0,1,1,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(25,NULL,4,3,0,0,0,1,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(26,NULL,3,3,0,0,1,1,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(27,NULL,2,3,0,1,0,0,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(28,NULL,4,3,0,0,1,1,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(29,NULL,3,3,0,0,2,1,'0','system','2026-01-15 18:43:32','',NULL,NULL),
(30,NULL,2,3,0,1,2,1,'0','system','2026-01-15 18:43:32','',NULL,NULL);

/*Table structure for table `biz_utility_bill` */

DROP TABLE IF EXISTS `biz_utility_bill`;

CREATE TABLE `biz_utility_bill` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `month` varchar(7) NOT NULL COMMENT '月份',
  `total_amount` decimal(10,2) DEFAULT '0.00' COMMENT '总金额',
  `status` int DEFAULT '0' COMMENT '0-未付 1-已付',
  `version` int DEFAULT '1' COMMENT '乐观锁',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_month` (`room_id`,`month`)
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-水电账单表';

/*Data for the table `biz_utility_bill` */

insert  into `biz_utility_bill`(`id`,`room_id`,`month`,`total_amount`,`status`,`version`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,1,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(2,2,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(3,3,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(4,4,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(5,5,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(6,6,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(7,7,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(8,8,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(9,9,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(10,10,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(11,11,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(12,12,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(13,13,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(14,14,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(15,15,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(16,16,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(17,17,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(18,18,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(19,19,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(20,20,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(21,21,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(22,22,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(23,23,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(24,24,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(25,25,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(26,26,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(27,27,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(28,28,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(29,29,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(30,30,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(31,31,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(32,32,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(33,33,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(34,34,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(35,35,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(36,36,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(37,37,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(38,38,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(39,39,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(40,40,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(41,41,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(42,42,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(43,43,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(44,44,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(45,45,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(46,46,'2024-01',50.00,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL);

/*Table structure for table `dorm_bed` */

DROP TABLE IF EXISTS `dorm_bed`;

CREATE TABLE `dorm_bed` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '所属房间ID',
  `bed_label` varchar(20) NOT NULL COMMENT '床位号(如:1号床)',
  `occupant_id` bigint DEFAULT NULL COMMENT '居住者ID (关联sys_ordinary_user)',
  `status` tinyint DEFAULT '0' COMMENT '状态: 0-空闲 1-占用 2-报修',
  `version` int DEFAULT '1' COMMENT '乐观锁 (防并发分配冲突)',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_room` (`room_id`),
  KEY `idx_occupant` (`occupant_id`),
  CONSTRAINT `fk_bed_room` FOREIGN KEY (`room_id`) REFERENCES `dorm_room` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_bed_user` FOREIGN KEY (`occupant_id`) REFERENCES `sys_ordinary_user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=155 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-床位表';

/*Data for the table `dorm_bed` */

insert  into `dorm_bed`(`id`,`room_id`,`bed_label`,`occupant_id`,`status`,`version`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,1,'1号床',15,1,1,'0','admin','2026-01-15 18:43:32','','2026-01-15 18:43:32',NULL),
(2,2,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(3,3,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(4,4,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(5,5,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(6,6,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(7,7,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(8,8,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(9,9,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(10,10,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(11,11,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(12,12,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(13,13,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(14,14,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(15,15,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(16,16,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(17,17,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(18,18,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(19,19,'1号床',11,1,1,'0','admin','2026-01-15 18:43:32','','2026-01-15 18:43:32',NULL),
(20,20,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(21,21,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(22,22,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(23,23,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(24,24,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(25,25,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(26,26,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(27,27,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(28,28,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(29,29,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(30,30,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(31,31,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(32,32,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(33,33,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(34,34,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(35,35,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(36,36,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(37,37,'1号床',8,1,1,'0','admin','2026-01-15 18:43:32','','2026-01-15 18:43:32',NULL),
(38,38,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(39,39,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(40,40,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(41,41,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(42,42,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(43,43,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(44,44,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(45,45,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(46,46,'1号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(47,1,'2号床',16,1,1,'0','admin','2026-01-15 18:43:32','','2026-01-15 18:43:32',NULL),
(48,2,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(49,3,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(50,4,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(51,5,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(52,6,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(53,7,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(54,8,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(55,9,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(56,10,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(57,11,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(58,12,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(59,13,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(60,14,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(61,15,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(62,16,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(63,17,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(64,18,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(65,19,'2号床',12,1,1,'0','admin','2026-01-15 18:43:32','','2026-01-15 18:43:32',NULL),
(66,20,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(67,21,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(68,22,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(69,23,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(70,24,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(71,25,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(72,26,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(73,27,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(74,28,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(75,29,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(76,30,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(77,31,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(78,32,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(79,33,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(80,34,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(81,35,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(82,36,'2号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(83,1,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(84,2,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(85,3,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(86,4,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(87,5,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(88,6,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(89,7,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(90,8,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(91,9,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(92,10,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(93,11,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(94,12,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(95,13,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(96,14,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(97,15,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(98,16,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(99,17,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(100,18,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(101,19,'3号床',13,1,1,'0','admin','2026-01-15 18:43:32','','2026-01-15 18:43:32',NULL),
(102,20,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(103,21,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(104,22,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(105,23,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(106,24,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(107,25,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(108,26,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(109,27,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(110,28,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(111,29,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(112,30,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(113,31,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(114,32,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(115,33,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(116,34,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(117,35,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(118,36,'3号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(119,1,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(120,2,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(121,3,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(122,4,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(123,5,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(124,6,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(125,7,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(126,8,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(127,9,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(128,10,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(129,11,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(130,12,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(131,13,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(132,14,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(133,15,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(134,16,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(135,17,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(136,18,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(137,19,'4号床',14,1,1,'0','admin','2026-01-15 18:43:32','','2026-01-15 18:43:32',NULL),
(138,20,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(139,21,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(140,22,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(141,23,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(142,24,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(143,25,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(144,26,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(145,27,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(146,28,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(147,29,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(148,30,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(149,31,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(150,32,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(151,33,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(152,34,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(153,35,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(154,36,'4号床',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL);

/*Table structure for table `dorm_building` */

DROP TABLE IF EXISTS `dorm_building`;

CREATE TABLE `dorm_building` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint NOT NULL COMMENT '所属校区ID',
  `name` varchar(50) NOT NULL COMMENT '楼栋名称',
  `type` int DEFAULT '1' COMMENT '性别限制: 1-男楼 2-女楼 3-混合楼',
  `usage_type` tinyint DEFAULT '0' COMMENT '用途: 0-学生宿舍 1-教职工公寓 (防刁民核心)',
  `floors` int DEFAULT '6' COMMENT '总层数',
  `has_elevator` tinyint DEFAULT '0' COMMENT '有无电梯',
  `power_limit` int DEFAULT '1000' COMMENT '默认限电功率(W)',
  `status` int DEFAULT '1' COMMENT '状态: 1-启用 0-维修',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `fk_building_campus` (`campus_id`),
  CONSTRAINT `fk_building_campus` FOREIGN KEY (`campus_id`) REFERENCES `sys_campus` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-楼栋表';

/*Data for the table `dorm_building` */

insert  into `dorm_building`(`id`,`campus_id`,`name`,`type`,`usage_type`,`floors`,`has_elevator`,`power_limit`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,1,'海棠苑1号楼(女)',2,0,6,0,1000,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(2,1,'丁香苑2号楼(男)',1,0,6,0,1000,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(3,2,'教工公寓A座',3,1,4,0,1000,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL);

/*Table structure for table `dorm_change_request` */

DROP TABLE IF EXISTS `dorm_change_request`;

CREATE TABLE `dorm_change_request` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '申请学生ID',
  `current_room_id` bigint NOT NULL COMMENT '原房间ID',
  `target_room_id` bigint DEFAULT NULL COMMENT '目标房间ID (可选)',
  `type` int DEFAULT '0' COMMENT '类型: 0-换房 1-退宿 2-互换',
  `reason` varchar(500) DEFAULT NULL COMMENT '原因',
  `status` int DEFAULT '0' COMMENT '状态: 0-待审批 1-通过 2-驳回',
  `swap_student_id` bigint DEFAULT NULL COMMENT '互换目标学生ID (防刁民: 只有互换类型才有值)',
  `audit_msg` varchar(255) DEFAULT NULL COMMENT '审批意见',
  `apply_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_student` (`student_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-调宿申请表';

/*Data for the table `dorm_change_request` */

insert  into `dorm_change_request`(`id`,`student_id`,`current_room_id`,`target_room_id`,`type`,`reason`,`status`,`swap_student_id`,`audit_msg`,`apply_time`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,15,1,NULL,0,'室友太吵',0,NULL,NULL,'2026-01-15 18:43:32','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(2,25,19,NULL,1,'退学',1,NULL,NULL,'2026-01-15 18:43:32','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(3,22,19,NULL,2,'想和好基友住',0,NULL,NULL,'2026-01-15 18:43:32','0','admin','2026-01-15 18:43:32','',NULL,NULL);

/*Table structure for table `dorm_fixed_asset` */

DROP TABLE IF EXISTS `dorm_fixed_asset`;

CREATE TABLE `dorm_fixed_asset` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `asset_name` varchar(100) NOT NULL COMMENT '资产名称',
  `asset_code` varchar(64) NOT NULL COMMENT '资产编号 (唯一标识)',
  `category` int NOT NULL COMMENT '分类: 1-家具 2-电器 3-基建',
  `room_id` bigint NOT NULL COMMENT '所属房间ID',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '价值',
  `status` int DEFAULT '1' COMMENT '状态: 1-正常 2-报修 3-丢失 4-报废',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_asset_code` (`asset_code`),
  KEY `idx_room` (`room_id`),
  CONSTRAINT `fk_asset_room` FOREIGN KEY (`room_id`) REFERENCES `dorm_room` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资产管理表';

/*Data for the table `dorm_fixed_asset` */

insert  into `dorm_fixed_asset`(`id`,`asset_name`,`asset_code`,`category`,`room_id`,`price`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,'空调','AC-101-01',2,1,0.00,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(2,'空调','AC-101-02',2,2,0.00,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(3,'空调','AC-101-03',2,3,0.00,2,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(4,'书桌','DK-101-01',1,1,0.00,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(5,'书桌','DK-101-02',1,1,0.00,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(6,'书桌','DK-101-03',1,1,0.00,3,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(7,'热水器','WH-202-01',2,20,0.00,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(8,'热水器','WH-202-02',2,21,0.00,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(9,'热水器','WH-202-03',2,22,0.00,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(10,'门锁','LK-303-01',3,30,0.00,2,'0','admin','2026-01-15 18:43:32','',NULL,NULL);

/*Table structure for table `dorm_floor` */

DROP TABLE IF EXISTS `dorm_floor`;

CREATE TABLE `dorm_floor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `building_id` bigint NOT NULL COMMENT '所属楼栋ID',
  `floor_num` int NOT NULL COMMENT '楼层号',
  `gender_limit` tinyint DEFAULT '0' COMMENT '性别限制: 0-无 1-男 2-女 (混合楼层必填)',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `fk_floor_building` (`building_id`),
  CONSTRAINT `fk_floor_building` FOREIGN KEY (`building_id`) REFERENCES `dorm_building` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-楼层表';

/*Data for the table `dorm_floor` */

insert  into `dorm_floor`(`id`,`building_id`,`floor_num`,`gender_limit`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,1,1,2,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(2,1,2,2,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(3,1,3,2,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(4,2,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(5,2,2,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(6,2,3,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(7,3,1,0,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(8,3,2,0,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(9,3,3,0,'0','admin','2026-01-15 18:43:32','',NULL,NULL);

/*Table structure for table `dorm_meter_electric` */

DROP TABLE IF EXISTS `dorm_meter_electric`;

CREATE TABLE `dorm_meter_electric` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `meter_no` varchar(50) NOT NULL COMMENT '设备号',
  `current_reading` decimal(10,2) DEFAULT '0.00' COMMENT '当前读数',
  `balance` decimal(10,2) DEFAULT '0.00' COMMENT '余额',
  `status` int DEFAULT '1' COMMENT '状态: 1-在线 0-离线',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_e` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物联-电表设备表';

/*Data for the table `dorm_meter_electric` */

/*Table structure for table `dorm_meter_water` */

DROP TABLE IF EXISTS `dorm_meter_water`;

CREATE TABLE `dorm_meter_water` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `meter_no` varchar(50) NOT NULL COMMENT '设备号',
  `current_reading` decimal(10,2) DEFAULT '0.00' COMMENT '当前读数',
  `status` int DEFAULT '1' COMMENT '状态: 1-在线 0-离线',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_w` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物联-水表设备表';

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
  `room_no` varchar(20) NOT NULL COMMENT '房间号(如101)',
  `capacity` int DEFAULT '4' COMMENT '额定床位数',
  `current_num` int DEFAULT '0' COMMENT '实住人数 (程序自动维护)',
  `gender` int DEFAULT '1' COMMENT '性别: 1-男 2-女 (防刁民校验)',
  `usage_type` tinyint DEFAULT '0' COMMENT '用途: 0-学生 1-教工 (冗余校验)',
  `apartment_type` varchar(50) DEFAULT NULL COMMENT '户型: 单间/一室一厅',
  `status` int DEFAULT '1' COMMENT '状态: 1-正常 0-维修 2-满员',
  `version` int DEFAULT '1' COMMENT '乐观锁 (并发控制)',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_building` (`building_id`),
  KEY `fk_room_floor` (`floor_id`),
  CONSTRAINT `fk_room_floor` FOREIGN KEY (`floor_id`) REFERENCES `dorm_floor` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-房间表';

/*Data for the table `dorm_room` */

insert  into `dorm_room`(`id`,`building_id`,`floor_id`,`floor_no`,`room_no`,`capacity`,`current_num`,`gender`,`usage_type`,`apartment_type`,`status`,`version`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,1,1,1,'101',4,2,2,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','','2026-01-15 18:43:32',NULL),
(2,1,1,1,'102',4,0,2,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(3,1,1,1,'103',4,0,2,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(4,1,1,1,'104',4,0,2,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(5,1,1,1,'105',4,0,2,0,NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(6,1,1,1,'106',4,0,2,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(7,1,2,2,'201',4,0,2,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(8,1,2,2,'202',4,0,2,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(9,1,2,2,'203',4,0,2,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(10,1,2,2,'204',4,0,2,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(11,1,2,2,'205',4,0,2,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(12,1,2,2,'206',4,0,2,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(13,1,3,3,'301',4,0,2,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(14,1,3,3,'302',4,0,2,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(15,1,3,3,'303',4,0,2,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(16,1,3,3,'304',4,0,2,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(17,1,3,3,'305',4,0,2,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(18,1,3,3,'306',4,0,2,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(19,2,4,1,'101',4,4,1,0,NULL,2,1,'0','admin','2026-01-15 18:43:32','','2026-01-15 18:43:32',NULL),
(20,2,4,1,'102',4,0,1,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(21,2,4,1,'103',4,0,1,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(22,2,4,1,'104',4,0,1,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(23,2,4,1,'105',4,0,1,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(24,2,4,1,'106',4,0,1,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(25,2,5,2,'201',4,0,1,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(26,2,5,2,'202',4,0,1,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(27,2,5,2,'203',4,0,1,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(28,2,5,2,'204',4,0,1,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(29,2,5,2,'205',4,0,1,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(30,2,5,2,'206',4,0,1,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(31,2,6,3,'301',4,0,1,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(32,2,6,3,'302',4,0,1,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(33,2,6,3,'303',4,0,1,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(34,2,6,3,'304',4,0,1,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(35,2,6,3,'305',4,0,1,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(36,2,6,3,'306',4,0,1,0,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(37,3,7,1,'101',1,1,1,1,NULL,2,1,'0','admin','2026-01-15 18:43:32','','2026-01-15 18:43:32',NULL),
(38,3,7,1,'102',1,0,2,1,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(39,3,7,1,'103',1,0,0,1,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(40,3,7,1,'104',1,0,0,1,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(41,3,7,1,'105',1,0,0,1,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(42,3,7,1,'106',1,0,0,1,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(43,3,8,2,'201',1,0,0,1,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(44,3,8,2,'202',1,0,0,1,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(45,3,8,2,'203',1,0,0,1,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(46,3,8,2,'204',1,0,0,1,NULL,1,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL);

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
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-教职工住宿申请单';

/*Data for the table `dorm_staff_application` */

/*Table structure for table `stu_profile` */

DROP TABLE IF EXISTS `stu_profile`;

CREATE TABLE `stu_profile` (
  `user_id` bigint NOT NULL COMMENT '用户ID (关联 sys_ordinary_user.id)',
  `entry_year` int NOT NULL DEFAULT '2024' COMMENT '入学年份',
  `status` tinyint DEFAULT '0' COMMENT '学籍状态: 0-在读 1-休学 2-毕业 3-退学',
  `current_bed_id` bigint DEFAULT NULL COMMENT '当前入住床位ID (冗余查询)',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生学籍档案扩展表';

/*Data for the table `stu_profile` */

insert  into `stu_profile`(`user_id`,`entry_year`,`status`,`current_bed_id`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(11,2024,0,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(12,2024,0,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(13,2024,0,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(14,2024,0,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(15,2024,0,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(16,2024,0,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(17,2024,0,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(18,2024,0,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(19,2024,0,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(20,2024,0,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(21,2024,0,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(22,2024,0,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(23,2024,0,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(24,2024,0,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(25,2024,0,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(26,2024,0,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(27,2024,0,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(28,2024,0,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(29,2024,0,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(30,2024,0,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(31,2024,0,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(32,2024,0,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(33,2024,0,NULL,'0','admin','2026-01-15 18:43:32','',NULL,NULL);

/*Table structure for table `sys_admin_user` */

DROP TABLE IF EXISTS `sys_admin_user`;

CREATE TABLE `sys_admin_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(64) NOT NULL COMMENT '登录账号',
  `password` varchar(100) NOT NULL COMMENT '加密密码',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `nickname` varchar(64) DEFAULT NULL COMMENT '用户昵称',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '电子邮箱',
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-后台管理员表';

/*Data for the table `sys_admin_user` */

insert  into `sys_admin_user`(`id`,`username`,`password`,`real_name`,`nickname`,`phone`,`email`,`avatar`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,'admin','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','超级管理员','超级管理员',NULL,NULL,NULL,'0','0','admin','2026-01-15 18:43:32','','2026-01-16 13:48:35',NULL),
(2,'dorm_admin_1','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','海棠苑宿管阿姨',NULL,NULL,NULL,NULL,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(3,'dorm_admin_2','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','丁香苑宿管大叔',NULL,NULL,NULL,NULL,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(4,'college_sec_1','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','计算机学院王书记',NULL,NULL,NULL,NULL,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(5,'repair_boss','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','后勤李工头',NULL,NULL,NULL,NULL,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL);

/*Table structure for table `sys_algorithm_config` */

DROP TABLE IF EXISTS `sys_algorithm_config`;

CREATE TABLE `sys_algorithm_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(100) NOT NULL COMMENT '参数键',
  `config_value` varchar(100) NOT NULL COMMENT '参数值',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-算法参数配置';

/*Data for the table `sys_algorithm_config` */

insert  into `sys_algorithm_config`(`id`,`config_key`,`config_value`,`description`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,'weight_sleep','0.8','作息权重','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(2,'weight_game','0.5','游戏权重','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(3,'allocate_open','true','开放分配','0','admin','2026-01-15 18:43:32','',NULL,NULL);

/*Table structure for table `sys_campus` */

DROP TABLE IF EXISTS `sys_campus`;

CREATE TABLE `sys_campus` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_name` varchar(64) NOT NULL COMMENT '校区名称',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `status` tinyint DEFAULT '1' COMMENT '状态: 1-启用 0-停用',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除: 0-未删 1-已删',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-校区表';

/*Data for the table `sys_campus` */

insert  into `sys_campus`(`id`,`campus_name`,`address`,`contact_phone`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,'東京都港区主校区','智慧大街100号',NULL,1,'0','admin','2026-01-15 18:43:32','','2026-01-15 18:48:08',NULL),
(2,'军马场东校区','建设大路5号',NULL,1,'0','admin','2026-01-15 18:43:32','','2026-01-15 18:45:07',NULL),
(3,'中德国际校区','开发大路88号',NULL,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL);

/*Table structure for table `sys_college` */

DROP TABLE IF EXISTS `sys_college`;

CREATE TABLE `sys_college` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint NOT NULL COMMENT '所属校区ID',
  `college_name` varchar(64) NOT NULL COMMENT '学院名称',
  `short_name` varchar(32) DEFAULT NULL COMMENT '学院简称',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态: 1-启用 0-停用',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_campus` (`campus_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-学院表';

/*Data for the table `sys_college` */

insert  into `sys_college`(`id`,`campus_id`,`college_name`,`short_name`,`sort`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,1,'计算机科学与工程学院',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(2,1,'外国语学院',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(3,2,'机械工程学院',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(4,2,'土木建筑学院',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(5,3,'中德应用技术学院',NULL,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL);

/*Table structure for table `sys_dept` */

DROP TABLE IF EXISTS `sys_dept`;

CREATE TABLE `sys_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint NOT NULL COMMENT '所属校区ID',
  `dept_name` varchar(64) NOT NULL COMMENT '部门名称',
  `parent_id` bigint DEFAULT '0' COMMENT '父部门ID (0为顶级)',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态: 1-启用 0-停用',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_campus` (`campus_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-部门表';

/*Data for the table `sys_dept` */

insert  into `sys_dept`(`id`,`campus_id`,`dept_name`,`parent_id`,`sort`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,1,'学生工作处',0,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(2,1,'后勤保障处',0,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(3,1,'保卫处',0,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(4,2,'财务处',0,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(5,3,'国际交流中心',0,0,1,'0','admin','2026-01-15 18:43:32','',NULL,NULL);

/*Table structure for table `sys_login_log` */

DROP TABLE IF EXISTS `sys_login_log`;

CREATE TABLE `sys_login_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  `username` varchar(50) DEFAULT '' COMMENT '登录账号',
  `ipaddr` varchar(128) DEFAULT '' COMMENT '登录IP地址',
  `status` char(1) DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) DEFAULT '' COMMENT '提示信息',
  `access_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
  PRIMARY KEY (`id`),
  KEY `idx_access_time` (`access_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-登录日志表';

/*Data for the table `sys_login_log` */

/*Table structure for table `sys_major` */

DROP TABLE IF EXISTS `sys_major`;

CREATE TABLE `sys_major` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `college_id` bigint NOT NULL COMMENT '所属学院ID',
  `name` varchar(50) NOT NULL COMMENT '专业名称',
  `level` varchar(20) DEFAULT '本科' COMMENT '培养层次(本科/研究生)',
  `duration` int DEFAULT '4' COMMENT '学制(年)',
  `sort` int DEFAULT '0' COMMENT '排序',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `fk_major_college` (`college_id`),
  CONSTRAINT `fk_major_college` FOREIGN KEY (`college_id`) REFERENCES `sys_college` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-专业表';

/*Data for the table `sys_major` */

insert  into `sys_major`(`id`,`college_id`,`name`,`level`,`duration`,`sort`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,1,'软件工程','本科',4,0,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(2,1,'人工智能','研究生',4,0,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(3,2,'英语','本科',4,0,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(4,3,'机械设计制造','本科',4,0,'0','admin','2026-01-15 18:43:32','',NULL,NULL),
(5,4,'土木工程','本科',4,0,'0','admin','2026-01-15 18:43:32','',NULL,NULL);

/*Table structure for table `sys_notice` */

DROP TABLE IF EXISTS `sys_notice`;

CREATE TABLE `sys_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(128) NOT NULL COMMENT '标题',
  `content` text NOT NULL COMMENT '内容',
  `type` int DEFAULT '1' COMMENT '类型: 1-通知 2-公告',
  `status` char(1) DEFAULT '0' COMMENT '0-正常 1-关闭',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-通知公告';

/*Data for the table `sys_notice` */

insert  into `sys_notice`(`id`,`title`,`content`,`type`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,'关于寒假封楼的通知','全员离校...',1,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(2,'失物招领','捡到饭卡一张...',2,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(3,'违规电器通报批评','以下寝室...',1,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL);

/*Table structure for table `sys_oper_log` */

DROP TABLE IF EXISTS `sys_oper_log`;

CREATE TABLE `sys_oper_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) DEFAULT '' COMMENT '模块标题',
  `business_type` int DEFAULT '0' COMMENT '业务类型（0其它 1新增 2修改 3删除 4分配）',
  `method` varchar(100) DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) DEFAULT '' COMMENT '请求方式',
  `oper_name` varchar(50) DEFAULT '' COMMENT '操作人员',
  `oper_url` varchar(255) DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) DEFAULT '' COMMENT '主机地址',
  `oper_param` varchar(2000) DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) DEFAULT '' COMMENT '返回参数',
  `status` int DEFAULT '0' COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_oper_time` (`oper_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-操作日志表';

/*Data for the table `sys_oper_log` */

/*Table structure for table `sys_ordinary_user` */

DROP TABLE IF EXISTS `sys_ordinary_user`;

CREATE TABLE `sys_ordinary_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(64) NOT NULL COMMENT '学号/工号 (唯一登录凭证)',
  `password` varchar(100) NOT NULL COMMENT '加密密码',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `nickname` varchar(64) DEFAULT NULL COMMENT '用户昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像路径',
  `user_category` int DEFAULT '0' COMMENT '类别: 0-学生 1-教职工',
  `sex` tinyint DEFAULT '1' COMMENT '性别: 1-男 2-女 (防刁民校验基准)',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `id_card` varchar(20) DEFAULT NULL COMMENT '身份证号(敏感)',
  `ethnicity` varchar(20) DEFAULT '汉族' COMMENT '民族',
  `campus_id` bigint DEFAULT NULL COMMENT '归属校区ID',
  `college_id` bigint DEFAULT NULL COMMENT '学院ID (学生必填)',
  `major_id` bigint DEFAULT NULL COMMENT '专业ID (学生必填)',
  `class_id` bigint DEFAULT NULL COMMENT '班级ID (学生必填)',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID (教工必填)',
  `residence_type` int DEFAULT '0' COMMENT '居住类型: 0-住校 1-校外(走读)',
  `status` char(1) DEFAULT '0' COMMENT '状态: 0-正常 1-停用(封禁)',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_class` (`class_id`),
  KEY `idx_dept` (`dept_id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-普通用户表(学生/教工)';

/*Data for the table `sys_ordinary_user` */

insert  into `sys_ordinary_user`(`id`,`username`,`password`,`real_name`,`nickname`,`avatar`,`user_category`,`sex`,`phone`,`id_card`,`ethnicity`,`campus_id`,`college_id`,`major_id`,`class_id`,`dept_id`,`residence_type`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,'2015JZG001','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','张教授',NULL,NULL,1,1,NULL,NULL,'汉族',NULL,NULL,NULL,NULL,1,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(2,'2018JZG002','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','李辅导员',NULL,NULL,1,2,NULL,NULL,'汉族',NULL,NULL,NULL,NULL,1,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(3,'2019JZG003','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','赵保卫',NULL,NULL,1,1,NULL,NULL,'汉族',NULL,NULL,NULL,NULL,3,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(4,'2020JZG004','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','孙会计',NULL,NULL,1,2,NULL,NULL,'汉族',NULL,NULL,NULL,NULL,4,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(5,'2021JZG005','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','周维修',NULL,NULL,1,1,NULL,NULL,'汉族',NULL,NULL,NULL,NULL,2,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(6,'2022JZG006','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','吴外教',NULL,NULL,1,1,NULL,NULL,'汉族',NULL,NULL,NULL,NULL,5,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(7,'2023JZG007','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','郑行政',NULL,NULL,1,2,NULL,NULL,'汉族',NULL,NULL,NULL,NULL,1,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(8,'2024JZG008','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','王新讲师',NULL,NULL,1,1,NULL,NULL,'汉族',NULL,NULL,NULL,NULL,1,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(9,'2024JZG009','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','刘新讲师',NULL,NULL,1,2,NULL,NULL,'汉族',NULL,NULL,NULL,NULL,1,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(10,'2000JZG010','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','老院长',NULL,NULL,1,1,NULL,NULL,'汉族',NULL,NULL,NULL,NULL,1,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(11,'20240101','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','张三',NULL,NULL,0,1,NULL,NULL,'汉族',NULL,1,1,1,NULL,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(12,'20240102','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','李四',NULL,NULL,0,1,NULL,NULL,'汉族',NULL,1,1,1,NULL,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(13,'20240103','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','王五',NULL,NULL,0,1,NULL,NULL,'汉族',NULL,1,1,1,NULL,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(14,'20240104','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','赵六',NULL,NULL,0,1,NULL,NULL,'汉族',NULL,1,1,1,NULL,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(15,'20240201','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','小红',NULL,NULL,0,2,NULL,NULL,'汉族',NULL,1,1,2,NULL,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(16,'20240202','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','小兰',NULL,NULL,0,2,NULL,NULL,'汉族',NULL,1,1,2,NULL,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(17,'20240203','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','小美',NULL,NULL,0,2,NULL,NULL,'汉族',NULL,1,1,2,NULL,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(18,'20240204','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','小丽',NULL,NULL,0,2,NULL,NULL,'汉族',NULL,1,1,2,NULL,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(19,'20250301','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','Alice',NULL,NULL,0,2,NULL,NULL,'汉族',NULL,2,3,4,NULL,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(20,'20250302','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','Bob',NULL,NULL,0,1,NULL,NULL,'汉族',NULL,2,3,4,NULL,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(21,'20250303','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','Cindy',NULL,NULL,0,2,NULL,NULL,'汉族',NULL,2,3,4,NULL,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(22,'20240401','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','铁柱',NULL,NULL,0,1,NULL,NULL,'汉族',NULL,3,4,6,NULL,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(23,'20240402','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','钢蛋',NULL,NULL,0,1,NULL,NULL,'汉族',NULL,3,4,6,NULL,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(24,'20240403','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','二狗',NULL,NULL,0,1,NULL,NULL,'汉族',NULL,3,4,6,NULL,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(25,'BAD001','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','刁民甲',NULL,NULL,0,1,NULL,NULL,'汉族',NULL,1,1,1,NULL,0,'1','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(26,'BAD002','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','刁民乙',NULL,NULL,0,2,NULL,NULL,'汉族',NULL,1,1,2,NULL,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(27,'2023Y01','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','研一师兄',NULL,NULL,0,1,NULL,NULL,'汉族',NULL,1,2,3,NULL,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(28,'2023Y02','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','研二师姐',NULL,NULL,0,2,NULL,NULL,'汉族',NULL,1,2,3,NULL,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(29,'2023Y03','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','李小牧',NULL,NULL,0,1,NULL,NULL,'汉族',NULL,1,2,3,NULL,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(30,'FILL01','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','路人A',NULL,NULL,0,1,NULL,NULL,'汉族',NULL,5,5,8,NULL,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(31,'FILL02','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','路人B',NULL,NULL,0,1,NULL,NULL,'汉族',NULL,5,5,8,NULL,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(32,'FILL03','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','路人C',NULL,NULL,0,2,NULL,NULL,'汉族',NULL,5,5,8,NULL,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(33,'FILL04','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','路人D',NULL,NULL,0,2,NULL,NULL,'汉族',NULL,5,5,8,NULL,0,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL);

/*Table structure for table `sys_role` */

DROP TABLE IF EXISTS `sys_role`;

CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) NOT NULL COMMENT '角色标识(代码鉴权用)',
  `sort` int DEFAULT '0' COMMENT '显示顺序',
  `status` char(1) DEFAULT '0' COMMENT '状态: 0-正常 1-停用',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-角色表';

/*Data for the table `sys_role` */

insert  into `sys_role`(`id`,`role_name`,`role_key`,`sort`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,'超级管理员','super_admin',1,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(2,'部门/学院管理员','dept_admin',2,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(3,'宿管经理','dorm_manager',3,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(4,'辅导员','counselor',4,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(5,'维修人员','repair_staff',5,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(6,'学生','student',6,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(7,'学院教职工','college_teacher',7,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(8,'后勤职工','staff',8,'0','0','admin','2026-01-15 18:43:32','',NULL,NULL);

/*Table structure for table `sys_user_role` */

DROP TABLE IF EXISTS `sys_user_role`;

CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID (包括admin和ordinary)',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-用户角色关联表';

/*Data for the table `sys_user_role` */

insert  into `sys_user_role`(`user_id`,`role_id`,`create_time`) values 
(1,1,'2026-01-16 13:11:39'),
(29,7,'2026-01-15 18:43:32');

/*Table structure for table `sys_utility_price` */

DROP TABLE IF EXISTS `sys_utility_price`;

CREATE TABLE `sys_utility_price` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` int NOT NULL COMMENT '1-水 2-电',
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
(1,1,'生活用水',3.5000,'吨','0','admin','2026-01-15 18:43:32','',NULL,NULL),
(2,2,'基础电价',0.5600,'度','0','admin','2026-01-15 18:43:32','','2026-01-15 18:44:01',NULL);

/*Table structure for table `sys_utility_price_rule` */

DROP TABLE IF EXISTS `sys_utility_price_rule`;

CREATE TABLE `sys_utility_price_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `rule_name` varchar(100) NOT NULL,
  `utility_type` tinyint NOT NULL COMMENT '1-电 2-水',
  `tier_start` decimal(10,2) NOT NULL,
  `tier_end` decimal(10,2) DEFAULT '9999.99',
  `unit_price` decimal(10,3) NOT NULL,
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='配置-阶梯计价规则';

/*Data for the table `sys_utility_price_rule` */

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
