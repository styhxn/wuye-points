-- ============================================================
-- 补充缺失的测试数据（手机端全覆盖）
-- 用法: mysql -u root -p123456 -h 8.147.60.8 wuye_points < deploy-missing-test-data.sql
-- ============================================================

USE wuye_points;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. 商品表（products）- 为空，手机端下单/商品列表需要
-- ============================================================

INSERT IGNORE INTO products (id, merchant_id, name, description, price, member_price, point_price, stock, category, status) VALUES
-- 口福餐厅（mch_demo001）商品
('prd_001', 'mch_demo001', '红烧牛肉面', '手工拉面+秘制牛肉', 22.00, 20.00, 2200, 999, '面食', 'active'),
('prd_002', 'mch_demo001', '宫保鸡丁套餐', '宫保鸡丁+米饭+汤', 28.00, 25.00, 2800, 999, '套餐', 'active'),
('prd_003', 'mch_demo001', '麻辣香锅', '自选食材麻辣香锅', 35.00, 32.00, 3500, 999, '锅类', 'active'),
('prd_004', 'mch_demo001', '冰镇柠檬水', '新鲜柠檬现榨', 8.00, 7.00, 800, 999, '饮品', 'active'),
('prd_005', 'mch_demo001', '蛋炒饭', '扬州炒饭经典款', 15.00, 13.00, 1500, 999, '主食', 'active'),
-- 便民超市（mch_demo002）商品
('prd_006', 'mch_demo002', '农夫山泉550ml', '天然矿泉水550ml*24瓶', 28.00, 25.00, 2800, 200, '饮料', 'active'),
('prd_007', 'mch_demo002', '金龙鱼花生油5L', '压榨一级花生油5L', 89.90, 85.00, 8990, 50, '粮油', 'active'),
('prd_008', 'mch_demo002', '蒙牛纯牛奶250ml', '纯牛奶250ml*12盒', 45.00, 42.00, 4500, 100, '乳品', 'active'),
('prd_009', 'mch_demo002', '康师傅方便面5连包', '红烧牛肉面5连包', 12.50, 11.00, 1250, 300, '方便食品', 'active'),
('prd_010', 'mch_demo002', '维达抽纸3包装', '超韧系列抽纸3包', 15.90, 14.00, 1590, 150, '日用品', 'active'),
-- 绿色农场（mch_demo003）商品
('prd_011', 'mch_demo003', '精品果篮', '当季水果组合装', 58.00, 52.00, 5800, 20, '水果', 'active'),
('prd_012', 'mch_demo003', '进口车厘子1kg', '智利进口JJ级车厘子', 89.00, 82.00, 8900, 40, '水果', 'active');

-- ============================================================
-- 1.5 商家-物业挂接配置（merchant_property_configs）- 支付/分成依赖
-- ============================================================

INSERT IGNORE INTO merchant_property_configs (id, merchant_id, property_company_id, commission_rate, point_exchange_rate, member_discount_price, coin_rebate_rate, coin_rebate_enabled, status) VALUES
('mpc_001', 'mch_demo001', 'pc_demo001', 0.1500, 100.0000, NULL, 0.0200, TRUE, 'active'),
('mpc_002', 'mch_demo002', 'pc_demo001', 0.1000, 200.0000, NULL, 0.0100, TRUE, 'active'),
('mpc_003', 'mch_demo003', 'pc_demo001', 0.2000, 50.0000, NULL, 0.0300, TRUE, 'active');

-- ============================================================
-- 2. 社区服务表（community_services）- 为空，手机端服务列表需要
-- ============================================================

