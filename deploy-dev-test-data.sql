-- ============================================================
-- dev_13800000001 测试账号全覆盖数据
-- 用法: mysql -u wuye_app -p wuye_points < deploy-dev-test-data.sql
-- 前提: 已执行 deploy-database.sql
-- ============================================================

USE wuye_points;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. 测试用户（openid: dev_openid_13800000001）
-- ============================================================

-- 清理已存在的同名 openid 记录，避免 INSERT IGNORE 因唯一键冲突静默跳过
DELETE FROM residents WHERE openid = 'dev_openid_13800000001';
DELETE FROM residents WHERE openid IN ('dev_openid_13800000002','dev_openid_13800000003','dev_openid_13800000004');
DELETE FROM families WHERE id = 'fam_dev001';

INSERT IGNORE INTO residents (
    id, openid, name, phone, role, user_type,
    gender, age, marital_status, has_children,
    property_company_id, community_id, building, unit, room,
    family_id, family_relation,
    point_balance, coin_balance, status
) VALUES (
    'res_dev001',
    'dev_openid_13800000001',
    '张阿姨',
    NULL,
    'resident',
    'owner',
    2, 58, 'married', TRUE,
    'pc_demo001', 'com_demo001', '3', '12', '1201',
    'fam_dev001', 'owner',
    12480, 350.50,
    'active'
);

-- ============================================================
-- 2. 家庭
-- ============================================================

INSERT IGNORE INTO families (
    id, name, owner_id, property_company_id, community_id,
    building, unit, room, member_count,
    point_balance, coin_balance, vote_weight, audit_status, status
) VALUES (
    'fam_dev001', '张阿姨家庭', 'res_dev001', 'pc_demo001', 'com_demo001',
    '3', '12', '1201', 4,
    12480, 350.50, 1, 'approved', 'active'
);

-- 家庭成员
INSERT IGNORE INTO residents (
    id, openid, name, role, user_type,
    gender, age, marital_status, has_children,
    property_company_id, community_id, building, unit, room,
    family_id, family_relation,
    point_balance, coin_balance, status
) VALUES
('res_dev002', 'dev_openid_13800000002', '张建设', 'resident', 'owner', 1, 60, 'married', TRUE, 'pc_demo001', 'com_demo001', '3', '12', '1201', 'fam_dev001', 'spouse', 5616, 120.00, 'active'),
('res_dev003', 'dev_openid_13800000003', '张子涵', 'resident', 'owner', 1, 28, 'single', FALSE, 'pc_demo001', 'com_demo001', '3', '12', '1201', 'fam_dev001', 'child', 2246, 80.00, 'active'),
('res_dev004', 'dev_openid_13800000004', '王秀兰', 'resident', 'owner', 2, 82, 'widowed', FALSE, 'pc_demo001', 'com_demo001', '3', '12', '1201', 'fam_dev001', 'parent', 624, 50.00, 'active');

-- ============================================================
-- 3. 积分记录（覆盖各种来源）
-- ============================================================

