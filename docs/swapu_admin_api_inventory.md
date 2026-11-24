# SwapU 管理端接口清单（前端开发版）

> 依据：`swapu_admin` 源码逐接口核对，核对时间 2026-11-21；覆盖 `controller/admin` 下全部 5 个 Controller（管理员 / 审核 / 分类 / 用户 / 操作日志）。
> 本文只描述**代码里真实存在**的接口。旧稿 [`swapu_admin_interface_doc.md`](./swapu_admin_interface_doc.md) 是最初设计稿，其中「平台统计」「头像审核」等尚未实现，与本文冲突时以本文和源码为准。
> **2026-11 新增权限体系**：三种角色 + 四个权限点，接口按权限点鉴权，越权返回 `code=403`；角色与权限矩阵见 [`swapu_admin_rbac.md`](./swapu_admin_rbac.md)。
> 文中路径均为后端真实路径，不含任何前端代理前缀。

---

## 0. 一页速览

| 项 | 结论 |
| --- | --- |
| 服务端口 | `8081`（用户端是 `8080`，两个服务独立） |
| Controller 数 | 7 个：管理员 / 审核 / 分类 / 用户管理 / 管理员操作日志 / 平台统计 / 图形验证码 |
| 接口总数 | **31 个**（管理员 9 + 审核 6 + 分类 5 + 用户 5 + 操作日志 1 + 平台统计 4 + 图形验证码 1） |
| 鉴权 | 两道拦截器：① JWT 验签（放行 `POST /admin/login` 与 `/admin/captcha/**`）② 权限点校验（越权返回 `code=403`） |
| 角色 | 1 超级管理员（全部权限）/ 3 运营管理员（审核 + 分类 + 用户）/ 2 普通管理员（仅审核） |
| 响应 | `{ code, msg, data }`，成功 `code=200`；分页 `data = { total, records }` |
| 未鉴权/过期 | **HTTP 401** + `{code:401,msg:"未登录或登录已过期"}` |
| 已实现功能 | 管理员登录与账号管理、管理员操作日志、商品审核、评论审核、审核流水、分类 CRUD/启停、用户查询/启停/信用分/交易统计、平台统计（总览/流量趋势/商品状态/分类销售） |
| 未实现功能 | **头像审核 0 个接口**、订单/商品运营管理接口 |
| 初始超管 | `root / 123456`（见 `swapu_admin/src/main/resources/sql/admin.sql`） |

### 前端必须注意的 6 个坑

1. **分页参数不是都有默认值**：`GET /admin/page`、`GET /admin/user/page` 的 DTO 没有默认值，前端必须始终传 `pageNum`、`pageSize`；审核列表和流水列表才有默认值 `1 / 10`。
2. **HTTP 401 和业务 code 含义不同**：HTTP 401 才是登录失效（清 token 回登录页）；HTTP 200 且 `code=403` 表示**当前角色无权限**（提示 `msg`，不要登出）；`code=401` 多为参数校验失败等业务失败（同样只提示）。
3. **token 存储键要加命名空间**：用户端已占用 `localStorage` 的 `token`、`userInfo`、`userId`。管理端若并入同一站点，必须改用 `adminToken`、`adminInfo`，否则两套登录态互相覆盖。
4. **成功提示在 `data` 不在 `msg`**：新增/修改/删除类接口返回 `msg:"success"`，真正的中文提示在 `data`（如 `"分类删除成功"`）；`data` 也可能是 `null`，前端不要依赖它。
5. **没有统计接口**：工作台的「成交额、今日订单、流量趋势、商品状态分布」目前**无后端接口**，不能按旧文档直接开发图表页。
6. **没有上传、登出、刷新 token 接口**：头像只能填 URL；登出由前端清 localStorage 完成；token 有效期 2 小时，过期后重新登录。

---

## 1. 接入信息

### 1.1 服务与开发代理

- Base URL：`http://localhost:8081`
- 接口文档（Knife4j，启动后可直接调试）：`http://localhost:8081/doc.html`
- 管理端接口全部以 `/admin` 开头；后端**没有配置 CORS**，开发期必须通过 Vite 代理或同源部署访问。

若并入 `swapu_front`（推荐方案，见第 8 节），在 `vite.config.js` 的 `server.proxy` 增加：

```js
// 管理端接口走 8081；/admin/xxx 的页面请求（Accept: text/html）走前端路由
'/admin': {
  target: 'http://localhost:8081',
  changeOrigin: true,
  bypass: spaBypass   // 复用文件顶部已有的 spaBypass
}
```

> 注意：管理端页面路由也建议放在 `/admin/*` 下，因此必须保留 `bypass: spaBypass`，否则浏览器直接访问或刷新 `/admin/users` 会被代理到 8081 变成 404。

### 1.2 鉴权

| 项 | 说明 |
| --- | --- |
| 请求头 | `token: <adminJwt>`（头名来自 `sky.jwt.admin-token-name`，当前固定为 `token`） |
| JWT 载荷 | `adminId`、`username` |
| 有效期 | 2 小时（`admin-ttl: 7200000` 毫秒） |
| 无 token / 解析失败 | 拦截器返回 **HTTP 401**，body `{"code":401,"msg":"未登录或登录已过期","data":null}` |
| 角色 | `1` 超级管理员、`2` 普通管理员；**只有管理员的新增/修改/启停/删除**在 Service 层校验超管，其余接口任意已登录管理员都可用 |

登录成功后前端保存：

```js
localStorage.setItem('adminToken', data.token)
localStorage.setItem('adminInfo', JSON.stringify(data.adminInfo))
```

`adminInfo = { id, username, name, avatar, role }`，没有 `status`、`createTime`。

### 1.3 统一响应与错误处理

```json
{ "code": 200, "msg": "success", "data": {} }
```

| 场景 | HTTP 状态 | body.code | 前端处理 |
| --- | --- | --- | --- |
| 成功 | 200 | 200 | 取 `data` |
| 未登录 / token 过期 | 401 | 401 | 清 `adminToken`、`adminInfo`，跳 `/admin/login` |
| 业务校验失败 | 200 | 401 / 403 / 4101-4403 | 直接展示 `msg`；`code=401` 多为参数问题，`code=403` 是角色无权限，**都不要登出** |
| 未捕获异常 | 200 | 401 | `msg = "系统繁忙，请稍后重试"` |

