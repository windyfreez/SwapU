# 管理端 RBAC：角色权限方案与实施记录

> 目标：为 SwapU 管理后台建立「超级管理员 / 运营管理员 / 普通管理员」三角色权限体系，
> 并把管理员管理（CRUD）从"仅超管可用"扩展为"按角色授权 + 可审计"。
>
> 记录方式：本文既是**方案**（第三节起），也是**实施记录**（第七节按批次更新）。
> 最近更新：批次一完成（权限内核 + 接口拦截）。

---

## 一、背景

管理端原有两种角色：`1` 超级管理员、`2` 普通管理员，但**只在管理员管理模块做了超管校验**：

- `AdminServiceImpl.addAdmin / update / updateStatus / deleteById` 会调 `checkSuperAdmin()`；
- 而 `AuditController`（6 个接口）、`CategoryController`（5 个）、`UserManageController`（5 个）**完全没有角色校验**——任何登录的管理员都能删分类、能禁用用户、能改信用分。

也就是说，"普通管理员"这个角色在代码里只约束了「不能管管理员」，没约束「只能审核」。

本次要解决三件事：

1. 增加第三种角色 **运营管理员**；
2. 把 16 个业务接口真正按角色收口；
3. 让管理员管理可审计（操作留痕）、删除不破坏历史数据。

---

## 二、现状盘点

### 2.1 已有能力（不重复造）

| 能力 | 位置 | 说明 |
|---|---|---|
| 管理员 CRUD | `AdminController` + `AdminServiceImpl` | 新增、分页、修改、启停、删除、改自己密码、按 id 查详情 |
| 超管校验 | `AdminServiceImpl.checkSuperAdmin()` | 增删改启停均要求角色=1 且状态启用 |
| 自我保护 | 同上 | 不能禁用自己、不能删除自己、必须保留 ≥1 名启用超管 |
| 密码 | BCrypt | 落库即 BCrypt；实体 `password` 有 `@JsonIgnore` |
| 前端 | `AdminManage.vue`、`api/admin.js`、路由守卫 | CRUD 界面与 `requiresSuper` 都已就绪 |
| 角色常量 | `common/NumberConstant` | `ADMIN_ROLE_SUPER=1`、`ADMIN_ROLE_NORMAL=2` |

### 2.2 缺口

| 缺口 | 影响 |
|---|---|
| 没有运营管理员角色 | 无法表达"审核 + 分类 + 用户"这一档 |
| 16 个业务接口零校验 | 普通管理员越权（删分类、禁用户、改信用分） |
| `GET /admin/page`、`GET /admin/{id}` 无校验 | 任何管理员可翻看管理员名单 |
| `AdminDTO.role` 不校验取值 | 传 `role=99` 也能落库 |
| 物理删除 | `audit_record.reviewer_id` 变悬挂引用 |

---

## 三、角色与权限模型

### 3.1 权限点（唯一真源）

| 权限点 | 说明 |
|---|---|
| `AUDIT` | 商品审核、评论审核、审核流水查询 |
| `CATEGORY_MANAGE` | 分类新增/修改/启停/删除 |
| `USER_MANAGE` | 用户列表/详情/启停/信用分/交易统计 |
| `ADMIN_MANAGE` | 管理员增删改启停、重置密码、查看管理员列表 |

实现位置：`swapu_admin/constant/AdminPermission.java`。

### 3.2 角色 → 权限矩阵

| 角色 | role 值 | 权限集合 |
|---|---|---|
| 超级管理员 | 1 | AUDIT + CATEGORY_MANAGE + USER_MANAGE + ADMIN_MANAGE |
| 运营管理员 | 3 | AUDIT + CATEGORY_MANAGE + USER_MANAGE |
| 普通管理员 | 2 | AUDIT |

> 角色值用 `3` 表示运营，是为了**不改变已有数据**：现存账号的 1/2 语义不变。

### 3.3 接口 → 权限映射

