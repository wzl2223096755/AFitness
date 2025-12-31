# AFitness - 力量训练负荷与恢复监控系统

本项目是一个全栈式的健身管理系统，包含基于 Spring Boot 的后端服务和基于 Vue 3 的移动端前端应用。

## 目录结构

- `Fitness/`: 后端 Spring Boot 项目根目录
  - `src/main/java/`: 后端源码
  - `frontend/`: 前端 Vue 3 项目根目录
  - `scripts/`: 项目管理相关脚本 (SQL, 测试 HTML)
  - `docs/`: 项目文档与报告
  - `logs/`: 运行日志 (Git 已忽略)
- `.editorconfig`: 跨编辑器代码格式规范
- `.gitignore`: 全局 Git 忽略配置

## 技术栈

### 后端 (Backend)
- **框架**: Spring Boot 3.2.5
- **数据库**: MySQL 8.0 / H2 (开发)
- **安全**: Spring Security + JWT
- **缓存**: Redis
- **监控**: Actuator + Prometheus
- **文档**: Swagger/OpenAPI

### 前端 (Frontend)
- **框架**: Vue 3 + Vite
- **UI 组件**: Vant UI (移动端) + Element Plus (管理端)
- **状态管理**: Pinia
- **路由**: Vue Router
- **图表**: ECharts
- **风格**: Neon Ionized (离子化霓虹风格)

## 快速开始

### 后端启动
1. 进入 `Fitness/` 目录
2. 确保已安装 JDK 17 和 Maven
3. 运行 `./mvnw spring-boot:run`

### 前端启动
1. 进入 `Fitness/frontend/` 目录
2. 安装依赖：`npm install`
3. 启动开发服务器：`npm run dev`

## 项目规范

- **代码规范**: 前端使用 ESLint + Prettier，后端遵循标准 Java 开发规范。
- **管理规范**: 
  - 所有 SQL 脚本存放在 `Fitness/scripts/sql/`
  - 临时测试 HTML 存放在 `Fitness/scripts/test-html/`
  - 文档更新请参考 `Fitness/README.md`
