# SwapU 微服务改造方案 v2（2026-11 基线）

> 本文是按**改造后**的代码基线与新需求（RBAC、支付密码、P0 安全已落地）重写的执行方案。
> 旧稿 `swapu-microservices-migration.md` 保留为 v1 历史（其中的前置问题已完成，见其第 11 节）。

---

## 0. 结论摘要

1. **可以拆，但要克制**：建议 **5 个进程**（网关 + 账号 + 商品 + 订单 + 消息），而不是把 13 张表拆成 7~8 个服务。单人维护，进程越多收益递减、调试成本线性上升。
2. **前置工作已完成**：重复类已合并（94 → 12，且剩余均为各端自有基础设施）、admin 死代码已删除、P0 越权与参数校验已修、RBAC 已落地。**现在具备拆服务的条件**。
3. **最大变化是鉴权**：单体里"每请求查库取角色"，拆开后 admin 表只在账号服务。建议改用 **Redis 会话 + 网关注入 header**，角色变更立即生效，避免"角色写进 token 后要等过期"。
4. **一致性用 Outbox + Saga/TCC，不引入 Seata**：现有 `stock_change_log` + `StockSyncTask` 本来就是半个 Outbox，顺势升级成本最低。
5. **工期约 10~16 人日**（单人），分 7 个阶段，每阶段可独立验收、可回滚。

---

## 1. 当前基线（实测）

| 维度 | 数值 |
|---|---|
| 后端模块 | `SwapU_user` 91 类/7,052 行/57 接口/11 Controller；`swapu_admin` 79 类/4,943 行/26 接口/5 Controller |
| 公共模块 | `swapu_pojo` 52 类、`swapu_common` 14 类 |
| 合计 | **236 个 Java 类 / 约 13,600 行 / 83 个接口** |
| 前端 | 用户端 22 页/13,833 行；管理端 10 页/3,116 行（两个独立 Vite 项目） |
| 数据库 | **13 张表**，两个应用共用一个 `campus` 库 |
| 测试 | 10 个测试类 / **63 个用例**（管理端 40 + 用户端 23） |
| 技术栈 | Spring Boot 2.7.15 / JDK 17 / MyBatis + PageHelper / Redis / RabbitMQ / WebSocket / Bean Validation |
| 已具备的横切能力 | JWT 双端鉴权、RBAC（3 角色 4 权限点）、统一返回、全局异常、操作日志、Outbox 雏形、定时任务 |

13 张表：`user`、`address_book`、`admin`、`admin_operation_log`、`product`、`category`、`favorite`、`comment`、`audit_record`、`user_behavior_log`、`orders`、`stock_change_log`、`chat_message`。

---

## 2. 与 v1 方案的差异

| 项 | v1（改造前） | v2（现在） |
|---|---|---|
| 重复类 | 94 个同名类、admin 66% 是复制 | **已合并**，剩 12 个均为各端自有 Controller/Mapper/Config |
| 分层倒挂 | Service 注入 Controller | 仍在（`OrderServiceImpl` 注入 `ChatController`），拆消息服务时顺带解决 |
| 鉴权 | 只有 JWT | JWT + **RBAC 权限点**，拆服务后需要新的鉴权载体 |
| 管理端 | 只有后端 | 已有独立前端 10 个页面 |
| 支付 | MD5 密码、可并发双付 | BCrypt 独立支付密码 + 原子占位，已修 |

---

## 3. 服务划分

### 3.1 方案 A（推荐）：5 个进程

