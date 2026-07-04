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
  PRIMARY KEY (`id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_category` (`category`),
  CONSTRAINT `products_ibfk_1` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

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

-- Dump completed on 2026-07-03 14:56:30