- 分页：`data = { "total": 100, "records": [...] }`，没有 `pages`、`pageNum`，页码由前端按 `total / pageSize` 计算。
- 时间：统一 `yyyy-MM-dd HH:mm:ss` 字符串（`@JsonFormat`，GMT+8）。
- 金额：JSON 数字（`BigDecimal` 序列化为 number），展示时做两位小数。
- 业务错误码全集：

| code | 含义 |
| --- | --- |
| 401 | 未登录 / token 失效 / 通用参数校验失败（重载码，需结合 msg） |
| **403** | **当前角色无该操作权限（权限点不足）** |
| 4101 | 账号或密码错误 |
| 4102 | 账号已被禁用 |
| 4103 | 用户名已存在 |
| 4104 | 不能禁用/删除自己，或不能修改自己的角色 |
| 4105 | 至少保留一名启用的超级管理员 |
| **4106** | **角色取值不合法（只允许 1/2/3）** |
| 4200 | 审核参数不合法（状态筛选值、审核结论、驳回原因超长等） |
| 4201 | 审核对象不存在（商品/评论） |
| 4202 | 驳回时必须填写原因 |
| 4203 | 当前状态不可审核 |
| 4301 | 分类名称已存在 |
| 4302 | 分类下存在在售商品（禁用）或存在商品（删除） |
| 4303 | 分类不存在 |
| 4401 | 用户不存在 |
| 4402 | 用户状态值不合法 |
| 4403 | 信用分不合法（须 0-100） |

### 1.4 枚举字典

| 字典 | 取值 |
| --- | --- |
| 管理员角色 `admin.role` | 1 超级管理员，2 普通管理员，3 运营管理员 |
| 权限点 `AdminPermission` | `AUDIT` 审核、`CATEGORY_MANAGE` 分类管理、`USER_MANAGE` 用户管理、`ADMIN_MANAGE` 管理员管理 |
| 管理员操作日志动作 `admin_operation_log.action` | `CREATE` 新增管理员、`UPDATE` 修改管理员、`STATUS` 启停管理员、`DELETE` 删除管理员、`RESET_PASSWORD` 修改密码 |
| 管理员状态 `admin.status` | 1 启用，0 禁用 |
| 商品状态 `product.status` | 0 待审核，1 在售（审核通过），2 已售出，3 已下架，4 审核驳回 |
| 评论状态 `comment.status` | 0 待审核，1 已发布，2 审核驳回 |
| 审核类型 `audit_record.type` | 1 商品，2 评论，3 头像（仅流水里有，当前无头像审核接口） |
| 审核结论 `audit_record.result` | 1 通过，2 驳回 |
| 用户状态 `user.status` | 1 正常，0 禁用 |
| 分类状态 `category.status` | 1 启用，0 禁用 |
| 订单状态（交易统计用） | 2 待支付，3 待发货，4 待收货，5 已收货，6 取消 |
| 用户行为类型 `user_behavior_log.behavior_type` | 1 浏览，2 收藏，3 购买，4 搜索 |

### 1.5 分页约定

| 接口 | pageNum 默认 | pageSize 默认 | 排序 |
| --- | --- | --- | --- |
| `GET /admin/page` | **无（必传）** | **无（必传）** | 创建时间倒序 |
| `GET /admin/user/page` | **无（必传）** | **无（必传）** | 创建时间倒序 |
| `GET /admin/audit/product/page` | 1 | 10 | 发布时间倒序 |
| `GET /admin/audit/comment/page` | 1 | 10 | 评论时间倒序 |
| `GET /admin/audit/record/page` | 1 | 10 | 审核时间倒序 |
| `GET /admin/operation-log/page` | 1 | 10（上限 100） | 操作时间倒序 |

---

## 2. 接口总表（31 个）

| 编号 | 模块 | 方法 | 路径 | 用途 | 登录 | 权限点 | 对应页面 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| CAP-01 | 图形验证码 | GET | `/admin/captcha/image` | 获取登录图形验证码 | 否 | 公开 | 管理端登录 |
| ADM-01 | 管理员 | POST | `/admin/login` | 管理员登录 | 否 | 公开 | 管理端登录 |
| ADM-02 | 管理员 | POST | `/admin` | 新增管理员 | 是 | `ADMIN_MANAGE` | 管理员管理 |
| ADM-03 | 管理员 | GET | `/admin/page` | 分页查询管理员 | 是 | `ADMIN_MANAGE` | 管理员管理 |
| ADM-04 | 管理员 | PUT | `/admin` | 修改管理员/重置密码 | 是 | `ADMIN_MANAGE` | 管理员管理 |
| ADM-05 | 管理员 | PUT | `/admin/{id}/status` | 启用/禁用管理员 | 是 | `ADMIN_MANAGE` | 管理员管理 |
| ADM-06 | 管理员 | DELETE | `/admin/{id}` | 删除管理员（逻辑删除） | 是 | `ADMIN_MANAGE` | 管理员管理 |
| ADM-07 | 管理员 | PUT | `/admin/password` | 修改自己的密码 | 是 | 自助 | 个人设置 |
| ADM-08 | 管理员 | GET | `/admin/{id}` | 管理员详情回显 | 是 | 本人 或 `ADMIN_MANAGE` | 管理员管理/个人设置 |
| ADM-09 | 管理员 | GET | `/admin/me` | 当前登录管理员信息与权限 | 是 | 自助 | 全局（菜单/路由守卫） |
| AUD-01 | 审核 | GET | `/admin/audit/product/page` | 商品审核列表 | 是 | `AUDIT` | 商品审核 |
| AUD-02 | 审核 | GET | `/admin/audit/product/{id}` | 商品审核详情 | 是 | `AUDIT` | 商品审核 |
| AUD-03 | 审核 | POST | `/admin/audit/product/review` | 商品通过/驳回 | 是 | `AUDIT` | 商品审核 |
| AUD-04 | 审核 | GET | `/admin/audit/comment/page` | 评论审核列表 | 是 | `AUDIT` | 评论审核 |
| AUD-05 | 审核 | POST | `/admin/audit/comment/review` | 评论通过/驳回 | 是 | `AUDIT` | 评论审核 |
| AUD-06 | 审核 | GET | `/admin/audit/record/page` | 审核流水 | 是 | `AUDIT` | 审核流水 |
| CAT-01 | 分类 | GET | `/admin/category/list` | 分类列表（含停用） | 是 | 登录即可（只读字典） | 分类管理/审核页筛选 |
| CAT-02 | 分类 | POST | `/admin/category` | 新增分类 | 是 | `CATEGORY_MANAGE` | 分类管理 |
| CAT-03 | 分类 | PUT | `/admin/category` | 修改分类 | 是 | `CATEGORY_MANAGE` | 分类管理 |
| CAT-04 | 分类 | PUT | `/admin/category/{id}/status` | 启用/禁用分类 | 是 | `CATEGORY_MANAGE` | 分类管理 |
| CAT-05 | 分类 | DELETE | `/admin/category/{id}` | 删除分类 | 是 | `CATEGORY_MANAGE` | 分类管理 |
| USR-01 | 用户 | GET | `/admin/user/page` | 分页查询用户 | 是 | `USER_MANAGE` | 用户管理 |
| USR-02 | 用户 | GET | `/admin/user/{id}` | 用户详情 | 是 | `USER_MANAGE` | 用户管理 |
| USR-03 | 用户 | PUT | `/admin/user/{id}/status` | 启用/禁用用户 | 是 | `USER_MANAGE` | 用户管理 |
| USR-04 | 用户 | PUT | `/admin/user/{id}/credit-score` | 修改信用分 | 是 | `USER_MANAGE` | 用户管理 |
| USR-05 | 用户 | GET | `/admin/user/{id}/trade-statistics` | 用户交易统计 | 是 | `USER_MANAGE` | 用户管理 |
| LOG-01 | 操作日志 | GET | `/admin/operation-log/page` | 分页查询管理员操作日志 | 是 | `ADMIN_MANAGE` | 操作日志 |
| STA-01 | 平台统计 | GET | `/admin/statistics/overview` | 平台总览指标 | 是 | `AUDIT` | 工作台 |
| STA-02 | 平台统计 | GET | `/admin/statistics/traffic-trend` | 流量趋势（按天） | 是 | `AUDIT` | 工作台 |
| STA-03 | 平台统计 | GET | `/admin/statistics/product-status` | 商品状态分布 | 是 | `AUDIT` | 工作台 |
| STA-04 | 平台统计 | GET | `/admin/statistics/category-sales` | 分类成交额 Top N | 是 | `AUDIT` | 工作台 |

