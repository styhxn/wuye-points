-- ============================================================
-- 管理端测试全覆盖数据
-- 覆盖各管理角色的列表、详情、统计、操作等场景
-- 用法: mysql -u wuye_app -p wuye_points < deploy-admin-test-data.sql
-- 前提: 已执行 deploy-database.sql（基础表+基础数据）
--       本脚本补充基础账号（如deploy-database.sql未执行）+ 管理端测试数据
-- ============================================================

USE wuye_points;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 0. 基础角色账号（deploy-database.sql中的核心账号）
--    如已执行deploy-database.sql，INSERT IGNORE会跳过
-- ============================================================

INSERT IGNORE INTO residents (id, openid, name, phone, password, role, user_type, property_company_id, community_id, building, unit, room, point_balance, coin_balance, status) VALUES
-- 平台管理员
('res_admin01', NULL, '平台管理员', '13800138000', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'platform_admin', 'owner', 'pc_demo001', NULL, NULL, NULL, NULL, 0, 0.00, 'active'),
-- 物业管理员
('res_propadmin01', NULL, '物业管理员-刘芳', '13900000001', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'property_admin', 'owner', 'pc_demo001', 'com_demo001', NULL, NULL, NULL, 0, 0.00, 'active'),
-- 统筹
('res_coo01', NULL, '统筹-张伟', '13900000002', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'coordinator', 'owner', 'pc_demo001', 'com_demo001', NULL, NULL, NULL, 1200, 150.00, 'active'),
-- 板块负责人
('res_sec01', NULL, '板块负责人-李娜', '13900000003', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'sector_leader', 'owner', 'pc_demo001', 'com_demo001', NULL, NULL, NULL, 800, 80.00, 'active'),
-- 个体负责人
('res_ind01', NULL, '个体负责人-王强', '13900000004', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'individual_leader', 'owner', 'pc_demo001', 'com_demo001', '1', '1', NULL, 500, 50.00, 'active'),
-- 业主1
('res_001', NULL, '业主-赵六', '13900000005', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'resident', 'owner', 'pc_demo001', 'com_demo001', '3', '2', '201', 3000, 200.00, 'active'),
-- 业主2
('res_002', NULL, '业主-钱七', '13900000006', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'resident', 'owner', 'pc_demo001', 'com_demo001', '3', '2', '202', 1500, 100.00, 'active'),
-- 业主3
('res_003', NULL, '业主-孙八', '13900000010', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'resident', 'owner', 'pc_demo001', 'com_demo002', '5', '1', '101', 2000, 150.00, 'active'),
-- 租户
('res_004', NULL, '租户-周九', '13900000007', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'resident', 'tenant', 'pc_demo001', 'com_demo001', '4', '3', '301', 500, 30.00, 'active'),
-- 商家
('res_mch01', NULL, '商家-陈老板', '13900000008', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'merchant', 'owner', 'pc_demo001', NULL, NULL, NULL, NULL, 0, 0.00, 'active'),
-- 快递员
('res_courier01', NULL, '快递员-吴十', '13900000009', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'courier', 'owner', 'pc_demo001', 'com_demo001', NULL, NULL, NULL, 200, 15.00, 'active'),
-- 活动组组长
('res_act01', NULL, '活动组长-郑美丽', '13900000011', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'activity_leader', 'owner', 'pc_demo001', 'com_demo001', '2', '1', '101', 600, 40.00, 'active'),
-- 技工
('res_tech01', NULL, '技工-刘师傅', '13900000012', '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS', 'technician', 'owner', 'pc_demo001', 'com_demo001', NULL, NULL, NULL, 300, 20.00, 'active');

-- 统筹/板块/个体负责人关联表
INSERT IGNORE INTO coordinators (id, resident_id, property_company_id, commission_rate, status) VALUES
('coo_001', 'res_coo01', 'pc_demo001', 0.7000, 'active');

INSERT IGNORE INTO sector_leaders (id, resident_id, property_company_id, sector, commission_rate, coordinator_id, status) VALUES
('sl_001', 'res_sec01', 'pc_demo001', 'cleaning_repair', 0.1000, 'coo_001', 'active'),
('sl_002', 'res_sec01', 'pc_demo001', 'life_service', 0.1000, 'coo_001', 'active');

INSERT IGNORE INTO individual_leaders (id, resident_id, property_company_id, sector_leader_id, sector, name, commission_rate, status) VALUES
('il_001', 'res_ind01', 'pc_demo001', 'sl_001', 'cleaning_repair', '王强家政', 0.0000, 'active');

-- 业主家庭（deploy-dev-test-data.sql依赖）
INSERT IGNORE INTO families (id, name, owner_id, property_company_id, community_id, building, unit, room, member_count, point_balance, coin_balance, audit_status, status) VALUES
('fam_001', '赵六家庭', 'res_001', 'pc_demo001', 'com_demo001', '3', '2', '201', 3, 5000, 300.00, 'approved', 'active'),
('fam_002', '钱七家庭', 'res_002', 'pc_demo001', 'com_demo001', '3', '2', '202', 2, 2500, 150.00, 'approved', 'active'),
('fam_003', '孙八家庭', 'res_003', 'pc_demo001', 'com_demo002', '5', '1', '101', 2, 3000, 200.00, 'approved', 'active');

