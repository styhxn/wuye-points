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
  `introduction` text COMMENT '组员个人简介（姓名、年龄、擅长领域等）',
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
INSERT INTO `activity_group_members` VALUES ('agm_35154bbd221000','act_35154bbc1c1000','res_act01','leader','2026-08-12 09:21:16','active',NULL,'2026-08-12 09:21:16','2026-08-12 09:21:16'),('agm_351e3b45721000','act_351e3b45581000','res_act01','leader','2026-08-13 06:10:34','active',NULL,'2026-08-13 06:10:34','2026-08-13 06:10:34'),('agm_351e8e2ec61000','act_351e3b45581000','res_dev001','member','2026-08-23 02:45:13','active','{\"name\":\"张阿姨\",\"age\":41,\"expertise\":\"唱歌\",\"bio\":\"跳舞\"}','2026-08-13 06:55:51','2026-09-06 09:40:50');
/*!40000 ALTER TABLE `activity_group_members` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activity_group_pricing_tiers`
--

DROP TABLE IF EXISTS `activity_group_pricing_tiers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity_group_pricing_tiers` (
  `id` varchar(32) NOT NULL COMMENT 'agt_xxxxxxxx',
  `activity_group_id` varchar(32) NOT NULL,
  `tier_name` varchar(50) NOT NULL COMMENT '级别名称，如一级、二级',
  `tier_code` varchar(30) NOT NULL COMMENT '级别编码，如 level_1',
  `price` decimal(10,2) NOT NULL,
  `period` varchar(20) NOT NULL DEFAULT 'monthly' COMMENT 'monthly/quarterly/yearly',
  `sort_order` int DEFAULT '0',
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_activity_group_id` (`activity_group_id`),
  CONSTRAINT `fk_agpt_group` FOREIGN KEY (`activity_group_id`) REFERENCES `activity_groups` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='活动组多课时价格';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity_group_pricing_tiers`
--

LOCK TABLES `activity_group_pricing_tiers` WRITE;
/*!40000 ALTER TABLE `activity_group_pricing_tiers` DISABLE KEYS */;
INSERT INTO `activity_group_pricing_tiers` VALUES ('agt_5e2a1000','act_35154bbc1c1000','1','1',1.00,'monthly',1,'active','2026-09-05 05:57:46','2026-09-05 05:57:46'),('agt_94fc1000','act_351e3b45581000','1','1',1.00,'monthly',1,'active','2026-09-05 13:14:47','2026-09-05 13:14:47');
/*!40000 ALTER TABLE `activity_group_pricing_tiers` ENABLE KEYS */;
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
  `pricing_tier_id` varchar(32) DEFAULT NULL COMMENT '课时价格ID（agt_xxx）',
  `tier_name` varchar(50) DEFAULT NULL COMMENT '课时名称快照，如一级、二级',
  `tier_code` varchar(30) DEFAULT NULL COMMENT '课时编码快照，如 level_1',
  `tier_price` decimal(10,2) DEFAULT NULL COMMENT '课时价格快照',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `property_company_id` (`property_company_id`),
  KEY `idx_group` (`group_id`),
  KEY `idx_resident` (`resident_id`),
  KEY `idx_end_date` (`end_date`),
  KEY `idx_pricing_tier_id` (`pricing_tier_id`),
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
INSERT INTO `activity_group_subscriptions` VALUES ('ags_353d0614ca1000','act_35154bbc1c1000','res_dev001','pc_demo001','monthly',10.00,'2026-08-16','2026-09-16','cancelled',NULL,NULL,NULL,NULL,'2026-08-16 05:55:38','2026-08-16 06:30:00');
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
  `activity_type` varchar(30) DEFAULT NULL COMMENT '活动类型，如 piano/dance/english',
  `cover_url` varchar(500) DEFAULT NULL,
  `cover_urls` json DEFAULT NULL COMMENT '多张封面图URL数组',
  `leader_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `community_id` varchar(32) DEFAULT NULL,
  `member_count` int DEFAULT '0',
  `monthly_fee` decimal(10,2) DEFAULT '0.00' COMMENT '月费',
  `yearly_fee` decimal(10,2) DEFAULT '0.00' COMMENT '年费',
  `quarterly_fee` decimal(10,2) DEFAULT '0.00' COMMENT '季度费用',
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `review_status` varchar(20) DEFAULT NULL COMMENT '注册审核状态 pending/approved/rejected',
  `review_remark` varchar(500) DEFAULT NULL COMMENT '审核备注',
  PRIMARY KEY (`id`),
  KEY `idx_leader` (`leader_id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_activity_type` (`activity_type`),
  CONSTRAINT `activity_groups_ibfk_1` FOREIGN KEY (`leader_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `activity_groups_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='活动组表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity_groups`
--

LOCK TABLES `activity_groups` WRITE;
/*!40000 ALTER TABLE `activity_groups` DISABLE KEYS */;
INSERT INTO `activity_groups` VALUES ('act_35154bbc1c1000','摄影知识学习','学习摄影技巧，手把手学会摄影！',NULL,NULL,NULL,'res_act01','pc_demo001','com_demo001',1,10.00,100.00,NULL,'active','2026-08-12 09:21:16','2026-09-05 11:53:27','approved',NULL),('act_351e3b45581000','太极拳锻炼','太极有助于强筋骨、稳平衡、调呼吸、静心减压.','dance',NULL,'[]','res_act01','pc_demo001','com_demo001',2,0.00,0.00,NULL,'active','2026-08-13 06:10:34','2026-09-05 11:53:27','approved',NULL);
/*!40000 ALTER TABLE `activity_groups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `age_bracket_configs`
--

DROP TABLE IF EXISTS `age_bracket_configs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `age_bracket_configs` (
  `id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) DEFAULT NULL,
  `label` varchar(50) NOT NULL,
  `min_age` int NOT NULL DEFAULT '0',
  `max_age` int DEFAULT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_property_company` (`property_company_id`),
  CONSTRAINT `age_bracket_configs_ibfk_1` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `age_bracket_configs`
--

LOCK TABLES `age_bracket_configs` WRITE;
/*!40000 ALTER TABLE `age_bracket_configs` DISABLE KEYS */;
INSERT INTO `age_bracket_configs` VALUES ('ab_01',NULL,'18岁以下',0,17,1,'active','2026-07-13 10:58:06','2026-07-13 10:58:06'),('ab_02',NULL,'18-25岁',18,25,2,'active','2026-07-13 10:58:06','2026-07-13 10:58:06'),('ab_03',NULL,'26-35岁',26,35,3,'active','2026-07-13 10:58:06','2026-07-13 10:58:06'),('ab_04',NULL,'36-45岁',36,45,4,'active','2026-07-13 10:58:06','2026-07-13 10:58:06'),('ab_05',NULL,'46-55岁',46,55,5,'active','2026-07-13 10:58:06','2026-07-13 10:58:06'),('ab_06',NULL,'55岁以上',56,NULL,6,'active','2026-07-13 10:58:06','2026-07-13 10:58:06');
/*!40000 ALTER TABLE `age_bracket_configs` ENABLE KEYS */;
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
  `content_type` varchar(30) DEFAULT 'notification' COMMENT '内容类型: notification/product/activity',
  `product_id` varchar(32) DEFAULT NULL COMMENT '商品ID（产品推荐时使用）',
  `property_company_id` varchar(32) DEFAULT NULL,
  `community_id` varchar(32) DEFAULT NULL,
  `merchant_id` varchar(32) DEFAULT NULL,
  `activity_group_id` varchar(32) DEFAULT NULL COMMENT '关联活动组ID（活动公告专用）',
  `publisher_id` varchar(32) NOT NULL,
  `publisher_role` varchar(30) NOT NULL,
  `cover_urls` json DEFAULT NULL,
  `target_roles` json DEFAULT NULL COMMENT '目标角色',
  `target_buildings` json DEFAULT NULL COMMENT '目标楼栋',
  `collect_enabled` tinyint(1) DEFAULT '0' COMMENT '是否启用信息收集',
  `collect_fields` json DEFAULT NULL COMMENT '收集字段定义',
  `status` varchar(20) DEFAULT 'published' COMMENT 'draft/published/archived',
  `delivery_channel` varchar(30) NOT NULL DEFAULT 'announcement_board' COMMENT 'announcement_board/chat/both',
  `published_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_type` (`announcement_type`),
  KEY `idx_status` (`status`),
  KEY `idx_published_at` (`published_at`),
  KEY `idx_delivery_channel` (`delivery_channel`),
  KEY `idx_ann_activity_group` (`activity_group_id`),
  CONSTRAINT `announcements_ibfk_1` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公告表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `announcements`
--

LOCK TABLES `announcements` WRITE;
/*!40000 ALTER TABLE `announcements` DISABLE KEYS */;
INSERT INTO `announcements` VALUES ('ann_003','默认公告','请补充内容','property','notification',NULL,'pc_demo001',NULL,NULL,NULL,'res_admin01','platform_admin',NULL,NULL,NULL,0,NULL,'published','announcement_board','2026-08-07 02:21:10','2026-08-07 02:21:10','2026-08-07 02:21:10'),('ann_3583d2095e1000','823测试','1','property',NULL,NULL,'pc_demo001',NULL,NULL,NULL,'res_propadmin01','property_admin',NULL,NULL,NULL,0,NULL,'published','announcement_board','2026-08-23 02:53:43','2026-08-23 02:53:43','2026-08-23 02:53:43'),('ann_35840b03ee1000','823','823','activity','activity',NULL,'pc_demo001','com_demo001',NULL,NULL,'res_act01','activity_leader',NULL,NULL,NULL,NULL,NULL,'published','announcement_board','2026-08-22 19:24:49','2026-08-23 03:24:51','2026-08-23 03:24:51'),('ann_35853a95141000','823','823','property',NULL,NULL,'pc_demo001','com_demo001',NULL,NULL,'res_propadmin01','property_admin',NULL,'[\"resident\"]',NULL,0,NULL,'published','announcement_board','2026-08-23 06:10:38','2026-08-23 06:10:38','2026-08-23 06:10:38'),('ann_6cd61000','1','1','activity','notification',NULL,'pc_demo001','com_demo001',NULL,'act_351e3b45581000','res_act01','activity_leader',NULL,NULL,NULL,0,NULL,'published','announcement_board','2026-09-06 09:45:08','2026-09-06 09:45:08','2026-09-06 09:45:08'),('ann_admin001','2026年7月垃圾分类宣传','垃圾分类，人人有责。请各位业主将生活垃圾分类投放至指定垃圾桶。','community','notification',NULL,'pc_demo001','com_demo001',NULL,NULL,'res_propadmin01','property_admin',NULL,NULL,NULL,0,NULL,'published','announcement_board','2026-07-01 02:00:00','2026-07-03 08:02:56','2026-07-03 08:02:56'),('ann_admin002','物业费缴费优惠活动','凡在7月15日前缴纳Q3物业费的业主，可享受积分双倍奖励！','property','notification',NULL,'pc_demo001',NULL,NULL,NULL,'res_propadmin01','property_admin',NULL,NULL,NULL,0,NULL,'published','announcement_board','2026-07-01 00:00:00','2026-07-03 08:02:56','2026-07-03 08:02:56'),('ann_admin003','关于小区绿化改造的通知','小区将于7月10日至20日进行绿化升级改造，施工期间请注意安全。','property','notification',NULL,'pc_demo001','com_demo001',NULL,NULL,'res_propadmin01','property_admin',NULL,NULL,NULL,0,NULL,'draft','announcement_board',NULL,'2026-07-03 08:02:56','2026-07-03 08:02:56'),('ann_admin004','翠湖名苑电梯检修通知','翠湖名苑将于7月8日进行电梯年度检修，届时将分时段停运。','community','notification',NULL,'pc_demo001','com_demo002',NULL,NULL,'res_propadmin01','property_admin',NULL,NULL,NULL,0,NULL,'published','announcement_board','2026-07-02 01:00:00','2026-07-03 08:02:56','2026-07-03 08:02:56'),('ann_admin005','社区便民服务日','本周末社区中心将举办便民服务日活动，免费理发、义诊等。','activity','activity',NULL,'pc_demo001',NULL,NULL,NULL,'res_coo01','coordinator',NULL,NULL,NULL,0,NULL,'archived','announcement_board','2026-06-15 00:00:00','2026-07-03 08:02:56','2026-07-22 05:29:08'),('ann_dev001','关于2026年7月物业费缴纳通知','尊敬的业主：2026年第三季度物业费已出账，请于7月31日前完成缴纳。支持微信、物业币、积分抵扣等多种方式。','property','notification',NULL,'pc_demo001',NULL,NULL,NULL,'res_propadmin01','property_admin',NULL,NULL,NULL,0,NULL,'published','announcement_board','2026-07-01 01:00:00','2026-07-03 06:23:28','2026-07-03 06:23:28'),('ann_dev002','阳光花园小区停水通知','因自来水管道检修，7月5日9:00-17:00暂停供水，请提前储水。给您带来不便，敬请谅解。','community','notification',NULL,'pc_demo001','com_demo001',NULL,NULL,'res_propadmin01','property_admin',NULL,NULL,NULL,0,NULL,'published','announcement_board','2026-07-03 00:00:00','2026-07-03 06:23:28','2026-07-03 06:23:28'),('ann_dev003','社区夏令营活动报名开始','暑期社区青少年夏令营开始报名！包含书法、绘画、编程等课程，物业币抵扣50%。名额有限，先到先得。','activity','activity',NULL,'pc_demo001',NULL,NULL,NULL,'res_coo01','coordinator',NULL,NULL,NULL,0,NULL,'published','announcement_board','2026-07-02 02:00:00','2026-07-03 06:23:28','2026-07-22 05:29:08'),('ann_dev004','社区电梯维保完成公示','截止本周五，全区32部客梯已全部完成季度深度维保及质检，各项指标均符合安全运行要求。','property','notification',NULL,'pc_demo001','com_demo001',NULL,NULL,'res_propadmin01','property_admin',NULL,NULL,NULL,0,NULL,'published','announcement_board','2026-06-20 01:00:00','2026-07-03 06:23:28','2026-07-03 06:23:28'),('ann_dev005','邻里跳蚤市场活动招募公告','本周六社区中心广场将举办\"旧物换新颜\"二手市集，欢迎各位邻里踊跃报名设摊或参与交换。','community','notification',NULL,'pc_demo001','com_demo001',NULL,NULL,'res_act01','activity_leader',NULL,NULL,NULL,0,NULL,'published','announcement_board','2026-06-28 02:00:00','2026-07-03 06:23:28','2026-07-03 06:23:28'),('ann_seed001','关于物业费缴纳的温馨提示','尊敬的业主：2026年第三季度物业费已出账，请您于8月31日前通过微信、物业币或积分抵扣等方式完成缴纳，感谢您的支持与配合。','property','notification',NULL,'pc_demo001','com_demo001',NULL,NULL,'res_propadmin01','property_admin',NULL,NULL,NULL,0,NULL,'published','announcement_board','2026-08-12 09:00:00','2026-08-12 09:00:00','2026-08-12 09:00:00'),('ann_seed002','社区便民服务日招募','本周末社区中心将举办便民服务日，提供免费理发、义诊、家电检修等服务，欢迎邻里踊跃参与。','community','notification',NULL,'pc_demo001','com_demo001',NULL,NULL,'res_propadmin01','property_admin',NULL,NULL,NULL,0,NULL,'published','announcement_board','2026-08-12 09:00:00','2026-08-12 09:00:00','2026-08-12 09:00:00'),('ann_seed003','邻里跳蚤市场活动预告','本周六社区广场举办“旧物换新颜”二手市集，欢迎报名设摊或前往交换闲置好物。','activity','activity',NULL,'pc_demo001',NULL,NULL,NULL,'res_coo01','coordinator',NULL,NULL,NULL,0,NULL,'published','announcement_board','2026-08-12 09:00:00','2026-08-12 09:00:00','2026-08-12 09:00:00');
/*!40000 ALTER TABLE `announcements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `arrears_reminder_audit`
--

DROP TABLE IF EXISTS `arrears_reminder_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `arrears_reminder_audit` (
  `id` varchar(32) NOT NULL COMMENT '记录ID（主键）',
  `batch_id` varchar(64) DEFAULT NULL COMMENT '批次ID',
  `request_id` varchar(64) DEFAULT NULL COMMENT '客户端幂等ID',
  `property_company_id` varchar(32) DEFAULT NULL COMMENT '物业公司ID',
  `community_id` varchar(32) DEFAULT NULL COMMENT '小区ID',
  `building` varchar(50) DEFAULT NULL COMMENT '楼栋',
  `fee_type` varchar(30) DEFAULT NULL COMMENT '费用类型编码',
  `channels` varchar(50) DEFAULT NULL COMMENT '发送的渠道，如 in_app,wechat',
  `recipient_count` int NOT NULL DEFAULT '0' COMMENT '收件人数',
  `notified_count` int NOT NULL DEFAULT '0' COMMENT '成功通知数',
  `skipped_count` int NOT NULL DEFAULT '0' COMMENT '跳过数',
  `failed_count` int NOT NULL DEFAULT '0' COMMENT '失败数',
  `in_app_success` int NOT NULL DEFAULT '0' COMMENT '应用内消息成功数',
  `in_app_failed` int NOT NULL DEFAULT '0' COMMENT '应用内消息失败数',
  `wechat_success` int NOT NULL DEFAULT '0' COMMENT '微信消息成功数',
  `wechat_skipped` int NOT NULL DEFAULT '0' COMMENT '微信消息跳过数',
  `wechat_failed` int NOT NULL DEFAULT '0' COMMENT '微信消息失败数',
  `total_arrears_amount` decimal(12,2) DEFAULT NULL COMMENT '总欠费金额',
  `operator` varchar(64) DEFAULT NULL COMMENT '操作人',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_arrears_audit_batch` (`batch_id`),
  KEY `idx_arrears_audit_company` (`property_company_id`),
  KEY `idx_arrears_audit_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='欠费催缴发送审计';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `arrears_reminder_audit`
--

LOCK TABLES `arrears_reminder_audit` WRITE;
/*!40000 ALTER TABLE `arrears_reminder_audit` DISABLE KEYS */;
INSERT INTO `arrears_reminder_audit` VALUES ('audit_1b6a1001','batch_1b6a1000','arrears_1788793015226_lkpu2m2i','pc_demo001',NULL,NULL,NULL,'in_app',9,9,0,0,9,0,0,0,0,5471.11,'res_admin01','2026-09-07 14:56:58'),('audit_35866e25541000','batch_35866e24141000','arrears_1787475513410_n9vywtol','pc_demo001',NULL,NULL,NULL,'[in_app]',11,11,0,0,11,0,0,0,0,NULL,NULL,'2026-08-23 08:58:36');
/*!40000 ALTER TABLE `arrears_reminder_audit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `arrears_reminder_preview`
--

DROP TABLE IF EXISTS `arrears_reminder_preview`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `arrears_reminder_preview` (
  `token` varchar(64) NOT NULL COMMENT '预览令牌（主键），如 prev_<uuid>',
  `property_company_id` varchar(32) DEFAULT NULL COMMENT '物业公司ID',
  `community_id` varchar(32) DEFAULT NULL COMMENT '小区ID',
  `building` varchar(50) DEFAULT NULL COMMENT '楼栋',
  `fee_type` varchar(30) DEFAULT NULL COMMENT '费用类型编码',
  `resident_ids` text COMMENT '收件人住户ID（JSON 数组字符串）',
  `recipient_count` int NOT NULL DEFAULT '0' COMMENT '收件人数',
  `total_arrears_amount` decimal(12,2) DEFAULT NULL COMMENT '总欠费金额',
  `expires_at` datetime DEFAULT NULL COMMENT '预览过期时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`token`),
  KEY `idx_arrears_preview_expires` (`expires_at`),
  KEY `idx_arrears_preview_company` (`property_company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='欠费催缴预览快照';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `arrears_reminder_preview`
--

LOCK TABLES `arrears_reminder_preview` WRITE;
/*!40000 ALTER TABLE `arrears_reminder_preview` DISABLE KEYS */;
/*!40000 ALTER TABLE `arrears_reminder_preview` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `arrears_reminder_request`
--

DROP TABLE IF EXISTS `arrears_reminder_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `arrears_reminder_request` (
  `request_id` varchar(64) NOT NULL COMMENT '客户端幂等ID（主键）',
  `batch_id` varchar(64) DEFAULT NULL COMMENT '关联审计批次ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`request_id`),
  KEY `idx_arrears_request_batch` (`batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='欠费催缴发送幂等记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `arrears_reminder_request`
--

LOCK TABLES `arrears_reminder_request` WRITE;
/*!40000 ALTER TABLE `arrears_reminder_request` DISABLE KEYS */;
INSERT INTO `arrears_reminder_request` VALUES ('arrears_1787475513410_n9vywtol',NULL,'2026-08-23 08:58:36'),('arrears_1788793015226_lkpu2m2i',NULL,'2026-09-07 14:56:58');
/*!40000 ALTER TABLE `arrears_reminder_request` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `building_change_requests`
--

DROP TABLE IF EXISTS `building_change_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `building_change_requests` (
  `id` varchar(32) NOT NULL COMMENT 'bcr_xxxxxxxx',
  `resident_id` varchar(32) NOT NULL COMMENT '申请住户ID',
  `property_company_id` varchar(32) DEFAULT NULL COMMENT '物业公司ID',
  `community_id` varchar(32) DEFAULT NULL COMMENT '目标小区ID',
  `building` varchar(20) DEFAULT NULL COMMENT '目标楼栋',
  `unit` varchar(20) DEFAULT NULL COMMENT '目标单元',
  `floor` varchar(20) DEFAULT NULL COMMENT '目标楼层',
  `room` varchar(20) DEFAULT NULL COMMENT '目标房号',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT 'pending/approved/rejected',
  `reject_reason` varchar(200) DEFAULT NULL COMMENT '驳回原因',
  `applied_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `audited_at` timestamp NULL DEFAULT NULL COMMENT '审批时间',
  `auditor_id` varchar(32) DEFAULT NULL COMMENT '审批人ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_bcr_resident` (`resident_id`),
  KEY `idx_bcr_property` (`property_company_id`),
  KEY `idx_bcr_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='楼栋变更申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `building_change_requests`
--

LOCK TABLES `building_change_requests` WRITE;
/*!40000 ALTER TABLE `building_change_requests` DISABLE KEYS */;
INSERT INTO `building_change_requests` VALUES ('bcr_0c661000','res_dev001','pc_demo001','com_demo001','1','1','1','108','pending',NULL,'2026-09-11 08:03:35',NULL,NULL,'2026-09-11 08:03:35','2026-09-11 08:03:35'),('bcr_3547c609f61000','res_dev001','pc_demo001','com_demo001','1','1','1','101','approved',NULL,'2026-08-17 06:58:34','2026-08-18 01:21:42','res_admin01','2026-08-17 06:58:34','2026-08-18 01:21:42'),('bcr_354fdb211a1000','res_dev001','pc_demo001','com_demo002','1','1','1','101','approved',NULL,'2026-08-18 01:48:34','2026-08-18 01:48:43','res_admin01','2026-08-18 01:48:34','2026-08-18 01:48:43'),('bcr_35527d83de1000','res_dev001','pc_demo001','com_demo001','1','1','1','108','approved',NULL,'2026-08-18 07:56:52','2026-08-18 07:56:59','res_propadmin01','2026-08-18 07:56:52','2026-08-18 07:56:59'),('bcr_3564b7f9601000','res_dev001','pc_demo001','com_demo001','2','1','1层','101','rejected','1','2026-08-20 02:25:23','2026-08-20 02:28:32','res_admin01','2026-08-20 02:25:23','2026-08-20 02:28:32'),('bcr_356fc73f461000','res_dev001','pc_demo001','com_demo001','3','1','5层','503','approved',NULL,'2026-08-21 04:11:38','2026-08-21 07:42:57','res_admin01','2026-08-21 04:11:38','2026-08-21 07:42:57'),('bcr_357b4a91401000','res_dev001','pc_demo001','com_demo001','3','1','5层','503','rejected','不通过','2026-08-22 07:01:16','2026-08-23 02:32:15','res_admin01','2026-08-22 07:01:16','2026-08-23 02:32:15'),('bcr_3583aeabc41000','res_dev001','pc_demo001','com_demo001','1','1','1','108','approved',NULL,'2026-08-23 02:34:25','2026-08-23 02:34:29','res_admin01','2026-08-23 02:34:25','2026-08-23 02:34:29');
/*!40000 ALTER TABLE `building_change_requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `canteen_directed_balances`
--

DROP TABLE IF EXISTS `canteen_directed_balances`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `canteen_directed_balances` (
  `id` varchar(32) NOT NULL COMMENT 'cdb_xxxxxxxx',
  `resident_id` varchar(32) NOT NULL COMMENT '用户ID',
  `main_merchant_id` varchar(32) NOT NULL COMMENT '所属主商家体系（餐饮公司）',
  `property_company_id` varchar(32) NOT NULL COMMENT '所属物业公司',
  `balance` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '可用定向余额',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cdb_resident_main` (`resident_id`,`main_merchant_id`),
  UNIQUE KEY `uk_canteen_directed_balance` (`resident_id`,`main_merchant_id`),
  KEY `idx_cdb_property` (`property_company_id`),
  KEY `fk_cdb_main` (`main_merchant_id`),
  CONSTRAINT `fk_cdb_main` FOREIGN KEY (`main_merchant_id`) REFERENCES `merchants` (`id`),
  CONSTRAINT `fk_cdb_property` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`),
  CONSTRAINT `fk_cdb_resident` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社区食堂-用户定向余额账户';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `canteen_directed_balances`
--

LOCK TABLES `canteen_directed_balances` WRITE;
/*!40000 ALTER TABLE `canteen_directed_balances` DISABLE KEYS */;
INSERT INTO `canteen_directed_balances` VALUES ('cdb_35d143eb1c1000','res_dev001','mch_35c14661b81000','pc_demo001',2.00,2,'2026-08-30 15:21:18','2026-08-30 15:22:00');
/*!40000 ALTER TABLE `canteen_directed_balances` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `canteen_directed_flows`
--

DROP TABLE IF EXISTS `canteen_directed_flows`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `canteen_directed_flows` (
  `id` varchar(32) NOT NULL COMMENT 'cdf_xxxxxxxx',
  `resident_id` varchar(32) NOT NULL COMMENT '用户ID',
  `main_merchant_id` varchar(32) NOT NULL COMMENT '所属主商家体系',
  `merchant_id` varchar(32) NOT NULL COMMENT '实际消费商家（主商家或子商家）',
  `order_id` varchar(32) DEFAULT NULL COMMENT '关联订单ID',
  `flow_type` varchar(20) NOT NULL COMMENT 'consume_deduct 消费扣减 / refund_back 退款退回',
  `amount` decimal(12,2) NOT NULL COMMENT '发生金额（正数）',
  `balance_after` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '变动后定向余额快照',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_cdf_resident` (`resident_id`),
  KEY `idx_cdf_main` (`main_merchant_id`),
  KEY `idx_cdf_merchant` (`merchant_id`),
  KEY `idx_cdf_order` (`order_id`),
  CONSTRAINT `fk_cdf_main` FOREIGN KEY (`main_merchant_id`) REFERENCES `merchants` (`id`),
  CONSTRAINT `fk_cdf_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`),
  CONSTRAINT `fk_cdf_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_cdf_resident` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社区食堂-定向余额消费/退款流水';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `canteen_directed_flows`
--

LOCK TABLES `canteen_directed_flows` WRITE;
/*!40000 ALTER TABLE `canteen_directed_flows` DISABLE KEYS */;
INSERT INTO `canteen_directed_flows` VALUES ('cdf_35d144bfc61000','res_dev001','mch_35c14661b81000','mch_35c14661b81000','ord_35d14493e61000','consume_deduct',10.00,10.00,'2026-08-30 15:21:45','2026-08-30 15:21:45'),('cdf_35d14537061000','res_dev001','mch_35c14661b81000','mch_35c14bfbe61000','ord_35d14511e01000','consume_deduct',8.00,2.00,'2026-08-30 15:22:00','2026-08-30 15:22:00');
/*!40000 ALTER TABLE `canteen_directed_flows` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `canteen_directed_recharges`
--

DROP TABLE IF EXISTS `canteen_directed_recharges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `canteen_directed_recharges` (
  `id` varchar(32) NOT NULL COMMENT 'cdr_xxxxxxxx',
  `main_merchant_id` varchar(32) NOT NULL COMMENT '充值主体（主商家）',
  `resident_id` varchar(32) NOT NULL COMMENT '被充值用户ID',
  `property_company_id` varchar(32) NOT NULL COMMENT '所属物业公司',
  `amount` decimal(12,2) NOT NULL COMMENT '充值金额',
  `balance_after` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '充值后该体系定向余额快照',
  `operator_id` varchar(32) DEFAULT NULL COMMENT '操作人住户ID（主商家店主）',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_cdr_main` (`main_merchant_id`),
  KEY `idx_cdr_resident` (`resident_id`),
  KEY `idx_cdr_property` (`property_company_id`),
  CONSTRAINT `fk_cdr_main` FOREIGN KEY (`main_merchant_id`) REFERENCES `merchants` (`id`),
  CONSTRAINT `fk_cdr_property` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`),
  CONSTRAINT `fk_cdr_resident` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社区食堂-定向充值流水';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `canteen_directed_recharges`
--

LOCK TABLES `canteen_directed_recharges` WRITE;
/*!40000 ALTER TABLE `canteen_directed_recharges` DISABLE KEYS */;
INSERT INTO `canteen_directed_recharges` VALUES ('cdr_35d143eb2a1000','mch_35c14661b81000','res_dev001','pc_demo001',20.00,20.00,'res_35c14607ae1000',NULL,'2026-08-30 15:21:18','2026-08-30 15:21:18');
/*!40000 ALTER TABLE `canteen_directed_recharges` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `canteen_merchant_bindings`
--

DROP TABLE IF EXISTS `canteen_merchant_bindings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `canteen_merchant_bindings` (
  `id` varchar(32) NOT NULL COMMENT 'cmb_xxxxxxxx',
  `main_merchant_id` varchar(32) NOT NULL COMMENT '主商家ID（餐饮公司，统一开票主体）',
  `sub_merchant_id` varchar(32) NOT NULL COMMENT '子商家ID（麻辣烫/米线/烧烤等供货商家）',
  `property_company_id` varchar(32) NOT NULL COMMENT '所属物业公司',
  `supply_discount_rate` decimal(5,4) NOT NULL DEFAULT '0.2000' COMMENT '供货优惠比例（0.20=子商家给主商家供货8折）',
  `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT 'active 启用 / inactive 停用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_canteen_binding` (`main_merchant_id`,`sub_merchant_id`),
  KEY `idx_canteen_sub_merchant` (`sub_merchant_id`),
  KEY `idx_canteen_property` (`property_company_id`),
  CONSTRAINT `fk_canteen_binding_main` FOREIGN KEY (`main_merchant_id`) REFERENCES `merchants` (`id`),
  CONSTRAINT `fk_canteen_binding_property` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`),
  CONSTRAINT `fk_canteen_binding_sub` FOREIGN KEY (`sub_merchant_id`) REFERENCES `merchants` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社区食堂-主商家子商家绑定表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `canteen_merchant_bindings`
--

LOCK TABLES `canteen_merchant_bindings` WRITE;
/*!40000 ALTER TABLE `canteen_merchant_bindings` DISABLE KEYS */;
INSERT INTO `canteen_merchant_bindings` VALUES ('cmb_35af1eb50e1000','mch_demo002','mch_tech01','pc_demo001',0.2000,'inactive','2026-08-27 07:47:26','2026-08-27 07:51:00'),('cmb_35c1833d461000','mch_35c14661b81000','mch_35c14bfbe61000','pc_demo001',0.2000,'active','2026-08-29 02:38:55','2026-08-29 02:38:55');
/*!40000 ALTER TABLE `canteen_merchant_bindings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `canteen_quota_purchases`
--

DROP TABLE IF EXISTS `canteen_quota_purchases`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `canteen_quota_purchases` (
  `id` varchar(32) NOT NULL COMMENT 'cqp_xxxxxxxx',
  `merchant_id` varchar(32) NOT NULL COMMENT '购买商家（必须为主商家）',
  `property_company_id` varchar(32) NOT NULL COMMENT '所属物业公司',
  `quota_amount` decimal(12,2) NOT NULL COMMENT '申请购买额度金额（= 扫呗支付金额）',
  `fee_rate` decimal(5,4) NOT NULL DEFAULT '0.0200' COMMENT '手续费比例快照（0.02=2%）',
  `fee_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '手续费金额 = quota_amount × fee_rate',
  `actual_quota` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '实际到账额度 = quota_amount - fee_amount',
  `pay_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '扫呗下单支付金额（= quota_amount）',
  `payment_record_id` varchar(32) DEFAULT NULL COMMENT '关联支付流水ID（payment_records.id）',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT 'pending待支付 / paid已到账 / refunded已退款 / closed已关闭',
  `remaining_quota` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '剩余可用额度（到账后初始=actual_quota，充值后递减）',
  `completed_at` timestamp NULL DEFAULT NULL COMMENT '支付完成/到账时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_cqp_merchant` (`merchant_id`),
  KEY `idx_cqp_property` (`property_company_id`),
  KEY `idx_cqp_status` (`status`),
  KEY `idx_cqp_payment` (`payment_record_id`),
  CONSTRAINT `fk_cqp_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`),
  CONSTRAINT `fk_cqp_payment` FOREIGN KEY (`payment_record_id`) REFERENCES `payment_records` (`id`),
  CONSTRAINT `fk_cqp_property` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社区食堂-商家额度购买流水';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `canteen_quota_purchases`
--

LOCK TABLES `canteen_quota_purchases` WRITE;
/*!40000 ALTER TABLE `canteen_quota_purchases` DISABLE KEYS */;
INSERT INTO `canteen_quota_purchases` VALUES ('cqp_35d01ac2601000','mch_35c14661b81000','pc_demo001',100.00,0.0000,0.00,100.00,0.00,NULL,'paid',80.00,'2026-08-30 12:39:01','2026-08-30 12:39:01','2026-08-30 15:21:18'),('cqp_35db1972ae1000','mch_35c14661b81000','pc_demo001',50.00,0.0000,0.00,50.00,0.00,NULL,'paid',50.00,'2026-08-31 14:16:12','2026-08-31 14:16:12','2026-08-31 14:16:12');
/*!40000 ALTER TABLE `canteen_quota_purchases` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cbk_accounts`
--

DROP TABLE IF EXISTS `cbk_accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cbk_accounts` (
  `id` varchar(32) NOT NULL COMMENT '账户注册ID',
  `owner_type` varchar(32) NOT NULL COMMENT '收款方类型(见 CbkOwnerTypes)',
  `owner_id` varchar(32) NOT NULL COMMENT '实体ID;平台固定为 PLATFORM',
  `merchant_no` varchar(64) DEFAULT NULL COMMENT 'CBK 商户号 merchant_no',
  `account_no` varchar(64) DEFAULT NULL COMMENT 'CBK 账户号(account_in/account_out 使用)',
  `account_name` varchar(128) DEFAULT NULL COMMENT '账户名/户名',
  `currency` varchar(8) DEFAULT 'CNY' COMMENT '币种',
  `verified` tinyint(1) DEFAULT '0' COMMENT '是否已通过开户/绑卡校验',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_cbk_account_owner` (`owner_type`,`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='CBK 收款账户注册表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cbk_accounts`
--

LOCK TABLES `cbk_accounts` WRITE;
/*!40000 ALTER TABLE `cbk_accounts` DISABLE KEYS */;
/*!40000 ALTER TABLE `cbk_accounts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cbk_split_record`
--

DROP TABLE IF EXISTS `cbk_split_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cbk_split_record` (
  `id` varchar(32) NOT NULL COMMENT '分账记录ID',
  `payment_record_id` varchar(32) DEFAULT NULL COMMENT '关联 payment_records.id',
  `out_trade_no` varchar(64) DEFAULT NULL COMMENT '扫呗支付单号(源支付)',
  `biz_type` varchar(32) DEFAULT NULL COMMENT '业务类型 PaymentBizType',
  `order_id` varchar(32) DEFAULT NULL COMMENT '业务单号(订单/充值单等)',
  `split_no` varchar(64) DEFAULT NULL COMMENT '自定义分账订单号(6.2/6.12 out_trade_no)',
  `cbk_sub_trade_no` varchar(64) DEFAULT NULL COMMENT '扫呗分账交易号 sub_trade_no',
  `payer_account` varchar(64) DEFAULT NULL COMMENT '出账方CBK账户',
  `payee_account` varchar(64) DEFAULT NULL COMMENT '入账方CBK账户',
  `freeze_amount` decimal(10,2) DEFAULT '0.00' COMMENT '冻结/分账金额(元)',
  `status` varchar(32) DEFAULT NULL COMMENT 'frozen_pending/frozen/finished/cancelled/reverted...',
  `freeze_resp` text COMMENT '6.2冻结返回报文',
  `finish_resp` text COMMENT '6.12完结/撤销返回报文',
  `reverse_resp` text COMMENT '6.9冲正退回返回报文',
  `remark` text COMMENT '备注/失败原因',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `group_no` varchar(32) DEFAULT NULL COMMENT '分账批次号(同笔支付所有收款方共享)',
  `owner_type` varchar(32) DEFAULT NULL COMMENT '收款方类型 PROPERTY/MERCHANT/COORDINATOR/SECTOR_LEADER/INDIVIDUAL_LEADER/COURIER/PLATFORM',
  `owner_id` varchar(32) DEFAULT NULL COMMENT '收款方实体ID(平台固定PLATFORM)',
  `currency` varchar(8) DEFAULT 'CNY' COMMENT '币种',
  `cbk_out_trade_no` varchar(64) DEFAULT NULL COMMENT '扫呗6.7回调分账订单号(out_trade_no)',
  `withdraw_status` varchar(32) DEFAULT NULL COMMENT '提现状态(Path B): null/withdrawing/withdrawn/withdraw_failed',
  `withdraw_resp` text COMMENT '6.4 提现接口返回报文(JSON)',
  `withdraw_trade_no` varchar(64) DEFAULT NULL COMMENT '提现幂等单号(trade_no=splitNo+_WD)',
  PRIMARY KEY (`id`),
  KEY `idx_payment_record` (`payment_record_id`),
  KEY `idx_out_trade_no` (`out_trade_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_split_no` (`split_no`),
  KEY `idx_sub_trade_no` (`cbk_sub_trade_no`),
  KEY `idx_status` (`status`),
  KEY `idx_group_no` (`group_no`),
  KEY `idx_owner` (`owner_type`,`owner_id`),
  KEY `idx_withdraw_status` (`withdraw_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='扫呗分账流水记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cbk_split_record`
--

LOCK TABLES `cbk_split_record` WRITE;
/*!40000 ALTER TABLE `cbk_split_record` DISABLE KEYS */;
INSERT INTO `cbk_split_record` VALUES ('sp__355169726e1000',NULL,'ord_35513f81481000','MERCHANT_FREEZE','ord_35513f81481000','MERDdlv_355140d5241000',NULL,NULL,NULL,0.18,'skipped',NULL,NULL,NULL,'商家未注册CBK账户(缺户)，待补冻','2026-08-18 05:26:06','2026-08-18 05:26:06',NULL,'MERCHANT','mch_demo002','CNY',NULL,NULL,NULL,NULL),('sp__355222dbbe1000',NULL,'ord_3552221f381000','MERCHANT_FREEZE','ord_3552221f381000','MERDdlv_355222cb781000',NULL,NULL,NULL,0.18,'skipped',NULL,NULL,NULL,'商家未注册CBK账户(缺户)，待补冻','2026-08-18 07:07:21','2026-08-18 07:07:21',NULL,'MERCHANT','mch_demo002','CNY',NULL,NULL,NULL,NULL),('sp__358f11fcea1000','pr_35268b1d9e1000','304188407901626081409323912266','PAY_FREEZE','ord_35268adb481000',NULL,NULL,NULL,NULL,NULL,'skipped',NULL,NULL,NULL,'未配置微信收款账户(商户号/openid)','2026-08-24 05:06:34','2026-08-24 05:06:34','grp_358f11fcdc1000','MERCHANT','mch_demo002','CNY',NULL,NULL,NULL,NULL),('sp__358f11fd441000','pr_35268b1d9e1000','304188407901626081409323912266','PAY_FREEZE','ord_35268adb481000',NULL,NULL,NULL,NULL,NULL,'skipped',NULL,NULL,NULL,'未配置微信收款账户(商户号/openid)','2026-08-24 05:06:34','2026-08-24 05:06:34','grp_358f11fcdc1000','PROPERTY','pc_demo001','CNY',NULL,NULL,NULL,NULL),('sp__358f11fd641000','pr_35268b1d9e1000','304188407901626081409323912266','PAY_FREEZE','ord_35268adb481000',NULL,NULL,NULL,NULL,NULL,'skipped',NULL,NULL,NULL,'未配置微信收款账户(商户号/openid)','2026-08-24 05:06:34','2026-08-24 05:06:34','grp_358f11fcdc1000','COORDINATOR','coo_001','CNY',NULL,NULL,NULL,NULL),('sp__358f11fd721000','pr_35268b1d9e1000','304188407901626081409323912266','UNFREEZE','ord_35268adb481000','unf_358f11fd721001',NULL,NULL,NULL,NULL,'finish_failed',NULL,NULL,NULL,'未知 transaction_id，分账失败: 304188407901626081409323912266','2026-08-24 05:06:34','2026-08-24 18:30:00','grp_358f11fcdc1000','PLATFORM','PLATFORM','CNY',NULL,NULL,NULL,NULL),('sp__358f11fe461000','pr_3526ad7ab01000','304188401601626081409512519175','PAY_FREEZE','ord_3526ad6bfc1000',NULL,NULL,NULL,NULL,NULL,'skipped',NULL,NULL,NULL,'未配置微信收款账户(商户号/openid)','2026-08-24 05:06:34','2026-08-24 05:06:34','grp_358f11fe3c1000','MERCHANT','mch_demo002','CNY',NULL,NULL,NULL,NULL),('sp__358f11fe621000','pr_3526ad7ab01000','304188401601626081409512519175','PAY_FREEZE','ord_3526ad6bfc1000',NULL,NULL,NULL,NULL,NULL,'skipped',NULL,NULL,NULL,'未配置微信收款账户(商户号/openid)','2026-08-24 05:06:34','2026-08-24 05:06:34','grp_358f11fe3c1000','PROPERTY','pc_demo001','CNY',NULL,NULL,NULL,NULL),('sp__358f11fe6e1000','pr_3526ad7ab01000','304188401601626081409512519175','UNFREEZE','ord_3526ad6bfc1000','unf_358f11fe6e1001',NULL,NULL,NULL,NULL,'finish_failed',NULL,NULL,NULL,'未知 transaction_id，分账失败: 304188401601626081409512519175','2026-08-24 05:06:34','2026-08-24 18:30:00','grp_358f11fe3c1000','PLATFORM','PLATFORM','CNY',NULL,NULL,NULL,NULL),('sp__358f172fe61000','pr_35266c00941000','304188401401626081409153905802','PAY_FREEZE','ord_35266bd4c41000',NULL,NULL,NULL,NULL,NULL,'skipped',NULL,NULL,NULL,'未配置微信收款账户(商户号/openid)','2026-08-24 05:09:24','2026-08-24 05:09:24','grp_358f172fe21000','MERCHANT','mch_demo002','CNY',NULL,NULL,NULL,NULL),('sp__358f1730101000','pr_35266c00941000','304188401401626081409153905802','PAY_FREEZE','ord_35266bd4c41000',NULL,NULL,NULL,NULL,NULL,'skipped',NULL,NULL,NULL,'未配置微信收款账户(商户号/openid)','2026-08-24 05:09:24','2026-08-24 05:09:24','grp_358f172fe21000','PROPERTY','pc_demo001','CNY',NULL,NULL,NULL,NULL),('sp__358f1730141000','pr_35266c00941000','304188401401626081409153905802','UNFREEZE','ord_35266bd4c41000','unf_358f1730141001',NULL,NULL,NULL,NULL,'finish_failed',NULL,NULL,NULL,'未知 transaction_id，分账失败: 304188401401626081409153905802','2026-08-24 05:09:24','2026-08-24 18:30:00','grp_358f172fe21000','PLATFORM','PLATFORM','CNY',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `cbk_split_record` ENABLE KEYS */;
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
INSERT INTO `coin_freeze_records` VALUES ('cfr_358443819a1000','res_dev001','pc_demo001','freeze',0.00,'1','res_propadmin01','2026-08-23 03:55:42','2026-08-23 03:55:42'),('cfr_35844525ac1000','res_dev001','pc_demo001','unfreeze',0.00,'1','res_propadmin01','2026-08-23 03:56:35','2026-08-23 03:56:35'),('cfr_3584bb04a21000','res_dev001','pc_demo001','freeze',0.00,'1','res_propadmin01','2026-08-23 05:00:58','2026-08-23 05:00:58'),('cfr_35852f7e741000','res_dev001','pc_demo001','unfreeze',0.00,'1','res_propadmin01','2026-08-23 06:04:34','2026-08-23 06:04:34');
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
  `completed_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `audited_at` timestamp NULL DEFAULT NULL COMMENT '审核时间',
  `version` bigint NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
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
  `latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度',
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
INSERT INTO `communities` VALUES ('com_8e101000','pc_8df81000','测试小区0811a',NULL,NULL,NULL,'active',NULL,NULL,'2026-09-07 14:38:38','2026-09-07 14:38:38'),('com_baid_001','pc_baid001','碧桂园·翡翠湾','深圳市南山区科技园路1号',8,16,'active',NULL,NULL,'2026-08-01 14:17:53','2026-08-01 14:17:53'),('com_baid_002','pc_baid001','碧桂园·凤凰城','深圳市宝安区西乡大道1号',12,24,'active',NULL,NULL,'2026-08-01 14:17:53','2026-08-01 14:17:53'),('com_demo001','pc_demo001','演示小区',NULL,10,500,'active',NULL,NULL,'2026-07-02 01:57:36','2026-07-02 01:57:36'),('com_demo002','pc_demo001','演示小区2',NULL,10,500,'active',NULL,NULL,'2026-08-07 02:21:10','2026-08-07 02:21:10'),('com_hrl_001','pc_hrl001','华润·幸福里','深圳市福田区中心路2号',6,12,'active',NULL,NULL,'2026-08-01 14:17:53','2026-08-01 14:17:53'),('com_hrl_002','pc_hrl001','华润·悦府','深圳市罗湖区深南东路1号',9,18,'active',NULL,NULL,'2026-08-01 14:17:53','2026-08-01 14:17:53'),('com_vanke_001','pc_vanke001','万科·金域华府','深圳市龙岗区坂田路3号',10,20,'active',NULL,NULL,'2026-08-01 14:17:53','2026-08-01 14:17:53'),('com_vanke_002','pc_vanke001','万科·城市花园','深圳市龙华区民治大道1号',7,14,'active',NULL,NULL,'2026-08-01 14:17:53','2026-08-01 14:17:53');
/*!40000 ALTER TABLE `communities` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `community_comments`
--

DROP TABLE IF EXISTS `community_comments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `community_comments` (
  `id` varchar(32) NOT NULL,
  `post_id` varchar(32) DEFAULT NULL,
  `author_id` varchar(32) DEFAULT NULL,
  `author_name` varchar(100) DEFAULT NULL,
  `author_role` varchar(30) DEFAULT NULL,
  `content` text,
  `parent_id` varchar(32) DEFAULT NULL COMMENT '被回复评论ID，顶级评论为NULL',
  `reply_to_user_id` varchar(32) DEFAULT NULL COMMENT '被回复用户ID',
  `reply_to_user_name` varchar(100) DEFAULT NULL COMMENT '被回复用户名称',
  `status` varchar(20) DEFAULT 'published',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_post` (`post_id`,`status`,`created_at`),
  KEY `fk_cc_post` (`post_id`),
  CONSTRAINT `fk_cc_post` FOREIGN KEY (`post_id`) REFERENCES `community_posts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `community_comments`
--

LOCK TABLES `community_comments` WRITE;
/*!40000 ALTER TABLE `community_comments` DISABLE KEYS */;
INSERT INTO `community_comments` VALUES ('cc__35601835681000','cp_seed001','res_dev001','张阿姨','resident','。？。',NULL,NULL,NULL,'published','2026-08-19 15:38:53','2026-08-19 15:38:53'),('cc__3583bb41401000','cp__3583baf9461000','res_dev001','张阿姨','resident','真的',NULL,NULL,NULL,'deleted','2026-08-23 02:41:17','2026-08-23 08:27:20'),('cc__cb801000','cp__830c1000','res_dev001','张阿姨','resident','哇塞',NULL,NULL,NULL,'published','2026-09-06 10:01:11','2026-09-06 10:01:11'),('cc_gen0001','cp_gen001','res_courier03','快递员-王五','resident','收到，感谢提醒',NULL,NULL,NULL,'published','2026-07-17 02:50:15','2026-07-17 02:50:15'),('cc_gen0002','cp_gen001','res_001','业主-赵六','resident','已收藏，谢谢分享',NULL,NULL,NULL,'published','2026-07-27 04:07:55','2026-07-27 04:07:55'),('cc_gen0003','cp_gen002','res_courier02','快递员-李四','resident','收到，感谢提醒',NULL,NULL,NULL,'published','2026-07-12 20:21:17','2026-07-12 20:21:17'),('cc_gen0004','cp_gen003','res_mch02','口福老板','resident','请问怎么报名？',NULL,NULL,NULL,'published','2026-07-04 22:28:09','2026-07-04 22:28:09'),('cc_gen0005','cp_gen003','res_dev002','张建设','resident','已收藏，谢谢分享',NULL,NULL,NULL,'published','2026-07-20 02:26:30','2026-07-20 02:26:30'),('cc_gen0006','cp_gen003','res_003','业主-孙八','resident','点赞',NULL,NULL,NULL,'published','2026-08-09 22:41:09','2026-08-09 22:41:09'),('cc_gen0007','cp_gen003','res_dev004','王秀兰','resident','说得对！',NULL,NULL,NULL,'published','2026-07-16 19:09:01','2026-07-16 19:09:01'),('cc_gen0008','cp_gen004','res_001','业主-赵六','resident','说得对！',NULL,NULL,NULL,'published','2026-08-01 23:20:28','2026-08-01 23:20:28'),('cc_gen0009','cp_gen004','res_courier01','快递员-吴十','resident','说得对！',NULL,NULL,NULL,'published','2026-08-02 15:44:16','2026-08-02 15:44:16'),('cc_gen0010','cp_gen005','res_propadmin01','物业管理员-刘芳','resident','已收藏，谢谢分享',NULL,NULL,NULL,'published','2026-07-06 00:08:34','2026-07-06 00:08:34'),('cc_gen0011','cp_gen005','res_ind01','个体负责人-王强','resident','支持一下',NULL,NULL,NULL,'published','2026-07-03 00:20:46','2026-07-03 00:20:46'),('cc_gen0012','cp_gen005','res_dev004','王秀兰','resident','点赞',NULL,NULL,NULL,'published','2026-07-24 01:23:10','2026-07-24 01:23:10'),('cc_gen0013','cp_gen005','res_dev003','张子涵','resident','已转发到业主群',NULL,NULL,NULL,'published','2026-07-05 10:49:01','2026-07-05 10:49:01'),('cc_gen0014','cp_gen006','res_dev004','王秀兰','resident','太实用了',NULL,NULL,NULL,'published','2026-07-06 00:25:52','2026-07-06 00:25:52'),('cc_gen0015','cp_gen007','res_mch02','口福老板','resident','我也遇到了',NULL,NULL,NULL,'published','2026-08-09 22:14:21','2026-08-09 22:14:21'),('cc_gen0016','cp_gen007','res_dev002','张建设','resident','请问怎么报名？',NULL,NULL,NULL,'published','2026-07-16 12:11:17','2026-07-16 12:11:17'),('cc_gen0017','cp_gen008','res_dev004','王秀兰','resident','收到，感谢提醒',NULL,NULL,NULL,'published','2026-07-18 12:39:10','2026-07-18 12:39:10'),('cc_gen0018','cp_gen008','res_courier01','快递员-吴十','resident','点赞',NULL,NULL,NULL,'published','2026-07-19 01:52:03','2026-07-19 01:52:03'),('cc_gen0019','cp_gen008','res_admin01','平台管理员','resident','已收藏，谢谢分享',NULL,NULL,NULL,'published','2026-07-26 03:32:11','2026-07-26 03:32:11'),('cc_gen0020','cp_gen009','res_dev002','张建设','resident','周末有空去看看',NULL,NULL,NULL,'published','2026-08-08 17:14:36','2026-08-08 17:14:36'),('cc_gen0021','cp_gen009','res_dev001','张阿姨','resident','已收藏，谢谢分享',NULL,NULL,NULL,'published','2026-08-05 03:40:04','2026-08-05 03:40:04'),('cc_gen0022','cp_gen010','res_004','租户-周九','resident','已转发到业主群',NULL,NULL,NULL,'published','2026-08-11 20:34:04','2026-08-11 20:34:04'),('cc_gen0023','cp_gen010','res_dev003','张子涵','resident','说得对！',NULL,NULL,NULL,'published','2026-08-12 12:52:36','2026-08-12 12:52:36'),('cc_gen0024','cp_gen010','res_004','租户-周九','resident','已收藏，谢谢分享',NULL,NULL,NULL,'published','2026-08-04 17:42:54','2026-08-04 17:42:54'),('cc_gen0025','cp_gen010','res_dev002','张建设','resident','周末有空去看看',NULL,NULL,NULL,'published','2026-08-10 23:40:51','2026-08-10 23:40:51'),('cc_gen0026','cp_gen011','res_tech01','技工-刘师傅','resident','已收藏，谢谢分享',NULL,NULL,NULL,'published','2026-08-03 21:37:18','2026-08-03 21:37:18'),('cc_gen0027','cp_gen012','res_002','业主-钱七','resident','我也遇到了',NULL,NULL,NULL,'published','2026-07-06 23:41:46','2026-07-06 23:41:46'),('cc_gen0028','cp_gen013','res_mch01','商家-陈老板','resident','请问怎么报名？',NULL,NULL,NULL,'published','2026-07-19 03:11:49','2026-07-19 03:11:49'),('cc_gen0029','cp_gen014','res_002','业主-钱七','resident','说得对！',NULL,NULL,NULL,'published','2026-07-30 17:24:12','2026-07-30 17:24:12'),('cc_gen0030','cp_gen015','res_act01','活动组长-郑美丽','resident','说得对！',NULL,NULL,NULL,'published','2026-07-26 10:43:27','2026-07-26 10:43:27'),('cc_gen0031','cp_gen015','res_mch01','商家-陈老板','resident','已收藏，谢谢分享',NULL,NULL,NULL,'published','2026-07-16 18:48:12','2026-07-16 18:48:12'),('cc_gen0032','cp_gen015','res_sec01','板块负责人-李娜','resident','已收藏，谢谢分享',NULL,NULL,NULL,'published','2026-07-08 12:24:23','2026-07-08 12:24:23'),('cc_gen0033','cp_gen015','res_courier01','快递员-吴十','resident','支持一下',NULL,NULL,NULL,'published','2026-07-14 21:50:36','2026-07-14 21:50:36'),('cc_gen0034','cp_gen016','res_act01','活动组长-郑美丽','resident','周末有空去看看',NULL,NULL,NULL,'published','2026-07-07 07:05:00','2026-07-07 07:05:00'),('cc_gen0035','cp_gen016','res_coo01','统筹-张伟','resident','已转发到业主群',NULL,NULL,NULL,'published','2026-08-08 05:25:22','2026-08-08 05:25:22'),('cc_gen0036','cp_gen016','res_ind01','个体负责人-王强','resident','太实用了',NULL,NULL,NULL,'published','2026-07-17 03:20:46','2026-07-17 03:20:46'),('cc_gen0037','cp_gen016','res_dev003','张子涵','resident','说得对！',NULL,NULL,NULL,'published','2026-07-24 12:41:47','2026-07-24 12:41:47'),('cc_gen0038','cp_gen016','res_ind01','个体负责人-王强','resident','点赞',NULL,NULL,NULL,'published','2026-07-24 04:58:52','2026-07-24 04:58:52'),('cc_gen0039','cp_gen017','res_coo01','统筹-张伟','resident','已转发到业主群',NULL,NULL,NULL,'published','2026-07-21 12:18:23','2026-07-21 12:18:23'),('cc_gen0040','cp_gen017','res_001','业主-赵六','resident','请问怎么报名？',NULL,NULL,NULL,'published','2026-08-03 03:38:09','2026-08-03 03:38:09'),('cc_gen0041','cp_gen017','res_admin01','平台管理员','resident','点赞',NULL,NULL,NULL,'published','2026-08-01 09:52:44','2026-08-01 09:52:44'),('cc_gen0042','cp_gen018','res_mch03','超市老板','resident','收到，感谢提醒',NULL,NULL,NULL,'published','2026-07-23 07:44:25','2026-07-23 07:44:25'),('cc_gen0043','cp_gen018','res_sec01','板块负责人-李娜','resident','周末有空去看看',NULL,NULL,NULL,'published','2026-07-08 02:03:39','2026-07-08 02:03:39'),('cc_gen0044','cp_gen018','res_propadmin01','物业管理员-刘芳','resident','我也遇到了',NULL,NULL,NULL,'published','2026-07-26 15:11:43','2026-07-26 15:11:43');
/*!40000 ALTER TABLE `community_comments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `community_entities`
--

DROP TABLE IF EXISTS `community_entities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `community_entities` (
  `id` varchar(32) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `admin_resident_id` varchar(32) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `community_entities_ibfk_1` (`admin_resident_id`),
  CONSTRAINT `community_entities_ibfk_1` FOREIGN KEY (`admin_resident_id`) REFERENCES `residents` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `community_entities`
--

LOCK TABLES `community_entities` WRITE;
/*!40000 ALTER TABLE `community_entities` DISABLE KEYS */;
/*!40000 ALTER TABLE `community_entities` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `community_point_pools`
--

DROP TABLE IF EXISTS `community_point_pools`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `community_point_pools` (
  `id` varchar(32) NOT NULL COMMENT 'cpp_xxxxxxxx',
  `community_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `balance` decimal(10,2) DEFAULT '0.00' COMMENT '当前建设积分余额',
  `total_in` decimal(10,2) DEFAULT '0.00',
  `total_out` decimal(10,2) DEFAULT '0.00',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_community` (`community_id`),
  KEY `fk_cpp_property` (`property_company_id`),
  CONSTRAINT `fk_cpp_community` FOREIGN KEY (`community_id`) REFERENCES `communities` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cpp_property` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='小区建设积分池';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `community_point_pools`
--

LOCK TABLES `community_point_pools` WRITE;
/*!40000 ALTER TABLE `community_point_pools` DISABLE KEYS */;
/*!40000 ALTER TABLE `community_point_pools` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `community_point_records`
--

DROP TABLE IF EXISTS `community_point_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `community_point_records` (
  `id` varchar(32) NOT NULL COMMENT 'cpr_xxxxxxxx',
  `community_id` varchar(32) NOT NULL,
  `order_id` varchar(32) DEFAULT NULL,
  `amount` decimal(10,2) NOT NULL COMMENT '正数=注入，负数=消耗',
  `source` varchar(30) NOT NULL COMMENT 'order_contribution/manual_adjust',
  `remark` varchar(200) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_community_id` (`community_id`),
  CONSTRAINT `fk_cpr_community` FOREIGN KEY (`community_id`) REFERENCES `communities` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='小区建设积分流水';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `community_point_records`
--

LOCK TABLES `community_point_records` WRITE;
/*!40000 ALTER TABLE `community_point_records` DISABLE KEYS */;
/*!40000 ALTER TABLE `community_point_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `community_post_likes`
--

DROP TABLE IF EXISTS `community_post_likes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `community_post_likes` (
  `id` varchar(32) NOT NULL COMMENT 'like_雪花ID 或 MD5 迁移ID',
  `post_id` varchar(32) NOT NULL,
  `resident_id` varchar(32) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_resident` (`post_id`,`resident_id`),
  KEY `idx_post` (`post_id`),
  KEY `idx_resident` (`resident_id`),
  CONSTRAINT `fk_like_post` FOREIGN KEY (`post_id`) REFERENCES `community_posts` (`id`),
  CONSTRAINT `fk_like_resident` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `community_post_likes`
--

LOCK TABLES `community_post_likes` WRITE;
/*!40000 ALTER TABLE `community_post_likes` DISABLE KEYS */;
INSERT INTO `community_post_likes` VALUES ('like__3583bb185e1000','cp__3583baf9461000','res_dev001','2026-08-23 02:41:11','2026-08-23 02:41:11'),('like__93201000','cp__830c1000','res_dev001','2026-09-06 10:01:03','2026-09-06 10:01:03'),('like_gen00001','cp_gen001','res_tech01','2026-07-16 00:34:32','2026-07-16 00:34:32'),('like_gen00002','cp_gen001','res_dev002','2026-07-30 08:01:10','2026-07-30 08:01:10'),('like_gen00003','cp_gen001','res_mch01','2026-07-20 03:51:34','2026-07-20 03:51:34'),('like_gen00004','cp_gen002','res_mch04','2026-07-10 07:45:51','2026-07-10 07:45:51'),('like_gen00005','cp_gen002','res_mch01','2026-07-26 03:47:13','2026-07-26 03:47:13'),('like_gen00006','cp_gen002','res_dev002','2026-07-11 14:12:05','2026-07-11 14:12:05'),('like_gen00007','cp_gen003','res_mch02','2026-07-07 07:44:53','2026-07-07 07:44:53'),('like_gen00008','cp_gen003','res_tech01','2026-07-08 20:10:45','2026-07-08 20:10:45'),('like_gen00009','cp_gen004','res_propadmin01','2026-07-07 11:42:08','2026-07-07 11:42:08'),('like_gen00010','cp_gen004','res_act01','2026-07-11 14:30:09','2026-07-11 14:30:09'),('like_gen00011','cp_gen004','res_dev003','2026-07-24 05:07:39','2026-07-24 05:07:39'),('like_gen00012','cp_gen004','res_dev002','2026-08-10 00:36:11','2026-08-10 00:36:11'),('like_gen00013','cp_gen004','res_ind01','2026-07-15 21:54:22','2026-07-15 21:54:22'),('like_gen00014','cp_gen004','res_003','2026-07-03 22:51:39','2026-07-03 22:51:39'),('like_gen00015','cp_gen004','res_dev001','2026-08-09 10:11:36','2026-08-09 10:11:36'),('like_gen00016','cp_gen005','res_dev003','2026-07-24 14:34:31','2026-07-24 14:34:31'),('like_gen00017','cp_gen005','res_004','2026-08-03 14:49:30','2026-08-03 14:49:30'),('like_gen00018','cp_gen005','res_admin01','2026-08-05 23:43:26','2026-08-05 23:43:26'),('like_gen00019','cp_gen005','res_dev004','2026-07-24 01:01:01','2026-07-24 01:01:01'),('like_gen00020','cp_gen005','res_courier03','2026-08-01 11:48:58','2026-08-01 11:48:58'),('like_gen00021','cp_gen006','res_coo01','2026-07-20 18:28:51','2026-07-20 18:28:51'),('like_gen00022','cp_gen006','res_mch03','2026-08-01 08:27:09','2026-08-01 08:27:09'),('like_gen00023','cp_gen006','res_courier03','2026-07-06 03:20:31','2026-07-06 03:20:31'),('like_gen00024','cp_gen007','res_dev002','2026-07-06 14:23:13','2026-07-06 14:23:13'),('like_gen00025','cp_gen007','res_dev001','2026-07-14 02:58:03','2026-07-14 02:58:03'),('like_gen00026','cp_gen007','res_mch03','2026-07-21 11:18:42','2026-07-21 11:18:42'),('like_gen00027','cp_gen007','res_propadmin01','2026-07-08 11:04:32','2026-07-08 11:04:32'),('like_gen00028','cp_gen007','res_mch01','2026-07-25 00:18:40','2026-07-25 00:18:40'),('like_gen00029','cp_gen007','res_courier01','2026-08-11 07:06:23','2026-08-11 07:06:23'),('like_gen00030','cp_gen007','res_dev004','2026-07-14 14:42:53','2026-07-14 14:42:53'),('like_gen00031','cp_gen007','res_001','2026-07-23 15:36:56','2026-07-23 15:36:56'),('like_gen00032','cp_gen007','res_sec01','2026-07-29 00:46:01','2026-07-29 00:46:01'),('like_gen00033','cp_gen007','res_004','2026-07-12 20:03:27','2026-07-12 20:03:27'),('like_gen00034','cp_gen008','res_courier01','2026-07-08 23:35:11','2026-07-08 23:35:11'),('like_gen00035','cp_gen008','res_002','2026-07-05 03:58:17','2026-07-05 03:58:17'),('like_gen00036','cp_gen008','res_courier03','2026-07-08 09:16:49','2026-07-08 09:16:49'),('like_gen00037','cp_gen008','res_dev001','2026-07-20 17:44:07','2026-07-20 17:44:07'),('like_gen00038','cp_gen008','res_tech01','2026-07-17 18:40:43','2026-07-17 18:40:43'),('like_gen00039','cp_gen008','res_admin01','2026-07-13 02:05:30','2026-07-13 02:05:30'),('like_gen00040','cp_gen009','res_dev003','2026-07-10 07:22:14','2026-07-10 07:22:14'),('like_gen00041','cp_gen009','res_courier01','2026-07-07 21:27:24','2026-07-07 21:27:24'),('like_gen00042','cp_gen009','res_003','2026-07-09 05:06:28','2026-07-09 05:06:28'),('like_gen00043','cp_gen009','res_propadmin01','2026-07-07 02:08:43','2026-07-07 02:08:43'),('like_gen00044','cp_gen009','res_mch02','2026-07-21 21:56:23','2026-07-21 21:56:23'),('like_gen00045','cp_gen009','res_dev002','2026-08-09 09:16:00','2026-08-09 09:16:00'),('like_gen00046','cp_gen009','res_mch04','2026-07-15 04:32:07','2026-07-15 04:32:07'),('like_gen00047','cp_gen010','res_mch03','2026-07-06 07:13:34','2026-07-06 07:13:34'),('like_gen00048','cp_gen010','res_mch01','2026-07-17 15:36:13','2026-07-17 15:36:13'),('like_gen00049','cp_gen010','res_mch04','2026-07-21 21:55:54','2026-07-21 21:55:54'),('like_gen00050','cp_gen010','res_dev001','2026-07-30 11:57:02','2026-07-30 11:57:02'),('like_gen00051','cp_gen010','res_coo01','2026-07-03 02:32:53','2026-07-03 02:32:53'),('like_gen00052','cp_gen010','res_admin01','2026-07-17 13:42:01','2026-07-17 13:42:01'),('like_gen00053','cp_gen010','res_mch02','2026-07-17 23:51:46','2026-07-17 23:51:46'),('like_gen00054','cp_gen010','res_propadmin01','2026-08-05 17:27:23','2026-08-05 17:27:23'),('like_gen00055','cp_gen011','res_ind01','2026-08-02 21:30:30','2026-08-02 21:30:30'),('like_gen00056','cp_gen011','res_sec01','2026-07-22 13:40:35','2026-07-22 13:40:35'),('like_gen00057','cp_gen011','res_mch04','2026-08-10 18:28:38','2026-08-10 18:28:38'),('like_gen00058','cp_gen011','res_001','2026-07-13 03:31:07','2026-07-13 03:31:07'),('like_gen00059','cp_gen011','res_004','2026-08-08 07:20:51','2026-08-08 07:20:51'),('like_gen00060','cp_gen012','res_courier02','2026-07-10 19:15:05','2026-07-10 19:15:05'),('like_gen00061','cp_gen012','res_coo01','2026-08-10 15:44:51','2026-08-10 15:44:51'),('like_gen00062','cp_gen012','res_admin01','2026-07-22 22:01:27','2026-07-22 22:01:27'),('like_gen00063','cp_gen012','res_ind01','2026-07-06 02:29:22','2026-07-06 02:29:22'),('like_gen00064','cp_gen013','res_002','2026-08-11 04:04:34','2026-08-11 04:04:34'),('like_gen00065','cp_gen013','res_003','2026-08-10 17:55:49','2026-08-10 17:55:49'),('like_gen00066','cp_gen013','res_coo01','2026-08-06 05:50:00','2026-08-06 05:50:00'),('like_gen00067','cp_gen013','res_courier01','2026-08-02 01:14:13','2026-08-02 01:14:13'),('like_gen00068','cp_gen013','res_ind01','2026-07-19 12:51:02','2026-07-19 12:51:02'),('like_gen00069','cp_gen013','res_mch02','2026-07-25 07:37:21','2026-07-25 07:37:21'),('like_gen00070','cp_gen014','res_coo01','2026-07-31 12:30:41','2026-07-31 12:30:41'),('like_gen00071','cp_gen014','res_mch04','2026-07-08 21:00:47','2026-07-08 21:00:47'),('like_gen00072','cp_gen014','res_dev001','2026-08-11 00:55:25','2026-08-11 00:55:25'),('like_gen00073','cp_gen014','res_propadmin01','2026-08-06 17:22:01','2026-08-06 17:22:01'),('like_gen00074','cp_gen014','res_act01','2026-07-17 19:07:21','2026-07-17 19:07:21'),('like_gen00075','cp_gen014','res_mch03','2026-07-20 06:21:20','2026-07-20 06:21:20'),('like_gen00076','cp_gen014','res_003','2026-08-02 10:15:43','2026-08-02 10:15:43'),('like_gen00077','cp_gen014','res_dev003','2026-08-11 13:39:50','2026-08-11 13:39:50'),('like_gen00078','cp_gen015','res_mch03','2026-07-29 06:01:54','2026-07-29 06:01:54'),('like_gen00079','cp_gen015','res_courier01','2026-07-13 09:40:22','2026-07-13 09:40:22'),('like_gen00080','cp_gen015','res_mch02','2026-08-01 10:21:31','2026-08-01 10:21:31'),('like_gen00081','cp_gen015','res_dev001','2026-07-22 12:04:13','2026-07-22 12:04:13'),('like_gen00082','cp_gen015','res_propadmin01','2026-07-03 20:35:58','2026-07-03 20:35:58'),('like_gen00083','cp_gen015','res_mch04','2026-07-18 16:21:11','2026-07-18 16:21:11'),('like_gen00084','cp_gen016','res_dev003','2026-07-28 07:31:46','2026-07-28 07:31:46'),('like_gen00085','cp_gen016','res_dev004','2026-07-24 14:16:39','2026-07-24 14:16:39'),('like_gen00086','cp_gen016','res_004','2026-07-25 13:28:20','2026-07-25 13:28:20'),('like_gen00087','cp_gen016','res_mch01','2026-07-09 01:00:38','2026-07-09 01:00:38'),('like_gen00088','cp_gen017','res_tech01','2026-07-14 03:13:06','2026-07-14 03:13:06'),('like_gen00089','cp_gen017','res_dev002','2026-07-29 03:32:38','2026-07-29 03:32:38'),('like_gen00090','cp_gen017','res_coo01','2026-08-08 03:56:07','2026-08-08 03:56:07'),('like_gen00091','cp_gen018','res_admin01','2026-07-20 05:12:47','2026-07-20 05:12:47'),('like_gen00092','cp_gen018','res_dev002','2026-07-25 15:00:08','2026-07-25 15:00:08'),('like_gen00093','cp_gen018','res_courier01','2026-07-22 13:40:42','2026-07-22 13:40:42'),('like_gen00094','cp_gen018','res_mch01','2026-07-27 07:07:35','2026-07-27 07:07:35'),('like_gen00095','cp_gen018','res_courier03','2026-07-07 04:08:32','2026-07-07 04:08:32');
/*!40000 ALTER TABLE `community_post_likes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `community_posts`
--

DROP TABLE IF EXISTS `community_posts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `community_posts` (
  `id` varchar(32) NOT NULL,
  `community_id` varchar(32) DEFAULT NULL,
  `property_company_id` varchar(32) DEFAULT NULL,
  `author_id` varchar(32) DEFAULT NULL,
  `author_name` varchar(100) DEFAULT NULL,
  `author_role` varchar(30) DEFAULT NULL,
  `content` text,
  `image_urls` json DEFAULT NULL,
  `status` varchar(20) DEFAULT 'published',
  `like_count` int DEFAULT '0',
  `comment_count` int DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `visibility_type` varchar(20) DEFAULT NULL COMMENT '可见范围 community/family',
  `visible_resident_id` varchar(32) DEFAULT NULL COMMENT 'family 可见时指定的住户ID',
  PRIMARY KEY (`id`),
  KEY `idx_community` (`community_id`,`status`,`created_at`),
  KEY `fk_cp_community` (`community_id`),
  KEY `fk_cp_company` (`property_company_id`),
  KEY `fk_cp_author` (`author_id`),
  CONSTRAINT `fk_cp_author` FOREIGN KEY (`author_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `fk_cp_community` FOREIGN KEY (`community_id`) REFERENCES `communities` (`id`),
  CONSTRAINT `fk_cp_company` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `community_posts`
--

LOCK TABLES `community_posts` WRITE;
/*!40000 ALTER TABLE `community_posts` DISABLE KEYS */;
INSERT INTO `community_posts` VALUES ('cp__3583baf9461000','com_demo001','pc_demo001','res_dev001','张阿姨','resident','周末晚上17点有公益巡演！在小区对面','[]','deleted',1,0,'2026-08-23 02:41:08','2026-08-23 08:27:26',NULL,NULL),('cp__830c1000','com_demo001','pc_demo001','res_dev001','张阿姨','resident','96测试','[]','published',1,1,'2026-09-06 10:01:02','2026-09-06 10:01:11',NULL,NULL),('cp_gen001','com_demo002','pc_demo001','res_003','业主-孙八','resident','宠物友好提醒：遛狗请牵绳，便便及时清理哦。','[]','published',3,2,'2026-07-09 21:23:06','2026-07-09 21:23:06',NULL,NULL),('cp_gen002','com_demo001','pc_demo001','res_001','业主-赵六','resident','寻物启事：今天下午在中心花园捡到一串钥匙，请失主联系。','[]','published',3,1,'2026-07-10 19:23:06','2026-07-10 19:23:06',NULL,NULL),('cp_gen003','com_demo001','pc_demo001','res_courier03','快递员-王五','resident','车位紧张，建议错峰停车，别堵住消防通道。','[]','published',2,4,'2026-07-11 06:23:06','2026-07-11 06:23:06',NULL,NULL),('cp_gen004','com_demo001','pc_demo001','res_dev004','王秀兰','resident','今天小区绿化带新种了一批月季，路过闻到香味心情都好了～','[]','published',7,2,'2026-07-12 05:23:06','2026-07-12 05:23:06',NULL,NULL),('cp_gen005','com_demo001','pc_demo001','res_sec01','板块负责人-李娜','resident','车位紧张，建议错峰停车，别堵住消防通道。','[]','published',5,4,'2026-07-13 03:23:06','2026-07-13 03:23:06',NULL,NULL),('cp_gen006','com_demo001','pc_demo001','res_admin01','平台管理员','resident','快递柜满了，取件请尽快，避免超时收费。','[]','published',3,1,'2026-07-14 01:23:06','2026-07-14 01:23:06',NULL,NULL),('cp_gen007','com_demo001','pc_demo001','res_tech01','技工-刘师傅','resident','楼道里的共享单车请挪一下，影响出行了哈。','[]','published',10,2,'2026-07-15 06:23:06','2026-07-15 06:23:06',NULL,NULL),('cp_gen008','com_demo001','pc_demo001','res_propadmin01','物业管理员-刘芳','resident','寻物启事：今天下午在中心花园捡到一串钥匙，请失主联系。','[]','published',6,3,'2026-07-16 07:23:06','2026-07-16 07:23:06',NULL,NULL),('cp_gen009','com_demo001','pc_demo001','res_mch03','超市老板','resident','邻居拿手菜征集，周末打算办个美食分享会！','[]','published',7,2,'2026-07-16 21:23:06','2026-07-16 21:23:06',NULL,NULL),('cp_gen010','com_demo001','pc_demo001','res_admin01','平台管理员','resident','感谢物业师傅半夜来修水管，服务态度特别好！','[]','published',8,4,'2026-07-17 06:23:06','2026-07-17 06:23:06',NULL,NULL),('cp_gen011','com_demo001','pc_demo001','res_ind01','个体负责人-王强','resident','二手转让：九成新婴儿推车，有需要的邻居私聊我。','[]','published',5,1,'2026-07-17 19:23:06','2026-07-17 19:23:06',NULL,NULL),('cp_gen012','com_demo001','pc_demo001','res_coo01','统筹-张伟','resident','楼道里的共享单车请挪一下，影响出行了哈。','[]','published',4,1,'2026-07-18 16:23:06','2026-07-18 16:23:06',NULL,NULL),('cp_gen013','com_demo001','pc_demo001','res_mch03','超市老板','resident','团购新鲜草莓，5斤装29.9，想拼的邻居扣1。','[]','published',6,1,'2026-07-19 18:23:06','2026-07-19 18:23:06',NULL,NULL),('cp_gen014','com_demo001','pc_demo001','res_propadmin01','物业管理员-刘芳','resident','暴雨预警，请大家注意阳台物品，关好门窗。','[]','published',8,1,'2026-07-20 23:23:06','2026-07-20 23:23:06',NULL,NULL),('cp_gen015','com_demo001','pc_demo001','res_courier02','快递员-李四','resident','周末亲子活动报名开始啦，名额有限，先到先得！','[]','published',6,4,'2026-07-22 03:23:06','2026-07-22 03:23:06',NULL,NULL),('cp_gen016','com_demo001','pc_demo001','res_courier03','快递员-王五','resident','本月垃圾分类评比咱们楼拿第一，大家继续保持！','[]','published',4,5,'2026-07-22 09:23:06','2026-07-22 09:23:06',NULL,NULL),('cp_gen017','com_demo001','pc_demo001','res_dev001','张阿姨','resident','团购新鲜草莓，5斤装29.9，想拼的邻居扣1。','[]','published',3,3,'2026-07-22 15:23:06','2026-07-22 15:23:06',NULL,NULL),('cp_gen018','com_demo001','pc_demo001','res_001','业主-赵六','resident','二手转让：九成新婴儿推车，有需要的邻居私聊我。','[]','published',5,3,'2026-07-23 21:23:06','2026-07-23 21:23:06',NULL,NULL),('cp_seed001','com_demo001','pc_demo001','res_dev001','张阿姨','resident','大家好，欢迎来到阳光花园邻里社区，有事随时在群里交流～','[]','published',0,1,'2026-08-12 09:00:00','2026-08-19 15:38:53',NULL,NULL),('cp_seed002','com_demo001','pc_demo001','res_dev001','张阿姨','resident','周末社区有便民服务日活动，免费理发和义诊，大家别错过啦！','[]','published',0,0,'2026-08-12 09:00:00','2026-08-12 09:00:00',NULL,NULL);
/*!40000 ALTER TABLE `community_posts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `community_property_bindings`
--

DROP TABLE IF EXISTS `community_property_bindings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `community_property_bindings` (
  `id` varchar(32) NOT NULL,
  `community_entity_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `permission_level` varchar(20) NOT NULL DEFAULT 'read_only',
  `bound_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_community_property` (`community_entity_id`,`property_company_id`),
  KEY `community_property_bindings_ibfk_2` (`property_company_id`),
  CONSTRAINT `community_property_bindings_ibfk_1` FOREIGN KEY (`community_entity_id`) REFERENCES `community_entities` (`id`) ON DELETE CASCADE,
  CONSTRAINT `community_property_bindings_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `community_property_bindings`
--

LOCK TABLES `community_property_bindings` WRITE;
/*!40000 ALTER TABLE `community_property_bindings` DISABLE KEYS */;
/*!40000 ALTER TABLE `community_property_bindings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `community_property_permissions`
--

DROP TABLE IF EXISTS `community_property_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `community_property_permissions` (
  `id` varchar(32) NOT NULL,
  `binding_id` varchar(32) NOT NULL,
  `allowed_modules` json DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_binding` (`binding_id`),
  CONSTRAINT `community_property_permissions_ibfk_1` FOREIGN KEY (`binding_id`) REFERENCES `community_property_bindings` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `community_property_permissions`
--

LOCK TABLES `community_property_permissions` WRITE;
/*!40000 ALTER TABLE `community_property_permissions` DISABLE KEYS */;
/*!40000 ALTER TABLE `community_property_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `community_rooms`
--

DROP TABLE IF EXISTS `community_rooms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `community_rooms` (
  `id` varchar(32) NOT NULL COMMENT '主键 room_xxx',
  `community_id` varchar(32) NOT NULL COMMENT '小区ID com_xxx',
  `building` varchar(20) NOT NULL COMMENT '楼栋',
  `unit` varchar(20) NOT NULL DEFAULT '' COMMENT '单元（无单元则为空串）',
  `floor` varchar(20) DEFAULT NULL COMMENT '楼层',
  `room` varchar(20) NOT NULL COMMENT '房号',
  `status` varchar(20) NOT NULL DEFAULT 'vacant' COMMENT '房号状态：vacant 空置 / occupied 已入住 / locked 锁定',
  `resident_id` varchar(32) DEFAULT NULL COMMENT '当前入住住户ID（冗余，便于检索）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_community_room` (`community_id`,`building`,`unit`,`room`),
  KEY `idx_community_id` (`community_id`),
  KEY `idx_resident_id` (`resident_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社区房号主表（楼宇结构主数据，空房可管理）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `community_rooms`
--

LOCK TABLES `community_rooms` WRITE;
/*!40000 ALTER TABLE `community_rooms` DISABLE KEYS */;
INSERT INTO `community_rooms` VALUES ('room_20260817180147000001','com_demo001','1','1',NULL,'101','vacant',NULL,'2026-08-17 18:01:47','2026-08-18 09:48:43'),('room_20260817180147000002','com_demo001','1','1',NULL,'302','occupied','res_35217712f21000','2026-08-17 18:01:47','2026-08-17 18:01:47'),('room_20260817180147000003','com_demo001','1','1',NULL,'501','occupied','res_demo01','2026-08-17 18:01:47','2026-08-17 18:01:47'),('room_20260817180147000004','com_demo001','1','2',NULL,'201','occupied','res_001','2026-08-17 18:01:47','2026-08-17 18:01:47'),('room_20260817180147000005','com_demo001','1','2',NULL,'205','occupied','res_3516a17fb01000','2026-08-17 18:01:47','2026-08-17 18:01:47'),('room_20260817180147000006','com_demo001','2','1',NULL,'101','occupied','res_act01','2026-08-17 18:01:47','2026-08-17 18:01:47'),('room_20260817180147000007','com_demo001','2','1',NULL,'502','occupied','res_demo02','2026-08-17 18:01:47','2026-08-17 18:01:47'),('room_20260817180147000008','com_demo001','3','1',NULL,'503','occupied','res_demo03','2026-08-17 18:01:47','2026-08-17 18:01:47'),('room_20260817180147000009','com_demo001','3','12',NULL,'1201','occupied','res_dev003','2026-08-17 18:01:47','2026-08-17 18:01:47'),('room_20260817180147000010','com_demo001','3','2',NULL,'202','occupied','res_002','2026-08-17 18:01:47','2026-08-17 18:01:47'),('room_20260817180147000011','com_demo002','5','1',NULL,'101','occupied','res_003','2026-08-17 18:01:47','2026-08-17 18:01:47'),('room_354b40fbea1000','com_demo001','1','1','1','108','occupied','res_dev001','2026-08-17 23:05:08','2026-08-23 10:34:29'),('room_354fdaa3ae1000','com_demo002','1','1','1','101','vacant',NULL,'2026-08-18 09:48:18','2026-08-18 15:56:59'),('room_35890656281000','com_demo001','6','1',NULL,'601','occupied','res_35890655f01000','2026-08-23 23:01:20','2026-08-23 23:01:20');
/*!40000 ALTER TABLE `community_rooms` ENABLE KEYS */;
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
INSERT INTO `community_services` VALUES ('svc_001','家电维修','专业家电维修服务，涵盖空调、冰箱、洗衣机等',NULL,'cleaning_repair',50.00,40.00,'次','res_tech01','technician','pc_demo001',1,0,'active','2026-07-05 10:18:21','2026-07-05 10:18:21'),('svc_002','家庭保洁','专业保洁团队上门服务，2小时起',NULL,'cleaning_repair',80.00,65.00,'次','res_tech01','technician','pc_demo001',2,0,'active','2026-07-05 10:18:21','2026-07-05 10:18:21'),('svc_003','管道疏通','厨卫管道疏通维修',NULL,'cleaning_repair',60.00,50.00,'次','res_tech01','technician','pc_demo001',3,0,'active','2026-07-05 10:18:21','2026-07-05 10:18:21'),('svc_004','社区食堂午餐','工作日营养午餐配送',NULL,'canteen',15.00,12.00,'份','res_ind01','individual_leader','pc_demo001',4,1,'active','2026-07-05 10:18:21','2026-07-05 10:18:21'),('svc_005','少儿编程辅导','8-15岁少儿编程入门课程',NULL,'tutoring',200.00,180.00,'月','res_ind01','individual_leader','pc_demo001',5,1,'active','2026-07-05 10:18:21','2026-07-05 10:18:21'),('svc_006','老人日间照料','提供日间看护、陪伴、基础护理',NULL,'elderly_care',100.00,85.00,'天','res_ind01','individual_leader','pc_demo001',6,0,'active','2026-07-05 10:18:21','2026-07-05 10:18:21'),('svc_007','社区义诊','每月一次免费基础体检',NULL,'medical',0.00,0.00,'次','res_propadmin01','property_admin','pc_demo001',7,0,'active','2026-07-05 10:18:21','2026-07-05 10:18:21'),('svc_008','宠物寄养','节假日宠物临时寄养服务',NULL,'pet_medical',30.00,25.00,'天','res_ind01','individual_leader','pc_demo001',8,0,'active','2026-07-05 10:18:21','2026-07-05 10:18:21'),('svc_009','代收快递','上门代收快递服务',NULL,'life_service',5.00,3.00,'件','res_courier01','courier','pc_demo001',9,0,'active','2026-07-05 10:18:21','2026-07-05 10:18:21'),('svc_010','相亲交友','社区单身交友活动',NULL,'dating',50.00,40.00,'次','res_coo01','coordinator','pc_demo001',10,0,'active','2026-07-05 10:18:21','2026-07-05 10:18:21'),('svc_353d0d595c1000','修空调',NULL,NULL,'cleaning_repair',20.00,NULL,'次','res_tech01','technician','pc_demo001',0,0,'active','2026-08-16 05:59:36','2026-09-15 09:42:14'),('svc_355d12d27c1000','测试',NULL,NULL,'cleaning_repair',1.00,NULL,'次','res_35217712f21000','technician','pc_demo001',0,0,'inactive','2026-08-19 08:36:31','2026-08-19 08:36:51'),('svc_35840e289e1000','更换窗户',NULL,NULL,'cleaning_repair',20.00,NULL,'次','res_tech01','technician','pc_demo001',0,0,'inactive','2026-08-23 03:26:34','2026-09-15 09:41:10'),('svc_369ab145321000','换修灯泡',NULL,NULL,'cleaning_repair',10.00,NULL,'次','res_tech01','technician','pc_demo001',0,0,'inactive','2026-09-19 04:42:51','2026-09-19 04:42:55');
/*!40000 ALTER TABLE `community_services` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `company_account_records`
--

DROP TABLE IF EXISTS `company_account_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `company_account_records` (
  `id` varchar(32) NOT NULL COMMENT 'car_xxxxxxxx',
  `property_company_id` varchar(32) NOT NULL,
  `order_id` varchar(32) DEFAULT NULL,
  `amount` decimal(10,2) NOT NULL,
  `source` varchar(30) NOT NULL COMMENT 'leader_share/withdrawal_deposit/manual',
  `remark` varchar(200) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_property_company_id` (`property_company_id`),
  KEY `idx_order_source` (`order_id`,`source`),
  CONSTRAINT `fk_car_property` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公司账户资金流水';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `company_account_records`
--

LOCK TABLES `company_account_records` WRITE;
/*!40000 ALTER TABLE `company_account_records` DISABLE KEYS */;
/*!40000 ALTER TABLE `company_account_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `consultants`
--

DROP TABLE IF EXISTS `consultants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `consultants` (
  `id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `category` varchar(20) NOT NULL,
  `name` varchar(50) NOT NULL,
  `title` varchar(100) DEFAULT NULL,
  `organization` varchar(100) DEFAULT NULL,
  `specialty` varchar(100) DEFAULT NULL,
  `avatar_url` varchar(500) DEFAULT NULL,
  `chat_price` decimal(10,2) DEFAULT '0.00',
  `appointment_price` decimal(10,2) DEFAULT NULL COMMENT '面诊价格',
  `appointment_enabled` tinyint(1) DEFAULT '1',
  `introduction` text COMMENT '简介',
  `audit_status` varchar(20) DEFAULT 'pending_audit',
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `consultants_ibfk_1` (`property_company_id`),
  CONSTRAINT `consultants_ibfk_1` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consultants`
--

LOCK TABLES `consultants` WRITE;
/*!40000 ALTER TABLE `consultants` DISABLE KEYS */;
/*!40000 ALTER TABLE `consultants` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `consultation_appointments`
--

DROP TABLE IF EXISTS `consultation_appointments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `consultation_appointments` (
  `id` varchar(32) NOT NULL,
  `consultant_id` varchar(32) NOT NULL,
  `resident_id` varchar(32) NOT NULL,
  `appointment_time` timestamp NOT NULL,
  `patient_name` varchar(50) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'pending',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `consultation_appointments_ibfk_1` (`consultant_id`),
  KEY `consultation_appointments_ibfk_2` (`resident_id`),
  CONSTRAINT `consultation_appointments_ibfk_1` FOREIGN KEY (`consultant_id`) REFERENCES `consultants` (`id`),
  CONSTRAINT `consultation_appointments_ibfk_2` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consultation_appointments`
--

LOCK TABLES `consultation_appointments` WRITE;
/*!40000 ALTER TABLE `consultation_appointments` DISABLE KEYS */;
/*!40000 ALTER TABLE `consultation_appointments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `consultation_orders`
--

DROP TABLE IF EXISTS `consultation_orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `consultation_orders` (
  `id` varchar(32) NOT NULL,
  `consultant_id` varchar(32) NOT NULL,
  `resident_id` varchar(32) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `payment_method` varchar(20) NOT NULL,
  `status` varchar(20) DEFAULT 'paid',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `consultation_orders_ibfk_1` (`consultant_id`),
  KEY `consultation_orders_ibfk_2` (`resident_id`),
  CONSTRAINT `consultation_orders_ibfk_1` FOREIGN KEY (`consultant_id`) REFERENCES `consultants` (`id`),
  CONSTRAINT `consultation_orders_ibfk_2` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consultation_orders`
--

LOCK TABLES `consultation_orders` WRITE;
/*!40000 ALTER TABLE `consultation_orders` DISABLE KEYS */;
/*!40000 ALTER TABLE `consultation_orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `content_reports`
--

DROP TABLE IF EXISTS `content_reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `content_reports` (
  `id` varchar(32) NOT NULL,
  `reporter_id` varchar(32) DEFAULT NULL,
  `reporter_name` varchar(100) DEFAULT NULL,
  `target_type` varchar(20) DEFAULT NULL COMMENT 'post / comment',
  `target_id` varchar(32) DEFAULT NULL,
  `reason_type` varchar(30) DEFAULT NULL,
  `reason_detail` varchar(500) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'pending' COMMENT 'pending / accepted / rejected',
  `community_id` varchar(32) DEFAULT NULL,
  `property_company_id` varchar(32) DEFAULT NULL,
  `handler_id` varchar(32) DEFAULT NULL,
  `handler_name` varchar(100) DEFAULT NULL,
  `handle_remark` varchar(500) DEFAULT NULL,
  `handled_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reporter_target` (`reporter_id`,`target_type`,`target_id`),
  KEY `idx_status` (`status`,`created_at`),
  KEY `idx_cr_company` (`property_company_id`),
  KEY `fk_cr_reporter` (`reporter_id`),
  CONSTRAINT `fk_cr_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `residents` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `content_reports`
--

LOCK TABLES `content_reports` WRITE;
/*!40000 ALTER TABLE `content_reports` DISABLE KEYS */;
/*!40000 ALTER TABLE `content_reports` ENABLE KEYS */;
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
  `description` varchar(200) DEFAULT NULL COMMENT '统筹描述/备注',
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
INSERT INTO `coordinators` VALUES ('coo_001','res_coo01','pc_demo001',0.7000,NULL,'active','2026-07-03 08:09:15','2026-07-03 08:09:15'),('coo_584c1000','res_3516a17fb01000','pc_demo001',0.3000,NULL,'inactive','2026-09-07 00:30:23','2026-09-07 12:57:20');
/*!40000 ALTER TABLE `coordinators` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `courier_managers`
--

DROP TABLE IF EXISTS `courier_managers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `courier_managers` (
  `id` varchar(32) NOT NULL COMMENT 'cm_xxxxxxxx',
  `resident_id` varchar(32) NOT NULL COMMENT '关联residents.id',
  `property_company_id` varchar(32) NOT NULL COMMENT '所属物业公司',
  `community_id` varchar(32) DEFAULT NULL COMMENT '负责小区',
  `name` varchar(50) DEFAULT NULL COMMENT '负责人姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `responsible_area` varchar(500) DEFAULT NULL COMMENT '负责区域描述',
  `delivery_time_config` json DEFAULT NULL COMMENT '配送时间配置，如{"start":"08:00","end":"20:00"}',
  `status` varchar(20) DEFAULT 'active' COMMENT 'active/inactive',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `share_rate` decimal(10,4) DEFAULT NULL COMMENT '快递负责人分成比例(0~1)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resident_property` (`resident_id`,`property_company_id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_community` (`community_id`),
  CONSTRAINT `courier_managers_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `courier_managers_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='快递负责人表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `courier_managers`
--

LOCK TABLES `courier_managers` WRITE;
/*!40000 ALTER TABLE `courier_managers` DISABLE KEYS */;
INSERT INTO `courier_managers` VALUES ('cm_demo001','res_courier01','pc_demo001','com_demo001','快递负责人-吴十','13900000016','1栋-20栋','{\"end\": \"20:00\", \"start\": \"08:00\"}','active','2026-07-22 05:29:07','2026-08-02 04:27:10',NULL),('cm_demo002','res_courier02','pc_demo001','com_demo001','快递负责人-李四','13900000013','6栋-10栋','{\"end\": \"18:00\", \"start\": \"09:00\"}','active','2026-07-22 05:29:07','2026-07-22 05:29:07',NULL);
/*!40000 ALTER TABLE `courier_managers` ENABLE KEYS */;
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
  `platform_earning` decimal(10,2) DEFAULT '0.00' COMMENT '平台管理员从配送费中的分成',
  `original_delivery_fee` decimal(10,2) DEFAULT NULL COMMENT '满额减免前基础配送费',
  `delivery_subsidy_amount` decimal(10,2) DEFAULT '0.00' COMMENT '配送补贴金额',
  `delivery_subsidy_sponsor` varchar(20) DEFAULT NULL COMMENT '减免承担方 none/merchant/property/platform',
  `delivery_settlement_base` decimal(10,2) DEFAULT NULL COMMENT '配送结算基数 = 配送费 + 补贴',
  `platform_delivery_share` decimal(10,2) DEFAULT '0.00' COMMENT '公司配送收入（规范化命名，替代 platform_earning）',
  `settlement_status` varchar(20) DEFAULT 'pending' COMMENT '配送结算状态 pending/settled/reversed',
  `settled_at` timestamp NULL DEFAULT NULL COMMENT '配送结算时间',
  `pickup_address` varchar(200) DEFAULT NULL,
  `delivery_address` varchar(200) DEFAULT NULL,
  `contact_phone` varchar(20) DEFAULT NULL,
  `accepted_at` timestamp NULL DEFAULT NULL,
  `picked_at` timestamp NULL DEFAULT NULL,
  `delivered_at` timestamp NULL DEFAULT NULL,
  `proof_image_urls` text COMMENT '配送完成凭证图片URL列表(JSON)',
  `complete_remark` varchar(200) DEFAULT NULL COMMENT '完成配送备注',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `carrier_type` varchar(20) DEFAULT NULL COMMENT '承运类型 courier(平台配送员)/merchant(商家自配)',
  `merchant_delivery_fee_share` decimal(10,2) DEFAULT NULL COMMENT '商家自配时商家从配送费中的分成（平台抽成后余下部分）',
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
INSERT INTO `deliveries` VALUES ('dlv_000001','ord_000001','res_courier03','pc_demo001','completed',0.00,0.80,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo00110栋3单元602室','com_demo00110栋3单元602室','1917935769','2026-08-01 21:44:43','2026-08-01 21:44:43','2026-08-01 21:44:43',NULL,'已送达','2026-08-01 21:44:43','2026-08-18 04:13:22','courier',NULL),('dlv_000002','ord_000002','res_courier03','pc_demo001','completed',0.00,0.80,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0019栋1单元1204室','com_demo0019栋1单元1204室','1366538546','2026-07-14 03:56:57','2026-07-14 03:56:57','2026-07-14 03:56:57',NULL,'已送达','2026-07-14 03:56:57','2026-08-18 04:13:22','courier',NULL),('dlv_000003','ord_000003','res_courier02','pc_demo001','delivering',0.00,1.11,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0013栋4单元1405室','com_demo0013栋4单元1405室','1991162797',NULL,NULL,NULL,NULL,NULL,'2026-07-09 11:51:22','2026-08-18 04:13:22','courier',NULL),('dlv_000004','ord_000004','res_courier02','pc_demo001','completed',0.00,1.05,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo00212栋1单元2603室','com_demo00212栋1单元2603室','1381139936','2026-07-10 19:33:51','2026-07-10 19:33:51','2026-07-10 19:33:51',NULL,'已送达','2026-07-10 19:33:51','2026-08-18 04:13:22','courier',NULL),('dlv_000005','ord_000005','res_courier01','pc_demo001','completed',0.00,0.61,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0015栋2单元101室','com_demo0015栋2单元101室','1971486971','2026-08-09 10:26:45','2026-08-09 10:26:45','2026-08-09 10:26:45',NULL,'已送达','2026-08-09 10:26:45','2026-08-18 04:13:22','courier',NULL),('dlv_000006','ord_000006','res_courier03','pc_demo001','completed',0.00,0.43,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo00110栋3单元1602室','com_demo00110栋3单元1602室','1941050028','2026-07-25 21:59:36','2026-07-25 21:59:36','2026-07-25 21:59:36',NULL,'已送达','2026-07-25 21:59:36','2026-08-18 04:13:22','courier',NULL),('dlv_000007','ord_000010','res_courier02','pc_demo001','delivering',0.00,0.92,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0018栋4单元503室','com_demo0018栋4单元503室','1721582786',NULL,NULL,NULL,NULL,NULL,'2026-07-10 12:49:26','2026-08-18 04:13:22','courier',NULL),('dlv_000008','ord_000014','res_courier01','pc_demo001','delivering',0.00,0.40,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0015栋1单元605室','com_demo0015栋1单元605室','1553939441',NULL,NULL,NULL,NULL,NULL,'2026-07-29 02:48:15','2026-08-18 04:13:22','courier',NULL),('dlv_000009','ord_000015','res_courier01','pc_demo001','completed',0.00,1.11,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0018栋4单元606室','com_demo0018栋4单元606室','1360184076','2026-07-07 18:15:20','2026-07-07 18:15:20','2026-07-07 18:15:20',NULL,'已送达','2026-07-07 18:15:20','2026-08-18 04:13:22','courier',NULL),('dlv_000010','ord_000016','res_courier01','pc_demo001','delivering',0.00,0.51,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0013栋1单元603室','com_demo0013栋1单元603室','1305025253',NULL,NULL,NULL,NULL,NULL,'2026-07-24 13:13:31','2026-08-18 04:13:22','courier',NULL),('dlv_000011','ord_000017','res_courier01','pc_demo001','completed',0.00,0.69,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo00110栋1单元2401室','com_demo00110栋1单元2401室','1865619068','2026-07-22 19:55:58','2026-07-22 19:55:58','2026-07-22 19:55:58',NULL,'已送达','2026-07-22 19:55:58','2026-08-18 04:13:22','courier',NULL),('dlv_000012','ord_000018','res_courier01','pc_demo001','completed',0.00,0.85,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0014栋3单元1206室','com_demo0014栋3单元1206室','1864159723','2026-07-13 14:24:04','2026-07-13 14:24:04','2026-07-13 14:24:04',NULL,'已送达','2026-07-13 14:24:04','2026-08-18 04:13:22','courier',NULL),('dlv_000013','ord_000021','res_courier02','pc_demo001','completed',0.00,0.63,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0018栋4单元1402室','com_demo0018栋4单元1402室','1985158727','2026-07-11 08:35:40','2026-07-11 08:35:40','2026-07-11 08:35:40',NULL,'已送达','2026-07-11 08:35:40','2026-08-18 04:13:22','courier',NULL),('dlv_000014','ord_000023','res_courier02','pc_demo001','completed',0.00,0.44,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0017栋1单元2705室','com_demo0017栋1单元2705室','1924487661','2026-07-19 22:34:26','2026-07-19 22:34:26','2026-07-19 22:34:26',NULL,'已送达','2026-07-19 22:34:26','2026-08-18 04:13:22','courier',NULL),('dlv_000015','ord_000024','res_courier02','pc_demo001','delivering',0.00,0.92,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0017栋2单元1706室','com_demo0017栋2单元1706室','1504743981',NULL,NULL,NULL,NULL,NULL,'2026-07-21 17:23:40','2026-08-18 04:13:22','courier',NULL),('dlv_000016','ord_000025','res_courier02','pc_demo001','completed',0.00,0.79,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0019栋4单元901室','com_demo0019栋4单元901室','1575077682','2026-07-16 06:22:17','2026-07-16 06:22:17','2026-07-16 06:22:17',NULL,'已送达','2026-07-16 06:22:17','2026-08-18 04:13:22','courier',NULL),('dlv_000017','ord_000026','res_courier01','pc_demo001','completed',0.00,1.12,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0013栋3单元702室','com_demo0013栋3单元702室','1584909180','2026-07-15 17:22:21','2026-07-15 17:22:21','2026-07-15 17:22:21',NULL,'已送达','2026-07-15 17:22:21','2026-08-18 04:13:22','courier',NULL),('dlv_000018','ord_000027','res_courier03','pc_demo001','completed',0.00,0.90,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0011栋3单元2904室','com_demo0011栋3单元2904室','1554818238','2026-08-09 22:58:22','2026-08-09 22:58:22','2026-08-09 22:58:22',NULL,'已送达','2026-08-09 22:58:22','2026-08-18 04:13:22','courier',NULL),('dlv_000019','ord_000029','res_courier02','pc_demo001','completed',0.00,0.82,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0013栋3单元401室','com_demo0013栋3单元401室','1327081750','2026-07-16 14:26:48','2026-07-16 14:26:48','2026-07-16 14:26:48',NULL,'已送达','2026-07-16 14:26:48','2026-08-18 04:13:22','courier',NULL),('dlv_000020','ord_000030','res_courier03','pc_demo001','completed',0.00,0.95,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0017栋4单元1801室','com_demo0017栋4单元1801室','1558979282','2026-07-24 22:35:12','2026-07-24 22:35:12','2026-07-24 22:35:12',NULL,'已送达','2026-07-24 22:35:12','2026-08-18 04:13:22','courier',NULL),('dlv_000021','ord_000031','res_courier02','pc_demo001','completed',0.00,0.58,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0018栋4单元2201室','com_demo0018栋4单元2201室','1816391960','2026-07-28 09:33:59','2026-07-28 09:33:59','2026-07-28 09:33:59',NULL,'已送达','2026-07-28 09:33:59','2026-08-18 04:13:22','courier',NULL),('dlv_000022','ord_000032','res_courier02','pc_demo001','completed',0.00,0.75,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0019栋4单元1904室','com_demo0019栋4单元1904室','1608168457','2026-08-03 04:37:32','2026-08-03 04:37:32','2026-08-03 04:37:32',NULL,'已送达','2026-08-03 04:37:32','2026-08-18 04:13:22','courier',NULL),('dlv_000023','ord_000033','res_courier01','pc_demo001','completed',0.00,0.65,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo00111栋2单元2906室','com_demo00111栋2单元2906室','1949003094','2026-07-04 23:31:58','2026-07-04 23:31:58','2026-07-04 23:31:58',NULL,'已送达','2026-07-04 23:31:58','2026-08-18 04:13:22','courier',NULL),('dlv_000024','ord_000035','res_courier02','pc_demo001','completed',0.00,0.67,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0011栋4单元203室','com_demo0011栋4单元203室','1353265187','2026-07-25 20:06:35','2026-07-25 20:06:35','2026-07-25 20:06:35',NULL,'已送达','2026-07-25 20:06:35','2026-08-18 04:13:22','courier',NULL),('dlv_000025','ord_000036','res_courier02','pc_demo001','completed',0.00,1.03,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0018栋4单元2304室','com_demo0018栋4单元2304室','1929445443','2026-08-01 10:34:11','2026-08-01 10:34:11','2026-08-01 10:34:11',NULL,'已送达','2026-08-01 10:34:11','2026-08-18 04:13:22','courier',NULL),('dlv_000026','ord_000038','res_courier01','pc_demo001','completed',0.00,0.51,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0019栋1单元1306室','com_demo0019栋1单元1306室','1696570629','2026-07-03 13:26:10','2026-07-03 13:26:10','2026-07-03 13:26:10',NULL,'已送达','2026-07-03 13:26:10','2026-08-18 04:13:22','courier',NULL),('dlv_000027','ord_000039','res_courier01','pc_demo001','completed',0.00,1.07,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0019栋2单元1202室','com_demo0019栋2单元1202室','1325604769','2026-07-28 17:53:52','2026-07-28 17:53:52','2026-07-28 17:53:52',NULL,'已送达','2026-07-28 17:53:52','2026-08-18 04:13:22','courier',NULL),('dlv_000028','ord_000040','res_courier03','pc_demo001','delivering',0.00,0.91,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0012栋4单元2406室','com_demo0012栋4单元2406室','1866718798',NULL,NULL,NULL,NULL,NULL,'2026-08-05 14:23:44','2026-08-18 04:13:22','courier',NULL),('dlv_000029','ord_000041','res_courier03','pc_demo001','completed',0.00,1.11,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0026栋3单元601室','com_demo0026栋3单元601室','1508209091','2026-08-07 02:04:53','2026-08-07 02:04:53','2026-08-07 02:04:53',NULL,'已送达','2026-08-07 02:04:53','2026-08-18 04:13:22','courier',NULL),('dlv_000030','ord_000043','res_courier01','pc_demo001','completed',0.00,0.38,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0014栋3单元2101室','com_demo0014栋3单元2101室','1848527337','2026-07-28 14:12:45','2026-07-28 14:12:45','2026-07-28 14:12:45',NULL,'已送达','2026-07-28 14:12:45','2026-08-18 04:13:22','courier',NULL),('dlv_000031','ord_000045','res_courier03','pc_demo001','delivering',0.00,0.49,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo00111栋1单元1301室','com_demo00111栋1单元1301室','1952523661',NULL,NULL,NULL,NULL,NULL,'2026-08-06 03:58:02','2026-08-18 04:13:22','courier',NULL),('dlv_000032','ord_000046','res_courier03','pc_demo001','delivering',0.00,0.71,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0011栋4单元2403室','com_demo0011栋4单元2403室','1925745066',NULL,NULL,NULL,NULL,NULL,'2026-07-26 17:15:53','2026-08-18 04:13:22','courier',NULL),('dlv_000033','ord_000048','res_courier02','pc_demo001','completed',0.00,0.59,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0013栋1单元1702室','com_demo0013栋1单元1702室','1968989060','2026-07-30 06:20:06','2026-07-30 06:20:06','2026-07-30 06:20:06',NULL,'已送达','2026-07-30 06:20:06','2026-08-18 04:13:22','courier',NULL),('dlv_000034','ord_000049','res_courier02','pc_demo001','completed',0.00,0.83,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0012栋3单元1502室','com_demo0012栋3单元1502室','1812175584','2026-07-17 16:49:19','2026-07-17 16:49:19','2026-07-17 16:49:19',NULL,'已送达','2026-07-17 16:49:19','2026-08-18 04:13:22','courier',NULL),('dlv_000035','ord_000050','res_courier03','pc_demo001','completed',0.00,1.16,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0013栋1单元1701室','com_demo0013栋1单元1701室','1338879749','2026-08-04 21:57:29','2026-08-04 21:57:29','2026-08-04 21:57:29',NULL,'已送达','2026-08-04 21:57:29','2026-08-18 04:13:22','courier',NULL),('dlv_000036','ord_000051','res_courier02','pc_demo001','completed',0.00,0.46,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo00112栋1单元1604室','com_demo00112栋1单元1604室','1683372544','2026-07-04 23:57:41','2026-07-04 23:57:41','2026-07-04 23:57:41',NULL,'已送达','2026-07-04 23:57:41','2026-08-18 04:13:22','courier',NULL),('dlv_000037','ord_000052','res_courier01','pc_demo001','completed',0.00,1.16,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0014栋4单元2403室','com_demo0014栋4单元2403室','1616949345','2026-07-07 12:35:38','2026-07-07 12:35:38','2026-07-07 12:35:38',NULL,'已送达','2026-07-07 12:35:38','2026-08-18 04:13:22','courier',NULL),('dlv_000038','ord_000055','res_courier02','pc_demo001','completed',0.00,0.57,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0012栋3单元701室','com_demo0012栋3单元701室','1821082092','2026-08-10 11:05:29','2026-08-10 11:05:29','2026-08-10 11:05:29',NULL,'已送达','2026-08-10 11:05:29','2026-08-18 04:13:22','courier',NULL),('dlv_000039','ord_000056','res_courier02','pc_demo001','completed',0.00,1.06,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0014栋2单元3005室','com_demo0014栋2单元3005室','1768563404','2026-07-03 02:01:20','2026-07-03 02:01:20','2026-07-03 02:01:20',NULL,'已送达','2026-07-03 02:01:20','2026-08-18 04:13:22','courier',NULL),('dlv_000040','ord_000057','res_courier01','pc_demo001','completed',0.00,0.64,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0014栋3单元203室','com_demo0014栋3单元203室','1866039834','2026-07-03 00:31:23','2026-07-03 00:31:23','2026-07-03 00:31:23',NULL,'已送达','2026-07-03 00:31:23','2026-08-18 04:13:22','courier',NULL),('dlv_000041','ord_000060','res_courier02','pc_demo001','completed',0.00,0.50,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo00112栋4单元1706室','com_demo00112栋4单元1706室','1815632036','2026-07-26 14:39:28','2026-07-26 14:39:28','2026-07-26 14:39:28',NULL,'已送达','2026-07-26 14:39:28','2026-08-18 04:13:22','courier',NULL),('dlv_000042','ord_000062','res_courier02','pc_demo001','delivering',0.00,0.95,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0018栋1单元3006室','com_demo0018栋1单元3006室','1848719516',NULL,NULL,NULL,NULL,NULL,'2026-07-06 01:31:52','2026-08-18 04:13:22','courier',NULL),('dlv_000043','ord_000064','res_courier02','pc_demo001','completed',0.00,1.05,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0019栋2单元903室','com_demo0019栋2单元903室','1618824237','2026-07-22 05:55:11','2026-07-22 05:55:11','2026-07-22 05:55:11',NULL,'已送达','2026-07-22 05:55:11','2026-08-18 04:13:22','courier',NULL),('dlv_000044','ord_000069','res_courier01','pc_demo001','completed',0.00,0.59,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0012栋3单元905室','com_demo0012栋3单元905室','1834785523','2026-07-17 08:44:23','2026-07-17 08:44:23','2026-07-17 08:44:23',NULL,'已送达','2026-07-17 08:44:23','2026-08-18 04:13:22','courier',NULL),('dlv_000045','ord_000070','res_courier01','pc_demo001','completed',0.00,0.38,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo00112栋2单元1004室','com_demo00112栋2单元1004室','1986698155','2026-07-04 00:35:11','2026-07-04 00:35:11','2026-07-04 00:35:11',NULL,'已送达','2026-07-04 00:35:11','2026-08-18 04:13:22','courier',NULL),('dlv_000046','ord_000071','res_courier01','pc_demo001','completed',0.00,0.99,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0011栋3单元2505室','com_demo0011栋3单元2505室','1528859294','2026-07-18 05:36:42','2026-07-18 05:36:42','2026-07-18 05:36:42',NULL,'已送达','2026-07-18 05:36:42','2026-08-18 04:13:22','courier',NULL),('dlv_000047','ord_000072','res_courier02','pc_demo001','completed',0.00,1.19,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo00110栋1单元3003室','com_demo00110栋1单元3003室','1832479839','2026-07-14 13:17:53','2026-07-14 13:17:53','2026-07-14 13:17:53',NULL,'已送达','2026-07-14 13:17:53','2026-08-18 04:13:22','courier',NULL),('dlv_000048','ord_000075','res_courier02','pc_demo001','completed',0.00,1.04,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo00112栋2单元2501室','com_demo00112栋2单元2501室','1978874764','2026-07-13 18:26:57','2026-07-13 18:26:57','2026-07-13 18:26:57',NULL,'已送达','2026-07-13 18:26:57','2026-08-18 04:13:22','courier',NULL),('dlv_000049','ord_000076','res_courier02','pc_demo001','completed',0.00,0.89,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0018栋3单元506室','com_demo0018栋3单元506室','1675988023','2026-08-08 16:19:22','2026-08-08 16:19:22','2026-08-08 16:19:22',NULL,'已送达','2026-08-08 16:19:22','2026-08-18 04:13:22','courier',NULL),('dlv_000050','ord_000077','res_courier02','pc_demo001','delivering',0.00,0.49,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo00112栋3单元202室','com_demo00112栋3单元202室','1546953346',NULL,NULL,NULL,NULL,NULL,'2026-07-12 12:26:09','2026-08-18 04:13:22','courier',NULL),('dlv_000051','ord_000082','res_courier03','pc_demo001','completed',0.00,0.52,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0012栋4单元1306室','com_demo0012栋4单元1306室','1898281320','2026-08-08 19:22:09','2026-08-08 19:22:09','2026-08-08 19:22:09',NULL,'已送达','2026-08-08 19:22:09','2026-08-18 04:13:22','courier',NULL),('dlv_000052','ord_000083','res_courier01','pc_demo001','completed',0.00,0.48,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo00112栋2单元1603室','com_demo00112栋2单元1603室','1925902612','2026-07-25 05:49:06','2026-07-25 05:49:06','2026-07-25 05:49:06',NULL,'已送达','2026-07-25 05:49:06','2026-08-18 04:13:22','courier',NULL),('dlv_000053','ord_000085','res_courier01','pc_demo001','completed',0.00,0.82,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0022栋4单元1304室','com_demo0022栋4单元1304室','1300411279','2026-07-10 16:02:48','2026-07-10 16:02:48','2026-07-10 16:02:48',NULL,'已送达','2026-07-10 16:02:48','2026-08-18 04:13:22','courier',NULL),('dlv_000054','ord_000087','res_courier01','pc_demo001','completed',0.00,0.64,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0013栋4单元2402室','com_demo0013栋4单元2402室','1698249937','2026-07-03 19:30:01','2026-07-03 19:30:01','2026-07-03 19:30:01',NULL,'已送达','2026-07-03 19:30:01','2026-08-18 04:13:22','courier',NULL),('dlv_000055','ord_000088','res_courier01','pc_demo001','delivering',0.00,0.77,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0011栋3单元903室','com_demo0011栋3单元903室','1610806375',NULL,NULL,NULL,NULL,NULL,'2026-08-06 15:39:12','2026-08-18 04:13:22','courier',NULL),('dlv_000056','ord_000089','res_courier03','pc_demo001','completed',0.00,0.50,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo00111栋4单元2402室','com_demo00111栋4单元2402室','1518664327','2026-07-05 23:08:53','2026-07-05 23:08:53','2026-07-05 23:08:53',NULL,'已送达','2026-07-05 23:08:53','2026-08-18 04:13:22','courier',NULL),('dlv_000057','ord_000091','res_courier02','pc_demo001','completed',0.00,1.04,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0018栋2单元2806室','com_demo0018栋2单元2806室','1602236114','2026-08-05 08:09:22','2026-08-05 08:09:22','2026-08-05 08:09:22',NULL,'已送达','2026-08-05 08:09:22','2026-08-18 04:13:22','courier',NULL),('dlv_000058','ord_000092','res_courier01','pc_demo001','completed',0.00,1.00,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0015栋4单元2903室','com_demo0015栋4单元2903室','1382793154','2026-07-14 08:33:26','2026-07-14 08:33:26','2026-07-14 08:33:26',NULL,'已送达','2026-07-14 08:33:26','2026-08-18 04:13:22','courier',NULL),('dlv_000059','ord_000093','res_courier01','pc_demo001','completed',0.00,1.09,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0015栋3单元502室','com_demo0015栋3单元502室','1579592619','2026-08-01 20:40:32','2026-08-01 20:40:32','2026-08-01 20:40:32',NULL,'已送达','2026-08-01 20:40:32','2026-08-18 04:13:22','courier',NULL),('dlv_000060','ord_000096','res_courier02','pc_demo001','completed',0.00,0.39,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0011栋1单元903室','com_demo0011栋1单元903室','1949471905','2026-08-04 10:17:05','2026-08-04 10:17:05','2026-08-04 10:17:05',NULL,'已送达','2026-08-04 10:17:05','2026-08-18 04:13:22','courier',NULL),('dlv_000061','ord_000098','res_courier02','pc_demo001','completed',0.00,0.54,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo00111栋3单元803室','com_demo00111栋3单元803室','1961021827','2026-07-08 15:51:17','2026-07-08 15:51:17','2026-07-08 15:51:17',NULL,'已送达','2026-07-08 15:51:17','2026-08-18 04:13:22','courier',NULL),('dlv_000062','ord_000099','res_courier01','pc_demo001','completed',0.00,0.66,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0017栋4单元504室','com_demo0017栋4单元504室','1747428298','2026-07-28 13:24:16','2026-07-28 13:24:16','2026-07-28 13:24:16',NULL,'已送达','2026-07-28 13:24:16','2026-08-18 04:13:22','courier',NULL),('dlv_000063','ord_000100','res_courier03','pc_demo001','completed',0.00,0.64,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0018栋4单元2801室','com_demo0018栋4单元2801室','1874439208','2026-07-03 02:08:20','2026-07-03 02:08:20','2026-07-03 02:08:20',NULL,'已送达','2026-07-03 02:08:20','2026-08-18 04:13:22','courier',NULL),('dlv_000064','ord_000101','res_courier02','pc_demo001','completed',0.00,0.38,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0013栋3单元1103室','com_demo0013栋3单元1103室','1304773042','2026-07-09 18:02:12','2026-07-09 18:02:12','2026-07-09 18:02:12',NULL,'已送达','2026-07-09 18:02:12','2026-08-18 04:13:22','courier',NULL),('dlv_000065','ord_000108','res_courier01','pc_demo001','completed',0.00,1.17,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0011栋4单元603室','com_demo0011栋4单元603室','1313704830','2026-07-31 21:51:57','2026-07-31 21:51:57','2026-07-31 21:51:57',NULL,'已送达','2026-07-31 21:51:57','2026-08-18 04:13:22','courier',NULL),('dlv_000066','ord_000110','res_courier03','pc_demo001','delivering',0.00,0.43,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo00111栋4单元2601室','com_demo00111栋4单元2601室','1638083713',NULL,NULL,NULL,NULL,NULL,'2026-07-21 04:55:15','2026-08-18 04:13:22','courier',NULL),('dlv_000067','ord_000111','res_courier02','pc_demo001','completed',0.00,0.95,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0013栋4单元2806室','com_demo0013栋4单元2806室','1747985286','2026-08-07 06:35:22','2026-08-07 06:35:22','2026-08-07 06:35:22',NULL,'已送达','2026-08-07 06:35:22','2026-08-18 04:13:22','courier',NULL),('dlv_000068','ord_000114','res_courier02','pc_demo001','completed',0.00,0.45,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo00110栋4单元2505室','com_demo00110栋4单元2505室','1637920212','2026-07-07 08:49:58','2026-07-07 08:49:58','2026-07-07 08:49:58',NULL,'已送达','2026-07-07 08:49:58','2026-08-18 04:13:22','courier',NULL),('dlv_000069','ord_000115','res_courier01','pc_demo001','completed',0.00,0.74,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo00112栋1单元1902室','com_demo00112栋1单元1902室','1837933874','2026-07-24 18:06:56','2026-07-24 18:06:56','2026-07-24 18:06:56',NULL,'已送达','2026-07-24 18:06:56','2026-08-18 04:13:22','courier',NULL),('dlv_000070','ord_000118','res_courier01','pc_demo001','delivering',0.00,0.80,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0016栋4单元3001室','com_demo0016栋4单元3001室','1799226321',NULL,NULL,NULL,NULL,NULL,'2026-07-08 07:29:06','2026-08-18 04:13:22','courier',NULL),('dlv_000071','ord_000119','res_courier02','pc_demo001','completed',0.00,1.02,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo00110栋1单元806室','com_demo00110栋1单元806室','1522953023','2026-07-12 05:55:12','2026-07-12 05:55:12','2026-07-12 05:55:12',NULL,'已送达','2026-07-12 05:55:12','2026-08-18 04:13:22','courier',NULL),('dlv_000072','ord_000120','res_courier01','pc_demo001','completed',0.00,0.43,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,'com_demo0014栋4单元1102室','com_demo0014栋4单元1102室','1570737741','2026-07-24 20:07:16','2026-07-24 20:07:16','2026-07-24 20:07:16',NULL,'已送达','2026-07-24 20:07:16','2026-08-18 04:13:22','courier',NULL),('dlv_351c88e17c1000','ord_351c88bee41000','res_courier01','pc_demo001','delivered',0.00,0.00,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,NULL,'11101',NULL,'2026-08-13 02:24:28','2026-08-13 02:24:30','2026-08-13 02:24:33',NULL,NULL,'2026-08-13 02:13:20','2026-08-18 04:13:22','courier',NULL),('dlv_351e9cc2181000','ord_351e9bf41e1000',NULL,'pc_demo001','property_fallback',0.00,0.00,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,NULL,'11101',NULL,NULL,NULL,NULL,NULL,NULL,'2026-08-13 07:03:48','2026-08-18 04:13:22','courier',NULL),('dlv_352164b6b41000','ord_352164559e1000','res_courier01','pc_demo001','delivered',0.10,0.10,0.00,0.10,0.00,'none',0.10,0.00,'settled','2026-08-13 13:41:14',NULL,'11101',NULL,'2026-08-13 13:41:03','2026-08-13 13:41:06','2026-08-13 13:41:14',NULL,NULL,'2026-08-13 13:32:38','2026-08-18 04:13:22','courier',NULL),('dlv_35266db2e61000','ord_35266bd4c41000','res_courier01','pc_demo001','delivered',0.10,0.10,0.00,0.10,0.00,'none',0.10,0.00,'settled','2026-08-14 01:18:13',NULL,'11302','19852681536','2026-08-14 01:17:50','2026-08-14 01:17:52','2026-08-14 01:18:13',NULL,NULL,'2026-08-14 01:16:35','2026-08-18 04:13:22','courier',NULL),('dlv_35267c5c6a1000','ord_35267afe501000',NULL,'pc_demo001','refunded',0.10,0.10,0.00,0.10,0.00,'none',0.10,0.00,'reversed',NULL,NULL,'11302','19852681536',NULL,NULL,NULL,NULL,NULL,'2026-08-14 01:24:36','2026-08-18 04:13:22','courier',NULL),('dlv_35268cd9221000','ord_35268adb481000','res_courier01','pc_demo001','delivered',0.10,0.10,0.00,0.10,0.00,'none',0.10,0.00,'settled','2026-08-14 01:34:09',NULL,'11302','19852681536','2026-08-14 01:34:03','2026-08-14 01:34:06','2026-08-14 01:34:09',NULL,NULL,'2026-08-14 01:33:36','2026-08-18 04:13:22','courier',NULL),('dlv_3526aebbf41000','ord_3526ad6bfc1000','res_courier01','pc_demo001','delivered',0.10,0.09,0.01,0.10,0.00,'none',0.10,0.01,'settled','2026-08-14 01:53:05',NULL,'11302','19852681536','2026-08-14 01:52:59','2026-08-14 01:53:01','2026-08-14 01:53:05',NULL,NULL,'2026-08-14 01:52:06','2026-08-18 04:13:22','courier',NULL),('dlv_3526f494ca1000','ord_3526f46ef21000','res_courier01','pc_demo001','delivered',0.00,0.09,0.01,0.10,0.10,'merchant',0.10,0.01,'settled','2026-08-14 02:30:32',NULL,'11101',NULL,'2026-08-14 02:30:24','2026-08-14 02:30:28','2026-08-14 02:30:32',NULL,NULL,'2026-08-14 02:30:15','2026-08-18 04:13:22','courier',NULL),('dlv_352869c8e81000','ord_35286996141000','res_courier01','pc_demo001','delivered',0.10,0.09,0.01,0.10,0.00,'none',0.10,0.01,'settled','2026-08-14 05:57:53',NULL,'11101',NULL,'2026-08-14 05:57:29','2026-08-14 05:57:36','2026-08-14 05:57:53',NULL,NULL,'2026-08-14 05:54:04','2026-08-18 04:13:22','courier',NULL),('dlv_35288b08d01000','ord_35288abea81000','res_courier01','pc_demo001','delivered',0.10,0.09,0.01,0.10,0.00,'none',0.10,0.01,'settled','2026-08-14 06:13:17',NULL,'11101',NULL,'2026-08-14 06:12:48','2026-08-14 06:13:06','2026-08-14 06:13:17',NULL,NULL,'2026-08-14 06:12:14','2026-08-18 04:13:22','courier',NULL),('dlv_354fc8ed021000','ord_354fc89cc21000','res_courier01','pc_demo001','delivered',0.10,0.09,0.01,0.10,0.00,'none',0.10,0.01,'settled','2026-08-18 01:41:53',NULL,'11101','13624964818','2026-08-18 01:41:46','2026-08-18 01:41:50','2026-08-18 01:41:53',NULL,NULL,'2026-08-18 01:38:37','2026-08-18 04:13:22','courier',NULL),('dlv_35511b520e1000','ord_000007',NULL,'pc_demo001','property_fallback',0.00,0.00,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,NULL,'com_demo0011栋2单元2203室','1982153819',NULL,NULL,NULL,NULL,NULL,'2026-08-18 04:43:26','2026-08-18 05:14:02','courier',0.00),('dlv_35511b53901000','ord_000009',NULL,'pc_demo001','property_fallback',0.00,0.00,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,NULL,'com_demo0018栋1单元1003室','1856988147',NULL,NULL,NULL,NULL,NULL,'2026-08-18 04:43:26','2026-08-18 05:14:02','courier',0.00),('dlv_35511b54241000','ord_000037',NULL,'pc_demo001','property_fallback',0.00,0.00,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,NULL,'com_demo0011栋4单元204室','1354918986',NULL,NULL,NULL,NULL,NULL,'2026-08-18 04:43:26','2026-08-18 05:14:02','courier',0.00),('dlv_35511b54d61000','ord_000042',NULL,'pc_demo001','property_fallback',0.00,0.00,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,NULL,'com_demo00110栋2单元1702室','1342718298',NULL,NULL,NULL,NULL,NULL,'2026-08-18 04:43:26','2026-08-18 05:14:02','courier',0.00),('dlv_35511b556e1000','ord_000047',NULL,'pc_demo001','property_fallback',0.00,0.00,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,NULL,'com_demo0019栋4单元2605室','1945600610',NULL,NULL,NULL,NULL,NULL,'2026-08-18 04:43:26','2026-08-18 05:14:02','courier',0.00),('dlv_35511b56041000','ord_000054',NULL,'pc_demo001','property_fallback',0.00,0.00,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,NULL,'com_demo0014栋3单元2805室','1797537574',NULL,NULL,NULL,NULL,NULL,'2026-08-18 04:43:26','2026-08-18 05:14:02','courier',0.00),('dlv_35511b568e1000','ord_000063',NULL,'pc_demo001','property_fallback',0.00,0.00,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,NULL,'com_demo0019栋3单元1004室','1589933353',NULL,NULL,NULL,NULL,NULL,'2026-08-18 04:43:26','2026-08-18 05:14:02','courier',0.00),('dlv_35511b572a1000','ord_000078',NULL,'pc_demo001','property_fallback',0.00,0.00,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,NULL,'com_demo0021栋2单元805室','1580703639',NULL,NULL,NULL,NULL,NULL,'2026-08-18 04:43:26','2026-08-18 05:14:02','courier',0.00),('dlv_35511b57b41000','ord_000081',NULL,'pc_demo001','property_fallback',0.00,0.00,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,NULL,'com_demo0015栋2单元2903室','1651978414',NULL,NULL,NULL,NULL,NULL,'2026-08-18 04:43:27','2026-08-18 05:14:02','courier',0.00),('dlv_35511b581a1000','ord_000084',NULL,'pc_demo001','property_fallback',0.00,0.00,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,NULL,'com_demo00112栋2单元302室','1662313499',NULL,NULL,NULL,NULL,NULL,'2026-08-18 04:43:27','2026-08-18 05:14:02','courier',0.00),('dlv_35511b58941000','ord_000094',NULL,'pc_demo001','property_fallback',0.00,0.00,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,NULL,'com_demo00112栋1单元1702室','1887838616',NULL,NULL,NULL,NULL,NULL,'2026-08-18 04:43:27','2026-08-18 05:14:02','courier',0.00),('dlv_35511b58f81000','ord_000109',NULL,'pc_demo001','property_fallback',0.00,0.00,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,NULL,'com_demo0018栋1单元206室','1379989107',NULL,NULL,NULL,NULL,NULL,'2026-08-18 04:43:27','2026-08-18 05:14:02','courier',0.00),('dlv_35511b59801000','ord_000117',NULL,'pc_demo001','property_fallback',0.00,0.00,0.00,0.00,0.00,'none',0.00,0.00,'pending',NULL,NULL,'com_demo0011栋4单元1902室','1968415086',NULL,NULL,NULL,NULL,NULL,'2026-08-18 04:43:27','2026-08-18 05:14:02','courier',0.00),('dlv_355140d5241000','ord_35513f81481000',NULL,'pc_demo001','delivered',0.20,0.00,0.02,0.20,0.00,'none',0.20,0.02,'settled','2026-08-18 05:26:06',NULL,'11101','13624964818',NULL,NULL,'2026-08-18 05:26:06',NULL,NULL,'2026-08-18 05:03:55','2026-08-18 05:26:06','merchant',0.18),('dlv_355222cb781000','ord_3552221f381000',NULL,'pc_demo001','delivered',0.20,0.00,0.02,0.20,0.00,'none',0.20,0.02,'settled','2026-08-18 07:07:21',NULL,'11101','13624964818',NULL,NULL,'2026-08-18 07:07:21',NULL,NULL,'2026-08-18 07:07:19','2026-08-18 07:07:21','merchant',0.18),('dlv_3552582d2a1000','ord_355220b31e1000',NULL,'pc_demo001','property_fallback',0.20,0.18,0.02,0.20,0.00,'none',0.20,0.02,'pending',NULL,NULL,'11101','13624964818',NULL,NULL,NULL,NULL,NULL,'2026-08-18 07:36:29','2026-08-18 08:07:29','courier',0.00),('dlv_35853280ea1000','ord_358531aa921000',NULL,'pc_demo001','property_fallback',0.00,0.09,0.01,0.10,0.10,'merchant',0.10,0.01,'pending',NULL,NULL,'11108','13624964818',NULL,NULL,NULL,NULL,NULL,'2026-08-23 06:06:13','2026-08-23 06:36:48','courier',0.00),('dlv_358534c4ec1000','ord_358533a5ce1000',NULL,'pc_demo001','property_fallback',0.00,0.09,0.01,0.10,0.10,'merchant',0.10,0.01,'pending',NULL,NULL,'11108','13624964818',NULL,NULL,NULL,NULL,NULL,'2026-08-23 06:07:27','2026-08-23 06:37:48','courier',0.00),('dlv_358540c3f41000','ord_35854036621000',NULL,'pc_demo001','property_fallback',0.00,0.09,0.01,0.10,0.10,'merchant',0.10,0.01,'pending',NULL,NULL,'11108','13624964818',NULL,NULL,NULL,NULL,NULL,'2026-08-23 06:14:00','2026-08-23 06:44:48','courier',0.00),('dlv_35860b0d101000','ord_358609a9a81000','res_courier01','pc_demo001','delivered',0.00,0.09,0.01,0.10,0.10,'merchant',0.10,0.01,'settled','2026-08-23 08:25:38',NULL,'11108','13624964818','2026-08-23 08:25:23','2026-08-23 08:25:34','2026-08-23 08:25:38',NULL,NULL,'2026-08-23 08:04:29','2026-08-23 08:25:38','courier',0.00),('dlv_35862fee8c1000','ord_358628e0fa1000',NULL,'pc_demo001','merchant_self',0.00,0.00,0.01,0.10,0.10,'merchant',0.10,0.01,'pending',NULL,NULL,'11108','13624964818',NULL,NULL,NULL,NULL,NULL,'2026-08-23 08:24:37','2026-08-23 08:24:38','merchant',0.09),('dlv_358630dec41000','ord_358630648a1000','res_courier01','pc_demo001','delivered',0.10,0.09,0.01,0.10,0.00,'none',0.10,0.01,'settled','2026-08-23 08:25:36',NULL,'11108','13624964818','2026-08-23 08:25:26','2026-08-23 08:25:33','2026-08-23 08:25:36',NULL,NULL,'2026-08-23 08:25:08','2026-08-23 08:25:36','courier',0.00),('dlv_36cd38e3b21000','ord_36cd384b201000',NULL,'pc_demo001','property_fallback',0.00,0.35,NULL,NULL,0.00,NULL,NULL,0.00,NULL,NULL,NULL,'11108','18861350048',NULL,NULL,NULL,NULL,NULL,'2026-09-24 02:27:26','2026-09-24 02:57:38','courier',NULL),('dlv_903a1000','ord_593c1000',NULL,'pc_demo001','property_fallback',0.10,0.35,NULL,NULL,0.00,NULL,NULL,0.00,NULL,NULL,NULL,'11108','13624964818',NULL,NULL,NULL,NULL,NULL,'2026-09-06 09:33:12','2026-09-09 12:08:42','courier',NULL),('dlv_aa681000','ord_821c1000',NULL,'pc_demo001','merchant_self',0.10,0.00,NULL,NULL,0.00,NULL,NULL,0.00,NULL,NULL,NULL,'11108','13624964818',NULL,NULL,NULL,NULL,NULL,'2026-09-11 08:51:26','2026-09-11 08:52:15','merchant',0.09),('dlv_e2601000','ord_aed01000','res_courier01','pc_demo001','completed',0.10,0.35,NULL,NULL,0.00,NULL,NULL,0.00,NULL,NULL,NULL,'11108','13624964818','2026-09-09 01:41:06','2026-09-09 01:41:17','2026-09-09 01:41:21',NULL,NULL,'2026-09-09 01:38:09','2026-09-09 12:08:42','courier',NULL);
/*!40000 ALTER TABLE `deliveries` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `delivery_price_ranges`
--

DROP TABLE IF EXISTS `delivery_price_ranges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `delivery_price_ranges` (
  `id` varchar(32) NOT NULL COMMENT 'dpr_xxxxxxxx',
  `property_company_id` varchar(32) NOT NULL COMMENT '所属物业公司',
  `min_price` decimal(10,2) NOT NULL COMMENT '最低单价（元/单）',
  `max_price` decimal(10,2) NOT NULL COMMENT '最高单价（元/单）',
  `distance_type` varchar(20) DEFAULT 'any' COMMENT '1km/3km/5km/citywide/any',
  `delivery_scope` varchar(20) DEFAULT 'both' COMMENT 'in_community/out_community/both',
  `status` varchar(20) DEFAULT 'active' COMMENT 'active/inactive',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_property_company` (`property_company_id`),
  CONSTRAINT `delivery_price_ranges_ibfk_1` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业公司配送价格区间表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `delivery_price_ranges`
--

LOCK TABLES `delivery_price_ranges` WRITE;
/*!40000 ALTER TABLE `delivery_price_ranges` DISABLE KEYS */;
INSERT INTO `delivery_price_ranges` VALUES ('dpr_demo001','pc_demo001',0.10,0.50,'any','both','active','2026-07-22 05:29:07','2026-07-22 05:29:07'),('dpr_demo002','pc_demo001',0.20,0.80,'any','both','active','2026-07-22 05:29:07','2026-07-22 05:29:07');
/*!40000 ALTER TABLE `delivery_price_ranges` ENABLE KEYS */;
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
  `delivery_scope` varchar(20) DEFAULT 'both' COMMENT 'in_community/out_community/both',
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
INSERT INTO `delivery_rules` VALUES ('drl_8ef41000','默认配送规则',0.00,0.00,0.00,NULL,NULL,'both',1.0000,0.35,30,'pc_8df81000','active','2026-09-07 14:38:38','2026-09-07 14:38:38'),('drl_demo001','默认配送规则',1.00,0.50,NULL,NULL,NULL,'both',1.0000,0.35,30,'pc_demo001','active','2026-07-05 05:49:39','2026-07-05 05:49:39');
/*!40000 ALTER TABLE `delivery_rules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `directed_message_recipients`
--

DROP TABLE IF EXISTS `directed_message_recipients`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `directed_message_recipients` (
  `id` varchar(32) NOT NULL,
  `task_id` varchar(32) NOT NULL,
  `recipient_id` varchar(32) NOT NULL,
  `message_id` varchar(32) DEFAULT NULL,
  `read_status` varchar(20) DEFAULT 'unread',
  `read_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_recipient` (`task_id`,`recipient_id`),
  KEY `directed_message_recipients_ibfk_2` (`recipient_id`),
  CONSTRAINT `directed_message_recipients_ibfk_1` FOREIGN KEY (`task_id`) REFERENCES `directed_message_tasks` (`id`) ON DELETE CASCADE,
  CONSTRAINT `directed_message_recipients_ibfk_2` FOREIGN KEY (`recipient_id`) REFERENCES `residents` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `directed_message_recipients`
--

LOCK TABLES `directed_message_recipients` WRITE;
/*!40000 ALTER TABLE `directed_message_recipients` DISABLE KEYS */;
INSERT INTO `directed_message_recipients` VALUES ('dmr_351ca33acc1000','dmt_351ca33ab21000','res_001','msg_351ca33aba1000','unread',NULL,'2026-08-13 02:27:43','2026-08-13 02:27:43'),('dmr_351ca33ad61000','dmt_351ca33ab21000','res_002','msg_351ca33ad21000','unread',NULL,'2026-08-13 02:27:43','2026-08-13 02:27:43'),('dmr_351ca33ade1000','dmt_351ca33ab21000','res_003','msg_351ca33adc1000','unread',NULL,'2026-08-13 02:27:43','2026-08-13 02:27:43'),('dmr_351ca33ae21000','dmt_351ca33ab21000','res_3516a17fb01000','msg_351ca33ae01000','unread',NULL,'2026-08-13 02:27:43','2026-08-13 02:27:43'),('dmr_351ca33ae41001','dmt_351ca33ab21000','res_act01','msg_351ca33ae41000','read','2026-08-23 03:25:39','2026-08-13 02:27:43','2026-08-23 03:25:39'),('dmr_351ca33ae61001','dmt_351ca33ab21000','res_coo01','msg_351ca33ae61000','unread',NULL,'2026-08-13 02:27:43','2026-08-13 02:27:43'),('dmr_351ca33ae81001','dmt_351ca33ab21000','res_courier01','msg_351ca33ae81000','unread',NULL,'2026-08-13 02:27:43','2026-08-13 02:27:43'),('dmr_351ca33aee1000','dmt_351ca33ab21000','res_courier02','msg_351ca33aec1000','unread',NULL,'2026-08-13 02:27:43','2026-08-13 02:27:43'),('dmr_351ca33af21000','dmt_351ca33ab21000','res_courier03','msg_351ca33af01000','unread',NULL,'2026-08-13 02:27:43','2026-08-13 02:27:43'),('dmr_351ca33af41001','dmt_351ca33ab21000','res_dev001','msg_351ca33af41000','read','2026-08-13 05:45:36','2026-08-13 02:27:43','2026-08-13 05:45:36'),('dmr_351ca33af81000','dmt_351ca33ab21000','res_dev003','msg_351ca33af61000','unread',NULL,'2026-08-13 02:27:43','2026-08-13 02:27:43'),('dmr_351ca33af81002','dmt_351ca33ab21000','res_dev004','msg_351ca33af81001','unread',NULL,'2026-08-13 02:27:43','2026-08-13 02:27:43'),('dmr_351ca33afa1001','dmt_351ca33ab21000','res_ind01','msg_351ca33afa1000','unread',NULL,'2026-08-13 02:27:43','2026-08-13 02:27:43'),('dmr_351ca33afc1001','dmt_351ca33ab21000','res_mch01','msg_351ca33afc1000','unread',NULL,'2026-08-13 02:27:43','2026-08-13 02:27:43'),('dmr_351ca33afe1001','dmt_351ca33ab21000','res_mch02','msg_351ca33afe1000','unread',NULL,'2026-08-13 02:27:43','2026-08-13 02:27:43'),('dmr_351ca33b001000','dmt_351ca33ab21000','res_mch03','msg_351ca33afe1002','unread',NULL,'2026-08-13 02:27:43','2026-08-13 02:27:43'),('dmr_351ca33b041001','dmt_351ca33ab21000','res_mch04','msg_351ca33b041000','unread',NULL,'2026-08-13 02:27:43','2026-08-13 02:27:43'),('dmr_351ca33b061001','dmt_351ca33ab21000','res_pleader01','msg_351ca33b061000','unread',NULL,'2026-08-13 02:27:43','2026-08-13 02:27:43'),('dmr_351ca33b0a1000','dmt_351ca33ab21000','res_propadmin01','msg_351ca33b081000','unread',NULL,'2026-08-13 02:27:43','2026-08-13 02:27:43'),('dmr_351ca33b0c1000','dmt_351ca33ab21000','res_sec01','msg_351ca33b0a1001','unread',NULL,'2026-08-13 02:27:43','2026-08-13 02:27:43'),('dmr_351ca33b0c1002','dmt_351ca33ab21000','res_tech01','msg_351ca33b0c1001','unread',NULL,'2026-08-13 02:27:43','2026-08-13 02:27:43'),('dmr_35853b029c1000','dmt_35853b02881000','res_001','msg_35853b02901000','unread',NULL,'2026-08-23 06:10:52','2026-08-23 06:10:52'),('dmr_35853b02a41000','dmt_35853b02881000','res_002','msg_35853b02a21000','unread',NULL,'2026-08-23 06:10:52','2026-08-23 06:10:52'),('dmr_35853b02a61000','dmt_35853b02881000','res_003','msg_35853b02a41001','unread',NULL,'2026-08-23 06:10:52','2026-08-23 06:10:52'),('dmr_35853b02a81000','dmt_35853b02881000','res_3516a17fb01000','msg_35853b02a61001','unread',NULL,'2026-08-23 06:10:52','2026-08-23 06:10:52'),('dmr_35853b02aa1001','dmt_35853b02881000','res_act01','msg_35853b02aa1000','unread',NULL,'2026-08-23 06:10:52','2026-08-23 06:10:52'),('dmr_35853b02ae1000','dmt_35853b02881000','res_admin01','msg_35853b02ac1000','unread',NULL,'2026-08-23 06:10:52','2026-08-23 06:10:52'),('dmr_35853b02b01000','dmt_35853b02881000','res_coo01','msg_35853b02ae1001','unread',NULL,'2026-08-23 06:10:52','2026-08-23 06:10:52'),('dmr_35853b02b21001','dmt_35853b02881000','res_courier01','msg_35853b02b21000','unread',NULL,'2026-08-23 06:10:52','2026-08-23 06:10:52'),('dmr_35853b02b61000','dmt_35853b02881000','res_courier02','msg_35853b02b41000','unread',NULL,'2026-08-23 06:10:52','2026-08-23 06:10:52'),('dmr_35853b02b81000','dmt_35853b02881000','res_courier03','msg_35853b02b61001','unread',NULL,'2026-08-23 06:10:52','2026-08-23 06:10:52'),('dmr_35853b02bc1000','dmt_35853b02881000','res_demo01','msg_35853b02ba1000','unread',NULL,'2026-08-23 06:10:52','2026-08-23 06:10:52'),('dmr_35853b02cc1000','dmt_35853b02881000','res_demo02','msg_35853b02ca1000','unread',NULL,'2026-08-23 06:10:52','2026-08-23 06:10:52'),('dmr_35853b02ce1000','dmt_35853b02881000','res_demo03','msg_35853b02cc1001','unread',NULL,'2026-08-23 06:10:52','2026-08-23 06:10:52'),('dmr_35853b02d01001','dmt_35853b02881000','res_dev001','msg_35853b02d01000','read','2026-08-23 08:42:58','2026-08-23 06:10:52','2026-08-23 08:42:58'),('dmr_35853b02d21001','dmt_35853b02881000','res_dev003','msg_35853b02d21000','unread',NULL,'2026-08-23 06:10:52','2026-08-23 06:10:52'),('dmr_35853b02d61001','dmt_35853b02881000','res_dev004','msg_35853b02d61000','unread',NULL,'2026-08-23 06:10:52','2026-08-23 06:10:52'),('dmr_35853b02d81001','dmt_35853b02881000','res_ind01','msg_35853b02d81000','unread',NULL,'2026-08-23 06:10:52','2026-08-23 06:10:52'),('dmr_35853b02dc1000','dmt_35853b02881000','res_mch01','msg_35853b02da1000','unread',NULL,'2026-08-23 06:10:52','2026-08-23 06:10:52'),('dmr_35853b02de1001','dmt_35853b02881000','res_mch02','msg_35853b02de1000','unread',NULL,'2026-08-23 06:10:52','2026-08-23 06:10:52'),('dmr_35853b02e01001','dmt_35853b02881000','res_mch03','msg_35853b02e01000','unread',NULL,'2026-08-23 06:10:52','2026-08-23 06:10:52'),('dmr_35853b02e21001','dmt_35853b02881000','res_mch04','msg_35853b02e21000','unread',NULL,'2026-08-23 06:10:52','2026-08-23 06:10:52'),('dmr_35853b02e61000','dmt_35853b02881000','res_pleader01','msg_35853b02e41000','unread',NULL,'2026-08-23 06:10:52','2026-08-23 06:10:52'),('dmr_35853b02e81001','dmt_35853b02881000','res_sec01','msg_35853b02e81000','unread',NULL,'2026-08-23 06:10:52','2026-08-23 06:10:52'),('dmr_35853b02ea1001','dmt_35853b02881000','res_tech01','msg_35853b02ea1000','unread',NULL,'2026-08-23 06:10:52','2026-08-23 06:10:52'),('dmr_c1ea1000','dmt_c07a1000','res_001','msg_c1b61000','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c2061000','dmt_c07a1000','res_002','msg_c2021000','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c2081001','dmt_c07a1000','res_003','msg_c2081000','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c2141000','dmt_c07a1000','res_3516a17fb01000','msg_c20c1000','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c2181000','dmt_c07a1000','res_35890655f01000','msg_c2161000','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c21a1000','dmt_c07a1000','res_35c14607ae1000','msg_c2181001','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c21c1000','dmt_c07a1000','res_35c14b96101000','msg_c21a1001','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c2221000','dmt_c07a1000','res_act01','msg_c21c1001','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c22a1000','dmt_c07a1000','res_admin01','msg_c2261000','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c22c1001','dmt_c07a1000','res_coo01','msg_c22c1000','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c22e1001','dmt_c07a1000','res_courier01','msg_c22e1000','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c2321000','dmt_c07a1000','res_courier02','msg_c2301000','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c2341000','dmt_c07a1000','res_courier03','msg_c2321001','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c2361000','dmt_c07a1000','res_demo01','msg_c2341001','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c2381000','dmt_c07a1000','res_demo02','msg_c2361001','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c23c1000','dmt_c07a1000','res_demo03','msg_c2381001','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c2401000','dmt_c07a1000','res_dev001','msg_c23e1000','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c2521000','dmt_c07a1000','res_dev003','msg_c2401001','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c2561000','dmt_c07a1000','res_dev004','msg_c2541000','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c2581000','dmt_c07a1000','res_ind01','msg_c2561001','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c25a1000','dmt_c07a1000','res_mch01','msg_c2581001','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c25c1000','dmt_c07a1000','res_mch02','msg_c25a1001','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c25e1000','dmt_c07a1000','res_mch03','msg_c25c1001','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c25e1002','dmt_c07a1000','res_mch04','msg_c25e1001','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c2641000','dmt_c07a1000','res_pleader01','msg_c2621000','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c2661000','dmt_c07a1000','res_sec01','msg_c2641001','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11'),('dmr_c2661002','dmt_c07a1000','res_tech01','msg_c2661001','unread',NULL,'2026-09-07 02:06:11','2026-09-07 02:06:11');
/*!40000 ALTER TABLE `directed_message_recipients` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `directed_message_tasks`
--

DROP TABLE IF EXISTS `directed_message_tasks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `directed_message_tasks` (
  `id` varchar(32) NOT NULL,
  `sender_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `official_sender_type` varchar(30) NOT NULL,
  `title` varchar(128) NOT NULL,
  `content` text NOT NULL,
  `image_urls` json DEFAULT NULL,
  `filter_buildings` json DEFAULT NULL,
  `filter_gender` varchar(10) DEFAULT 'all',
  `filter_age_bracket_ids` json DEFAULT NULL,
  `merchant_ids` json DEFAULT NULL COMMENT '按商家消费筛选（商家ID数组）',
  `recipient_count` int NOT NULL DEFAULT '0',
  `read_count` int NOT NULL DEFAULT '0',
  `status` varchar(20) DEFAULT 'sent',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `product_ids` json DEFAULT NULL COMMENT '按产品购买筛选的ID数组',
  PRIMARY KEY (`id`),
  KEY `idx_sender` (`sender_id`),
  KEY `idx_property_company` (`property_company_id`),
  CONSTRAINT `directed_message_tasks_ibfk_1` FOREIGN KEY (`sender_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `directed_message_tasks_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `directed_message_tasks`
--

LOCK TABLES `directed_message_tasks` WRITE;
/*!40000 ALTER TABLE `directed_message_tasks` DISABLE KEYS */;
INSERT INTO `directed_message_tasks` VALUES ('dmt_351ca33ab21000','res_admin01','pc_demo001','platform','欢迎加入邻里商城服务！','欢迎加入邻里商城服务！',NULL,NULL,'all',NULL,NULL,21,2,'sent','2026-08-13 02:27:43','2026-08-23 03:25:39',NULL),('dmt_35853b02881000','res_propadmin01','pc_demo001','property','823823','823',NULL,NULL,'all',NULL,NULL,24,1,'sent','2026-08-23 06:10:52','2026-08-23 08:42:58',NULL),('dmt_c07a1000','res_propadmin01','pc_demo001','property','97','97',NULL,NULL,'all',NULL,NULL,27,0,'sent','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL);
/*!40000 ALTER TABLE `directed_message_tasks` ENABLE KEYS */;
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
  `platform_share` decimal(10,2) DEFAULT '0.00' COMMENT '平台管理员分成金额',
  `courier_share` decimal(10,2) DEFAULT '0.00' COMMENT '快递员分成金额（平台份额中按快递分成比例预留）',
  `coordinator_id` varchar(32) DEFAULT NULL COMMENT '统筹ID',
  `sector_leader_id` varchar(32) DEFAULT NULL COMMENT '板块负责人ID',
  `individual_leader_id` varchar(32) DEFAULT NULL COMMENT '个体负责人ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delivery_fee` decimal(10,2) DEFAULT '0.00' COMMENT '配送费',
  `commission_base_amount` decimal(10,2) DEFAULT '0.00' COMMENT '佣金基数（订单总额扣除优惠等）',
  `merchant_share` decimal(10,2) DEFAULT '0.00' COMMENT '商家分成',
  `deficit_amount` decimal(10,2) DEFAULT '0.00' COMMENT '赤字/亏损金额',
  `product_amount` decimal(10,2) DEFAULT '0.00' COMMENT '商品金额(不含配送费)',
  `product_commission` decimal(10,2) DEFAULT '0.00' COMMENT '抽佣基础额 = 商品金额 × 抽佣率',
  `product_base` decimal(10,2) DEFAULT '0.00' COMMENT '商品实付 = 商品金额 − 积分成本 − 物业币成本',
  `original_delivery_fee` decimal(10,2) DEFAULT '0.00' COMMENT '满额减免前基础配送费',
  `delivery_waiver_amount` decimal(10,2) DEFAULT '0.00' COMMENT '满额减免金额',
  `effective_delivery_fee` decimal(10,2) DEFAULT '0.00' COMMENT '实付配送费',
  `delivery_subsidy_sponsor` varchar(20) DEFAULT NULL COMMENT '减免承担方 none/merchant/property/platform',
  `delivery_subsidy_amount` decimal(10,2) DEFAULT '0.00' COMMENT '配送补贴金额',
  `delivery_settlement_base` decimal(10,2) DEFAULT '0.00' COMMENT '配送结算基数',
  `merchant_goods_share` decimal(10,2) DEFAULT '0.00' COMMENT '商家商品实得',
  `merchant_delivery_subsidy` decimal(10,2) DEFAULT '0.00' COMMENT '商家承担的配送补贴',
  `platform_delivery_share` decimal(10,2) DEFAULT '0.00' COMMENT '公司配送收入',
  `courier_earning` decimal(10,2) DEFAULT '0.00' COMMENT '配送员最终收入',
  `platform_share_rate` decimal(5,4) DEFAULT '0.1000' COMMENT '公司商品平台盘比例快照',
  `management_pool_amount` decimal(10,2) DEFAULT '0.00' COMMENT '管理盘 = 平台盘 − 平台 − 物业',
  `sector_gross_amount` decimal(10,2) DEFAULT '0.00' COMMENT '板块毛额 = 管理盘 × sectorLeaderRate',
  `deficit_sponsor` varchar(20) DEFAULT NULL COMMENT '成本击穿承担方（默认 property）',
  `merchant_adjustment_amount` decimal(10,2) DEFAULT '0.00' COMMENT '商家调整额（退款/补差/冲账）',
  `calculation_version` varchar(20) DEFAULT NULL COMMENT '分账计算版本 legacy/distribution_b_v1',
  `status` varchar(20) DEFAULT 'SETTLED' COMMENT '结算状态 SETTLED/REVERSED',
  `platform_earning` decimal(10,2) DEFAULT '0.00' COMMENT '平台收入/平台分成收入',
  `platform_fee_share` decimal(10,2) DEFAULT '0.00' COMMENT '平台费用分成（平台抽成部分）',
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
INSERT INTO `distribution_records` VALUES ('dr_351c88e19c1000','ord_351c88bee41000','mch_demo001','pc_demo001','res_dev001',16.00,0.1500,0.00,16.00,0.00,0.00,0.00,0.00,0.00,0.00,NULL,NULL,NULL,NULL,'2026-08-13 02:13:20','2026-08-13 09:59:44',0.00,0.00,0.00,13.60,0.00,0.00,0.00,0.00,0.00,0.00,'none',0.00,0.00,0.00,0.00,0.00,0.00,0.1000,0.00,0.00,NULL,0.00,'legacy','SETTLED',0.00,0.00),('dr_351e9cc2401000','ord_351e9bf41e1000','mch_demo002','pc_demo001','res_dev001',20.00,0.1000,20.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,NULL,NULL,NULL,NULL,'2026-08-13 07:03:48','2026-08-13 09:59:44',0.00,0.00,0.00,18.00,0.00,0.00,0.00,0.00,0.00,0.00,'none',0.00,0.00,0.00,0.00,0.00,0.00,0.1000,0.00,0.00,NULL,0.00,'legacy','SETTLED',0.00,0.00),('dr_352164b6fa1000','ord_352164559e1000','mch_demo002','pc_demo001','res_dev001',0.20,0.1000,0.00,0.20,0.00,0.00,0.00,0.00,0.00,0.00,0.10,'coo_001',NULL,NULL,'2026-08-13 13:32:38','2026-08-13 13:32:38',0.10,0.01,0.09,0.19,0.10,0.01,-0.10,0.10,0.00,0.10,'none',0.00,0.10,0.09,0.00,0.00,0.10,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_35266db31a1000','ord_35266bd4c41000','mch_demo002','pc_demo001','res_35217712f21000',0.20,0.1000,0.00,0.00,0.01,0.01,0.00,0.00,0.00,0.00,0.10,'coo_001',NULL,NULL,'2026-08-14 01:16:35','2026-08-14 01:16:35',0.10,0.01,0.09,0.00,0.10,0.01,0.10,0.10,0.00,0.10,'none',0.00,0.10,0.09,0.00,0.00,0.10,0.1000,0.00,0.00,NULL,0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_35267c5c701000','ord_35267afe501000','mch_demo002','pc_demo001','res_35217712f21000',0.20,0.1000,0.00,0.00,0.01,0.01,0.00,0.00,0.00,0.00,0.10,'coo_001',NULL,NULL,'2026-08-14 01:24:36','2026-08-14 01:24:36',0.10,0.01,0.09,0.00,0.10,0.01,0.10,0.10,0.00,0.10,'none',0.00,0.10,0.09,0.00,0.00,0.10,0.1000,0.00,0.00,NULL,0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_35267d54641001','ord_35267afe501000','mch_demo002','pc_demo001','res_35217712f21000',-0.20,0.1000,0.00,0.00,-0.01,-0.01,0.00,0.00,0.00,0.00,-0.10,'coo_001',NULL,NULL,'2026-08-14 01:25:07','2026-08-14 01:25:07',-0.10,-0.01,-0.09,0.00,-0.10,-0.01,NULL,-0.10,0.00,-0.10,NULL,0.00,-0.10,-0.09,0.00,0.00,-0.10,0.1000,0.00,0.00,NULL,0.00,'distribution_b_v1','REVERSED',0.00,0.00),('dr_35268cd92a1000','ord_35268adb481000','mch_demo002','pc_demo001','res_35217712f21000',1.10,0.1000,0.00,0.00,0.10,0.06,0.01,0.01,0.01,0.01,0.10,'coo_001',NULL,NULL,'2026-08-14 01:33:36','2026-08-14 01:33:36',0.10,0.10,0.90,0.00,1.00,0.10,1.00,0.10,0.00,0.10,'none',0.00,0.10,0.90,0.00,0.00,0.10,0.1000,0.03,0.02,NULL,0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_3526aebc001000','ord_3526ad6bfc1000','mch_demo002','pc_demo001','res_35217712f21000',0.20,0.1000,0.00,0.00,0.01,0.01,0.00,0.00,0.00,0.00,0.09,'coo_001',NULL,NULL,'2026-08-14 01:52:06','2026-08-14 01:52:06',0.10,0.01,0.09,0.00,0.10,0.01,0.10,0.10,0.00,0.10,'none',0.00,0.10,0.09,0.00,0.01,0.09,0.1000,0.00,0.00,NULL,0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_3526f494e61000','ord_3526f46ef21000','mch_demo002','pc_demo001','res_dev001',20.00,0.1000,0.00,20.00,0.00,0.00,0.00,0.00,0.00,0.00,0.09,'coo_001',NULL,NULL,'2026-08-14 02:30:15','2026-08-14 02:30:15',0.00,2.00,17.90,18.00,20.00,2.00,0.00,0.10,0.10,0.00,'merchant',0.10,0.10,18.00,0.10,0.01,0.09,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_352869c8f81000','ord_35286996141000','mch_demo002','pc_demo001','res_dev001',7.10,0.1000,0.00,7.10,0.00,0.00,0.00,0.00,0.00,0.00,0.09,'coo_001',NULL,NULL,'2026-08-14 05:54:04','2026-08-14 05:54:04',0.10,0.70,6.30,6.40,7.00,0.70,-0.10,0.10,0.00,0.10,'none',0.00,0.10,6.30,0.00,0.01,0.09,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_35288b08e01000','ord_35288abea81000','mch_demo002','pc_demo001','res_dev001',7.10,0.1000,0.00,7.10,0.00,0.00,0.00,0.00,0.00,0.00,0.09,'coo_001',NULL,NULL,'2026-08-14 06:12:14','2026-08-14 06:12:14',0.10,0.70,6.30,6.40,7.00,0.70,-0.10,0.10,0.00,0.10,'none',0.00,0.10,6.30,0.00,0.01,0.09,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_354fc8ed341000','ord_354fc89cc21000','mch_demo002','pc_demo001','res_dev001',7.10,0.1000,0.00,7.10,0.00,0.00,0.00,0.00,0.00,0.00,0.09,'coo_001',NULL,NULL,'2026-08-18 01:38:37','2026-08-18 01:38:37',0.10,0.70,6.30,6.40,7.00,0.70,-0.10,0.10,0.00,0.10,'none',0.00,0.10,6.30,0.00,0.01,0.09,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_35511b52801000','ord_000007','mch_demo001','pc_demo001','res_004',291.40,0.1500,0.00,0.00,43.71,26.23,5.24,3.93,3.94,4.37,0.00,'coo_001',NULL,NULL,'2026-08-18 04:43:26','2026-08-18 04:43:26',0.00,43.71,247.69,0.00,291.40,43.71,291.40,0.00,0.00,0.00,'none',0.00,0.00,247.69,0.00,0.00,0.00,0.1000,13.11,7.87,NULL,0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_35511b53b81000','ord_000009','mch_demo002','pc_demo001','res_mch03',80.00,0.1000,40.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,'coo_001',NULL,NULL,'2026-08-18 04:43:26','2026-08-18 04:43:26',0.00,8.00,72.00,32.00,80.00,8.00,40.00,0.00,0.00,0.00,'none',0.00,0.00,72.00,0.00,0.00,0.00,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_35511b54361000','ord_000037','mch_demo001','pc_demo001','res_tech01',227.50,0.1500,113.75,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,'coo_001',NULL,NULL,'2026-08-18 04:43:26','2026-08-18 04:43:26',0.00,34.13,193.37,79.62,227.50,34.13,113.75,0.00,0.00,0.00,'none',0.00,0.00,193.37,0.00,0.00,0.00,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_35511b54ea1000','ord_000042','mch_demo001','pc_demo001','res_dev003',129.80,0.1500,0.00,0.00,19.47,11.68,2.34,1.75,1.75,1.95,0.00,'coo_001',NULL,NULL,'2026-08-18 04:43:26','2026-08-18 04:43:26',0.00,19.47,110.33,0.00,129.80,19.47,129.80,0.00,0.00,0.00,'none',0.00,0.00,110.33,0.00,0.00,0.00,0.1000,5.84,3.50,NULL,0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_35511b55901000','ord_000047','mch_demo002','pc_demo001','res_001',38.00,0.1000,9.50,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,'coo_001',NULL,NULL,'2026-08-18 04:43:26','2026-08-18 04:43:26',0.00,3.80,34.20,5.70,38.00,3.80,28.50,0.00,0.00,0.00,'none',0.00,0.00,34.20,0.00,0.00,0.00,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_35511b561a1000','ord_000054','mch_demo001','pc_demo001','res_dev002',404.50,0.1500,202.25,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,'coo_001',NULL,NULL,'2026-08-18 04:43:26','2026-08-18 04:43:26',0.00,60.68,343.82,141.57,404.50,60.68,202.25,0.00,0.00,0.00,'none',0.00,0.00,343.82,0.00,0.00,0.00,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_35511b569e1000','ord_000063','mch_demo001','pc_demo001','res_mch04',404.70,0.1500,202.35,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,'coo_001',NULL,NULL,'2026-08-18 04:43:26','2026-08-18 04:43:26',0.00,60.71,343.99,141.64,404.70,60.71,202.35,0.00,0.00,0.00,'none',0.00,0.00,343.99,0.00,0.00,0.00,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_35511b57401000','ord_000078','mch_demo002','pc_demo001','res_003',84.00,0.1000,0.00,0.00,8.40,5.04,1.01,0.75,0.76,0.84,0.00,'coo_001',NULL,NULL,'2026-08-18 04:43:26','2026-08-18 04:43:26',0.00,8.40,75.60,0.00,84.00,8.40,84.00,0.00,0.00,0.00,'none',0.00,0.00,75.60,0.00,0.00,0.00,0.1000,2.52,1.51,NULL,0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_35511b57ce1000','ord_000081','mch_demo002','pc_demo001','res_mch04',8.00,0.1000,4.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,'coo_001',NULL,NULL,'2026-08-18 04:43:27','2026-08-18 04:43:27',0.00,0.80,7.20,3.20,8.00,0.80,4.00,0.00,0.00,0.00,'none',0.00,0.00,7.20,0.00,0.00,0.00,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_35511b58341000','ord_000084','mch_demo002','pc_demo001','res_mch04',106.00,0.1000,53.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,'coo_001',NULL,NULL,'2026-08-18 04:43:27','2026-08-18 04:43:27',0.00,10.60,95.40,42.40,106.00,10.60,53.00,0.00,0.00,0.00,'none',0.00,0.00,95.40,0.00,0.00,0.00,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_35511b58a61000','ord_000094','mch_demo001','pc_demo001','res_mch03',18.00,0.1500,9.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,'coo_001',NULL,NULL,'2026-08-18 04:43:27','2026-08-18 04:43:27',0.00,2.70,15.30,6.30,18.00,2.70,9.00,0.00,0.00,0.00,'none',0.00,0.00,15.30,0.00,0.00,0.00,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_35511b59081000','ord_000109','mch_demo002','pc_demo001','res_002',65.00,0.1000,32.50,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,'coo_001',NULL,NULL,'2026-08-18 04:43:27','2026-08-18 04:43:27',0.00,6.50,58.50,26.00,65.00,6.50,32.50,0.00,0.00,0.00,'none',0.00,0.00,58.50,0.00,0.00,0.00,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_35511b59901000','ord_000117','mch_demo002','pc_demo001','res_mch04',24.00,0.1000,0.00,0.00,2.40,1.44,0.29,0.21,0.22,0.24,0.00,'coo_001',NULL,NULL,'2026-08-18 04:43:27','2026-08-18 04:43:27',0.00,2.40,21.60,0.00,24.00,2.40,24.00,0.00,0.00,0.00,'none',0.00,0.00,21.60,0.00,0.00,0.00,0.1000,0.72,0.43,NULL,0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_355140d5641000','ord_35513f81481000','mch_demo002','pc_demo001','res_dev001',7.20,0.1000,0.00,7.20,0.00,0.00,0.00,0.00,0.00,0.00,0.18,'coo_001',NULL,NULL,'2026-08-18 05:03:55','2026-08-18 05:03:55',0.20,0.70,6.30,6.50,7.00,0.70,-0.20,0.20,0.00,0.20,'none',0.00,0.20,6.30,0.00,0.02,0.18,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_355222cba21000','ord_3552221f381000','mch_demo002','pc_demo001','res_dev001',7.20,0.1000,0.00,7.20,0.00,0.00,0.00,0.00,0.00,0.00,0.18,'coo_001',NULL,NULL,'2026-08-18 07:07:19','2026-08-18 07:07:19',0.20,0.70,6.30,6.50,7.00,0.70,-0.20,0.20,0.00,0.20,'none',0.00,0.20,6.30,0.00,0.02,0.18,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_3552582d401000','ord_355220b31e1000','mch_demo002','pc_demo001','res_dev001',7.20,0.1000,0.00,7.20,0.00,0.00,0.00,0.00,0.00,0.00,0.18,'coo_001',NULL,NULL,'2026-08-18 07:36:29','2026-08-18 07:36:29',0.20,0.70,6.30,6.50,7.00,0.70,-0.20,0.20,0.00,0.20,'none',0.00,0.20,6.30,0.00,0.02,0.18,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_35853281161000','ord_358531aa921000','mch_demo002','pc_demo001','res_dev001',20.00,0.1000,0.00,20.00,0.00,0.00,0.00,0.00,0.00,0.00,0.09,'coo_001',NULL,NULL,'2026-08-23 06:06:13','2026-08-23 06:06:13',0.00,2.00,17.90,18.00,20.00,2.00,0.00,0.10,0.10,0.00,'merchant',0.10,0.10,18.00,0.10,0.01,0.09,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_358534c5041000','ord_358533a5ce1000','mch_demo002','pc_demo001','res_dev001',20.00,0.1000,0.00,20.00,0.00,0.00,0.00,0.00,0.00,0.00,0.09,'coo_001',NULL,NULL,'2026-08-23 06:07:27','2026-08-23 06:07:27',0.00,2.00,17.90,18.00,20.00,2.00,0.00,0.10,0.10,0.00,'merchant',0.10,0.10,18.00,0.10,0.01,0.09,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_358540c4001000','ord_35854036621000','mch_demo002','pc_demo001','res_dev001',20.00,0.1000,0.00,20.00,0.00,0.00,0.00,0.00,0.00,0.00,0.09,'coo_001',NULL,NULL,'2026-08-23 06:14:00','2026-08-23 06:14:00',0.00,2.00,17.90,18.00,20.00,2.00,0.00,0.10,0.10,0.00,'merchant',0.10,0.10,18.00,0.10,0.01,0.09,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_35860b0d1c1000','ord_358609a9a81000','mch_demo002','pc_demo001','res_dev001',20.00,0.1000,0.00,20.00,0.00,0.00,0.00,0.00,0.00,0.00,0.09,'coo_001',NULL,NULL,'2026-08-23 08:04:29','2026-08-23 08:04:29',0.00,2.00,17.90,18.00,20.00,2.00,0.00,0.10,0.10,0.00,'merchant',0.10,0.10,18.00,0.10,0.01,0.09,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_35862feee81000','ord_358628e0fa1000','mch_demo002','pc_demo001','res_dev001',25.00,0.1000,0.00,25.00,0.00,0.00,0.00,0.00,0.00,0.00,0.09,'coo_001',NULL,NULL,'2026-08-23 08:24:37','2026-08-23 08:24:37',0.00,2.50,22.40,22.50,25.00,2.50,0.00,0.10,0.10,0.00,'merchant',0.10,0.10,22.50,0.10,0.01,0.09,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_358630dee21000','ord_358630648a1000','mch_demo002','pc_demo001','res_dev001',7.10,0.1000,0.00,7.10,0.00,0.00,0.00,0.00,0.00,0.00,0.09,'coo_001',NULL,NULL,'2026-08-23 08:25:08','2026-08-23 08:25:08',0.10,0.70,6.30,6.40,7.00,0.70,-0.10,0.10,0.00,0.10,'none',0.00,0.10,6.30,0.00,0.01,0.09,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_35cc1093881000','ord_35839426361000','mch_demo002','pc_demo001','res_dev001',20.00,0.1000,0.00,20.00,0.00,0.00,0.00,0.00,0.00,0.00,0.09,'coo_001',NULL,NULL,'2026-08-30 03:14:13','2026-08-30 03:14:13',0.00,2.00,17.90,18.00,20.00,2.00,0.00,0.10,0.10,0.00,'merchant',0.10,0.10,18.00,0.10,0.01,0.09,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_35cd5a276c1000','ord_3585315b4c1000','mch_demo002','pc_demo001','res_dev001',0.09,0.1000,0.00,0.09,0.00,0.00,0.00,0.00,0.00,0.00,0.00,'coo_001',NULL,NULL,'2026-08-30 06:14:12','2026-08-30 06:14:12',0.00,0.01,0.08,0.08,0.09,0.01,0.00,0.00,0.00,0.00,'merchant',0.00,0.00,0.08,0.00,0.00,0.00,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_35ce9460801000','ord_35863441be1000','mch_demo002','pc_demo001','res_dev001',0.09,0.1000,0.00,0.09,0.00,0.00,0.00,0.00,0.00,0.00,0.00,'coo_001',NULL,NULL,'2026-08-30 09:05:49','2026-08-30 09:05:49',0.00,0.01,0.08,0.08,0.09,0.01,0.00,0.00,0.00,0.00,'merchant',0.00,0.00,0.08,0.00,0.00,0.00,0.1000,0.00,0.00,'property',0.00,'distribution_b_v1','SETTLED',0.00,0.00),('dr_d5421000','ord_35ce6aeb821000','mch_demo002','pc_demo001','res_dev001',0.09,0.1000,0.00,0.09,-0.08,0.00,0.00,0.00,0.00,NULL,0.00,NULL,NULL,NULL,'2026-09-07 02:25:20','2026-09-07 02:25:20',0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,NULL,0.00,0.00,0.00,0.00,0.00,0.00,0.1000,0.00,0.00,NULL,0.00,NULL,'SETTLED',NULL,NULL);
/*!40000 ALTER TABLE `distribution_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `distributor_products`
--

DROP TABLE IF EXISTS `distributor_products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `distributor_products` (
  `id` varchar(32) NOT NULL,
  `distributor_merchant_id` varchar(32) NOT NULL,
  `name` varchar(100) NOT NULL,
  `cover_url` varchar(500) DEFAULT NULL,
  `description` text,
  `wholesale_price` decimal(10,2) NOT NULL,
  `suggested_retail_price` decimal(10,2) NOT NULL,
  `stock` int NOT NULL DEFAULT '0',
  `status` varchar(20) DEFAULT 'active',
  `billing_cycle` varchar(20) DEFAULT 'one_time' COMMENT '收费周期: one_time/monthly/quarterly/yearly',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `service_scope` varchar(20) DEFAULT NULL COMMENT '服务区间 city/community',
  `category` varchar(50) DEFAULT NULL COMMENT '货源类目（如：食品饮料、日用百货）',
  PRIMARY KEY (`id`),
  KEY `distributor_products_ibfk_1` (`distributor_merchant_id`),
  CONSTRAINT `distributor_products_ibfk_1` FOREIGN KEY (`distributor_merchant_id`) REFERENCES `merchants` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `distributor_products`
--

LOCK TABLES `distributor_products` WRITE;
/*!40000 ALTER TABLE `distributor_products` DISABLE KEYS */;
INSERT INTO `distributor_products` VALUES ('dpr_6c221000','mch_demo002','测试97',NULL,NULL,9.00,15.00,1,'active','one_time','2026-09-07 02:34:24','2026-09-08 00:24:24',NULL,NULL),('dpr_seed0001','mch_demo002','五常大米 5kg',NULL,'东北五常稻花香，产地直发',39.90,49.90,100,'active','one_time','2026-09-04 16:14:17','2026-09-04 16:14:17','city',NULL),('dpr_seed0002','mch_demo002','鲁花花生油 5L',NULL,'物理压榨一级花生油',89.00,109.00,50,'active','one_time','2026-09-04 16:14:17','2026-09-04 16:14:17','city',NULL),('dpr_seed0003','mch_demo002','本色抽纸 30 包',NULL,'原生木浆，三层加厚',29.90,39.90,199,'active','one_time','2026-09-04 16:14:17','2026-09-05 09:15:08','city',NULL);
/*!40000 ALTER TABLE `distributor_products` ENABLE KEYS */;
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
INSERT INTO `families` VALUES ('fam_001','赵六家庭','res_001','pc_demo001','com_demo001','3','2','201',3,5000,300.00,1,'approved','active','2026-07-03 08:09:15','2026-07-03 08:09:15'),('fam_002','钱七家庭','res_002','pc_demo001','com_demo001','3','2','202',2,2500,150.00,1,'approved','active','2026-07-03 08:09:15','2026-07-03 08:09:15'),('fam_003','孙八家庭','res_003','pc_demo001','com_demo002','5','1','101',2,3000,200.00,1,'approved','active','2026-07-03 08:09:15','2026-07-03 08:09:15'),('fam_35217f046c1000','外套家庭','res_35217712f21000','pc_demo001','com_demo001','1','1','302',1,302,0.10,1,'pending','active','2026-08-13 13:47:00','2026-08-21 07:55:40'),('fam_dev001','张阿姨家庭','res_dev001','pc_demo001','com_demo001','3','12','1201',2,78896,887.24,1,'approved','active','2026-07-03 07:40:59','2026-09-24 03:24:31');
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
INSERT INTO `files` VALUES ('file_369c1da6d21000','Htt0ITrngNsQ741b123801df83d187ae3c7523b5ace0.jpg','res_mch03_369c1da6d21000.jpg','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch03_369c1da6d21000.jpg','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch03_369c1da6d21000_thumb.jpg','image/jpeg',8255,'merchant','res_mch03','pc_demo001','2026-09-19 08:01:52','2026-09-19 08:01:52'),('file_369c1dec9e1000','_ZF8ydcqr9NW721da2ac36dd127682ee116917d02294.jpg','res_mch03_369c1dec9e1000.jpg','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch03_369c1dec9e1000.jpg','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch03_369c1dec9e1000_thumb.jpg','image/jpeg',136045,'merchant','res_mch03','pc_demo001','2026-09-19 08:02:01','2026-09-19 08:02:01'),('file_369c1e38c61000','C-1UeoezWoFu4d34a3762b895c673d14b43cef005c7b.jpg','res_mch03_369c1e38c61000.jpg','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch03_369c1e38c61000.jpg','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch03_369c1e38c61000_thumb.jpg','image/jpeg',963576,'merchant','res_mch03','pc_demo001','2026-09-19 08:02:10','2026-09-19 08:02:10'),('file_369c1e7c5e1000','kHlxrJozeDtK81c66bab709377e92920c5f63e0ffd72.jpg','res_mch03_369c1e7c5e1000.jpg','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch03_369c1e7c5e1000.jpg','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch03_369c1e7c5e1000_thumb.jpg','image/jpeg',345888,'merchant','res_mch03','pc_demo001','2026-09-19 08:02:19','2026-09-19 08:02:19'),('file_369c1ec78e1000','7omtuB1441D1a6ec1f38963022f765dbce329d31f709.jpg','res_mch03_369c1ec78e1000.jpg','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch03_369c1ec78e1000.jpg','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch03_369c1ec78e1000_thumb.jpg','image/jpeg',22509,'merchant','res_mch03','pc_demo001','2026-09-19 08:02:28','2026-09-19 08:02:28'),('file_369c400a461000','pN_Kp3fT7lzm4640ba182a6b0d847e70cdfcfe486755.jpg','res_mch02_369c400a461000.jpg','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c400a461000.jpg','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c400a461000_thumb.jpg','image/jpeg',605285,'merchant','res_mch02','pc_demo001','2026-09-19 08:20:38','2026-09-19 08:20:38'),('file_369c4064001000','09800s1CQ0whf9e6624e69efa44fa14903a8dc956f26.webp','res_mch02_369c4064001000.webp','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c4064001000.webp','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c4064001000.webp','image/webp',62626,'merchant','res_mch02','pc_demo001','2026-09-19 08:20:50','2026-09-19 08:20:50'),('file_369c40cd341000','dYNx6zlNNIso31592e602bf065b545fb7870718d2fcc.webp','res_mch02_369c40cd341000.webp','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c40cd341000.webp','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c40cd341000.webp','image/webp',4942,'merchant','res_mch02','pc_demo001','2026-09-19 08:21:03','2026-09-19 08:21:03'),('file_369c4119da1000','QHj0cTEeQ8dOdae9480b038e063d90bf1affc99c39e9.webp','res_mch02_369c4119da1000.webp','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c4119da1000.webp','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c4119da1000.webp','image/webp',3848,'merchant','res_mch02','pc_demo001','2026-09-19 08:21:13','2026-09-19 08:21:13'),('file_369c419fa61000','qdi5-m0wr5OUe1db486d55e987b41b5d8a567c62df41.jpg','res_mch02_369c419fa61000.jpg','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c419fa61000.jpg','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c419fa61000_thumb.jpg','image/jpeg',238391,'merchant','res_mch02','pc_demo001','2026-09-19 08:21:30','2026-09-19 08:21:30'),('file_369c41e3741000','mdirF17oXpwR02409e99d54860b01a1445b16e5f2af9.jpg','res_mch02_369c41e3741000.jpg','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c41e3741000.jpg','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c41e3741000_thumb.jpg','image/jpeg',329375,'merchant','res_mch02','pc_demo001','2026-09-19 08:21:39','2026-09-19 08:21:39'),('file_369c4238ea1000','b9oBcvyY9t1Hfb24754f748f4f27506d497b2a6d2e07.jpg','res_mch02_369c4238ea1000.jpg','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c4238ea1000.jpg','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c4238ea1000_thumb.jpg','image/jpeg',210326,'merchant','res_mch02','pc_demo001','2026-09-19 08:21:50','2026-09-19 08:21:50'),('file_369c4276801000','RXfPR9EO-1Ka166dde793cb7c30eded8e4866f2ac317.webp','res_mch02_369c4276801000.webp','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c4276801000.webp','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c4276801000.webp','image/webp',209736,'merchant','res_mch02','pc_demo001','2026-09-19 08:21:58','2026-09-19 08:21:58'),('file_369c42d2481000','Voj1qeFKofIWfbc4750a90f64be24b907f9d2729e23e.webp','res_mch02_369c42d2481000.webp','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c42d2481000.webp','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c42d2481000.webp','image/webp',11736,'merchant','res_mch02','pc_demo001','2026-09-19 08:22:09','2026-09-19 08:22:09'),('file_369c430dde1000','mi-Sv7cEWXpgcf856fe181fa1dca0a9015055f205469.jpg','res_mch02_369c430dde1000.jpg','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c430dde1000.jpg','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c430dde1000_thumb.jpg','image/jpeg',82435,'merchant','res_mch02','pc_demo001','2026-09-19 08:22:17','2026-09-19 08:22:17'),('file_369cf884961000','DRn03PpaaHfPaf48ceb47b7824cbfeab71fefeeb9cd3.jpg','res_act01_369cf884961000.jpg','https://test.yuanxusoftware.top/api/v1/static/product/res_act01_369cf884961000.jpg','https://test.yuanxusoftware.top/api/v1/static/product/res_act01_369cf884961000_thumb.jpg','image/jpeg',59079,'product','res_act01','pc_demo001','2026-09-19 10:01:24','2026-09-19 10:01:24'),('file_5dca1000','vOyjFC8cLEaY58a2c16d1ae9f2f6131de6fb4dde8697.jpg','res_act01_5dca1000.jpg','https://test.yuanxusoftware.top/api/v1/static/activity/res_act01_5dca1000.jpg','https://test.yuanxusoftware.top/api/v1/static/activity/res_act01_5dca1000_thumb.jpg','image/jpeg',98272,'activity','res_act01','pc_demo001','2026-09-05 05:57:14','2026-09-05 05:57:14'),('file_e6261000','QcpItq7RF9uD58a2c16d1ae9f2f6131de6fb4dde8697.jpg','res_act01_e6261000.jpg','https://test.yuanxusoftware.top/api/v1/static/activity/res_act01_e6261000.jpg','https://test.yuanxusoftware.top/api/v1/static/activity/res_act01_e6261000_thumb.jpg','image/jpeg',98272,'activity','res_act01','pc_demo001','2026-09-06 09:40:30','2026-09-06 09:40:30');
/*!40000 ALTER TABLE `files` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `script` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flyway_schema_history`
--

LOCK TABLES `flyway_schema_history` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history` DISABLE KEYS */;
INSERT INTO `flyway_schema_history` VALUES (1,'1','baseline from wuye_points_latest.sql','BASELINE','baseline from wuye_points_latest.sql',NULL,'wuye_app','2026-09-03 01:24:36',0,1),(2,'2','concurrency and idempotency','SQL','V2__concurrency_and_idempotency.sql',-1651307643,'wuye_app','2026-09-03 01:24:36',42,1),(3,'3','canteen directed balance unique','SQL','V3__canteen_directed_balance_unique.sql',-1714743048,'wuye_app','2026-09-03 01:24:36',10,1),(4,'4','merchant recommend','SQL','V4__merchant_recommend.sql',1739282217,'wuye_app','2026-09-03 01:24:36',7,1),(5,'5','community post visibility','SQL','V5__community_post_visibility.sql',-2109310075,'wuye_app','2026-09-03 01:24:36',7,1),(6,'6','directed message product filter','SQL','V6__directed_message_product_filter.sql',1400091181,'wuye_app','2026-09-03 01:24:36',5,1),(7,'7','courier manager share rate','SQL','V7__courier_manager_share_rate.sql',1926546204,'wuye_app','2026-09-03 01:24:36',13,1),(8,'8','activity group review','SQL','V8__activity_group_review.sql',1475953490,'wuye_app','2026-09-03 01:24:37',19,1),(9,'9','requirement alignment','SQL','V9__requirement_alignment.sql',1520078001,'wuye_app','2026-09-03 06:57:12',0,1);
/*!40000 ALTER TABLE `flyway_schema_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flyway_schema_history_bak_20260903`
--

DROP TABLE IF EXISTS `flyway_schema_history_bak_20260903`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history_bak_20260903` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `script` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flyway_schema_history_bak_20260903`
--

LOCK TABLES `flyway_schema_history_bak_20260903` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history_bak_20260903` DISABLE KEYS */;
INSERT INTO `flyway_schema_history_bak_20260903` VALUES (1,'1','baseline from wuye_points_latest.sql','BASELINE','baseline from wuye_points_latest.sql',NULL,'wuye_app','2026-09-03 01:24:36',0,1),(2,'2','concurrency and idempotency','SQL','V2__concurrency_and_idempotency.sql',-1651307643,'wuye_app','2026-09-03 01:24:36',42,1),(3,'3','canteen directed balance unique','SQL','V3__canteen_directed_balance_unique.sql',-1714743048,'wuye_app','2026-09-03 01:24:36',10,1),(4,'4','merchant recommend','SQL','V4__merchant_recommend.sql',1739282217,'wuye_app','2026-09-03 01:24:36',7,1),(5,'5','community post visibility','SQL','V5__community_post_visibility.sql',-2109310075,'wuye_app','2026-09-03 01:24:36',7,1),(6,'6','directed message product filter','SQL','V6__directed_message_product_filter.sql',1400091181,'wuye_app','2026-09-03 01:24:36',5,1),(7,'7','courier manager share rate','SQL','V7__courier_manager_share_rate.sql',1926546204,'wuye_app','2026-09-03 01:24:36',13,1),(8,'8','activity group review','SQL','V8__activity_group_review.sql',1475953490,'wuye_app','2026-09-03 01:24:37',19,1),(9,'9','requirement alignment','SQL','V9__requirement_alignment.sql',1520078001,'wuye_app','2026-09-03 01:24:37',34,0);
/*!40000 ALTER TABLE `flyway_schema_history_bak_20260903` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fulfillment_choice_logs`
--

DROP TABLE IF EXISTS `fulfillment_choice_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fulfillment_choice_logs` (
  `id` varchar(32) NOT NULL COMMENT '主键 fcl_xxx',
  `order_id` varchar(32) NOT NULL COMMENT '关联订单ID',
  `merchant_id` varchar(32) NOT NULL COMMENT '关联商家ID',
  `actor_type` varchar(20) NOT NULL COMMENT '触发方 merchant/system/admin',
  `actor_id` varchar(32) NOT NULL COMMENT '触发方ID（商家ID/管理员ID/system）',
  `action` varchar(20) NOT NULL COMMENT '审计动作 choose/auto_choose/timeout/override',
  `before_mode` varchar(20) DEFAULT NULL COMMENT '变更前履约方式',
  `after_mode` varchar(20) NOT NULL COMMENT '变更后履约方式 merchant_self/courier_hall',
  `remark` varchar(200) DEFAULT NULL COMMENT '说明（如超时兜底、管理员改派原因）',
  `delivery_id` varchar(32) DEFAULT NULL COMMENT '本次选择生成的配送单ID',
  `choice_at` datetime DEFAULT NULL COMMENT '操作时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_fcl_order` (`order_id`),
  KEY `idx_fcl_choice_at` (`choice_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='履约方式选择审计日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fulfillment_choice_logs`
--

LOCK TABLES `fulfillment_choice_logs` WRITE;
/*!40000 ALTER TABLE `fulfillment_choice_logs` DISABLE KEYS */;
INSERT INTO `fulfillment_choice_logs` VALUES ('fcl_28281000','ord_821c1000','mch_demo002','merchant','mch_demo002','choose','pending_choice','merchant_self',NULL,'dlv_aa681000','2026-09-11 16:52:14','2026-09-11 16:52:14','2026-09-11 16:52:14'),('fcl_35511b52de1000','ord_000007','mch_demo001','system','system','timeout','pending_choice','courier_hall','超过履约选择时限，默认平台配送','dlv_35511b520e1000','2026-08-18 12:43:26','2026-08-18 12:43:26','2026-08-18 12:43:26'),('fcl_35511b53e41000','ord_000009','mch_demo002','system','system','timeout','pending_choice','courier_hall','超过履约选择时限，默认平台配送','dlv_35511b53901000','2026-08-18 12:43:26','2026-08-18 12:43:26','2026-08-18 12:43:26'),('fcl_35511b54681000','ord_000037','mch_demo001','system','system','timeout','pending_choice','courier_hall','超过履约选择时限，默认平台配送','dlv_35511b54241000','2026-08-18 12:43:26','2026-08-18 12:43:26','2026-08-18 12:43:26'),('fcl_35511b55261000','ord_000042','mch_demo001','system','system','timeout','pending_choice','courier_hall','超过履约选择时限，默认平台配送','dlv_35511b54d61000','2026-08-18 12:43:26','2026-08-18 12:43:26','2026-08-18 12:43:26'),('fcl_35511b55b01000','ord_000047','mch_demo002','system','system','timeout','pending_choice','courier_hall','超过履约选择时限，默认平台配送','dlv_35511b556e1000','2026-08-18 12:43:26','2026-08-18 12:43:26','2026-08-18 12:43:26'),('fcl_35511b563e1000','ord_000054','mch_demo001','system','system','timeout','pending_choice','courier_hall','超过履约选择时限，默认平台配送','dlv_35511b56041000','2026-08-18 12:43:26','2026-08-18 12:43:26','2026-08-18 12:43:26'),('fcl_35511b56bc1000','ord_000063','mch_demo001','system','system','timeout','pending_choice','courier_hall','超过履约选择时限，默认平台配送','dlv_35511b568e1000','2026-08-18 12:43:26','2026-08-18 12:43:26','2026-08-18 12:43:26'),('fcl_35511b576a1000','ord_000078','mch_demo002','system','system','timeout','pending_choice','courier_hall','超过履约选择时限，默认平台配送','dlv_35511b572a1000','2026-08-18 12:43:27','2026-08-18 12:43:27','2026-08-18 12:43:27'),('fcl_35511b57e01000','ord_000081','mch_demo002','system','system','timeout','pending_choice','courier_hall','超过履约选择时限，默认平台配送','dlv_35511b57b41000','2026-08-18 12:43:27','2026-08-18 12:43:27','2026-08-18 12:43:27'),('fcl_35511b584c1000','ord_000084','mch_demo002','system','system','timeout','pending_choice','courier_hall','超过履约选择时限，默认平台配送','dlv_35511b581a1000','2026-08-18 12:43:27','2026-08-18 12:43:27','2026-08-18 12:43:27'),('fcl_35511b58bc1000','ord_000094','mch_demo001','system','system','timeout','pending_choice','courier_hall','超过履约选择时限，默认平台配送','dlv_35511b58941000','2026-08-18 12:43:27','2026-08-18 12:43:27','2026-08-18 12:43:27'),('fcl_35511b591c1000','ord_000109','mch_demo002','system','system','timeout','pending_choice','courier_hall','超过履约选择时限，默认平台配送','dlv_35511b58f81000','2026-08-18 12:43:27','2026-08-18 12:43:27','2026-08-18 12:43:27'),('fcl_35511b59ae1000','ord_000117','mch_demo002','system','system','timeout','pending_choice','courier_hall','超过履约选择时限，默认平台配送','dlv_35511b59801000','2026-08-18 12:43:27','2026-08-18 12:43:27','2026-08-18 12:43:27'),('fcl_355140d6041000','ord_35513f81481000','mch_demo002','merchant','res_mch03','choose','pending_choice','merchant_self','商家选择履约方式','dlv_355140d5241000','2026-08-18 13:03:55','2026-08-18 13:03:55','2026-08-18 13:03:55'),('fcl_355222cbec1000','ord_3552221f381000','mch_demo002','merchant','res_mch03','choose','pending_choice','merchant_self','商家选择履约方式','dlv_355222cb781000','2026-08-18 15:07:19','2026-08-18 15:07:19','2026-08-18 15:07:19'),('fcl_3552582d5a1000','ord_355220b31e1000','mch_demo002','system','system','timeout','pending_choice','courier_hall','超过履约选择时限，默认平台配送','dlv_3552582d2a1000','2026-08-18 15:36:29','2026-08-18 15:36:29','2026-08-18 15:36:29'),('fcl_358532816c1000','ord_358531aa921000','mch_demo002','merchant','res_mch03','choose','pending_choice','courier_hall','商家选择履约方式','dlv_35853280ea1000','2026-08-23 14:06:13','2026-08-23 14:06:13','2026-08-23 14:06:13'),('fcl_358534c51c1000','ord_358533a5ce1000','mch_demo002','merchant','res_mch03','choose','pending_choice','courier_hall','商家选择履约方式','dlv_358534c4ec1000','2026-08-23 14:07:27','2026-08-23 14:07:27','2026-08-23 14:07:27'),('fcl_358540c4261000','ord_35854036621000','mch_demo002','merchant','res_mch03','choose','pending_choice','courier_hall','商家选择履约方式','dlv_358540c3f41000','2026-08-23 14:14:00','2026-08-23 14:14:00','2026-08-23 14:14:00'),('fcl_35860b0d341000','ord_358609a9a81000','mch_demo002','merchant','res_mch03','choose','pending_choice','courier_hall','商家选择履约方式','dlv_35860b0d101000','2026-08-23 16:04:29','2026-08-23 16:04:29','2026-08-23 16:04:29'),('fcl_35862fef661000','ord_358628e0fa1000','mch_demo002','merchant','res_mch03','choose','pending_choice','merchant_self','商家选择履约方式','dlv_35862fee8c1000','2026-08-23 16:24:38','2026-08-23 16:24:38','2026-08-23 16:24:38'),('fcl_358630df521000','ord_358630648a1000','mch_demo002','merchant','res_mch03','choose','pending_choice','courier_hall','商家选择履约方式','dlv_358630dec41000','2026-08-23 16:25:08','2026-08-23 16:25:08','2026-08-23 16:25:08'),('fcl_36cd709c561000','ord_36cd384b201000','mch_demo002','system','system','timeout','pending_choice','courier_hall','超过履约选择时限，默认平台配送','dlv_36cd38e3b21000','2026-09-24 10:57:52','2026-09-24 10:57:52','2026-09-24 10:57:52');
/*!40000 ALTER TABLE `fulfillment_choice_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `individual_leader_applications`
--

DROP TABLE IF EXISTS `individual_leader_applications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `individual_leader_applications` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `resident_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申请人住户ID',
  `property_company_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物业公司ID',
  `sector` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申请板块',
  `remark` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '自我介绍/备注',
  `audit_status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT 'pending/approved/rejected',
  `reject_reason` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '拒绝原因',
  `individual_leader_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核通过后创建的个体负责人ID',
  `audited_at` datetime DEFAULT NULL COMMENT '审核时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ila_resident` (`resident_id`),
  KEY `idx_ila_property` (`property_company_id`),
  KEY `idx_ila_status` (`audit_status`),
  KEY `idx_ila_property_status` (`property_company_id`,`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='个体负责人申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `individual_leader_applications`
--

LOCK TABLES `individual_leader_applications` WRITE;
/*!40000 ALTER TABLE `individual_leader_applications` DISABLE KEYS */;
INSERT INTO `individual_leader_applications` VALUES ('ila_290c1000','res_dev001','pc_demo001','cleaning',NULL,'rejected','1',NULL,'2026-09-08 14:43:08','2026-09-08 14:42:54','2026-09-08 14:43:08'),('ila_3571aeb0a41000','res_dev001','pc_demo001','repair',NULL,'rejected','不通过',NULL,'2026-08-22 10:35:54','2026-08-21 16:37:50','2026-08-22 10:35:54'),('ila_357965ffd61000','res_dev001','pc_demo001','repair',NULL,'rejected','不通过',NULL,'2026-08-22 10:37:01','2026-08-22 10:36:37','2026-08-22 10:37:01'),('ila_35798c29981000','res_dev001','pc_demo001','repair',NULL,'rejected','不通过',NULL,'2026-08-23 10:58:11','2026-08-22 10:57:28','2026-08-23 10:58:11');
/*!40000 ALTER TABLE `individual_leader_applications` ENABLE KEYS */;
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
  `sector_leader_id` varchar(32) DEFAULT NULL COMMENT '所属板块负责人（申请审核通过后可先为空，待管理端分配）',
  `sector` varchar(50) NOT NULL COMMENT 'cleaning/repair/security/greening/other（保洁/维修/安保/绿化/其它）',
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
INSERT INTO `individual_leaders` VALUES ('il_001','res_ind01','pc_demo001','sl_001','cleaning','王强家政',0.0000,'active','2026-07-03 08:09:15','2026-07-17 10:29:25'),('il_358471b2b21000','res_001','pc_demo001','sl_001','cleaning','业主-赵六',0.5000,'active','2026-08-23 04:20:55','2026-08-23 04:20:55'),('il_3ef61000','res_3516a17fb01000','pc_demo001','sl_6d461000','cleaning','www',0.0000,'inactive','2026-09-07 14:21:32','2026-09-08 06:58:31'),('il_c6f61000','res_35890655f01000','pc_demo001','sl_6d461000','security','微微',0.0000,'active','2026-09-09 01:04:14','2026-09-09 01:04:14');
/*!40000 ALTER TABLE `individual_leaders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inquiries`
--

DROP TABLE IF EXISTS `inquiries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inquiries` (
  `id` varchar(32) NOT NULL COMMENT 'inq_xxxxxxxx',
  `property_company_id` varchar(32) NOT NULL COMMENT '所属物业公司',
  `inquirer_id` varchar(32) NOT NULL COMMENT '询价人ID（业主）',
  `merchant_id` varchar(32) NOT NULL COMMENT '被询价商家ID',
  `product_id` varchar(32) DEFAULT NULL COMMENT '关联商品ID（可为空）',
  `product_name` varchar(100) DEFAULT NULL COMMENT '商品名称',
  `content` text NOT NULL COMMENT '询价内容',
  `quoted_price` decimal(10,2) DEFAULT NULL COMMENT '商家报价',
  `quoted_remark` varchar(500) DEFAULT NULL COMMENT '报价备注',
  `status` varchar(20) DEFAULT 'pending' COMMENT 'pending/quoted/accepted/rejected/closed',
  `quoted_at` timestamp NULL DEFAULT NULL COMMENT '报价时间',
  `closed_at` timestamp NULL DEFAULT NULL COMMENT '关闭时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_inquirer` (`inquirer_id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `inquiries_ibfk_1` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`),
  CONSTRAINT `inquiries_ibfk_2` FOREIGN KEY (`inquirer_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `inquiries_ibfk_3` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='询价记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inquiries`
--

LOCK TABLES `inquiries` WRITE;
/*!40000 ALTER TABLE `inquiries` DISABLE KEYS */;
INSERT INTO `inquiries` VALUES ('inq_3583bd8b4c1000','pc_demo001','res_dev001','mch_demo002','prd_357991e8e01000','优惠券10元','1',0.50,'1','accepted','2026-09-04 02:35:55','2026-09-04 06:33:00','2026-08-23 02:42:32','2026-09-04 06:33:00'),('inq_5ef01000','pc_demo001','res_dev001','mch_demo002','prd_357991e8e01000','优惠券10元','111',NULL,NULL,'pending',NULL,NULL,'2026-09-04 06:54:57','2026-09-04 06:54:57'),('inq_b6c01000','pc_demo001','res_dev001','mch_demo002','prd_357991e8e01000','优惠券10元','1',NULL,NULL,'pending',NULL,NULL,'2026-09-06 02:09:17','2026-09-06 02:09:17'),('inq_cc701000','pc_demo001','res_dev001','mch_demo002','prd_357991e8e01000','优惠券10元','2块',1.00,NULL,'quoted','2026-09-04 06:41:44',NULL,'2026-09-04 06:32:48','2026-09-04 06:41:44');
/*!40000 ALTER TABLE `inquiries` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `merchant_ad_quota`
--

DROP TABLE IF EXISTS `merchant_ad_quota`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `merchant_ad_quota` (
  `id` varchar(32) NOT NULL,
  `merchant_id` varchar(32) NOT NULL,
  `week_start_date` date NOT NULL,
  `free_quota` int NOT NULL DEFAULT '1',
  `purchased_quota` int NOT NULL DEFAULT '0',
  `used_count` int NOT NULL DEFAULT '0',
  `free_used_count` int NOT NULL DEFAULT '0' COMMENT '本周免费额度已用条数（付费广告不占用）',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_week` (`merchant_id`,`week_start_date`),
  CONSTRAINT `merchant_ad_quota_ibfk_1` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `merchant_ad_quota`
--

LOCK TABLES `merchant_ad_quota` WRITE;
/*!40000 ALTER TABLE `merchant_ad_quota` DISABLE KEYS */;
INSERT INTO `merchant_ad_quota` VALUES ('maq_35857aa84e1000','mch_demo002','2026-08-17',1,0,1,1,'2026-08-23 06:45:37','2026-08-23 06:45:37'),('maq_8cfc1000','mch_demo002','2026-08-31',1,0,1,1,'2026-09-05 09:06:17','2026-09-05 09:06:17');
/*!40000 ALTER TABLE `merchant_ad_quota` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `merchant_ads`
--

DROP TABLE IF EXISTS `merchant_ads`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `merchant_ads` (
  `id` varchar(32) NOT NULL,
  `merchant_id` varchar(32) NOT NULL,
  `title` varchar(128) NOT NULL,
  `content` text NOT NULL,
  `image_urls` json DEFAULT NULL,
  `product_id` varchar(32) DEFAULT NULL,
  `recipient_count` int DEFAULT '0',
  `week_start_date` date NOT NULL,
  `duration_days` int DEFAULT NULL COMMENT '广告展示天数',
  `start_date` date DEFAULT NULL COMMENT '投放开始日期',
  `end_date` date DEFAULT NULL COMMENT '投放结束日期',
  `pay_amount` decimal(10,2) DEFAULT NULL COMMENT '支付金额（0=免费额度投放）',
  `payment_method` varchar(10) DEFAULT NULL COMMENT '支付方式: free/coin/point/wechat/mock',
  `target_buildings` json DEFAULT NULL COMMENT '定向广告-目标楼栋',
  `target_gender` varchar(10) DEFAULT NULL COMMENT '定向广告-目标性别: male/female/all',
  `target_age_brackets` json DEFAULT NULL COMMENT '定向广告-目标年龄段',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `merchant_ads_ibfk_1` (`merchant_id`),
  CONSTRAINT `merchant_ads_ibfk_1` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `merchant_ads`
--

LOCK TABLES `merchant_ads` WRITE;
/*!40000 ALTER TABLE `merchant_ads` DISABLE KEYS */;
INSERT INTO `merchant_ads` VALUES ('ma_gen001','mch_demo001','夏日清凉饮品第二件半价','下单即送积分，冰镇饮料配送到家！',NULL,'prd_seed_towel',258,'2026-08-04',12,'2026-08-04','2026-08-16',7.46,'free',NULL,'all',NULL,'2026-07-05 03:55:14','2026-07-05 03:55:14'),('ma_gen002','mch_demo001','生鲜满39减10','新鲜蔬菜肉类社区团购，今日下单明日达。',NULL,'prd_seed_rice',278,'2026-07-03',27,'2026-07-03','2026-07-30',17.83,'free',NULL,'all',NULL,'2026-07-30 06:58:58','2026-07-30 06:58:58'),('ma_gen003','mch_demo001','便民维修服务上线','水电小修、开锁换锁，一键预约师傅上门。',NULL,'prd_seed_cola',147,'2026-07-28',25,'2026-07-28','2026-08-22',0.06,'free',NULL,'all',NULL,'2026-07-18 12:46:44','2026-07-18 12:46:44'),('ma_gen004','mch_demo001','积分兑换专区新品','用积分免费换好物，数量有限先到先得。',NULL,'prd_002',394,'2026-07-21',7,'2026-07-21','2026-07-28',9.53,'free',NULL,'all',NULL,'2026-07-24 06:00:10','2026-07-24 06:00:10'),('ma_gen005','mch_demo001','新店开业大促','首单立减，积分还能翻倍赚，邻居专享福利！',NULL,'prd_seed_liquid',247,'2026-07-13',9,'2026-07-13','2026-07-22',19.05,'free',NULL,'all',NULL,'2026-07-07 05:32:54','2026-07-07 05:32:54'),('ma_gen006','mch_demo002','夏日清凉饮品第二件半价','下单即送积分，冰镇饮料配送到家！',NULL,'prd_007',422,'2026-07-31',20,'2026-07-31','2026-08-20',0.38,'free',NULL,'all',NULL,'2026-07-20 06:53:00','2026-07-20 06:53:00'),('ma_gen007','mch_demo002','生鲜满39减10','新鲜蔬菜肉类社区团购，今日下单明日达。',NULL,'prd_006',98,'2026-07-16',13,'2026-07-16','2026-07-29',5.45,'coin',NULL,'all',NULL,'2026-07-30 16:48:57','2026-07-30 16:48:57'),('ma_gen008','mch_demo002','便民维修服务上线','水电小修、开锁换锁，一键预约师傅上门。',NULL,'prd_009',237,'2026-07-06',21,'2026-07-06','2026-07-27',2.37,'free',NULL,'all',NULL,'2026-07-27 12:26:39','2026-07-27 12:26:39'),('ma_gen009','mch_demo002','积分兑换专区新品','用积分免费换好物，数量有限先到先得。',NULL,'prd_006',468,'2026-07-25',8,'2026-07-25','2026-08-02',5.02,'free',NULL,'all',NULL,'2026-07-16 04:59:23','2026-07-16 04:59:23'),('ma_gen010','mch_demo002','新店开业大促','首单立减，积分还能翻倍赚，邻居专享福利！',NULL,'prd_006',280,'2026-07-07',10,'2026-07-07','2026-07-17',9.51,'free',NULL,'all',NULL,'2026-07-31 06:42:42','2026-07-31 06:42:42'),('mad_35857aa85a1000','mch_demo002','广告','广告',NULL,NULL,24,'2026-08-17',7,'2026-08-23','2026-08-29',0.00,'free',NULL,NULL,NULL,'2026-08-23 06:45:37','2026-08-23 06:45:37'),('mad_8d241000','mch_demo002','广告','1',NULL,NULL,27,'2026-08-31',4,'2026-09-05','2026-09-08',0.00,'free',NULL,NULL,NULL,'2026-09-05 09:06:17','2026-09-05 09:06:17');
/*!40000 ALTER TABLE `merchant_ads` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `merchant_applyments`
--

DROP TABLE IF EXISTS `merchant_applyments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `merchant_applyments` (
  `id` varchar(32) NOT NULL COMMENT '主键 ma_',
  `merchant_id` varchar(32) NOT NULL COMMENT '商家ID mch_（merchants.id）',
  `applyment_id` varchar(64) DEFAULT NULL COMMENT '微信进件申请单号（applyments 接口返回）',
  `applyment_state` varchar(32) NOT NULL DEFAULT 'init' COMMENT '本地状态机：init/submitted/auditing/legal_validating/signing/finished/rejected',
  `wx_state` varchar(64) DEFAULT NULL COMMENT '微信侧申请状态原值',
  `organization_type` varchar(20) DEFAULT NULL COMMENT '主体类型：ENTERPRISE/INDIVIDUAL/MICRO(小微)/GOVERNMENT',
  `merchant_shortname` varchar(64) DEFAULT NULL COMMENT '商户简称（进件必填，对外显示）',
  `merchant_category` varchar(64) DEFAULT NULL COMMENT '经营类目（微信类目，占位，联调对齐）',
  `contact_name` varchar(64) DEFAULT NULL COMMENT '联系人姓名',
  `contact_mobile` varchar(32) DEFAULT NULL COMMENT '联系人手机号',
  `contact_email` varchar(128) DEFAULT NULL COMMENT '联系人邮箱',
  `id_card_name_enc` varchar(512) DEFAULT NULL COMMENT '法人/经营者姓名（RSA加密）',
  `id_card_number_enc` varchar(1024) DEFAULT NULL COMMENT '身份证号（RSA加密）',
  `id_card_copy_media` varchar(128) DEFAULT NULL COMMENT '身份证人像面 media_id',
  `id_card_national_media` varchar(128) DEFAULT NULL COMMENT '身份证国徽面 media_id',
  `business_license_media` varchar(128) DEFAULT NULL COMMENT '营业执照 media_id（小微可空）',
  `bank_account_type` varchar(20) DEFAULT NULL COMMENT '结算账户类型：BANK_ACCOUNT_TYPE_CORPORATE(对公)/BANK_ACCOUNT_TYPE_PERSONAL(个人)',
  `bank_account_name_enc` varchar(512) DEFAULT NULL COMMENT '开户名（RSA加密）',
  `bank_account_number_enc` varchar(1024) DEFAULT NULL COMMENT '银行账号（RSA加密）',
  `bank_name` varchar(128) DEFAULT NULL COMMENT '开户银行全称（含支行）',
  `legal_validation_url` varchar(1024) DEFAULT NULL COMMENT '法人验证链接（审核通过后返回，有时效）',
  `sign_url` varchar(1024) DEFAULT NULL COMMENT '签约链接（法人验证通过后返回）',
  `reject_reason` varchar(512) DEFAULT NULL COMMENT '微信驳回原因',
  `sub_mchid` varchar(32) DEFAULT NULL COMMENT '进件成功后微信分配的二级商户号（回填 merchant_wechat_accounts）',
  `submit_mode` varchar(16) NOT NULL DEFAULT 'manual' COMMENT '提交方式：manual(运营后台)/auto(审核通过自动)',
  `retry_count` int NOT NULL DEFAULT '0' COMMENT '状态推进重试次数',
  `last_error` varchar(500) DEFAULT NULL COMMENT '最近一次查询/提交错误',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant` (`merchant_id`),
  KEY `idx_applyment_state` (`applyment_state`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家微信进件申请单（平台收付通 applyments）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `merchant_applyments`
--

LOCK TABLES `merchant_applyments` WRITE;
/*!40000 ALTER TABLE `merchant_applyments` DISABLE KEYS */;
/*!40000 ALTER TABLE `merchant_applyments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `merchant_category_dict`
--

DROP TABLE IF EXISTS `merchant_category_dict`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `merchant_category_dict` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称（中文）',
  `sort_order` int DEFAULT '0' COMMENT '排序权重（升序）',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'active' COMMENT 'active/inactive',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mcd_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家分类字典';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `merchant_category_dict`
--

LOCK TABLES `merchant_category_dict` WRITE;
/*!40000 ALTER TABLE `merchant_category_dict` DISABLE KEYS */;
INSERT INTO `merchant_category_dict` VALUES ('mcd_001','外卖',1,'active','2026-08-21 12:54:24','2026-08-21 12:54:24'),('mcd_002','周围商家',2,'active','2026-08-21 12:54:24','2026-08-21 12:54:24'),('mcd_003','自营',3,'active','2026-08-21 12:54:24','2026-08-21 12:54:24'),('mcd_004','农家',4,'active','2026-08-21 12:54:24','2026-08-21 12:54:24'),('mcd_005','外卖小吃',5,'active','2026-08-21 12:54:24','2026-08-21 12:54:24'),('mcd_006','团购',6,'active','2026-08-21 12:54:24','2026-08-21 12:54:24'),('mcd_007','酒店民宿',7,'active','2026-08-21 12:54:24','2026-08-21 12:54:24'),('mcd_008','洗浴汗蒸',8,'active','2026-08-21 12:54:24','2026-08-21 12:54:24'),('mcd_009','按摩足疗',9,'active','2026-08-21 12:54:24','2026-08-21 12:54:24'),('mcd_010','美食',10,'active','2026-08-21 12:54:24','2026-08-21 12:54:24'),('mcd_011','外卖小区',11,'active','2026-08-21 12:54:24','2026-08-21 12:54:24'),('mcd_012','看病买药',12,'active','2026-08-21 14:10:29','2026-08-21 14:10:29'),('mcd_013','休闲玩乐',13,'active','2026-08-21 14:10:29','2026-08-21 14:10:29');
/*!40000 ALTER TABLE `merchant_category_dict` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `merchant_community_distances`
--

DROP TABLE IF EXISTS `merchant_community_distances`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `merchant_community_distances` (
  `id` varchar(32) NOT NULL COMMENT '主键 mcd_xxx',
  `merchant_id` varchar(32) NOT NULL COMMENT '商家ID mch_xxx',
  `community_id` varchar(32) NOT NULL COMMENT '小区ID com_xxx',
  `property_company_id` varchar(32) NOT NULL COMMENT '物业公司ID pc_xxx（冗余，便于按物业检索）',
  `distance_km` decimal(10,2) NOT NULL COMMENT '商家距该小区的公里数（管理端配置）',
  `operator_id` varchar(32) DEFAULT NULL COMMENT '最后配置人ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_community` (`merchant_id`,`community_id`),
  KEY `idx_community_id` (`community_id`),
  KEY `idx_property_company_id` (`property_company_id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家-小区距离配置表（管理端配置，商品/商家列表展示「距您小区x.xkm」）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `merchant_community_distances`
--

LOCK TABLES `merchant_community_distances` WRITE;
/*!40000 ALTER TABLE `merchant_community_distances` DISABLE KEYS */;
INSERT INTO `merchant_community_distances` VALUES ('mcd_354b6073361000','mch_demo002','com_demo001','pc_demo001',1.00,'res_propadmin01','2026-08-17 23:22:19','2026-08-17 23:22:19'),('mcd_354b6073461000','mch_demo002','com_demo002','pc_demo001',2.00,'res_propadmin01','2026-08-17 23:22:19','2026-08-17 23:22:19');
/*!40000 ALTER TABLE `merchant_community_distances` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `merchant_keywords`
--

DROP TABLE IF EXISTS `merchant_keywords`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `merchant_keywords` (
  `id` varchar(32) NOT NULL COMMENT 'mk_xxxxxxxx',
  `merchant_id` varchar(32) NOT NULL,
  `keyword` varchar(100) NOT NULL,
  `weight` int DEFAULT '0' COMMENT '权重，越大越优先匹配',
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_keyword` (`merchant_id`,`keyword`),
  KEY `idx_keyword` (`keyword`),
  CONSTRAINT `fk_mk_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家搜索关键词';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `merchant_keywords`
--

LOCK TABLES `merchant_keywords` WRITE;
/*!40000 ALTER TABLE `merchant_keywords` DISABLE KEYS */;
INSERT INTO `merchant_keywords` VALUES ('mk_261c1000','mch_demo002','便民',10,'active','2026-09-04 06:40:38','2026-09-04 06:40:44'),('mk_gen0001','mch_demo001','早餐',0,'active','2026-07-27 17:35:13','2026-07-27 17:35:13'),('mk_gen0002','mch_demo001','外卖',0,'active','2026-07-30 03:22:39','2026-07-30 03:22:39'),('mk_gen0003','mch_demo001','生鲜',0,'active','2026-07-20 12:44:48','2026-07-20 12:44:48'),('mk_gen0004','mch_demo001','日用品',0,'active','2026-07-30 02:48:33','2026-07-30 02:48:33'),('mk_gen0005','mch_demo001','便民',0,'active','2026-07-21 08:04:15','2026-07-21 08:04:15'),('mk_gen0006','mch_demo001','社区团购',0,'active','2026-07-05 08:33:11','2026-07-05 08:33:11'),('mk_gen0007','mch_demo002','早餐',20,'active','2026-07-31 09:12:24','2026-09-04 06:40:53'),('mk_gen0008','mch_demo002','外卖',10,'active','2026-07-12 15:20:16','2026-09-04 06:40:47'),('mk_gen0009','mch_demo002','生鲜',2,'active','2026-07-06 19:09:10','2026-09-04 06:40:57'),('mk_gen0010','mch_demo002','日用品',1,'active','2026-07-02 18:48:04','2026-09-04 06:40:51'),('mk_gen0012','mch_demo002','社区团购',1,'active','2026-07-23 21:48:03','2026-09-04 06:40:59');
/*!40000 ALTER TABLE `merchant_keywords` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `merchant_message_services`
--

DROP TABLE IF EXISTS `merchant_message_services`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `merchant_message_services` (
  `id` varchar(32) NOT NULL,
  `merchant_id` varchar(32) NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT '0',
  `service_radius` varchar(20) DEFAULT NULL,
  `category_ids` json DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant` (`merchant_id`),
  CONSTRAINT `merchant_message_services_ibfk_1` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `merchant_message_services`
--

LOCK TABLES `merchant_message_services` WRITE;
/*!40000 ALTER TABLE `merchant_message_services` DISABLE KEYS */;
INSERT INTO `merchant_message_services` VALUES ('mms_2f241000','mch_demo002',0,'5km','[\"scd_02\"]','2026-09-05 08:58:26','2026-09-05 09:04:58');
/*!40000 ALTER TABLE `merchant_message_services` ENABLE KEYS */;
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
INSERT INTO `merchant_point_purchases` VALUES ('mpp_353bd6a4921000','mch_demo002','pc_demo001',10,0.05,'approved','2026-08-17 08:31:38','res_admin01',10,'2026-08-16 03:09:55','2026-08-17 08:31:38'),('mpp_35daf789921000','mch_demo002','pc_demo001',20,0.10,'pending_audit',NULL,NULL,0,'2026-08-31 13:57:41','2026-08-31 13:57:41'),('mpp_35daf7bbe21000','mch_demo002','pc_demo001',88,0.44,'pending_audit',NULL,NULL,0,'2026-08-31 13:57:48','2026-08-31 13:57:48');
/*!40000 ALTER TABLE `merchant_point_purchases` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `merchant_posts`
--

DROP TABLE IF EXISTS `merchant_posts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `merchant_posts` (
  `id` varchar(32) NOT NULL COMMENT 'mp_xxxxxxxx',
  `merchant_id` varchar(32) NOT NULL,
  `title` varchar(200) DEFAULT NULL,
  `content` text,
  `image_urls` json DEFAULT NULL,
  `video_url` varchar(500) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'published' COMMENT 'draft/published/archived',
  `view_count` int DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  CONSTRAINT `fk_mp_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家动态';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `merchant_posts`
--

LOCK TABLES `merchant_posts` WRITE;
/*!40000 ALTER TABLE `merchant_posts` DISABLE KEYS */;
INSERT INTO `merchant_posts` VALUES ('mp_2ad21000','mch_35c14bfbe61000','99','99','[]',NULL,'published',0,'2026-09-09 03:31:22','2026-09-09 03:31:22'),('mp_358579fff81000','mch_demo002','促销','促销1','[]',NULL,'published',2,'2026-08-23 06:45:16','2026-09-09 05:46:28'),('mp_91461000','mch_35c14661b81000','1','1','[]',NULL,'published',0,'2026-09-09 03:22:18','2026-09-09 03:22:18');
/*!40000 ALTER TABLE `merchant_posts` ENABLE KEYS */;
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
INSERT INTO `merchant_property_configs` VALUES ('mpc_001','mch_demo001','pc_demo001',0.1500,100.0000,NULL,0.0200,1,'active','2026-07-07 05:10:48','2026-08-13 13:04:31'),('mpc_002','mch_demo002','pc_demo001',0.1000,200.0000,NULL,0.0100,1,'active','2026-07-07 05:10:48','2026-08-13 13:04:31'),('mpc_355b51167c1000','mch_355b51166c1000','pc_demo001',0.1000,100.0000,NULL,0.0000,0,'active','2026-08-19 04:30:54','2026-08-19 04:30:54'),('mpc_355d08ea721000','mch_355d08ea621000','pc_demo001',0.1000,100.0000,NULL,0.0000,0,'active','2026-08-19 08:31:06','2026-08-19 08:31:06'),('mpc_355d11a1f41000','mch_355d11a1f21000','pc_demo001',0.1000,100.0000,NULL,0.0000,0,'active','2026-08-19 08:35:52','2026-08-19 08:35:52'),('mpc_35c14661d21000','mch_35c14661b81000','pc_demo001',0.1000,100.0000,NULL,0.0000,0,'active','2026-08-29 02:05:41','2026-08-29 02:05:41'),('mpc_35c14bfbe81000','mch_35c14bfbe61000','pc_demo001',0.1000,100.0000,NULL,0.0000,0,'active','2026-08-29 02:08:45','2026-08-29 02:08:45'),('mpc_fix_act01','mch_act01','pc_demo001',0.1000,100.0000,NULL,0.0000,0,'active','2026-09-06 14:23:20','2026-09-06 14:23:20'),('mpc_fix_mch01','mch_mch01','pc_demo001',0.1000,100.0000,NULL,0.0000,0,'active','2026-09-06 14:23:20','2026-09-06 14:23:20'),('mpc_fix_tech01','mch_tech01','pc_demo001',0.1000,100.0000,NULL,0.0000,0,'active','2026-09-06 14:23:20','2026-09-06 14:23:20');
/*!40000 ALTER TABLE `merchant_property_configs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `merchant_service_communities`
--

DROP TABLE IF EXISTS `merchant_service_communities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `merchant_service_communities` (
  `id` varchar(32) NOT NULL,
  `merchant_id` varchar(32) NOT NULL,
  `community_id` varchar(32) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_community` (`merchant_id`,`community_id`),
  KEY `merchant_service_communities_ibfk_2` (`community_id`),
  CONSTRAINT `merchant_service_communities_ibfk_1` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`) ON DELETE CASCADE,
  CONSTRAINT `merchant_service_communities_ibfk_2` FOREIGN KEY (`community_id`) REFERENCES `communities` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `merchant_service_communities`
--

LOCK TABLES `merchant_service_communities` WRITE;
/*!40000 ALTER TABLE `merchant_service_communities` DISABLE KEYS */;
/*!40000 ALTER TABLE `merchant_service_communities` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `merchant_transfer_accounts`
--

DROP TABLE IF EXISTS `merchant_transfer_accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `merchant_transfer_accounts` (
  `id` varchar(32) NOT NULL COMMENT '主键 mta_',
  `owner_type` varchar(32) NOT NULL COMMENT '角色：PROPERTY/COORDINATOR/SECTOR_LEADER/INDIVIDUAL_LEADER',
  `owner_id` varchar(32) NOT NULL COMMENT '角色ID（物业公司/统筹/板块/个体负责人ID）',
  `channel` varchar(16) NOT NULL DEFAULT 'zero' COMMENT '收款渠道：zero(微信零钱)/bank(银行卡)',
  `openid` varchar(64) DEFAULT NULL COMMENT '微信 openid（zero 渠道）',
  `bank_account_no_enc` varchar(1024) DEFAULT NULL COMMENT '银行卡号（RSA加密，bank 渠道）',
  `bank_name` varchar(128) DEFAULT NULL COMMENT '开户银行全称（bank 渠道）',
  `account_name_enc` varchar(512) DEFAULT NULL COMMENT '持卡人姓名（RSA加密，bank 渠道）',
  `cert_serial` varchar(64) DEFAULT NULL COMMENT '加密所用微信平台证书序列号（证书轮换后需重新绑定）',
  `verified` tinyint NOT NULL DEFAULT '0' COMMENT '是否已核验可用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_owner` (`owner_type`,`owner_id`,`channel`),
  KEY `idx_verified` (`verified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='四层树角色商家转账收款账号';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `merchant_transfer_accounts`
--

LOCK TABLES `merchant_transfer_accounts` WRITE;
/*!40000 ALTER TABLE `merchant_transfer_accounts` DISABLE KEYS */;
/*!40000 ALTER TABLE `merchant_transfer_accounts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `merchant_transfers`
--

DROP TABLE IF EXISTS `merchant_transfers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `merchant_transfers` (
  `id` varchar(32) NOT NULL COMMENT '主键 mt_',
  `owner_type` varchar(32) NOT NULL COMMENT '角色：PROPERTY/COORDINATOR/SECTOR_LEADER/INDIVIDUAL_LEADER',
  `owner_id` varchar(32) NOT NULL COMMENT '角色ID',
  `biz_id` varchar(64) DEFAULT NULL COMMENT '业务ID（订单号/分账 group_no）',
  `amount` decimal(10,2) NOT NULL COMMENT '转账金额（元）',
  `channel` varchar(16) NOT NULL DEFAULT 'zero' COMMENT '转账渠道：zero/bank',
  `payee_account` varchar(64) DEFAULT NULL COMMENT '收款账号（openid 或脱敏银行卡，冗余展示）',
  `status` varchar(24) NOT NULL DEFAULT 'pending' COMMENT 'pending/processing/success/failed/closed',
  `wx_batch_id` varchar(64) DEFAULT NULL COMMENT '微信转账批次号 batch_id',
  `wx_transfer_bill_id` varchar(64) DEFAULT NULL COMMENT '微信转账单号 transfer_bill_id',
  `fail_reason` varchar(500) DEFAULT NULL COMMENT '失败原因',
  `retry_count` int NOT NULL DEFAULT '0' COMMENT '重试次数',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_owner` (`owner_type`,`owner_id`),
  KEY `idx_status` (`status`),
  KEY `idx_biz` (`biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='四层树角色内部结算转账单（商家转账）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `merchant_transfers`
--

LOCK TABLES `merchant_transfers` WRITE;
/*!40000 ALTER TABLE `merchant_transfers` DISABLE KEYS */;
/*!40000 ALTER TABLE `merchant_transfers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `merchant_wechat_accounts`
--

DROP TABLE IF EXISTS `merchant_wechat_accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `merchant_wechat_accounts` (
  `id` varchar(32) NOT NULL COMMENT '主键 mwa_',
  `merchant_id` varchar(32) NOT NULL COMMENT '商家ID mch_',
  `sub_mchid` varchar(32) NOT NULL COMMENT '微信二级商户号（电商收付通进件后分配）',
  `merchant_name` varchar(128) DEFAULT NULL COMMENT '二级商户主体名称（营业执照/身份证姓名）',
  `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT 'unbound/auditing/active/frozen',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_id` (`merchant_id`),
  UNIQUE KEY `uk_sub_mchid` (`sub_mchid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商户微信二级商户账号（电商收付通）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `merchant_wechat_accounts`
--

LOCK TABLES `merchant_wechat_accounts` WRITE;
/*!40000 ALTER TABLE `merchant_wechat_accounts` DISABLE KEYS */;
/*!40000 ALTER TABLE `merchant_wechat_accounts` ENABLE KEYS */;
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
  `platform_fee_share` decimal(10,2) DEFAULT '0.00' COMMENT '平台管理员从提现手续费中的分成',
  `actual_amount` decimal(10,2) NOT NULL COMMENT '实际到账',
  `audit_status` varchar(20) DEFAULT 'pending' COMMENT 'pending/approved/rejected/completed',
  `audited_at` timestamp NULL DEFAULT NULL,
  `auditor_id` varchar(32) DEFAULT NULL,
  `completed_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `disburse_channel` varchar(20) DEFAULT NULL COMMENT '打款渠道: manual(人工)/auto_cbk(扫呗自动)',
  `disburse_trade_no` varchar(64) DEFAULT NULL COMMENT '扫呗打款流水号(trade_no)',
  `disburse_status` varchar(20) DEFAULT NULL COMMENT '打款状态: pending/success/failed',
  `disburse_at` datetime DEFAULT NULL COMMENT '打款完成时间',
  `disburse_remark` varchar(255) DEFAULT NULL COMMENT '打款失败原因/备注',
  `transfer_proof` varchar(512) DEFAULT NULL COMMENT '人工转账凭证URL',
  PRIMARY KEY (`id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_disburse_status` (`disburse_status`),
  CONSTRAINT `merchant_withdrawals_ibfk_1` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`),
  CONSTRAINT `merchant_withdrawals_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家提现表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `merchant_withdrawals`
--

LOCK TABLES `merchant_withdrawals` WRITE;
/*!40000 ALTER TABLE `merchant_withdrawals` DISABLE KEYS */;
INSERT INTO `merchant_withdrawals` VALUES ('mw_3520ebdbea1000','mch_tech01','pc_demo001',0.01,0.0050,0.00,0.00,0.01,'approved','2026-08-13 12:27:10','res_admin01',NULL,'2026-08-13 12:26:37','2026-08-13 12:27:10',NULL,NULL,NULL,NULL,NULL,NULL),('mw_352671255a1000','mch_demo002','pc_demo001',0.01,0.0050,0.00,0.00,0.01,'pending_audit',NULL,NULL,NULL,'2026-08-14 01:18:28','2026-08-14 01:18:28',NULL,NULL,NULL,NULL,NULL,NULL),('mw_35319503421000','mch_demo002','pc_demo001',0.01,0.0050,0.00,0.00,0.01,'pending_audit',NULL,NULL,NULL,'2026-08-15 03:15:58','2026-08-15 03:15:58',NULL,NULL,NULL,NULL,NULL,NULL);
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
  `is_official_recommended` tinyint(1) DEFAULT '0' COMMENT '是否官方推荐',
  `recommended_sort` int DEFAULT '0' COMMENT '推荐排序',
  `withdrawal_blocked` tinyint(1) DEFAULT '0' COMMENT '是否被手动阻止提现',
  `business_hours` varchar(50) DEFAULT NULL,
  `contact_phone` varchar(20) DEFAULT NULL,
  `resident_id` varchar(32) DEFAULT NULL COMMENT '店主住户ID（res_xxx，商家登录/聊天对端账号）',
  `address` varchar(200) DEFAULT NULL,
  `delivery_fee` decimal(10,2) DEFAULT '0.00' COMMENT '商家设定的配送费',
  `free_delivery_threshold` decimal(10,2) DEFAULT NULL COMMENT '满额免配送费',
  `free_delivery_sponsor` varchar(20) DEFAULT 'merchant' COMMENT '满额减免承担方 none/merchant/property/platform',
  `free_delivery_enabled` tinyint(1) DEFAULT NULL COMMENT '是否启用满额减免',
  `delivery_scope` varchar(30) DEFAULT 'both' COMMENT '配送范围: community_inside / community_outside / both',
  `delivery_fee_tiers` json DEFAULT NULL COMMENT '多档配送费配置：[{minKm,maxKm,fee,enabled}]',
  `status` varchar(20) DEFAULT 'active',
  `merchant_source` varchar(20) DEFAULT 'platform' COMMENT '商家来源：platform/group_leader/technician',
  `merchant_type` varchar(20) NOT NULL DEFAULT 'goods' COMMENT '商家角色类型：goods商品商家 / technician技工商家 / group_leader组长商家 / canteen社区食堂商家',
  `apply_role` varchar(20) DEFAULT 'merchant' COMMENT '入驻申请身份 merchant/technician/activity_leader',
  `is_canteen_main_merchant` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否社区食堂主商家（餐饮公司，可购买额度）',
  `latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度',
  `serve_all_communities` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否服务本公司下全部小区',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `default_fulfillment_mode` varchar(20) DEFAULT NULL COMMENT '商家默认履约方式 courier_hall/merchant_self/unspecified',
  `service_communities` json DEFAULT NULL COMMENT '服务小区列表 JSON',
  `service_radius_km` decimal(6,2) DEFAULT NULL COMMENT '服务半径 km',
  `qualifications` json DEFAULT NULL COMMENT '资质材料 JSON',
  `delivery_mode` varchar(20) DEFAULT 'unspecified' COMMENT '配送方式 courier_hall/merchant_self/unspecified',
  `is_recommended` tinyint(1) DEFAULT NULL COMMENT '是否官方推荐',
  `individual_leader_id` varchar(32) DEFAULT NULL COMMENT '归属个体负责人ID',
  `individual_leader_rate` decimal(5,4) DEFAULT NULL COMMENT '个体负责人分成比例(0~1)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_platform_property` (`platform_merchant_id`,`property_company_id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_level` (`merchant_level`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_category` (`category`),
  KEY `idx_rank` (`rank_order`),
  KEY `idx_is_official_recommended` (`is_official_recommended`),
  KEY `idx_merchant_source` (`merchant_source`),
  KEY `idx_resident_id` (`resident_id`),
  KEY `idx_merchants_apply_role` (`apply_role`),
  KEY `idx_merchant_type` (`merchant_type`),
  KEY `idx_merchants_il` (`individual_leader_id`),
  CONSTRAINT `merchants_ibfk_1` FOREIGN KEY (`platform_merchant_id`) REFERENCES `platform_merchants` (`id`),
  CONSTRAINT `merchants_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `merchants`
--

LOCK TABLES `merchants` WRITE;
/*!40000 ALTER TABLE `merchants` DISABLE KEYS */;
INSERT INTO `merchants` VALUES ('mch_355b51166c1000','pm_355b5116541000','pc_demo001','外套的店',NULL,NULL,NULL,NULL,'normal',0,'外卖','approved',0,NULL,NULL,NULL,NULL,'19852681536','res_35217712f21000',NULL,2.00,20.00,'merchant',1,'both',NULL,'kicked','platform','goods','merchant',0,NULL,NULL,1,'2026-08-19 04:30:54','2026-08-19 04:47:39',NULL,NULL,NULL,NULL,'unspecified',NULL,NULL,NULL),('mch_355d08ea621000','pm_355d08ea541000','pc_demo001','外套的店',NULL,NULL,NULL,NULL,'normal',0,'外卖','approved',0,NULL,NULL,NULL,NULL,'19852681536','res_35217712f21000',NULL,0.00,NULL,'merchant',0,'both',NULL,'kicked','platform','goods','merchant',0,NULL,NULL,1,'2026-08-19 08:31:06','2026-08-19 08:34:17',NULL,NULL,NULL,NULL,'unspecified',NULL,NULL,NULL),('mch_355d11a1f21000','pm_355d11a1f01000','pc_demo001','外套的技工',NULL,NULL,NULL,NULL,'normal',0,'技工服务','approved',0,NULL,NULL,NULL,NULL,'19852681536','res_35217712f21000',NULL,0.00,NULL,'merchant',0,'both',NULL,'kicked','technician','technician','technician',0,NULL,NULL,1,'2026-08-19 08:35:52','2026-08-28 10:15:34',NULL,NULL,NULL,NULL,'unspecified',NULL,NULL,NULL),('mch_35c14661b81000','pm_35c14661a61000','pc_demo001','立民社区食堂',NULL,NULL,NULL,NULL,'normal',0,'社区食堂','approved',0,NULL,NULL,NULL,NULL,'13987654321','res_35c14607ae1000',NULL,0.00,NULL,NULL,0,'both',NULL,'active','platform','canteen','merchant',1,NULL,NULL,1,'2026-08-29 02:05:41','2026-08-29 02:10:27',NULL,NULL,NULL,NULL,'unspecified',NULL,NULL,NULL),('mch_35c14bfbe61000','pm_35c14bfbe41000','pc_demo001','民生小面',NULL,NULL,NULL,NULL,'normal',0,'社区食堂','approved',0,NULL,NULL,NULL,NULL,'12987654321','res_35c14b96101000',NULL,0.00,NULL,NULL,0,'both',NULL,'active','platform','canteen','merchant',0,NULL,NULL,1,'2026-08-29 02:08:45','2026-08-29 02:08:45',NULL,NULL,NULL,NULL,'unspecified',NULL,NULL,NULL),('mch_act01','pm_act01','pc_demo001','郑美丽的小店','郑美丽的小店',NULL,NULL,NULL,'normal',0,'周围商家','approved',0,1,0,0,NULL,'13900000011','res_act01',NULL,1.00,10.00,'merchant',1,'out_community','[{\"fee\": 1, \"minKm\": 0, \"enabled\": true}]','active','group_leader','group_leader','merchant',0,NULL,NULL,1,'2026-08-12 08:57:57','2026-09-06 09:31:13',NULL,NULL,NULL,NULL,'unspecified',NULL,NULL,NULL),('mch_demo001','pm_demo001','pc_demo001','口福餐厅','社区餐厅',NULL,NULL,NULL,'property_certified',0,'外卖小吃','approved',4,1,0,0,NULL,'13900000009','res_mch02',NULL,0.00,NULL,'merchant',NULL,'both',NULL,'active','platform','goods','merchant',0,NULL,NULL,1,'2026-07-05 06:24:41','2026-09-01 03:03:24',NULL,NULL,NULL,NULL,'unspecified',NULL,NULL,NULL),('mch_demo002','pm_demo002','pc_demo001','便民超市','社区便利店','[\"/static/default/merchant.png\"]',NULL,NULL,'official_certified',100,'周围商家','approved',5,1,2,0,NULL,'13900000017','res_mch03',NULL,0.10,30.00,'merchant',1,'both','[{\"fee\": 0.11, \"maxKm\": 1.1, \"minKm\": 0, \"enabled\": true}, {\"fee\": 0.2, \"maxKm\": 3, \"minKm\": 1.1, \"enabled\": true}, {\"fee\": 0.3, \"maxKm\": 5, \"minKm\": 3, \"enabled\": true}]','active','platform','goods','merchant',0,NULL,NULL,1,'2026-07-05 06:24:41','2026-09-06 09:32:27',NULL,NULL,NULL,NULL,'unspecified',NULL,NULL,NULL),('mch_mch01','pm_mch01','pc_demo001','陈老板的店','陈老板的店','[\"/static/default/merchant.png\"]',NULL,NULL,'normal',0,'周围商家','approved',1,1,0,0,NULL,'13900000008','res_mch01',NULL,0.10,50.00,'merchant',1,'both',NULL,'kicked','platform','goods','merchant',0,NULL,NULL,1,'2026-08-12 08:40:12','2026-09-01 02:54:20',NULL,NULL,NULL,NULL,'unspecified',NULL,NULL,NULL),('mch_tech01','pm_tech01','pc_demo001','刘师傅的服务','刘师傅的服务',NULL,NULL,NULL,'normal',0,'便民维修','approved',0,1,0,0,NULL,'13900000012','res_tech01',NULL,0.00,NULL,'merchant',NULL,'both',NULL,'active','technician','technician','merchant',0,NULL,NULL,1,'2026-08-12 08:57:57','2026-08-28 10:15:34',NULL,NULL,NULL,NULL,'unspecified',NULL,NULL,NULL);
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
  `sender_identity` varchar(20) DEFAULT 'personal' COMMENT 'personal/official',
  `official_sender_type` varchar(30) DEFAULT NULL COMMENT 'community/property/platform/coordinator',
  `related_task_id` varchar(32) DEFAULT NULL COMMENT '关联定向任务/公告/广告/服务需求ID',
  `content` text NOT NULL,
  `chat_type` varchar(20) DEFAULT NULL COMMENT 'private/group/broadcast',
  `group_id` varchar(32) DEFAULT NULL,
  `read_status` varchar(20) DEFAULT 'unread',
  `property_company_id` varchar(32) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `card_type` varchar(20) DEFAULT NULL COMMENT '卡片类型 product/order',
  `card_id` varchar(32) DEFAULT NULL COMMENT '卡片关联ID（商品ID/订单ID）',
  PRIMARY KEY (`id`),
  KEY `idx_from` (`from_id`),
  KEY `idx_to` (`to_id`),
  KEY `idx_type` (`message_type`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_sender_identity` (`sender_identity`),
  KEY `idx_official_type` (`official_sender_type`),
  KEY `idx_related_task` (`related_task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messages`
--

LOCK TABLES `messages` WRITE;
/*!40000 ALTER TABLE `messages` DISABLE KEYS */;
INSERT INTO `messages` VALUES ('msg_1b341000','res_admin01','res_004','resident','property_message',NULL,'property',NULL,'物业费催缴通知\n您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','private',NULL,'unread','pc_demo001','2026-09-07 14:56:58','2026-09-07 14:56:58',NULL,NULL),('msg_1b561000','res_admin01','res_act01','resident','property_message',NULL,'property',NULL,'物业费催缴通知\n您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','private',NULL,'unread','pc_demo001','2026-09-07 14:56:58','2026-09-07 14:56:58',NULL,NULL),('msg_1b5c1000','res_admin01','res_coo01','resident','property_message',NULL,'property',NULL,'物业费催缴通知\n您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','private',NULL,'unread','pc_demo001','2026-09-07 14:56:58','2026-09-07 14:56:58',NULL,NULL),('msg_1b601000','res_admin01','res_dev001','resident','property_message',NULL,'property',NULL,'物业费催缴通知\n您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','private',NULL,'read','pc_demo001','2026-09-07 14:56:58','2026-09-08 11:49:35',NULL,NULL),('msg_1b621000','res_admin01','res_dev002','resident','property_message',NULL,'property',NULL,'物业费催缴通知\n您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','private',NULL,'unread','pc_demo001','2026-09-07 14:56:58','2026-09-07 14:56:58',NULL,NULL),('msg_1b621001','res_admin01','res_dev003','resident','property_message',NULL,'property',NULL,'物业费催缴通知\n您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','private',NULL,'unread','pc_demo001','2026-09-07 14:56:58','2026-09-07 14:56:58',NULL,NULL),('msg_1b641000','res_admin01','res_ind01','resident','property_message',NULL,'property',NULL,'物业费催缴通知\n您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','private',NULL,'unread','pc_demo001','2026-09-07 14:56:58','2026-09-07 14:56:58',NULL,NULL),('msg_1b661000','res_admin01','res_mch03','resident','property_message',NULL,'property',NULL,'物业费催缴通知\n您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','private',NULL,'unread','pc_demo001','2026-09-07 14:56:58','2026-09-07 14:56:58',NULL,NULL),('msg_1b681000','res_admin01','res_tech01','resident','property_message',NULL,'property',NULL,'物业费催缴通知\n您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','private',NULL,'unread','pc_demo001','2026-09-07 14:56:58','2026-09-07 14:56:58',NULL,NULL),('msg_351ca33aba1000','official_platform_pc_demo001','res_001',NULL,'official_message','official','platform','dmt_351ca33ab21000','欢迎加入邻里商城服务！\n欢迎加入邻里商城服务！','official',NULL,'unread','pc_demo001','2026-08-13 02:27:43','2026-08-13 02:27:43',NULL,NULL),('msg_351ca33ad21000','official_platform_pc_demo001','res_002',NULL,'official_message','official','platform','dmt_351ca33ab21000','欢迎加入邻里商城服务！\n欢迎加入邻里商城服务！','official',NULL,'unread','pc_demo001','2026-08-13 02:27:43','2026-08-13 02:27:43',NULL,NULL),('msg_351ca33adc1000','official_platform_pc_demo001','res_003',NULL,'official_message','official','platform','dmt_351ca33ab21000','欢迎加入邻里商城服务！\n欢迎加入邻里商城服务！','official',NULL,'unread','pc_demo001','2026-08-13 02:27:43','2026-08-13 02:27:43',NULL,NULL),('msg_351ca33ae01000','official_platform_pc_demo001','res_3516a17fb01000',NULL,'official_message','official','platform','dmt_351ca33ab21000','欢迎加入邻里商城服务！\n欢迎加入邻里商城服务！','official',NULL,'unread','pc_demo001','2026-08-13 02:27:43','2026-08-13 02:27:43',NULL,NULL),('msg_351ca33ae41000','official_platform_pc_demo001','res_act01',NULL,'official_message','official','platform','dmt_351ca33ab21000','欢迎加入邻里商城服务！\n欢迎加入邻里商城服务！','official',NULL,'read','pc_demo001','2026-08-13 02:27:43','2026-08-23 03:25:39',NULL,NULL),('msg_351ca33ae61000','official_platform_pc_demo001','res_coo01',NULL,'official_message','official','platform','dmt_351ca33ab21000','欢迎加入邻里商城服务！\n欢迎加入邻里商城服务！','official',NULL,'unread','pc_demo001','2026-08-13 02:27:43','2026-08-13 02:27:43',NULL,NULL),('msg_351ca33ae81000','official_platform_pc_demo001','res_courier01',NULL,'official_message','official','platform','dmt_351ca33ab21000','欢迎加入邻里商城服务！\n欢迎加入邻里商城服务！','official',NULL,'unread','pc_demo001','2026-08-13 02:27:43','2026-08-13 02:27:43',NULL,NULL),('msg_351ca33aec1000','official_platform_pc_demo001','res_courier02',NULL,'official_message','official','platform','dmt_351ca33ab21000','欢迎加入邻里商城服务！\n欢迎加入邻里商城服务！','official',NULL,'unread','pc_demo001','2026-08-13 02:27:43','2026-08-13 02:27:43',NULL,NULL),('msg_351ca33af01000','official_platform_pc_demo001','res_courier03',NULL,'official_message','official','platform','dmt_351ca33ab21000','欢迎加入邻里商城服务！\n欢迎加入邻里商城服务！','official',NULL,'unread','pc_demo001','2026-08-13 02:27:43','2026-08-13 02:27:43',NULL,NULL),('msg_351ca33af41000','official_platform_pc_demo001','res_dev001',NULL,'official_message','official','platform','dmt_351ca33ab21000','欢迎加入邻里商城服务！\n欢迎加入邻里商城服务！','official',NULL,'read','pc_demo001','2026-08-13 02:27:43','2026-08-13 05:45:36',NULL,NULL),('msg_351ca33af61000','official_platform_pc_demo001','res_dev003',NULL,'official_message','official','platform','dmt_351ca33ab21000','欢迎加入邻里商城服务！\n欢迎加入邻里商城服务！','official',NULL,'unread','pc_demo001','2026-08-13 02:27:43','2026-08-13 02:27:43',NULL,NULL),('msg_351ca33af81001','official_platform_pc_demo001','res_dev004',NULL,'official_message','official','platform','dmt_351ca33ab21000','欢迎加入邻里商城服务！\n欢迎加入邻里商城服务！','official',NULL,'unread','pc_demo001','2026-08-13 02:27:43','2026-08-13 02:27:43',NULL,NULL),('msg_351ca33afa1000','official_platform_pc_demo001','res_ind01',NULL,'official_message','official','platform','dmt_351ca33ab21000','欢迎加入邻里商城服务！\n欢迎加入邻里商城服务！','official',NULL,'unread','pc_demo001','2026-08-13 02:27:43','2026-08-13 02:27:43',NULL,NULL),('msg_351ca33afc1000','official_platform_pc_demo001','res_mch01',NULL,'official_message','official','platform','dmt_351ca33ab21000','欢迎加入邻里商城服务！\n欢迎加入邻里商城服务！','official',NULL,'unread','pc_demo001','2026-08-13 02:27:43','2026-08-13 02:27:43',NULL,NULL),('msg_351ca33afe1000','official_platform_pc_demo001','res_mch02',NULL,'official_message','official','platform','dmt_351ca33ab21000','欢迎加入邻里商城服务！\n欢迎加入邻里商城服务！','official',NULL,'unread','pc_demo001','2026-08-13 02:27:43','2026-08-13 02:27:43',NULL,NULL),('msg_351ca33afe1002','official_platform_pc_demo001','res_mch03',NULL,'official_message','official','platform','dmt_351ca33ab21000','欢迎加入邻里商城服务！\n欢迎加入邻里商城服务！','official',NULL,'unread','pc_demo001','2026-08-13 02:27:43','2026-08-13 02:27:43',NULL,NULL),('msg_351ca33b041000','official_platform_pc_demo001','res_mch04',NULL,'official_message','official','platform','dmt_351ca33ab21000','欢迎加入邻里商城服务！\n欢迎加入邻里商城服务！','official',NULL,'unread','pc_demo001','2026-08-13 02:27:43','2026-08-13 02:27:43',NULL,NULL),('msg_351ca33b061000','official_platform_pc_demo001','res_pleader01',NULL,'official_message','official','platform','dmt_351ca33ab21000','欢迎加入邻里商城服务！\n欢迎加入邻里商城服务！','official',NULL,'unread','pc_demo001','2026-08-13 02:27:43','2026-08-13 02:27:43',NULL,NULL),('msg_351ca33b081000','official_platform_pc_demo001','res_propadmin01',NULL,'official_message','official','platform','dmt_351ca33ab21000','欢迎加入邻里商城服务！\n欢迎加入邻里商城服务！','official',NULL,'unread','pc_demo001','2026-08-13 02:27:43','2026-08-13 02:27:43',NULL,NULL),('msg_351ca33b0a1001','official_platform_pc_demo001','res_sec01',NULL,'official_message','official','platform','dmt_351ca33ab21000','欢迎加入邻里商城服务！\n欢迎加入邻里商城服务！','official',NULL,'unread','pc_demo001','2026-08-13 02:27:43','2026-08-13 02:27:43',NULL,NULL),('msg_351ca33b0c1001','official_platform_pc_demo001','res_tech01',NULL,'official_message','official','platform','dmt_351ca33ab21000','欢迎加入邻里商城服务！\n欢迎加入邻里商城服务！','official',NULL,'unread','pc_demo001','2026-08-13 02:27:43','2026-08-13 02:27:43',NULL,NULL),('msg_351df7ddde1000','official_property_pc_demo001','res_dev001',NULL,'official_message','official','property',NULL,'物业费催缴通知\n\n您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','official',NULL,'read','pc_demo001','2026-08-13 05:33:45','2026-08-13 05:34:02',NULL,NULL),('msg_351df7dfb81000','official_property_pc_demo001','res_ind01',NULL,'official_message','official','property',NULL,'物业费催缴通知\n\n您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','official',NULL,'unread','pc_demo001','2026-08-13 05:33:45','2026-08-13 05:33:45',NULL,NULL),('msg_351df7e0fa1000','official_property_pc_demo001','res_dev001',NULL,'official_message','official','property',NULL,'物业费催缴通知\n您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','official',NULL,'read','pc_demo001','2026-08-13 05:33:45','2026-08-13 05:34:02',NULL,NULL),('msg_351df7e0fe1000','official_property_pc_demo001','res_dev002',NULL,'official_message','official','property',NULL,'物业费催缴通知\n您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','official',NULL,'unread','pc_demo001','2026-08-13 05:33:45','2026-08-13 05:33:45',NULL,NULL),('msg_351df7e1001000','official_property_pc_demo001','res_dev003',NULL,'official_message','official','property',NULL,'物业费催缴通知\n您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','official',NULL,'unread','pc_demo001','2026-08-13 05:33:45','2026-08-13 05:33:45',NULL,NULL),('msg_351e9411681000','res_dev001','res_act01',NULL,'community_message',NULL,NULL,NULL,'明天什么时候开始活动？','private',NULL,'read','pc_demo001','2026-08-13 06:59:03','2026-08-23 03:25:33',NULL,NULL),('msg_3583e059821000','res_dev001','res_mch03',NULL,'community_message',NULL,NULL,NULL,'1','private',NULL,'read','pc_demo001','2026-08-23 03:01:32','2026-09-06 08:07:43',NULL,NULL),('msg_3583e73fac1000','res_dev001','res_tech01',NULL,'community_message',NULL,NULL,NULL,'1','private',NULL,'read','pc_demo001','2026-08-23 03:05:19','2026-08-23 03:05:27',NULL,NULL),('msg_3583e7e4021000','res_tech01','res_dev001',NULL,'community_message',NULL,NULL,NULL,'在的','private',NULL,'read','pc_demo001','2026-08-23 03:05:40','2026-08-23 03:18:36',NULL,NULL),('msg_35840768441000','res_dev001','res_001',NULL,'resident_message',NULL,NULL,NULL,'1','private',NULL,'unread','pc_demo001','2026-08-23 03:22:52','2026-08-23 03:22:52',NULL,NULL),('msg_358407c0581000','res_dev001','res_3516a17fb01000',NULL,'resident_message',NULL,NULL,NULL,'1','private',NULL,'unread','pc_demo001','2026-08-23 03:23:04','2026-08-23 03:23:04',NULL,NULL),('msg_358407fa721000','res_dev001','res_001',NULL,'resident_message',NULL,NULL,NULL,'1','private',NULL,'unread','pc_demo001','2026-08-23 03:23:11','2026-08-23 03:23:11',NULL,NULL),('msg_3584082a741000','res_dev001','res_demo01',NULL,'resident_message',NULL,NULL,NULL,'～','private',NULL,'unread','pc_demo001','2026-08-23 03:23:17','2026-08-23 03:23:17',NULL,NULL),('msg_35840897381000','res_dev001','res_act01',NULL,'community_message',NULL,NULL,NULL,'1','private',NULL,'read','pc_demo001','2026-08-23 03:23:31','2026-08-23 03:25:33',NULL,NULL),('msg_35840c6c9a1000','res_act01','res_dev001',NULL,'community_message',NULL,NULL,NULL,'1','private',NULL,'read','pc_demo001','2026-08-23 03:25:37','2026-08-23 03:40:48',NULL,NULL),('msg_35853b02901000','official_property_pc_demo001','res_001',NULL,'official_message','official','property','dmt_35853b02881000','823823\n823','official',NULL,'unread','pc_demo001','2026-08-23 06:10:52','2026-08-23 06:10:52',NULL,NULL),('msg_35853b02a21000','official_property_pc_demo001','res_002',NULL,'official_message','official','property','dmt_35853b02881000','823823\n823','official',NULL,'unread','pc_demo001','2026-08-23 06:10:52','2026-08-23 06:10:52',NULL,NULL),('msg_35853b02a41001','official_property_pc_demo001','res_003',NULL,'official_message','official','property','dmt_35853b02881000','823823\n823','official',NULL,'unread','pc_demo001','2026-08-23 06:10:52','2026-08-23 06:10:52',NULL,NULL),('msg_35853b02a61001','official_property_pc_demo001','res_3516a17fb01000',NULL,'official_message','official','property','dmt_35853b02881000','823823\n823','official',NULL,'unread','pc_demo001','2026-08-23 06:10:52','2026-08-23 06:10:52',NULL,NULL),('msg_35853b02aa1000','official_property_pc_demo001','res_act01',NULL,'official_message','official','property','dmt_35853b02881000','823823\n823','official',NULL,'unread','pc_demo001','2026-08-23 06:10:52','2026-08-23 06:10:52',NULL,NULL),('msg_35853b02ac1000','official_property_pc_demo001','res_admin01',NULL,'official_message','official','property','dmt_35853b02881000','823823\n823','official',NULL,'unread','pc_demo001','2026-08-23 06:10:52','2026-08-23 06:10:52',NULL,NULL),('msg_35853b02ae1001','official_property_pc_demo001','res_coo01',NULL,'official_message','official','property','dmt_35853b02881000','823823\n823','official',NULL,'unread','pc_demo001','2026-08-23 06:10:52','2026-08-23 06:10:52',NULL,NULL),('msg_35853b02b21000','official_property_pc_demo001','res_courier01',NULL,'official_message','official','property','dmt_35853b02881000','823823\n823','official',NULL,'unread','pc_demo001','2026-08-23 06:10:52','2026-08-23 06:10:52',NULL,NULL),('msg_35853b02b41000','official_property_pc_demo001','res_courier02',NULL,'official_message','official','property','dmt_35853b02881000','823823\n823','official',NULL,'unread','pc_demo001','2026-08-23 06:10:52','2026-08-23 06:10:52',NULL,NULL),('msg_35853b02b61001','official_property_pc_demo001','res_courier03',NULL,'official_message','official','property','dmt_35853b02881000','823823\n823','official',NULL,'unread','pc_demo001','2026-08-23 06:10:52','2026-08-23 06:10:52',NULL,NULL),('msg_35853b02ba1000','official_property_pc_demo001','res_demo01',NULL,'official_message','official','property','dmt_35853b02881000','823823\n823','official',NULL,'unread','pc_demo001','2026-08-23 06:10:52','2026-08-23 06:10:52',NULL,NULL),('msg_35853b02ca1000','official_property_pc_demo001','res_demo02',NULL,'official_message','official','property','dmt_35853b02881000','823823\n823','official',NULL,'unread','pc_demo001','2026-08-23 06:10:52','2026-08-23 06:10:52',NULL,NULL),('msg_35853b02cc1001','official_property_pc_demo001','res_demo03',NULL,'official_message','official','property','dmt_35853b02881000','823823\n823','official',NULL,'unread','pc_demo001','2026-08-23 06:10:52','2026-08-23 06:10:52',NULL,NULL),('msg_35853b02d01000','official_property_pc_demo001','res_dev001',NULL,'official_message','official','property','dmt_35853b02881000','823823\n823','official',NULL,'read','pc_demo001','2026-08-23 06:10:52','2026-08-23 08:42:58',NULL,NULL),('msg_35853b02d21000','official_property_pc_demo001','res_dev003',NULL,'official_message','official','property','dmt_35853b02881000','823823\n823','official',NULL,'unread','pc_demo001','2026-08-23 06:10:52','2026-08-23 06:10:52',NULL,NULL),('msg_35853b02d61000','official_property_pc_demo001','res_dev004',NULL,'official_message','official','property','dmt_35853b02881000','823823\n823','official',NULL,'unread','pc_demo001','2026-08-23 06:10:52','2026-08-23 06:10:52',NULL,NULL),('msg_35853b02d81000','official_property_pc_demo001','res_ind01',NULL,'official_message','official','property','dmt_35853b02881000','823823\n823','official',NULL,'unread','pc_demo001','2026-08-23 06:10:52','2026-08-23 06:10:52',NULL,NULL),('msg_35853b02da1000','official_property_pc_demo001','res_mch01',NULL,'official_message','official','property','dmt_35853b02881000','823823\n823','official',NULL,'unread','pc_demo001','2026-08-23 06:10:52','2026-08-23 06:10:52',NULL,NULL),('msg_35853b02de1000','official_property_pc_demo001','res_mch02',NULL,'official_message','official','property','dmt_35853b02881000','823823\n823','official',NULL,'unread','pc_demo001','2026-08-23 06:10:52','2026-08-23 06:10:52',NULL,NULL),('msg_35853b02e01000','official_property_pc_demo001','res_mch03',NULL,'official_message','official','property','dmt_35853b02881000','823823\n823','official',NULL,'unread','pc_demo001','2026-08-23 06:10:52','2026-08-23 06:10:52',NULL,NULL),('msg_35853b02e21000','official_property_pc_demo001','res_mch04',NULL,'official_message','official','property','dmt_35853b02881000','823823\n823','official',NULL,'unread','pc_demo001','2026-08-23 06:10:52','2026-08-23 06:10:52',NULL,NULL),('msg_35853b02e41000','official_property_pc_demo001','res_pleader01',NULL,'official_message','official','property','dmt_35853b02881000','823823\n823','official',NULL,'unread','pc_demo001','2026-08-23 06:10:52','2026-08-23 06:10:52',NULL,NULL),('msg_35853b02e81000','official_property_pc_demo001','res_sec01',NULL,'official_message','official','property','dmt_35853b02881000','823823\n823','official',NULL,'unread','pc_demo001','2026-08-23 06:10:52','2026-08-23 06:10:52',NULL,NULL),('msg_35853b02ea1000','official_property_pc_demo001','res_tech01',NULL,'official_message','official','property','dmt_35853b02881000','823823\n823','official',NULL,'unread','pc_demo001','2026-08-23 06:10:52','2026-08-23 06:10:52',NULL,NULL),('msg_35853b6e9a1000','official_property_pc_demo001','res_dev001',NULL,'official_message','official','property',NULL,'1','official',NULL,'read','pc_demo001','2026-08-23 06:11:06','2026-08-23 08:42:58',NULL,NULL),('msg_35857aa86a1000','res_mch03','res_001',NULL,'merchant_ad',NULL,NULL,'mad_35857aa85a1000','广告\n广告','private',NULL,'unread','pc_demo001','2026-08-23 06:45:37','2026-08-23 06:45:37',NULL,NULL),('msg_35857aa86c1000','res_mch03','res_002',NULL,'merchant_ad',NULL,NULL,'mad_35857aa85a1000','广告\n广告','private',NULL,'unread','pc_demo001','2026-08-23 06:45:37','2026-08-23 06:45:37',NULL,NULL),('msg_35857aa8701000','res_mch03','res_003',NULL,'merchant_ad',NULL,NULL,'mad_35857aa85a1000','广告\n广告','private',NULL,'unread','pc_demo001','2026-08-23 06:45:37','2026-08-23 06:45:37',NULL,NULL),('msg_35857aa8721000','res_mch03','res_3516a17fb01000',NULL,'merchant_ad',NULL,NULL,'mad_35857aa85a1000','广告\n广告','private',NULL,'unread','pc_demo001','2026-08-23 06:45:37','2026-08-23 06:45:37',NULL,NULL),('msg_35857aa8741000','res_mch03','res_act01',NULL,'merchant_ad',NULL,NULL,'mad_35857aa85a1000','广告\n广告','private',NULL,'unread','pc_demo001','2026-08-23 06:45:37','2026-08-23 06:45:37',NULL,NULL),('msg_35857aa8761000','res_mch03','res_admin01',NULL,'merchant_ad',NULL,NULL,'mad_35857aa85a1000','广告\n广告','private',NULL,'unread','pc_demo001','2026-08-23 06:45:37','2026-08-23 06:45:37',NULL,NULL),('msg_35857aa8781000','res_mch03','res_coo01',NULL,'merchant_ad',NULL,NULL,'mad_35857aa85a1000','广告\n广告','private',NULL,'unread','pc_demo001','2026-08-23 06:45:37','2026-08-23 06:45:37',NULL,NULL),('msg_35857aa87a1000','res_mch03','res_courier01',NULL,'merchant_ad',NULL,NULL,'mad_35857aa85a1000','广告\n广告','private',NULL,'unread','pc_demo001','2026-08-23 06:45:37','2026-08-23 06:45:37',NULL,NULL),('msg_35857aa87a1001','res_mch03','res_courier02',NULL,'merchant_ad',NULL,NULL,'mad_35857aa85a1000','广告\n广告','private',NULL,'unread','pc_demo001','2026-08-23 06:45:37','2026-08-23 06:45:37',NULL,NULL),('msg_35857aa87c1000','res_mch03','res_courier03',NULL,'merchant_ad',NULL,NULL,'mad_35857aa85a1000','广告\n广告','private',NULL,'unread','pc_demo001','2026-08-23 06:45:37','2026-08-23 06:45:37',NULL,NULL),('msg_35857aa87e1000','res_mch03','res_demo01',NULL,'merchant_ad',NULL,NULL,'mad_35857aa85a1000','广告\n广告','private',NULL,'unread','pc_demo001','2026-08-23 06:45:37','2026-08-23 06:45:37',NULL,NULL),('msg_35857aa8801000','res_mch03','res_demo02',NULL,'merchant_ad',NULL,NULL,'mad_35857aa85a1000','广告\n广告','private',NULL,'unread','pc_demo001','2026-08-23 06:45:37','2026-08-23 06:45:37',NULL,NULL),('msg_35857aa8821000','res_mch03','res_demo03',NULL,'merchant_ad',NULL,NULL,'mad_35857aa85a1000','广告\n广告','private',NULL,'unread','pc_demo001','2026-08-23 06:45:37','2026-08-23 06:45:37',NULL,NULL),('msg_35857aa8841000','res_mch03','res_dev001',NULL,'merchant_ad',NULL,NULL,'mad_35857aa85a1000','广告\n广告','private',NULL,'read','pc_demo001','2026-08-23 06:45:37','2026-08-23 08:43:01',NULL,NULL),('msg_35857aa8841001','res_mch03','res_dev003',NULL,'merchant_ad',NULL,NULL,'mad_35857aa85a1000','广告\n广告','private',NULL,'unread','pc_demo001','2026-08-23 06:45:37','2026-08-23 06:45:37',NULL,NULL),('msg_35857aa8861000','res_mch03','res_dev004',NULL,'merchant_ad',NULL,NULL,'mad_35857aa85a1000','广告\n广告','private',NULL,'unread','pc_demo001','2026-08-23 06:45:37','2026-08-23 06:45:37',NULL,NULL),('msg_35857aa8861001','res_mch03','res_ind01',NULL,'merchant_ad',NULL,NULL,'mad_35857aa85a1000','广告\n广告','private',NULL,'unread','pc_demo001','2026-08-23 06:45:37','2026-08-23 06:45:37',NULL,NULL),('msg_35857aa8881000','res_mch03','res_mch01',NULL,'merchant_ad',NULL,NULL,'mad_35857aa85a1000','广告\n广告','private',NULL,'unread','pc_demo001','2026-08-23 06:45:37','2026-08-23 06:45:37',NULL,NULL),('msg_35857aa88a1000','res_mch03','res_mch02',NULL,'merchant_ad',NULL,NULL,'mad_35857aa85a1000','广告\n广告','private',NULL,'unread','pc_demo001','2026-08-23 06:45:37','2026-08-23 06:45:37',NULL,NULL),('msg_35857aa88c1000','res_mch03','res_mch04',NULL,'merchant_ad',NULL,NULL,'mad_35857aa85a1000','广告\n广告','private',NULL,'unread','pc_demo001','2026-08-23 06:45:37','2026-08-23 06:45:37',NULL,NULL),('msg_35857aa88c1001','res_mch03','res_pleader01',NULL,'merchant_ad',NULL,NULL,'mad_35857aa85a1000','广告\n广告','private',NULL,'unread','pc_demo001','2026-08-23 06:45:37','2026-08-23 06:45:37',NULL,NULL),('msg_35857aa88e1000','res_mch03','res_propadmin01',NULL,'merchant_ad',NULL,NULL,'mad_35857aa85a1000','广告\n广告','private',NULL,'unread','pc_demo001','2026-08-23 06:45:37','2026-08-23 06:45:37',NULL,NULL),('msg_35857aa8901000','res_mch03','res_sec01',NULL,'merchant_ad',NULL,NULL,'mad_35857aa85a1000','广告\n广告','private',NULL,'unread','pc_demo001','2026-08-23 06:45:37','2026-08-23 06:45:37',NULL,NULL),('msg_35857aa8901001','res_mch03','res_tech01',NULL,'merchant_ad',NULL,NULL,'mad_35857aa85a1000','广告\n广告','private',NULL,'unread','pc_demo001','2026-08-23 06:45:37','2026-08-23 06:45:37',NULL,NULL),('msg_35866e241e1000','official_property_pc_demo001','res_003',NULL,'official_message','official','property','arrears_reminder','尊敬的业主-孙八，您在演示小区25-1-101存在未缴物业费，\n欠费账期：2026-Q1\n欠费金额：¥479.76\n请于2026-07-10前完成缴费。您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','official',NULL,'unread','pc_demo001','2026-08-23 08:58:36','2026-08-23 08:58:36',NULL,NULL),('msg_35866e248e1000','official_property_pc_demo001','res_004',NULL,'official_message','official','property','arrears_reminder','尊敬的租户-周九，您在演示小区4-3-301存在未缴物业费，\n欠费账期：2026-Q1\n欠费金额：¥286.37\n请于2026-07-10前完成缴费。您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','official',NULL,'unread','pc_demo001','2026-08-23 08:58:36','2026-08-23 08:58:36',NULL,NULL),('msg_35866e24a01000','official_property_pc_demo001','res_act01',NULL,'official_message','official','property','arrears_reminder','尊敬的活动组长-郑美丽，您在演示小区2-1-101存在未缴物业费，\n欠费账期：2026-Q1 至 2026-Q3\n欠费金额：¥909.07\n请于2026-07-10前完成缴费。您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','official',NULL,'unread','pc_demo001','2026-08-23 08:58:36','2026-08-23 08:58:36',NULL,NULL),('msg_35866e24c81000','official_property_pc_demo001','res_coo01',NULL,'official_message','official','property','arrears_reminder','尊敬的统筹-张伟，您在演示小区存在未缴物业费，\n欠费账期：2026-Q1 至 2026-Q2\n欠费金额：¥912.45\n请于2026-07-10前完成缴费。您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','official',NULL,'unread','pc_demo001','2026-08-23 08:58:36','2026-08-23 08:58:36',NULL,NULL),('msg_35866e24dc1000','official_property_pc_demo001','res_dev001',NULL,'official_message','official','property','arrears_reminder','尊敬的张阿姨，您在演示小区1-1-108存在未缴物业费，\n欠费账期：2026-Q1\n欠费金额：¥559.43\n请于2026-07-10前完成缴费。您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','official',NULL,'read','pc_demo001','2026-08-23 08:58:36','2026-08-23 14:54:28',NULL,NULL),('msg_35866e24f61000','official_property_pc_demo001','res_dev002',NULL,'official_message','official','property','arrears_reminder','尊敬的张建设，您在演示小区3-12-1201存在未缴物业费，\n欠费账期：2026-Q1\n欠费金额：¥260.35\n请于2026-07-10前完成缴费。您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','official',NULL,'unread','pc_demo001','2026-08-23 08:58:36','2026-08-23 08:58:36',NULL,NULL),('msg_35866e25061000','official_property_pc_demo001','res_dev003',NULL,'official_message','official','property','arrears_reminder','尊敬的张子涵，您在演示小区3-12-1201存在未缴物业费，\n欠费账期：2026-Q1\n欠费金额：¥570.35\n请于2026-07-10前完成缴费。您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','official',NULL,'unread','pc_demo001','2026-08-23 08:58:36','2026-08-23 08:58:36',NULL,NULL),('msg_35866e25141000','official_property_pc_demo001','res_ind01',NULL,'official_message','official','property','arrears_reminder','尊敬的个体负责人-王强，您在演示小区5-1存在未缴物业费，\n欠费账期：2026-Q2\n欠费金额：¥519.09\n请于2026-07-10前完成缴费。您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','official',NULL,'unread','pc_demo001','2026-08-23 08:58:36','2026-08-23 08:58:36',NULL,NULL),('msg_35866e25261000','official_property_pc_demo001','res_mch03',NULL,'official_message','official','property','arrears_reminder','尊敬的超市老板，您在演示小区存在未缴物业费，\n欠费账期：2026-Q1\n欠费金额：¥322.41\n请于2026-07-10前完成缴费。您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','official',NULL,'unread','pc_demo001','2026-08-23 08:58:36','2026-08-23 08:58:36',NULL,NULL),('msg_35866e25381000','official_property_pc_demo001','res_propadmin01',NULL,'official_message','official','property','arrears_reminder','尊敬的物业管理员-刘芳，您在演示小区存在未缴物业费，\n欠费账期：2026-Q1\n欠费金额：¥596.68\n请于2026-07-10前完成缴费。您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','official',NULL,'unread','pc_demo001','2026-08-23 08:58:36','2026-08-23 08:58:36',NULL,NULL),('msg_35866e254a1000','official_property_pc_demo001','res_tech01',NULL,'official_message','official','property','arrears_reminder','尊敬的技工-刘师傅，您在演示小区存在未缴物业费，\n欠费账期：2026-Q1 至 2026-Q3\n欠费金额：¥1131.59\n请于2026-07-10前完成缴费。您有未缴纳的物业费，请尽快缴纳，以免产生违约金。','official',NULL,'unread','pc_demo001','2026-08-23 08:58:36','2026-08-23 08:58:36',NULL,NULL),('msg_376e1000','res_act01','res_dev001',NULL,'community_message',NULL,NULL,NULL,'2','private',NULL,'read','pc_demo001','2026-09-06 01:40:04','2026-09-06 01:40:23',NULL,NULL),('msg_462e1000','res_act01','res_dev001',NULL,'community_message',NULL,NULL,NULL,'11','private',NULL,'read','pc_demo001','2026-09-06 01:39:33','2026-09-06 01:39:46',NULL,NULL),('msg_4f5e1000','res_act01','res_dev001',NULL,'community_message',NULL,NULL,NULL,'1','private',NULL,'read','pc_demo001','2026-09-06 09:40:43','2026-09-06 09:45:20',NULL,NULL),('msg_83dc1000','res_act01','res_dev001',NULL,'community_message',NULL,NULL,NULL,'1111','private',NULL,'read','pc_demo001','2026-09-06 01:40:13','2026-09-06 01:40:23',NULL,NULL),('msg_8c381000','res_dev001','res_mch03',NULL,'community_message',NULL,NULL,NULL,'1','private',NULL,'read','pc_demo001','2026-09-05 01:32:27','2026-09-06 08:07:43',NULL,NULL),('msg_8d701000','res_mch03','res_001',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e0e1000','res_mch03','res_002',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e101000','res_mch03','res_003',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e121000','res_mch03','res_3516a17fb01000',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e141000','res_mch03','res_35890655f01000',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e141001','res_mch03','res_35c14607ae1000',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e161000','res_mch03','res_35c14b96101000',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e181000','res_mch03','res_act01',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e1a1000','res_mch03','res_admin01',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e1c1000','res_mch03','res_coo01',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e1e1000','res_mch03','res_courier01',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e1e1001','res_mch03','res_courier02',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e201000','res_mch03','res_courier03',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e201001','res_mch03','res_demo01',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e221000','res_mch03','res_demo02',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e221001','res_mch03','res_demo03',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e241000','res_mch03','res_dev001',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'read','pc_demo001','2026-09-05 09:06:17','2026-09-06 02:08:43',NULL,NULL),('msg_8e261000','res_mch03','res_dev003',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e261001','res_mch03','res_dev004',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e281000','res_mch03','res_ind01',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e281001','res_mch03','res_mch01',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e2a1000','res_mch03','res_mch02',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e2a1001','res_mch03','res_mch04',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e2c1000','res_mch03','res_pleader01',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e2c1001','res_mch03','res_propadmin01',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e2e1000','res_mch03','res_sec01',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_8e2e1001','res_mch03','res_tech01',NULL,'merchant_ad',NULL,NULL,'mad_8d241000','广告\n1','private',NULL,'unread','pc_demo001','2026-09-05 09:06:17','2026-09-05 09:06:17',NULL,NULL),('msg_90221000','res_dev001','res_mch03',NULL,'community_message',NULL,NULL,NULL,'1','private',NULL,'read','pc_demo001','2026-09-06 02:09:12','2026-09-06 08:07:43',NULL,NULL),('msg_94801000','res_dev001','res_act01',NULL,'community_message',NULL,NULL,NULL,'1','private',NULL,'read','pc_demo001','2026-09-04 07:28:56','2026-09-06 09:40:39',NULL,NULL),('msg_a8021000','res_dev001','res_act01',NULL,'community_message',NULL,NULL,NULL,'1','private',NULL,'read','pc_demo001','2026-09-04 06:37:38','2026-09-06 09:40:39',NULL,NULL),('msg_bbc81000','res_dev001','res_mch03',NULL,'community_message',NULL,NULL,NULL,'11','private',NULL,'read','pc_demo001','2026-09-06 02:08:45','2026-09-06 08:07:43',NULL,NULL),('msg_c1b61000','official_property_pc_demo001','res_001',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c2021000','official_property_pc_demo001','res_002',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c2081000','official_property_pc_demo001','res_003',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c20c1000','official_property_pc_demo001','res_3516a17fb01000',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c2161000','official_property_pc_demo001','res_35890655f01000',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c2181001','official_property_pc_demo001','res_35c14607ae1000',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c21a1001','official_property_pc_demo001','res_35c14b96101000',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c21c1001','official_property_pc_demo001','res_act01',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c2261000','official_property_pc_demo001','res_admin01',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c22c1000','official_property_pc_demo001','res_coo01',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c22e1000','official_property_pc_demo001','res_courier01',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c2301000','official_property_pc_demo001','res_courier02',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c2321001','official_property_pc_demo001','res_courier03',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c2341001','official_property_pc_demo001','res_demo01',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c2361001','official_property_pc_demo001','res_demo02',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c2381001','official_property_pc_demo001','res_demo03',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c23e1000','official_property_pc_demo001','res_dev001',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'read','pc_demo001','2026-09-07 02:06:11','2026-09-11 07:51:08',NULL,NULL),('msg_c2401001','official_property_pc_demo001','res_dev003',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c2541000','official_property_pc_demo001','res_dev004',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c2561001','official_property_pc_demo001','res_ind01',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c2581001','official_property_pc_demo001','res_mch01',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c25a1001','official_property_pc_demo001','res_mch02',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c25c1001','official_property_pc_demo001','res_mch03',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c25e1001','official_property_pc_demo001','res_mch04',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c2621000','official_property_pc_demo001','res_pleader01',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c2641001','official_property_pc_demo001','res_sec01',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_c2661001','official_property_pc_demo001','res_tech01',NULL,'official_message','official','property','dmt_c07a1000','97\n97','official',NULL,'unread','pc_demo001','2026-09-07 02:06:11','2026-09-07 02:06:11',NULL,NULL),('msg_f45a1000','res_act01','res_dev001',NULL,'community_message',NULL,NULL,NULL,'1','private',NULL,'read','pc_demo001','2026-09-06 01:33:22','2026-09-06 01:39:46',NULL,NULL),('msg_gen00001','system','res_courier01',NULL,'platform','official','platform',NULL,'您有一笔订单已发货，请留意物流信息。','private',NULL,'read','pc_demo001','2026-07-30 23:27:49','2026-07-30 23:27:49',NULL,NULL),('msg_gen00002','property','res_dev004',NULL,'property','official','property',NULL,'社区便民服务日活动将于本周六举行，欢迎参加。','private',NULL,'read','pc_demo001','2026-07-14 07:01:45','2026-07-14 07:01:45',NULL,NULL),('msg_gen00003','system','res_sec01',NULL,'platform','official','platform',NULL,'您有一笔订单已发货，请留意物流信息。','private',NULL,'read','pc_demo001','2026-07-26 14:07:19','2026-07-26 14:07:19',NULL,NULL),('msg_gen00004','property','res_dev001',NULL,'community','personal','community',NULL,'您发布的动态收到了新的评论，点击查看。','private',NULL,'read','pc_demo001','2026-08-05 09:37:22','2026-09-17 03:49:11',NULL,NULL),('msg_gen00005','property','res_admin01',NULL,'property','official','property',NULL,'本月物业费账单已生成，请及时缴纳。','private',NULL,'read','pc_demo001','2026-07-20 02:13:42','2026-07-20 02:13:42',NULL,NULL),('msg_gen00006','system','res_admin01',NULL,'system','official','system',NULL,'您参与的社区活动已审核通过。','private',NULL,'unread','pc_demo001','2026-07-05 15:37:00','2026-07-05 15:37:00',NULL,NULL),('msg_gen00007','property','res_sec01',NULL,'property','official','property',NULL,'社区便民服务日活动将于本周六举行，欢迎参加。','private',NULL,'read','pc_demo001','2026-07-22 08:40:13','2026-07-22 08:40:13',NULL,NULL),('msg_gen00008','property','res_mch03',NULL,'property','official','property',NULL,'社区便民服务日活动将于本周六举行，欢迎参加。','private',NULL,'read','pc_demo001','2026-08-10 20:14:55','2026-08-10 20:14:55',NULL,NULL),('msg_gen00009','system','res_004',NULL,'system','official','system',NULL,'您的积分已到账，请注意查收。','private',NULL,'read','pc_demo001','2026-07-09 16:29:15','2026-07-09 16:29:15',NULL,NULL),('msg_gen00010','property','res_propadmin01',NULL,'property','official','property',NULL,'本月物业费账单已生成，请及时缴纳。','private',NULL,'read','pc_demo001','2026-08-11 05:31:19','2026-08-11 05:31:19',NULL,NULL),('msg_gen00011','system','res_courier03',NULL,'platform','official','platform',NULL,'您有一笔订单已发货，请留意物流信息。','private',NULL,'read','pc_demo001','2026-08-09 20:49:19','2026-08-09 20:49:19',NULL,NULL),('msg_gen00012','system','res_001',NULL,'system','official','system',NULL,'您参与的社区活动已审核通过。','private',NULL,'read','pc_demo001','2026-07-20 16:16:43','2026-07-20 16:16:43',NULL,NULL),('msg_gen00013','system','res_mch04',NULL,'system','official','system',NULL,'您参与的社区活动已审核通过。','private',NULL,'read','pc_demo001','2026-07-09 14:44:34','2026-07-09 14:44:34',NULL,NULL),('msg_gen00014','system','res_ind01',NULL,'system','official','system',NULL,'您参与的社区活动已审核通过。','private',NULL,'unread','pc_demo001','2026-07-24 08:16:38','2026-07-24 08:16:38',NULL,NULL),('msg_gen00015','system','res_mch03',NULL,'platform','official','platform',NULL,'您有一笔订单已发货，请留意物流信息。','private',NULL,'read','pc_demo001','2026-08-04 23:56:17','2026-08-04 23:56:17',NULL,NULL),('msg_gen00016','property','res_mch03',NULL,'property','official','property',NULL,'社区便民服务日活动将于本周六举行，欢迎参加。','private',NULL,'unread','pc_demo001','2026-07-18 04:09:49','2026-07-18 04:09:49',NULL,NULL),('msg_gen00017','system','res_003',NULL,'platform','official','platform',NULL,'您有一笔订单已发货，请留意物流信息。','private',NULL,'read','pc_demo001','2026-08-06 19:10:55','2026-08-06 19:10:55',NULL,NULL),('msg_gen00018','system','res_mch03',NULL,'system','official','system',NULL,'您的积分已到账，请注意查收。','private',NULL,'unread','pc_demo001','2026-08-08 19:58:45','2026-08-08 19:58:45',NULL,NULL),('msg_gen00019','property','res_004',NULL,'property','official','property',NULL,'社区便民服务日活动将于本周六举行，欢迎参加。','private',NULL,'read','pc_demo001','2026-08-10 22:32:52','2026-08-10 22:32:52',NULL,NULL),('msg_gen00020','property','res_ind01',NULL,'property','official','property',NULL,'社区便民服务日活动将于本周六举行，欢迎参加。','private',NULL,'unread','pc_demo001','2026-08-01 02:58:16','2026-08-01 02:58:16',NULL,NULL),('msg_gen00021','system','res_sec01',NULL,'system','official','system',NULL,'您的积分已到账，请注意查收。','private',NULL,'read','pc_demo001','2026-08-09 07:05:45','2026-08-09 07:05:45',NULL,NULL),('msg_gen00022','property','res_admin01',NULL,'community','personal','community',NULL,'您发布的动态收到了新的评论，点击查看。','private',NULL,'read','pc_demo001','2026-08-03 14:43:56','2026-08-03 14:43:56',NULL,NULL),('msg_gen00023','system','res_tech01',NULL,'platform','official','platform',NULL,'您有一笔订单已发货，请留意物流信息。','private',NULL,'unread','pc_demo001','2026-07-23 08:54:05','2026-07-23 08:54:05',NULL,NULL),('msg_gen00024','system','res_002',NULL,'platform','official','platform',NULL,'您有一笔订单已发货，请留意物流信息。','private',NULL,'read','pc_demo001','2026-08-05 05:25:10','2026-08-05 05:25:10',NULL,NULL),('msg_gen00025','system','res_mch03',NULL,'system','official','system',NULL,'您的积分已到账，请注意查收。','private',NULL,'read','pc_demo001','2026-08-04 04:10:21','2026-08-04 04:10:21',NULL,NULL),('msg_gen00026','system','res_admin01',NULL,'system','official','system',NULL,'您的积分已到账，请注意查收。','private',NULL,'read','pc_demo001','2026-07-18 13:23:59','2026-07-18 13:23:59',NULL,NULL),('msg_gen00027','system','res_courier02',NULL,'platform','official','platform',NULL,'您有一笔订单已发货，请留意物流信息。','private',NULL,'read','pc_demo001','2026-08-10 16:19:06','2026-08-10 16:19:06',NULL,NULL),('msg_gen00028','property','res_dev001',NULL,'community','personal','community',NULL,'您发布的动态收到了新的评论，点击查看。','private',NULL,'read','pc_demo001','2026-07-21 06:48:06','2026-09-17 03:49:11',NULL,NULL),('msg_gen00029','system','res_mch03',NULL,'system','official','system',NULL,'您的积分已到账，请注意查收。','private',NULL,'read','pc_demo001','2026-07-26 22:09:34','2026-07-26 22:09:34',NULL,NULL),('msg_gen00030','property','res_mch01',NULL,'property','official','property',NULL,'本月物业费账单已生成，请及时缴纳。','private',NULL,'read','pc_demo001','2026-07-28 01:39:18','2026-07-28 01:39:18',NULL,NULL),('msg_gen00031','property','res_propadmin01',NULL,'community','personal','community',NULL,'您发布的动态收到了新的评论，点击查看。','private',NULL,'read','pc_demo001','2026-07-30 02:20:25','2026-07-30 02:20:25',NULL,NULL),('msg_gen00032','system','res_mch04',NULL,'platform','official','platform',NULL,'您有一笔订单已发货，请留意物流信息。','private',NULL,'read','pc_demo001','2026-08-10 22:16:06','2026-08-10 22:16:06',NULL,NULL),('msg_gen00033','system','res_mch02',NULL,'system','official','system',NULL,'您的积分已到账，请注意查收。','private',NULL,'read','pc_demo001','2026-07-23 03:25:24','2026-07-23 03:25:24',NULL,NULL),('msg_gen00034','system','res_mch04',NULL,'system','official','system',NULL,'您参与的社区活动已审核通过。','private',NULL,'read','pc_demo001','2026-08-03 05:11:24','2026-08-03 05:11:24',NULL,NULL),('msg_gen00035','system','res_dev001',NULL,'system','official','system',NULL,'您的积分已到账，请注意查收。','private',NULL,'read','pc_demo001','2026-07-03 20:30:30','2026-07-03 20:30:30',NULL,NULL),('msg_gen00036','property','res_courier02',NULL,'property','official','property',NULL,'本月物业费账单已生成，请及时缴纳。','private',NULL,'read','pc_demo001','2026-07-29 10:35:11','2026-07-29 10:35:11',NULL,NULL),('msg_gen00037','system','res_003',NULL,'system','official','system',NULL,'您参与的社区活动已审核通过。','private',NULL,'read','pc_demo001','2026-08-11 11:13:05','2026-08-11 11:13:05',NULL,NULL),('msg_gen00038','system','res_propadmin01',NULL,'system','official','system',NULL,'您参与的社区活动已审核通过。','private',NULL,'read','pc_demo001','2026-08-02 16:45:27','2026-08-02 16:45:27',NULL,NULL),('msg_gen00039','property','res_coo01',NULL,'property','official','property',NULL,'社区便民服务日活动将于本周六举行，欢迎参加。','private',NULL,'unread','pc_demo001','2026-07-30 12:20:55','2026-07-30 12:20:55',NULL,NULL),('msg_gen00040','property','res_courier03',NULL,'property','official','property',NULL,'社区便民服务日活动将于本周六举行，欢迎参加。','private',NULL,'unread','pc_demo001','2026-08-04 22:37:21','2026-08-04 22:37:21',NULL,NULL),('msg_gen00041','system','res_propadmin01',NULL,'system','official','system',NULL,'您的积分已到账，请注意查收。','private',NULL,'read','pc_demo001','2026-07-24 23:49:34','2026-07-24 23:49:34',NULL,NULL),('msg_gen00042','system','res_dev002',NULL,'system','official','system',NULL,'您的积分已到账，请注意查收。','private',NULL,'read','pc_demo001','2026-08-10 17:24:59','2026-08-10 17:24:59',NULL,NULL),('msg_gen00043','system','res_mch03',NULL,'system','official','system',NULL,'您参与的社区活动已审核通过。','private',NULL,'read','pc_demo001','2026-08-05 14:25:24','2026-08-05 14:25:24',NULL,NULL),('msg_gen00044','system','res_001',NULL,'system','official','system',NULL,'您的积分已到账，请注意查收。','private',NULL,'read','pc_demo001','2026-07-29 21:16:48','2026-07-29 21:16:48',NULL,NULL),('msg_gen00045','property','res_sec01',NULL,'property','official','property',NULL,'本月物业费账单已生成，请及时缴纳。','private',NULL,'read','pc_demo001','2026-07-12 10:25:39','2026-07-12 10:25:39',NULL,NULL),('msg_gen00046','system','res_act01',NULL,'platform','official','platform',NULL,'您有一笔订单已发货，请留意物流信息。','private',NULL,'unread','pc_demo001','2026-08-06 05:10:13','2026-08-06 05:10:13',NULL,NULL),('msg_gen00047','system','res_mch02',NULL,'system','official','system',NULL,'您的积分已到账，请注意查收。','private',NULL,'read','pc_demo001','2026-07-05 20:56:16','2026-07-05 20:56:16',NULL,NULL),('msg_gen00048','property','res_ind01',NULL,'community','personal','community',NULL,'您发布的动态收到了新的评论，点击查看。','private',NULL,'unread','pc_demo001','2026-07-09 22:20:33','2026-07-09 22:20:33',NULL,NULL),('msg_gen00049','system','res_dev001',NULL,'system','official','system',NULL,'您参与的社区活动已审核通过。','private',NULL,'read','pc_demo001','2026-07-18 20:42:24','2026-07-18 20:42:24',NULL,NULL),('msg_gen00050','property','res_003',NULL,'property','official','property',NULL,'本月物业费账单已生成，请及时缴纳。','private',NULL,'read','pc_demo001','2026-08-12 14:49:35','2026-08-12 14:49:35',NULL,NULL),('msg_gen00051','property','res_ind01',NULL,'property','official','property',NULL,'社区便民服务日活动将于本周六举行，欢迎参加。','private',NULL,'read','pc_demo001','2026-07-08 06:57:52','2026-07-08 06:57:52',NULL,NULL),('msg_gen00052','system','res_courier01',NULL,'system','official','system',NULL,'您的积分已到账，请注意查收。','private',NULL,'read','pc_demo001','2026-07-24 13:05:01','2026-07-24 13:05:01',NULL,NULL),('msg_gen00053','system','res_mch04',NULL,'platform','official','platform',NULL,'您有一笔订单已发货，请留意物流信息。','private',NULL,'unread','pc_demo001','2026-07-09 19:07:01','2026-07-09 19:07:01',NULL,NULL),('msg_gen00054','system','res_mch04',NULL,'platform','official','platform',NULL,'您有一笔订单已发货，请留意物流信息。','private',NULL,'read','pc_demo001','2026-07-23 01:44:01','2026-07-23 01:44:01',NULL,NULL),('msg_gen00055','system','res_004',NULL,'system','official','system',NULL,'您的积分已到账，请注意查收。','private',NULL,'read','pc_demo001','2026-07-03 18:42:18','2026-07-03 18:42:18',NULL,NULL),('msg_gen00056','system','res_tech01',NULL,'system','official','system',NULL,'您的积分已到账，请注意查收。','private',NULL,'read','pc_demo001','2026-08-04 16:03:16','2026-08-04 16:03:16',NULL,NULL),('msg_gen00057','property','res_001',NULL,'property','official','property',NULL,'社区便民服务日活动将于本周六举行，欢迎参加。','private',NULL,'read','pc_demo001','2026-07-13 12:22:58','2026-07-13 12:22:58',NULL,NULL),('msg_gen00058','system','res_courier03',NULL,'platform','official','platform',NULL,'您有一笔订单已发货，请留意物流信息。','private',NULL,'read','pc_demo001','2026-07-29 09:12:47','2026-07-29 09:12:47',NULL,NULL),('msg_gen00059','property','res_sec01',NULL,'community','personal','community',NULL,'您发布的动态收到了新的评论，点击查看。','private',NULL,'unread','pc_demo001','2026-07-06 06:43:34','2026-07-06 06:43:34',NULL,NULL),('msg_gen00060','system','res_coo01',NULL,'platform','official','platform',NULL,'您有一笔订单已发货，请留意物流信息。','private',NULL,'read','pc_demo001','2026-07-20 11:55:39','2026-07-20 11:55:39',NULL,NULL),('msg_gen00061','system','res_mch04',NULL,'system','official','system',NULL,'您参与的社区活动已审核通过。','private',NULL,'read','pc_demo001','2026-07-05 05:56:24','2026-07-05 05:56:24',NULL,NULL),('msg_gen00062','property','res_dev001',NULL,'property','official','property',NULL,'本月物业费账单已生成，请及时缴纳。','private',NULL,'read','pc_demo001','2026-07-29 07:18:23','2026-07-29 07:18:23',NULL,NULL),('msg_gen00063','system','res_dev003',NULL,'system','official','system',NULL,'您的积分已到账，请注意查收。','private',NULL,'read','pc_demo001','2026-08-02 10:44:50','2026-08-02 10:44:50',NULL,NULL),('msg_gen00064','property','res_propadmin01',NULL,'property','official','property',NULL,'本月物业费账单已生成，请及时缴纳。','private',NULL,'read','pc_demo001','2026-07-10 10:21:28','2026-07-10 10:21:28',NULL,NULL),('msg_gen00065','property','res_courier03',NULL,'property','official','property',NULL,'社区便民服务日活动将于本周六举行，欢迎参加。','private',NULL,'read','pc_demo001','2026-07-10 23:25:28','2026-07-10 23:25:28',NULL,NULL),('msg_gen00066','system','res_tech01',NULL,'platform','official','platform',NULL,'您有一笔订单已发货，请留意物流信息。','private',NULL,'read','pc_demo001','2026-08-08 21:41:35','2026-08-08 21:41:35',NULL,NULL),('msg_gen00067','property','res_sec01',NULL,'community','personal','community',NULL,'您发布的动态收到了新的评论，点击查看。','private',NULL,'read','pc_demo001','2026-07-16 20:14:04','2026-07-16 20:14:04',NULL,NULL),('msg_gen00068','property','res_dev004',NULL,'community','personal','community',NULL,'您发布的动态收到了新的评论，点击查看。','private',NULL,'read','pc_demo001','2026-08-04 04:40:15','2026-08-04 04:40:15',NULL,NULL),('msg_gen00069','property','res_dev003',NULL,'property','official','property',NULL,'本月物业费账单已生成，请及时缴纳。','private',NULL,'unread','pc_demo001','2026-08-01 17:23:37','2026-08-01 17:23:37',NULL,NULL),('msg_gen00070','property','res_dev001',NULL,'property','official','property',NULL,'本月物业费账单已生成，请及时缴纳。','private',NULL,'read','pc_demo001','2026-08-07 14:22:14','2026-08-07 14:22:14',NULL,NULL);
/*!40000 ALTER TABLE `messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `navigation_items`
--

DROP TABLE IF EXISTS `navigation_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `navigation_items` (
  `id` varchar(32) NOT NULL,
  `position` varchar(20) NOT NULL,
  `module` varchar(30) NOT NULL DEFAULT 'home',
  `title` varchar(50) NOT NULL,
  `icon` varchar(200) DEFAULT NULL,
  `link_type` varchar(20) NOT NULL,
  `link_url` varchar(200) DEFAULT NULL,
  `sort_order` int NOT NULL,
  `enabled` tinyint(1) NOT NULL,
  `visible_roles` varchar(200) DEFAULT NULL,
  `property_company_id` varchar(32) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_nav_pc` (`property_company_id`,`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='手机端导航栏配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `navigation_items`
--

LOCK TABLES `navigation_items` WRITE;
/*!40000 ALTER TABLE `navigation_items` DISABLE KEYS */;
/*!40000 ALTER TABLE `navigation_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `neighbor_blocks`
--

DROP TABLE IF EXISTS `neighbor_blocks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `neighbor_blocks` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `blocker_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '屏蔽发起人ID',
  `blocked_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '被屏蔽人ID',
  `property_company_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属物业公司ID',
  `blocked_at` timestamp NOT NULL COMMENT '屏蔽时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_blocker_blocked` (`blocker_id`,`blocked_id`),
  KEY `idx_blocker` (`blocker_id`),
  KEY `idx_blocked` (`blocked_id`),
  KEY `idx_property_company` (`property_company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `neighbor_blocks`
--

LOCK TABLES `neighbor_blocks` WRITE;
/*!40000 ALTER TABLE `neighbor_blocks` DISABLE KEYS */;
/*!40000 ALTER TABLE `neighbor_blocks` ENABLE KEYS */;
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
INSERT INTO `neighbor_contact_limits` VALUES ('ncl_35840768401000','res_dev001','pc_demo001','2026-08-23',3,'2026-08-23 03:22:52','2026-08-23 03:23:17');
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
INSERT INTO `operation_logs` VALUES ('ol_3520ecd9a81000','res_admin01','pc_demo001','merchant_withdrawal','audit','merchant_withdrawal','mw_3520ebdbea1000',NULL,'{\"remark\": \"1\", \"auditResult\": \"approved\"}',NULL,NULL,'2026-08-13 12:27:10','2026-08-13 12:27:10'),('ol_352137bf541000','res_admin01','pc_demo001','role_withdrawal','audit','role_withdrawal','wr_3520d9a0ac1000',NULL,'{\"remark\": \"线上测试通过\", \"auditResult\": \"approved\"}',NULL,NULL,'2026-08-13 13:08:04','2026-08-13 13:08:04'),('ol_35213837101000','res_admin01','pc_demo001','role_withdrawal','audit','role_withdrawal','wr_3520d9a0ac1000',NULL,'{\"auditResult\": \"completed\", \"transferProof\": \"https://proof.example.com/test.jpg\"}',NULL,NULL,'2026-08-13 13:08:19','2026-08-13 13:08:19'),('ol_35487073ee1000','res_admin01','pc_demo001','merchant_point_purchase','audit','merchant_point_purchase','mpp_353bd6a4921000',NULL,'{\"remark\": \"1\", \"auditResult\": \"approved\"}',NULL,NULL,'2026-08-17 08:31:38','2026-08-17 08:31:38');
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
INSERT INTO `opinion_boxes` VALUES ('ob_gen001','关于小区增设电动车充电桩的提议','目前充电位不足，建议在不影响通行处增设10个智能充电桩。','res_002','fam_002','pc_demo001','2026-07-03 07:55:04','2026-07-13 07:55:04',75,61,8,6,'active',0,'2026-07-03 07:55:04','2026-07-03 07:55:04'),('ob_gen002','提升垃圾分类点环境','建议为垃圾房加装除臭和冲洗设备，改善夏季异味。','res_001','fam_001','pc_demo001','2026-07-14 22:58:40','2026-07-24 22:58:40',41,37,4,0,'active',0,'2026-07-14 22:58:40','2026-07-14 22:58:40'),('ob_gen003','希望延长健身器材区开放时间','晚间很多上班族想锻炼，建议开放至22:00。','res_003','fam_003','pc_demo001','2026-08-05 03:13:08','2026-08-11 03:13:08',108,93,5,10,'active',0,'2026-08-05 03:13:08','2026-08-05 03:13:08'),('ob_gen004','提议增设社区图书角','利用架空层设置共享书架，促进邻里阅读。','res_002','fam_002','pc_demo001','2026-08-01 07:24:02','2026-08-09 07:24:02',87,73,9,5,'active',0,'2026-08-01 07:24:02','2026-08-01 07:24:02'),('ob_gen005','关于规范访客停车的建议','建议访客车辆登记限时免费2小时，避免长期占用。','res_dev001','fam_dev001','pc_demo001','2026-07-19 09:02:39','2026-07-27 09:02:39',100,91,2,7,'active',0,'2026-07-19 09:02:39','2026-07-19 09:02:39'),('ob_gen006','希望增加儿童游乐设施','现有滑梯较旧，建议补充沙池和攀爬架。','res_dev001','fam_dev001','pc_demo001','2026-07-07 22:46:48','2026-07-14 22:46:48',31,26,4,1,'active',0,'2026-07-07 22:46:48','2026-07-07 22:46:48'),('ob_gen007','提议举办邻里美食节','促进邻里交流，建议每季度办一次。','res_001','fam_001','pc_demo001','2026-07-12 23:19:22','2026-07-18 23:19:22',41,40,0,1,'active',0,'2026-07-12 23:19:22','2026-07-12 23:19:22'),('ob_gen008','关于加强夜间巡逻的提议','建议增加保安夜间巡逻频次，提升安全感。','res_003','fam_003','pc_demo001','2026-07-15 05:44:52','2026-07-24 05:44:52',76,60,3,13,'active',0,'2026-07-15 05:44:52','2026-07-15 05:44:52');
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
INSERT INTO `order_items` VALUES ('oi_000001','ord_000001','prd_seed_laundry','29.9',NULL,3,29.90,89.70,'2026-08-01 21:44:43','2026-08-01 21:44:43'),('oi_000002','ord_000001','prd_seed_cola','18.0',NULL,2,18.00,36.00,'2026-08-01 21:44:43','2026-08-01 21:44:43'),('oi_000003','ord_000002','prd_007','28.0',NULL,1,28.00,28.00,'2026-07-14 03:56:57','2026-07-14 03:56:57'),('oi_000004','ord_000002','prd_009','8.0',NULL,3,8.00,24.00,'2026-07-14 03:56:57','2026-07-14 03:56:57'),('oi_000005','ord_000003','prd_007','28.0',NULL,2,28.00,56.00,'2026-07-09 11:51:22','2026-07-09 11:51:22'),('oi_000006','ord_000003','prd_009','8.0',NULL,3,8.00,24.00,'2026-07-09 11:51:22','2026-07-09 11:51:22'),('oi_000007','ord_000004','prd_seed_cola','18.0',NULL,3,18.00,54.00,'2026-07-10 19:33:51','2026-07-10 19:33:51'),('oi_000008','ord_000004','prd_001','28.0',NULL,2,28.00,56.00,'2026-07-10 19:33:51','2026-07-10 19:33:51'),('oi_000009','ord_000005','prd_003','45.0',NULL,2,45.00,90.00,'2026-08-09 10:26:45','2026-08-09 10:26:45'),('oi_000010','ord_000005','prd_seed_rice','39.9',NULL,2,39.90,79.80,'2026-08-09 10:26:45','2026-08-09 10:26:45'),('oi_000011','ord_000006','prd_seed_laundry','29.9',NULL,1,29.90,29.90,'2026-07-25 21:59:36','2026-07-25 21:59:36'),('oi_000012','ord_000007','prd_002','89.9',NULL,2,89.90,179.80,'2026-08-09 19:04:38','2026-08-09 19:04:38'),('oi_000013','ord_000007','prd_005','15.9',NULL,2,15.90,31.80,'2026-08-09 19:04:38','2026-08-09 19:04:38'),('oi_000014','ord_000007','prd_seed_rice','39.9',NULL,2,39.90,79.80,'2026-08-09 19:04:38','2026-08-09 19:04:38'),('oi_000015','ord_000008','prd_009','8.0',NULL,3,8.00,24.00,'2026-07-05 15:51:51','2026-07-05 15:51:51'),('oi_000016','ord_000008','prd_008','35.0',NULL,3,35.00,105.00,'2026-07-05 15:51:51','2026-07-05 15:51:51'),('oi_000017','ord_000008','prd_007','28.0',NULL,3,28.00,84.00,'2026-07-05 15:51:51','2026-07-05 15:51:51'),('oi_000018','ord_000009','prd_009','8.0',NULL,3,8.00,24.00,'2026-07-12 16:07:09','2026-07-12 16:07:09'),('oi_000019','ord_000009','prd_007','28.0',NULL,2,28.00,56.00,'2026-07-12 16:07:09','2026-07-12 16:07:09'),('oi_000020','ord_000010','prd_008','35.0',NULL,2,35.00,70.00,'2026-07-10 12:49:26','2026-07-10 12:49:26'),('oi_000021','ord_000010','prd_006','22.0',NULL,2,22.00,44.00,'2026-07-10 12:49:26','2026-07-10 12:49:26'),('oi_000022','ord_000011','prd_008','35.0',NULL,3,35.00,105.00,'2026-07-27 07:48:04','2026-07-27 07:48:04'),('oi_000023','ord_000011','prd_006','22.0',NULL,1,22.00,22.00,'2026-07-27 07:48:04','2026-07-27 07:48:04'),('oi_000024','ord_000012','prd_seed_cola','18.0',NULL,3,18.00,54.00,'2026-07-25 04:03:18','2026-07-25 04:03:18'),('oi_000025','ord_000012','prd_seed_rice','39.9',NULL,2,39.90,79.80,'2026-07-25 04:03:18','2026-07-25 04:03:18'),('oi_000026','ord_000013','prd_006','22.0',NULL,2,22.00,44.00,'2026-07-18 17:14:29','2026-07-18 17:14:29'),('oi_000027','ord_000013','prd_007','28.0',NULL,2,28.00,56.00,'2026-07-18 17:14:29','2026-07-18 17:14:29'),('oi_000028','ord_000013','prd_009','8.0',NULL,2,8.00,16.00,'2026-07-18 17:14:29','2026-07-18 17:14:29'),('oi_000029','ord_000014','prd_003','45.0',NULL,1,45.00,45.00,'2026-07-29 02:48:15','2026-07-29 02:48:15'),('oi_000030','ord_000015','prd_009','8.0',NULL,3,8.00,24.00,'2026-07-07 18:15:20','2026-07-07 18:15:20'),('oi_000031','ord_000015','prd_008','35.0',NULL,3,35.00,105.00,'2026-07-07 18:15:20','2026-07-07 18:15:20'),('oi_000032','ord_000015','prd_007','28.0',NULL,1,28.00,28.00,'2026-07-07 18:15:20','2026-07-07 18:15:20'),('oi_000033','ord_000016','prd_007','28.0',NULL,2,28.00,56.00,'2026-07-24 13:13:31','2026-07-24 13:13:31'),('oi_000034','ord_000016','prd_009','8.0',NULL,3,8.00,24.00,'2026-07-24 13:13:31','2026-07-24 13:13:31'),('oi_000035','ord_000016','prd_006','22.0',NULL,2,22.00,44.00,'2026-07-24 13:13:31','2026-07-24 13:13:31'),('oi_000036','ord_000017','prd_003','45.0',NULL,1,45.00,45.00,'2026-07-22 19:55:58','2026-07-22 19:55:58'),('oi_000037','ord_000017','prd_005','15.9',NULL,1,15.90,15.90,'2026-07-22 19:55:58','2026-07-22 19:55:58'),('oi_000038','ord_000017','prd_002','89.9',NULL,1,89.90,89.90,'2026-07-22 19:55:58','2026-07-22 19:55:58'),('oi_000039','ord_000018','prd_004','12.5',NULL,3,12.50,37.50,'2026-07-13 14:24:04','2026-07-13 14:24:04'),('oi_000040','ord_000018','prd_seed_rice','39.9',NULL,2,39.90,79.80,'2026-07-13 14:24:04','2026-07-13 14:24:04'),('oi_000041','ord_000018','prd_002','89.9',NULL,3,89.90,269.70,'2026-07-13 14:24:04','2026-07-13 14:24:04'),('oi_000042','ord_000019','prd_003','45.0',NULL,2,45.00,90.00,'2026-08-06 14:39:05','2026-08-06 14:39:05'),('oi_000043','ord_000019','prd_seed_rice','39.9',NULL,3,39.90,119.70,'2026-08-06 14:39:05','2026-08-06 14:39:05'),('oi_000044','ord_000020','prd_008','35.0',NULL,2,35.00,70.00,'2026-07-21 13:00:30','2026-07-21 13:00:30'),('oi_000045','ord_000020','prd_009','8.0',NULL,1,8.00,8.00,'2026-07-21 13:00:30','2026-07-21 13:00:30'),('oi_000046','ord_000020','prd_006','22.0',NULL,1,22.00,22.00,'2026-07-21 13:00:30','2026-07-21 13:00:30'),('oi_000047','ord_000021','prd_008','35.0',NULL,2,35.00,70.00,'2026-07-11 08:35:40','2026-07-11 08:35:40'),('oi_000048','ord_000021','prd_009','8.0',NULL,3,8.00,24.00,'2026-07-11 08:35:40','2026-07-11 08:35:40'),('oi_000049','ord_000021','prd_006','22.0',NULL,1,22.00,22.00,'2026-07-11 08:35:40','2026-07-11 08:35:40'),('oi_000050','ord_000022','prd_006','22.0',NULL,1,22.00,22.00,'2026-07-05 17:48:57','2026-07-05 17:48:57'),('oi_000051','ord_000023','prd_003','45.0',NULL,2,45.00,90.00,'2026-07-19 22:34:26','2026-07-19 22:34:26'),('oi_000052','ord_000024','prd_006','22.0',NULL,3,22.00,66.00,'2026-07-21 17:23:40','2026-07-21 17:23:40'),('oi_000053','ord_000024','prd_008','35.0',NULL,3,35.00,105.00,'2026-07-21 17:23:40','2026-07-21 17:23:40'),('oi_000054','ord_000025','prd_002','89.9',NULL,3,89.90,269.70,'2026-07-16 06:22:17','2026-07-16 06:22:17'),('oi_000055','ord_000026','prd_008','35.0',NULL,1,35.00,35.00,'2026-07-15 17:22:21','2026-07-15 17:22:21'),('oi_000056','ord_000027','prd_seed_rice','39.9',NULL,1,39.90,39.90,'2026-08-09 22:58:22','2026-08-09 22:58:22'),('oi_000057','ord_000027','prd_seed_towel','12.9',NULL,2,12.90,25.80,'2026-08-09 22:58:22','2026-08-09 22:58:22'),('oi_000058','ord_000027','prd_seed_cola','18.0',NULL,3,18.00,54.00,'2026-08-09 22:58:22','2026-08-09 22:58:22'),('oi_000059','ord_000028','prd_003','45.0',NULL,1,45.00,45.00,'2026-07-08 04:49:44','2026-07-08 04:49:44'),('oi_000060','ord_000029','prd_003','45.0',NULL,3,45.00,135.00,'2026-07-16 14:26:48','2026-07-16 14:26:48'),('oi_000061','ord_000030','prd_006','22.0',NULL,2,22.00,44.00,'2026-07-24 22:35:12','2026-07-24 22:35:12'),('oi_000062','ord_000030','prd_008','35.0',NULL,3,35.00,105.00,'2026-07-24 22:35:12','2026-07-24 22:35:12'),('oi_000063','ord_000030','prd_009','8.0',NULL,1,8.00,8.00,'2026-07-24 22:35:12','2026-07-24 22:35:12'),('oi_000064','ord_000031','prd_009','8.0',NULL,3,8.00,24.00,'2026-07-28 09:33:59','2026-07-28 09:33:59'),('oi_000065','ord_000032','prd_008','35.0',NULL,2,35.00,70.00,'2026-08-03 04:37:32','2026-08-03 04:37:32'),('oi_000066','ord_000032','prd_006','22.0',NULL,3,22.00,66.00,'2026-08-03 04:37:32','2026-08-03 04:37:32'),('oi_000067','ord_000032','prd_007','28.0',NULL,2,28.00,56.00,'2026-08-03 04:37:32','2026-08-03 04:37:32'),('oi_000068','ord_000033','prd_008','35.0',NULL,2,35.00,70.00,'2026-07-04 23:31:58','2026-07-04 23:31:58'),('oi_000069','ord_000033','prd_007','28.0',NULL,3,28.00,84.00,'2026-07-04 23:31:58','2026-07-04 23:31:58'),('oi_000070','ord_000033','prd_009','8.0',NULL,2,8.00,16.00,'2026-07-04 23:31:58','2026-07-04 23:31:58'),('oi_000071','ord_000034','prd_007','28.0',NULL,1,28.00,28.00,'2026-07-18 08:32:20','2026-07-18 08:32:20'),('oi_000072','ord_000035','prd_008','35.0',NULL,1,35.00,35.00,'2026-07-25 20:06:35','2026-07-25 20:06:35'),('oi_000073','ord_000036','prd_006','22.0',NULL,2,22.00,44.00,'2026-08-01 10:34:11','2026-08-01 10:34:11'),('oi_000074','ord_000036','prd_007','28.0',NULL,2,28.00,56.00,'2026-08-01 10:34:11','2026-08-01 10:34:11'),('oi_000075','ord_000036','prd_009','8.0',NULL,3,8.00,24.00,'2026-08-01 10:34:11','2026-08-01 10:34:11'),('oi_000076','ord_000037','prd_002','89.9',NULL,2,89.90,179.80,'2026-07-16 11:21:08','2026-07-16 11:21:08'),('oi_000077','ord_000037','prd_005','15.9',NULL,3,15.90,47.70,'2026-07-16 11:21:08','2026-07-16 11:21:08'),('oi_000078','ord_000038','prd_003','45.0',NULL,1,45.00,45.00,'2026-07-03 13:26:10','2026-07-03 13:26:10'),('oi_000079','ord_000038','prd_001','28.0',NULL,3,28.00,84.00,'2026-07-03 13:26:10','2026-07-03 13:26:10'),('oi_000080','ord_000038','prd_seed_rice','39.9',NULL,3,39.90,119.70,'2026-07-03 13:26:10','2026-07-03 13:26:10'),('oi_000081','ord_000039','prd_seed_rice','39.9',NULL,2,39.90,79.80,'2026-07-28 17:53:52','2026-07-28 17:53:52'),('oi_000082','ord_000040','prd_002','89.9',NULL,3,89.90,269.70,'2026-08-05 14:23:44','2026-08-05 14:23:44'),('oi_000083','ord_000041','prd_008','35.0',NULL,1,35.00,35.00,'2026-08-07 02:04:53','2026-08-07 02:04:53'),('oi_000084','ord_000041','prd_006','22.0',NULL,1,22.00,22.00,'2026-08-07 02:04:53','2026-08-07 02:04:53'),('oi_000085','ord_000042','prd_002','89.9',NULL,1,89.90,89.90,'2026-08-07 02:35:47','2026-08-07 02:35:47'),('oi_000086','ord_000042','prd_seed_rice','39.9',NULL,1,39.90,39.90,'2026-08-07 02:35:47','2026-08-07 02:35:47'),('oi_000087','ord_000043','prd_008','35.0',NULL,1,35.00,35.00,'2026-07-28 14:12:45','2026-07-28 14:12:45'),('oi_000088','ord_000044','prd_008','35.0',NULL,1,35.00,35.00,'2026-07-30 09:28:20','2026-07-30 09:28:20'),('oi_000089','ord_000045','prd_seed_laundry','29.9',NULL,2,29.90,59.80,'2026-08-06 03:58:02','2026-08-06 03:58:02'),('oi_000090','ord_000046','prd_007','28.0',NULL,1,28.00,28.00,'2026-07-26 17:15:53','2026-07-26 17:15:53'),('oi_000091','ord_000046','prd_006','22.0',NULL,3,22.00,66.00,'2026-07-26 17:15:53','2026-07-26 17:15:53'),('oi_000092','ord_000047','prd_009','8.0',NULL,2,8.00,16.00,'2026-07-04 05:29:30','2026-07-04 05:29:30'),('oi_000093','ord_000047','prd_006','22.0',NULL,1,22.00,22.00,'2026-07-04 05:29:30','2026-07-04 05:29:30'),('oi_000094','ord_000048','prd_006','22.0',NULL,3,22.00,66.00,'2026-07-30 06:20:06','2026-07-30 06:20:06'),('oi_000095','ord_000048','prd_008','35.0',NULL,3,35.00,105.00,'2026-07-30 06:20:06','2026-07-30 06:20:06'),('oi_000096','ord_000048','prd_007','28.0',NULL,2,28.00,56.00,'2026-07-30 06:20:06','2026-07-30 06:20:06'),('oi_000097','ord_000049','prd_001','28.0',NULL,2,28.00,56.00,'2026-07-17 16:49:19','2026-07-17 16:49:19'),('oi_000098','ord_000050','prd_002','89.9',NULL,2,89.90,179.80,'2026-08-04 21:57:29','2026-08-04 21:57:29'),('oi_000099','ord_000051','prd_006','22.0',NULL,2,22.00,44.00,'2026-07-04 23:57:41','2026-07-04 23:57:41'),('oi_000100','ord_000052','prd_007','28.0',NULL,2,28.00,56.00,'2026-07-07 12:35:38','2026-07-07 12:35:38'),('oi_000101','ord_000052','prd_009','8.0',NULL,3,8.00,24.00,'2026-07-07 12:35:38','2026-07-07 12:35:38'),('oi_000102','ord_000053','prd_003','45.0',NULL,3,45.00,135.00,'2026-08-08 20:30:35','2026-08-08 20:30:35'),('oi_000103','ord_000053','prd_seed_laundry','29.9',NULL,2,29.90,59.80,'2026-08-08 20:30:35','2026-08-08 20:30:35'),('oi_000104','ord_000053','prd_seed_liquid','15.9',NULL,3,15.90,47.70,'2026-08-08 20:30:35','2026-08-08 20:30:35'),('oi_000105','ord_000054','prd_seed_laundry','29.9',NULL,3,29.90,89.70,'2026-08-07 10:12:00','2026-08-07 10:12:00'),('oi_000106','ord_000054','prd_002','89.9',NULL,2,89.90,179.80,'2026-08-07 10:12:00','2026-08-07 10:12:00'),('oi_000107','ord_000054','prd_003','45.0',NULL,3,45.00,135.00,'2026-08-07 10:12:00','2026-08-07 10:12:00'),('oi_000108','ord_000055','prd_008','35.0',NULL,1,35.00,35.00,'2026-08-10 11:05:29','2026-08-10 11:05:29'),('oi_000109','ord_000055','prd_006','22.0',NULL,3,22.00,66.00,'2026-08-10 11:05:29','2026-08-10 11:05:29'),('oi_000110','ord_000056','prd_seed_rice','39.9',NULL,2,39.90,79.80,'2026-07-03 02:01:20','2026-07-03 02:01:20'),('oi_000111','ord_000056','prd_002','89.9',NULL,1,89.90,89.90,'2026-07-03 02:01:20','2026-07-03 02:01:20'),('oi_000112','ord_000057','prd_seed_rice','39.9',NULL,3,39.90,119.70,'2026-07-03 00:31:23','2026-07-03 00:31:23'),('oi_000113','ord_000058','prd_006','22.0',NULL,1,22.00,22.00,'2026-07-24 04:57:31','2026-07-24 04:57:31'),('oi_000114','ord_000059','prd_001','28.0',NULL,2,28.00,56.00,'2026-07-18 11:22:37','2026-07-18 11:22:37'),('oi_000115','ord_000060','prd_008','35.0',NULL,2,35.00,70.00,'2026-07-26 14:39:28','2026-07-26 14:39:28'),('oi_000116','ord_000060','prd_007','28.0',NULL,3,28.00,84.00,'2026-07-26 14:39:28','2026-07-26 14:39:28'),('oi_000117','ord_000061','prd_008','35.0',NULL,3,35.00,105.00,'2026-07-16 06:01:17','2026-07-16 06:01:17'),('oi_000118','ord_000062','prd_seed_rice','39.9',NULL,3,39.90,119.70,'2026-07-06 01:31:52','2026-07-06 01:31:52'),('oi_000119','ord_000062','prd_seed_cola','18.0',NULL,3,18.00,54.00,'2026-07-06 01:31:52','2026-07-06 01:31:52'),('oi_000120','ord_000062','prd_001','28.0',NULL,1,28.00,28.00,'2026-07-06 01:31:52','2026-07-06 01:31:52'),('oi_000121','ord_000063','prd_002','89.9',NULL,3,89.90,269.70,'2026-07-15 09:49:49','2026-07-15 09:49:49'),('oi_000122','ord_000063','prd_003','45.0',NULL,3,45.00,135.00,'2026-07-15 09:49:49','2026-07-15 09:49:49'),('oi_000123','ord_000064','prd_007','28.0',NULL,3,28.00,84.00,'2026-07-22 05:55:11','2026-07-22 05:55:11'),('oi_000124','ord_000064','prd_009','8.0',NULL,2,8.00,16.00,'2026-07-22 05:55:11','2026-07-22 05:55:11'),('oi_000125','ord_000064','prd_006','22.0',NULL,3,22.00,66.00,'2026-07-22 05:55:11','2026-07-22 05:55:11'),('oi_000126','ord_000065','prd_008','35.0',NULL,3,35.00,105.00,'2026-07-25 04:35:55','2026-07-25 04:35:55'),('oi_000127','ord_000065','prd_006','22.0',NULL,3,22.00,66.00,'2026-07-25 04:35:55','2026-07-25 04:35:55'),('oi_000128','ord_000066','prd_005','15.9',NULL,1,15.90,15.90,'2026-07-31 02:09:46','2026-07-31 02:09:46'),('oi_000129','ord_000066','prd_seed_liquid','15.9',NULL,2,15.90,31.80,'2026-07-31 02:09:46','2026-07-31 02:09:46'),('oi_000130','ord_000066','prd_004','12.5',NULL,1,12.50,12.50,'2026-07-31 02:09:46','2026-07-31 02:09:46'),('oi_000131','ord_000067','prd_005','15.9',NULL,3,15.90,47.70,'2026-08-02 17:12:51','2026-08-02 17:12:51'),('oi_000132','ord_000068','prd_008','35.0',NULL,3,35.00,105.00,'2026-07-22 23:42:43','2026-07-22 23:42:43'),('oi_000133','ord_000068','prd_006','22.0',NULL,2,22.00,44.00,'2026-07-22 23:42:43','2026-07-22 23:42:43'),('oi_000134','ord_000068','prd_009','8.0',NULL,3,8.00,24.00,'2026-07-22 23:42:43','2026-07-22 23:42:43'),('oi_000135','ord_000069','prd_002','89.9',NULL,3,89.90,269.70,'2026-07-17 08:44:23','2026-07-17 08:44:23'),('oi_000136','ord_000069','prd_005','15.9',NULL,3,15.90,47.70,'2026-07-17 08:44:23','2026-07-17 08:44:23'),('oi_000137','ord_000070','prd_005','15.9',NULL,1,15.90,15.90,'2026-07-04 00:35:11','2026-07-04 00:35:11'),('oi_000138','ord_000070','prd_002','89.9',NULL,3,89.90,269.70,'2026-07-04 00:35:11','2026-07-04 00:35:11'),('oi_000139','ord_000070','prd_001','28.0',NULL,1,28.00,28.00,'2026-07-04 00:35:11','2026-07-04 00:35:11'),('oi_000140','ord_000071','prd_seed_liquid','15.9',NULL,1,15.90,15.90,'2026-07-18 05:36:42','2026-07-18 05:36:42'),('oi_000141','ord_000072','prd_seed_rice','39.9',NULL,1,39.90,39.90,'2026-07-14 13:17:53','2026-07-14 13:17:53'),('oi_000142','ord_000073','prd_002','89.9',NULL,2,89.90,179.80,'2026-07-13 12:41:24','2026-07-13 12:41:24'),('oi_000143','ord_000073','prd_005','15.9',NULL,2,15.90,31.80,'2026-07-13 12:41:24','2026-07-13 12:41:24'),('oi_000144','ord_000074','prd_006','22.0',NULL,1,22.00,22.00,'2026-07-10 13:07:14','2026-07-10 13:07:14'),('oi_000145','ord_000075','prd_seed_laundry','29.9',NULL,3,29.90,89.70,'2026-07-13 18:26:57','2026-07-13 18:26:57'),('oi_000146','ord_000075','prd_001','28.0',NULL,3,28.00,84.00,'2026-07-13 18:26:57','2026-07-13 18:26:57'),('oi_000147','ord_000076','prd_006','22.0',NULL,2,22.00,44.00,'2026-08-08 16:19:22','2026-08-08 16:19:22'),('oi_000148','ord_000076','prd_009','8.0',NULL,1,8.00,8.00,'2026-08-08 16:19:22','2026-08-08 16:19:22'),('oi_000149','ord_000077','prd_003','45.0',NULL,2,45.00,90.00,'2026-07-12 12:26:09','2026-07-12 12:26:09'),('oi_000150','ord_000077','prd_002','89.9',NULL,3,89.90,269.70,'2026-07-12 12:26:09','2026-07-12 12:26:09'),('oi_000151','ord_000077','prd_005','15.9',NULL,2,15.90,31.80,'2026-07-12 12:26:09','2026-07-12 12:26:09'),('oi_000152','ord_000078','prd_007','28.0',NULL,3,28.00,84.00,'2026-07-15 16:39:23','2026-07-15 16:39:23'),('oi_000153','ord_000079','prd_003','45.0',NULL,1,45.00,45.00,'2026-08-07 13:00:31','2026-08-07 13:00:31'),('oi_000154','ord_000080','prd_seed_cola','18.0',NULL,2,18.00,36.00,'2026-07-13 09:06:14','2026-07-13 09:06:14'),('oi_000155','ord_000080','prd_seed_towel','12.9',NULL,3,12.90,38.70,'2026-07-13 09:06:14','2026-07-13 09:06:14'),('oi_000156','ord_000081','prd_009','8.0',NULL,1,8.00,8.00,'2026-07-21 09:11:58','2026-07-21 09:11:58'),('oi_000157','ord_000082','prd_seed_towel','12.9',NULL,3,12.90,38.70,'2026-08-08 19:22:09','2026-08-08 19:22:09'),('oi_000158','ord_000082','prd_seed_laundry','29.9',NULL,3,29.90,89.70,'2026-08-08 19:22:09','2026-08-08 19:22:09'),('oi_000159','ord_000082','prd_001','28.0',NULL,2,28.00,56.00,'2026-08-08 19:22:09','2026-08-08 19:22:09'),('oi_000160','ord_000083','prd_seed_liquid','15.9',NULL,2,15.90,31.80,'2026-07-25 05:49:06','2026-07-25 05:49:06'),('oi_000161','ord_000084','prd_008','35.0',NULL,2,35.00,70.00,'2026-07-03 00:43:18','2026-07-03 00:43:18'),('oi_000162','ord_000084','prd_009','8.0',NULL,1,8.00,8.00,'2026-07-03 00:43:18','2026-07-03 00:43:18'),('oi_000163','ord_000084','prd_007','28.0',NULL,1,28.00,28.00,'2026-07-03 00:43:18','2026-07-03 00:43:18'),('oi_000164','ord_000085','prd_008','35.0',NULL,2,35.00,70.00,'2026-07-10 16:02:48','2026-07-10 16:02:48'),('oi_000165','ord_000085','prd_006','22.0',NULL,1,22.00,22.00,'2026-07-10 16:02:48','2026-07-10 16:02:48'),('oi_000166','ord_000085','prd_009','8.0',NULL,2,8.00,16.00,'2026-07-10 16:02:48','2026-07-10 16:02:48'),('oi_000167','ord_000086','prd_002','89.9',NULL,3,89.90,269.70,'2026-07-21 03:35:09','2026-07-21 03:35:09'),('oi_000168','ord_000086','prd_004','12.5',NULL,1,12.50,12.50,'2026-07-21 03:35:09','2026-07-21 03:35:09'),('oi_000169','ord_000086','prd_005','15.9',NULL,1,15.90,15.90,'2026-07-21 03:35:09','2026-07-21 03:35:09'),('oi_000170','ord_000087','prd_seed_cola','18.0',NULL,3,18.00,54.00,'2026-07-03 19:30:01','2026-07-03 19:30:01'),('oi_000171','ord_000088','prd_seed_towel','12.9',NULL,2,12.90,25.80,'2026-08-06 15:39:12','2026-08-06 15:39:12'),('oi_000172','ord_000088','prd_seed_liquid','15.9',NULL,1,15.90,15.90,'2026-08-06 15:39:12','2026-08-06 15:39:12'),('oi_000173','ord_000088','prd_005','15.9',NULL,2,15.90,31.80,'2026-08-06 15:39:12','2026-08-06 15:39:12'),('oi_000174','ord_000089','prd_seed_rice','39.9',NULL,1,39.90,39.90,'2026-07-05 23:08:53','2026-07-05 23:08:53'),('oi_000175','ord_000089','prd_005','15.9',NULL,2,15.90,31.80,'2026-07-05 23:08:53','2026-07-05 23:08:53'),('oi_000176','ord_000089','prd_seed_laundry','29.9',NULL,2,29.90,59.80,'2026-07-05 23:08:53','2026-07-05 23:08:53'),('oi_000177','ord_000090','prd_006','22.0',NULL,1,22.00,22.00,'2026-07-12 15:58:27','2026-07-12 15:58:27'),('oi_000178','ord_000090','prd_008','35.0',NULL,3,35.00,105.00,'2026-07-12 15:58:27','2026-07-12 15:58:27'),('oi_000179','ord_000090','prd_009','8.0',NULL,1,8.00,8.00,'2026-07-12 15:58:27','2026-07-12 15:58:27'),('oi_000180','ord_000091','prd_seed_liquid','15.9',NULL,2,15.90,31.80,'2026-08-05 08:09:22','2026-08-05 08:09:22'),('oi_000181','ord_000092','prd_003','45.0',NULL,1,45.00,45.00,'2026-07-14 08:33:26','2026-07-14 08:33:26'),('oi_000182','ord_000093','prd_007','28.0',NULL,1,28.00,28.00,'2026-08-01 20:40:32','2026-08-01 20:40:32'),('oi_000183','ord_000093','prd_008','35.0',NULL,3,35.00,105.00,'2026-08-01 20:40:32','2026-08-01 20:40:32'),('oi_000184','ord_000094','prd_seed_cola','18.0',NULL,1,18.00,18.00,'2026-08-10 13:50:24','2026-08-10 13:50:24'),('oi_000185','ord_000095','prd_007','28.0',NULL,3,28.00,84.00,'2026-07-29 18:21:53','2026-07-29 18:21:53'),('oi_000186','ord_000095','prd_006','22.0',NULL,1,22.00,22.00,'2026-07-29 18:21:53','2026-07-29 18:21:53'),('oi_000187','ord_000095','prd_009','8.0',NULL,2,8.00,16.00,'2026-07-29 18:21:53','2026-07-29 18:21:53'),('oi_000188','ord_000096','prd_009','8.0',NULL,2,8.00,16.00,'2026-08-04 10:17:05','2026-08-04 10:17:05'),('oi_000189','ord_000096','prd_006','22.0',NULL,2,22.00,44.00,'2026-08-04 10:17:05','2026-08-04 10:17:05'),('oi_000190','ord_000096','prd_007','28.0',NULL,2,28.00,56.00,'2026-08-04 10:17:05','2026-08-04 10:17:05'),('oi_000191','ord_000097','prd_002','89.9',NULL,2,89.90,179.80,'2026-08-04 14:26:20','2026-08-04 14:26:20'),('oi_000192','ord_000098','prd_006','22.0',NULL,1,22.00,22.00,'2026-07-08 15:51:17','2026-07-08 15:51:17'),('oi_000193','ord_000098','prd_009','8.0',NULL,3,8.00,24.00,'2026-07-08 15:51:17','2026-07-08 15:51:17'),('oi_000194','ord_000098','prd_008','35.0',NULL,2,35.00,70.00,'2026-07-08 15:51:17','2026-07-08 15:51:17'),('oi_000195','ord_000099','prd_seed_cola','18.0',NULL,2,18.00,36.00,'2026-07-28 13:24:16','2026-07-28 13:24:16'),('oi_000196','ord_000099','prd_seed_laundry','29.9',NULL,3,29.90,89.70,'2026-07-28 13:24:16','2026-07-28 13:24:16'),('oi_000197','ord_000100','prd_001','28.0',NULL,3,28.00,84.00,'2026-07-03 02:08:20','2026-07-03 02:08:20'),('oi_000198','ord_000100','prd_seed_liquid','15.9',NULL,3,15.90,47.70,'2026-07-03 02:08:20','2026-07-03 02:08:20'),('oi_000199','ord_000100','prd_002','89.9',NULL,1,89.90,89.90,'2026-07-03 02:08:20','2026-07-03 02:08:20'),('oi_000200','ord_000101','prd_008','35.0',NULL,3,35.00,105.00,'2026-07-09 18:02:12','2026-07-09 18:02:12'),('oi_000201','ord_000101','prd_006','22.0',NULL,3,22.00,66.00,'2026-07-09 18:02:12','2026-07-09 18:02:12'),('oi_000202','ord_000101','prd_007','28.0',NULL,2,28.00,56.00,'2026-07-09 18:02:12','2026-07-09 18:02:12'),('oi_000203','ord_000102','prd_seed_laundry','29.9',NULL,1,29.90,29.90,'2026-08-04 21:14:05','2026-08-04 21:14:05'),('oi_000204','ord_000103','prd_005','15.9',NULL,1,15.90,15.90,'2026-07-22 19:37:52','2026-07-22 19:37:52'),('oi_000205','ord_000103','prd_001','28.0',NULL,1,28.00,28.00,'2026-07-22 19:37:52','2026-07-22 19:37:52'),('oi_000206','ord_000104','prd_009','8.0',NULL,2,8.00,16.00,'2026-07-28 12:53:45','2026-07-28 12:53:45'),('oi_000207','ord_000105','prd_008','35.0',NULL,2,35.00,70.00,'2026-07-28 00:29:13','2026-07-28 00:29:13'),('oi_000208','ord_000105','prd_006','22.0',NULL,3,22.00,66.00,'2026-07-28 00:29:13','2026-07-28 00:29:13'),('oi_000209','ord_000105','prd_009','8.0',NULL,1,8.00,8.00,'2026-07-28 00:29:13','2026-07-28 00:29:13'),('oi_000210','ord_000106','prd_006','22.0',NULL,3,22.00,66.00,'2026-08-01 09:33:57','2026-08-01 09:33:57'),('oi_000211','ord_000106','prd_007','28.0',NULL,1,28.00,28.00,'2026-08-01 09:33:57','2026-08-01 09:33:57'),('oi_000212','ord_000106','prd_008','35.0',NULL,2,35.00,70.00,'2026-08-01 09:33:57','2026-08-01 09:33:57'),('oi_000213','ord_000107','prd_006','22.0',NULL,2,22.00,44.00,'2026-07-20 09:30:06','2026-07-20 09:30:06'),('oi_000214','ord_000107','prd_007','28.0',NULL,3,28.00,84.00,'2026-07-20 09:30:06','2026-07-20 09:30:06'),('oi_000215','ord_000108','prd_seed_towel','12.9',NULL,1,12.90,12.90,'2026-07-31 21:51:57','2026-07-31 21:51:57'),('oi_000216','ord_000108','prd_seed_rice','39.9',NULL,1,39.90,39.90,'2026-07-31 21:51:57','2026-07-31 21:51:57'),('oi_000217','ord_000109','prd_006','22.0',NULL,1,22.00,22.00,'2026-07-07 13:24:54','2026-07-07 13:24:54'),('oi_000218','ord_000109','prd_009','8.0',NULL,1,8.00,8.00,'2026-07-07 13:24:54','2026-07-07 13:24:54'),('oi_000219','ord_000109','prd_008','35.0',NULL,1,35.00,35.00,'2026-07-07 13:24:54','2026-07-07 13:24:54'),('oi_000220','ord_000110','prd_seed_liquid','15.9',NULL,2,15.90,31.80,'2026-07-21 04:55:15','2026-07-21 04:55:15'),('oi_000221','ord_000110','prd_004','12.5',NULL,1,12.50,12.50,'2026-07-21 04:55:15','2026-07-21 04:55:15'),('oi_000222','ord_000110','prd_seed_cola','18.0',NULL,3,18.00,54.00,'2026-07-21 04:55:15','2026-07-21 04:55:15'),('oi_000223','ord_000111','prd_004','12.5',NULL,1,12.50,12.50,'2026-08-07 06:35:22','2026-08-07 06:35:22'),('oi_000224','ord_000111','prd_seed_liquid','15.9',NULL,1,15.90,15.90,'2026-08-07 06:35:22','2026-08-07 06:35:22'),('oi_000225','ord_000111','prd_003','45.0',NULL,1,45.00,45.00,'2026-08-07 06:35:22','2026-08-07 06:35:22'),('oi_000226','ord_000112','prd_006','22.0',NULL,1,22.00,22.00,'2026-07-10 17:05:24','2026-07-10 17:05:24'),('oi_000227','ord_000112','prd_008','35.0',NULL,1,35.00,35.00,'2026-07-10 17:05:24','2026-07-10 17:05:24'),('oi_000228','ord_000113','prd_009','8.0',NULL,3,8.00,24.00,'2026-07-08 05:36:05','2026-07-08 05:36:05'),('oi_000229','ord_000113','prd_006','22.0',NULL,1,22.00,22.00,'2026-07-08 05:36:05','2026-07-08 05:36:05'),('oi_000230','ord_000114','prd_seed_towel','12.9',NULL,3,12.90,38.70,'2026-07-07 08:49:58','2026-07-07 08:49:58'),('oi_000231','ord_000115','prd_006','22.0',NULL,1,22.00,22.00,'2026-07-24 18:06:56','2026-07-24 18:06:56'),('oi_000232','ord_000115','prd_009','8.0',NULL,3,8.00,24.00,'2026-07-24 18:06:56','2026-07-24 18:06:56'),('oi_000233','ord_000116','prd_006','22.0',NULL,3,22.00,66.00,'2026-07-26 17:39:17','2026-07-26 17:39:17'),('oi_000234','ord_000117','prd_009','8.0',NULL,3,8.00,24.00,'2026-07-23 06:32:55','2026-07-23 06:32:55'),('oi_000235','ord_000118','prd_seed_liquid','15.9',NULL,2,15.90,31.80,'2026-07-08 07:29:06','2026-07-08 07:29:06'),('oi_000236','ord_000118','prd_seed_laundry','29.9',NULL,3,29.90,89.70,'2026-07-08 07:29:06','2026-07-08 07:29:06'),('oi_000237','ord_000118','prd_seed_towel','12.9',NULL,3,12.90,38.70,'2026-07-08 07:29:06','2026-07-08 07:29:06'),('oi_000238','ord_000119','prd_seed_towel','12.9',NULL,2,12.90,25.80,'2026-07-12 05:55:12','2026-07-12 05:55:12'),('oi_000239','ord_000120','prd_seed_laundry','29.9',NULL,2,29.90,59.80,'2026-07-24 20:07:16','2026-07-24 20:07:16'),('oi_07141000','ord_008a1000','prd_357991e8e01000','优惠券10元','/static/default/product.png',1,0.08,0.08,'2026-09-15 06:10:02','2026-09-15 06:10:02'),('oi_0bcc1000','ord_0bc81000','prd_357991e8e01000','优惠券10元','/static/default/product.png',1,0.08,0.08,'2026-09-15 06:10:03','2026-09-15 06:10:03'),('oi_102e1000','ord_102c1000','prd_357991e8e01000','优惠券10元','/static/default/product.png',1,0.08,0.08,'2026-09-15 06:10:03','2026-09-15 06:10:03'),('oi_129e1000','ord_129c1000','prd_357991e8e01000','优惠券10元','/static/default/product.png',1,0.08,0.08,'2026-09-15 06:10:04','2026-09-15 06:10:04'),('oi_1b141000','ord_1aaa1000','prd_006','红烧牛肉面',NULL,1,20.00,20.00,'2026-09-04 08:13:27','2026-09-04 08:13:27'),('oi_30041000','ord_30021000','prd_357991e8e01000','优惠券10元','/static/default/product.png',1,0.08,0.08,'2026-09-06 02:20:28','2026-09-06 02:20:28'),('oi_351c88bf181000','ord_351c88bee41000','prd_seed_cola','可口可乐330ml*6罐',NULL,1,16.00,16.00,'2026-08-13 02:13:15','2026-08-13 02:13:15'),('oi_351e9bf4341000','ord_351e9bf41e1000','prd_006','红烧牛肉面',NULL,1,20.00,20.00,'2026-08-13 07:03:22','2026-08-13 07:03:22'),('oi_3520305d8c1000','ord_3520305d321000','prd_006','红烧牛肉面',NULL,1,20.00,20.00,'2026-08-13 10:44:14','2026-08-13 10:44:14'),('oi_3520b9aa161000','ord_3520b9a9f21000','prd_3520b9254c1000','813测试',NULL,1,0.10,0.10,'2026-08-13 11:59:13','2026-08-13 11:59:13'),('oi_3520bac73c1000','ord_3520bac7361000','prd_3520b9254c1000','813测试',NULL,1,0.10,0.10,'2026-08-13 11:59:49','2026-08-13 11:59:49'),('oi_3520bbb3d21000','ord_3520bbb3ce1000','prd_007','宫保鸡丁套餐',NULL,1,25.00,25.00,'2026-08-13 12:00:19','2026-08-13 12:00:19'),('oi_3520bc9e501000','ord_3520bc9e461000','prd_004','康师傅方便面5连包',NULL,1,11.00,11.00,'2026-08-13 12:00:49','2026-08-13 12:00:49'),('oi_3520cb570a1000','ord_3520cb57041000','prd_3520b9254c1000','813测试',NULL,1,0.10,0.10,'2026-08-13 12:08:52','2026-08-13 12:08:52'),('oi_35213bfd7e1000','ord_35213bfd541000','prd_seed_cola','可口可乐330ml*6罐',NULL,1,16.00,16.00,'2026-08-13 13:10:23','2026-08-13 13:10:23'),('oi_35213c4f361000','ord_35213c4f2a1000','prd_seed_cola','可口可乐330ml*6罐',NULL,1,16.00,16.00,'2026-08-13 13:10:34','2026-08-13 13:10:34'),('oi_352163fc1c1000','ord_352163fc121000','prd_3520b9254c1000','813测试',NULL,1,0.10,0.10,'2026-08-13 13:32:14','2026-08-13 13:32:14'),('oi_35216455a81000','ord_352164559e1000','prd_3520b9254c1000','813测试',NULL,1,0.10,0.10,'2026-08-13 13:32:25','2026-08-13 13:32:25'),('oi_352164e3821000','ord_352164e37c1000','prd_3520b9254c1000','813测试',NULL,1,0.10,0.10,'2026-08-13 13:32:43','2026-08-13 13:32:43'),('oi_352183f3cc1000','ord_352183f3ca1000','prd_3520b9254c1000','813测试',NULL,1,0.10,0.10,'2026-08-13 13:49:41','2026-08-13 13:49:41'),('oi_35266bd5041000','ord_35266bd4c41000','prd_3520b9254c1000','813测试',NULL,1,0.10,0.10,'2026-08-14 01:15:34','2026-08-14 01:15:34'),('oi_3526715cfe1000','ord_3526715cfa1000','prd_3520b9254c1000','813测试',NULL,1,0.10,0.10,'2026-08-14 01:18:35','2026-08-14 01:18:35'),('oi_35267afe521000','ord_35267afe501000','prd_3520b9254c1000','813测试',NULL,1,0.10,0.10,'2026-08-14 01:23:51','2026-08-14 01:23:51'),('oi_35268adb4a1000','ord_35268adb481000','prd_3520b9254c1000','813测试','/static/default/product.png',1,1.00,1.00,'2026-08-14 01:32:31','2026-08-14 01:32:31'),('oi_3526ad6bfe1000','ord_3526ad6bfc1000','prd_3520b9254c1000','813测试','/static/default/product.png',1,0.10,0.10,'2026-08-14 01:51:23','2026-08-14 01:51:23'),('oi_3526f3413e1000','ord_3526f3413a1000','prd_3520b9254c1000','813测试','/static/default/product.png',1,0.10,0.10,'2026-08-14 02:29:31','2026-08-14 02:29:31'),('oi_3526f46efa1000','ord_3526f46ef21000','prd_006','红烧牛肉面',NULL,1,20.00,20.00,'2026-08-14 02:30:10','2026-08-14 02:30:10'),('oi_35286996241000','ord_35286996141000','prd_009','冰镇柠檬水',NULL,1,7.00,7.00,'2026-08-14 05:53:58','2026-08-14 05:53:58'),('oi_35288a68d21000','ord_35288a68ce1000','prd_009','冰镇柠檬水',NULL,1,7.00,7.00,'2026-08-14 06:11:53','2026-08-14 06:11:53'),('oi_35288abeaa1000','ord_35288abea81000','prd_009','冰镇柠檬水',NULL,1,7.00,7.00,'2026-08-14 06:12:04','2026-08-14 06:12:04'),('oi_354fc89d041000','ord_354fc89cc21000','prd_009','冰镇柠檬水',NULL,1,7.00,7.00,'2026-08-18 01:38:27','2026-08-18 01:38:27'),('oi_354fe4d5a81000','ord_354fe4d5a41000','prd_009','冰镇柠檬水',NULL,1,7.00,7.00,'2026-08-18 01:53:52','2026-08-18 01:53:52'),('oi_35513f81841000','ord_35513f81481000','prd_009','冰镇柠檬水',NULL,1,7.00,7.00,'2026-08-18 05:03:12','2026-08-18 05:03:12'),('oi_355220b35a1000','ord_355220b31e1000','prd_009','冰镇柠檬水',NULL,1,7.00,7.00,'2026-08-18 07:06:11','2026-08-18 07:06:11'),('oi_3552221f481000','ord_3552221f381000','prd_009','冰镇柠檬水',NULL,1,7.00,7.00,'2026-08-18 07:06:57','2026-08-18 07:06:57'),('oi_355cf637b01000','ord_355cf6377e1000','prd_seed_cola','可口可乐330ml*6罐',NULL,1,16.00,16.00,'2026-08-19 08:20:53','2026-08-19 08:20:53'),('oi_3579946dac1000','ord_3579946d841000','prd_357991e8e01000','优惠券10元',NULL,1,0.09,0.09,'2026-08-22 03:01:59','2026-08-22 03:01:59'),('oi_357997afde1000','ord_357997afda1000','prd_357991e8e01000','优惠券10元',NULL,1,0.09,0.09,'2026-08-22 03:03:45','2026-08-22 03:03:45'),('oi_3579ade7861000','ord_3579ade7821000','prd_357991e8e01000','优惠券10元',NULL,1,0.09,0.09,'2026-08-22 03:15:53','2026-08-22 03:15:53'),('oi_357b1700921000','ord_357b1700801000','prd_357991e8e01000','优惠券10元',NULL,1,0.09,0.09,'2026-08-22 06:33:06','2026-08-22 06:33:06'),('oi_35838e40b21000','ord_35838e3ff01000','prd_007','宫保鸡丁套餐',NULL,1,25.00,25.00,'2026-08-23 02:16:42','2026-08-23 02:16:42'),('oi_358392e46a1000','ord_358392e4681000','prd_357991e8e01000','优惠券10元',NULL,1,0.09,0.09,'2026-08-23 02:19:14','2026-08-23 02:19:14'),('oi_358394263c1000','ord_35839426361000','prd_006','红烧牛肉面',NULL,1,20.00,20.00,'2026-08-23 02:19:56','2026-08-23 02:19:56'),('oi_35844476d81000','ord_35844476841000','prd_357991e8e01000','优惠券10元',NULL,1,0.09,0.09,'2026-08-23 03:56:13','2026-08-23 03:56:13'),('oi_3584bb47661000','ord_3584bb47601000','prd_357991e8e01000','优惠券10元',NULL,1,0.09,0.09,'2026-08-23 05:01:06','2026-08-23 05:01:06'),('oi_3584bdea4a1000','ord_3584bdea3c1000','prd_357991e8e01000','优惠券10元',NULL,1,0.09,0.09,'2026-08-23 05:02:33','2026-08-23 05:02:33'),('oi_3584bed6121000','ord_3584bed60c1000','prd_357991e8e01000','优惠券10元',NULL,1,0.09,0.09,'2026-08-23 05:03:03','2026-08-23 05:03:03'),('oi_35851742b41000','ord_358517427e1000','prd_357991e8e01000','优惠券10元',NULL,1,0.09,0.09,'2026-08-23 05:51:20','2026-08-23 05:51:20'),('oi_358517a0cc1000','ord_358517a0c61000','prd_006','红烧牛肉面',NULL,1,20.00,20.00,'2026-08-23 05:51:32','2026-08-23 05:51:32'),('oi_35851e602e1000','ord_35851e60281000','prd_357991e8e01000','优惠券10元',NULL,1,0.09,0.09,'2026-08-23 05:55:14','2026-08-23 05:55:14'),('oi_35851e73b01000','ord_35851e73aa1000','prd_357991e8e01000','优惠券10元',NULL,1,0.09,0.09,'2026-08-23 05:55:16','2026-08-23 05:55:16'),('oi_3585253c401000','ord_3585253c3c1000','prd_357991e8e01000','优惠券10元',NULL,1,0.09,0.09,'2026-08-23 05:58:58','2026-08-23 05:58:58'),('oi_3585315b521000','ord_3585315b4c1000','prd_357991e8e01000','优惠券10元',NULL,1,0.09,0.09,'2026-08-23 06:05:36','2026-08-23 06:05:36'),('oi_358531aa961000','ord_358531aa921000','prd_006','红烧牛肉面',NULL,1,20.00,20.00,'2026-08-23 06:05:46','2026-08-23 06:05:46'),('oi_358533a5d81000','ord_358533a5ce1000','prd_006','红烧牛肉面',NULL,1,20.00,20.00,'2026-08-23 06:06:51','2026-08-23 06:06:51'),('oi_35854036661000','ord_35854036621000','prd_006','红烧牛肉面',NULL,1,20.00,20.00,'2026-08-23 06:13:42','2026-08-23 06:13:42'),('oi_358609a9b41000','ord_358609a9a81000','prd_006','红烧牛肉面',NULL,1,20.00,20.00,'2026-08-23 08:03:43','2026-08-23 08:03:43'),('oi_358628c4ce1000','ord_358628c4cc1000','prd_006','红烧牛肉面',NULL,1,20.00,20.00,'2026-08-23 08:20:43','2026-08-23 08:20:43'),('oi_358628e0fc1000','ord_358628e0fa1000','prd_007','宫保鸡丁套餐',NULL,1,25.00,25.00,'2026-08-23 08:20:46','2026-08-23 08:20:46'),('oi_35863064a41000','ord_358630648a1000','prd_009','冰镇柠檬水',NULL,1,7.00,7.00,'2026-08-23 08:24:53','2026-08-23 08:24:53'),('oi_35863441c61000','ord_35863441be1000','prd_357991e8e01000','优惠券10元',NULL,1,0.09,0.09,'2026-08-23 08:26:59','2026-08-23 08:26:59'),('oi_358f14401a1000','ord_358f143fb21000','prd_357991e8e01000','优惠券10元','/static/default/product.png',1,0.09,0.09,'2026-08-24 05:07:48','2026-08-24 05:07:48'),('oi_358f1a66aa1000','ord_358f1a669e1000','prd_357991e8e01000','优惠券10元','/static/default/product.png',1,0.09,0.09,'2026-08-24 05:11:09','2026-08-24 05:11:09'),('oi_358f3e6da01000','ord_358f3e6d9a1000','prd_357991e8e01000','优惠券10元','/static/default/product.png',1,0.09,0.09,'2026-08-24 05:30:50','2026-08-24 05:30:50'),('oi_35ce6a6fc41000','ord_35ce6a6f921000','prd_35cb71ebc41000','水饺',NULL,1,10.00,10.00,'2026-08-30 08:42:54','2026-08-30 08:42:54'),('oi_35ce6aeb881000','ord_35ce6aeb821000','prd_357991e8e01000','优惠券10元','/static/default/product.png',1,0.09,0.09,'2026-08-30 08:43:10','2026-08-30 08:43:10'),('oi_35d14493f41000','ord_35d14493e61000','prd_35cb71ebc41000','水饺',NULL,1,10.00,10.00,'2026-08-30 15:21:40','2026-08-30 15:21:40'),('oi_35d14511e41000','ord_35d14511e01000','prd_35cb72c10e1000','小面',NULL,1,8.00,8.00,'2026-08-30 15:21:56','2026-08-30 15:21:56'),('oi_35d86348d21000','ord_35d86348b01000','prd_seed_cola','可口可乐330ml*6罐',NULL,1,16.00,16.00,'2026-08-31 07:57:06','2026-08-31 07:57:06'),('oi_35d87258621000','ord_35d872585e1000','prd_seed_cola','可口可乐330ml*6罐',NULL,1,16.00,16.00,'2026-08-31 08:05:20','2026-08-31 08:05:20'),('oi_35d875ba3a1000','ord_35d875ba381000','prd_seed_cola','可口可乐330ml*6罐',NULL,1,16.00,16.00,'2026-08-31 08:07:10','2026-08-31 08:07:10'),('oi_369b7a2d7c1000','ord_369b7a2c601000','prd_357991e8e01000','优惠券10元','/static/default/product.png',1,0.08,0.08,'2026-09-19 06:32:35','2026-09-19 06:32:35'),('oi_369b7e38ce1002','ord_369b7e38ce1000','prd_357991e8e01000','优惠券10元','/static/default/product.png',1,0.08,0.08,'2026-09-19 06:34:47','2026-09-19 06:34:47'),('oi_369bd9ccea1000','ord_369bd9ccd41000','prd_357991e8e01000','优惠券10元','/static/default/product.png',1,0.08,0.08,'2026-09-19 07:24:48','2026-09-19 07:24:48'),('oi_369c9ea04e1000','ord_369c9ea0341000','dpr_seed0003','本色抽纸 30 包',NULL,1,31.00,31.00,'2026-09-19 09:12:18','2026-09-19 09:12:18'),('oi_369cfeb66e1000','ord_369cfeb66a1000','prd_357991e8e01000','优惠券10元','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch03_369c1da6d21000.jpg',1,0.08,0.08,'2026-09-19 10:04:46','2026-09-19 10:04:46'),('oi_369d00aae41002','ord_369d00aae41000','prd_357991e8e01000','优惠券10元','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch03_369c1da6d21000.jpg',1,0.08,0.08,'2026-09-19 10:05:50','2026-09-19 10:05:50'),('oi_36bcbe74e21000','ord_36bcbe74321000','dpr_seed0003','本色抽纸 30 包',NULL,1,31.00,31.00,'2026-09-22 12:03:36','2026-09-22 12:03:36'),('oi_36bcbfab601000','ord_36bcbfab5e1000','dpr_seed0003','本色抽纸 30 包',NULL,1,31.00,31.00,'2026-09-22 12:04:16','2026-09-22 12:04:16'),('oi_36bcc766d61000','ord_36bcc766d01000','dpr_seed0003','本色抽纸 30 包',NULL,1,31.00,31.00,'2026-09-22 12:08:29','2026-09-22 12:08:29'),('oi_36cd384bc01000','ord_36cd384b201000','dpr_seed0003','本色抽纸 30 包',NULL,1,31.00,31.00,'2026-09-24 02:27:06','2026-09-24 02:27:06'),('oi_59cc1000','ord_593c1000','prd_006','红烧牛肉面',NULL,1,20.00,20.00,'2026-09-06 09:33:05','2026-09-06 09:33:05'),('oi_67701000','ord_67561000','prd_009','冰镇柠檬水',NULL,1,7.00,7.00,'2026-09-06 02:02:01','2026-09-06 02:02:01'),('oi_78b01000','ord_78ae1000','prd_357991e8e01000','优惠券10元','/static/default/product.png',1,0.08,0.08,'2026-09-09 13:34:27','2026-09-09 13:34:27'),('oi_825c1000','ord_821c1000','prd_009','冰镇柠檬水',NULL,1,7.00,7.00,'2026-09-11 08:51:20','2026-09-11 08:51:20'),('oi_af2e1000','ord_aed01000','prd_009','冰镇柠檬水',NULL,1,7.00,7.00,'2026-09-09 01:38:03','2026-09-09 01:38:03'),('oi_eb0c1000','ord_eaa61000','prd_357991e8e01000','优惠券10元','/static/default/product.png',1,0.08,0.08,'2026-09-09 13:34:09','2026-09-09 13:34:09');
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
  `resident_merchant_listing_id` varchar(32) DEFAULT NULL COMMENT '业主商户上架ID',
  `reseller_id` varchar(32) DEFAULT NULL COMMENT '分享业主商户住户ID',
  `property_company_id` varchar(32) NOT NULL COMMENT '按住户所属物业归属',
  `family_id` varchar(32) DEFAULT NULL,
  `order_status` varchar(20) DEFAULT 'pending' COMMENT 'pending/paid/delivering/completed/cancelled/refunding/refunded',
  `total_amount` decimal(10,2) NOT NULL,
  `product_amount` decimal(10,2) DEFAULT NULL COMMENT '商品优惠后成交小计（不含配送费，满额判断口径）',
  `delivery_fee` decimal(10,2) DEFAULT '0.00',
  `original_delivery_fee` decimal(10,2) DEFAULT NULL COMMENT '满额减免前基础配送费',
  `delivery_waiver_amount` decimal(10,2) DEFAULT '0.00' COMMENT '满额减免金额',
  `delivery_subsidy_sponsor` varchar(20) DEFAULT NULL COMMENT '减免承担方 none/merchant/property/platform',
  `delivery_subsidy_amount` decimal(10,2) DEFAULT '0.00' COMMENT '配送补贴金额（注入配送链路）',
  `delivery_settlement_base` decimal(10,2) DEFAULT NULL COMMENT '配送结算基数 = 配送费 + 补贴',
  `free_delivery_threshold_snapshot` decimal(10,2) DEFAULT NULL COMMENT '下单时满额门槛快照',
  `free_delivery_eligible` tinyint(1) DEFAULT NULL COMMENT '是否命中满额减免',
  `waiver_reason` varchar(50) DEFAULT NULL COMMENT '减免原因 threshold/campaign/manual/none/unknown',
  `pricing_rule_snapshot` text COMMENT '定价规则快照(JSON)',
  `distribution_rule_snapshot` text COMMENT '分账规则快照(JSON)',
  `calculation_version` varchar(20) DEFAULT NULL COMMENT '分账计算版本 legacy/distribution_b_v1',
  `payment_method` varchar(20) DEFAULT NULL COMMENT 'property_coin/point/wechat/mixed',
  `point_used` int DEFAULT '0' COMMENT '混合支付使用的积分',
  `coin_used` decimal(10,2) DEFAULT '0.00' COMMENT '混合支付使用的物业币',
  `directed_balance_used` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '定向余额抵扣金额',
  `directed_main_merchant_id` varchar(32) DEFAULT NULL COMMENT '定向余额所属主商家体系ID（退款原路退回依据）',
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
  `requires_delivery` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否需要配送（0 到店自提 / 1 需要配送）',
  `fulfillment_mode` varchar(20) DEFAULT NULL COMMENT '履约方式 pending_choice/merchant_self/courier_hall',
  `fulfillment_deadline` datetime DEFAULT NULL COMMENT '履约方式选择截止时间（超时默认平台配送）',
  `fulfillment_chosen_at` datetime DEFAULT NULL COMMENT '履约方式选定时间',
  `verified_at` datetime DEFAULT NULL COMMENT '商家核实通过时间',
  `verification_deadline` datetime DEFAULT NULL COMMENT '商家核实截止时间（下单后24h）',
  `freeze_end_date` datetime DEFAULT NULL COMMENT '冻结期结束时间（核实通过后N天）',
  `verified_by` varchar(32) DEFAULT NULL COMMENT '核实操作人ID',
  `verification_remark` varchar(200) DEFAULT NULL COMMENT '核实备注',
  `listing_id` varchar(32) DEFAULT NULL COMMENT '业主商户上架记录ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`),
  KEY `family_id` (`family_id`),
  KEY `idx_resident` (`resident_id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_status` (`order_status`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_resident_merchant_listing` (`resident_merchant_listing_id`),
  KEY `idx_reseller` (`reseller_id`),
  KEY `idx_orders_freeze_end` (`order_status`,`freeze_end_date`),
  KEY `idx_orders_verification_deadline` (`order_status`,`verification_deadline`),
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
INSERT INTO `orders` VALUES ('ord_000001','NO100001','res_002','mch_demo001',NULL,NULL,'pc_demo001','fam_002','completed',125.70,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',12570,0.00,0.00,NULL,0.00,'2026-08-01 21:44:43','2026-08-01 21:44:43','2026-08-01 21:44:43',NULL,NULL,125,0.00,'res_courier03','com_demo00110栋3单元602室','1804427827','工作日配送',0,'2026-08-01 21:44:43','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000002','NO100002','res_act01','mch_demo002',NULL,NULL,'pc_demo001',NULL,'completed',52.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',5200,0.00,0.00,NULL,0.00,'2026-07-14 03:56:57','2026-07-14 03:56:57','2026-07-14 03:56:57',NULL,NULL,52,0.00,'res_courier03','com_demo0019栋1单元1204室','1553555614','工作日配送',0,'2026-07-14 03:56:57','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000003','NO100003','res_courier02','mch_demo002',NULL,NULL,'pc_demo001',NULL,'delivering',80.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',4000,0.00,0.00,NULL,40.00,'2026-07-09 11:51:22','2026-07-09 11:51:22',NULL,NULL,NULL,80,0.00,'res_courier02','com_demo0013栋4单元1405室','1854770448','请尽快送达',0,'2026-07-09 11:51:22','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000004','NO100004','res_003','mch_demo001',NULL,NULL,'pc_demo001','fam_003','completed',110.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',11000,0.00,0.00,NULL,0.00,'2026-07-10 19:33:51','2026-07-10 19:33:51','2026-07-10 19:33:51',NULL,NULL,110,0.00,'res_courier02','com_demo00212栋1单元2603室','1784143645','放在门口即可',0,'2026-07-10 19:33:51','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000005','NO100005','res_courier03','mch_demo001',NULL,NULL,'pc_demo001',NULL,'completed',169.80,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,169.80,'2026-08-09 10:26:45','2026-08-09 10:26:45','2026-08-09 10:26:45',NULL,NULL,169,0.00,'res_courier01','com_demo0015栋2单元101室','1734552912','放在门口即可',0,'2026-08-09 10:26:45','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000006','NO100006','res_courier01','mch_demo001',NULL,NULL,'pc_demo001',NULL,'completed',29.90,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',1495,0.00,0.00,NULL,14.95,'2026-07-25 21:59:36','2026-07-25 21:59:36','2026-07-25 21:59:36',NULL,NULL,29,0.00,'res_courier03','com_demo00110栋3单元1602室','1568804797','工作日配送',0,'2026-07-25 21:59:36','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000007','NO100007','res_004','mch_demo001',NULL,NULL,'pc_demo001',NULL,'delivering',291.40,NULL,0.00,0.00,0.00,'none',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'distribution_b_v1','wechat',0,0.00,0.00,NULL,291.40,'2026-08-09 19:04:38',NULL,NULL,NULL,NULL,291,0.00,'res_courier02','com_demo0011栋2单元2203室','1982153819','放在门口即可',2,'2026-08-09 19:04:38','2026-08-18 04:43:26',1,'courier_hall','2026-08-18 12:43:22','2026-08-18 12:43:26',NULL,NULL,NULL,NULL,NULL,NULL),('ord_000008','NO100008','res_mch02','mch_demo002',NULL,NULL,'pc_demo001',NULL,'cancelled',213.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',10650,0.00,0.00,NULL,106.50,NULL,NULL,NULL,'2026-07-05 15:51:51','用户取消',0,0.00,NULL,'com_demo00112栋4单元2901室','1305815182','放在门口即可',0,'2026-07-05 15:51:51','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000009','NO100009','res_mch03','mch_demo002',NULL,NULL,'pc_demo001',NULL,'delivering',80.00,NULL,0.00,0.00,0.00,'none',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'distribution_b_v1','point',8000,0.00,0.00,NULL,0.00,'2026-07-12 16:07:09',NULL,NULL,NULL,NULL,80,0.00,'res_courier01','com_demo0018栋1单元1003室','1856988147','工作日配送',2,'2026-07-12 16:07:09','2026-08-18 04:43:26',1,'courier_hall','2026-08-18 12:43:22','2026-08-18 12:43:26',NULL,NULL,NULL,NULL,NULL,NULL),('ord_000010','NO100010','res_mch03','mch_demo002',NULL,NULL,'pc_demo001',NULL,'delivering',114.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',11400,0.00,0.00,NULL,0.00,'2026-07-10 12:49:26','2026-07-10 12:49:26',NULL,NULL,NULL,114,0.00,'res_courier02','com_demo0018栋4单元503室','1392197685','放在门口即可',0,'2026-07-10 12:49:26','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000011','NO100011','res_mch04','mch_demo002',NULL,NULL,'pc_demo001',NULL,'cancelled',127.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',12700,0.00,0.00,NULL,0.00,NULL,NULL,NULL,'2026-07-27 07:48:04','用户取消',0,0.00,NULL,'com_demo0014栋3单元602室','1324152569','放在门口即可',0,'2026-07-27 07:48:04','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000012','NO100012','res_002','mch_demo001',NULL,NULL,'pc_demo001','fam_002','pending',133.80,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',13380,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'com_demo00112栋4单元2606室','1516096863','请尽快送达',0,'2026-07-25 04:03:18','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000013','NO100013','res_dev004','mch_demo002',NULL,NULL,'pc_demo001',NULL,'cancelled',116.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,116.00,NULL,NULL,NULL,'2026-07-18 17:14:29','用户取消',0,0.00,NULL,'com_demo00111栋4单元2903室','1633747422','工作日配送',0,'2026-07-18 17:14:29','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000014','NO100014','res_courier03','mch_demo001',NULL,NULL,'pc_demo001',NULL,'delivering',45.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',2250,0.00,0.00,NULL,22.50,'2026-07-29 02:48:15','2026-07-29 02:48:15',NULL,NULL,NULL,45,0.00,'res_courier01','com_demo0015栋1单元605室','1778445810','工作日配送',0,'2026-07-29 02:48:15','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000015','NO100015','res_mch02','mch_demo002',NULL,NULL,'pc_demo001',NULL,'completed',157.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',15700,0.00,0.00,NULL,0.00,'2026-07-07 18:15:20','2026-07-07 18:15:20','2026-07-07 18:15:20',NULL,NULL,157,0.00,'res_courier01','com_demo0018栋4单元606室','1352245344','请尽快送达',0,'2026-07-07 18:15:20','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000016','NO100016','res_courier02','mch_demo002',NULL,NULL,'pc_demo001',NULL,'delivering',124.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',12400,0.00,0.00,NULL,0.00,'2026-07-24 13:13:31','2026-07-24 13:13:31',NULL,NULL,NULL,124,0.00,'res_courier01','com_demo0013栋1单元603室','1972367547','请尽快送达',0,'2026-07-24 13:13:31','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000017','NO100017','res_002','mch_demo001',NULL,NULL,'pc_demo001','fam_002','completed',150.80,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',7540,0.00,0.00,NULL,75.40,'2026-07-22 19:55:58','2026-07-22 19:55:58','2026-07-22 19:55:58',NULL,NULL,150,0.00,'res_courier01','com_demo00110栋1单元2401室','1660496251','请尽快送达',0,'2026-07-22 19:55:58','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000018','NO100018','res_001','mch_demo001',NULL,NULL,'pc_demo001','fam_001','completed',387.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,387.00,'2026-07-13 14:24:04','2026-07-13 14:24:04','2026-07-13 14:24:04',NULL,NULL,387,0.00,'res_courier01','com_demo0014栋3单元1206室','1558349408','工作日配送',0,'2026-07-13 14:24:04','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000019','NO100019','res_propadmin01','mch_demo001',NULL,NULL,'pc_demo001',NULL,'cancelled',209.70,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',10485,0.00,0.00,NULL,104.85,NULL,NULL,NULL,'2026-08-06 14:39:05','用户取消',0,0.00,NULL,'com_demo00112栋3单元1503室','1503559861','工作日配送',0,'2026-08-06 14:39:05','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000020','NO100020','res_mch02','mch_demo002',NULL,NULL,'pc_demo001',NULL,'cancelled',100.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,100.00,NULL,NULL,NULL,'2026-07-21 13:00:30','重复下单',0,0.00,NULL,'com_demo00110栋3单元3002室','1309594901','工作日配送',0,'2026-07-21 13:00:30','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000021','NO100021','res_mch04','mch_demo002',NULL,NULL,'pc_demo001',NULL,'completed',116.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,116.00,'2026-07-11 08:35:40','2026-07-11 08:35:40','2026-07-11 08:35:40',NULL,NULL,116,0.00,'res_courier02','com_demo0018栋4单元1402室','1725912531','工作日配送',0,'2026-07-11 08:35:40','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000022','NO100022','res_004','mch_demo002',NULL,NULL,'pc_demo001',NULL,'cancelled',22.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',2200,0.00,0.00,NULL,0.00,NULL,NULL,NULL,'2026-07-05 17:48:57','重复下单',0,0.00,NULL,'com_demo00110栋2单元2205室','1389293666','放在门口即可',0,'2026-07-05 17:48:57','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000023','NO100023','res_admin01','mch_demo001',NULL,NULL,'pc_demo001',NULL,'completed',90.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',4500,0.00,0.00,NULL,45.00,'2026-07-19 22:34:26','2026-07-19 22:34:26','2026-07-19 22:34:26',NULL,NULL,90,0.00,'res_courier02','com_demo0017栋1单元2705室','1326387483','放在门口即可',0,'2026-07-19 22:34:26','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000024','NO100024','res_mch01','mch_demo002',NULL,NULL,'pc_demo001',NULL,'delivering',171.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',17100,0.00,0.00,NULL,0.00,'2026-07-21 17:23:40','2026-07-21 17:23:40',NULL,NULL,NULL,171,0.00,'res_courier02','com_demo0017栋2单元1706室','1949279275','工作日配送',0,'2026-07-21 17:23:40','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000025','NO100025','res_mch03','mch_demo001',NULL,NULL,'pc_demo001',NULL,'completed',269.70,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',13485,0.00,0.00,NULL,134.85,'2026-07-16 06:22:17','2026-07-16 06:22:17','2026-07-16 06:22:17',NULL,NULL,269,0.00,'res_courier02','com_demo0019栋4单元901室','1517179318','请尽快送达',0,'2026-07-16 06:22:17','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000026','NO100026','res_dev002','mch_demo002',NULL,NULL,'pc_demo001',NULL,'completed',35.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,35.00,'2026-07-15 17:22:21','2026-07-15 17:22:21','2026-07-15 17:22:21',NULL,NULL,35,0.00,'res_courier01','com_demo0013栋3单元702室','1681967569','请尽快送达',0,'2026-07-15 17:22:21','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000027','NO100027','res_dev004','mch_demo001',NULL,NULL,'pc_demo001',NULL,'completed',119.70,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',5985,0.00,0.00,NULL,59.85,'2026-08-09 22:58:22','2026-08-09 22:58:22','2026-08-09 22:58:22',NULL,NULL,119,0.00,'res_courier03','com_demo0011栋3单元2904室','1987945972','放在门口即可',0,'2026-08-09 22:58:22','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000028','NO100028','res_tech01','mch_demo001',NULL,NULL,'pc_demo001',NULL,'cancelled',45.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',4500,0.00,0.00,NULL,0.00,NULL,NULL,NULL,'2026-07-08 04:49:44','商品缺货',0,0.00,NULL,'com_demo0016栋1单元403室','1847416205','工作日配送',0,'2026-07-08 04:49:44','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000029','NO100029','res_mch02','mch_demo001',NULL,NULL,'pc_demo001',NULL,'completed',135.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',13500,0.00,0.00,NULL,0.00,'2026-07-16 14:26:48','2026-07-16 14:26:48','2026-07-16 14:26:48',NULL,NULL,135,0.00,'res_courier02','com_demo0013栋3单元401室','1791932854','放在门口即可',0,'2026-07-16 14:26:48','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000030','NO100030','res_mch02','mch_demo002',NULL,NULL,'pc_demo001',NULL,'completed',157.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',15700,0.00,0.00,NULL,0.00,'2026-07-24 22:35:12','2026-07-24 22:35:12','2026-07-24 22:35:12',NULL,NULL,157,0.00,'res_courier03','com_demo0017栋4单元1801室','1347285548','工作日配送',0,'2026-07-24 22:35:12','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000031','NO100031','res_courier02','mch_demo002',NULL,NULL,'pc_demo001',NULL,'completed',24.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',2400,0.00,0.00,NULL,0.00,'2026-07-28 09:33:59','2026-07-28 09:33:59','2026-07-28 09:33:59',NULL,NULL,24,0.00,'res_courier02','com_demo0018栋4单元2201室','1505331834','工作日配送',0,'2026-07-28 09:33:59','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000032','NO100032','res_mch01','mch_demo002',NULL,NULL,'pc_demo001',NULL,'completed',192.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,192.00,'2026-08-03 04:37:32','2026-08-03 04:37:32','2026-08-03 04:37:32',NULL,NULL,192,0.00,'res_courier02','com_demo0019栋4单元1904室','1958267597','工作日配送',0,'2026-08-03 04:37:32','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000033','NO100033','res_courier03','mch_demo002',NULL,NULL,'pc_demo001',NULL,'completed',170.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',8500,0.00,0.00,NULL,85.00,'2026-07-04 23:31:58','2026-07-04 23:31:58','2026-07-04 23:31:58',NULL,NULL,170,0.00,'res_courier01','com_demo00111栋2单元2906室','1917452812','放在门口即可',0,'2026-07-04 23:31:58','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000034','NO100034','res_admin01','mch_demo002',NULL,NULL,'pc_demo001',NULL,'pending',28.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,28.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'com_demo0018栋2单元2604室','1685416599','请尽快送达',0,'2026-07-18 08:32:20','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000035','NO100035','res_ind01','mch_demo002',NULL,NULL,'pc_demo001',NULL,'completed',35.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',3500,0.00,0.00,NULL,0.00,'2026-07-25 20:06:35','2026-07-25 20:06:35','2026-07-25 20:06:35',NULL,NULL,35,0.00,'res_courier02','com_demo0011栋4单元203室','1715010256','请尽快送达',0,'2026-07-25 20:06:35','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000036','NO100036','res_courier03','mch_demo002',NULL,NULL,'pc_demo001',NULL,'completed',124.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,124.00,'2026-08-01 10:34:11','2026-08-01 10:34:11','2026-08-01 10:34:11',NULL,NULL,124,0.00,'res_courier02','com_demo0018栋4单元2304室','1360436977','工作日配送',0,'2026-08-01 10:34:11','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000037','NO100037','res_tech01','mch_demo001',NULL,NULL,'pc_demo001',NULL,'delivering',227.50,NULL,0.00,0.00,0.00,'none',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'distribution_b_v1','mixed',11375,0.00,0.00,NULL,113.75,'2026-07-16 11:21:08',NULL,NULL,NULL,NULL,227,0.00,'res_courier02','com_demo0011栋4单元204室','1354918986','放在门口即可',2,'2026-07-16 11:21:08','2026-08-18 04:43:26',1,'courier_hall','2026-08-18 12:43:22','2026-08-18 12:43:26',NULL,NULL,NULL,NULL,NULL,NULL),('ord_000038','NO100038','res_courier01','mch_demo001',NULL,NULL,'pc_demo001',NULL,'completed',248.70,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,248.70,'2026-07-03 13:26:10','2026-07-03 13:26:10','2026-07-03 13:26:10',NULL,NULL,248,0.00,'res_courier01','com_demo0019栋1单元1306室','1890228110','放在门口即可',0,'2026-07-03 13:26:10','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000039','NO100039','res_002','mch_demo001',NULL,NULL,'pc_demo001','fam_002','completed',79.80,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',3990,0.00,0.00,NULL,39.90,'2026-07-28 17:53:52','2026-07-28 17:53:52','2026-07-28 17:53:52',NULL,NULL,79,0.00,'res_courier01','com_demo0019栋2单元1202室','1726612304','请尽快送达',0,'2026-07-28 17:53:52','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000040','NO100040','res_mch02','mch_demo001',NULL,NULL,'pc_demo001',NULL,'delivering',269.70,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',26970,0.00,0.00,NULL,0.00,'2026-08-05 14:23:44','2026-08-05 14:23:44',NULL,NULL,NULL,269,0.00,'res_courier03','com_demo0012栋4单元2406室','1367249445','放在门口即可',0,'2026-08-05 14:23:44','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000041','NO100041','res_003','mch_demo002',NULL,NULL,'pc_demo001','fam_003','completed',57.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',5700,0.00,0.00,NULL,0.00,'2026-08-07 02:04:53','2026-08-07 02:04:53','2026-08-07 02:04:53',NULL,NULL,57,0.00,'res_courier03','com_demo0026栋3单元601室','1961029222','工作日配送',0,'2026-08-07 02:04:53','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000042','NO100042','res_dev003','mch_demo001',NULL,NULL,'pc_demo001',NULL,'delivering',129.80,NULL,0.00,0.00,0.00,'none',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'distribution_b_v1','wechat',0,0.00,0.00,NULL,129.80,'2026-08-07 02:35:47',NULL,NULL,NULL,NULL,129,0.00,'res_courier03','com_demo00110栋2单元1702室','1342718298','工作日配送',2,'2026-08-07 02:35:47','2026-08-18 04:43:26',1,'courier_hall','2026-08-18 12:43:22','2026-08-18 12:43:26',NULL,NULL,NULL,NULL,NULL,NULL),('ord_000043','NO100043','res_dev003','mch_demo002',NULL,NULL,'pc_demo001',NULL,'completed',35.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,35.00,'2026-07-28 14:12:45','2026-07-28 14:12:45','2026-07-28 14:12:45',NULL,NULL,35,0.00,'res_courier01','com_demo0014栋3单元2101室','1888325890','放在门口即可',0,'2026-07-28 14:12:45','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000044','NO100044','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','cancelled',35.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',3500,0.00,0.00,NULL,0.00,NULL,NULL,NULL,'2026-07-30 09:28:20','用户取消',0,0.00,NULL,'com_demo0014栋2单元205室','1353641042','请尽快送达',0,'2026-07-30 09:28:20','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000045','NO100045','res_dev003','mch_demo001',NULL,NULL,'pc_demo001',NULL,'delivering',59.80,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,59.80,'2026-08-06 03:58:02','2026-08-06 03:58:02',NULL,NULL,NULL,59,0.00,'res_courier03','com_demo00111栋1单元1301室','1380125402','放在门口即可',0,'2026-08-06 03:58:02','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000046','NO100046','res_courier02','mch_demo002',NULL,NULL,'pc_demo001',NULL,'delivering',94.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',9400,0.00,0.00,NULL,0.00,'2026-07-26 17:15:53','2026-07-26 17:15:53',NULL,NULL,NULL,94,0.00,'res_courier03','com_demo0011栋4单元2403室','1358583064','放在门口即可',0,'2026-07-26 17:15:53','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000047','NO100047','res_001','mch_demo002',NULL,NULL,'pc_demo001','fam_001','delivering',38.00,NULL,0.00,0.00,0.00,'none',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'distribution_b_v1','mixed',1900,0.00,0.00,NULL,19.00,'2026-07-04 05:29:30',NULL,NULL,NULL,NULL,38,0.00,'res_courier02','com_demo0019栋4单元2605室','1945600610','工作日配送',2,'2026-07-04 05:29:30','2026-08-18 04:43:26',1,'courier_hall','2026-08-18 12:43:22','2026-08-18 12:43:26',NULL,NULL,NULL,NULL,NULL,NULL),('ord_000048','NO100048','res_propadmin01','mch_demo002',NULL,NULL,'pc_demo001',NULL,'completed',227.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',11350,0.00,0.00,NULL,113.50,'2026-07-30 06:20:06','2026-07-30 06:20:06','2026-07-30 06:20:06',NULL,NULL,227,0.00,'res_courier02','com_demo0013栋1单元1702室','1391790004','请尽快送达',0,'2026-07-30 06:20:06','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000049','NO100049','res_dev001','mch_demo001',NULL,NULL,'pc_demo001','fam_dev001','completed',56.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',5600,0.00,0.00,NULL,0.00,'2026-07-17 16:49:19','2026-07-17 16:49:19','2026-07-17 16:49:19',NULL,NULL,56,0.00,'res_courier02','com_demo0012栋3单元1502室','1661449179','请尽快送达',0,'2026-07-17 16:49:19','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000050','NO100050','res_sec01','mch_demo001',NULL,NULL,'pc_demo001',NULL,'completed',179.80,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',17980,0.00,0.00,NULL,0.00,'2026-08-04 21:57:29','2026-08-04 21:57:29','2026-08-04 21:57:29',NULL,NULL,179,0.00,'res_courier03','com_demo0013栋1单元1701室','1566788662','工作日配送',0,'2026-08-04 21:57:29','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000051','NO100051','res_mch04','mch_demo002',NULL,NULL,'pc_demo001',NULL,'completed',44.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',4400,0.00,0.00,NULL,0.00,'2026-07-04 23:57:41','2026-07-04 23:57:41','2026-07-04 23:57:41',NULL,NULL,44,0.00,'res_courier02','com_demo00112栋1单元1604室','1500023679','工作日配送',0,'2026-07-04 23:57:41','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000052','NO100052','res_mch02','mch_demo002',NULL,NULL,'pc_demo001',NULL,'completed',80.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',4000,0.00,0.00,NULL,40.00,'2026-07-07 12:35:38','2026-07-07 12:35:38','2026-07-07 12:35:38',NULL,NULL,80,0.00,'res_courier01','com_demo0014栋4单元2403室','1629848965','请尽快送达',0,'2026-07-07 12:35:38','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000053','NO100053','res_courier03','mch_demo001',NULL,NULL,'pc_demo001',NULL,'pending',242.50,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',24250,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'com_demo0017栋3单元202室','1383028947','请尽快送达',0,'2026-08-08 20:30:35','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000054','NO100054','res_dev002','mch_demo001',NULL,NULL,'pc_demo001',NULL,'delivering',404.50,NULL,0.00,0.00,0.00,'none',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'distribution_b_v1','mixed',20225,0.00,0.00,NULL,202.25,'2026-08-07 10:12:00',NULL,NULL,NULL,NULL,404,0.00,'res_courier01','com_demo0014栋3单元2805室','1797537574','请尽快送达',2,'2026-08-07 10:12:00','2026-08-18 04:43:26',1,'courier_hall','2026-08-18 12:43:22','2026-08-18 12:43:26',NULL,NULL,NULL,NULL,NULL,NULL),('ord_000055','NO100055','res_courier02','mch_demo002',NULL,NULL,'pc_demo001',NULL,'completed',101.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,101.00,'2026-08-10 11:05:29','2026-08-10 11:05:29','2026-08-10 11:05:29',NULL,NULL,101,0.00,'res_courier02','com_demo0012栋3单元701室','1752877359','请尽快送达',0,'2026-08-10 11:05:29','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000056','NO100056','res_sec01','mch_demo001',NULL,NULL,'pc_demo001',NULL,'completed',169.70,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',16970,0.00,0.00,NULL,0.00,'2026-07-03 02:01:20','2026-07-03 02:01:20','2026-07-03 02:01:20',NULL,NULL,169,0.00,'res_courier02','com_demo0014栋2单元3005室','1610341730','请尽快送达',0,'2026-07-03 02:01:20','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000057','NO100057','res_mch02','mch_demo001',NULL,NULL,'pc_demo001',NULL,'completed',119.70,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',11970,0.00,0.00,NULL,0.00,'2026-07-03 00:31:23','2026-07-03 00:31:23','2026-07-03 00:31:23',NULL,NULL,119,0.00,'res_courier01','com_demo0014栋3单元203室','1564821789','请尽快送达',0,'2026-07-03 00:31:23','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000058','NO100058','res_mch04','mch_demo002',NULL,NULL,'pc_demo001',NULL,'pending',22.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',1100,0.00,0.00,NULL,11.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'com_demo0014栋4单元201室','1914709169','工作日配送',0,'2026-07-24 04:57:31','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000059','NO100059','res_dev002','mch_demo001',NULL,NULL,'pc_demo001',NULL,'pending',56.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',2800,0.00,0.00,NULL,28.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'com_demo0012栋4单元1401室','1678580585','工作日配送',0,'2026-07-18 11:22:37','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000060','NO100060','res_act01','mch_demo002',NULL,NULL,'pc_demo001',NULL,'completed',154.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,154.00,'2026-07-26 14:39:28','2026-07-26 14:39:28','2026-07-26 14:39:28',NULL,NULL,154,0.00,'res_courier02','com_demo00112栋4单元1706室','1583033662','工作日配送',0,'2026-07-26 14:39:28','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000061','NO100061','res_mch03','mch_demo002',NULL,NULL,'pc_demo001',NULL,'pending',105.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',10500,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'com_demo00112栋2单元901室','1740105307','请尽快送达',0,'2026-07-16 06:01:17','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000062','NO100062','res_dev003','mch_demo001',NULL,NULL,'pc_demo001',NULL,'delivering',201.70,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',20170,0.00,0.00,NULL,0.00,'2026-07-06 01:31:52','2026-07-06 01:31:52',NULL,NULL,NULL,201,0.00,'res_courier02','com_demo0018栋1单元3006室','1567680553','工作日配送',0,'2026-07-06 01:31:52','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000063','NO100063','res_mch04','mch_demo001',NULL,NULL,'pc_demo001',NULL,'delivering',404.70,NULL,0.00,0.00,0.00,'none',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'distribution_b_v1','mixed',20235,0.00,0.00,NULL,202.35,'2026-07-15 09:49:49',NULL,NULL,NULL,NULL,404,0.00,'res_courier02','com_demo0019栋3单元1004室','1589933353','放在门口即可',2,'2026-07-15 09:49:49','2026-08-18 04:43:26',1,'courier_hall','2026-08-18 12:43:22','2026-08-18 12:43:26',NULL,NULL,NULL,NULL,NULL,NULL),('ord_000064','NO100064','res_dev002','mch_demo002',NULL,NULL,'pc_demo001',NULL,'completed',166.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',16600,0.00,0.00,NULL,0.00,'2026-07-22 05:55:11','2026-07-22 05:55:11','2026-07-22 05:55:11',NULL,NULL,166,0.00,'res_courier02','com_demo0019栋2单元903室','1851953228','放在门口即可',0,'2026-07-22 05:55:11','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000065','NO100065','res_courier02','mch_demo002',NULL,NULL,'pc_demo001',NULL,'pending',171.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,171.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'com_demo00112栋4单元2202室','1885901730','放在门口即可',0,'2026-07-25 04:35:55','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000066','NO100066','res_dev003','mch_demo001',NULL,NULL,'pc_demo001',NULL,'cancelled',60.20,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',3010,0.00,0.00,NULL,30.10,NULL,NULL,NULL,'2026-07-31 02:09:46','重复下单',0,0.00,NULL,'com_demo0019栋4单元1802室','1886165148','工作日配送',0,'2026-07-31 02:09:46','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000067','NO100067','res_mch03','mch_demo001',NULL,NULL,'pc_demo001',NULL,'cancelled',47.70,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',4770,0.00,0.00,NULL,0.00,NULL,NULL,NULL,'2026-08-02 17:12:51','商品缺货',0,0.00,NULL,'com_demo00112栋4单元2603室','1650488411','请尽快送达',0,'2026-08-02 17:12:51','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000068','NO100068','res_dev003','mch_demo002',NULL,NULL,'pc_demo001',NULL,'pending',173.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',17300,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'com_demo0019栋4单元501室','1598750747','请尽快送达',0,'2026-07-22 23:42:43','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000069','NO100069','res_dev003','mch_demo001',NULL,NULL,'pc_demo001',NULL,'completed',317.40,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,317.40,'2026-07-17 08:44:23','2026-07-17 08:44:23','2026-07-17 08:44:23',NULL,NULL,317,0.00,'res_courier01','com_demo0012栋3单元905室','1731644727','放在门口即可',0,'2026-07-17 08:44:23','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000070','NO100070','res_mch01','mch_demo001',NULL,NULL,'pc_demo001',NULL,'completed',313.60,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',31360,0.00,0.00,NULL,0.00,'2026-07-04 00:35:11','2026-07-04 00:35:11','2026-07-04 00:35:11',NULL,NULL,313,0.00,'res_courier01','com_demo00112栋2单元1004室','1789019571','放在门口即可',0,'2026-07-04 00:35:11','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000071','NO100071','res_001','mch_demo001',NULL,NULL,'pc_demo001','fam_001','completed',15.90,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,15.90,'2026-07-18 05:36:42','2026-07-18 05:36:42','2026-07-18 05:36:42',NULL,NULL,15,0.00,'res_courier01','com_demo0011栋3单元2505室','1995353271','放在门口即可',0,'2026-07-18 05:36:42','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000072','NO100072','res_courier02','mch_demo001',NULL,NULL,'pc_demo001',NULL,'completed',39.90,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',1995,0.00,0.00,NULL,19.95,'2026-07-14 13:17:53','2026-07-14 13:17:53','2026-07-14 13:17:53',NULL,NULL,39,0.00,'res_courier02','com_demo00110栋1单元3003室','1637915839','请尽快送达',0,'2026-07-14 13:17:53','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000073','NO100073','res_courier01','mch_demo001',NULL,NULL,'pc_demo001',NULL,'pending',211.60,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',21160,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'com_demo0015栋3单元1905室','1726838170','工作日配送',0,'2026-07-13 12:41:24','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000074','NO100074','res_mch04','mch_demo002',NULL,NULL,'pc_demo001',NULL,'cancelled',22.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,22.00,NULL,NULL,NULL,'2026-07-10 13:07:14','用户取消',0,0.00,NULL,'com_demo0014栋1单元1006室','1318769453','工作日配送',0,'2026-07-10 13:07:14','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000075','NO100075','res_001','mch_demo001',NULL,NULL,'pc_demo001','fam_001','completed',173.70,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',17370,0.00,0.00,NULL,0.00,'2026-07-13 18:26:57','2026-07-13 18:26:57','2026-07-13 18:26:57',NULL,NULL,173,0.00,'res_courier02','com_demo00112栋2单元2501室','1399292759','工作日配送',0,'2026-07-13 18:26:57','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000076','NO100076','res_courier02','mch_demo002',NULL,NULL,'pc_demo001',NULL,'completed',52.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,52.00,'2026-08-08 16:19:22','2026-08-08 16:19:22','2026-08-08 16:19:22',NULL,NULL,52,0.00,'res_courier02','com_demo0018栋3单元506室','1737409429','请尽快送达',0,'2026-08-08 16:19:22','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000077','NO100077','res_mch01','mch_demo001',NULL,NULL,'pc_demo001',NULL,'delivering',391.50,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',39150,0.00,0.00,NULL,0.00,'2026-07-12 12:26:09','2026-07-12 12:26:09',NULL,NULL,NULL,391,0.00,'res_courier02','com_demo00112栋3单元202室','1718352957','工作日配送',0,'2026-07-12 12:26:09','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000078','NO100078','res_003','mch_demo002',NULL,NULL,'pc_demo001','fam_003','delivering',84.00,NULL,0.00,0.00,0.00,'none',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'distribution_b_v1','wechat',0,0.00,0.00,NULL,84.00,'2026-07-15 16:39:23',NULL,NULL,NULL,NULL,84,0.00,'res_courier02','com_demo0021栋2单元805室','1580703639','请尽快送达',2,'2026-07-15 16:39:23','2026-08-18 04:43:27',1,'courier_hall','2026-08-18 12:43:22','2026-08-18 12:43:26',NULL,NULL,NULL,NULL,NULL,NULL),('ord_000079','NO100079','res_courier02','mch_demo001',NULL,NULL,'pc_demo001',NULL,'cancelled',45.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',4500,0.00,0.00,NULL,0.00,NULL,NULL,NULL,'2026-08-07 13:00:31','商品缺货',0,0.00,NULL,'com_demo0016栋1单元2005室','1327485586','工作日配送',0,'2026-08-07 13:00:31','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000080','NO100080','res_tech01','mch_demo001',NULL,NULL,'pc_demo001',NULL,'pending',74.70,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',7470,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'com_demo00112栋1单元1604室','1313662279','放在门口即可',0,'2026-07-13 09:06:14','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000081','NO100081','res_mch04','mch_demo002',NULL,NULL,'pc_demo001',NULL,'delivering',8.00,NULL,0.00,0.00,0.00,'none',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'distribution_b_v1','point',800,0.00,0.00,NULL,0.00,'2026-07-21 09:11:58',NULL,NULL,NULL,NULL,8,0.00,'res_courier02','com_demo0015栋2单元2903室','1651978414','放在门口即可',2,'2026-07-21 09:11:58','2026-08-18 04:43:27',1,'courier_hall','2026-08-18 12:43:22','2026-08-18 12:43:27',NULL,NULL,NULL,NULL,NULL,NULL),('ord_000082','NO100082','res_courier03','mch_demo001',NULL,NULL,'pc_demo001',NULL,'completed',184.40,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',18440,0.00,0.00,NULL,0.00,'2026-08-08 19:22:09','2026-08-08 19:22:09','2026-08-08 19:22:09',NULL,NULL,184,0.00,'res_courier03','com_demo0012栋4单元1306室','1806866518','工作日配送',0,'2026-08-08 19:22:09','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000083','NO100083','res_mch03','mch_demo001',NULL,NULL,'pc_demo001',NULL,'completed',31.80,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',3180,0.00,0.00,NULL,0.00,'2026-07-25 05:49:06','2026-07-25 05:49:06','2026-07-25 05:49:06',NULL,NULL,31,0.00,'res_courier01','com_demo00112栋2单元1603室','1935363171','放在门口即可',0,'2026-07-25 05:49:06','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000084','NO100084','res_mch04','mch_demo002',NULL,NULL,'pc_demo001',NULL,'delivering',106.00,NULL,0.00,0.00,0.00,'none',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'distribution_b_v1','point',10600,0.00,0.00,NULL,0.00,'2026-07-03 00:43:18',NULL,NULL,NULL,NULL,106,0.00,'res_courier01','com_demo00112栋2单元302室','1662313499','工作日配送',2,'2026-07-03 00:43:18','2026-08-18 04:43:27',1,'courier_hall','2026-08-18 12:43:22','2026-08-18 12:43:27',NULL,NULL,NULL,NULL,NULL,NULL),('ord_000085','NO100085','res_003','mch_demo002',NULL,NULL,'pc_demo001','fam_003','completed',108.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,108.00,'2026-07-10 16:02:48','2026-07-10 16:02:48','2026-07-10 16:02:48',NULL,NULL,108,0.00,'res_courier01','com_demo0022栋4单元1304室','1750112184','请尽快送达',0,'2026-07-10 16:02:48','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000086','NO100086','res_mch01','mch_demo001',NULL,NULL,'pc_demo001',NULL,'pending',298.10,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',14905,0.00,0.00,NULL,149.05,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'com_demo00112栋2单元2304室','1870057746','请尽快送达',0,'2026-07-21 03:35:09','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000087','NO100087','res_propadmin01','mch_demo001',NULL,NULL,'pc_demo001',NULL,'completed',54.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,54.00,'2026-07-03 19:30:01','2026-07-03 19:30:01','2026-07-03 19:30:01',NULL,NULL,54,0.00,'res_courier01','com_demo0013栋4单元2402室','1837938247','放在门口即可',0,'2026-07-03 19:30:01','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000088','NO100088','res_mch01','mch_demo001',NULL,NULL,'pc_demo001',NULL,'delivering',73.50,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',7350,0.00,0.00,NULL,0.00,'2026-08-06 15:39:12','2026-08-06 15:39:12',NULL,NULL,NULL,73,0.00,'res_courier01','com_demo0011栋3单元903室','1608961502','工作日配送',0,'2026-08-06 15:39:12','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000089','NO100089','res_dev003','mch_demo001',NULL,NULL,'pc_demo001',NULL,'completed',131.50,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',13150,0.00,0.00,NULL,0.00,'2026-07-05 23:08:53','2026-07-05 23:08:53','2026-07-05 23:08:53',NULL,NULL,131,0.00,'res_courier03','com_demo00111栋4单元2402室','1656719425','放在门口即可',0,'2026-07-05 23:08:53','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000090','NO100090','res_act01','mch_demo002',NULL,NULL,'pc_demo001',NULL,'cancelled',135.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',13500,0.00,0.00,NULL,0.00,NULL,NULL,NULL,'2026-07-12 15:58:27','重复下单',0,0.00,NULL,'com_demo0014栋1单元504室','1727463192','放在门口即可',0,'2026-07-12 15:58:27','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000091','NO100091','res_001','mch_demo001',NULL,NULL,'pc_demo001','fam_001','completed',31.80,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',3180,0.00,0.00,NULL,0.00,'2026-08-05 08:09:22','2026-08-05 08:09:22','2026-08-05 08:09:22',NULL,NULL,31,0.00,'res_courier02','com_demo0018栋2单元2806室','1650711739','工作日配送',0,'2026-08-05 08:09:22','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000092','NO100092','res_dev004','mch_demo001',NULL,NULL,'pc_demo001',NULL,'completed',45.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',4500,0.00,0.00,NULL,0.00,'2026-07-14 08:33:26','2026-07-14 08:33:26','2026-07-14 08:33:26',NULL,NULL,45,0.00,'res_courier01','com_demo0015栋4单元2903室','1628747163','放在门口即可',0,'2026-07-14 08:33:26','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000093','NO100093','res_ind01','mch_demo002',NULL,NULL,'pc_demo001',NULL,'completed',133.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',13300,0.00,0.00,NULL,0.00,'2026-08-01 20:40:32','2026-08-01 20:40:32','2026-08-01 20:40:32',NULL,NULL,133,0.00,'res_courier01','com_demo0015栋3单元502室','1372696733','放在门口即可',0,'2026-08-01 20:40:32','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000094','NO100094','res_mch03','mch_demo001',NULL,NULL,'pc_demo001',NULL,'delivering',18.00,NULL,0.00,0.00,0.00,'none',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'distribution_b_v1','mixed',900,0.00,0.00,NULL,9.00,'2026-08-10 13:50:24',NULL,NULL,NULL,NULL,18,0.00,'res_courier02','com_demo00112栋1单元1702室','1887838616','请尽快送达',2,'2026-08-10 13:50:24','2026-08-18 04:43:27',1,'courier_hall','2026-08-18 12:43:22','2026-08-18 12:43:27',NULL,NULL,NULL,NULL,NULL,NULL),('ord_000095','NO100095','res_dev004','mch_demo002',NULL,NULL,'pc_demo001',NULL,'cancelled',122.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',6100,0.00,0.00,NULL,61.00,NULL,NULL,NULL,'2026-07-29 18:21:53','商品缺货',0,0.00,NULL,'com_demo0011栋3单元802室','1835912384','请尽快送达',0,'2026-07-29 18:21:53','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000096','NO100096','res_admin01','mch_demo002',NULL,NULL,'pc_demo001',NULL,'completed',116.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',5800,0.00,0.00,NULL,58.00,'2026-08-04 10:17:05','2026-08-04 10:17:05','2026-08-04 10:17:05',NULL,NULL,116,0.00,'res_courier02','com_demo0011栋1单元903室','1851744770','放在门口即可',0,'2026-08-04 10:17:05','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000097','NO100097','res_mch02','mch_demo001',NULL,NULL,'pc_demo001',NULL,'pending',179.80,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,179.80,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'com_demo0019栋1单元601室','1933665265','请尽快送达',0,'2026-08-04 14:26:20','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000098','NO100098','res_sec01','mch_demo002',NULL,NULL,'pc_demo001',NULL,'completed',116.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',11600,0.00,0.00,NULL,0.00,'2026-07-08 15:51:17','2026-07-08 15:51:17','2026-07-08 15:51:17',NULL,NULL,116,0.00,'res_courier02','com_demo00111栋3单元803室','1638667435','请尽快送达',0,'2026-07-08 15:51:17','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000099','NO100099','res_dev003','mch_demo001',NULL,NULL,'pc_demo001',NULL,'completed',125.70,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',12570,0.00,0.00,NULL,0.00,'2026-07-28 13:24:16','2026-07-28 13:24:16','2026-07-28 13:24:16',NULL,NULL,125,0.00,'res_courier01','com_demo0017栋4单元504室','1528060201','工作日配送',0,'2026-07-28 13:24:16','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000100','NO100100','res_coo01','mch_demo001',NULL,NULL,'pc_demo001',NULL,'completed',221.60,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',22160,0.00,0.00,NULL,0.00,'2026-07-03 02:08:20','2026-07-03 02:08:20','2026-07-03 02:08:20',NULL,NULL,221,0.00,'res_courier03','com_demo0018栋4单元2801室','1771152852','请尽快送达',0,'2026-07-03 02:08:20','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000101','NO100101','res_dev003','mch_demo002',NULL,NULL,'pc_demo001',NULL,'completed',227.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',11350,0.00,0.00,NULL,113.50,'2026-07-09 18:02:12','2026-07-09 18:02:12','2026-07-09 18:02:12',NULL,NULL,227,0.00,'res_courier02','com_demo0013栋3单元1103室','1614132313','请尽快送达',0,'2026-07-09 18:02:12','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000102','NO100102','res_mch03','mch_demo001',NULL,NULL,'pc_demo001',NULL,'cancelled',29.90,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',2990,0.00,0.00,NULL,0.00,NULL,NULL,NULL,'2026-08-04 21:14:05','商品缺货',0,0.00,NULL,'com_demo0012栋3单元1303室','1925030175','放在门口即可',0,'2026-08-04 21:14:05','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000103','NO100103','res_courier02','mch_demo001',NULL,NULL,'pc_demo001',NULL,'pending',43.90,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',4390,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'com_demo0014栋2单元2601室','1339015234','放在门口即可',0,'2026-07-22 19:37:52','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000104','NO100104','res_dev004','mch_demo002',NULL,NULL,'pc_demo001',NULL,'pending',16.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',1600,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'com_demo0013栋1单元2605室','1355873068','请尽快送达',0,'2026-07-28 12:53:45','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000105','NO100105','res_dev004','mch_demo002',NULL,NULL,'pc_demo001',NULL,'cancelled',144.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',14400,0.00,0.00,NULL,0.00,NULL,NULL,NULL,'2026-07-28 00:29:13','商品缺货',0,0.00,NULL,'com_demo0013栋3单元2503室','1517743775','放在门口即可',0,'2026-07-28 00:29:13','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000106','NO100106','res_dev003','mch_demo002',NULL,NULL,'pc_demo001',NULL,'pending',164.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',16400,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'com_demo0017栋4单元404室','1712384912','请尽快送达',0,'2026-08-01 09:33:57','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000107','NO100107','res_dev003','mch_demo002',NULL,NULL,'pc_demo001',NULL,'pending',128.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,128.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'com_demo0017栋3单元2103室','1570397399','工作日配送',0,'2026-07-20 09:30:06','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000108','NO100108','res_ind01','mch_demo001',NULL,NULL,'pc_demo001',NULL,'completed',52.80,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',5280,0.00,0.00,NULL,0.00,'2026-07-31 21:51:57','2026-07-31 21:51:57','2026-07-31 21:51:57',NULL,NULL,52,0.00,'res_courier01','com_demo0011栋4单元603室','1662851506','工作日配送',0,'2026-07-31 21:51:57','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000109','NO100109','res_002','mch_demo002',NULL,NULL,'pc_demo001','fam_002','delivering',65.00,NULL,0.00,0.00,0.00,'none',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'distribution_b_v1','point',6500,0.00,0.00,NULL,0.00,'2026-07-07 13:24:54',NULL,NULL,NULL,NULL,65,0.00,'res_courier01','com_demo0018栋1单元206室','1379989107','工作日配送',2,'2026-07-07 13:24:54','2026-08-18 04:43:27',1,'courier_hall','2026-08-18 12:43:22','2026-08-18 12:43:27',NULL,NULL,NULL,NULL,NULL,NULL),('ord_000110','NO100110','res_mch03','mch_demo001',NULL,NULL,'pc_demo001',NULL,'delivering',98.30,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,98.30,'2026-07-21 04:55:15','2026-07-21 04:55:15',NULL,NULL,NULL,98,0.00,'res_courier03','com_demo00111栋4单元2601室','1720582998','请尽快送达',0,'2026-07-21 04:55:15','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000111','NO100111','res_dev004','mch_demo001',NULL,NULL,'pc_demo001',NULL,'completed',73.40,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',3670,0.00,0.00,NULL,36.70,'2026-08-07 06:35:22','2026-08-07 06:35:22','2026-08-07 06:35:22',NULL,NULL,73,0.00,'res_courier02','com_demo0013栋4单元2806室','1842101553','放在门口即可',0,'2026-08-07 06:35:22','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000112','NO100112','res_act01','mch_demo002',NULL,NULL,'pc_demo001',NULL,'pending',57.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,57.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'com_demo0012栋2单元1103室','1738574413','请尽快送达',0,'2026-07-10 17:05:24','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000113','NO100113','res_admin01','mch_demo002',NULL,NULL,'pc_demo001',NULL,'cancelled',46.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,46.00,NULL,NULL,NULL,'2026-07-08 05:36:05','重复下单',0,0.00,NULL,'com_demo0011栋4单元1301室','1848287686','工作日配送',0,'2026-07-08 05:36:05','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000114','NO100114','res_dev001','mch_demo001',NULL,NULL,'pc_demo001','fam_dev001','completed',38.70,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',1935,0.00,0.00,NULL,19.35,'2026-07-07 08:49:58','2026-07-07 08:49:58','2026-07-07 08:49:58',NULL,NULL,38,0.00,'res_courier02','com_demo00110栋4单元2505室','1302029286','工作日配送',0,'2026-07-07 08:49:58','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000115','NO100115','res_001','mch_demo002',NULL,NULL,'pc_demo001','fam_001','completed',46.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','wechat',0,0.00,0.00,NULL,46.00,'2026-07-24 18:06:56','2026-07-24 18:06:56','2026-07-24 18:06:56',NULL,NULL,46,0.00,'res_courier01','com_demo00112栋1单元1902室','1832075813','工作日配送',0,'2026-07-24 18:06:56','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000116','NO100116','res_mch01','mch_demo002',NULL,NULL,'pc_demo001',NULL,'pending',66.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',3300,0.00,0.00,NULL,33.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'com_demo0012栋3单元2302室','1321363356','工作日配送',0,'2026-07-26 17:39:17','2026-08-13 09:59:44',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000117','NO100117','res_mch04','mch_demo002',NULL,NULL,'pc_demo001',NULL,'delivering',24.00,NULL,0.00,0.00,0.00,'none',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'distribution_b_v1','wechat',0,0.00,0.00,NULL,24.00,'2026-07-23 06:32:55',NULL,NULL,NULL,NULL,24,0.00,'res_courier02','com_demo0011栋4单元1902室','1968415086','放在门口即可',2,'2026-07-23 06:32:55','2026-08-18 04:43:27',1,'courier_hall','2026-08-18 12:43:22','2026-08-18 12:43:27',NULL,NULL,NULL,NULL,NULL,NULL),('ord_000118','NO100118','res_admin01','mch_demo001',NULL,NULL,'pc_demo001',NULL,'delivering',160.20,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','mixed',8010,0.00,0.00,NULL,80.10,'2026-07-08 07:29:06','2026-07-08 07:29:06',NULL,NULL,NULL,160,0.00,'res_courier01','com_demo0016栋4单元3001室','1970932207','放在门口即可',0,'2026-07-08 07:29:06','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000119','NO100119','res_001','mch_demo001',NULL,NULL,'pc_demo001','fam_001','completed',25.80,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',2580,0.00,0.00,NULL,0.00,'2026-07-12 05:55:12','2026-07-12 05:55:12','2026-07-12 05:55:12',NULL,NULL,25,0.00,'res_courier02','com_demo00110栋1单元806室','1806099124','请尽快送达',0,'2026-07-12 05:55:12','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_000120','NO100120','res_004','mch_demo001',NULL,NULL,'pc_demo001',NULL,'completed',59.80,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',5980,0.00,0.00,NULL,0.00,'2026-07-24 20:07:16','2026-07-24 20:07:16','2026-07-24 20:07:16',NULL,NULL,59,0.00,'res_courier01','com_demo0014栋4单元1102室','1911767474','工作日配送',0,'2026-07-24 20:07:16','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_008a1000','2026091514100150624','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.08,NULL,0.10,NULL,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,0,'2026-09-15 06:10:02','2026-09-15 06:10:02',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_0bc81000','2026091514100221441','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.08,NULL,0.10,NULL,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,0,'2026-09-15 06:10:03','2026-09-15 06:10:03',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_102c1000','2026091514100383905','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.08,NULL,0.10,NULL,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,0,'2026-09-15 06:10:03','2026-09-15 06:10:03',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_129c1000','2026091514100378369','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.08,NULL,0.10,NULL,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,0,'2026-09-15 06:10:04','2026-09-15 06:10:04',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_1aaa1000','2026090416132717888','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',20.00,NULL,0.00,NULL,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'1栋 · 1单元 · 108室','13624964818',NULL,0,'2026-09-04 08:13:27','2026-09-04 08:13:27',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_30021000','2026090610202736609','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.08,NULL,0.10,NULL,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,0,'2026-09-06 02:20:28','2026-09-09 12:08:42',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_351c88bee41000','2026081310131505312','res_dev001','mch_demo001',NULL,NULL,'pc_demo001','fam_dev001','completed',16.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','property_coin',0,16.00,0.00,NULL,0.00,'2026-08-13 02:13:20','2026-08-13 02:24:33','2026-08-13 02:24:33',NULL,NULL,1600,0.00,'res_courier01','11101',NULL,NULL,5,'2026-08-13 02:13:15','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_351e9bf41e1000','2026081315032139809','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','paid',20.00,NULL,0.00,0.00,0.00,'unknown',0.00,0.00,NULL,NULL,'unknown',NULL,NULL,'legacy','point',4000,0.00,0.00,NULL,0.00,'2026-08-13 07:03:48',NULL,NULL,NULL,NULL,0,0.00,NULL,'11101',NULL,NULL,2,'2026-08-13 07:03:22','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_3520305d321000','2026081318441345793','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',20.00,20.00,0.00,0.00,0.00,'none',0.00,0.00,NULL,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":false}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.0200,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1',NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11101',NULL,NULL,0,'2026-08-13 10:44:14','2026-08-13 10:44:14',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_3520b9a9f21000','2026081319591216673','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.10,0.10,0.00,0.00,0.00,'none',0.00,0.00,NULL,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":false}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.0200,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1',NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11101',NULL,NULL,0,'2026-08-13 11:59:13','2026-08-13 11:59:13',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_3520bac7361000','2026081319594979681','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.20,0.10,0.10,0.10,0.00,'none',0.00,0.10,50.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":50.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.0200,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1',NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11101',NULL,NULL,0,'2026-08-13 11:59:49','2026-08-13 11:59:49',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_3520bbb3ce1000','2026081320001964129','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',25.10,25.00,0.10,0.10,0.00,'none',0.00,0.10,50.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":50.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.0200,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1',NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11101',NULL,NULL,0,'2026-08-13 12:00:19','2026-08-13 12:00:19',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_3520bc9e461000','2026081320004996993','res_dev001','mch_demo001',NULL,NULL,'pc_demo001','fam_dev001','pending',11.00,11.00,0.00,0.00,0.00,'none',0.00,0.00,NULL,0,'none','{\"merchantId\":\"mch_demo001\",\"deliveryFee\":0.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":false}','{\"merchantId\":\"mch_demo001\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1500,\"pointExchangeRate\":100.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.0200,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1',NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11101',NULL,NULL,0,'2026-08-13 12:00:49','2026-08-13 12:00:49',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_3520cb57041000','2026081320085198721','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.20,0.10,0.10,0.10,0.00,'none',0.00,0.10,50.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":50.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.0200,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1',NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11101',NULL,NULL,0,'2026-08-13 12:08:52','2026-08-13 12:08:52',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_35213bfd541000','2026081321102396609','res_dev001','mch_demo001',NULL,NULL,'pc_demo001','fam_dev001','pending',16.00,16.00,0.00,0.00,0.00,'none',0.00,0.00,NULL,0,'none','{\"merchantId\":\"mch_demo001\",\"deliveryFee\":0.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":false}','{\"merchantId\":\"mch_demo001\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1500,\"pointExchangeRate\":100.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.0200,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1',NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'阳光花园1号楼1单元101','13800138000','线上测试',0,'2026-08-13 13:10:23','2026-08-13 13:10:23',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_35213c4f2a1000','2026081321103375809','res_dev001','mch_demo001',NULL,NULL,'pc_demo001','fam_dev001','pending',16.00,16.00,0.00,0.00,0.00,'none',0.00,0.00,NULL,0,'none','{\"merchantId\":\"mch_demo001\",\"deliveryFee\":0.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":false}','{\"merchantId\":\"mch_demo001\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1500,\"pointExchangeRate\":100.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.0200,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1',NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'阳光花园1号楼1单元101','13800138000','线上测试',0,'2026-08-13 13:10:34','2026-08-13 13:10:34',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_352163fc121000','2026081321321385857','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.20,0.10,0.10,0.10,0.00,'none',0.00,0.10,50.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":50.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.0200,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1',NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11101',NULL,NULL,0,'2026-08-13 13:32:14','2026-08-13 13:32:14',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_352164559e1000','2026081321322564192','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','completed',0.20,0.10,0.10,0.10,0.00,'none',0.00,0.10,50.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":50.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.0200,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,0.20,0.00,NULL,0.00,'2026-08-13 13:32:38','2026-08-13 13:41:14','2026-08-13 13:41:14',NULL,NULL,40,0.00,'res_courier01','11101',NULL,NULL,6,'2026-08-13 13:32:25','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_352164e37c1000','2026081321324369569','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.20,0.10,0.10,0.10,0.00,'none',0.00,0.10,50.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":50.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.0200,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1',NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11101',NULL,NULL,0,'2026-08-13 13:32:43','2026-08-13 13:32:43',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_352183f3ca1000','2026081321494103009','res_35217712f21000','mch_demo002',NULL,NULL,'pc_demo001','fam_35217f046c1000','cancelled',0.20,0.10,0.10,0.10,0.00,'none',0.00,0.10,50.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":50.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.0200,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1',NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,'2026-08-14 01:23:43','1',0,0.00,NULL,'11302','19852681536',NULL,1,'2026-08-13 13:49:41','2026-08-14 01:23:43',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_35266bd4c41000','2026081409153357376','res_35217712f21000','mch_demo002',NULL,NULL,'pc_demo001','fam_35217f046c1000','completed',0.20,0.10,0.10,0.10,0.00,'none',0.00,0.10,50.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":50.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.0200,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','wechat',0,0.00,0.00,NULL,0.20,'2026-08-14 01:16:35','2026-08-14 01:18:13','2026-08-14 01:18:13',NULL,NULL,40,0.00,'res_courier01','11302','19852681536',NULL,5,'2026-08-14 01:15:34','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_3526715cfa1000','2026081409183547744','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','cancelled',0.20,0.10,0.10,0.10,0.00,'none',0.00,0.10,50.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":50.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.0200,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','wechat',0,0.00,0.00,NULL,0.20,NULL,NULL,NULL,'2026-08-14 01:50:00','支付超时自动关闭',0,0.00,NULL,'11101',NULL,NULL,2,'2026-08-14 01:18:35','2026-08-14 01:50:00',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_35267afe501000','2026081409235090209','res_35217712f21000','mch_demo002',NULL,NULL,'pc_demo001','fam_35217f046c1000','cancelled',0.20,0.10,0.10,0.10,0.00,'none',0.00,0.10,50.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":50.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.0200,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','wechat',0,0.00,0.00,NULL,0.20,'2026-08-14 01:24:36',NULL,NULL,'2026-08-14 01:25:07','缺货',40,0.00,NULL,'11302','19852681536',NULL,3,'2026-08-14 01:23:51','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_35268adb481000','2026081409323040097','res_35217712f21000','mch_demo002',NULL,NULL,'pc_demo001','fam_35217f046c1000','completed',1.10,1.00,0.10,0.10,0.00,'none',0.00,0.10,50.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":50.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.0200,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','wechat',0,0.00,0.00,NULL,1.10,'2026-08-14 01:33:36','2026-08-14 01:34:09','2026-08-14 01:34:09',NULL,NULL,220,0.00,'res_courier01','11302','19852681536',NULL,5,'2026-08-14 01:32:31','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_3526ad6bfc1000','2026081409512343745','res_35217712f21000','mch_demo002',NULL,NULL,'pc_demo001','fam_35217f046c1000','completed',0.20,0.10,0.10,0.10,0.00,'none',0.00,0.10,50.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":50.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','wechat',0,0.00,0.00,NULL,0.20,'2026-08-14 01:52:06','2026-08-14 01:53:05','2026-08-14 01:53:05',NULL,NULL,40,0.00,'res_courier01','11302','19852681536',NULL,5,'2026-08-14 01:51:23','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_3526f3413a1000','2026081410293197409','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.20,0.10,0.10,0.10,0.00,'none',0.00,0.10,10.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1',NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11101',NULL,NULL,0,'2026-08-14 02:29:31','2026-08-14 02:29:31',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_3526f46ef21000','2026081410301098049','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','completed',20.00,20.00,0.00,0.10,0.10,'merchant',0.10,0.10,10.00,1,'threshold','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,20.00,0.00,NULL,0.00,'2026-08-14 02:30:15','2026-08-14 02:30:32','2026-08-14 02:30:32',NULL,NULL,4000,0.00,'res_courier01','11101',NULL,NULL,6,'2026-08-14 02:30:10','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_35286996141000','2026081413535739105','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','completed',7.10,7.00,0.10,0.10,0.00,'none',0.00,0.10,10.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,7.10,0.00,NULL,0.00,'2026-08-14 05:54:04','2026-08-14 05:57:53','2026-08-14 05:57:53',NULL,NULL,1420,0.00,'res_courier01','11101',NULL,NULL,6,'2026-08-14 05:53:58','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_35288a68ce1000','2026081414115328704','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',7.10,7.00,0.10,0.10,0.00,'none',0.00,0.10,10.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1',NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11101',NULL,NULL,0,'2026-08-14 06:11:53','2026-08-14 06:11:53',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_35288abea81000','2026081414120447841','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','completed',7.10,7.00,0.10,0.10,0.00,'none',0.00,0.10,10.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,7.10,0.00,NULL,0.00,'2026-08-14 06:12:14','2026-08-14 06:13:17','2026-08-14 06:13:17',NULL,NULL,1420,0.00,'res_courier01','11101',NULL,NULL,6,'2026-08-14 06:12:04','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_354fc89cc21000','2026081809382706336','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','completed',7.10,7.00,0.10,0.10,0.00,'none',0.00,0.10,10.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,7.10,0.00,NULL,0.00,'2026-08-18 01:38:37','2026-08-18 01:41:53','2026-08-18 01:41:53',NULL,NULL,1420,0.00,'res_courier01','11101','13624964818',NULL,6,'2026-08-18 01:38:27','2026-08-18 04:13:22',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_354fe4d5a41000','2026081809535132640','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',7.20,7.00,0.20,0.20,0.00,'none',0.00,0.20,10.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1',NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11101','13624964818',NULL,0,'2026-08-18 01:53:52','2026-08-18 01:53:52',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_35513f81481000','2026081813031137825','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','completed',7.20,7.00,0.20,0.20,0.00,'none',0.00,0.20,10.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,7.20,0.00,NULL,0.00,'2026-08-18 05:03:22','2026-08-18 05:26:06','2026-08-18 05:26:06',NULL,NULL,1440,0.00,NULL,'11101','13624964818',NULL,5,'2026-08-18 05:03:12','2026-08-18 05:26:06',1,'merchant_self','2026-08-18 13:33:22','2026-08-18 13:03:55',NULL,NULL,NULL,NULL,NULL,NULL),('ord_355220b31e1000','2026081815061087713','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','delivering',7.20,7.00,0.20,0.20,0.00,'none',0.00,0.20,10.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,7.20,0.00,NULL,0.00,'2026-08-18 07:06:23',NULL,NULL,NULL,NULL,1440,0.00,NULL,'11101','13624964818',NULL,4,'2026-08-18 07:06:11','2026-08-18 07:36:29',1,'courier_hall','2026-08-18 15:36:23','2026-08-18 15:36:29',NULL,NULL,NULL,NULL,NULL,NULL),('ord_3552221f381000','2026081815065798273','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','completed',7.20,7.00,0.20,0.20,0.00,'none',0.00,0.20,10.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,7.20,0.00,NULL,0.00,'2026-08-18 07:07:02','2026-08-18 07:07:21','2026-08-18 07:07:21',NULL,NULL,1440,0.00,NULL,'11101','13624964818',NULL,5,'2026-08-18 07:06:57','2026-08-18 07:07:21',1,'merchant_self','2026-08-18 15:37:02','2026-08-18 15:07:19',NULL,NULL,NULL,NULL,NULL,NULL),('ord_355cf6377e1000','2026081916205383489','res_dev001','mch_demo001',NULL,NULL,'pc_demo001','fam_dev001','pending',16.00,16.00,0.00,0.00,0.00,'none',0.00,0.00,NULL,0,'none','{\"merchantId\":\"mch_demo001\",\"deliveryFee\":0.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":false}','{\"merchantId\":\"mch_demo001\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1500,\"pointExchangeRate\":100.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1',NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,0,'2026-08-19 08:20:53','2026-08-19 08:20:53',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_3579946d841000','2026082211015856865','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.19,0.09,0.10,0.10,0.00,'none',0.00,0.10,10.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1',NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'31503','13624964818',NULL,0,'2026-08-22 03:01:59','2026-09-09 12:08:42',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_357997afda1000','2026082211034591105','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','cancelled',0.19,0.09,0.10,0.10,0.00,'none',0.00,0.10,10.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,0.19,0.00,NULL,0.00,'2026-08-22 03:03:54',NULL,NULL,'2026-08-22 03:04:24','不通过',38,0.00,NULL,'31503','13624964818',NULL,4,'2026-08-22 03:03:45','2026-09-09 12:08:42',0,'none',NULL,NULL,NULL,'2026-08-23 11:03:54',NULL,'res_mch03','不通过',NULL),('ord_3579ade7821000','2026082211155328545','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','refunded',0.19,0.09,0.10,0.10,0.00,'none',0.00,0.10,10.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,0.19,0.00,NULL,0.00,'2026-08-22 03:16:00',NULL,NULL,NULL,'1',38,0.00,NULL,'31503','13624964818',NULL,6,'2026-08-22 03:15:53','2026-09-09 12:08:42',0,'none',NULL,NULL,'2026-08-22 11:25:07','2026-08-23 11:16:00','2026-08-29 11:25:07','res_mch03',NULL,NULL),('ord_357b1700801000','2026082214330521729','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','cancelled',0.19,0.09,0.10,0.10,0.00,'none',0.00,0.10,10.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','wechat',0,0.00,0.00,NULL,0.19,NULL,NULL,NULL,'2026-08-22 07:10:00','支付超时自动关闭',0,0.00,NULL,'31503','13624964818',NULL,2,'2026-08-22 06:33:06','2026-09-09 12:08:42',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_35838e3ff01000','2026082310164236448','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',25.00,25.00,0.00,0.10,0.10,'merchant',0.10,0.10,10.00,1,'threshold','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1',NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'31503','13624964818',NULL,0,'2026-08-23 02:16:42','2026-08-23 02:16:42',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_358392e4681000','2026082310191453729','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.19,0.09,0.10,0.10,0.00,'none',0.00,0.10,10.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1',NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'31503','13624964818',NULL,0,'2026-08-23 02:19:14','2026-09-09 12:08:42',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_35839426361000','2026082310195540481','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','completed',20.00,20.00,0.00,0.10,0.10,'merchant',0.10,0.10,10.00,1,'threshold','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,20.00,0.00,NULL,0.00,'2026-08-23 02:19:59',NULL,'2026-08-30 03:14:12',NULL,NULL,4000,0.00,NULL,'31503','13624964818',NULL,5,'2026-08-23 02:19:56','2026-08-30 03:14:13',1,NULL,NULL,NULL,'2026-08-23 10:20:22','2026-08-24 10:19:59','2026-08-30 10:20:22','res_mch03',NULL,NULL),('ord_35844476841000','2026082311561273665','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.09,0.09,0.00,0.00,0.00,NULL,0.00,0.00,NULL,0,NULL,'{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,0.09,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,1,'2026-08-23 03:56:13','2026-08-23 03:56:22',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_3584bb47601000','2026082313010693441','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.09,0.09,0.00,0.00,0.00,NULL,0.00,0.00,NULL,0,NULL,'{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,0.09,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,1,'2026-08-23 05:01:06','2026-08-23 05:01:10',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_3584bdea3c1000','2026082313023254945','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.09,0.09,0.00,0.00,0.00,NULL,0.00,0.00,NULL,0,NULL,'{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,0.09,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,1,'2026-08-23 05:02:33','2026-08-23 05:02:38',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_3584bed60c1000','2026082313030232193','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.09,0.09,0.00,0.00,0.00,NULL,0.00,0.00,NULL,0,NULL,'{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,0.09,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,1,'2026-08-23 05:03:03','2026-08-23 05:03:06',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_358517427e1000','2026082313512064673','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.09,0.09,0.00,0.00,0.00,NULL,0.00,0.00,NULL,0,NULL,'{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,0.09,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,1,'2026-08-23 05:51:20','2026-08-23 05:51:25',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_358517a0c61000','2026082313513241569','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',20.00,20.00,0.00,0.10,0.10,'merchant',0.10,0.10,10.00,1,'threshold','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,20.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,1,'2026-08-23 05:51:32','2026-08-23 05:51:40',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_35851e60281000','2026082313551316129','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.09,0.09,0.00,0.00,0.00,NULL,0.00,0.00,NULL,0,NULL,'{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1',NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,0,'2026-08-23 05:55:14','2026-08-23 05:55:14',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_35851e73aa1000','2026082313551602913','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.09,0.09,0.00,0.00,0.00,NULL,0.00,0.00,NULL,0,NULL,'{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,0.09,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,1,'2026-08-23 05:55:16','2026-08-23 05:55:20',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_3585253c3c1000','2026082313585818145','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.09,0.09,0.00,0.00,0.00,NULL,0.00,0.00,NULL,0,NULL,'{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,0.09,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,1,'2026-08-23 05:58:58','2026-08-23 05:59:02',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_3585315b4c1000','2026082314053567969','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','completed',0.09,0.09,0.00,0.00,0.00,'merchant',0.00,0.00,NULL,0,NULL,'{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,0.09,0.00,NULL,0.00,'2026-08-23 06:05:40',NULL,'2026-08-30 06:14:12',NULL,NULL,18,0.00,NULL,'11108','13624964818',NULL,6,'2026-08-23 06:05:36','2026-08-30 06:14:12',0,'none',NULL,NULL,'2026-08-23 14:08:20','2026-08-24 14:05:40','2026-08-30 14:08:20','res_mch03',NULL,NULL),('ord_358531aa921000','2026082314054555553','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','delivering',20.00,20.00,0.00,0.10,0.10,'merchant',0.10,0.10,10.00,1,'threshold','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,20.00,0.00,NULL,0.00,'2026-08-23 06:05:49',NULL,NULL,NULL,NULL,4000,0.00,NULL,'11108','13624964818',NULL,4,'2026-08-23 06:05:46','2026-08-23 06:06:13',1,'courier_hall','2026-08-23 14:35:49','2026-08-23 14:06:13',NULL,NULL,NULL,NULL,NULL,NULL),('ord_358533a5ce1000','2026082314065036225','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','delivering',20.00,20.00,0.00,0.10,0.10,'merchant',0.10,0.10,10.00,1,'threshold','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,20.00,0.00,NULL,0.00,'2026-08-23 06:06:54',NULL,NULL,NULL,NULL,4000,0.00,NULL,'11108','13624964818',NULL,4,'2026-08-23 06:06:51','2026-08-23 06:07:27',1,'courier_hall','2026-08-23 14:36:54','2026-08-23 14:07:27',NULL,NULL,NULL,NULL,NULL,NULL),('ord_35854036621000','2026082314134262209','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','delivering',20.00,20.00,0.00,0.10,0.10,'merchant',0.10,0.10,10.00,1,'threshold','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,20.00,0.00,NULL,0.00,'2026-08-23 06:13:48',NULL,NULL,NULL,NULL,4000,0.00,NULL,'11108','13624964818',NULL,4,'2026-08-23 06:13:42','2026-08-23 06:14:00',1,'courier_hall','2026-08-23 14:43:48','2026-08-23 14:14:00',NULL,NULL,NULL,NULL,NULL,NULL),('ord_358609a9a81000','2026082316034380352','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','completed',20.00,20.00,0.00,0.10,0.10,'merchant',0.10,0.10,10.00,1,'threshold','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,20.00,0.00,NULL,0.00,'2026-08-23 08:03:50','2026-08-23 08:25:38','2026-08-23 08:25:38',NULL,NULL,4000,0.00,'res_courier01','11108','13624964818',NULL,6,'2026-08-23 08:03:43','2026-08-23 08:25:38',1,'courier_hall','2026-08-23 16:33:50','2026-08-23 16:04:29',NULL,NULL,NULL,NULL,NULL,NULL),('ord_358628c4cc1000','2026082316204286369','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',20.00,20.00,0.00,0.10,0.10,'merchant',0.10,0.10,10.00,1,'threshold','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1',NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,0,'2026-08-23 08:20:43','2026-08-23 08:20:43',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_358628e0fa1000','2026082316204663073','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','delivering',25.00,25.00,0.00,0.10,0.10,'merchant',0.10,0.10,10.00,1,'threshold','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,25.00,0.00,NULL,0.00,'2026-08-23 08:20:52',NULL,NULL,NULL,NULL,5000,0.00,NULL,'11108','13624964818',NULL,4,'2026-08-23 08:20:46','2026-08-23 08:24:37',1,'merchant_self','2026-08-23 16:50:52','2026-08-23 16:24:37',NULL,NULL,NULL,NULL,NULL,NULL),('ord_358630648a1000','2026082316245286625','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','completed',7.10,7.00,0.10,0.10,0.00,'none',0.00,0.10,10.00,0,'none','{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,7.10,0.00,NULL,0.00,'2026-08-23 08:24:58','2026-08-23 08:25:36','2026-08-23 08:25:36',NULL,NULL,1420,0.00,'res_courier01','11108','13624964818',NULL,6,'2026-08-23 08:24:53','2026-08-23 08:25:36',1,'courier_hall','2026-08-23 16:54:58','2026-08-23 16:25:08',NULL,NULL,NULL,NULL,NULL,NULL),('ord_35863441be1000','2026082316265961121','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','completed',0.09,0.09,0.00,0.00,0.00,'merchant',0.00,0.00,NULL,0,NULL,'{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":10.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,0.09,0.00,NULL,0.00,'2026-08-23 08:27:05',NULL,'2026-08-30 09:05:48',NULL,NULL,18,0.00,NULL,'11108','13624964818',NULL,6,'2026-08-23 08:26:59','2026-08-30 09:05:49',0,'none',NULL,NULL,'2026-08-23 16:27:14','2026-08-24 16:27:05','2026-08-30 16:27:14','res_mch03',NULL,NULL),('ord_358f143fb21000','2026082413074716769','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.09,0.09,0.00,0.00,0.00,NULL,0.00,0.00,NULL,0,NULL,'{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":9.99,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','wechat',0,0.00,0.00,NULL,0.09,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,1,'2026-08-24 05:07:48','2026-08-24 05:08:01',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_358f1a669e1000','2026082413110921249','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.09,0.09,0.00,0.00,0.00,NULL,0.00,0.00,NULL,0,NULL,'{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":9.99,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','wechat',0,0.00,0.00,NULL,0.09,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,1,'2026-08-24 05:11:09','2026-08-24 05:11:14',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_358f3e6d9a1000','2026082413304922273','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.09,0.09,0.00,0.00,0.00,NULL,0.00,0.00,NULL,0,NULL,'{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":9.99,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','wechat',0,0.00,0.00,NULL,0.09,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,1,'2026-08-24 05:30:50','2026-08-24 05:30:53',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_35ce6a6f921000','2026083016425463329','res_dev001','mch_35c14661b81000',NULL,NULL,'pc_demo001','fam_dev001','pending',10.00,10.00,0.00,0.00,0.00,NULL,0.00,0.00,NULL,0,NULL,'{\"merchantId\":\"mch_35c14661b81000\",\"deliveryFee\":0.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":false}','{\"merchantId\":\"mch_35c14661b81000\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":100.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1',NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,0,'2026-08-30 08:42:54','2026-08-30 08:42:54',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_35ce6aeb821000','2026083016431089537','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','completed',0.09,0.09,0.00,0.00,0.00,NULL,0.00,0.00,NULL,0,NULL,'{\"merchantId\":\"mch_demo002\",\"deliveryFee\":0.10,\"freeDeliveryThreshold\":9.99,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":true}','{\"merchantId\":\"mch_demo002\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":200.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','property_coin',0,0.09,0.00,NULL,0.00,'2026-08-30 08:43:16',NULL,'2026-09-07 02:25:20',NULL,NULL,18,0.00,NULL,'11108','13624964818',NULL,5,'2026-08-30 08:43:10','2026-09-07 02:25:20',0,'none',NULL,NULL,'2026-08-31 10:04:08','2026-08-31 16:43:16','2026-09-07 10:04:08','res_mch03',NULL,NULL),('ord_35d14493e61000','2026083023213974209','res_dev001','mch_35c14661b81000',NULL,NULL,'pc_demo001','fam_dev001','completed',10.00,10.00,0.00,0.00,0.00,NULL,0.00,0.00,NULL,0,NULL,'{\"merchantId\":\"mch_35c14661b81000\",\"deliveryFee\":0.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":false}','{\"merchantId\":\"mch_35c14661b81000\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":100.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','mixed',0,0.00,10.00,'mch_35c14661b81000',0.00,'2026-08-30 15:21:45',NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,3,'2026-08-30 15:21:40','2026-08-30 15:21:45',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_35d14511e01000','2026083023215510209','res_dev001','mch_35c14bfbe61000',NULL,NULL,'pc_demo001','fam_dev001','completed',8.00,8.00,0.00,0.00,0.00,NULL,0.00,0.00,NULL,0,NULL,'{\"merchantId\":\"mch_35c14bfbe61000\",\"deliveryFee\":0.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":false}','{\"merchantId\":\"mch_35c14bfbe61000\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1000,\"pointExchangeRate\":100.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1','mixed',0,0.00,8.00,'mch_35c14661b81000',0.00,'2026-08-30 15:22:00',NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,3,'2026-08-30 15:21:56','2026-08-30 15:22:00',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_35d86348b01000','2026083115570617888','res_dev001','mch_demo001',NULL,NULL,'pc_demo001','fam_dev001','pending',16.00,16.00,0.00,0.00,0.00,'none',0.00,0.00,NULL,0,'none','{\"merchantId\":\"mch_demo001\",\"deliveryFee\":0.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":false}','{\"merchantId\":\"mch_demo001\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1500,\"pointExchangeRate\":100.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1',NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,0,'2026-08-31 07:57:06','2026-08-31 07:57:06',1,'pending_choice',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_35d872585e1000','2026083116051995617','res_dev001','mch_demo001',NULL,NULL,'pc_demo001','fam_dev001','pending',16.00,16.00,0.00,0.00,0.00,'none',0.00,0.00,NULL,0,'none','{\"merchantId\":\"mch_demo001\",\"deliveryFee\":0.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":false}','{\"merchantId\":\"mch_demo001\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1500,\"pointExchangeRate\":100.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1',NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,0,'2026-08-31 08:05:20','2026-08-31 08:05:20',1,'pending_choice',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_35d875ba381000','2026083116071074305','res_dev001','mch_demo001',NULL,NULL,'pc_demo001','fam_dev001','pending',16.00,16.00,0.00,0.00,0.00,'none',0.00,0.00,NULL,0,'none','{\"merchantId\":\"mch_demo001\",\"deliveryFee\":0.00,\"freeDeliverySponsor\":\"merchant\",\"freeDeliveryEnabled\":false}','{\"merchantId\":\"mch_demo001\",\"propertyCompanyId\":\"pc_demo001\",\"commissionRate\":0.1500,\"pointExchangeRate\":100.0000,\"platformShareRate\":0.1000,\"propertyShareRate\":0.6000,\"coordinatorShareRate\":0.3000,\"sectorLeaderRate\":0.6000,\"individualLeaderRate\":0.5000,\"platformDeliveryShareRate\":0.1000,\"calculationVersion\":\"distribution_b_v1\"}','distribution_b_v1',NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,0,'2026-08-31 08:07:10','2026-08-31 08:07:10',1,'pending_choice',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_369b7a2c601000','2026091914323483873','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.08,NULL,0.10,NULL,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','18861350048',NULL,0,'2026-09-19 06:32:35','2026-09-19 06:32:35',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_369b7e38ce1000','2026091914344788609','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.08,NULL,0.10,NULL,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','18861350048',NULL,0,'2026-09-19 06:34:47','2026-09-19 06:34:47',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_369bd9ccd41000','2026091915244833729','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.08,NULL,0.10,NULL,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','18861350048',NULL,0,'2026-09-19 07:24:48','2026-09-19 07:24:48',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_369c9ea0341000','2026091917121707777','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',31.00,NULL,0.00,NULL,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','18861350048',NULL,0,'2026-09-19 09:12:18','2026-09-19 09:12:18',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'rml_c0b01000'),('ord_369cfeb66a1000','2026091918044605889','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.08,NULL,0.10,NULL,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','18861350048',NULL,0,'2026-09-19 10:04:46','2026-09-19 10:04:46',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_369d00aae41000','2026091918055009281','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.08,NULL,0.10,NULL,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','18861350048',NULL,0,'2026-09-19 10:05:50','2026-09-19 10:05:50',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_36bcbe74321000','2026092220033663296','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',31.00,NULL,0.00,NULL,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','18861350048',NULL,0,'2026-09-22 12:03:36','2026-09-22 12:03:36',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'rml_c0b01000'),('ord_36bcbfab5e1000','2026092220041519265','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',31.00,NULL,0.00,NULL,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','18861350048',NULL,0,'2026-09-22 12:04:16','2026-09-22 12:04:16',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'rml_c0b01000'),('ord_36bcc766d01000','2026092220082900833','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',31.00,NULL,0.00,NULL,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','18861350048',NULL,0,'2026-09-22 12:08:29','2026-09-22 12:08:29',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'rml_c0b01000'),('ord_36cd384b201000','2026092410270679104','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','delivering',31.00,NULL,0.00,NULL,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'point',6200,0.00,0.00,NULL,0.00,'2026-09-24 02:27:26',NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','18861350048',NULL,2,'2026-09-24 02:27:06','2026-09-24 02:57:52',1,'courier_hall',NULL,'2026-09-24 10:57:52',NULL,NULL,NULL,NULL,NULL,'rml_c0b01000'),('ord_593c1000','2026090617330501985','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','paid',20.00,NULL,0.10,NULL,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'property_coin',0,20.10,0.00,NULL,0.00,'2026-09-06 09:33:12',NULL,NULL,NULL,NULL,4020,0.00,NULL,'11108','13624964818',NULL,1,'2026-09-06 09:33:05','2026-09-09 12:08:42',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_67561000','2026090610020000449','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',7.00,NULL,0.10,NULL,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,0,'2026-09-06 02:02:00','2026-09-06 02:02:00',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_78ae1000','2026090921342722977','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.08,NULL,0.10,NULL,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,0,'2026-09-09 13:34:27','2026-09-09 13:34:27',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_821c1000','2026091116512012576','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','delivering',7.00,NULL,0.10,NULL,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'property_coin',0,7.10,0.00,NULL,0.00,'2026-09-11 08:51:26',NULL,NULL,NULL,NULL,1420,0.00,NULL,'11108','13624964818',NULL,3,'2026-09-11 08:51:20','2026-09-11 08:52:15',1,'merchant_self',NULL,'2026-09-11 16:52:14',NULL,NULL,NULL,NULL,NULL,NULL),('ord_aed01000','2026090909380259584','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','delivering',7.00,NULL,0.10,NULL,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'point',1420,0.00,0.00,NULL,0.00,'2026-09-09 01:38:09','2026-09-09 01:41:21',NULL,NULL,NULL,0,0.00,'res_courier01','11108','13624964818',NULL,4,'2026-09-09 01:38:03','2026-09-09 12:08:42',1,'courier_hall',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('ord_eaa61000','2026090921340934017','res_dev001','mch_demo002',NULL,NULL,'pc_demo001','fam_dev001','pending',0.08,NULL,0.10,NULL,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0.00,NULL,0.00,NULL,NULL,NULL,NULL,NULL,0,0.00,NULL,'11108','13624964818',NULL,0,'2026-09-09 13:34:09','2026-09-09 13:34:09',0,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment_records`
--

DROP TABLE IF EXISTS `payment_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_records` (
  `id` varchar(32) NOT NULL COMMENT 'pr_xxxxxxxx',
  `biz_type` varchar(32) NOT NULL COMMENT 'order/property_fee/coin_recharge/service_order/service_subscription/activity_subscription/resident_shop_deposit',
  `biz_id` varchar(32) DEFAULT NULL COMMENT '业务单ID',
  `resident_id` varchar(32) DEFAULT NULL,
  `property_company_id` varchar(32) DEFAULT NULL,
  `payment_method` varchar(20) DEFAULT NULL COMMENT 'wechat/mixed',
  `amount` decimal(10,2) DEFAULT '0.00' COMMENT '应付总额(元)',
  `point_used` int DEFAULT '0' COMMENT '积分抵扣快照',
  `coin_used` decimal(10,2) DEFAULT '0.00' COMMENT '物业币抵扣快照',
  `directed_balance_used` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '定向余额抵扣（快照）',
  `cash_amount` decimal(10,2) DEFAULT '0.00' COMMENT '现金部分(元)=扫呗下单金额',
  `status` varchar(20) DEFAULT 'pending' COMMENT 'pending/paid/closed/refunding/refunded/failed',
  `out_trade_no` varchar(64) DEFAULT NULL COMMENT '扫呗订单号',
  `trace_no` varchar(64) DEFAULT NULL COMMENT '终端流水号',
  `channel_trade_no` varchar(64) DEFAULT NULL COMMENT '微信订单号',
  `channel_order_no` varchar(64) DEFAULT NULL COMMENT '收单通道侧订单号',
  `sub_mchid` varchar(32) DEFAULT NULL COMMENT '该笔支付实际入账的微信二级商户号（电商收付通）',
  `out_refund_no` varchar(64) DEFAULT NULL COMMENT '扫呗退款单号',
  `notify_body` varchar(4000) DEFAULT NULL COMMENT '回调原始报文',
  `error_msg` varchar(500) DEFAULT NULL,
  `paid_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `active_flag` varchar(1) GENERATED ALWAYS AS (if((`status` = _utf8mb4'pending'),_utf8mb4'1',NULL)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `out_trade_no` (`out_trade_no`),
  UNIQUE KEY `uk_payment_records_out_trade_no` (`out_trade_no`),
  UNIQUE KEY `uk_payment_records_biz_pending` (`biz_type`,`biz_id`,`active_flag`),
  KEY `idx_biz` (`biz_type`,`biz_id`),
  KEY `idx_status_created` (`status`,`created_at`),
  KEY `idx_resident` (`resident_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='扫呗支付流水表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_records`
--

LOCK TABLES `payment_records` WRITE;
/*!40000 ALTER TABLE `payment_records` DISABLE KEYS */;
INSERT INTO `payment_records` (`id`, `biz_type`, `biz_id`, `resident_id`, `property_company_id`, `payment_method`, `amount`, `point_used`, `coin_used`, `directed_balance_used`, `cash_amount`, `status`, `out_trade_no`, `trace_no`, `channel_trade_no`, `channel_order_no`, `sub_mchid`, `out_refund_no`, `notify_body`, `error_msg`, `paid_at`, `created_at`, `updated_at`) VALUES ('pr_351c0ea40c1000','resident_shop_deposit','rma_351c0e77581000','res_dev001','pc_demo001','wechat',500.00,0,0.00,0.00,500.00,'closed',NULL,'pr_351c0ea40c1000',NULL,NULL,NULL,NULL,NULL,'支付超时自动关闭',NULL,'2026-08-13 01:06:34','2026-08-13 01:40:00'),('pr_35266c00941000','order','ord_35266bd4c41000','res_35217712f21000','pc_demo001','wechat',0.20,0,0.00,0.00,0.20,'paid','304188401401626081409153905802','pr_35266c00941000',NULL,NULL,NULL,NULL,'[reconciled-by-scheduler]',NULL,'2026-08-14 01:16:35','2026-08-14 01:15:39','2026-08-14 01:16:35'),('pr_3526716fbc1000','order','ord_3526715cfa1000','res_dev001','pc_demo001','wechat',0.20,0,0.00,0.00,0.20,'closed',NULL,'pr_3526716fbc1000',NULL,NULL,NULL,NULL,NULL,'支付超时自动关闭',NULL,'2026-08-14 01:18:38','2026-08-14 01:50:00'),('pr_3526718fb21000','order','ord_3526715cfa1000','res_dev001','pc_demo001','wechat',0.20,0,0.00,0.00,0.20,'closed',NULL,'pr_3526718fb21000',NULL,NULL,NULL,NULL,NULL,'支付超时自动关闭',NULL,'2026-08-14 01:18:42','2026-08-14 01:50:00'),('pr_35267b359e1000','order','ord_35267afe501000','res_35217712f21000','pc_demo001','wechat',0.20,0,0.00,0.00,0.20,'refunded','304188408001626081409235808874','pr_35267b359e1000',NULL,NULL,NULL,'304188405001026081409250609398','[reconciled-by-scheduler]',NULL,'2026-08-14 01:24:35','2026-08-14 01:23:58','2026-08-14 01:25:07'),('pr_35268b1d9e1000','order','ord_35268adb481000','res_35217712f21000','pc_demo001','wechat',1.10,0,0.00,0.00,1.10,'paid','304188407901626081409323912266','pr_35268b1d9e1000',NULL,NULL,NULL,NULL,'[reconciled-by-scheduler]',NULL,'2026-08-14 01:33:36','2026-08-14 01:32:39','2026-08-14 01:33:36'),('pr_3526ad7ab01000','order','ord_3526ad6bfc1000','res_35217712f21000','pc_demo001','wechat',0.20,0,0.00,0.00,0.20,'paid','304188401601626081409512519175','pr_3526ad7ab01000',NULL,NULL,NULL,NULL,'[reconciled-by-scheduler]',NULL,'2026-08-14 01:52:06','2026-08-14 01:51:25','2026-08-14 01:52:06'),('pr_353d0615381000','activity_subscription','ags_353d0614ca1000','res_dev001','pc_demo001','wechat',10.00,0,0.00,0.00,10.00,'closed',NULL,'pr_353d0615381000',NULL,NULL,NULL,NULL,NULL,'支付超时自动关闭',NULL,'2026-08-16 05:55:38','2026-08-16 06:30:00'),('pr_353d0e5b5c1000','service_order','so_353d0e5b1e1000','res_dev001','pc_demo001','wechat',20.00,0,0.00,0.00,20.00,'closed',NULL,'pr_353d0e5b5c1000',NULL,NULL,NULL,NULL,NULL,'支付超时自动关闭',NULL,'2026-08-16 06:00:09','2026-08-16 06:40:00'),('pr_354fea029c1000','coin_recharge',NULL,'res_dev001','pc_demo001','wechat',1.00,0,0.00,0.00,1.00,'closed',NULL,'pr_354fea029c1000',NULL,NULL,NULL,NULL,NULL,'支付超时自动关闭',NULL,'2026-08-18 01:56:41','2026-08-18 02:30:00'),('pr_35600196261000','resident_shop_deposit','rma_351c0e77581000','res_dev001','pc_demo001','wechat',500.00,0,0.00,0.00,500.00,'closed',NULL,'pr_35600196261000',NULL,NULL,NULL,NULL,NULL,'支付超时自动关闭',NULL,'2026-08-19 15:26:32','2026-08-19 16:00:00'),('pr_35649756341000','resident_shop_deposit','rma_351c0e77581000','res_dev001','pc_demo001','wechat',500.00,0,0.00,0.00,500.00,'closed',NULL,'pr_35649756341000',NULL,NULL,NULL,NULL,NULL,'支付超时自动关闭',NULL,'2026-08-20 02:07:33','2026-08-20 02:40:00'),('pr_35671ae3301000','coin_recharge',NULL,'res_dev001','pc_demo001','wechat',100.00,0,0.00,0.00,100.00,'closed',NULL,'pr_35671ae3301000',NULL,NULL,NULL,NULL,NULL,'支付超时自动关闭',NULL,'2026-08-20 07:59:01','2026-08-20 08:30:00'),('pr_35673b7d8e1000','resident_shop_deposit','rma_351c0e77581000','res_dev001','pc_demo001','wechat',500.00,0,0.00,0.00,500.00,'closed',NULL,'pr_35673b7d8e1000',NULL,NULL,NULL,NULL,NULL,'支付超时自动关闭',NULL,'2026-08-20 08:16:49','2026-08-20 08:50:00'),('pr_35715a9e7e1000','resident_shop_deposit','rma_351c0e77581000','res_dev001','pc_demo001','wechat',500.00,0,0.00,0.00,500.00,'closed',NULL,'pr_35715a9e7e1000',NULL,NULL,NULL,NULL,NULL,'支付超时自动关闭',NULL,'2026-08-21 07:51:55','2026-08-21 08:30:00'),('pr_357b1761ae1000','order','ord_357b1700801000','res_dev001','pc_demo001','wechat',0.19,0,0.00,0.00,0.19,'closed',NULL,'pr_357b1761ae1000',NULL,NULL,NULL,NULL,NULL,'支付超时自动关闭',NULL,'2026-08-22 06:33:18','2026-08-22 07:10:00'),('pr_3583da83161000','resident_shop_deposit','rma_351c0e77581000','res_dev001','pc_demo001','wechat',500.00,0,0.00,0.00,500.00,'closed',NULL,'pr_3583da83161000',NULL,NULL,NULL,NULL,NULL,'支付超时自动关闭',NULL,'2026-08-23 02:58:21','2026-08-23 03:30:00'),('pr_gen000001','property_fee','pf_gen00001','res_001','pc_demo001','wechat',541.89,0,0.00,0.00,541.89,'paid','XB0000000002','T0000000002','WX000000000002','CO000000000002',NULL,NULL,NULL,NULL,'2026-07-24 07:32:07','2026-07-22 11:50:07','2026-07-22 11:50:07'),('pr_gen000002','property_fee','pf_gen00002','res_001','pc_demo001','wechat',567.34,0,0.00,0.00,567.34,'paid','XB0000000003','T0000000003','WX000000000003','CO000000000003',NULL,NULL,NULL,NULL,'2026-08-08 07:18:30','2026-07-17 22:21:52','2026-07-17 22:21:52'),('pr_gen000003','property_fee','pf_gen00003','res_002','pc_demo001','wechat',149.37,0,0.00,0.00,149.37,'paid','XB0000000004','T0000000004','WX000000000004','CO000000000004',NULL,NULL,NULL,NULL,'2026-08-07 07:23:34','2026-07-15 09:12:33','2026-07-15 09:12:33'),('pr_gen000004','property_fee','pf_gen00004','res_002','pc_demo001','wechat',149.35,0,0.00,0.00,149.35,'paid','XB0000000005','T0000000005','WX000000000005','CO000000000005',NULL,NULL,NULL,NULL,'2026-07-20 07:52:07','2026-07-28 04:50:07','2026-07-28 04:50:07'),('pr_gen000005','property_fee','pf_gen00005','res_002','pc_demo001','wechat',161.58,0,0.00,0.00,161.58,'paid','XB0000000006','T0000000006','WX000000000006','CO000000000006',NULL,NULL,NULL,NULL,'2026-07-05 04:27:19','2026-07-07 12:26:07','2026-07-07 12:26:07'),('pr_gen000006','property_fee','pf_gen00007','res_003','pc_demo001','wechat',350.58,0,0.00,0.00,350.58,'paid','XB0000000007','T0000000007','WX000000000007','CO000000000007',NULL,NULL,NULL,NULL,'2026-07-27 20:44:17','2026-07-21 22:20:16','2026-07-21 22:20:16'),('pr_gen000007','property_fee','pf_gen00010','res_act01','pc_demo001','wechat',485.36,0,0.00,0.00,485.36,'paid','XB0000000008','T0000000008','WX000000000008','CO000000000008',NULL,NULL,NULL,NULL,'2026-07-27 14:02:17','2026-07-04 01:21:33','2026-07-04 01:21:33'),('pr_gen000008','property_fee','pf_gen00012','res_admin01','pc_demo001','wechat',241.49,0,0.00,0.00,241.49,'paid','XB0000000009','T0000000009','WX000000000009','CO000000000009',NULL,NULL,NULL,NULL,'2026-07-03 03:39:54','2026-07-08 02:39:08','2026-07-08 02:39:08'),('pr_gen000009','property_fee','pf_gen00015','res_courier03','pc_demo001','wechat',600.61,0,0.00,0.00,600.61,'paid','XB0000000010','T0000000010','WX000000000010','CO000000000010',NULL,NULL,NULL,NULL,'2026-07-27 02:18:34','2026-08-04 09:18:46','2026-08-04 09:18:46'),('pr_gen000010','property_fee','pf_gen00018','res_dev002','pc_demo001','wechat',477.75,0,0.00,0.00,477.75,'paid','XB0000000011','T0000000011','WX000000000011','CO000000000011',NULL,NULL,NULL,NULL,'2026-07-09 08:27:14','2026-07-11 10:17:02','2026-07-11 10:17:02'),('pr_gen000011','property_fee','pf_gen00019','res_dev002','pc_demo001','wechat',276.60,0,0.00,0.00,276.60,'paid','XB0000000012','T0000000012','WX000000000012','CO000000000012',NULL,NULL,NULL,NULL,'2026-08-10 10:53:48','2026-07-26 16:51:29','2026-07-26 16:51:29'),('pr_gen000012','property_fee','pf_gen00021','res_dev004','pc_demo001','wechat',673.15,0,0.00,0.00,673.15,'paid','XB0000000013','T0000000013','WX000000000013','CO000000000013',NULL,NULL,NULL,NULL,'2026-07-03 04:07:30','2026-07-27 20:48:25','2026-07-27 20:48:25'),('pr_gen000013','property_fee','pf_gen00022','res_ind01','pc_demo001','wechat',538.11,0,0.00,0.00,538.11,'paid','XB0000000014','T0000000014','WX000000000014','CO000000000014',NULL,NULL,NULL,NULL,'2026-08-01 11:42:55','2026-07-27 12:41:18','2026-07-27 12:41:18'),('pr_gen000014','property_fee','pf_gen00025','res_mch04','pc_demo001','wechat',268.48,0,0.00,0.00,268.48,'paid','XB0000000015','T0000000015','WX000000000015','CO000000000015',NULL,NULL,NULL,NULL,'2026-07-18 16:15:39','2026-08-03 02:15:39','2026-08-03 02:15:39'),('pr_gen000015','property_fee','pf_gen00027','res_propadmin01','pc_demo001','wechat',406.49,0,0.00,0.00,406.49,'paid','XB0000000016','T0000000016','WX000000000016','CO000000000016',NULL,NULL,NULL,NULL,'2026-07-04 02:13:56','2026-07-06 10:19:22','2026-07-06 10:19:22'),('pr_gen000016','property_fee','pf_gen00028','res_sec01','pc_demo001','wechat',640.72,0,0.00,0.00,640.72,'paid','XB0000000017','T0000000017','WX000000000017','CO000000000017',NULL,NULL,NULL,NULL,'2026-07-12 17:27:32','2026-08-06 05:59:37','2026-08-06 05:59:37'),('pr_gen000017','property_fee','pf_gen00030','res_tech01','pc_demo001','wechat',344.69,0,0.00,0.00,344.69,'paid','XB0000000018','T0000000018','WX000000000018','CO000000000018',NULL,NULL,NULL,NULL,'2026-08-09 21:56:20','2026-07-20 15:17:25','2026-07-20 15:17:25'),('pr_gen000018','order','ord_000003','res_courier02','pc_demo001','wechat',80.00,4000,0.00,0.00,40.00,'paid','XB0000000019','T0000000019','WX000000000019','CO000000000019',NULL,NULL,NULL,NULL,'2026-07-09 11:51:22','2026-07-09 11:51:22','2026-07-09 11:51:22'),('pr_gen000019','order','ord_000005','res_courier03','pc_demo001','wechat',169.80,0,0.00,0.00,169.80,'paid','XB0000000020','T0000000020','WX000000000020','CO000000000020',NULL,NULL,NULL,NULL,'2026-08-09 10:26:45','2026-08-09 10:26:45','2026-08-09 10:26:45'),('pr_gen000020','order','ord_000006','res_courier01','pc_demo001','wechat',29.90,1495,0.00,0.00,14.95,'paid','XB0000000021','T0000000021','WX000000000021','CO000000000021',NULL,NULL,NULL,NULL,'2026-07-25 21:59:36','2026-07-25 21:59:36','2026-07-25 21:59:36'),('pr_gen000021','order','ord_000007','res_004','pc_demo001','wechat',291.40,0,0.00,0.00,291.40,'paid','XB0000000022','T0000000022','WX000000000022','CO000000000022',NULL,NULL,NULL,NULL,'2026-08-09 19:04:38','2026-08-09 19:04:38','2026-08-09 19:04:38'),('pr_gen000022','order','ord_000014','res_courier03','pc_demo001','wechat',45.00,2250,0.00,0.00,22.50,'paid','XB0000000023','T0000000023','WX000000000023','CO000000000023',NULL,NULL,NULL,NULL,'2026-07-29 02:48:15','2026-07-29 02:48:15','2026-07-29 02:48:15'),('pr_gen000023','order','ord_000017','res_002','pc_demo001','wechat',150.80,7540,0.00,0.00,75.40,'paid','XB0000000024','T0000000024','WX000000000024','CO000000000024',NULL,NULL,NULL,NULL,'2026-07-22 19:55:58','2026-07-22 19:55:58','2026-07-22 19:55:58'),('pr_gen000024','order','ord_000018','res_001','pc_demo001','wechat',387.00,0,0.00,0.00,387.00,'paid','XB0000000025','T0000000025','WX000000000025','CO000000000025',NULL,NULL,NULL,NULL,'2026-07-13 14:24:04','2026-07-13 14:24:04','2026-07-13 14:24:04'),('pr_gen000025','order','ord_000021','res_mch04','pc_demo001','wechat',116.00,0,0.00,0.00,116.00,'paid','XB0000000026','T0000000026','WX000000000026','CO000000000026',NULL,NULL,NULL,NULL,'2026-07-11 08:35:40','2026-07-11 08:35:40','2026-07-11 08:35:40'),('pr_gen000026','order','ord_000023','res_admin01','pc_demo001','wechat',90.00,4500,0.00,0.00,45.00,'paid','XB0000000027','T0000000027','WX000000000027','CO000000000027',NULL,NULL,NULL,NULL,'2026-07-19 22:34:26','2026-07-19 22:34:26','2026-07-19 22:34:26'),('pr_gen000027','order','ord_000025','res_mch03','pc_demo001','wechat',269.70,13485,0.00,0.00,134.85,'paid','XB0000000028','T0000000028','WX000000000028','CO000000000028',NULL,NULL,NULL,NULL,'2026-07-16 06:22:17','2026-07-16 06:22:17','2026-07-16 06:22:17'),('pr_gen000028','order','ord_000026','res_dev002','pc_demo001','wechat',35.00,0,0.00,0.00,35.00,'paid','XB0000000029','T0000000029','WX000000000029','CO000000000029',NULL,NULL,NULL,NULL,'2026-07-15 17:22:21','2026-07-15 17:22:21','2026-07-15 17:22:21'),('pr_gen000029','order','ord_000027','res_dev004','pc_demo001','wechat',119.70,5985,0.00,0.00,59.85,'paid','XB0000000030','T0000000030','WX000000000030','CO000000000030',NULL,NULL,NULL,NULL,'2026-08-09 22:58:22','2026-08-09 22:58:22','2026-08-09 22:58:22'),('pr_gen000030','order','ord_000032','res_mch01','pc_demo001','wechat',192.00,0,0.00,0.00,192.00,'paid','XB0000000031','T0000000031','WX000000000031','CO000000000031',NULL,NULL,NULL,NULL,'2026-08-03 04:37:32','2026-08-03 04:37:32','2026-08-03 04:37:32'),('pr_gen000031','order','ord_000033','res_courier03','pc_demo001','wechat',170.00,8500,0.00,0.00,85.00,'paid','XB0000000032','T0000000032','WX000000000032','CO000000000032',NULL,NULL,NULL,NULL,'2026-07-04 23:31:58','2026-07-04 23:31:58','2026-07-04 23:31:58'),('pr_gen000032','order','ord_000036','res_courier03','pc_demo001','wechat',124.00,0,0.00,0.00,124.00,'paid','XB0000000033','T0000000033','WX000000000033','CO000000000033',NULL,NULL,NULL,NULL,'2026-08-01 10:34:11','2026-08-01 10:34:11','2026-08-01 10:34:11'),('pr_gen000033','order','ord_000037','res_tech01','pc_demo001','wechat',227.50,11375,0.00,0.00,113.75,'paid','XB0000000034','T0000000034','WX000000000034','CO000000000034',NULL,NULL,NULL,NULL,'2026-07-16 11:21:08','2026-07-16 11:21:08','2026-07-16 11:21:08'),('pr_gen000034','order','ord_000038','res_courier01','pc_demo001','wechat',248.70,0,0.00,0.00,248.70,'paid','XB0000000035','T0000000035','WX000000000035','CO000000000035',NULL,NULL,NULL,NULL,'2026-07-03 13:26:10','2026-07-03 13:26:10','2026-07-03 13:26:10'),('pr_gen000035','order','ord_000039','res_002','pc_demo001','wechat',79.80,3990,0.00,0.00,39.90,'paid','XB0000000036','T0000000036','WX000000000036','CO000000000036',NULL,NULL,NULL,NULL,'2026-07-28 17:53:52','2026-07-28 17:53:52','2026-07-28 17:53:52'),('pr_gen000036','order','ord_000042','res_dev003','pc_demo001','wechat',129.80,0,0.00,0.00,129.80,'paid','XB0000000037','T0000000037','WX000000000037','CO000000000037',NULL,NULL,NULL,NULL,'2026-08-07 02:35:47','2026-08-07 02:35:47','2026-08-07 02:35:47'),('pr_gen000037','order','ord_000043','res_dev003','pc_demo001','wechat',35.00,0,0.00,0.00,35.00,'paid','XB0000000038','T0000000038','WX000000000038','CO000000000038',NULL,NULL,NULL,NULL,'2026-07-28 14:12:45','2026-07-28 14:12:45','2026-07-28 14:12:45'),('pr_gen000038','order','ord_000045','res_dev003','pc_demo001','wechat',59.80,0,0.00,0.00,59.80,'paid','XB0000000039','T0000000039','WX000000000039','CO000000000039',NULL,NULL,NULL,NULL,'2026-08-06 03:58:02','2026-08-06 03:58:02','2026-08-06 03:58:02'),('pr_gen000039','order','ord_000047','res_001','pc_demo001','wechat',38.00,1900,0.00,0.00,19.00,'paid','XB0000000040','T0000000040','WX000000000040','CO000000000040',NULL,NULL,NULL,NULL,'2026-07-04 05:29:30','2026-07-04 05:29:30','2026-07-04 05:29:30'),('pr_gen000040','order','ord_000048','res_propadmin01','pc_demo001','wechat',227.00,11350,0.00,0.00,113.50,'paid','XB0000000041','T0000000041','WX000000000041','CO000000000041',NULL,NULL,NULL,NULL,'2026-07-30 06:20:06','2026-07-30 06:20:06','2026-07-30 06:20:06'),('pr_gen000041','order','ord_000052','res_mch02','pc_demo001','wechat',80.00,4000,0.00,0.00,40.00,'paid','XB0000000042','T0000000042','WX000000000042','CO000000000042',NULL,NULL,NULL,NULL,'2026-07-07 12:35:38','2026-07-07 12:35:38','2026-07-07 12:35:38'),('pr_gen000042','order','ord_000054','res_dev002','pc_demo001','wechat',404.50,20225,0.00,0.00,202.25,'paid','XB0000000043','T0000000043','WX000000000043','CO000000000043',NULL,NULL,NULL,NULL,'2026-08-07 10:12:00','2026-08-07 10:12:00','2026-08-07 10:12:00'),('pr_gen000043','order','ord_000055','res_courier02','pc_demo001','wechat',101.00,0,0.00,0.00,101.00,'paid','XB0000000044','T0000000044','WX000000000044','CO000000000044',NULL,NULL,NULL,NULL,'2026-08-10 11:05:29','2026-08-10 11:05:29','2026-08-10 11:05:29'),('pr_gen000044','order','ord_000060','res_act01','pc_demo001','wechat',154.00,0,0.00,0.00,154.00,'paid','XB0000000045','T0000000045','WX000000000045','CO000000000045',NULL,NULL,NULL,NULL,'2026-07-26 14:39:28','2026-07-26 14:39:28','2026-07-26 14:39:28'),('pr_gen000045','order','ord_000063','res_mch04','pc_demo001','wechat',404.70,20235,0.00,0.00,202.35,'paid','XB0000000046','T0000000046','WX000000000046','CO000000000046',NULL,NULL,NULL,NULL,'2026-07-15 09:49:49','2026-07-15 09:49:49','2026-07-15 09:49:49'),('pr_gen000046','order','ord_000069','res_dev003','pc_demo001','wechat',317.40,0,0.00,0.00,317.40,'paid','XB0000000047','T0000000047','WX000000000047','CO000000000047',NULL,NULL,NULL,NULL,'2026-07-17 08:44:23','2026-07-17 08:44:23','2026-07-17 08:44:23'),('pr_gen000047','order','ord_000071','res_001','pc_demo001','wechat',15.90,0,0.00,0.00,15.90,'paid','XB0000000048','T0000000048','WX000000000048','CO000000000048',NULL,NULL,NULL,NULL,'2026-07-18 05:36:42','2026-07-18 05:36:42','2026-07-18 05:36:42'),('pr_gen000048','order','ord_000072','res_courier02','pc_demo001','wechat',39.90,1995,0.00,0.00,19.95,'paid','XB0000000049','T0000000049','WX000000000049','CO000000000049',NULL,NULL,NULL,NULL,'2026-07-14 13:17:53','2026-07-14 13:17:53','2026-07-14 13:17:53'),('pr_gen000049','order','ord_000076','res_courier02','pc_demo001','wechat',52.00,0,0.00,0.00,52.00,'paid','XB0000000050','T0000000050','WX000000000050','CO000000000050',NULL,NULL,NULL,NULL,'2026-08-08 16:19:22','2026-08-08 16:19:22','2026-08-08 16:19:22'),('pr_gen000050','order','ord_000078','res_003','pc_demo001','wechat',84.00,0,0.00,0.00,84.00,'paid','XB0000000051','T0000000051','WX000000000051','CO000000000051',NULL,NULL,NULL,NULL,'2026-07-15 16:39:23','2026-07-15 16:39:23','2026-07-15 16:39:23'),('pr_gen000051','order','ord_000085','res_003','pc_demo001','wechat',108.00,0,0.00,0.00,108.00,'paid','XB0000000052','T0000000052','WX000000000052','CO000000000052',NULL,NULL,NULL,NULL,'2026-07-10 16:02:48','2026-07-10 16:02:48','2026-07-10 16:02:48'),('pr_gen000052','order','ord_000087','res_propadmin01','pc_demo001','wechat',54.00,0,0.00,0.00,54.00,'paid','XB0000000053','T0000000053','WX000000000053','CO000000000053',NULL,NULL,NULL,NULL,'2026-07-03 19:30:01','2026-07-03 19:30:01','2026-07-03 19:30:01'),('pr_gen000053','order','ord_000094','res_mch03','pc_demo001','wechat',18.00,900,0.00,0.00,9.00,'paid','XB0000000054','T0000000054','WX000000000054','CO000000000054',NULL,NULL,NULL,NULL,'2026-08-10 13:50:24','2026-08-10 13:50:24','2026-08-10 13:50:24'),('pr_gen000054','order','ord_000096','res_admin01','pc_demo001','wechat',116.00,5800,0.00,0.00,58.00,'paid','XB0000000055','T0000000055','WX000000000055','CO000000000055',NULL,NULL,NULL,NULL,'2026-08-04 10:17:05','2026-08-04 10:17:05','2026-08-04 10:17:05'),('pr_gen000055','order','ord_000101','res_dev003','pc_demo001','wechat',227.00,11350,0.00,0.00,113.50,'paid','XB0000000056','T0000000056','WX000000000056','CO000000000056',NULL,NULL,NULL,NULL,'2026-07-09 18:02:12','2026-07-09 18:02:12','2026-07-09 18:02:12'),('pr_gen000056','order','ord_000110','res_mch03','pc_demo001','wechat',98.30,0,0.00,0.00,98.30,'paid','XB0000000057','T0000000057','WX000000000057','CO000000000057',NULL,NULL,NULL,NULL,'2026-07-21 04:55:15','2026-07-21 04:55:15','2026-07-21 04:55:15'),('pr_gen000057','order','ord_000111','res_dev004','pc_demo001','wechat',73.40,3670,0.00,0.00,36.70,'paid','XB0000000058','T0000000058','WX000000000058','CO000000000058',NULL,NULL,NULL,NULL,'2026-08-07 06:35:22','2026-08-07 06:35:22','2026-08-07 06:35:22'),('pr_gen000058','order','ord_000114','res_dev001','pc_demo001','wechat',38.70,1935,0.00,0.00,19.35,'paid','XB0000000059','T0000000059','WX000000000059','CO000000000059',NULL,NULL,NULL,NULL,'2026-07-07 08:49:58','2026-07-07 08:49:58','2026-07-07 08:49:58'),('pr_gen000059','order','ord_000115','res_001','pc_demo001','wechat',46.00,0,0.00,0.00,46.00,'paid','XB0000000060','T0000000060','WX000000000060','CO000000000060',NULL,NULL,NULL,NULL,'2026-07-24 18:06:56','2026-07-24 18:06:56','2026-07-24 18:06:56'),('pr_gen000060','order','ord_000117','res_mch04','pc_demo001','wechat',24.00,0,0.00,0.00,24.00,'paid','XB0000000061','T0000000061','WX000000000061','CO000000000061',NULL,NULL,NULL,NULL,'2026-07-23 06:32:55','2026-07-23 06:32:55','2026-07-23 06:32:55'),('pr_gen000061','order','ord_000118','res_admin01','pc_demo001','wechat',160.20,8010,0.00,0.00,80.10,'paid','XB0000000062','T0000000062','WX000000000062','CO000000000062',NULL,NULL,NULL,NULL,'2026-07-08 07:29:06','2026-07-08 07:29:06','2026-07-08 07:29:06');
/*!40000 ALTER TABLE `payment_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment_settle_outbox`
--

DROP TABLE IF EXISTS `payment_settle_outbox`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_settle_outbox` (
  `id` varchar(32) NOT NULL COMMENT 'pso_xxxxxxxx 发件箱主键',
  `record_id` varchar(32) NOT NULL COMMENT 'payment_records.id 支付流水ID',
  `biz_type` varchar(32) NOT NULL COMMENT 'order/property_fee/coin_recharge/service_order/service_subscription/activity_subscription/resident_shop_deposit',
  `biz_id` varchar(32) DEFAULT NULL COMMENT '业务单ID(订单ID/物业费单ID/充值单ID等)',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PROCESSING/DONE/FAILED/FAILED_PERMANENT',
  `retry_count` int NOT NULL DEFAULT '0' COMMENT '已重试次数(指数退避,超过阈值转 FAILED_PERMANENT)',
  `next_retry_at` timestamp NULL DEFAULT NULL COMMENT '下次重试时间',
  `last_error` varchar(2000) DEFAULT NULL COMMENT '最近一次 finalize 失败原因(便于人工对账)',
  `version` bigint NOT NULL DEFAULT '0' COMMENT '乐观锁版本(防止并发重复处理)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record` (`record_id`),
  KEY `idx_status_next` (`status`,`next_retry_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付回调业务finalize发件箱(Outbox)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_settle_outbox`
--

LOCK TABLES `payment_settle_outbox` WRITE;
/*!40000 ALTER TABLE `payment_settle_outbox` DISABLE KEYS */;
INSERT INTO `payment_settle_outbox` VALUES ('pso__35266db24e1000','pr_35266c00941000','order','ord_35266bd4c41000','PROCESSING',0,NULL,NULL,11367,'2026-08-14 01:16:35','2026-09-03 01:18:50'),('pso__35267c5c3e1000','pr_35267b359e1000','order','ord_35267afe501000','PROCESSING',0,NULL,NULL,11365,'2026-08-14 01:24:35','2026-09-03 01:21:50'),('pso__35268cd8f61000','pr_35268b1d9e1000','order','ord_35268adb481000','PROCESSING',0,NULL,NULL,11359,'2026-08-14 01:33:36','2026-09-03 01:20:00'),('pso__3526aebbc21000','pr_3526ad7ab01000','order','ord_3526ad6bfc1000','PROCESSING',0,NULL,NULL,11351,'2026-08-14 01:52:06','2026-09-03 01:20:20');
/*!40000 ALTER TABLE `payment_settle_outbox` ENABLE KEYS */;
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
INSERT INTO `permission_change_logs` VALUES ('pcl_3585dab8a61000','res_35217712f21000','res_admin01','pc_demo001','grant','activity_group:manage',NULL,'false','true','2026-08-23 07:38:05','2026-08-23 07:38:05'),('pcl_3585dab8ba1000','res_35217712f21000','res_admin01','pc_demo001','grant','announcement:publish',NULL,'false','true','2026-08-23 07:38:05','2026-08-23 07:38:05'),('pcl_3585dab8c01000','res_35217712f21000','res_admin01','pc_demo001','grant','merchant:audit',NULL,'false','true','2026-08-23 07:38:05','2026-08-23 07:38:05'),('pcl_3585dab8ca1001','res_35217712f21000','res_admin01','pc_demo001','grant','promotion:push',NULL,'false','true','2026-08-23 07:38:05','2026-08-23 07:38:05'),('pcl_3585dab8de1001','res_35217712f21000','res_admin01','pc_demo001','grant','rank:drag',NULL,'false','true','2026-08-23 07:38:05','2026-08-23 07:38:05'),('pcl_3585dab8e41001','res_35217712f21000','res_admin01','pc_demo001','grant','sector:manage',NULL,'false','true','2026-08-23 07:38:05','2026-08-23 07:38:05'),('pcl_3585dab8ec1001','res_35217712f21000','res_admin01','pc_demo001','grant','sector:read',NULL,'false','true','2026-08-23 07:38:05','2026-08-23 07:38:05'),('pcl_3585dbcda01000','res_35217712f21000','res_admin01','pc_demo001','revoke','rank:drag',NULL,'true','false','2026-08-23 07:38:41','2026-08-23 07:38:41'),('pcl_3585dbcdaa1000','res_35217712f21000','res_admin01','pc_demo001','revoke','sector:read',NULL,'true','false','2026-08-23 07:38:41','2026-08-23 07:38:41'),('pcl_3585dbcdb01000','res_35217712f21000','res_admin01','pc_demo001','revoke','merchant:audit',NULL,'true','false','2026-08-23 07:38:41','2026-08-23 07:38:41'),('pcl_3585dbcdb61000','res_35217712f21000','res_admin01','pc_demo001','revoke','activity_group:manage',NULL,'true','false','2026-08-23 07:38:41','2026-08-23 07:38:41'),('pcl_3585dbcdbc1000','res_35217712f21000','res_admin01','pc_demo001','revoke','promotion:push',NULL,'true','false','2026-08-23 07:38:41','2026-08-23 07:38:41'),('pcl_3585dbcdc21000','res_35217712f21000','res_admin01','pc_demo001','revoke','sector:manage',NULL,'true','false','2026-08-23 07:38:41','2026-08-23 07:38:41'),('pcl_3585dbcdc61000','res_35217712f21000','res_admin01','pc_demo001','revoke','announcement:publish',NULL,'true','false','2026-08-23 07:38:41','2026-08-23 07:38:41'),('pcl_35e07c9c481000','res_coo01','res_pleader01','pc_demo001','grant','merchant:audit',NULL,'false','true','2026-09-01 02:49:25','2026-09-01 02:49:25'),('pcl_35e07ca2421000','res_coo01','res_pleader01','pc_demo001','revoke','pool:read',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca2521000','res_coo01','res_pleader01','pc_demo001','revoke','notice:collect',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca25a1001','res_coo01','res_pleader01','pc_demo001','revoke','config:set',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca2641001','res_coo01','res_pleader01','pc_demo001','revoke','pool:adjust',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca26a1000','res_coo01','res_pleader01','pc_demo001','revoke','distributor:manage',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca2741000','res_coo01','res_pleader01','pc_demo001','revoke','coordinator:read',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca27c1001','res_coo01','res_pleader01','pc_demo001','revoke','data:view',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca28a1000','res_coo01','res_pleader01','pc_demo001','revoke','reports:view',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca2921000','res_coo01','res_pleader01','pc_demo001','revoke','community:read',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca2981001','res_coo01','res_pleader01','pc_demo001','revoke','log:read',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca29e1001','res_coo01','res_pleader01','pc_demo001','revoke','community_entity:manage',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca2a41001','res_coo01','res_pleader01','pc_demo001','revoke','coin:audit',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca2a81001','res_coo01','res_pleader01','pc_demo001','revoke','user:manage',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca2ae1001','res_coo01','res_pleader01','pc_demo001','revoke','permission:manage',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca2b41001','res_coo01','res_pleader01','pc_demo001','revoke','individual:manage',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca2ba1001','res_coo01','res_pleader01','pc_demo001','revoke','merchant:kickout',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca2be1001','res_coo01','res_pleader01','pc_demo001','revoke','resident_merchant:audit',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca2c61000','res_coo01','res_pleader01','pc_demo001','revoke','coordinator:manage',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca2cc1000','res_coo01','res_pleader01','pc_demo001','revoke','points:input',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca2d01001','res_coo01','res_pleader01','pc_demo001','revoke','point:clear_run',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca2d81001','res_coo01','res_pleader01','pc_demo001','revoke','property_operator:manage',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca2de1000','res_coo01','res_pleader01','pc_demo001','revoke','refund:approve',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca2e41001','res_coo01','res_pleader01','pc_demo001','revoke','coin:freeze',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca2ea1001','res_coo01','res_pleader01','pc_demo001','revoke','coin:display_control',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca2f21000','res_coo01','res_pleader01','pc_demo001','revoke','consultation:manage',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca2fa1000','res_coo01','res_pleader01','pc_demo001','revoke','property_fee:manage',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca2fe1000','res_coo01','res_pleader01','pc_demo001','revoke','permission:read',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca3021000','res_coo01','res_pleader01','pc_demo001','revoke','distribution:read',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca3081001','res_coo01','res_pleader01','pc_demo001','revoke','directed_message:send',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca3101001','res_coo01','res_pleader01','pc_demo001','revoke','directed_message:stats',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca3181001','res_coo01','res_pleader01','pc_demo001','revoke','community:manage',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca3201000','res_coo01','res_pleader01','pc_demo001','revoke','directed_message:read',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26'),('pcl_35e07ca3261000','res_coo01','res_pleader01','pc_demo001','revoke','delivery:grab',NULL,'true','false','2026-09-01 02:49:26','2026-09-01 02:49:26');
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
INSERT INTO `permissions` VALUES ('perm_001','announcement.publish','发布公告',NULL,'announcement','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_002','merchant.audit','商家审核',NULL,'merchant','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_003','merchant.kickout','踢出商家',NULL,'merchant','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_004','rank.drag','排名拖拽',NULL,'rank','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_005','promotion.push','特惠推送',NULL,'promotion','active','2026-07-02 01:57:36','2026-09-08 04:58:41'),('perm_006','activity_group.manage','活动组管理',NULL,'activity_group','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_007','sector.manage','板块管理',NULL,'sector','active','2026-07-02 01:57:36','2026-09-08 04:58:41'),('perm_008','individual.manage','个体负责人管理',NULL,'individual','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_009','delivery.grab','送货抢单',NULL,'delivery','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_010','data.view','数据查看',NULL,'data','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_011','notice.collect','通知征集',NULL,'notice','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_012','refund.approve','退费审批',NULL,'refund','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_013','points.input','积分录入',NULL,'points','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_014','coin.freeze','物业币冻结',NULL,'coin','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_015','coin.display_control','物业币显示控制',NULL,'coin','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_016','config.set','参数配置',NULL,'config','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_017','property_fee.manage','物业费管理',NULL,'property_fee','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_018','reports.view','报表查看',NULL,'reports','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_019','user.manage','用户管理',NULL,'user','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_020','permission.manage','权限管理',NULL,'permission','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_021','permission.read','权限查看',NULL,'permission','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_022','pool.read','积分池查看',NULL,'pool','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_023','pool.adjust','积分池调整',NULL,'pool','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_024','coordinator.read','统筹查看',NULL,'coordinator','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_025','coordinator.manage','统筹管理',NULL,'coordinator','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_026','sector.read','板块负责人查看',NULL,'sector','active','2026-07-02 01:57:36','2026-09-08 04:58:41'),('perm_027','distribution.read','分成记录查看',NULL,'distribution','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_028','coin.audit','物业币兑换审核',NULL,'coin','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_029','point.clear_run','2年积分清零',NULL,'points','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_030','log.read','操作日志查看',NULL,'log','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_031','merchant.create','创建平台商家',NULL,'merchant','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_032','merchant.read','平台商家查看',NULL,'merchant','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_033','merchant.update','更新平台商家',NULL,'merchant','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_034','property_company.manage','物业公司管理',NULL,'property_company','active','2026-07-02 01:57:36','2026-07-02 01:57:36'),('perm_035','directed_message.send','定向消息发送',NULL,'message','active','2026-07-13 10:58:06','2026-07-13 10:58:06'),('perm_036','directed_message.read','定向消息查看',NULL,'message','active','2026-07-13 10:58:06','2026-07-13 10:58:06'),('perm_037','directed_message.stats','定向消息统计',NULL,'message','active','2026-07-13 10:58:06','2026-07-13 10:58:06'),('perm_038','community_entity.manage','社区管理',NULL,'community','active','2026-07-13 10:58:06','2026-07-13 10:58:06'),('perm_039','property_operator.manage','物业管理员管理',NULL,'user','active','2026-07-13 10:58:06','2026-07-13 10:58:06'),('perm_040','consultation.manage','咨询板块管理',NULL,'consultation','active','2026-07-13 10:58:06','2026-07-13 10:58:06'),('perm_041','resident_merchant.audit','业主商户审核',NULL,'merchant','active','2026-07-13 10:58:06','2026-07-13 10:58:06'),('perm_042','distributor.manage','一级经销商管理',NULL,'merchant','active','2026-07-13 10:58:06','2026-07-13 10:58:06'),('perm_043','community.read','社区论坛-查看','查看本小区帖子与举报列表','community','active','2026-08-03 06:57:26','2026-08-03 06:57:26'),('perm_044','community.manage','社区论坛-管理','删帖与处理举报','community','active','2026-08-03 06:57:26','2026-08-03 06:57:26');
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
INSERT INTO `platform_merchants` VALUES ('pm_355b5116541000','外套的店',NULL,NULL,NULL,'19852681536','外卖',NULL,'active','2026-08-19 04:30:54','2026-08-19 04:30:54'),('pm_355d08ea541000','外套的店',NULL,NULL,NULL,'19852681536','外卖',NULL,'active','2026-08-19 08:31:06','2026-08-19 08:31:06'),('pm_355d11a1f01000','外套的技工',NULL,NULL,NULL,'19852681536',NULL,NULL,'active','2026-08-19 08:35:52','2026-08-19 08:35:52'),('pm_35c14661a61000','立民社区食堂',NULL,NULL,NULL,'13987654321',NULL,NULL,'active','2026-08-29 02:05:41','2026-08-29 02:05:41'),('pm_35c14bfbe41000','民生小面',NULL,NULL,NULL,'12987654321',NULL,NULL,'active','2026-08-29 02:08:45','2026-08-29 02:08:45'),('pm_act01','郑美丽的小店',NULL,NULL,NULL,NULL,'周围商家',NULL,'active','2026-08-12 08:57:49','2026-08-12 08:57:49'),('pm_demo001','口福餐厅',NULL,NULL,NULL,NULL,'餐饮',NULL,'active','2026-07-05 06:24:24','2026-07-05 06:24:24'),('pm_demo002','便民超市',NULL,NULL,NULL,NULL,'超市',NULL,'active','2026-07-05 06:24:24','2026-07-05 06:24:24'),('pm_mch01','陈老板的店',NULL,NULL,NULL,NULL,'外卖',NULL,'active','2026-08-12 08:40:05','2026-08-12 08:40:05'),('pm_tech01','刘师傅的服务',NULL,NULL,NULL,NULL,'便民维修',NULL,'active','2026-08-12 08:57:49','2026-08-12 08:57:49');
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
INSERT INTO `point_pools` VALUES ('pp_8ed41000','pc_8df81000',0.00,0.00,0.00,'2026-09-07 14:38:38','2026-09-07 14:38:38'),('pp_fix_baid001','pc_baid001',0.00,0.00,0.00,'2026-09-07 11:08:02','2026-09-07 11:08:02'),('pp_fix_demo001','pc_demo001',0.00,0.00,0.00,'2026-09-07 11:08:02','2026-09-07 11:08:02'),('pp_fix_hrl001','pc_hrl001',0.00,0.00,0.00,'2026-09-07 11:08:02','2026-09-07 11:08:02'),('pp_fix_vanke001','pc_vanke001',0.00,0.00,0.00,'2026-09-07 11:08:02','2026-09-07 11:08:02');
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
  `idempotency_key` varchar(64) DEFAULT NULL COMMENT '幂等键，防重复出账流水',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_point_records_idempotency` (`idempotency_key`),
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
INSERT INTO `point_records` VALUES ('pt_000001','res_001','fam_001','pc_demo001','earn',3000,3000,'manual',NULL,'账户初始化积分',NULL,NULL,'2026-07-19 01:17:43','2026-07-19 01:17:43'),('pt_000002','res_001','fam_001','pc_demo001','earn',2503,5503,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-07-21 20:17:43','2026-07-21 20:17:43'),('pt_000003','res_001','fam_001','pc_demo001','spend',-2503,3000,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-22 09:17:43','2026-07-22 09:17:43'),('pt_000004','res_001','fam_001','pc_demo001','earn',2503,5503,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-07-25 02:17:43','2026-07-25 02:17:43'),('pt_000005','res_001','fam_001','pc_demo001','spend',-2503,3000,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-25 15:17:43','2026-07-25 15:17:43'),('pt_000006','res_001','fam_001','pc_demo001','earn',2503,5503,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-07-27 22:17:43','2026-07-27 22:17:43'),('pt_000007','res_001','fam_001','pc_demo001','spend',-2503,3000,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-28 15:17:43','2026-07-28 15:17:43'),('pt_000008','res_001','fam_001','pc_demo001','earn',2503,5503,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-07-29 16:17:43','2026-07-29 16:17:43'),('pt_000009','res_001','fam_001','pc_demo001','spend',-2503,3000,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-30 23:17:43','2026-07-30 23:17:43'),('pt_000010','res_001','fam_001','pc_demo001','earn',2503,5503,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-08-01 11:17:43','2026-08-01 11:17:43'),('pt_000011','res_001','fam_001','pc_demo001','spend',-2503,3000,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-04 01:17:43','2026-08-04 01:17:43'),('pt_000012','res_001','fam_001','pc_demo001','earn',2503,5503,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-08-04 11:17:43','2026-08-04 11:17:43'),('pt_000013','res_001','fam_001','pc_demo001','spend',-2503,3000,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-06 03:17:43','2026-08-06 03:17:43'),('pt_000014','res_001','fam_001','pc_demo001','earn',2503,5503,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-08-08 20:17:43','2026-08-08 20:17:43'),('pt_000015','res_001','fam_001','pc_demo001','spend',-2503,3000,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-09 21:17:43','2026-08-09 21:17:43'),('pt_000016','res_001','fam_001','pc_demo001','earn',2503,5503,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-08-11 14:17:43','2026-08-11 14:17:43'),('pt_000017','res_001','fam_001','pc_demo001','spend',-2503,3000,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-12 06:17:43','2026-08-12 06:17:43'),('pt_000018','res_001','fam_001','pc_demo001','earn',2503,5503,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-08-13 04:17:43','2026-08-13 04:17:43'),('pt_000019','res_001','fam_001','pc_demo001','spend',-2503,3000,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-15 22:17:43','2026-08-15 22:17:43'),('pt_000020','res_001','fam_001','pc_demo001','earn',2503,5503,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-08-17 13:17:43','2026-08-17 13:17:43'),('pt_000021','res_001','fam_001','pc_demo001','spend',-2503,3000,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-20 13:17:43','2026-08-20 13:17:43'),('pt_000022','res_002','fam_002','pc_demo001','earn',1500,1500,'manual',NULL,'账户初始化积分',NULL,NULL,'2026-07-16 23:12:53','2026-07-16 23:12:53'),('pt_000023','res_002','fam_002','pc_demo001','earn',3060,4560,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-07-17 17:12:53','2026-07-17 17:12:53'),('pt_000024','res_002','fam_002','pc_demo001','spend',-3060,1500,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-19 10:12:53','2026-07-19 10:12:53'),('pt_000025','res_002','fam_002','pc_demo001','earn',3060,4560,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-07-20 03:12:53','2026-07-20 03:12:53'),('pt_000026','res_002','fam_002','pc_demo001','spend',-3060,1500,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-22 08:12:53','2026-07-22 08:12:53'),('pt_000027','res_002','fam_002','pc_demo001','earn',3060,4560,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-07-24 10:12:53','2026-07-24 10:12:53'),('pt_000028','res_002','fam_002','pc_demo001','spend',-3060,1500,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-25 13:12:53','2026-07-25 13:12:53'),('pt_000029','res_002','fam_002','pc_demo001','earn',3060,4560,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-07-28 10:12:53','2026-07-28 10:12:53'),('pt_000030','res_002','fam_002','pc_demo001','spend',-3060,1500,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-29 16:12:53','2026-07-29 16:12:53'),('pt_000031','res_002','fam_002','pc_demo001','earn',3060,4560,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-07-31 02:12:53','2026-07-31 02:12:53'),('pt_000032','res_002','fam_002','pc_demo001','spend',-3060,1500,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-02 19:12:53','2026-08-02 19:12:53'),('pt_000033','res_002','fam_002','pc_demo001','earn',3060,4560,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-08-04 16:12:53','2026-08-04 16:12:53'),('pt_000034','res_002','fam_002','pc_demo001','spend',-3060,1500,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-05 14:12:53','2026-08-05 14:12:53'),('pt_000035','res_002','fam_002','pc_demo001','earn',3060,4560,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-08-08 01:12:53','2026-08-08 01:12:53'),('pt_000036','res_002','fam_002','pc_demo001','spend',-3060,1500,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-10 11:12:53','2026-08-10 11:12:53'),('pt_000037','res_002','fam_002','pc_demo001','earn',3060,4560,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-08-11 23:12:53','2026-08-11 23:12:53'),('pt_000038','res_002','fam_002','pc_demo001','spend',-3060,1500,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-14 15:12:53','2026-08-14 15:12:53'),('pt_000039','res_002','fam_002','pc_demo001','earn',3060,4560,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-08-17 09:12:53','2026-08-17 09:12:53'),('pt_000040','res_002','fam_002','pc_demo001','spend',-3060,1500,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-19 23:12:53','2026-08-19 23:12:53'),('pt_000041','res_002','fam_002','pc_demo001','earn',3060,4560,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-08-21 04:12:53','2026-08-21 04:12:53'),('pt_000042','res_002','fam_002','pc_demo001','spend',-3060,1500,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-23 20:12:53','2026-08-23 20:12:53'),('pt_000043','res_003','fam_003','pc_demo001','earn',2000,2000,'manual',NULL,'账户初始化积分',NULL,NULL,'2026-07-14 08:41:18','2026-07-14 08:41:18'),('pt_000044','res_003','fam_003','pc_demo001','earn',1855,3855,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-07-16 21:41:18','2026-07-16 21:41:18'),('pt_000045','res_003','fam_003','pc_demo001','spend',-1855,2000,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-18 21:41:18','2026-07-18 21:41:18'),('pt_000046','res_003','fam_003','pc_demo001','earn',1855,3855,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-07-21 20:41:18','2026-07-21 20:41:18'),('pt_000047','res_003','fam_003','pc_demo001','spend',-1855,2000,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-23 03:41:18','2026-07-23 03:41:18'),('pt_000048','res_003','fam_003','pc_demo001','earn',1855,3855,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-07-24 00:41:18','2026-07-24 00:41:18'),('pt_000049','res_003','fam_003','pc_demo001','spend',-1855,2000,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-25 00:41:18','2026-07-25 00:41:18'),('pt_000050','res_003','fam_003','pc_demo001','earn',1855,3855,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-07-26 06:41:18','2026-07-26 06:41:18'),('pt_000051','res_003','fam_003','pc_demo001','spend',-1855,2000,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-27 22:41:18','2026-07-27 22:41:18'),('pt_000052','res_003','fam_003','pc_demo001','earn',1856,3856,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-07-28 07:41:18','2026-07-28 07:41:18'),('pt_000053','res_003','fam_003','pc_demo001','spend',-1856,2000,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-30 05:41:18','2026-07-30 05:41:18'),('pt_000054','res_003','fam_003','pc_demo001','earn',1856,3856,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-07-30 14:41:18','2026-07-30 14:41:18'),('pt_000055','res_003','fam_003','pc_demo001','spend',-1856,2000,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-30 22:41:18','2026-07-30 22:41:18'),('pt_000056','res_003','fam_003','pc_demo001','earn',1856,3856,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-08-01 10:41:18','2026-08-01 10:41:18'),('pt_000057','res_003','fam_003','pc_demo001','spend',-1856,2000,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-02 05:41:18','2026-08-02 05:41:18'),('pt_000058','res_003','fam_003','pc_demo001','earn',1856,3856,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-08-03 03:41:18','2026-08-03 03:41:18'),('pt_000059','res_003','fam_003','pc_demo001','spend',-1856,2000,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-03 07:41:18','2026-08-03 07:41:18'),('pt_000060','res_003','fam_003','pc_demo001','earn',1856,3856,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-08-04 05:41:18','2026-08-04 05:41:18'),('pt_000061','res_003','fam_003','pc_demo001','spend',-1856,2000,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-05 06:41:18','2026-08-05 06:41:18'),('pt_000062','res_004',NULL,'pc_demo001','earn',500,500,'manual',NULL,'账户初始化积分',NULL,NULL,'2026-07-26 05:44:21','2026-07-26 05:44:21'),('pt_000063','res_004',NULL,'pc_demo001','earn',1993,2493,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-07-26 12:44:21','2026-07-26 12:44:21'),('pt_000064','res_004',NULL,'pc_demo001','spend',-1993,500,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-28 18:44:21','2026-07-28 18:44:21'),('pt_000065','res_004',NULL,'pc_demo001','earn',1993,2493,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-07-29 08:44:21','2026-07-29 08:44:21'),('pt_000066','res_004',NULL,'pc_demo001','spend',-1993,500,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-30 01:44:21','2026-07-30 01:44:21'),('pt_000067','res_004',NULL,'pc_demo001','earn',1994,2494,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-07-31 18:44:21','2026-07-31 18:44:21'),('pt_000068','res_004',NULL,'pc_demo001','spend',-1994,500,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-01 17:44:21','2026-08-01 17:44:21'),('pt_000069','res_act01',NULL,'pc_demo001','earn',600,600,'manual',NULL,'账户初始化积分',NULL,NULL,'2026-07-19 12:23:09','2026-07-19 12:23:09'),('pt_000070','res_act01',NULL,'pc_demo001','earn',1733,2333,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-07-21 01:23:09','2026-07-21 01:23:09'),('pt_000071','res_act01',NULL,'pc_demo001','spend',-1733,600,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-23 02:23:09','2026-07-23 02:23:09'),('pt_000072','res_act01',NULL,'pc_demo001','earn',1733,2333,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-07-24 07:23:09','2026-07-24 07:23:09'),('pt_000073','res_act01',NULL,'pc_demo001','spend',-1733,600,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-25 17:23:09','2026-07-25 17:23:09'),('pt_000074','res_act01',NULL,'pc_demo001','earn',1734,2334,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-07-26 17:23:09','2026-07-26 17:23:09'),('pt_000075','res_act01',NULL,'pc_demo001','spend',-1734,600,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-29 10:23:09','2026-07-29 10:23:09'),('pt_000076','res_admin01',NULL,'pc_demo001','earn',0,0,'manual',NULL,'账户初始化积分',NULL,NULL,'2026-07-25 22:14:25','2026-07-25 22:14:25'),('pt_000077','res_admin01',NULL,'pc_demo001','earn',1831,1831,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-07-27 06:14:25','2026-07-27 06:14:25'),('pt_000078','res_admin01',NULL,'pc_demo001','spend',-1831,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-29 19:14:25','2026-07-29 19:14:25'),('pt_000079','res_admin01',NULL,'pc_demo001','earn',1831,1831,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-07-31 06:14:25','2026-07-31 06:14:25'),('pt_000080','res_admin01',NULL,'pc_demo001','spend',-1831,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-31 13:14:25','2026-07-31 13:14:25'),('pt_000081','res_admin01',NULL,'pc_demo001','earn',1831,1831,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-08-01 04:14:25','2026-08-01 04:14:25'),('pt_000082','res_admin01',NULL,'pc_demo001','spend',-1831,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-01 11:14:25','2026-08-01 11:14:25'),('pt_000083','res_admin01',NULL,'pc_demo001','earn',1831,1831,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-08-04 10:14:25','2026-08-04 10:14:25'),('pt_000084','res_admin01',NULL,'pc_demo001','spend',-1831,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-05 16:14:25','2026-08-05 16:14:25'),('pt_000085','res_admin01',NULL,'pc_demo001','earn',1831,1831,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-08-07 21:14:25','2026-08-07 21:14:25'),('pt_000086','res_admin01',NULL,'pc_demo001','spend',-1831,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-10 03:14:25','2026-08-10 03:14:25'),('pt_000087','res_admin01',NULL,'pc_demo001','earn',1831,1831,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-08-12 22:14:25','2026-08-12 22:14:25'),('pt_000088','res_admin01',NULL,'pc_demo001','spend',-1831,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-15 08:14:25','2026-08-15 08:14:25'),('pt_000089','res_admin01',NULL,'pc_demo001','earn',1831,1831,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-08-18 01:14:25','2026-08-18 01:14:25'),('pt_000090','res_admin01',NULL,'pc_demo001','spend',-1831,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-20 19:14:25','2026-08-20 19:14:25'),('pt_000091','res_admin01',NULL,'pc_demo001','earn',1831,1831,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-08-23 15:14:25','2026-08-23 15:14:25'),('pt_000092','res_admin01',NULL,'pc_demo001','spend',-1831,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-26 07:14:25','2026-08-26 07:14:25'),('pt_000093','res_admin01',NULL,'pc_demo001','earn',1831,1831,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-08-28 21:14:25','2026-08-28 21:14:25'),('pt_000094','res_admin01',NULL,'pc_demo001','spend',-1831,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-29 17:14:25','2026-08-29 17:14:25'),('pt_000095','res_admin01',NULL,'pc_demo001','earn',1831,1831,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-08-30 17:14:25','2026-08-30 17:14:25'),('pt_000096','res_admin01',NULL,'pc_demo001','spend',-1831,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-30 21:14:25','2026-08-30 21:14:25'),('pt_000097','res_coo01',NULL,'pc_demo001','earn',1200,1200,'manual',NULL,'账户初始化积分',NULL,NULL,'2026-07-11 09:10:48','2026-07-11 09:10:48'),('pt_000098','res_coo01',NULL,'pc_demo001','earn',2216,3416,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-07-12 07:10:48','2026-07-12 07:10:48'),('pt_000099','res_coo01',NULL,'pc_demo001','spend',-2216,1200,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-14 18:10:48','2026-07-14 18:10:48'),('pt_000100','res_coo01',NULL,'pc_demo001','earn',2216,3416,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-07-15 05:10:48','2026-07-15 05:10:48'),('pt_000101','res_coo01',NULL,'pc_demo001','spend',-2216,1200,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-15 22:10:48','2026-07-15 22:10:48'),('pt_000102','res_coo01',NULL,'pc_demo001','earn',2216,3416,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-07-16 07:10:48','2026-07-16 07:10:48'),('pt_000103','res_coo01',NULL,'pc_demo001','spend',-2216,1200,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-19 05:10:48','2026-07-19 05:10:48'),('pt_000104','res_coo01',NULL,'pc_demo001','earn',2216,3416,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-07-20 02:10:48','2026-07-20 02:10:48'),('pt_000105','res_coo01',NULL,'pc_demo001','spend',-2216,1200,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-21 11:10:48','2026-07-21 11:10:48'),('pt_000106','res_coo01',NULL,'pc_demo001','earn',2216,3416,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-07-22 04:10:48','2026-07-22 04:10:48'),('pt_000107','res_coo01',NULL,'pc_demo001','spend',-2216,1200,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-23 02:10:48','2026-07-23 02:10:48'),('pt_000108','res_coo01',NULL,'pc_demo001','earn',2216,3416,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-07-26 01:10:48','2026-07-26 01:10:48'),('pt_000109','res_coo01',NULL,'pc_demo001','spend',-2216,1200,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-28 21:10:48','2026-07-28 21:10:48'),('pt_000110','res_coo01',NULL,'pc_demo001','earn',2216,3416,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-07-29 11:10:48','2026-07-29 11:10:48'),('pt_000111','res_coo01',NULL,'pc_demo001','spend',-2216,1200,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-30 19:10:48','2026-07-30 19:10:48'),('pt_000112','res_coo01',NULL,'pc_demo001','earn',2216,3416,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-08-01 08:10:48','2026-08-01 08:10:48'),('pt_000113','res_coo01',NULL,'pc_demo001','spend',-2216,1200,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-02 20:10:48','2026-08-02 20:10:48'),('pt_000114','res_coo01',NULL,'pc_demo001','earn',2216,3416,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-08-03 20:10:48','2026-08-03 20:10:48'),('pt_000115','res_coo01',NULL,'pc_demo001','spend',-2216,1200,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-05 16:10:48','2026-08-05 16:10:48'),('pt_000116','res_coo01',NULL,'pc_demo001','earn',2216,3416,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-08-05 18:10:48','2026-08-05 18:10:48'),('pt_000117','res_coo01',NULL,'pc_demo001','spend',-2216,1200,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-06 14:10:48','2026-08-06 14:10:48'),('pt_000118','res_courier01',NULL,'pc_demo001','earn',200,200,'manual',NULL,'账户初始化积分',NULL,NULL,'2026-07-15 02:02:40','2026-07-15 02:02:40'),('pt_000119','res_courier01',NULL,'pc_demo001','earn',1495,1695,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-07-16 20:02:40','2026-07-16 20:02:40'),('pt_000120','res_courier01',NULL,'pc_demo001','spend',-1495,200,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-18 11:02:40','2026-07-18 11:02:40'),('pt_000121','res_courier02',NULL,'pc_demo001','earn',150,150,'manual',NULL,'账户初始化积分',NULL,NULL,'2026-07-22 10:44:46','2026-07-22 10:44:46'),('pt_000122','res_courier02',NULL,'pc_demo001','earn',3019,3169,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-07-24 04:44:46','2026-07-24 04:44:46'),('pt_000123','res_courier02',NULL,'pc_demo001','spend',-3019,150,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-25 08:44:46','2026-07-25 08:44:46'),('pt_000124','res_courier02',NULL,'pc_demo001','earn',3019,3169,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-07-26 05:44:46','2026-07-26 05:44:46'),('pt_000125','res_courier02',NULL,'pc_demo001','spend',-3019,150,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-26 18:44:46','2026-07-26 18:44:46'),('pt_000126','res_courier02',NULL,'pc_demo001','earn',3019,3169,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-07-28 14:44:46','2026-07-28 14:44:46'),('pt_000127','res_courier02',NULL,'pc_demo001','spend',-3019,150,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-30 22:44:46','2026-07-30 22:44:46'),('pt_000128','res_courier02',NULL,'pc_demo001','earn',3019,3169,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-08-01 05:44:46','2026-08-01 05:44:46'),('pt_000129','res_courier02',NULL,'pc_demo001','spend',-3019,150,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-03 13:44:46','2026-08-03 13:44:46'),('pt_000130','res_courier02',NULL,'pc_demo001','earn',3019,3169,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-08-05 20:44:46','2026-08-05 20:44:46'),('pt_000131','res_courier02',NULL,'pc_demo001','spend',-3019,150,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-07 05:44:46','2026-08-07 05:44:46'),('pt_000132','res_courier02',NULL,'pc_demo001','earn',3020,3170,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-08-07 14:44:46','2026-08-07 14:44:46'),('pt_000133','res_courier02',NULL,'pc_demo001','spend',-3020,150,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-09 15:44:46','2026-08-09 15:44:46'),('pt_000134','res_courier02',NULL,'pc_demo001','earn',3020,3170,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-08-09 18:44:46','2026-08-09 18:44:46'),('pt_000135','res_courier02',NULL,'pc_demo001','spend',-3020,150,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-10 04:44:46','2026-08-10 04:44:46'),('pt_000136','res_courier02',NULL,'pc_demo001','earn',3020,3170,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-08-12 19:44:46','2026-08-12 19:44:46'),('pt_000137','res_courier02',NULL,'pc_demo001','spend',-3020,150,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-15 13:44:46','2026-08-15 13:44:46'),('pt_000138','res_courier02',NULL,'pc_demo001','earn',3020,3170,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-08-18 07:44:46','2026-08-18 07:44:46'),('pt_000139','res_courier02',NULL,'pc_demo001','spend',-3020,150,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-19 07:44:46','2026-08-19 07:44:46'),('pt_000140','res_courier02',NULL,'pc_demo001','earn',3020,3170,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-08-22 04:44:46','2026-08-22 04:44:46'),('pt_000141','res_courier02',NULL,'pc_demo001','spend',-3020,150,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-24 00:44:46','2026-08-24 00:44:46'),('pt_000142','res_courier03',NULL,'pc_demo001','earn',100,100,'manual',NULL,'账户初始化积分',NULL,NULL,'2026-07-11 20:22:40','2026-07-11 20:22:40'),('pt_000143','res_courier03',NULL,'pc_demo001','earn',2919,3019,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-07-13 10:22:40','2026-07-13 10:22:40'),('pt_000144','res_courier03',NULL,'pc_demo001','spend',-2919,100,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-14 02:22:40','2026-07-14 02:22:40'),('pt_000145','res_courier03',NULL,'pc_demo001','earn',2919,3019,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-07-14 12:22:40','2026-07-14 12:22:40'),('pt_000146','res_courier03',NULL,'pc_demo001','spend',-2919,100,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-15 17:22:40','2026-07-15 17:22:40'),('pt_000147','res_courier03',NULL,'pc_demo001','earn',2919,3019,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-07-17 13:22:40','2026-07-17 13:22:40'),('pt_000148','res_courier03',NULL,'pc_demo001','spend',-2919,100,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-18 21:22:40','2026-07-18 21:22:40'),('pt_000149','res_courier03',NULL,'pc_demo001','earn',2919,3019,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-07-21 17:22:40','2026-07-21 17:22:40'),('pt_000150','res_courier03',NULL,'pc_demo001','spend',-2919,100,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-23 01:22:40','2026-07-23 01:22:40'),('pt_000151','res_courier03',NULL,'pc_demo001','earn',2919,3019,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-07-24 01:22:40','2026-07-24 01:22:40'),('pt_000152','res_courier03',NULL,'pc_demo001','spend',-2919,100,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-24 22:22:40','2026-07-24 22:22:40'),('pt_000153','res_courier03',NULL,'pc_demo001','earn',2919,3019,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-07-27 07:22:40','2026-07-27 07:22:40'),('pt_000154','res_courier03',NULL,'pc_demo001','spend',-2919,100,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-29 03:22:40','2026-07-29 03:22:40'),('pt_000155','res_courier03',NULL,'pc_demo001','earn',2919,3019,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-07-31 12:22:40','2026-07-31 12:22:40'),('pt_000156','res_courier03',NULL,'pc_demo001','spend',-2919,100,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-31 18:22:40','2026-07-31 18:22:40'),('pt_000157','res_courier03',NULL,'pc_demo001','earn',2919,3019,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-08-03 16:22:40','2026-08-03 16:22:40'),('pt_000158','res_courier03',NULL,'pc_demo001','spend',-2919,100,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-06 03:22:40','2026-08-06 03:22:40'),('pt_000159','res_courier03',NULL,'pc_demo001','earn',2919,3019,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-08-08 07:22:40','2026-08-08 07:22:40'),('pt_000160','res_courier03',NULL,'pc_demo001','spend',-2919,100,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-09 20:22:40','2026-08-09 20:22:40'),('pt_000161','res_courier03',NULL,'pc_demo001','earn',2919,3019,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-08-12 07:22:40','2026-08-12 07:22:40'),('pt_000162','res_courier03',NULL,'pc_demo001','spend',-2919,100,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-14 20:22:40','2026-08-14 20:22:40'),('pt_000163','res_dev001','fam_dev001','pc_demo001','earn',74984,74984,'manual',NULL,'账户初始化积分',NULL,NULL,'2026-07-03 16:37:21','2026-07-03 16:37:21'),('pt_000164','res_dev001','fam_dev001','pc_demo001','earn',1883,76867,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-07-04 21:37:21','2026-07-04 21:37:21'),('pt_000165','res_dev001','fam_dev001','pc_demo001','spend',-1883,74984,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-04 23:37:21','2026-07-04 23:37:21'),('pt_000166','res_dev001','fam_dev001','pc_demo001','earn',1884,76868,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-07-05 13:37:21','2026-07-05 13:37:21'),('pt_000167','res_dev001','fam_dev001','pc_demo001','spend',-1884,74984,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-08 02:37:21','2026-07-08 02:37:21'),('pt_000168','res_dev001','fam_dev001','pc_demo001','earn',1884,76868,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-07-09 16:37:21','2026-07-09 16:37:21'),('pt_000169','res_dev001','fam_dev001','pc_demo001','spend',-1884,74984,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-12 01:37:21','2026-07-12 01:37:21'),('pt_000170','res_dev001','fam_dev001','pc_demo001','earn',1884,76868,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-07-14 22:37:21','2026-07-14 22:37:21'),('pt_000171','res_dev001','fam_dev001','pc_demo001','spend',-1884,74984,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-15 16:37:21','2026-07-15 16:37:21'),('pt_000172','res_dev002',NULL,'pc_demo001','earn',6933,6933,'manual',NULL,'账户初始化积分',NULL,NULL,'2026-07-28 19:27:09','2026-07-28 19:27:09'),('pt_000173','res_dev002',NULL,'pc_demo001','earn',3682,10615,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-07-29 22:27:09','2026-07-29 22:27:09'),('pt_000174','res_dev002',NULL,'pc_demo001','spend',-3682,6933,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-31 14:27:09','2026-07-31 14:27:09'),('pt_000175','res_dev002',NULL,'pc_demo001','earn',3682,10615,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-08-03 10:27:09','2026-08-03 10:27:09'),('pt_000176','res_dev002',NULL,'pc_demo001','spend',-3682,6933,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-05 17:27:09','2026-08-05 17:27:09'),('pt_000177','res_dev002',NULL,'pc_demo001','earn',3682,10615,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-08-08 13:27:09','2026-08-08 13:27:09'),('pt_000178','res_dev002',NULL,'pc_demo001','spend',-3682,6933,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-11 10:27:09','2026-08-11 10:27:09'),('pt_000179','res_dev002',NULL,'pc_demo001','earn',3682,10615,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-08-14 09:27:09','2026-08-14 09:27:09'),('pt_000180','res_dev002',NULL,'pc_demo001','spend',-3682,6933,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-15 10:27:09','2026-08-15 10:27:09'),('pt_000181','res_dev002',NULL,'pc_demo001','earn',3682,10615,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-08-17 10:27:09','2026-08-17 10:27:09'),('pt_000182','res_dev002',NULL,'pc_demo001','spend',-3682,6933,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-19 14:27:09','2026-08-19 14:27:09'),('pt_000183','res_dev002',NULL,'pc_demo001','earn',3683,10616,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-08-19 18:27:09','2026-08-19 18:27:09'),('pt_000184','res_dev002',NULL,'pc_demo001','spend',-3683,6933,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-22 03:27:09','2026-08-22 03:27:09'),('pt_000185','res_dev002',NULL,'pc_demo001','earn',3683,10616,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-08-23 15:27:09','2026-08-23 15:27:09'),('pt_000186','res_dev002',NULL,'pc_demo001','spend',-3683,6933,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-24 17:27:09','2026-08-24 17:27:09'),('pt_000187','res_dev002',NULL,'pc_demo001','earn',3683,10616,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-08-26 23:27:09','2026-08-26 23:27:09'),('pt_000188','res_dev002',NULL,'pc_demo001','spend',-3683,6933,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-27 12:27:09','2026-08-27 12:27:09'),('pt_000189','res_dev002',NULL,'pc_demo001','earn',3683,10616,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-08-27 23:27:09','2026-08-27 23:27:09'),('pt_000190','res_dev002',NULL,'pc_demo001','spend',-3683,6933,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-29 09:27:09','2026-08-29 09:27:09'),('pt_000191','res_dev002',NULL,'pc_demo001','earn',3683,10616,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-08-30 21:27:09','2026-08-30 21:27:09'),('pt_000192','res_dev002',NULL,'pc_demo001','spend',-3683,6933,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-30 22:27:09','2026-08-30 22:27:09'),('pt_000193','res_dev003',NULL,'pc_demo001','earn',3188,3188,'manual',NULL,'账户初始化积分',NULL,NULL,'2026-07-25 04:34:58','2026-07-25 04:34:58'),('pt_000194','res_dev003',NULL,'pc_demo001','earn',5724,8912,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-07-26 16:34:58','2026-07-26 16:34:58'),('pt_000195','res_dev003',NULL,'pc_demo001','spend',-5724,3188,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-27 19:34:58','2026-07-27 19:34:58'),('pt_000196','res_dev003',NULL,'pc_demo001','earn',5724,8912,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-07-28 04:34:58','2026-07-28 04:34:58'),('pt_000197','res_dev003',NULL,'pc_demo001','spend',-5724,3188,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-30 18:34:58','2026-07-30 18:34:58'),('pt_000198','res_dev003',NULL,'pc_demo001','earn',5724,8912,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-08-01 16:34:58','2026-08-01 16:34:58'),('pt_000199','res_dev003',NULL,'pc_demo001','spend',-5724,3188,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-04 14:34:58','2026-08-04 14:34:58'),('pt_000200','res_dev003',NULL,'pc_demo001','earn',5724,8912,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-08-07 00:34:58','2026-08-07 00:34:58'),('pt_000201','res_dev003',NULL,'pc_demo001','spend',-5724,3188,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-08 08:34:58','2026-08-08 08:34:58'),('pt_000202','res_dev003',NULL,'pc_demo001','earn',5724,8912,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-08-09 00:34:58','2026-08-09 00:34:58'),('pt_000203','res_dev003',NULL,'pc_demo001','spend',-5724,3188,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-10 02:34:58','2026-08-10 02:34:58'),('pt_000204','res_dev003',NULL,'pc_demo001','earn',5724,8912,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-08-10 14:34:58','2026-08-10 14:34:58'),('pt_000205','res_dev003',NULL,'pc_demo001','spend',-5724,3188,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-12 14:34:58','2026-08-12 14:34:58'),('pt_000206','res_dev003',NULL,'pc_demo001','earn',5724,8912,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-08-14 01:34:58','2026-08-14 01:34:58'),('pt_000207','res_dev003',NULL,'pc_demo001','spend',-5724,3188,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-16 08:34:58','2026-08-16 08:34:58'),('pt_000208','res_dev003',NULL,'pc_demo001','earn',5724,8912,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-08-18 11:34:58','2026-08-18 11:34:58'),('pt_000209','res_dev003',NULL,'pc_demo001','spend',-5724,3188,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-21 07:34:58','2026-08-21 07:34:58'),('pt_000210','res_dev003',NULL,'pc_demo001','earn',5724,8912,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-08-23 07:34:58','2026-08-23 07:34:58'),('pt_000211','res_dev003',NULL,'pc_demo001','spend',-5724,3188,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-24 17:34:58','2026-08-24 17:34:58'),('pt_000212','res_dev003',NULL,'pc_demo001','earn',5724,8912,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-08-27 00:34:58','2026-08-27 00:34:58'),('pt_000213','res_dev003',NULL,'pc_demo001','spend',-5724,3188,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-27 23:34:58','2026-08-27 23:34:58'),('pt_000214','res_dev004',NULL,'pc_demo001','earn',524,524,'manual',NULL,'账户初始化积分',NULL,NULL,'2026-07-29 23:10:54','2026-07-29 23:10:54'),('pt_000215','res_dev004',NULL,'pc_demo001','earn',1769,2293,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-08-01 15:10:54','2026-08-01 15:10:54'),('pt_000216','res_dev004',NULL,'pc_demo001','spend',-1769,524,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-02 22:10:54','2026-08-02 22:10:54'),('pt_000217','res_dev004',NULL,'pc_demo001','earn',1769,2293,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-08-03 09:10:54','2026-08-03 09:10:54'),('pt_000218','res_dev004',NULL,'pc_demo001','spend',-1769,524,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-04 11:10:54','2026-08-04 11:10:54'),('pt_000219','res_dev004',NULL,'pc_demo001','earn',1769,2293,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-08-06 00:10:54','2026-08-06 00:10:54'),('pt_000220','res_dev004',NULL,'pc_demo001','spend',-1769,524,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-08 14:10:54','2026-08-08 14:10:54'),('pt_000221','res_dev004',NULL,'pc_demo001','earn',1769,2293,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-08-10 18:10:54','2026-08-10 18:10:54'),('pt_000222','res_dev004',NULL,'pc_demo001','spend',-1769,524,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-11 12:10:54','2026-08-11 12:10:54'),('pt_000223','res_dev004',NULL,'pc_demo001','earn',1769,2293,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-08-11 21:10:54','2026-08-11 21:10:54'),('pt_000224','res_dev004',NULL,'pc_demo001','spend',-1769,524,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-12 11:10:54','2026-08-12 11:10:54'),('pt_000225','res_dev004',NULL,'pc_demo001','earn',1770,2294,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-08-14 02:10:54','2026-08-14 02:10:54'),('pt_000226','res_dev004',NULL,'pc_demo001','spend',-1770,524,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-16 01:10:54','2026-08-16 01:10:54'),('pt_000227','res_dev004',NULL,'pc_demo001','earn',1770,2294,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-08-16 02:10:54','2026-08-16 02:10:54'),('pt_000228','res_dev004',NULL,'pc_demo001','spend',-1770,524,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-19 02:10:54','2026-08-19 02:10:54'),('pt_000229','res_dev004',NULL,'pc_demo001','earn',1770,2294,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-08-20 08:10:54','2026-08-20 08:10:54'),('pt_000230','res_dev004',NULL,'pc_demo001','spend',-1770,524,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-22 15:10:54','2026-08-22 15:10:54'),('pt_000231','res_ind01',NULL,'pc_demo001','earn',500,500,'manual',NULL,'账户初始化积分',NULL,NULL,'2026-07-30 02:34:58','2026-07-30 02:34:58'),('pt_000232','res_ind01',NULL,'pc_demo001','earn',2208,2708,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-07-30 18:34:58','2026-07-30 18:34:58'),('pt_000233','res_ind01',NULL,'pc_demo001','spend',-2208,500,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-31 10:34:58','2026-07-31 10:34:58'),('pt_000234','res_ind01',NULL,'pc_demo001','earn',2208,2708,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-07-31 13:34:58','2026-07-31 13:34:58'),('pt_000235','res_ind01',NULL,'pc_demo001','spend',-2208,500,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-03 06:34:58','2026-08-03 06:34:58'),('pt_000236','res_ind01',NULL,'pc_demo001','earn',2208,2708,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-08-05 19:34:58','2026-08-05 19:34:58'),('pt_000237','res_ind01',NULL,'pc_demo001','spend',-2208,500,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-08 09:34:58','2026-08-08 09:34:58'),('pt_000238','res_ind01',NULL,'pc_demo001','earn',2208,2708,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-08-08 17:34:58','2026-08-08 17:34:58'),('pt_000239','res_ind01',NULL,'pc_demo001','spend',-2208,500,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-09 07:34:58','2026-08-09 07:34:58'),('pt_000240','res_ind01',NULL,'pc_demo001','earn',2208,2708,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-08-11 05:34:58','2026-08-11 05:34:58'),('pt_000241','res_ind01',NULL,'pc_demo001','spend',-2208,500,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-13 00:34:58','2026-08-13 00:34:58'),('pt_000242','res_ind01',NULL,'pc_demo001','earn',2208,2708,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-08-14 05:34:58','2026-08-14 05:34:58'),('pt_000243','res_ind01',NULL,'pc_demo001','spend',-2208,500,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-17 02:34:58','2026-08-17 02:34:58'),('pt_000244','res_ind01',NULL,'pc_demo001','earn',2208,2708,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-08-17 05:34:58','2026-08-17 05:34:58'),('pt_000245','res_ind01',NULL,'pc_demo001','spend',-2208,500,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-19 12:34:58','2026-08-19 12:34:58'),('pt_000246','res_ind01',NULL,'pc_demo001','earn',2208,2708,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-08-20 22:34:58','2026-08-20 22:34:58'),('pt_000247','res_ind01',NULL,'pc_demo001','spend',-2208,500,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-21 01:34:58','2026-08-21 01:34:58'),('pt_000248','res_ind01',NULL,'pc_demo001','earn',2208,2708,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-08-22 18:34:58','2026-08-22 18:34:58'),('pt_000249','res_ind01',NULL,'pc_demo001','spend',-2208,500,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-24 15:34:58','2026-08-24 15:34:58'),('pt_000250','res_ind01',NULL,'pc_demo001','earn',2208,2708,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-08-26 08:34:58','2026-08-26 08:34:58'),('pt_000251','res_ind01',NULL,'pc_demo001','spend',-2208,500,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-28 08:34:58','2026-08-28 08:34:58'),('pt_000252','res_mch01',NULL,'pc_demo001','earn',0,0,'manual',NULL,'账户初始化积分',NULL,NULL,'2026-07-12 07:45:54','2026-07-12 07:45:54'),('pt_000253','res_mch01',NULL,'pc_demo001','earn',9496,9496,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-07-14 13:45:54','2026-07-14 13:45:54'),('pt_000254','res_mch01',NULL,'pc_demo001','spend',-9496,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-15 02:45:54','2026-07-15 02:45:54'),('pt_000255','res_mch01',NULL,'pc_demo001','earn',9496,9496,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-07-17 10:45:54','2026-07-17 10:45:54'),('pt_000256','res_mch01',NULL,'pc_demo001','spend',-9496,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-17 15:45:54','2026-07-17 15:45:54'),('pt_000257','res_mch01',NULL,'pc_demo001','earn',9496,9496,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-07-17 23:45:54','2026-07-17 23:45:54'),('pt_000258','res_mch01',NULL,'pc_demo001','spend',-9496,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-19 14:45:54','2026-07-19 14:45:54'),('pt_000259','res_mch01',NULL,'pc_demo001','earn',9496,9496,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-07-21 12:45:54','2026-07-21 12:45:54'),('pt_000260','res_mch01',NULL,'pc_demo001','spend',-9496,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-22 08:45:54','2026-07-22 08:45:54'),('pt_000261','res_mch01',NULL,'pc_demo001','earn',9496,9496,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-07-24 02:45:54','2026-07-24 02:45:54'),('pt_000262','res_mch01',NULL,'pc_demo001','spend',-9496,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-25 13:45:54','2026-07-25 13:45:54'),('pt_000263','res_mch01',NULL,'pc_demo001','earn',9496,9496,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-07-27 16:45:54','2026-07-27 16:45:54'),('pt_000264','res_mch01',NULL,'pc_demo001','spend',-9496,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-28 05:45:54','2026-07-28 05:45:54'),('pt_000265','res_mch01',NULL,'pc_demo001','earn',9496,9496,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-07-29 03:45:54','2026-07-29 03:45:54'),('pt_000266','res_mch01',NULL,'pc_demo001','spend',-9496,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-01 03:45:54','2026-08-01 03:45:54'),('pt_000267','res_mch01',NULL,'pc_demo001','earn',9496,9496,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-08-02 19:45:54','2026-08-02 19:45:54'),('pt_000268','res_mch01',NULL,'pc_demo001','spend',-9496,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-03 16:45:54','2026-08-03 16:45:54'),('pt_000269','res_mch01',NULL,'pc_demo001','earn',9496,9496,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-08-04 02:45:54','2026-08-04 02:45:54'),('pt_000270','res_mch01',NULL,'pc_demo001','spend',-9496,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-05 10:45:54','2026-08-05 10:45:54'),('pt_000271','res_mch01',NULL,'pc_demo001','earn',9496,9496,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-08-07 00:45:54','2026-08-07 00:45:54'),('pt_000272','res_mch01',NULL,'pc_demo001','spend',-9496,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-09 11:45:54','2026-08-09 11:45:54'),('pt_000273','res_mch02',NULL,'pc_demo001','earn',0,0,'manual',NULL,'账户初始化积分',NULL,NULL,'2026-07-10 19:21:21','2026-07-10 19:21:21'),('pt_000274','res_mch02',NULL,'pc_demo001','earn',8784,8784,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-07-10 22:21:21','2026-07-10 22:21:21'),('pt_000275','res_mch02',NULL,'pc_demo001','spend',-8784,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-12 18:21:21','2026-07-12 18:21:21'),('pt_000276','res_mch02',NULL,'pc_demo001','earn',8784,8784,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-07-13 02:21:21','2026-07-13 02:21:21'),('pt_000277','res_mch02',NULL,'pc_demo001','spend',-8784,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-15 18:21:21','2026-07-15 18:21:21'),('pt_000278','res_mch02',NULL,'pc_demo001','earn',8784,8784,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-07-18 16:21:21','2026-07-18 16:21:21'),('pt_000279','res_mch02',NULL,'pc_demo001','spend',-8784,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-19 18:21:21','2026-07-19 18:21:21'),('pt_000280','res_mch02',NULL,'pc_demo001','earn',8784,8784,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-07-19 21:21:21','2026-07-19 21:21:21'),('pt_000281','res_mch02',NULL,'pc_demo001','spend',-8784,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-22 06:21:21','2026-07-22 06:21:21'),('pt_000282','res_mch02',NULL,'pc_demo001','earn',8784,8784,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-07-23 13:21:21','2026-07-23 13:21:21'),('pt_000283','res_mch02',NULL,'pc_demo001','spend',-8784,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-26 11:21:21','2026-07-26 11:21:21'),('pt_000284','res_mch02',NULL,'pc_demo001','earn',8784,8784,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-07-28 06:21:21','2026-07-28 06:21:21'),('pt_000285','res_mch02',NULL,'pc_demo001','spend',-8784,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-28 13:21:21','2026-07-28 13:21:21'),('pt_000286','res_mch02',NULL,'pc_demo001','earn',8784,8784,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-07-29 10:21:21','2026-07-29 10:21:21'),('pt_000287','res_mch02',NULL,'pc_demo001','spend',-8784,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-31 11:21:21','2026-07-31 11:21:21'),('pt_000288','res_mch02',NULL,'pc_demo001','earn',8784,8784,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-08-03 10:21:21','2026-08-03 10:21:21'),('pt_000289','res_mch02',NULL,'pc_demo001','spend',-8784,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-05 10:21:21','2026-08-05 10:21:21'),('pt_000290','res_mch02',NULL,'pc_demo001','earn',8784,8784,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-08-05 14:21:21','2026-08-05 14:21:21'),('pt_000291','res_mch02',NULL,'pc_demo001','spend',-8784,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-06 14:21:21','2026-08-06 14:21:21'),('pt_000292','res_mch02',NULL,'pc_demo001','earn',8784,8784,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-08-08 01:21:21','2026-08-08 01:21:21'),('pt_000293','res_mch02',NULL,'pc_demo001','spend',-8784,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-09 06:21:21','2026-08-09 06:21:21'),('pt_000294','res_mch03',NULL,'pc_demo001','earn',0,0,'manual',NULL,'账户初始化积分',NULL,NULL,'2026-07-13 12:19:33','2026-07-13 12:19:33'),('pt_000295','res_mch03',NULL,'pc_demo001','earn',3696,3696,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-07-15 12:19:33','2026-07-15 12:19:33'),('pt_000296','res_mch03',NULL,'pc_demo001','spend',-3696,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-17 07:19:33','2026-07-17 07:19:33'),('pt_000297','res_mch03',NULL,'pc_demo001','earn',3696,3696,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-07-18 05:19:33','2026-07-18 05:19:33'),('pt_000298','res_mch03',NULL,'pc_demo001','spend',-3696,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-18 14:19:33','2026-07-18 14:19:33'),('pt_000299','res_mch03',NULL,'pc_demo001','earn',3696,3696,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-07-19 14:19:33','2026-07-19 14:19:33'),('pt_000300','res_mch03',NULL,'pc_demo001','spend',-3696,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-22 05:19:33','2026-07-22 05:19:33'),('pt_000301','res_mch03',NULL,'pc_demo001','earn',3696,3696,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-07-23 14:19:33','2026-07-23 14:19:33'),('pt_000302','res_mch03',NULL,'pc_demo001','spend',-3696,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-23 23:19:33','2026-07-23 23:19:33'),('pt_000303','res_mch03',NULL,'pc_demo001','earn',3696,3696,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-07-25 10:19:33','2026-07-25 10:19:33'),('pt_000304','res_mch03',NULL,'pc_demo001','spend',-3696,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-26 02:19:33','2026-07-26 02:19:33'),('pt_000305','res_mch03',NULL,'pc_demo001','earn',3697,3697,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-07-26 22:19:33','2026-07-26 22:19:33'),('pt_000306','res_mch03',NULL,'pc_demo001','spend',-3697,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-27 18:19:33','2026-07-27 18:19:33'),('pt_000307','res_mch03',NULL,'pc_demo001','earn',3697,3697,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-07-29 17:19:33','2026-07-29 17:19:33'),('pt_000308','res_mch03',NULL,'pc_demo001','spend',-3697,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-30 17:19:33','2026-07-30 17:19:33'),('pt_000309','res_mch03',NULL,'pc_demo001','earn',3697,3697,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-07-31 05:19:33','2026-07-31 05:19:33'),('pt_000310','res_mch03',NULL,'pc_demo001','spend',-3697,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-02 07:19:33','2026-08-02 07:19:33'),('pt_000311','res_mch03',NULL,'pc_demo001','earn',3697,3697,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-08-03 10:19:33','2026-08-03 10:19:33'),('pt_000312','res_mch03',NULL,'pc_demo001','spend',-3697,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-05 21:19:33','2026-08-05 21:19:33'),('pt_000313','res_mch03',NULL,'pc_demo001','earn',3697,3697,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-08-07 11:19:33','2026-08-07 11:19:33'),('pt_000314','res_mch03',NULL,'pc_demo001','spend',-3697,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-10 09:19:33','2026-08-10 09:19:33'),('pt_000315','res_mch04',NULL,'pc_demo001','earn',0,0,'manual',NULL,'账户初始化积分',NULL,NULL,'2026-07-19 19:18:21','2026-07-19 19:18:21'),('pt_000316','res_mch04',NULL,'pc_demo001','earn',3603,3603,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-07-20 14:18:21','2026-07-20 14:18:21'),('pt_000317','res_mch04',NULL,'pc_demo001','spend',-3603,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-20 18:18:21','2026-07-20 18:18:21'),('pt_000318','res_mch04',NULL,'pc_demo001','earn',3603,3603,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-07-23 11:18:21','2026-07-23 11:18:21'),('pt_000319','res_mch04',NULL,'pc_demo001','spend',-3603,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-26 00:18:21','2026-07-26 00:18:21'),('pt_000320','res_mch04',NULL,'pc_demo001','earn',3603,3603,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-07-27 08:18:21','2026-07-27 08:18:21'),('pt_000321','res_mch04',NULL,'pc_demo001','spend',-3603,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-29 15:18:21','2026-07-29 15:18:21'),('pt_000322','res_mch04',NULL,'pc_demo001','earn',3603,3603,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-08-01 02:18:21','2026-08-01 02:18:21'),('pt_000323','res_mch04',NULL,'pc_demo001','spend',-3603,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-01 23:18:21','2026-08-01 23:18:21'),('pt_000324','res_mch04',NULL,'pc_demo001','earn',3603,3603,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-08-04 03:18:21','2026-08-04 03:18:21'),('pt_000325','res_mch04',NULL,'pc_demo001','spend',-3603,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-05 17:18:21','2026-08-05 17:18:21'),('pt_000326','res_mch04',NULL,'pc_demo001','earn',3604,3604,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-08-06 07:18:21','2026-08-06 07:18:21'),('pt_000327','res_mch04',NULL,'pc_demo001','spend',-3604,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-09 02:18:21','2026-08-09 02:18:21'),('pt_000328','res_mch04',NULL,'pc_demo001','earn',3604,3604,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-08-10 13:18:21','2026-08-10 13:18:21'),('pt_000329','res_mch04',NULL,'pc_demo001','spend',-3604,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-11 22:18:21','2026-08-11 22:18:21'),('pt_000330','res_mch04',NULL,'pc_demo001','earn',3604,3604,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-08-13 03:18:21','2026-08-13 03:18:21'),('pt_000331','res_mch04',NULL,'pc_demo001','spend',-3604,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-15 13:18:21','2026-08-15 13:18:21'),('pt_000332','res_mch04',NULL,'pc_demo001','earn',3604,3604,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-08-17 20:18:21','2026-08-17 20:18:21'),('pt_000333','res_mch04',NULL,'pc_demo001','spend',-3604,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-18 14:18:21','2026-08-18 14:18:21'),('pt_000334','res_mch04',NULL,'pc_demo001','earn',3604,3604,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-08-18 17:18:21','2026-08-18 17:18:21'),('pt_000335','res_mch04',NULL,'pc_demo001','spend',-3604,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-21 02:18:21','2026-08-21 02:18:21'),('pt_000336','res_propadmin01',NULL,'pc_demo001','earn',0,0,'manual',NULL,'账户初始化积分',NULL,NULL,'2026-07-22 06:56:55','2026-07-22 06:56:55'),('pt_000337','res_propadmin01',NULL,'pc_demo001','earn',1891,1891,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-07-23 05:56:55','2026-07-23 05:56:55'),('pt_000338','res_propadmin01',NULL,'pc_demo001','spend',-1891,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-25 13:56:55','2026-07-25 13:56:55'),('pt_000339','res_propadmin01',NULL,'pc_demo001','earn',1891,1891,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-07-27 20:56:55','2026-07-27 20:56:55'),('pt_000340','res_propadmin01',NULL,'pc_demo001','spend',-1891,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-29 12:56:55','2026-07-29 12:56:55'),('pt_000341','res_propadmin01',NULL,'pc_demo001','earn',1892,1892,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-07-30 19:56:55','2026-07-30 19:56:55'),('pt_000342','res_propadmin01',NULL,'pc_demo001','spend',-1892,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-01 19:56:55','2026-08-01 19:56:55'),('pt_000343','res_propadmin01',NULL,'pc_demo001','earn',1892,1892,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-08-03 07:56:55','2026-08-03 07:56:55'),('pt_000344','res_propadmin01',NULL,'pc_demo001','spend',-1892,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-04 03:56:55','2026-08-04 03:56:55'),('pt_000345','res_propadmin01',NULL,'pc_demo001','earn',1892,1892,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-08-04 10:56:55','2026-08-04 10:56:55'),('pt_000346','res_propadmin01',NULL,'pc_demo001','spend',-1892,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-05 04:56:55','2026-08-05 04:56:55'),('pt_000347','res_propadmin01',NULL,'pc_demo001','earn',1892,1892,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-08-05 12:56:55','2026-08-05 12:56:55'),('pt_000348','res_propadmin01',NULL,'pc_demo001','spend',-1892,0,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-06 17:56:55','2026-08-06 17:56:55'),('pt_000349','res_sec01',NULL,'pc_demo001','earn',800,800,'manual',NULL,'账户初始化积分',NULL,NULL,'2026-07-21 02:50:07','2026-07-21 02:50:07'),('pt_000350','res_sec01',NULL,'pc_demo001','earn',4655,5455,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-07-21 14:50:07','2026-07-21 14:50:07'),('pt_000351','res_sec01',NULL,'pc_demo001','spend',-4655,800,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-23 12:50:07','2026-07-23 12:50:07'),('pt_000352','res_sec01',NULL,'pc_demo001','earn',4655,5455,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-07-26 09:50:07','2026-07-26 09:50:07'),('pt_000353','res_sec01',NULL,'pc_demo001','spend',-4655,800,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-07-29 09:50:07','2026-07-29 09:50:07'),('pt_000354','res_sec01',NULL,'pc_demo001','earn',4655,5455,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-08-01 08:50:07','2026-08-01 08:50:07'),('pt_000355','res_sec01',NULL,'pc_demo001','spend',-4655,800,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-02 23:50:07','2026-08-02 23:50:07'),('pt_000356','res_sec01',NULL,'pc_demo001','earn',4655,5455,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-08-05 18:50:07','2026-08-05 18:50:07'),('pt_000357','res_sec01',NULL,'pc_demo001','spend',-4655,800,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-06 09:50:07','2026-08-06 09:50:07'),('pt_000358','res_sec01',NULL,'pc_demo001','earn',4655,5455,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-08-06 16:50:07','2026-08-06 16:50:07'),('pt_000359','res_sec01',NULL,'pc_demo001','spend',-4655,800,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-08 21:50:07','2026-08-08 21:50:07'),('pt_000360','res_sec01',NULL,'pc_demo001','earn',4655,5455,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-08-09 17:50:07','2026-08-09 17:50:07'),('pt_000361','res_sec01',NULL,'pc_demo001','spend',-4655,800,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-10 08:50:07','2026-08-10 08:50:07'),('pt_000362','res_sec01',NULL,'pc_demo001','earn',4655,5455,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-08-10 21:50:07','2026-08-10 21:50:07'),('pt_000363','res_sec01',NULL,'pc_demo001','spend',-4655,800,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-13 01:50:07','2026-08-13 01:50:07'),('pt_000364','res_sec01',NULL,'pc_demo001','earn',4655,5455,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-08-15 10:50:07','2026-08-15 10:50:07'),('pt_000365','res_sec01',NULL,'pc_demo001','spend',-4655,800,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-17 16:50:07','2026-08-17 16:50:07'),('pt_000366','res_sec01',NULL,'pc_demo001','earn',4655,5455,'pool_diff',NULL,'家庭积分池转入',NULL,NULL,'2026-08-17 22:50:07','2026-08-17 22:50:07'),('pt_000367','res_sec01',NULL,'pc_demo001','spend',-4655,800,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-18 14:50:07','2026-08-18 14:50:07'),('pt_000368','res_sec01',NULL,'pc_demo001','earn',4655,5455,'activity',NULL,'参与社区活动奖励',NULL,NULL,'2026-08-19 09:50:07','2026-08-19 09:50:07'),('pt_000369','res_sec01',NULL,'pc_demo001','spend',-4655,800,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-21 22:50:07','2026-08-21 22:50:07'),('pt_000370','res_tech01',NULL,'pc_demo001','earn',300,300,'manual',NULL,'账户初始化积分',NULL,NULL,'2026-08-08 13:56:30','2026-08-08 13:56:30'),('pt_000371','res_tech01',NULL,'pc_demo001','earn',1895,2195,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-08-10 03:56:30','2026-08-10 03:56:30'),('pt_000372','res_tech01',NULL,'pc_demo001','spend',-1895,300,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-10 15:56:30','2026-08-10 15:56:30'),('pt_000373','res_tech01',NULL,'pc_demo001','earn',1896,2196,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-08-10 17:56:30','2026-08-10 17:56:30'),('pt_000374','res_tech01',NULL,'pc_demo001','spend',-1896,300,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-11 14:56:30','2026-08-11 14:56:30'),('pt_000375','res_tech01',NULL,'pc_demo001','earn',1896,2196,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-08-12 06:56:30','2026-08-12 06:56:30'),('pt_000376','res_tech01',NULL,'pc_demo001','spend',-1896,300,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-12 07:56:30','2026-08-12 07:56:30'),('pt_000377','res_tech01',NULL,'pc_demo001','earn',1896,2196,'manual',NULL,'物业发放奖励',NULL,NULL,'2026-08-13 18:56:30','2026-08-13 18:56:30'),('pt_000378','res_tech01',NULL,'pc_demo001','spend',-1896,300,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-16 01:56:30','2026-08-16 01:56:30'),('pt_000379','res_tech01',NULL,'pc_demo001','earn',1896,2196,'sign_in',NULL,'每日签到奖励',NULL,NULL,'2026-08-16 17:56:30','2026-08-16 17:56:30'),('pt_000380','res_tech01',NULL,'pc_demo001','spend',-1896,300,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-19 15:56:30','2026-08-19 15:56:30'),('pt_000381','res_tech01',NULL,'pc_demo001','earn',1896,2196,'merchant_grant',NULL,'商家消费返积分',NULL,NULL,'2026-08-20 00:56:30','2026-08-20 00:56:30'),('pt_000382','res_tech01',NULL,'pc_demo001','spend',-1896,300,'order',NULL,'积分商城兑换消费',NULL,NULL,'2026-08-20 22:56:30','2026-08-20 22:56:30'),('pt_351c88e1481000','res_dev001','fam_dev001','pc_demo001','earn',1600,76584,'order','ord_351c88bee41000','订单消费返积分',NULL,NULL,'2026-08-13 02:13:20','2026-08-13 02:13:20'),('pt_351e9cc18e1000','res_dev001','fam_dev001','pc_demo001','spend',-4000,76584,'order','ord_351e9bf41e1000','订单积分支付',NULL,NULL,'2026-08-13 07:03:48','2026-08-13 07:03:48'),('pt_352164b65a1000','res_dev001','fam_dev001','pc_demo001','earn',40,76624,'order','ord_352164559e1000','订单消费返积分',NULL,NULL,'2026-08-13 13:32:38','2026-08-13 13:32:38'),('pt_35217834b41000','res_35217712f21000',NULL,'pc_demo001','earn',1,1,'manual','pcr_demo_birthday','信息采集奖励: birthday','res_35217712f21000',NULL,'2026-08-13 13:43:16','2026-08-13 13:43:16'),('pt_35266db2dc1000','res_35217712f21000','fam_35217f046c1000','pc_demo001','earn',40,40,'order','ord_35266bd4c41000','订单消费返积分',NULL,NULL,'2026-08-14 01:16:35','2026-08-14 01:16:35'),('pt_35267c5c641000','res_35217712f21000','fam_35217f046c1000','pc_demo001','earn',40,80,'order','ord_35267afe501000','订单消费返积分',NULL,NULL,'2026-08-14 01:24:36','2026-08-14 01:24:36'),('pt_35267d4a4e1000','res_35217712f21000','fam_35217f046c1000','pc_demo001','spend',-40,80,'order','ord_35267afe501000','订单取消扣回返积分',NULL,NULL,'2026-08-14 01:25:06','2026-08-14 01:25:06'),('pt_35268cd91c1000','res_35217712f21000','fam_35217f046c1000','pc_demo001','earn',220,300,'order','ord_35268adb481000','订单消费返积分',NULL,NULL,'2026-08-14 01:33:36','2026-08-14 01:33:36'),('pt_3526aebbec1000','res_35217712f21000','fam_35217f046c1000','pc_demo001','earn',40,340,'order','ord_3526ad6bfc1000','订单消费返积分',NULL,NULL,'2026-08-14 01:52:06','2026-08-14 01:52:06'),('pt_3526f494a61000','res_dev001','fam_dev001','pc_demo001','earn',4000,80624,'order','ord_3526f46ef21000','订单消费返积分',NULL,NULL,'2026-08-14 02:30:15','2026-08-14 02:30:15'),('pt_352869c8c41000','res_dev001','fam_dev001','pc_demo001','earn',1420,82044,'order','ord_35286996141000','订单消费返积分',NULL,NULL,'2026-08-14 05:54:04','2026-08-14 05:54:04'),('pt_35288b08a81000','res_dev001','fam_dev001','pc_demo001','earn',1420,83464,'order','ord_35288abea81000','订单消费返积分',NULL,NULL,'2026-08-14 06:12:14','2026-08-14 06:12:14'),('pt_354898fe141000','res_dev001','fam_dev001','pc_demo001','earn',1,83465,'manual','pcr_demo_birthday','信息采集奖励: birthday','res_dev001',NULL,'2026-08-17 08:53:46','2026-08-17 08:53:46'),('pt_354fc8ecb01000','res_dev001','fam_dev001','pc_demo001','earn',1420,84885,'order','ord_354fc89cc21000','订单消费返积分',NULL,NULL,'2026-08-18 01:38:37','2026-08-18 01:38:37'),('pt_354fe932b61000','res_dev001','fam_dev001','pc_demo001','spend',-1,84884,'transfer_to_property',NULL,'转给物业',NULL,NULL,'2026-08-18 01:56:15','2026-08-18 01:56:15'),('pt_35513fd2321000','res_dev001','fam_dev001','pc_demo001','earn',1440,86324,'order','ord_35513f81481000','订单消费返积分',NULL,NULL,'2026-08-18 05:03:22','2026-08-18 05:03:22'),('pt_35522113041000','res_dev001','fam_dev001','pc_demo001','earn',1440,87764,'order','ord_355220b31e1000','订单消费返积分',NULL,NULL,'2026-08-18 07:06:23','2026-08-18 07:06:23'),('pt_355222475e1000','res_dev001','fam_dev001','pc_demo001','earn',1440,89204,'order','ord_3552221f381000','订单消费返积分',NULL,NULL,'2026-08-18 07:07:02','2026-08-18 07:07:02'),('pt_35651ff1ba1000','res_dev001','fam_dev001','pc_demo001','spend',-89204,0,'transfer_to_property',NULL,'转给物业',NULL,NULL,'2026-08-20 03:22:10','2026-08-20 03:22:10'),('pt_35716175b61000','res_35217712f21000','fam_35217f046c1000','pc_demo001','earn',1,341,'manual','pcr_demo_phone','信息采集奖励: phone','res_35217712f21000',NULL,'2026-08-21 07:55:40','2026-08-21 07:55:40'),('pt_357997f4201000','res_dev001','fam_dev001','pc_demo001','earn',38,38,'order','ord_357997afda1000','订单消费返积分',NULL,NULL,'2026-08-22 03:03:54','2026-08-22 03:03:54'),('pt_3579ae189c1000','res_dev001','fam_dev001','pc_demo001','earn',38,76,'order','ord_3579ade7821000','订单消费返积分',NULL,NULL,'2026-08-22 03:16:00','2026-08-22 03:16:00'),('pt_357aa1f8721000','res_dev001','fam_dev001','pc_demo001','spend',-38,76,'order','ord_3579ade7821000','订单取消扣回返积分',NULL,NULL,'2026-08-22 05:29:11','2026-08-22 05:29:11'),('pt_357b488e181000','res_dev001','fam_dev001','pc_demo001','spend',-76,0,'transfer_to_property',NULL,'转给物业',NULL,NULL,'2026-08-22 07:00:10','2026-08-22 07:00:10'),('pt_358394437c1000','res_dev001','fam_dev001','pc_demo001','earn',4000,4000,'order','ord_35839426361000','订单消费返积分',NULL,NULL,'2026-08-23 02:19:59','2026-08-23 02:19:59'),('pt_3585317b0e1000','res_dev001','fam_dev001','pc_demo001','earn',18,4018,'order','ord_3585315b4c1000','订单消费返积分',NULL,NULL,'2026-08-23 06:05:40','2026-08-23 06:05:40'),('pt_358531c2fa1000','res_dev001','fam_dev001','pc_demo001','earn',4000,8018,'order','ord_358531aa921000','订单消费返积分',NULL,NULL,'2026-08-23 06:05:49','2026-08-23 06:05:49'),('pt_358533be6c1000','res_dev001','fam_dev001','pc_demo001','earn',4000,12018,'order','ord_358533a5ce1000','订单消费返积分',NULL,NULL,'2026-08-23 06:06:54','2026-08-23 06:06:54'),('pt_35854062721000','res_dev001','fam_dev001','pc_demo001','earn',4000,16018,'order','ord_35854036621000','订单消费返积分',NULL,NULL,'2026-08-23 06:13:48','2026-08-23 06:13:48'),('pt_358609da1e1000','res_dev001','fam_dev001','pc_demo001','earn',4000,20018,'order','ord_358609a9a81000','订单消费返积分',NULL,NULL,'2026-08-23 08:03:50','2026-08-23 08:03:50'),('pt_35862909e61000','res_dev001','fam_dev001','pc_demo001','earn',5000,25018,'order','ord_358628e0fa1000','订单消费返积分',NULL,NULL,'2026-08-23 08:20:52','2026-08-23 08:20:52'),('pt_3586308cf41000','res_dev001','fam_dev001','pc_demo001','earn',1420,26438,'order','ord_358630648a1000','订单消费返积分',NULL,NULL,'2026-08-23 08:24:58','2026-08-23 08:24:58'),('pt_35863471341000','res_dev001','fam_dev001','pc_demo001','earn',18,26456,'order','ord_35863441be1000','订单消费返积分',NULL,NULL,'2026-08-23 08:27:05','2026-08-23 08:27:05'),('pt_3597c9c9741000','res_dev001','fam_dev001','pc_demo001','spend',-26456,0,'transfer_to_property',NULL,'转给物业',NULL,NULL,'2026-08-25 01:25:25','2026-08-25 01:25:25'),('pt_35ce6b1b541000','res_dev001','fam_dev001','pc_demo001','earn',18,18,'order','ord_35ce6aeb821000','订单消费返积分',NULL,NULL,'2026-08-30 08:43:16','2026-08-30 08:43:16'),('pt_35d9d7b67c1000','res_dev001','fam_dev001','pc_demo001','spend',-18,0,'transfer_to_property',NULL,'转给物业',NULL,NULL,'2026-08-31 11:20:30','2026-08-31 11:20:30'),('pt_36cd38e35c1000','res_dev001','fam_dev001','pc_demo001','spend',-6200,3184,'order','ord_36cd384b201000','订单积分支付',NULL,NULL,'2026-09-24 02:27:26','2026-09-24 02:27:26'),('pt_36cda053621000','res_dev001','fam_dev001','pc_demo001','spend',-20,3164,'transfer_to_property','res_dev001','住户积分转赠物业',NULL,NULL,'2026-09-24 03:23:55','2026-09-24 03:23:55'),('pt_44761000','res_dev001','fam_dev001','pc_demo001','spend',-2255,3184,'transfer_to_property','res_dev001','住户积分转赠物业',NULL,NULL,'2026-09-15 12:57:02','2026-09-15 12:57:02'),('pt_7b4e1000','res_dev001','fam_dev001','pc_demo001','spend',-1,4019,'transfer_to_property','res_dev001','住户积分转赠物业',NULL,NULL,'2026-09-11 00:41:27','2026-09-11 00:41:27'),('pt_80fa1000','res_dev001','fam_dev001','pc_demo001','earn',1,5439,'manual','pcr_demo_phone','信息采集奖励: phone','res_dev001',NULL,'2026-09-15 06:10:51','2026-09-15 06:10:51'),('pt_8fbc1000','res_dev001','fam_dev001','pc_demo001','earn',4020,4020,'order','ord_593c1000','订单消费返积分',NULL,NULL,'2026-09-06 09:33:12','2026-09-06 09:33:12'),('pt_9d4a1000','res_dev001','fam_dev001','pc_demo001','spend',-1,4018,'transfer_to_property','res_dev001','住户积分转赠物业',NULL,NULL,'2026-09-11 00:41:31','2026-09-11 00:41:31'),('pt_a9aa1000','res_dev001','fam_dev001','pc_demo001','earn',1420,5438,'order','ord_821c1000','订单消费返积分',NULL,NULL,'2026-09-11 08:51:26','2026-09-11 08:51:26'),('pt_e1f41000','res_dev001','fam_dev001','pc_demo001','spend',-1420,4020,'order','ord_aed01000','订单积分支付',NULL,NULL,'2026-09-09 01:38:09','2026-09-09 01:38:09');
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
INSERT INTO `point_rules` VALUES ('pr_001','消费返积分','订单消费后按金额返积分','earn','order',1.0000,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'pc_demo001','active','2026-07-05 10:18:21','2026-07-05 10:18:21'),('pr_002','签到积分','每日签到获得积分','earn','sign_in',NULL,NULL,NULL,NULL,NULL,10,200,NULL,'pc_demo001','active','2026-07-05 10:18:21','2026-07-05 10:18:21'),('pr_003','活动奖励积分','参与社区活动获得积分','earn','activity',NULL,NULL,NULL,NULL,NULL,NULL,500,NULL,'pc_demo001','active','2026-07-05 10:18:21','2026-07-05 10:18:21'),('pr_004','积分兑换物业币','100积分兑换1物业币','spend','redeem_coin',0.0100,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'pc_demo001','active','2026-07-05 10:18:21','2026-07-05 10:18:21'),('pr_005','积分抵扣物业费','100积分抵扣1元物业费','spend','deduct_fee',0.0100,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'pc_demo001','active','2026-07-05 10:18:21','2026-07-05 10:18:21');
/*!40000 ALTER TABLE `point_rules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `price_approvals`
--

DROP TABLE IF EXISTS `price_approvals`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `price_approvals` (
  `id` varchar(32) NOT NULL COMMENT 'pa_xxxxxxxx',
  `property_company_id` varchar(32) NOT NULL COMMENT '所属物业公司',
  `applicant_id` varchar(32) NOT NULL COMMENT '申请人ID',
  `applicant_role` varchar(30) NOT NULL COMMENT '申请人角色',
  `item_type` varchar(30) NOT NULL COMMENT '审批类型: delivery_fee/product_price/merchant_ad等',
  `item_id` varchar(32) DEFAULT NULL COMMENT '关联项目ID',
  `old_value` varchar(500) DEFAULT NULL COMMENT '原值（JSON）',
  `new_value` varchar(500) NOT NULL COMMENT '新值（JSON）',
  `reason` varchar(500) DEFAULT NULL COMMENT '申请理由',
  `status` varchar(20) DEFAULT 'pending' COMMENT 'pending/approved/rejected',
  `approver_id` varchar(32) DEFAULT NULL COMMENT '审批人ID（物业领导）',
  `approver_remark` varchar(500) DEFAULT NULL COMMENT '审批备注',
  `approved_at` timestamp NULL DEFAULT NULL COMMENT '审批时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_applicant` (`applicant_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `price_approvals_ibfk_1` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`),
  CONSTRAINT `price_approvals_ibfk_2` FOREIGN KEY (`applicant_id`) REFERENCES `residents` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='价格审批记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `price_approvals`
--

LOCK TABLES `price_approvals` WRITE;
/*!40000 ALTER TABLE `price_approvals` DISABLE KEYS */;
INSERT INTO `price_approvals` VALUES ('pa_351e70a5721000','pc_demo001','res_admin01','platform_admin','resident_share_rate','pc_demo001','{\"residentPointShareRate\":0.004,\"merchantPointShareRate\":0.002,\"coinPointShareRate\":0.002,\"sharedPointShareRate\":0.003}','{\"residentPointShareRate\":0.4,\"merchantPointShareRate\":0.2,\"coinPointShareRate\":0.2,\"sharedPointShareRate\":0.2}','参数配置：调整积分分成比例（业主/商家/物业币/共享）','approved','res_pleader01',NULL,'2026-08-13 06:40:31','2026-08-13 06:39:43','2026-08-13 06:40:31'),('pa_35894b79f81000','pc_demo001','res_admin01','platform_admin','merchant_distribution','mch_355d11a1f21000','{\"rate\":0.1}','{\"rate\":0.2}',NULL,'approved','res_pleader01',NULL,'2026-08-23 15:39:32','2026-08-23 15:39:06','2026-08-23 15:39:32'),('pa_358952b5d41000','pc_demo001','res_admin01','platform_admin','product_price','prd_357991e8e01000','{\"price\":0.1}','{\"price\":1}',NULL,'approved','res_pleader01',NULL,'2026-08-23 15:43:27','2026-08-23 15:43:03','2026-08-23 15:43:27'),('pa_358956721e1000','pc_demo001','res_pleader01','property_admin','merchant_distribution','mch_355d11a1f21000','{\"rate\":0.1}','{\"rate\":0}',NULL,'approved','res_pleader01',NULL,'2026-08-23 15:45:10','2026-08-23 15:45:05','2026-08-23 15:45:10');
/*!40000 ALTER TABLE `price_approvals` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_category_dict`
--

DROP TABLE IF EXISTS `product_category_dict`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_category_dict` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类目名称（中文）',
  `sort_order` int DEFAULT '0' COMMENT '排序权重（升序）',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'active' COMMENT 'active/inactive',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pcd_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业主商城商品类目字典';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_category_dict`
--

LOCK TABLES `product_category_dict` WRITE;
/*!40000 ALTER TABLE `product_category_dict` DISABLE KEYS */;
INSERT INTO `product_category_dict` VALUES ('pcd_001','食品饮料',1,'active','2026-08-21 12:54:24','2026-08-21 12:54:24'),('pcd_002','日用百货',2,'active','2026-08-21 12:54:24','2026-08-21 12:54:24'),('pcd_003','服饰鞋包',3,'active','2026-08-21 12:54:24','2026-08-21 12:54:24'),('pcd_004','家居用品',4,'active','2026-08-21 12:54:24','2026-08-21 12:54:24');
/*!40000 ALTER TABLE `product_category_dict` ENABLE KEYS */;
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
  `is_recommended` tinyint(1) DEFAULT NULL COMMENT '是否平台/物业推荐',
  `recommended_sort` int DEFAULT NULL COMMENT '推荐排序，越大越靠前',
  `version` bigint NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
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
INSERT INTO `products` VALUES ('prd_001','mch_demo001','农夫山泉550ml','天然矿泉水550ml*24瓶','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c41e3741000.jpg',28.00,25.00,2800,195,'饮料','active','2026-07-05 10:18:21','2026-09-19 08:21:40',NULL,NULL,NULL,0),('prd_002','mch_demo001','金龙鱼花生油5L','压榨一级花生油5L','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c4238ea1000.jpg',89.90,85.00,8990,50,'粮油','active','2026-07-05 10:18:21','2026-09-19 08:21:51',NULL,NULL,NULL,0),('prd_003','mch_demo001','蒙牛纯牛奶250ml','纯牛奶250ml*12盒','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c4276801000.webp',45.00,42.00,4500,100,'乳品','active','2026-07-05 10:18:21','2026-09-19 08:21:59',NULL,NULL,NULL,0),('prd_004','mch_demo001','康师傅方便面5连包','红烧牛肉面5连包','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c42d2481000.webp',12.50,11.00,1250,300,'方便食品','active','2026-07-05 10:18:21','2026-09-19 08:22:10',NULL,NULL,NULL,0),('prd_005','mch_demo001','维达抽纸3包装','超韧系列抽纸3包','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c430dde1000.jpg',15.90,14.00,1590,150,'日用品','active','2026-07-05 10:18:21','2026-09-19 08:22:18',NULL,NULL,NULL,0),('prd_006','mch_demo002','红烧牛肉面','手工拉面+秘制牛肉','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch03_369c1dec9e1000.jpg',22.00,20.00,2200,989,'面食','active','2026-07-05 10:18:21','2026-09-19 08:02:01',NULL,NULL,NULL,0),('prd_007','mch_demo002','宫保鸡丁套餐','宫保鸡丁+米饭+汤','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch03_369c1e38c61000.jpg',28.00,25.00,2800,998,'套餐','active','2026-07-05 10:18:21','2026-09-19 08:02:12',NULL,NULL,NULL,0),('prd_008','mch_demo002','麻辣香锅','自选食材麻辣香锅','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch03_369c1e7c5e1000.jpg',35.00,32.00,3500,999,'锅类','active','2026-07-05 10:18:21','2026-09-19 08:02:20',NULL,NULL,NULL,0),('prd_009','mch_demo002','冰镇柠檬水','新鲜柠檬现榨','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch03_369c1ec78e1000.jpg',8.00,7.00,800,990,'饮品','active','2026-07-05 10:18:21','2026-09-19 08:02:29',NULL,NULL,NULL,0),('prd_35153e6e9c1000','mch_act01','摄影机',NULL,'https://test.yuanxusoftware.top/api/v1/static/product/res_act01_369cf884961000.jpg',10.00,NULL,NULL,100,NULL,'active','2026-08-12 09:14:00','2026-09-19 10:01:25',NULL,NULL,NULL,0),('prd_3520aa79381000','mch_mch01','813测试','813测试',NULL,0.10,NULL,NULL,100,NULL,'inactive','2026-08-13 11:50:55','2026-08-13 11:57:40','2026-08-13 03:57:40',NULL,NULL,0),('prd_3520b9254c1000','mch_demo002','813测试',NULL,'/static/default/product.png',0.10,NULL,NULL,95,NULL,'inactive','2026-08-13 11:58:56','2026-08-14 02:32:17','2026-08-13 18:32:17',NULL,NULL,0),('prd_357991e8e01000','mch_demo002','优惠券10元',NULL,'https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch03_369c1da6d21000.jpg',1.00,0.08,NULL,20,'团购','active','2026-08-22 03:00:36','2026-09-19 08:01:53',NULL,NULL,NULL,0),('prd_35cb71ebc41000','mch_35c14661b81000','水饺',NULL,NULL,10.00,NULL,NULL,99,'食堂伙食','active','2026-08-30 01:47:34','2026-08-30 15:21:45',NULL,NULL,NULL,0),('prd_35cb72c10e1000','mch_35c14bfbe61000','小面',NULL,NULL,8.00,NULL,NULL,99,'食堂伙食','active','2026-08-30 01:48:01','2026-08-30 15:22:00',NULL,NULL,NULL,0),('prd_seed_cola','mch_demo001','可口可乐330ml*6罐','经典原味碳酸饮料，冰爽畅快','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c400a461000.jpg',18.00,16.00,1800,149,'饮料','active','2026-08-12 00:00:00','2026-09-19 08:20:40',NULL,NULL,NULL,0),('prd_seed_laundry','mch_demo001','洗衣液3kg','深层洁净，持久留香','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c4064001000.webp',29.90,26.00,2990,80,'日用品','active','2026-08-12 00:00:00','2026-09-19 08:20:51',NULL,NULL,NULL,0),('prd_seed_liquid','mch_demo001','家用洗洁精2kg','强力去油，温和不伤手','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c40cd341000.webp',15.90,14.00,1590,120,'日用品','active','2026-08-12 00:00:00','2026-09-19 08:21:04',NULL,NULL,NULL,0),('prd_seed_rice','mch_demo001','东北珍珠大米5kg','当季新米，颗粒饱满米香浓郁','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c4119da1000.webp',39.90,36.00,3990,100,'粮油','active','2026-08-12 00:00:00','2026-09-19 08:21:14',NULL,NULL,NULL,0),('prd_seed_towel','mch_demo001','纯棉毛巾(加厚)','精选纯棉材质，吸水柔软不掉毛','https://test.yuanxusoftware.top/api/v1/static/merchant/res_mch02_369c419fa61000.jpg',12.90,11.50,1290,200,'日用品','active','2026-08-12 00:00:00','2026-09-19 08:21:31',NULL,NULL,NULL,0);
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `profile_completion_rewards`
--

DROP TABLE IF EXISTS `profile_completion_rewards`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `profile_completion_rewards` (
  `id` varchar(32) NOT NULL COMMENT 'pcr_xxxxxxxx',
  `scope` varchar(20) NOT NULL COMMENT 'property_company/community/platform/coordinator',
  `scope_id` varchar(32) NOT NULL COMMENT '对应 scope 的 ID',
  `field_name` varchar(50) NOT NULL COMMENT 'phone/birthday/has_children/gender/family_info 等',
  `reward_points` int NOT NULL DEFAULT '0',
  `once_per_user` tinyint(1) DEFAULT '1',
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_scope` (`scope`,`scope_id`),
  KEY `idx_field_name` (`field_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='信息采集积分奖励配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profile_completion_rewards`
--

LOCK TABLES `profile_completion_rewards` WRITE;
/*!40000 ALTER TABLE `profile_completion_rewards` DISABLE KEYS */;
INSERT INTO `profile_completion_rewards` VALUES ('pcr_demo_birthday','property_company','pc_demo001','birthday',1,1,'active','2026-07-30 05:39:11','2026-08-01 04:58:26'),('pcr_demo_has_children','property_company','pc_demo001','has_children',1,1,'active','2026-07-30 05:39:11','2026-08-01 04:58:30'),('pcr_demo_phone','property_company','pc_demo001','phone',1,1,'active','2026-07-30 05:39:11','2026-08-01 04:58:33');
/*!40000 ALTER TABLE `profile_completion_rewards` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `profile_reward_records`
--

DROP TABLE IF EXISTS `profile_reward_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `profile_reward_records` (
  `id` varchar(32) NOT NULL COMMENT 'prr_xxxxxxxx',
  `resident_id` varchar(32) NOT NULL,
  `field_name` varchar(50) NOT NULL,
  `reward_points` int NOT NULL,
  `reward_config_id` varchar(32) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resident_field_config` (`resident_id`,`field_name`,`reward_config_id`),
  CONSTRAINT `fk_prr_resident` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='信息采集奖励发放记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profile_reward_records`
--

LOCK TABLES `profile_reward_records` WRITE;
/*!40000 ALTER TABLE `profile_reward_records` DISABLE KEYS */;
INSERT INTO `profile_reward_records` VALUES ('prr_35217834a61000','res_35217712f21000','birthday',1,'pcr_demo_birthday','2026-08-13 13:43:16'),('prr_354898fdd41000','res_dev001','birthday',1,'pcr_demo_birthday','2026-08-17 08:53:46'),('prr_35716175901000','res_35217712f21000','phone',1,'pcr_demo_phone','2026-08-21 07:55:40'),('prr_80ca1000','res_dev001','phone',1,'pcr_demo_phone','2026-09-15 06:10:51');
/*!40000 ALTER TABLE `profile_reward_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_leaders`
--

DROP TABLE IF EXISTS `project_leaders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_leaders` (
  `id` varchar(32) NOT NULL COMMENT 'pl_xxxxxxxx',
  `resident_id` varchar(32) NOT NULL,
  `regional_leader_id` varchar(32) DEFAULT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `name` varchar(50) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `project_name` varchar(100) DEFAULT NULL,
  `share_rate` decimal(5,4) DEFAULT '0.0000' COMMENT '从区域负责人中抽取的分成比例',
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resident_id` (`resident_id`),
  KEY `idx_regional_leader_id` (`regional_leader_id`),
  CONSTRAINT `fk_pl_regional_leader` FOREIGN KEY (`regional_leader_id`) REFERENCES `regional_leaders` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_pl_resident` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目负责人';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_leaders`
--

LOCK TABLES `project_leaders` WRITE;
/*!40000 ALTER TABLE `project_leaders` DISABLE KEYS */;
/*!40000 ALTER TABLE `project_leaders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `property_bank_cards`
--

DROP TABLE IF EXISTS `property_bank_cards`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `property_bank_cards` (
  `id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `bank_name` varchar(100) NOT NULL,
  `branch_name` varchar(100) DEFAULT NULL,
  `account_name` varchar(50) NOT NULL,
  `account_no` varchar(50) NOT NULL,
  `purpose` varchar(20) NOT NULL DEFAULT 'points',
  `is_default` tinyint(1) NOT NULL,
  `status` varchar(20) NOT NULL,
  `remark` varchar(200) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_pbc_pc` (`property_company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业公司收款银行卡';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `property_bank_cards`
--

LOCK TABLES `property_bank_cards` WRITE;
/*!40000 ALTER TABLE `property_bank_cards` DISABLE KEYS */;
/*!40000 ALTER TABLE `property_bank_cards` ENABLE KEYS */;
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
  `idempotency_key` varchar(64) DEFAULT NULL COMMENT '幂等键，防重复出账流水',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_coin_records_idempotency` (`idempotency_key`),
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
INSERT INTO `property_coin_records` VALUES ('coin_351c88e10c1000','res_dev001','fam_dev001','pc_demo001','spend',-16.00,1002.50,'order','ord_351c88bee41000','订单物业币支付',NULL,NULL,'2026-08-13 02:13:20','2026-08-13 02:13:20'),('coin_3520d257ee1000','res_dev001','fam_dev001','pc_demo001','spend',-40.00,962.50,'manual','svc_001','服务预约',NULL,NULL,'2026-08-13 12:12:41','2026-08-13 12:12:41'),('coin_352164b60e1000','res_dev001','fam_dev001','pc_demo001','spend',-0.20,962.50,'order','ord_352164559e1000','订单物业币支付',NULL,NULL,'2026-08-13 13:32:38','2026-08-13 13:32:38'),('coin_352182f62c1000','res_35217712f21000','fam_35217f046c1000','pc_demo001','earn',0.10,0.10,'manual',NULL,'管理员充值','res_pleader01',NULL,'2026-08-13 13:49:09','2026-08-13 13:49:09'),('coin_3526f494961000','res_dev001','fam_dev001','pc_demo001','spend',-20.00,962.50,'order','ord_3526f46ef21000','订单物业币支付',NULL,NULL,'2026-08-14 02:30:15','2026-08-14 02:30:15'),('coin_352869c8ac1000','res_dev001','fam_dev001','pc_demo001','spend',-7.10,962.50,'order','ord_35286996141000','订单物业币支付',NULL,NULL,'2026-08-14 05:54:04','2026-08-14 05:54:04'),('coin_35288b08941000','res_dev001','fam_dev001','pc_demo001','spend',-7.10,962.50,'order','ord_35288abea81000','订单物业币支付',NULL,NULL,'2026-08-14 06:12:14','2026-08-14 06:12:14'),('coin_3545b8e6da1000','res_dev001','fam_dev001','pc_demo001','spend',-12.00,950.50,'manual','svc_004','服务订阅',NULL,NULL,'2026-08-17 02:11:46','2026-08-17 02:11:46'),('coin_354fc8ec2c1000','res_dev001','fam_dev001','pc_demo001','spend',-7.10,950.50,'order','ord_354fc89cc21000','订单物业币支付',NULL,NULL,'2026-08-18 01:38:37','2026-08-18 01:38:37'),('coin_354fea5bc61000','res_dev001','fam_dev001','pc_demo001','spend',-0.10,950.40,'transfer_to_property',NULL,'转给物业',NULL,NULL,'2026-08-18 01:56:53','2026-08-18 01:56:53'),('coin_35513fd1dc1000','res_dev001','fam_dev001','pc_demo001','spend',-7.20,950.40,'order','ord_35513f81481000','订单物业币支付',NULL,NULL,'2026-08-18 05:03:22','2026-08-18 05:03:22'),('coin_35522112b81000','res_dev001','fam_dev001','pc_demo001','spend',-7.20,950.40,'order','ord_355220b31e1000','订单物业币支付',NULL,NULL,'2026-08-18 07:06:23','2026-08-18 07:06:23'),('coin_35522247441000','res_dev001','fam_dev001','pc_demo001','spend',-7.20,950.40,'order','ord_3552221f381000','订单物业币支付',NULL,NULL,'2026-08-18 07:07:02','2026-08-18 07:07:02'),('coin_3566fb4d461000','res_dev001','fam_dev001','pc_demo001','spend',-950.40,0.00,'transfer_to_property',NULL,'转给物业',NULL,NULL,'2026-08-20 07:41:46','2026-08-20 07:41:46'),('coin_357996a1221000','res_dev001','fam_dev001','pc_demo001','earn',10.00,10.00,'manual',NULL,'管理员充值','res_admin01',NULL,'2026-08-22 03:03:11','2026-08-22 03:03:11'),('coin_357997f3fe1000','res_dev001','fam_dev001','pc_demo001','spend',-0.19,10.00,'order','ord_357997afda1000','订单物业币支付',NULL,NULL,'2026-08-22 03:03:54','2026-08-22 03:03:54'),('coin_357998de5e1000','res_dev001','fam_dev001','pc_demo001','spend',0.19,10.00,'order_cancel','ord_357997afda1000','商家核实拒绝退款-物业币',NULL,NULL,'2026-08-22 03:04:24','2026-08-22 03:04:24'),('coin_3579ae18721000','res_dev001','fam_dev001','pc_demo001','spend',-0.19,10.00,'order','ord_3579ade7821000','订单物业币支付',NULL,NULL,'2026-08-22 03:16:00','2026-08-22 03:16:00'),('coin_357aa1f7e61000','res_dev001','fam_dev001','pc_demo001','earn',0.19,10.19,'order','ord_3579ade7821000','订单取消退回物业币',NULL,NULL,'2026-08-22 05:29:11','2026-08-22 05:29:11'),('coin_357b4829e21000','res_dev001','fam_dev001','pc_demo001','spend',-1.00,9.19,'transfer_to_property',NULL,'转给物业',NULL,NULL,'2026-08-22 06:59:57','2026-08-22 06:59:57'),('coin_358393d55c1000','res_dev001','fam_dev001','pc_demo001','earn',100.00,109.19,'manual',NULL,'管理员充值','res_admin01',NULL,'2026-08-23 02:19:45','2026-08-23 02:19:45'),('coin_358394435e1000','res_dev001','fam_dev001','pc_demo001','spend',-20.00,109.19,'order','ord_35839426361000','订单物业币支付',NULL,NULL,'2026-08-23 02:19:59','2026-08-23 02:19:59'),('coin_3583e6e2641000','res_dev001','fam_dev001','pc_demo001','spend',-20.00,89.19,'manual','svc_353d0d595c1000','服务预约',NULL,NULL,'2026-08-23 03:05:07','2026-08-23 03:05:07'),('coin_3583ea84e21000','res_dev001','fam_dev001','pc_demo001','spend',-20.00,69.19,'manual','svc_353d0d595c1000','服务预约',NULL,NULL,'2026-08-23 03:07:06','2026-08-23 03:07:06'),('coin_3585317ae41000','res_dev001','fam_dev001','pc_demo001','spend',-0.09,69.19,'order','ord_3585315b4c1000','订单物业币支付',NULL,NULL,'2026-08-23 06:05:40','2026-08-23 06:05:40'),('coin_358531c2da1000','res_dev001','fam_dev001','pc_demo001','spend',-20.00,69.19,'order','ord_358531aa921000','订单物业币支付',NULL,NULL,'2026-08-23 06:05:49','2026-08-23 06:05:49'),('coin_358533be4a1000','res_dev001','fam_dev001','pc_demo001','spend',-20.00,69.19,'order','ord_358533a5ce1000','订单物业币支付',NULL,NULL,'2026-08-23 06:06:54','2026-08-23 06:06:54'),('coin_35854062641000','res_dev001','fam_dev001','pc_demo001','spend',-20.00,69.19,'order','ord_35854036621000','订单物业币支付',NULL,NULL,'2026-08-23 06:13:48','2026-08-23 06:13:48'),('coin_358609da0c1000','res_dev001','fam_dev001','pc_demo001','spend',-20.00,69.19,'order','ord_358609a9a81000','订单物业币支付',NULL,NULL,'2026-08-23 08:03:50','2026-08-23 08:03:50'),('coin_35862909d61000','res_dev001','fam_dev001','pc_demo001','spend',-25.00,69.19,'order','ord_358628e0fa1000','订单物业币支付',NULL,NULL,'2026-08-23 08:20:52','2026-08-23 08:20:52'),('coin_3586308c761000','res_dev001','fam_dev001','pc_demo001','spend',-7.10,69.19,'order','ord_358630648a1000','订单物业币支付',NULL,NULL,'2026-08-23 08:24:58','2026-08-23 08:24:58'),('coin_358634711c1000','res_dev001','fam_dev001','pc_demo001','spend',-0.09,69.19,'order','ord_35863441be1000','订单物业币支付',NULL,NULL,'2026-08-23 08:27:05','2026-08-23 08:27:05'),('coin_35cb86c4b21000','res_dev001','fam_dev001','pc_demo001','spend',-20.00,49.19,'manual','svc_353d0d595c1000','服务预约',NULL,NULL,'2026-08-30 01:58:57','2026-08-30 01:58:57'),('coin_35ce6b1ac81000','res_dev001','fam_dev001','pc_demo001','spend',-0.09,49.19,'order','ord_35ce6aeb821000','订单物业币支付',NULL,NULL,'2026-08-30 08:43:16','2026-08-30 08:43:16'),('coin_36cda168641000','res_dev001','fam_dev001','pc_demo001','spend',-20.00,887.24,'transfer_to_property','res_dev001','住户家庭物业币转赠物业',NULL,NULL,'2026-09-24 03:24:31','2026-09-24 03:24:31'),('coin_6b981000','res_dev001','fam_dev001','pc_demo001','recharge',1.00,50.19,'manual',NULL,'物业币充值',NULL,NULL,'2026-09-10 05:50:22','2026-09-10 05:50:22'),('coin_8fa01000','res_dev001','fam_dev001','pc_demo001','spend',-20.10,49.19,'order','ord_593c1000','订单物业币支付',NULL,NULL,'2026-09-06 09:33:12','2026-09-06 09:33:12'),('coin_a99e1000','res_dev001','fam_dev001','pc_demo001','spend',-7.10,49.19,'order','ord_821c1000','订单物业币支付',NULL,NULL,'2026-09-11 08:51:26','2026-09-11 08:51:26'),('coin_f9c01000','res_dev001','fam_dev001','pc_demo001','spend',-1.00,49.19,'transfer_to_property','res_dev001','住户物业币转赠物业',NULL,NULL,'2026-09-11 00:41:43','2026-09-11 00:41:43');
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
INSERT INTO `property_coin_rules` VALUES ('pcr_001','充值物业币','1元充值1物业币','earn','recharge',1.0000,NULL,NULL,NULL,NULL,'pc_demo001','active','2026-07-05 10:18:21','2026-07-05 10:18:21'),('pcr_002','消费返物业币','订单消费后按比例返物业币','earn','order',0.0500,NULL,NULL,NULL,NULL,'pc_demo001','active','2026-07-05 10:18:21','2026-07-05 10:18:21'),('pcr_003','物业币抵扣订单','1物业币抵扣1元','spend','pay_order',1.0000,NULL,NULL,NULL,NULL,'pc_demo001','active','2026-07-05 10:18:21','2026-07-05 10:18:21');
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
  `regional_leader_rate` decimal(5,4) DEFAULT '0.0000',
  `project_leader_rate` decimal(5,4) DEFAULT '0.0000',
  `platform_share_rate` decimal(5,4) DEFAULT '0.0000' COMMENT '平台管理员从订单可分配金额中的分成比例',
  `courier_share_rate` decimal(5,4) DEFAULT '0.1500' COMMENT '快递员从订单可分配金额中的分成比例',
  `platform_delivery_share_rate` decimal(5,4) DEFAULT '0.0000' COMMENT '平台管理员从配送费中的分成比例',
  `platform_withdrawal_fee_share_rate` decimal(5,4) DEFAULT '1.0000' COMMENT '平台管理员从提现手续费中的分成比例（默认100%归平台）',
  `coin_display_enabled` tinyint(1) DEFAULT '1' COMMENT '物业币卡片全局显示',
  `point_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '积分功能是否启用',
  `coin_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '物业币功能是否启用',
  `point_display_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '积分卡片是否全局展示',
  `coin_issue_mode` varchar(20) DEFAULT 'auto' COMMENT '发放模式',
  `coin_expiry_days` int DEFAULT '365' COMMENT '物业币有效期天数',
  `coin_freeze_default` tinyint(1) DEFAULT '0' COMMENT '新用户物业币默认冻结',
  `coin_use_condition` varchar(30) DEFAULT 'none' COMMENT 'none/point_threshold/property_fee_paid/manual_issue',
  `coin_point_threshold` int DEFAULT '0' COMMENT '积分门槛（coin_use_condition=point_threshold 时生效）',
  `coin_mall_enabled` tinyint(1) DEFAULT '1' COMMENT '物业币商城是否启用',
  `coin_mall_max_ratio` decimal(5,4) DEFAULT '1.0000' COMMENT '物业币抵扣最大比例（0~1）',
  `coin_mall_min_amount` decimal(10,2) DEFAULT '0.01' COMMENT '物业币抵扣最低消费金额',
  `delivery_base_fee` decimal(10,2) DEFAULT '0.00' COMMENT '配送起步价',
  `delivery_per_kg_fee` decimal(10,2) DEFAULT '0.00' COMMENT '按重量加价(元/kg)',
  `delivery_courier_share_rate` decimal(5,4) DEFAULT '1.0000' COMMENT '快递员配送费分成比例',
  `delivery_courier_per_order` decimal(10,2) DEFAULT '0.35' COMMENT '快递员每单报酬',
  `withdrawal_fee_rate` decimal(5,4) DEFAULT '0.0060' COMMENT '提现手续费率',
  `auto_withdrawal_enabled` tinyint(1) DEFAULT '0' COMMENT '是否开启自动提现',
  `auto_withdrawal_period_days` int DEFAULT '1' COMMENT '自动提现周期天数',
  `company_account_balance` decimal(10,2) DEFAULT NULL,
  `settlement_balance` decimal(10,2) DEFAULT NULL,
  `point_to_fee_rate` decimal(10,4) DEFAULT '0.0100' COMMENT '积分抵扣物业费比率',
  `point_exchange_rate` decimal(10,2) DEFAULT '100.00' COMMENT '物业级积分兑换比（1元=X积分，默认100积分=1元；订单支付以商家挂接配置为准）',
  `two_year_clear_enabled` tinyint(1) DEFAULT '1' COMMENT '2年欠费积分清零开关',
  `neighbor_daily_contact_limit` int DEFAULT '3' COMMENT '邻居私聊每日限制（不同人数）',
  `status` varchar(20) DEFAULT 'active' COMMENT 'active/inactive',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `resident_point_share_rate` decimal(5,4) DEFAULT '0.0000' COMMENT '居民积分分成比例',
  `merchant_point_share_rate` decimal(5,4) DEFAULT '0.0000' COMMENT '商家积分分成比例',
  `coin_point_share_rate` decimal(5,4) DEFAULT '0.0000' COMMENT '物业币积分分成比例',
  `shared_point_share_rate` decimal(5,4) DEFAULT '0.0000' COMMENT '共享积分分成比例',
  `points_attribution_mode` varchar(20) DEFAULT NULL COMMENT '积分归属 property/resident',
  `reward_attribution_mode` varchar(20) DEFAULT NULL COMMENT '奖励金归属 property/resident',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业公司表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `property_companies`
--

LOCK TABLES `property_companies` WRITE;
/*!40000 ALTER TABLE `property_companies` DISABLE KEYS */;
INSERT INTO `property_companies` VALUES ('pc_8df81000','测试物业0811a',NULL,'0427-88110811',NULL,0.3000,0.7000,0.1000,0.0000,NULL,NULL,NULL,NULL,NULL,NULL,1,1,1,1,'auto',365,0,NULL,NULL,NULL,NULL,NULL,0.00,0.00,1.0000,0.35,0.0060,NULL,NULL,NULL,NULL,0.0100,100.00,1,2,'inactive','2026-09-07 14:38:38','2026-09-08 06:47:55',NULL,NULL,NULL,NULL,NULL,NULL),('pc_baid001','碧桂园物业服务','https://example.com/logo_bgy.png','0755-88880001','深圳市南山区科技园路1号',0.6000,0.3000,0.6000,0.5000,0.0000,0.0000,0.1000,0.1500,0.0000,1.0000,1,1,1,1,'auto',365,0,'none',0,1,1.0000,0.01,2.00,0.00,1.0000,0.35,0.0060,0,1,0.00,0.00,0.0100,100.00,1,2,'active','2026-08-01 14:17:53','2026-08-09 08:26:08',0.0000,0.0000,0.0000,0.0000,NULL,NULL),('pc_demo001','万达花园物业',NULL,NULL,NULL,0.6010,0.2990,0.6000,0.5000,0.0000,0.0000,0.1000,0.1500,0.1000,1.0000,1,1,1,1,'manual',366,1,'none',0,1,1.0000,0.01,0.50,0.04,1.0000,0.40,0.0050,1,2,0.00,0.48,0.0200,100.00,0,3,'active','2026-07-02 01:57:36','2026-09-08 05:43:22',0.3000,0.2000,0.2000,0.3000,NULL,NULL),('pc_hrl001','华润置地物业','https://example.com/logo_hr.png','0755-88880002','深圳市福田区中心路2号',0.6000,0.3000,0.6000,0.5000,0.0000,0.0000,0.1000,0.1500,0.0000,1.0000,1,1,1,1,'auto',365,0,'none',0,1,1.0000,0.01,2.00,0.00,1.0000,0.35,0.0060,0,1,0.00,0.00,0.0100,100.00,1,2,'active','2026-08-01 14:17:53','2026-08-09 08:26:08',0.0000,0.0000,0.0000,0.0000,NULL,NULL),('pc_vanke001','万科物业服务','https://example.com/logo_vanke.png','0755-88880003','深圳市龙岗区坂田路3号',0.6000,0.3000,0.6000,0.5000,0.0000,0.0000,0.1000,0.1500,0.0000,1.0000,1,1,1,1,'auto',365,0,'none',0,1,1.0000,0.01,2.00,0.00,1.0000,0.35,0.0060,0,1,0.00,0.00,0.0100,100.00,1,2,'active','2026-08-01 14:17:53','2026-08-09 08:26:08',0.0000,0.0000,0.0000,0.0000,NULL,NULL);
/*!40000 ALTER TABLE `property_companies` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `property_contact_configs`
--

DROP TABLE IF EXISTS `property_contact_configs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `property_contact_configs` (
  `id` varchar(32) NOT NULL,
  `property_name` varchar(100) NOT NULL COMMENT '物业名称',
  `contact_phone` varchar(50) NOT NULL COMMENT '联系电话（座机/服务热线）',
  `contact_mobile` varchar(50) DEFAULT NULL COMMENT '备用手机/微信电话',
  `address` varchar(255) DEFAULT NULL COMMENT '物业地址',
  `service_hours` varchar(100) DEFAULT NULL COMMENT '服务时间',
  `email` varchar(100) DEFAULT NULL COMMENT '联系邮箱',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注（仅管理端可见）',
  `enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业联系方式配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `property_contact_configs`
--

LOCK TABLES `property_contact_configs` WRITE;
/*!40000 ALTER TABLE `property_contact_configs` DISABLE KEYS */;
INSERT INTO `property_contact_configs` VALUES ('pcc_35217b69561000','万达小区','13912345678',NULL,NULL,NULL,NULL,NULL,1,'2026-08-13 13:45:01','2026-08-13 13:45:01');
/*!40000 ALTER TABLE `property_contact_configs` ENABLE KEYS */;
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
INSERT INTO `property_fee_rules` VALUES ('pfr_001','物业费','property','area',1.50,'元/㎡/月','quarterly',1,0.0010,'pc_demo001','active','2026-07-05 10:28:49','2026-07-05 10:28:49'),('pfr_002','停车费','parking','fixed',150.00,'元/月','monthly',1,0.0000,'pc_demo001','active','2026-07-05 10:28:49','2026-07-05 10:28:49'),('pfr_003','水费','water','usage',3.50,'元/吨','monthly',0,0.0010,'pc_demo001','active','2026-07-05 10:28:49','2026-07-05 10:28:49'),('pfr_004','电费','electricity','usage',0.56,'元/度','monthly',0,0.0010,'pc_demo001','active','2026-07-05 10:28:49','2026-07-05 10:28:49');
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
  `period_start` date DEFAULT NULL COMMENT '账期起始日',
  `period_end` date DEFAULT NULL COMMENT '账期结束日',
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
INSERT INTO `property_fees` VALUES ('pf_gen00001','res_001','fam_001','pc_demo001','com_demo001','property_management','2026-Q1','2026-01-01','2026-03-31',541.89,541.89,0,0.00,'paid','2026-07-10','2026-07-24 07:32:07',0,0.00,'2026-07-22 11:50:07','2026-07-22 11:50:07'),('pf_gen00002','res_001','fam_001','pc_demo001','com_demo001','property_management','2026-Q2','2026-04-01','2026-06-30',567.34,567.34,0,0.00,'paid','2026-07-10','2026-08-08 07:18:30',0,0.00,'2026-07-17 22:21:52','2026-07-17 22:21:52'),('pf_gen00003','res_002','fam_002','pc_demo001','com_demo001','property_management','2026-Q1','2026-01-01','2026-03-31',149.37,149.37,0,0.00,'paid','2026-07-10','2026-08-07 07:23:34',0,0.00,'2026-07-15 09:12:33','2026-07-15 09:12:33'),('pf_gen00004','res_002','fam_002','pc_demo001','com_demo001','property_management','2026-Q2','2026-04-01','2026-06-30',149.35,149.35,0,0.00,'paid','2026-07-10','2026-07-20 07:52:07',0,0.00,'2026-07-28 04:50:07','2026-07-28 04:50:07'),('pf_gen00005','res_002','fam_002','pc_demo001','com_demo001','property_management','2026-Q3','2026-07-01','2026-09-30',161.58,161.58,0,0.00,'paid','2026-07-10','2026-07-05 04:27:19',0,0.00,'2026-07-07 12:26:07','2026-07-07 12:26:07'),('pf_gen00006','res_003','fam_003','pc_demo001','com_demo002','property_management','2026-Q1','2026-01-01','2026-03-31',479.76,0.00,0,0.00,'overdue','2026-07-10',NULL,0,0.00,'2026-08-03 22:43:22','2026-08-03 22:43:22'),('pf_gen00007','res_003','fam_003','pc_demo001','com_demo002','property_management','2026-Q2','2026-04-01','2026-06-30',350.58,350.58,0,0.00,'paid','2026-07-10','2026-07-27 20:44:17',0,0.00,'2026-07-21 22:20:16','2026-07-21 22:20:16'),('pf_gen00008','res_004',NULL,'pc_demo001','com_demo001','property_management','2026-Q1','2026-01-01','2026-03-31',286.37,0.00,0,0.00,'unpaid','2026-07-10',NULL,0,0.00,'2026-07-05 10:36:29','2026-07-05 10:36:29'),('pf_gen00009','res_act01',NULL,'pc_demo001','com_demo001','property_management','2026-Q1','2026-01-01','2026-03-31',562.34,0.00,0,0.00,'unpaid','2026-07-10',NULL,0,0.00,'2026-07-09 02:18:47','2026-07-09 02:18:47'),('pf_gen00010','res_act01',NULL,'pc_demo001','com_demo001','property_management','2026-Q2','2026-04-01','2026-06-30',485.36,485.36,0,0.00,'paid','2026-07-10','2026-07-27 14:02:17',0,0.00,'2026-07-04 01:21:33','2026-07-04 01:21:33'),('pf_gen00011','res_act01',NULL,'pc_demo001','com_demo001','property_management','2026-Q3','2026-07-01','2026-09-30',346.73,0.00,0,0.00,'unpaid','2026-07-10',NULL,0,0.00,'2026-08-06 22:10:39','2026-08-06 22:10:39'),('pf_gen00012','res_admin01',NULL,'pc_demo001','com_demo001','property_management','2026-Q1','2026-01-01','2026-03-31',241.49,241.49,0,0.00,'paid','2026-07-10','2026-07-03 03:39:54',0,0.00,'2026-07-08 02:39:08','2026-07-08 02:39:08'),('pf_gen00013','res_coo01',NULL,'pc_demo001','com_demo001','property_management','2026-Q1','2026-01-01','2026-03-31',537.05,0.00,0,0.00,'unpaid','2026-07-10',NULL,0,0.00,'2026-07-04 21:02:30','2026-07-04 21:02:30'),('pf_gen00014','res_coo01',NULL,'pc_demo001','com_demo001','property_management','2026-Q2','2026-04-01','2026-06-30',375.40,0.00,0,0.00,'unpaid','2026-07-10',NULL,0,0.00,'2026-07-20 07:25:25','2026-07-20 07:25:25'),('pf_gen00015','res_courier03',NULL,'pc_demo001','com_demo001','property_management','2026-Q1','2026-01-01','2026-03-31',600.61,600.61,0,0.00,'paid','2026-07-10','2026-07-27 02:18:34',0,0.00,'2026-08-04 09:18:46','2026-08-04 09:18:46'),('pf_gen00016','res_dev001','fam_dev001','pc_demo001','com_demo001','property_management','2026-Q1','2026-01-01','2026-03-31',559.43,0.00,0,0.00,'unpaid','2026-07-10',NULL,0,0.00,'2026-07-18 01:22:10','2026-07-18 01:22:10'),('pf_gen00017','res_dev002',NULL,'pc_demo001','com_demo001','property_management','2026-Q1','2026-01-01','2026-03-31',260.35,0.00,0,0.00,'unpaid','2026-07-10',NULL,0,0.00,'2026-07-07 16:01:26','2026-07-07 16:01:26'),('pf_gen00018','res_dev002',NULL,'pc_demo001','com_demo001','property_management','2026-Q2','2026-04-01','2026-06-30',477.75,477.75,0,0.00,'paid','2026-07-10','2026-07-09 08:27:14',0,0.00,'2026-07-11 10:17:02','2026-07-11 10:17:02'),('pf_gen00019','res_dev002',NULL,'pc_demo001','com_demo001','property_management','2026-Q3','2026-07-01','2026-09-30',276.60,276.60,0,0.00,'paid','2026-07-10','2026-08-10 10:53:48',0,0.00,'2026-07-26 16:51:29','2026-07-26 16:51:29'),('pf_gen00020','res_dev003',NULL,'pc_demo001','com_demo001','property_management','2026-Q1','2026-01-01','2026-03-31',570.35,0.00,0,0.00,'unpaid','2026-07-10',NULL,0,0.00,'2026-07-28 12:37:32','2026-07-28 12:37:32'),('pf_gen00021','res_dev004',NULL,'pc_demo001','com_demo001','property_management','2026-Q1','2026-01-01','2026-03-31',673.15,673.15,0,0.00,'paid','2026-07-10','2026-07-03 04:07:30',0,0.00,'2026-07-27 20:48:25','2026-07-27 20:48:25'),('pf_gen00022','res_ind01',NULL,'pc_demo001','com_demo001','property_management','2026-Q1','2026-01-01','2026-03-31',538.11,538.11,0,0.00,'paid','2026-07-10','2026-08-01 11:42:55',0,0.00,'2026-07-27 12:41:18','2026-07-27 12:41:18'),('pf_gen00023','res_ind01',NULL,'pc_demo001','com_demo001','property_management','2026-Q2','2026-04-01','2026-06-30',519.09,0.00,0,0.00,'unpaid','2026-07-10',NULL,0,0.00,'2026-07-20 11:52:28','2026-07-20 11:52:28'),('pf_gen00024','res_mch03',NULL,'pc_demo001','com_demo001','property_management','2026-Q1','2026-01-01','2026-03-31',322.41,0.00,0,0.00,'unpaid','2026-07-10',NULL,0,0.00,'2026-07-04 16:00:47','2026-07-04 16:00:47'),('pf_gen00025','res_mch04',NULL,'pc_demo001','com_demo001','property_management','2026-Q1','2026-01-01','2026-03-31',268.48,268.48,0,0.00,'paid','2026-07-10','2026-07-18 16:15:39',0,0.00,'2026-08-03 02:15:39','2026-08-03 02:15:39'),('pf_gen00026','res_propadmin01',NULL,'pc_demo001','com_demo001','property_management','2026-Q1','2026-01-01','2026-03-31',596.68,0.00,0,0.00,'overdue','2026-07-10',NULL,0,0.00,'2026-07-21 11:59:16','2026-07-21 11:59:16'),('pf_gen00027','res_propadmin01',NULL,'pc_demo001','com_demo001','property_management','2026-Q2','2026-04-01','2026-06-30',406.49,406.49,0,0.00,'paid','2026-07-10','2026-07-04 02:13:56',0,0.00,'2026-07-06 10:19:22','2026-07-06 10:19:22'),('pf_gen00028','res_sec01',NULL,'pc_demo001','com_demo001','property_management','2026-Q1','2026-01-01','2026-03-31',640.72,640.72,0,0.00,'paid','2026-07-10','2026-07-12 17:27:32',0,0.00,'2026-08-06 05:59:37','2026-08-06 05:59:37'),('pf_gen00029','res_tech01',NULL,'pc_demo001','com_demo001','property_management','2026-Q1','2026-01-01','2026-03-31',547.16,0.00,0,0.00,'unpaid','2026-07-10',NULL,0,0.00,'2026-08-06 03:19:26','2026-08-06 03:19:26'),('pf_gen00030','res_tech01',NULL,'pc_demo001','com_demo001','property_management','2026-Q2','2026-04-01','2026-06-30',344.69,344.69,0,0.00,'paid','2026-07-10','2026-08-09 21:56:20',0,0.00,'2026-07-20 15:17:25','2026-07-20 15:17:25'),('pf_gen00031','res_tech01',NULL,'pc_demo001','com_demo001','property_management','2026-Q3','2026-07-01','2026-09-30',584.43,0.00,0,0.00,'unpaid','2026-07-10',NULL,0,0.00,'2026-07-29 02:30:22','2026-07-29 02:30:22');
/*!40000 ALTER TABLE `property_fees` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `property_operator_scopes`
--

DROP TABLE IF EXISTS `property_operator_scopes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `property_operator_scopes` (
  `id` varchar(32) NOT NULL,
  `resident_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `community_ids` json DEFAULT NULL,
  `building_nos` json DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resident_company` (`resident_id`,`property_company_id`),
  KEY `property_operator_scopes_ibfk_2` (`property_company_id`),
  CONSTRAINT `property_operator_scopes_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `property_operator_scopes_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `property_operator_scopes`
--

LOCK TABLES `property_operator_scopes` WRITE;
/*!40000 ALTER TABLE `property_operator_scopes` DISABLE KEYS */;
INSERT INTO `property_operator_scopes` VALUES ('pos_fb941000','res_35217712f21000','pc_demo001',NULL,NULL,'2026-09-07 00:07:47','2026-09-07 00:07:47'),('pos_pleader01','res_pleader01','pc_demo001','[\"com_demo001\"]','[\"1\", \"2\"]','2026-07-15 02:24:42','2026-07-29 01:41:53'),('pos_poperator01','res_poperator01','pc_demo001','[\"com_demo001\"]','[\"1\", \"2\"]','2026-07-15 02:24:42','2026-07-15 02:24:42');
/*!40000 ALTER TABLE `property_operator_scopes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `region_quotas`
--

DROP TABLE IF EXISTS `region_quotas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `region_quotas` (
  `id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `sector` varchar(50) NOT NULL,
  `quota_type` varchar(20) NOT NULL,
  `quota_amount` decimal(12,2) NOT NULL,
  `used_amount` decimal(12,2) NOT NULL,
  `period_start` date NOT NULL,
  `period_end` date NOT NULL,
  `status` varchar(20) NOT NULL,
  `remark` varchar(200) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_rq_pc_sector` (`property_company_id`,`sector`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='区域/板块额度配额';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `region_quotas`
--

LOCK TABLES `region_quotas` WRITE;
/*!40000 ALTER TABLE `region_quotas` DISABLE KEYS */;
/*!40000 ALTER TABLE `region_quotas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `regional_leaders`
--

DROP TABLE IF EXISTS `regional_leaders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `regional_leaders` (
  `id` varchar(32) NOT NULL COMMENT 'rl_xxxxxxxx',
  `resident_id` varchar(32) NOT NULL,
  `sector_leader_id` varchar(32) DEFAULT NULL COMMENT '所属板块负责人',
  `coordinator_id` varchar(32) DEFAULT NULL COMMENT '所属统筹（也可由统筹直接任命）',
  `property_company_id` varchar(32) NOT NULL,
  `name` varchar(50) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `region_name` varchar(100) DEFAULT NULL COMMENT '负责区域名称',
  `share_rate` decimal(5,4) DEFAULT '0.0000' COMMENT '从板块负责人中抽取的分成比例',
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resident_id` (`resident_id`),
  KEY `idx_sector_leader_id` (`sector_leader_id`),
  CONSTRAINT `fk_rl_resident` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `fk_rl_sector_leader` FOREIGN KEY (`sector_leader_id`) REFERENCES `sector_leaders` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='区域负责人';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `regional_leaders`
--

LOCK TABLES `regional_leaders` WRITE;
/*!40000 ALTER TABLE `regional_leaders` DISABLE KEYS */;
INSERT INTO `regional_leaders` VALUES ('rl_351d6b06541000','res_3516a17fb01000','sl_001',NULL,'pc_demo001','www','138****5678','保洁',0.0500,'inactive','2026-08-13 04:16:50','2026-08-13 04:17:11');
/*!40000 ALTER TABLE `regional_leaders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resident_merchant_applications`
--

DROP TABLE IF EXISTS `resident_merchant_applications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resident_merchant_applications` (
  `id` varchar(32) NOT NULL,
  `resident_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `deposit_amount` decimal(10,2) NOT NULL DEFAULT '500.00',
  `status` varchar(30) DEFAULT 'pending_deposit',
  `visibility` varchar(10) NOT NULL DEFAULT 'private' COMMENT '店铺对外展示：public(对外，任何人可查看)/private(不对外，默认)',
  `audit_remark` varchar(200) DEFAULT NULL,
  `audited_by` varchar(32) DEFAULT NULL,
  `audited_at` timestamp NULL DEFAULT NULL,
  `quit_requested_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resident` (`resident_id`),
  KEY `resident_merchant_applications_ibfk_2` (`property_company_id`),
  KEY `idx_visibility_status` (`visibility`,`status`),
  CONSTRAINT `resident_merchant_applications_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `resident_merchant_applications_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resident_merchant_applications`
--

LOCK TABLES `resident_merchant_applications` WRITE;
/*!40000 ALTER TABLE `resident_merchant_applications` DISABLE KEYS */;
INSERT INTO `resident_merchant_applications` VALUES ('rma_351c0e77581000','res_dev001','pc_demo001',500.00,'active','public','线上保证金支付阻塞，线下确认收款后开通（2026-09-04 数据修复）','system','2026-09-04 16:14:17',NULL,'2026-08-13 01:06:28','2026-09-05 13:13:04');
/*!40000 ALTER TABLE `resident_merchant_applications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resident_merchant_deposits`
--

DROP TABLE IF EXISTS `resident_merchant_deposits`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resident_merchant_deposits` (
  `id` varchar(32) NOT NULL,
  `application_id` varchar(32) NOT NULL,
  `resident_id` varchar(32) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `status` varchar(20) DEFAULT 'frozen',
  `payment_method` varchar(20) DEFAULT 'wechat',
  `paid_at` timestamp NULL DEFAULT NULL,
  `refunded_at` timestamp NULL DEFAULT NULL,
  `deducted_amount` decimal(10,2) DEFAULT '0.00',
  `deduct_reason` varchar(200) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `resident_merchant_deposits_ibfk_1` (`application_id`),
  KEY `resident_merchant_deposits_ibfk_2` (`resident_id`),
  CONSTRAINT `resident_merchant_deposits_ibfk_1` FOREIGN KEY (`application_id`) REFERENCES `resident_merchant_applications` (`id`),
  CONSTRAINT `resident_merchant_deposits_ibfk_2` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resident_merchant_deposits`
--

LOCK TABLES `resident_merchant_deposits` WRITE;
/*!40000 ALTER TABLE `resident_merchant_deposits` DISABLE KEYS */;
INSERT INTO `resident_merchant_deposits` VALUES ('rmd_dev001offline','rma_351c0e77581000','res_dev001',500.00,'frozen','offline','2026-09-04 16:14:17',NULL,0.00,NULL,'2026-09-04 16:14:17','2026-09-04 16:14:17');
/*!40000 ALTER TABLE `resident_merchant_deposits` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resident_merchant_listings`
--

DROP TABLE IF EXISTS `resident_merchant_listings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resident_merchant_listings` (
  `id` varchar(32) NOT NULL,
  `resident_merchant_id` varchar(32) NOT NULL,
  `resident_id` varchar(32) NOT NULL,
  `distributor_product_id` varchar(32) NOT NULL,
  `retail_price` decimal(10,2) NOT NULL,
  `enabled` tinyint(1) DEFAULT '1',
  `share_count` int DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `resident_merchant_listings_ibfk_1` (`resident_merchant_id`),
  KEY `resident_merchant_listings_ibfk_2` (`resident_id`),
  KEY `resident_merchant_listings_ibfk_3` (`distributor_product_id`),
  CONSTRAINT `resident_merchant_listings_ibfk_1` FOREIGN KEY (`resident_merchant_id`) REFERENCES `resident_merchant_applications` (`id`),
  CONSTRAINT `resident_merchant_listings_ibfk_2` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `resident_merchant_listings_ibfk_3` FOREIGN KEY (`distributor_product_id`) REFERENCES `distributor_products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resident_merchant_listings`
--

LOCK TABLES `resident_merchant_listings` WRITE;
/*!40000 ALTER TABLE `resident_merchant_listings` DISABLE KEYS */;
INSERT INTO `resident_merchant_listings` VALUES ('rml_c0b01000','rma_351c0e77581000','res_dev001','dpr_seed0003',31.00,1,1,'2026-09-05 09:15:08','2026-09-24 10:21:50');
/*!40000 ALTER TABLE `resident_merchant_listings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resident_merchant_settlements`
--

DROP TABLE IF EXISTS `resident_merchant_settlements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resident_merchant_settlements` (
  `id` varchar(32) NOT NULL,
  `order_id` varchar(32) NOT NULL,
  `listing_id` varchar(32) NOT NULL,
  `resident_id` varchar(32) NOT NULL,
  `distributor_merchant_id` varchar(32) NOT NULL,
  `order_amount` decimal(10,2) NOT NULL,
  `wholesale_amount` decimal(10,2) NOT NULL,
  `reseller_profit` decimal(10,2) NOT NULL,
  `platform_commission` decimal(10,2) DEFAULT '0.00',
  `status` varchar(20) DEFAULT 'pending',
  `settle_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `resident_merchant_settlements_ibfk_1` (`order_id`),
  KEY `resident_merchant_settlements_ibfk_2` (`listing_id`),
  KEY `resident_merchant_settlements_ibfk_3` (`resident_id`),
  CONSTRAINT `resident_merchant_settlements_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `resident_merchant_settlements_ibfk_2` FOREIGN KEY (`listing_id`) REFERENCES `resident_merchant_listings` (`id`),
  CONSTRAINT `resident_merchant_settlements_ibfk_3` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resident_merchant_settlements`
--

LOCK TABLES `resident_merchant_settlements` WRITE;
/*!40000 ALTER TABLE `resident_merchant_settlements` DISABLE KEYS */;
/*!40000 ALTER TABLE `resident_merchant_settlements` ENABLE KEYS */;
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
  `birthday` date DEFAULT NULL COMMENT '生日',
  `marital_status` varchar(20) DEFAULT NULL COMMENT 'single/married/divorced/widowed',
  `has_children` tinyint(1) DEFAULT '0',
  `user_type` varchar(20) DEFAULT 'owner' COMMENT 'owner业主/tenant租住人员',
  `role` varchar(30) DEFAULT 'resident' COMMENT '见角色枚举',
  `courier_manager_id` varchar(32) DEFAULT NULL COMMENT '关联快递负责人ID',
  `property_sub_role` varchar(30) DEFAULT NULL COMMENT 'property_leader/property_operator',
  `property_company_id` varchar(32) DEFAULT NULL,
  `community_id` varchar(32) DEFAULT NULL,
  `building` varchar(20) DEFAULT NULL,
  `floor` varchar(20) DEFAULT NULL COMMENT '楼层',
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
  `on_time_payment_badge` tinyint(1) DEFAULT '0' COMMENT '准时缴费徽章',
  `withdrawal_blocked` tinyint(1) DEFAULT '0' COMMENT '是否被手动阻止提现',
  PRIMARY KEY (`id`),
  UNIQUE KEY `openid` (`openid`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_community` (`community_id`),
  KEY `idx_family` (`family_id`),
  KEY `idx_phone` (`phone`),
  KEY `idx_openid` (`openid`),
  KEY `idx_role` (`role`),
  KEY `idx_residents_address` (`community_id`,`building`,`unit`,`room`),
  CONSTRAINT `residents_ibfk_1` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`),
  CONSTRAINT `residents_ibfk_2` FOREIGN KEY (`community_id`) REFERENCES `communities` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业主/住户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `residents`
--

LOCK TABLES `residents` WRITE;
/*!40000 ALTER TABLE `residents` DISABLE KEYS */;
INSERT INTO `residents` VALUES ('res_001','dev_openid_13900000005',NULL,'13900000005','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','业主-赵六',NULL,0,NULL,NULL,NULL,0,'owner','resident',NULL,NULL,'pc_demo001','com_demo001','1','2','2','201','fam_001','owner',3000,200.00,0,0,0,'active','2026-07-03 08:09:15','2026-08-04 08:53:49',NULL,0,0),('res_002',NULL,NULL,'13900000006','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','业主-钱七',NULL,0,NULL,NULL,NULL,0,'owner','resident',NULL,NULL,'pc_demo001','com_demo001','3',NULL,'2','202','fam_002','owner',1500,100.00,0,0,0,'active','2026-07-03 08:09:15','2026-07-17 09:55:33',NULL,0,0),('res_003',NULL,NULL,'13900000010','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','业主-孙八',NULL,0,NULL,NULL,NULL,0,'owner','resident',NULL,NULL,'pc_demo001','com_demo002','5',NULL,'1','101','fam_003','owner',2000,150.00,0,0,0,'active','2026-07-03 08:09:15','2026-07-03 08:09:15',NULL,0,0),('res_004','dev_openid_13900000007',NULL,'13900000007','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','租户-周九',NULL,0,NULL,NULL,NULL,0,'tenant','resident',NULL,NULL,'pc_demo001','com_demo001','4',NULL,'3','301',NULL,NULL,500,30.00,0,0,0,'deleted','2026-07-03 08:09:15','2026-07-05 10:28:49','2026-07-04 03:23:55',0,0),('res_3516a17fb01000',NULL,NULL,'13812345678',NULL,'www',NULL,1,31,NULL,NULL,NULL,'owner','resident',NULL,NULL,'pc_demo001','com_demo001','1','1','2','205',NULL,NULL,0,0.00,0,0,0,'active','2026-08-12 12:27:55','2026-09-07 12:57:20',NULL,NULL,NULL),('res_35217712f21000','oTZ8Q5M7jRzuvi5eZgIGM88vUmnE','oCkdc1laVnuOzgYfy01HIrnSW_Hc','18061611751','$2a$10$ZywwlKIVxILBVqZtLPSRDOtDZ5MoCVgNgOwAgRQIRu5rK8JG7PhJu','测试管理员',NULL,1,22,NULL,'single',0,'owner','property_admin',NULL,'property_operator','pc_demo001','com_demo001','1','3','1','302','fam_35217f046c1000','owner',341,0.10,0,0,1,'deleted','2026-08-13 13:42:39','2026-09-07 00:07:47','2026-08-19 05:43:49',NULL,NULL),('res_353cfd32481000','oTZ8Q5EVN4b2tFz6ZuuRDha9pXyE','oCkdc1hsClCrW-Tkm2Xh0-5PUmLM',NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,0,'owner','resident',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0,0,1,'active','2026-08-16 05:50:47','2026-08-16 05:50:47',NULL,NULL,NULL),('res_355efb1ae81000','oTZ8Q5BwGQLHbgN8tpIWPSV8EVOw','oCkdc1giDWRaSELTi0RUl-9MGKcc',NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,0,'owner','resident',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0,0,1,'active','2026-08-19 13:03:11','2026-08-19 13:03:11',NULL,NULL,NULL),('res_356526b0741000','oTZ8Q5NhZNqP3XIDwy5uhMjivqDg','oCkdc1vMOBaMiv2xqChg36LG3zT8',NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,0,'owner','resident',NULL,NULL,'pc_baid001',NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0,0,1,'active','2026-08-20 03:25:51','2026-08-20 03:25:51',NULL,NULL,NULL),('res_357cadf0581000','oTZ8Q5A49nwWF7OGJ9LG5ntliOr0','oCkdc1kJ-Ibk5UWVajbTdhiwvl2Q','18012930438',NULL,'吕昊文',NULL,1,20,NULL,'single',0,'owner','resident',NULL,NULL,'pc_baid001','com_baid_001',NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0,0,1,'active','2026-08-22 10:15:20','2026-08-22 10:16:36',NULL,NULL,NULL),('res_35890655f01000',NULL,NULL,'13955512345',NULL,'微微',NULL,1,20,'2006-06-10',NULL,NULL,'owner','resident',NULL,NULL,'pc_demo001','com_demo001','6','6','1','601',NULL,NULL,0,0.00,0,0,0,'active','2026-08-23 15:01:20','2026-08-23 15:01:20',NULL,NULL,NULL),('res_35c14607ae1000',NULL,NULL,'13987654321','$2a$10$op.Whu9Tyj70TjldD7uKLObpIjdqGtewRX//2bF2hdujaKQuQRDTe','立民',NULL,0,NULL,NULL,NULL,NULL,'owner','merchant',NULL,NULL,'pc_demo001','com_demo001',NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0,0,0,'active','2026-08-29 02:05:29','2026-08-30 01:11:16',NULL,NULL,NULL),('res_35c14b96101000',NULL,NULL,'12987654321','$2a$10$op.Whu9Tyj70TjldD7uKLObpIjdqGtewRX//2bF2hdujaKQuQRDTe','民生小面',NULL,0,NULL,NULL,NULL,NULL,'owner','merchant',NULL,NULL,'pc_demo001','com_demo001',NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0,0,0,'active','2026-08-29 02:08:31','2026-08-30 01:11:16',NULL,NULL,NULL),('res_8e2a1000',NULL,NULL,'13981108112','$2a$10$EsVZnmut5JD0nBppAfBPeectahr0Q0GOnTg8HHmMAoUPpQ.rUprJC','管理97',NULL,NULL,NULL,NULL,NULL,NULL,'owner','property_admin',NULL,NULL,'pc_8df81000',NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0,0,0,'active','2026-09-07 14:38:38','2026-09-07 14:38:38',NULL,NULL,NULL),('res_act01',NULL,NULL,'13900000011','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','活动组长-郑美丽',NULL,0,NULL,NULL,NULL,0,'owner','activity_leader',NULL,NULL,'pc_demo001','com_demo001','2',NULL,'1','101',NULL,NULL,600,40.00,0,0,0,'active','2026-07-03 08:09:15','2026-07-03 08:09:15',NULL,0,0),('res_admin01',NULL,NULL,'13800138000','$2b$10$b86xg99Rxo1pSziHNT/5Ju0TQ59eNg3WG9CBQgr/sCP1d6apCjaDO','平台管理员',NULL,0,NULL,NULL,NULL,0,'owner','platform_admin',NULL,NULL,'pc_demo001','com_demo001',NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0,0,0,'active','2026-07-02 01:57:36','2026-08-07 02:21:10',NULL,0,0),('res_coo01',NULL,NULL,'13900000002','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','统筹-张伟',NULL,0,NULL,NULL,NULL,0,'owner','coordinator',NULL,NULL,'pc_demo001','com_demo001',NULL,NULL,NULL,NULL,NULL,NULL,1200,150.00,0,0,0,'active','2026-07-03 08:09:15','2026-07-03 08:09:15',NULL,0,0),('res_courier01',NULL,NULL,'13900000016','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','快递员-吴十',NULL,0,NULL,NULL,NULL,0,'owner','courier','cm_demo001',NULL,'pc_demo001','com_demo001',NULL,NULL,NULL,NULL,NULL,NULL,200,15.00,0,0,0,'active','2026-07-03 08:09:15','2026-07-22 05:29:07',NULL,0,0),('res_courier02','courier_openid_002',NULL,'13900000013','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','快递员-李四',NULL,0,NULL,NULL,NULL,0,'owner','courier','cm_demo002',NULL,'pc_demo001','com_demo001',NULL,NULL,NULL,NULL,NULL,NULL,150,10.00,0,0,0,'active','2026-07-05 05:49:39','2026-07-30 03:24:02',NULL,0,0),('res_courier03','courier_openid_003',NULL,'13900000014','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','快递员-王五',NULL,0,NULL,NULL,NULL,0,'owner','courier','cm_demo002',NULL,'pc_demo001','com_demo001',NULL,NULL,NULL,NULL,NULL,NULL,100,5.00,0,0,0,'active','2026-07-05 05:49:39','2026-07-22 05:29:07',NULL,0,0),('res_demo01',NULL,NULL,'13700000001','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','住户-甲',NULL,0,NULL,NULL,NULL,0,'owner','resident',NULL,NULL,'pc_demo001','com_demo001','1','1','1','501',NULL,NULL,0,0.00,0,0,0,'active','2026-08-17 08:28:25','2026-08-17 08:28:25',NULL,0,0),('res_demo02',NULL,NULL,'13700000002','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','住户-乙',NULL,0,NULL,NULL,NULL,0,'owner','resident',NULL,NULL,'pc_demo001','com_demo001','2','1','1','502',NULL,NULL,0,0.00,0,0,0,'active','2026-08-17 08:28:25','2026-08-17 08:28:25',NULL,0,0),('res_demo03',NULL,NULL,'13700000003','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','住户-丙',NULL,0,NULL,NULL,NULL,0,'owner','resident',NULL,NULL,'pc_demo001','com_demo001','3','1','1','503',NULL,NULL,0,0.00,0,0,0,'active','2026-08-17 08:28:25','2026-08-17 08:28:25',NULL,0,0),('res_dev001','dev_openid_13800000001',NULL,'18861350048',NULL,'张阿姨','https://test.yuanxusoftware.top/api/v1/static/avatar/res_dev001_46cc1000.png',2,41,'1986-01-01','single',0,'owner','resident',NULL,NULL,'pc_demo001','com_demo001','1','1','1','108','fam_dev001','owner',3164,49.19,0,0,1,'active','2026-07-03 07:40:59','2026-09-24 03:23:55',NULL,1,0),('res_dev002','dev_openid_13800000002',NULL,NULL,NULL,'张建设',NULL,1,60,NULL,'married',1,'owner','resident',NULL,NULL,'pc_demo001','com_demo001','3',NULL,'12','1201',NULL,NULL,6933,56.86,0,0,1,'disabled','2026-07-03 07:40:59','2026-07-26 09:51:44','2026-07-04 06:33:11',0,0),('res_dev003','dev_openid_13800000003',NULL,NULL,NULL,'张子涵',NULL,1,28,NULL,'single',0,'owner','resident',NULL,NULL,'pc_demo001','com_demo001','3',NULL,'12','1201',NULL,NULL,3188,83.62,0,0,1,'active','2026-07-03 07:40:59','2026-07-09 06:30:55',NULL,0,0),('res_dev004','dev_openid_13800000004',NULL,NULL,NULL,'王秀兰','/static/logo.png',2,28,NULL,'widowed',0,'owner','resident',NULL,NULL,'pc_demo001','com_demo001','3',NULL,'12','1201','fam_dev001','parent',524,50.00,0,0,1,'active','2026-07-03 07:40:59','2026-07-11 01:43:26',NULL,0,0),('res_ind01',NULL,NULL,'13900000004','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','个体负责人-王强',NULL,0,NULL,NULL,NULL,1,'owner','individual_leader',NULL,NULL,'pc_demo001','com_demo001','5','1','1',NULL,NULL,NULL,500,50.00,0,0,0,'active','2026-07-03 08:09:15','2026-08-23 02:37:44',NULL,0,0),('res_mch01',NULL,NULL,'13900000008','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','商家-陈老板',NULL,0,NULL,NULL,NULL,0,'owner','resident',NULL,NULL,'pc_demo001','com_demo001',NULL,NULL,NULL,NULL,NULL,NULL,0,10.04,0,0,0,'active','2026-07-03 08:09:15','2026-09-01 02:54:20',NULL,0,0),('res_mch02',NULL,NULL,'13900000009','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','口福老板',NULL,0,NULL,NULL,NULL,0,'owner','merchant',NULL,NULL,'pc_demo001','com_demo001',NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0,0,0,'active','2026-07-07 01:42:27','2026-08-12 08:39:23',NULL,0,0),('res_mch03',NULL,NULL,'13900000017','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','超市老板',NULL,0,NULL,NULL,NULL,0,'owner','merchant',NULL,NULL,'pc_demo001','com_demo001',NULL,NULL,NULL,NULL,NULL,NULL,0,200.00,0,0,0,'active','2026-07-07 01:42:27','2026-08-13 08:18:34',NULL,0,0),('res_mch04',NULL,NULL,'13900000018',NULL,'农场老板',NULL,0,NULL,NULL,NULL,0,'owner','resident',NULL,NULL,'pc_demo001','com_demo001',NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0,0,0,'active','2026-07-07 01:42:27','2026-08-19 08:07:43',NULL,0,0),('res_pleader01',NULL,NULL,'13900000020','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','物业领导',NULL,0,NULL,NULL,NULL,0,'owner','property_admin',NULL,'property_leader','pc_demo001','com_demo001',NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0,0,0,'active','2026-08-12 08:02:37','2026-08-12 08:31:16',NULL,0,0),('res_propadmin01',NULL,NULL,'13900000001','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','物业管理员-刘芳',NULL,0,NULL,NULL,NULL,0,'owner','property_admin',NULL,'property_operator','pc_demo001','com_demo001',NULL,NULL,NULL,NULL,NULL,NULL,0,0.00,0,0,0,'active','2026-07-03 08:09:15','2026-08-12 08:31:16',NULL,0,0),('res_sec01',NULL,NULL,'13900000003','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','板块负责人-李娜',NULL,0,NULL,NULL,NULL,0,'owner','sector_leader',NULL,NULL,'pc_demo001','com_demo001',NULL,NULL,NULL,NULL,NULL,NULL,800,80.00,0,0,0,'active','2026-07-03 08:09:15','2026-07-03 08:09:15',NULL,0,0),('res_tech01',NULL,NULL,'13900000012','$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS','技工-刘师傅',NULL,0,NULL,NULL,NULL,0,'owner','technician',NULL,NULL,'pc_demo001','com_demo001',NULL,NULL,NULL,NULL,NULL,NULL,300,20.00,0,0,0,'active','2026-07-03 08:09:15','2026-08-07 01:14:13',NULL,0,0);
/*!40000 ALTER TABLE `residents` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reward_grant_applications`
--

DROP TABLE IF EXISTS `reward_grant_applications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reward_grant_applications` (
  `id` varchar(32) NOT NULL,
  `applicant_id` varchar(32) NOT NULL,
  `applicant_role` varchar(20) NOT NULL,
  `target_resident_id` varchar(32) NOT NULL,
  `target_resident_name` varchar(50) DEFAULT NULL,
  `grant_type` varchar(20) NOT NULL DEFAULT 'points',
  `point_amount` int NOT NULL,
  `attribution_mode` varchar(20) NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `audit_status` varchar(20) NOT NULL,
  `approver_id` varchar(32) DEFAULT NULL,
  `approver_remark` varchar(255) DEFAULT NULL,
  `audited_at` datetime DEFAULT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_rga_applicant` (`applicant_id`),
  KEY `idx_rga_pc_status` (`property_company_id`,`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='奖励金/积分发放申请';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reward_grant_applications`
--

LOCK TABLES `reward_grant_applications` WRITE;
/*!40000 ALTER TABLE `reward_grant_applications` DISABLE KEYS */;
INSERT INTO `reward_grant_applications` VALUES ('rga_36a36a75381000','res_dev001','resident','res_dev001','张阿姨','points',1,'property','填信息','pending_audit',NULL,NULL,NULL,'pc_demo001','2026-09-20 09:02:29','2026-09-20 09:02:29');
/*!40000 ALTER TABLE `reward_grant_applications` ENABLE KEYS */;
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
INSERT INTO `role_preset_permissions` VALUES ('rpp_001','rp_001','perm_001','2026-07-02 01:57:36','2026-07-02 01:57:36'),('rpp_002','rp_001','perm_004','2026-07-02 01:57:36','2026-07-02 01:57:36'),('rpp_003','rp_001','perm_005','2026-07-02 01:57:36','2026-07-02 01:57:36'),('rpp_004','rp_001','perm_006','2026-07-02 01:57:36','2026-07-02 01:57:36'),('rpp_005','rp_001','perm_007','2026-07-02 01:57:36','2026-07-02 01:57:36'),('rpp_006','rp_001','perm_002','2026-07-02 01:57:36','2026-07-02 01:57:36'),('rpp_007','rp_001','perm_026','2026-07-12 12:10:52','2026-07-12 12:10:52'),('rpp_010','rp_004','perm_009','2026-07-02 01:57:36','2026-07-02 01:57:36'),('rpp_coord_read_20260907','rp_001','perm_024','2026-09-07 11:08:03','2026-09-07 11:08:03'),('rpp_sec01','rp_002','perm_003','2026-07-08 03:58:33','2026-07-08 03:58:33'),('rpp_sec02','rp_002','perm_004','2026-07-08 03:58:33','2026-07-08 03:58:33'),('rpp_sec03','rp_002','perm_005','2026-07-08 03:58:33','2026-07-08 03:58:33');
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
  `sector` varchar(50) NOT NULL COMMENT 'cleaning/repair/security/greening/other（保洁/维修/安保/绿化/其它）',
  `description` varchar(200) DEFAULT NULL COMMENT '板块描述/备注',
  `commission_rate` decimal(5,4) DEFAULT '0.1000' COMMENT '从统筹中抽取的比例',
  `can_manage_individual_leaders` tinyint(1) DEFAULT '1' COMMENT '是否可管理个体负责人',
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
INSERT INTO `sector_leaders` VALUES ('sl_001','res_sec01','pc_demo001','cleaning',NULL,0.1000,1,'coo_001','active','2026-07-03 08:09:15','2026-09-08 06:33:17'),('sl_6d461000','res_3516a17fb01000','pc_demo001','security','1',0.6000,NULL,'coo_001','inactive','2026-09-07 00:33:09','2026-09-09 01:03:50');
/*!40000 ALTER TABLE `sector_leaders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sensitive_words`
--

DROP TABLE IF EXISTS `sensitive_words`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sensitive_words` (
  `id` varchar(32) NOT NULL,
  `word` varchar(100) NOT NULL,
  `category` varchar(30) DEFAULT NULL,
  `level` int DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sensitive_words`
--

LOCK TABLES `sensitive_words` WRITE;
/*!40000 ALTER TABLE `sensitive_words` DISABLE KEYS */;
INSERT INTO `sensitive_words` VALUES ('sw_001','赌博','spam',1,'2026-08-03 06:57:26','2026-08-03 06:57:26'),('sw_002','博彩','spam',1,'2026-08-03 06:57:26','2026-08-03 06:57:26'),('sw_003','私彩','spam',1,'2026-08-03 06:57:26','2026-08-03 06:57:26'),('sw_004','六合彩','spam',1,'2026-08-03 06:57:26','2026-08-03 06:57:26'),('sw_005','外围','porn',1,'2026-08-03 06:57:26','2026-08-03 06:57:26'),('sw_006','色情','porn',1,'2026-08-03 06:57:26','2026-08-03 06:57:26'),('sw_007','裸聊','porn',1,'2026-08-03 06:57:26','2026-08-03 06:57:26'),('sw_008','办证','fraud',1,'2026-08-03 06:57:26','2026-08-03 06:57:26'),('sw_009','代开发票','fraud',1,'2026-08-03 06:57:26','2026-08-03 06:57:26'),('sw_010','贷款','spam',1,'2026-08-03 06:57:26','2026-08-03 06:57:26'),('sw_011','高利贷','fraud',1,'2026-08-03 06:57:26','2026-08-03 06:57:26'),('sw_012','刷单','fraud',1,'2026-08-03 06:57:26','2026-08-03 06:57:26'),('sw_013','兼职刷单','fraud',1,'2026-08-03 06:57:26','2026-08-03 06:57:26'),('sw_014','传销','fraud',1,'2026-08-03 06:57:26','2026-08-03 06:57:26'),('sw_015','诈骗','fraud',1,'2026-08-03 06:57:26','2026-08-03 06:57:26'),('sw_016','中奖','spam',1,'2026-08-03 06:57:26','2026-08-03 06:57:26'),('sw_017','免费领','spam',1,'2026-08-03 06:57:26','2026-08-03 06:57:26'),('sw_018','返利','spam',1,'2026-08-03 06:57:26','2026-08-03 06:57:26'),('sw_019','加微信','spam',1,'2026-08-03 06:57:26','2026-08-03 06:57:26'),('sw_020','低价出售','spam',1,'2026-08-03 06:57:26','2026-08-03 06:57:26');
/*!40000 ALTER TABLE `sensitive_words` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_category_dict`
--

DROP TABLE IF EXISTS `service_category_dict`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_category_dict` (
  `id` varchar(32) NOT NULL,
  `name` varchar(50) NOT NULL,
  `keywords` json DEFAULT NULL,
  `sort_order` int DEFAULT '0',
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_category_dict`
--

LOCK TABLES `service_category_dict` WRITE;
/*!40000 ALTER TABLE `service_category_dict` DISABLE KEYS */;
INSERT INTO `service_category_dict` VALUES ('scd_01','修窗帘','[\"窗帘\", \"遮光\", \"轨道\"]',1,'active','2026-07-13 10:58:06','2026-07-13 10:58:06'),('scd_02','修下水','[\"下水\", \"下水道\", \"疏通\", \"堵塞\"]',2,'active','2026-07-13 10:58:06','2026-07-13 10:58:06'),('scd_03','修电器','[\"电器\", \"电路\", \"插座\", \"开关\"]',3,'active','2026-07-13 10:58:06','2026-07-13 10:58:06'),('scd_04','修门窗','[\"门窗\", \"门锁\", \"玻璃\"]',4,'active','2026-07-13 10:58:06','2026-07-13 10:58:06'),('scd_05','修水管','[\"水管\", \"漏水\", \"龙头\", \"阀门\"]',5,'active','2026-07-13 10:58:06','2026-07-13 10:58:06'),('scd_06','修家电','[\"家电\", \"冰箱\", \"洗衣机\", \"空调\"]',6,'active','2026-07-13 10:58:06','2026-07-13 10:58:06'),('scd_07','保洁','[\"保洁\", \"清洁\", \"打扫\"]',7,'active','2026-07-13 10:58:06','2026-07-13 10:58:06'),('scd_08','搬家','[\"搬家\", \"搬运\"]',8,'active','2026-07-13 10:58:06','2026-07-13 10:58:06');
/*!40000 ALTER TABLE `service_category_dict` ENABLE KEYS */;
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
INSERT INTO `service_orders` VALUES ('so_3520d257d21000','svc_001','res_dev001','res_tech01','pc_demo001','completed',40.00,'property_coin','2026-08-12 16:00:00','11101','13900000017',NULL,'2026-08-13 12:12:41','2026-08-13 12:26:25'),('so_353d0e5b1e1000','svc_353d0d595c1000','res_dev001','res_tech01','pc_demo001','cancelled',20.00,'wechat','2026-08-15 17:00:00','11101','13900000012',NULL,'2026-08-16 06:00:09','2026-08-16 06:40:00'),('so_3583e6e2601000','svc_353d0d595c1000','res_dev001','res_tech01','pc_demo001','completed',20.00,'property_coin','2026-08-22 16:00:00','11108','13624964818',NULL,'2026-08-23 03:05:07','2026-08-23 03:06:03'),('so_3583ea84de1000','svc_353d0d595c1000','res_dev001','res_tech01','pc_demo001','cancelled',20.00,'property_coin','2026-08-22 16:00:00','11108','13624964818','确认取消该工单？取消后不可恢复。','2026-08-23 03:07:06','2026-08-23 03:07:20'),('so_35cb869c5a1000','svc_353d0d595c1000','res_dev001','res_tech01','pc_demo001','pending',20.00,'wechat','2026-08-29 16:00:00','11108','13624964818',NULL,'2026-08-30 01:58:52','2026-08-30 01:58:52'),('so_35cb86c49c1000','svc_353d0d595c1000','res_dev001','res_tech01','pc_demo001','confirmed',20.00,'property_coin','2026-08-29 16:00:00','11108','13624964818',NULL,'2026-08-30 01:58:57','2026-08-30 01:58:57');
/*!40000 ALTER TABLE `service_orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_request_merchant_queue`
--

DROP TABLE IF EXISTS `service_request_merchant_queue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_request_merchant_queue` (
  `id` varchar(32) NOT NULL,
  `request_id` varchar(32) NOT NULL,
  `merchant_id` varchar(32) NOT NULL,
  `queue_order` int NOT NULL DEFAULT '0',
  `status` varchar(20) DEFAULT 'pending',
  `notified_at` timestamp NULL DEFAULT NULL,
  `responded_at` timestamp NULL DEFAULT NULL,
  `timeout_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `service_request_merchant_queue_ibfk_1` (`request_id`),
  KEY `service_request_merchant_queue_ibfk_2` (`merchant_id`),
  CONSTRAINT `service_request_merchant_queue_ibfk_1` FOREIGN KEY (`request_id`) REFERENCES `service_request_orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `service_request_merchant_queue_ibfk_2` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_request_merchant_queue`
--

LOCK TABLES `service_request_merchant_queue` WRITE;
/*!40000 ALTER TABLE `service_request_merchant_queue` DISABLE KEYS */;
/*!40000 ALTER TABLE `service_request_merchant_queue` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_request_orders`
--

DROP TABLE IF EXISTS `service_request_orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_request_orders` (
  `id` varchar(32) NOT NULL,
  `resident_id` varchar(32) NOT NULL,
  `property_company_id` varchar(32) NOT NULL,
  `community_id` varchar(32) DEFAULT NULL,
  `description` text NOT NULL,
  `contact_phone` varchar(20) DEFAULT NULL,
  `address` varchar(200) DEFAULT NULL COMMENT '服务地址（自动带出楼栋+单元+房号，可覆盖）',
  `preferred_time` timestamp NULL DEFAULT NULL,
  `matched_category_id` varchar(32) DEFAULT NULL,
  `current_merchant_id` varchar(32) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'matching',
  `quote_amount` decimal(10,2) DEFAULT NULL,
  `quote_payment_method` varchar(20) DEFAULT NULL,
  `service_order_id` varchar(32) DEFAULT NULL,
  `closed_reason` varchar(200) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `service_request_orders_ibfk_1` (`resident_id`),
  KEY `service_request_orders_ibfk_2` (`property_company_id`),
  CONSTRAINT `service_request_orders_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `service_request_orders_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_request_orders`
--

LOCK TABLES `service_request_orders` WRITE;
/*!40000 ALTER TABLE `service_request_orders` DISABLE KEYS */;
/*!40000 ALTER TABLE `service_request_orders` ENABLE KEYS */;
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
INSERT INTO `service_subscriptions` VALUES ('ss_3545b8e48e1000','svc_004','res_dev001','pc_demo001','monthly',12.00,'2026-08-17','2026-09-17','active',0,'2026-08-17 02:11:46','2026-08-17 02:11:46');
/*!40000 ALTER TABLE `service_subscriptions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shedlock`
--

DROP TABLE IF EXISTS `shedlock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shedlock` (
  `name` varchar(64) NOT NULL COMMENT '锁名称，与 @SchedulerLock(name) 一致',
  `lock_until` timestamp(3) NULL DEFAULT NULL COMMENT '锁到期时间',
  `locked_at` timestamp(3) NULL DEFAULT NULL COMMENT '加锁时间',
  `locked_by` varchar(255) DEFAULT NULL COMMENT '持锁实例标识',
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ShedLock 分布式任务锁';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shedlock`
--

LOCK TABLES `shedlock` WRITE;
/*!40000 ALTER TABLE `shedlock` DISABLE KEYS */;
INSERT INTO `shedlock` VALUES ('auto_withdrawal_daily','2026-09-23 12:05:00.002','2026-09-23 12:00:00.002','iZ3o1089n66ygfZ'),('ecommerce_applyment_status_sync','2026-09-23 09:11:00.031','2026-09-23 09:10:00.031','iZ3o1089n66ygfZ'),('fulfillment_choice_timeout','2026-09-24 03:44:57.562','2026-09-24 03:44:57.557','iZ3o1089n66ygfZ'),('merchant_transfer_process','2026-09-24 03:46:40.278','2026-09-24 03:45:40.278','iZ3o1089n66ygfZ'),('merchant_verification_auto_approve','2026-09-24 03:40:35.469','2026-09-24 03:40:35.464','iZ3o1089n66ygfZ'),('merchant_verification_settle_frozen','2026-09-24 03:41:35.464','2026-09-24 03:40:35.464','iZ3o1089n66ygfZ'),('payment_settle_reconcile_pending','2026-09-24 03:45:48.260','2026-09-24 03:45:48.255','iZ3o1089n66ygfZ'),('payment_settle_retry_pending','2026-09-24 03:45:48.447','2026-09-24 03:45:47.646','iZ3o1089n66ygfZ'),('pending_payment_close','2026-09-24 03:40:00.005','2026-09-24 03:40:00.000','iZ3o1089n66ygfZ'),('refund_query_poll','2026-09-24 03:44:56.617','2026-09-24 03:44:56.612','iZ3o1089n66ygfZ'),('service_request_timeout','2026-09-24 03:44:57.140','2026-09-24 03:44:57.135','iZ3o1089n66ygfZ'),('split_failure_retry','2026-09-24 03:44:47.199','2026-09-24 03:44:47.194','iZ3o1089n66ygfZ'),('split_finish_expired_window','2026-09-23 10:31:00.003','2026-09-23 10:30:00.003','iZ3o1089n66ygfZ'),('split_monitor_collection_balance','2026-09-23 17:01:00.023','2026-09-23 17:00:00.023','iZ3o1089n66ygfZ'),('split_reconcile_daily','2026-09-23 11:11:00.002','2026-09-23 11:10:00.002','iZ3o1089n66ygfZ'),('split_recovery_alert','2026-09-23 12:01:00.003','2026-09-23 12:00:00.003','iZ3o1089n66ygfZ'),('split_retry_failed_withdrawals','2026-09-23 10:51:00.002','2026-09-23 10:50:00.002','iZ3o1089n66ygfZ'),('split_retry_skipped','2026-09-23 11:21:00.002','2026-09-23 11:20:00.002','iZ3o1089n66ygfZ'),('split_reverse_failed_retry','2026-09-24 03:41:36.967','2026-09-24 03:40:36.967','iZ3o1089n66ygfZ');
/*!40000 ALTER TABLE `shedlock` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `special_offers`
--

DROP TABLE IF EXISTS `special_offers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `special_offers` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'so_xxxxxxxx',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容',
  `target_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '推送对象类型：all/tag/building/role',
  `target_tags` json DEFAULT NULL COMMENT '推送标签（JSON数组）',
  `publisher_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发布者ID',
  `publisher_role` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发布者角色',
  `property_company_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物业公司ID',
  `community_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '小区ID',
  `merchant_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商家ID',
  `cover_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面图片',
  `discount_info` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '优惠信息',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '结束时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'draft' COMMENT '状态：draft/published/ended/archived',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `min_consumption` decimal(10,2) DEFAULT NULL COMMENT '最低消费门槛',
  `total_quota` int DEFAULT NULL COMMENT '总配额',
  `per_user_quota` int DEFAULT NULL COMMENT '每人限领数量',
  `used_quota` int NOT NULL DEFAULT '0' COMMENT '已领取数量',
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
INSERT INTO `special_offers` VALUES ('so_0cf01000','111','1111','all',NULL,'res_coo01','coordinator','pc_demo001',NULL,NULL,NULL,NULL,'2026-09-08 03:49:00','2026-09-15 03:49:00','published','2026-09-08 11:49:13','2026-09-08 11:50:41',NULL,NULL,NULL,0),('so_3bb41000','测试98','测试98','all',NULL,'res_coo01','coordinator','pc_demo001',NULL,NULL,NULL,NULL,'2026-09-07 22:53:00','2026-09-14 22:53:00','draft','2026-09-08 06:53:51','2026-09-09 01:16:14',NULL,NULL,NULL,0),('so_5f0c1000','测试910','测试910','all',NULL,'res_sec01','sector_leader','pc_demo001',NULL,NULL,NULL,NULL,'2026-09-09 16:33:00','2026-09-16 16:33:00','published','2026-09-10 00:33:35','2026-09-10 00:33:35',NULL,NULL,NULL,0),('so_admin001','家政清洁服务8折','社区家政服务限时8折优惠，专业清洁团队上门服务','all',NULL,'res_ind01','individual_leader','pc_demo001','com_demo001',NULL,NULL,'全场8折','2026-06-30 16:00:00','2026-07-15 15:59:59','archived','2026-07-03 08:02:56','2026-07-08 06:48:38',NULL,NULL,NULL,0),('so_admin002','暑期托管班招生','社区暑期儿童托管班开始报名，物业币可抵扣50%1','all',NULL,'res_coo01','coordinator','pc_demo001',NULL,NULL,NULL,'物业币抵扣50%','2026-07-04 16:00:00','2026-07-20 15:59:00','archived','2026-07-03 08:02:56','2026-09-09 01:10:09',NULL,NULL,NULL,0),('so_dev001','新用户首单立减10元','新用户首次下单享受立减10元优惠，满30元可用','all',NULL,'res_admin01','platform_admin','pc_demo001',NULL,NULL,NULL,'满30减10','2026-06-30 16:00:00','2026-07-31 15:59:59','archived','2026-07-03 06:53:28','2026-09-09 01:06:42',NULL,NULL,NULL,0),('so_dev002','口福餐厅限时特惠','东北拉皮原价18元，现价12元，限时3天！','all',NULL,'res_mch01','merchant','pc_demo001',NULL,'mch_demo001',NULL,'原价18 现价12','2026-07-04 16:00:00','2026-07-07 15:59:59','archived','2026-07-03 06:53:28','2026-09-09 01:05:40',NULL,NULL,NULL,0),('so_e68a1000','测试99','99','all',NULL,'res_coo01','coordinator','pc_demo001',NULL,NULL,NULL,NULL,'2026-09-08 17:11:00','2026-09-15 17:11:00','published','2026-09-09 01:11:24','2026-09-09 01:11:24',NULL,NULL,NULL,0),('so_seed001','新用户首单立减10元','新注册业主首次下单立减10元，满30元可用。','all',NULL,'res_admin01','platform_admin','pc_demo001',NULL,NULL,NULL,'满30减10','2026-08-12 00:00:00','2026-09-12 00:00:00','published','2026-08-12 00:00:00','2026-08-12 00:00:00',30.00,NULL,NULL,0),('so_seed002','夏日饮品特惠','可乐、矿泉水等清凉饮品限时8折，物业币可叠加抵扣。','all',NULL,'res_admin01','platform_admin','pc_demo001',NULL,NULL,NULL,'全场8折','2026-08-12 00:00:00','2026-08-26 00:00:00','archived','2026-08-12 00:00:00','2026-09-09 01:09:39',NULL,NULL,NULL,0);
/*!40000 ALTER TABLE `special_offers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `split_recovery_records`
--

DROP TABLE IF EXISTS `split_recovery_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `split_recovery_records` (
  `id` varchar(32) NOT NULL COMMENT '主键 srr_',
  `split_record_id` varchar(32) NOT NULL COMMENT '分账记录ID wechat_split_record.id',
  `order_id` varchar(32) NOT NULL COMMENT '订单ID',
  `owner_type` varchar(32) NOT NULL COMMENT '角色：COURIER等',
  `owner_id` varchar(32) DEFAULT NULL COMMENT '角色ID（快递员ID）',
  `amount` decimal(10,2) NOT NULL COMMENT '待追回金额（元）',
  `status` varchar(24) NOT NULL DEFAULT 'pending' COMMENT 'pending(待追回)/recovered(已追回)/written_off(已核销)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注（追回方式/线下处理记录）',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_split` (`split_record_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='个人接收方不可回退分账-平台垫付待追回台账';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `split_recovery_records`
--

LOCK TABLES `split_recovery_records` WRITE;
/*!40000 ALTER TABLE `split_recovery_records` DISABLE KEYS */;
/*!40000 ALTER TABLE `split_recovery_records` ENABLE KEYS */;
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
INSERT INTO `user_permissions` VALUES ('up_3585dab89e1000','res_35217712f21000','perm_006',0,'pc_demo001','res_admin01','2026-08-23 07:38:05','2026-08-23 07:38:41'),('up_3585dab8b81000','res_35217712f21000','perm_001',0,'pc_demo001','res_admin01','2026-08-23 07:38:05','2026-08-23 07:38:41'),('up_3585dab8be1000','res_35217712f21000','perm_002',0,'pc_demo001','res_admin01','2026-08-23 07:38:05','2026-08-23 07:38:41'),('up_3585dab8ca1000','res_35217712f21000','perm_005',0,'pc_demo001','res_admin01','2026-08-23 07:38:05','2026-08-23 07:38:41'),('up_3585dab8de1000','res_35217712f21000','perm_004',0,'pc_demo001','res_admin01','2026-08-23 07:38:05','2026-08-23 07:38:41'),('up_3585dab8e41000','res_35217712f21000','perm_007',0,'pc_demo001','res_admin01','2026-08-23 07:38:05','2026-08-23 07:38:41'),('up_3585dab8ec1000','res_35217712f21000','perm_026',0,'pc_demo001','res_admin01','2026-08-23 07:38:05','2026-08-23 07:38:41'),('up_35e07ca2401000','res_coo01','perm_022',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca2501000','res_coo01','perm_011',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca25a1000','res_coo01','perm_016',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca2641000','res_coo01','perm_023',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca2681000','res_coo01','perm_042',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca2721000','res_coo01','perm_024',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca27c1000','res_coo01','perm_010',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca2861000','res_coo01','perm_018',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca2901000','res_coo01','perm_043',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca2981000','res_coo01','perm_030',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca29e1000','res_coo01','perm_038',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca2a41000','res_coo01','perm_028',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca2a81000','res_coo01','perm_019',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca2ae1000','res_coo01','perm_020',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca2b41000','res_coo01','perm_008',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca2ba1000','res_coo01','perm_003',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca2be1000','res_coo01','perm_041',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca2c41000','res_coo01','perm_025',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca2ca1000','res_coo01','perm_013',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca2d01000','res_coo01','perm_029',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca2d81000','res_coo01','perm_039',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca2dc1000','res_coo01','perm_012',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca2e41000','res_coo01','perm_014',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca2ea1000','res_coo01','perm_015',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca2f01000','res_coo01','perm_040',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca2f81000','res_coo01','perm_017',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca2fc1000','res_coo01','perm_021',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca3001000','res_coo01','perm_027',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca3081000','res_coo01','perm_035',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca3101000','res_coo01','perm_037',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca3181000','res_coo01','perm_044',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca31e1000','res_coo01','perm_036',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_35e07ca3241000','res_coo01','perm_009',0,'pc_demo001','res_pleader01','2026-09-01 02:49:26','2026-09-01 02:49:26'),('up_admin001','res_coo01','perm_001',1,'pc_demo001','res_propadmin01','2026-06-20 06:00:00','2026-07-03 08:02:56'),('up_admin002','res_coo01','perm_004',1,'pc_demo001','res_propadmin01','2026-06-20 06:00:00','2026-07-03 08:02:56'),('up_admin003','res_coo01','perm_005',1,'pc_demo001','res_propadmin01','2026-06-20 06:00:00','2026-07-03 08:02:56'),('up_admin004','res_sec01','perm_008',1,'pc_demo001','res_admin01','2026-06-01 02:00:00','2026-07-03 08:02:56'),('up_admin005','res_sec01','perm_006',1,'pc_demo001','res_admin01','2026-06-01 02:00:00','2026-07-03 08:02:56'),('up_admin006','res_courier01','perm_009',1,'pc_demo001','res_propadmin01','2026-06-05 01:00:00','2026-07-03 08:02:56'),('up_admin007','res_act01','perm_001',1,'pc_demo001','res_propadmin01','2026-06-10 02:00:00','2026-07-03 08:02:56'),('up_admin008','res_act01','perm_006',1,'pc_demo001','res_propadmin01','2026-06-10 02:00:00','2026-07-03 08:02:56'),('up_admin009','res_coo01','perm_002',1,'pc_demo001','res_pleader01','2026-06-25 08:00:00','2026-09-01 02:49:25');
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
/*!40000 ALTER TABLE `vote_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wallet_flows`
--

DROP TABLE IF EXISTS `wallet_flows`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wallet_flows` (
  `id` varchar(32) NOT NULL COMMENT '流水ID wf_xxxxxxxx',
  `owner_type` varchar(20) NOT NULL COMMENT '主体类型 merchant/platform/property/coordinator/sector_leader/individual_leader/courier',
  `owner_id` varchar(32) NOT NULL COMMENT '主体ID（platform 固定为 platform）',
  `order_id` varchar(32) DEFAULT NULL COMMENT '关联订单ID',
  `delivery_id` varchar(32) DEFAULT NULL COMMENT '关联配送单ID',
  `distribution_record_id` varchar(32) DEFAULT NULL COMMENT '关联分账记录ID',
  `entry_type` varchar(40) NOT NULL COMMENT '入账/冲正类型（规范§7.4）',
  `amount` decimal(12,2) NOT NULL COMMENT '正数入账，负数冲正/扣减',
  `balance_type` varchar(20) NOT NULL DEFAULT 'withdrawable' COMMENT 'pending/withdrawable/frozen/receivable',
  `idempotency_key` varchar(100) NOT NULL COMMENT '幂等键',
  `ref_record_id` varchar(32) DEFAULT NULL COMMENT '冲正流水关联的原流水ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wallet_flows_idempotency` (`idempotency_key`),
  KEY `idx_wallet_flows_owner` (`owner_type`,`owner_id`),
  KEY `idx_wallet_flows_order` (`order_id`),
  KEY `idx_wallet_flows_delivery` (`delivery_id`),
  KEY `idx_wallet_flows_record` (`distribution_record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='钱包流水账本（规范§7.4）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wallet_flows`
--

LOCK TABLES `wallet_flows` WRITE;
/*!40000 ALTER TABLE `wallet_flows` DISABLE KEYS */;
INSERT INTO `wallet_flows` VALUES ('wf_352164b70e1000','merchant','mch_demo002','ord_352164559e1000',NULL,'dr_352164b6fa1000','merchant_goods_income',0.09,'withdrawable','distribution:settle:ord_352164559e1000:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-13 21:32:38','2026-08-13 21:32:38'),('wf_352164b7241000','property','pc_demo001','ord_352164559e1000',NULL,'dr_352164b6fa1000','deficit_cost',-0.19,'withdrawable','distribution:settle:ord_352164559e1000:deficit_cost:property:pc_demo001',NULL,'2026-08-13 21:32:38','2026-08-13 21:32:38'),('wf_3521747c601000','courier','res_courier01','ord_352164559e1000','dlv_352164b6b41000',NULL,'courier_delivery_income',0.10,'withdrawable','delivery:settle:dlv_352164b6b41000',NULL,'2026-08-13 21:41:14','2026-08-13 21:41:14'),('wf_35266db3361000','merchant','mch_demo002','ord_35266bd4c41000',NULL,'dr_35266db31a1000','merchant_goods_income',0.09,'withdrawable','distribution:settle:ord_35266bd4c41000:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-14 09:16:35','2026-08-14 09:16:35'),('wf_35266db3601000','property','pc_demo001','ord_35266bd4c41000',NULL,'dr_35266db31a1000','property_product_share',0.01,'withdrawable','distribution:settle:ord_35266bd4c41000:property_product_share:property:pc_demo001',NULL,'2026-08-14 09:16:35','2026-08-14 09:16:35'),('wf_352670af6a1000','courier','res_courier01','ord_35266bd4c41000','dlv_35266db2e61000',NULL,'courier_delivery_income',0.10,'withdrawable','delivery:settle:dlv_35266db2e61000',NULL,'2026-08-14 09:18:13','2026-08-14 09:18:13'),('wf_35267c5c741000','merchant','mch_demo002','ord_35267afe501000',NULL,'dr_35267c5c701000','merchant_goods_income',0.09,'withdrawable','distribution:settle:ord_35267afe501000:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-14 09:24:36','2026-08-14 09:24:36'),('wf_35267c5c801000','property','pc_demo001','ord_35267afe501000',NULL,'dr_35267c5c701000','property_product_share',0.01,'withdrawable','distribution:settle:ord_35267afe501000:property_product_share:property:pc_demo001',NULL,'2026-08-14 09:24:36','2026-08-14 09:24:36'),('wf_35267d544e1000','merchant','mch_demo002','ord_35267afe501000',NULL,'dr_35267c5c701000','distribution_reversal',-0.09,'withdrawable','distribution:reverse:ord_35267afe501000:merchant_goods_income:merchant:mch_demo002','wf_35267c5c741000','2026-08-14 09:25:07','2026-08-14 09:25:07'),('wf_35267d54641000','property','pc_demo001','ord_35267afe501000',NULL,'dr_35267c5c701000','distribution_reversal',-0.01,'withdrawable','distribution:reverse:ord_35267afe501000:property_product_share:property:pc_demo001','wf_35267c5c801000','2026-08-14 09:25:07','2026-08-14 09:25:07'),('wf_35268cd9301000','merchant','mch_demo002','ord_35268adb481000',NULL,'dr_35268cd92a1000','merchant_goods_income',0.90,'withdrawable','distribution:settle:ord_35268adb481000:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-14 09:33:36','2026-08-14 09:33:36'),('wf_35268cd94c1000','platform','platform','ord_35268adb481000',NULL,'dr_35268cd92a1000','platform_product_share',0.01,'withdrawable','distribution:settle:ord_35268adb481000:platform_product_share:platform:platform',NULL,'2026-08-14 09:33:36','2026-08-14 09:33:36'),('wf_35268cd9501000','property','pc_demo001','ord_35268adb481000',NULL,'dr_35268cd92a1000','property_product_share',0.06,'withdrawable','distribution:settle:ord_35268adb481000:property_product_share:property:pc_demo001',NULL,'2026-08-14 09:33:36','2026-08-14 09:33:36'),('wf_35268cd9521000','coordinator','coo_001','ord_35268adb481000',NULL,'dr_35268cd92a1000','coordinator_share',0.01,'withdrawable','distribution:settle:ord_35268adb481000:coordinator_share:coordinator:coo_001',NULL,'2026-08-14 09:33:36','2026-08-14 09:33:36'),('wf_35268ddf261000','courier','res_courier01','ord_35268adb481000','dlv_35268cd9221000',NULL,'courier_delivery_income',0.10,'withdrawable','delivery:settle:dlv_35268cd9221000',NULL,'2026-08-14 09:34:09','2026-08-14 09:34:09'),('wf_3526aebc041000','merchant','mch_demo002','ord_3526ad6bfc1000',NULL,'dr_3526aebc001000','merchant_goods_income',0.09,'withdrawable','distribution:settle:ord_3526ad6bfc1000:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-14 09:52:06','2026-08-14 09:52:06'),('wf_3526aebc101000','platform','platform','ord_3526ad6bfc1000',NULL,'dr_3526aebc001000','platform_delivery_share',0.01,'withdrawable','distribution:settle:ord_3526ad6bfc1000:platform_delivery_share:platform:platform',NULL,'2026-08-14 09:52:06','2026-08-14 09:52:06'),('wf_3526aebc121000','property','pc_demo001','ord_3526ad6bfc1000',NULL,'dr_3526aebc001000','property_product_share',0.01,'withdrawable','distribution:settle:ord_3526ad6bfc1000:property_product_share:property:pc_demo001',NULL,'2026-08-14 09:52:06','2026-08-14 09:52:06'),('wf_3526b084921000','courier','res_courier01','ord_3526ad6bfc1000','dlv_3526aebbf41000',NULL,'courier_delivery_income',0.09,'withdrawable','delivery:settle:dlv_3526aebbf41000',NULL,'2026-08-14 09:53:05','2026-08-14 09:53:05'),('wf_3526f494ec1000','merchant','mch_demo002','ord_3526f46ef21000',NULL,'dr_3526f494e61000','merchant_goods_income',18.00,'withdrawable','distribution:settle:ord_3526f46ef21000:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-14 10:30:15','2026-08-14 10:30:15'),('wf_3526f494f41000','merchant','mch_demo002','ord_3526f46ef21000',NULL,'dr_3526f494e61000','merchant_delivery_subsidy',-0.10,'withdrawable','distribution:settle:ord_3526f46ef21000:merchant_delivery_subsidy:merchant:mch_demo002',NULL,'2026-08-14 10:30:15','2026-08-14 10:30:15'),('wf_3526f494f81000','platform','platform','ord_3526f46ef21000',NULL,'dr_3526f494e61000','platform_delivery_share',0.01,'withdrawable','distribution:settle:ord_3526f46ef21000:platform_delivery_share:platform:platform',NULL,'2026-08-14 10:30:15','2026-08-14 10:30:15'),('wf_3526f494fc1000','property','pc_demo001','ord_3526f46ef21000',NULL,'dr_3526f494e61000','deficit_cost',-18.00,'withdrawable','distribution:settle:ord_3526f46ef21000:deficit_cost:property:pc_demo001',NULL,'2026-08-14 10:30:15','2026-08-14 10:30:15'),('wf_3526f517e81000','courier','res_courier01','ord_3526f46ef21000','dlv_3526f494ca1000',NULL,'courier_delivery_income',0.09,'withdrawable','delivery:settle:dlv_3526f494ca1000',NULL,'2026-08-14 10:30:32','2026-08-14 10:30:32'),('wf_352869c8fe1000','merchant','mch_demo002','ord_35286996141000',NULL,'dr_352869c8f81000','merchant_goods_income',6.30,'withdrawable','distribution:settle:ord_35286996141000:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-14 13:54:04','2026-08-14 13:54:04'),('wf_352869c9021000','platform','platform','ord_35286996141000',NULL,'dr_352869c8f81000','platform_delivery_share',0.01,'withdrawable','distribution:settle:ord_35286996141000:platform_delivery_share:platform:platform',NULL,'2026-08-14 13:54:04','2026-08-14 13:54:04'),('wf_352869c9061000','property','pc_demo001','ord_35286996141000',NULL,'dr_352869c8f81000','deficit_cost',-6.40,'withdrawable','distribution:settle:ord_35286996141000:deficit_cost:property:pc_demo001',NULL,'2026-08-14 13:54:04','2026-08-14 13:54:04'),('wf_352870c58e1000','courier','res_courier01','ord_35286996141000','dlv_352869c8e81000',NULL,'courier_delivery_income',0.09,'withdrawable','delivery:settle:dlv_352869c8e81000',NULL,'2026-08-14 13:57:53','2026-08-14 13:57:53'),('wf_35288b08e61000','merchant','mch_demo002','ord_35288abea81000',NULL,'dr_35288b08e01000','merchant_goods_income',6.30,'withdrawable','distribution:settle:ord_35288abea81000:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-14 14:12:14','2026-08-14 14:12:14'),('wf_35288b08ee1000','platform','platform','ord_35288abea81000',NULL,'dr_35288b08e01000','platform_delivery_share',0.01,'withdrawable','distribution:settle:ord_35288abea81000:platform_delivery_share:platform:platform',NULL,'2026-08-14 14:12:14','2026-08-14 14:12:14'),('wf_35288b08f01000','property','pc_demo001','ord_35288abea81000',NULL,'dr_35288b08e01000','deficit_cost',-6.40,'withdrawable','distribution:settle:ord_35288abea81000:deficit_cost:property:pc_demo001',NULL,'2026-08-14 14:12:14','2026-08-14 14:12:14'),('wf_35288cfb7e1000','courier','res_courier01','ord_35288abea81000','dlv_35288b08d01000',NULL,'courier_delivery_income',0.09,'withdrawable','delivery:settle:dlv_35288b08d01000',NULL,'2026-08-14 14:13:17','2026-08-14 14:13:17'),('wf_354fc8ed4a1000','merchant','mch_demo002','ord_354fc89cc21000',NULL,'dr_354fc8ed341000','merchant_goods_income',6.30,'withdrawable','distribution:settle:ord_354fc89cc21000:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-18 09:38:37','2026-08-18 09:38:37'),('wf_354fc8ed801000','platform','platform','ord_354fc89cc21000',NULL,'dr_354fc8ed341000','platform_delivery_share',0.01,'withdrawable','distribution:settle:ord_354fc89cc21000:platform_delivery_share:platform:platform',NULL,'2026-08-18 09:38:37','2026-08-18 09:38:37'),('wf_354fc8ed861000','property','pc_demo001','ord_354fc89cc21000',NULL,'dr_354fc8ed341000','deficit_cost',-6.40,'withdrawable','distribution:settle:ord_354fc89cc21000:deficit_cost:property:pc_demo001',NULL,'2026-08-18 09:38:37','2026-08-18 09:38:37'),('wf_35511b52a81000','merchant','mch_demo001','ord_000007',NULL,'dr_35511b52801000','merchant_goods_income',247.69,'withdrawable','distribution:settle:ord_000007:merchant_goods_income:merchant:mch_demo001',NULL,'2026-08-18 12:43:26','2026-08-18 12:43:26'),('wf_35511b52be1000','platform','platform','ord_000007',NULL,'dr_35511b52801000','platform_product_share',4.37,'withdrawable','distribution:settle:ord_000007:platform_product_share:platform:platform',NULL,'2026-08-18 12:43:26','2026-08-18 12:43:26'),('wf_35511b52c61000','property','pc_demo001','ord_000007',NULL,'dr_35511b52801000','property_product_share',26.23,'withdrawable','distribution:settle:ord_000007:property_product_share:property:pc_demo001',NULL,'2026-08-18 12:43:26','2026-08-18 12:43:26'),('wf_35511b52ca1000','coordinator','coo_001','ord_000007',NULL,'dr_35511b52801000','coordinator_share',5.24,'withdrawable','distribution:settle:ord_000007:coordinator_share:coordinator:coo_001',NULL,'2026-08-18 12:43:26','2026-08-18 12:43:26'),('wf_35511b53be1000','merchant','mch_demo002','ord_000009',NULL,'dr_35511b53b81000','merchant_goods_income',72.00,'withdrawable','distribution:settle:ord_000009:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-18 12:43:26','2026-08-18 12:43:26'),('wf_35511b53d41000','property','pc_demo001','ord_000009',NULL,'dr_35511b53b81000','deficit_cost',-32.00,'withdrawable','distribution:settle:ord_000009:deficit_cost:property:pc_demo001',NULL,'2026-08-18 12:43:26','2026-08-18 12:43:26'),('wf_35511b543a1000','merchant','mch_demo001','ord_000037',NULL,'dr_35511b54361000','merchant_goods_income',193.37,'withdrawable','distribution:settle:ord_000037:merchant_goods_income:merchant:mch_demo001',NULL,'2026-08-18 12:43:26','2026-08-18 12:43:26'),('wf_35511b54461000','property','pc_demo001','ord_000037',NULL,'dr_35511b54361000','deficit_cost',-79.62,'withdrawable','distribution:settle:ord_000037:deficit_cost:property:pc_demo001',NULL,'2026-08-18 12:43:26','2026-08-18 12:43:26'),('wf_35511b54ee1000','merchant','mch_demo001','ord_000042',NULL,'dr_35511b54ea1000','merchant_goods_income',110.33,'withdrawable','distribution:settle:ord_000042:merchant_goods_income:merchant:mch_demo001',NULL,'2026-08-18 12:43:26','2026-08-18 12:43:26'),('wf_35511b54fe1000','platform','platform','ord_000042',NULL,'dr_35511b54ea1000','platform_product_share',1.95,'withdrawable','distribution:settle:ord_000042:platform_product_share:platform:platform',NULL,'2026-08-18 12:43:26','2026-08-18 12:43:26'),('wf_35511b55061000','property','pc_demo001','ord_000042',NULL,'dr_35511b54ea1000','property_product_share',11.68,'withdrawable','distribution:settle:ord_000042:property_product_share:property:pc_demo001',NULL,'2026-08-18 12:43:26','2026-08-18 12:43:26'),('wf_35511b550e1000','coordinator','coo_001','ord_000042',NULL,'dr_35511b54ea1000','coordinator_share',2.34,'withdrawable','distribution:settle:ord_000042:coordinator_share:coordinator:coo_001',NULL,'2026-08-18 12:43:26','2026-08-18 12:43:26'),('wf_35511b559c1000','merchant','mch_demo002','ord_000047',NULL,'dr_35511b55901000','merchant_goods_income',34.20,'withdrawable','distribution:settle:ord_000047:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-18 12:43:26','2026-08-18 12:43:26'),('wf_35511b55a81000','property','pc_demo001','ord_000047',NULL,'dr_35511b55901000','deficit_cost',-5.70,'withdrawable','distribution:settle:ord_000047:deficit_cost:property:pc_demo001',NULL,'2026-08-18 12:43:26','2026-08-18 12:43:26'),('wf_35511b56241000','merchant','mch_demo001','ord_000054',NULL,'dr_35511b561a1000','merchant_goods_income',343.82,'withdrawable','distribution:settle:ord_000054:merchant_goods_income:merchant:mch_demo001',NULL,'2026-08-18 12:43:26','2026-08-18 12:43:26'),('wf_35511b56321000','property','pc_demo001','ord_000054',NULL,'dr_35511b561a1000','deficit_cost',-141.57,'withdrawable','distribution:settle:ord_000054:deficit_cost:property:pc_demo001',NULL,'2026-08-18 12:43:26','2026-08-18 12:43:26'),('wf_35511b56a21000','merchant','mch_demo001','ord_000063',NULL,'dr_35511b569e1000','merchant_goods_income',343.99,'withdrawable','distribution:settle:ord_000063:merchant_goods_income:merchant:mch_demo001',NULL,'2026-08-18 12:43:26','2026-08-18 12:43:26'),('wf_35511b56aa1000','property','pc_demo001','ord_000063',NULL,'dr_35511b569e1000','deficit_cost',-141.64,'withdrawable','distribution:settle:ord_000063:deficit_cost:property:pc_demo001',NULL,'2026-08-18 12:43:26','2026-08-18 12:43:26'),('wf_35511b57461000','merchant','mch_demo002','ord_000078',NULL,'dr_35511b57401000','merchant_goods_income',75.60,'withdrawable','distribution:settle:ord_000078:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-18 12:43:26','2026-08-18 12:43:26'),('wf_35511b57501000','platform','platform','ord_000078',NULL,'dr_35511b57401000','platform_product_share',0.84,'withdrawable','distribution:settle:ord_000078:platform_product_share:platform:platform',NULL,'2026-08-18 12:43:27','2026-08-18 12:43:27'),('wf_35511b57561000','property','pc_demo001','ord_000078',NULL,'dr_35511b57401000','property_product_share',5.04,'withdrawable','distribution:settle:ord_000078:property_product_share:property:pc_demo001',NULL,'2026-08-18 12:43:27','2026-08-18 12:43:27'),('wf_35511b57601000','coordinator','coo_001','ord_000078',NULL,'dr_35511b57401000','coordinator_share',1.01,'withdrawable','distribution:settle:ord_000078:coordinator_share:coordinator:coo_001',NULL,'2026-08-18 12:43:27','2026-08-18 12:43:27'),('wf_35511b57d21000','merchant','mch_demo002','ord_000081',NULL,'dr_35511b57ce1000','merchant_goods_income',7.20,'withdrawable','distribution:settle:ord_000081:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-18 12:43:27','2026-08-18 12:43:27'),('wf_35511b57d81000','property','pc_demo001','ord_000081',NULL,'dr_35511b57ce1000','deficit_cost',-3.20,'withdrawable','distribution:settle:ord_000081:deficit_cost:property:pc_demo001',NULL,'2026-08-18 12:43:27','2026-08-18 12:43:27'),('wf_35511b583c1000','merchant','mch_demo002','ord_000084',NULL,'dr_35511b58341000','merchant_goods_income',95.40,'withdrawable','distribution:settle:ord_000084:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-18 12:43:27','2026-08-18 12:43:27'),('wf_35511b58461000','property','pc_demo001','ord_000084',NULL,'dr_35511b58341000','deficit_cost',-42.40,'withdrawable','distribution:settle:ord_000084:deficit_cost:property:pc_demo001',NULL,'2026-08-18 12:43:27','2026-08-18 12:43:27'),('wf_35511b58aa1000','merchant','mch_demo001','ord_000094',NULL,'dr_35511b58a61000','merchant_goods_income',15.30,'withdrawable','distribution:settle:ord_000094:merchant_goods_income:merchant:mch_demo001',NULL,'2026-08-18 12:43:27','2026-08-18 12:43:27'),('wf_35511b58b21000','property','pc_demo001','ord_000094',NULL,'dr_35511b58a61000','deficit_cost',-6.30,'withdrawable','distribution:settle:ord_000094:deficit_cost:property:pc_demo001',NULL,'2026-08-18 12:43:27','2026-08-18 12:43:27'),('wf_35511b590c1000','merchant','mch_demo002','ord_000109',NULL,'dr_35511b59081000','merchant_goods_income',58.50,'withdrawable','distribution:settle:ord_000109:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-18 12:43:27','2026-08-18 12:43:27'),('wf_35511b59121000','property','pc_demo001','ord_000109',NULL,'dr_35511b59081000','deficit_cost',-26.00,'withdrawable','distribution:settle:ord_000109:deficit_cost:property:pc_demo001',NULL,'2026-08-18 12:43:27','2026-08-18 12:43:27'),('wf_35511b59941000','merchant','mch_demo002','ord_000117',NULL,'dr_35511b59901000','merchant_goods_income',21.60,'withdrawable','distribution:settle:ord_000117:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-18 12:43:27','2026-08-18 12:43:27'),('wf_35511b599a1000','platform','platform','ord_000117',NULL,'dr_35511b59901000','platform_product_share',0.24,'withdrawable','distribution:settle:ord_000117:platform_product_share:platform:platform',NULL,'2026-08-18 12:43:27','2026-08-18 12:43:27'),('wf_35511b599e1000','property','pc_demo001','ord_000117',NULL,'dr_35511b59901000','property_product_share',1.44,'withdrawable','distribution:settle:ord_000117:property_product_share:property:pc_demo001',NULL,'2026-08-18 12:43:27','2026-08-18 12:43:27'),('wf_35511b59a61000','coordinator','coo_001','ord_000117',NULL,'dr_35511b59901000','coordinator_share',0.29,'withdrawable','distribution:settle:ord_000117:coordinator_share:coordinator:coo_001',NULL,'2026-08-18 12:43:27','2026-08-18 12:43:27'),('wf_355140d58a1000','merchant','mch_demo002','ord_35513f81481000',NULL,'dr_355140d5641000','merchant_goods_income',6.30,'withdrawable','distribution:settle:ord_35513f81481000:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-18 13:03:55','2026-08-18 13:03:55'),('wf_355140d5d61000','platform','platform','ord_35513f81481000',NULL,'dr_355140d5641000','platform_delivery_share',0.02,'withdrawable','distribution:settle:ord_35513f81481000:platform_delivery_share:platform:platform',NULL,'2026-08-18 13:03:55','2026-08-18 13:03:55'),('wf_355140d5e81000','property','pc_demo001','ord_35513f81481000',NULL,'dr_355140d5641000','deficit_cost',-6.50,'withdrawable','distribution:settle:ord_35513f81481000:deficit_cost:property:pc_demo001',NULL,'2026-08-18 13:03:55','2026-08-18 13:03:55'),('wf_355222cbba1000','merchant','mch_demo002','ord_3552221f381000',NULL,'dr_355222cba21000','merchant_goods_income',6.30,'withdrawable','distribution:settle:ord_3552221f381000:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-18 15:07:19','2026-08-18 15:07:19'),('wf_355222cbd01000','platform','platform','ord_3552221f381000',NULL,'dr_355222cba21000','platform_delivery_share',0.02,'withdrawable','distribution:settle:ord_3552221f381000:platform_delivery_share:platform:platform',NULL,'2026-08-18 15:07:19','2026-08-18 15:07:19'),('wf_355222cbd81000','property','pc_demo001','ord_3552221f381000',NULL,'dr_355222cba21000','deficit_cost',-6.50,'withdrawable','distribution:settle:ord_3552221f381000:deficit_cost:property:pc_demo001',NULL,'2026-08-18 15:07:19','2026-08-18 15:07:19'),('wf_3552582d461000','merchant','mch_demo002','ord_355220b31e1000',NULL,'dr_3552582d401000','merchant_goods_income',6.30,'withdrawable','distribution:settle:ord_355220b31e1000:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-18 15:36:29','2026-08-18 15:36:29'),('wf_3552582d4c1000','platform','platform','ord_355220b31e1000',NULL,'dr_3552582d401000','platform_delivery_share',0.02,'withdrawable','distribution:settle:ord_355220b31e1000:platform_delivery_share:platform:platform',NULL,'2026-08-18 15:36:29','2026-08-18 15:36:29'),('wf_3552582d521000','property','pc_demo001','ord_355220b31e1000',NULL,'dr_3552582d401000','deficit_cost',-6.50,'withdrawable','distribution:settle:ord_355220b31e1000:deficit_cost:property:pc_demo001',NULL,'2026-08-18 15:36:29','2026-08-18 15:36:29'),('wf_35853281321000','merchant','mch_demo002','ord_358531aa921000',NULL,'dr_35853281161000','merchant_goods_income',18.00,'withdrawable','distribution:settle:ord_358531aa921000:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-23 14:06:13','2026-08-23 14:06:13'),('wf_358532814c1000','merchant','mch_demo002','ord_358531aa921000',NULL,'dr_35853281161000','merchant_delivery_subsidy',-0.10,'withdrawable','distribution:settle:ord_358531aa921000:merchant_delivery_subsidy:merchant:mch_demo002',NULL,'2026-08-23 14:06:13','2026-08-23 14:06:13'),('wf_35853281521000','platform','platform','ord_358531aa921000',NULL,'dr_35853281161000','platform_delivery_share',0.01,'withdrawable','distribution:settle:ord_358531aa921000:platform_delivery_share:platform:platform',NULL,'2026-08-23 14:06:13','2026-08-23 14:06:13'),('wf_358532815a1000','property','pc_demo001','ord_358531aa921000',NULL,'dr_35853281161000','deficit_cost',-18.00,'withdrawable','distribution:settle:ord_358531aa921000:deficit_cost:property:pc_demo001',NULL,'2026-08-23 14:06:13','2026-08-23 14:06:13'),('wf_358534c5061000','merchant','mch_demo002','ord_358533a5ce1000',NULL,'dr_358534c5041000','merchant_goods_income',18.00,'withdrawable','distribution:settle:ord_358533a5ce1000:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-23 14:07:27','2026-08-23 14:07:27'),('wf_358534c50e1000','merchant','mch_demo002','ord_358533a5ce1000',NULL,'dr_358534c5041000','merchant_delivery_subsidy',-0.10,'withdrawable','distribution:settle:ord_358533a5ce1000:merchant_delivery_subsidy:merchant:mch_demo002',NULL,'2026-08-23 14:07:27','2026-08-23 14:07:27'),('wf_358534c5121000','platform','platform','ord_358533a5ce1000',NULL,'dr_358534c5041000','platform_delivery_share',0.01,'withdrawable','distribution:settle:ord_358533a5ce1000:platform_delivery_share:platform:platform',NULL,'2026-08-23 14:07:27','2026-08-23 14:07:27'),('wf_358534c5161000','property','pc_demo001','ord_358533a5ce1000',NULL,'dr_358534c5041000','deficit_cost',-18.00,'withdrawable','distribution:settle:ord_358533a5ce1000:deficit_cost:property:pc_demo001',NULL,'2026-08-23 14:07:27','2026-08-23 14:07:27'),('wf_358540c4021000','merchant','mch_demo002','ord_35854036621000',NULL,'dr_358540c4001000','merchant_goods_income',18.00,'withdrawable','distribution:settle:ord_35854036621000:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-23 14:14:00','2026-08-23 14:14:00'),('wf_358540c4121000','merchant','mch_demo002','ord_35854036621000',NULL,'dr_358540c4001000','merchant_delivery_subsidy',-0.10,'withdrawable','distribution:settle:ord_35854036621000:merchant_delivery_subsidy:merchant:mch_demo002',NULL,'2026-08-23 14:14:00','2026-08-23 14:14:00'),('wf_358540c4161000','platform','platform','ord_35854036621000',NULL,'dr_358540c4001000','platform_delivery_share',0.01,'withdrawable','distribution:settle:ord_35854036621000:platform_delivery_share:platform:platform',NULL,'2026-08-23 14:14:00','2026-08-23 14:14:00'),('wf_358540c41a1000','property','pc_demo001','ord_35854036621000',NULL,'dr_358540c4001000','deficit_cost',-18.00,'withdrawable','distribution:settle:ord_35854036621000:deficit_cost:property:pc_demo001',NULL,'2026-08-23 14:14:00','2026-08-23 14:14:00'),('wf_35860b0d241000','merchant','mch_demo002','ord_358609a9a81000',NULL,'dr_35860b0d1c1000','merchant_goods_income',18.00,'withdrawable','distribution:settle:ord_358609a9a81000:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-23 16:04:29','2026-08-23 16:04:29'),('wf_35860b0d2c1000','merchant','mch_demo002','ord_358609a9a81000',NULL,'dr_35860b0d1c1000','merchant_delivery_subsidy',-0.10,'withdrawable','distribution:settle:ord_358609a9a81000:merchant_delivery_subsidy:merchant:mch_demo002',NULL,'2026-08-23 16:04:29','2026-08-23 16:04:29'),('wf_35860b0d2e1000','platform','platform','ord_358609a9a81000',NULL,'dr_35860b0d1c1000','platform_delivery_share',0.01,'withdrawable','distribution:settle:ord_358609a9a81000:platform_delivery_share:platform:platform',NULL,'2026-08-23 16:04:29','2026-08-23 16:04:29'),('wf_35860b0d301000','property','pc_demo001','ord_358609a9a81000',NULL,'dr_35860b0d1c1000','deficit_cost',-18.00,'withdrawable','distribution:settle:ord_358609a9a81000:deficit_cost:property:pc_demo001',NULL,'2026-08-23 16:04:29','2026-08-23 16:04:29'),('wf_35862fef061000','merchant','mch_demo002','ord_358628e0fa1000',NULL,'dr_35862feee81000','merchant_goods_income',22.50,'withdrawable','distribution:settle:ord_358628e0fa1000:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-23 16:24:38','2026-08-23 16:24:38'),('wf_35862fef2a1000','merchant','mch_demo002','ord_358628e0fa1000',NULL,'dr_35862feee81000','merchant_delivery_subsidy',-0.10,'withdrawable','distribution:settle:ord_358628e0fa1000:merchant_delivery_subsidy:merchant:mch_demo002',NULL,'2026-08-23 16:24:38','2026-08-23 16:24:38'),('wf_35862fef3a1000','platform','platform','ord_358628e0fa1000',NULL,'dr_35862feee81000','platform_delivery_share',0.01,'withdrawable','distribution:settle:ord_358628e0fa1000:platform_delivery_share:platform:platform',NULL,'2026-08-23 16:24:38','2026-08-23 16:24:38'),('wf_35862fef4a1000','property','pc_demo001','ord_358628e0fa1000',NULL,'dr_35862feee81000','deficit_cost',-22.50,'withdrawable','distribution:settle:ord_358628e0fa1000:deficit_cost:property:pc_demo001',NULL,'2026-08-23 16:24:38','2026-08-23 16:24:38'),('wf_358630dee81000','merchant','mch_demo002','ord_358630648a1000',NULL,'dr_358630dee21000','merchant_goods_income',6.30,'withdrawable','distribution:settle:ord_358630648a1000:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-23 16:25:08','2026-08-23 16:25:08'),('wf_358630df0e1000','platform','platform','ord_358630648a1000',NULL,'dr_358630dee21000','platform_delivery_share',0.01,'withdrawable','distribution:settle:ord_358630648a1000:platform_delivery_share:platform:platform',NULL,'2026-08-23 16:25:08','2026-08-23 16:25:08'),('wf_358630df3e1000','property','pc_demo001','ord_358630648a1000',NULL,'dr_358630dee21000','deficit_cost',-6.40,'withdrawable','distribution:settle:ord_358630648a1000:deficit_cost:property:pc_demo001',NULL,'2026-08-23 16:25:08','2026-08-23 16:25:08'),('wf_35cc1093da1000','merchant','mch_demo002','ord_35839426361000',NULL,'dr_35cc1093881000','merchant_goods_income',18.00,'withdrawable','distribution:settle:ord_35839426361000:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-30 11:14:13','2026-08-30 11:14:13'),('wf_35cc1094961000','merchant','mch_demo002','ord_35839426361000',NULL,'dr_35cc1093881000','merchant_delivery_subsidy',-0.10,'withdrawable','distribution:settle:ord_35839426361000:merchant_delivery_subsidy:merchant:mch_demo002',NULL,'2026-08-30 11:14:13','2026-08-30 11:14:13'),('wf_35cc1094c41000','platform','platform','ord_35839426361000',NULL,'dr_35cc1093881000','platform_delivery_share',0.01,'withdrawable','distribution:settle:ord_35839426361000:platform_delivery_share:platform:platform',NULL,'2026-08-30 11:14:13','2026-08-30 11:14:13'),('wf_35cc1094ea1000','property','pc_demo001','ord_35839426361000',NULL,'dr_35cc1093881000','deficit_cost',-18.00,'withdrawable','distribution:settle:ord_35839426361000:deficit_cost:property:pc_demo001',NULL,'2026-08-30 11:14:13','2026-08-30 11:14:13'),('wf_35cd5a27821000','merchant','mch_demo002','ord_3585315b4c1000',NULL,'dr_35cd5a276c1000','merchant_goods_income',0.08,'withdrawable','distribution:settle:ord_3585315b4c1000:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-30 14:14:12','2026-08-30 14:14:12'),('wf_35cd5a27e01000','property','pc_demo001','ord_3585315b4c1000',NULL,'dr_35cd5a276c1000','deficit_cost',-0.08,'withdrawable','distribution:settle:ord_3585315b4c1000:deficit_cost:property:pc_demo001',NULL,'2026-08-30 14:14:12','2026-08-30 14:14:12'),('wf_35ce9460ce1000','merchant','mch_demo002','ord_35863441be1000',NULL,'dr_35ce9460801000','merchant_goods_income',0.08,'withdrawable','distribution:settle:ord_35863441be1000:merchant_goods_income:merchant:mch_demo002',NULL,'2026-08-30 17:05:49','2026-08-30 17:05:49'),('wf_35ce9461381000','property','pc_demo001','ord_35863441be1000',NULL,'dr_35ce9460801000','deficit_cost',-0.08,'withdrawable','distribution:settle:ord_35863441be1000:deficit_cost:property:pc_demo001',NULL,'2026-08-30 17:05:49','2026-08-30 17:05:49');
/*!40000 ALTER TABLE `wallet_flows` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wechat_split_receiver_accounts`
--

DROP TABLE IF EXISTS `wechat_split_receiver_accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wechat_split_receiver_accounts` (
  `id` varchar(32) NOT NULL,
  `receiver_account` varchar(64) NOT NULL COMMENT '分账接收方账户',
  `receiver_type` varchar(20) NOT NULL COMMENT 'MERCHANT_ID/PERSONAL_OPENID',
  `receiver_name_encrypted` text COMMENT '加密后的收款方姓名',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `account_name` varchar(128) DEFAULT NULL,
  `account_no` varchar(64) DEFAULT NULL,
  `currency` varchar(8) DEFAULT NULL,
  `merchant_no` varchar(64) DEFAULT NULL,
  `owner_id` varchar(32) NOT NULL,
  `owner_type` varchar(32) NOT NULL,
  `verified` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wechat_split_receiver_accounts`
--

LOCK TABLES `wechat_split_receiver_accounts` WRITE;
/*!40000 ALTER TABLE `wechat_split_receiver_accounts` DISABLE KEYS */;
/*!40000 ALTER TABLE `wechat_split_receiver_accounts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wechat_split_record`
--

DROP TABLE IF EXISTS `wechat_split_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wechat_split_record` (
  `id` varchar(32) NOT NULL,
  `order_id` varchar(32) NOT NULL COMMENT '关联订单ID',
  `out_trade_no` varchar(64) NOT NULL COMMENT '支付单号',
  `split_no` varchar(64) NOT NULL COMMENT '分账单号',
  `receiver_account` varchar(64) NOT NULL COMMENT '接收方账户',
  `amount` decimal(10,2) NOT NULL COMMENT '分账金额',
  `status` varchar(20) DEFAULT 'pending' COMMENT 'pending/processing/success/failed',
  `transaction_id` varchar(64) DEFAULT NULL COMMENT '微信分账单号',
  `error_msg` varchar(200) DEFAULT NULL COMMENT '失败原因',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `biz_type` varchar(32) DEFAULT NULL,
  `currency` varchar(8) DEFAULT NULL,
  `finish_resp` varchar(4000) DEFAULT NULL,
  `reverse_resp` text COMMENT '6.9冲正退回返回报文',
  `freeze_amount` decimal(10,2) DEFAULT NULL,
  `freeze_resp` varchar(4000) DEFAULT NULL,
  `group_no` varchar(32) DEFAULT NULL,
  `owner_id` varchar(32) DEFAULT NULL,
  `owner_type` varchar(32) DEFAULT NULL,
  `payee_account` varchar(64) DEFAULT NULL,
  `payer_account` varchar(64) DEFAULT NULL,
  `payment_record_id` varchar(32) DEFAULT NULL,
  `remark` varchar(4000) DEFAULT NULL,
  `wechat_out_trade_no` varchar(64) DEFAULT NULL,
  `wechat_sub_trade_no` varchar(64) DEFAULT NULL,
  `withdraw_status` varchar(32) DEFAULT NULL,
  `withdraw_resp` text COMMENT '6.4提现接口返回报文(JSON)',
  `withdraw_trade_no` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_out_trade_no` (`out_trade_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wechat_split_record`
--

LOCK TABLES `wechat_split_record` WRITE;
/*!40000 ALTER TABLE `wechat_split_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `wechat_split_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `withdrawal_disbursements`
--

DROP TABLE IF EXISTS `withdrawal_disbursements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `withdrawal_disbursements` (
  `id` varchar(32) NOT NULL COMMENT '主键 wd_xxxxxxxx',
  `withdrawal_id` varchar(32) NOT NULL COMMENT '关联提现单ID',
  `withdrawal_type` varchar(20) NOT NULL COMMENT 'merchant/role',
  `channel` varchar(20) NOT NULL COMMENT '打款渠道: auto_cbk',
  `amount` decimal(10,2) NOT NULL COMMENT '打款金额(实际到账)',
  `payee_type` varchar(20) DEFAULT NULL COMMENT '收款方类型: merchant/resident',
  `payee_id` varchar(32) DEFAULT NULL COMMENT '收款方ID',
  `settle_card_no` varchar(64) DEFAULT NULL COMMENT '收款结算卡号(脱敏存储)',
  `request_body` text COMMENT '请求报文(JSON)',
  `response_body` text COMMENT '响应报文(JSON)',
  `trade_no` varchar(64) DEFAULT NULL COMMENT '扫呗流水号',
  `status` varchar(20) NOT NULL COMMENT 'init/calling/success/failed',
  `error_msg` varchar(512) DEFAULT NULL COMMENT '失败原因',
  `retry_count` int NOT NULL DEFAULT '0' COMMENT '重试次数',
  `last_tried_at` datetime DEFAULT NULL COMMENT '末次尝试时间',
  `callback_at` datetime DEFAULT NULL COMMENT '异步到账回调时间',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_wd_withdrawal` (`withdrawal_id`,`withdrawal_type`),
  KEY `idx_wd_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='提现自动打款流水(出金调用全生命周期)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `withdrawal_disbursements`
--

LOCK TABLES `withdrawal_disbursements` WRITE;
/*!40000 ALTER TABLE `withdrawal_disbursements` DISABLE KEYS */;
/*!40000 ALTER TABLE `withdrawal_disbursements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `withdrawal_records`
--

DROP TABLE IF EXISTS `withdrawal_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `withdrawal_records` (
  `id` varchar(32) NOT NULL COMMENT 'wr_xxxxxxxx',
  `withdrawal_type` varchar(30) NOT NULL COMMENT '提现类型: individual_leader/courier',
  `applicant_id` varchar(32) NOT NULL COMMENT '申请人主体ID（个体负责人ID或快递负责人ID）',
  `resident_id` varchar(32) NOT NULL COMMENT '关联住户ID',
  `property_company_id` varchar(32) NOT NULL COMMENT '所属物业公司',
  `amount` decimal(10,2) NOT NULL COMMENT '提现金额',
  `fee_rate` decimal(5,4) NOT NULL COMMENT '手续费率',
  `fee_amount` decimal(10,2) NOT NULL COMMENT '手续费金额',
  `platform_fee_share` decimal(10,2) DEFAULT '0.00' COMMENT '平台管理员从提现手续费中的分成',
  `actual_amount` decimal(10,2) NOT NULL COMMENT '实际到账金额',
  `audit_status` varchar(20) DEFAULT 'pending' COMMENT 'pending/approved/rejected/completed',
  `auditor_id` varchar(32) DEFAULT NULL COMMENT '审核人ID',
  `audited_at` timestamp NULL DEFAULT NULL COMMENT '审核时间',
  `completed_at` timestamp NULL DEFAULT NULL COMMENT '完成时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `disburse_channel` varchar(20) DEFAULT NULL COMMENT '打款渠道: manual/auto_cbk',
  `disburse_trade_no` varchar(64) DEFAULT NULL COMMENT '扫呗打款流水号',
  `disburse_status` varchar(20) DEFAULT NULL COMMENT '打款状态: pending/success/failed',
  `disburse_at` datetime DEFAULT NULL COMMENT '打款完成时间',
  `disburse_remark` varchar(255) DEFAULT NULL COMMENT '打款失败原因/备注',
  `transfer_proof` varchar(512) DEFAULT NULL COMMENT '人工转账凭证URL',
  PRIMARY KEY (`id`),
  KEY `idx_applicant` (`withdrawal_type`,`applicant_id`),
  KEY `idx_resident` (`resident_id`),
  KEY `idx_property_company` (`property_company_id`),
  KEY `idx_status` (`audit_status`),
  KEY `idx_disburse_status` (`disburse_status`),
  CONSTRAINT `withdrawal_records_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`),
  CONSTRAINT `withdrawal_records_ibfk_2` FOREIGN KEY (`property_company_id`) REFERENCES `property_companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='多角色通用提现记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `withdrawal_records`
--

LOCK TABLES `withdrawal_records` WRITE;
/*!40000 ALTER TABLE `withdrawal_records` DISABLE KEYS */;
INSERT INTO `withdrawal_records` VALUES ('wr_3520d9a0ac1000','courier','cm_demo001','res_courier01','pc_demo001',0.01,0.0050,0.00,0.00,0.01,'completed','res_admin01','2026-08-13 13:08:04','2026-08-13 13:08:19','2026-08-13 12:16:40','2026-08-13 13:08:19','manual',NULL,'success',NULL,NULL,'https://proof.example.com/test.jpg');
/*!40000 ALTER TABLE `withdrawal_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'wuye_points'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-24 19:45:52