| 接口 | 权限 | 超管 | 运营 | 普通 |
|---|---|:--:|:--:|:--:|
| `GET /admin/audit/product/page` 等 6 个 | AUDIT | ✓ | ✓ | ✓ |
| `GET /admin/user/page` 等 5 个 | USER_MANAGE | ✓ | ✓ | ✗ |
| `POST/PUT/DELETE /admin/category`、`PUT /admin/category/{id}/status` | CATEGORY_MANAGE | ✓ | ✓ | ✗ |
| `GET /admin/category/list` | **无（只读字典）** | ✓ | ✓ | ✓ |
| `POST /admin`、`GET /admin/page`、`PUT /admin`、`PUT /admin/{id}/status`、`DELETE /admin/{id}` | ADMIN_MANAGE | ✓ | ✗ | ✗ |
| `POST /admin/login`、`PUT /admin/password`、`GET /admin/{id}`、`GET /admin/me` | 无（登录 / 自助） | ✓ | ✓ | ✓ |

### 3.4 决策记录

| # | 问题 | 决定 |
|---|---|---|
| 1 | 普通管理员可见范围 | **完全隔离**：菜单不可见 + 接口拒绝（分类列表除外，见下） |
| 2 | 运营管理员的用户管理范围 | **全部 5 个操作**（含改信用分） |
| 3 | 运营管理员的分类管理范围 | **可删除**，受"分类下有商品不可删"的既有约束 |
| 4 | 删除管理员 | **逻辑删除**（`deleted` 标记，列表默认过滤） |
| 5 | 操作日志 | **要**，新增 `admin_operation_log` |
| 6 | 鉴权实现 | **自定义注解 + 拦截器**，不引入 Spring Security |

**关于第 1 条的一处偏离（需注意）**

`ProductAudit.vue` 会调用 `GET /admin/category/list` 来渲染「类目筛选下拉」。如果把这个接口也对普通管理员封死，审核页的筛选会直接报错——而被封的这个人恰恰是**专门做审核的**。

因此实现上把分类拆成两类：

- `GET /admin/category/list` —— 视为**共享只读字典**，登录即可访问；
- 4 个写操作 —— 要求 `CATEGORY_MANAGE`。

结果：普通管理员「不能改分类」，但「能读分类名」。如果你要求连读都不允许，替代方案是让审核页的筛选下拉改为从已加载的商品行里聚合类目（只覆盖当前页，体验更差），需要改前端。

---

## 四、技术设计

### 4.1 鉴权链路

```
请求 ─► JwtTokenAdminInterceptor       校验 token，把 adminId 写入 BaseContext
     ─► AdminPermissionInterceptor     读取 @RequirePermission，按 adminId 查库拿 role
                                        角色 → 权限集合，不含所需权限则抛 403
     ─► Controller
```

要点：

1. **两道拦截器职责分离**：前者回答"你是谁"，后者回答"你能做什么"；注册顺序即执行顺序，权限拦截器必须排在令牌校验之后。
2. **只拦标注了注解的接口**：未标注 = 登录即可访问（登录、改自己密码、查自己信息、分类字典）。
3. **方法级注解优先于类级**：`AuditController` 用类级（整模块同权），`AdminController` 用方法级（登录/自助接口不能拦）。
4. **同一请求内缓存权限**：用 `request.setAttribute` 存已解析的权限集合，一次请求只查一次库。
5. **用 `ClassUtils.getUserClass` 取原始类**：避免 Controller 将来被 AOP 代理后类级注解读不到。

### 4.2 为什么不把 role 写进 JWT

写进 token 的代价是：超管把某人降级/禁用后，最长要等 2 小时（token TTL）才生效。管理端 QPS 很低，每请求查一次库换来「降级立即生效」是划算的。若将来要优化，应加 Redis 缓存（60s），而不是退回把角色塞 token。

### 4.3 错误码约定

| 场景 | code | 前端行为 |
|---|---|---|
| 未登录 / token 失效 | 401 + HTTP 401 | 清登录态，跳登录页 |
| 已登录但角色无权 | **403**（HTTP 200） | 提示"当前角色无权执行该操作"，**不登出** |
| 账号被禁用 | 4102 | 提示"账号已被禁用" |

前端 `api/request.js` 只在 HTTP 401 时清会话；业务码非 200 只抛 `ApiError`，因此 403 不会误登出。

### 4.4 逻辑删除（批次二，已实现）