| 服务 | 端口 | 职责 | 独立库 | 代码来源 |
|---|---|---|---|---|
| `swapu-gateway` | 8080 | 路由、会话校验、限流、CORS | — | 新增 |
| `swapu-account` | 8101 | 用户/管理员认证、资料、地址、余额、支付密码、**管理员管理 + 操作日志 + 后台用户管理** | `swapu_account` | user/address + admin 全部 + 后台用户管理 |
| `swapu-product` | 8102 | 商品、分类、收藏、评论、库存、行为埋点、推荐、LLM 重排、**审核（商品/评论/流水）** | `swapu_product` | product/category/favorite/comment/behavior/stock/AI + 审核 |
| `swapu-order` | 8103 | 订单状态机、支付、退款、库存 Saga 协调、超时任务 | `swapu_order` | order + `stock_change_log`（升级为 outbox） |
| `swapu-message` | 8104 | WebSocket 聊天、系统消息 | `swapu_message` | chat + WebSocket + `SystemMessageAspect` 改事件驱动 |

**为什么这样切**：

- `message` 有状态（WS 连接池），必须独立扩容，且不持有交易数据 → 最适合第一个拆；
- `product` 是读多写少 + 缓存密集 + 计算重（推荐/AI），需要独立扩容；
- 审核与商品状态强相关（审核通过 = 改 `product.status` + 写 `audit_record`），放同一库可保持**本地事务**，不需要分布式事务；
- `account` 聚合了"账号与权限"，RBAC 的角色数据、操作日志都在这里，避免每个服务都要查管理员表；
- AI 暂留 `product` 内部（只有 2 个类），量大了再拆。

### 3.2 方案 B（完整）：7 个进程

在 A 的基础上再拆：

- `swapu-user`（用户资料/地址/余额）与 `swapu-auth`（认证令牌）分离；
- `swapu-ai`（LLM 客服 + 重排）独立，便于单独限流/熔断/换模型；
- `swapu-audit`（审核流水）独立。

代价：多 2~3 个进程、3 个跨服务调用、审核要拆成"改状态 + 写流水"两段（需 MQ 最终一致）。**仅当课程/答辩明确要求"多服务"时才选 B。**

### 3.3 管理端路由（方案 A）

管理端前端仍只连网关，按前缀分发：

| 管理端功能 | 网关路由到 |
|---|---|
| `/admin-api/admins/**`、`/admin-api/operation-log/**`、`/admin-api/users/**` | account |
| `/admin-api/audit/**`、`/admin-api/categories/**` | product |

---

## 4. 鉴权与 RBAC 的落地（本方案的关键差异）

### 4.1 问题

单体里 `AdminPermissionInterceptor` 每个受保护请求查一次 `admin` 表。拆开后 `admin` 表只在 `account` 服务，`product`/`order` 无法查 → 必须换载体。

### 4.2 方案：Redis 会话 + 网关注入 header

```
登录（account）→ 生成 token，同时写 Redis:  session:{token} -> {adminId, role, permissions[]}, TTL 30min
请求 → gateway 校验 token → 从 Redis 取身份 → 删掉外部传入的 X-* 头，重写 X-SwapU-User-Id / X-SwapU-Role / X-SwapU-Perms
      → 路由到目标服务
服务 → AuthHeaderInterceptor 读 header 塞进上下文 → @RequirePermission 做纯本地判断（不再查库）
```

**角色变更如何立即生效**：超管改某人角色/禁用时，`account` 服务**删除该管理员的会话 key**（或按 adminId 维护会话索引），下一次请求网关取不到角色 → 401，强制重新登录。这比"角色写进 token 等过期"更安全，也比"每个服务回查账号库"更便宜。

**共享组件**：`swapu-common-web` 里放 `@RequirePermission`、`AuthHeaderInterceptor`、`RequirePermissionInterceptor`（逻辑与现有 `AdminPermissionInterceptor` 一致，只是数据来源从 DB 换成 header）。两个前端应用的服务都引这个模块，规则只写一份。

**为什么不用 Spring Security**：现有实现只需"注解 + 拦截器"两个类，引入 Security 会带来过滤器链、CSRF、默认登录页等噪音，收益不成比例。

---

## 5. 数据拆分

### 5.1 13 张表 → 4 个库

