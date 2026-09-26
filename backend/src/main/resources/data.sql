-- =============================================================
-- 可选初始化数据：内置管理员账号
-- 用户名：admin  密码：admin123（BCrypt 密文，H2 profile 下自动执行）
-- MySQL 环境如需管理员，请手动执行下方 INSERT。
-- =============================================================

INSERT INTO users (username, password, nickname, role)
VALUES ('admin', '$2a$10$y3TQ5GXbdJyXRzbeoxOzreNzxsi0T5tALVje7RuiR9sBeMjtbXhAC', '系统管理员', 'ADMIN');
