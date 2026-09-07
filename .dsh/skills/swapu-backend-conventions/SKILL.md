---
name: swapu-backend-conventions
description: SwapU 后端（Java + Spring Boot）编码规范速查：分层铁律、命名、注解、注释、异常、统一返回 Result/PageResult、JWT 鉴权、MyBatis、日志、时间金额、常量三件套与已知坑点。在 SwapU_user / swapu_pojo / swapu_common / swapu_admin 模块中新增或修改 Java 代码、Mapper XML、编写或审查后端接口时使用。
whenToUse: 在本仓库后端模块编写或审查 Java 代码时使用。
---

# SwapU 后端编码规范

## 分层铁律（一票否决）

- 调用链严格 `Controller → Service → Mapper → DB`：禁止 Controller 直接调 Mapper、禁止 Service 里写 Servlet API。
- 包按技术分层组织：`controller.user` / `service` / `service.impl` / `mapper` / `entity·dto·vo` / `constant` / `context` / `exception` / `handler` / `interceptor` / `task` / `utils` / `config` / `properties` / `aspect`。
- Controller 只接收请求、返回 `Result`，不写业务逻辑。

## 命名规范

| 类别 | 规范 | 示例 |
|------|------|------|
| 类名 | PascalCase + 层后缀 | `UserController`、`ProductServiceImpl`、`OrderMapper`、`UserLoginDTO` |
| 接口/实现 | `XxxService` / `XxxServiceImpl`（放 `service.impl` 包） | `UserService` / `UserServiceImpl` |
| 方法名 | 动词开头短语 | `register`、`pageQuery`、`getProductById`、`takedown` |
| 常量 | 全大写 + 下划线，集中在常量类 | `ACCOUNT_STATUS_NORMAL`、`PHONE_ALREADY_EXIST` |
| 变量/参数 | 小驼峰 | `orderNo`、`productDTO` |
| Mapper 方法 | 语义化动词 | `checkRepeatByPhone`、`findByUsername` |
| 数据库 | 表/列 snake_case ↔ 实体驼峰 | `user_id` ↔ `userId` |

## 注解使用

- 实体/DTO/VO：`@Data`（必要时 `@Builder`、`@AllArgsConstructor`、`@NoArgsConstructor`）；需要日志的类一律 `@Slf4j`。
- Controller：`@RestController` + `@RequestMapping("/模块")` + `@Api(tags = "模块接口")`；每个方法加 `@ApiOperation("中文描述")`；JSON 入参 `@RequestBody`、路径参数 `@PathVariable`、查询参数 `@RequestParam` 或 DTO 接收。
- Service 实现：`@Service`，每个实现方法加 `@Override`。
- 事务：**仅多表写操作**（支付、订单状态流转）加 `@Transactional`；单表/查询一律不加。
- Mapper：`@Mapper`；简单 SQL 用 `@Select/@Insert` 注解，复杂动态 SQL 用 XML。
- 依赖注入：字段注入，`@Autowired` 为主；非 Mapper 组件（如 `StringRedisTemplate`）用 `@Resource`。

## 注释

- 每个公开方法必须有 Javadoc：`/** 中文功能描述 @param xxx @return */`（参数注释与形参同名）。
- 常量类、工具类、上下文类类头写中文注释说明用途。
- 代码内用中文行注释解释业务意图（状态流转、字段含义、分支逻辑），`//` 后带空格。
- 常量值旁标注含义：`PRODUCT_STATUS_CHECKED = 0;//审核中`

## 异常处理

- 业务异常继承 `BaseException`（extends RuntimeException），如 `OrderException`、`AccountRepeatException`。
- 在 Service 层抛，Controller 不 catch、不 try-catch。
- 提示文案一律放 `MessageConstant`，禁止裸字符串：`throw new OrderException(MessageConstant.BALANCE_NOT_ENOUGH)`。
- `GlobalExceptionHandler`（`@RestControllerAdvice`）按异常类型逐级兜底，最外层 `Exception` 返回"系统繁忙，请稍后重试"，禁止向客户端泄露堆栈。
- 可预期分支错误（参数不合法、状态不允许）用业务异常；真正意外错误才抛系统异常。

## 统一返回与分页

- 响应 `Result<T>` = `{code, msg, data}`：成功 `Result.success(data)`（code=200），失败 `Result.error(msg)`（code=401）。
- 分页 `PageResult` = `{total, records}`：`PageHelper.startPage(page, pageSize)` → 查询 → 强转 `Page<T>` → `new PageResult(page.getTotal(), page.getResult())`。
- 三件套：请求用 DTO、响应用 VO、持久化用 Entity；属性拷贝统一 `BeanUtils.copyProperties(source, target)`，多来源字段手动 set 补全。
- 分页列表只返回封面图（`images.get(0)` 截断），详情才返回全部图片。

## 用户上下文与鉴权

