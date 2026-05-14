# Lingmo Studio Backend

Lingmo Studio Backend 是一个基于 Spring Boot 3 和 Java 21 构建的现代化后端应用。该项目结合了 Spring AI Alibaba 框架实现了智能化的文章生成和处理，并支持异步任务、实时进度推送（SSE）、第三方图库检索（Pexels）以及腾讯云对象存储（COS）。

## 🛠️ 技术栈

### 核心框架
- **Java**: 21
- **Spring Boot**: 3.5.13
- **数据库操作**: MyBatis-Flex 1.11.1 + HikariCP
- **数据库**: MySQL 8.x
- **缓存与会话**: Spring Data Redis + Spring Session Redis
- **AOP**: Spring Boot Starter AOP (用于权限校验等切面编程)

### AI 与第三方服务
- **AI 框架**: Spring AI Alibaba (Agent Framework, DashScope Starter)
- **对象存储**: 腾讯云 COS SDK (Tencent Cloud Object Storage)
- **第三方API调用**: OkHttp3 (主要用于 Pexels 图库 API 调用)
- **实时通信**: SSE (Server-Sent Events)

### 工具与文档
- **工具库**: Hutool, Gson, Lombok
- **接口文档**: Knife4j (基于 OpenAPI 3)

## ✨ 核心功能特性

1. **用户管理系统**
   - 用户注册、登录、信息更新
   - 基于 Spring Session 的分布式会话管理
   - 基于 AOP 的角色权限控制拦截 (`@AuthCheck`)

2. **智能文章生成与管理**
   - 结合 Spring AI Alibaba / 通义千问（DashScope）提供 AI 智能写作和文章内容处理。
   - **异步处理与进度反馈**：支持耗时任务的异步化 (`ArticleAsyncService`)，并通过 SSE (`SseEmitterManager`) 向前端实时推送文章生成和处理的进度。
   - 状态机流转管理：支持不同生成阶段 (`ArticlePhaseEnum`) 和状态 (`ArticleStatusEnum`) 的追踪。

3. **资源与图片管理**
   - 集成腾讯云 COS 实现稳定的文件、图片上传与存储管理。
   - 集成 Pexels API，支持一键检索并获取高质量配图 (`PexelsService`, `ImageSearchService`)。

4. **全局异常与响应处理**
   - 统一的 API 响应封装 (`BaseResponse`, `ResultUtils`)。
   - 全局异常拦截与处理 (`GlobalExceptionHandler`, `BusinessException`)。

## 🚀 快速开始

### 1. 环境准备
- JDK 21+
- MySQL 8.0+
- Redis
- Maven 3.6+

### 2. 数据库初始化
项目包含 SQL 初始化脚本，请在 MySQL 中执行：
- `db/user.sql`
- `db/article.sql`

### 3. 修改配置文件
在 `src/main/resources/application.yml` 中配置你的环境信息，主要包括：
- 数据库连接信息 (`spring.datasource`)
- Redis 连接信息 (`spring.data.redis`)
- 腾讯云 COS 密钥及 Bucket 配置
- 通义千问 (DashScope) API Key
- Pexels API Key

### 4. 运行项目
可以直接在 IDE 中运行 `LingmoStudioBackendApplication.java` 的 `main` 方法，或者通过 Maven 打包后运行：
```bash
# 编译并打包
mvn clean package -DskipTests

# 运行
java -jar target/lingmo-studio-backend-0.0.1-SNAPSHOT.jar
```

### 5. Docker 部署
项目根目录下提供了 `Dockerfile`，可以快速构建镜像并部署：
```bash
docker build -t lingmo-studio-backend:latest .
docker run -d -p 8080:8080 --name lingmo-backend lingmo-studio-backend:latest
```

## 📚 接口文档

项目启动后，可以通过浏览器访问 Knife4j 生成的交互式 API 文档页面：
- 地址：`http://localhost:8080/doc.html` (端口以实际配置为准)