UPDATE IGNORE residents SET family_id = 'fam_001', family_relation = 'owner' WHERE id = 'res_001';
UPDATE IGNORE residents SET family_id = 'fam_002', family_relation = 'owner' WHERE id = 'res_002';
UPDATE IGNORE residents SET family_id = 'fam_003', family_relation = 'owner' WHERE id = 'res_003';

-- ============================================================
-- 1. 补充业主/住户（管理端业主管理列表用）
-- ============================================================

INSERT IGNORE INTO residents (id, openid, name, phone, password, role, user_type, gender, age, marital_status, has_children, property_company_id, community_id, building, unit, room, family_id, family_relation, point_balance, coin_balance, status, created_at) VALUES
-- 已冻结住户
('res_admin_test01', NULL, '冻结住户-张三', '13911110001', NULL, 'resident', 'owner', 1, 45, 'married', TRUE, 'pc_demo001', 'com_demo001', '1', '1', '101', NULL, NULL, 1200, 80.00, 'frozen', '2026-01-15 10:00:00'),
-- 已禁用住户
('res_admin_test02', NULL, '禁用住户-李四', '13911110002', NULL, 'resident', 'tenant', 1, 30, 'single', FALSE, 'pc_demo001', 'com_demo001', '1', '2', '201', NULL, NULL, 0, 0.00, 'disabled', '2026-03-20 14:00:00'),
-- 待审核住户
('res_admin_test03', NULL, '待审核-王五', '13911110003', NULL, 'resident', 'owner', 2, 35, 'married', TRUE, 'pc_demo001', 'com_demo001', '2', '1', '102', NULL, NULL, 0, 0.00, 'active', '2026-07-03 09:00:00'),
-- 普通活跃住户
('res_admin_test04', NULL, '住户-赵小明', '13911110004', NULL, 'resident', 'owner', 1, 28, 'single', FALSE, 'pc_demo001', 'com_demo001', '2', '2', '202', NULL, NULL, 3500, 200.00, 'active', '2026-02-01 08:00:00'),
('res_admin_test05', NULL, '住户-钱小红', '13911110005', NULL, 'resident', 'owner', 2, 32, 'married', TRUE, 'pc_demo001', 'com_demo001', '4', '1', '301', NULL, NULL, 2800, 150.00, 'active', '2026-02-10 09:30:00'),
('res_admin_test06', NULL, '租户-孙小刚', '13911110006', NULL, 'resident', 'tenant', 1, 24, 'single', FALSE, 'pc_demo001', 'com_demo001', '4', '2', '302', NULL, NULL, 800, 30.00, 'active', '2026-03-01 10:00:00'),
-- 翠湖名苑住户
('res_admin_test07', NULL, '住户-周美丽', '13911110007', NULL, 'resident', 'owner', 2, 40, 'married', TRUE, 'pc_demo001', 'com_demo002', '1', '1', '101', NULL, NULL, 5000, 300.00, 'active', '2026-01-20 11:00:00'),
('res_admin_test08', NULL, '住户-吴大伟', '13911110008', NULL, 'resident', 'owner', 1, 50, 'married', TRUE, 'pc_demo001', 'com_demo002', '2', '2', '202', NULL, NULL, 1800, 90.00, 'active', '2026-04-05 14:00:00');

-- ============================================================
-- 2. 补充家庭
-- ============================================================

INSERT IGNORE INTO families (id, name, owner_id, property_company_id, community_id, building, unit, room, member_count, point_balance, coin_balance, audit_status, status) VALUES
('fam_admin001', '赵小明家庭', 'res_admin_test04', 'pc_demo001', 'com_demo001', '2', '2', '202', 1, 3500, 200.00, 'approved', 'active'),
('fam_admin002', '钱小红家庭', 'res_admin_test05', 'pc_demo001', 'com_demo001', '4', '1', '301', 3, 2800, 150.00, 'approved', 'active'),
('fam_admin003', '周美丽家庭', 'res_admin_test07', 'pc_demo001', 'com_demo002', '1', '1', '101', 4, 5000, 300.00, 'approved', 'active'),
('fam_admin004', '吴大伟家庭', 'res_admin_test08', 'pc_demo001', 'com_demo002', '2', '2', '202', 2, 1800, 90.00, 'pending', 'active');

-- 关联家庭成员
UPDATE residents SET family_id = 'fam_admin001', family_relation = 'owner' WHERE id = 'res_admin_test04';
UPDATE residents SET family_id = 'fam_admin002', family_relation = 'owner' WHERE id = 'res_admin_test05';
UPDATE residents SET family_id = 'fam_admin003', family_relation = 'owner' WHERE id = 'res_admin_test07';
UPDATE residents SET family_id = 'fam_admin004', family_relation = 'owner' WHERE id = 'res_admin_test08';

