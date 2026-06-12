## 接口文档
### 基础信息
- **Base URL**: `http://localhost:8080/api/v1`
- **请求头**: 
  ```
  Authorization: Bearer {token}
  Content-Type: application/json
  ```
- **响应格式**: JSON

---

## 目录
1. [用户模块](#用户模块)
2. [商品模块](#商品模块)
3. [订单模块](#订单模块)
4. [收藏模块](#收藏模块)
5. [聊天模块](#聊天模块)
6. [分类模块](#分类模块)
7. [校园认证模块](#校园认证模块)

---

## 用户模块

### 1.1 用户注册

#### 接口描述
新用户注册账号

#### 请求信息
- **URL**: `/user/register`
- **方法**: `POST`
- **需要登录**: 否

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| studentId | String | 是 | 学号 | "20210001" |
| username | String | 是 | 用户名（2-20个字符） | "张三" |
| password | String | 是 | 密码（6-20位） | "123456" |
| phone | String | 是 | 手机号 | "13800138000" |
| email | String | 否 | 邮箱 | "zhangsan@university.com" |
| college | String | 是 | 学院 | "计算机学院" |

#### 请求示例
```json
{
  "studentId": "20210001",
  "username": "张三",
  "password": "123456",
  "phone": "13800138000",
  "email": "zhangsan@university.com",
  "college": "计算机学院",
}
```

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码：200成功 |
| message | String | 响应信息 |
| data | Object | 返回数据 |
| data.userId | Long | 用户ID |
| data.studentId | String | 学号 |
| data.username | String | 用户名 |

#### 响应示例
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1,
    "studentId": "20210001",
    "username": "张三"
  }
}
```

#### 错误码说明
| 错误码 | 说明 |
|--------|------|
| 1001 | 学号已注册 |
| 1002 | 手机号已注册 |
| 1003 | 验证码错误或已过期 |

---

### 1.2 用户登录

#### 接口描述
用户登录获取token

#### 请求信息
- **URL**: `/user/login`
- **方法**: `POST`
- **需要登录**: 否

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| studentId | String | 是 | 学号 | "20210001" |
| password | String | 是 | 密码 | "123456" |

#### 请求示例
```json
{
  "studentId": "20210001",
  "password": "123456"
}
```

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码 |
| message | String | 响应信息 |
| data | Object | 登录数据 |
| data.token | String | JWT token |
| data.expireTime | Long | token过期时间（毫秒时间戳） |
| data.userInfo | Object | 用户信息 |
| data.userInfo.id | Long | 用户ID |
| data.userInfo.username | String | 用户名 |
| data.userInfo.studentId | String | 学号 |
| data.userInfo.avatar | String | 头像URL |
| data.userInfo.creditScore | Integer | 信用分 |

#### 响应示例
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expireTime": 604800000,
    "userInfo": {
      "id": 1,
      "username": "张三",
      "studentId": "20210001",
      "avatar": null,
      "creditScore": 100
    }
  }
}
```

#### 错误码说明
| 错误码 | 说明 |
|--------|------|
| 1004 | 学号不存在 |
| 1005 | 密码错误 |
| 1006 | 账号已被禁用 |

---

### 1.3 获取用户信息

#### 接口描述
获取当前登录用户的详细信息

#### 请求信息
- **URL**: `/user/info`
- **方法**: `GET`
- **需要登录**: 是

#### 请求参数
无

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码 |
| message | String | 响应信息 |
| data | Object | 用户信息 |
| data.id | Long | 用户ID |
| data.username | String | 用户名 |
| data.studentId | String | 学号 |
| data.phone | String | 手机号（脱敏） |
| data.email | String | 邮箱（脱敏） |
| data.avatar | String | 头像URL |
| data.college | String | 学院 |
| data.balance | BigDecimal | 账户余额 |
| data.creditScore | Integer | 信用分 |
| data.publishCount | Integer | 发布商品数量 |
| data.soldCount | Integer | 已售出数量 |
| data.favoriteCount | Integer | 收藏数量 |
| data.createTime | DateTime | 注册时间 |

#### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "张三",
    "studentId": "20210001",
    "phone": "138****8000",
    "email": "zhan***@university.com",
    "avatar": "https://cdn.example.com/avatar/1.jpg",
    "college": "计算机学院",
    "balance": 1250.00,
    "creditScore": 98,
    "publishCount": 15,
    "soldCount": 8,
    "favoriteCount": 23,
    "createTime": "2023-09-01 10:00:00"
  }
}
```

---

### 1.4 更新用户信息

#### 接口描述
修改用户个人信息

#### 请求信息
- **URL**: `/user/info`
- **方法**: `PUT`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| username | String | 否 | 用户名 | "张三丰" |
| phone | String | 否 | 手机号 | "13900139000" |
| email | String | 否 | 邮箱 | "newemail@university.com" |
| avatar | String | 否 | 头像URL | "https://xxx.com/avatar.jpg" |
| college | String | 否 | 学院 | "软件学院" |

#### 请求示例
```json
{
  "username": "张三丰",
  "phone": "13900139000",
  "email": "zhangsf@university.com",
  "college": "软件学院"
}
```

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码 |
| message | String | 响应信息 |
| data | Boolean | 是否成功 |

#### 响应示例
```json
{
  "code": 200,
  "message": "更新成功",
  "data": true
}
```

---

### 1.5 修改密码

#### 接口描述
修改用户登录密码

#### 请求信息
- **URL**: `/user/password`
- **方法**: `PUT`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| oldPassword | String | 是 | 原密码 | "123456" |
| newPassword | String | 是 | 新密码（6-20位） | "654321" |
| confirmPassword | String | 是 | 确认密码 | "654321" |

#### 请求示例
```json
{
  "oldPassword": "123456",
  "newPassword": "654321",
  "confirmPassword": "654321"
}
```

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码 |
| message | String | 响应信息 |
| data | Boolean | 是否成功 |

#### 响应示例
```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": true
}
```

---

### 1.6 设置支付密码

#### 接口描述
设置或修改支付密码

#### 请求信息
- **URL**: `/user/pay-password`
- **方法**: `PUT`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| payPassword | String | 是 | 支付密码（6位数字） | "123456" |
| confirmPayPassword | String | 是 | 确认支付密码 | "123456" |
| smsCode | String | 是 | 短信验证码 | "123456" |

#### 请求示例
```json
{
  "payPassword": "123456",
  "confirmPayPassword": "123456",
  "smsCode": "123456"
}
```

#### 响应示例
```json
{
  "code": 200,
  "message": "支付密码设置成功",
  "data": true
}
```

---

### 1.7 用户注销（退出登录）

#### 接口描述
退出登录，清除token

#### 请求信息
- **URL**: `/user/logout`
- **方法**: `POST`
- **需要登录**: 是

#### 请求参数
无

#### 响应示例
```json
{
  "code": 200,
  "message": "退出成功",
  "data": null
}
```

---

## 商品模块

### 2.1 发布商品

#### 接口描述
卖家发布二手商品

#### 请求信息
- **URL**: `/product`
- **方法**: `POST`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| title | String | 是 | 商品标题（2-50字符） | "二手自行车" |
| description | String | 否 | 商品描述 | "九成新，只骑了两个月" |
| categoryId | Integer | 是 | 分类ID | 1 |
| price | BigDecimal | 是 | 售价（0.01-99999.99） | 200.00 |
| originalPrice | BigDecimal | 否 | 原价 | 500.00 |
| condition | Integer | 是 | 成色：1全新 2几乎全新 3有使用痕迹 | 2 |
| images | Array | 是 | 商品图片URL数组（最多9张） | ["url1","url2"] |

#### 请求示例
```json
{
  "title": "二手自行车",
  "description": "九成新，只骑了两个月，车况良好，赠送车锁",
  "categoryId": 1,
  "price": 200.00,
  "originalPrice": 500.00,
  "condition": 2,
  "images": [
    "https://cdn.example.com/images/bike1.jpg",
    "https://cdn.example.com/images/bike2.jpg"
  ]
}
```

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码 |
| message | String | 响应信息 |
| data | Object | 商品数据 |
| data.productId | Long | 商品ID |
| data.status | Integer | 状态：1审核中 2已上架 |

#### 响应示例
```json
{
  "code": 200,
  "message": "发布成功",
  "data": {
    "productId": 1001,
    "status": 2
  }
}
```

---

### 2.2 商品列表查询

#### 接口描述
分页查询商品列表，支持多条件筛选

#### 请求信息
- **URL**: `/product/list`
- **方法**: `GET`
- **需要登录**: 否

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| page | Integer | 否 | 页码，默认1 | 1 |
| pageSize | Integer | 否 | 每页数量，默认20，最大100 | 20 |
| categoryId | Integer | 否 | 分类ID | 1 |
| keyword | String | 否 | 搜索关键词 | "自行车" |
| minPrice | BigDecimal | 否 | 最低价格 | 50 |
| maxPrice | BigDecimal | 否 | 最高价格 | 300 |
| condition | Integer | 否 | 成色：1全新 2几乎全新 3有使用痕迹 | 2 |
| status | Integer | 否 | 商品状态：1在售 2已售 | 1 |
| sort | String | 否 | 排序方式：time(最新) price_asc(价格升) price_desc(价格降) hot(最热) | "time" |
| userId | Long | 否 | 指定用户ID（查询某用户的商品） | 1 |

#### 请求示例
```
GET /api/v1/product/list?page=1&size=20&categoryId=1&keyword=自行车&minPrice=50&maxPrice=300&sort=hot
```

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码 |
| message | String | 响应信息 |
| data | Object | 分页数据 |
| data.total | Long | 总记录数 |
| data.pages | Integer | 总页数 |
| data.current | Integer | 当前页码 |
| data.size | Integer | 每页数量 |
| data.list | Array | 商品列表 |
| data.list[].id | Long | 商品ID |
| data.list[].title | String | 商品标题 |
| data.list[].price | BigDecimal | 售价 |
| data.list[].originalPrice | BigDecimal | 原价 |
| data.list[].image | String | 商品封面图 |
| data.list[].condition | Integer | 成色 |
| data.list[].conditionDesc | String | 成色描述 |
| data.list[].viewCount | Integer | 浏览次数 |
| data.list[].favoriteCount | Integer | 收藏数量 |
| data.list[].status | Integer | 状态 |
| data.list[].createTime | DateTime | 发布时间 |
| data.list[].seller | Object | 卖家信息 |
| data.list[].seller.id | Long | 卖家ID |
| data.list[].seller.username | String | 卖家昵称 |
| data.list[].seller.avatar | String | 卖家头像 |
| data.list[].seller.creditScore | Integer | 卖家信用分 |

#### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 150,
    "pages": 8,
    "current": 1,
    "size": 20,
    "list": [
      {
        "id": 1001,
        "title": "二手自行车",
        "price": 200.00,
        "originalPrice": 500.00,
        "image": "https://cdn.example.com/images/bike1.jpg",
        "condition": 2,
        "conditionDesc": "几乎全新",
        "viewCount": 128,
        "favoriteCount": 15,
        "status": 1,
        "createTime": "2024-01-15 10:30:00",
        "seller": {
          "id": 1,
          "username": "张三",
          "avatar": "https://cdn.example.com/avatar/1.jpg",
          "creditScore": 98
        }
      }
    ]
  }
}
```

---

### 2.3 商品详情

#### 接口描述
获取商品详细信息

#### 请求信息
- **URL**: `/product/detail/{id}`
- **方法**: `GET`
- **需要登录**: 否

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 商品ID（路径参数） | 1001 |

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码 |
| message | String | 响应信息 |
| data | Object | 商品详情 |
| data.id | Long | 商品ID |
| data.title | String | 商品标题 |
| data.description | String | 商品描述 |
| data.categoryId | Integer | 分类ID |
| data.categoryName | String | 分类名称 |
| data.price | BigDecimal | 售价 |
| data.originalPrice | BigDecimal | 原价 |
| data.condition | Integer | 成色 |
| data.conditionDesc | String | 成色描述 |
| data.images | Array | 商品图片列表 |
| data.viewCount | Integer | 浏览次数 |
| data.favoriteCount | Integer | 收藏数量 |
| data.status | Integer | 状态：1在售 2已售 3下架 |
| data.statusDesc | String | 状态描述 |
| data.createTime | DateTime | 发布时间 |
| data.updateTime | DateTime | 更新时间 |
| data.seller | Object | 卖家信息 |
| data.seller.id | Long | 卖家ID |
| data.seller.username | String | 卖家昵称 |
| data.seller.avatar | String | 卖家头像 |
| data.seller.creditScore | Integer | 信用分 |
| data.seller.publishCount | Integer | 发布数量 |
| data.seller.soldCount | Integer | 售出数量 |
| data.isFavorite | Boolean | 当前用户是否已收藏 |
| data.contactWay | String | 联系方式 |

#### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1001,
    "title": "二手自行车",
    "description": "九成新，只骑了两个月，车况良好，赠送车锁",
    "categoryId": 1,
    "categoryName": "交通工具",
    "price": 200.00,
    "originalPrice": 500.00,
    "condition": 2,
    "conditionDesc": "几乎全新",
    "images": [
      "https://cdn.example.com/images/bike1.jpg",
      "https://cdn.example.com/images/bike2.jpg"
    ],
    "viewCount": 129,
    "favoriteCount": 16,
    "status": 1,
    "statusDesc": "在售",
    "createTime": "2024-01-15 10:30:00",
    "updateTime": "2024-01-15 10:30:00",
    "seller": {
      "id": 1,
      "username": "张三",
      "avatar": "https://cdn.example.com/avatar/1.jpg",
      "creditScore": 98,
      "publishCount": 15,
      "soldCount": 8
    },
    "isFavorite": false,
    "contactWay": "微信：zhangsan123"
  }
}
```

---

### 2.4 更新商品

#### 接口描述
修改商品信息（仅卖家可操作）

#### 请求信息
- **URL**: `/product/{id}`
- **方法**: `PUT`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 商品ID（路径参数） | 1001 |
| title | String | 否 | 商品标题 | "二手自行车-降价" |
| description | String | 否 | 商品描述 | "几乎全新，降价出售" |
| categoryId | Integer | 否 | 分类ID | 1 |
| price | BigDecimal | 否 | 售价 | 180.00 |
| originalPrice | BigDecimal | 否 | 原价 | 500.00 |
| condition | Integer | 否 | 成色 | 2 |
| images | Array | 否 | 商品图片 | ["url1","url2"] |

#### 请求示例
```json
{
  "title": "二手自行车-降价",
  "price": 180.00,
  "description": "几乎全新，降价出售"
}
```

#### 响应示例
```json
{
  "code": 200,
  "message": "更新成功",
  "data": true
}
```

---

### 2.5 下架商品

#### 接口描述
卖家下架自己的商品

#### 请求信息
- **URL**: `/product/{id}/off`
- **方法**: `PUT`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 商品ID（路径参数） | 1001 |

#### 响应示例
```json
{
  "code": 200,
  "message": "下架成功",
  "data": true
}
```

---

### 2.6 删除商品

#### 接口描述
删除自己的商品（软删除）

#### 请求信息
- **URL**: `/product/{id}`
- **方法**: `DELETE`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 商品ID（路径参数） | 1001 |

#### 响应示例
```json
{
  "code": 200,
  "message": "删除成功",
  "data": true
}
```

---

### 2.7 获取我发布的商品

#### 接口描述
获取当前用户发布的商品列表

#### 请求信息
- **URL**: `/product/my-products`
- **方法**: `GET`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| page | Integer | 否 | 页码，默认1 | 1 |
| pageSize | Integer | 否 | 每页数量，默认20 | 20 |
| status | Integer | 否 | 商品状态：1在售 2已售 3下架 | 1 |

#### 响应参数
同商品列表接口

#### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 15,
    "pages": 1,
    "current": 1,
    "size": 20,
    "list": [
      {
        "id": 1001,
        "title": "二手自行车",
        "price": 200.00,
        "status": 1,
        "viewCount": 128,
        "createTime": "2024-01-15 10:30:00"
      }
    ]
  }
}
```

