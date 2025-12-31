# 项目运行与演示步骤说明

## 1. 环境准备

### 1.1 后端环境
- **JDK 17+**: 建议使用 OpenJDK 17 或 Oracle JDK 17。
- **Maven 3.6+**: 用于构建和依赖管理。
- **MySQL 8.0+**: 数据库服务。
- **Redis (可选)**: 用于缓存优化（默认配置支持内存缓存作为备选）。

### 1.2 前端环境
- **Node.js 16+**: 建议使用 v18 或 v20 LTS 版本。
- **npm** 或 **pnpm**: 包管理工具。

---

## 2. 数据库配置

1. 启动 MySQL 服务。
2. 创建数据库 `fitness_db`：
   ```sql
   CREATE DATABASE fitness_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. 执行初始化脚本 `mysql_database_init.sql`（位于项目根目录）以创建表结构并插入初始数据。
4. 修改后端配置文件 `src/main/resources/application.yml` 中的数据库连接信息：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/fitness_db?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=utf-8
       username: your_username
       password: your_password
   ```

---

## 3. 运行后端 (Spring Boot)

1. 进入项目根目录 `Fitness`。
2. 使用命令行或 IDE（如 IntelliJ IDEA）运行：
   ```bash
   mvn spring-boot:run
   ```
3. 默认启动端口为 `8080`。访问 `http://localhost:8080/swagger-ui/index.html` 可查看接口文档。

---

## 4. 运行前端 (Vue 3)

1. 进入前端目录 `Fitness/frontend`。
2. 安装依赖：
   ```bash
   npm install
   ```
3. 启动开发服务器：
   ```bash
   npm run dev
   ```
4. 默认启动端口为 `5173`。访问 `http://localhost:5173` 进入系统。

---

## 5. 演示步骤

### 5.1 用户登录
- **管理员账号**: `admin` / `admin123`
- **普通用户**: 可通过注册页面自行注册。

### 5.2 核心功能演示
1. **仪表盘 (Dashboard)**: 登录后首屏展示个人健身概览、近期活动和身体指标趋势。
2. **1RM 计算 (Load Analysis)**:
   - 进入“训练分析”页面。
   - 切换到“1RM 计算”标签。
   - 输入重量和次数，实时查看多种公式的计算结果及强度建议。
3. **训练计划 (Fitness Plans)**:
   - 查看当前生效的周计划。
   - 勾选已完成的动作。
   - 体验计划的增删改查功能。
4. **历史统计 (History Statistics)**:
   - 查看训练量趋势折线图。
   - 查看动作分布饼图。
   - 筛选不同日期范围和动作类型的数据。
5. **负荷监控 (Load Trend)**:
   - 在“训练分析”中查看负荷趋势图。
   - 获取系统根据恢复状态给出的训练建议。

---

## 6. 常见问题排查

- **跨域问题**: 后端已配置 `CorsConfig.java`，前端 Vite 配置了 `proxy` 代理。如遇到跨域错误，请检查 `vite.config.js` 中的代理目标端口是否与后端一致。
- **数据库连接失败**: 请确保 MySQL 服务已启动且 `application.yml` 中的用户名密码正确。
- **图标不显示**: 系统采用 Element Plus 图标，确保网络连接正常（部分图标通过 CDN 或包管理器安装）。
