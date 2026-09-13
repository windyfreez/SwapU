-- =========================
-- 管理端：管理员表
-- 说明：密码使用 BCrypt 密文存储，禁止明文；管理端 JWT 由 sky.jwt.admin-secret-key 签发
-- =========================
CREATE TABLE IF NOT EXISTS admin
(
    id          BIGINT AUTO_INCREMENT COMMENT '管理员ID'
        PRIMARY KEY,
    username    VARCHAR(20)  NOT NULL COMMENT '登录账号',
    password    VARCHAR(100) NOT NULL COMMENT '登录密码（BCrypt密文）',
    name        VARCHAR(50) COMMENT '姓名',
    avatar      VARCHAR(255) COMMENT '头像地址',
    role        TINYINT DEFAULT 2 COMMENT '角色：1超级管理员 2普通管理员',
    status      TINYINT DEFAULT 1 COMMENT '账号状态：1启用 0禁用',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    CONSTRAINT admin_username UNIQUE (username)
) COMMENT='管理员表';

-- =========================
-- 初始化超级管理员：root / 123456
-- 生产环境请在首次登录后立即修改密码
-- =========================
INSERT IGNORE INTO admin (username, password, name, avatar, role, status, create_time, update_time)
VALUES ('root', '$2a$10$.PPyceKI2sByJDobK.Sqj.gKsLc.CNNC0L2vAeDJvnLolUWaCQnKu', '系统管理员', NULL, 1, 1, NOW(), NOW());