INSERT IGNORE INTO point_records (id, resident_id, family_id, property_company_id, point_type, amount, balance, source, source_id, description, created_at) VALUES
-- 消费订单
('pt_dev001', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 500, 500, 'order', NULL, '万达超市·日用品消费', '2026-06-01 14:30:00'),
('pt_dev002', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 200, 700, 'order', NULL, '幸福食堂·午餐消费', '2026-06-05 12:15:00'),
('pt_dev003', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 100, 800, 'order', NULL, '阳光补习·数学课', '2026-06-08 18:00:00'),
('pt_dev004', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 800, 1600, 'order', NULL, '家电购买奖励', '2026-06-12 14:00:00'),
('pt_dev005', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 300, 1900, 'order', NULL, '有机蔬菜组合购买', '2026-06-15 09:30:00'),
-- 签到
('pt_dev006', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 5, 1905, 'sign_in', NULL, '每日签到', '2026-06-16 08:00:00'),
('pt_dev007', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 5, 1910, 'sign_in', NULL, '每日签到', '2026-06-17 08:00:00'),
('pt_dev008', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 5, 1915, 'sign_in', NULL, '每日签到', '2026-06-18 08:00:00'),
('pt_dev009', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 5, 1920, 'sign_in', NULL, '每日签到', '2026-06-19 08:00:00'),
('pt_dev010', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 5, 1925, 'sign_in', NULL, '每日签到', '2026-06-20 08:00:00'),
('pt_dev011', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 5, 1930, 'sign_in', NULL, '每日签到', '2026-06-21 08:00:00'),
('pt_dev012', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 5, 1935, 'sign_in', NULL, '每日签到', '2026-06-22 08:00:00'),
-- 活动奖励
('pt_dev013', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 500, 2435, 'activity', NULL, '社区志愿者活动奖励', '2026-06-23 16:30:00'),
('pt_dev014', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 400, 2835, 'activity', NULL, '满意度问卷调查完成', '2026-06-24 09:12:00'),
-- 物业费奖励
('pt_dev015', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 1500, 4335, 'order', NULL, '缴纳物业费奖励', '2026-06-25 14:20:00'),
-- 兑换扣除
('pt_dev016', 'res_dev001', 'fam_dev001', 'pc_demo001', 'spend', -850, 3485, 'fee_deduct', NULL, '礼品兑换 - 品牌大米', '2026-06-26 10:45:00'),
-- 积分兑换物业币
('pt_dev017', 'res_dev001', 'fam_dev001', 'pc_demo001', 'spend', -3200, 285, 'pool_diff', NULL, '积分兑换物业币', '2026-06-27 11:00:00'),
-- 更多消费
('pt_dev018', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 2000, 2285, 'order', NULL, '大额订单消费', '2026-06-28 18:00:00'),
('pt_dev019', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 500, 2785, 'order', NULL, '日用品消费', '2026-06-29 15:00:00'),
('pt_dev020', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 300, 3085, 'order', NULL, '生鲜配送', '2026-06-30 10:00:00'),
-- 7月记录
('pt_dev021', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 500, 3585, 'order', NULL, '社区团购订单', '2026-07-01 09:00:00'),
('pt_dev022', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 200, 3785, 'sign_in', NULL, '连续签到奖励', '2026-07-01 08:00:00'),
('pt_dev023', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 800, 4585, 'order', NULL, '家电维修服务', '2026-07-02 14:00:00'),
('pt_dev024', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 500, 5085, 'order', NULL, '超市购物', '2026-07-02 16:00:00'),
('pt_dev025', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 100, 5185, 'order', NULL, '食堂午餐', '2026-07-02 12:00:00'),
-- 更多消费
('pt_dev026', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 300, 5485, 'order', NULL, '洗衣服务', '2026-07-03 09:00:00'),
('pt_dev027', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 200, 5685, 'order', NULL, '宠物美容', '2026-07-03 10:30:00'),
('pt_dev028', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 500, 6185, 'order', NULL, '早教课程', '2026-07-03 14:00:00'),
('pt_dev029', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 300, 6485, 'order', NULL, '家政服务', '2026-07-03 15:30:00'),
('pt_dev030', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 1000, 7485, 'order', NULL, '大家电购买', '2026-07-04 11:00:00'),
('pt_dev031', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 500, 7985, 'order', NULL, '超市大采购', '2026-07-04 16:00:00'),
('pt_dev032', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 200, 8185, 'order', NULL, '外卖订单', '2026-07-04 18:30:00'),
('pt_dev033', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 100, 8285, 'order', NULL, '饮品订单', '2026-07-05 10:00:00'),
('pt_dev034', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 300, 8585, 'order', NULL, '社区活动报名', '2026-07-05 14:00:00'),
('pt_dev035', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 200, 8785, 'order', NULL, '便利店购物', '2026-07-05 17:00:00'),
('pt_dev036', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 500, 9285, 'order', NULL, '健身卡续费', '2026-07-06 09:00:00'),
('pt_dev037', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 300, 9585, 'order', NULL, '瑜伽课程', '2026-07-06 15:00:00'),
('pt_dev038', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 200, 9785, 'order', NULL, '水果采购', '2026-07-06 17:30:00'),
('pt_dev039', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 500, 10285, 'order', NULL, '教育培训', '2026-07-07 10:00:00'),
('pt_dev040', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 300, 10585, 'order', NULL, '美容美发', '2026-07-07 14:00:00'),
('pt_dev041', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 200, 10785, 'order', NULL, '鲜花订购', '2026-07-07 16:00:00'),
('pt_dev042', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 500, 11285, 'order', NULL, '家电清洗', '2026-07-08 09:00:00'),
('pt_dev043', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 300, 11585, 'order', NULL, '搬家服务', '2026-07-08 11:00:00'),
('pt_dev044', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 200, 11785, 'order', NULL, '干洗服务', '2026-07-08 14:00:00'),
('pt_dev045', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 300, 12085, 'order', NULL, '宠物寄养', '2026-07-09 09:00:00'),
('pt_dev046', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 200, 12285, 'order', NULL, '鲜花配送', '2026-07-09 14:00:00'),
('pt_dev047', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 195, 12480, 'order', NULL, '最后一笔消费', '2026-07-10 10:00:00'),
-- 物业费抵扣
('pt_dev048', 'res_dev001', 'fam_dev001', 'pc_demo001', 'spend', -500, 11980, 'fee_deduct', NULL, 'Q1物业费积分抵扣', '2026-03-15 10:00:00'),
('pt_dev049', 'res_dev001', 'fam_dev001', 'pc_demo001', 'spend', -500, 11480, 'fee_deduct', NULL, 'Q2物业费积分抵扣', '2026-06-15 10:00:00');