INSERT IGNORE INTO community_services (id, name, description, category, price, member_price, price_unit, provider_id, provider_type, property_company_id, rank_order, is_subscription, status) VALUES
('svc_001', '家电维修', '专业家电维修服务，涵盖空调、冰箱、洗衣机等', 'cleaning_repair', 50.00, 40.00, '次', 'res_tech01', 'technician', 'pc_demo001', 1, false, 'active'),
('svc_002', '家庭保洁', '专业保洁团队上门服务，2小时起', 'cleaning_repair', 80.00, 65.00, '次', 'res_tech01', 'technician', 'pc_demo001', 2, false, 'active'),
('svc_003', '管道疏通', '厨卫管道疏通维修', 'cleaning_repair', 60.00, 50.00, '次', 'res_tech01', 'technician', 'pc_demo001', 3, false, 'active'),
('svc_004', '社区食堂午餐', '工作日营养午餐配送', 'canteen', 15.00, 12.00, '份', 'res_ind01', 'individual_leader', 'pc_demo001', 4, true, 'active'),
('svc_005', '少儿编程辅导', '8-15岁少儿编程入门课程', 'tutoring', 200.00, 180.00, '月', 'res_ind01', 'individual_leader', 'pc_demo001', 5, true, 'active'),
('svc_006', '老人日间照料', '提供日间看护、陪伴、基础护理', 'elderly_care', 100.00, 85.00, '天', 'res_ind01', 'individual_leader', 'pc_demo001', 6, false, 'active'),
('svc_007', '社区义诊', '每月一次免费基础体检', 'medical', 0.00, 0.00, '次', 'res_propadmin01', 'property_admin', 'pc_demo001', 7, false, 'active'),
('svc_008', '宠物寄养', '节假日宠物临时寄养服务', 'pet_medical', 30.00, 25.00, '天', 'res_ind01', 'individual_leader', 'pc_demo001', 8, false, 'active'),
('svc_009', '代收快递', '上门代收快递服务', 'life_service', 5.00, 3.00, '件', 'res_courier01', 'courier', 'pc_demo001', 9, false, 'active'),
('svc_010', '相亲交友', '社区单身交友活动', 'dating', 50.00, 40.00, '次', 'res_coo01', 'coordinator', 'pc_demo001', 10, false, 'active');

-- ============================================================
-- 3. 积分规则表（point_rules）- 为空
-- ============================================================

INSERT IGNORE INTO point_rules (id, name, description, point_type, source, rate, min_amount, daily_limit, monthly_limit, property_company_id, status) VALUES
('pr_001', '消费返积分', '订单消费后按金额返积分', 'earn', 'order', 1.00, NULL, NULL, NULL, 'pc_demo001', 'active'),
('pr_002', '签到积分', '每日签到获得积分', 'earn', 'sign_in', NULL, NULL, 10, 200, 'pc_demo001', 'active'),
('pr_003', '活动奖励积分', '参与社区活动获得积分', 'earn', 'activity', NULL, NULL, NULL, 500, 'pc_demo001', 'active'),
('pr_004', '积分兑换物业币', '100积分兑换1物业币', 'spend', 'redeem_coin', 0.01, NULL, NULL, NULL, 'pc_demo001', 'active'),
('pr_005', '积分抵扣物业费', '100积分抵扣1元物业费', 'spend', 'deduct_fee', 0.01, NULL, NULL, NULL, 'pc_demo001', 'active');

-- ============================================================
-- 4. 物业币规则表（property_coin_rules）- 为空
-- ============================================================

INSERT IGNORE INTO property_coin_rules (id, name, description, coin_type, source, rate, property_company_id, status) VALUES
('pcr_001', '充值物业币', '1元充值1物业币', 'earn', 'recharge', 1.00, 'pc_demo001', 'active'),
('pcr_002', '消费返物业币', '订单消费后按比例返物业币', 'earn', 'order', 0.05, 'pc_demo001', 'active'),
('pcr_003', '物业币抵扣订单', '1物业币抵扣1元', 'spend', 'pay_order', 1.00, 'pc_demo001', 'active');

-- ============================================================
-- 5. 物业费规则表（property_fee_rules）- 为空
-- ============================================================

