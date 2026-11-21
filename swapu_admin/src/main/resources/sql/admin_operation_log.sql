-- =============================================================
-- 管理员操作日志（批次三）
--
-- 用途：管理员管理属于高危操作（新增账号、改角色、启停、删除、重置密码），
--       需要留下「谁在什么时候对谁做了什么」的审计轨迹。
--
-- 说明：
--   1) admin_username 为冗余快照：账号被逻辑删除/改名后，日志仍可读；
--   2) detail 存变更摘要（如 role:2→3），不存密码等敏感信息；
--   3) 只记录管理员管理相关的写操作，查询类操作不入库，避免日志爆炸。
-- =============================================================

CREATE TABLE IF NOT EXISTS admin_operation_log
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    admin_id       BIGINT       NOT NULL COMMENT '操作人ID',
    admin_username VARCHAR(50)  COMMENT '操作人账号（冗余快照）',
    action         VARCHAR(32)  NOT NULL COMMENT '动作：CREATE/UPDATE/STATUS/DELETE/RESET_PASSWORD',
    target_type    VARCHAR(32)  COMMENT '对象类型：ADMIN',
    target_id      BIGINT       COMMENT '对象ID',
    detail         VARCHAR(500) COMMENT '变更摘要，如 role:2->3',
    ip             VARCHAR(64)  COMMENT '操作IP',
    create_time    DATETIME     NOT NULL COMMENT '操作时间',
    KEY idx_admin_time (admin_id, create_time),
    KEY idx_time (create_time)
) COMMENT='管理员操作日志表';
