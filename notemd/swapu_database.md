# 数据库设计

## 1. category（商品分类表）

| 字段名         | 类型          | 主键 | 非空 | 说明         |
| ----------- | ----------- | -- | -- | ---------- |
| id          | int         | ✓  | ✓  | 分类ID       |
| name        | varchar(50) |    | ✓  | 分类名称       |
| sort        | int         |    |    | 排序值        |
| status      | tinyint     |    |    | 状态：1启用 0禁用 |
| create_time | datetime    |    |    | 创建时间       |
| update_time | datetime    |    |    | 更新时间       |
| create_user | bigint      |    |    | 创建人ID      |
| update_user | bigint      |    |    | 更新人ID      |

---

## 2. user（用户信息表）

| 字段名          | 类型            | 主键 | 非空 | 说明           |
| ------------ | ------------- | -- | -- | ------------ |
| id           | bigint        | ✓  | ✓  | 用户ID         |
| student_id   | varchar(20)   |    | ✓  | 学号           |
| username     | varchar(50)   |    | ✓  | 登录账号         |
| password     | varchar(255)  |    | ✓  | 登录密码         |
| avatar       | varchar(255)  |    |    | 用户头像         |
| phone        | varchar(11)   |    |    | 手机号          |
| email        | varchar(100)  |    |    | 邮箱           |
| college      | varchar(100)  |    |    | 学院名称         |
| balance      | decimal(10,2) |    |    | 账户余额         |
| credit_score | int           |    |    | 信用分          |
| status       | tinyint       |    |    | 账户状态：1正常 0禁用 |
| create_time  | datetime      |    |    | 注册时间         |
| update_time  | datetime      |    |    | 更新时间         |
| nickname     | varchar(15)   |    |    | 用户昵称         |

---

## 3. product（商品信息表）

| 字段名               | 类型            | 主键 | 非空 | 说明                 |
| ----------------- | ------------- | -- | -- | ------------------ |
| id                | bigint        | ✓  | ✓  | 商品ID               |
| user_id           | bigint        |    | ✓  | 发布用户ID             |
| title             | varchar(100)  |    | ✓  | 商品标题               |
| description       | text          |    |    | 商品描述               |
| category_id       | int           |    |    | 分类ID               |
| price             | decimal(10,2) |    | ✓  | 售价                 |
| original_price    | decimal(10,2) |    |    | 原价                 |
| images            | json          |    |    | 商品图片列表             |
| product_condition | varchar(50)   |    | ✓  | 商品成色               |
| status            | tinyint       |    |    | 商品状态：1在售 2已售出 3已下架 |
| view_count        | int           |    |    | 浏览量                |
| is_top            | tinyint       |    |    | 是否热门商品             |
| quantity          | int           |    |    | 库存数量               |
| create_time       | datetime      |    |    | 发布时间               |
| update_time       | datetime      |    |    | 更新时间               |

---

## 4. favorite（商品收藏表）

| 字段名         | 类型       | 主键 | 非空 | 说明   |
| ----------- | -------- | -- | -- | ---- |
| id          | bigint   | ✓  | ✓  | 收藏ID |
| user_id     | bigint   |    | ✓  | 用户ID |
| product_id  | bigint   |    | ✓  | 商品ID |
| create_time | datetime |    |    | 收藏时间 |

### 唯一约束

```sql
UNIQUE(user_id, product_id)
```

用于防止同一用户重复收藏同一商品。

---

## 5. orders（订单表）