---

## 3. 管理员模块（9 个）

### CAP-01 获取登录图形验证码

- **GET** `/admin/captcha/image`，无需登录（令牌与权限两个拦截器都已放行该路径）
- 响应 `data`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| captchaKey | string | 本次验证码标识，登录时原样回传 |
| captchaImage | string | base64 图片，形如 `data:image/png;base64,...` |
| expireSeconds | number | 有效期（秒），默认 120 |

- 说明：验证码**一次性**，校验后立即从 Redis 删除（无论对错）；同一 IP 每分钟最多生成 30 次，超限返回 `code=401`。
- 前端：登录页挂载时拉取一张，登录失败后必须重新拉取再提交。

### ADM-01 管理员登录

- **POST** `/admin/login`，无需登录
- 请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| username | string | 是 | 管理员账号 |
| password | string | 是 | 明文密码，后端 BCrypt 校验 |
| captchaKey | string | 是 | CAP-01 返回的标识 |
| captchaCode | string | 是 | 管理员输入的验证码，忽略大小写 |

```json
{ "username": "superadmin", "password": "123456", "captchaKey": "9f2c1d7a...", "captchaCode": "A7K2" }
```

- 响应 `data`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| token | string | 管理端 JWT，后续放在请求头 `token` |
| expireTime | number | **有效期时长（毫秒）**，固定 7200000，不是过期时间戳 |
| adminInfo.id | number | 管理员 ID |
| adminInfo.username | string | 账号 |
| adminInfo.name | string | 姓名 |
| adminInfo.avatar | string/null | 头像 URL |
| adminInfo.role | number | 1 超管，2 普通，3 运营 |

- 错误：`4101` 账号或密码错误（账号为空也返回它）、`4102` 账号已被禁用；验证码不通过返回 `code=401` 与具体原因（未填写/已过期/不正确）。
- 前端：登录成功后保存 `adminToken`、`adminInfo`，**再调 `GET /admin/me` 拉取 `permissions`**，菜单与路由守卫按权限点判断（不要硬编码 `role === 1`）。

### ADM-02 新增管理员（`ADMIN_MANAGE`）

- **POST** `/admin`，请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| username | string | 是 | 2-20 字符，唯一 |
| password | string | 是 | 6-20 位 |
| name | string | 否 | 姓名 |
| avatar | string | 否 | 头像 URL |
| role | number | 否 | 1 超管 / 2 普通 / 3 运营，默认 2 |

- 响应 `data`：`{ "adminId": 3 }`
- 错误：`403` 当前角色无 `ADMIN_MANAGE` / 账号或密码长度不合法、`4103` 用户名已存在、`4106` 角色取值不合法。

### ADM-03 分页查询管理员（`ADMIN_MANAGE`）

- **GET** `/admin/page`
- Query：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| pageNum | number | **是** | 从 1 开始，DTO 无默认值 |
| pageSize | number | **是** | 每页条数 |
| username | string | 否 | 账号模糊查询 |
| role | number | 否 | 1 / 2 / 3 |
| status | number | 否 | 1 / 0 |

- 响应 `data.records[]`：`id`、`username`、`name`、`avatar`、`role`、`status`、`createTime`（**没有 updateTime、password**）。
- 该接口需要 `ADMIN_MANAGE`，普通管理员与运营管理员均无权访问；已逻辑删除的账号不会出现在列表中。

### ADM-04 修改管理员（`ADMIN_MANAGE`）

- **PUT** `/admin`，请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | number | 是 | 管理员 ID |
| name | string | 否 | 传空字符串不会更新 |
| avatar | string | 否 | 传空字符串不会更新 |
| role | number | 否 | 修改角色：1 / 2 / 3；**不能改自己的角色** |
| password | string | 否 | 传入则重置密码，6-20 位 |

- **username 不可修改**（Mapper 不更新该列），前端编辑表单里账号应只读。
- 错误：`403` 当前角色无 `ADMIN_MANAGE` / 管理员不存在 / 密码长度不合法；`4104` 尝试修改自己的角色；`4105` 把最后一名启用的超级管理员降级为任意非超管角色；`4106` 角色取值不合法。
- 成功响应 `data` 为字符串 `"管理员信息修改成功"`，`msg` 仍是 `success`。

