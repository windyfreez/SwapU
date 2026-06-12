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

---

## 🧩 二、 核心业务架构模块
我们将业务逻辑拆解为五个核心协作模块：

### 1. 用户身份与资产管理
打造基于校园身份的信任基石。
* **账户体系**：支持用户注册、登录，提供密码修改与基础信息更新接口。
* **个人画像与查询**：方便在交易过程中进行实名认证与信用展示。
* **收藏夹服务**：提供“心愿清单”功能，支持快捷添加或移除心仪闲置。

### 2. 商品全生命周期管理
解决交易信息不集中、发布不规范的问题。
* **精准分类与浏览**：通过分类列表进行筛选，并提供热门商品推荐及多维度分页查询。
* **商品标准化维护**：支持商品发布、详情编辑、下架及删除操作。
* **透明检索**：大幅提升闲置商品的查找效率。

### 3. 订单交易闭环
将非正式的“私下交易”转化为标准化的“平台订单”。

* **交易流程**：线上创建订单、支付及取消订单，确保交易意向明确。
* **闭环物流与执行**：提供商家确认接单、订单发货，以及买家确认收货的闭环链路。
* **统计分析**：提供订单详情查询及系统级统计信息。

### 4. 实时通讯 (IM) 模块
解决买卖双方沟通不及时、社交隐私泄露问题。
* **会话与历史管理**：支持获取聊天会话列表及历史消息记录。
* **实时交互**：基于 WebSocket 实现即时消息推送，具备“消息已读”状态同步功能。

### 5. 辅助服务
* **统一资源服务**：提供标准化的文件上传接口，确保展示图的标准化存储与加载。

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
