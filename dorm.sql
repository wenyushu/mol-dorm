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
  `counselor_id` bigint DEFAULT NULL COMMENT '辅导员ID (关联sys_admin_user)',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0:正常 1:删除)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_major` (`major_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-班级表';

/*Data for the table `biz_class` */

insert  into `biz_class`(`id`,`major_id`,`grade`,`class_name`,`counselor_id`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,101,2024,'软工2401班',10,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(2,101,2024,'软工2402班',10,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(3,102,2024,'人工智能2401班',10,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(4,201,2024,'英语2401班',11,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(5,301,2024,'机自2401班',12,'admin','2026-01-28 11:31:38','',NULL,'0',NULL);

/*Table structure for table `biz_electric_violation_log` */

DROP TABLE IF EXISTS `biz_electric_violation_log`;

CREATE TABLE `biz_electric_violation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `violation_type` int NOT NULL COMMENT '违规类型 (1:恶性负载 2:大功率 3:阻性负载)',
  `power_val` decimal(10,2) DEFAULT NULL COMMENT '检测到的功率值(W)',
  `detected_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物联-违规日志';

/*Data for the table `biz_electric_violation_log` */

insert  into `biz_electric_violation_log`(`id`,`room_id`,`violation_type`,`power_val`,`detected_time`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,4,2,2500.00,'2026-01-20 17:20:22','system','2026-01-28 09:09:27','',NULL,'0','检测到大功率电器(电磁炉)');

/*Table structure for table `biz_repair_order` */

DROP TABLE IF EXISTS `biz_repair_order`;

CREATE TABLE `biz_repair_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(32) NOT NULL COMMENT '工单号 (唯一)',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID',
  `description` text NOT NULL COMMENT '报修描述',
  `images` varchar(1000) DEFAULT NULL COMMENT '图片地址(逗号分隔)',
  `status` int DEFAULT '0' COMMENT '状态: 0待处理 1维修中 2已完成 3已评价',
  `repairman_id` bigint DEFAULT NULL COMMENT '维修工ID',
  `finish_time` datetime DEFAULT NULL COMMENT '完工时间',
  `rating` tinyint DEFAULT NULL COMMENT '评分 (1-5)',
  `comment` varchar(255) DEFAULT NULL COMMENT '评价内容',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-报修工单';

/*Data for the table `biz_repair_order` */

insert  into `biz_repair_order`(`id`,`order_no`,`room_id`,`applicant_id`,`description`,`images`,`status`,`repairman_id`,`finish_time`,`rating`,`comment`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,'REP20260128001',201,10005,'洗手间水龙头漏水，需要更换垫圈',NULL,0,NULL,NULL,NULL,NULL,'admin','2026-01-28 11:31:38','',NULL,'0',NULL);

/*Table structure for table `biz_user_preference` */

DROP TABLE IF EXISTS `biz_user_preference`;

CREATE TABLE `biz_user_preference` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `team_code` varchar(32) DEFAULT NULL COMMENT '组队码 (用于预分配)',
  `bed_time` tinyint DEFAULT '3' COMMENT '就寝时间',
  `wake_time` tinyint DEFAULT '3' COMMENT '起床时间',
  `siesta_habit` tinyint DEFAULT '0' COMMENT '午休习惯',
  `out_late_freq` tinyint DEFAULT '0' COMMENT '晚归频率',
  `sleep_quality` tinyint DEFAULT '2' COMMENT '睡眠质量',
  `snoring_level` tinyint DEFAULT '0' COMMENT '打呼噜程度',
  `grinding_teeth` tinyint DEFAULT '0' COMMENT '磨牙习惯',
  `sleep_talk` tinyint DEFAULT '0' COMMENT '说梦话',
  `climb_bed_noise` tinyint DEFAULT '1' COMMENT '上下床动静',
  `shower_freq` tinyint DEFAULT '1' COMMENT '洗澡频率',
  `sock_wash` tinyint DEFAULT '0' COMMENT '洗袜子习惯',
  `trash_habit` tinyint DEFAULT '2' COMMENT '倒垃圾习惯',
  `clean_freq` tinyint DEFAULT '2' COMMENT '打扫频率',
  `toilet_clean` tinyint DEFAULT '1' COMMENT '如厕卫生',
  `desk_messy` tinyint DEFAULT '2' COMMENT '桌面整洁度',
  `personal_hygiene` tinyint DEFAULT '3' COMMENT '个人卫生评价',
  `odor_tolerance` tinyint DEFAULT '2' COMMENT '异味容忍度',
  `smoking` tinyint DEFAULT '0' COMMENT '是否吸烟',
  `smoke_tolerance` tinyint DEFAULT '0' COMMENT '吸烟容忍度',
  `drinking` tinyint DEFAULT '0' COMMENT '是否饮酒',
  `ac_temp` tinyint DEFAULT '26' COMMENT '空调温度偏好',
  `ac_duration` tinyint DEFAULT '2' COMMENT '空调时长偏好',
  `game_type_lol` tinyint DEFAULT '0' COMMENT '玩LOL',
  `game_type_fps` tinyint DEFAULT '0' COMMENT '玩FPS',
  `game_type_3a` tinyint DEFAULT '0' COMMENT '玩3A大作',
  `game_type_mmo` tinyint DEFAULT '0' COMMENT '玩MMO',
  `game_type_mobile` tinyint DEFAULT '0' COMMENT '玩手游',
  `game_habit` tinyint DEFAULT '0' COMMENT '游戏频率',
  `game_voice` tinyint DEFAULT '1' COMMENT '连麦习惯',
  `keyboard_axis` tinyint DEFAULT '1' COMMENT '键盘轴体',
  `is_cosplay` tinyint DEFAULT '0' COMMENT '是否玩Cosplay',
  `is_anime` tinyint DEFAULT '0' COMMENT '是否二次元',
  `mbti_ei` char(1) DEFAULT NULL COMMENT 'MBTI E/I',
  `mbti_result` varchar(4) DEFAULT NULL COMMENT 'MBTI结果',
  `social_battery` tinyint DEFAULT '3' COMMENT '社交能量',
  `share_items` tinyint DEFAULT '1' COMMENT '物品分享意愿',
  `bring_guest` tinyint DEFAULT '0' COMMENT '带客习惯',
  `visitors` tinyint DEFAULT '0' COMMENT '访客频率',
  `relationship_status` tinyint DEFAULT '0' COMMENT '恋爱状态',
  `has_disability` tinyint DEFAULT '0' COMMENT '是否有残疾',
  `has_insulin` tinyint DEFAULT '0' COMMENT '是否注射胰岛素',
  `has_infectious` tinyint DEFAULT '0' COMMENT '是否有传染病',
  `religion_taboo` varchar(255) DEFAULT NULL COMMENT '宗教禁忌',
  `special_disease` varchar(255) DEFAULT NULL COMMENT '特殊疾病',
  `game_rank` tinyint DEFAULT '0' COMMENT '游戏段位',
  `game_role` tinyint DEFAULT '0' COMMENT '游戏定位',
  `eat_luosifen` tinyint DEFAULT '0' COMMENT '吃螺蛳粉',
  `eat_durian` tinyint DEFAULT '0' COMMENT '吃榴莲',
  `region_type` tinyint DEFAULT NULL COMMENT '地区偏好',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-用户画像';

/*Data for the table `biz_user_preference` */

/*Table structure for table `biz_utility_bill` */

DROP TABLE IF EXISTS `biz_utility_bill`;

CREATE TABLE `biz_utility_bill` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `month` varchar(7) NOT NULL COMMENT '账单月份 (yyyy-MM)',
  `total_amount` decimal(10,2) DEFAULT '0.00' COMMENT '总金额',
  `water_cold` decimal(10,2) DEFAULT '0.00' COMMENT '冷水用量',
  `water_hot` decimal(10,2) DEFAULT '0.00' COMMENT '热水用量',
  `electric_usage` decimal(10,2) DEFAULT '0.00' COMMENT '用电量',
  `cost_water_cold` decimal(10,2) DEFAULT '0.00' COMMENT '冷水费',
  `cost_water_hot` decimal(10,2) DEFAULT '0.00' COMMENT '热水费',
  `cost_electric` decimal(10,2) DEFAULT '0.00' COMMENT '电费',
  `status` int DEFAULT '0' COMMENT '状态: 0未缴费 1已缴费 2逾期 3作废',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `payment_status` int DEFAULT '0' COMMENT '支付状态码',
  `version` int DEFAULT '1' COMMENT '乐观锁',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_month` (`room_id`,`month`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-水电账单';

/*Data for the table `biz_utility_bill` */

insert  into `biz_utility_bill`(`id`,`room_id`,`month`,`total_amount`,`water_cold`,`water_hot`,`electric_usage`,`cost_water_cold`,`cost_water_hot`,`cost_electric`,`status`,`pay_time`,`payment_status`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,101,'2026-01',150.50,10.00,5.00,100.00,35.00,90.00,58.00,1,NULL,1,1,'admin','2026-01-28 11:31:38','',NULL,'0',NULL);

/*Table structure for table `dorm_bed` */

DROP TABLE IF EXISTS `dorm_bed`;

CREATE TABLE `dorm_bed` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint DEFAULT NULL COMMENT '校区ID (冗余)',
  `building_id` bigint DEFAULT NULL COMMENT '楼栋ID (冗余)',
  `floor_id` bigint DEFAULT NULL COMMENT '楼层ID (冗余)',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `bed_label` varchar(32) DEFAULT NULL COMMENT '床号 (如: 1号床)',
  `sort_order` int NOT NULL DEFAULT '1' COMMENT '排序',
  `occupant_id` bigint DEFAULT NULL COMMENT '占用者ID (用户ID)',
  `occupant_type` int DEFAULT '0' COMMENT '入住类型: 0学生 1教工',
  `status` int DEFAULT '0' COMMENT '状态: 0空闲 1占用 2报修',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_room` (`room_id`),
  KEY `idx_occupant` (`occupant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-床位表';

/*Data for the table `dorm_bed` */

insert  into `dorm_bed`(`id`,`campus_id`,`building_id`,`floor_id`,`room_id`,`bed_label`,`sort_order`,`occupant_id`,`occupant_type`,`status`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,NULL,NULL,NULL,101,'101-1',1,10001,0,1,0,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(2,NULL,NULL,NULL,101,'101-2',2,10002,0,1,0,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(3,NULL,NULL,NULL,101,'101-3',3,10003,0,1,0,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(4,NULL,NULL,NULL,101,'101-4',4,10004,0,1,0,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(5,NULL,NULL,NULL,102,'102-1',1,NULL,0,0,0,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(6,NULL,NULL,NULL,102,'102-2',2,NULL,0,0,0,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(7,NULL,NULL,NULL,102,'102-3',3,NULL,0,0,0,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(8,NULL,NULL,NULL,102,'102-4',4,NULL,0,0,0,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(9,NULL,NULL,NULL,201,'201-1',1,10005,0,1,0,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(10,NULL,NULL,NULL,201,'201-2',2,10006,0,1,0,'admin','2026-01-28 11:31:38','',NULL,'0',NULL);

/*Table structure for table `dorm_building` */

DROP TABLE IF EXISTS `dorm_building`;

CREATE TABLE `dorm_building` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint NOT NULL COMMENT '校区ID',
  `building_name` varchar(64) NOT NULL COMMENT '楼栋名',
  `building_no` varchar(32) DEFAULT NULL COMMENT '楼栋编号',
  `floor_count` int DEFAULT NULL COMMENT '层数',
  `gender_limit` int DEFAULT '3' COMMENT '性别限制: 1男 2女 3混合',
  `usage_type` int DEFAULT '0' COMMENT '用途: 0学生 1教工',
  `manager_id` bigint DEFAULT NULL COMMENT '宿管负责人ID',
  `location` varchar(255) DEFAULT NULL COMMENT '地理位置',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态: 1启用 0停用',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_campus` (`campus_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-楼栋表';

/*Data for the table `dorm_building` */

insert  into `dorm_building`(`id`,`campus_id`,`building_name`,`building_no`,`floor_count`,`gender_limit`,`usage_type`,`manager_id`,`location`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,'北区1号楼(海棠苑)','HT-01',6,1,0,20,NULL,1,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(2,1,'北区2号楼(丁香苑)','DX-01',6,2,0,NULL,NULL,1,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(3,2,'南区1号楼(机械楼)','ME-01',5,1,0,21,NULL,1,'admin','2026-01-28 11:31:38','',NULL,'0',NULL);

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
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-调宿申请';

/*Data for the table `dorm_change_request` */

/*Table structure for table `dorm_fixed_asset` */

DROP TABLE IF EXISTS `dorm_fixed_asset`;

CREATE TABLE `dorm_fixed_asset` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `asset_name` varchar(100) NOT NULL COMMENT '资产名称',
  `asset_code` varchar(64) NOT NULL COMMENT '资产编号',
  `category` int NOT NULL COMMENT '类型: 1家具 2电器',
  `room_id` bigint NOT NULL COMMENT '所属房间ID',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '价格',
  `status` int DEFAULT '1' COMMENT '状态: 1正常 2报修 3丢失',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_asset_code` (`asset_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资产表';

/*Data for the table `dorm_fixed_asset` */

/*Table structure for table `dorm_floor` */

DROP TABLE IF EXISTS `dorm_floor`;

CREATE TABLE `dorm_floor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint DEFAULT NULL COMMENT '校区ID (冗余)',
  `building_id` bigint NOT NULL COMMENT '楼栋ID',
  `floor_num` int NOT NULL COMMENT '楼层号',
  `gender_limit` int DEFAULT '0' COMMENT '性别限制: 0混合 1男 2女',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态: 1正常 0停用',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_building` (`building_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-楼层表';

/*Data for the table `dorm_floor` */

insert  into `dorm_floor`(`id`,`campus_id`,`building_id`,`floor_num`,`gender_limit`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,NULL,1,1,1,1,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(2,NULL,2,1,2,1,'admin','2026-01-28 11:31:38','',NULL,'0',NULL);

/*Table structure for table `dorm_meter_electric` */

DROP TABLE IF EXISTS `dorm_meter_electric`;

CREATE TABLE `dorm_meter_electric` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `meter_no` varchar(50) NOT NULL COMMENT '电表编号',
  `current_reading` decimal(10,2) DEFAULT '0.00' COMMENT '当前读数',
  `balance` decimal(10,2) DEFAULT '0.00' COMMENT '电费余额',
  `status` int DEFAULT '1' COMMENT '状态: 1正常 2故障',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物联-电表';

/*Data for the table `dorm_meter_electric` */

/*Table structure for table `dorm_meter_water` */

DROP TABLE IF EXISTS `dorm_meter_water`;

CREATE TABLE `dorm_meter_water` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `meter_no` varchar(50) NOT NULL COMMENT '水表编号',
  `current_reading` decimal(10,2) DEFAULT '0.00' COMMENT '当前读数',
  `status` int DEFAULT '1' COMMENT '状态: 1正常 2故障',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物联-水表';

/*Data for the table `dorm_meter_water` */

/*Table structure for table `dorm_room` */

DROP TABLE IF EXISTS `dorm_room`;

CREATE TABLE `dorm_room` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint DEFAULT NULL COMMENT '校区ID (冗余)',
  `building_id` bigint DEFAULT NULL COMMENT '楼栋ID (冗余)',
  `floor_id` bigint NOT NULL COMMENT '楼层ID',
  `floor_no` int DEFAULT NULL COMMENT '楼层号 (冗余)',
  `room_no` varchar(32) NOT NULL COMMENT '房号',
  `apartment_type` varchar(32) DEFAULT '四人间' COMMENT '房型: 四人间/二人间',
  `capacity` int DEFAULT '4' COMMENT '核定人数',
  `current_num` int DEFAULT '0' COMMENT '当前人数',
  `gender` char(1) DEFAULT '1' COMMENT '性别限制: 0女 1男',
  `status` int DEFAULT '10' COMMENT '状态: 10正常 20满员 40维修',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_hierarchy` (`campus_id`,`building_id`,`floor_id`)
) ENGINE=InnoDB AUTO_INCREMENT=202 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-房间表';

/*Data for the table `dorm_room` */

insert  into `dorm_room`(`id`,`campus_id`,`building_id`,`floor_id`,`floor_no`,`room_no`,`apartment_type`,`capacity`,`current_num`,`gender`,`status`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(101,1,1,1,1,'101','四人间',4,4,'1',20,0,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(102,1,1,1,1,'102','四人间',4,0,'1',10,0,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(201,1,2,2,1,'201','二人间',2,2,'0',20,0,'admin','2026-01-28 11:31:38','',NULL,'0',NULL);

/*Table structure for table `dorm_staff_application` */

DROP TABLE IF EXISTS `dorm_staff_application`;

CREATE TABLE `dorm_staff_application` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '教工ID',
  `apply_type` tinyint DEFAULT '0' COMMENT '类型: 0入住 1退宿 2换房',
  `target_room_type` varchar(50) DEFAULT NULL COMMENT '期望房型',
  `reason` varchar(255) DEFAULT NULL COMMENT '申请理由',
  `status` tinyint DEFAULT '0' COMMENT '状态: 0待审 1通过 2驳回',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-教工住宿申请';

/*Data for the table `dorm_staff_application` */

/*Table structure for table `sys_admin_user` */

DROP TABLE IF EXISTS `sys_admin_user`;

CREATE TABLE `sys_admin_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(64) NOT NULL COMMENT '登录账号',
  `password` varchar(100) NOT NULL COMMENT '加密密码',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
  `gender` char(1) NOT NULL DEFAULT '1' COMMENT '性别:0女 1男',
  `phone` varchar(255) NOT NULL DEFAULT '' COMMENT '手机号(密文)',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(500) DEFAULT '' COMMENT '头像URL',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态:0正常 1停用',
  `residence_type` int NOT NULL DEFAULT '1' COMMENT '居住类型(0住校 1校外)',
  `is_initial_pwd` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否初始密码',
  `campus_id` bigint DEFAULT NULL COMMENT '所属校区ID',
  `dept_id` bigint DEFAULT NULL COMMENT '所属部门ID',
  `college_id` bigint DEFAULT NULL COMMENT '所属学院ID',
  `emergency_contact` varchar(64) DEFAULT '' COMMENT '紧急联系人',
  `emergency_phone` varchar(255) DEFAULT '' COMMENT '紧急电话',
  `emergency_relation` varchar(32) DEFAULT '' COMMENT '关系',
  `current_address` varchar(500) DEFAULT '' COMMENT '居住地址',
  `id_card` varchar(255) DEFAULT '' COMMENT '身份证(密文)',
  `ethnicity` varchar(20) DEFAULT '汉族' COMMENT '民族',
  `hometown` varchar(64) DEFAULT '未知' COMMENT '籍贯',
  `campus_status` int DEFAULT '1' COMMENT '在岗状态: 1在岗 0休假/离校',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间(安全审计)',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_name` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-后台管理员表';

/*Data for the table `sys_admin_user` */

insert  into `sys_admin_user`(`id`,`username`,`password`,`real_name`,`nickname`,`gender`,`phone`,`email`,`avatar`,`status`,`residence_type`,`is_initial_pwd`,`campus_id`,`dept_id`,`college_id`,`emergency_contact`,`emergency_phone`,`emergency_relation`,`current_address`,`id_card`,`ethnicity`,`hometown`,`campus_status`,`last_login_time`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,'admin','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','超级管理员','SuperAdmin','1','13800000000',NULL,'','0',1,0,NULL,NULL,NULL,'','','','','','汉族','未知',1,NULL,'system','2026-01-28 09:09:28','',NULL,'0','拥有所有权限'),
(10,'20JZG3101001','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','刘志刚','计院辅导员','1','13800100010',NULL,'','0',1,1,1,101,1,'','','','','','汉族','未知',1,NULL,'system','2026-01-28 11:31:38','',NULL,'0','负责计算机学院大一新生'),
(11,'21JZG3102001','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','王芳','外语辅导员','0','13800100011',NULL,'','0',1,1,1,101,2,'','','','','','汉族','未知',1,NULL,'system','2026-01-28 11:31:38','',NULL,'0','负责外语学院女生工作'),
(12,'19JZG3203001','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','张建国','机械辅导员','1','13800100012',NULL,'','0',1,1,2,201,3,'','','','','','汉族','未知',1,NULL,'system','2026-01-28 11:31:38','',NULL,'0','负责南校区机械学院'),
(20,'18JZG5102001','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','赵桂兰','北区宿管长','0','13800100020',NULL,'','0',1,1,1,102,NULL,'','','','','','汉族','未知',1,NULL,'system','2026-01-28 11:31:38','',NULL,'0','海棠苑1号楼宿管'),
(21,'18JZG5202002','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','李大爷','南区宿管','1','13800100021',NULL,'','0',1,1,2,102,NULL,'','','','','','汉族','未知',1,NULL,'system','2026-01-28 11:31:38','',NULL,'0','负责南区男生宿舍'),
(30,'22JZG1102001','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','孙师傅','金牌电工','1','13800100030',NULL,'','0',1,1,1,102,NULL,'','','','','','汉族','未知',1,NULL,'system','2026-01-28 11:31:38','',NULL,'0','负责全校电路维修'),
(40,'23JZG1103001','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','周队长','保安队长','1','13800100040',NULL,'','0',1,1,1,103,NULL,'','','','','','汉族','未知',1,NULL,'system','2026-01-28 11:31:38','',NULL,'0','退伍军人');

/*Table structure for table `sys_algorithm_config` */

DROP TABLE IF EXISTS `sys_algorithm_config`;

CREATE TABLE `sys_algorithm_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` varchar(100) NOT NULL COMMENT '配置值',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='配置-算法';

/*Data for the table `sys_algorithm_config` */

insert  into `sys_algorithm_config`(`id`,`config_key`,`config_value`,`description`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,'weight_sleep','0.8','作息权重','admin','2026-01-28 09:09:28','',NULL,'0',NULL);

/*Table structure for table `sys_campus` */

DROP TABLE IF EXISTS `sys_campus`;

CREATE TABLE `sys_campus` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_name` varchar(64) NOT NULL COMMENT '校区名称',
  `campus_code` varchar(32) NOT NULL COMMENT '校区编码',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态: 1启用 0停用',
  `price_water_cold` decimal(10,2) DEFAULT '3.50' COMMENT '冷水单价(元/吨)',
  `price_water_hot` decimal(10,2) DEFAULT '18.00' COMMENT '热水单价(元/吨)',
  `price_electric` decimal(10,2) DEFAULT '0.58' COMMENT '电费单价(元/度)',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-校区表';

/*Data for the table `sys_campus` */

insert  into `sys_campus`(`id`,`campus_name`,`campus_code`,`address`,`status`,`price_water_cold`,`price_water_hot`,`price_electric`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,'北校区','CAMP-N','北京市海淀区学府路1号',1,3.50,18.00,0.58,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(2,'南校区','CAMP-S','北京市昌平区科技园路8号',1,3.20,16.50,0.52,'admin','2026-01-28 11:31:38','',NULL,'0',NULL);

/*Table structure for table `sys_college` */

DROP TABLE IF EXISTS `sys_college`;

CREATE TABLE `sys_college` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint NOT NULL COMMENT '所属校区ID',
  `college_name` varchar(64) NOT NULL COMMENT '学院名称',
  `short_name` varchar(32) DEFAULT NULL COMMENT '简称',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态: 1正常 0停用',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_campus` (`campus_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-学院表';

/*Data for the table `sys_college` */

insert  into `sys_college`(`id`,`campus_id`,`college_name`,`short_name`,`sort`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,'计算机学院','CS',1,1,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(2,1,'外国语学院','FL',2,1,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(3,2,'机械工程学院','ME',3,1,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(4,2,'经济管理学院','SEM',4,1,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(5,2,'艺术设计学院','ART',5,1,'admin','2026-01-28 11:31:38','',NULL,'0',NULL);

/*Table structure for table `sys_dept` */

DROP TABLE IF EXISTS `sys_dept`;

CREATE TABLE `sys_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint NOT NULL COMMENT '所属校区ID',
  `dept_name` varchar(64) NOT NULL COMMENT '部门名称',
  `parent_id` bigint DEFAULT '0' COMMENT '父部门ID',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态: 1正常 0停用',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_campus` (`campus_id`)
) ENGINE=InnoDB AUTO_INCREMENT=202 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-部门表';

/*Data for the table `sys_dept` */

insert  into `sys_dept`(`id`,`campus_id`,`dept_name`,`parent_id`,`sort`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(101,1,'学工处',0,1,1,'admin','2026-01-28 11:31:38','',NULL,'0','负责学生事务管理'),
(102,1,'后勤处',0,2,1,'admin','2026-01-28 11:31:38','',NULL,'0','负责宿舍与食堂'),
(103,1,'保卫处',0,3,1,'admin','2026-01-28 11:31:38','',NULL,'0','负责校园安全'),
(201,2,'南校区管委会',0,1,1,'admin','2026-01-28 11:31:38','',NULL,'0','南校区综合管理');

/*Table structure for table `sys_login_log` */

DROP TABLE IF EXISTS `sys_login_log`;

CREATE TABLE `sys_login_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) DEFAULT '' COMMENT '登录账号',
  `ipaddr` varchar(128) DEFAULT '' COMMENT 'IP地址',
  `status` char(1) DEFAULT '0' COMMENT '状态: 0成功 1失败',
  `msg` varchar(255) DEFAULT '' COMMENT '提示信息',
  `access_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
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
  `sort` int DEFAULT '0' COMMENT '排序',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_college` (`college_id`)
) ENGINE=InnoDB AUTO_INCREMENT=502 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础-专业表';

/*Data for the table `sys_major` */

insert  into `sys_major`(`id`,`college_id`,`name`,`level`,`duration`,`sort`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(101,1,'软件工程','本科',4,0,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(102,1,'人工智能','本科',4,0,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(201,2,'英语','本科',4,0,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(301,3,'机械自动化','本科',4,0,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(401,4,'会计学','本科',4,0,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(501,5,'视觉传达','本科',4,0,'admin','2026-01-28 11:31:38','',NULL,'0',NULL);

/*Table structure for table `sys_notice` */

DROP TABLE IF EXISTS `sys_notice`;

CREATE TABLE `sys_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(128) NOT NULL COMMENT '标题',
  `content` text NOT NULL COMMENT '内容',
  `type` int DEFAULT '1' COMMENT '类型: 1通知 2公告',
  `status` char(1) DEFAULT '0' COMMENT '状态: 0正常 1关闭',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-通知公告';

/*Data for the table `sys_notice` */

insert  into `sys_notice`(`id`,`title`,`content`,`type`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,'2026 年春季学期开学通知','请各位同学于2月20日前返校报到...',1,'0','admin','2026-01-28 11:31:38','','2026-01-29 07:57:49','0',NULL),
(2,'关于严禁使用违规电器的公告','近期宿舍检查发现...',2,'0','admin','2026-01-28 11:31:38','',NULL,'0',NULL);

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
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
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
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态: 0正常 1停用 2已归档',
  `emergency_contact` varchar(64) NOT NULL COMMENT '紧急联系人',
  `emergency_phone` varchar(255) NOT NULL COMMENT '紧急电话',
  `emergency_relation` varchar(32) NOT NULL COMMENT '关系',
  `current_address` varchar(500) DEFAULT '' COMMENT '居住地址',
  `birth_date` date DEFAULT NULL COMMENT '出生日期',
  `political_status` varchar(20) DEFAULT '群众' COMMENT '政治面貌',
  `email` varchar(100) DEFAULT NULL COMMENT '电子邮箱',
  `landline` varchar(32) DEFAULT NULL COMMENT '固定电话',
  `entry_date` date DEFAULT NULL COMMENT '入学/入职日期',
  `campus_status` int DEFAULT '1' COMMENT '在校状态: 1在校 0离校(假期)',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间(用于判断不活跃)',
  `suspension_start_date` date DEFAULT NULL COMMENT '休学开始日期(用于计算2年期限)',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=10052 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-普通用户表(学生/教工)';

/*Data for the table `sys_ordinary_user` */

insert  into `sys_ordinary_user`(`id`,`username`,`password`,`real_name`,`nickname`,`user_category`,`gender`,`phone`,`id_card`,`ethnicity`,`hometown`,`avatar`,`campus_id`,`college_id`,`major_id`,`class_id`,`dept_id`,`contract_year`,`enrollment_year`,`entry_year`,`residence_type`,`is_initial_pwd`,`status`,`emergency_contact`,`emergency_phone`,`emergency_relation`,`current_address`,`birth_date`,`political_status`,`email`,`landline`,`entry_date`,`campus_status`,`last_login_time`,`suspension_start_date`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(10001,'24B1110001','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','陈伟',NULL,0,'1','13900000001','110101200601010001','汉族','未知','',1,1,101,1,NULL,1,NULL,NULL,0,1,'0','','','','',NULL,'群众',NULL,NULL,NULL,1,NULL,NULL,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(10002,'24B1110002','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','杨洋',NULL,0,'1','13900000002','110101200601010002','汉族','未知','',1,1,101,1,NULL,1,NULL,NULL,0,1,'0','','','','',NULL,'群众',NULL,NULL,NULL,1,NULL,NULL,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(10003,'24B1110003','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','吴杰',NULL,0,'1','13900000003','110101200601010003','汉族','未知','',1,1,101,1,NULL,1,NULL,NULL,0,1,'0','','','','',NULL,'群众',NULL,NULL,NULL,1,NULL,NULL,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(10004,'24B1110004','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','郑强',NULL,0,'1','13900000004','110101200601010004','汉族','未知','',1,1,101,1,NULL,1,NULL,NULL,0,1,'0','','','','',NULL,'群众',NULL,NULL,NULL,0,NULL,NULL,'admin','2026-01-28 11:31:38','',NULL,'0','寒假离校'),
(10005,'24B1210001','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','刘婷',NULL,0,'0','13900000005','110101200601010005','汉族','未知','',1,2,201,4,NULL,1,NULL,NULL,0,1,'0','','','','',NULL,'群众',NULL,NULL,NULL,1,NULL,NULL,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(10006,'24B1210002','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','何敏',NULL,0,'0','13900000006','110101200601010006','汉族','未知','',1,2,201,4,NULL,1,NULL,NULL,0,1,'0','','','','',NULL,'群众',NULL,NULL,NULL,1,NULL,NULL,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(10007,'24B2310001','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','朱刚',NULL,0,'1','13900000007','110101200601010007','汉族','未知','',2,3,301,5,NULL,1,NULL,NULL,0,1,'0','','','','',NULL,'群众',NULL,NULL,NULL,1,NULL,NULL,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(10008,'24B2310002','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','秦明',NULL,0,'1','13900000008','110101200601010008','汉族','未知','',2,3,301,5,NULL,1,NULL,NULL,0,1,'0','','','','',NULL,'群众',NULL,NULL,NULL,1,NULL,NULL,'admin','2026-01-28 11:31:38','',NULL,'0',NULL),
(10020,'10JZG910101','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','钱学森',NULL,1,'1','13900000020','110101197001010001','汉族','未知','',1,1,NULL,NULL,NULL,1,NULL,NULL,0,1,'0','','','','',NULL,'群众',NULL,NULL,NULL,1,NULL,NULL,'admin','2026-01-28 11:31:38','',NULL,'0','计算机教授'),
(10021,'15JZG310201','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','林徽因',NULL,1,'0','13900000021','110101198001010001','汉族','未知','',1,2,NULL,NULL,NULL,1,NULL,NULL,0,1,'0','','','','',NULL,'群众',NULL,NULL,NULL,1,NULL,NULL,'admin','2026-01-28 11:31:38','',NULL,'0','外语讲师'),
(10050,'23B1119998','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','李休学',NULL,0,'1','13900009998','110101200501019998','汉族','未知','',NULL,NULL,NULL,NULL,NULL,1,NULL,NULL,0,1,'2','','','','',NULL,'群众',NULL,NULL,NULL,0,NULL,'2025-10-01','admin','2026-01-28 11:31:38','',NULL,'0','【因病休学】保留学籍2年'),
(10051,'23B1119999','$2a$10$48HUfLrTafrpSIQYH9Yulehfj.Vak/gzRvgqLb5YrOMwtGmeGPTdO','王退学',NULL,0,'1','13900009999','110101200501019999','汉族','未知','',NULL,NULL,NULL,NULL,NULL,1,NULL,NULL,0,1,'2','','','','',NULL,'群众',NULL,NULL,NULL,0,NULL,NULL,'admin','2026-01-28 11:31:38','',NULL,'0','【主动退学】已离校');

/*Table structure for table `sys_role` */

DROP TABLE IF EXISTS `sys_role`;

CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` varchar(30) NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) NOT NULL COMMENT '角色权限字符',
  `sort` int DEFAULT '0' COMMENT '显示顺序',
  `status` char(1) DEFAULT '0' COMMENT '状态: 0正常 1停用',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-角色表';

/*Data for the table `sys_role` */

insert  into `sys_role`(`id`,`role_name`,`role_key`,`sort`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,'超级管理员','super_admin',1,'0','admin','2026-01-28 09:09:28','',NULL,'0',NULL),
(2,'宿管经理','dorm_manager',2,'0','admin','2026-01-28 09:09:28','',NULL,'0',NULL),
(3,'行政辅导员','counselor',3,'0','admin','2026-01-28 09:09:28','',NULL,'0',NULL),
(4,'维修工头','repair_master',4,'0','admin','2026-01-28 09:09:28','',NULL,'0',NULL),
(5,'普通学生','student',5,'0','admin','2026-01-28 09:09:28','',NULL,'0',NULL),
(6,'教职工','college_teacher',6,'0','admin','2026-01-28 09:09:28','',NULL,'0',NULL),
(7,'部门管理员','dept_admin',7,'0','admin','2026-01-28 09:09:28','',NULL,'0',NULL),
(8,'工勤人员','staff',8,'0','admin','2026-01-28 09:09:28','',NULL,'0',NULL);

/*Table structure for table `sys_user_archive` */

DROP TABLE IF EXISTS `sys_user_archive`;

CREATE TABLE `sys_user_archive` (
  `id` bigint NOT NULL COMMENT '原始用户ID(保留原ID)',
  `username` varchar(64) DEFAULT NULL COMMENT '账号',
  `real_name` varchar(50) DEFAULT NULL COMMENT '姓名',
  `user_category` int DEFAULT NULL COMMENT '0学生 1教工',
  `college_name` varchar(64) DEFAULT NULL COMMENT '归档时所属学院快照',
  `phone` varchar(255) DEFAULT NULL COMMENT '手机号',
  `id_card` varchar(255) DEFAULT NULL COMMENT '身份证',
  `archive_type` int NOT NULL COMMENT '归档类型: 10正常毕业, 20教工离职, 30长期不活跃冻结, 40主动退学, 41勒令退学, 50因病休学, 51因事休学, 52休学期满自动退学',
  `archive_reason` varchar(500) DEFAULT NULL COMMENT '详细原因',
  `archive_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '归档操作时间',
  `operator` varchar(64) DEFAULT NULL COMMENT '操作人(防抵赖)',
  `original_data_json` longtext COMMENT '原始数据全量备份(JSON)',
  `entry_year` int DEFAULT NULL COMMENT '入学/入职年份',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-用户归档表';

/*Data for the table `sys_user_archive` */

insert  into `sys_user_archive`(`id`,`username`,`real_name`,`user_category`,`college_name`,`phone`,`id_card`,`archive_type`,`archive_reason`,`archive_time`,`operator`,`original_data_json`,`entry_year`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(10050,'23B1119998','李休学',0,'计算机学院',NULL,NULL,50,'因病申请休学两年','2025-10-01 10:00:00','admin',NULL,NULL,'admin','2025-10-01 10:00:00','',NULL,'0','历史归档记录'),
(10051,'23B1119999','王退学',0,'计算机学院',NULL,NULL,40,'个人原因申请退学','2025-09-15 14:00:00','admin',NULL,NULL,'admin','2025-09-15 14:00:00','',NULL,'0','历史归档记录');

/*Table structure for table `sys_user_role` */

DROP TABLE IF EXISTS `sys_user_role`;

CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_by` varchar(64) DEFAULT 'system' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-用户角色关联';

/*Data for the table `sys_user_role` */

insert  into `sys_user_role`(`user_id`,`role_id`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,'system','2026-01-28 11:31:38','',NULL,'0',NULL),
(10,3,'system','2026-01-28 11:31:38','',NULL,'0',NULL),
(11,3,'system','2026-01-28 11:31:38','',NULL,'0',NULL),
(12,3,'system','2026-01-28 11:31:38','',NULL,'0',NULL),
(20,2,'system','2026-01-28 11:31:38','',NULL,'0',NULL),
(21,2,'system','2026-01-28 11:31:38','',NULL,'0',NULL),
(30,4,'system','2026-01-28 11:31:38','',NULL,'0',NULL),
(40,8,'system','2026-01-28 11:31:38','',NULL,'0',NULL),
(10001,5,'system','2026-01-28 11:31:38','',NULL,'0',NULL),
(10002,5,'system','2026-01-28 11:31:38','',NULL,'0',NULL),
(10003,5,'system','2026-01-28 11:31:38','',NULL,'0',NULL),
(10004,5,'system','2026-01-28 11:31:38','',NULL,'0',NULL),
(10005,5,'system','2026-01-28 11:31:38','',NULL,'0',NULL),
(10006,5,'system','2026-01-28 11:31:38','',NULL,'0',NULL),
(10007,5,'system','2026-01-28 11:31:38','',NULL,'0',NULL),
(10008,5,'system','2026-01-28 11:31:38','',NULL,'0',NULL),
(10020,6,'system','2026-01-28 11:31:38','',NULL,'0',NULL),
(10021,6,'system','2026-01-28 11:31:38','',NULL,'0',NULL),
(10050,5,'system','2026-01-28 11:31:38','',NULL,'0',NULL),
(10051,5,'system','2026-01-28 11:31:38','',NULL,'0',NULL);

/*Table structure for table `sys_utility_price` */

DROP TABLE IF EXISTS `sys_utility_price`;

CREATE TABLE `sys_utility_price` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type` int NOT NULL COMMENT '类型: 1水 2电',
  `name` varchar(50) NOT NULL COMMENT '名称',
  `price` decimal(10,4) NOT NULL COMMENT '单价',
  `unit` varchar(10) DEFAULT NULL COMMENT '单位',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='配置-水电价(全局兜底)';

/*Data for the table `sys_utility_price` */

insert  into `sys_utility_price`(`id`,`type`,`name`,`price`,`unit`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,'生活用水',4.3200,'吨','admin','2026-01-28 09:09:29','','2026-01-28 15:21:07','0',NULL),
(2,2,'生活用电',0.5600,'度','admin','2026-01-28 09:09:29','',NULL,'0',NULL);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
