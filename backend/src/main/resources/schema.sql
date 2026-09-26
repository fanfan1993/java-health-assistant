-- =============================================================
-- AI 心理健康助手 建表脚本（MySQL 8 / H2 双兼容）
-- MySQL 使用方式：
--   CREATE DATABASE IF NOT EXISTS mind_assistant DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
--   USE mind_assistant;
--   SOURCE schema.sql;
-- （H2 内存库由 application-h2.yml 自动执行本脚本，无需建库语句）
-- =============================================================

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username    VARCHAR(64)  NOT NULL COMMENT '用户名（唯一）',
    password    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt 密文）',
    nickname    VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
    avatar      VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    role        VARCHAR(16)  NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- 对话会话表
CREATE TABLE IF NOT EXISTS chat_session (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    user_id     BIGINT       NOT NULL COMMENT '所属用户ID',
    title       VARCHAR(128) NOT NULL DEFAULT '新的对话' COMMENT '会话标题',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_cs_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'AI 对话会话表';

-- 对话消息表
CREATE TABLE IF NOT EXISTS chat_message (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    session_id  BIGINT      NOT NULL COMMENT '所属会话ID',
    role        VARCHAR(16) NOT NULL COMMENT '角色：user/assistant',
    content     TEXT        NOT NULL COMMENT '消息内容',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_cm_session_id (session_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'AI 对话消息表';

-- 心理测评记录表
CREATE TABLE IF NOT EXISTS assessment_record (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    scale_id    VARCHAR(32)  NOT NULL COMMENT '量表编号：SDS/SAS/PSS/PSQI',
    scale_name  VARCHAR(64)  NOT NULL COMMENT '量表名称',
    total_score INT          NOT NULL COMMENT '总分',
    `level`     VARCHAR(16)  NOT NULL COMMENT '等级：正常/轻度/中度/重度 等',
    suggestion  VARCHAR(1024) DEFAULT NULL COMMENT '建议文案',
    json_detail TEXT         DEFAULT NULL COMMENT '逐题作答明细（JSON）',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_ar_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '心理测评记录表';

-- 心情日记表
CREATE TABLE IF NOT EXISTS mood_journal (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '日记ID',
    user_id     BIGINT      NOT NULL COMMENT '用户ID',
    content     TEXT        NOT NULL COMMENT '日记内容',
    mood        INT         NOT NULL COMMENT '心情 1-5（1低落 ~ 5开心）',
    tags        VARCHAR(255) DEFAULT NULL COMMENT '标签（逗号分隔）',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_mj_user_ct (user_id, create_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '心情日记表';