-- ============================================================
-- 4. 物业币记录（覆盖各种来源）
-- ============================================================

INSERT IGNORE INTO property_coin_records (id, resident_id, family_id, property_company_id, coin_type, amount, balance, source, description, created_at) VALUES
('coin_dev001', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 50.00, 50.00, 'order', '万达超市消费返币', '2026-06-01 14:30:00'),
('coin_dev002', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 30.00, 80.00, 'order', '幸福食堂消费返币', '2026-06-05 12:15:00'),
('coin_dev003', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 20.00, 100.00, 'order', '阳光补习消费返币', '2026-06-08 18:00:00'),
('coin_dev004', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 100.00, 200.00, 'order', '家电购买返币', '2026-06-12 14:00:00'),
('coin_dev005', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 50.00, 250.00, 'order', '有机蔬菜购买返币', '2026-06-15 09:30:00'),
('coin_dev006', 'res_dev001', 'fam_dev001', 'pc_demo001', 'spend', -50.00, 200.00, 'order', '外卖订单抵扣', '2026-06-20 18:00:00'),
('coin_dev007', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 80.00, 280.00, 'order', '超市购物返币', '2026-06-25 16:00:00'),
('coin_dev008', 'res_dev001', 'fam_dev001', 'pc_demo001', 'spend', -30.00, 250.00, 'order', '日用品抵扣', '2026-06-28 15:00:00'),
('coin_dev009', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 50.00, 300.00, 'order', '社区团购返币', '2026-07-01 09:00:00'),
('coin_dev010', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 30.50, 330.50, 'order', '生鲜配送返币', '2026-07-02 10:00:00'),
('coin_dev011', 'res_dev001', 'fam_dev001', 'pc_demo001', 'earn', 20.00, 350.50, 'order', '便利店返币', '2026-07-03 17:00:00');

-- ============================================================
-- 5. 物业费账单（历史+当前）
-- ============================================================

INSERT IGNORE INTO property_fees (id, resident_id, family_id, property_company_id, community_id, fee_type, period, amount, paid_amount, point_deducted, coin_deducted, payment_status, due_date, paid_at) VALUES
-- 已缴账单
('pf_dev001', 'res_dev001', 'fam_dev001', 'pc_demo001', 'com_demo001', 'property', '2025-Q1', 360.00, 360.00, 0, 0.00, 'paid', '2025-03-31', '2025-03-20 10:00:00'),
('pf_dev002', 'res_dev001', 'fam_dev001', 'pc_demo001', 'com_demo001', 'property', '2025-Q2', 360.00, 360.00, 0, 0.00, 'paid', '2025-06-30', '2025-06-15 10:00:00'),
('pf_dev003', 'res_dev001', 'fam_dev001', 'pc_demo001', 'com_demo001', 'property', '2025-Q3', 360.00, 360.00, 500, 0.00, 'paid', '2025-09-30', '2025-09-15 10:00:00'),
('pf_dev004', 'res_dev001', 'fam_dev001', 'pc_demo001', 'com_demo001', 'property', '2025-Q4', 360.00, 360.00, 0, 0.00, 'paid', '2025-12-31', '2025-12-15 10:00:00'),
('pf_dev005', 'res_dev001', 'fam_dev001', 'pc_demo001', 'com_demo001', 'property', '2026-Q1', 360.00, 360.00, 500, 0.00, 'paid', '2026-03-31', '2026-03-15 10:00:00'),
('pf_dev006', 'res_dev001', 'fam_dev001', 'pc_demo001', 'com_demo001', 'property', '2026-Q2', 360.00, 360.00, 500, 0.00, 'paid', '2026-06-30', '2026-06-15 10:00:00'),
('pf_dev007', 'res_dev001', 'fam_dev001', 'pc_demo001', 'com_demo001', 'parking', '2025-Q1', 200.00, 200.00, 0, 0.00, 'paid', '2025-03-31', '2025-03-20 10:00:00'),
('pf_dev008', 'res_dev001', 'fam_dev001', 'pc_demo001', 'com_demo001', 'parking', '2025-Q2', 200.00, 200.00, 0, 0.00, 'paid', '2025-06-30', '2025-06-15 10:00:00'),
('pf_dev009', 'res_dev001', 'fam_dev001', 'pc_demo001', 'com_demo001', 'parking', '2025-Q3', 200.00, 200.00, 0, 0.00, 'paid', '2025-09-30', '2025-09-15 10:00:00'),
('pf_dev010', 'res_dev001', 'fam_dev001', 'pc_demo001', 'com_demo001', 'parking', '2025-Q4', 200.00, 200.00, 0, 0.00, 'paid', '2025-12-31', '2025-12-15 10:00:00'),
('pf_dev011', 'res_dev001', 'fam_dev001', 'pc_demo001', 'com_demo001', 'parking', '2026-Q1', 200.00, 200.00, 0, 0.00, 'paid', '2026-03-31', '2026-03-15 10:00:00'),
('pf_dev012', 'res_dev001', 'fam_dev001', 'pc_demo001', 'com_demo001', 'parking', '2026-Q2', 200.00, 200.00, 0, 0.00, 'paid', '2026-06-30', '2026-06-15 10:00:00'),
-- 待缴账单
('pf_dev013', 'res_dev001', 'fam_dev001', 'pc_demo001', 'com_demo001', 'property', '2026-Q3', 360.00, 0.00, 0, 0.00, 'unpaid', '2026-07-31', NULL),
('pf_dev014', 'res_dev001', 'fam_dev001', 'pc_demo001', 'com_demo001', 'parking', '2026-Q3', 200.00, 0.00, 0, 0.00, 'unpaid', '2026-07-31', NULL),
('pf_dev015', 'res_dev001', 'fam_dev001', 'pc_demo001', 'com_demo001', 'water', '2026-07', 85.50, 0.00, 0, 0.00, 'unpaid', '2026-07-31', NULL),
('pf_dev016', 'res_dev001', 'fam_dev001', 'pc_demo001', 'com_demo001', 'electricity', '2026-07', 156.80, 0.00, 0, 0.00, 'unpaid', '2026-07-31', NULL);