INSERT IGNORE INTO property_fee_rules (id, name, fee_type, calculation_method, unit_price, unit, billing_cycle, auto_deduct_enabled, overdue_rate, property_company_id, status) VALUES
('pfr_001', '物业费', 'property', 'area', 1.50, '元/㎡/月', 'quarterly', true, 0.001, 'pc_demo001', 'active'),
('pfr_002', '停车费', 'parking', 'fixed', 150.00, '元/月', 'monthly', true, 0, 'pc_demo001', 'active'),
('pfr_003', '水费', 'water', 'usage', 3.50, '元/吨', 'monthly', false, 0.001, 'pc_demo001', 'active'),
('pfr_004', '电费', 'electricity', 'usage', 0.56, '元/度', 'monthly', false, 0.001, 'pc_demo001', 'active');

-- ============================================================
-- 6. 补充居民的 openid（让手机端能登录）
-- ============================================================

-- 给测试用户添加 openid，方便手机端微信登录测试
UPDATE residents SET openid = 'dev_openid_13900000005' WHERE id = 'res_001' AND openid IS NULL;
UPDATE residents SET openid = 'dev_openid_13900000007' WHERE id = 'res_004' AND openid IS NULL;

-- 补充 dev 测试用户的物业币冻结记录（手机端"冻结"状态测试）
INSERT IGNORE INTO coin_freeze_records (id, resident_id, property_company_id, action, amount, reason, operator_id, created_at) VALUES
('cfr_dev001', 'res_002', 'pc_demo001', 'freeze', 50.00, '异常操作测试', 'res_propadmin01', NOW());

-- ============================================================
-- 7. 补充手机端需要的邻里聊天限制数据
-- ============================================================

-- 确保测试用户有聊天限制记录
INSERT IGNORE INTO neighbor_contact_limits (id, resident_id, property_company_id, contact_date, contact_count) VALUES
('ncl_dev001', 'res_dev001', 'pc_demo001', CURDATE(), 0),
('ncl_dev002', 'res_001', 'pc_demo001', CURDATE(), 0);

-- ============================================================
-- 8. 补充物业币过期记录（手机端"即将过期"测试）
-- ============================================================

INSERT IGNORE INTO coin_expiry_records (id, resident_id, property_company_id, expired_amount, pool_in_amount, expired_at, created_at) VALUES
('cer_dev001', 'res_dev001', 'pc_demo001', 20.00, 10.00, DATE_ADD(NOW(), INTERVAL 30 DAY), NOW()),
('cer_dev002', 'res_001', 'pc_demo001', 30.00, 15.00, DATE_ADD(NOW(), INTERVAL 15 DAY), NOW());

-- ============================================================
-- 9. 补充积分即将过期记录
-- ============================================================

INSERT IGNORE INTO point_records (id, resident_id, family_id, property_company_id, point_type, amount, balance, source, source_id, description, operator_id, created_at) VALUES
('pt_exp001', 'res_dev001', 'fam_dev001', 'pc_demo001', 'expire', -100, 12380, 'system', NULL, '积分过期', NULL, DATE_ADD(NOW(), INTERVAL 30 DAY)),
('pt_exp002', 'res_001', 'fam_001', 'pc_demo001', 'expire', -50, 2950, 'system', NULL, '积分过期', NULL, DATE_ADD(NOW(), INTERVAL 15 DAY));

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 完成提示
-- ============================================================
SELECT '✅ 测试数据补充完成！' AS message;
SELECT 'products' AS `table`, COUNT(*) AS `rows` FROM products
UNION ALL
SELECT 'community_services', COUNT(*) FROM community_services
UNION ALL
SELECT 'point_rules', COUNT(*) FROM point_rules
UNION ALL
SELECT 'property_coin_rules', COUNT(*) FROM property_coin_rules
UNION ALL
SELECT 'property_fee_rules', COUNT(*) FROM property_fee_rules;