---

## 订单模块

### 3.1 创建订单

#### 接口描述
买家下单购买商品

#### 请求信息
- **URL**: `/order/create`
- **方法**: `POST`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| productId | Long | 是 | 商品ID | 1001 |
| quantity | Integer | 是 | 购买数量（必须为1） | 1 |
| buyerMessage | String | 否 | 买家留言（最多100字） | "麻烦尽快发货" |

#### 请求示例
```json
{
  "productId": 1001,
  "quantity": 1,
  "buyerMessage": "麻烦尽快发货，谢谢！"
}
```

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码 |
| message | String | 响应信息 |
| data | Object | 订单数据 |
| data.orderId | Long | 订单ID |
| data.orderNo | String | 订单编号（格式：年月日+8位随机数） |
| data.productId | Long | 商品ID |
| data.productTitle | String | 商品标题 |
| data.productImage | String | 商品图片 |
| data.quantity | Integer | 购买数量 |
| data.unitPrice | BigDecimal | 单价 |
| data.amount | BigDecimal | 商品金额（单价×数量） |
| data.freight | BigDecimal | 运费（0元） |
| data.totalAmount | BigDecimal | 总金额 |
| data.status | Integer | 订单状态：1待付款 |
| data.statusDesc | String | 状态描述 |
| data.expireTime | DateTime | 订单过期时间（30分钟后） |
| data.buyerMessage | String | 买家留言 |
| data.createTime | DateTime | 创建时间 |

