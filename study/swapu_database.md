# SwapU数据库设计


## 1. admin（管理员表）

| 字段名 | 类型 | 主键 | 非空 | 说明 |
|---|---|:---:|:---:|---|
| id | bigint | ✓ | — | 管理员 ID |
| username | varchar(50) |  | — | 管理员登录账号 |
| password | varchar(255) |  | — | 登录密码 |
| name | varchar(50) |  | — | 管理员姓名 |
| avatar | varchar(255) |  | — | 管理员头像 |
| role | tinyint |  | — | 管理员角色：图中备注为不同级别的管理员角色 |
| status | tinyint |  | — | 管理员状态：图中备注为启用/禁用 |
| create_time | datetime |  | — | 创建时间 |
| update_time | datetime |  | — | 更新时间 |

## 2. user（用户信息表）

| 字段名 | 类型 | 主键 | 非空 | 说明 |
|---|---|:---:|:---:|---|
| id | bigint | ✓ | — | 用户 ID |
| student_id | varchar(20) |  | — | 学号 |
| username | varchar(50) |  | — | 登录账号 |
| password | varchar(255) |  | — | 登录密码 |
| avatar | varchar(255) |  | — | 用户头像 |
| phone | varchar(11) |  | — | 手机号 |
| email | varchar(100) |  | — | 邮箱 |
| college | varchar(100) |  | — | 学院名称 |
| balance | decimal(10,2) |  | — | 账户余额 |
| credit_score | int |  | — | 信用分 |
| status | tinyint |  | — | 用户状态：图中备注为正常/禁用 |
| create_time | datetime |  | — | 注册时间 |
| update_time | datetime |  | — | 更新时间 |
| nickname | varchar(15) |  | — | 用户昵称 |

## 3. category（商品分类表）

| 字段名 | 类型 | 主键 | 非空 | 说明 |
|---|---|:---:|:---:|---|
| id | int | ✓ | — | 分类 ID |
| name | varchar(50) |  | — | 分类名称 |
| sort | int |  | — | 排序值 |
| status | tinyint |  | — | 分类状态：启用/禁用 |
| create_time | datetime |  | — | 创建时间 |
| update_time | datetime |  | — | 更新时间 |
| create_user | bigint |  | — | 创建该分类的管理员 ID |
| update_user | bigint |  | — | 最后更新该分类的管理员 ID |

## 4. product（商品信息表）

| 字段名 | 类型 | 主键 | 非空 | 说明 |
|---|---|:---:|:---:|---|
| id | bigint | ✓ | — | 商品 ID |
| user_id | bigint |  | — | 发布商品的用户 ID |
| title | varchar(100) |  | — | 商品标题 |
| description | text |  | — | 商品描述 |
| category_id | int |  | — | 商品分类 ID |
| price | decimal(10,2) |  | — | 当前售价 |
| original_price | decimal(10,2) |  | — | 商品原价 |
| images | json |  | — | 商品图片列表 |
| product_condition | varchar(50) |  | — | 商品成色 |
| status | tinyint |  | — | 商品状态：图中备注为在售、已售出、已下架等状态 |
| view_count | int |  | — | 商品浏览量 |
| is_top | tinyint |  | — | 是否置顶/热门商品 |
| create_time | datetime |  | — | 商品发布时间 |
| update_time | datetime |  | — | 商品更新时间 |
| quantity | int |  | — | 商品数量/库存 |

## 5. address_book（收货地址表）

| 字段名 | 类型 | 主键 | 非空 | 说明 |
|---|---|:---:|:---:|---|
| id | bigint | ✓ | — | 地址 ID |
| user_id | bigint |  | — | 所属用户 ID |
| consignee | varchar(50) |  | — | 收货人姓名 |
| phone | varchar(20) |  | — | 收货人手机号 |
| sex | varchar(2) |  | — | 收货人性别 |
| province_code | varchar(20) |  | — | 省份编码 |
| province_name | varchar(50) |  | — | 省份名称 |
| city_code | varchar(20) |  | — | 城市编码 |
| city_name | varchar(50) |  | — | 城市名称 |
| district_code | varchar(20) |  | — | 区/县编码 |
| district_name | varchar(50) |  | — | 区/县名称 |
| detail | varchar(200) |  | — | 详细地址 |
| label | varchar(50) |  | — | 地址标签，如家、学校等 |
| is_default | tinyint |  | — | 是否默认地址 |

## 6. orders（订单表）

