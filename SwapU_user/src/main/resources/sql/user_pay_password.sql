-- =============================================================
-- 支付密码独立化：user 表新增 pay_password
--
-- 背景：原实现直接用「登录密码」校验支付，且两者都是无盐 MD5。
-- 变更：
--   1) 新增 pay_password 列，存 BCrypt 密文（$2a$ 开头），与登录密码完全独立；
--   2) 该列允许为 NULL，表示「历史账号尚未初始化」，服务端会在首次余额支付时
--      用登录密码校验一次并顺手把 hash 升级为 BCrypt（惰性迁移）；
--   3) 新注册用户在注册时就写入 BCrypt(登录密码)，可直接支付。
--
-- 兼容性：不改动 password 列，现有登录流程不受影响。
-- =============================================================

ALTER TABLE `user`
    ADD COLUMN pay_password VARCHAR(100) NULL
        COMMENT '支付密码（BCrypt 密文，与登录密码独立；NULL 表示历史账号未初始化）'
        AFTER password;

-- 可选：查看还有多少账号未初始化支付密码
-- SELECT COUNT(*) FROM `user` WHERE pay_password IS NULL;