- `admin` 表加 `deleted TINYINT NOT NULL DEFAULT 0`；
- 登录、鉴权、分页、唯一性校验统一加 `deleted = 0`；
- 删除 = `logicDeleteById`：`set deleted = 1, username = 原账号_del_{id}`，**释放账号名**，避免"删了不能重建同名账号"；语句自带 `deleted = 0` 条件，重复删除影响行数为 0，可做幂等判断；
- "至少保留一名启用超管"的保护覆盖**禁用、降级、删除**三条路径（删除这条是本批次补上的）。

### 4.5 操作日志（批次三）

新表 `admin_operation_log`：

| 字段 | 说明 |
|---|---|
| `id` | 主键 |
| `admin_id` / `admin_username` | 操作人（冗余 username，改名后仍可追溯） |
| `action` | CREATE / UPDATE / STATUS / DELETE / RESET_PASSWORD / ROLE_CHANGE |
| `target_type` / `target_id` | 被操作对象类型与 id |
| `detail` | 变更摘要，如 `role:2→3` |
| `ip` | 操作 IP |
| `create_time` | 时间 |

写入方式：**在 Service 层显式记录**（`AdminOperationLogService.record`），而不是原计划的 `@AdminLog` + AOP。
原因：AOP 只能拿到 Controller 入参，拿不到"修改前的值"，做不出 `role:2→3` 这类变更摘要；而 Service 里本来就加载了 `existAdmin`，天然能生成精确的 before/after。

### 4.6 `GET /admin/me`（批次二）

返回 `{id, username, name, role, roleLabel, permissions[]}`，作为**前端菜单/路由的唯一真源**，让前端不再硬编码角色判断。

### 4.7 前端权限呈现（批次四）

| 位置 | 做法 |
|---|---|
| `utils/enums.js` | `ADMIN_ROLE` 增加 `3: 运营管理员` |
| `router/index.js` | `requiresSuper` 改为 `permission: 'ADMIN_MANAGE' / 'CATEGORY_MANAGE' / 'USER_MANAGE'` |
| `AdminLayout.vue` | 按 permissions 过滤菜单分组 |
| `Dashboard.vue` | 普通管理员隐藏用户/分类统计卡片（否则接口 403） |
| `AdminManage.vue` | 角色筛选/表单下拉增加"运营管理员" |

---

## 五、实施批次

| 批次 | 内容 | 状态 |
|---|---|---|
| 一 | 权限内核：角色常量、权限枚举、注解、权限服务、拦截器、20 个接口加注解 | ✅ 已完成 |
| 二 | CRUD 完善：role 取值校验、逻辑删除、`/admin/me`、列表/详情权限、禁止改自己角色 | ✅ 已完成 |
| 三 | 操作日志：建表 + 实体/Mapper + 记录 + 查询接口 | ✅ 已完成 |
| 四 | 前端：三角色菜单/路由/页面/仪表盘适配 + 操作日志页 + `/admin/me` 接入 | ✅ 已完成 |
| 五 | 文档收尾：接口清单、数据库文档、README 同步（测试已在前四批内完成） | ✅ 已完成 |

---

## 六、验收标准

| 场景 | 期望 |
|---|---|
| 普通管理员调 `GET /admin/user/page` | code=403，提示无权限 |
| 普通管理员调 `PUT /admin/category/{id}/status` | code=403 |
| 普通管理员调 `GET /admin/category/list` | 200（审核页筛选需要） |
| 普通管理员调 `POST /admin/audit/product/review` | 200，审核成功 |
| 运营管理员调 `DELETE /admin/category/{id}` | 200（受"有商品不可删"约束） |
| 运营管理员调 `GET /admin/page` | code=403 |
| 超管调用全部接口 | 均 200 |
| 账号被禁用后用旧 token 请求 | code=4102，立即失效（不用等 token 过期） |

---

## 七、批次一实施记录

### 7.1 新增文件