| 字段名 | 类型 | 主键 | 非空 | 说明 |
|---|---|:---:|:---:|---|
| order_id | bigint | ✓ | — | 订单 ID |
| order_no | varchar(64) |  | — | 订单编号 |
| product_id | bigint |  | — | 关联商品 ID |
| product_title | varchar(255) |  | — | 商品标题快照 |
| product_image | varchar(500) |  | — | 商品图片快照 |
| quantity | int |  | — | 购买数量 |
| buyer_id | bigint |  | — | 买家用户 ID |
| seller_id | bigint |  | — | 卖家用户 ID |
| unit_price | decimal(10,2) |  | — | 商品成交单价 |
| amount | decimal(10,2) |  | — | 商品金额 |
| freight | decimal(10,2) |  | — | 运费 |
| total_amount | decimal(10,2) |  | — | 订单总金额 |
| status | int |  | — | 订单状态 |
| expire_time | datetime |  | — | 订单过期时间 |
| buyer_message | varchar(500) |  | — | 买家留言 |
| create_time | datetime |  | — | 订单创建时间 |
| pay_type | int |  | — | 支付方式 |
| pay_time | datetime |  | — | 支付时间 |
| cancel_reason | varchar(500) |  | — | 取消原因 |
| cancel_time | datetime |  | — | 取消时间 |
| logistics_company | varchar(100) |  | — | 物流公司 |
| logistics_no | varchar(100) |  | — | 物流单号 |
| deliver_time | datetime |  | — | 发货时间 |
| confirm_time | datetime |  | — | 卖家确认/接单时间 |
| receive_time | datetime |  | — | 买家确认收货时间 |
| delivery_method | tinyint |  | — | 配送方式 |
| address_id | bigint |  | — | 收货地址 ID（图中字段显示为 address_id） |

### 订单状态说明

ER 图未清晰显示全部状态码，建议以项目代码中的枚举或订单状态常量为准。字段 `status` 用于记录订单当前所处状态。

## 7. favorite（商品收藏表）

| 字段名 | 类型 | 主键 | 非空 | 说明 |
|---|---|:---:|:---:|---|
| id | bigint | ✓ | — | 收藏记录 ID |
| user_id | bigint |  | — | 收藏用户 ID |
| product_id | bigint |  | — | 被收藏的商品 ID |
| create_time | datetime |  | — | 收藏时间 |

### 约束建议

可为 `(user_id, product_id)` 建立唯一约束，避免同一用户重复收藏同一商品；该约束是否已在实际数据库中创建，需要以建表 SQL 为准。

## 8. comment（用户评论/评价表）

| 字段名 | 类型 | 主键 | 非空 | 说明 |
|---|---|:---:|:---:|---|
| id | bigint | ✓ | — | 评论记录 ID |
| sender_id | bigint |  | — | 发送评论的用户 ID |
| receiver_id | bigint |  | — | 接收评论的用户 ID |
| image | varchar(255) |  | — | 评论图片/图片地址 |
| content | varchar(1000) |  | — | 评论内容 |
| send_time | datetime |  | — | 发送时间 |
| status | tinyint |  | — | 评论状态 |
| comment_type | tinyint |  | — | 评论类型 |

## 9. chat_message（聊天消息表）

| 字段名 | 类型 | 主键 | 非空 | 说明 |
|---|---|:---:|:---:|---|
| id | bigint | ✓ | — | 消息 ID |
| from_user_id | bigint |  | — | 发送者用户 ID |
| to_user_id | bigint |  | — | 接收者用户 ID |
| product_id | bigint |  | — | 关联商品 ID |
| message | text |  | — | 消息内容 |
| message_type | tinyint |  | — | 消息类型；图中备注为文字/图片等类型 |
| is_read | tinyint |  | — | 是否已读 |
| create_time | datetime |  | — | 消息发送时间 |

## 10. stock_change_log（库存变更日志表）

| 字段名 | 类型 | 主键 | 非空 | 说明 |
|---|---|:---:|:---:|---|
| id | bigint | ✓ | — | 日志 ID |
| change_no | varchar(64) |  | — | 库存变更单号 |
| order_no | varchar(64) |  | — | 关联订单编号 |
| product_id | bigint |  | — | 关联商品 ID |
| quantity | int |  | — | 库存变更数量 |
| operation_type | tinyint |  | — | 操作类型：图中备注为不同库存变更操作 |
| status | tinyint |  | — | 处理状态 |
| create_time | datetime |  | — | 创建时间 |
| update_time | datetime |  | — | 更新时间 |

## 11. user_behavior_log（用户行为日志表）

