-- MySQL dump 10.13  Distrib 8.0.46, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: wuye_points
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `activity_group_members`
--

DROP TABLE IF EXISTS `activity_group_members`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity_group_members` (
  `id` varchar(32) NOT NULL,
  `group_id` varchar(32) NOT NULL,
  `resident_id` varchar(32) NOT NULL,
  `role` varchar(20) DEFAULT 'member' COMMENT 'leader/member',
  `joined_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_resident` (`group_id`,`resident_id`),
  KEY `idx_group` (`group_id`),
  KEY `idx_resident` (`resident_id`),
  CONSTRAINT `activity_group_members_ibfk_1` FOREIGN KEY (`group_id`) REFERENCES `activity_groups` (`id`) ON DELETE CASCADE,
  CONSTRAINT `activity_group_members_ibfk_2` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='活动组成员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity_group_members`
--

LOCK TABLES `activity_group_members` WRITE;
/*!40000 ALTER TABLE `activity_group_members` DISABLE KEYS */;
INSERT INTO `activity_group_members` VALUES ('agm_073c1000','act_65101000','res_dev001','member','2026-07-04 07:24:34','active','2026-07-04 07:24:34','2026-07-04 07:24:34'),('agm_65141000','act_65101000','res_dev002','leader','2026-07-04 02:06:22','active','2026-07-04 02:06:22','2026-07-04 02:06:22'),('agm_733e1000','act_74601000','res_dev001','member','2026-07-04 08:44:32','active','2026-07-04 08:44:32','2026-07-04 08:44:32'),('agm_74801000','act_74601000','res_dev002','leader','2026-07-04 02:03:40','active','2026-07-04 02:03:40','2026-07-04 02:03:40'),('agm_b3a41000','act_74601000','res_48fa1000','member','2026-07-04 09:04:53','active','2026-07-04 09:04:53','2026-07-04 09:04:53'),('agm_dev001','ag_dev001','res_dev001','member','2026-07-03 06:23:28','active','2026-07-03 06:23:28','2026-07-03 06:23:28'),('agm_dev002','ag_dev001','res_dev002','member','2026-07-03 06:23:28','active','2026-07-03 06:23:28','2026-07-03 06:23:28'),('agm_dev003','ag_dev002','res_dev003','member','2026-07-03 06:23:28','active','2026-07-03 06:23:28','2026-07-03 06:23:28'),('agm_e81e1000','act_65101000','res_48fa1000','member','2026-07-04 09:04:27','active','2026-07-04 09:04:27','2026-07-04 09:04:27');
/*!40000 ALTER TABLE `activity_group_members` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activity_group_subscriptions`
--

DROP TABLE IF EXISTS `activity_group_subscriptions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity_group_subscriptions` (
  `id` varchar(32) NOT NULL,
  `group_id` varchar(32) NOT NULL,
  `resident_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `sub_type` varchar(20) NOT NULL COMMENT 'monthly/yearly',
  `amount` decimal(10,2) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `property_company_id` (`property_company_id`),
  KEY `idx_group` (`group_id`),
  KEY `idx_resident` (`resident_id`),
  KEY `idx_end_date` (`end_date`),
  CONSTRAINT `activity_group_subscriptions_ibfk_1` FOREIGN KEY (`group_id`) REFERENCES `activity_groups` (`id`),
  CONSTRAINT `activity_group_subscriptions_ibfk_2` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `activity_group_subscriptions_ibfk_3` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='活动组订阅表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity_group_subscriptions`
--

LOCK TABLES `activity_group_subscriptions` WRITE;
/*!40000 ALTER TABLE `activity_group_subscriptions` DISABLE KEYS */;
INSERT INTO `activity_group_subscriptions` VALUES ('ags_dev001','ag_dev001','res_dev001','pc_demo001','yearly',0.00,'2026-01-01','2026-12-31','active','2026-07-03 06:23:28','2026-07-03 06:23:28'),('ags_dev002','ag_dev002','res_dev003','pc_demo001','monthly',30.00,'2026-07-01','2026-07-31','active','2026-07-03 06:23:28','2026-07-03 06:23:28');
/*!40000 ALTER TABLE `activity_group_subscriptions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activity_groups`
--

DROP TABLE IF EXISTS `activity_groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity_groups` (
  `id` varchar(32) NOT NULL COMMENT 'act_xxxxxxxx',
  `name` varchar(100) NOT NULL,
  `description` text,
  `cover_url` varchar(500) DEFAULT NULL,
  `leader_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `community_id` varchar(32) DEFAULT NULL,
  `member_count` int DEFAULT '0',
  `monthly_fee` decimal(10,2) DEFAULT '0.00' COMMENT '月费',
  `yearly_fee` decimal(10,2) DEFAULT '0.00' COMMENT '年费',
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_leader` (`leader_id`),
  KEY `idx_property_company` (`property_company_id`),
  CONSTRAINT `activity_groups_ibfk_1` FOREIGN KEY (`leader_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `activity_groups_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='活动组表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity_groups`
--

LOCK TABLES `activity_groups` WRITE;
/*!40000 ALTER TABLE `activity_groups` DISABLE KEYS */;
INSERT INTO `activity_groups` VALUES ('act_65101000','更新组名','每周六晨跑',NULL,'res_dev002','pc_demo001','com_demo001',3,0.00,0.00,'active','2026-07-04 02:06:22','2026-07-04 09:04:27'),('act_74601000','周末跑步组','每周六晨跑',NULL,'res_dev002','pc_demo001','com_demo001',3,0.00,0.00,'active','2026-07-04 02:03:40','2026-07-04 09:04:53'),('ag_admin001','广场舞队','每晚7点中心广场广场舞，欢迎加入',NULL,'res_admin_test04','pc_demo001','com_demo001',35,0.00,0.00,'active','2026-07-03 08:02:56','2026-07-03 08:02:56'),('ag_admin002','围棋社','每周六下午围棋交流活动',NULL,'res_admin_test07','pc_demo001','com_demo002',18,20.00,200.00,'active','2026-07-03 08:02:56','2026-07-03 08:02:56'),('ag_admin003','摄影爱好者','社区摄影交流群，定期组织外拍',NULL,'res_act01','pc_demo001','com_demo001',42,0.00,0.00,'active','2026-07-03 08:02:56','2026-07-03 08:02:56'),('ag_admin004','闲置活动组','已废弃的活动组',NULL,'res_act01','pc_demo001','com_demo001',0,0.00,0.00,'inactive','2026-07-03 08:02:56','2026-07-03 08:02:56'),('ag_dev001','太极拳晨练组','每日早晨7:30在中心广场练习太极拳，强身健体，老少皆宜',NULL,'res_act01','pc_demo001','com_demo001',28,0.00,0.00,'active','2026-07-03 06:23:28','2026-07-03 06:23:28'),('ag_dev002','篮球赛','周末篮球友谊赛，新手友好，户外运动',NULL,'res_dev003','pc_demo001','com_demo001',128,30.00,300.00,'active','2026-07-03 06:23:28','2026-07-03 06:23:28'),('ag_dev003','插花课','每周插花课程，学习花艺，陶冶情操',NULL,'res_002','pc_demo001','com_demo001',56,45.00,450.00,'active','2026-07-03 06:23:28','2026-07-03 06:23:28'),('ag_dev004','书友会','社区读书交流会，每月一次线下分享',NULL,'res_003','pc_demo001','com_demo001',210,0.00,0.00,'active','2026-07-03 06:23:28','2026-07-03 06:23:28'),('ag_dev005','太极拳','太极拳进阶班，室内教学',NULL,'res_act01','pc_demo001','com_demo001',84,30.00,300.00,'active','2026-07-03 06:23:28','2026-07-03 06:23:28');
/*!40000 ALTER TABLE `activity_groups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `announcement_collects`
--

DROP TABLE IF EXISTS `announcement_collects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `announcement_collects` (
  `id` varchar(32) NOT NULL,
  `announcement_id` varchar(32) NOT NULL,
  `resident_id` varchar(32) NOT NULL,
  `collected_data` json NOT NULL COMMENT '业主填写的资料',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_announcement_resident` (`announcement_id`,`resident_id`),
  KEY `idx_announcement` (`announcement_id`),
  KEY `idx_resident` (`resident_id`),
  CONSTRAINT `announcement_collects_ibfk_1` FOREIGN KEY (`announcement_id`) REFERENCES `announcements` (`id`),
  CONSTRAINT `announcement_collects_ibfk_2` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公告信息收集表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `announcement_collects`
--

LOCK TABLES `announcement_collects` WRITE;
/*!40000 ALTER TABLE `announcement_collects` DISABLE KEYS */;
INSERT INTO `announcement_collects` VALUES ('ac_admin001','ann_003','res_dev001','{\"name\": \"张阿姨\", \"phone\": \"13800138000\", \"course\": \"书法\", \"child_age\": 8, \"child_name\": \"张子涵\"}','2026-07-02 06:00:00','2026-07-03 08:02:56'),('ac_admin002','ann_003','res_admin_test04','{\"name\": \"赵小明\", \"phone\": \"13911110004\", \"course\": \"编程\", \"child_age\": 6, \"child_name\": \"赵小宝\"}','2026-07-02 07:30:00','2026-07-03 08:02:56'),('ac_admin003','ann_003','res_admin_test05','{\"name\": \"钱小红\", \"phone\": \"13911110005\", \"course\": \"绘画\", \"child_age\": 10, \"child_name\": \"钱朵朵\"}','2026-07-02 08:00:00','2026-07-03 08:02:56');
/*!40000 ALTER TABLE `announcement_collects` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `announcements`
--

DROP TABLE IF EXISTS `announcements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `announcements` (
  `id` varchar(32) NOT NULL COMMENT 'ann_xxxxxxxx',
  `title` varchar(200) NOT NULL,
  `content` text NOT NULL,
  `announcement_type` varchar(30) NOT NULL COMMENT 'property/community/platform/system/merchant/activity',
  `property_company_id` varchar(32) DEFAULT NULL,
  `community_id` varchar(32) DEFAULT NULL,
  `merchant_id` varchar(32) DEFAULT NULL,
  `publisher_id` varchar(32) NOT NULL,
  `publisher_role` varchar(30) NOT NULL,
  `cover_urls` json DEFAULT NULL,
  `target_roles` json DEFAULT NULL COMMENT '目标角色',
  `target_buildings` json DEFAULT NULL COMMENT '目标楼栋',
  `collect_enabled` tinyint(1) DEFAULT '0' COMMENT '是否启用信息收集',
  `collect_fields` json DEFAULT NULL COMMENT '收集字段定义',
  `status` varchar(20) DEFAULT 'published' COMMENT 'draft/published/archived',
  `published_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_type` (`announcement_type`),
  KEY `idx_status` (`status`),
  KEY `idx_published_at` (`published_at`),
  CONSTRAINT `announcements_ibfk_1` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公告表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `announcements`
--

LOCK TABLES `announcements` WRITE;
/*!40000 ALTER TABLE `announcements` DISABLE KEYS */;
INSERT INTO `announcements` VALUES ('ann_03621000','测试2','222222','merchant','pc_demo001',NULL,NULL,'res_admin01','platform_admin',NULL,NULL,'[\"1栋\", \"2栋\"]',0,NULL,'published','2026-07-03 06:08:30','2026-07-03 06:08:30','2026-07-03 06:08:30'),('ann_1bf81000','测试 1','1111111111111111111111111111111111111111111111','property','pc_demo001',NULL,NULL,'res_admin01','platform_admin',NULL,NULL,'[\"1栋\", \"2栋\"]',0,NULL,'published','2026-07-03 05:47:15','2026-07-03 05:47:15','2026-07-03 05:47:15'),('ann_8d6e1000','11111','1111111111111111','merchant','pc_demo001',NULL,NULL,'res_admin01','platform_admin',NULL,NULL,'[\"2栋\", \"3栋\"]',0,NULL,'published','2026-07-03 06:09:20','2026-07-03 06:09:20','2026-07-03 06:09:20'),('ann_admin001','2026年7月垃圾分类宣传','垃圾分类，人人有责。请各位业主将生活垃圾分类投放至指定垃圾桶。','community','pc_demo001','com_demo001',NULL,'res_propadmin01','property_admin',NULL,NULL,NULL,0,NULL,'published','2026-07-01 02:00:00','2026-07-03 08:02:56','2026-07-03 08:02:56'),('ann_admin002','物业费缴费优惠活动','凡在7月15日前缴纳Q3物业费的业主，可享受积分双倍奖励！','property','pc_demo001',NULL,NULL,'res_propadmin01','property_admin',NULL,NULL,NULL,0,NULL,'published','2026-07-01 00:00:00','2026-07-03 08:02:56','2026-07-03 08:02:56'),('ann_admin003','关于小区绿化改造的通知','小区将于7月10日至20日进行绿化升级改造，施工期间请注意安全。','property','pc_demo001','com_demo001',NULL,'res_propadmin01','property_admin',NULL,NULL,NULL,0,NULL,'draft',NULL,'2026-07-03 08:02:56','2026-07-03 08:02:56'),('ann_admin004','翠湖名苑电梯检修通知','翠湖名苑将于7月8日进行电梯年度检修，届时将分时段停运。','community','pc_demo001','com_demo002',NULL,'res_propadmin01','property_admin',NULL,NULL,NULL,0,NULL,'published','2026-07-02 01:00:00','2026-07-03 08:02:56','2026-07-03 08:02:56'),('ann_admin005','社区便民服务日','本周末社区中心将举办便民服务日活动，免费理发、义诊等。','activity','pc_demo001',NULL,NULL,'res_coo01','coordinator',NULL,NULL,NULL,0,NULL,'archived','2026-06-15 00:00:00','2026-07-03 08:02:56','2026-07-03 08:02:56'),('ann_c4c41000','测试1','1111111111111111111111111111111111112','property','pc_demo001','com_demo001',NULL,'res_admin01','platform_admin',NULL,NULL,NULL,0,NULL,'published','2026-07-05 05:23:20','2026-07-05 05:23:20','2026-07-05 05:23:20'),('ann_dev001','关于2026年7月物业费缴纳通知','尊敬的业主：2026年第三季度物业费已出账，请于7月31日前完成缴纳。支持微信、物业币、积分抵扣等多种方式。','property','pc_demo001',NULL,NULL,'res_propadmin01','property_admin',NULL,NULL,NULL,0,NULL,'published','2026-07-01 01:00:00','2026-07-03 06:23:28','2026-07-03 06:23:28'),('ann_dev002','阳光花园小区停水通知','因自来水管道检修，7月5日9:00-17:00暂停供水，请提前储水。给您带来不便，敬请谅解。','community','pc_demo001','com_demo001',NULL,'res_propadmin01','property_admin',NULL,NULL,NULL,0,NULL,'published','2026-07-03 00:00:00','2026-07-03 06:23:28','2026-07-03 06:23:28'),('ann_dev003','社区夏令营活动报名开始','暑期社区青少年夏令营开始报名！包含书法、绘画、编程等课程，物业币抵扣50%。名额有限，先到先得。','activity','pc_demo001',NULL,NULL,'res_coo01','coordinator',NULL,NULL,NULL,0,NULL,'published','2026-07-02 02:00:00','2026-07-03 06:23:28','2026-07-03 06:23:28'),('ann_dev004','社区电梯维保完成公示','截止本周五，全区32部客梯已全部完成季度深度维保及质检，各项指标均符合安全运行要求。','property','pc_demo001','com_demo001',NULL,'res_propadmin01','property_admin',NULL,NULL,NULL,0,NULL,'published','2026-06-20 01:00:00','2026-07-03 06:23:28','2026-07-03 06:23:28'),('ann_dev005','邻里跳蚤市场活动招募公告','本周六社区中心广场将举办\"旧物换新颜\"二手市集，欢迎各位邻里踊跃报名设摊或参与交换。','community','pc_demo001','com_demo001',NULL,'res_act01','activity_leader',NULL,NULL,NULL,0,NULL,'published','2026-06-28 02:00:00','2026-07-03 06:23:28','2026-07-03 06:23:28');
/*!40000 ALTER TABLE `announcements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coin_expiry_records`
--

DROP TABLE IF EXISTS `coin_expiry_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coin_expiry_records` (
  `id` varchar(32) NOT NULL COMMENT 'cer_xxxxxxxx',
  `resident_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `expired_amount` decimal(10,2) NOT NULL COMMENT '清零金额',
  `pool_in_amount` decimal(10,2) NOT NULL COMMENT '进入积分池金额',
  `expired_at` timestamp NOT NULL COMMENT '过期时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resident` (`resident_id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_expired_at` (`expired_at`),
  CONSTRAINT `coin_expiry_records_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `coin_expiry_records_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业币过期清零记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coin_expiry_records`
--

LOCK TABLES `coin_expiry_records` WRITE;
/*!40000 ALTER TABLE `coin_expiry_records` DISABLE KEYS */;
INSERT INTO `coin_expiry_records` VALUES ('cer_admin001','res_admin_test01','pc_demo001',80.00,80.00,'2026-06-30 15:59:59','2026-06-30 15:59:59','2026-07-03 08:02:56'),('cer_admin002','res_admin_test04','pc_demo001',120.00,120.00,'2026-03-31 15:59:59','2026-03-31 15:59:59','2026-07-03 08:02:56'),('cer_admin003','res_002','pc_demo001',50.00,50.00,'2026-06-30 15:59:59','2026-06-30 15:59:59','2026-07-03 08:02:56');
/*!40000 ALTER TABLE `coin_expiry_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coin_freeze_records`
--

DROP TABLE IF EXISTS `coin_freeze_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coin_freeze_records` (
  `id` varchar(32) NOT NULL COMMENT 'cfr_xxxxxxxx',
  `resident_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `action` varchar(20) NOT NULL COMMENT 'freeze/unfreeze',
  `amount` decimal(10,2) DEFAULT NULL COMMENT '涉及金额',
  `reason` varchar(200) DEFAULT NULL,
  `operator_id` varchar(32) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resident` (`resident_id`),
  KEY `idx_property_company` (`property_company_id`),
  CONSTRAINT `coin_freeze_records_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `coin_freeze_records_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业币冻结/解冻记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coin_freeze_records`
--

LOCK TABLES `coin_freeze_records` WRITE;
/*!40000 ALTER TABLE `coin_freeze_records` DISABLE KEYS */;
INSERT INTO `coin_freeze_records` VALUES ('cfr_01841000','res_48fa1000','pc_demo001','unfreeze',5.00,'1','res_admin01','2026-07-05 05:12:33','2026-07-05 05:12:33'),('cfr_0f9a1000','res_admin01','pc_demo001','freeze',0.00,'1','res_admin01','2026-07-03 01:13:04','2026-07-03 01:13:04'),('cfr_4cbe1000','res_48fa1000','pc_demo001','freeze',0.01,'1','res_admin01','2026-07-05 05:12:42','2026-07-05 05:12:42'),('cfr_87581000','res_admin01','pc_demo001','unfreeze',0.00,'1','res_admin01','2026-07-03 01:54:17','2026-07-03 01:54:17'),('cfr_admin001','res_admin_test01','pc_demo001','freeze',80.00,'账户异常，冻结物业币','res_propadmin01','2026-06-15 06:00:00','2026-07-03 08:02:56'),('cfr_admin002','res_admin_test01','pc_demo001','unfreeze',80.00,'账户已核实，解冻物业币','res_propadmin01','2026-06-20 02:00:00','2026-07-03 08:02:56'),('cfr_f32e1000','res_48fa1000','pc_demo001','freeze',5.00,'测试冻结','res_admin01','2026-07-05 02:24:18','2026-07-05 02:24:18');
/*!40000 ALTER TABLE `coin_freeze_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coin_withdrawals`
--

DROP TABLE IF EXISTS `coin_withdrawals`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coin_withdrawals` (
  `id` varchar(32) NOT NULL,
  `merchant_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `coin_amount` decimal(10,2) NOT NULL COMMENT '兑换物业币数量',
  `cash_amount` decimal(10,2) NOT NULL COMMENT '兑换现金金额',
  `fee_rate` decimal(5,4) NOT NULL,
  `fee_amount` decimal(10,2) NOT NULL,
  `actual_amount` decimal(10,2) NOT NULL,
  `audit_status` varchar(20) DEFAULT 'pending',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_property_company` (`property_company_id`),
  CONSTRAINT `coin_withdrawals_ibfk_1` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`),
  CONSTRAINT `coin_withdrawals_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业币兑换现金表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coin_withdrawals`
--

LOCK TABLES `coin_withdrawals` WRITE;
/*!40000 ALTER TABLE `coin_withdrawals` DISABLE KEYS */;
INSERT INTO `coin_withdrawals` VALUES ('cw_admin001','mch_demo001','pc_demo001',500.00,5.00,0.0060,0.03,4.97,'approved','2026-06-25 02:00:00','2026-07-03 08:02:56'),('cw_admin002','mch_demo002','pc_demo001',200.00,2.00,0.0060,0.01,1.99,'pending','2026-07-03 01:00:00','2026-07-03 08:02:56');
/*!40000 ALTER TABLE `coin_withdrawals` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `communities`
--

DROP TABLE IF EXISTS `communities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `communities` (
  `id` varchar(32) NOT NULL COMMENT 'com_xxxxxxxx',
  `property_company_id` varchar(32) NOT NULL,
  `name` varchar(100) NOT NULL,
  `address` varchar(200) DEFAULT NULL,
  `total_buildings` int DEFAULT NULL,
  `total_units` int DEFAULT NULL,
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_property_company` (`property_company_id`),
  CONSTRAINT `communities_ibfk_1` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='小区表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `communities`
--

LOCK TABLES `communities` WRITE;
/*!40000 ALTER TABLE `communities` DISABLE KEYS */;
INSERT INTO `communities` VALUES ('com_demo001','pc_demo001','演示小区',NULL,10,500,'active','2026-07-02 01:57:36','2026-07-02 01:57:36');
/*!40000 ALTER TABLE `communities` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `community_services`
--

DROP TABLE IF EXISTS `community_services`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `community_services` (
  `id` varchar(32) NOT NULL COMMENT 'svc_xxxxxxxx',
  `name` varchar(100) NOT NULL,
  `description` text,
  `cover_urls` json DEFAULT NULL,
  `category` varchar(50) NOT NULL COMMENT 'cleaning_repair/canteen/tutoring/elderly_care/medical/pet_medical/life_service/dating',
  `price` decimal(10,2) DEFAULT NULL,
  `member_price` decimal(10,2) DEFAULT NULL COMMENT '业主折扣价',
  `price_unit` varchar(20) DEFAULT NULL COMMENT '次/小时/平方米/月/年',
  `provider_id` varchar(32) DEFAULT NULL,
  `provider_type` varchar(20) DEFAULT NULL COMMENT 'merchant/resident/individual_leader',
  `property_company_id` varchar(32) NOT NULL,
  `rank_order` int DEFAULT '0' COMMENT '手动拖拽排名',
  `is_subscription` tinyint(1) DEFAULT '0' COMMENT '是否支持包月/包年',
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `provider_id` (`provider_id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_category` (`category`),
  KEY `idx_rank` (`rank_order`),
  CONSTRAINT `community_services_ibfk_1` FOREIGN KEY (`provider_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `community_services_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社区服务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `community_services`
--

LOCK TABLES `community_services` WRITE;
/*!40000 ALTER TABLE `community_services` DISABLE KEYS */;
/*!40000 ALTER TABLE `community_services` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coordinators`
--

DROP TABLE IF EXISTS `coordinators`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coordinators` (
  `id` varchar(32) NOT NULL COMMENT 'coo_xxxxxxxx',
  `resident_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `commission_rate` decimal(5,4) DEFAULT '0.7000' COMMENT '统筹分成比例',
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resident_property` (`resident_id`,`property_company_id`),
  KEY `property_company_id` (`property_company_id`),
  CONSTRAINT `coordinators_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `coordinators_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统筹表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coordinators`
--

LOCK TABLES `coordinators` WRITE;
/*!40000 ALTER TABLE `coordinators` DISABLE KEYS */;
INSERT INTO `coordinators` VALUES ('coo_001','res_coo01','pc_demo001',0.7000,'active','2026-07-03 08:09:15','2026-07-03 08:09:15');
/*!40000 ALTER TABLE `coordinators` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `deliveries`
--

DROP TABLE IF EXISTS `deliveries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `deliveries` (
  `id` varchar(32) NOT NULL COMMENT 'dlv_xxxxxxxx',
  `order_id` varchar(32) NOT NULL,
  `courier_id` varchar(32) DEFAULT NULL COMMENT '快递员ID',
  `property_company_id` varchar(32) NOT NULL,
  `status` varchar(20) DEFAULT 'pending' COMMENT 'pending/accepted/delivering/completed/failed/property_fallback',
  `fee` decimal(10,2) NOT NULL COMMENT '配送费',
  `courier_earning` decimal(10,2) DEFAULT NULL COMMENT '快递员收入',
  `pickup_address` varchar(200) DEFAULT NULL,
  `delivery_address` varchar(200) DEFAULT NULL,
  `contact_phone` varchar(20) DEFAULT NULL,
  `accepted_at` timestamp NULL DEFAULT NULL,
  `picked_at` timestamp NULL DEFAULT NULL,
  `delivered_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_courier` (`courier_id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `deliveries_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `deliveries_ibfk_2` FOREIGN KEY (`courier_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `deliveries_ibfk_3` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='配送表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `deliveries`
--

LOCK TABLES `deliveries` WRITE;
/*!40000 ALTER TABLE `deliveries` DISABLE KEYS */;
INSERT INTO `deliveries` VALUES ('dlv_admin001','ord_admin006','res_courier01','pc_demo001','delivering',0.00,0.00,'便民超市(华帝店)','华帝·阳光花园4栋301','13911110005',NULL,NULL,NULL,'2026-07-03 03:00:00','2026-07-03 08:02:56'),('dlv_chart001','ord_chart001','res_courier01','pc_demo001','delivered',1.00,0.35,'万达超市','3栋2单元201','139****0005','2026-07-05 00:10:00',NULL,'2026-07-05 00:40:00','2026-07-05 00:00:00','2026-07-05 06:24:46'),('dlv_chart002','ord_chart002','res_courier01','pc_demo001','delivered',1.00,0.35,'万达超市','3栋2单元202','139****0006','2026-07-05 00:25:00',NULL,'2026-07-05 00:55:00','2026-07-05 00:15:00','2026-07-05 06:24:46'),('dlv_chart003','ord_chart003','res_courier02','pc_demo001','delivered',1.00,0.35,'京东快递站','5栋1单元101','139****0010','2026-07-05 01:10:00',NULL,'2026-07-05 01:40:00','2026-07-05 01:00:00','2026-07-05 06:24:46'),('dlv_chart004','ord_chart004','res_courier02','pc_demo001','delivered',1.50,0.35,'美团优选','4栋3单元301','139****0007','2026-07-05 02:20:00',NULL,'2026-07-05 02:50:00','2026-07-05 02:00:00','2026-07-05 06:24:46'),('dlv_chart005','ord_chart005','res_courier03','pc_demo001','delivered',1.00,0.35,'菜鸟驿站','2栋1单元101','139****0008','2026-07-05 03:15:00',NULL,'2026-07-05 03:45:00','2026-07-05 03:00:00','2026-07-05 06:24:46'),('dlv_chart006','ord_chart006','res_courier01','pc_demo001','delivered',1.00,0.35,'便利店','3栋2单元201','139****0005','2026-07-05 04:30:00',NULL,'2026-07-05 05:00:00','2026-07-05 04:15:00','2026-07-05 06:24:46'),('dlv_chart007','ord_chart007','res_courier02','pc_demo001','delivered',1.50,0.35,'快递柜','5栋1单元101','139****0010','2026-07-05 05:40:00',NULL,'2026-07-05 06:10:00','2026-07-05 05:30:00','2026-07-05 06:24:46'),('dlv_chart008','ord_chart008','res_courier03','pc_demo001','delivered',1.00,0.35,'文具店','6栋2单元201','139****0011','2026-07-05 06:05:00',NULL,'2026-07-05 06:35:00','2026-07-05 05:55:00','2026-07-05 06:24:46'),('dlv_chart009','ord_chart009','res_courier01','pc_demo001','delivered',1.00,0.35,'水果店','2栋1单元101','139****0008','2026-07-05 06:20:00',NULL,'2026-07-05 06:50:00','2026-07-05 06:10:00','2026-07-05 06:24:46'),('dlv_chart010','ord_chart010','res_courier02','pc_demo001','delivered',1.50,0.35,'药店','4栋3单元301','139****0007','2026-07-05 07:00:00',NULL,'2026-07-05 07:30:00','2026-07-05 06:50:00','2026-07-05 06:24:46'),('dlv_chart011','ord_chart011','res_courier03','pc_demo001','accepted',1.00,0.35,'超市','3栋2单元202','139****0006','2026-07-05 07:20:00',NULL,NULL,'2026-07-05 07:10:00','2026-07-05 06:24:46'),('dlv_chart012','ord_chart012','res_courier01','pc_demo001','pending',1.00,0.35,'面包店','5栋1单元101','139****0010',NULL,NULL,NULL,'2026-07-05 07:30:00','2026-07-05 06:24:46'),('dlv_dev001','ord_dev001','res_courier01','pc_demo001','completed',0.00,0.00,'便民超市(华帝店)','华帝·阳光花园3栋1201','13800138000',NULL,NULL,'2026-07-01 02:00:00','2026-07-01 01:00:00','2026-07-03 06:53:28'),('dlv_dev002','ord_dev002','res_courier01','pc_demo001','completed',2.00,0.35,'口福餐厅(华帝店)','华帝·阳光花园3栋1201','13800138000',NULL,NULL,'2026-07-02 05:00:00','2026-07-02 04:00:00','2026-07-03 06:53:28'),('dlv_dev003','ord_dev003','res_courier01','pc_demo001','completed',3.00,0.35,'绿色农场直供(华帝站)','华帝·阳光花园3栋1201','13800138000',NULL,NULL,'2026-07-03 02:30:00','2026-07-03 01:30:00','2026-07-03 06:53:28');
/*!40000 ALTER TABLE `deliveries` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `delivery_rules`
--

DROP TABLE IF EXISTS `delivery_rules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `delivery_rules` (
  `id` varchar(32) NOT NULL,
  `name` varchar(100) NOT NULL,
  `base_fee` decimal(10,2) NOT NULL COMMENT '物业设定起步价',
  `per_kg_fee` decimal(10,2) DEFAULT NULL,
  `free_threshold` decimal(10,2) DEFAULT NULL,
  `max_weight` decimal(10,2) DEFAULT NULL,
  `max_distance` decimal(10,2) DEFAULT NULL,
  `courier_share_rate` decimal(5,4) DEFAULT '1.0000',
  `courier_per_order` decimal(10,2) DEFAULT '0.35' COMMENT '快递员每单报酬',
  `timeout_minutes` int DEFAULT '30',
  `property_company_id` varchar(32) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_property_company` (`property_company_id`),
  CONSTRAINT `delivery_rules_ibfk_1` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='配送规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `delivery_rules`
--

LOCK TABLES `delivery_rules` WRITE;
/*!40000 ALTER TABLE `delivery_rules` DISABLE KEYS */;
INSERT INTO `delivery_rules` VALUES ('dlv_rule001','华帝配送规则',2.00,0.50,20.00,50.00,5.00,1.0000,0.35,30,'pc_demo001','active','2026-07-03 08:02:56','2026-07-03 08:02:56'),('drl_demo001','默认配送规则',1.00,0.50,NULL,NULL,NULL,1.0000,0.35,30,'pc_demo001','active','2026-07-05 05:49:39','2026-07-05 05:49:39');
/*!40000 ALTER TABLE `delivery_rules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `distribution_records`
--

DROP TABLE IF EXISTS `distribution_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `distribution_records` (
  `id` varchar(32) NOT NULL COMMENT 'dr_xxxxxxxx',
  `order_id` varchar(32) NOT NULL,
  `merchant_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL COMMENT '住户所属物业',
  `resident_id` varchar(32) NOT NULL,
  `total_amount` decimal(10,2) NOT NULL COMMENT '消费金额P',
  `commission_rate` decimal(5,4) NOT NULL COMMENT '商家在该物业的抽佣比例R_T',
  `point_cost` decimal(10,2) DEFAULT '0.00' COMMENT '积分成本',
  `coin_cost` decimal(10,2) DEFAULT '0.00' COMMENT '物业币返现成本',
  `distributable_amount` decimal(10,2) NOT NULL COMMENT '可分利润',
  `property_share` decimal(10,2) NOT NULL COMMENT '物业公司收入',
  `coordinator_share` decimal(10,2) NOT NULL COMMENT '统筹收入',
  `sector_leader_share` decimal(10,2) DEFAULT '0.00' COMMENT '板块负责人收入',
  `individual_leader_share` decimal(10,2) DEFAULT '0.00' COMMENT '个体负责人收入',
  `coordinator_id` varchar(32) DEFAULT NULL COMMENT '统筹ID',
  `sector_leader_id` varchar(32) DEFAULT NULL COMMENT '板块负责人ID',
  `individual_leader_id` varchar(32) DEFAULT NULL COMMENT '个体负责人ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `resident_id` (`resident_id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_coordinator` (`coordinator_id`),
  KEY `idx_sector_leader` (`sector_leader_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `distribution_records_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `distribution_records_ibfk_2` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`),
  CONSTRAINT `distribution_records_ibfk_3` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`),
  CONSTRAINT `distribution_records_ibfk_4` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='四级分成记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `distribution_records`
--

LOCK TABLES `distribution_records` WRITE;
/*!40000 ALTER TABLE `distribution_records` DISABLE KEYS */;
INSERT INTO `distribution_records` VALUES ('dr_admin001','ord_dev001','mch_demo002','pc_demo001','res_dev001',88.00,0.1000,0.00,5.00,3.80,1.14,2.66,0.00,0.00,'coo_001',NULL,NULL,'2026-07-01 02:00:00','2026-07-03 08:02:56'),('dr_admin002','ord_dev003','mch_demo003','pc_demo001','res_dev001',126.50,0.2000,0.00,8.00,17.30,5.19,12.11,0.00,0.00,'coo_001',NULL,NULL,'2026-07-03 02:30:00','2026-07-03 08:02:56'),('dr_admin003','ord_dev006','mch_demo003','pc_demo001','res_dev001',98.00,0.2000,0.00,5.00,14.60,4.38,10.22,0.00,0.00,'coo_001',NULL,NULL,'2026-07-06 03:00:00','2026-07-03 08:02:56'),('dr_admin004','ord_admin007','mch_demo001','pc_demo001','res_admin_test07',72.00,0.1500,0.00,4.00,6.80,2.04,4.76,0.00,0.00,'coo_001',NULL,NULL,'2026-07-01 04:30:00','2026-07-03 08:02:56'),('dr_admin005','ord_admin008','mch_demo003','pc_demo001','res_admin_test08',95.00,0.2000,3.00,6.00,10.00,3.00,7.00,0.00,0.00,'coo_001',NULL,NULL,'2026-07-02 08:00:00','2026-07-03 08:02:56');
/*!40000 ALTER TABLE `distribution_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `families`
--

DROP TABLE IF EXISTS `families`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `families` (
  `id` varchar(32) NOT NULL COMMENT 'fam_xxxxxxxx',
  `name` varchar(50) DEFAULT NULL,
  `owner_id` varchar(32) NOT NULL COMMENT '户主ID',
  `property_company_id` varchar(32) NOT NULL,
  `community_id` varchar(32) DEFAULT NULL,
  `building` varchar(20) DEFAULT NULL,
  `unit` varchar(20) DEFAULT NULL,
  `room` varchar(20) DEFAULT NULL,
  `member_count` int DEFAULT '1',
  `point_balance` int DEFAULT '0' COMMENT '家庭积分池',
  `coin_balance` decimal(10,2) DEFAULT '0.00' COMMENT '家庭物业币池',
  `vote_weight` int DEFAULT '1' COMMENT '投票权重',
  `audit_status` varchar(20) DEFAULT 'pending' COMMENT 'pending/approved/rejected',
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_owner` (`owner_id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_audit_status` (`audit_status`),
  CONSTRAINT `families_ibfk_1` FOREIGN KEY (`owner_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `families_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='家庭表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `families`
--

LOCK TABLES `families` WRITE;
/*!40000 ALTER TABLE `families` DISABLE KEYS */;
INSERT INTO `families` VALUES ('fam_001','赵六家庭','res_001','pc_demo001','com_demo001','3','2','201',3,5000,300.00,1,'approved','active','2026-07-03 08:09:15','2026-07-03 08:09:15'),('fam_002','钱七家庭','res_002','pc_demo001','com_demo001','3','2','202',2,2500,150.00,1,'approved','active','2026-07-03 08:09:15','2026-07-03 08:09:15'),('fam_003','孙八家庭','res_003','pc_demo001','com_demo002','5','1','101',2,3000,200.00,1,'approved','active','2026-07-03 08:09:15','2026-07-03 08:09:15'),('fam_1ea41000','张家','res_48fa1000','pc_demo001','com_demo001','3栋','5单元','502',1,50,5.00,1,'pending','active','2026-07-05 02:04:44','2026-07-05 02:23:53'),('fam_5f4e1000','张家','res_e7de1000','pc_demo001','com_demo001','3栋','5单元','502',1,0,20.00,1,'pending','active','2026-07-03 04:59:20','2026-07-03 06:03:29'),('fam_admin001','赵小明家庭','res_admin_test04','pc_demo001','com_demo001','2','2','202',1,3500,200.00,1,'approved','active','2026-07-03 08:02:56','2026-07-03 08:02:56'),('fam_admin002','钱小红家庭','res_admin_test05','pc_demo001','com_demo001','4','1','301',3,2800,150.00,1,'approved','active','2026-07-03 08:02:56','2026-07-03 08:02:56'),('fam_admin003','周美丽家庭','res_admin_test07','pc_demo001','com_demo002','1','1','101',4,5000,300.00,1,'approved','active','2026-07-03 08:02:56','2026-07-03 08:02:56'),('fam_admin004','吴大伟家庭','res_admin_test08','pc_demo001','com_demo002','2','2','202',2,1800,90.00,1,'pending','active','2026-07-03 08:02:56','2026-07-03 08:02:56'),('fam_dev001','张阿姨家庭','res_dev001','pc_demo001','com_demo001','3','12','1201',4,12580,350.50,1,'approved','active','2026-07-03 07:40:59','2026-07-03 08:01:26');
/*!40000 ALTER TABLE `families` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `files`
--

DROP TABLE IF EXISTS `files`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `files` (
  `id` varchar(32) NOT NULL COMMENT 'file_xxxxxxxx',
  `original_name` varchar(200) NOT NULL,
  `stored_name` varchar(200) NOT NULL,
  `url` varchar(500) NOT NULL,
  `thumbnail_url` varchar(500) DEFAULT NULL,
  `mime_type` varchar(50) DEFAULT NULL,
  `size` bigint DEFAULT NULL,
  `category` varchar(30) DEFAULT NULL COMMENT 'avatar/announcement/merchant/service/activity',
  `uploader_id` varchar(32) DEFAULT NULL,
  `property_company_id` varchar(32) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_uploader` (`uploader_id`),
  KEY `idx_category` (`category`),
  KEY `idx_property_company` (`property_company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `files`
--

LOCK TABLES `files` WRITE;
/*!40000 ALTER TABLE `files` DISABLE KEYS */;
/*!40000 ALTER TABLE `files` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `individual_leaders`
--

DROP TABLE IF EXISTS `individual_leaders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `individual_leaders` (
  `id` varchar(32) NOT NULL COMMENT 'il_xxxxxxxx',
  `resident_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `sector_leader_id` varchar(32) NOT NULL COMMENT '所属板块负责人',
  `sector` varchar(50) NOT NULL,
  `name` varchar(50) DEFAULT NULL COMMENT '个体名称',
  `commission_rate` decimal(5,4) DEFAULT '0.0000' COMMENT '从板块中抽取的比例',
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `resident_id` (`resident_id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_sector_leader` (`sector_leader_id`),
  KEY `idx_sector` (`sector`),
  CONSTRAINT `individual_leaders_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `individual_leaders_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`),
  CONSTRAINT `individual_leaders_ibfk_3` FOREIGN KEY (`sector_leader_id`) REFERENCES `sector_leaders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='个体负责人表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `individual_leaders`
--

LOCK TABLES `individual_leaders` WRITE;
/*!40000 ALTER TABLE `individual_leaders` DISABLE KEYS */;
INSERT INTO `individual_leaders` VALUES ('il_001','res_ind01','pc_demo001','sl_001','cleaning_repair','王强家政',0.0000,'active','2026-07-03 08:09:15','2026-07-03 08:09:15');
/*!40000 ALTER TABLE `individual_leaders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `merchant_point_grants`
--

DROP TABLE IF EXISTS `merchant_point_grants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `merchant_point_grants` (
  `id` varchar(32) NOT NULL COMMENT 'mpg_xxxxxxxx',
  `merchant_id` varchar(32) NOT NULL,
  `purchase_id` varchar(32) NOT NULL COMMENT '关联购买记录',
  `resident_id` varchar(32) NOT NULL COMMENT '接收用户',
  `family_id` varchar(32) DEFAULT NULL COMMENT '家庭ID',
  `property_company_id` varchar(32) NOT NULL COMMENT '按住户所属物业',
  `point_amount` int NOT NULL COMMENT '录入积分数量',
  `exchange_rate` decimal(10,4) DEFAULT NULL COMMENT '该物业兑换比',
  `description` varchar(200) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `purchase_id` (`purchase_id`),
  KEY `family_id` (`family_id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_resident` (`resident_id`),
  KEY `idx_property_company` (`property_company_id`),
  CONSTRAINT `merchant_point_grants_ibfk_1` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`),
  CONSTRAINT `merchant_point_grants_ibfk_2` FOREIGN KEY (`purchase_id`) REFERENCES `merchant_point_purchases` (`id`),
  CONSTRAINT `merchant_point_grants_ibfk_3` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `merchant_point_grants_ibfk_4` FOREIGN KEY (`family_id`) REFERENCES `families` (`id`),
  CONSTRAINT `merchant_point_grants_ibfk_5` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家积分录入表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `merchant_point_grants`
--

LOCK TABLES `merchant_point_grants` WRITE;
/*!40000 ALTER TABLE `merchant_point_grants` DISABLE KEYS */;
INSERT INTO `merchant_point_grants` VALUES ('mpg_admin001','mch_demo001','mpp_admin001','res_dev001','fam_dev001','pc_demo001',500,100.0000,'口福餐厅消费赠积分','2026-06-15 04:00:00','2026-07-03 08:02:56'),('mpg_admin002','mch_demo001','mpp_admin001','res_001','fam_001','pc_demo001',300,100.0000,'口福餐厅消费赠积分','2026-06-20 10:00:00','2026-07-03 08:02:56'),('mpg_admin003','mch_demo002','mpp_admin002','res_dev001','fam_dev001','pc_demo001',200,200.0000,'便民超市消费赠积分','2026-06-25 08:00:00','2026-07-03 08:02:56');
/*!40000 ALTER TABLE `merchant_point_grants` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `merchant_point_purchases`
--

DROP TABLE IF EXISTS `merchant_point_purchases`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `merchant_point_purchases` (
  `id` varchar(32) NOT NULL COMMENT 'mpp_xxxxxxxx',
  `merchant_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `point_amount` int NOT NULL COMMENT '购买积分数量',
  `pay_amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `audit_status` varchar(20) DEFAULT 'pending' COMMENT 'pending/approved/rejected',
  `audited_at` timestamp NULL DEFAULT NULL,
  `auditor_id` varchar(32) DEFAULT NULL,
  `remaining_points` int DEFAULT NULL COMMENT '剩余未录入积分',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_audit_status` (`audit_status`),
  CONSTRAINT `merchant_point_purchases_ibfk_1` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`),
  CONSTRAINT `merchant_point_purchases_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家积分购买表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `merchant_point_purchases`
--

LOCK TABLES `merchant_point_purchases` WRITE;
/*!40000 ALTER TABLE `merchant_point_purchases` DISABLE KEYS */;
INSERT INTO `merchant_point_purchases` VALUES ('mpp_admin001','mch_demo001','pc_demo001',5000,50.00,'approved','2026-06-01 02:00:00','res_admin01',2000,'2026-06-01 01:00:00','2026-07-03 08:02:56'),('mpp_admin002','mch_demo002','pc_demo001',10000,50.00,'approved','2026-06-05 02:00:00','res_admin01',5000,'2026-06-05 01:00:00','2026-07-03 08:02:56'),('mpp_admin003','mch_demo003','pc_demo001',3000,60.00,'pending',NULL,NULL,3000,'2026-07-03 00:00:00','2026-07-03 08:02:56');
/*!40000 ALTER TABLE `merchant_point_purchases` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `merchant_property_configs`
--

DROP TABLE IF EXISTS `merchant_property_configs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `merchant_property_configs` (
  `id` varchar(32) NOT NULL COMMENT 'mpc_xxxxxxxx',
  `merchant_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `commission_rate` decimal(5,4) NOT NULL COMMENT '抽佣比例',
  `point_exchange_rate` decimal(10,4) DEFAULT NULL COMMENT '积分兑换比',
  `member_discount_price` decimal(10,2) DEFAULT NULL COMMENT '业主折扣价',
  `coin_rebate_rate` decimal(5,4) DEFAULT '0.0000' COMMENT '物业币返现比',
  `coin_rebate_enabled` tinyint(1) DEFAULT '0' COMMENT '是否启用物业币返现',
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_property` (`merchant_id`,`property_company_id`),
  KEY `idx_property_company` (`property_company_id`),
  CONSTRAINT `merchant_property_configs_ibfk_1` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`),
  CONSTRAINT `merchant_property_configs_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家-物业参数配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `merchant_property_configs`
--

LOCK TABLES `merchant_property_configs` WRITE;
/*!40000 ALTER TABLE `merchant_property_configs` DISABLE KEYS */;
INSERT INTO `merchant_property_configs` VALUES ('mpc_b6f61000','mch_b6e81000','pc_demo001',0.1000,100.0000,NULL,0.0000,0,'active','2026-07-03 01:59:18','2026-07-03 01:59:18'),('mpc_ed061000','mch_ecfc1000','pc_demo001',0.1000,100.0000,NULL,0.0000,0,'active','2026-07-05 05:47:27','2026-07-05 05:47:27');
/*!40000 ALTER TABLE `merchant_property_configs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `merchant_withdrawals`
--

DROP TABLE IF EXISTS `merchant_withdrawals`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `merchant_withdrawals` (
  `id` varchar(32) NOT NULL COMMENT 'mw_xxxxxxxx',
  `merchant_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `amount` decimal(10,2) NOT NULL COMMENT '提现金额',
  `fee_rate` decimal(5,4) NOT NULL COMMENT '手续费率',
  `fee_amount` decimal(10,2) NOT NULL COMMENT '手续费',
  `actual_amount` decimal(10,2) NOT NULL COMMENT '实际到账',
  `audit_status` varchar(20) DEFAULT 'pending' COMMENT 'pending/approved/rejected/completed',
  `audited_at` timestamp NULL DEFAULT NULL,
  `auditor_id` varchar(32) DEFAULT NULL,
  `completed_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_audit_status` (`audit_status`),
  CONSTRAINT `merchant_withdrawals_ibfk_1` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`),
  CONSTRAINT `merchant_withdrawals_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家提现表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `merchant_withdrawals`
--

LOCK TABLES `merchant_withdrawals` WRITE;
/*!40000 ALTER TABLE `merchant_withdrawals` DISABLE KEYS */;
INSERT INTO `merchant_withdrawals` VALUES ('mw_admin001','mch_demo001','pc_demo001',1000.00,0.0060,6.00,994.00,'approved','2026-06-20 03:00:00','res_admin01',NULL,'2026-06-19 02:00:00','2026-07-03 08:02:56'),('mw_admin002','mch_demo002','pc_demo001',500.00,0.0060,3.00,497.00,'pending',NULL,NULL,NULL,'2026-07-02 06:00:00','2026-07-03 08:02:56'),('mw_admin003','mch_demo003','pc_demo001',2000.00,0.0060,12.00,1988.00,'rejected','2026-07-01 01:00:00','res_admin01',NULL,'2026-06-30 02:00:00','2026-07-03 08:02:56');
/*!40000 ALTER TABLE `merchant_withdrawals` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `merchants`
--

DROP TABLE IF EXISTS `merchants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `merchants` (
  `id` varchar(32) NOT NULL COMMENT 'mch_xxxxxxxx',
  `platform_merchant_id` varchar(32) NOT NULL COMMENT '关联平台商家库ID',
  `property_company_id` varchar(32) NOT NULL COMMENT '挂接的物业公司',
  `name` varchar(100) NOT NULL COMMENT '商家名称',
  `description` text,
  `cover_urls` json DEFAULT NULL COMMENT '封面图片数组',
  `video_url` varchar(500) DEFAULT NULL,
  `qr_code_url` varchar(500) DEFAULT NULL COMMENT '专属收款二维码',
  `merchant_level` varchar(30) DEFAULT 'normal' COMMENT 'official_certified/property_certified/normal',
  `level_weight` int DEFAULT '0' COMMENT '分级排序权重',
  `category` varchar(50) DEFAULT NULL COMMENT '分类',
  `audit_status` varchar(20) DEFAULT 'pending_audit' COMMENT 'pending_audit/approved/rejected',
  `rank_order` int DEFAULT '0' COMMENT '手动拖拽排序值',
  `business_hours` varchar(50) DEFAULT NULL,
  `contact_phone` varchar(20) DEFAULT NULL,
  `address` varchar(200) DEFAULT NULL,
  `delivery_fee` decimal(10,2) DEFAULT '0.00' COMMENT '商家设定的配送费',
  `free_delivery_threshold` decimal(10,2) DEFAULT NULL COMMENT '满额免配送费',
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_platform_property` (`platform_merchant_id`,`property_company_id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_level` (`merchant_level`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_category` (`category`),
  KEY `idx_rank` (`rank_order`),
  CONSTRAINT `merchants_ibfk_1` FOREIGN KEY (`platform_merchant_id`) REFERENCES `platform_merchants` (`id`),
  CONSTRAINT `merchants_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `merchants`
--

LOCK TABLES `merchants` WRITE;
/*!40000 ALTER TABLE `merchants` DISABLE KEYS */;
INSERT INTO `merchants` VALUES ('mch_b6e81000','pm_b6c61000','pc_demo001','测试商家',NULL,NULL,NULL,NULL,'property_certified',0,'外卖','approved',0,NULL,'13800000000',NULL,0.00,NULL,'active','2026-07-03 01:59:18','2026-07-03 03:40:04'),('mch_demo001','pm_demo001','pc_demo001','口福餐厅','社区餐厅',NULL,NULL,NULL,'normal',0,'餐饮','pending_audit',0,NULL,NULL,NULL,0.00,NULL,'active','2026-07-05 06:24:41','2026-07-05 06:24:41'),('mch_demo002','pm_demo002','pc_demo001','便民超市','社区便利店',NULL,NULL,NULL,'normal',0,'超市','pending_audit',0,NULL,NULL,NULL,0.00,NULL,'active','2026-07-05 06:24:41','2026-07-05 06:24:41'),('mch_demo003','pm_demo003','pc_demo001','绿色农场','有机蔬菜直供',NULL,NULL,NULL,'normal',0,'生鲜','pending_audit',0,NULL,NULL,NULL,0.00,NULL,'active','2026-07-05 06:24:41','2026-07-05 06:24:41'),('mch_ecfc1000','pm_ecf21000','pc_demo001','我的超市',NULL,NULL,NULL,NULL,'property_certified',0,'外卖','approved',0,NULL,'13812345678','小区东门',0.30,50.00,'active','2026-07-05 05:47:27','2026-07-05 05:49:06'),('mch_test001','pm_test001','pc_demo001','测试外卖商家1','测试用的外卖店铺',NULL,NULL,NULL,'normal',0,'外卖','approved',2,'08:00-22:00','13900001111','小区东门',0.00,0.03,'kicked','2026-07-03 03:51:21','2026-07-05 03:36:22');
/*!40000 ALTER TABLE `merchants` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `messages` (
  `id` varchar(32) NOT NULL COMMENT 'msg_xxxxxxxx',
  `from_id` varchar(32) NOT NULL,
  `to_id` varchar(32) DEFAULT NULL,
  `to_role` varchar(30) DEFAULT NULL COMMENT '群发时的目标角色',
  `message_type` varchar(30) NOT NULL COMMENT 'property/platform/system/community/resident/technician',
  `content` text NOT NULL,
  `chat_type` varchar(20) DEFAULT NULL COMMENT 'private/group/broadcast',
  `group_id` varchar(32) DEFAULT NULL,
  `read_status` varchar(20) DEFAULT 'unread',
  `property_company_id` varchar(32) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_from` (`from_id`),
  KEY `idx_to` (`to_id`),
  KEY `idx_type` (`message_type`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `messages_ibfk_1` FOREIGN KEY (`from_id`) REFERENCES `residents` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messages`
--

LOCK TABLES `messages` WRITE;
/*!40000 ALTER TABLE `messages` DISABLE KEYS */;
INSERT INTO `messages` VALUES ('msg_admin001','res_propadmin01','res_admin_test04',NULL,'property','您所在的楼栋将于明日进行供水管道检修，请提前储水。','broadcast',NULL,'unread','pc_demo001','2026-07-03 00:00:00','2026-07-03 08:02:56'),('msg_admin002','res_admin01','res_admin_test05',NULL,'system','系统升级通知：7月5日凌晨2:00-4:00进行系统维护，届时部分功能可能无法使用。','broadcast',NULL,'read','pc_demo001','2026-07-02 08:00:00','2026-07-03 08:02:56'),('msg_admin003','res_coo01','res_admin_test07',NULL,'coordinator','翠湖名苑业主满意度调查开启，请积极参与。','broadcast',NULL,'unread','pc_demo001','2026-07-03 02:00:00','2026-07-03 08:02:56'),('msg_admin004','res_mch01','res_admin_test04',NULL,'resident','您的订单已开始配送，预计30分钟内送达。','private',NULL,'read','pc_demo001','2026-07-03 03:00:00','2026-07-03 08:02:56'),('msg_dev001','res_propadmin01','res_dev001',NULL,'property','您所在的楼栋将于明日14:00进行供水管道检修。尊敬的业主：因管道例行检查，3号楼A单元将于明日下午暂停供水，预计耗时2小时，请提前储水。','broadcast',NULL,'unread','pc_demo001','2026-07-09 02:45:00','2026-07-03 06:23:28'),('msg_dev002','res_admin01','res_dev001',NULL,'system','积金变动提醒：已成功到账 500 积分。您的物业费预缴奖励积分已发放。您可以在\"积金商城\"查看并兑换您心仪的礼品。','broadcast',NULL,'read','pc_demo001','2026-07-08 01:00:00','2026-07-03 06:23:28'),('msg_dev003','res_coo01','res_dev001',NULL,'coordinator','年度物业满意度调查已开启。为了提供更好的社区服务，请您参与2023年度业主满意度调查，您的建议对我们至关重要。','broadcast',NULL,'read','pc_demo001','2026-07-08 00:00:00','2026-07-03 06:23:28'),('msg_dev004','res_act01','res_dev001',NULL,'community','邻里跳蚤市场活动招募公告。本周六社区中心广场将举办\"旧物换新颜\"二手市集，欢迎各位邻里踊跃报名设摊或参与交换。','broadcast',NULL,'read','pc_demo001','2026-07-07 02:00:00','2026-07-03 06:23:28'),('msg_dev005','res_propadmin01','res_dev001',NULL,'property','社区电梯维保完成公示。截止本周五，全区32部客梯已全部完成季度深度维保及质检，各项指标均符合安全运行要求。','broadcast',NULL,'read','pc_demo001','2026-06-20 01:00:00','2026-07-03 06:23:28'),('msg_dev006','res_mch01','res_dev001',NULL,'resident','您好，您的订单已发货，预计今日18:00前送达，请保持手机畅通。','private',NULL,'read','pc_demo001','2026-07-10 06:00:00','2026-07-03 06:23:28');
/*!40000 ALTER TABLE `messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `neighbor_contact_limits`
--

DROP TABLE IF EXISTS `neighbor_contact_limits`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `neighbor_contact_limits` (
  `id` varchar(32) NOT NULL,
  `resident_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `contact_date` date NOT NULL,
  `contact_count` int DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resident_date` (`resident_id`,`contact_date`),
  KEY `property_company_id` (`property_company_id`),
  KEY `idx_resident` (`resident_id`),
  CONSTRAINT `neighbor_contact_limits_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `neighbor_contact_limits_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='邻居私聊每日限制表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `neighbor_contact_limits`
--

LOCK TABLES `neighbor_contact_limits` WRITE;
/*!40000 ALTER TABLE `neighbor_contact_limits` DISABLE KEYS */;
INSERT INTO `neighbor_contact_limits` VALUES ('ncl_admin001','res_dev001','pc_demo001','2026-07-03',1,'2026-07-03 08:02:56','2026-07-03 08:02:56'),('ncl_admin002','res_admin_test04','pc_demo001','2026-07-02',2,'2026-07-03 08:02:56','2026-07-03 08:02:56');
/*!40000 ALTER TABLE `neighbor_contact_limits` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `operation_logs`
--

DROP TABLE IF EXISTS `operation_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `operation_logs` (
  `id` varchar(32) NOT NULL,
  `operator_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) DEFAULT NULL,
  `module` varchar(50) NOT NULL,
  `action` varchar(50) NOT NULL,
  `target_type` varchar(50) DEFAULT NULL,
  `target_id` varchar(32) DEFAULT NULL,
  `before_data` json DEFAULT NULL,
  `after_data` json DEFAULT NULL,
  `ip_address` varchar(50) DEFAULT NULL,
  `user_agent` varchar(500) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_operator` (`operator_id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_module` (`module`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `operation_logs_ibfk_1` FOREIGN KEY (`operator_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `operation_logs_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `operation_logs`
--

LOCK TABLES `operation_logs` WRITE;
/*!40000 ALTER TABLE `operation_logs` DISABLE KEYS */;
INSERT INTO `operation_logs` VALUES ('log_admin001','res_propadmin01','pc_demo001','user','create','resident','res_admin_test03',NULL,'{\"role\": \"resident\", \"status\": \"active\"}',NULL,NULL,'2026-07-03 01:00:00','2026-07-03 08:02:56'),('log_admin002','res_propadmin01','pc_demo001','user','freeze','resident','res_admin_test01','{\"status\": \"active\"}','{\"status\": \"frozen\"}',NULL,NULL,'2026-06-15 06:00:00','2026-07-03 08:02:56'),('log_admin003','res_propadmin01','pc_demo001','user','disable','resident','res_admin_test02','{\"status\": \"active\"}','{\"status\": \"disabled\"}',NULL,NULL,'2026-06-20 02:30:00','2026-07-03 08:02:56'),('log_admin004','res_propadmin01','pc_demo001','user','update','resident','res_001','{\"name\": \"赵六\", \"phone\": \"13900000005\"}','{\"name\": \"赵六\", \"phone\": \"13900000005\", \"building\": \"3\"}',NULL,NULL,'2026-06-25 03:00:00','2026-07-03 08:02:56'),('log_admin005','res_propadmin01','pc_demo001','merchant','audit_approve','merchant','mch_demo001','{\"audit_status\": \"pending_audit\"}','{\"audit_status\": \"approved\"}',NULL,NULL,'2026-06-10 01:00:00','2026-07-03 08:02:56'),('log_admin006','res_propadmin01','pc_demo001','merchant','update','merchant','mch_demo002','{\"delivery_fee\": 2.0}','{\"delivery_fee\": 0.0}',NULL,NULL,'2026-06-18 07:00:00','2026-07-03 08:02:56'),('log_admin007','res_propadmin01','pc_demo001','announcement','publish','announcement','ann_001',NULL,'{\"title\": \"关于2026年7月物业费缴纳通知\"}',NULL,NULL,'2026-07-01 01:00:00','2026-07-03 08:02:56'),('log_admin008','res_coo01','pc_demo001','announcement','publish','announcement','ann_003',NULL,'{\"title\": \"社区夏令营活动报名开始\"}',NULL,NULL,'2026-07-02 02:00:00','2026-07-03 08:02:56'),('log_admin009','res_admin01','pc_demo001','pool','adjust','point_pool','pp_demo001','{\"balance\": 40000.0}','{\"balance\": 50000.0}',NULL,NULL,'2026-06-30 16:00:00','2026-07-03 08:02:56'),('log_admin010','res_propadmin01','pc_demo001','property_fee','create','property_fee','pf_001',NULL,'{\"amount\": 360.0, \"period\": \"2026-Q3\"}',NULL,NULL,'2026-06-30 00:00:00','2026-07-03 08:02:56'),('log_admin011','res_propadmin01','pc_demo001','permission','grant','resident','res_coo01',NULL,'{\"permission\": \"announcement.publish\"}',NULL,NULL,'2026-06-20 06:00:00','2026-07-03 08:02:56'),('log_admin012','res_admin01','pc_demo001','permission','apply_preset','resident','res_sec01',NULL,'{\"preset\": \"sector_leader\"}',NULL,NULL,'2026-06-01 02:00:00','2026-07-03 08:02:56'),('log_admin013','res_mch01','pc_demo001','promotion','publish','special_offer','so_dev002',NULL,'{\"title\": \"口福餐厅限时特惠\"}',NULL,NULL,'2026-07-04 16:00:00','2026-07-03 08:02:56'),('log_admin014','res_act01','pc_demo001','activity_group','create','activity_group','ag_dev001',NULL,'{\"name\": \"太极拳晨练组\"}',NULL,NULL,'2026-06-01 00:00:00','2026-07-03 08:02:56'),('log_admin015','res_act01','pc_demo001','activity_group','update','activity_group','ag_dev001','{\"member_count\": 20}','{\"member_count\": 28}',NULL,NULL,'2026-06-28 02:00:00','2026-07-03 08:02:56'),('ol_09721000','res_admin01','pc_demo001','coin_expiry','run','coin_expiry_task','task_09641000',NULL,'{\"expiryDate\": \"2026-07-31\", \"processedCount\": 0, \"totalExpiredAmount\": 0}',NULL,NULL,'2026-07-05 04:07:02','2026-07-05 04:07:02');
/*!40000 ALTER TABLE `operation_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `opinion_boxes`
--

DROP TABLE IF EXISTS `opinion_boxes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `opinion_boxes` (
  `id` varchar(32) NOT NULL,
  `title` varchar(200) NOT NULL,
  `content` text NOT NULL,
  `publisher_id` varchar(32) NOT NULL,
  `family_id` varchar(32) NOT NULL COMMENT '按家庭限制每月1条',
  `property_company_id` varchar(32) NOT NULL,
  `start_time` timestamp NOT NULL,
  `end_time` timestamp NOT NULL,
  `total_votes` int DEFAULT '0',
  `support_count` int DEFAULT '0',
  `oppose_count` int DEFAULT '0',
  `abstain_count` int DEFAULT '0',
  `status` varchar(20) DEFAULT 'active' COMMENT 'active/ended/closed',
  `result_published` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `publisher_id` (`publisher_id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_family` (`family_id`),
  KEY `idx_status` (`status`),
  KEY `idx_end_time` (`end_time`),
  CONSTRAINT `opinion_boxes_ibfk_1` FOREIGN KEY (`publisher_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `opinion_boxes_ibfk_2` FOREIGN KEY (`family_id`) REFERENCES `families` (`id`),
  CONSTRAINT `opinion_boxes_ibfk_3` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='意见箱表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `opinion_boxes`
--

LOCK TABLES `opinion_boxes` WRITE;
/*!40000 ALTER TABLE `opinion_boxes` DISABLE KEYS */;
INSERT INTO `opinion_boxes` VALUES ('ob_dev001','升级大堂空气净化系统','建议在大堂和电梯轿厢安装高效HEPA过滤器，以改善花粉季节的空气质量。','res_002','fam_002','pc_demo001','2026-06-30 16:00:00','2026-07-15 15:59:59',142,97,17,28,'active',0,'2026-06-30 16:00:00','2026-07-03 06:23:28'),('ob_dev002','延长健身房开放时间','由于居民需求量大，健身房将从下周一开始延长开放至晚上 11:30。','res_003','fam_003','pc_demo001','2026-06-09 16:00:00','2026-06-24 15:59:59',180,148,22,10,'ended',1,'2026-06-09 16:00:00','2026-07-03 06:23:28'),('ob_dev003','取消访客停车时间限制','申请允许周末访客在无需事先登记的情况下进行过夜停车。','res_004','','pc_demo001','2026-05-31 16:00:00','2026-06-20 15:59:59',160,35,120,5,'ended',1,'2026-05-31 16:00:00','2026-07-03 06:23:28');
/*!40000 ALTER TABLE `opinion_boxes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` varchar(32) NOT NULL COMMENT 'oi_xxxxxxxx',
  `order_id` varchar(32) NOT NULL,
  `product_id` varchar(32) NOT NULL,
  `product_name` varchar(100) DEFAULT NULL COMMENT '冗余',
  `product_cover` varchar(500) DEFAULT NULL COMMENT '冗余',
  `quantity` int NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `total_price` decimal(10,2) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES ('oi_admin001','ord_admin001','prd_006','纸巾套装(3包)',NULL,3,15.00,45.00,'2026-07-03 08:02:56','2026-07-03 08:02:56'),('oi_admin002','ord_admin002','prd_001','宫保鸡丁',NULL,1,28.00,28.00,'2026-07-03 08:02:56','2026-07-03 08:02:56'),('oi_admin003','ord_admin002','prd_005','蛋炒饭',NULL,2,12.00,24.00,'2026-07-03 08:02:56','2026-07-03 08:02:56'),('oi_admin004','ord_admin003','prd_009','有机小白菜(500g)',NULL,5,8.00,40.00,'2026-07-03 08:02:56','2026-07-03 08:02:56'),('oi_admin005','ord_admin003','prd_010','土鸡蛋(30枚)',NULL,2,38.00,76.00,'2026-07-03 08:02:56','2026-07-03 08:02:56'),('oi_admin006','ord_admin004','prd_007','洗衣液(2kg)',NULL,1,39.90,39.90,'2026-07-03 08:02:56','2026-07-03 08:02:56'),('oi_admin007','ord_admin005','prd_002','红烧肉',NULL,1,35.00,35.00,'2026-07-03 08:02:56','2026-07-03 08:02:56'),('oi_admin008','ord_admin005','prd_003','东北拉皮',NULL,1,18.00,18.00,'2026-07-03 08:02:56','2026-07-03 08:02:56'),('oi_admin009','ord_admin006','prd_006','纸巾套装(3包)',NULL,3,15.00,45.00,'2026-07-03 08:02:56','2026-07-03 08:02:56'),('oi_admin010','ord_admin006','prd_008','矿泉水(24瓶)',NULL,2,28.00,56.00,'2026-07-03 08:02:56','2026-07-03 08:02:56'),('oi_admin011','ord_admin007','prd_004','酸菜白肉血肠',NULL,1,48.00,48.00,'2026-07-03 08:02:56','2026-07-03 08:02:56'),('oi_admin012','ord_admin007','prd_005','蛋炒饭',NULL,2,12.00,24.00,'2026-07-03 08:02:56','2026-07-03 08:02:56'),('oi_admin013','ord_admin008','prd_009','有机小白菜(500g)',NULL,3,8.00,24.00,'2026-07-03 08:02:56','2026-07-03 08:02:56'),('oi_admin014','ord_admin008','prd_010','土鸡蛋(30枚)',NULL,1,38.00,38.00,'2026-07-03 08:02:56','2026-07-03 08:02:56'),('oi_admin015','ord_admin008','prd_011','有机西红柿(1kg)',NULL,2,12.00,24.00,'2026-07-03 08:02:56','2026-07-03 08:02:56'),('oi_dev001','ord_dev001','prd_006','纸巾套装(3包)',NULL,2,15.00,30.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('oi_dev002','ord_dev001','prd_007','洗衣液(2kg)',NULL,1,39.90,39.90,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('oi_dev003','ord_dev001','prd_008','矿泉水(24瓶)',NULL,1,28.00,28.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('oi_dev004','ord_dev002','prd_001','宫保鸡丁',NULL,1,28.00,28.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('oi_dev005','ord_dev002','prd_002','红烧肉',NULL,1,35.00,35.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('oi_dev006','ord_dev003','prd_009','有机小白菜(500g)',NULL,3,8.00,24.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('oi_dev007','ord_dev003','prd_010','土鸡蛋(30枚)',NULL,2,38.00,76.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('oi_dev008','ord_dev003','prd_011','有机西红柿(1kg)',NULL,2,12.00,24.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('oi_dev009','ord_dev004','prd_006','纸巾套装(3包)',NULL,5,15.00,75.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('oi_dev010','ord_dev004','prd_007','洗衣液(2kg)',NULL,2,39.90,79.80,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('oi_dev011','ord_dev005','prd_003','东北拉皮',NULL,1,18.00,18.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('oi_dev012','ord_dev005','prd_005','蛋炒饭',NULL,2,12.00,24.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('oi_dev013','ord_dev006','prd_009','有机小白菜(500g)',NULL,5,8.00,40.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('oi_dev014','ord_dev006','prd_011','有机西红柿(1kg)',NULL,4,12.00,48.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('oi_dev015','ord_dev007','prd_006','纸巾套装(3包)',NULL,10,15.00,150.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('oi_dev016','ord_dev007','prd_007','洗衣液(2kg)',NULL,2,39.90,79.80,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('oi_dev017','ord_dev008','prd_004','酸菜白肉血肠',NULL,1,48.00,48.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('oi_dev018','ord_dev008','prd_001','宫保鸡丁',NULL,1,28.00,28.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('oi_dev019','ord_dev009','prd_009','有机小白菜(500g)',NULL,3,8.00,24.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('oi_dev020','ord_dev009','prd_010','土鸡蛋(30枚)',NULL,1,38.00,38.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('oi_dev021','ord_dev010','prd_007','洗衣液(2kg)',NULL,5,39.90,199.50,'2026-07-03 06:23:28','2026-07-03 06:23:28');
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` varchar(32) NOT NULL COMMENT 'ord_xxxxxxxx',
  `order_no` varchar(32) DEFAULT NULL,
  `resident_id` varchar(32) NOT NULL,
  `merchant_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL COMMENT '按住户所属物业归属',
  `family_id` varchar(32) DEFAULT NULL,
  `order_status` varchar(20) DEFAULT 'pending' COMMENT 'pending/paid/delivering/completed/cancelled/refunding/refunded',
  `total_amount` decimal(10,2) NOT NULL,
  `delivery_fee` decimal(10,2) DEFAULT '0.00',
  `payment_method` varchar(20) DEFAULT NULL COMMENT 'property_coin/point/wechat/mixed',
  `point_used` int DEFAULT '0' COMMENT '混合支付使用的积分',
  `coin_used` decimal(10,2) DEFAULT '0.00' COMMENT '混合支付使用的物业币',
  `cash_amount` decimal(10,2) DEFAULT '0.00' COMMENT '混合支付现金部分',
  `paid_at` timestamp NULL DEFAULT NULL,
  `delivered_at` timestamp NULL DEFAULT NULL,
  `completed_at` timestamp NULL DEFAULT NULL,
  `cancelled_at` timestamp NULL DEFAULT NULL,
  `cancel_reason` varchar(200) DEFAULT NULL COMMENT '取消原因',
  `point_earned` int DEFAULT '0' COMMENT '本次获得积分',
  `coin_earned` decimal(10,2) DEFAULT '0.00' COMMENT '本次获得物业币返现',
  `courier_id` varchar(32) DEFAULT NULL,
  `address` varchar(200) DEFAULT NULL,
  `contact_phone` varchar(20) DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`),
  KEY `family_id` (`family_id`),
  KEY `idx_resident` (`resident_id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_status` (`order_status`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `orders_ibfk_2` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`),
  CONSTRAINT `orders_ibfk_3` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`),
  CONSTRAINT `orders_ibfk_4` FOREIGN KEY (`family_id`) REFERENCES `families` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES ('ord_admin001','ORD20260601001','res_admin_test04','mch_demo002','pc_demo001','fam_admin001','cancelled',45.00,0.00,'wechat',0,0.00,45.00,NULL,NULL,NULL,'2026-06-01 02:30:00','用户主动取消',0,0.00,NULL,'华帝·阳光花园2栋202','13911110004',NULL,0,'2026-06-01 02:00:00','2026-07-03 08:02:56'),('ord_admin002','ORD20260615001','res_admin_test05','mch_demo001','pc_demo001','fam_admin002','cancelled',68.00,2.00,'wechat',0,0.00,68.00,NULL,NULL,NULL,'2026-06-15 04:20:00','商家库存不足',0,0.00,NULL,'华帝·阳光花园4栋301','13911110005',NULL,0,'2026-06-15 04:00:00','2026-07-03 08:02:56'),('ord_admin003','ORD20260620001','res_admin_test04','mch_demo003','pc_demo001','fam_admin001','refunding',128.00,3.00,'wechat',0,0.00,128.00,NULL,NULL,NULL,NULL,'商品质量问题',0,0.00,NULL,'华帝·阳光花园2栋202','13911110004',NULL,0,'2026-06-20 01:00:00','2026-07-03 08:02:56'),('ord_admin004','ORD20260625001','res_admin_test05','mch_demo002','pc_demo001','fam_admin002','refunded',35.00,0.00,'property_coin',0,35.00,0.00,NULL,NULL,NULL,'2026-06-26 02:00:00','发错商品已退款',0,0.00,NULL,'华帝·阳光花园4栋301','13911110005',NULL,0,'2026-06-25 06:00:00','2026-07-03 08:02:56'),('ord_admin006','ORD20260703002','res_admin_test05','mch_demo002','pc_demo001','fam_admin002','delivering',89.50,0.00,'wechat',0,0.00,89.50,NULL,NULL,NULL,NULL,NULL,500,5.00,NULL,'华帝·阳光花园4栋301','13911110005',NULL,0,'2026-07-03 03:00:00','2026-07-03 08:02:56'),('ord_admin007','ORD20260701002','res_admin_test07','mch_demo001','pc_demo001','fam_admin003','completed',72.00,2.00,'wechat',0,0.00,72.00,NULL,NULL,'2026-07-01 04:30:00',NULL,NULL,400,4.00,NULL,'华帝·翠湖名苑1栋101','13911110007',NULL,0,'2026-07-01 03:30:00','2026-07-03 08:02:56'),('ord_admin008','ORD20260702002','res_admin_test08','mch_demo003','pc_demo001','fam_admin004','completed',95.00,3.00,'mixed',300,10.00,82.00,NULL,NULL,'2026-07-02 08:00:00',NULL,NULL,600,6.00,NULL,'华帝·翠湖名苑2栋202','13911110008',NULL,0,'2026-07-02 07:00:00','2026-07-03 08:02:56'),('ord_chart001','ORD20260705021','res_001','mch_demo001','pc_demo001',NULL,'completed',35.00,1.00,NULL,0,0.00,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'3栋2单元201','139****0005',NULL,0,'2026-07-04 23:50:00','2026-07-05 06:24:46'),('ord_chart002','ORD20260705022','res_002','mch_demo001','pc_demo001',NULL,'completed',28.00,1.00,NULL,0,0.00,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'3栋2单元202','139****0006',NULL,0,'2026-07-05 00:05:00','2026-07-05 06:24:46'),('ord_chart003','ORD20260705023','res_003','mch_demo002','pc_demo001',NULL,'completed',42.00,1.00,NULL,0,0.00,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'5栋1单元101','139****0010',NULL,0,'2026-07-05 00:50:00','2026-07-05 06:24:46'),('ord_chart004','ORD20260705024','res_004','mch_demo002','pc_demo001',NULL,'completed',55.00,1.50,NULL,0,0.00,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'4栋3单元301','139****0007',NULL,0,'2026-07-05 01:40:00','2026-07-05 06:24:46'),('ord_chart005','ORD20260705025','res_001','mch_demo001','pc_demo001',NULL,'completed',18.00,1.00,NULL,0,0.00,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'2栋1单元101','139****0008',NULL,0,'2026-07-05 02:45:00','2026-07-05 06:24:46'),('ord_chart006','ORD20260705026','res_002','mch_demo003','pc_demo001',NULL,'completed',66.00,1.00,NULL,0,0.00,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'3栋2单元201','139****0005',NULL,0,'2026-07-05 04:00:00','2026-07-05 06:24:46'),('ord_chart007','ORD20260705027','res_003','mch_demo001','pc_demo001',NULL,'completed',88.00,1.50,NULL,0,0.00,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'5栋1单元101','139****0010',NULL,0,'2026-07-05 05:20:00','2026-07-05 06:24:46'),('ord_chart008','ORD20260705028','res_004','mch_demo002','pc_demo001',NULL,'completed',32.00,1.00,NULL,0,0.00,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'6栋2单元201','139****0011',NULL,0,'2026-07-05 05:45:00','2026-07-05 06:24:46'),('ord_chart009','ORD20260705029','res_001','mch_demo003','pc_demo001',NULL,'completed',45.00,1.00,NULL,0,0.00,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'2栋1单元101','139****0008',NULL,0,'2026-07-05 06:00:00','2026-07-05 06:24:46'),('ord_chart010','ORD20260705030','res_002','mch_demo001','pc_demo001',NULL,'completed',72.00,1.50,NULL,0,0.00,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'4栋3单元301','139****0007',NULL,0,'2026-07-05 06:30:00','2026-07-05 06:24:46'),('ord_chart011','ORD20260705031','res_003','mch_demo002','pc_demo001',NULL,'paid',29.00,1.00,NULL,0,0.00,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'3栋2单元202','139****0006',NULL,0,'2026-07-05 07:00:00','2026-07-05 06:24:46'),('ord_chart012','ORD20260705032','res_004','mch_demo001','pc_demo001',NULL,'paid',38.00,1.00,NULL,0,0.00,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'5栋1单元101','139****0010',NULL,0,'2026-07-05 07:20:00','2026-07-05 06:24:46'),('ord_dev001','ORD20260701001','res_dev001','mch_demo002','pc_demo001','fam_dev001','completed',88.00,0.00,'wechat',0,0.00,88.00,NULL,NULL,'2026-07-01 02:00:00',NULL,NULL,500,5.00,NULL,'华帝·阳光花园3栋1201','13800138000',NULL,0,'2026-07-01 01:00:00','2026-07-03 06:23:28'),('ord_dev002','ORD20260702001','res_dev001','mch_demo001','pc_demo001','fam_dev001','completed',63.00,2.00,'property_coin',0,20.00,41.00,NULL,NULL,'2026-07-02 05:00:00',NULL,NULL,300,3.00,NULL,'华帝·阳光花园3栋1201','13800138000',NULL,0,'2026-07-02 04:00:00','2026-07-03 06:23:28'),('ord_dev003','ORD20260703001','res_dev001','mch_demo003','pc_demo001','fam_dev001','completed',126.50,3.00,'mixed',200,10.00,113.50,NULL,NULL,'2026-07-03 02:30:00',NULL,NULL,800,8.00,NULL,'华帝·阳光花园3栋1201','13800138000',NULL,0,'2026-07-03 01:30:00','2026-07-03 06:23:28'),('ord_dev004','ORD20260704001','res_dev001','mch_demo002','pc_demo001','fam_dev001','completed',156.00,0.00,'wechat',0,0.00,156.00,NULL,NULL,'2026-07-04 09:00:00',NULL,NULL,1000,10.00,NULL,'华帝·阳光花园3栋1201','13800138000',NULL,0,'2026-07-04 08:00:00','2026-07-03 06:23:28'),('ord_dev005','ORD20260705001','res_dev001','mch_demo001','pc_demo001','fam_dev001','completed',45.00,2.00,'property_coin',0,10.00,33.00,NULL,NULL,'2026-07-05 11:00:00',NULL,NULL,200,2.00,NULL,'华帝·阳光花园3栋1201','13800138000',NULL,0,'2026-07-05 10:00:00','2026-07-03 06:23:28'),('ord_dev006','ORD20260706001','res_dev001','mch_demo003','pc_demo001','fam_dev001','completed',98.00,3.00,'wechat',0,0.00,98.00,NULL,NULL,'2026-07-06 03:00:00',NULL,NULL,500,5.00,NULL,'华帝·阳光花园3栋1201','13800138000',NULL,0,'2026-07-06 02:00:00','2026-07-03 06:23:28'),('ord_dev007','ORD20260707001','res_dev001','mch_demo002','pc_demo001','fam_dev001','completed',238.00,0.00,'wechat',0,0.00,238.00,NULL,NULL,'2026-07-07 07:00:00',NULL,NULL,1500,15.00,NULL,'华帝·阳光花园3栋1201','13800138000',NULL,0,'2026-07-07 06:00:00','2026-07-03 06:23:28'),('ord_dev008','ORD20260708001','res_dev001','mch_demo001','pc_demo001','fam_dev001','completed',82.00,2.00,'mixed',100,5.00,74.50,NULL,NULL,'2026-07-08 05:00:00',NULL,NULL,300,3.00,NULL,'华帝·阳光花园3栋1201','13800138000',NULL,0,'2026-07-08 04:00:00','2026-07-03 06:23:28'),('ord_dev009','ORD20260709001','res_dev001','mch_demo003','pc_demo001','fam_dev001','completed',68.00,3.00,'wechat',0,0.00,68.00,NULL,NULL,'2026-07-09 02:00:00',NULL,NULL,200,2.00,NULL,'华帝·阳光花园3栋1201','13800138000',NULL,0,'2026-07-09 01:00:00','2026-07-03 06:23:28'),('ord_dev010','ORD20260710001','res_dev001','mch_demo002','pc_demo001','fam_dev001','pending',199.00,0.00,'wechat',0,0.00,199.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'华帝·阳光花园3栋1201','13800138000',NULL,0,'2026-07-10 02:00:00','2026-07-03 06:23:28');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permission_change_logs`
--

DROP TABLE IF EXISTS `permission_change_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permission_change_logs` (
  `id` varchar(32) NOT NULL,
  `resident_id` varchar(32) NOT NULL COMMENT '被操作用户',
  `operator_id` varchar(32) NOT NULL COMMENT '操作人',
  `property_company_id` varchar(32) NOT NULL,
  `action` varchar(20) NOT NULL COMMENT 'grant/revoke/apply_preset',
  `permission_code` varchar(50) DEFAULT NULL COMMENT '权限编码',
  `preset_code` varchar(50) DEFAULT NULL COMMENT '应用的预设编码',
  `before_value` varchar(200) DEFAULT NULL COMMENT '变更前',
  `after_value` varchar(200) DEFAULT NULL COMMENT '变更后',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resident` (`resident_id`),
  KEY `idx_operator` (`operator_id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `permission_change_logs_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `permission_change_logs_ibfk_2` FOREIGN KEY (`operator_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `permission_change_logs_ibfk_3` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限变更日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permission_change_logs`
--

LOCK TABLES `permission_change_logs` WRITE;
/*!40000 ALTER TABLE `permission_change_logs` DISABLE KEYS */;
INSERT INTO `permission_change_logs` VALUES ('pcl_13941000','res_001','res_admin01','pc_demo001','grant','activity_group:manage',NULL,'false','true','2026-07-05 03:40:17','2026-07-05 03:40:17'),('pcl_13d21000','res_001','res_admin01','pc_demo001','grant','announcement:publish',NULL,'false','true','2026-07-05 03:40:17','2026-07-05 03:40:17'),('pcl_13dc1000','res_001','res_admin01','pc_demo001','grant','delivery:grab',NULL,'false','true','2026-07-05 03:40:17','2026-07-05 03:40:17'),('pcl_13e61000','res_001','res_admin01','pc_demo001','grant','promotion:push',NULL,'false','true','2026-07-05 03:40:17','2026-07-05 03:40:17'),('pcl_13f21000','res_001','res_admin01','pc_demo001','grant','rank:drag',NULL,'false','true','2026-07-05 03:40:17','2026-07-05 03:40:17'),('pcl_13fc1000','res_001','res_admin01','pc_demo001','grant','sector:manage',NULL,'false','true','2026-07-05 03:40:17','2026-07-05 03:40:17'),('pcl_fdf81000','res_002','res_admin01','pc_demo001','grant','coin:audit',NULL,'false','true','2026-07-05 03:40:47','2026-07-05 03:40:47');
/*!40000 ALTER TABLE `permission_change_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permissions`
--

DROP TABLE IF EXISTS `permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permissions` (
  `id` varchar(32) NOT NULL COMMENT 'perm_xxxxxxxx',
  `code` varchar(50) NOT NULL COMMENT '权限编码',
  `name` varchar(100) NOT NULL COMMENT '权限名称',
  `description` varchar(200) DEFAULT NULL,
  `module` varchar(50) DEFAULT NULL COMMENT '所属模块',
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_code` (`code`),
  KEY `idx_module` (`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限池表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permissions`
--

LOCK TABLES `permissions` WRITE;
/*!40000 ALTER TABLE `permissions` DISABLE KEYS */;
INSERT INTO `permissions` VALUES ('perm_001','announcement.publish','发布公告',NULL,'announcement','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_002','merchant.audit','商家审核',NULL,'merchant','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_003','merchant.kickout','踢出商家',NULL,'merchant','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_004','rank.drag','排名拖拽',NULL,'rank','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_005','promotion.push','特惠推送',NULL,'promotion','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_006','activity_group.manage','活动组管理',NULL,'activity_group','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_007','sector.manage','板块管理',NULL,'sector','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_008','individual.manage','个体负责人管理',NULL,'individual','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_009','delivery.grab','送货抢单',NULL,'delivery','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_010','data.view','数据查看',NULL,'data','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_011','notice.collect','通知征集',NULL,'notice','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_012','refund.approve','退费审批',NULL,'refund','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_013','points.input','积分录入',NULL,'points','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_014','coin.freeze','物业币冻结',NULL,'coin','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_015','coin.display_control','物业币显示控制',NULL,'coin','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_016','config.set','参数配置',NULL,'config','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_017','property_fee.manage','物业费管理',NULL,'property_fee','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_018','reports.view','报表查看',NULL,'reports','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_019','user.manage','用户管理',NULL,'user','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_020','permission.manage','权限管理',NULL,'permission','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_021','permission.read','权限查看',NULL,'permission','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_022','pool.read','积分池查看',NULL,'pool','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_023','pool.adjust','积分池调整',NULL,'pool','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_024','coordinator.read','统筹查看',NULL,'coordinator','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_025','coordinator.manage','统筹管理',NULL,'coordinator','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_026','sector.read','板块负责人查看',NULL,'sector','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_027','distribution.read','分成记录查看',NULL,'distribution','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_028','coin.audit','物业币兑换审核',NULL,'coin','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_029','point.clear_run','2年积分清零',NULL,'points','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_030','log.read','操作日志查看',NULL,'log','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_031','merchant.create','创建平台商家',NULL,'merchant','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_032','merchant.read','平台商家查看',NULL,'merchant','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_033','merchant.update','更新平台商家',NULL,'merchant','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_034','property_company.manage','物业公司管理',NULL,'property_company','active','2026-07-02 01:57:36','2026-07-02 01:57:36');
/*!40000 ALTER TABLE `permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `platform_merchants`
--

DROP TABLE IF EXISTS `platform_merchants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `platform_merchants` (
  `id` varchar(32) NOT NULL COMMENT 'pm_xxxxxxxx',
  `name` varchar(100) NOT NULL COMMENT '商家名称',
  `description` text,
  `cover_url` varchar(500) DEFAULT NULL,
  `address` varchar(200) DEFAULT NULL,
  `contact_phone` varchar(20) DEFAULT NULL,
  `category` varchar(50) DEFAULT NULL COMMENT '外卖/周围商家/自营/农家/外卖小吃/团购',
  `business_hours` varchar(50) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='平台级商家库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `platform_merchants`
--

LOCK TABLES `platform_merchants` WRITE;
/*!40000 ALTER TABLE `platform_merchants` DISABLE KEYS */;
INSERT INTO `platform_merchants` VALUES ('pm_b6c61000','测试商家',NULL,NULL,NULL,'13800000000','外卖',NULL,'active','2026-07-03 01:59:18','2026-07-03 01:59:18'),('pm_dc0e1000','1','11111','1','11','11111111111','外卖',NULL,'active','2026-07-05 03:37:26','2026-07-05 03:37:26'),('pm_demo001','口福餐厅',NULL,NULL,NULL,NULL,'餐饮',NULL,'active','2026-07-05 06:24:24','2026-07-05 06:24:24'),('pm_demo002','便民超市',NULL,NULL,NULL,NULL,'超市',NULL,'active','2026-07-05 06:24:24','2026-07-05 06:24:24'),('pm_demo003','绿色农场',NULL,NULL,NULL,NULL,'生鲜',NULL,'active','2026-07-05 06:24:24','2026-07-05 06:24:24'),('pm_ecf21000','我的超市',NULL,NULL,'小区东门','13812345678','外卖',NULL,'active','2026-07-05 05:47:27','2026-07-05 05:47:27'),('pm_test001','测试外卖商家','测试用的外卖店铺',NULL,'小区东门','13900001111','外卖','08:00-22:00','active','2026-07-03 03:51:21','2026-07-03 03:51:21');
/*!40000 ALTER TABLE `platform_merchants` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `point_pool_records`
--

DROP TABLE IF EXISTS `point_pool_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `point_pool_records` (
  `id` varchar(32) NOT NULL COMMENT 'ppr_xxxxxxxx',
  `property_company_id` varchar(32) NOT NULL,
  `pool_type` varchar(30) NOT NULL COMMENT 'pool_in/pool_out',
  `amount` decimal(10,2) NOT NULL,
  `balance` decimal(10,2) NOT NULL,
  `source` varchar(30) DEFAULT NULL COMMENT 'exchange_diff/coin_expired/fee_clear/spend',
  `source_id` varchar(32) DEFAULT NULL,
  `description` varchar(200) DEFAULT NULL,
  `operator_id` varchar(32) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_source` (`source`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `point_pool_records_ibfk_1` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分池进出记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `point_pool_records`
--

LOCK TABLES `point_pool_records` WRITE;
/*!40000 ALTER TABLE `point_pool_records` DISABLE KEYS */;
INSERT INTO `point_pool_records` VALUES ('ppr_admin001','pc_demo001','pool_out',500.00,49500.00,'spend',NULL,'积分兑换商品支出',NULL,'2026-06-15 02:00:00','2026-07-03 08:02:56'),('ppr_admin002','pc_demo001','pool_in',2000.00,51500.00,'exchange_diff',NULL,'商家积分购买注入',NULL,'2026-06-20 06:00:00','2026-07-03 08:02:56'),('ppr_admin003','pc_demo001','pool_out',800.00,50700.00,'fee_clear',NULL,'物业费积分抵扣结算',NULL,'2026-06-30 15:59:59','2026-07-03 08:02:56'),('ppr_dev001','pc_demo001','pool_in',50000.00,50000.00,'exchange_diff',NULL,'积分池初始资金',NULL,'2025-12-31 16:00:00','2026-07-03 06:53:28'),('ppr_dev002','pc_demo001','pool_out',5000.00,45000.00,'fee_clear',NULL,'Q1物业费积分抵扣',NULL,'2026-03-31 15:59:59','2026-07-03 06:53:28'),('ppr_dev003','pc_demo001','pool_out',5000.00,40000.00,'fee_clear',NULL,'Q2物业费积分抵扣',NULL,'2026-06-30 15:59:59','2026-07-03 06:53:28'),('ppr_dev004','pc_demo001','pool_in',10000.00,50000.00,'exchange_diff',NULL,'商家积分购买注入',NULL,'2026-06-30 16:00:00','2026-07-03 06:53:28'),('ppr_init_001','pc_demo001','pool_in',37950.00,37950.00,'manual_adjust',NULL,'初始化积分池（同步用户积分）',NULL,'2026-07-05 05:29:15','2026-07-05 05:29:15');
/*!40000 ALTER TABLE `point_pool_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `point_pools`
--

DROP TABLE IF EXISTS `point_pools`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `point_pools` (
  `id` varchar(32) NOT NULL COMMENT 'pp_xxxxxxxx',
  `property_company_id` varchar(32) NOT NULL,
  `balance` decimal(10,2) DEFAULT '0.00' COMMENT '积分池资金余额',
  `total_in` decimal(10,2) DEFAULT '0.00' COMMENT '累计流入',
  `total_out` decimal(10,2) DEFAULT '0.00' COMMENT '累计流出',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_property_company` (`property_company_id`),
  CONSTRAINT `point_pools_ibfk_1` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分池表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `point_pools`
--

LOCK TABLES `point_pools` WRITE;
/*!40000 ALTER TABLE `point_pools` DISABLE KEYS */;
INSERT INTO `point_pools` VALUES ('pp_demo001','pc_demo001',37950.00,37950.00,0.00,'2026-07-02 01:57:36','2026-07-05 05:29:15');
/*!40000 ALTER TABLE `point_pools` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `point_records`
--

DROP TABLE IF EXISTS `point_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `point_records` (
  `id` varchar(32) NOT NULL COMMENT 'pt_xxxxxxxx',
  `resident_id` varchar(32) NOT NULL,
  `family_id` varchar(32) DEFAULT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `point_type` varchar(30) NOT NULL COMMENT 'earn/spend/transfer/expire/pool_in',
  `amount` int NOT NULL COMMENT '正数增加，负数减少',
  `balance` int NOT NULL COMMENT '操作后余额',
  `source` varchar(30) DEFAULT NULL COMMENT 'order/activity/sign_in/manual/merchant_grant/fee_deduct/pool_diff',
  `source_id` varchar(32) DEFAULT NULL,
  `description` varchar(200) DEFAULT NULL,
  `operator_id` varchar(32) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resident` (`resident_id`),
  KEY `idx_family` (`family_id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_source` (`source`,`source_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `point_records_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `point_records_ibfk_2` FOREIGN KEY (`family_id`) REFERENCES `families` (`id`),
  CONSTRAINT `point_records_ibfk_3` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `point_records`
--

LOCK TABLES `point_records` WRITE;
/*!40000 ALTER TABLE `point_records` DISABLE KEYS */;
INSERT INTO `point_records` VALUES ('pt_0c481000','res_dev001','fam_dev001','pc_demo001','transfer',-1,12579,'transfer','res_dev004','家庭内积分转移',NULL,'2026-07-03 18:23:37','2026-07-03 18:23:37'),('pt_0c881000','res_dev004','fam_dev001','pc_demo001','transfer',1,625,'transfer','res_dev001','家庭内积分转移',NULL,'2026-07-03 18:23:37','2026-07-03 18:23:37'),('pt_72de1000','res_48fa1000','fam_1ea41000','pc_demo001','earn',50,50,'manual',NULL,'测试发放','res_admin01','2026-07-05 02:18:34','2026-07-05 02:18:34'),('pt_cc6c1000','res_dev001','fam_dev001','pc_demo001','earn',100,12580,'on_time_payment_reward','pf_dev013','准时缴费奖励',NULL,'2026-07-03 08:01:26','2026-07-03 08:01:26'),('pt_dev001','res_dev001','fam_dev001','pc_demo001','earn',500,500,'order',NULL,'万达超市·日用品消费',NULL,'2026-06-01 06:30:00','2026-07-03 06:23:28'),('pt_dev002','res_dev001','fam_dev001','pc_demo001','earn',200,700,'order',NULL,'幸福食堂·午餐消费',NULL,'2026-06-05 04:15:00','2026-07-03 06:23:28'),('pt_dev003','res_dev001','fam_dev001','pc_demo001','earn',100,800,'order',NULL,'阳光补习·数学课',NULL,'2026-06-08 10:00:00','2026-07-03 06:23:28'),('pt_dev004','res_dev001','fam_dev001','pc_demo001','earn',800,1600,'order',NULL,'家电购买奖励',NULL,'2026-06-12 06:00:00','2026-07-03 06:23:28'),('pt_dev005','res_dev001','fam_dev001','pc_demo001','earn',300,1900,'order',NULL,'有机蔬菜组合购买',NULL,'2026-06-15 01:30:00','2026-07-03 06:23:28'),('pt_dev006','res_dev001','fam_dev001','pc_demo001','earn',5,1905,'sign_in',NULL,'每日签到',NULL,'2026-06-16 00:00:00','2026-07-03 06:23:28'),('pt_dev007','res_dev001','fam_dev001','pc_demo001','earn',5,1910,'sign_in',NULL,'每日签到',NULL,'2026-06-17 00:00:00','2026-07-03 06:23:28'),('pt_dev008','res_dev001','fam_dev001','pc_demo001','earn',5,1915,'sign_in',NULL,'每日签到',NULL,'2026-06-18 00:00:00','2026-07-03 06:23:28'),('pt_dev009','res_dev001','fam_dev001','pc_demo001','earn',5,1920,'sign_in',NULL,'每日签到',NULL,'2026-06-19 00:00:00','2026-07-03 06:23:28'),('pt_dev010','res_dev001','fam_dev001','pc_demo001','earn',5,1925,'sign_in',NULL,'每日签到',NULL,'2026-06-20 00:00:00','2026-07-03 06:23:28'),('pt_dev011','res_dev001','fam_dev001','pc_demo001','earn',5,1930,'sign_in',NULL,'每日签到',NULL,'2026-06-21 00:00:00','2026-07-03 06:23:28'),('pt_dev012','res_dev001','fam_dev001','pc_demo001','earn',5,1935,'sign_in',NULL,'每日签到',NULL,'2026-06-22 00:00:00','2026-07-03 06:23:28'),('pt_dev013','res_dev001','fam_dev001','pc_demo001','earn',500,2435,'activity',NULL,'社区志愿者活动奖励',NULL,'2026-06-23 08:30:00','2026-07-03 06:23:28'),('pt_dev014','res_dev001','fam_dev001','pc_demo001','earn',400,2835,'activity',NULL,'满意度问卷调查完成',NULL,'2026-06-24 01:12:00','2026-07-03 06:23:28'),('pt_dev015','res_dev001','fam_dev001','pc_demo001','earn',1500,4335,'order',NULL,'缴纳物业费奖励',NULL,'2026-06-25 06:20:00','2026-07-03 06:23:28'),('pt_dev016','res_dev001','fam_dev001','pc_demo001','spend',-850,3485,'fee_deduct',NULL,'礼品兑换 - 品牌大米',NULL,'2026-06-26 02:45:00','2026-07-03 06:23:28'),('pt_dev017','res_dev001','fam_dev001','pc_demo001','spend',-3200,285,'pool_diff',NULL,'积分兑换物业币',NULL,'2026-06-27 03:00:00','2026-07-03 06:23:28'),('pt_dev018','res_dev001','fam_dev001','pc_demo001','earn',2000,2285,'order',NULL,'大额订单消费',NULL,'2026-06-28 10:00:00','2026-07-03 06:23:28'),('pt_dev019','res_dev001','fam_dev001','pc_demo001','earn',500,2785,'order',NULL,'日用品消费',NULL,'2026-06-29 07:00:00','2026-07-03 06:23:28'),('pt_dev020','res_dev001','fam_dev001','pc_demo001','earn',300,3085,'order',NULL,'生鲜配送',NULL,'2026-06-30 02:00:00','2026-07-03 06:23:28'),('pt_dev021','res_dev001','fam_dev001','pc_demo001','earn',500,3585,'order',NULL,'社区团购订单',NULL,'2026-07-01 01:00:00','2026-07-03 06:23:28'),('pt_dev022','res_dev001','fam_dev001','pc_demo001','earn',200,3785,'sign_in',NULL,'连续签到奖励',NULL,'2026-07-01 00:00:00','2026-07-03 06:23:28'),('pt_dev023','res_dev001','fam_dev001','pc_demo001','earn',800,4585,'order',NULL,'家电维修服务',NULL,'2026-07-02 06:00:00','2026-07-03 06:23:28'),('pt_dev024','res_dev001','fam_dev001','pc_demo001','earn',500,5085,'order',NULL,'超市购物',NULL,'2026-07-02 08:00:00','2026-07-03 06:23:28'),('pt_dev025','res_dev001','fam_dev001','pc_demo001','earn',100,5185,'order',NULL,'食堂午餐',NULL,'2026-07-02 04:00:00','2026-07-03 06:23:28'),('pt_dev026','res_dev001','fam_dev001','pc_demo001','earn',300,5485,'order',NULL,'洗衣服务',NULL,'2026-07-03 01:00:00','2026-07-03 06:23:28'),('pt_dev027','res_dev001','fam_dev001','pc_demo001','earn',200,5685,'order',NULL,'宠物美容',NULL,'2026-07-03 02:30:00','2026-07-03 06:23:28'),('pt_dev028','res_dev001','fam_dev001','pc_demo001','earn',500,6185,'order',NULL,'早教课程',NULL,'2026-07-03 06:00:00','2026-07-03 06:23:28'),('pt_dev029','res_dev001','fam_dev001','pc_demo001','earn',300,6485,'order',NULL,'家政服务',NULL,'2026-07-03 07:30:00','2026-07-03 06:23:28'),('pt_dev030','res_dev001','fam_dev001','pc_demo001','earn',1000,7485,'order',NULL,'大家电购买',NULL,'2026-07-04 03:00:00','2026-07-03 06:23:28'),('pt_dev031','res_dev001','fam_dev001','pc_demo001','earn',500,7985,'order',NULL,'超市大采购',NULL,'2026-07-04 08:00:00','2026-07-03 06:23:28'),('pt_dev032','res_dev001','fam_dev001','pc_demo001','earn',200,8185,'order',NULL,'外卖订单',NULL,'2026-07-04 10:30:00','2026-07-03 06:23:28'),('pt_dev033','res_dev001','fam_dev001','pc_demo001','earn',100,8285,'order',NULL,'饮品订单',NULL,'2026-07-05 02:00:00','2026-07-03 06:23:28'),('pt_dev034','res_dev001','fam_dev001','pc_demo001','earn',300,8585,'order',NULL,'社区活动报名',NULL,'2026-07-05 06:00:00','2026-07-03 06:23:28'),('pt_dev035','res_dev001','fam_dev001','pc_demo001','earn',200,8785,'order',NULL,'便利店购物',NULL,'2026-07-05 09:00:00','2026-07-03 06:23:28'),('pt_dev036','res_dev001','fam_dev001','pc_demo001','earn',500,9285,'order',NULL,'健身卡续费',NULL,'2026-07-06 01:00:00','2026-07-03 06:23:28'),('pt_dev037','res_dev001','fam_dev001','pc_demo001','earn',300,9585,'order',NULL,'瑜伽课程',NULL,'2026-07-06 07:00:00','2026-07-03 06:23:28'),('pt_dev038','res_dev001','fam_dev001','pc_demo001','earn',200,9785,'order',NULL,'水果采购',NULL,'2026-07-06 09:30:00','2026-07-03 06:23:28'),('pt_dev039','res_dev001','fam_dev001','pc_demo001','earn',500,10285,'order',NULL,'教育培训',NULL,'2026-07-07 02:00:00','2026-07-03 06:23:28'),('pt_dev040','res_dev001','fam_dev001','pc_demo001','earn',300,10585,'order',NULL,'美容美发',NULL,'2026-07-07 06:00:00','2026-07-03 06:23:28'),('pt_dev041','res_dev001','fam_dev001','pc_demo001','earn',200,10785,'order',NULL,'鲜花订购',NULL,'2026-07-07 08:00:00','2026-07-03 06:23:28'),('pt_dev042','res_dev001','fam_dev001','pc_demo001','earn',500,11285,'order',NULL,'家电清洗',NULL,'2026-07-08 01:00:00','2026-07-03 06:23:28'),('pt_dev043','res_dev001','fam_dev001','pc_demo001','earn',300,11585,'order',NULL,'搬家服务',NULL,'2026-07-08 03:00:00','2026-07-03 06:23:28'),('pt_dev044','res_dev001','fam_dev001','pc_demo001','earn',200,11785,'order',NULL,'干洗服务',NULL,'2026-07-08 06:00:00','2026-07-03 06:23:28'),('pt_dev045','res_dev001','fam_dev001','pc_demo001','earn',300,12085,'order',NULL,'宠物寄养',NULL,'2026-07-09 01:00:00','2026-07-03 06:23:28'),('pt_dev046','res_dev001','fam_dev001','pc_demo001','earn',200,12285,'order',NULL,'鲜花配送',NULL,'2026-07-09 06:00:00','2026-07-03 06:23:28'),('pt_dev047','res_dev001','fam_dev001','pc_demo001','earn',195,12480,'order',NULL,'最后一笔消费',NULL,'2026-07-10 02:00:00','2026-07-03 06:23:28'),('pt_dev048','res_dev001','fam_dev001','pc_demo001','spend',-500,11980,'fee_deduct',NULL,'Q1物业费积分抵扣',NULL,'2026-03-15 02:00:00','2026-07-03 06:23:28'),('pt_dev049','res_dev001','fam_dev001','pc_demo001','spend',-500,11480,'fee_deduct',NULL,'Q2物业费积分抵扣',NULL,'2026-06-15 02:00:00','2026-07-03 06:23:28');
/*!40000 ALTER TABLE `point_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `point_rules`
--

DROP TABLE IF EXISTS `point_rules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `point_rules` (
  `id` varchar(32) NOT NULL COMMENT 'prr_xxxxxxxx',
  `name` varchar(100) NOT NULL,
  `description` text,
  `point_type` varchar(30) NOT NULL COMMENT 'earn/spend/redeem/expire',
  `source` varchar(30) NOT NULL,
  `rate` decimal(10,4) DEFAULT NULL COMMENT '兑换比率',
  `min_amount` decimal(10,2) DEFAULT NULL,
  `max_amount` decimal(10,2) DEFAULT NULL,
  `min_points` int DEFAULT NULL,
  `max_points` int DEFAULT NULL,
  `daily_limit` int DEFAULT NULL,
  `monthly_limit` int DEFAULT NULL,
  `expire_days` int DEFAULT NULL COMMENT '过期天数(730=2年)',
  `property_company_id` varchar(32) DEFAULT NULL COMMENT 'null为全局',
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_type` (`point_type`),
  KEY `idx_source` (`source`),
  CONSTRAINT `point_rules_ibfk_1` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `point_rules`
--

LOCK TABLES `point_rules` WRITE;
/*!40000 ALTER TABLE `point_rules` DISABLE KEYS */;
/*!40000 ALTER TABLE `point_rules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` varchar(32) NOT NULL COMMENT 'prd_xxxxxxxx',
  `merchant_id` varchar(32) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` text,
  `cover_url` varchar(500) DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `member_price` decimal(10,2) DEFAULT NULL COMMENT '业主专属折扣价',
  `point_price` int DEFAULT NULL COMMENT '积分兑换价格',
  `stock` int DEFAULT '0',
  `category` varchar(50) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'active' COMMENT 'active/inactive/sold_out',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_category` (`category`),
  CONSTRAINT `products_ibfk_1` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `property_coin_records`
--

DROP TABLE IF EXISTS `property_coin_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `property_coin_records` (
  `id` varchar(32) NOT NULL COMMENT 'coin_xxxxxxxx',
  `resident_id` varchar(32) NOT NULL,
  `family_id` varchar(32) DEFAULT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `coin_type` varchar(30) NOT NULL COMMENT 'earn/spend/recharge/withdraw/expire/freeze/unfreeze',
  `amount` decimal(10,2) NOT NULL,
  `balance` decimal(10,2) NOT NULL,
  `source` varchar(30) DEFAULT NULL COMMENT 'order/property_fee/manual/coin_expired',
  `source_id` varchar(32) DEFAULT NULL,
  `description` varchar(200) DEFAULT NULL,
  `operator_id` varchar(32) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resident` (`resident_id`),
  KEY `idx_family` (`family_id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `property_coin_records_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `property_coin_records_ibfk_2` FOREIGN KEY (`family_id`) REFERENCES `families` (`id`),
  CONSTRAINT `property_coin_records_ibfk_3` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业币记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `property_coin_records`
--

LOCK TABLES `property_coin_records` WRITE;
/*!40000 ALTER TABLE `property_coin_records` DISABLE KEYS */;
INSERT INTO `property_coin_records` VALUES ('coin_2a3c1000','res_e7de1000',NULL,'pc_demo001','recharge',10.00,10.00,'manual',NULL,'物业币充值',NULL,'2026-07-03 01:30:03','2026-07-03 01:30:03'),('coin_2d3e1000','res_48fa1000','fam_1ea41000','pc_demo001','earn',5.00,5.00,'manual',NULL,'测试发放','res_admin01','2026-07-05 02:23:53','2026-07-05 02:23:53'),('coin_d7aa1000','res_e7de1000','fam_5f4e1000','pc_demo001','recharge',10.00,10.00,'manual',NULL,'物业币充值',NULL,'2026-07-03 06:03:29','2026-07-03 06:03:29'),('coin_dev001','res_dev001','fam_dev001','pc_demo001','earn',50.00,50.00,'order',NULL,'万达超市消费返币',NULL,'2026-06-01 06:30:00','2026-07-03 06:23:28'),('coin_dev002','res_dev001','fam_dev001','pc_demo001','earn',30.00,80.00,'order',NULL,'幸福食堂消费返币',NULL,'2026-06-05 04:15:00','2026-07-03 06:23:28'),('coin_dev003','res_dev001','fam_dev001','pc_demo001','earn',20.00,100.00,'order',NULL,'阳光补习消费返币',NULL,'2026-06-08 10:00:00','2026-07-03 06:23:28'),('coin_dev004','res_dev001','fam_dev001','pc_demo001','earn',100.00,200.00,'order',NULL,'家电购买返币',NULL,'2026-06-12 06:00:00','2026-07-03 06:23:28'),('coin_dev005','res_dev001','fam_dev001','pc_demo001','earn',50.00,250.00,'order',NULL,'有机蔬菜购买返币',NULL,'2026-06-15 01:30:00','2026-07-03 06:23:28'),('coin_dev006','res_dev001','fam_dev001','pc_demo001','spend',-50.00,200.00,'order',NULL,'外卖订单抵扣',NULL,'2026-06-20 10:00:00','2026-07-03 06:23:28'),('coin_dev007','res_dev001','fam_dev001','pc_demo001','earn',80.00,280.00,'order',NULL,'超市购物返币',NULL,'2026-06-25 08:00:00','2026-07-03 06:23:28'),('coin_dev008','res_dev001','fam_dev001','pc_demo001','spend',-30.00,250.00,'order',NULL,'日用品抵扣',NULL,'2026-06-28 07:00:00','2026-07-03 06:23:28'),('coin_dev009','res_dev001','fam_dev001','pc_demo001','earn',50.00,300.00,'order',NULL,'社区团购返币',NULL,'2026-07-01 01:00:00','2026-07-03 06:23:28'),('coin_dev010','res_dev001','fam_dev001','pc_demo001','earn',30.50,330.50,'order',NULL,'生鲜配送返币',NULL,'2026-07-02 02:00:00','2026-07-03 06:23:28'),('coin_dev011','res_dev001','fam_dev001','pc_demo001','earn',20.00,350.50,'order',NULL,'便利店返币',NULL,'2026-07-03 09:00:00','2026-07-03 06:23:28');
/*!40000 ALTER TABLE `property_coin_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `property_coin_rules`
--

DROP TABLE IF EXISTS `property_coin_rules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `property_coin_rules` (
  `id` varchar(32) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` text,
  `coin_type` varchar(30) NOT NULL,
  `source` varchar(30) NOT NULL,
  `rate` decimal(10,4) DEFAULT NULL,
  `min_amount` decimal(10,2) DEFAULT NULL,
  `max_amount` decimal(10,2) DEFAULT NULL,
  `daily_limit` decimal(10,2) DEFAULT NULL,
  `monthly_limit` decimal(10,2) DEFAULT NULL,
  `property_company_id` varchar(32) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_property_company` (`property_company_id`),
  CONSTRAINT `property_coin_rules_ibfk_1` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业币规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `property_coin_rules`
--

LOCK TABLES `property_coin_rules` WRITE;
/*!40000 ALTER TABLE `property_coin_rules` DISABLE KEYS */;
/*!40000 ALTER TABLE `property_coin_rules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `property_companies`
--

DROP TABLE IF EXISTS `property_companies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `property_companies` (
  `id` varchar(32) NOT NULL COMMENT 'pc_xxxxxxxx',
  `name` varchar(100) NOT NULL COMMENT '名称',
  `logo_url` varchar(500) DEFAULT NULL COMMENT 'Logo',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `address` varchar(200) DEFAULT NULL COMMENT '地址',
  `property_share_rate` decimal(5,4) DEFAULT '0.3000' COMMENT '物业分成比例',
  `coordinator_share_rate` decimal(5,4) DEFAULT '0.7000' COMMENT '统筹分成比例',
  `sector_leader_rate` decimal(5,4) DEFAULT '0.1000' COMMENT '板块负责人比例',
  `individual_leader_rate` decimal(5,4) DEFAULT '0.0000' COMMENT '个体负责人比例',
  `coin_display_enabled` tinyint(1) DEFAULT '1' COMMENT '物业币卡片全局显示',
  `coin_issue_mode` varchar(20) DEFAULT 'auto' COMMENT '发放模式',
  `coin_expiry_days` int DEFAULT '365' COMMENT '物业币有效期天数',
  `coin_freeze_default` tinyint(1) DEFAULT '0' COMMENT '新用户物业币默认冻结',
  `delivery_base_fee` decimal(10,2) DEFAULT '0.00' COMMENT '配送起步价',
  `delivery_courier_share_rate` decimal(5,4) DEFAULT '1.0000' COMMENT '快递员配送费分成比例',
  `delivery_courier_per_order` decimal(10,2) DEFAULT '0.35' COMMENT '快递员每单报酬',
  `withdrawal_fee_rate` decimal(5,4) DEFAULT '0.0060' COMMENT '提现手续费率',
  `point_to_fee_rate` decimal(10,4) DEFAULT '0.0100' COMMENT '积分抵扣物业费比率',
  `two_year_clear_enabled` tinyint(1) DEFAULT '1' COMMENT '2年欠费积分清零开关',
  `neighbor_daily_contact_limit` int DEFAULT '2' COMMENT '邻居私聊每日限制',
  `status` varchar(20) DEFAULT 'active' COMMENT 'active/inactive',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业公司表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `property_companies`
--

LOCK TABLES `property_companies` WRITE;
/*!40000 ALTER TABLE `property_companies` DISABLE KEYS */;
INSERT INTO `property_companies` VALUES ('pc_demo001','更新名称',NULL,NULL,NULL,0.3100,0.6900,0.1000,0.0000,1,'manual',366,1,0.50,1.0000,0.35,0.0060,0.0200,0,2,'active','2026-07-02 01:57:36','2026-07-05 06:07:01');
/*!40000 ALTER TABLE `property_companies` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `property_fee_rules`
--

DROP TABLE IF EXISTS `property_fee_rules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `property_fee_rules` (
  `id` varchar(32) NOT NULL,
  `name` varchar(100) NOT NULL,
  `fee_type` varchar(30) NOT NULL COMMENT 'property/parking/water/electricity',
  `calculation_method` varchar(30) NOT NULL COMMENT 'fixed/per_square_meter/per_unit',
  `unit_price` decimal(10,2) NOT NULL,
  `unit` varchar(20) DEFAULT NULL,
  `billing_cycle` varchar(20) NOT NULL COMMENT 'monthly/quarterly/yearly',
  `auto_deduct_enabled` tinyint(1) DEFAULT '0',
  `overdue_rate` decimal(5,4) DEFAULT NULL COMMENT '日滞纳金比例',
  `property_company_id` varchar(32) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_fee_type` (`fee_type`),
  CONSTRAINT `property_fee_rules_ibfk_1` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业费规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `property_fee_rules`
--

LOCK TABLES `property_fee_rules` WRITE;
/*!40000 ALTER TABLE `property_fee_rules` DISABLE KEYS */;
/*!40000 ALTER TABLE `property_fee_rules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `property_fees`
--

DROP TABLE IF EXISTS `property_fees`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `property_fees` (
  `id` varchar(32) NOT NULL,
  `resident_id` varchar(32) NOT NULL,
  `family_id` varchar(32) DEFAULT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `community_id` varchar(32) DEFAULT NULL,
  `fee_type` varchar(30) NOT NULL,
  `period` varchar(20) NOT NULL COMMENT '账期如2026-Q3',
  `amount` decimal(10,2) NOT NULL,
  `paid_amount` decimal(10,2) DEFAULT '0.00',
  `point_deducted` int DEFAULT '0' COMMENT '积分抵扣数量',
  `coin_deducted` decimal(10,2) DEFAULT '0.00' COMMENT '物业币抵扣金额',
  `payment_status` varchar(20) DEFAULT 'unpaid' COMMENT 'unpaid/partial/paid/overdue',
  `due_date` date DEFAULT NULL,
  `paid_at` timestamp NULL DEFAULT NULL,
  `auto_deduct` tinyint(1) DEFAULT '0',
  `late_fee` decimal(10,2) DEFAULT '0.00' COMMENT '滞纳金',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `family_id` (`family_id`),
  KEY `community_id` (`community_id`),
  KEY `idx_resident` (`resident_id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_status` (`payment_status`),
  KEY `idx_period` (`period`),
  CONSTRAINT `property_fees_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `property_fees_ibfk_2` FOREIGN KEY (`family_id`) REFERENCES `families` (`id`),
  CONSTRAINT `property_fees_ibfk_3` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`),
  CONSTRAINT `property_fees_ibfk_4` FOREIGN KEY (`community_id`) REFERENCES `communities` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业费表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `property_fees`
--

LOCK TABLES `property_fees` WRITE;
/*!40000 ALTER TABLE `property_fees` DISABLE KEYS */;
INSERT INTO `property_fees` VALUES ('pf_admin001','res_admin_test04','fam_admin001','pc_demo001','com_demo001','property','2026-Q1',360.00,180.00,0,0.00,'overdue','2026-03-31',NULL,0,15.00,'2026-07-03 08:02:56','2026-07-03 08:02:56'),('pf_admin002','res_admin_test04','fam_admin001','pc_demo001','com_demo001','parking','2026-Q1',200.00,0.00,0,0.00,'overdue','2026-03-31',NULL,0,8.50,'2026-07-03 08:02:56','2026-07-03 08:02:56'),('pf_admin003','res_admin_test05','fam_admin002','pc_demo001','com_demo001','property','2026-Q1',360.00,360.00,200,0.00,'paid','2026-03-31','2026-03-20 02:00:00',0,0.00,'2026-07-03 08:02:56','2026-07-03 08:02:56'),('pf_admin004','res_admin_test05','fam_admin002','pc_demo001','com_demo001','property','2026-Q2',360.00,360.00,0,50.00,'paid','2026-06-30','2026-06-15 02:00:00',0,0.00,'2026-07-03 08:02:56','2026-07-03 08:02:56'),('pf_admin005','res_admin_test05','fam_admin002','pc_demo001','com_demo001','parking','2026-Q3',200.00,0.00,0,0.00,'unpaid','2026-07-31',NULL,0,0.00,'2026-07-03 08:02:56','2026-07-03 08:02:56'),('pf_admin006','res_admin_test07','fam_admin003','pc_demo001','com_demo002','property','2026-Q3',300.00,100.00,0,0.00,'partial','2026-07-31','2026-07-01 02:00:00',0,0.00,'2026-07-03 08:02:56','2026-07-03 08:02:56'),('pf_dev001','res_dev001','fam_dev001','pc_demo001','com_demo001','property','2025-Q1',360.00,360.00,0,0.00,'paid','2025-03-31','2025-03-20 02:00:00',0,0.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('pf_dev002','res_dev001','fam_dev001','pc_demo001','com_demo001','property','2025-Q2',360.00,360.00,0,0.00,'paid','2025-06-30','2025-06-15 02:00:00',0,0.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('pf_dev003','res_dev001','fam_dev001','pc_demo001','com_demo001','property','2025-Q3',360.00,360.00,500,0.00,'paid','2025-09-30','2025-09-15 02:00:00',0,0.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('pf_dev004','res_dev001','fam_dev001','pc_demo001','com_demo001','property','2025-Q4',360.00,360.00,0,0.00,'paid','2025-12-31','2025-12-15 02:00:00',0,0.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('pf_dev005','res_dev001','fam_dev001','pc_demo001','com_demo001','property','2026-Q1',360.00,360.00,500,0.00,'paid','2026-03-31','2026-03-15 02:00:00',0,0.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('pf_dev006','res_dev001','fam_dev001','pc_demo001','com_demo001','property','2026-Q2',360.00,360.00,500,0.00,'paid','2026-06-30','2026-06-15 02:00:00',0,0.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('pf_dev007','res_dev001','fam_dev001','pc_demo001','com_demo001','parking','2025-Q1',200.00,200.00,0,0.00,'paid','2025-03-31','2025-03-20 02:00:00',0,0.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('pf_dev008','res_dev001','fam_dev001','pc_demo001','com_demo001','parking','2025-Q2',200.00,200.00,0,0.00,'paid','2025-06-30','2025-06-15 02:00:00',0,0.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('pf_dev009','res_dev001','fam_dev001','pc_demo001','com_demo001','parking','2025-Q3',200.00,200.00,0,0.00,'paid','2025-09-30','2025-09-15 02:00:00',0,0.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('pf_dev010','res_dev001','fam_dev001','pc_demo001','com_demo001','parking','2025-Q4',200.00,200.00,0,0.00,'paid','2025-12-31','2025-12-15 02:00:00',0,0.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('pf_dev011','res_dev001','fam_dev001','pc_demo001','com_demo001','parking','2026-Q1',200.00,200.00,0,0.00,'paid','2026-03-31','2026-03-15 02:00:00',0,0.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('pf_dev012','res_dev001','fam_dev001','pc_demo001','com_demo001','parking','2026-Q2',200.00,200.00,0,0.00,'paid','2026-06-30','2026-06-15 02:00:00',0,0.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('pf_dev013','res_dev001','fam_dev001','pc_demo001','com_demo001','property','2026-Q3',360.00,360.00,0,0.00,'paid','2026-07-31','2026-07-03 08:01:26',0,0.00,'2026-07-03 06:23:28','2026-07-03 08:01:26'),('pf_dev014','res_dev001','fam_dev001','pc_demo001','com_demo001','parking','2026-Q3',200.00,0.00,0,0.00,'unpaid','2026-07-31',NULL,0,0.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('pf_dev015','res_dev001','fam_dev001','pc_demo001','com_demo001','water','2026-07',85.50,0.00,0,0.00,'unpaid','2026-07-31',NULL,0,0.00,'2026-07-03 06:23:28','2026-07-03 06:23:28'),('pf_dev016','res_dev001','fam_dev001','pc_demo001','com_demo001','electricity','2026-07',156.80,0.00,0,0.00,'unpaid','2026-07-31',NULL,0,0.00,'2026-07-03 06:23:28','2026-07-03 06:23:28');
/*!40000 ALTER TABLE `property_fees` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `residents`
--

DROP TABLE IF EXISTS `residents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `residents` (
  `id` varchar(32) NOT NULL COMMENT 'res_xxxxxxxx',
  `openid` varchar(100) DEFAULT NULL COMMENT '微信openid',
  `unionid` varchar(100) DEFAULT NULL COMMENT '微信unionid',
  `phone` varchar(20) DEFAULT NULL,
  `password` varchar(200) DEFAULT NULL COMMENT '密码（BCrypt加密，管理端登录用）',
  `name` varchar(50) DEFAULT NULL,
  `avatar_url` varchar(500) DEFAULT NULL,
  `gender` int DEFAULT '0' COMMENT '0未知 1男 2女',
  `age` int DEFAULT NULL COMMENT '年龄',
  `marital_status` varchar(20) DEFAULT NULL COMMENT 'single/married/divorced/widowed',
  `has_children` tinyint(1) DEFAULT '0',
  `user_type` varchar(20) DEFAULT 'owner' COMMENT 'owner业主/tenant租住人员',
  `role` varchar(30) DEFAULT 'resident' COMMENT '见角色枚举',
  `property_company_id` varchar(32) DEFAULT NULL,
  `community_id` varchar(32) DEFAULT NULL,
  `building` varchar(20) DEFAULT NULL,
  `unit` varchar(20) DEFAULT NULL,
  `room` varchar(20) DEFAULT NULL,
  `family_id` varchar(32) DEFAULT NULL,
  `family_relation` varchar(20) DEFAULT NULL COMMENT 'owner/spouse/child/parent/sibling/other',
  `point_balance` int DEFAULT '0',
  `coin_balance` decimal(10,2) DEFAULT '0.00',
  `coin_frozen` tinyint(1) DEFAULT '0' COMMENT '物业币是否冻结',
  `coin_hidden` tinyint(1) DEFAULT '0' COMMENT '物业币卡片是否隐藏',
  `wechat_bound` tinyint(1) DEFAULT '0',
  `status` varchar(20) DEFAULT 'active' COMMENT 'active/frozen/disabled',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `openid` (`openid`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_community` (`community_id`),
  KEY `idx_family` (`family_id`),
  KEY `idx_phone` (`phone`),
  KEY `idx_openid` (`openid`),
  KEY `idx_role` (`role`),
  CONSTRAINT `residents_ibfk_1` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`),
  CONSTRAINT `residents_ibfk_2` FOREIGN KEY (`community_id`) REFERENCES `communities` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业主/住户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `residents`
--

LOCK TABLES `residents` WRITE;
/*!40000 ALTER TABLE `residents` DISABLE KEYS */;
INSERT INTO `residents` VALUES ('res_001',NULL,NULL,'13900000005','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','业主-赵六',NULL,0,NULL,NULL,0,'owner','resident','pc_demo001','com_demo001','3','2','201','fam_001','owner',3000,200.00,0,0,0,'active','2026-07-03 08:09:15','2026-07-03 08:09:15',NULL),('res_002',NULL,NULL,'13900000006','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','业主-钱七',NULL,0,NULL,NULL,0,'owner','resident','pc_demo001','com_demo001','3','2','202','fam_002','owner',1500,100.00,0,0,0,'frozen','2026-07-03 08:09:15','2026-07-04 03:24:18',NULL),('res_003',NULL,NULL,'13900000010','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','业主-孙八',NULL,0,NULL,NULL,0,'owner','resident','pc_demo001','com_demo002','5','1','101','fam_003','owner',2000,150.00,0,0,0,'active','2026-07-03 08:09:15','2026-07-03 08:09:15',NULL),('res_004',NULL,NULL,'13900000007','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','租户-周九',NULL,0,NULL,NULL,0,'tenant','resident','pc_demo001','com_demo001','4','3','301',NULL,NULL,500,30.00,0,0,0,'deleted','2026-07-03 08:09:15','2026-07-04 03:23:55','2026-07-04 03:23:55'),('res_28221000','dev_openid_0f3JTAFa1NUVZL0MTlHa1OdF6C1JTAFJ',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,'owner','resident',NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0,0,1,'active','2026-07-03 05:51:39','2026-07-03 05:51:39',NULL),('res_2ef41000',NULL,NULL,'11111111112',NULL,'1',NULL,0,12,'married',1,'owner','resident','pc_demo001','com_demo001','1','1','1',NULL,NULL,0,0.00,0,0,0,'active','2026-07-04 03:34:44','2026-07-04 04:36:23',NULL),('res_36ec1000','dev_openid_0e3wjpGa1TjvZL0zaSGa1EQNq71wjpGv',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,'owner','resident',NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0,0,1,'active','2026-07-03 05:39:40','2026-07-03 05:39:40',NULL),('res_48fa1000','dev_openid_13911110004',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,'owner','resident','pc_demo001','com_demo001','3栋','5单元','502','fam_1ea41000','owner',50,5.00,1,0,1,'frozen','2026-07-04 07:11:36','2026-07-05 05:15:22',NULL),('res_4fce1000','dev_openid_13911110005',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,'owner','resident','pc_demo001',NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0,0,1,'frozen','2026-07-05 05:46:34','2026-07-05 05:46:53',NULL),('res_51b21000','dev_openid_0f374HGa1R0QYL0WlEHa1apEPJ374HG7',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,'owner','resident',NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0,0,1,'active','2026-07-03 05:51:11','2026-07-03 05:51:11',NULL),('res_65aa1000','dev_openid_0c3nlyGa1D25ZL0iXRIa1PpclM1nlyGZ',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,'owner','resident',NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0,0,1,'active','2026-07-03 12:47:23','2026-07-03 12:47:23',NULL),('res_75aa1000',NULL,NULL,'11111111111',NULL,'11',NULL,1,12,NULL,NULL,'owner','resident','pc_demo001','com_demo001','1','1','1',NULL,NULL,0,0.00,0,0,0,'active','2026-07-04 03:34:20','2026-07-04 03:34:20',NULL),('res_a7ca1000','dev_openid_0d3CWd0w30byk73rXg2w3H956n3CWd03',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,'owner','resident',NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0,0,1,'active','2026-07-03 05:59:01','2026-07-03 05:59:01',NULL),('res_act01',NULL,NULL,'13900000011','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','活动组长-郑美丽',NULL,0,NULL,NULL,0,'owner','activity_leader','pc_demo001','com_demo001','2','1','101',NULL,NULL,600,40.00,0,0,0,'active','2026-07-03 08:09:15','2026-07-03 08:09:15',NULL),('res_admin_test01',NULL,NULL,'13911110001',NULL,'冻结住户-张三',NULL,1,45,'married',1,'owner','resident','pc_demo001','com_demo001','1','1','101',NULL,NULL,1200,80.00,0,0,0,'frozen','2026-01-15 02:00:00','2026-07-03 08:02:56',NULL),('res_admin_test02',NULL,NULL,'13911110002',NULL,'禁用住户-李四',NULL,1,30,'single',0,'tenant','resident','pc_demo001','com_demo001','1','2','201',NULL,NULL,0,0.00,0,0,0,'disabled','2026-03-20 06:00:00','2026-07-03 08:02:56',NULL),('res_admin_test03',NULL,NULL,'13911110003',NULL,'待审核-王五',NULL,2,35,'married',1,'owner','resident','pc_demo001','com_demo001','2','1','102',NULL,NULL,0,0.00,0,0,0,'active','2026-07-03 01:00:00','2026-07-03 08:02:56',NULL),('res_admin_test04',NULL,NULL,'13911110004',NULL,'住户-赵小明',NULL,1,28,'single',0,'owner','resident','pc_demo001','com_demo001','2','2','202','fam_admin001','owner',3500,200.00,0,0,0,'active','2026-02-01 00:00:00','2026-07-03 08:02:56',NULL),('res_admin_test05',NULL,NULL,'13911110005',NULL,'住户-钱小红',NULL,2,32,'married',1,'owner','resident','pc_demo001','com_demo001','4','1','301','fam_admin002','owner',2800,150.00,0,0,0,'active','2026-02-10 01:30:00','2026-07-03 08:02:56',NULL),('res_admin_test06',NULL,NULL,'13911110006',NULL,'租户-孙小刚',NULL,1,24,'single',0,'tenant','resident','pc_demo001','com_demo001','4','2','302',NULL,NULL,800,30.00,0,0,0,'active','2026-03-01 02:00:00','2026-07-03 08:02:56',NULL),('res_admin_test07',NULL,NULL,'13911110007',NULL,'住户-周美丽',NULL,2,40,'married',1,'owner','resident','pc_demo001','com_demo002','1','1','101','fam_admin003','owner',5000,300.00,0,0,0,'active','2026-01-20 03:00:00','2026-07-03 08:02:56',NULL),('res_admin_test08',NULL,NULL,'13911110008',NULL,'住户-吴大伟',NULL,1,50,'married',1,'owner','resident','pc_demo001','com_demo002','2','2','202','fam_admin004','owner',1800,90.00,0,0,0,'active','2026-04-05 06:00:00','2026-07-03 08:02:56',NULL),('res_admin01',NULL,NULL,'13800138000','$2b$10$b86xg99Rxo1pSziHNT/5Ju0TQ59eNg3WG9CBQgr/sCP1d6apCjaDO','平台管理员',NULL,0,NULL,NULL,0,'owner','platform_admin','pc_demo001',NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0,0,0,'active','2026-07-02 01:57:36','2026-07-03 01:54:17',NULL),('res_ba061000','dev_openid_1380000001',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,'owner','resident',NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0,0,1,'active','2026-07-03 06:56:24','2026-07-03 06:56:24',NULL),('res_c0581000','dev_openid_0a3959Ga1YWnZL0MGTGa12BYWg2959Gd',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,'owner','resident',NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0,0,1,'active','2026-07-03 05:53:04','2026-07-03 05:53:04',NULL),('res_coo01',NULL,NULL,'13900000002','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','统筹-张伟',NULL,0,NULL,NULL,0,'owner','coordinator','pc_demo001','com_demo001',NULL,NULL,NULL,NULL,NULL,1200,150.00,0,0,0,'active','2026-07-03 08:09:15','2026-07-03 08:09:15',NULL),('res_courier01',NULL,NULL,'13900000009','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','快递员-吴十',NULL,0,NULL,NULL,0,'owner','courier','pc_demo001','com_demo001',NULL,NULL,NULL,NULL,NULL,200,15.00,0,0,0,'active','2026-07-03 08:09:15','2026-07-03 08:09:15',NULL),('res_courier02','courier_openid_002',NULL,'13900000013',NULL,'快递员-李四',NULL,0,NULL,NULL,0,'owner','courier','pc_demo001','com_demo001',NULL,NULL,NULL,NULL,NULL,150,10.00,0,0,0,'active','2026-07-05 05:49:39','2026-07-05 05:49:39',NULL),('res_courier03','courier_openid_003',NULL,'13900000014',NULL,'快递员-王五',NULL,0,NULL,NULL,0,'owner','courier','pc_demo001','com_demo001',NULL,NULL,NULL,NULL,NULL,100,5.00,0,0,0,'active','2026-07-05 05:49:39','2026-07-05 05:49:39',NULL),('res_dev001','dev_openid_13800000001',NULL,NULL,NULL,'张阿姨','/static/logo.png',2,58,'married',1,'owner','resident','pc_demo001','com_demo001','3','12','1201','fam_dev001','owner',12579,350.50,0,0,1,'active','2026-07-03 07:40:59','2026-07-03 18:23:37',NULL),('res_dev002','dev_openid_13800000002',NULL,NULL,NULL,'张建设',NULL,1,60,'married',1,'owner','resident','pc_demo001','com_demo001','3','12','1201','fam_dev001','spouse',5616,120.00,0,0,1,'disabled','2026-07-03 07:40:59','2026-07-04 06:33:11','2026-07-04 06:33:11'),('res_dev003','dev_openid_13800000003',NULL,NULL,NULL,'张子涵',NULL,1,28,'single',0,'owner','resident','pc_demo001','com_demo001','3','12','1201','fam_dev001','child',2246,80.00,0,0,1,'active','2026-07-03 07:40:59','2026-07-05 04:51:37',NULL),('res_dev004','dev_openid_13800000004',NULL,NULL,NULL,'王秀兰','/static/logo.png',2,82,'widowed',0,'owner','resident','pc_demo001','com_demo001','3','12','1201','fam_dev001','parent',625,50.00,0,0,1,'active','2026-07-03 07:40:59','2026-07-05 06:37:02',NULL),('res_e10c1000','dev_openid_0a3tnzll2fUgZh477Eol2mHKch0tnzlP',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,'owner','resident',NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0,0,1,'active','2026-07-03 07:47:49','2026-07-03 07:47:49',NULL),('res_ind01',NULL,NULL,'13900000004','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','个体负责人-王强',NULL,0,NULL,NULL,0,'owner','individual_leader','pc_demo001','com_demo001','1','1',NULL,NULL,NULL,500,50.00,0,0,0,'active','2026-07-03 08:09:15','2026-07-03 08:09:15',NULL),('res_mch01',NULL,NULL,'13900000008','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','商家-陈老板',NULL,0,NULL,NULL,0,'owner','merchant','pc_demo001',NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0,0,0,'active','2026-07-03 08:09:15','2026-07-03 08:09:15',NULL),('res_propadmin01',NULL,NULL,'13900000001','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','物业管理员-刘芳',NULL,0,NULL,NULL,0,'owner','property_admin','pc_demo001','com_demo001',NULL,NULL,NULL,NULL,NULL,0,0.00,0,0,0,'active','2026-07-03 08:09:15','2026-07-03 08:09:15',NULL),('res_sec01',NULL,NULL,'13900000003','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','板块负责人-李娜',NULL,0,NULL,NULL,0,'owner','sector_leader','pc_demo001','com_demo001',NULL,NULL,NULL,NULL,NULL,800,80.00,0,0,0,'active','2026-07-03 08:09:15','2026-07-03 08:09:15',NULL),('res_tech01',NULL,NULL,'13900000012','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','技工-刘师傅',NULL,0,NULL,NULL,0,'owner','technician','pc_demo001','com_demo001',NULL,NULL,NULL,NULL,NULL,300,20.00,0,0,0,'active','2026-07-03 08:09:15','2026-07-03 08:09:15',NULL);
/*!40000 ALTER TABLE `residents` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role_preset_permissions`
--

DROP TABLE IF EXISTS `role_preset_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_preset_permissions` (
  `id` varchar(32) NOT NULL,
  `preset_id` varchar(32) NOT NULL,
  `permission_id` varchar(32) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_preset_permission` (`preset_id`,`permission_id`),
  KEY `permission_id` (`permission_id`),
  KEY `idx_preset` (`preset_id`),
  CONSTRAINT `role_preset_permissions_ibfk_1` FOREIGN KEY (`preset_id`) REFERENCES `role_presets` (`id`) ON DELETE CASCADE,
  CONSTRAINT `role_preset_permissions_ibfk_2` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预设-权限关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role_preset_permissions`
--

LOCK TABLES `role_preset_permissions` WRITE;
/*!40000 ALTER TABLE `role_preset_permissions` DISABLE KEYS */;
INSERT INTO `role_preset_permissions` VALUES ('rpp_001','rp_001','perm_001','2026-07-02 01:57:36','2026-07-02 01:57:36'),('rpp_002','rp_001','perm_004','2026-07-02 01:57:36','2026-07-02 01:57:36'),('rpp_003','rp_001','perm_005','2026-07-02 01:57:36','2026-07-02 01:57:36'),('rpp_004','rp_001','perm_006','2026-07-02 01:57:36','2026-07-02 01:57:36'),('rpp_005','rp_001','perm_007','2026-07-02 01:57:36','2026-07-02 01:57:36'),('rpp_006','rp_001','perm_002','2026-07-02 01:57:36','2026-07-02 01:57:36'),('rpp_010','rp_004','perm_009','2026-07-02 01:57:36','2026-07-02 01:57:36');
/*!40000 ALTER TABLE `role_preset_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role_presets`
--

DROP TABLE IF EXISTS `role_presets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_presets` (
  `id` varchar(32) NOT NULL COMMENT 'rp_xxxxxxxx',
  `name` varchar(50) NOT NULL COMMENT '预设名称',
  `code` varchar(50) NOT NULL COMMENT 'coordinator/sector_leader等',
  `description` varchar(200) DEFAULT NULL,
  `property_company_id` varchar(32) DEFAULT NULL COMMENT 'null为平台级预设',
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_code` (`code`),
  KEY `idx_property_company` (`property_company_id`),
  CONSTRAINT `role_presets_ibfk_1` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色快捷预设表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role_presets`
--

LOCK TABLES `role_presets` WRITE;
/*!40000 ALTER TABLE `role_presets` DISABLE KEYS */;
INSERT INTO `role_presets` VALUES ('rp_001','统筹','coordinator','统筹标准权限包',NULL,'active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('rp_002','板块负责人','sector_leader','板块负责人标准权限包',NULL,'active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('rp_003','活动组组长','activity_leader','组长标准权限包',NULL,'active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('rp_004','快递员','courier','快递员标准权限包',NULL,'active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('rp_005','社区管理员','community_admin','社区标准权限包',NULL,'active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('rp_006','物业员工','property_staff','物业内部人员全权限',NULL,'active','2026-07-02 01:57:36','2026-07-02 01:57:36');
/*!40000 ALTER TABLE `role_presets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sector_leaders`
--

DROP TABLE IF EXISTS `sector_leaders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sector_leaders` (
  `id` varchar(32) NOT NULL COMMENT 'sl_xxxxxxxx',
  `resident_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `sector` varchar(50) NOT NULL COMMENT 'elderly/medical/education/cleaning/canteen/pet/life_service',
  `commission_rate` decimal(5,4) DEFAULT '0.1000' COMMENT '从统筹中抽取的比例',
  `coordinator_id` varchar(32) DEFAULT NULL COMMENT '所属统筹',
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resident_property_sector` (`resident_id`,`property_company_id`,`sector`),
  KEY `coordinator_id` (`coordinator_id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_sector` (`sector`),
  CONSTRAINT `sector_leaders_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `sector_leaders_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`),
  CONSTRAINT `sector_leaders_ibfk_3` FOREIGN KEY (`coordinator_id`) REFERENCES `coordinators` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='板块负责人表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sector_leaders`
--

LOCK TABLES `sector_leaders` WRITE;
/*!40000 ALTER TABLE `sector_leaders` DISABLE KEYS */;
INSERT INTO `sector_leaders` VALUES ('sl_001','res_sec01','pc_demo001','cleaning_repair',0.1000,'coo_001','active','2026-07-03 08:09:15','2026-07-03 08:09:15'),('sl_002','res_sec01','pc_demo001','life_service',0.1000,'coo_001','active','2026-07-03 08:09:15','2026-07-03 08:09:15');
/*!40000 ALTER TABLE `sector_leaders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_orders`
--

DROP TABLE IF EXISTS `service_orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_orders` (
  `id` varchar(32) NOT NULL,
  `service_id` varchar(32) NOT NULL,
  `resident_id` varchar(32) NOT NULL,
  `provider_id` varchar(32) DEFAULT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `order_status` varchar(20) DEFAULT 'pending',
  `amount` decimal(10,2) NOT NULL,
  `payment_method` varchar(20) DEFAULT NULL,
  `service_time` timestamp NULL DEFAULT NULL,
  `address` varchar(200) DEFAULT NULL,
  `contact_phone` varchar(20) DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `service_id` (`service_id`),
  KEY `idx_resident` (`resident_id`),
  KEY `idx_provider` (`provider_id`),
  KEY `idx_property_company` (`property_company_id`),
  CONSTRAINT `service_orders_ibfk_1` FOREIGN KEY (`service_id`) REFERENCES `community_services` (`id`),
  CONSTRAINT `service_orders_ibfk_2` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `service_orders_ibfk_3` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_orders`
--

LOCK TABLES `service_orders` WRITE;
/*!40000 ALTER TABLE `service_orders` DISABLE KEYS */;
INSERT INTO `service_orders` VALUES ('svc_ord_admin001','svc_001','res_admin_test04','res_ind01','pc_demo001','completed',65.00,'wechat','2026-06-15 01:00:00','华帝·阳光花园2栋202','13911110004','全屋深度清洁','2026-06-14 10:00:00','2026-07-03 08:02:56'),('svc_ord_admin002','svc_003','res_admin_test05',NULL,'pc_demo001','completed',40.00,'wechat','2026-06-20 06:00:00','华帝·阳光花园4栋301','13911110005','冰箱维修','2026-06-19 08:00:00','2026-07-03 08:02:56'),('svc_ord_admin003','svc_005','res_admin_test04',NULL,'pc_demo001','pending',50.00,'wechat','2026-07-05 02:00:00','华帝·阳光花园2栋202','13911110004','宠物寄养1天','2026-07-03 01:00:00','2026-07-03 08:02:56'),('svc_ord_admin004','svc_002','res_admin_test07',NULL,'pc_demo001','pending',120.00,'wechat','2026-07-06 06:00:00','华帝·翠湖名苑1栋101','13911110007','老人陪护2小时','2026-07-04 08:00:00','2026-07-03 08:02:56'),('svc_ord001','svc_001','res_dev001','res_ind01','pc_demo001','completed',65.00,'wechat','2026-07-01 01:00:00','华帝·阳光花园3栋1201','13800138000','全屋保洁','2026-06-30 10:00:00','2026-07-03 06:23:28'),('svc_ord002','svc_003','res_dev001',NULL,'pc_demo001','completed',40.00,'wechat','2026-07-03 06:00:00','华帝·阳光花园3栋1201','13800138000','空调维修','2026-07-02 08:00:00','2026-07-03 06:23:28'),('svc_ord003','svc_004','res_dev001',NULL,'pc_demo001','completed',12.00,'property_coin','2026-07-05 03:30:00','华帝·阳光花园3栋1201','13800138000','午餐2份','2026-07-05 02:00:00','2026-07-03 06:23:28'),('svc_ord004','svc_002','res_dev001',NULL,'pc_demo001','pending',120.00,'wechat','2026-07-10 06:00:00','华帝·阳光花园3栋1201','13800138000','陪护2小时','2026-07-09 08:00:00','2026-07-03 06:23:28');
/*!40000 ALTER TABLE `service_orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_subscriptions`
--

DROP TABLE IF EXISTS `service_subscriptions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_subscriptions` (
  `id` varchar(32) NOT NULL,
  `service_id` varchar(32) NOT NULL,
  `resident_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `sub_type` varchar(20) NOT NULL COMMENT 'monthly/yearly',
  `amount` decimal(10,2) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `status` varchar(20) DEFAULT 'active' COMMENT 'active/expired/cancelled',
  `auto_renew` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `property_company_id` (`property_company_id`),
  KEY `idx_resident` (`resident_id`),
  KEY `idx_service` (`service_id`),
  KEY `idx_end_date` (`end_date`),
  CONSTRAINT `service_subscriptions_ibfk_1` FOREIGN KEY (`service_id`) REFERENCES `community_services` (`id`),
  CONSTRAINT `service_subscriptions_ibfk_2` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `service_subscriptions_ibfk_3` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务订阅表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_subscriptions`
--

LOCK TABLES `service_subscriptions` WRITE;
/*!40000 ALTER TABLE `service_subscriptions` DISABLE KEYS */;
INSERT INTO `service_subscriptions` VALUES ('ss_admin001','svc_002','res_admin_test07','pc_demo001','monthly',120.00,'2026-07-01','2026-07-31','active',1,'2026-07-01 02:00:00','2026-07-03 08:02:56'),('ss_admin002','svc_001','res_admin_test04','pc_demo001','monthly',65.00,'2026-06-01','2026-06-30','expired',0,'2026-06-01 02:00:00','2026-07-03 08:02:56');
/*!40000 ALTER TABLE `service_subscriptions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `special_offers`
--

DROP TABLE IF EXISTS `special_offers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `special_offers` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'so_xxxxxxxx',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容',
  `target_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '推送对象类型：all/tag/building/role',
  `target_tags` json DEFAULT NULL COMMENT '推送标签（JSON数组）',
  `publisher_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发布者ID',
  `publisher_role` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发布者角色',
  `property_company_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物业公司ID',
  `community_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '小区ID',
  `merchant_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商家ID',
  `cover_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面图片',
  `discount_info` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '优惠信息',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '结束时间',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'draft' COMMENT '状态：draft/published/ended/archived',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_special_offers_property` (`property_company_id`),
  KEY `idx_special_offers_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='特惠推送表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `special_offers`
--

LOCK TABLES `special_offers` WRITE;
/*!40000 ALTER TABLE `special_offers` DISABLE KEYS */;
INSERT INTO `special_offers` VALUES ('so_admin001','家政清洁服务8折','社区家政服务限时8折优惠，专业清洁团队上门服务','all',NULL,'res_ind01','individual_leader','pc_demo001','com_demo001',NULL,NULL,'全场8折','2026-06-30 16:00:00','2026-07-15 15:59:59','published','2026-07-03 08:02:56','2026-07-03 08:02:56'),('so_admin002','暑期托管班招生','社区暑期儿童托管班开始报名，物业币可抵扣50%','all',NULL,'res_coo01','coordinator','pc_demo001',NULL,NULL,NULL,'物业币抵扣50%','2026-07-04 16:00:00','2026-07-20 15:59:59','draft','2026-07-03 08:02:56','2026-07-03 08:02:56'),('so_dev001','新用户首单立减10元','新用户首次下单享受立减10元优惠，满30元可用','all',NULL,'res_admin01','platform_admin','pc_demo001',NULL,NULL,NULL,'满30减10','2026-06-30 16:00:00','2026-07-31 15:59:59','published','2026-07-03 06:53:28','2026-07-03 06:53:28'),('so_dev002','口福餐厅限时特惠','东北拉皮原价18元，现价12元，限时3天！','all',NULL,'res_mch01','merchant','pc_demo001',NULL,'mch_demo001',NULL,'原价18 现价12','2026-07-04 16:00:00','2026-07-07 15:59:59','published','2026-07-03 06:53:28','2026-07-03 06:53:28'),('so_dev003','有机蔬菜5折优惠','绿色农场有机蔬菜全场5折，新鲜直达','all',NULL,'res_mch01','merchant','pc_demo001',NULL,'mch_demo003',NULL,'全场5折','2026-07-07 16:00:00','2026-07-10 15:59:59','published','2026-07-03 06:53:28','2026-07-03 06:53:28');
/*!40000 ALTER TABLE `special_offers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_permissions`
--

DROP TABLE IF EXISTS `user_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_permissions` (
  `id` varchar(32) NOT NULL,
  `resident_id` varchar(32) NOT NULL,
  `permission_id` varchar(32) NOT NULL,
  `granted` tinyint(1) NOT NULL COMMENT 'TRUE=勾选/FALSE=取消',
  `property_company_id` varchar(32) NOT NULL,
  `operator_id` varchar(32) NOT NULL COMMENT '操作人',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resident_permission` (`resident_id`,`permission_id`),
  KEY `permission_id` (`permission_id`),
  KEY `idx_resident` (`resident_id`),
  KEY `idx_property_company` (`property_company_id`),
  CONSTRAINT `user_permissions_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `user_permissions_ibfk_2` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`),
  CONSTRAINT `user_permissions_ibfk_3` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_permissions`
--

LOCK TABLES `user_permissions` WRITE;
/*!40000 ALTER TABLE `user_permissions` DISABLE KEYS */;
INSERT INTO `user_permissions` VALUES ('up_138c1000','res_001','perm_006',1,'pc_demo001','res_admin01','2026-07-05 03:40:17','2026-07-05 03:40:17'),('up_13d01000','res_001','perm_001',1,'pc_demo001','res_admin01','2026-07-05 03:40:17','2026-07-05 03:40:17'),('up_13da1000','res_001','perm_009',1,'pc_demo001','res_admin01','2026-07-05 03:40:17','2026-07-05 03:40:17'),('up_13e41000','res_001','perm_005',1,'pc_demo001','res_admin01','2026-07-05 03:40:17','2026-07-05 03:40:17'),('up_13f01000','res_001','perm_004',1,'pc_demo001','res_admin01','2026-07-05 03:40:17','2026-07-05 03:40:17'),('up_13fa1000','res_001','perm_007',1,'pc_demo001','res_admin01','2026-07-05 03:40:17','2026-07-05 03:40:17'),('up_admin001','res_coo01','perm_001',1,'pc_demo001','res_propadmin01','2026-06-20 06:00:00','2026-07-03 08:02:56'),('up_admin002','res_coo01','perm_004',1,'pc_demo001','res_propadmin01','2026-06-20 06:00:00','2026-07-03 08:02:56'),('up_admin003','res_coo01','perm_005',1,'pc_demo001','res_propadmin01','2026-06-20 06:00:00','2026-07-03 08:02:56'),('up_admin004','res_sec01','perm_008',1,'pc_demo001','res_admin01','2026-06-01 02:00:00','2026-07-03 08:02:56'),('up_admin005','res_sec01','perm_006',1,'pc_demo001','res_admin01','2026-06-01 02:00:00','2026-07-03 08:02:56'),('up_admin006','res_courier01','perm_009',1,'pc_demo001','res_propadmin01','2026-06-05 01:00:00','2026-07-03 08:02:56'),('up_admin007','res_act01','perm_001',1,'pc_demo001','res_propadmin01','2026-06-10 02:00:00','2026-07-03 08:02:56'),('up_admin008','res_act01','perm_006',1,'pc_demo001','res_propadmin01','2026-06-10 02:00:00','2026-07-03 08:02:56'),('up_admin009','res_coo01','perm_002',0,'pc_demo001','res_propadmin01','2026-06-25 08:00:00','2026-07-03 08:02:56'),('up_fdf61000','res_002','perm_028',1,'pc_demo001','res_admin01','2026-07-05 03:40:47','2026-07-05 03:40:47');
/*!40000 ALTER TABLE `user_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vote_records`
--

DROP TABLE IF EXISTS `vote_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vote_records` (
  `id` varchar(32) NOT NULL,
  `opinion_box_id` varchar(32) NOT NULL,
  `resident_id` varchar(32) NOT NULL,
  `family_id` varchar(32) NOT NULL,
  `vote_option` varchar(20) NOT NULL COMMENT 'support/oppose/abstain',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_opinion_family` (`opinion_box_id`,`family_id`) COMMENT '一户一票',
  KEY `resident_id` (`resident_id`),
  KEY `idx_opinion_box` (`opinion_box_id`),
  KEY `idx_family` (`family_id`),
  CONSTRAINT `vote_records_ibfk_1` FOREIGN KEY (`opinion_box_id`) REFERENCES `opinion_boxes` (`id`),
  CONSTRAINT `vote_records_ibfk_2` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `vote_records_ibfk_3` FOREIGN KEY (`family_id`) REFERENCES `families` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='投票记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vote_records`
--

LOCK TABLES `vote_records` WRITE;
/*!40000 ALTER TABLE `vote_records` DISABLE KEYS */;
INSERT INTO `vote_records` VALUES ('vr_dev001','ob_dev001','res_dev001','fam_dev001','support','2026-07-05 02:00:00','2026-07-03 06:23:28'),('vr_dev002','ob_dev002','res_dev001','fam_dev001','support','2026-06-15 02:00:00','2026-07-03 06:23:28'),('vr_dev003','ob_dev003','res_dev001','fam_dev001','oppose','2026-06-05 02:00:00','2026-07-03 06:23:28');
/*!40000 ALTER TABLE `vote_records` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-05 18:07:13