| 文件 | 作用 |
|---|---|
| `swapu_admin/constant/AdminPermission.java` | 4 个权限点 + 「角色→权限」不可变映射 + `isKnownRole` |
| `swapu_admin/annotation/RequirePermission.java` | 可标在类或方法上的权限声明 |
| `swapu_admin/service/AdminPermissionService.java` | 权限查询/判断/校验接口 |
| `swapu_admin/service/impl/AdminPermissionServiceImpl.java` | 按 adminId 查库 → 校验状态 → 返回角色权限集；403/4102 |
| `swapu_admin/interceptor/AdminPermissionInterceptor.java` | 注解解析（方法优先→类兜底）、403、请求内缓存 |

### 7.2 修改文件

| 文件 | 改动 |
|---|---|
| `common/constant/NumberConstant.java` | 新增 `ADMIN_ROLE_OPERATION = 3` |
| `common/constant/MessageConstant.java` | 新增 `ADMIN_PERMISSION_DENIED` |
| `swapu_admin/constant/ErrorCodeConstant.java` | 新增 `FORBIDDEN = 403` |
| `swapu_admin/config/WebMvcConfiguration.java` | 注册第二道拦截器（`/admin/**`，排除登录） |
| `AuditController` | 类级 `@RequirePermission(AUDIT)`（6 个接口） |
| `UserManageController` | 类级 `@RequirePermission(USER_MANAGE)`（5 个接口） |
| `CategoryController` | 4 个写操作加 `CATEGORY_MANAGE`；`list` 保持开放 |
| `AdminController` | 新增/分页/修改/启停/删除 5 个方法加 `ADMIN_MANAGE` |
| `swapu_pojo/entity/Admin.java` | 角色注释补充 3=运营管理员 |

统计：**20 个接口加权限注解，5 个接口按设计保持开放**（登录、改自己密码、查自己信息、分类字典）。

### 7.3 测试（18 个，全部通过）

| 测试类 | 数量 | 覆盖点 |
|---|---|---|
| `AdminPermissionTest` | 5 | 三角色矩阵、未知角色返回空权限、角色合法性校验 |
| `AdminPermissionServiceImplTest` | 8 | 普通/运营/超管的放行与拦截、禁用账号 4102、账号不存在 403、无登录上下文 403、未知角色空权限 |
| `AdminPermissionInterceptorTest` | 5 | 无注解放行且不查库、类级注解生效、方法级覆盖类级、请求内只查一次、未登录 403 |

构建验证：`mvn -pl swapu_pojo,swapu_common,SwapU_user,swapu_admin -am clean test-compile` → BUILD SUCCESS，0 error。

### 7.4 本批次已知影响

1. **普通管理员的"工作台"会报错**：`Dashboard.vue` 现在会调 `pageUsers` + `listCategories`（后者仍开放，前者已 403）。批次四会按权限隐藏这两个统计卡片；在批次四完成前，普通管理员登录会看到一条无权限提示。
2. 前端菜单/路由仍按旧的 `role === 1` 判断，普通管理员**手动输入** `/admin/users` 仍能进入页面（但接口已 403，页面会提示无权限）。批次四会补路由守卫。
3. 运营管理员（role=3）目前前端下拉里还没有选项，无法通过界面创建；批次二/四补齐。**临时验证方式**：直接用超管调 `POST /admin` 传 `role: 3`，或 SQL 改 `admin.role`。

---

## 八、风险与遗留

| 风险 | 说明 / 对策 |
|---|---|
| 每请求一次 DB 查询 | 管理端流量小，先接受；后续可加 Redis 60s 缓存 |
| 角色变更后的旧 token | 因每次查库，降级/禁用**立即生效**；若将来改成 token 内带角色，需重新评估 |
| 逻辑删除与唯一索引 | 通过删除时改写 username 释放账号名；若库里有 `username` 唯一索引，需确认改名不冲突 |
| 分类字典的边界 | 目前"读开放、写受限"，是功能性妥协，已在 3.4 说明 |
| 前端权限判断滞后 | 依赖批次二的 `/admin/me` 作为唯一真源，避免前后端规则漂移 |

---

## 九、批次二实施记录

> ⚠️ **部署前置条件**：先执行 `swapu_admin/src/main/resources/sql/admin_v2.sql`
> （`ALTER TABLE admin ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0`）。
> 所有管理员查询已带 `deleted = 0` 条件，**缺列会导致登录、鉴权、列表全部 SQL 报错**。

