# Lingmo Studio

Lingmo Studio 是一个基于大语言模型（LLM）的智能文章生成与管理平台。项目采用前后端分离架构，集成了 Spring AI Alibaba、通义千问、腾讯云 COS 以及 Pexels 高清图库，通过提供一站式的智能写作、素材获取和内容管理体验，帮助用户高效创作。

## 🌟 核心特性

- **🤖 AI 智能生成**：结合 Spring AI Alibaba 与通义千问大模型，支持自动化撰写高质量文章。
- **⚡ 实时进度推送**：后端采用异步任务处理结合 Server-Sent Events (SSE) 技术，向前端实时推送文章生成的精确进度和状态。
- **🖼️ 智能素材库**：无缝集成 Pexels 高清图库 API，一键检索并获取高质量文章配图。
- **☁️ 可靠的对象存储**：集成腾讯云 COS，安全稳定地管理图片等静态资源。
- **🔐 完整的权限控制**：提供从用户注册、登录到不同角色（如管理员/普通用户）的细粒度权限控制与会话管理。

## 🏗️ 项目架构与技术栈

项目包含完整的前端和后端工程，通过 `docker-compose` 进行统一编排。

### 📁 后端 (lingmo-studio-backend)
- **核心框架**: Java 21, Spring Boot 3.5.x
- **数据访问**: MyBatis-Flex, MySQL 8.x
- **缓存与会话**: Spring Data Redis, Spring Session Redis
- **AI 与第三方服务**: Spring AI Alibaba (DashScope), 腾讯云 COS SDK, OkHttp3 (Pexels)
- **接口文档**: Knife4j (OpenAPI 3)

### 📁 前端 (lingmo-studio-frontend)
- **核心框架**: Vue 3.5 (Composition API), Vite 7
- **开发语言**: TypeScript
- **UI 组件库**: Ant Design Vue 4.x
- **状态与路由**: Pinia, Vue Router
- **网络请求**: Axios, Server-Sent Events (SSE)
- **API 自动生成**: `@umijs/openapi` (openapi2ts)

## 📂 目录结构

```text
Lingmo-Studio/
├── lingmo-studio-backend/    # 后端 Spring Boot 工程代码及配置
│   ├── db/                   # 数据库初始化 SQL 脚本
│   ├── src/                  # Java 源码及配置文件
│   ├── Dockerfile            # 后端 Docker 构建文件
│   └── pom.xml               # Maven 依赖管理
├── lingmo-studio-frontend/   # 前端 Vue3 工程代码及配置
│   ├── src/                  # 前端源码
│   ├── Dockerfile            # 前端 Nginx Docker 构建文件
│   └── package.json          # Node 依赖管理
└── docker-compose.yml        # 全栈 Docker Compose 编排文件
```

## 🚀 快速开始

本项目提供了 Docker 一键部署和本地开发两种启动方式。

### 方式一：Docker 一键部署（推荐）

使用 Docker Compose 可以快速在服务器或本地运行整套 Lingmo Studio 系统。

1. 确保已安装 [Docker](https://www.docker.com/) 和 [Docker Compose](https://docs.docker.com/compose/)。
2. 确保已在外部准备好 MySQL 和 Redis 数据库，或者将它们加入到 `docker-compose.yml` 中。
3. （重要）在后端配置中填入你的第三方服务密钥：
   - 通义千问 API Key
   - Pexels API Key
   - 腾讯云 COS 配置
4. 在项目根目录（`d:\Project\Lingmo-Studio`）执行启动命令：
   ```bash
   docker-compose up -d --build
   ```
5. 访问应用：
   - 前端页面：`http://localhost:80`
   - 后端 API：`http://localhost:8567`
   - 接口文档：`http://localhost:8567/api/doc.html`

### 方式二：本地独立开发

如果您需要进行代码修改和二次开发，请分别启动前后端项目。

#### 1. 启动后端
```bash
cd lingmo-studio-backend
# 确保本地 MySQL、Redis 已启动，并在 application.yml 中配置好相关参数及 API Keys
mvn clean package -DskipTests
java -jar target/lingmo-studio-backend-0.0.1-SNAPSHOT.jar
```
详细说明请参阅：[后端 README.md](./lingmo-studio-backend/README.md)

#### 2. 启动前端
```bash
cd lingmo-studio-frontend
# 安装依赖 (推荐 Node.js v22+)
npm install
# 启动开发服务器
npm run dev
```
详细说明请参阅：[前端 README.md](./lingmo-studio-frontend/README.md)