#### 响应示例
```json
{
  "code": 200,
  "message": "创建订单成功",
  "data": {
    "orderId": 5001,
    "orderNo": "20240115103000123456",
    "productId": 1001,
    "productTitle": "二手自行车",
    "productImage": "https://cdn.example.com/images/bike1.jpg",
    "quantity": 1,
    "unitPrice": 200.00,
    "amount": 200.00,
    "freight": 0.00,
    "totalAmount": 200.00,
    "status": 1,
    "statusDesc": "待付款",
    "expireTime": "2024-01-15 11:00:00",
    "buyerMessage": "麻烦尽快发货，谢谢！",
    "createTime": "2024-01-15 10:30:00"
  }
}
```

#### 错误码说明
| 错误码 | 说明 |
|--------|------|
| 4001 | 商品不存在 |
| 4002 | 商品已下架或已售出 |
| 4003 | 不能购买自己的商品 |
| 4004 | 存在未支付的订单，请先支付或取消 |
| 4005 | 订单创建失败，请重试 |

---

### 3.2 支付订单

#### 接口描述
买家支付订单

#### 请求信息
- **URL**: `/order/pay`
- **方法**: `POST`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| orderNo | String | 是 | 订单编号 | "20240115103000123456" |
| payType | Integer | 是 | 支付方式：1余额支付 2微信支付 3支付宝 | 1 |
| payPassword | String | 条件必填 | 余额支付时必填（6位数字） | "123456" |

