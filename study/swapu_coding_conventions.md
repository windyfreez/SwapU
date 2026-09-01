# SwapU 后端编码规范（从 SwapU_user 代码提炼）

> 本文档从 `SwapU_user`（及关联的 `swapu_pojo` / `swapu_common` / `swapu_admin`）现有代码中提炼，反映作者实际写代码时遵循（或不经意间形成）的约定。新代码请按此规范书写，保持全项目风格一致。

---

## 1. 分层与包结构

严格按**技术分层**组织包，一个功能贯穿 6 层：

```
controller.user        # 接收请求，返回 Result，不写业务逻辑
service                # 业务接口（定义方法签名）
service.impl           # 业务实现（接口实现类所在包）
mapper                 # 数据访问接口
entity / dto / vo      # 持久化实体 / 请求参数 / 响应视图
constant               # 常量类（Message/Number/String 三件套）
context                # 线程上下文（BaseContext）
exception              # 自定义业务异常
handler                # 全局异常处理、WebSocket 处理、JSON typeHandler
interceptor            # JWT 登录拦截器
task                   # @Scheduled 定时任务
utils                  # 静态工具类
config                 # Spring 配置类（WebMvc/Redis/Oss/WebSocket）
properties             # 自定义配置属性类（@ConfigurationProperties）
aspect                 # AOP 切面（行为埋点）
```

**分层调用铁律**：`Controller → Service → Mapper → DB`，禁止 Controller 直接调 Mapper、Service 里写 Servlet API。

## 2. 命名规范

| 类别 | 规范 | 示例 |
|------|------|------|
| 类名 | PascalCase + 层后缀 | `UserController`、`ProductServiceImpl`、`OrderMapper`、`ProductDetailVO`、`UserLoginDTO` |
| 接口/实现 | 接口 `XxxService`，实现 `XxxServiceImpl` 放 `service.impl` 包 | `UserService` / `UserServiceImpl` |
| 方法名 | 动词开头，动词短语 | `register`、`login`、`addProduct`、`takedown`、`takeup`、`pageQuery`、`getProductById` |
| 常量 | 全大写 + 下划线，常量类集中管理 | `ACCOUNT_STATUS_NORMAL`、`PHONE_ALREADY_EXIST` |
| 变量/参数 | 小驼峰 | `orderNo`、`productDTO` |
| Mapper 方法 | 语义化动词 | `checkRepeatByPhone`、`findByUsername`、`getOrderByOrderNo` |
| 数据库 | 表/列一律 snake_case，实体驼峰 | `user_id` ↔ `userId` |

> ✅ 已修正：`CatrgoryServiceImpl` → `CategoryServiceImpl`；包名 `service.Impl` → `service.impl`（用户端+管理端已统一小写）。

## 3. 注解使用规范

- **Lombok**：实体/DTO/VO 用 `@Data`（必要时 `@Builder`、`@AllArgsConstructor`、`@NoArgsConstructor`）；需要日志的类一律 `@Slf4j`。
- **Controller**：
  - `@RestController` + `@RequestMapping("/模块")` + `@Api(tags = "模块接口")`
  - 每个方法 `@ApiOperation("中文描述")`
  - JSON 入参 `@RequestBody`，路径参数 `@PathVariable`，查询参数 `@RequestParam` 或直接 DTO 接收
- **Service 实现**：`@Service` + 每个方法 `@Override`
- **事务**：**仅多表写操作**加 `@Transactional`（如支付、订单状态流转），单表/查询不加
- **Mapper**：`@Mapper` 接口；简单 SQL 用 `@Select/@Insert` 注解，复杂动态 SQL 用 XML
- **依赖注入**：字段注入，`@Autowired` 为主，`@Resource` 用于非 Mapper 组件（`StringRedisTemplate` 等）

## 4. 注释规范

