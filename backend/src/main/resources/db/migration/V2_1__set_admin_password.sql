-- 为现有管理员设置初始密码
-- 超管账号: 13800138000 / admin123456

-- 更新平台管理员密码
UPDATE residents SET password = '$2b$10$36uecJYOkhVkyg3wc9BJLuDZWCpL9AgcgFNREFlE3JeoIbpL7jDJS' WHERE id = 'res_admin01';

-- 查看当前管理员账号
SELECT id, name, phone, role, property_company_id FROM residents WHERE role IN ('property_admin', 'platform_admin');
