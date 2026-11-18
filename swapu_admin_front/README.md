# SwapU 管理后台前端

独立的管理端前端项目（Vue 3 + Vue Router + Vite，纯 JavaScript，无 UI 组件库）。

## 快速开始

```bash
# 安装依赖
npm install

# 启动开发服务器（默认 http://localhost:5174）
npm run dev

# 生产构建
npm run build

# 预览构建产物（默认 4174，代理同样指向 8081）
npm run preview
```

## 后端与代理

- 管理端接口服务：`http://localhost:8081`，接口统一以 `/admin` 开头。
- 开发服务器在 `vite.config.js` 中把 `/admin` 代理到 8081；页面请求（`Accept: text/html`）由 `spaBypass` 交给前端路由处理。
- 后端未配置 CORS，请始终通过代理或同源部署访问。
- 登录初始账号：`root / 123456`（见 `swapu_admin/src/main/resources/sql/admin.sql`，首次部署后请修改）。

## 页面

| 路由 | 页面 |
| --- | --- |
| `/admin/login` | 登录 |
| `/admin/dashboard` | 工作台（仅展示现有接口可取的数据） |
| `/admin/audit/products` | 商品审核 |
| `/admin/audit/comments` | 评论审核 |
| `/admin/audit/records` | 审核流水 |
| `/admin/categories` | 分类管理 |
| `/admin/users` | 用户管理 |
| `/admin/admins` | 管理员管理（仅超级管理员） |
| `/admin/profile` | 个人设置 |

## 说明

- 登录态保存在 `localStorage`：`adminToken`、`adminInfo`（与用户端 `swapu_front` 的 `token`、`userInfo` 隔离）。
- 统一请求封装见 `src/api/request.js`：HTTP 401 视为登录失效并跳转登录页；HTTP 200 但 `code !== 200` 作为业务错误提示，不登出。
- 平台统计（成交额、流量趋势、商品状态分布等）后端尚未实现，工作台只展示待审核商品、待审核评论、注册用户、分类数等现有数据。
- 完整接口清单见 [`../docs/swapu_admin_api_inventory.md`](../docs/swapu_admin_api_inventory.md)。