- **每个公开方法必须有 Javadoc**：`/** 中文功能描述 @param xxx @return */`（参数注释与形参同名）
- 常量类、工具类、上下文类**类头写中文注释**说明用途
- 代码内用**中文行注释**解释业务意图，尤其状态流转、字段含义、分支逻辑：
  ```java
  //密码隐私保护
  user.setPassword("****");
  //提高代码健壮性再来一次状态判断
  if(order.getStatus() != Order.ORDER_STATUS_WAIT_PAY){ ... }
  ```
- 常量值旁标注含义：`PRODUCT_STATUS_CHECKED = 0;//审核中`

## 5. 异常处理规范

- **异常体系**：`BaseException extends RuntimeException`；业务异常继承它（`AccountRepeatException`、`OrderException`）
- **抛出位置**：Service 层抛，Controller 层不 catch、不 try-catch
- **提示语集中管理**：`throw new OrderException(MessageConstant.BALANCE_NOT_ENOUGH)`，文案一律放 `MessageConstant`，不写裸字符串
- **全局兜底**：`GlobalExceptionHandler`（`@RestControllerAdvice`）按异常类型逐级处理，最外层 `Exception` 返回固定文案"系统繁忙，请稍后重试"，**不向客户端泄露堆栈**
- 可预期的分支错误（参数不合法、状态不允许）用业务异常；真正意外错误才抛系统异常

## 6. 统一返回与分页

- **响应结构**：`Result<T>` = `{code, msg, data}`；`Result.success(data)` 成功（code=200），`Result.error(msg)` 失败（code=401）
- **分页结构**：`PageResult` = `{total, records}`；Service 内 `PageHelper.startPage(page, pageSize)` → 查询 → 强转 `Page<T>` → `new PageResult(page.getTotal(), page.getResult())`
- **数据三件套**：请求用 DTO、响应用 VO、持久化用 Entity；属性拷贝统一 `BeanUtils.copyProperties(source, target)`（大量使用），需要组装多个来源字段时手动 set 补全
- 分页列表只返回封面图（`images.get(0)` 截断），详情才返回全部图片

## 7. 用户上下文与鉴权

- **当前用户**：`BaseContext`（`ThreadLocal<Long>`）存当前登录用户 ID；Service 层 `BaseContext.getCurrentId()` 获取，**Controller 不直接接触**
- **鉴权链路**：`JwtUtil.createJWT/parseJWT`（HS256）→ `JwtTokenUserInterceptor.preHandle` 校验 `token` 请求头 → 解析出 userId 写入 `BaseContext` → `WebMvcConfiguration.addInterceptors` 注册，`addPathPatterns("/**")` + `excludePathPatterns` 白名单（登录/注册/公开接口/文档资源）
- 未登录统一响应 401 JSON，不放行
- ⚠️ 注意：拦截器目前**没有 `afterCompletion` 清理 ThreadLocal**，新写拦截器/上下文时建议补 `removeCurrentId()`

## 8. 数据库访问风格

- **命名映射**：`map-underscore-to-camel-case: true`，表列 snake_case ↔ 实体驼峰
- **简单 SQL** 用注解（`@Select("select count(*) from user where phone = #{phone}")`）
- **复杂/动态 SQL** 用 XML（`src/main/resources/mapper/*.xml`）：
  - `resultMap` 显式声明字段映射，JSON 列配自定义 `typeHandler`（`JsonStringListTypeHandler`）
  - 更新统一动态 `<set>` + `<if test="xx != null">`（只更新非 null 字段）
  - 查询条件用 `<where>` + `<if>`，排序用 `<choose>/<when>` 防注入
  - 批量用 `<foreach>`
- **分页**：PageHelper
- **时间字段**：`createTime/updateTime` 在代码里 `LocalDateTime.now()` 手动赋值（不依赖 DB 默认值）
- **只查上架商品**：列表/推荐 SQL 统一 `WHERE status = 1`

## 9. 日志规范

- `@Slf4j` + 分级：
  - `log.info`：Controller 打印入参、Service 关键步骤、拦截器打印 token/用户 ID
  - `log.warn`：可预期情况（游客访问、缓存未命中、参数缺失）
  - `log.error`：异常堆栈，带上下文信息