| 字段名               | 类型            | 主键 | 非空 | 说明     |
| ----------------- | ------------- | -- | -- | ------ |
| order_id          | bigint        | ✓  | ✓  | 订单ID   |
| order_no          | varchar(64)   |    | ✓  | 订单编号   |
| product_id        | bigint        |    | ✓  | 商品ID   |
| product_title     | varchar(255)  |    | ✓  | 商品标题快照 |
| product_image     | varchar(500)  |    |    | 商品图片快照 |
| quantity          | int           |    | ✓  | 购买数量   |
| buyer_id          | bigint        |    | ✓  | 买家ID   |
| seller_id         | bigint        |    | ✓  | 卖家ID   |
| unit_price        | decimal(10,2) |    | ✓  | 商品单价   |
| amount            | decimal(10,2) |    | ✓  | 商品金额   |
| freight           | decimal(10,2) |    |    | 运费     |
| total_amount      | decimal(10,2) |    |    | 订单总金额  |
| status            | int           |    | ✓  | 订单状态   |
| expire_time       | datetime      |    |    | 订单过期时间 |
| buyer_message     | varchar(500)  |    |    | 买家留言   |
| create_time       | datetime      |    | ✓  | 创建时间   |
| pay_type          | int           |    |    | 支付方式   |
| pay_time          | datetime      |    |    | 支付时间   |
| cancel_reason     | varchar(500)  |    |    | 取消原因   |
| cancel_time       | datetime      |    |    | 取消时间   |
| logistics_company | varchar(100)  |    |    | 物流公司   |
| logistics_no      | varchar(100)  |    |    | 物流单号   |
| deliver_time      | datetime      |    |    | 发货时间   |
| confirm_time      | datetime      |    |    | 接单时间   |
| receive_time      | datetime      |    |    | 收货时间   |
| delivery_method   | tinyint       |    |    | 配送方式   |
| address_book_id   | bigint        |    |    | 收货地址ID |

### 订单状态说明

| 状态值 | 含义  |
| --- | --- |
| 1   | 待确认 |
| 2   | 待支付 |
| 3   | 待发货 |
| 4   | 待收货 |
| 5   | 已完成 |
| 6   | 已取消 |

---

## 6. chat_message（聊天消息表）

| 字段名          | 类型       | 主键 | 非空 | 说明     |
| ------------ | -------- | -- | -- | ------ |
| id           | bigint   | ✓  | ✓  | 消息ID   |
| from_user_id | bigint   |    | ✓  | 发送者ID  |
| to_user_id   | bigint   |    | ✓  | 接收者ID  |
| product_id   | bigint   |    |    | 关联商品ID |
| message      | text     |    | ✓  | 消息内容   |
| message_type | tinyint  |    |    | 消息类型   |
| is_read      | tinyint  |    |    | 是否已读   |
| create_time  | datetime |    |    | 发送时间   |

### 消息类型

| 值 | 说明   |
| - | ---- |
| 1 | 文本消息 |
| 2 | 图片消息 |

### 已读状态

| 值 | 说明 |
| - | -- |
| 0 | 未读 |
| 1 | 已读 |


