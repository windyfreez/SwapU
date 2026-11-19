# SwapU 微服务化改造方案

> 目标：把当前"名义上分模块、实际高度耦合"的双应用单体，改造成边界清晰、可独立部署的标准微服务架构。
> 适用版本：Spring Boot 2.7.18 / Spring Cloud 2021.0.8 / Spring Cloud Alibaba 2021.0.5.0 / JDK 17
> 编写依据：仓库当前状态（`90f99fa`，用户端 147 类、管理端 141 类、pojo 51 类、common 14 类，user 与 admin 同名类 93 个）

---

## 0. 结论摘要（TL;DR）

1. **不要直接拆进程。** 现在拆成 7 个服务，等于把 93 个重复类复制成 `93 × 7` 个副本，耦合不减反增。
2. **耦合乱的根因不是"模块没分"，而是三件事没做**：共享数据库、共享 Entity、反向依赖。这三件事不解决，微服务只是把单体的问题分布式化。
3. **正确顺序**：模块化单体（切边界）→ 工程骨架（契约层）→ 网关 + 第一个服务 → 核心域拆分 → 观测性收尾。
4. **最短可信路径 3~5 周单人**，其中 Phase 0 的 2~3 天是"不做就白做"的前置条件。
5. **不引入 Seata AT**，用「本地消息表(Outbox) + RabbitMQ + 幂等消费 + TCC 用于余额」，因为本项目只有 2 处跨服务一致性诉求，Saga 补偿更简单可控。
6. **现有代码里已经有一个可复用的资产**：`stock_change_log` + `StockSyncTask` 的双兜底设计，本质上就是 Outbox + 对账，顺手就能升级成微服务的 Saga 骨架。

---

## 1. 现状诊断：为什么"分了模块还是乱"

### 1.1 病根一：模块被"抄"而不是被"依赖"

| 证据 | 说明 |
|---|---|
| `SwapU_user` 有 45 个类与 `swapu_pojo` 同名，13 个与 `swapu_common` 同名 | 公共模块抽出来了，但业务模块自己仍保留一份副本，谁也没真的去用 |
| `swapu_admin` 有 **93 个类**与 `SwapU_user` 同名（占 admin 全部类的 66%） | 整个用户端被复制了一遍 |
| `swapu_admin` 的 11 个 Service 接口中，**7 个不被任何 Controller 引用**（AddressBook/Chat/Favorite/Order/Product/ProductView/User），其中 4 个连对外引用都是 0 | 复制过来后没有删的脚手架，纯死代码 |
| `SwapU_user` 同时 import `com.itsean.common.constant.MessageConstant` 和 `com.itsean.campus_second_hand.constant.MessageConstant` | 同一个常量有两份，改一处必漏一处 |

**结论**：抽模块这个动作只增加了"副本数量"，没有形成"单向依赖"。Maven 模块只是打包边界，没有依赖约束，所以谁都能 import 谁。

### 1.2 病根二：共享数据库 —— 最大的耦合点

两个应用连同一个 schema（`jdbc:mysql://localhost:3306/campus`），管理端直接 `update product set status = ...`。

代码层面可以假装有边界，数据库层面完全没有。只要两边都能写同一张表：
- 任何一方改表结构，另一方静默出问题；
- 无法独立部署、独立演进；
- 事务边界横跨两个应用，出问题无法归因。

**共享数据库是比代码耦合更致命的耦合**，而它恰好是微服务最强调要切开的东西。

### 1.3 病根三：Entity 同时是持久化对象、接口契约、跨端传输对象

`Product` 这个实体被用在：Controller 出参、Service 内部、Redis 缓存 JSON、管理端审核 VO 的字段来源。

后果：
- 加一个字段，缓存反序列化、前端展示、管理端列表全要跟着改；
- 数据库列名（snake_case）泄漏到接口契约；
- 实体一旦被跨服务共享，服务就无法独立演进。

### 1.4 病根四：反向依赖与脆弱的横切

| 位置 | 问题 |
|---|---|
| `OrderServiceImpl` 注入 `ChatController` | Service 依赖 Controller，分层倒挂 |
| `SystemMessageAspect` 注入 `ChatController`，并用反射从 Controller 入参里按字段名取 `orderNo` | 跨切面耦合到 Controller 方法名和参数结构；改个方法名，系统消息静默消失 |
| `BehaviorAspect` 按 Controller 方法名（`getProductById`/`addFavorite`/`createOrder`）判断行为类型 | 埋点逻辑寄生在 Web 层签名上，无法脱离 HTTP 复用 |

在微服务里这三处会直接失效——因为 Controller 和各服务不在一个进程里了。

### 1.5 病根五：没有技术手段强制边界

没有 ArchUnit / 没有 API 模块 / 没有 internal 包约定。边界靠自觉，而自觉必然失守。

---

## 2. 目标架构