- 敏感信息（密码）不打日志

## 10. 时间与金额

- **时间类型**：`LocalDateTime`；实体字段配 `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")` + `@DateTimeFormat` 成对出现
- **金额**：一律 `BigDecimal`，比较用 `compareTo`（如余额校验 `user.getBalance().compareTo(totalAmount) < 0`），运算用 `subtract/add`

## 11. 常量管理（三件套）

| 常量类 | 职责 | 示例 |
|--------|------|------|
| `MessageConstant` | 用户提示文案 | `"余额不足，请充值或选择其他支付方式"` |
| `NumberConstant` | 数字状态值 | `ACCOUNT_STATUS_NORMAL = 1`、`FULL_CREDIT_SCORE = 100` |
| `StringConstant` | 字符串/Redis key | `"hot:products:"`、`"log:behavior:queue"` |

- 状态枚举值**不带语义魔法数**，全部走常量；状态描述文案也集中管理（`PRODUCT_STATUS_SELLING_DESC`）
- Redis key 带 `模块:类型:` 前缀，冒号分隔，集中放 `StringConstant`

## 12. 配置管理

- `application.yml`：按功能分区注释（数据库/redis/multipart/mybatis/jwt/oss），数据库密码等敏感值优先环境变量注入
- 自定义配置用 `@ConfigurationProperties` 前缀类（`JwtProperties`、`AliOssProperties`）+ `@Configuration` 注入 Bean（`JwtUtil`、`AliOssUtil`）
- 端口规划：用户端 8080、管理端 8081

## 13. 工具类与定时任务

- 工具类 = **无状态静态方法**（`JwtUtil.createJWT`、`SimpleRandomSortUtil.weightedRandomSort`），不实例化
- 定时任务放 `task` 包，`@Scheduled(cron)`，启动类 `@EnableScheduling`
- **高并发写削峰三板斧**（项目核心模式）：
  1. Redis 暂存（计数器/List 队列）
  2. 定时任务批量同步 DB（如 `BehaviorTask` 每分 500 条、`ViewCountSyncTask` 每 5 分）
  3. 查询走 Redis 缓存、DB 兜底（`top20List` 先 Redis 后 DB）

## 14. 代码风格细节

- K&R 大括号（左括号同行），4 空格缩进
- 逻辑块之间空行分组；中文注释 `//` 后带空格
- 状态→描述映射用 `switch-case`（成色、商品状态）
- 方法职责单一：`Controller 组装入参 → Service 编排 → Mapper 单查`，复杂方法拆私有 helper（如 `BehaviorAspect.extractProductId`）

## 15. 风格渊源

本项目的分层、`Result`/`PageResult`、`MessageConstant`、`BaseContext`、`JWT 拦截器 + 白名单`、`PageHelper 分页` 等模式与**黑马程序员《苍穹外卖》教学项目**（sky-take-out）一脉相承，属于国内主流 Spring Boot 单体项目范式。README 亦说明前端代码多由 AI IDE 生成、本人负责 Review。

## 16. 已知不一致点（维护时注意）

| 问题 | 位置 | 建议 |
|------|------|------|
| `Result/PageResult` 双份拷贝 | `SwapU_user/entity/result` 与 `swapu_pojo` 根包并存 | 收敛到 `swapu_pojo` |
| 公共类模块内重复拷贝 | 用户端自带 `BaseContext/JwtUtil/AliOssUtil/entity` 副本，又引用 `swapu_common/swapu_pojo` | 收敛到公共模块 |
| `@Autowired` / `@Resource` 混用 | 各 Service | 统一一种 |
| `Result.error` 返回 code=401 用于业务失败 | `Result.java` | 语义上业务失败建议独立错误码（如 4xx 业务码），`401` 留给未登录 |
| ThreadLocal 未清理 | `JwtTokenUserInterceptor` | 补 `afterCompletion` |
| 密码 MD5 明文 | `DigestUtils.md5DigestAsHex` | 建议加盐或 BCrypt |