-- ============================================================
-- 3. 更多订单（覆盖各种状态）
-- ============================================================

INSERT IGNORE INTO orders (id, order_no, resident_id, merchant_id, property_company_id, family_id, order_status, total_amount, delivery_fee, payment_method, point_used, coin_used, cash_amount, point_earned, coin_earned, address, contact_phone, created_at, completed_at, cancelled_at, cancel_reason) VALUES
-- 已取消订单
('ord_admin001', 'ORD20260601001', 'res_admin_test04', 'mch_demo002', 'pc_demo001', 'fam_admin001', 'cancelled', 45.00, 0.00, 'wechat', 0, 0.00, 45.00, 0, 0.00, '华帝·阳光花园2栋202', '13911110004', '2026-06-01 10:00:00', NULL, '2026-06-01 10:30:00', '用户主动取消'),
('ord_admin002', 'ORD20260615001', 'res_admin_test05', 'mch_demo001', 'pc_demo001', 'fam_admin002', 'cancelled', 68.00, 2.00, 'wechat', 0, 0.00, 68.00, 0, 0.00, '华帝·阳光花园4栋301', '13911110005', '2026-06-15 12:00:00', NULL, '2026-06-15 12:20:00', '商家库存不足'),
-- 退款中
('ord_admin003', 'ORD20260620001', 'res_admin_test04', 'mch_demo003', 'pc_demo001', 'fam_admin001', 'refunding', 128.00, 3.00, 'wechat', 0, 0.00, 128.00, 0, 0.00, '华帝·阳光花园2栋202', '13911110004', '2026-06-20 09:00:00', NULL, NULL, '商品质量问题'),
-- 已退款
('ord_admin004', 'ORD20260625001', 'res_admin_test05', 'mch_demo002', 'pc_demo001', 'fam_admin002', 'refunded', 35.00, 0.00, 'property_coin', 0, 35.00, 0.00, 0, 0.00, '华帝·阳光花园4栋301', '13911110005', '2026-06-25 14:00:00', NULL, '2026-06-26 10:00:00', '发错商品已退款'),
-- 待支付
('ord_admin005', 'ORD20260703001', 'res_admin_test04', 'mch_demo001', 'pc_demo001', 'fam_admin001', 'pending', 56.00, 2.00, NULL, 0, 0.00, 0.00, 0, 0.00, '华帝·阳光花园2栋202', '13911110004', '2026-07-03 08:00:00', NULL, NULL, NULL),
-- 配送中
('ord_admin006', 'ORD20260703002', 'res_admin_test05', 'mch_demo002', 'pc_demo001', 'fam_admin002', 'delivering', 89.50, 0.00, 'wechat', 0, 0.00, 89.50, 500, 5.00, '华帝·阳光花园4栋301', '13911110005', '2026-07-03 11:00:00', NULL, NULL, NULL),
-- 翠湖名苑订单
('ord_admin007', 'ORD20260701002', 'res_admin_test07', 'mch_demo001', 'pc_demo001', 'fam_admin003', 'completed', 72.00, 2.00, 'wechat', 0, 0.00, 72.00, 400, 4.00, '华帝·翠湖名苑1栋101', '13911110007', '2026-07-01 11:30:00', '2026-07-01 12:30:00', NULL, NULL),
('ord_admin008', 'ORD20260702002', 'res_admin_test08', 'mch_demo003', 'pc_demo001', 'fam_admin004', 'completed', 95.00, 3.00, 'mixed', 300, 10.00, 82.00, 600, 6.00, '华帝·翠湖名苑2栋202', '13911110008', '2026-07-02 15:00:00', '2026-07-02 16:00:00', NULL, NULL);

INSERT IGNORE INTO order_items (id, order_id, product_id, product_name, quantity, unit_price, total_price) VALUES
('oi_admin001', 'ord_admin001', 'prd_006', '纸巾套装(3包)', 3, 15.00, 45.00),
('oi_admin002', 'ord_admin002', 'prd_001', '宫保鸡丁', 1, 28.00, 28.00),
('oi_admin003', 'ord_admin002', 'prd_005', '蛋炒饭', 2, 12.00, 24.00),
('oi_admin004', 'ord_admin003', 'prd_009', '有机小白菜(500g)', 5, 8.00, 40.00),
('oi_admin005', 'ord_admin003', 'prd_010', '土鸡蛋(30枚)', 2, 38.00, 76.00),
('oi_admin006', 'ord_admin004', 'prd_007', '洗衣液(2kg)', 1, 39.90, 39.90),
('oi_admin007', 'ord_admin005', 'prd_002', '红烧肉', 1, 35.00, 35.00),
('oi_admin008', 'ord_admin005', 'prd_003', '东北拉皮', 1, 18.00, 18.00),
('oi_admin009', 'ord_admin006', 'prd_006', '纸巾套装(3包)', 3, 15.00, 45.00),
('oi_admin010', 'ord_admin006', 'prd_008', '矿泉水(24瓶)', 2, 28.00, 56.00),
('oi_admin011', 'ord_admin007', 'prd_004', '酸菜白肉血肠', 1, 48.00, 48.00),
('oi_admin012', 'ord_admin007', 'prd_005', '蛋炒饭', 2, 12.00, 24.00),
('oi_admin013', 'ord_admin008', 'prd_009', '有机小白菜(500g)', 3, 8.00, 24.00),
('oi_admin014', 'ord_admin008', 'prd_010', '土鸡蛋(30枚)', 1, 38.00, 38.00),
('oi_admin015', 'ord_admin008', 'prd_011', '有机西红柿(1kg)', 2, 12.00, 24.00);

