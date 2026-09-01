# SwapU 管理端接口文档

## 基础信息

- **Base URL**: `http://localhost:8081`（管理端独立服务；前端开发环境经 Vite proxy 转发 `/admin` 前缀）
- **鉴权方式**: 登录后所有接口（除登录外）请求头携带管理端 JWT
  ```
  token: {adminJwt}
  Content-Type: application/json
  ```
- **响应格式**: JSON 统一结构
  ```json
  { "code": 200, "msg": "success", "data": {} }
  ```
  - `code=200` 成功；`code=401` 未登录/无权限/token 过期或业务失败；具体业务错误见各接口错误码
- **分页结构**: `data = { "total": 总记录数, "records": [数据] }`
- **时间格式**: `yyyy-MM-dd HH:mm:ss`

---

## 通用状态约定

| 枚举 | 取值 | 说明 |
|------|------|------|
| 商品审核状态 `product.status` | 0待审核 / 1在售(审核通过) / 2已售出 / 3已下架 / 4审核驳回 | 用户端仅展示 `status=1` |
| 评论状态 `comment.status` | 0待审核 / 1已发布 / 2审核驳回 | 用户端仅展示 `status=1` |
| 审核类型 `audit_record.type` | 1商品 / 2评论 / 3头像 | |
| 审核结论 `audit_record.result` | 1通过 / 2驳回 | |
| 管理员角色 `admin.role` | 1超级管理员 / 2普通管理员 | |
| 用户状态 `user.status` | 1正常 / 0禁用 | |
| 分类状态 `category.status` | 1启用 / 0禁用 | |

> 审核通过/驳回时需同时写入 `audit_record` 审核流水表（含 `reviewer_id`、`reason`、`ai_result` 字段，供 AI 审核与人工兜底复用）。

---

## 目录