```
                         浏览器
              ┌────────────┴────────────┐
        swapu_front :5173        swapu_admin_front :5174
              └────────────┬────────────┘
                     [Nginx]
        /api/**  /admin-api/**  /ws/**
                          │
                 ┌────────▼────────┐
                 │ swapu-gateway   │ :8080
                 │ 路由/验签/限流/CORS│
                 └────────┬────────┘
                          │ Nacos 服务发现 + Spring Cloud LoadBalancer
   ┌──────────┬───────────┼───────────┬───────────┬────────────┐
   ▼          ▼           ▼           ▼           ▼            ▼
 auth      user       product      order      message       admin        ai
:8101     :8102       :8103       :8104       :8105        :8106       :8107
凭证/令牌  资料/地址   商品/分类    订单状态机  聊天 WS      审核/运营     LLM 客服
            余额      收藏/评论    支付/退款   系统消息      用户管理     语义重排
             │        库存/行为                                统计
             │        推荐召回
   └──────────┴───────────┴───────────┴───────────┴────────────┘
                          │
   MySQL(每服务独库) · Redis · RabbitMQ · OSS · DeepSeek API
   横切：Nacos Config · Sentinel · SkyWalking · docker-compose
```

### 2.1 版本矩阵（重要，别升到 Spring Boot 3）

| 组件 | 版本 | 说明 |
|---|---|---|
| JDK | 17 | 项目已是 17 |
| Spring Boot | 2.7.18 | 2.x 最后一版，与现有代码零改动 |
| Spring Cloud | 2021.0.8 | 对应 Boot 2.6~2.7 |
| Spring Cloud Alibaba | 2021.0.5.0 | 官方表标注 2.6.13，2.7.x 社区广泛验证可用 |
| Nacos | 2.2.0+ | 注册 + 配置中心 |
| Sentinel | 1.8.6 | 随 SCA BOM |
| Seata | **不用** | 见 §4 |
| Spring Cloud Gateway | 2021.0.8 | 基于 WebFlux |

