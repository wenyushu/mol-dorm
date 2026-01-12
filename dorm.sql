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
  `id` bigint NOT NULL COMMENT '主键ID (建议规则: 专业ID+年级+班级号)',
  `major_id` bigint NOT NULL COMMENT '所属专业ID (关联 sys_major.id)',
  `grade` varchar(10) NOT NULL COMMENT '年级 (如: 2024)',
  `class_name` varchar(100) NOT NULL COMMENT '班级名称 (如: 软工2401班)',
  `counselor_id` bigint DEFAULT NULL COMMENT '辅导员ID (关联 sys_admin_user.id)',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除 (0:未删 1:已删)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='班级表';

/*Data for the table `biz_class` */

insert  into `biz_class`(`id`,`major_id`,`grade`,`class_name`,`counselor_id`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(10301,201,'2023','软工2301班',NULL,'admin','2026-01-11 15:41:44',NULL,NULL,'0',NULL),
(10302,201,'2023','软工2302班',NULL,'admin','2026-01-11 15:41:44',NULL,NULL,'0',NULL),
(10303,201,'2024','软工2401班',NULL,'admin','2026-01-11 15:41:44',NULL,NULL,'0',NULL),
(10304,202,'2023','网安2301班',NULL,'admin','2026-01-11 15:41:44',NULL,NULL,'0',NULL),
(10305,202,'2024','网安2401班',NULL,'admin','2026-01-11 15:41:44',NULL,NULL,'0',NULL),
(10306,203,'2023','会计2301班',NULL,'admin','2026-01-11 15:41:44',NULL,NULL,'0',NULL),
(10307,205,'2023','英语2301班',NULL,'admin','2026-01-11 15:41:44',NULL,NULL,'0',NULL),
(10308,203,'2022','会计2201班',NULL,'admin','2026-01-11 15:41:44',NULL,NULL,'0',NULL),
(10309,204,'2024','计科2401班',NULL,'admin','2026-01-11 15:41:44',NULL,NULL,'0',NULL),
(10310,204,'2024','计科2402班',NULL,'admin','2026-01-11 15:41:44',NULL,NULL,'0',NULL);

/*Table structure for table `biz_electric_violation_log` */

DROP TABLE IF EXISTS `biz_electric_violation_log`;

CREATE TABLE `biz_electric_violation_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `violation_type` tinyint NOT NULL COMMENT '违规类型 (1-超载 2-恶性负载/违禁电器)',
  `description` varchar(255) DEFAULT NULL COMMENT '详细描述',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除 (0:未删 1:已删)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='违规用电告警表';

/*Data for the table `biz_electric_violation_log` */

/*Table structure for table `biz_meter_reading` */

DROP TABLE IF EXISTS `biz_meter_reading`;

CREATE TABLE `biz_meter_reading` (
  `id` bigint NOT NULL COMMENT '主键ID',
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
  `order_no` varchar(32) NOT NULL COMMENT '工单号 (规则: R+年月日+序列)',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID (sys_ordinary_user.id)',
  `description` text NOT NULL COMMENT '故障描述',
  `status` int DEFAULT '0' COMMENT '状态 (0:待处理 1:维修中 2:已完成 3:已驳回)',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除 (0:未删 1:已删)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注/维修反馈',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='报修工单表';

/*Data for the table `biz_repair_order` */

insert  into `biz_repair_order`(`id`,`order_no`,`room_id`,`applicant_id`,`description`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,'R20240901001',201,10001,'空调漏水，滴到床上了',0,'admin','2026-01-11 15:41:45','',NULL,'0',NULL),
(2,'R20240901002',202,10008,'阳台门锁坏了',2,'admin','2026-01-11 15:41:45','',NULL,'0',NULL),
(3,'R20240902001',209,10003,'厕所堵了',1,'admin','2026-01-11 15:41:45','',NULL,'0',NULL);

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
(70001,10101,'教授','长期',0,NULL,'admin','2026-01-08 10:32:50',NULL,NULL,'0',NULL),
(70002,10103,'讲师','长期',0,NULL,'admin','2026-01-08 10:32:50',NULL,NULL,'0',NULL);

/*Table structure for table `biz_user_preference` */

DROP TABLE IF EXISTS `biz_user_preference`;

CREATE TABLE `biz_user_preference` (
  `user_id` bigint NOT NULL COMMENT '用户ID (主键)',
  `team_code` varchar(32) DEFAULT NULL COMMENT '组队码 (有相同码优先分配)',
  `smoking` tinyint DEFAULT '0' COMMENT '抽烟习惯 (0:不抽 1:偶尔 2:经常)',
  `smoke_tolerance` tinyint DEFAULT '0' COMMENT '烟味容忍度 (0:绝不 1:可以)',
  `drinking` tinyint DEFAULT '0' COMMENT '喝酒习惯 (0:不喝 1:偶尔 2:经常)',
  `bed_time` tinyint DEFAULT '2' COMMENT '就寝时间 (1:22点前 2:23点 3:24点 4:凌晨1点 5:2点+)',
  `wake_time` tinyint DEFAULT '2' COMMENT '起床时间 (1:6点前 2:7点 3:8点 4:9点 5:10点+)',
  `sleep_light` tinyint DEFAULT '0' COMMENT '睡眠深浅 (0:雷打不动 1:普通 2:轻度敏感 3:神经衰弱)',
  `snoring` tinyint DEFAULT '0' COMMENT '打鼾情况 (0:无 1:轻微 2:严重)',
  `ac_temp_summer` tinyint DEFAULT '24' COMMENT '夏季空调偏好温度',
  `clean_freq` tinyint DEFAULT '2' COMMENT '打扫频率 (1:每天 2:每周 3:每月 4:随缘)',
  `toilet_clean` tinyint DEFAULT '1' COMMENT '接受轮流刷厕所 (1:接受 0:拒绝)',
  `personal_hygiene` tinyint DEFAULT '3' COMMENT '个人卫生自评 (1-5分)',
  `odor_tolerance` tinyint DEFAULT '2' COMMENT '异味容忍度 (1-5分)',
  `game_habit` tinyint DEFAULT '0' COMMENT '游戏习惯 (0:不玩 1:手游 2:端游/键鼠 3:主机)',
  `game_voice` tinyint DEFAULT '1' COMMENT '游戏语音音量 (0:静音 1:小声 2:激动)',
  `keyboard_type` tinyint DEFAULT '0' COMMENT '键盘类型 (0:静音/薄膜 1:机械/吵闹)',
  `is_acg` tinyint DEFAULT '0' COMMENT '二次元浓度 (0:现充 1:路人 2:老二次元)',
  `gym_habit` tinyint DEFAULT '0' COMMENT '健身习惯 (0:无 1:偶尔 2:狂热)',
  `mbti_e_i` char(1) DEFAULT 'E' COMMENT 'MBTI-EI维度 (E/I)',
  `mbti_type` varchar(4) DEFAULT NULL COMMENT '完整MBTI类型 (如 INTJ)',
  `social_battery` tinyint DEFAULT '3' COMMENT '社交电量 (1:社恐 - 5:社牛)',
  `visitors` tinyint DEFAULT '0' COMMENT '带人回寝接受度 (0:绝不 1:偶尔同性 2:经常同性 3:带异性)',
  `special_disease` varchar(255) DEFAULT NULL COMMENT '特殊病史 (如: 糖尿病需冰箱)',
  `disability` tinyint DEFAULT '0' COMMENT '残疾辅助需求 (0:无 1:需低楼层/电梯)',
  `religion_habit` varchar(100) DEFAULT NULL COMMENT '宗教习惯',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '其他备注',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户宿舍分配偏好画像表';

/*Data for the table `biz_user_preference` */

insert  into `biz_user_preference`(`user_id`,`team_code`,`smoking`,`smoke_tolerance`,`drinking`,`bed_time`,`wake_time`,`sleep_light`,`snoring`,`ac_temp_summer`,`clean_freq`,`toilet_clean`,`personal_hygiene`,`odor_tolerance`,`game_habit`,`game_voice`,`keyboard_type`,`is_acg`,`gym_habit`,`mbti_e_i`,`mbti_type`,`social_battery`,`visitors`,`special_disease`,`disability`,`religion_habit`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(10001,NULL,0,0,0,2,2,0,0,24,2,1,3,2,2,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'admin','2026-01-11 15:41:45','','2026-01-11 15:41:45','0',NULL),
(10002,NULL,1,0,0,3,2,0,0,24,2,1,3,2,2,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'admin','2026-01-11 15:41:45','','2026-01-11 15:41:45','0',NULL),
(10003,NULL,0,0,0,1,2,0,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'admin','2026-01-11 15:41:45','','2026-01-11 15:41:45','0',NULL),
(10004,NULL,0,0,0,2,2,0,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'admin','2026-01-11 15:41:45','','2026-01-11 15:41:45','0',NULL),
(10085,NULL,1,1,0,1,3,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10086,NULL,0,1,0,4,2,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10087,NULL,0,1,0,3,5,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10088,NULL,0,1,0,2,4,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10089,NULL,0,0,0,5,4,1,2,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10090,NULL,0,0,0,2,5,3,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10091,NULL,0,1,0,4,5,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10092,'TEAM_软工2401_8',0,1,0,2,5,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10093,'TEAM_软工2401_8',0,1,0,4,2,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10094,NULL,0,1,0,3,3,3,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10095,NULL,0,0,0,1,3,3,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10096,NULL,0,1,0,1,1,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10097,NULL,0,1,0,1,3,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10098,NULL,1,0,0,1,5,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10099,NULL,0,0,0,1,4,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10100,'TEAM_软工2401_16',1,1,0,4,4,3,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10101,'TEAM_软工2401_16',0,0,0,1,3,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10102,NULL,0,0,0,5,4,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10103,NULL,0,1,0,5,3,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10104,NULL,0,1,0,1,3,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10105,NULL,1,1,0,5,5,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10106,NULL,0,1,0,2,1,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10107,NULL,0,0,0,5,4,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10108,'TEAM_软工2401_24',0,0,0,2,4,3,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10109,'TEAM_软工2401_24',0,1,0,5,5,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10110,NULL,0,0,0,5,5,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10111,NULL,0,1,0,5,4,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10112,NULL,0,0,0,4,1,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10113,NULL,0,0,0,1,2,3,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10114,NULL,0,1,0,5,2,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10115,NULL,0,0,0,5,2,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10116,'TEAM_软工2401_32',0,0,0,1,3,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10117,'TEAM_软工2401_32',1,1,0,2,5,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10118,NULL,1,0,0,4,5,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10119,NULL,1,0,0,1,1,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10120,NULL,1,1,0,2,2,3,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10121,NULL,0,0,0,1,2,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10122,NULL,0,1,0,1,5,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10123,NULL,1,0,0,4,4,3,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10124,NULL,0,1,0,2,4,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10125,NULL,0,0,0,2,2,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10126,NULL,0,0,0,3,3,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10127,NULL,0,0,0,5,5,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10128,NULL,0,1,0,5,3,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10129,NULL,1,0,0,5,4,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10130,NULL,0,0,0,2,1,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10131,NULL,0,1,0,4,3,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10132,'TEAM_会计2401_8',0,0,0,4,2,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10133,'TEAM_会计2401_8',0,0,0,5,1,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10134,NULL,1,0,0,2,1,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10135,NULL,1,1,0,3,2,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10136,NULL,0,0,0,1,4,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10137,NULL,1,0,0,1,2,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10138,NULL,0,0,0,4,5,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10139,NULL,0,1,0,4,3,3,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10140,'TEAM_会计2401_16',0,1,0,1,3,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10141,'TEAM_会计2401_16',0,0,0,3,5,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10142,NULL,0,1,0,1,2,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10143,NULL,1,1,0,5,5,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10144,NULL,0,1,0,2,1,1,0,24,2,1,3,2,0,1,0,0,0,'E',NULL,3,0,NULL,0,NULL,'-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL);

/*Table structure for table `biz_utility_bill` */

DROP TABLE IF EXISTS `biz_utility_bill`;

CREATE TABLE `biz_utility_bill` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `month` varchar(7) NOT NULL COMMENT '账单月份 (2024-06)',
  `water_usage` decimal(10,2) DEFAULT '0.00' COMMENT '用水量',
  `electric_usage` decimal(10,2) DEFAULT '0.00' COMMENT '用电量',
  `total_cost` decimal(10,2) NOT NULL COMMENT '总费用',
  `status` int DEFAULT '0' COMMENT '支付状态 (0:未缴 1:已缴)',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='水电费账单表';

/*Data for the table `biz_utility_bill` */

insert  into `biz_utility_bill`(`id`,`room_id`,`month`,`water_usage`,`electric_usage`,`total_cost`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,201,'2024-09',12.50,108.00,85.60,1,'admin','2026-01-11 15:41:45','',NULL,'0',NULL),
(2,201,'2024-10',10.00,90.00,70.00,0,'admin','2026-01-11 15:41:45','',NULL,'0',NULL);

/*Table structure for table `dorm_bed` */

DROP TABLE IF EXISTS `dorm_bed`;

CREATE TABLE `dorm_bed` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '所属房间ID',
  `bed_label` varchar(20) NOT NULL COMMENT '床位号',
  `occupant_id` bigint DEFAULT NULL COMMENT '居住者ID (可为空)',
  `status` tinyint DEFAULT '0' COMMENT '状态',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_bed_room` (`room_id`),
  KEY `fk_bed_student` (`occupant_id`),
  CONSTRAINT `fk_bed_room` FOREIGN KEY (`room_id`) REFERENCES `dorm_room` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_bed_student` FOREIGN KEY (`occupant_id`) REFERENCES `sys_ordinary_user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍床位表';

/*Data for the table `dorm_bed` */

/*Table structure for table `dorm_building` */

DROP TABLE IF EXISTS `dorm_building`;

CREATE TABLE `dorm_building` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint NOT NULL COMMENT '所属校区ID',
  `name` varchar(50) NOT NULL COMMENT '楼栋名称',
  `type` int DEFAULT '1' COMMENT '类型 (1:男 2:女 3:混合)',
  `floors` int DEFAULT '6' COMMENT '总层数',
  `manager` varchar(50) DEFAULT NULL COMMENT '宿管负责人',
  `status` int DEFAULT '1' COMMENT '状态 (1:启用 0:维修)',
  `location` varchar(100) DEFAULT NULL COMMENT '位置',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_building_campus` (`campus_id`),
  CONSTRAINT `fk_building_campus` FOREIGN KEY (`campus_id`) REFERENCES `sys_campus` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍楼栋表';

/*Data for the table `dorm_building` */

/*Table structure for table `dorm_change_request` */

DROP TABLE IF EXISTS `dorm_change_request`;

CREATE TABLE `dorm_change_request` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请单ID',
  `student_id` bigint NOT NULL COMMENT '申请学生ID',
  `current_room_id` bigint NOT NULL COMMENT '原房间ID',
  `target_room_id` bigint NOT NULL COMMENT '目标房间ID',
  `reason` varchar(500) DEFAULT NULL COMMENT '申请原因',
  `status` int DEFAULT '0' COMMENT '审批状态 (0:待辅导员审 1:待宿管审 2:已完成 3:已驳回)',
  `auditor_id` bigint DEFAULT NULL COMMENT '审批人ID',
  `audit_msg` varchar(255) DEFAULT NULL COMMENT '审批意见记录',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='调宿申请单表';

/*Data for the table `dorm_change_request` */

insert  into `dorm_change_request`(`id`,`student_id`,`current_room_id`,`target_room_id`,`reason`,`status`,`auditor_id`,`audit_msg`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,10001,101,102,'宿舍太吵，想换个安静的',0,NULL,NULL,'admin','2026-01-10 16:02:33','',NULL,'0',NULL);

/*Table structure for table `dorm_fixed_asset` */

DROP TABLE IF EXISTS `dorm_fixed_asset`;

CREATE TABLE `dorm_fixed_asset` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `asset_name` varchar(100) NOT NULL COMMENT '资产名称',
  `asset_code` varchar(64) DEFAULT NULL COMMENT '资产编号',
  `level_type` tinyint NOT NULL COMMENT '层级 (1:房间 2:层 3:楼)',
  `target_id` bigint NOT NULL COMMENT '关联目标ID (房间/层/楼)',
  `status` tinyint DEFAULT '1' COMMENT '状态 (1:正常 0:损坏)',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除 (0:未删 1:已删)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='固定资产表';

/*Data for the table `dorm_fixed_asset` */

insert  into `dorm_fixed_asset`(`id`,`asset_name`,`asset_code`,`level_type`,`target_id`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(80001,'格力空调',NULL,1,40001,1,'admin','2026-01-08 10:32:49',NULL,NULL,'0',NULL),
(80002,'格力空调',NULL,1,40002,1,'admin','2026-01-08 10:32:49',NULL,NULL,'0',NULL),
(80003,'格力空调',NULL,1,40005,1,'admin','2026-01-08 10:32:49',NULL,NULL,'0',NULL),
(80004,'公用饮水机',NULL,2,30001,1,'admin','2026-01-08 10:32:49',NULL,NULL,'0',NULL),
(80005,'公用洗衣机',NULL,2,30001,1,'admin','2026-01-08 10:32:49',NULL,NULL,'0',NULL),
(80006,'吹风机',NULL,2,30004,1,'admin','2026-01-08 10:32:49',NULL,NULL,'0',NULL),
(80007,'门禁闸机',NULL,3,20001,1,'admin','2026-01-08 10:32:49',NULL,NULL,'0',NULL),
(80008,'人脸识别终端',NULL,3,20002,1,'admin','2026-01-08 10:32:49',NULL,NULL,'0',NULL),
(80009,'电梯左',NULL,3,20001,1,'admin','2026-01-08 10:32:49',NULL,NULL,'0',NULL),
(80010,'电梯右',NULL,3,20001,1,'admin','2026-01-08 10:32:49',NULL,NULL,'0',NULL);

/*Table structure for table `dorm_floor` */

DROP TABLE IF EXISTS `dorm_floor`;

CREATE TABLE `dorm_floor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `building_id` bigint NOT NULL COMMENT '所属楼栋ID',
  `floor_num` int NOT NULL COMMENT '楼层号',
  `gender_limit` tinyint DEFAULT '0' COMMENT '性别限制 (0:无 1:男 2:女)',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_floor_building` (`building_id`),
  CONSTRAINT `fk_floor_building` FOREIGN KEY (`building_id`) REFERENCES `dorm_building` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍楼层表';

/*Data for the table `dorm_floor` */

/*Table structure for table `dorm_meter_electric` */

DROP TABLE IF EXISTS `dorm_meter_electric`;

CREATE TABLE `dorm_meter_electric` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `meter_no` varchar(50) NOT NULL COMMENT '电表编号',
  `current_reading` decimal(10,2) DEFAULT '0.00' COMMENT '当前读数',
  `balance` decimal(10,2) DEFAULT '0.00' COMMENT '余额',
  `is_tripped` tinyint DEFAULT '0' COMMENT '跳闸状态 (0:正常 1:跳闸)',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='水表表';

/*Data for the table `dorm_meter_water` */

/*Table structure for table `dorm_power_rule` */

DROP TABLE IF EXISTS `dorm_power_rule`;

CREATE TABLE `dorm_power_rule` (
  `id` bigint NOT NULL COMMENT '主键ID',
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
  `floor_no` int NOT NULL COMMENT '所在楼层',
  `room_no` varchar(20) NOT NULL COMMENT '房间号',
  `capacity` int DEFAULT '4' COMMENT '床位数',
  `current_num` int DEFAULT '0' COMMENT '实住人数',
  `gender` int DEFAULT '1' COMMENT '性别 (1:男 2:女)',
  `status` int DEFAULT '1' COMMENT '状态',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_room_building` (`building_id`),
  CONSTRAINT `fk_room_building` FOREIGN KEY (`building_id`) REFERENCES `dorm_building` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_room_gender` CHECK ((`gender` in (0,1,2)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍房间表';

/*Data for the table `dorm_room` */

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
(10001,2024,0,20001,'admin','2026-01-11 16:57:47','',NULL,'0',NULL),
(10002,2024,0,20002,'admin','2026-01-11 16:57:47','',NULL,'0',NULL),
(10003,2024,0,20005,'admin','2026-01-11 16:57:47','',NULL,'0',NULL);

/*Table structure for table `sys_admin_user` */

DROP TABLE IF EXISTS `sys_admin_user`;

CREATE TABLE `sys_admin_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(64) NOT NULL COMMENT '登录账号',
  `password` varchar(100) NOT NULL COMMENT '加密密码 (BCrypt)',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `status` char(1) DEFAULT '0' COMMENT '帐号状态 (0:正常 1:停用)',
  `email` varchar(50) DEFAULT NULL COMMENT '电子邮箱',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像地址',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除 (0:未删 1:已删)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注信息',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='后台管理员表 (宿管/后勤)';

/*Data for the table `sys_admin_user` */

insert  into `sys_admin_user`(`id`,`username`,`password`,`real_name`,`phone`,`status`,`email`,`avatar`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(6,'admin','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','系统超管',NULL,'0',NULL,NULL,'admin','2026-01-11 15:41:44','',NULL,'0',NULL),
(7,'manager','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','张宿管',NULL,'0',NULL,NULL,'admin','2026-01-11 15:41:44','',NULL,'0',NULL),
(8,'counselor1','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','李辅导员',NULL,'0',NULL,NULL,'admin','2026-01-11 15:41:44','',NULL,'0',NULL),
(9,'worker1','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','王维修工',NULL,'0',NULL,NULL,'admin','2026-01-11 15:41:44','',NULL,'0',NULL),
(10,'logistics','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','赵后勤',NULL,'0',NULL,NULL,'admin','2026-01-11 15:41:44','',NULL,'0',NULL);

/*Table structure for table `sys_algorithm_config` */

DROP TABLE IF EXISTS `sys_algorithm_config`;

CREATE TABLE `sys_algorithm_config` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` varchar(100) NOT NULL COMMENT '配置值',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '配置说明',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='算法与系统参数配置表';

/*Data for the table `sys_algorithm_config` */

insert  into `sys_algorithm_config`(`id`,`config_key`,`config_value`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,'max_introverts_per_room','2','admin','2026-01-11 16:57:51','',NULL,'0','内向者最大比例'),
(2,'max_minority_per_room','2','admin','2026-01-11 16:57:51','',NULL,'0','少数民族最大聚集数'),
(3,'game_clustering_weight','0.5','admin','2026-01-11 16:57:51','',NULL,'0','游戏发烧友聚合权重'),
(4,'sleep_time_weight','0.8','admin','2026-01-11 16:57:51','',NULL,'0','作息时间权重'),
(5,'smoke_free_policy','strict','admin','2026-01-11 16:57:51','',NULL,'0','禁烟策略'),
(6,'bed_allocation_mode','auto','admin','2026-01-11 16:57:51','',NULL,'0','床位分配模式'),
(7,'priority_freshman','true','admin','2026-01-11 16:57:51','',NULL,'0','新生优先'),
(8,'priority_disabled','true','admin','2026-01-11 16:57:51','',NULL,'0','残障人士优先'),
(9,'system_open_date','2026-08-20','admin','2026-01-11 16:57:51','',NULL,'0','系统开放时间'),
(10,'system_close_date','2026-09-01','admin','2026-01-11 16:57:51','',NULL,'0','系统关闭时间');

/*Table structure for table `sys_campus` */

DROP TABLE IF EXISTS `sys_campus`;

CREATE TABLE `sys_campus` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_name` varchar(100) NOT NULL COMMENT '校区名称',
  `campus_code` varchar(20) NOT NULL COMMENT '校区编码',
  `status` char(1) DEFAULT '0' COMMENT '状态 (0:正常 1:停用)',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='校区信息表';

/*Data for the table `sys_campus` */

/*Table structure for table `sys_college` */

DROP TABLE IF EXISTS `sys_college`;

CREATE TABLE `sys_college` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint DEFAULT NULL COMMENT '所属校区ID (关联 sys_campus)',
  `name` varchar(50) NOT NULL COMMENT '学院名称 (如: 计算机学院)',
  `code` varchar(20) NOT NULL COMMENT '学院代码 (如: CS01)',
  `sort` int DEFAULT '0' COMMENT '显示排序 (数值越小越靠前)',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除 (0:未删 1:已删)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=106 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='二级学院表';

/*Data for the table `sys_college` */

insert  into `sys_college`(`id`,`campus_id`,`name`,`code`,`sort`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(101,1,'计算机科学与技术学院','CS',1,'admin','2026-01-11 15:41:44','',NULL,'0',NULL),
(102,1,'网络空间安全学院','SEC',2,'admin','2026-01-11 15:41:44','',NULL,'0',NULL),
(103,2,'经济管理学院','ECO',3,'admin','2026-01-11 15:41:44','',NULL,'0',NULL),
(104,1,'人工智能学院','AI',4,'admin','2026-01-11 15:41:44','',NULL,'0',NULL),
(105,2,'外国语学院','FL',5,'admin','2026-01-11 15:41:44','',NULL,'0',NULL);

/*Table structure for table `sys_major` */

DROP TABLE IF EXISTS `sys_major`;

CREATE TABLE `sys_major` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `college_id` bigint NOT NULL COMMENT '所属学院ID (关联 sys_college)',
  `name` varchar(50) NOT NULL COMMENT '专业名称',
  `level` varchar(20) DEFAULT '本科' COMMENT '培养层次 (本科/专科/硕士/博士)',
  `duration` int DEFAULT '4' COMMENT '学制年份 (如: 4年)',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除 (0:未删 1:已删)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=206 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='专业信息表';

/*Data for the table `sys_major` */

insert  into `sys_major`(`id`,`college_id`,`name`,`level`,`duration`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(201,101,'软件工程','本科',4,'admin','2026-01-11 15:41:44','',NULL,'0',NULL),
(202,102,'网络安全','本科',4,'admin','2026-01-11 15:41:44','',NULL,'0',NULL),
(203,103,'会计学','本科',4,'admin','2026-01-11 15:41:44','',NULL,'0',NULL),
(204,101,'计算机科学与技术','本科',4,'admin','2026-01-11 15:41:44','',NULL,'0',NULL),
(205,105,'商务英语','本科',4,'admin','2026-01-11 15:41:44','',NULL,'0',NULL);

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
  `ethnicity` varchar(20) DEFAULT NULL COMMENT '民族 (如: 汉族)',
  `hometown` varchar(50) DEFAULT NULL COMMENT '籍贯 (如: 江苏南京)',
  `birth_date` date DEFAULT NULL COMMENT '出生日期',
  `political_status` varchar(20) DEFAULT '群众' COMMENT '政治面貌',
  `email` varchar(50) DEFAULT NULL COMMENT '电子邮箱',
  `landline` varchar(20) DEFAULT NULL COMMENT '座机/家庭电话',
  `emergency_contact` varchar(50) DEFAULT NULL COMMENT '紧急联系人姓名',
  `emergency_phone` varchar(20) DEFAULT NULL COMMENT '紧急联系人电话',
  `emergency_relation` varchar(20) DEFAULT NULL COMMENT '与本人关系',
  `residence_type` int DEFAULT '0' COMMENT '居住类型: 0-住校 1-校外',
  `current_address` varchar(255) DEFAULT NULL COMMENT '当前居住地址',
  `entry_date` date DEFAULT NULL COMMENT '入学/入职时间',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stuno` (`username`),
  CONSTRAINT `chk_user_sex` CHECK ((`sex` in (1,2)))
) ENGINE=InnoDB AUTO_INCREMENT=10145 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='普通用户表 (学生/教职工)';

/*Data for the table `sys_ordinary_user` */

insert  into `sys_ordinary_user`(`id`,`username`,`password`,`real_name`,`phone`,`user_category`,`sex`,`college_id`,`major_id`,`class_id`,`id_card`,`status`,`ethnicity`,`hometown`,`birth_date`,`political_status`,`email`,`landline`,`emergency_contact`,`emergency_phone`,`emergency_relation`,`residence_type`,`current_address`,`entry_date`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(10085,'软工2401_1543','123456','赵敏3',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10086,'软工2401_7971','123456','张伟a',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10087,'软工2401_3650','123456','赵敏r',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10088,'软工2401_9141','123456','张伟W',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10089,'软工2401_9158','123456','孙杰3',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10090,'软工2401_3576','123456','刘波U',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10091,'软工2401_4692','123456','王强d',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10092,'软工2401_5022','123456','孙杰X',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10093,'软工2401_3167','123456','孙杰r',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10094,'软工2401_6888','123456','王强h',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10095,'软工2401_7811','123456','刘波9',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10096,'软工2401_6256','123456','刘波a',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10097,'软工2401_3507','123456','王强8',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10098,'软工2401_1218','123456','刘波u',NULL,0,1,101,201,10303,NULL,'0','回族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10099,'软工2401_8782','123456','李军t',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10100,'软工2401_8695','123456','王强8',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10101,'软工2401_7800','123456','周洋n',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10102,'软工2401_8629','123456','赵敏x',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10103,'软工2401_3326','123456','周洋f',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10104,'软工2401_5728','123456','陈涛Y',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10105,'软工2401_1862','123456','刘波0',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10106,'软工2401_8859','123456','赵敏K',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10107,'软工2401_7945','123456','李军j',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10108,'软工2401_9485','123456','赵敏5',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10109,'软工2401_3184','123456','张伟Q',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10110,'软工2401_6311','123456','陈涛7',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10111,'软工2401_1427','123456','孙杰P',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10112,'软工2401_6187','123456','刘波a',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10113,'软工2401_4670','123456','赵敏m',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10114,'软工2401_6458','123456','孙杰G',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10115,'软工2401_2721','123456','孙杰k',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10116,'软工2401_0141','123456','王强3',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10117,'软工2401_4577','123456','张伟S',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10118,'软工2401_2302','123456','陈涛N',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10119,'软工2401_0318','123456','李军h',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10120,'软工2401_1178','123456','王强q',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10121,'软工2401_3035','123456','孙杰b',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10122,'软工2401_6694','123456','王强7',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10123,'软工2401_3546','123456','张伟9',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10124,'软工2401_1809','123456','张伟F',NULL,0,1,101,201,10303,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10125,'会计2401_2628','123456','李娜t',NULL,0,2,103,203,10306,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10126,'会计2401_6761','123456','陈雪S',NULL,0,2,103,203,10306,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10127,'会计2401_7511','123456','刘婷x',NULL,0,2,103,203,10306,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10128,'会计2401_2265','123456','孙悦x',NULL,0,2,103,203,10306,NULL,'0','回族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10129,'会计2401_8466','123456','杨柳V',NULL,0,2,103,203,10306,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10130,'会计2401_1358','123456','李娜7',NULL,0,2,103,203,10306,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10131,'会计2401_0193','123456','孙悦i',NULL,0,2,103,203,10306,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10132,'会计2401_5224','123456','杨柳U',NULL,0,2,103,203,10306,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10133,'会计2401_1913','123456','李娜t',NULL,0,2,103,203,10306,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10134,'会计2401_0053','123456','王静3',NULL,0,2,103,203,10306,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10135,'会计2401_5618','123456','王静U',NULL,0,2,103,203,10306,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10136,'会计2401_3106','123456','李娜t',NULL,0,2,103,203,10306,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10137,'会计2401_1361','123456','刘婷R',NULL,0,2,103,203,10306,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10138,'会计2401_1967','123456','赵燕A',NULL,0,2,103,203,10306,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10139,'会计2401_7328','123456','孙悦q',NULL,0,2,103,203,10306,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10140,'会计2401_2927','123456','王静p',NULL,0,2,103,203,10306,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10141,'会计2401_8649','123456','赵燕u',NULL,0,2,103,203,10306,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10142,'会计2401_7955','123456','孙悦l',NULL,0,2,103,203,10306,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10143,'会计2401_9019','123456','陈雪H',NULL,0,2,103,203,10306,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL),
(10144,'会计2401_9820','123456','刘婷P',NULL,0,2,103,203,10306,NULL,'0','汉族',NULL,NULL,'群众',NULL,NULL,NULL,NULL,NULL,0,NULL,'2026-01-12','-1','2026-01-12 01:01:38','-1','2026-01-12 01:01:38','0',NULL);

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
(1,'超级管理员','super_admin','0','admin','2026-01-11 15:41:44','',NULL,'0',NULL),
(2,'宿管经理','dorm_manager','0','admin','2026-01-11 15:41:44','',NULL,'0',NULL),
(3,'辅导员','counselor','0','admin','2026-01-11 15:41:44','',NULL,'0',NULL),
(4,'学生','student','0','admin','2026-01-11 15:41:44','',NULL,'0',NULL);

/*Table structure for table `sys_user_role` */

DROP TABLE IF EXISTS `sys_user_role`;

CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户与角色关联表 (多对多)';

/*Data for the table `sys_user_role` */

insert  into `sys_user_role`(`user_id`,`role_id`) values 
(1,1),
(2,2),
(10001,4),
(10002,4),
(10003,4),
(10004,4),
(10005,4),
(10006,4),
(10007,4),
(10008,4),
(10009,4),
(10010,4),
(10011,4),
(10012,4),
(10013,4),
(10014,4),
(10015,4),
(10016,4),
(10017,4),
(10018,4),
(10019,4),
(10020,4);

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

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
