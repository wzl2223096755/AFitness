# CI/CD Pipeline Documentation

## Overview

本项目使用 GitHub Actions 实现持续集成和持续部署（CI/CD）流水线。

## 工作流文件

### 1. CI Pipeline (`workflows/ci.yml`)

主要的持续集成流水线，在代码提交和PR时自动运行。

**触发条件：**
- Push 到 `main`、`develop`、`master` 分支
- 向 `main`、`master` 分支发起 Pull Request

**任务：**
- `test-frontend`: 前端单元测试
- `test-e2e`: 端到端测试（依赖前端测试通过）
- `test-backend`: 后端单元测试
- `build`: 构建应用（依赖前端和后端测试通过）
- `code-quality`: 代码质量检查（ESLint）
- `notify-results`: 测试结果通知

### 2. Deploy Pipeline (`workflows/deploy.yml`)

部署流水线，用于将应用部署到测试环境。

**触发条件：**
- Push 到 `main`、`master` 分支
- 手动触发（workflow_dispatch）

**任务：**
- `build-and-push`: 构建并推送 Docker 镜像到 GitHub Container Registry
- `deploy-test`: 部署到测试环境
- `notify`: 发送部署通知

## Docker 配置

### 后端 Dockerfile (`Fitness/Dockerfile`)

- 多阶段构建优化镜像大小
- 使用 Maven 构建 Spring Boot 应用
- 运行时使用 Eclipse Temurin JRE Alpine
- 包含健康检查和 JVM 容器优化

### 前端 Dockerfile (`Fitness/frontend/Dockerfile`)

- 多阶段构建优化镜像大小
- 使用 Node.js 构建 Vue 应用
- 运行时使用 Nginx Alpine
- 包含 API 代理配置和健康检查

### Docker Compose

- `docker-compose.yml`: 完整环境（包含 MySQL）
- `docker-compose.dev.yml`: 开发环境（使用 H2 内存数据库）

## 使用方法

### 本地运行 Docker

```bash
# 开发环境（H2数据库）
cd Fitness
docker-compose -f docker-compose.dev.yml up --build

# 完整环境（MySQL）
cd Fitness
docker-compose up --build
```

### 访问应用

- 前端: http://localhost:3001 (开发模式) / http://localhost:80 (Docker)
- 后端 API: http://localhost:8080
- H2 Console (开发环境): http://localhost:8080/h2-console

## 配置 Secrets

在 GitHub 仓库设置中配置以下 Secrets（如需要）：

| Secret | 描述 |
|--------|------|
| `TEST_SERVER_HOST` | 测试服务器地址 |
| `TEST_SERVER_USER` | 测试服务器用户名 |
| `TEST_SERVER_SSH_KEY` | SSH 私钥 |

## 配置 Variables

| Variable | 描述 |
|----------|------|
| `SLACK_WEBHOOK_URL` | Slack 通知 Webhook URL（可选） |

## 分支保护规则

建议为 `main` 分支配置以下保护规则：

1. 要求 PR 审核
2. 要求状态检查通过：
   - `test-frontend`
   - `test-backend`
   - `notify-results`
3. 要求分支是最新的

## 故障排除

### 测试失败

1. 查看 Actions 日志获取详细错误信息
2. 下载测试报告 artifacts 进行分析
3. 本地运行测试复现问题

### Docker 构建失败

1. 检查 Dockerfile 语法
2. 确保所有依赖文件存在
3. 检查 .dockerignore 配置

### 部署失败

1. 检查服务器连接配置
2. 验证 Docker 镜像是否正确推送
3. 检查服务器上的 Docker 服务状态