### ADM-05 启用/禁用管理员（`ADMIN_MANAGE`）

- **PUT** `/admin/{id}/status`，请求体 `{ "status": 1 }`（1 启用 / 0 禁用）
- 错误：`403` 当前角色无 `ADMIN_MANAGE` / 管理员不存在 / 状态值不合法；`4104` 不能禁用自己；`4105` 不能禁用最后一名启用的超管。
- 响应 `data`：`"管理员状态修改成功"`。

### ADM-06 删除管理员（`ADMIN_MANAGE`）

- **DELETE** `/admin/{id}`
- **逻辑删除**：置 `deleted=1`，同时把账号名改写为 `原账号_del_{id}` 以释放原账号名（删除后可重新创建同名账号）；记录保留，审核流水里的 `reviewer_id` 仍可追溯。
- 错误：`403` 当前角色无 `ADMIN_MANAGE` / 管理员不存在；`4104` 不能删除自己；`4105` 不能删除最后一名启用的超管。
- 响应 `data`：`"管理员删除成功"`。

### ADM-07 修改自己的密码

- **PUT** `/admin/password`，请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| oldPassword | string | 是 | 原密码 |
| newPassword | string | 是 | 6-20 位，且不能与原密码相同 |

- 错误：`401` 原密码不正确 / 新密码与旧密码相同 / 新密码长度不合法。
- 响应 `data`：`"密码修改成功"`；修改后旧 token 仍有效（JWT 不主动失效），前端可提示重新登录。

### ADM-08 管理员详情

- **GET** `/admin/{id}`
- 响应 `data`：`id`、`username`、`name`、`avatar`、`role`、`status`、`createTime`、`updateTime`（`password`、`deleted` 被 `@JsonIgnore` 排除）。
- **id 不存在时返回业务错误 `管理员不存在`**（逻辑删除的账号也视为不存在）。
- 权限：**本人可查自己**；查他人需要 `ADMIN_MANAGE`，否则 `403`。

### ADM-09 当前登录管理员信息与权限

- **GET** `/admin/me`
- 响应 `data`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | long | 管理员 ID |
| username | string | 登录账号 |
| name | string | 姓名 |
| avatar | string | 头像 URL |
| role | int | 1 超级管理员 / 2 普通管理员 / 3 运营管理员 |
| roleLabel | string | 角色中文名 |
| permissions | string[] | 权限点列表，如 `["ADMIN_MANAGE","AUDIT",...]` |

