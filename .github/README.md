# CI/CD Pipeline 说明

## 概述

本项目使用 GitHub Actions 实现自动化构建流水线。

## 工作流

### CI Pipeline (`workflows/ci.yml`)

**触发条件：**
- Push 到 `main` 或 `master` 分支
- 向 `main` 或 `master` 分支发起 Pull Request

**任务：**

| 任务 | 说明 |
|------|------|
| `backend` | 后端 Maven 构建和测试 |
| `frontend` | 前端 npm 构建 |
| `build-success` | 构建状态汇总 |

## 构建产物

每次成功构建后会生成以下 Artifacts（保留7天）：

- `backend-jar`: Spring Boot JAR 包
- `frontend-dist`: Vue 前端构建产物

## 本地开发

```bash
# 后端
cd Fitness
./mvnw spring-boot:run

# 前端
cd Fitness/frontend
npm install
npm run dev
```

## 技术栈

- **后端**: Spring Boot 3.2.5, Java 17, Maven
- **前端**: Vue 3, Vite, Element Plus
- **数据库**: MySQL / H2 (开发环境)