### 9.1 新增文件

| 文件 | 作用 |
|---|---|
| `swapu_admin/vo/AdminMeVO.java` | `/admin/me` 响应：id/username/name/avatar/role/roleLabel/permissions |
| `swapu_admin/resources/sql/admin_v2.sql` | 逻辑删除字段迁移脚本 |
| `swapu_admin/test/.../AdminManageServiceTest.java` | 16 个用例（见 9.4） |

### 9.2 修改文件

| 文件 | 改动 |
|---|---|
| `swapu_pojo/entity/Admin.java` | 新增 `deleted` 字段（`@JsonIgnore`），角色注释补充 3=运营 |
| `AdminMapper.java` | `findByUsername` / `findById` / `countByRoleAndStatusExcludeId` 全部加 `deleted = 0`；**物理删除 `deleteById` 替换为 `logicDeleteById`**（返回影响行数） |
| `AdminMapper.xml` | `resultMap` 增加 `deleted` 映射；`pageQuery` 增加 `deleted = 0` |
| `AdminServiceImpl` | ① 新增/修改校验角色取值（1/2/3）② 禁止改自己角色 ③ 降级为**任意非超管角色**都触发"最后超管"保护 ④ 删除改逻辑删除并改写账号名 ⑤ `getById` 增加"本人或 ADMIN_MANAGE"规则 ⑥ 新增 `currentAdmin()` 与 `roleLabel()` |
| `AdminService` | 新增 `currentAdmin()` |
| `AdminController` | 新增 `GET /admin/me`；`getById` 注释说明权限规则 |
| `AdminDTO` | 角色说明改为 1/2/3 |
| `common/StringConstant` | 新增三个角色中文名 + 删除改名后缀 `_del_` |
| `common/MessageConstant` | 新增 `ADMIN_ROLE_ERROR`、`ADMIN_CANNOT_CHANGE_SELF_ROLE` |
| `ErrorCodeConstant` | 新增 `ADMIN_ROLE_ERROR = 4106` |
| `docs/swapu_database.md` | admin 表 DDL 与字段表补 `deleted`、角色取值说明 |

### 9.3 行为变化清单

| 变化 | 说明 |
|---|---|
| 删除 = 逻辑删除 | 记录保留、`deleted=1`、账号名改为 `原账号_del_{id}`；列表/登录/鉴权都过滤已删除 |
| 删除需幂等 | 语句带 `deleted = 0`，并发重复删除时影响行数 0，返回"管理员不存在" |
| 不能改自己的角色 | 即便超管也不行，防止自锁与自我提权；需另一名超管操作 |
| 降级保护扩大 | 原来只拦"超管→普通管理员"，现在"超管→运营管理员"同样校验最后一名超管 |
| 查他人详情受限 | `GET /admin/{id}`：本人可直接查，查他人需 `ADMIN_MANAGE`，否则 403 |
| 新增 `/admin/me` | 返回角色名称与权限点列表，供前端菜单/路由守卫使用 |
| 非法角色被拒 | `role` 传 99 等值返回 4106，不再落库 |

### 9.4 测试（16 个新增，累计 34 个）

| 分组 | 用例数 | 覆盖点 |
|---|---|---|
| 新增校验 | 4 | 非法角色 4106、默认角色=2、运营角色可落库、非超管不能新增 |
| 修改保护 | 4 | 禁改自己角色、降级最后超管被拒、还有其他超管时允许降级、非法角色被拒 |
| 逻辑删除 | 4 | 不能删自己、不能删最后超管、普通管理员走逻辑删除并改名、并发重复删除报不存在 |
| 详情与 /me | 4 | 查他人 403、查自己免权限、超管 /me 返回 4 个权限、运营 /me 返回 3 个权限 |

构建验证：`mvn -pl swapu_admin -am test` 指定四个测试类 → **34 passed**；
全量 `clean test-compile` → BUILD SUCCESS。

### 9.5 遗留到后续批次