1. [管理员登录与账号管理](#1-管理员登录与账号管理)
2. [审核模块](#2-审核模块)
3. [分类管理](#3-分类管理)
4. [用户管理](#4-用户管理)
5. [平台统计](#5-平台统计)

---

## 1. 管理员登录与账号管理

### 1.1 管理员登录

#### 接口描述
管理员账号密码登录，签发管理端 JWT（`admin-secret-key`）

#### 请求信息
- **URL**: `/admin/login`
- **方法**: `POST`
- **需要登录**: 否

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| username | String | 是 | 管理员账号 | "root" |
| password | String | 是 | 密码 | "123456" |

#### 请求示例
```json
{ "username": "root", "password": "123456" }
```

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| data.token | String | 管理端 JWT |
| data.expireTime | Long | 过期时间（毫秒时间戳） |
| data.adminInfo.id | Long | 管理员ID |
| data.adminInfo.username | String | 账号 |
| data.adminInfo.name | String | 姓名 |
| data.adminInfo.avatar | String | 头像URL |
| data.adminInfo.role | Integer | 角色：1超级 2普通 |

#### 响应示例
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "expireTime": 7200000,
    "adminInfo": { "id": 1, "username": "root", "name": "系统管理员", "avatar": null, "role": 1 }
  }
}
```

#### 错误码说明
| 错误码 | 说明 |
|--------|------|
| 4101 | 账号或密码错误 |
| 4102 | 账号已被禁用 |

---

### 1.2 新增管理员

#### 接口描述
创建新管理员（仅超级管理员可操作）

#### 请求信息
- **URL**: `/admin`
- **方法**: `POST`
- **需要登录**: 是（超级管理员）

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| username | String | 是 | 账号（2-20字符） | "zhangsan" |
| password | String | 是 | 密码（6-20位） | "123456" |
| name | String | 否 | 姓名 | "张三" |
| avatar | String | 否 | 头像URL | "" |
| role | Integer | 否 | 角色，默认2 | 2 |

#### 响应示例
```json
{ "code": 200, "msg": "success", "data": { "adminId": 2 } }
```

#### 错误码说明
| 错误码 | 说明 |
|--------|------|
| 4103 | 用户名已存在 |

---

### 1.3 分页查询管理员

#### 接口描述
按条件分页查询管理员列表

#### 请求信息
- **URL**: `/admin/page`
- **方法**: `GET`
- **需要登录**: 是

#### 请求参数（Query）
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 是 | 页码，从1开始 |
| pageSize | Integer | 是 | 每页条数 |
| username | String | 否 | 账号模糊查询 |
| role | Integer | 否 | 角色过滤 |
| status | Integer | 否 | 状态过滤：1启用 0禁用 |

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| data.total | Long | 总数 |
| data.records[].id | Long | 管理员ID |
| data.records[].username | String | 账号 |
| data.records[].name | String | 姓名 |
| data.records[].avatar | String | 头像 |
| data.records[].role | Integer | 角色 |
| data.records[].status | Integer | 状态 |
| data.records[].createTime | String | 创建时间 |

#### 响应示例
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 2,
    "records": [
      { "id": 1, "username": "root", "name": "系统管理员", "avatar": null, "role": 1, "status": 1, "createTime": "2025-01-01 10:00:00" }
    ]
  }
}
```

---

### 1.4 修改管理员

#### 接口描述
修改管理员姓名/头像/角色，或重置密码

#### 请求信息
- **URL**: `/admin`
- **方法**: `PUT`
- **需要登录**: 是（超级管理员）

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 管理员ID |
| name | String | 否 | 姓名 |
| avatar | String | 否 | 头像URL |
| role | Integer | 否 | 角色 |
| password | String | 否 | 传入则重置密码 |

---

### 1.5 启用/禁用管理员

#### 请求信息
- **URL**: `/admin/{id}/status`
- **方法**: `PUT`
- **需要登录**: 是（超级管理员）

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 路径参数，管理员ID |
| status | Integer | 是 | 1启用 0禁用 |

#### 错误码说明
| 错误码 | 说明 |
|--------|------|
| 4104 | 不能禁用自己 |
| 4105 | 至少保留一名启用的超级管理员 |

---

### 1.6 删除管理员

#### 请求信息
- **URL**: `/admin/{id}`
- **方法**: `DELETE`
- **需要登录**: 是（超级管理员）

#### 错误码说明
| 错误码 | 说明 |
|--------|------|
| 4104 | 不能删除自己 |

---

### 1.7 管理员修改自己密码

#### 请求信息
- **URL**: `/admin/password`
- **方法**: `PUT`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| oldPassword | String | 是 | 原密码 |
| newPassword | String | 是 | 新密码（6-20位） |

---

## 2. 审核模块

> 说明：以下审核接口均围绕 `audit_record` 流水表 + 目标业务状态机。AI 审核（第5大项 TODO）接入后，`ai_result` 字段承载 AI 结论，人工审核作为兜底，接口形态不变。

### 2.1 商品审核-分页列表

#### 接口描述
分页查询待审核（或已审核）商品列表

#### 请求信息
- **URL**: `/admin/audit/product/page`
- **方法**: `GET`
- **需要登录**: 是

#### 请求参数（Query）
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 是 | 页码 |
| pageSize | Integer | 是 | 每页条数 |
| status | Integer | 否 | 审核状态：0待审核(默认) / 1已通过 / 4已驳回 |
| categoryId | Integer | 否 | 分类过滤 |
| title | String | 否 | 标题模糊查询 |

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| data.records[].id | Long | 商品ID |
| data.records[].title | String | 标题 |
| data.records[].userId | Long | 发布人ID |
| data.records[].username | String | 发布人账号 |
| data.records[].categoryId | Integer | 分类ID |
| data.records[].categoryName | String | 分类名称 |
| data.records[].price | BigDecimal | 售价 |
| data.records[].originalPrice | BigDecimal | 原价 |
| data.records[].images | List\<String\> | 图片列表 |
| data.records[].productCondition | Integer | 成色 |
| data.records[].status | Integer | 审核状态 |
| data.records[].createTime | String | 发布时间 |

#### 响应示例
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 1,
    "records": [
      {
        "id": 10, "title": "九成新 iPad", "userId": 3, "username": "李四",
        "categoryId": 2, "categoryName": "数码", "price": 1800.00, "originalPrice": 3299.00,
        "images": ["https://oss.xxx/ipad.jpg"], "productCondition": 9,
        "status": 0, "createTime": "2025-01-15 12:00:00"
      }
    ]
  }
}
```

---

### 2.2 商品审核-详情

#### 接口描述
查看商品完整信息 + 发布人信息 + 历史审核流水

#### 请求信息
- **URL**: `/admin/audit/product/{id}`
- **方法**: `GET`
- **需要登录**: 是

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| data.product | Object | 商品完整信息（含 description、quantity 等） |
| data.seller | Object | 发布人信息（id/username/nickname/college/creditScore） |
| data.auditRecords | List | 该商品历史审核流水（含 aiResult） |

#### 错误码说明
| 错误码 | 说明 |
|--------|------|
| 4201 | 商品不存在 |

---

### 2.3 商品审核-通过/驳回

#### 接口描述
审核商品：通过则 `product.status → 1在售`；驳回则 `→ 4审核驳回`，并写入审核流水

#### 请求信息
- **URL**: `/admin/audit/product/review`
- **方法**: `POST`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | Long | 是 | 商品ID |
| result | Integer | 是 | 1通过 2驳回 |
| reason | String | 否（驳回时必填） | 驳回原因，将展示给卖家 |

#### 请求示例
```json
{ "productId": 10, "result": 2, "reason": "商品描述与图片不符，请修改后重新提交" }
```

#### 响应示例
```json
{ "code": 200, "msg": "success", "data": null }
```

#### 错误码说明
| 错误码 | 说明 |
|--------|------|
| 4201 | 商品不存在 |
| 4202 | 驳回时必须填写原因 |
| 4203 | 商品当前状态不可审核 |

---

### 2.4 评论审核-分页列表

#### 接口描述
分页查询待审核评论

#### 请求信息
- **URL**: `/admin/audit/comment/page`
- **方法**: `GET`
- **需要登录**: 是

#### 请求参数（Query）
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 是 | 页码 |
| pageSize | Integer | 是 | 每页条数 |
| status | Integer | 否 | 0待审核(默认) / 1已发布 / 2已驳回 |
| productId | Long | 否 | 按商品过滤 |
| keyword | String | 否 | 评论内容关键字 |

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| data.records[].id | Long | 评论ID |
| data.records[].productId | Long | 商品ID |
| data.records[].productTitle | String | 商品标题 |
| data.records[].userId | Long | 评论人ID |
| data.records[].username | String | 评论人账号 |
| data.records[].content | String | 评论内容 |
| data.records[].images | List\<String\> | 评论图片 |
| data.records[].status | Integer | 审核状态 |
| data.records[].createTime | String | 评论时间 |

---

### 2.5 评论审核-通过/驳回

#### 请求信息
- **URL**: `/admin/audit/comment/review`
- **方法**: `POST`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| commentId | Long | 是 | 评论ID |
| result | Integer | 是 | 1通过 2驳回 |
| reason | String | 否（驳回时必填） | 驳回原因 |

---

### 2.6 头像审核-分页列表

#### 接口描述
分页查询头像待审核的用户（用户上传新头像后进入待审核）

#### 请求信息
- **URL**: `/admin/audit/avatar/page`
- **方法**: `GET`
- **需要登录**: 是

#### 请求参数（Query）
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 是 | 页码 |
| pageSize | Integer | 是 | 每页条数 |

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| data.records[].userId | Long | 用户ID |
| data.records[].username | String | 账号 |
| data.records[].nickname | String | 昵称 |
| data.records[].avatar | String | 待审核头像URL |
| data.records[].status | Integer | 审核状态 |
| data.records[].updateTime | String | 头像更新时间 |

---

### 2.7 头像审核-通过/驳回

#### 请求信息
- **URL**: `/admin/audit/avatar/review`
- **方法**: `POST`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |
| result | Integer | 是 | 1通过 2驳回 |
| reason | String | 否（驳回时必填） | 驳回原因 |

> 驳回后头像回退为默认头像，并提示用户重新上传。

---

### 2.8 审核流水查询

#### 接口描述
按条件查询审核流水（商品/评论/头像统一）

#### 请求信息
- **URL**: `/admin/audit/record/page`
- **方法**: `GET`
- **需要登录**: 是

#### 请求参数（Query）
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 是 | 页码 |
| pageSize | Integer | 是 | 每页条数 |
| type | Integer | 否 | 审核类型：1商品 2评论 3头像 |
| result | Integer | 否 | 审核结论：1通过 2驳回 |
| reviewerId | Long | 否 | 审核人ID |

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| data.records[].id | Long | 流水ID |
| data.records[].type | Integer | 审核类型 |
| data.records[].targetId | Long | 被审对象ID（商品/评论/用户） |
| data.records[].result | Integer | 1通过 2驳回 |
| data.records[].reason | String | 驳回原因 |
| data.records[].aiResult | String | AI 审核结论（JSON，AI 审核接入后） |
| data.records[].reviewerId | Long | 审核人ID |
| data.records[].reviewTime | String | 审核时间 |

---

## 3. 分类管理

### 3.1 分类列表

#### 接口描述
获取全部分类（含停用），按 sort 升序

#### 请求信息
- **URL**: `/admin/category/list`
- **方法**: `GET`
- **需要登录**: 是

#### 响应示例
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    { "id": 1, "name": "教材书籍", "sort": 1, "status": 1, "createTime": "2025-01-01 10:00:00" },
    { "id": 2, "name": "数码产品", "sort": 2, "status": 0, "createTime": "2025-01-01 10:00:00" }
  ]
}
```

---

### 3.2 新增分类

#### 请求信息
- **URL**: `/admin/category`
- **方法**: `POST`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 分类名称 |
| sort | Integer | 否 | 排序值，默认0 |

#### 错误码说明
| 错误码 | 说明 |
|--------|------|
| 4301 | 分类名称已存在 |

---

### 3.3 修改分类

#### 请求信息
- **URL**: `/admin/category`
- **方法**: `PUT`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 分类ID |
| name | String | 否 | 分类名称 |
| sort | Integer | 否 | 排序值 |

---

### 3.4 启用/禁用分类

#### 请求信息
- **URL**: `/admin/category/{id}/status`
- **方法**: `PUT`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 路径参数 |
| status | Integer | 是 | 1启用 0禁用 |

#### 错误码说明
| 错误码 | 说明 |
|--------|------|
| 4302 | 该分类下存在在售商品，请先下架商品再禁用分类 |

---

### 3.5 删除分类

#### 请求信息
- **URL**: `/admin/category/{id}`
- **方法**: `DELETE`
- **需要登录**: 是

#### 错误码说明
| 错误码 | 说明 |
|--------|------|
| 4302 | 该分类下存在商品，无法删除 |
| 4303 | 分类不存在 |

---

## 4. 用户管理

### 4.1 分页查询用户

#### 接口描述
按条件分页查询用户列表

#### 请求信息
- **URL**: `/admin/user/page`
- **方法**: `GET`
- **需要登录**: 是

#### 请求参数（Query）
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 是 | 页码 |
| pageSize | Integer | 是 | 每页条数 |
| username | String | 否 | 账号模糊查询 |
| studentId | String | 否 | 学号精确查询 |
| college | String | 否 | 学院 |
| status | Integer | 否 | 1正常 0禁用 |
| creditScoreMin | Integer | 否 | 信用分下限过滤 |

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| data.records[].id | Long | 用户ID |
| data.records[].studentId | String | 学号 |
| data.records[].username | String | 账号 |
| data.records[].nickname | String | 昵称 |
| data.records[].avatar | String | 头像 |
| data.records[].phone | String | 手机号 |
| data.records[].college | String | 学院 |
| data.records[].balance | BigDecimal | 账户余额 |
| data.records[].creditScore | Integer | 信用分 |
| data.records[].status | Integer | 1正常 0禁用 |
| data.records[].createTime | String | 注册时间 |

---

### 4.2 用户详情

#### 请求信息
- **URL**: `/admin/user/{id}`
- **方法**: `GET`
- **需要登录**: 是

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| data.userInfo | Object | 用户完整信息（含 email、updateTime） |
| data.tradeStatistics | Object | 交易统计（同 4.5） |

#### 错误码说明
| 错误码 | 说明 |
|--------|------|
| 4401 | 用户不存在 |

---

### 4.3 启用/禁用账号

#### 接口描述
禁用后该用户无法登录、无法进行交易

#### 请求信息
- **URL**: `/admin/user/{id}/status`
- **方法**: `PUT`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 路径参数 |
| status | Integer | 是 | 1启用 0禁用 |

---

### 4.4 修改信用分

#### 接口描述
调整用户信用分（如违规扣分、申诉恢复）

#### 请求信息
- **URL**: `/admin/user/{id}/credit`
- **方法**: `PUT`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 路径参数 |
| creditScore | Integer | 是 | 新信用分（0-100） |
| reason | String | 否 | 调整原因（建议写入操作日志） |

#### 错误码说明
| 错误码 | 说明 |
|--------|------|
| 4402 | 信用分超出范围（0-100） |

---

### 4.5 用户交易统计

#### 接口描述
单个用户的交易数据统计

#### 请求信息
- **URL**: `/admin/user/{id}/trade-statistics`
- **方法**: `GET`
- **需要登录**: 是

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| data.publishCount | Integer | 发布商品总数 |
| data.sellCount | Integer | 卖出件数（订单 status=5 且为卖家） |
| data.buyCount | Integer | 买入件数（订单 status=5 且为买家） |
| data.sellAmount | BigDecimal | 卖出成交总额 |
| data.buyAmount | BigDecimal | 买入消费总额 |
| data.favoriteCount | Integer | 被收藏次数 |
| data.creditScore | Integer | 当前信用分 |

#### 响应示例
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "publishCount": 5, "sellCount": 3, "buyCount": 2,
    "sellAmount": 1500.00, "buyAmount": 800.00,
    "favoriteCount": 12, "creditScore": 100
  }
}
```

---

## 5. 平台统计

### 5.1 平台总览

#### 接口描述
核心经营指标总览（工作台首页卡片）

#### 请求信息
- **URL**: `/admin/statistics/overview`
- **方法**: `GET`
- **需要登录**: 是

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| data.onSaleCount | Integer | 在售商品数（status=1） |
| data.pendingAuditCount | Integer | 待审核商品数（status=0） |
| data.soldCount | Integer | 交易完成数（订单 status=5） |
| data.totalAmount | BigDecimal | 累计成交总额 |
| data.totalViewCount | Integer | 平台商品总浏览量 |
| data.totalUserCount | Integer | 注册用户总数 |
| data.todayOrderCount | Integer | 今日新增订单数 |
| data.todayAmount | BigDecimal | 今日成交额 |

#### 响应示例
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "onSaleCount": 120, "pendingAuditCount": 8, "soldCount": 356,
    "totalAmount": 45600.00, "totalViewCount": 52000,
    "totalUserCount": 890, "todayOrderCount": 23, "todayAmount": 1850.00
  }
}
```