-- ============================================================
-- 5.5 商家-物业挂接配置（支付/分成必需）
-- ============================================================

INSERT IGNORE INTO merchant_property_configs (id, merchant_id, property_company_id, commission_rate, point_exchange_rate, member_discount_price, coin_rebate_rate, coin_rebate_enabled, status) VALUES
('mpc_001', 'mch_demo001', 'pc_demo001', 0.1500, 100.0000, NULL, 0.0200, TRUE, 'active'),
('mpc_002', 'mch_demo002', 'pc_demo001', 0.1000, 200.0000, NULL, 0.0100, TRUE, 'active'),
('mpc_003', 'mch_demo003', 'pc_demo001', 0.2000, 50.0000, NULL, 0.0300, TRUE, 'active');

-- ============================================================
-- 6. 商家订单（模拟消费记录）
-- ============================================================

INSERT IGNORE INTO orders (id, order_no, resident_id, merchant_id, property_company_id, family_id, order_status, total_amount, delivery_fee, payment_method, point_used, coin_used, cash_amount, point_earned, coin_earned, address, contact_phone, created_at, completed_at) VALUES
('ord_dev001', 'ORD20260701001', 'res_dev001', 'mch_demo002', 'pc_demo001', 'fam_dev001', 'completed', 88.00, 0.00, 'wechat', 0, 0.00, 88.00, 500, 5.00, '华帝·阳光花园3栋1201', '13800138000', '2026-07-01 09:00:00', '2026-07-01 10:00:00'),
('ord_dev002', 'ORD20260702001', 'res_dev001', 'mch_demo001', 'pc_demo001', 'fam_dev001', 'completed', 63.00, 2.00, 'property_coin', 0, 20.00, 41.00, 300, 3.00, '华帝·阳光花园3栋1201', '13800138000', '2026-07-02 12:00:00', '2026-07-02 13:00:00'),
('ord_dev003', 'ORD20260703001', 'res_dev001', 'mch_demo003', 'pc_demo001', 'fam_dev001', 'completed', 126.50, 3.00, 'mixed', 200, 10.00, 113.50, 800, 8.00, '华帝·阳光花园3栋1201', '13800138000', '2026-07-03 09:30:00', '2026-07-03 10:30:00'),
('ord_dev004', 'ORD20260704001', 'res_dev001', 'mch_demo002', 'pc_demo001', 'fam_dev001', 'completed', 156.00, 0.00, 'wechat', 0, 0.00, 156.00, 1000, 10.00, '华帝·阳光花园3栋1201', '13800138000', '2026-07-04 16:00:00', '2026-07-04 17:00:00'),
('ord_dev005', 'ORD20260705001', 'res_dev001', 'mch_demo001', 'pc_demo001', 'fam_dev001', 'completed', 45.00, 2.00, 'property_coin', 0, 10.00, 33.00, 200, 2.00, '华帝·阳光花园3栋1201', '13800138000', '2026-07-05 18:00:00', '2026-07-05 19:00:00'),
('ord_dev006', 'ORD20260706001', 'res_dev001', 'mch_demo003', 'pc_demo001', 'fam_dev001', 'completed', 98.00, 3.00, 'wechat', 0, 0.00, 98.00, 500, 5.00, '华帝·阳光花园3栋1201', '13800138000', '2026-07-06 10:00:00', '2026-07-06 11:00:00'),
('ord_dev007', 'ORD20260707001', 'res_dev001', 'mch_demo002', 'pc_demo001', 'fam_dev001', 'completed', 238.00, 0.00, 'wechat', 0, 0.00, 238.00, 1500, 15.00, '华帝·阳光花园3栋1201', '13800138000', '2026-07-07 14:00:00', '2026-07-07 15:00:00'),
('ord_dev008', 'ORD20260708001', 'res_dev001', 'mch_demo001', 'pc_demo001', 'fam_dev001', 'completed', 82.00, 2.00, 'mixed', 100, 5.00, 74.50, 300, 3.00, '华帝·阳光花园3栋1201', '13800138000', '2026-07-08 12:00:00', '2026-07-08 13:00:00'),
('ord_dev009', 'ORD20260709001', 'res_dev001', 'mch_demo003', 'pc_demo001', 'fam_dev001', 'completed', 68.00, 3.00, 'wechat', 0, 0.00, 68.00, 200, 2.00, '华帝·阳光花园3栋1201', '13800138000', '2026-07-09 09:00:00', '2026-07-09 10:00:00'),
('ord_dev010', 'ORD20260710001', 'res_dev001', 'mch_demo002', 'pc_demo001', 'fam_dev001', 'pending', 199.00, 0.00, 'wechat', 0, 0.00, 199.00, 0, 0.00, '华帝·阳光花园3栋1201', '13800138000', '2026-07-10 10:00:00', NULL);

