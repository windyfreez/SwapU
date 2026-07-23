[README.md](https://github.com/user-attachments/files/30312185/README.md)
<div align="center">
  <img src="https://github.com/windyfreez/SwapU/raw/main/notemd/logo.png" width="400" alt="SwapU Logo">
  
  <h1>SwapU 云市集</h1>
  <p><b>一个采用前后端分离架构设计的云端市集平台</b></p>

  <img src="https://img.shields.io/badge/Java-Spring%20Boot-brightgreen.svg?style=flat-square&logo=springboot">
  <img src="https://img.shields.io/badge/MyBatis-Framework-orange.svg?style=flat-square&logo=mybatis">
  <img src="https://img.shields.io/badge/MySQL-Database-blue.svg?style=flat-square&logo=mysql">
  <img src="https://img.shields.io/badge/Redis-Cache-red.svg?style=flat-square&logo=redis">
  <img src="https://img.shields.io/badge/Vue.js-Frontend-green.svg?style=flat-square&logo=vuedotjs">
  
  <div style="margin-top: 10px;">
    <img src="https://img.shields.io/github/stars/windyfreez/SwapU?style=social">
    <img src="https://img.shields.io/github/forks/windyfreez/SwapU?style=social">
    <img src="https://img.shields.io/github/issues/windyfreez/SwapU">
    <img src="https://img.shields.io/github/license/windyfreez/SwapU">
    <img src="https://img.shields.io/github/languages/top/windyfreez/SwapU">
  </div>
</div>

## 前言
本人是一个西安电子科技大学的计算机科学与技术专业的学生，这是我的第一个Spring Boot单体项目，希望通过完成这个项目来扎实自己的技术栈知识掌握程度以及编码调试能力，目前项目所有接口功能开发已经完成，正在做性能优化，例如高并发运用场景下的流量承压、热门商品的秒杀与库存预扣减等。
- 由于我主要学习后端，因而前端代码大部分由AI IDE完成，Bug调试和代码Review由本人全部完成。各位大佬请多多指教，问题可以在issue中反映，因为issue流程清晰可见。
- 后续将完成管理端、AI审核和AI助手，AI部分会涉及到RAG、Langchain等技术栈，本人将一边学习技术栈一边完成开发，并且不断完善和优化已有功能。

## 项目简介

对于在商圈或校园中传统的线下市集来说，信息杂乱不集中、交易不方便、商品流转流程不够规范且浪费公共资源是始终存在的痛点，而SwapU云市集平台则是为解决这些痛点而诞生的商品交易平台。

SwapU云市集，一个采用**前后端分离架构**设计的云端市集平台，前端使用 **Vue3 + Element Plus** 构建用户界面，后端采用 **Spring Boot** 框架实现业务逻辑处理。

为提高系统性能，使用 **Redis** 作为缓存中间件，实现热门商品以及库存信息缓存。使用 **JWT** 实现用户身份鉴权，采用拦截器校验用户Token。系统聊天模块采用 **WebSocket** 技术实现实时通信，支持买卖双方即时消息交互。同时通过**阿里云OSS**存储提供文件上传服务用于商品图片存储。整体架构具有良好的扩展性、可维护性和高并发处理能力。

## 功能特性

- 📱 **用户模块**：注册、登录、密码修改、个人信息管理、收藏管理
- 🛒 **商品模块**：发布、浏览、分类查询、编辑、下架、删除、热门推荐、分页查询
- 📋 **订单模块**：创建、支付、取消、确认接单、发货、确认收货、订单详情、订单统计
- 💬 **聊天模块**：实时消息、会话列表、历史消息、消息已读状态同步
- 📍 **地址模块**：收货地址管理（增删改查、默认地址设置）
- 🚀 **性能优化**：Redis缓存、库存预扣减、异步同步、定时任务调度

## 项目目录

```
com.itsean.swapu
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

## 项目文档

- [接口文档](notemd/swapu_interface_doc.md)
- [性能优化指南](notemd/optimization.md)
- [数据库表设计](notemd/swapu_database.md)

## 项目地址

本项目的所有代码已经托管到Github上 ，欢迎大家 Star 和 Fork 支持~
项目地址：https://github.com/windyfreez/SwapU

## 技术选型

### 后端技术

| 技术名称 | 实现功能 | 官网网址 |
| :------- | :------- | :------- |
| Spring Boot | 后端核心业务逻辑处理框架，提供 RESTful API | https://spring.io/projects/spring-boot |
| MyBatis | 持久化方案，负责与 MySQL 数据库的数据交互 | https://mybatis.org/ |
| MySQL | 业务数据持久化存储（用户、商品、订单、聊天记录等） | https://www.mysql.com/ |
| Redis | 高性能缓存中间件，缓存 Token、热门商品、库存、浏览量等 | https://redis.io/ |
| JWT (JSON Web Token) | 用户身份认证与权限校验，配合拦截器校验用户 Token | https://jwt.io/ |
| WebSocket | 实现买卖双方全双工实时通信，支持即时消息交互 | https://spring.io/guides/gs/messaging-stomp-websocket/ |
| 阿里云 OSS | 对象存储服务，提供商品图片上传与访问 | https://www.aliyun.com/product/oss |
| Spring Schedule | 定时任务调度，用于热门商品统计、浏览量同步、订单状态维护 | https://spring.io/projects/spring-framework |
| slf4j | 日志门面接口，统一日志管理 | https://www.slf4j.org/ |
| Lombok | 自动生成 getter/setter/toString 等样板代码，简化开发 | https://projectlombok.org/ |

### 前端技术

| 技术名称 | 实现功能 | 官网网址 |
| :------- | :------- | :------- |
| Vue 3 | 构建响应式用户界面，前端核心框架 | https://vuejs.org/ |
| Element Plus | 提供 UI 组件库，快速搭建页面 | https://element-plus.org/ |
| Nginx | 静态资源服务器与反向代理，部署前端应用 | https://nginx.org/ |


### 系统架构图：
<img width="2660" height="1131" alt="mermaid-diagram (1)" src="https://github.com/user-attachments/assets/65fd9db0-7eea-4c3a-afba-eed67cf3989f" />

### 数据库ER图：
<img width="2207" height="2374" alt="mermaid-diagram (2)" src="https://github.com/user-attachments/assets/842dbf9e-8c1a-4740-a1e0-c803f263510e" />

## 📸 项目截图

### 首页展示
![首页展示](https://github.com/windyfreez/SwapU/raw/main/notemd/screenshots/home.png)

### 商品详情
![商品详情](https://github.com/windyfreez/SwapU/raw/main/notemd/screenshots/product.png)

### 交易订单
![交易订单](https://github.com/windyfreez/SwapU/raw/main/notemd/screenshots/order.png)

### 实时聊天
![实时聊天](https://github.com/windyfreez/SwapU/raw/main/notemd/screenshots/chat.png)

---

## 业务架构
我们将业务逻辑拆解为五个核心协作模块：

本系统采用模块化设计思想，将业务功能划分为用户管理、商品管理、订单管理、即时通讯以及辅助服务五个核心模块，各模块相互协作，共同完成校园二手交易平台的业务闭环。

### 用户管理模块

用户管理模块负责系统用户的身份认证与基础信息维护，是平台运行的基础模块。系统支持用户注册、登录、密码修改以及个人信息更新等功能，并通过 JWT 实现用户身份认证与权限校验。同时，用户可查看个人资料以及管理自己的收藏商品，为后续商品浏览和交易活动提供支持。

### 商品管理模块

商品管理模块负责闲置商品信息的发布、维护与展示。系统提供商品分类查询、商品发布、商品编辑、商品下架、商品删除以及商品详情查看等功能，并支持分页查询和热门商品推荐。通过规范化管理商品信息，提高了商品展示效果和用户检索效率，为买卖双方提供便捷的交易环境。

### 订单管理模块

订单管理模块用于实现交易流程的标准化管理。买家可在线创建订单、支付订单或取消订单；卖家可对订单进行确认接单和发货操作；买家在收到商品后可确认收货，从而完成整个交易流程。系统还支持订单详情查询、订单分页查询以及订单统计分析等功能，实现交易全过程的可追溯管理。

### 即时通讯模块

即时通讯模块基于 WebSocket 技术实现买卖双方的实时消息交互。系统支持会话列表查询、历史消息查询、消息发送以及消息已读状态同步等功能。聊天消息在数据库中进行持久化存储，确保用户能够随时查看历史沟通记录，提高交易沟通效率和用户体验。

### 辅助服务模块

辅助服务模块为系统其他业务模块提供公共支撑能力。系统集成对象存储服务（OSS）实现商品图片上传与访问；利用 Redis 实现热点数据缓存、用户状态缓存以及热门商品缓存，提高系统访问性能；通过定时任务机制完成热门商品统计、浏览量同步以及订单状态维护等后台任务，保障系统稳定运行。

## 性能优化

为提升系统在高并发访问场景下的响应能力与稳定性，本系统从缓存机制、数据同步以及推荐策略等方面进行了优化设计。

### 热门商品缓存优化

针对热门商品访问频率高、数据库查询压力大的问题，系统引入 Redis 作为缓存中间件，实现热点数据缓存机制。

* **缓存热点数据**

  系统定时统计商品浏览量、收藏量等指标，计算热门商品列表，并将结果缓存至 Redis 中。用户访问热门商品接口时优先读取缓存数据，减少对 MySQL 数据库的频繁访问，提高接口响应速度。

* **浏览量异步更新**

  用户访问商品详情时，浏览量首先写入 Redis 缓存，由定时任务统一进行批量同步并更新至 MySQL 数据库。该方案避免了高并发场景下频繁更新数据库带来的写入压力，降低了数据库锁竞争风险，提高系统整体吞吐能力。

* **缓存与数据库协同机制**

  系统采用“缓存优先、数据库兜底”的访问策略。当缓存未命中时，从数据库加载数据并回写缓存，从而保证数据访问效率与系统稳定性。

### 高并发库存扣减优化

针对限时抢购、秒杀等高并发交易场景下的库存扣减难题，系统采用 **"Redis 预扣减 + 异步同步 + 兜底对账"** 的三层防护体系，在避免超卖的同时显著提升并发处理能力。

* **Redis 原子预扣减**

  所有库存扣减请求首先在 Redis 中通过 Lua 脚本执行原子性操作，利用 Redis 单线程模型天然保证原子性，避免大量线程串行等待数据库行锁导致的性能瓶颈。该方案将库存扣减接口的 TPS 从原来的 500 左右提升至 8000+，性能提升超过 16 倍。

* **异步同步到数据库**

  预扣减成功后，系统将扣减日志放入消息队列，由消费者异步更新数据库。通过 `UPDATE ... WHERE stock >= #{quantity}` 的条件更新进行二次校验，防止最终超卖。该机制将高频随机写入转化为低频批量同步，数据库连接占用从高峰期的 80% 下降到 10% 以下，有效避免了连接资源耗尽与事务长尾问题。

* **定时对账机制**

  系统每 30 分钟执行一次 Redis 与数据库库存的全量对账。对于微小偏差自动以数据库为准修正 Redis；偏差较大时触发告警通知人工介入，从而保障库存数据的最终一致性。

* **缓存预热与库存初始化**

  系统启动或商品上架时，自动将活跃商品库存数据预热到 Redis 中，避免高并发下缓存击穿导致的请求直透数据库。

通过 Lua 脚本原子性保证配合数据库条件更新二次校验，系统实现了零超卖，显著提升了高并发交易场景下的稳定性与吞吐能力。

### 首页商品推荐优化

为了提升用户浏览体验并增加商品曝光机会，系统设计了基于随机重排的推荐策略。

* **分区推荐机制**

  根据商品热度将商品划分为热门商品区和普通商品区。热门商品优先展示，以保证高质量商品获得更多曝光；普通商品则参与随机推荐，避免长期处于低曝光状态。

* **随机重排算法**

  在各分区内部采用随机排序策略，对商品展示顺序进行动态调整，降低首页内容固定化现象，提高用户探索兴趣与浏览新鲜感。

* **推荐效果优化**

  该策略兼顾热门商品展示与普通商品曝光需求，在保证用户快速发现优质商品的同时，也为新发布商品提供更多展示机会，从而促进平台整体交易活跃度。

### 定时任务优化

系统基于 Spring Schedule 实现定时任务调度机制，对热点数据和业务状态进行自动维护。

* 定时更新热门商品排行榜；
* 定时同步 Redis 中的商品浏览量数据；
* 定时检测订单状态，处理超时未支付订单；
* 定期清理失效缓存数据。

通过后台自动化任务处理机制，减少实时计算压力，提高系统运行效率和数据一致性。

## 环境搭建

### 开发工具

|     工具     |       说明        |                             官网                             |
| :----------: | :---------------: | :----------------------------------------------------------: |
|     IDEA     |    Java开发IDE    |           https://www.jetbrains.com/idea/download            |
|   WebStorm   |    前端开发IDE    |             https://www.jetbrains.com/webstorm/              |
| RedisDesktop |  Redis可视化工具  | [ https://redisdesktop.com/download](https://redisdesktop.com/download) |
|    DataGrip    |  数据库开发工具   |               [https://www.jetbrains.com/datagrip/download](https://www.jetbrains.com/datagrip/download)               |

### 开发环境

|     工具      | 版本号 |                             下载                             |
| :-----------: | :----: | :----------------------------------------------------------: |
|      JDK      |  17   | https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html |
|     Maven     | 3.3.0+ |                   http://maven.apache.org/                   |
|     MySQL     |  5.6   |                    https://www.mysql.com/                    |
|   RabbitMQ    | 3.7.4  |            http://www.rabbitmq.com/download.html             |
|     Nginx     |  1.22.1  |              http://nginx.org/en/download.html               |
|     Redis     | 3.3.0  |                  https://redis.io/download                   |       

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.3.0+
- MySQL 5.6+
- Redis 3.3.0+
- Node.js 16+

### 后端启动

```bash
# 克隆项目
git clone https://github.com/windyfreez/SwapU.git

# 进入项目目录
cd SwapU

# 创建数据库
CREATE DATABASE swapu CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 修改数据库配置
# 编辑 src/main/resources/application.yml
# 配置数据库连接信息（username、password）
# 配置Redis连接信息（host、port）
# 配置阿里云OSS信息（endpoint、accessKeyId、accessKeySecret、bucketName）

# 启动后端
mvn spring-boot:run
```

### 前端启动

```bash
# 进入前端目录
cd front

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

### 访问地址

| 服务 | 地址 |
| :--- | :--- |
| 前端 | http://localhost:5173 |
| 后端API | http://localhost:8080 |
| Swagger文档 | http://localhost:8080/swagger-ui.html |
| Redis | localhost:6379 |

### 部署说明

```bash
# 前端打包
cd front
npm run build

# Nginx配置
# 将前端dist目录部署到Nginx
# 配置反向代理指向后端API
```

## 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request