1. **前端还没有 `/admin/me` 的调用**：目前仍读 localStorage 缓存的 `adminInfo`，角色/权限变更后不会自动刷新。批次四接入。
2. **操作日志未接**：本批次的增删改尚无留痕，批次三补 `admin_operation_log`。
3. **接口文档未同步 `/admin/me`**：`docs/swapu_admin_interface_doc.md` 在批次五统一更新。
4. **运营管理员仍无法从界面创建**：前端下拉缺 role=3 选项（批次四）。

---

## 十、批次三实施记录

> ⚠️ **部署前置条件**：先执行 `swapu_admin/src/main/resources/sql/admin_operation_log.sql` 建表。
> 与批次二不同，**缺表不会阻断管理员操作**（日志写入失败被捕获并打 error 日志），但操作会**没有留痕**。

### 10.1 新增文件

| 文件 | 作用 |
|---|---|
| `resources/sql/admin_operation_log.sql` | 建表脚本（含 `(admin_id, create_time)` 与 `create_time` 索引） |
| `entity/AdminOperationLog.java` | 日志实体（仅用于写入） |
| `constant/AdminLogAction.java` | 5 种动作枚举 + `descriptionOf` 中文翻译 |
| `mapper/AdminOperationLogMapper.java` + `.xml` | `insert` + `pageQuery`（按动作/操作人/时间范围过滤） |
| `dto/AdminOperationLogPageQueryDTO.java` | 查询条件（页码、条数、动作、操作人、时间范围） |
| `vo/AdminOperationLogVO.java` | 列表项（含 `actionDesc` 中文描述） |
| `service/AdminOperationLogService.java` + 实现 | `record` 记录、`pageQuery` 查询 |
| `controller/admin/AdminOperationLogController.java` | `GET /admin/operation-log/page`，类级 `ADMIN_MANAGE` |
| `test/.../AdminOperationLogServiceTest.java` | 6 个用例 |

### 10.2 修改文件

| 文件 | 改动 |
|---|---|
| `AdminServiceImpl` | 5 个写操作接入日志：新增 / 修改 / 启停 / 删除 / 改自己密码；新增 `buildUpdateDetail` 生成字段级变更摘要 |
| `AdminManageServiceTest` | 补 `AdminOperationLogService` mock，并对新增/修改/删除三个用例断言日志被调用 |

### 10.3 记录点与摘要格式

| 操作 | action | detail 示例 |
|---|---|---|
| 新增管理员 | `CREATE` | `username=bob, role=2` |
| 修改管理员 | `UPDATE` | `role:2->3, name:小明->小红, password:reset` |
| 启停管理员 | `STATUS` | `status:1->0` |
| 删除管理员 | `DELETE` | `username=bob, released=bob_del_5` |
| 改自己密码 | `RESET_PASSWORD` | `修改自己的密码` |

设计约束：

- **不记录密码明文**，密码重置只标记 `password:reset`；
- `detail` 超过 500 字符会被截断（与字段长度对齐），避免因超长导致整条日志写入失败；
- 操作人账号冗余存快照，账号后续改名/逻辑删除后日志仍可读；
- IP 优先取 `X-Forwarded-For` 首段，非 Web 线程（如定时任务）记录为空。

### 10.4 失败策略（有意为之的取舍）

日志写入包在 `try/catch` 里，**失败不抛出**，只记 `log.error`。理由：审计表异常不应把管理员的业务操作一起弄挂。
代价是可能丢日志。生产环境若要强一致，应改为与业务同事务写入（或写入独立审计库），这一点已在代码注释和数据库文档中标注。

### 10.5 测试（新增 6 个，累计 40 个）

| 用例 | 覆盖点 |
|---|---|
| 无登录上下文 | 不写日志（定时任务线程安全） |
| 正常记录 | adminId / 操作人账号快照 / action / target / detail / createTime 正确，IP 为空 |
| 写入失败 | 异常被吞、不影响调用方 |
| 超长摘要 | 截断到 500 字符并以 `...` 结尾 |
| 分页归一化 | 缺省补 1/10，动作码翻译为中文 |
| pageSize 上限 | 999 被压到 100 |

`Tests run: 40, Failures: 0, Errors: 0`

### 10.6 遗留

