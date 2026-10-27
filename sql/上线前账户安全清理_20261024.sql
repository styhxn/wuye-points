-- =====================================================================
-- 上线前账户安全清理（数据脚本，非结构迁移）
--
-- 背景：基线 SQL / API 文档中存在平台超管 13800138000 / admin123456 及多个种子账号。
-- 代码层已禁止 admin123456 登录，但生产仍应重置/禁用这些账号。
--
-- ⚠️ 执行本脚本前，先确认部署环境 APP_AUTH_TEST_MODE=false。
--    该开关默认 true 仅为联调，会放开 admin123456 并允许普通住户登录管理端。
--
-- ⚠️ 本脚本会修改 residents.status / password，属于破坏性数据操作。
--    默认 @confirm_cleanup = 0，只有确认后改成 1 才会真正执行。
--    执行前请先备份 residents 表，并确认已有可用的新平台管理员。
--
-- 生成强密码哈希（在 backend 目录执行）：
--   java -cp target/wuye-points.jar com.wuye.points.util.PasswordUtil '你的强密码'
-- 然后用输出的 UPDATE 语句重置 res_admin01（或新建平台管理员）。
-- =====================================================================

SET @confirm_cleanup = 0;

-- 0) 执行前查看：当前所有管理端账号
SELECT id, phone, name, role, status, property_company_id
FROM residents
WHERE role IN ('platform_admin', 'property_admin')
ORDER BY role, id;

-- 1) 重置平台超管密码（推荐保留账号，只换强密码）
--    把 <BCRYPT_HASH> 替换为 PasswordUtil 生成的值，并单独执行：
-- UPDATE residents
-- SET password = '<BCRYPT_HASH>', status = 'active'
-- WHERE id = 'res_admin01' AND phone = '13800138000';

-- 2) 禁用基线种子/演示账号（确认后执行；不影响上面重置后的 res_admin01）
UPDATE residents
SET status = 'disabled'
WHERE @confirm_cleanup = 1
  AND id IN (
    'res_propadmin01', 'res_pleader01', 'res_coo01', 'res_sec01', 'res_ind01',
    'res_mch01', 'res_mch02', 'res_mch03', 'res_tech01',
    'res_courier01', 'res_courier02', 'res_courier03',
    'res_act01', 'res_demo01', 'res_demo02', 'res_demo03',
    'res_dev001', 'res_dev002', 'res_dev003', 'res_dev004'
  );

-- 3) 检查是否还有 active 的平台管理员（必须至少 1 个）
SELECT COUNT(*) AS active_platform_admin_count
FROM residents
WHERE role = 'platform_admin' AND status = 'active';

-- 4) 执行后查看
SELECT id, phone, name, role, status
FROM residents
WHERE role IN ('platform_admin', 'property_admin')
ORDER BY role, id;