- 前端用它作为**菜单与路由守卫的唯一数据源**：登录后调一次、进入后台布局时再调一次。
- 账号被禁用后调用会返回 `4102`（即使 token 未过期）。

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1, "username": "root", "name": "超级管理员", "avatar": "",
    "role": 1, "roleLabel": "超级管理员",
    "permissions": ["ADMIN_MANAGE", "AUDIT", "CATEGORY_MANAGE", "USER_MANAGE"]
  }
}
```

---

## 4. 审核模块（6 个）

> 审核对象：商品（`product.status` 只有从 `0` 才能流转）和评论（`comment.status` 只有从 `0` 才能流转）。
> 每次通过/驳回都会写 `audit_record`（`type`、`target_id`、`result`、`reason`、`reviewer_id`、`review_time`），`ai_result` 当前恒为 `null`。
> 当前没有头像审核接口；`audit_record.type=3` 只可能出现在流水里。

### AUD-01 商品审核列表

- **GET** `/admin/audit/product/page`
- Query：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| pageNum | number | 否 | 默认 1 |
| pageSize | number | 否 | 默认 10 |
| status | number | 否 | **默认 0（待审核）**；只允许 0 / 1 / 4，传 2、3 会报 `4200` |
| categoryId | number | 否 | 分类过滤 |
| title | string | 否 | 标题模糊查询 |

- 响应 `data.records[]`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 商品 ID |
| title | string | 标题 |
| userId | number | 发布人 ID |
| username | string | 发布人账号 |
| categoryId | number | 分类 ID |
| categoryName | string | 分类名称（分类被删后为 null） |
| price | number | 售价 |
| originalPrice | number | 原价 |
| images | string[] | 图片数组，无图时为 `[]` |
| productCondition | number | 成色 |
| status | number | 0 / 1 / 4 |
| createTime | string | 发布时间 |

- 错误：`4200` 状态筛选值不合法。

### AUD-02 商品审核详情

- **GET** `/admin/audit/product/{id}`
- 响应 `data`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| product | object | 商品完整实体：`id`、`userId`、`title`、`description`、`categoryId`、`price`、`originalPrice`、`images[]`、`productCondition`、`status`、`viewCount`、`isTop`、`quantity`、`createTime`、`updateTime` |
| seller | object/null | 发布人：`id`、`username`、`nickname`、`college`、`creditScore`；用户被删时为 null |
| auditRecords | array | 该商品历史审核流水，字段见 AUD-06，时间倒序 |

- 错误：`4201` 商品不存在。
- 前端：详情页/抽屉需展示 `description`、图片轮播、卖家信用分，以及历史审核记录（含驳回原因）。

### AUD-03 商品审核通过/驳回

- **POST** `/admin/audit/product/review`
- 请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| productId | number | 是 | 商品 ID |
| result | number | 是 | 1 通过 / 2 驳回 |
| reason | string | 驳回必填 | 驳回原因，≤500 字符；通过时后端丢弃 |

- 状态流转：通过 `0 → 1`（在售）；驳回 `0 → 4`（审核驳回）。
- 错误：`4200` 商品 ID 为空 / 审核结论不合法 / 驳回原因超 500 字；`4201` 商品不存在；`4202` 驳回未填原因；`4203` 商品当前状态不是待审核（并发/重复审核）。
- 响应 `data`：`null`。
- 前端：驳回原因用弹窗强制填写；成功后刷新列表并重新拉 `total`。

### AUD-04 评论审核列表

- **GET** `/admin/audit/comment/page`
- Query：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| pageNum | number | 否 | 默认 1 |
| pageSize | number | 否 | 默认 10 |
| status | number | 否 | **默认 0**；只允许 0 / 1 / 2 |
| receiverId | number | 否 | 按**被评论人** ID 过滤 |
| keyword | string | 否 | 评论内容模糊查询 |

- 响应 `data.records[]`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 评论 ID |
| receiverId | number | 被评论人 ID |
| receiverUsername | string | 被评论人账号 |
| receiverNickname | string | 被评论人昵称 |
| userId | number | 评论人 ID（注意不是 `senderId`） |
| username | string | 评论人账号 |
| content | string | 评论内容 |
| images | string[] | 评论图片；后端把单图字段包装成数组，无图 `[]` |
| status | number | 0 / 1 / 2 |
| createTime | string | 评论时间（库里的 `send_time`） |

- **评论是用户对用户，没有商品 ID / 商品标题**，旧文档里的 `productId`、`productTitle` 不存在。
- 错误：`4200` 状态筛选值不合法。

### AUD-05 评论审核通过/驳回

- **POST** `/admin/audit/comment/review`
- 请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| commentId | number | 是 | 评论 ID |
| result | number | 是 | 1 通过 / 2 驳回 |
| reason | string | 驳回必填 | ≤500 字符 |

- 状态流转：通过 `0 → 1`；驳回 `0 → 2`。
- 错误：`4200` 参数不合法；`4201` 评论不存在；`4202` 驳回未填原因；`4203` 评论当前状态不是待审核。
- 响应 `data`：`null`。

### AUD-06 审核流水

- **GET** `/admin/audit/record/page`
- Query：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| pageNum | number | 否 | 默认 1 |
| pageSize | number | 否 | 默认 10 |
| type | number | 否 | 1 商品 / 2 评论 / 3 头像 |
| result | number | 否 | 1 通过 / 2 驳回 |
| reviewerId | number | 否 | 审核人（管理员）ID |

- 响应 `data.records[]`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 流水 ID |
| type | number | 1 / 2 / 3 |
| targetId | number | 被审对象 ID（商品/评论/用户） |
| result | number | 1 / 2 |
| reason | string/null | 驳回原因，通过时为 null |
| aiResult | string/null | AI 审核结论，当前恒为 null |
| reviewerId | number | 审核人 ID（**没有审核人姓名**） |
| reviewTime | string | 审核时间 |

- 错误：`4200` 类型或结论筛选值不合法。
- 前端：审核人姓名需要另查管理员列表做映射，或直接展示 `reviewerId`。

---

## 5. 分类管理（5 个）

### CAT-01 分类列表

- **GET** `/admin/category/list`
- 无参数，返回全部分类（**含停用**），按 `sort` 升序、`id` 升序。
- 响应 `data[]`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 分类 ID |
| name | string | 名称 |
| sort | number | 排序值，越小越靠前 |
| status | number | 1 启用 / 0 禁用 |
| createTime | string | 创建时间 |
| updateTime | string | 更新时间 |

- 空列表返回 `data: []`，不报错。
- 前端：分类下拉框（用户管理/商品筛选）同时可用此接口取启用项。

### CAT-02 新增分类

- **POST** `/admin/category`，请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| name | string | 是 | 去首尾空格后 1-50 字符，唯一 |
| sort | number | 否 | 默认 0 |

- 响应 `data`：`{ "categoryId": 5 }`
- 新增后默认 `status=1`。
- 错误：`401` 名称为空 / 超过 50 字符；`4301` 分类名称已存在。
- 成功后无需再手动清缓存，后端会清 Redis 的用户端分类缓存。

### CAT-03 修改分类

- **PUT** `/admin/category`，请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | number | 是 | 分类 ID |
| name | string | 否 | 传空字符串不更新 |
| sort | number | 否 | 传值才更新 |

- 错误：`401` 名称不合法；`4301` 名称与其他分类重复；`4303` 分类不存在。
- 响应 `data`：`"分类信息修改成功"`。

### CAT-04 启用/禁用分类

- **PUT** `/admin/category/{id}/status`，请求体 `{ "status": 1 }`
- 禁用前校验该分类下是否还有**在售**商品（`product.status=1`），有则拒绝。
- 错误：`401` 状态值不合法；`4302` 该分类下存在在售商品；`4303` 分类不存在。
- 响应 `data`：`"分类状态修改成功"`。

### CAT-05 删除分类

- **DELETE** `/admin/category/{id}`
- 分类下存在**任意商品**（不论状态）即拒绝。
- 错误：`4302` 该分类下存在商品；`4303` 分类不存在。
- 响应 `data`：`"分类删除成功"`。

---

## 6. 用户管理（5 个）

### USR-01 分页查询用户

- **GET** `/admin/user/page`
- Query：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| pageNum | number | **是** | DTO 无默认值 |
| pageSize | number | **是** | DTO 无默认值 |
| keyword | string | 否 | 用户名**或**昵称模糊 |
| studentId | string | 否 | 学号模糊 |
| phone | string | 否 | 手机号模糊 |
| status | number | 否 | 1 正常 / 0 禁用 |

- 响应 `data.records[]`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 用户 ID |
| studentId | string | 学号 |
| username | string | 账号 |
| nickname | string | 昵称 |
| avatar | string | 头像 |
| phone | string | 手机号 |
| email | string | 邮箱 |
| college | string | 学院 |
| balance | number | 余额 |
| creditScore | number | 信用分 |
| status | number | 1 / 0 |
| createTime | string | 注册时间 |
| updateTime | string | 更新时间 |

- 旧文档里的 `college` 精确筛选、`creditScoreMin` 不存在，实际是 `keyword`、`studentId`、`phone`。

### USR-02 用户详情

- **GET** `/admin/user/{id}`
- 响应 `data`：与列表单条一致（`UserManageVO`），**不包含交易统计**，需要另调 USR-05。
- 错误：`4401` 用户不存在。

### USR-03 启用/禁用用户

- **PUT** `/admin/user/{id}/status`，请求体 `{ "status": 1 }`（1 正常 / 0 禁用）
- 错误：`4401` 用户不存在；`4402` 状态值不合法。
- 响应 `data`：`"用户状态修改成功"`。
- 禁用后用户端登录与交易应被拦截（用户端逻辑），管理端不做二次校验。

### USR-04 修改信用分

- **PUT** `/admin/user/{id}/credit-score`（注意路径是 `credit-score`，不是旧文档的 `credit`）
- 请求体 `{ "creditScore": 90 }`，取值 0-100（含边界）。
- 错误：`4401` 用户不存在；`4403` 信用分不合法。
- 响应 `data`：`"信用分修改成功"`。
- **没有调整原因字段**，也没有操作日志记录接口。

### USR-05 用户交易统计

- **GET** `/admin/user/{id}/trade-statistics`
- 响应 `data`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| buyWaitPay | number | 买家侧：待支付订单数（status=2） |
| buyWaitReceive | number | 买家侧：待收货订单数（status=4） |
| buyCompleted | number | 买家侧：已成交订单数（status=5） |
| buyCanceled | number | 买家侧：已取消订单数（status=6） |
| buyTotalAmount | number | 买家侧：累计买入金额（status=5，无订单为 0） |
| sellWaitDeliver | number | 卖家侧：待发货订单数（status=3） |
| sellCompleted | number | 卖家侧：已成交订单数（status=5） |
| sellCanceled | number | 卖家侧：已取消订单数（status=6） |
| sellTotalAmount | number | 卖家侧：累计卖出金额（status=5，无订单为 0） |

- 错误：`4401` 用户不存在。
- 旧文档的 `publishCount`、`sellCount`、`buyCount`、`favoriteCount`、`creditScore` 在这个接口里**都不存在**。

---

## 6.1 管理员操作日志（1 个）

### LOG-01 分页查询管理员操作日志（`ADMIN_MANAGE`）

- **GET** `/admin/operation-log/page`
- Query：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| pageNum | number | 否 | 默认 1 |
| pageSize | number | 否 | 默认 10，**上限 100** |
| action | string | 否 | `CREATE` / `UPDATE` / `STATUS` / `DELETE` / `RESET_PASSWORD` |
| adminUsername | string | 否 | 操作人账号，模糊匹配 |
| startTime | string | 否 | ISO 时间，如 `2026-11-21T10:00` |
| endTime | string | 否 | ISO 时间 |

- 响应 `data.records[]`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 日志 ID |
| adminId | number | 操作人 ID |
| adminUsername | string | 操作人账号快照（账号改名/删除后仍可读） |
| action | string | 动作码 |
| actionDesc | string | 动作中文名（后端已翻译） |
| targetType | string | 对象类型，目前只有 `ADMIN` |
| targetId | number | 对象 ID |
| detail | string | 变更摘要，如 `role:2->3`、`status:1->0`、`password:reset`（**不含密码明文**） |
| ip | string/null | 操作 IP，非 Web 线程为空 |
| createTime | string | 操作时间 `yyyy-MM-dd HH:mm:ss` |

- 记录范围：管理员管理的 5 类写操作（新增/修改/启停/删除/改自己密码）。
- 错误：`403` 当前角色无 `ADMIN_MANAGE`（运营与普通管理员均无权查看）。

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 1,
    "records": [{
      "id": 12, "adminId": 1, "adminUsername": "root",
      "action": "UPDATE", "actionDesc": "修改管理员",
      "targetType": "ADMIN", "targetId": 3,
      "detail": "role:2->3", "ip": "127.0.0.1", "createTime": "2026-11-21 19:40:12"
    }]
  }
}
```