1. **前端还没有日志页面**：接口已就绪，页面在批次四补（`/admin/operation-log/page`）。
2. **日志只覆盖管理员管理**：审核、分类、用户管理的操作尚未记录；`AdminLogAction` 已预留扩展空间，需要时按 `target_type` 增加即可。
3. **接口文档未同步**：`docs/swapu_admin_interface_doc.md` 在批次五统一补 `/admin/me` 与 `/admin/operation-log/page`。

---

## 十一、批次四实施记录（前端）

### 11.1 权限的唯一数据源

```
登录成功 → GET /admin/me → 写入 localStorage.adminInfo（含 role / roleLabel / permissions[]）
```

- 登录后调用一次；每次进入后台布局（`AdminLayout` mounted）再刷新一次 —— 角色被调整后菜单能及时变化；
- 路由守卫发现旧会话缺 `permissions` 时会主动补拉一次（升级后老用户不需要重新登录）；
- 前端只做「显示/拦截」体验层，**最终鉴权仍在服务端**，绕过前端也拿不到数据。

### 11.2 新增文件

| 文件 | 作用 |
|---|---|
| `utils/permission.js` | `PERMISSION` 常量（与后端 `AdminPermission` 对齐）+ `currentPermissions/hasPermission/hasAnyPermission` |
| `api/operationLog.js` | `pageOperationLogs` |
| `views/OperationLog.vue` | 操作日志页：动作/操作人/时间范围筛选 + 分页表格（时间、操作人、动作标签、对象、摘要、IP） |

### 11.3 修改文件

| 文件 | 改动 |
|---|---|
| `utils/enums.js` | `ADMIN_ROLE` 增加 `3: 运营管理员`；新增 `ADMIN_LOG_ACTION` 与下拉选项 |
| `api/auth.js` | 新增 `getAdminMe()` 与 `refreshAdminMe()`（拉取并写回缓存） |
| `router/index.js` | 路由 meta 由 `requiresSuper` 改为 `permission`；守卫按权限点跳转；新增 `/admin/operation-log` 路由 |
| `components/AdminLayout.vue` | 菜单按 `permissions` 过滤并新增「系统管理」分组；进入后台刷新 `/admin/me`；角色名优先用后端 `roleLabel` |
| `views/Dashboard.vue` | 无 `USER_MANAGE`/`CATEGORY_MANAGE` 时**不展示卡片也不发请求**，普通管理员不再看到 403 报错 |
| `views/AdminManage.vue` | 筛选与表单增加「运营管理员」；`isSuper` 改为权限判断 `canManage`；编辑回填 `form.role = row.role \\|\\| 2`（不再把 role 二值化） |
| `views/Login.vue` | 登录成功后拉取 `/admin/me` 写入权限（失败不阻断登录） |
| 后端 `AdminOperationLogPageQueryDTO` | 时间字段补 `@DateTimeFormat(iso = ISO.DATE_TIME)`，支持前端 `datetime-local` 传值 |

### 11.4 三处一致，避免前后端规则漂移

| 层 | 依据 |
|---|---|
| 菜单 | `AdminLayout` 按 `permissions` 过滤 navGroups |
| 路由 | `router.beforeEach` 读 `meta.permission`，无权限回落工作台 |
| 页面内请求 | `Dashboard` 按权限决定是否发起用户/分类请求 |

菜单与路由共用同一组 `PERMISSION` 常量，新增页面只改一处 meta 即可。

### 11.5 不同角色看到的效果

| 角色 | 菜单 |
|---|---|
| 超级管理员 | 工作台 / 商品审核 / 评论审核 / 审核流水 / 分类管理 / 用户管理 / 管理员管理 / 操作日志 / 个人设置 |
| 运营管理员 | 工作台 / 三个审核页 / 分类管理 / 用户管理 / 个人设置 |
| 普通管理员 | 工作台 / 三个审核页 / 个人设置（工作台只显示两个审核统计卡） |

### 11.6 验证

| 项 | 结果 |
|---|---|
| 前端构建 | `npm run build` → ✓ 52 modules，0 error |
| 后端构建 | `clean test-compile` → BUILD SUCCESS，0 error |
| 后端单测 | 40 个全通过 |

### 11.7 关于「403 统一提示」