-- 配送中订单的配送记录
INSERT IGNORE INTO deliveries (id, order_id, courier_id, property_company_id, status, fee, courier_earning, pickup_address, delivery_address, contact_phone, created_at) VALUES
('dlv_admin001', 'ord_admin006', 'res_courier01', 'pc_demo001', 'delivering', 0.00, 0.00, '便民超市(华帝店)', '华帝·阳光花园4栋301', '13911110005', '2026-07-03 11:00:00');

-- ============================================================
-- 4. 分成记录（管理端分成数据查看用）
-- ============================================================

INSERT IGNORE INTO distribution_records (id, order_id, merchant_id, property_company_id, resident_id, total_amount, commission_rate, point_cost, coin_cost, distributable_amount, property_share, coordinator_share, sector_leader_share, individual_leader_share, coordinator_id, sector_leader_id, individual_leader_id, created_at) VALUES
('dr_admin001', 'ord_dev001', 'mch_demo002', 'pc_demo001', 'res_dev001', 88.00, 0.1000, 0.00, 5.00, 3.80, 1.14, 2.66, 0.00, 0.00, 'coo_001', NULL, NULL, '2026-07-01 10:00:00'),
('dr_admin002', 'ord_dev003', 'mch_demo003', 'pc_demo001', 'res_dev001', 126.50, 0.2000, 0.00, 8.00, 17.30, 5.19, 12.11, 0.00, 0.00, 'coo_001', NULL, NULL, '2026-07-03 10:30:00'),
('dr_admin003', 'ord_dev006', 'mch_demo003', 'pc_demo001', 'res_dev001', 98.00, 0.2000, 0.00, 5.00, 14.60, 4.38, 10.22, 0.00, 0.00, 'coo_001', NULL, NULL, '2026-07-06 11:00:00'),
('dr_admin004', 'ord_admin007', 'mch_demo001', 'pc_demo001', 'res_admin_test07', 72.00, 0.1500, 0.00, 4.00, 6.80, 2.04, 4.76, 0.00, 0.00, 'coo_001', NULL, NULL, '2026-07-01 12:30:00'),
('dr_admin005', 'ord_admin008', 'mch_demo003', 'pc_demo001', 'res_admin_test08', 95.00, 0.2000, 3.00, 6.00, 10.00, 3.00, 7.00, 0.00, 0.00, 'coo_001', NULL, NULL, '2026-07-02 16:00:00');

-- ============================================================
-- 5. 操作日志（管理端日志查看用）
-- ============================================================

INSERT IGNORE INTO operation_logs (id, operator_id, property_company_id, module, action, target_type, target_id, before_data, after_data, created_at) VALUES
-- 用户管理操作
('log_admin001', 'res_propadmin01', 'pc_demo001', 'user', 'create', 'resident', 'res_admin_test03', NULL, '{"status": "active", "role": "resident"}', '2026-07-03 09:00:00'),
('log_admin002', 'res_propadmin01', 'pc_demo001', 'user', 'freeze', 'resident', 'res_admin_test01', '{"status": "active"}', '{"status": "frozen"}', '2026-06-15 14:00:00'),
('log_admin003', 'res_propadmin01', 'pc_demo001', 'user', 'disable', 'resident', 'res_admin_test02', '{"status": "active"}', '{"status": "disabled"}', '2026-06-20 10:30:00'),
('log_admin004', 'res_propadmin01', 'pc_demo001', 'user', 'update', 'resident', 'res_001', '{"name": "赵六", "phone": "13900000005"}', '{"name": "赵六", "phone": "13900000005", "building": "3"}', '2026-06-25 11:00:00'),
-- 商家管理操作
('log_admin005', 'res_propadmin01', 'pc_demo001', 'merchant', 'audit_approve', 'merchant', 'mch_demo001', '{"audit_status": "pending_audit"}', '{"audit_status": "approved"}', '2026-06-10 09:00:00'),
('log_admin006', 'res_propadmin01', 'pc_demo001', 'merchant', 'update', 'merchant', 'mch_demo002', '{"delivery_fee": 2.00}', '{"delivery_fee": 0.00}', '2026-06-18 15:00:00'),
-- 公告管理
('log_admin007', 'res_propadmin01', 'pc_demo001', 'announcement', 'publish', 'announcement', 'ann_001', NULL, '{"title": "关于2026年7月物业费缴纳通知"}', '2026-07-01 09:00:00'),
('log_admin008', 'res_coo01', 'pc_demo001', 'announcement', 'publish', 'announcement', 'ann_003', NULL, '{"title": "社区夏令营活动报名开始"}', '2026-07-02 10:00:00'),
-- 积分池操作
('log_admin009', 'res_admin01', 'pc_demo001', 'pool', 'adjust', 'point_pool', 'pp_demo001', '{"balance": 40000.00}', '{"balance": 50000.00}', '2026-07-01 00:00:00'),
-- 物业费操作
('log_admin010', 'res_propadmin01', 'pc_demo001', 'property_fee', 'create', 'property_fee', 'pf_001', NULL, '{"amount": 360.00, "period": "2026-Q3"}', '2026-06-30 08:00:00'),
-- 权限操作
('log_admin011', 'res_propadmin01', 'pc_demo001', 'permission', 'grant', 'resident', 'res_coo01', NULL, '{"permission": "announcement.publish"}', '2026-06-20 14:00:00'),
('log_admin012', 'res_admin01', 'pc_demo001', 'permission', 'apply_preset', 'resident', 'res_sec01', NULL, '{"preset": "sector_leader"}', '2026-06-01 10:00:00'),
-- 特惠推送
('log_admin013', 'res_mch01', 'pc_demo001', 'promotion', 'publish', 'special_offer', 'so_dev002', NULL, '{"title": "口福餐厅限时特惠"}', '2026-07-05 00:00:00'),
-- 活动组管理
('log_admin014', 'res_act01', 'pc_demo001', 'activity_group', 'create', 'activity_group', 'ag_dev001', NULL, '{"name": "太极拳晨练组"}', '2026-06-01 08:00:00'),
('log_admin015', 'res_act01', 'pc_demo001', 'activity_group', 'update', 'activity_group', 'ag_dev001', '{"member_count": 20}', '{"member_count": 28}', '2026-06-28 10:00:00');

