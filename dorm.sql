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
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `fk_class_major` (`major_id`),
  CONSTRAINT `fk_class_major` FOREIGN KEY (`major_id`) REFERENCES `sys_major` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=400 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础信息-班级表';

/*Data for the table `biz_class` */

insert  into `biz_class`(`id`,`major_id`,`grade`,`class_name`,`counselor_id`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(300,200,2024,'测试班级',NULL,'0','admin','2026-01-14 10:11:38','',NULL,NULL);

/*Table structure for table `biz_electric_violation_log` */

DROP TABLE IF EXISTS `biz_electric_violation_log`;

CREATE TABLE `biz_electric_violation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `violation_type` int NOT NULL COMMENT '违规类型: 1-功率超载 2-恶性负载(阻性) 3-夜间不归',
  `power_val` decimal(10,2) DEFAULT NULL COMMENT '违规时的功率(W)',
  `detected_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',
  `create_by` varchar(64) DEFAULT 'system',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL COMMENT '处理结果备注',
  PRIMARY KEY (`id`),
  KEY `fk_violation_room` (`room_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='违规用电/安全警报日志';

/*Data for the table `biz_electric_violation_log` */

insert  into `biz_electric_violation_log`(`id`,`room_id`,`violation_type`,`power_val`,`detected_time`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,101,2,1500.00,'2026-01-13 15:02:59','system','2026-01-13 15:02:59','',NULL,'0','检测到疑似热得快，已自动断电'),
(2,102,1,3200.00,'2026-01-13 15:02:59','system','2026-01-13 15:02:59','',NULL,'0','总功率超限，请检查大功率电器');

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
  `status` int DEFAULT '0' COMMENT '状态',
  `audit_msg` varchar(255) DEFAULT NULL COMMENT '审批意见',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-假期留校表';

/*Data for the table `biz_holiday_stay` */

insert  into `biz_holiday_stay`(`id`,`student_id`,`start_date`,`end_date`,`reason`,`emergency_name`,`emergency_relation`,`emergency_phone`,`status`,`audit_msg`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(10,1002,'2026-01-15','2026-02-10','实验室项目科研','李父','父亲','13800000000',0,NULL,'0','admin','2026-01-13 14:52:42','',NULL,NULL);

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
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-报修工单表';

/*Data for the table `biz_repair_order` */

insert  into `biz_repair_order`(`id`,`order_no`,`room_id`,`applicant_id`,`description`,`status`,`images`,`repairman_id`,`finish_time`,`rating`,`comment`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(10,'R2026010101',101,1001,'空调漏水严重',0,NULL,NULL,NULL,NULL,NULL,'0','admin','2026-01-13 14:52:42','',NULL,NULL),
(11,'R2026010102',102,1005,'阳台门把手掉了',2,NULL,NULL,NULL,NULL,NULL,'0','admin','2026-01-13 14:52:42','',NULL,NULL);

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
  `create_by` varchar(64) DEFAULT 'system',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户全维度画像表';

/*Data for the table `biz_user_preference` */

insert  into `biz_user_preference`(`user_id`,`team_code`,`bed_time`,`wake_time`,`siesta_habit`,`out_late_freq`,`sleep_quality`,`snoring_level`,`grinding_teeth`,`sleep_talk`,`climb_bed_noise`,`shower_freq`,`sock_wash`,`trash_habit`,`clean_freq`,`toilet_clean`,`desk_messy`,`personal_hygiene`,`odor_tolerance`,`smoking`,`smoke_tolerance`,`drinking`,`ac_temp`,`ac_duration`,`game_type_lol`,`game_type_fps`,`game_type_3a`,`game_type_mmo`,`game_type_mobile`,`game_habit`,`game_voice`,`keyboard_axis`,`is_cosplay`,`is_anime`,`mbti_e_i`,`mbti_result`,`social_battery`,`share_items`,`bring_guest`,`visitors`,`relationship_status`,`has_disability`,`has_insulin`,`has_infectious`,`religion_taboo`,`special_disease`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1001,NULL,3,3,0,0,2,0,0,0,0,1,0,1,2,1,2,3,2,2,1,0,26,1,0,0,0,0,0,0,1,0,0,0,'E',NULL,3,1,1,0,0,0,0,0,NULL,NULL,'-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38','0',NULL),
(1002,NULL,1,1,0,0,2,0,0,0,0,1,0,1,2,1,2,3,2,0,0,0,26,1,0,0,0,0,0,0,1,0,0,0,'E',NULL,3,1,1,0,0,0,0,0,NULL,NULL,'-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38','0',NULL),
(1003,NULL,6,6,0,0,2,0,0,0,0,1,0,1,2,1,2,3,2,0,1,0,26,1,0,0,0,0,0,0,1,3,0,0,'E',NULL,3,1,1,0,0,0,0,0,NULL,NULL,'-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38','0',NULL),
(1004,NULL,6,6,0,0,2,0,0,0,0,1,0,1,2,1,2,3,2,0,1,0,26,1,0,0,0,0,0,0,1,3,0,0,'E',NULL,3,1,1,0,0,0,0,0,NULL,NULL,'-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38','0',NULL),
(1005,'TEAM_XB',3,3,0,0,2,0,0,0,0,1,0,1,2,1,2,3,2,0,1,0,26,1,0,0,0,0,0,0,1,1,0,0,'E',NULL,3,1,1,0,0,0,0,0,NULL,NULL,'-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38','0',NULL),
(1006,'TEAM_XB',3,3,0,0,2,0,0,0,0,1,0,1,2,1,2,3,2,0,1,0,26,1,0,0,0,0,0,0,1,1,0,0,'E',NULL,3,1,1,0,0,0,0,0,NULL,NULL,'-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38','0',NULL);

/*Table structure for table `biz_utility_bill` */

DROP TABLE IF EXISTS `biz_utility_bill`;

CREATE TABLE `biz_utility_bill` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `month` varchar(7) NOT NULL COMMENT '月份',
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
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-水电账单表';

/*Data for the table `biz_utility_bill` */

insert  into `biz_utility_bill`(`id`,`room_id`,`month`,`water_cold`,`water_hot`,`elec_light`,`elec_ac`,`cost_water`,`cost_elec`,`total_amount`,`status`,`pay_time`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`,`version`) values 
(10,101,'2026-01',0.00,0.00,0.00,0.00,0.00,0.00,120.50,0,NULL,'0','admin','2026-01-13 14:52:42','',NULL,NULL,1),
(11,102,'2026-01',0.00,0.00,0.00,0.00,0.00,0.00,88.00,1,NULL,'0','admin','2026-01-13 14:52:42','',NULL,NULL,1);

/*Table structure for table `dorm_bed` */

DROP TABLE IF EXISTS `dorm_bed`;

CREATE TABLE `dorm_bed` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '所属房间ID',
  `bed_label` varchar(20) NOT NULL COMMENT '床位号(如:1号床)',
  `occupant_id` bigint DEFAULT NULL COMMENT '居住者ID',
  `status` tinyint DEFAULT '0' COMMENT '状态',
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
) ENGINE=InnoDB AUTO_INCREMENT=1046 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-床位表';