| 库 | 表 |
|---|---|
| `swapu_account` | `user`、`address_book`、`admin`、`admin_operation_log` |
| `swapu_product` | `product`、`category`、`favorite`、`comment`、`audit_record`、`user_behavior_log` |
| `swapu_order` | `orders`、`stock_change_log`（重命名为 `outbox`） |
| `swapu_message` | `chat_message` |

**硬规则**：禁止跨库 JOIN；禁止服务直连别人的库（用独立 DB 账号 + 网络隔离）；跨服务只传 ID 与快照字段（`orders` 里已有的 `product_title/product_image/unit_price` 保留，明确为"下单快照"）。

### 5.2 跨服务一致性

| 场景 | 方案 |
|---|---|
| 下单扣库存 | **Outbox + MQ**：order 本地事务写 `orders` + `outbox(status=NEW)` → 投递 → product 幂等扣减（唯一键 `orderNo`）→ 回执推进；超时未回执则关单（复用现有 `OrderStatusTask`） |
| 余额支付 | **TCC**：order → account `try 冻结 / confirm 划转 / cancel 解冻`，`bizNo = orderNo` 幂等 |
| 审核通过/驳回 | product 本地事务（`product.status` + `audit_record` 同库） |
| 系统消息 | order 发 `swapu.order.status` → message 消费生成系统聊天（替代现有 `SystemMessageAspect` 的反射取参） |

**不引入 Seata AT**：需要每库 `undo_log`、对 SQL 有限制、运维组件多；本项目只有两处真正的分布式一致性问题，Saga/TCC 更可控。

---

## 6. 工程骨架

```
SwapU/
├── pom.xml                        # 父 POM：Boot 2.7.18 + Spring Cloud 2021.0.8 + SCA 2021.0.5.0
├── swapu-common/
│   ├── swapu-common-core/         # 常量、异常基类、工具、金额/时间
│   └── swapu-common-web/          # Result/PageResult、全局异常、AuthHeaderInterceptor、
│                                  # @RequirePermission、traceId 日志、MyBatis 基础配置
├── swapu-api/                     # Feign 接口 + 跨服务 DTO（严禁放 Entity）
├── swapu-gateway/
├── swapu-account-service/
├── swapu-product-service/
├── swapu-order-service/
├── swapu-message-service/
├── swapu_front/  swapu_admin_front/
└── deploy/
    ├── docker-compose.infra.yml   # mysql/redis/rabbitmq/nacos
    └── start-all.ps1
```

**契约层铁律**：`swapu-api` 只放 Feign + 跨服务 DTO；各服务自己维护 `entity`（持久化）与 `dto/vo`（对外）。这一条是防止回到"实体到处共享"的关键。

**版本选择**：保持 Spring Boot 2.7.18 + Spring Cloud 2021.0.8 + SCA 2021.0.5.0（Nacos 2.2、Sentinel 1.8.6）。不要升 Boot 3：`javax → jakarta` 全量迁移对本项目是纯成本。

---

## 7. 分阶段路线

| 阶段 | 内容 | 工期 | 验收 |
|---|---|---|---|
| **P1 工程骨架** | 父 POM + 三套 BOM；新增 `swapu-api`、`swapu-common-core`、`swapu-common-web`；**仍是单体**（两个应用照旧跑） | 1~2 人日 | 全量构建通过、63 个单测全绿 |
| **P2 网关 + 会话** | Redis 会话、`swapu-gateway` 落地、前端统一 `/api`、`/admin-api`、`/ws` | 1~2 人日 | 网关单入口；两个前端经网关跑通登录与主要页面；角色变更后 ≤5s 失效 |
| **P3 拆 message** | WS 独立；在线池换 Redis Pub/Sub 广播；`SystemMessageAspect` 改 MQ 事件 | 1~2 人日 | 停 message 不影响浏览下单；两个浏览器标签跨实例能收消息 |
| **P4 拆 product** | 商品/分类/收藏/评论/审核/推荐/AI；库存消费 Outbox；切 `swapu_product` 库 | 2~3 人日 | 下单→扣库存→审核流转正常；200 QPS 无超卖 |
| **P5 拆 order** | 订单状态机、支付 TCC、退款、超时任务、对账；切 `swapu_order` 库 | 2~3 人日 | 支付/退款/取消全链路；kill -9 product 后 order 最终一致 |
| **P6 拆 account** | 认证 + RBAC + 操作日志 + 后台用户管理；切 `swapu_account` 库 | 1~2 人日 | 三种角色登录后菜单/接口权限与现在一致 |
| **P7 收尾** | Sentinel 限流、SkyWalking 链路、每服务 Dockerfile + compose、压测报告、文档 | 2 人日 | 一条命令拉起全部依赖与服务 |