INSERT IGNORE INTO order_items (id, order_id, product_id, product_name, quantity, unit_price, total_price) VALUES
('oi_dev001', 'ord_dev001', 'prd_006', '纸巾套装(3包)', 2, 15.00, 30.00),
('oi_dev002', 'ord_dev001', 'prd_007', '洗衣液(2kg)', 1, 39.90, 39.90),
('oi_dev003', 'ord_dev001', 'prd_008', '矿泉水(24瓶)', 1, 28.00, 28.00),
('oi_dev004', 'ord_dev002', 'prd_001', '宫保鸡丁', 1, 28.00, 28.00),
('oi_dev005', 'ord_dev002', 'prd_002', '红烧肉', 1, 35.00, 35.00),
('oi_dev006', 'ord_dev003', 'prd_009', '有机小白菜(500g)', 3, 8.00, 24.00),
('oi_dev007', 'ord_dev003', 'prd_010', '土鸡蛋(30枚)', 2, 38.00, 76.00),
('oi_dev008', 'ord_dev003', 'prd_011', '有机西红柿(1kg)', 2, 12.00, 24.00),
('oi_dev009', 'ord_dev004', 'prd_006', '纸巾套装(3包)', 5, 15.00, 75.00),
('oi_dev010', 'ord_dev004', 'prd_007', '洗衣液(2kg)', 2, 39.90, 79.80),
('oi_dev011', 'ord_dev005', 'prd_003', '东北拉皮', 1, 18.00, 18.00),
('oi_dev012', 'ord_dev005', 'prd_005', '蛋炒饭', 2, 12.00, 24.00),
('oi_dev013', 'ord_dev006', 'prd_009', '有机小白菜(500g)', 5, 8.00, 40.00),
('oi_dev014', 'ord_dev006', 'prd_011', '有机西红柿(1kg)', 4, 12.00, 48.00),
('oi_dev015', 'ord_dev007', 'prd_006', '纸巾套装(3包)', 10, 15.00, 150.00),
('oi_dev016', 'ord_dev007', 'prd_007', '洗衣液(2kg)', 2, 39.90, 79.80),
('oi_dev017', 'ord_dev008', 'prd_004', '酸菜白肉血肠', 1, 48.00, 48.00),
('oi_dev018', 'ord_dev008', 'prd_001', '宫保鸡丁', 1, 28.00, 28.00),
('oi_dev019', 'ord_dev009', 'prd_009', '有机小白菜(500g)', 3, 8.00, 24.00),
('oi_dev020', 'ord_dev009', 'prd_010', '土鸡蛋(30枚)', 1, 38.00, 38.00),
('oi_dev021', 'ord_dev010', 'prd_007', '洗衣液(2kg)', 5, 39.90, 199.50);