---

### 5.2 流量趋势（按天）

#### 接口描述
按天聚合的浏览/订单/成交趋势（折线图数据源）

#### 请求信息
- **URL**: `/admin/statistics/traffic-trend`
- **方法**: `GET`
- **需要登录**: 是

#### 请求参数（Query）
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startDate | String | 是 | 开始日期 `yyyy-MM-dd` |
| endDate | String | 是 | 结束日期 `yyyy-MM-dd`（跨度建议≤90天） |

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| data[].date | String | 日期 `yyyy-MM-dd` |
| data[].viewCount | Integer | 当日新增浏览量 |
| data[].orderCount | Integer | 当日新增订单数 |
| data[].amount | BigDecimal | 当日成交额（按完成订单） |

#### 响应示例
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    { "date": "2025-01-01", "viewCount": 320, "orderCount": 12, "amount": 980.00 },
    { "date": "2025-01-02", "viewCount": 410, "orderCount": 18, "amount": 1520.00 }
  ]
}
```

---

### 5.3 商品状态分布

#### 接口描述
商品按状态聚合统计（饼图数据源）

#### 请求信息
- **URL**: `/admin/statistics/product-status`
- **方法**: `GET`
- **需要登录**: 是

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| data[].status | Integer | 商品状态 |
| data[].statusDesc | String | 状态描述（在售/已售出/交易流程中/已下架/待审核/已驳回） |
| data[].count | Integer | 数量 |

> 说明："交易流程中" = 商品存在未完成订单（订单 status 1-4）。

#### 响应示例
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    { "status": 1, "statusDesc": "在售", "count": 120 },
    { "status": 2, "statusDesc": "已售出", "count": 356 },
    { "status": 3, "statusDesc": "已下架", "count": 45 },
    { "status": 0, "statusDesc": "待审核", "count": 8 }
  ]
}
```

---

### 5.4 分类销售分布（可选）

#### 接口描述
各分类成交额/成交量占比（饼图数据源，可选实现）

#### 请求信息
- **URL**: `/admin/statistics/category-sales`
- **方法**: `GET`
- **需要登录**: 是

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| data[].categoryId | Integer | 分类ID |
| data[].categoryName | String | 分类名称 |
| data[].salesCount | Integer | 成交件数 |
| data[].salesAmount | BigDecimal | 成交金额 |

---

## 通用错误码

| 错误码 | 说明 |
|--------|------|
| 401 | 未登录 / token 过期 / 无操作权限 |
| 4101-4105 | 管理员模块错误（见 1.1/1.2/1.5/1.6） |
| 4201-4203 | 审核模块错误（见 2.2/2.3） |
| 4301-4303 | 分类模块错误（见 3.2/3.4/3.5） |
| 4401-4402 | 用户模块错误（见 4.2/4.4） |