/*Data for the table `dorm_bed` */

insert  into `dorm_bed`(`id`,`room_id`,`bed_label`,`occupant_id`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`,`version`) values 
(1038,309,'101-1',NULL,0,'0','-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38',NULL,1),
(1039,309,'101-2',NULL,0,'0','-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38',NULL,1),
(1040,309,'101-3',NULL,0,'0','-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38',NULL,1),
(1041,309,'101-4',NULL,0,'0','-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38',NULL,1),
(1042,310,'102-1',NULL,0,'0','-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38',NULL,1),
(1043,310,'102-2',NULL,0,'0','-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38',NULL,1),
(1044,310,'102-3',NULL,0,'0','-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38',NULL,1),
(1045,310,'102-4',NULL,0,'0','-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38',NULL,1);

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
  PRIMARY KEY (`id`),
  KEY `fk_building_campus` (`campus_id`),
  CONSTRAINT `fk_building_campus` FOREIGN KEY (`campus_id`) REFERENCES `sys_campus` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-楼栋表';

/*Data for the table `dorm_building` */

insert  into `dorm_building`(`id`,`campus_id`,`name`,`type`,`floors`,`has_elevator`,`power_limit`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(23,2011380679107518466,'智能分配楼',1,1,0,1000,1,'0','-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38',NULL);

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
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务-调宿申请表';

/*Data for the table `dorm_change_request` */

insert  into `dorm_change_request`(`id`,`student_id`,`current_room_id`,`target_room_id`,`type`,`reason`,`status`,`swap_student_id`,`audit_msg`,`apply_time`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(10,1001,101,NULL,0,'室友睡觉磨牙，想换个安静的',0,NULL,NULL,'2026-01-13 14:52:42','0','admin','2026-01-13 14:52:42','',NULL,NULL);

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
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_asset_code` (`asset_code`),
  KEY `fk_asset_room` (`room_id`)
) ENGINE=InnoDB AUTO_INCREMENT=90020 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资产管理表';

