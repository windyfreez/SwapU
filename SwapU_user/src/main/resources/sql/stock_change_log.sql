-- 库存变更日志表（RabbitMQ 异步落库的幂等、审计与对账依据）
CREATE TABLE stock_change_log (
    id             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    change_no      VARCHAR(64) NOT NULL COMMENT '库存变更流水号（幂等键，如 订单号-D）',
    order_no       VARCHAR(64) NOT NULL COMMENT '业务单号',
    product_id     BIGINT      NOT NULL COMMENT '商品id',
    quantity       INT         NOT NULL COMMENT '变更数量',
    operation_type TINYINT     NOT NULL COMMENT '1=扣减 2=回补',
    status         TINYINT     NOT NULL DEFAULT 0 COMMENT '0=待处理 1=成功 2=失败(死信/需人工)',
    create_time    DATETIME    NOT NULL COMMENT '创建时间',
    update_time    DATETIME    NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_change_no (change_no),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存变更日志（MQ 异步落库的幂等与对账依据）';
