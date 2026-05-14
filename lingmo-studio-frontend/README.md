# Lingmo Studio - Frontend (前端项目)

## 📖 项目概述
**Lingmo Studio Frontend** 是 Lingmo Studio 系统的核心前端工程，致力于提供现代化、响应式且用户友好的界面交互体验。作为整个系统的前端承载者，本项目主要负责与后端 API 交互（如用户管理、文章生成、实时进度推送等），实现复杂的业务逻辑展示和高效的数据渲染。

## 🛠 技术栈明细
本项目采用现代化的前端技术栈构建，主要核心技术与版本如下：

*   **核心框架**: [Vue.js](https://vuejs.org/) (`^3.5.17`) - 采用 Composition API 构建高复用性、易维护的视图层。
*   **构建工具**: [Vite](https://vitejs.dev/) (`^7.0.0`) - 下一代前端构建工具，提供极速的冷启动和 HMR（热更新）。
*   **开发语言**: [TypeScript](https://www.typescriptlang.org/) (`~5.8.0`) - 提供强类型约束，提升代码健壮性和可维护性。
*   **UI 组件库**: [Ant Design Vue](https://www.antdv.com/) (`^4.2.6`) - 企业级中后台 UI 解决方案，提供丰富的组件支持。
*   **状态管理**: [Pinia](https://pinia.vuejs.org/) (`^3.0.3`) - Vue 官方推荐的新一代状态管理库，轻量且完全支持 TS。
*   **路由管理**: [Vue Router](https://router.vuejs.org/) (`^4.5.1`) - 负责单页面应用（SPA）的路由跳转与权限拦截。
*   **网络请求**: [Axios](https://axios-http.com/) (`^1.11.0`) - 强大且灵活的 HTTP 请求库。
*   **API 接口生成**: `@umijs/openapi` - 结合 `openapi2ts` 脚本自动化生成 API 接口与 TypeScript 类型定义，打通前后端接口协议。
*   **其他核心库**: `dayjs` (`^1.11.13` 日期处理), `marked` (`^17.0.1` Markdown 渲染)。

## ⚙️ 环境依赖要求
为了保证项目能够顺利运行和构建，请确保您的开发环境满足以下要求：

*   **Node.js**: `v22.0.0` 或更高版本 (推荐使用 NVM 或 Volta 进行版本管理)。
*   **包管理器**: `npm` (本项目包含 `package-lock.json` 锁文件，请统一使用 npm 安装依赖，避免依赖版本冲突)。
*   **操作系统**: Windows / macOS / Linux 均可。

## 🚀 快速开始

### 1. 克隆与安装

```bash
# 进入项目目录
cd lingmo-studio-frontend

# 安装依赖
npm install
```

### 2. 开发环境运行

```bash
# 启动本地开发服务器
npm run dev
```
运行成功后，可通过浏览器访问 Vite 提供的本地服务地址（通常为 `http://localhost:5173`）。

### 3. 构建与部署

```bash
# 生产环境打包构建
npm run build

# 预览打包后的产物
npm run preview
```
打包生成的文件将存放在根目录的 `dist/` 文件夹中，可配合 Nginx 或其他 Web 服务器进行静态托管部署。

### 4. 其他常用命令

```bash
# 自动化生成后端 API 对应的 TS 代码（依赖 openapi2ts.config.ts 配置）
npm run openapi2ts

# 执行 TypeScript 类型检查
npm run type-check

# 按照 Prettier 规范格式化代码
npm run format

# 代码规范检查及自动修复 (ESLint)
npm run lint
```

## 📂 目录结构说明

```text
lingmo-studio-frontend/
├── public/              # 静态资源文件（不会被 Vite 编译）
├── src/
│   ├── api/             # 自动生成及二次封装的后端 API 接口
│   ├── assets/          # 样式、图片等需编译的静态资源
│   ├── components/      # 业务通用和基础 UI 组件
│   ├── config/          # 全局配置文件（如环境参数）
│   ├── constants/       # 全局常量定义（如状态枚举）
│   ├── layouts/         # 页面整体布局组件（如 BasicLayout 骨架）
│   ├── pages/           # 页面级视图组件（按业务模块划分，如 article, user, admin）
│   ├── router/          # Vue Router 路由定义与拦截配置
│   ├── stores/          # Pinia 状态管理模块
│   ├── styles/          # 全局通用 CSS/SCSS 样式与变量
│   ├── utils/           # 实用工具函数（如时间格式化、SSE实时推送处理、Markdown解析）
│   ├── views/           # 常规视图页面（如 HomeView, AboutView）
│   ├── App.vue          # Vue 应用根组件
│   ├── access.ts        # 用户权限与路由访问控制逻辑
│   ├── request.ts       # Axios 请求与响应拦截器配置
│   └── main.ts          # 项目入口文件，应用挂载与插件初始化
├── .env.development     # 开发环境变量配置
├── .env.production      # 生产环境变量配置
├── openapi2ts.config.ts # OpenAPI 接口生成配置文件
├── package.json         # 项目依赖与脚本配置
├── tsconfig.json        # TypeScript 根配置文件
└── vite.config.ts       # Vite 构建与插件配置文件
```
