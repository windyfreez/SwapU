-- =============================================================
-- 管理员管理增强（批次二）：逻辑删除
--
-- 背景：原实现是物理删除（delete from admin），而 audit_record.reviewer_id
--       引用了管理员 id，物理删除后历史审核流水里的「谁审的」会变成查不到的人。
--
-- 变更：
--   1) admin 表新增 deleted 标记，0 正常 / 1 已删除；
--   2) 登录、鉴权、分页、唯一性校验全部过滤 deleted = 0；
--   3) 删除时同时把 username 改写为「原账号_del_{id}」，释放原账号名，
--      避免出现「删掉的账号名永远无法重新注册」。
--
-- 兼容性：存量数据默认 0，不需要刷数据。
-- =============================================================

ALTER TABLE admin
    ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0
        COMMENT '逻辑删除：0正常 1已删除'
        AFTER status;

-- 可选：确认当前没有已删除记录
-- SELECT COUNT(*) FROM admin WHERE deleted = 1;