-- ============================================================
-- 6. 用户权限分配（管理端权限管理用）
-- ============================================================

INSERT IGNORE INTO user_permissions (id, resident_id, permission_id, granted, property_company_id, operator_id, created_at) VALUES
('up_admin001', 'res_coo01', 'perm_001', TRUE, 'pc_demo001', 'res_propadmin01', '2026-06-20 14:00:00'),
('up_admin002', 'res_coo01', 'perm_004', TRUE, 'pc_demo001', 'res_propadmin01', '2026-06-20 14:00:00'),
('up_admin003', 'res_coo01', 'perm_005', TRUE, 'pc_demo001', 'res_propadmin01', '2026-06-20 14:00:00'),
('up_admin004', 'res_sec01', 'perm_008', TRUE, 'pc_demo001', 'res_admin01', '2026-06-01 10:00:00'),
('up_admin005', 'res_sec01', 'perm_006', TRUE, 'pc_demo001', 'res_admin01', '2026-06-01 10:00:00'),
('up_admin006', 'res_courier01', 'perm_009', TRUE, 'pc_demo001', 'res_propadmin01', '2026-06-05 09:00:00'),
('up_admin007', 'res_act01', 'perm_001', TRUE, 'pc_demo001', 'res_propadmin01', '2026-06-10 10:00:00'),
('up_admin008', 'res_act01', 'perm_006', TRUE, 'pc_demo001', 'res_propadmin01', '2026-06-10 10:00:00'),
-- 被取消的权限
('up_admin009', 'res_coo01', 'perm_002', FALSE, 'pc_demo001', 'res_propadmin01', '2026-06-25 16:00:00');

-- ============================================================
-- 7. 服务订单 & 订阅
-- ============================================================

INSERT IGNORE INTO service_orders (id, service_id, resident_id, provider_id, property_company_id, order_status, amount, payment_method, service_time, address, contact_phone, remark, created_at) VALUES
('svc_ord_admin001', 'svc_001', 'res_admin_test04', 'res_ind01', 'pc_demo001', 'completed', 65.00, 'wechat', '2026-06-15 09:00:00', '华帝·阳光花园2栋202', '13911110004', '全屋深度清洁', '2026-06-14 18:00:00'),
('svc_ord_admin002', 'svc_003', 'res_admin_test05', NULL, 'pc_demo001', 'completed', 40.00, 'wechat', '2026-06-20 14:00:00', '华帝·阳光花园4栋301', '13911110005', '冰箱维修', '2026-06-19 16:00:00'),
('svc_ord_admin003', 'svc_005', 'res_admin_test04', NULL, 'pc_demo001', 'pending', 50.00, 'wechat', '2026-07-05 10:00:00', '华帝·阳光花园2栋202', '13911110004', '宠物寄养1天', '2026-07-03 09:00:00'),
('svc_ord_admin004', 'svc_002', 'res_admin_test07', NULL, 'pc_demo001', 'pending', 120.00, 'wechat', '2026-07-06 14:00:00', '华帝·翠湖名苑1栋101', '13911110007', '老人陪护2小时', '2026-07-04 16:00:00');

