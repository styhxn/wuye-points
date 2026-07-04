-- 管理端登录：residents表增加password字段
ALTER TABLE residents ADD COLUMN password VARCHAR(200) COMMENT '密码（BCrypt加密，管理端登录用）' AFTER phone;