- 当前用户：`BaseContext`（ThreadLocal）存 userId；Service 层 `BaseContext.getCurrentId()` 获取，Controller 不直接接触。
- 链路：`JwtUtil.createJWT/parseJWT`（HS256）→ `JwtTokenUserInterceptor.preHandle` 校验 token 请求头 → 解析 userId 写入 BaseContext → `WebMvcConfiguration.addInterceptors` 注册，`addPathPatterns("/**")` + `excludePathPatterns` 白名单（登录/注册/公开接口/文档）。
- 未登录统一返回 401 JSON，不放行。
- ⚠️ 现有拦截器没有 `afterCompletion` 清理 ThreadLocal；新写拦截器/上下文务必补 `removeCurrentId()`。

## 数据库访问

- `map-underscore-to-camel-case: true`（表列 snake_case ↔ 实体驼峰）。
- 简单 SQL 用注解：`@Select("select count(*) from user where phone = #{phone}")`。
- 复杂/动态 SQL 用 XML（`src/main/resources/mapper/*.xml`）：
  - `resultMap` 显式声明映射；JSON 列配自定义 typeHandler（`JsonStringListTypeHandler`）。
  - 更新统一动态 `<set>` + `<if test="xx != null">`（只更新非 null 字段）。
  - 查询条件用 `<where>` + `<if>`；排序用 `<choose>/<when>` 防注入；批量用 `<foreach>`。
- 分页：PageHelper。
- 时间字段 `createTime/updateTime` 在代码里 `LocalDateTime.now()` 手动赋值，不依赖 DB 默认值。
- 列表/推荐 SQL 统一 `WHERE status = 1`（只查上架商品）。

## 日志

- 需要日志的类 `@Slf4j`，分级使用：
  - `log.info`：Controller 入参、Service 关键步骤、拦截器 token/用户 ID。
  - `log.warn`：可预期情况（游客访问、缓存未命中、参数缺失）。
  - `log.error`：异常堆栈，带上下文。
- 敏感信息（密码）禁止打日志。

## 时间与金额

- 时间一律 `LocalDateTime`；实体字段 `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")` 与 `@DateTimeFormat` 成对出现。
- 金额一律 `BigDecimal`：比较用 `compareTo`（如 `user.getBalance().compareTo(totalAmount) < 0`），运算用 `subtract/add`。

## 常量管理（三件套）

| 常量类 | 职责 | 示例 |
|--------|------|------|
| `MessageConstant` | 用户提示文案 | `"余额不足，请充值或选择其他支付方式"` |
| `NumberConstant` | 数字状态值 | `ACCOUNT_STATUS_NORMAL = 1`、`FULL_CREDIT_SCORE = 100` |
| `StringConstant` | 字符串 / Redis key | `"hot:products:"`、`"log:behavior:queue"` |

- 状态值禁止魔法数，一律走常量；状态描述文案也集中管理（如 `PRODUCT_STATUS_SELLING_DESC`）。
- Redis key 带 `模块:类型:` 前缀，冒号分隔，集中放 `StringConstant`。

## 配置

- `application.yml` 按功能分区注释（数据库/redis/multipart/mybatis/jwt/oss）；数据库密码等敏感值优先环境变量注入。
- 自定义配置用 `@ConfigurationProperties` 前缀类（`JwtProperties`、`AliOssProperties`）+ `@Configuration` 注入 Bean（`JwtUtil`、`AliOssUtil`）。
- 端口：用户端 8080、管理端 8081。

## 工具类与定时任务

- 工具类 = 无状态静态方法（`JwtUtil.createJWT`、`SimpleRandomSortUtil.weightedRandomSort`），不实例化。
- 定时任务放 `task` 包，`@Scheduled(cron)`；启动类 `@EnableScheduling`。
- 高并发写削峰三板斧：① Redis 暂存（计数器/List 队列）→ ② 定时任务批量同步 DB（`BehaviorTask` 每分 500 条、`ViewCountSyncTask` 每 5 分）→ ③ 查询走 Redis 缓存、DB 兜底（`top20List` 先 Redis 后 DB）。

## 代码风格

- K&R 大括号（左括号同行），4 空格缩进；逻辑块之间空行分组。
- 状态→描述映射用 `switch-case`（成色、商品状态）。
- 方法职责单一：Controller 组装入参 → Service 编排 → Mapper 单查；复杂方法拆私有 helper（如 `BehaviorAspect.extractProductId`）。

## ⚠️ 已知不一致点（别照抄旧代码）

| 问题 | 建议 |
|------|------|
| `Result/PageResult` 双份拷贝（`SwapU_user/entity/result` 与 `swapu_pojo` 并存） | 新代码统一用 `swapu_pojo` 里的 |
| 用户端自带 `BaseContext/JwtUtil/AliOssUtil/entity` 副本，又引用 `swapu_common/swapu_pojo` | 优先引用公共模块，不新建副本 |
| `@Autowired`/`@Resource` 混用 | 尽量统一用 `@Autowired` |
| `Result.error` 用 code=401 表示业务失败 | 语义上 401 留给未登录；新接口可考虑独立业务错误码 |
| `JwtTokenUserInterceptor` 未清理 ThreadLocal | 新代码补 `afterCompletion` + `removeCurrentId()` |
| 密码用 `DigestUtils.md5DigestAsHex` 明文 | 新逻辑建议加盐或 BCrypt |