---

## 6.2 平台统计（4 个）

权限统一为 `AUDIT`（三种角色都有），因为工作台对所有人可见。

### STA-01 平台总览

- **GET** `/admin/statistics/overview`
- 响应 `data`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| onSaleCount | number | 在售商品数（`product.status = 1`） |
| pendingAuditCount | number | 待审核商品数（`status = 0`） |
| soldCount | number | 交易完成数（`orders.status = 5` 已收货） |
| totalAmount | number | 累计成交额（已完成订单合计） |
| totalViewCount | number | 平台商品总浏览量（`sum(product.view_count)`） |
| totalUserCount | number | 注册用户总数 |
| todayOrderCount | number | 今日新增订单数（按下单时间） |
| todayAmount | number | 今日成交额（按完成时间） |

```json
{ "code": 200, "msg": "success", "data": {
  "onSaleCount": 78, "pendingAuditCount": 0, "soldCount": 6, "totalAmount": 6441.00,
  "totalViewCount": 1506, "totalUserCount": 11, "todayOrderCount": 0, "todayAmount": 0.00 } }
```

### STA-02 流量趋势（按天）

- **GET** `/admin/statistics/traffic-trend`
- Query：`startDate`、`endDate`（`yyyy-MM-dd`，**都可选**）
  - 都不传 → 最近 7 天；只传一个 → 另一端自动补齐；
  - 开始晚于结束、或跨度超过 **90 天** → `code=4500`。
- 响应 `data[]`：`date`、`viewCount`、`orderCount`、`amount`
- **缺失日期会被补成 0**（长度 = 区间天数），前端不需要自己补点。

### STA-03 商品状态分布

- **GET** `/admin/statistics/product-status`
- 响应 `data[]`：`status`、`statusDesc`、`count`
- 只返回 `count > 0` 的状态，按 status 升序；`statusDesc` 取自 `StringConstant`（审核中/售卖中/已售出/已下架/审核驳回）。

### STA-04 分类成交额 Top N

- **GET** `/admin/statistics/category-sales`
- Query：`limit`（可选，缺省 10、上限 50）
- 响应 `data[]`：`categoryId`、`categoryName`、`salesCount`（成交件数）、`salesAmount`（成交额）
- 只统计已完成订单（`orders.status = 5`），按成交额倒序。

> **数据口径（前端展示时建议标注）**
> 1. **流量** = 登录用户的浏览行为条数（`user_behavior_log.behavior_type = 1`）。游客浏览不落行为日志，所以这是**下限**，不等于真实 PV。
> 2. **成交额** 按 `orders.receive_time`（收货时间）归集，只算 `status = 5`；已退货退款（`status = 8`）不计入。
> 3. **今日订单** 按 `create_time`，**今日成交额** 按 `receive_time`，两者可能不同步。

---

## 7. 规划但未实现的接口（前端不能依赖）

以下内容只存在于旧设计稿或 TODO，**当前源码中没有 Controller/Service 实现**，开发页面时必须先补后端或降级。

| 规划项 | 旧文档位置 | 现状 |
| --- | --- | --- |
| 平台总览 `/admin/statistics/overview` | 旧文档 5.1 | 无 Controller、无 Service、无 Mapper 查询 |
| 流量趋势 `/admin/statistics/traffic-trend` | 旧文档 5.2 | 同上 |
| 商品状态分布 `/admin/statistics/product-status` | 旧文档 5.3 | 同上 |
| 分类销售分布 `/admin/statistics/category-sales` | 旧文档 5.4 | 同上 |
| 头像审核列表 `/admin/audit/avatar/page` | 旧文档 2.6 | 无接口；`audit_record.type=3` 仅是个枚举值 |
| 头像审核通过/驳回 `/admin/audit/avatar/review` | 旧文档 2.7 | 同上 |
| 订单管理（列表/详情/退款审核） | README 提及 | 无管理端 Controller |
| 商品运营管理（下架/删除/置顶） | README 提及 | 无管理端 Controller |
| 操作日志查询 | TODO 提及 | 无接口 |