INSERT IGNORE INTO service_subscriptions (id, service_id, resident_id, property_company_id, sub_type, amount, start_date, end_date, status, auto_renew, created_at) VALUES
('ss_admin001', 'svc_002', 'res_admin_test07', 'pc_demo001', 'monthly', 120.00, '2026-07-01', '2026-07-31', 'active', TRUE, '2026-07-01 10:00:00'),
('ss_admin002', 'svc_001', 'res_admin_test04', 'pc_demo001', 'monthly', 65.00, '2026-06-01', '2026-06-30', 'expired', FALSE, '2026-06-01 10:00:00');

-- ============================================================
-- 8. 配送规则
-- ============================================================

INSERT IGNORE INTO delivery_rules (id, name, base_fee, per_kg_fee, free_threshold, max_weight, max_distance, courier_share_rate, courier_per_order, timeout_minutes, property_company_id, status) VALUES
('dlv_rule001', '华帝配送规则', 2.00, 0.50, 20.00, 50.00, 5.00, 1.0000, 0.35, 30, 'pc_demo001', 'active');

-- ============================================================
-- 9. 物业币冻结/解冻记录
-- ============================================================

INSERT IGNORE INTO coin_freeze_records (id, resident_id, property_company_id, action, amount, reason, operator_id, created_at) VALUES
('cfr_admin001', 'res_admin_test01', 'pc_demo001', 'freeze', 80.00, '账户异常，冻结物业币', 'res_propadmin01', '2026-06-15 14:00:00'),
('cfr_admin002', 'res_admin_test01', 'pc_demo001', 'unfreeze', 80.00, '账户已核实，解冻物业币', 'res_propadmin01', '2026-06-20 10:00:00');

-- ============================================================
-- 10. 商家积分购买 & 录入
-- ============================================================

INSERT IGNORE INTO merchant_point_purchases (id, merchant_id, property_company_id, point_amount, pay_amount, audit_status, audited_at, auditor_id, remaining_points, created_at) VALUES
('mpp_admin001', 'mch_demo001', 'pc_demo001', 5000, 50.00, 'approved', '2026-06-01 10:00:00', 'res_admin01', 2000, '2026-06-01 09:00:00'),
('mpp_admin002', 'mch_demo002', 'pc_demo001', 10000, 50.00, 'approved', '2026-06-05 10:00:00', 'res_admin01', 5000, '2026-06-05 09:00:00'),
('mpp_admin003', 'mch_demo003', 'pc_demo001', 3000, 60.00, 'pending', NULL, NULL, 3000, '2026-07-03 08:00:00');

INSERT IGNORE INTO merchant_point_grants (id, merchant_id, purchase_id, resident_id, family_id, property_company_id, point_amount, exchange_rate, description, created_at) VALUES
('mpg_admin001', 'mch_demo001', 'mpp_admin001', 'res_dev001', 'fam_dev001', 'pc_demo001', 500, 100.0000, '口福餐厅消费赠积分', '2026-06-15 12:00:00'),
('mpg_admin002', 'mch_demo001', 'mpp_admin001', 'res_001', 'fam_001', 'pc_demo001', 300, 100.0000, '口福餐厅消费赠积分', '2026-06-20 18:00:00'),
('mpg_admin003', 'mch_demo002', 'mpp_admin002', 'res_dev001', 'fam_dev001', 'pc_demo001', 200, 200.0000, '便民超市消费赠积分', '2026-06-25 16:00:00');

-- ============================================================
-- 11. 商家提现 & 物业币兑换现金
-- ============================================================

INSERT IGNORE INTO merchant_withdrawals (id, merchant_id, property_company_id, amount, fee_rate, fee_amount, actual_amount, audit_status, audited_at, auditor_id, created_at) VALUES
('mw_admin001', 'mch_demo001', 'pc_demo001', 1000.00, 0.0060, 6.00, 994.00, 'approved', '2026-06-20 11:00:00', 'res_admin01', '2026-06-19 10:00:00'),
('mw_admin002', 'mch_demo002', 'pc_demo001', 500.00, 0.0060, 3.00, 497.00, 'pending', NULL, NULL, '2026-07-02 14:00:00'),
('mw_admin003', 'mch_demo003', 'pc_demo001', 2000.00, 0.0060, 12.00, 1988.00, 'rejected', '2026-07-01 09:00:00', 'res_admin01', '2026-06-30 10:00:00');

INSERT IGNORE INTO coin_withdrawals (id, merchant_id, property_company_id, coin_amount, cash_amount, fee_rate, fee_amount, actual_amount, audit_status, created_at) VALUES
('cw_admin001', 'mch_demo001', 'pc_demo001', 500.00, 5.00, 0.0060, 0.03, 4.97, 'approved', '2026-06-25 10:00:00'),
('cw_admin002', 'mch_demo002', 'pc_demo001', 200.00, 2.00, 0.0060, 0.01, 1.99, 'pending', '2026-07-03 09:00:00');

-- ============================================================
-- 12. 公告信息收集
-- ============================================================