-- ============================================================
-- 7. 服务订单（社区服务）
-- ============================================================

INSERT IGNORE INTO service_orders (id, service_id, resident_id, provider_id, property_company_id, order_status, amount, payment_method, service_time, address, contact_phone, remark, created_at) VALUES
('svc_ord001', 'svc_001', 'res_dev001', 'res_ind01', 'pc_demo001', 'completed', 65.00, 'wechat', '2026-07-01 09:00:00', '华帝·阳光花园3栋1201', '13800138000', '全屋保洁', '2026-06-30 18:00:00'),
('svc_ord002', 'svc_003', 'res_dev001', NULL, 'pc_demo001', 'completed', 40.00, 'wechat', '2026-07-03 14:00:00', '华帝·阳光花园3栋1201', '13800138000', '空调维修', '2026-07-02 16:00:00'),
('svc_ord003', 'svc_004', 'res_dev001', NULL, 'pc_demo001', 'completed', 12.00, 'property_coin', '2026-07-05 11:30:00', '华帝·阳光花园3栋1201', '13800138000', '午餐2份', '2026-07-05 10:00:00'),
('svc_ord004', 'svc_002', 'res_dev001', NULL, 'pc_demo001', 'pending', 120.00, 'wechat', '2026-07-10 14:00:00', '华帝·阳光花园3栋1201', '13800138000', '陪护2小时', '2026-07-09 16:00:00');

-- ============================================================
-- 8. 公告（覆盖各类型）
-- ============================================================

INSERT IGNORE INTO announcements (id, title, content, announcement_type, property_company_id, community_id, publisher_id, publisher_role, status, published_at) VALUES
('ann_dev001', '关于2026年7月物业费缴纳通知', '尊敬的业主：2026年第三季度物业费已出账，请于7月31日前完成缴纳。支持微信、物业币、积分抵扣等多种方式。', 'property', 'pc_demo001', NULL, 'res_propadmin01', 'property_admin', 'published', '2026-07-01 09:00:00'),
('ann_dev002', '阳光花园小区停水通知', '因自来水管道检修，7月5日9:00-17:00暂停供水，请提前储水。给您带来不便，敬请谅解。', 'community', 'pc_demo001', 'com_demo001', 'res_propadmin01', 'property_admin', 'published', '2026-07-03 08:00:00'),
('ann_dev003', '社区夏令营活动报名开始', '暑期社区青少年夏令营开始报名！包含书法、绘画、编程等课程，物业币抵扣50%。名额有限，先到先得。', 'activity', 'pc_demo001', NULL, 'res_coo01', 'coordinator', 'published', '2026-07-02 10:00:00'),
('ann_dev004', '社区电梯维保完成公示', '截止本周五，全区32部客梯已全部完成季度深度维保及质检，各项指标均符合安全运行要求。', 'property', 'pc_demo001', 'com_demo001', 'res_propadmin01', 'property_admin', 'published', '2026-06-20 09:00:00'),
('ann_dev005', '邻里跳蚤市场活动招募公告', '本周六社区中心广场将举办"旧物换新颜"二手市集，欢迎各位邻里踊跃报名设摊或参与交换。', 'community', 'pc_demo001', 'com_demo001', 'res_act01', 'activity_leader', 'published', '2026-06-28 10:00:00');

-- ============================================================
-- 9. 消息通知（覆盖各类型）
-- ============================================================

INSERT IGNORE INTO messages (id, from_id, to_id, message_type, content, chat_type, property_company_id, read_status, created_at) VALUES
-- 物业通知
('msg_dev001', 'res_propadmin01', 'res_dev001', 'property', '您所在的楼栋将于明日14:00进行供水管道检修。尊敬的业主：因管道例行检查，3号楼A单元将于明日下午暂停供水，预计耗时2小时，请提前储水。', 'broadcast', 'pc_demo001', 'unread', '2026-07-09 10:45:00'),
-- 系统通知
('msg_dev002', 'res_admin01', 'res_dev001', 'system', '积金变动提醒：已成功到账 500 积分。您的物业费预缴奖励积分已发放。您可以在"积金商城"查看并兑换您心仪的礼品。', 'broadcast', 'pc_demo001', 'read', '2026-07-08 09:00:00'),
-- 统筹通知
('msg_dev003', 'res_coo01', 'res_dev001', 'coordinator', '年度物业满意度调查已开启。为了提供更好的社区服务，请您参与2023年度业主满意度调查，您的建议对我们至关重要。', 'broadcast', 'pc_demo001', 'read', '2026-07-08 08:00:00'),
-- 社区通知
('msg_dev004', 'res_act01', 'res_dev001', 'community', '邻里跳蚤市场活动招募公告。本周六社区中心广场将举办"旧物换新颜"二手市集，欢迎各位邻里踊跃报名设摊或参与交换。', 'broadcast', 'pc_demo001', 'read', '2026-07-07 10:00:00'),
-- 物业通知
('msg_dev005', 'res_propadmin01', 'res_dev001', 'property', '社区电梯维保完成公示。截止本周五，全区32部客梯已全部完成季度深度维保及质检，各项指标均符合安全运行要求。', 'broadcast', 'pc_demo001', 'read', '2026-06-20 09:00:00'),
-- 私信
('msg_dev006', 'res_mch01', 'res_dev001', 'resident', '您好，您的订单已发货，预计今日18:00前送达，请保持手机畅通。', 'private', 'pc_demo001', 'read', '2026-07-10 14:00:00');