> 后端 `swapu_admin` 里还存在 `OrderService`、`ProductService`、`ChatService`、`AddressBookService`、`FavoriteService`、`WebSocket(/ws/chat/{userId})` 等从用户端复制过来的类，但**没有管理端 Controller 暴露它们**，前端不要按这些设计页面。

### 旧文档 → 实际代码 差异速查

| 旧文档 | 实际 |
| --- | --- |
| 用户详情返回 `{userInfo, tradeStatistics}` | 返回单个 `UserManageVO`，统计需另调 |
| 用户筛选 `college`、`creditScoreMin` | 实际 `keyword`、`studentId`、`phone`、`status` |
| 信用分接口 `/admin/user/{id}/credit` | 实际 `/admin/user/{id}/credit-score` |
| 评论筛选 `productId`、展示 `productTitle` | 实际按 `receiverId` 过滤，评论无商品关联 |
| 商品审核状态可选 0/1/2/3/4 | 实际只允许 0/1/4 |
| 审核流水有 AI 结论 | `aiResult` 字段存在但恒为 null |
| 分类列表只含启用 | 管理端返回全部分类（含停用） |
| `expireTime` 是过期时间戳 | 实际是有效期毫秒数 7200000 |
| 缺少 `GET /admin/{id}` | 实际存在，返回管理员详情（无密码） |
| 权限「仅超管」标注在多个接口 | 实际只有管理员增改启停删校验超管；查询、审核、分类、用户接口不校验角色 |

---

## 8. 为管理端前端做准备

### 8.1 项目形态选择

| 方案 | 做法 | 优点 | 代价 |
| --- | --- | --- | --- |
| A. 并入 `swapu_front`（TODO 推荐） | 在 `src/views/admin/` 建页面，`/admin/*` 路由组 + `AdminLayout` | 复用样式变量、`spaBypass` 配置；一个 dev server | 需在 `App.vue` 屏蔽用户端导航/页脚/WebSocket/悬浮客服；token 必须改名空间 |
| B. 独立 `swapu_admin_front` | 新建 Vite + Vue3 项目，proxy `/admin` → 8081 | 管理端与用户端彻底隔离 | 需复制样式/工具；README 已预留目录名但当前不存在 |

> 建议先用方案 A（当前工作目录就是 `swapu_front`），第 8.4、8.5 节按方案 A 给出改造点。
> **已确认采用方案 B**：管理端前端已作为独立项目 `swapu_admin_front` 搭建完成，以下方案 A 的 `App.vue` 改造不再需要。

### 8.2 建议页面与路由

| 路由 | 页面 | 依赖接口 | 状态 |
| --- | --- | --- | --- |
| `/admin/login` | 管理端登录 | ADM-01 | 可开发 |
| `/admin` 或 `/admin/dashboard` | 工作台 | 无统计接口 | **阻塞**，见 8.7 |
| `/admin/audit/products` | 商品审核 | AUD-01/02/03、CAT-01 | 可开发 |
| `/admin/audit/comments` | 评论审核 | AUD-04/05 | 可开发 |
| `/admin/audit/records` | 审核流水 | AUD-06 | 可开发 |
| `/admin/categories` | 分类管理 | CAT-01~05 | 可开发 |
| `/admin/users` | 用户管理 | USR-01~05 | 可开发 |
| `/admin/admins` | 管理员管理（超管） | ADM-02~06、08 | 可开发 |
| `/admin/profile` | 个人设置 | ADM-07、08 | 可开发 |

建议目录：

```
src/views/admin/
  Login.vue          Dashboard.vue      ProductAudit.vue
  CommentAudit.vue   AuditRecords.vue   CategoryManage.vue
  UserManage.vue     AdminManage.vue    AdminProfile.vue
src/components/admin/
  AdminLayout.vue    AdminSidebar.vue   AdminPager.vue    AdminModal.vue
src/api/admin/
  request.js         auth.js            audit.js
  category.js        user.js            admin.js
```

### 8.3 API 层约定

新增 `src/api/admin/request.js`，统一处理 token、HTTP 401、`code!==200`、查询串：

```js
const TOKEN_KEY = 'adminToken'

export class ApiError extends Error {
  constructor(code, msg) { super(msg); this.code = code }
}

function buildQuery(query) {
  if (!query) return ''
  const params = new URLSearchParams()
  Object.entries(query).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') params.append(k, v)
  })
  const s = params.toString()
  return s ? `?${s}` : ''
}

export async function request(path, { method = 'GET', query, body } = {}) {
  const headers = {}
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) headers.token = token
  if (body !== undefined) headers['Content-Type'] = 'application/json'

  const res = await fetch(path + buildQuery(query), {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body)
  })

  // 拦截器返回的 HTTP 401 才是登录失效
  if (res.status === 401) {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem('adminInfo')
    // 由调用方或路由守卫跳转，避免 api 层直接依赖 router
    throw new ApiError(401, '登录已过期，请重新登录')
  }

  const data = await res.json()
  if (data.code !== 200) throw new ApiError(data.code, data.msg || '请求失败')
  return data.data
}
```

各模块只写薄封装，例如：

```js
export const login = (body) => request('/admin/login', { method: 'POST', body })
export const pageProducts = (query) => request('/admin/audit/product/page', { query })
export const reviewProduct = (body) => request('/admin/audit/product/review', { method: 'POST', body })
```

### 8.4 登录态与路由守卫

- 存储：`adminToken`、`adminInfo`（JSON）。
- 路由：`/admin/login` 不需要登录；其余 `/admin/*` 在 `router.beforeEach` 中校验 `adminToken`，没有则 `next('/admin/login')`。
- 页面内捕获 `ApiError.code === 401` 且是 HTTP 401 时，清 token 并跳登录；业务 `code=401` 只弹 `msg`。
- 角色：`adminInfo.role !== 1` 时隐藏「新增/编辑/启停/删除管理员」按钮与 `/admin/admins` 菜单；后端仍会兜底返回 `401 无操作权限`。
- 登出：清 `adminToken`、`adminInfo`，`router.push('/admin/login')`，无后端接口。
- 若并入 `swapu_front`，`App.vue` 的 `showChrome` 需要改为同时排除 `/admin`：