INSERT IGNORE INTO announcement_collects (id, announcement_id, resident_id, collected_data, created_at) VALUES
('ac_admin001', 'ann_003', 'res_dev001', '{"name": "张阿姨", "phone": "13800138000", "child_name": "张子涵", "child_age": 8, "course": "书法"}', '2026-07-02 14:00:00'),
('ac_admin002', 'ann_003', 'res_admin_test04', '{"name": "赵小明", "phone": "13911110004", "child_name": "赵小宝", "child_age": 6, "course": "编程"}', '2026-07-02 15:30:00'),
('ac_admin003', 'ann_003', 'res_admin_test05', '{"name": "钱小红", "phone": "13911110005", "child_name": "钱朵朵", "child_age": 10, "course": "绘画"}', '2026-07-02 16:00:00');

-- ============================================================
-- 13. 更多物业费账单（历史欠费、逾期等）
-- ============================================================

INSERT IGNORE INTO property_fees (id, resident_id, family_id, property_company_id, community_id, fee_type, period, amount, paid_amount, point_deducted, coin_deducted, payment_status, due_date, paid_at, late_fee) VALUES
-- 逾期账单
('pf_admin001', 'res_admin_test04', 'fam_admin001', 'pc_demo001', 'com_demo001', 'property', '2026-Q1', 360.00, 180.00, 0, 0.00, 'overdue', '2026-03-31', NULL, 15.00),
('pf_admin002', 'res_admin_test04', 'fam_admin001', 'pc_demo001', 'com_demo001', 'parking', '2026-Q1', 200.00, 0.00, 0, 0.00, 'overdue', '2026-03-31', NULL, 8.50),
-- 已缴历史
('pf_admin003', 'res_admin_test05', 'fam_admin002', 'pc_demo001', 'com_demo001', 'property', '2026-Q1', 360.00, 360.00, 200, 0.00, 'paid', '2026-03-31', '2026-03-20 10:00:00', 0.00),
('pf_admin004', 'res_admin_test05', 'fam_admin002', 'pc_demo001', 'com_demo001', 'property', '2026-Q2', 360.00, 360.00, 0, 50.00, 'paid', '2026-06-30', '2026-06-15 10:00:00', 0.00),
-- 待缴
('pf_admin005', 'res_admin_test05', 'fam_admin002', 'pc_demo001', 'com_demo001', 'parking', '2026-Q3', 200.00, 0.00, 0, 0.00, 'unpaid', '2026-07-31', NULL, 0.00),
('pf_admin006', 'res_admin_test07', 'fam_admin003', 'pc_demo001', 'com_demo002', 'property', '2026-Q3', 300.00, 100.00, 0, 0.00, 'partial', '2026-07-31', '2026-07-01 10:00:00', 0.00);

-- ============================================================
-- 14. 更多积分池记录
-- ============================================================

INSERT IGNORE INTO point_pool_records (id, property_company_id, pool_type, amount, balance, source, description, created_at) VALUES
('ppr_admin001', 'pc_demo001', 'pool_out', 500.00, 49500.00, 'spend', '积分兑换商品支出', '2026-06-15 10:00:00'),
('ppr_admin002', 'pc_demo001', 'pool_in', 2000.00, 51500.00, 'exchange_diff', '商家积分购买注入', '2026-06-20 14:00:00'),
('ppr_admin003', 'pc_demo001', 'pool_out', 800.00, 50700.00, 'fee_clear', '物业费积分抵扣结算', '2026-06-30 23:59:59');

-- ============================================================
-- 15. 邻居私聊限制记录
-- ============================================================

INSERT IGNORE INTO neighbor_contact_limits (id, resident_id, property_company_id, contact_date, contact_count) VALUES
('ncl_admin001', 'res_dev001', 'pc_demo001', '2026-07-03', 1),
('ncl_admin002', 'res_admin_test04', 'pc_demo001', '2026-07-02', 2);

-- ============================================================
-- 16. 更多公告（管理端列表翻页用）
-- ============================================================

INSERT IGNORE INTO announcements (id, title, content, announcement_type, property_company_id, community_id, publisher_id, publisher_role, status, published_at) VALUES
('ann_admin001', '2026年7月垃圾分类宣传', '垃圾分类，人人有责。请各位业主将生活垃圾分类投放至指定垃圾桶。', 'community', 'pc_demo001', 'com_demo001', 'res_propadmin01', 'property_admin', 'published', '2026-07-01 10:00:00'),
('ann_admin002', '物业费缴费优惠活动', '凡在7月15日前缴纳Q3物业费的业主，可享受积分双倍奖励！', 'property', 'pc_demo001', NULL, 'res_propadmin01', 'property_admin', 'published', '2026-07-01 08:00:00'),
('ann_admin003', '关于小区绿化改造的通知', '小区将于7月10日至20日进行绿化升级改造，施工期间请注意安全。', 'property', 'pc_demo001', 'com_demo001', 'res_propadmin01', 'property_admin', 'draft', NULL),
('ann_admin004', '翠湖名苑电梯检修通知', '翠湖名苑将于7月8日进行电梯年度检修，届时将分时段停运。', 'community', 'pc_demo001', 'com_demo002', 'res_propadmin01', 'property_admin', 'published', '2026-07-02 09:00:00'),
('ann_admin005', '社区便民服务日', '本周末社区中心将举办便民服务日活动，免费理发、义诊等。', 'activity', 'pc_demo001', NULL, 'res_coo01', 'coordinator', 'archived', '2026-06-15 08:00:00');