> 不要升 Spring Boot 3：那需要 `javax.*` → `jakarta.*` 全量迁移（knife4j 3.0.2 也要换），对本项目是纯粹的成本。
> 参考：[Spring Cloud Alibaba 版本说明](https://sca.aliyun.com/docs/2021/overview/version-explain/)、[Spring Cloud 版本对应](https://spring.io/projects/spring-cloud)

### 2.2 为什么是这 7 个服务（而不是 4 个或 12 个）

| 服务 | 拆分理由（可独立扩容/独立发布的具体诉求） |
|---|---|
| gateway | 唯一入口，收敛 CORS/鉴权/限流；前端只需配一个代理前缀 |
| **message** | WebSocket 是**有状态**的，需要粘性会话、独立连接数与内存规格；且不持有核心交易数据 → **最适合第一个拆** |
| **ai** | LLM 调用慢（秒级）、贵、易失败，必须独立限流/熔断/降级；product 只依赖它的一个接口 |
| **product** | 读多写少、缓存密集、推荐计算重 → 需要独立扩容；被 order/message/admin 多方读取 |
| order | 写密集 + 事务边界；状态机独立演进 |
| auth | 凭证与令牌签发，被全站依赖，需要独立的安全基线 |
| user | 资料 + 余额（资产），变更频率低但正确性要求最高 |
| admin | 低频写、独立鉴权体系（管理员令牌），与用户端不同频扩容 |

**明确不拆出来的**：收藏、评论、分类、地址、行为埋点——它们都是所属聚合的一部分，单独拆只会制造跨服务 join。

**网关顺带解决的一个现存隐患**：当前 `swapu_admin_front` 的页面路由和 API 前缀都是 `/admin/*`，靠 `vite.config.js` 里 `spaBypass` 嗅探 `Accept: text/html` 来区分"这是页面还是接口"。这个判断在反向代理、预渲染、部分客户端下并不可靠。改造后页面走 `/admin/*`、接口走 `/admin-api/**`，前缀彻底分开，嗅探逻辑可以删掉。

---

## 3. 服务边界与代码归属映射

### 3.1 现有类 → 目标服务

| 目标服务 | 迁移的现有代码 |
|---|---|
| `swapu-gateway` | 新增。吸收 `WebMvcConfiguration` 里的 CORS/静态资源职责，`JwtToken*Interceptor` 的验签职责上移 |
| `swapu-auth` | `UserController` 的 login/register/password、`AdminController` 的 login、`JwtUtil`、`JwtProperties`、`JwtClaimsConstant` |
| `swapu-user` | `UserServiceImpl`(getInfo/update/getUserById/getUserProfile)、`AddressBookController/Service/Mapper`、余额账户 |
| `swapu-product` | `ProductController/Service`、`CategoryController/Service`、`FavoriteController/Service`、`CommentController/Service`、`ViewController`、`ProductViewService`、`StockService`、`BehaviorAspect`/`BehaviorBufferService`/`BehaviorTask`、`UserProfileTask`、`HotProductTask`、`ViewCountSyncTask`、`recommend()` 召回部分 |
| `swapu-order` | `OrderController/Service`、`OrderStatusTask`、`StockChangeProducer/Consumer`（升级为 Outbox）|
| `swapu-message` | `ChatController/Service`、`ChatWebSocketHandler`、`SystemMessageAspect`（改为事件驱动）|
| `swapu-admin` | `AuditController/Service`、`UserManageController/Service`、管理端 `CategoryController/Service`、`AdminController` 的 CRUD、`AuditRecordMapper` |
| `swapu-ai` | `AIController/Service`（客服）、`ReRankService`（重排）、`DeepSeekProperties` |

### 3.2 数据库拆分

| 库 | 表 |
|---|---|
| `swapu_auth` | `admin`、`user_credential`（从 user 表拆出 password/status 等凭证字段）|
| `swapu_user` | `user`、`address_book`、`account`（余额）|
| `swapu_product` | `product`、`category`、`favorite`、`comment`、`user_behavior_log`、`stock` |
| `swapu_order` | `orders`、`outbox`（现 `stock_change_log` 升级）、`payment` |
| `swapu_message` | `chat_message`、`chat_session` |
| `swapu_admin` | `audit_record`、`operation_log` |
| `swapu_ai` | 无业务库（仅调用日志，可选）|

**硬规则**：
1. 禁止跨库 JOIN；禁止服务直连别人的库（用独立 DB 账号 + 网络隔离保证）。
2. 跨服务取数据只能走 Feign 拿快照，并把快照冗余落库。`orders` 里已有的 `product_title`/`product_image`/`unit_price` 就是这个模式，**保留并明确为"下单快照"**——这是正确做法，不是冗余。
3. 用户信息只存 `swapu_user`，其他服务只存 `userId` + 展示所需快照（昵称/头像）。

### 3.3 契约层：`swapu-api`

```
swapu-api/
  ├── product/ProductFeignClient.java + ProductSnapshotDTO
  ├── user/UserFeignClient.java + UserSnapshotDTO
  ├── account/AccountFeignClient.java + TransferDTO
  ├── message/MessageFeignClient.java + SystemMessageDTO
  └── ai/AiFeignClient.java + RerankDTO
```

**铁律：`swapu-api` 只放 Feign 接口 + 跨服务 DTO，绝不放 Entity。**

各服务自己维护 `entity/`（数据库映射）与 `dto/`+`vo/`（对外契约），通过 BeanUtils/MapStruct 转换。

这一条直接消灭"93 个重复类"的根源：实体不再是共享物，只属于它的服务。

---

## 4. 数据一致性方案

### 4.1 选型：Outbox + Saga，不用 Seata AT

| 方案 | 结论 | 理由 |
|---|---|---|
| Seata AT | ✗ 不用 | 需要每库 undo_log、对 SQL 有限制、运维组件多；本项目只有 2 处一致性问题，收益远小于成本 |
| 本地消息表(Outbox) + MQ + 幂等消费 | ✓ 主方案 | 与现有 `stock_change_log` + `StockSyncTask` 天然同构，改造成本最低 |
| TCC | ✓ 仅用于余额 | 资金类需要 try/confirm/cancel 两阶段冻结 |

### 4.2 下单链路（Saga）

```
order-service                              product-service
──────────────                             ────────────────
① 本地事务：
   insert orders(status=WAIT_ACCEPT)
   insert outbox(event=DEDUCT_STOCK,
                 bizNo=orderNo, status=NEW)
② 提交后投递 MQ ─────────────────────▶ ③ 幂等消费：
                                          insert stock_change(orderNo, type)
                                          ON DUPLICATE KEY → 已处理则丢弃
                                          update product set quantity=quantity-?
                                           where id=? and quantity>=?
④ 收到 STOCK_DEDUCTED 回执 ◀──────────  ④ 回执 exchange
   outbox.status=DONE
⑤ 超时未回执 → 关单（cancelIfCancellable）
```

**关键点**：
- 幂等键：`(orderNo, changeType)` 唯一索引，重复消息直接丢弃；
- 补偿：`StockSyncTask` 保留为对账任务，扫描 `outbox.status=NEW AND create_time < now-5min` 补投；
- 失败不可丢：`registerStockChangeAfterCommit` 继续用，失败只记日志交对账；
- **Redis 预扣降级为"前置流量挡板"**，不再作为库存真相来源——库存真相永远在 `swapu_product` 的 DB 里。目前代码已经是这个思路（Redis 失败降级 DB），改造时把这个语义写死。

### 4.3 余额支付（TCC）

```
order-service → account-service
① try:   冻结 buyer 余额  （freeze_amount += total，bizNo 幂等）
② confirm: 扣减冻结 → 加给 seller（同一本地事务）
③ cancel:  解冻
```

- 所有操作以 `bizNo = orderNo + 动作` 做幂等键；
- 余额变更必须留 `account_flow` 流水（现有代码直接 `update user set balance` 且允许卖家为负，这是缺陷，改造时必须补流水与不为负约束）。

### 4.4 MQ 约定

| Topic | 生产者 | 消费者 |
|---|---|---|
| `swapu.stock.deduct` | order | product |
| `swapu.stock.result` | product | order |
| `swapu.order.status` | order | message（生成系统消息）、product（售出状态）|
| `swapu.behavior.log` | product | product 自身异步落库 |

所有消费者必须：手动 ACK + 幂等表 + 失败进死信队列。

---

## 5. 认证与链路安全

### 5.1 职责重划

| 层 | 职责 |
|---|---|
| gateway | 验签 JWT、解析 `userId`/`role`、**剔除外部传入的 `X-SwapU-*` 头**、重写为可信 header、限流 |
| 下游服务 | 只读 header，不解析 token、不查库；统一 `AuthHeaderInterceptor` → `UserContext`(ThreadLocal) |
| service→service | Feign 拦截器带 `X-Internal-Token`（HMAC(secret, body+timestamp)），防止绕过网关直连 |

`BaseContext` + `JwtTokenUserInterceptor` 的 ThreadLocal 方案保留，但**每个服务各自一份**，且必须 `afterCompletion` 清理（现有代码已修，别丢）。

### 5.2 令牌隔离

现在是 `admin-secret-key` 和 `user-secret-key` **同值 `itcast`**，隔离实际靠 claim 名（`ADMIN_ID` vs `USER_ID`）。改造时必须：
- 两套独立密钥（不同值、不同长度，走 Nacos 加密配置）；
- admin 令牌带 `role=ADMIN`，gateway 对 `/admin-api/**` 额外校验 role；
- 密钥禁止硬编码进 `application.yml` 默认值。

### 5.3 WebSocket

浏览器 → gateway `/ws/**`（`ws: true` 透传）→ message-service。
token 仍走 query 参数（现有前端 `chatSocket.js` 不用改），但 gateway 需要在 Upgrade 前校验一次 token。
**多实例前提**：message-service 扩容后在线池必须外置——用 Redis Pub/Sub 或 STOMP+Broker 广播，否则 A 实例的用户收不到 B 实例推送的消息。这是拆 message 服务时必须一起做的（当前是进程内 `Map<Long, Set<Session>>`）。

---

## 6. 工程骨架

```
SwapU/
├── pom.xml                          # 父 POM：dependencyManagement 全量锁版本
├── swapu-dependencies/              # 可选：BOM
├── swapu-common/
│   ├── swapu-common-core/           # 常量、异常基类、工具、金额/时间工具
│   └── swapu-common-web/            # Result/PageResult、全局异常、AuthHeaderInterceptor、
│                                    # MyBatis-Plus/PageHelper 配置、traceId 日志切面
├── swapu-api/                       # Feign 契约（无 Entity）
├── swapu-gateway/
├── swapu-auth-service/
├── swapu-user-service/
├── swapu-product-service/
├── swapu-order-service/
├── swapu-message-service/
├── swapu-admin-service/
├── swapu-ai-service/
├── swapu_front/
├── swapu_admin_front/
└── deploy/
    ├── docker-compose.infra.yml     # mysql/redis/rabbitmq/nacos
    └── nacos/                       # 各服务 DataId 配置
```

### 6.1 父 POM 关键片段

```xml
<properties>
    <java.version>17</java.version>
    <spring-boot.version>2.7.18</spring-boot.version>
    <spring-cloud.version>2021.0.8</spring-cloud.version>
    <spring-cloud-alibaba.version>2021.0.5.0</spring-cloud-alibaba.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>${spring-boot.version}</version>
            <type>pom</type><scope>import</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type><scope>import</scope>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-alibaba-dependencies</artifactId>
            <version>${spring-cloud-alibaba.version}</version>
            <type>pom</type><scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 6.2 Gateway 路由与验签

```java
@Bean
public RouteLocator routes(RouteLocatorBuilder b) {
    return b.routes()
        .route("product", r -> r.path("/api/product/**", "/api/category/**")
            .filters(f -> f.stripPrefix(1).filter(authFilter))
            .uri("lb://swapu-product"))
        .route("order", r -> r.path("/api/order/**")
            .filters(f -> f.stripPrefix(1).filter(authFilter))
            .uri("lb://swapu-order"))
        .route("admin", r -> r.path("/admin-api/**")
            .filters(f -> f.stripPrefix(1).filter(adminAuthFilter))
            .uri("lb://swapu-admin"))
        .route("ws", r -> r.path("/ws/**")
            .uri("lb:ws://swapu-message"))
        .build();
}
```

验签过滤器要点：
1. 白名单（login/register/公开商品接口）直接放行；
2. **先删** `X-SwapU-User-Id`/`X-SwapU-Role`（防止伪造），再按 token 写入；
3. 失败返回 401 JSON，与现有 `Result` 结构保持一致。

### 6.3 Feign + 降级

```java
@FeignClient(name = "swapu-ai", fallbackFactory = AiClientFallbackFactory.class)
public interface AiFeignClient {
    @PostMapping("/internal/ai/rerank")
    List<Long> rerank(@RequestBody RerankDTO dto);
}
```
`fallbackFactory` 里返回原顺序 —— 保留现有 `ReRankServiceImpl` "重排失败静默回退"的语义，这个设计是对的，别改掉。

### 6.4 Outbox 发布（替代 `registerStockChangeAfterCommit`）

```java
@Transactional
public OrderVO createOrder(OrderDTO dto) {
    orderMapper.insert(order);
    outboxMapper.insert(Outbox.of(DEDUCT_STOCK, order.getOrderNo(), payload));
    return vo;
}

// 事务提交后投递；失败留给对账任务
@TransactionalEventListener(phase = AFTER_COMMIT)
public void publish(OutboxCreatedEvent e) {
    outboxPublisher.trySend(e.getBizNo());
}
```

### 6.5 消除倒挂：系统消息改事件驱动

现状：`SystemMessageAspect` 拦截 Controller + 反射取参 → 改为：

```java
// order-service
applicationEventPublisher.publishEvent(new OrderStatusChangedEvent(orderNo, from, to));

// message-service 消费 MQ: swapu.order.status
@RabbitListener(queues = "swapu.order.status.message")
public void onStatusChanged(OrderStatusChangedMessage msg) { ... }
```

好处：不再依赖方法名、不再跨层注入 Controller、消息逻辑可以独立测试。

---

## 7. 分阶段实施路线

> 每个阶段结束时项目都必须**可运行、可回归**。不允许出现"拆到一半跑不起来"的状态。

### Phase 0 —— 模块化单体清理（2~3 人日）★ 不可跳过

| 动作 | 验收 |
|---|---|
| 以 `swapu_pojo` 为唯一来源，删除 `SwapU_user`/`swapu_admin` 内的 93+45+13 个副本类 | 同名类交集 = 0 |
| 删除 `swapu_admin` 中无 Controller 引用的 user 侧死代码（7 个无引用的 Service 及其 Mapper/XML，如 `OrderMapper.xml`、`ChatMapper.xml`、`FavoriteMapper.xml`、`AddressBookMapper.xml` 中无管理端功能的部分） | `swapu_admin` 活跃调用链只剩 Admin/Audit/Category/UserManage 四条；保守估计可删 ≥40 个类 |
| 抽 `SystemMessageService`，解除 Service→Controller 倒挂 | 全仓无 `service` 包 import `controller` 包 |
| 修复 P0：订单归属校验、DTO 加 `@Valid`、支付密码独立化 | 越权用例全部 403 |
| 加 ArchUnit 三条规则 | CI 里跑通 |

> **为什么必须先做**：微服务会把上述问题按服务数量放大。带着 93 个重复类和反向依赖去拆服务，是负收益重构。

### Phase 1 —— 工程骨架（2~3 人日）

- 父 POM + 三套 BOM 锁版本；
- 新增 `swapu-api`、`swapu-common-core`、`swapu-common-web`；
- 配置外置到 Nacos（先只做配置中心，不做服务发现）；
- **仍是单体部署**，保证业务不回归。

### Phase 2 —— 网关 + 第一个服务（3~5 人日）

- `swapu-gateway` 上线，两个前端统一打到 `/api`、`/admin-api`；
- 拆 `swapu-message-service`（含 Redis Pub/Sub 广播改造）；
- 验收：网关单入口；停掉 message 不影响商品浏览；两个标签页跨实例能收消息。

### Phase 3 —— 核心域（5~8 人日）

- `swapu-product-service`（商品/分类/收藏/评论/库存/行为/推荐）+ `swapu-ai-service`（重排）；
- `swapu-order-service`（状态机 + Outbox + Saga + 余额 TCC）；
- 数据按 §3.2 拆库（先脚本双写迁移，再切读）；
- 验收：下单→扣库存→支付→退款全链路；JMeter 200 QPS 下单**无超卖、无超卖回补错误**；kill -9 product 后 order 能降级并最终一致。

### Phase 4 —— 收尾（3~5 人日）

- `swapu-auth/user/admin` 拆分；
- Sentinel 限流（AI 接口、下单接口）、SkyWalking 链路追踪；
- 每服务 Dockerfile + `deploy/docker-compose.yml` 一键起；
- 重写 README：架构图、端口表、启动顺序、Nacos 配置清单。

### Phase 5（可选）

灰度发布、Prometheus+Grafana 看板、压测报告、管理端前端并入主前端。

**累计约 15~24 人日（3~5 周）。**

---

## 8. 反模式清单（明确不做）

| 不做 | 原因 |
|---|---|
| K8s / Istio / Service Mesh | 单人项目，docker-compose 足够；Mesh 引入的复杂度无法消化 |
| Seata AT 全量覆盖 | 见 §4.1 |
| 把收藏/评论/分类拆成独立服务 | 拆分过度，制造跨服务 join |
| 跨服务 JOIN 或共享数据库 | 等于没拆 |
| 保留两个应用连同一个 schema | 分阶段期间可以临时共存，但 Phase 3 结束必须切完 |
| 无测量数据就宣称性能提升 | 现有文档里"80~120ms 降到 5ms"没有可复现依据，别延续这个习惯 |
| 为拆而拆 | 每个服务必须能回答"它需要独立扩容/独立发布/独立安全基线"中的至少一条 |

---

## 9. 主要风险与对策

| 风险 | 对策 |
|---|---|
| 分布式一致性出 bug | Outbox + 幂等键 + 对账任务 + 每日资金/库存对账报告 |
| 本地开发变复杂（8 个进程） | `docker-compose.infra.yml` + 各服务 profile；提供 `start-all.ps1` |
| 跨服务调用链难排查 | 第一版就接入 traceId（MDC 透传到 Feign/MQ header），再上 SkyWalking |
| 工期失控 | Phase 0/1 不产生"可见成果"，容易半途而废；建议先把 Phase 0 做完再评估 |
| 面试被质疑过度设计 | 准备好一句话："我需要让商品读路径独立扩容、让 AI 慢调用不拖垮交易链路" |

---

## 10. 取舍建议

如果目标是**简历/答辩**，最小可信版本是 4 个进程：

```
gateway + (auth&user 合一) + product(+ai) + order(+message)
```

足以讲清网关鉴权、服务发现、Feign、Outbox 一致性、独立扩容这五个点，工期压到 2 周左右，维护成本减半。

7 个服务是"工程完整性"版本，适合课程要求明确写了"微服务"的场景。

---

## 附：与现有代码的兼容性说明

- 现有 `StockChangeLog` 表结构基本可直接复用为 `outbox`（改表名 + 加 `event_type`/`payload` 字段）；
- 现有 `StockSyncTask` 两个定时任务拆成：`OutboxRetryTask`（补投）+ `StockReconcileTask`（对账）；
- 现有 `ReRankServiceImpl` 的重试与回退逻辑保留，只把 HTTP 调用换成 Feign；
- 现有 `ChatWebSocketHandler` 的 token 鉴权与多标签页支持保留，只把在线池换成 Redis 广播；
- 现有 `Result`/`PageResult`/`GlobalExceptionHandler` 上移到 `swapu-common-web`，字段结构不变 → 前端零改动；
- 现有 `docs/swapu_interface_doc.md` 的接口路径可平移为 `/api/xxx`（gateway stripPrefix），前端只需改 `vite.config.js` 的 proxy 目标。

---

## 11. 实施记录

### Phase 0-1 已完成：重复类合并（2026-11-19）

目标：让每个逻辑类只存在一处定义。规范位置 = `swapu_pojo` / `swapu_common`。

**结果**

| 指标 | 合并前 | 合并后 |
|---|---|---|
| Java 文件总数（4 个后端模块） | 353 | **236**（-117） |
| 跨模块同名类 | 94 | **35**（全部为各端自有的 Controller/Service/Mapper/Config，非数据模型） |
| `SwapU_user` 类数 | 147 | 89 |
| `swapu_admin` 类数 | 141 | 82 |
| `swapu_pojo` / `swapu_common` | 51 / 14 | 51 / 14（内容变为超集） |

- `mvn -pl swapu_pojo,swapu_common,SwapU_user,swapu_admin -am test-compile` **BUILD SUCCESS**（JDK 17）。

**关键处理**

1. **内容一致的 54 个类**：直接删除 user/admin 副本，全仓改写 import 与 Mapper XML 中的 `type` 全限定名（XML 必须一起改，否则运行期才报错）。
2. **已分叉的 15 个类**按"取超集"合并，并记录取舍：
   - `Product.categoryId`：统一为 `Long`（推荐链路 `Map<Long,String>` 依赖它）；
   - `Category`：采用 admin 版（私有字段 + `Integer` + 注释），已确认全仓无直接字段访问；
   - `Order` / `OrderDetailVO` / `OrderStatisticsVO`：采用 user 版（含退款状态 7/8/9 与统计字段）；
   - `Admin`：采用 admin 版（更完整的字段与 `@JsonIgnore`）；
   - `BaseException`：采用 admin 版（带 `code`），默认值由 `ErrorCodeConstant.UNAUTHORIZED` 改为字面量 `401`，避免 common 反向依赖 admin。
3. **三个常量类做对称并集**：`MessageConstant` 73 个、`NumberConstant` 47 个、`StringConstant` 34 个声明，零重复；唯一同名异值 `NEED_MORE_THAN_STORE`（"商品库存不足！" vs 长句）保留 user 原值。
4. **配置前缀统一**：`JwtProperties` / `AliOssProperties` 的 `@ConfigurationProperties` 前缀统一为 `swapu.jwt` / `swapu.alioss`，并把 `swapu_admin` 的 `application.yml(.example)` 从 `sky:` 改为 `swapu:`。
5. **Bean 扫描**：两个启动类补 `@ComponentScan({"应用包", "com.itsean.common"})`，否则公共模块里的 `@Component` 配置类不会被注册。
6. **修掉一处只有运行期才暴露的冲突**：user 的 `GlobalExceptionHandler` 原有两个处理器分别对应"本模块 BaseException"和"common BaseException"，合并后指向同一类型会触发 Spring `Ambiguous @ExceptionHandler`，已合并为一个；同时把兜底日志改为打印堆栈。

**遗留（下一步）**

- ~~剩余 35 个同名类中，**18 个是 `swapu_admin` 的死代码**~~ → 已完成，见下方 Phase 0-2；
- 另 17 个中剩下的部分（`WebMvcConfiguration`、`JwtTokenUserInterceptor`、`GlobalExceptionHandler`、`JsonStringListTypeHandler`、`RedisConfiguration`、各 Mapper）**职责本就不同，不应合并**，属于 Phase 1 抽 `swapu-common-web` 的范畴；
- `NumberConstant`/`StringConstant` 中评论状态存在两套命名（`COMMENT_STATUS_CHECKING/APPROVED/VIOLATION` 与 `PENDING/PUBLISHED/REJECTED`，值相同），已原样保留两份，建议后续统一命名。

### Phase 0-2 已完成：删除 swapu_admin 死代码（2026-11-19）

方法：不做"照名单删文件"，而是先算**引用闭包**（从 4 个 Controller + `@Configuration` + `@RestControllerAdvice` 出发，并补上"接口 → 实现类"这条边），再人工复核仅靠静态分析判不准的项。

| 指标 | 本步前 | 本步后 |
|---|---|---|
| 后端 Java 类总数 | 236 | **213** |
| `swapu_admin` 类数 | 82 | **60** |
| 跨模块同名类 | 35 | **12** |

**删除（26 个文件）**

- 7 个 Service 接口 + 7 个 Impl：AddressBook、Chat、Favorite、Order、Product、ProductView、User；
- 3 个 Mapper + 3 个 XML：AddressBook、Chat、Favorite；
- 二阶死代码：`ChatWebSocketHandler`、`WebSocketConfiguration`（admin 前端无聊天）、`JwtTokenUserInterceptor`（admin 无用户端接口）、`OssConfiguration`（admin 无上传接口）、`SimpleRandomSortUtil`；
- 用户端对称残留：`mapper/AdminMapper.java`（空接口、无 XML、零引用）。

**静态分析没判准、靠人工复核留下的 2 个**

| 类 | 自动判定 | 实际 | 依据 |
|---|---|---|---|
| `JsonStringListTypeHandler` | 死代码 | **必须保留** | 被存活的 `ProductMapper.xml`、`CommentMapper.xml` 以 `typeHandler=` 引用（XML 不在 Java 引用图里） |
| `RedisConfiguration` | 存活的配置类 | **保留** | `CategoryServiceImpl` 有 5 处 Redis 调用 |

**连带修正**

- `WebMvcConfiguration` 去掉"用户端接口"Swagger 分组与用户端拦截器注册，只保留 `/admin/**` 一条链路；
- `pom.xml` 移除 `spring-boot-starter-websocket` 与 `aliyun-sdk-oss`（OSS 由 `swapu_common` 统一提供）；
- 清理 4 个空包目录（`context`、`properties`、`utils`、`entity/result`）。

**剩余 12 个同名类的处置结论**：`CategoryController/Service(Impl)/Mapper` 是两端不同职责（用户端只读列表、管理端 CRUD），`*Mapper` 是两端各自查询同一批表，`WebMvcConfiguration`/`GlobalExceptionHandler`/`RedisConfiguration` 是各端 Web 层配置——均**不应合并**，属于 Phase 1 抽取公共 Web 组件的范围，且这几处需要在 `swapu-api` 定型后再动。

### Phase 0-3 已完成：订单越权与参数校验（P0 安全，2026-11-19）

**背景**：订单接口只校验"订单当前状态"，不校验"当前用户是不是这笔订单的买家/卖家"，任何登录用户拿到 `orderNo` 就能替别人发货、确认收货、查看含真实姓名手机号的详情。

**接口级归属校验**

| 接口 | 修复前的缺陷 | 修复后 |
|---|---|---|
| `POST /order/create` | `quantity` 无校验，传负数会让 Redis `DECRBY -n` **反向加库存**、且 `amount = 单价 × 负数` 为负 | DTO `@Min(1)` + service 层二次兜底 |
| `POST /order/confirm` | 无身份校验、无状态校验（可把已支付订单打回待支付）；`freight` 为 null 时 NPE | 仅卖家 + 仅待接单；运费缺省按 0 |
| `GET /order/{orderNo}` | 任何人可按单号查任意订单 | 仅买家/卖家 |
| `GET /order/detail/{orderNo}` | 同上，且泄露双方姓名手机号 | 仅买家/卖家 |
| `POST /order/pay` | 无买家校验；支付密码为空时 NPE 变 500 | 仅买家；空密码返回业务异常 |
| `POST /order/deliver` | 任何人可替卖家发货 | 仅卖家 |
| `POST /order/receive` | 任何人可替买家收货（直接完成交易） | 仅买家 |

顺带把三处 `Integer` 引用比较（`order.getStatus() != 常量`）改为 `.equals()`，并给分页补默认值，避免 `PageHelper` 拆箱空指针。

**参数校验**：`swapu_pojo` 引入 `jakarta.validation-api`，`SwapU_user` 引入 `spring-boot-starter-validation`；12 个订单接口全部加 `@Valid`，DTO 上加 `@NotBlank/@NotNull/@Min/@Max/@Size/@DecimalMin`；`GlobalExceptionHandler` 新增 `MethodArgumentNotValidException`、`BindException`、`ConstraintViolationException`、`HttpMessageNotReadableException` 处理，校验失败返回首个中文提示而不是"系统繁忙"。

**一个差点造成回归的点**：给 `OrderDTO.deliveryMethod` 加 `@NotNull` 后，用户端下单页（`CreateOrder.vue`）根本不传该字段，会导致所有 UI 下单失败。已改为可选 + service 层默认「快递」，并逐一核对了前端 10 个订单接口的请求体与新约束是否兼容。

**测试**：新增 17 个不依赖数据库的单元测试（Mockito + Bean Validation）：

- `OrderAccessControlTest`（9 个）：买家/卖家可读、第三方读/发货/支付/收货被拒、订单不存在返回业务异常；
- `OrderValidationTest`（8 个）：数量 0/负数被拒、配送方式可缺省、负数运费被拒等。

**仍待处理**

1. **假支付漏洞（未修）**：`payType=1/2`（支付宝/微信）时服务端不做任何资金操作，却直接把订单改成"待发货" → 等于白拿商品；前端也确实提供了这两个选项。建议先只放开余额支付，或在 UI 上禁用另外两种；
2. ~~支付密码 == 登录密码，且都是无盐 MD5~~ → 已修，见 Phase 0-4；
3. ~~`payOrder` 并发双付窗口~~ → 已修，见 Phase 0-4。

### Phase 0-4 已完成：支付密码独立化 + 并发双付防护（2026-11-19）

> ⚠️ **部署前置条件**：必须先执行 `SwapU_user/src/main/resources/sql/user_pay_password.sql`（`ALTER TABLE user ADD COLUMN pay_password ...`）。
> 注册接口的 INSERT 已包含该列，**缺列会导致注册直接报 SQL 错误**。

**① 支付密码与登录密码解耦（BCrypt）**

| 项 | 改动 |
|---|---|
| 存储 | `user.pay_password VARCHAR(100)`：BCrypt 密文（`$2a$...`），与 `password` 完全独立的字段 |
| 注册 | 新用户注册时写入 `BCrypt.hashpw(登录密码)`，因此可直接支付，无需额外操作 |
| 支付校验 | 只读 `pay_password`，用 `BCrypt.checkpw` 比对；**不再读取登录密码字段** |
| 存量账号 | `pay_password` 为 NULL 时按旧逻辑用登录密码校验一次，通过后立即把 hash 升级为 BCrypt 并随同事务落库（**惰性迁移，无需停机刷数据**） |
| 改登录密码 | 若支付密码仍等于旧登录密码（说明用户从未单独设置过），同步更新；一旦用户单独设置过支付密码，改登录密码不再影响它 |
| 新增接口 | `POST /user/pay-password`（登录密码验证身份后设置/重置支付密码，6-20 位）、`GET /user/pay-password/status` |
| 防泄露 | `User.payPassword` 加 `@JsonIgnore`；同时给 `User.password` 也加了 `@JsonIgnore`——`GET /user/{id}` 原本会把登录密码的 MD5 哈希返回给任意登录用户，这是一个顺带修掉的真实泄露 |

**② 并发支付窗口（原子占位）**

原实现是"先查 status 判断 → 再 `update`"，两个并发请求可能同时通过检查、重复扣款。改为：

```
1) 校验：买家身份 → 状态为待支付 → 支付密码 → 余额
2) 原子占位：update orders set status=3, pay_type=?, pay_time=?
              where order_no=? and status=2      ← 只有一个请求 affected=1
3) affected != 1 → 抛「该订单当前状态不支持支付」（不扣款、不推进）
4) 余额划转（与 2、3 同一事务，异常整体回滚）
```

**测试**：`OrderPayTest`（6 个）覆盖——密码正确支付成功、密码错误拒绝且不推进、设置过支付密码后登录密码不再可用、历史账号惰性升级为 BCrypt、余额不足拒绝、保存并发竞争失败时不扣款。加上前述两个测试类，共 **23 个测试全绿**。

**遗留**：登录密码本身仍是**无盐 MD5**（本次只迁移了支付密码）。建议后续用同一套惰性升级模式改造：登录时若 hash 不以 `$2` 开头则走 MD5 校验、通过后重写为 BCrypt。改完需同步调整 `payOrder` 里历史账号的 MD5 分支。




