-- =========================
-- 管理端：审核模块建表脚本
-- 说明：audit_record 为审核流水表，商品、评论、头像审核共用。
-- 脚本可重复执行。
-- =========================

CREATE TABLE IF NOT EXISTS audit_record
(
    id          BIGINT AUTO_INCREMENT COMMENT '流水ID'
        PRIMARY KEY,
    type        TINYINT      NOT NULL COMMENT '审核类型：1商品 2评论 3头像',
    target_id   BIGINT       NOT NULL COMMENT '被审对象ID（商品/评论/用户）',
    result      TINYINT      NOT NULL COMMENT '审核结论：1通过 2驳回',
    reason      VARCHAR(500) DEFAULT NULL COMMENT '驳回原因',
    ai_result   JSON         DEFAULT NULL COMMENT 'AI审核结论（JSON，AI审核接入后）',
    reviewer_id BIGINT       DEFAULT NULL COMMENT '审核人ID（admin.id）',
    review_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    KEY idx_audit_record_type_target (type, target_id),
    KEY idx_audit_record_reviewer (reviewer_id),
    KEY idx_audit_record_review_time (review_time)
) COMMENT='审核流水表';