-- ============================================================
-- 17. 更多消息通知
-- ============================================================

INSERT IGNORE INTO messages (id, from_id, to_id, message_type, content, chat_type, property_company_id, read_status, created_at) VALUES
('msg_admin001', 'res_propadmin01', 'res_admin_test04', 'property', '您所在的楼栋将于明日进行供水管道检修，请提前储水。', 'broadcast', 'pc_demo001', 'unread', '2026-07-03 08:00:00'),
('msg_admin002', 'res_admin01', 'res_admin_test05', 'system', '系统升级通知：7月5日凌晨2:00-4:00进行系统维护，届时部分功能可能无法使用。', 'broadcast', 'pc_demo001', 'read', '2026-07-02 16:00:00'),
('msg_admin003', 'res_coo01', 'res_admin_test07', 'coordinator', '翠湖名苑业主满意度调查开启，请积极参与。', 'broadcast', 'pc_demo001', 'unread', '2026-07-03 10:00:00'),
('msg_admin004', 'res_mch01', 'res_admin_test04', 'resident', '您的订单已开始配送，预计30分钟内送达。', 'private', 'pc_demo001', 'read', '2026-07-03 11:00:00');

-- ============================================================
-- 18. 更多特惠推送
-- ============================================================

INSERT IGNORE INTO special_offers (id, title, content, target_type, publisher_id, publisher_role, property_company_id, community_id, merchant_id, discount_info, start_time, end_time, status) VALUES
('so_admin001', '家政清洁服务8折', '社区家政服务限时8折优惠，专业清洁团队上门服务', 'all', 'res_ind01', 'individual_leader', 'pc_demo001', 'com_demo001', NULL, '全场8折', '2026-07-01 00:00:00', '2026-07-15 23:59:59', 'published'),
('so_admin002', '暑期托管班招生', '社区暑期儿童托管班开始报名，物业币可抵扣50%', 'all', 'res_coo01', 'coordinator', 'pc_demo001', NULL, NULL, '物业币抵扣50%', '2026-07-05 00:00:00', '2026-07-20 23:59:59', 'draft');

-- ============================================================
-- 19. 更多活动组
-- ============================================================

INSERT IGNORE INTO activity_groups (id, name, description, leader_id, property_company_id, community_id, member_count, monthly_fee, yearly_fee, status) VALUES
('ag_admin001', '广场舞队', '每晚7点中心广场广场舞，欢迎加入', 'res_admin_test04', 'pc_demo001', 'com_demo001', 35, 0.00, 0.00, 'active'),
('ag_admin002', '围棋社', '每周六下午围棋交流活动', 'res_admin_test07', 'pc_demo001', 'com_demo002', 18, 20.00, 200.00, 'active'),
('ag_admin003', '摄影爱好者', '社区摄影交流群，定期组织外拍', 'res_act01', 'pc_demo001', 'com_demo001', 42, 0.00, 0.00, 'active'),
('ag_admin004', '闲置活动组', '已废弃的活动组', 'res_act01', 'pc_demo001', 'com_demo001', 0, 0.00, 0.00, 'inactive');

-- ============================================================
-- 20. 物业币过期清零记录
-- ============================================================

INSERT IGNORE INTO coin_expiry_records (id, resident_id, property_company_id, expired_amount, pool_in_amount, expired_at, created_at) VALUES
('cer_admin001', 'res_admin_test01', 'pc_demo001', 80.00, 80.00, '2026-06-30 23:59:59', '2026-06-30 23:59:59'),
('cer_admin002', 'res_admin_test04', 'pc_demo001', 120.00, 120.00, '2026-03-31 23:59:59', '2026-03-31 23:59:59'),
('cer_admin003', 'res_002', 'pc_demo001', 50.00, 50.00, '2026-06-30 23:59:59', '2026-06-30 23:59:59');

-- ============================================================
-- 完成
-- ============================================================
-- 管理端测试数据已添加
-- 覆盖的管理端角色（全部10个）：
--   平台管理员: res_admin01 (13800138000 / admin123456)
--   物业管理员: res_propadmin01 (13900000001 / admin123456)
--   统筹: res_coo01 (13900000002 / admin123456)
--   板块负责人: res_sec01 (13900000003 / admin123456)
--   个体负责人: res_ind01 (13900000004 / admin123456)
--   商家: res_mch01 (13900000008 / admin123456)
--   快递员: res_courier01 (13900000009 / admin123456)
--   活动组长: res_act01 (13900000011 / admin123456)
--   技工: res_tech01 (13900000012 / admin123456)
--   业主: res_001~004 + res_dev001~004
-- ============================================================

SET FOREIGN_KEY_CHECKS = 1;