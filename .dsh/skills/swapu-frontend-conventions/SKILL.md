---
name: swapu-frontend-conventions
description: swapu_front 项目（Vue 3 + Vite 二手交易前端）的开发规范与架构速查：目录结构、路由、API 代理、认证与代码风格。
whenToUse: 在本仓库（swapu_front）中新增页面、修改组件、调试接口或审查代码时使用。
---

# swapu_front 开发规范与架构速查

## 项目概况

- 技术栈：Vue 3.2 + Vue Router 4 + Vite 3，**纯 JavaScript**（无 TypeScript、无 ESLint/Prettier 配置）。
- 业务：二手交易平台前端；后端为本地 Java 服务（端口 8080，见仓库根 `pom.xml`）。
- 运行命令（见 `package.json`）：
  - `npm run dev` — 开发服务器
  - `npm run build` — 生产构建
  - `npm run preview` — 预览构建产物

## 目录结构

- `src/views/` — 页面组件，与路由一一对应（Home、Sell、ProductDetail、MyOrders、ChatRoom 等）。
- `src/components/` — 可复用组件（如 `NavBar.vue`、`AccountLayout.vue`）。
- `src/router/index.js` — 集中式路由配置。
- `src/assets/` — 全局样式 `main.css`、`base.css`。
- 路径别名：`@` → `./src`（配置在 `vite.config.js`）。

## 路由约定

- 所有路由集中注册在 `src/router/index.js`，使用 `createWebHistory`。
- 带参路由：`/order-detail/:orderNo`、`/order/create/:productId`、`/product/edit/:id`、`/product/:id`。
- 新增页面流程：在 `src/views/` 创建组件 → 在 `router/index.js` 注册路由。

## API 约定

- 一律使用**原生 `fetch`**，URL 直接写后端路径，例如 `/category/list`、`/product/hot`、`/product/list?${params}`。
- 开发环境由 `vite.config.js` 的 proxy 转发到 `http://localhost:8080`：`/api` 前缀会被剥掉，`/product`、`/category`、`/favorite`、`/order`、`/user`、`/address`、`/view-history`、`/ws` 原样转发。
- 如果某个路径 404 或连不上，先核对 `vite.config.js` 的 proxy 条目与后端实际路径是否匹配。

## 认证方式

- 登录成功后 `token`、`userId`、`userInfo` 存入 `localStorage`，key 分别为 `'token'`、`'userId'`、`'userInfo'`。
- 需要登录态的请求从 `localStorage` 读取并在请求中附带。

## 代码风格

- 单文件组件使用 `<script setup>` 组合式 API：`ref`、`reactive`、`computed`、`onMounted`、`watch`。
- 路由跳转与取参使用 `useRouter` / `useRoute`。
- 模板 class 使用 kebab-case 语义化命名，中文注释。
- 不引入未安装的依赖（目前仅 `vue`、`vue-router`、`vite`、`@vitejs/plugin-vue`、`chalk`），需要新依赖时先征询用户。

## 修改时注意

- 无 CSS 框架，全局样式集中在 `src/assets/main.css` / `base.css`，页面样式写在各自组件内。
- 优先复用 `src/components/` 下的现有组件（导航、账户布局等），避免重复造轮子。
- 后端返回结构以接口实际为准；拿不准字段时先查看同页面或相邻页面的 `fetch` 用法保持一致。
