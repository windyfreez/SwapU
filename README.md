<div align="center">
  <img src="https://github.com/windyfreez/SwapU/raw/main/notemd/logo.png" width="400" alt="SwapU Logo">
  
  <h1>SwapU 云市集</h1>
  <p><b>让校园闲置交易简单、透明、有趣！</b></p>

  <img src="https://img.shields.io/badge/Java-Spring%20Boot-brightgreen.svg?style=flat-square&logo=springboot">
  <img src="https://img.shields.io/badge/MyBatis-Framework-orange.svg?style=flat-square&logo=mybatis">
  <img src="https://img.shields.io/badge/MySQL-Database-blue.svg?style=flat-square&logo=mysql">
  <img src="https://img.shields.io/badge/Redis-Cache-red.svg?style=flat-square&logo=redis">
  <img src="https://img.shields.io/badge/Vue.js-Frontend-green.svg?style=flat-square&logo=vuedotjs">
</div>

---

## 📖 项目简介
**SwapU 云市集**是一款专为校园场景设计的二手交易平台。项目旨在解决传统校园二手交易中存在的**信息碎片化、流程不透明、交易不便捷**等核心痛点，打造一个规范、高效、诚信的校园闲置生态圈。

---

## 🛠️ 一、 技术栈与架构体系
* **后端核心**：Spring Boot
* **持久化方案**：MyBatis
* **数据存储**：MySQL (业务数据) + Redis (高性能缓存/数据削峰)
* **前端框架**：Vue.js (响应式交互)
* **即时通讯**：WebSocket (全双工实时通信)

本系统采用前后端分离架构设计，前端使用 Vue3 + Element Plus 构建用户界面，后端采用 Spring Boot 框架实现业务逻辑处理。系统整体按照 Controller、Service、Mapper 三层架构进行设计。Controller 层负责接收客户端请求并返回响应结果；Service 层负责业务逻辑处理，包括用户管理、商品管理、订单管理、收藏管理以及即时聊天等功能；Mapper 层负责与 MySQL 数据库进行数据交互。

为提高系统性能，引入 Redis 作为缓存中间件，实现用户登录 Token、热门商品以及库存信息缓存。系统聊天模块采用 WebSocket 技术实现实时通信，支持买卖双方即时消息交互。同时提供文件上传服务用于商品图片存储。整体架构具有良好的扩展性、可维护性和高并发处理能力。

系统架构图：
<img width="2660" height="1131" alt="mermaid-diagram (1)" src="https://github.com/user-attachments/assets/65fd9db0-7eea-4c3a-afba-eed67cf3989f" />
数据库ER图：
<img width="2207" height="2374" alt="mermaid-diagram (2)" src="https://github.com/user-attachments/assets/842dbf9e-8c1a-4740-a1e0-c803f263510e" />


---

## 🧩 二、 核心业务架构模块
我们将业务逻辑拆解为五个核心协作模块：

本系统采用模块化设计思想，将业务功能划分为用户管理、商品管理、订单管理、即时通讯以及辅助服务五个核心模块，各模块相互协作，共同完成校园二手交易平台的业务闭环。

### （1）用户管理模块

用户管理模块负责系统用户的身份认证与基础信息维护，是平台运行的基础模块。系统支持用户注册、登录、密码修改以及个人信息更新等功能，并通过 JWT 实现用户身份认证与权限校验。同时，用户可查看个人资料以及管理自己的收藏商品，为后续商品浏览和交易活动提供支持。

### （2）商品管理模块

商品管理模块负责闲置商品信息的发布、维护与展示。系统提供商品分类查询、商品发布、商品编辑、商品下架、商品删除以及商品详情查看等功能，并支持分页查询和热门商品推荐。通过规范化管理商品信息，提高了商品展示效果和用户检索效率，为买卖双方提供便捷的交易环境。

### （3）订单管理模块

订单管理模块用于实现交易流程的标准化管理。买家可在线创建订单、支付订单或取消订单；卖家可对订单进行确认接单和发货操作；买家在收到商品后可确认收货，从而完成整个交易流程。系统还支持订单详情查询、订单分页查询以及订单统计分析等功能，实现交易全过程的可追溯管理。