创建表结构sql文件：
```sql
-- =========================
-- 商品分类表
-- =========================
CREATE TABLE category
(
id          INT AUTO_INCREMENT COMMENT '分类ID'
PRIMARY KEY,
name        VARCHAR(50) NOT NULL COMMENT '分类名称',
sort        INT DEFAULT 0 COMMENT '排序值',
status      TINYINT COMMENT '状态：1启用 0禁用',
create_time DATETIME COMMENT '创建时间',
update_time DATETIME COMMENT '更新时间',
create_user BIGINT COMMENT '创建人ID',
update_user BIGINT COMMENT '更新人ID'
) COMMENT='商品分类表';

-- =========================
-- 聊天消息表
-- =========================
CREATE TABLE chat_message
(
id           BIGINT AUTO_INCREMENT COMMENT '消息ID'
PRIMARY KEY,
from_user_id BIGINT NOT NULL COMMENT '发送者用户ID',
to_user_id   BIGINT NOT NULL COMMENT '接收者用户ID',
product_id   BIGINT COMMENT '关联商品ID',
message      TEXT NOT NULL COMMENT '消息内容',
message_type TINYINT DEFAULT 1 COMMENT '消息类型：1文本 2图片',
is_read      TINYINT DEFAULT 0 COMMENT '是否已读：0未读 1已读',
create_time  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间'
) COMMENT='聊天消息表';

-- =========================
-- 收藏表
-- =========================
CREATE TABLE favorite
(
id          BIGINT AUTO_INCREMENT COMMENT '收藏ID'
PRIMARY KEY,
user_id     BIGINT NOT NULL COMMENT '用户ID',
product_id  BIGINT NOT NULL COMMENT '商品ID',
create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',


CONSTRAINT uk_user_product
    UNIQUE (user_id, product_id)


) COMMENT='商品收藏表';

-- =========================
-- 订单表
-- =========================
CREATE TABLE orders
(
order_id          BIGINT AUTO_INCREMENT COMMENT '订单ID'
PRIMARY KEY,


order_no          VARCHAR(64) NOT NULL COMMENT '订单编号',

product_id        BIGINT NOT NULL COMMENT '商品ID',
product_title     VARCHAR(255) NOT NULL COMMENT '商品标题',
product_image     VARCHAR(500) COMMENT '商品主图',

quantity          INT DEFAULT 1 NOT NULL COMMENT '购买数量',

buyer_id          BIGINT NOT NULL COMMENT '买家用户ID',
seller_id         BIGINT NOT NULL COMMENT '卖家用户ID',

unit_price        DECIMAL(10,2) NOT NULL COMMENT '商品单价',
amount            DECIMAL(10,2) NOT NULL COMMENT '商品总金额',

freight           DECIMAL(10,2) DEFAULT 0.00 COMMENT '运费',
total_amount      DECIMAL(10,2) COMMENT '订单总金额',

status            INT DEFAULT 1 NOT NULL COMMENT
    '订单状态：1待确认 2待支付 3待发货 4待收货 5已完成 6已取消',

expire_time       DATETIME COMMENT '订单过期时间',

buyer_message     VARCHAR(500) COMMENT '买家留言',

create_time       DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',

pay_type          INT COMMENT '支付方式：1支付宝 2微信 3余额支付',

pay_time          DATETIME COMMENT '支付时间',

cancel_reason     VARCHAR(500) COMMENT '取消原因',

cancel_time       DATETIME COMMENT '取消时间',

logistics_company VARCHAR(100) COMMENT '物流公司',

logistics_no      VARCHAR(100) COMMENT '物流单号',

deliver_time      DATETIME COMMENT '发货时间',

confirm_time      DATETIME COMMENT '卖家确认接单时间',

receive_time      DATETIME COMMENT '买家确认收货时间',

delivery_method   TINYINT COMMENT '配送方式：1自提 2快递',

address_book_id   BIGINT COMMENT '收货地址ID',

CONSTRAINT uk_order_no
    UNIQUE (order_no)


) COMMENT='订单表';

-- =========================
-- 商品表
-- =========================
CREATE TABLE product
(
id                BIGINT AUTO_INCREMENT COMMENT '商品ID'
PRIMARY KEY,


user_id           BIGINT NOT NULL COMMENT '发布用户ID',

title             VARCHAR(100) NOT NULL COMMENT '商品标题',

description       TEXT COMMENT '商品描述',

category_id       INT COMMENT '分类ID',

price             DECIMAL(10,2) NOT NULL COMMENT '出售价格',

original_price    DECIMAL(10,2) COMMENT '原价',

images            JSON COMMENT '商品图片列表',

product_condition VARCHAR(50) NOT NULL COMMENT '商品成色',

status            TINYINT DEFAULT 1 COMMENT
    '商品状态：1在售 2已售出 3已下架',

view_count        INT DEFAULT 0 COMMENT '浏览量',

is_top            TINYINT DEFAULT 0 COMMENT
    '是否热门商品：0否 1是',

create_time       DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',

update_time       DATETIME DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

quantity          INT COMMENT '库存数量'


) COMMENT='商品信息表';

-- =========================
-- 用户表
-- =========================
CREATE TABLE user
(
id           BIGINT AUTO_INCREMENT COMMENT '用户ID'
PRIMARY KEY,


student_id   VARCHAR(20) NOT NULL COMMENT '学号',

username     VARCHAR(50) NOT NULL COMMENT '登录账号',

password     VARCHAR(255) NOT NULL COMMENT '登录密码',

avatar       VARCHAR(255) COMMENT '头像地址',

phone        VARCHAR(11) COMMENT '手机号',

email        VARCHAR(100) COMMENT '邮箱',

college      VARCHAR(100) COMMENT '学院名称',

balance      DECIMAL(10,2) DEFAULT 0.00 COMMENT '账户余额',

credit_score INT DEFAULT 100 COMMENT '信用分',

status       TINYINT DEFAULT 1 COMMENT
    '账户状态：1正常 0禁用',

create_time  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',

update_time  DATETIME DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

nickname     VARCHAR(15) COMMENT '用户昵称',

CONSTRAINT student_id
    UNIQUE (student_id)


) COMMENT='用户信息表';
```