| 字段名 | 类型 | 主键 | 非空 | 说明 |
|---|---|:---:|:---:|---|
| id | bigint | ✓ | — | 行为日志 ID |
| user_id | bigint |  | — | 用户 ID |
| product_id | bigint |  | — | 商品 ID |
| behavior_type | tinyint |  | — | 行为类型，如浏览、点赞/收藏、购买等，具体编码以项目实现为准 |
| behavior_value | decimal(5,4) |  | — | 行为权重/行为值 |
| category_id | int |  | — | 商品分类 ID |
| source | varchar(50) |  | — | 行为来源/触发场景 |
| create_time | datetime |  | — | 行为发生时间 |

## 12. ER 关系说明

| 关系 | 关联字段 | 关系说明 |
|---|---|---|
| 用户—商品 | `product.user_id` → `user.id` | 一个用户可以发布多个商品，一个商品对应一个发布用户 |
| 分类—商品 | `product.category_id` → `category.id` | 一个分类可以包含多个商品，一个商品归属一个分类 |
| 管理员—分类 | `category.create_user` / `category.update_user` → `admin.id` | 管理员创建或更新商品分类 |
| 用户—收货地址 | `address_book.user_id` → `user.id` | 一个用户可以维护多个收货地址 |
| 商品—订单 | `orders.product_id` → `product.id` | 商品可以关联多笔订单记录；订单保存商品信息快照 |
| 买家/卖家—订单 | `orders.buyer_id` / `orders.seller_id` → `user.id` | 用户可作为买家或卖家参与订单 |
| 订单—收货地址 | `orders.address_id` → `address_book.id` | 订单可关联收货地址 |
| 用户—收藏 | `favorite.user_id` → `user.id` | 用户收藏记录 |
| 商品—收藏 | `favorite.product_id` → `product.id` | 商品被用户收藏的记录 |
| 用户—评论 | `comment.sender_id` / `comment.receiver_id` → `user.id` | 用户之间的评论/评价关系 |
| 用户—聊天消息 | `chat_message.from_user_id` / `to_user_id` → `user.id` | 用户发送和接收聊天消息 |
| 商品—聊天消息 | `chat_message.product_id` → `product.id` | 聊天消息可关联商品 |
| 商品—库存日志 | `stock_change_log.product_id` → `product.id` | 记录商品库存变化 |
| 订单—库存日志 | `stock_change_log.order_no` → `orders.order_no` | 库存变化日志可追溯至订单 |
| 用户—行为日志 | `user_behavior_log.user_id` → `user.id` | 记录用户行为 |
| 商品—行为日志 | `user_behavior_log.product_id` → `product.id` | 记录用户对商品的行为 |
| 分类—行为日志 | `user_behavior_log.category_id` → `category.id` | 行为记录关联商品分类 |

## 13. 设计说明与注意事项

1. **订单快照字段**：`product_title`、`product_image`、`unit_price` 等字段用于保留下单时的商品信息，避免商品后续修改影响历史订单展示。
2. **库存变更追踪**：`stock_change_log` 保存库存变更数量、操作类型及关联订单，便于审计和问题排查。
3. **用户行为分析**：`user_behavior_log` 将用户、商品、分类、行为类型及行为值关联起来，可用于行为统计或个性化推荐。
4. **地址管理**：`address_book` 通过 `user_id` 归属用户，`is_default` 表示默认收货地址。
5. **字段与约束核验**：本文件根据 ER 图整理。实际的 `NOT NULL`、默认值、唯一索引、普通索引、外键约束及状态码应以项目的建表 SQL 和代码枚举为准。


## 14. 创建表结构 SQL 文件

> 以下 SQL 根据 ER 图中的字段名称和数据类型整理，补充了常用的自增主键、默认时间和必要约束，便于初始化数据库。ER 图无法确认的业务默认值、唯一约束和状态码，需结合项目实际代码进一步核对。若项目已有正式建表 SQL，应以正式 SQL 为准。