-- ============================================================
-- 10. 意见箱（投票）
-- ============================================================

INSERT IGNORE INTO opinion_boxes (id, title, content, publisher_id, family_id, property_company_id, start_time, end_time, total_votes, support_count, oppose_count, abstain_count, status, result_published, created_at) VALUES
('ob_dev001', '升级大堂空气净化系统', '建议在大堂和电梯轿厢安装高效HEPA过滤器，以改善花粉季节的空气质量。', 'res_002', 'fam_002', 'pc_demo001', '2026-07-01 00:00:00', '2026-07-15 23:59:59', 142, 97, 17, 28, 'active', FALSE, '2026-07-01 00:00:00'),
('ob_dev002', '延长健身房开放时间', '由于居民需求量大，健身房将从下周一开始延长开放至晚上 11:30。', 'res_003', 'fam_003', 'pc_demo001', '2026-06-10 00:00:00', '2026-06-24 23:59:59', 180, 148, 22, 10, 'ended', TRUE, '2026-06-10 00:00:00'),
('ob_dev003', '取消访客停车时间限制', '申请允许周末访客在无需事先登记的情况下进行过夜停车。', 'res_004', 'fam_003', 'pc_demo001', '2026-06-01 00:00:00', '2026-06-20 23:59:59', 160, 35, 120, 5, 'ended', TRUE, '2026-06-01 00:00:00');

INSERT IGNORE INTO vote_records (id, opinion_box_id, resident_id, family_id, vote_option, created_at) VALUES
('vr_dev001', 'ob_dev001', 'res_dev001', 'fam_dev001', 'support', '2026-07-05 10:00:00'),
('vr_dev002', 'ob_dev002', 'res_dev001', 'fam_dev001', 'support', '2026-06-15 10:00:00'),
('vr_dev003', 'ob_dev003', 'res_dev001', 'fam_dev001', 'oppose', '2026-06-05 10:00:00');

-- ============================================================
-- 11. 活动小组
-- ============================================================

INSERT IGNORE INTO activity_groups (id, name, description, leader_id, property_company_id, community_id, member_count, monthly_fee, yearly_fee, status) VALUES
('ag_dev001', '太极拳晨练组', '每日早晨7:30在中心广场练习太极拳，强身健体，老少皆宜', 'res_act01', 'pc_demo001', 'com_demo001', 28, 0.00, 0.00, 'active'),
('ag_dev002', '篮球赛', '周末篮球友谊赛，新手友好，户外运动', 'res_dev003', 'pc_demo001', 'com_demo001', 128, 30.00, 300.00, 'active'),
('ag_dev003', '插花课', '每周插花课程，学习花艺，陶冶情操', 'res_002', 'pc_demo001', 'com_demo001', 56, 45.00, 450.00, 'active'),
('ag_dev004', '书友会', '社区读书交流会，每月一次线下分享', 'res_003', 'pc_demo001', 'com_demo001', 210, 0.00, 0.00, 'active'),
('ag_dev005', '太极拳', '太极拳进阶班，室内教学', 'res_act01', 'pc_demo001', 'com_demo001', 84, 30.00, 300.00, 'active');

INSERT IGNORE INTO activity_group_members (id, group_id, resident_id, role, status) VALUES
('agm_dev001', 'ag_dev001', 'res_dev001', 'member', 'active'),
('agm_dev002', 'ag_dev001', 'res_dev002', 'member', 'active'),
('agm_dev003', 'ag_dev002', 'res_dev003', 'member', 'active');