**合计 10~16 人日**。建议 P1、P2 做完先停一下评估：这两阶段不改业务语义，风险最低，也最能暴露"是否真的需要拆"。

---

## 8. 风险与对策

| 风险 | 说明 / 对策 |
|---|---|
| 63 个单测大面积失效 | Service 依赖变化会导致 Mock 失效；每拆一个服务同步补该服务的集成测试，不要留到最后 |
| 本地开发变重（5 进程 + 4 中间件） | `docker-compose.infra.yml` 起中间件 + `start-all.ps1` 分批启服务；先保证 P2 单命令可跑 |
| 分布式事务出 bug | Outbox + 唯一键幂等 + 每日库存/资金对账报告 |
| 角色变更延迟 | Redis 会话 + 主动失效，实测 ≤5s；不要退回"角色写 token" |
| 工期失控 | 每阶段独立可交付；P1/P2 不改变业务语义，随时可回滚 |
| 需求漂移 | 拆服务期间**冻结新功能**，否则每加一个功能都要在 5 个服务里改 |

---

## 9. 反模式（明确不做）

| 不做 | 原因 |
|---|---|
| K8s / Istio / Service Mesh | 单人项目撑不起；compose 足够 |
| Seata AT 全量覆盖 | 见 5.2 |
| 把收藏/评论/分类再拆成独立服务 | 拆分过度，制造跨服务 JOIN |
| 跨服务共享 Entity | 会让服务无法独立演进，等同没拆 |
| 两个应用继续共用一个库 | 分阶段期可共存，但 P6 结束必须切完 |
| 无测量数据就宣称性能提升 | 延续现有文档里"80ms→5ms"那种无依据说法的习惯要改 |

---

## 10. 需要你确认的 5 个决策

1. **服务粒度**：方案 A（5 个，推荐）还是方案 B（7 个）？
2. **鉴权载体**：Redis 会话（推荐，变更即时生效）还是把 `role` 放进 JWT（少一个 Redis 依赖，但降级要等 token 过期）？
3. **管理端接入**：前端直连网关按前缀分发（推荐）还是加一个 `admin-BFF` 聚合层？
4. **本地基础设施**：`docker-compose`（推荐）还是继续用你机器上已装的 MySQL/Redis/RabbitMQ？
5. **注册中心/配置中心**：Nacos（SCA 生态，推荐）还是 Eureka + Config？

---

## 11. 如果只想"讲得清楚"而不想真拆

拆 5 个服务后，简历/答辩上能讲的点其实只有 5 个：**网关鉴权、服务发现、Feign 契约、Outbox 最终一致、独立扩容**。

如果只是需要这些点，还有一个成本低得多的折中：**只做 P1 + P2**（模块化 + 网关 + 会话鉴权），代码按服务边界分包（`com.itsean.swapu.product.*`），仍是 2 个进程。这样同样能讲清网关与鉴权，但少了"服务发现/独立部署"的故事。

要不要真拆，取决于你更缺"可讲的故事"还是"可维护的工程"。我倾向：**如果是答辩/简历，做到 P3（消息服务独立）就有足够的故事，且风险最低**。