```sql
-- =========================
-- 1. 管理员表
-- =========================
CREATE TABLE admin (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '管理员ID',
    username    VARCHAR(50)  COMMENT '管理员账号',
    password    VARCHAR(255) COMMENT '登录密码',
    name        VARCHAR(50)  COMMENT '管理员姓名',
    avatar      VARCHAR(255) COMMENT '管理员头像',
    role        TINYINT      COMMENT '管理员角色',
    status      TINYINT      COMMENT '管理员状态',
    create_time DATETIME     COMMENT '创建时间',
    update_time DATETIME     COMMENT '更新时间'
) COMMENT='管理员表';

-- =========================
-- 2. 用户信息表
-- =========================
CREATE TABLE `user` (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    student_id   VARCHAR(20)  COMMENT '学号',
    username     VARCHAR(50)  COMMENT '登录账号',
    password     VARCHAR(255) COMMENT '登录密码',
    avatar       VARCHAR(255) COMMENT '用户头像',
    phone        VARCHAR(11)  COMMENT '手机号',
    email        VARCHAR(100) COMMENT '邮箱',
    college      VARCHAR(100) COMMENT '学院名称',
    balance      DECIMAL(10,2) COMMENT '账户余额',
    credit_score INT          COMMENT '信用分',
    status       TINYINT      COMMENT '用户状态',
    create_time  DATETIME     COMMENT '注册时间',
    update_time  DATETIME     COMMENT '更新时间',
    nickname     VARCHAR(15)  COMMENT '用户昵称'
) COMMENT='用户信息表';

-- =========================
-- 3. 商品分类表
-- =========================
CREATE TABLE category (
    id          INT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    name        VARCHAR(50) COMMENT '分类名称',
    sort        INT         COMMENT '排序值',
    status      TINYINT     COMMENT '分类状态',
    create_time DATETIME    COMMENT '创建时间',
    update_time DATETIME    COMMENT '更新时间',
    create_user BIGINT      COMMENT '创建管理员ID',
    update_user BIGINT      COMMENT '更新管理员ID'
) COMMENT='商品分类表';

-- =========================
-- 4. 商品信息表
-- =========================
CREATE TABLE product (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
    user_id           BIGINT        COMMENT '发布用户ID',
    title             VARCHAR(100)  COMMENT '商品标题',
    description       TEXT          COMMENT '商品描述',
    category_id       INT           COMMENT '商品分类ID',
    price             DECIMAL(10,2) COMMENT '售价',
    original_price    DECIMAL(10,2) COMMENT '原价',
    images            JSON          COMMENT '商品图片列表',
    product_condition VARCHAR(50)   COMMENT '商品成色',
    status            TINYINT       COMMENT '商品状态',
    view_count        INT           COMMENT '浏览量',
    is_top            TINYINT       COMMENT '是否置顶/热门',
    create_time       DATETIME      COMMENT '发布时间',
    update_time       DATETIME      COMMENT '更新时间',
    quantity          INT           COMMENT '商品数量/库存',
    INDEX idx_product_user_id (user_id),
    INDEX idx_product_category_id (category_id)
) COMMENT='商品信息表';

-- =========================
-- 5. 收货地址表
-- =========================
CREATE TABLE address_book (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '地址ID',
    user_id       BIGINT      COMMENT '所属用户ID',
    consignee     VARCHAR(50) COMMENT '收货人',
    phone         VARCHAR(20) COMMENT '收货人手机号',
    sex           VARCHAR(2)  COMMENT '性别',
    province_code VARCHAR(20) COMMENT '省份编码',
    province_name VARCHAR(50) COMMENT '省份名称',
    city_code     VARCHAR(20) COMMENT '城市编码',
    city_name     VARCHAR(50) COMMENT '城市名称',
    district_code VARCHAR(20) COMMENT '区县编码',
    district_name VARCHAR(50) COMMENT '区县名称',
    detail        VARCHAR(200) COMMENT '详细地址',
    label         VARCHAR(50) COMMENT '地址标签',
    is_default    TINYINT     COMMENT '是否默认地址',
    INDEX idx_address_user_id (user_id)
) COMMENT='收货地址表';

-- =========================
-- 6. 订单表
-- =========================
CREATE TABLE orders (
    order_id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    order_no          VARCHAR(64)   COMMENT '订单编号',
    product_id        BIGINT        COMMENT '商品ID',
    product_title     VARCHAR(255)  COMMENT '商品标题快照',
    product_image     VARCHAR(500)  COMMENT '商品图片快照',
    quantity          INT           COMMENT '购买数量',
    buyer_id          BIGINT        COMMENT '买家用户ID',
    seller_id         BIGINT        COMMENT '卖家用户ID',
    unit_price        DECIMAL(10,2) COMMENT '商品单价',
    amount            DECIMAL(10,2) COMMENT '商品金额',
    freight           DECIMAL(10,2) COMMENT '运费',
    total_amount      DECIMAL(10,2) COMMENT '订单总金额',
    status            INT           COMMENT '订单状态',
    expire_time       DATETIME      COMMENT '订单过期时间',
    buyer_message     VARCHAR(500)  COMMENT '买家留言',
    create_time       DATETIME      COMMENT '创建时间',
    pay_type          INT           COMMENT '支付方式',
    pay_time          DATETIME      COMMENT '支付时间',
    cancel_reason     VARCHAR(500)  COMMENT '取消原因',
    cancel_time       DATETIME      COMMENT '取消时间',
    logistics_company VARCHAR(100)  COMMENT '物流公司',
    logistics_no      VARCHAR(100)  COMMENT '物流单号',
    deliver_time      DATETIME      COMMENT '发货时间',
    confirm_time      DATETIME      COMMENT '确认/接单时间',
    receive_time      DATETIME      COMMENT '收货时间',
    delivery_method   TINYINT       COMMENT '配送方式',
    address_id        BIGINT        COMMENT '收货地址ID',
    UNIQUE KEY uk_orders_order_no (order_no),
    INDEX idx_orders_product_id (product_id),
    INDEX idx_orders_buyer_id (buyer_id),
    INDEX idx_orders_seller_id (seller_id)
) COMMENT='订单表';

-- =========================
-- 7. 商品收藏表
-- =========================
CREATE TABLE favorite (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '收藏记录ID',
    user_id     BIGINT   COMMENT '用户ID',
    product_id  BIGINT   COMMENT '商品ID',
    create_time DATETIME COMMENT '收藏时间',
    UNIQUE KEY uk_favorite_user_product (user_id, product_id),
    INDEX idx_favorite_product_id (product_id)
) COMMENT='商品收藏表';

-- =========================
-- 8. 用户评论/评价表
-- =========================
CREATE TABLE comment (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID',
    sender_id    BIGINT        COMMENT '发送者用户ID',
    receiver_id  BIGINT        COMMENT '接收者用户ID',
    image        VARCHAR(255)  COMMENT '评论图片',
    content      VARCHAR(1000) COMMENT '评论内容',
    send_time    DATETIME      COMMENT '发送时间',
    status       TINYINT       COMMENT '评论状态',
    comment_type TINYINT       COMMENT '评论类型',
    INDEX idx_comment_sender_id (sender_id),
    INDEX idx_comment_receiver_id (receiver_id)
) COMMENT='用户评论/评价表';

-- =========================
-- 9. 聊天消息表
-- =========================
CREATE TABLE chat_message (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    from_user_id BIGINT   COMMENT '发送者用户ID',
    to_user_id   BIGINT   COMMENT '接收者用户ID',
    product_id   BIGINT   COMMENT '关联商品ID',
    message      TEXT     COMMENT '消息内容',
    message_type TINYINT  COMMENT '消息类型',
    is_read      TINYINT  COMMENT '是否已读',
    create_time  DATETIME COMMENT '发送时间',
    INDEX idx_chat_from_user_id (from_user_id),
    INDEX idx_chat_to_user_id (to_user_id),
    INDEX idx_chat_product_id (product_id)
) COMMENT='聊天消息表';

-- =========================
-- 10. 库存变更日志表
-- =========================
CREATE TABLE stock_change_log (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    change_no      VARCHAR(64) COMMENT '库存变更单号',
    order_no       VARCHAR(64) COMMENT '关联订单编号',
    product_id     BIGINT      COMMENT '商品ID',
    quantity       INT         COMMENT '库存变更数量',
    operation_type TINYINT     COMMENT '库存操作类型',
    status         TINYINT     COMMENT '处理状态',
    create_time    DATETIME    COMMENT '创建时间',
    update_time    DATETIME    COMMENT '更新时间',
    INDEX idx_stock_change_product_id (product_id),
    INDEX idx_stock_change_order_no (order_no)
) COMMENT='库存变更日志表';

-- =========================
-- 11. 用户行为日志表
-- =========================
CREATE TABLE user_behavior_log (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '行为日志ID',
    user_id       BIGINT        COMMENT '用户ID',
    product_id    BIGINT        COMMENT '商品ID',
    behavior_type TINYINT       COMMENT '行为类型',
    behavior_value DECIMAL(5,4) COMMENT '行为权重/行为值',
    category_id   INT           COMMENT '商品分类ID',
    source        VARCHAR(50)   COMMENT '行为来源',
    create_time   DATETIME      COMMENT '行为发生时间',
    INDEX idx_behavior_user_id (user_id),
    INDEX idx_behavior_product_id (product_id),
    INDEX idx_behavior_category_id (category_id)
) COMMENT='用户行为日志表';
```

### SQL 使用说明

1. 执行前请先确认数据库版本支持 `JSON` 类型（MySQL 5.7 及以上）。
2. SQL 中的索引是根据常见查询关联添加的建议索引，并不代表 ER 图已经明确标注这些索引。
3. 此版本未添加外键约束，以避免在尚未确认项目实际约束策略时引入额外限制；关联关系可先通过业务层维护。
4. `orders.address_id` 按 ER 图字段名称整理；若实际项目字段为 `address_book_id`，请统一修改文档与 SQL。