原计划写"403 统一提示"，实际落地方式：后端返回 `code=403` + 中文文案「当前角色无权执行该操作」，`request.js` 抛 `ApiError`，各页面已有的 `error-box` 直接展示该文案——不需要额外加全局拦截。若后续要改成全局 toast，只需在 `request.js` 判断 `payload.code === 403` 即可。

### 11.8 遗留（批次五）

1. **前端没有自动化测试**：本次靠构建 + 人工路径验证，未引入 Vitest；
2. `docs/swapu_admin_interface_doc.md` 尚未补 `/admin/me`、`/admin/operation-log/page`；
3. `swapu_admin` 的 `application.yml.example` 无需变更（无新增配置项）。

---

## 十二、批次五实施记录（文档收尾）

### 12.1 同步的文档

| 文档 | 改动 |
|---|---|
| [`swapu_admin_api_inventory.md`](./swapu_admin_api_inventory.md) | ①「一页速览」改为 5 个 Controller / 26 个接口、补角色与两道鉴权说明；②接口总表由「超管」列改为**「权限点」列**并补 ADM-09、LOG-01；③新增 ADM-09（`/admin/me`）与 LOG-01（`/admin/operation-log/page`）完整字段说明；④ADM-02/03/04/05/06/08 更新权限与错误码；⑤错误码表补 403、4106，并说明「业务 401/403 都不登出」；⑥验收清单改为按三角色验证 |
| [`swapu_admin_interface_doc.md`](./swapu_admin_interface_doc.md) | 顶部注明角色已扩为三种、越权返回 403，并指向本文；角色枚举补 3=运营 |
| [`swapu_database.md`](./swapu_database.md) | 前三批已同步：`admin.deleted`、`admin_operation_log` 表与建表 SQL |
| `README.md` | 管理端模块描述更新为「管理员管理（含角色权限与操作日志）、商品/评论审核、分类管理、用户管理」，去掉「尚未完成」 |

### 12.2 整体交付汇总

| 维度 | 结果 |
|---|---|
| 角色 | 超级管理员(1) / 运营管理员(3) / 普通管理员(2) |
| 权限点 | `AUDIT` / `CATEGORY_MANAGE` / `USER_MANAGE` / `ADMIN_MANAGE` |
| 接口 | 26 个（管理员 9 + 审核 6 + 分类 5 + 用户 5 + 操作日志 1） |
| 新增/改动的后端类 | 权限枚举、注解、权限服务、拦截器、操作日志实体/Mapper/Service/Controller、`AdminMeVO`、`AdminOperationLogVO/DTO` 等 |
| 前端 | 权限工具、`/admin/me` 接入、权限化菜单与路由、三角色下拉、操作日志页、Dashboard 按权限渲染 |
| 数据库 | `admin.deleted`、`admin_operation_log`（两份迁移脚本） |
| 测试 | 管理端 40 个单测（权限矩阵 5 + 权限服务 8 + 拦截器 5 + 管理员 CRUD 16 + 操作日志 6） |

### 12.3 部署清单（按顺序）

```bash
# 1) 逻辑删除字段
mysql -u root -p campus < swapu_admin/src/main/resources/sql/admin_v2.sql
# 2) 操作日志表
mysql -u root -p campus < swapu_admin/src/main/resources/sql/admin_operation_log.sql
# 3) 重新构建并启动管理端
mvn -pl swapu_admin -am clean package -DskipTests
# 4) 前端
cd swapu_admin_front && npm install && npm run build
```

### 12.4 后续可选改进

1. **平台统计接口**：工作台图表仍是"规划未实现"，需要新增 `DashboardController`；
2. **其他模块的操作日志**：目前只记管理员管理，审核/分类/用户管理的写操作未留痕（按 `target_type` 扩展即可）；
3. **权限查询加缓存**：当前每个受保护请求查一次库，管理端量小可接受；要优化就加 Redis 60s 缓存，不要退回把角色写进 token；
4. **前端自动化测试**：可引入 Vitest 覆盖 `permission.js` 与路由守卫；
5. **登录密码仍是 MD5**：与本次支付密码独立化是两件事，建议后续用同样的惰性升级模式改成 BCrypt。