#### 请求示例（余额支付）
```json
{
  "orderNo": "20240115103000123456",
  "payType": 1,
  "payPassword": "123456"
}
```

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码 |
| message | String | 响应信息 |
| data | Object | 支付结果 |
| data.orderNo | String | 订单编号 |
| data.status | Integer | 订单状态：2待发货 |
| data.statusDesc | String | 状态描述 |
| data.payTime | DateTime | 支付时间 |
| data.payAmount | BigDecimal | 支付金额 |

#### 响应示例
```json
{
  "code": 200,
  "message": "支付成功",
  "data": {
    "orderNo": "20240115103000123456",
    "status": 2,
    "statusDesc": "待发货",
    "payTime": "2024-01-15 10:32:00",
    "payAmount": 200.00
  }
}
```

#### 错误码说明
| 错误码 | 说明 |
|--------|------|
| 4101 | 订单不存在 |
| 4102 | 订单状态不是待付款 |
| 4103 | 订单已过期 |
| 4104 | 余额不足 |
| 4105 | 支付密码错误 |
| 4106 | 支付失败，请重试 |

---

### 3.3 取消订单

#### 接口描述
买家取消未支付的订单

#### 请求信息
- **URL**: `/order/cancel`
- **方法**: `POST`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| orderNo | String | 是 | 订单编号 | "20240115103000123456" |
| cancelReason | String | 否 | 取消原因 | "不想要了" |