### （4）即时通讯模块

即时通讯模块基于 WebSocket 技术实现买卖双方的实时消息交互。系统支持会话列表查询、历史消息查询、消息发送以及消息已读状态同步等功能。聊天消息在数据库中进行持久化存储，确保用户能够随时查看历史沟通记录，提高交易沟通效率和用户体验。

### （5）辅助服务模块

辅助服务模块为系统其他业务模块提供公共支撑能力。系统集成对象存储服务（OSS）实现商品图片上传与访问；利用 Redis 实现热点数据缓存、用户状态缓存以及热门商品缓存，提高系统访问性能；通过定时任务机制完成热门商品统计、浏览量同步以及订单状态维护等后台任务，保障系统稳定运行。

springboot分层架构：
```
com.itsean.campus_second_hand
│
├── config                     # 配置层
│   ├── RedisConfiguration
│   ├── WebMvcConfiguration
│   ├── WebSocketConfiguration
│   └── OssConfiguration
│
├── controller                 # 接口控制层
│   ├── UserController
│   ├── ProductController
│   ├── OrderController
│   ├── FavoriteController
│   ├── CategoryController
│   ├── ChatController
│   └── AddressBookController
│
├── dto                        # 数据传输对象
│   ├── UserLoginDTO
│   ├── ProductDTO
│   ├── OrderDTO
│   ├── ChatMessageDTO
│   └── ...
│
├── service                    # 业务接口层
│   ├── UserService
│   ├── ProductService
│   ├── OrderService
│   ├── ChatService
│   └── ...
│
├── service.impl               # 业务实现层
│   ├── UserServiceImpl
│   ├── ProductServiceImpl
│   ├── OrderServiceImpl
│   ├── ChatServiceImpl
│   └── ...
│
├── mapper                     # 数据访问层
│   ├── UserMapper
│   ├── ProductMapper
│   ├── OrderMapper
│   ├── FavoriteMapper
│   ├── ChatMapper
│   └── ...
│
├── entity                     # 实体类
│   ├── User
│   ├── Product
│   ├── Order
│   ├── Favorite
│   ├── Category
│   └── ChatMessage
│
├── vo                         # 视图对象
│   ├── ProductVO
│   ├── OrderVO
│   ├── ChatResponseVO
│   └── ...
│
├── interceptor                # 拦截器
│   └── JwtTokenUserInterceptor
│
├── handler                    # 处理器
│   ├── GlobalExceptionHandler
│   └── ChatWebSocketHandler
│
├── task                       # 定时任务
│   ├── HotProductTask
│   ├── ViewCountSyncTask
│   └── OrderStatusTask
│
├── utils                      # 工具类
│   ├── JwtUtil
│   ├── AliOssUtil
│   └── SimpleRandomSortUtil
│
├── constant                   # 常量定义
│
├── exception                  # 自定义异常
│
├── properties                 # 配置属性绑定
│
├── context                    # 上下文管理
│   └── BaseContext
│
└── CampusSecondHandApplication
```

---

## ⚡ 三、 系统性能优化与架构设计
为应对高并发访问，我们实施了以下深度优化方案：

### 1. 热门商品高并发缓存策略
* **写入削峰**：利用 Redis 缓冲浏览量，定时批量回写 MySQL，避免数据库写锁竞争。
* **热点预热**：定时计算 Top 20 热门闲置并预置入 Redis，确保接口响应在 5ms 以内。

### 2. 首页智能推荐的“动态多样性”算法
* **分区随机重排**：将商品池拆分为“热门区”与“常规区”，并在分区内执行随机 Shuffle。
* **业务效果**：兼顾热门曝光与新品扶持，提升首页探索感。

---

## 💡 四、 开发与协作指南

> 🛡️ **安全校验**：核心接口 Header 需携带身份 Token。  
> 🚀 **缓存优先**：热门查询严禁绕过 Redis 直接检索数据库。  
> 💬 **实时通讯**：前端通过 WebSocket 与后端保持长连接。

---