/*Data for the table `dorm_fixed_asset` */

insert  into `dorm_fixed_asset`(`id`,`asset_name`,`asset_code`,`category`,`room_id`,`price`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(90000,'实木双层床架','FUR-101-BED-01',1,101,1200.00,1,'admin','2026-01-13 14:56:58','',NULL,'0',NULL),
(90001,'实木双层床架','FUR-101-BED-02',1,101,1200.00,1,'admin','2026-01-13 14:56:58','',NULL,'0',NULL),
(90002,'松木床板','FUR-101-BD-01',1,101,150.00,1,'admin','2026-01-13 14:56:58','',NULL,'0',NULL),
(90003,'松木床板','FUR-101-BD-02',1,101,150.00,1,'admin','2026-01-13 14:56:58','',NULL,'0',NULL),
(90004,'松木床板','FUR-101-BD-03',1,101,150.00,1,'admin','2026-01-13 14:56:58','',NULL,'0',NULL),
(90005,'松木床板','FUR-101-BD-04',1,101,150.00,1,'admin','2026-01-13 14:56:58','',NULL,'0',NULL),
(90006,'组合书桌椅','FUR-101-DSK-01',1,101,450.00,1,'admin','2026-01-13 14:56:58','',NULL,'0',NULL),
(90007,'组合书桌椅','FUR-101-DSK-02',1,101,450.00,1,'admin','2026-01-13 14:56:58','',NULL,'0',NULL),
(90008,'组合书桌椅','FUR-101-DSK-03',1,101,450.00,1,'admin','2026-01-13 14:56:58','',NULL,'0',NULL),
(90009,'组合书桌椅','FUR-101-DSK-04',1,101,450.00,1,'admin','2026-01-13 14:56:58','',NULL,'0',NULL),
(90010,'独立衣柜','FUR-101-CAB-01',1,101,600.00,1,'admin','2026-01-13 14:56:58','',NULL,'0',NULL),
(90011,'独立衣柜','FUR-101-CAB-02',1,101,600.00,1,'admin','2026-01-13 14:56:58','',NULL,'0',NULL),
(90012,'独立衣柜','FUR-101-CAB-03',1,101,600.00,1,'admin','2026-01-13 14:56:58','',NULL,'0',NULL),
(90013,'独立衣柜','FUR-101-CAB-04',1,101,600.00,1,'admin','2026-01-13 14:56:58','',NULL,'0',NULL),
(90014,'格力空调(1.5匹)','APP-101-AC',2,101,3500.00,1,'admin','2026-01-13 14:56:58','',NULL,'0',NULL),
(90015,'海尔电热水器','APP-101-HEAT',2,101,1200.00,1,'admin','2026-01-13 14:56:58','',NULL,'0',NULL),
(90016,'吸顶灯','APP-101-LGT',2,101,80.00,1,'admin','2026-01-13 14:56:58','',NULL,'0',NULL),
(90017,'防盗大门','INF-101-DOOR',3,101,2000.00,1,'admin','2026-01-13 14:56:58','',NULL,'0',NULL),
(90018,'大理石洗漱台','INF-101-SINK',3,101,800.00,1,'admin','2026-01-13 14:56:58','',NULL,'0',NULL),
(90019,'阳台推拉门','INF-101-BAL',3,101,1500.00,1,'admin','2026-01-13 14:56:58','',NULL,'0',NULL);

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
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-楼层表';

/*Data for the table `dorm_floor` */

insert  into `dorm_floor`(`id`,`building_id`,`floor_num`,`gender_limit`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(53,23,1,1,'0','-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38',NULL);

/*Table structure for table `dorm_meter_electric` */

DROP TABLE IF EXISTS `dorm_meter_electric`;

CREATE TABLE `dorm_meter_electric` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `meter_no` varchar(50) NOT NULL,
  `current_reading` decimal(10,2) DEFAULT '0.00',
  `balance` decimal(10,2) DEFAULT '0.00',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_e` (`room_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `dorm_meter_electric` */

insert  into `dorm_meter_electric`(`id`,`room_id`,`meter_no`,`current_reading`,`balance`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,'E101',120.50,50.00,'admin','2026-01-13 14:50:11','',NULL,'0',NULL),
(2,2,'E102',80.00,20.00,'admin','2026-01-13 14:50:11','',NULL,'0',NULL),
(3,7,'E3-101',50.00,100.00,'admin','2026-01-13 14:50:11','',NULL,'0',NULL);

/*Table structure for table `dorm_meter_water` */

DROP TABLE IF EXISTS `dorm_meter_water`;

CREATE TABLE `dorm_meter_water` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `meter_no` varchar(50) NOT NULL,
  `current_reading` decimal(10,2) DEFAULT '0.00',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_w` (`room_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `dorm_meter_water` */

insert  into `dorm_meter_water`(`id`,`room_id`,`meter_no`,`current_reading`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,1,'W101',30.50,'admin','2026-01-13 14:50:11','',NULL,'0',NULL),
(2,2,'W102',15.00,'admin','2026-01-13 14:50:11','',NULL,'0',NULL),
(3,7,'W3-101',10.00,'admin','2026-01-13 14:50:11','',NULL,'0',NULL);

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
  `floor_id` bigint DEFAULT NULL COMMENT '所属楼层ID',
  `floor_no` int NOT NULL COMMENT '楼层号(冗余)',
  `room_no` varchar(20) NOT NULL COMMENT '房间号',
  `capacity` int DEFAULT '4' COMMENT '床位数',
  `current_num` int DEFAULT '0' COMMENT '实住人数',
  `gender` int DEFAULT '1' COMMENT '性别 (1:男 2:女)',
  `status` int DEFAULT '1' COMMENT '状态 (1:正常 0:维修 2:满员)',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `version` int DEFAULT '1' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  KEY `fk_room_building` (`building_id`),
  KEY `fk_room_floor` (`floor_id`),
  CONSTRAINT `fk_room_building` FOREIGN KEY (`building_id`) REFERENCES `dorm_building` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_room_floor` FOREIGN KEY (`floor_id`) REFERENCES `dorm_floor` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=311 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍-房间表';

/*Data for the table `dorm_room` */

insert  into `dorm_room`(`id`,`building_id`,`floor_id`,`floor_no`,`room_no`,`capacity`,`current_num`,`gender`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`,`version`) values 
(309,23,53,1,'101',4,0,1,1,'0','-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38',NULL,1),
(310,23,53,1,'102',4,0,1,1,'0','-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38',NULL,1);

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
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-管理员表';

/*Data for the table `sys_admin_user` */

insert  into `sys_admin_user`(`id`,`username`,`password`,`real_name`,`avatar`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(10,'admin','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','超级管理员',NULL,'0','0','admin','2026-01-13 14:52:42','',NULL,NULL),
(11,'manager','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','宿管张阿姨',NULL,'0','0','admin','2026-01-13 14:52:42','',NULL,NULL),
(12,'counselor','$2a$10$fcVDpa5xQ0TqHnywFBCheu10/EqykWFJsbQrl5AWvPKEFYg009HwC','李辅导员',NULL,'0','0','admin','2026-01-13 14:52:42','',NULL,NULL);

/*Table structure for table `sys_algorithm_config` */

DROP TABLE IF EXISTS `sys_algorithm_config`;

CREATE TABLE `sys_algorithm_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键 (如: weight_sleep_time)',
  `config_value` varchar(100) NOT NULL COMMENT '配置值 (如: 0.8)',
  `description` varchar(255) DEFAULT NULL COMMENT '配置说明',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统核心参数配置表';

/*Data for the table `sys_algorithm_config` */

insert  into `sys_algorithm_config`(`id`,`config_key`,`config_value`,`description`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,'algo_weight_schedule','0.8','作息时间匹配权重(0-1)','admin','2026-01-13 15:02:51','',NULL,'0',NULL),
(2,'algo_weight_habit','0.6','生活习惯匹配权重(吸烟/卫生)','admin','2026-01-13 15:02:51','',NULL,'0',NULL),
(3,'algo_weight_game','0.5','游戏兴趣聚合权重','admin','2026-01-13 15:02:51','',NULL,'0',NULL),
(4,'sys_open_date','2026-08-20','新生选房系统开放时间','admin','2026-01-13 15:02:51','',NULL,'0',NULL),
(5,'sys_close_date','2026-09-01','新生选房系统关闭时间','admin','2026-01-13 15:02:51','',NULL,'0',NULL);

/*Table structure for table `sys_campus` */

DROP TABLE IF EXISTS `sys_campus`;

CREATE TABLE `sys_campus` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_name` varchar(100) NOT NULL COMMENT '校区名称',
  `campus_code` varchar(20) NOT NULL COMMENT '校区编码',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `status` char(1) DEFAULT '0' COMMENT '状态 (0:正常 1:停用)',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除 (0:未删 1:已删)',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2011380679107518467 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础信息-校区表';

/*Data for the table `sys_campus` */

insert  into `sys_campus`(`id`,`campus_name`,`campus_code`,`address`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(2011380679107518466,'测试校区','TEST',NULL,'0','0','-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38',NULL);

/*Table structure for table `sys_college` */

DROP TABLE IF EXISTS `sys_college`;

CREATE TABLE `sys_college` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `campus_id` bigint NOT NULL COMMENT '所属校区ID',
  `name` varchar(50) NOT NULL COMMENT '学院名称',
  `code` varchar(20) NOT NULL COMMENT '学院代码',
  `sort` int DEFAULT '0' COMMENT '排序',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'admin' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `description` varchar(500) DEFAULT NULL COMMENT '学院简介',
  PRIMARY KEY (`id`),
  KEY `fk_college_campus` (`campus_id`),
  CONSTRAINT `fk_college_campus` FOREIGN KEY (`campus_id`) REFERENCES `sys_campus` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=200 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础信息-二级学院表';

/*Data for the table `sys_college` */

insert  into `sys_college`(`id`,`campus_id`,`name`,`code`,`sort`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`,`description`) values 
(100,2011380679107518466,'测试学院','TC',0,'0','admin','2026-01-14 10:11:38','',NULL,NULL,NULL);

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
) ENGINE=InnoDB AUTO_INCREMENT=300 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础信息-专业表';

/*Data for the table `sys_major` */

insert  into `sys_major`(`id`,`college_id`,`name`,`level`,`duration`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`,`sort`) values 
(200,100,'测试专业','本科',4,'0','admin','2026-01-14 10:11:38','',NULL,NULL,0);

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
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-通知公告表';

/*Data for the table `sys_notice` */

insert  into `sys_notice`(`id`,`title`,`content`,`type`,`level`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(10,'2026年寒假放假安全须知','离校前请关闭电源...',1,0,'0','0','admin','2026-01-13 14:52:42','',NULL,NULL),
(11,'关于开展宿舍卫生大检查的通知','本周三下午进行检查...',2,0,'0','0','admin','2026-01-13 14:52:42','',NULL,NULL);

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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `fk_user_class` (`class_id`),
  CONSTRAINT `fk_user_class` FOREIGN KEY (`class_id`) REFERENCES `biz_class` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统-普通用户表';

/*Data for the table `sys_ordinary_user` */

insert  into `sys_ordinary_user`(`id`,`username`,`password`,`real_name`,`avatar`,`user_category`,`sex`,`phone`,`college_id`,`major_id`,`class_id`,`residence_type`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`,`id_card`) values 
(1001,'1001','123','老烟枪',NULL,0,1,NULL,100,200,300,0,'0','0','-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38',NULL,NULL),
(1002,'1002','123','养生哥',NULL,0,1,NULL,100,200,300,0,'0','0','-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38',NULL,NULL),
(1003,'1003','123','五杀王',NULL,0,1,NULL,100,200,300,0,'0','0','-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38',NULL,NULL),
(1004,'1004','123','辅助哥',NULL,0,1,NULL,100,200,300,0,'0','0','-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38',NULL,NULL),
(1005,'1005','123','死党A',NULL,0,1,NULL,100,200,300,0,'0','0','-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38',NULL,NULL),
(1006,'1006','123','死党B',NULL,0,1,NULL,100,200,300,0,'0','0','-1','2026-01-14 18:11:38','-1','2026-01-14 18:11:38',NULL,NULL);

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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `sys_role` */

insert  into `sys_role`(`id`,`role_name`,`role_key`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,'超级管理员','super_admin','0','0','admin','2026-01-13 14:52:42','',NULL,NULL),
(2,'宿管经理','dorm_manager','0','0','admin','2026-01-13 14:52:42','',NULL,NULL),
(3,'辅导员','counselor','0','0','admin','2026-01-13 14:52:42','',NULL,NULL),
(4,'学生','student','0','0','admin','2026-01-13 14:52:42','',NULL,NULL);

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
(1006,4,'2026-01-13 16:02:37'),
(1007,4,'2026-01-13 16:02:37'),
(1008,4,'2026-01-13 16:02:37'),
(1009,4,'2026-01-13 16:02:37'),
(1010,4,'2026-01-13 16:02:37');

/*Table structure for table `sys_utility_price` */

DROP TABLE IF EXISTS `sys_utility_price`;

CREATE TABLE `sys_utility_price` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` int NOT NULL COMMENT '类型: 1-水 2-电',
  `name` varchar(50) NOT NULL COMMENT '费用名称 (如: 居民用电)',
  `price` decimal(10,4) NOT NULL COMMENT '单价 (元)',
  `unit` varchar(10) DEFAULT NULL COMMENT '单位 (度/吨)',
  `create_by` varchar(64) DEFAULT 'admin',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` char(1) DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='水电费单价配置';

/*Data for the table `sys_utility_price` */

insert  into `sys_utility_price`(`id`,`type`,`name`,`price`,`unit`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`) values 
(1,2,'基础电费',0.6000,'度','admin','2026-01-13 14:55:07','',NULL,'0',NULL),
(2,2,'空调专线',0.9000,'度','admin','2026-01-13 14:55:07','',NULL,'0',NULL),
(3,1,'冷水费',3.5000,'吨','admin','2026-01-13 14:55:07','',NULL,'0',NULL),
(4,1,'热水费',15.0000,'吨','admin','2026-01-13 14:55:07','',NULL,'0',NULL);

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