```js
const showChrome = computed(() =>
  !['/login', '/register'].includes(route.path) && !route.path.startsWith('/admin')
)
```

并确认 `MessageToast`、`AiServiceWidget` 在 `/admin` 下不挂载（它们会建立用户端 WebSocket / 读取用户 token）。

### 8.5 Vite 代理

见 1.1。补充：

- 管理端页面路由与接口同前缀 `/admin`，**必须**配 `bypass: spaBypass`。
- 若独立项目，`spaBypass` 需要一并复制到新 `vite.config.js`。
- 不要把 `/admin` 配 `rewrite`，后端路径本身就带 `/admin`。

### 8.6 页面开发要点

**登录页**
- 表单只校验非空；`4101`、`4102` 直接展示 `msg`。
- 初始账号 `root / 123456`；登录成功按 `role` 进入工作台或审核页。

**商品审核**
- 顶部状态切换：待审核 0（默认）/ 已通过 1 / 已驳回 4；不要提供 2、3，否则 `4200`。
- 列表展示首图、标题、价格、分类、发布人、成色、时间、状态标签。
- 详情抽屉/页展示 `product.description`、图片、卖家信息、历史 `auditRecords`。
- 通过：二次确认；驳回：必填原因（≤500 字），提交后刷新列表和总数。
- 审核 `4203` 表示该商品已被别人处理，提示后刷新。

**评论审核**
- 状态 0/1/2 切换；展示评论人、被评论人、内容、图片。
- 评论没有商品信息，不要设计「商品标题」列。
- 通过/驳回交互同商品；驳回原因 ≤500。

**审核流水**
- 筛选：类型（1/2/3）、结论（1/2）、审核人 ID。
- 审核人姓名需前端用管理员列表映射（缓存到本地），否则只显示 ID。

**分类管理**
- 列表含停用项，按 `sort` 升序；支持新增、行内改名/改排序、启停、删除。
- 名称前端限 1-50 字并 trim；重复名 `4301` 直接提示。
- 禁用/删除遇到 `4302` 时提示「该分类下存在在售商品/商品」，引导先处理商品。
- 删除需二次确认（物理删除）。

**用户管理**
- 筛选：`keyword`（用户名/昵称）、`studentId`、`phone`、`status`；**必须带分页参数**。
- 详情：基本信息 + 交易统计卡片（买家/卖家两栏，金额 `toFixed(2)`，空值按 0）。
- 启停：switch + 二次确认；信用分：0-100 数字输入，越界前端先拦截，后端 `4403` 兜底。
- 没有「调整原因」输入，不要设计该字段。

**管理员管理**
- 仅 `role=1` 显示写操作；列表接口本身不拦角色。
- 新增：账号 2-20、密码 6-20、角色默认 2。
- 编辑：账号只读；密码留空不重置；`4104`（自己）、`4105`（最后超管）要原样提示。
- 删除：二次确认；不能删自己。

**个人设置**
- 修改密码：原密码、新密码、确认新密码（前端校验一致），长度 6-20 且新旧不同。
- 头像：当前没有上传接口，只能输入 URL 或暂不提供修改。

### 8.7 工作台（Dashboard）阻塞说明

旧需求的工作台需要：在售商品数、待审核数、成交数、成交总额、总浏览、总用户、今日订单、今日成交、按天流量、商品状态分布、分类销售分布。**这些统计接口一个都没有**。

可选处理：

1. **先不做工作台**，登录后落到商品审核页（成本最低，推荐）。
2. **做简化工作台**，只用现有接口拼三个数：
   - 待审核商品：`GET /admin/audit/product/page?status=0&pageNum=1&pageSize=1` 的 `total`
   - 待审核评论：`GET /admin/audit/comment/page?status=0&pageNum=1&pageSize=1` 的 `total`
   - 用户总数：`GET /admin/user/page?pageNum=1&pageSize=1` 的 `total`
   - 成交额/流量/趋势无数据源，不做或标注「待接入」。
3. **先补后端统计接口**再开发图表页（需要新增 Controller/Service/Mapper，属于另一项任务）。

图表库 ECharts 当前未安装；`package.json` 只有 `vue`、`vue-router`、`vite`、`@vitejs/plugin-vue`、`chalk`。按项目规范，新增依赖前需要先征询确认——在工作台接口落地前不建议引入。

### 8.8 前端验收清单

- [ ] 未登录访问 `/admin/*` 页面会跳登录页
- [ ] 管理端 token 与用户端 token 互不覆盖（`adminToken` vs `token`）
- [ ] HTTP 401 清登录态；业务 `code=401` 只提示不登出
- [ ] 所有分页请求都带 `pageNum`、`pageSize`
- [ ] 商品审核只出现 0/1/4 三种筛选
- [ ] 驳回原因必填且 ≤500 字
- [ ] 分类禁用/删除的 `4302` 有明确提示
- [ ] 信用分修改限制 0-100
- [ ] 普通管理员（角色 2）只看到工作台与审核菜单，直接输入 `/admin/users` 会被弹回工作台
- [ ] 运营管理员（角色 3）能看到分类管理与用户管理，但看不到管理员管理与操作日志
- [ ] 操作日志页能按动作筛选，且 `detail` 里的变更摘要可读
- [ ] 角色被降级后重新进入后台（或刷新），菜单按新权限变化
- [ ] 刷新 `/admin/...` 页面不会 404（代理 bypass 生效）

---

## 9. 建议的下一步

1. 管理端前端 `swapu_admin_front` 已完成：登录页、`AdminLayout`、路由守卫（按权限点）、API 层、六个业务页面、操作日志页。
2. RBAC 已落地：三角色 + 四权限点、接口拦截、逻辑删除、管理员操作日志；细节见 [`swapu_admin_rbac.md`](./swapu_admin_rbac.md)。
3. 后端待补接口优先级：平台总览/流量趋势 > 商品状态分布 > 分类销售分布；头像审核可暂缓。
4. 可选的工程化改进：给管理端补接口级集成测试（当前 40 个单测覆盖权限/CRUD/日志，但无前端自动化测试）。