#### 请求示例
```json
{
  "orderNo": "20240115103000123456",
  "cancelReason": "不想要了"
}
```

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码 |
| message | String | 响应信息 |
| data | Object | 取消结果 |
| data.orderNo | String | 订单编号 |
| data.status | Integer | 订单状态：5已取消 |
| data.statusDesc | String | 状态描述 |
| data.cancelTime | DateTime | 取消时间 |      
#### 响应示例
```json
{
  "code": 200,
  "message": "取消成功",
  "data": {
    "orderNo": "20240115103000123456",
    "status": 5,
    "statusDesc": "已取消",
    "cancelTime": "2024-01-15 10:35:00"
  }
}
```

#### 错误码说明
| 错误码 | 说明 |
|--------|------|
| 4101 | 订单不存在 |
| 4102 | 订单状态不是待付款 |
| 4103 | 订单已过期无法取消 |

---

### 3.4 卖家发货

#### 接口描述
卖家填写物流信息并发货

#### 请求信息
- **URL**: `/order/deliver`
- **方法**: `POST`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| orderNo | String | 是 | 订单编号 | "20240115103000123456" |
| logisticsCompany | String | 是 | 物流公司 | "顺丰速运" |
| logisticsNo | String | 是 | 物流单号 | "SF1234567890" |

#### 请求示例
```json
{
  "orderNo": "20240115103000123456",
  "logisticsCompany": "顺丰速运",
  "logisticsNo": "SF1234567890"
}
```

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码 |
| message | String | 响应信息 |
| data | Object | 发货结果 |
| data.orderNo | String | 订单编号 |
| data.status | Integer | 订单状态：3待收货 |
| data.statusDesc | String | 状态描述 |
| data.deliveryTime | DateTime | 发货时间 |
| data.logisticsCompany | String | 物流公司 |
| data.logisticsNo | String | 物流单号 |

#### 响应示例
```json
{
  "code": 200,
  "message": "发货成功",
  "data": {
    "orderNo": "20240115103000123456",
    "status": 3,
    "statusDesc": "待收货",
    "deliveryTime": "2024-01-16 09:00:00",
    "logisticsCompany": "顺丰速运",
    "logisticsNo": "SF1234567890"
  }
}
```

#### 错误码说明
| 错误码 | 说明 |
|--------|------|
| 4201 | 订单不存在 |
| 4202 | 订单状态不是待发货 |
| 4203 | 不是卖家身份 |

### 还有个确认接单（/order/confirm）

---

### 3.5 确认收货

#### 接口描述
买家确认收货

#### 请求信息
- **URL**: `/order/receive`
- **方法**: `POST`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| orderNo | String | 是 | 订单编号 | "20240115103000123456" |

#### 请求示例
```json
{
  "orderNo": "20240115103000123456"
}
```

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码 |
| message | String | 响应信息 |
| data | Object | 确认结果 |
| data.orderNo | String | 订单编号 |
| data.status | Integer | 订单状态：4已完成 |
| data.statusDesc | String | 状态描述 |
| data.receiveTime | DateTime | 确认时间 |

#### 响应示例
```json
{
  "code": 200,
  "message": "确认收货成功",
  "data": {
    "orderNo": "20240115103000123456",
    "status": 4,
    "statusDesc": "已完成",
    "receiveTime": "2024-01-18 14:30:00"
  }
}
```

#### 错误码说明
| 错误码 | 说明 |
|--------|------|
| 4301 | 订单不存在 |
| 4302 | 订单状态不是待收货 |
| 4303 | 不是买家身份 |

---

### 3.6 订单详情

#### 接口描述
查看订单详细信息

#### 请求信息
- **URL**: `/order/detail/{orderNo}`
- **方法**: `GET`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| orderNo | String | 是 | 订单编号（路径参数） | "20240115103000123456" |

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码 |
| message | String | 响应信息 |
| data | Object | 订单详情 |
| data.orderId | Long | 订单ID |
| data.orderNo | String | 订单编号 |
| data.productId | Long | 商品ID |
| data.productTitle | String | 商品标题 |
| data.productImage | String | 商品图片 |
| data.productDescription | String | 商品描述 |
| data.quantity | Integer | 数量 |
| data.unitPrice | BigDecimal | 单价 |
| data.amount | BigDecimal | 商品金额 |
| data.freight | BigDecimal | 运费 |
| data.totalAmount | BigDecimal | 总金额 |
| data.status | Integer | 订单状态 |
| data.statusDesc | String | 状态描述 |
| data.buyerId | Long | 买家ID |
| data.buyerName | String | 买家昵称 |
| data.buyerPhone | String | 买家电话 |
| data.sellerId | Long | 卖家ID |
| data.sellerName | String | 卖家昵称 |
| data.sellerPhone | String | 卖家电话 |
| data.buyerMessage | String | 买家留言 |
| data.sellerMessage | String | 卖家留言 |
| data.logisticsCompany | String | 物流公司 |
| data.logisticsNo | String | 物流单号 |
| data.createTime | DateTime | 创建时间 |
| data.payTime | DateTime | 支付时间 |
| data.deliveryTime | DateTime | 发货时间 |
| data.confirmTime | DateTime | 确认时间 |
| data.cancelTime | DateTime | 取消时间 |
| data.expireTime | DateTime | 过期时间 |

#### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "orderId": 5001,
    "orderNo": "20240115103000123456",
    "productId": 1001,
    "productTitle": "二手自行车",
    "productImage": "https://cdn.example.com/images/bike1.jpg",
    "productDescription": "九成新，只骑了两个月",
    "quantity": 1,
    "unitPrice": 200.00,
    "amount": 200.00,
    "freight": 0.00,
    "totalAmount": 200.00,
    "status": 2,
    "statusDesc": "待发货",
    "buyerId": 2,
    "buyerName": "李四",
    "buyerPhone": "138****0001",
    "sellerId": 1,
    "sellerName": "张三",
    "sellerPhone": "138****0002",
    "buyerMessage": "麻烦尽快发货",
    "sellerMessage": null,
    "logisticsCompany": null,
    "logisticsNo": null,
    "createTime": "2024-01-15 10:30:00",
    "payTime": "2024-01-15 10:32:00",
    "deliveryTime": null,
    "confirmTime": null,
    "cancelTime": null,
    "expireTime": "2024-01-15 11:00:00"
  }
}
```

---

### 3.7 我的订单列表

#### 接口描述
分页查询当前用户的订单列表

#### 请求信息
- **URL**: `/order/list`
- **方法**: `GET`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| page | Integer | 否 | 页码，默认1 | 1 |
| pageSize | Integer | 否 | 每页数量，默认20 | 20 |
| status | Integer | 否 | 订单状态：1待付款 2待发货 3待收货 4已完成 5已取消 | 2 |
| role | String | 否 | 角色：buyer(我是买家) seller(我是卖家)，默认buyer | "buyer" |
| startTime | DateTime | 否 | 开始时间 | "2024-01-01 00:00:00" |
| endTime | DateTime | 否 | 结束时间 | "2024-01-31 23:59:59" |

#### 请求示例
```
GET /api/v1/order/list?page=1&size=20&status=2&role=seller
```

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码 |
| message | String | 响应信息 |
| data | Object | 分页数据 |
| data.total | Long | 总记录数 |
| data.pages | Integer | 总页数 |
| data.current | Integer | 当前页码 |
| data.size | Integer | 每页数量 |
| data.list | Array | 订单列表 |
| data.list[].orderId | Long | 订单ID |
| data.list[].orderNo | String | 订单编号 |
| data.list[].productId | Long | 商品ID |
| data.list[].productTitle | String | 商品标题 |
| data.list[].productImage | String | 商品图片 |
| data.list[].quantity | Integer | 数量 |
| data.list[].totalAmount | BigDecimal | 总金额 |
| data.list[].status | Integer | 订单状态 |
| data.list[].tradeUserName | String | 交易对方昵称 |
| data.list[].tradeUserAvatar | String | 交易对方头像 |
| data.list[].createTime | DateTime | 创建时间 |
| data.list[].expireTime | DateTime | 过期时间（待付款订单显示） |

#### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 25,
    "pages": 2,
    "current": 1,
    "size": 20,
    "list": [
      {
        "orderId": 5001,
        "orderNo": "20240115103000123456",
        "productId": 1001,
        "productTitle": "二手自行车",
        "productImage": "https://cdn.example.com/images/bike1.jpg",
        "quantity": 1,
        "totalAmount": 200.00,
        "status": 2,
        "statusDesc": "待发货",
        "tradeUserName": "李四",
        "tradeUserAvatar": "https://cdn.example.com/avatar/lisi.jpg",
        "createTime": "2024-01-15 10:30:00",
        "expireTime": null
      }
    ]
  }
}
```

---

### 3.8 订单统计数据

#### 接口描述
获取用户的订单统计信息

#### 请求信息
- **URL**: `/order/statistics`
- **方法**: `GET`
- **需要登录**: 是

#### 请求参数
无

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码 |
| message | String | 响应信息 |
| data | Object | 统计数据 |
| data.waitPay | Integer | 待付款数量 |
| data.waitReceive | Integer | 待收货数量 |
| data.buyCompleted | Integer | 已完成数量 |
| data.totalCostAmount | BigDecimal | 累计消费金额 |
| data.waitDeliver | Integer | 待发货数量 |
| data.sellCompleted | Integer | 已完成数量 |
| data.totalSellAmount | BigDecimal | 累计销售额 |

#### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "asBuyer": {
      "waitPay": 2,
      "waitReceive": 1,
      "completed": 15,
      "totalAmount": 3250.00
    },
    "asSeller": {
      "waitDeliver": 3,
      "completed": 8,
      "totalAmount": 1850.00
    }
  }
}
```

---

### 3.9 申请退款

#### 接口描述
买家申请退款（仅在待发货状态下可申请）

#### 请求信息
- **URL**: `/order/refund`
- **方法**: `POST`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| orderNo | String | 是 | 订单编号 | "20240115103000123456" |
| refundReason | String | 是 | 退款原因 | "商品已售出" |
| refundAmount | BigDecimal | 否 | 退款金额（不填则全额退款） | 200.00 |

#### 请求示例
```json
{
  "orderNo": "20240115103000123456",
  "refundReason": "商品已售出，无法发货"
}
```

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码 |
| message | String | 响应信息 |
| data | Object | 退款申请结果 |
| data.refundId | Long | 退款单ID |
| data.orderNo | String | 订单编号 |
| data.refundAmount | BigDecimal | 退款金额 |
| data.status | Integer | 退款状态：1审核中 |
| data.createTime | DateTime | 申请时间 |

#### 响应示例
```json
{
  "code": 200,
  "message": "退款申请已提交",
  "data": {
    "refundId": 8001,
    "orderNo": "20240115103000123456",
    "refundAmount": 200.00,
    "status": 1,
    "createTime": "2024-01-16 10:00:00"
  }
}
```

---

## 收藏模块

### 4.1 收藏商品

#### 接口描述
收藏商品

#### 请求信息
- **URL**: `/favorite/add`
- **方法**: `POST`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| productId | Long | 是 | 商品ID | 1001 |

#### 请求示例
```json
{
  "productId": 1001
}
```

#### 响应示例
```json
{
  "code": 200,
  "message": "收藏成功",
  "data": true
}
```

---

### 4.2 取消收藏

#### 接口描述
取消收藏

#### 请求信息
- **URL**: `/favorite/cancel`
- **方法**: `DELETE`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| productId | Long | 是 | 商品ID | 1001 |

#### 请求示例
```json
{
  "productId": 1001
}
```

#### 响应示例
```json
{
  "code": 200,
  "message": "取消收藏成功",
  "data": true
}
```

---

### 4.3 我的收藏列表

#### 接口描述
获取当前用户的收藏列表

#### 请求信息
- **URL**: `/favorite/list`
- **方法**: `GET`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| page | Integer | 否 | 页码，默认1 | 1 |
| pageSize | Integer | 否 | 每页数量，默认20 | 20 |

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码 |
| message | String | 响应信息 |
| data | Object | 分页数据 |
| data.total | Long | 总记录数 |
| data.list | Array | 收藏商品列表 |
| data.list[].productId | Long | 商品ID |
| data.list[].title | String | 商品标题 |
| data.list[].price | BigDecimal | 售价 |
| data.list[].image | String | 商品图片 |
| data.list[].status | Integer | 商品状态 |
| data.list[].statusDesc | String | 状态描述 |
| data.list[].createTime | DateTime | 收藏时间 |

#### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 23,
    "list": [
      {
        "productId": 1001,
        "title": "二手自行车",
        "price": 200.00,
        "image": "https://cdn.example.com/images/bike1.jpg",
        "status": 1,
        "statusDesc": "在售",
        "createTime": "2024-01-10 15:30:00"
      }
    ]
  }
}
```

---

## 聊天模块

### 5.1 发送消息

#### 接口描述
发送聊天消息（WebSocket方式）

#### 请求信息
- **WebSocket URL**: `ws://localhost:8080/ws/chat/{userId}`
- **需要登录**: 是（通过token参数传递）

#### 请求参数（JSON格式）
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| toUserId | Long | 是 | 接收方用户ID | 2 |
| productId | Long | 否 | 关联商品ID | 1001 |
| messageType | Integer | 否 | 消息类型：1文字 2图片，默认1 | 1 |
| content | String | 是 | 消息内容 | "你好，自行车还在吗？" |

#### 请求示例
```json
{
  "toUserId": 2,
  "productId": 1001,
  "messageType": 1,
  "content": "你好，自行车还在吗？"
}
```

#### 响应参数（服务端推送）
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码 |
| messageId | Long | 消息ID |
| fromUserId | Long | 发送方ID |
| toUserId | Long | 接收方ID |
| productId | Long | 商品ID |
| messageType | Integer | 消息类型 |
| content | String | 消息内容 |
| createTime | DateTime | 发送时间 |

---

### 5.2 获取聊天记录

#### 接口描述
获取与某个用户的聊天记录

#### 请求信息
- **URL**: `/chat/history`
- **方法**: `GET`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| toUserId | Long | 是 | 对方用户ID | 2 |
| productId | Long | 否 | 商品ID（可选，指定商品聊天） | 1001 |
| page | Integer | 否 | 页码，默认1 | 1 |
| size | Integer | 否 | 每页数量，默认20 | 20 |

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码 |
| message | String | 响应信息 |
| data | Object | 分页数据 |
| data.total | Long | 总记录数 |
| data.list | Array | 消息列表 |
| data.list[].messageId | Long | 消息ID |
| data.list[].fromUserId | Long | 发送方ID |
| data.list[].fromUserName | String | 发送方昵称 |
| data.list[].fromUserAvatar | String | 发送方头像 |
| data.list[].toUserId | Long | 接收方ID |
| data.list[].messageType | Integer | 消息类型 |
| data.list[].content | String | 消息内容 |
| data.list[].isRead | Boolean | 是否已读 |
| data.list[].createTime | DateTime | 发送时间 |

#### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 50,
    "list": [
      {
        "messageId": 10001,
        "fromUserId": 1,
        "fromUserName": "张三",
        "fromUserAvatar": "https://cdn.example.com/avatar/1.jpg",
        "toUserId": 2,
        "messageType": 1,
        "content": "你好，自行车还在吗？",
        "isRead": true,
        "createTime": "2024-01-15 10:30:00"
      }
    ]
  }
}
```

---

### 5.3 获取会话列表

#### 接口描述
获取当前用户的所有聊天会话

#### 请求信息
- **URL**: `/chat/sessions`
- **方法**: `GET`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| page | Integer | 否 | 页码，默认1 | 1 |
| size | Integer | 否 | 每页数量，默认20 | 20 |

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码 |
| message | String | 响应信息 |
| data | Object | 分页数据 |
| data.list | Array | 会话列表 |
| data.list[].userId | Long | 对方用户ID |
| data.list[].username | String | 对方昵称 |
| data.list[].avatar | String | 对方头像 |
| data.list[].productId | Long | 关联商品ID |
| data.list[].productTitle | String | 商品标题 |
| data.list[].productImage | String | 商品图片 |
| data.list[].lastMessage | String | 最后一条消息 |
| data.list[].lastMessageTime | DateTime | 最后消息时间 |
| data.list[].unreadCount | Integer | 未读消息数 |

#### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "userId": 2,
        "username": "李四",
        "avatar": "https://cdn.example.com/avatar/2.jpg",
        "productId": 1001,
        "productTitle": "二手自行车",
        "productImage": "https://cdn.example.com/images/bike1.jpg",
        "lastMessage": "好的，我要了",
        "lastMessageTime": "2024-01-15 11:00:00",
        "unreadCount": 2
      }
    ]
  }
}
```

---

### 5.4 标记消息已读

#### 接口描述
标记与某用户的聊天消息为已读

#### 请求信息
- **URL**: `/chat/read`
- **方法**: `POST`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| fromUserId | Long | 是 | 消息发送方ID | 2 |

#### 请求示例
```json
{
  "fromUserId": 2
}
```

#### 响应示例
```json
{
  "code": 200,
  "message": "已标记已读",
  "data": true
}
```

---

## 分类模块

### 6.1 获取所有分类

#### 接口描述
获取商品分类列表

#### 请求信息
- **URL**: `/category/list`
- **方法**: `GET`
- **需要登录**: 否

#### 请求参数
无

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码 |
| message | String | 响应信息 |
| data | Array | 分类列表 |
| data[].id | Integer | 分类ID |
| data[].name | String | 分类名称 |
| data[].parentId | Integer | 父分类ID（0为顶级） |
| data[].sort | Integer | 排序 |
| data[].children | Array | 子分类列表 |

#### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "数码产品",
      "parentId": 0,
      "sort": 1,
      "children": [
        {
          "id": 11,
          "name": "手机",
          "parentId": 1,
          "sort": 1
        },
        {
          "id": 12,
          "name": "电脑",
          "parentId": 1,
          "sort": 2
        }
      ]
    },
    {
      "id": 2,
      "name": "生活用品",
      "parentId": 0,
      "sort": 2,
      "children": []
    }
  ]
}
```

---

## 校园认证模块

### 7.1 发送短信验证码

#### 接口描述
发送手机短信验证码

#### 请求信息
- **URL**: `/auth/send-code`
- **方法**: `POST`
- **需要登录**: 否

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| phone | String | 是 | 手机号 | "13800138000" |
| type | Integer | 是 | 类型：1注册 2找回密码 3绑定手机 | 1 |

#### 请求示例
```json
{
  "phone": "13800138000",
  "type": 1
}
```

#### 响应示例
```json
{
  "code": 200,
  "message": "验证码已发送",
  "data": true
}
```

---

### 7.2 实名认证（学号验证）

#### 接口描述
通过学号进行校园实名认证

#### 请求信息
- **URL**: `/auth/verify-student`
- **方法**: `POST`
- **需要登录**: 是

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| studentId | String | 是 | 学号 | "20210001" |
| realName | String | 是 | 真实姓名 | "张三" |
| idCard | String | 否 | 身份证号后6位 | "123456" |
| smsCode | String | 是 | 短信验证码 | "123456" |

#### 请求示例
```json
{
  "studentId": "20210001",
  "realName": "张三",
  "idCard": "123456",
  "smsCode": "123456"
}
```

#### 响应参数
| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码 |
| message | String | 响应信息 |
| data | Object | 认证结果 |
| data.isVerified | Boolean | 是否认证成功 |
| data.college | String | 学院信息 |

#### 响应示例
```json
{
  "code": 200,
  "message": "认证成功",
  "data": {
    "isVerified": true,
    "college": "计算机学院"
  }
}
```

---

## 公共响应码说明

| 响应码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或token失效 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |

---