INSERT IGNORE INTO activity_group_subscriptions (id, group_id, resident_id, property_company_id, sub_type, amount, start_date, end_date, status) VALUES
('ags_dev001', 'ag_dev001', 'res_dev001', 'pc_demo001', 'yearly', 0.00, '2026-01-01', '2026-12-31', 'active'),
('ags_dev002', 'ag_dev002', 'res_dev003', 'pc_demo001', 'monthly', 30.00, '2026-07-01', '2026-07-31', 'active');

-- ============================================================
-- 12. 配送记录
-- ============================================================

INSERT IGNORE INTO deliveries (id, order_id, courier_id, property_company_id, status, fee, courier_earning, pickup_address, delivery_address, contact_phone, created_at, delivered_at) VALUES
('dlv_dev001', 'ord_dev001', 'res_courier01', 'pc_demo001', 'completed', 0.00, 0.00, '便民超市(华帝店)', '华帝·阳光花园3栋1201', '13800138000', '2026-07-01 09:00:00', '2026-07-01 10:00:00'),
('dlv_dev002', 'ord_dev002', 'res_courier01', 'pc_demo001', 'completed', 2.00, 0.35, '口福餐厅(华帝店)', '华帝·阳光花园3栋1201', '13800138000', '2026-07-02 12:00:00', '2026-07-02 13:00:00'),
('dlv_dev003', 'ord_dev003', 'res_courier01', 'pc_demo001', 'completed', 3.00, 0.35, '绿色农场直供(华帝站)', '华帝·阳光花园3栋1201', '13800138000', '2026-07-03 09:30:00', '2026-07-03 10:30:00');

-- ============================================================
-- 13. 积分池记录
-- ============================================================

INSERT IGNORE INTO point_pool_records (id, property_company_id, pool_type, amount, balance, source, description, created_at) VALUES
('ppr_dev001', 'pc_demo001', 'pool_in', 50000.00, 50000.00, 'exchange_diff', '积分池初始资金', '2026-01-01 00:00:00'),
('ppr_dev002', 'pc_demo001', 'pool_out', 5000.00, 45000.00, 'fee_clear', 'Q1物业费积分抵扣', '2026-03-31 23:59:59'),
('ppr_dev003', 'pc_demo001', 'pool_out', 5000.00, 40000.00, 'fee_clear', 'Q2物业费积分抵扣', '2026-06-30 23:59:59'),
('ppr_dev004', 'pc_demo001', 'pool_in', 10000.00, 50000.00, 'exchange_diff', '商家积分购买注入', '2026-07-01 00:00:00');

-- ============================================================
-- 14. 特惠推送
-- ============================================================

INSERT IGNORE INTO special_offers (id, title, content, target_type, publisher_id, publisher_role, property_company_id, community_id, merchant_id, discount_info, start_time, end_time, status) VALUES
('so_dev001', '新用户首单立减10元', '新用户首次下单享受立减10元优惠，满30元可用', 'all', 'res_admin01', 'platform_admin', 'pc_demo001', NULL, NULL, '满30减10', '2026-07-01 00:00:00', '2026-07-31 23:59:59', 'published'),
('so_dev002', '口福餐厅限时特惠', '东北拉皮原价18元，现价12元，限时3天！', 'all', 'res_mch01', 'merchant', 'pc_demo001', NULL, 'mch_demo001', '原价18 现价12', '2026-07-05 00:00:00', '2026-07-07 23:59:59', 'published'),
('so_dev003', '有机蔬菜5折优惠', '绿色农场有机蔬菜全场5折，新鲜直达', 'all', 'res_mch01', 'merchant', 'pc_demo001', NULL, 'mch_demo003', '全场5折', '2026-07-08 00:00:00', '2026-07-10 23:59:59', 'published');

-- ============================================================
-- 完成
-- ============================================================
-- dev_13800000001 全覆盖测试数据已添加
-- 登录后可看到：
--   物业公司: 华帝物业管理有限公司
--   小区: 华帝·阳光花园 3栋12层1201室
--   家庭: 4人（户主+配偶+子女+父母）
--   家庭积分: 12,480
--   家庭物业币: 350.50
--   物业费: 历史已缴12笔 + 待缴4笔（物业费+停车费+水费+电费）
--   积分记录: 49条（消费/签到/活动/兑换/抵扣）
--   物业币记录: 11条
--   商家订单: 10笔（含3笔已配送+1笔待处理）
--   服务订单: 4笔
--   公告: 5条（物业/社区/活动）
--   消息通知: 6条（物业/系统/统筹/社区/私信）
--   意见箱: 3条（进行中1+已完成2）+ 投票记录3条
--   活动小组: 5个 + 成员3条 + 订阅2条
--   配送记录: 3条
--   积分池记录: 4条
--   特惠推送: 3条
-- ============================================================

SET FOREIGN_KEY_CHECKS = 1;
