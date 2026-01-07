# Fitness 系统部署指南

本文档提供 Fitness 力量训练负荷与恢复监控系统的完整部署指南，包括 Docker 部署、环境配置、启动脚本使用和故障排除。

## 目录

- [系统要求](#系统要求)
- [Docker 部署](#docker-部署)
- [环境变量配置](#环境变量配置)
- [生产环境配置清单](#生产环境配置清单)
- [启动脚本使用说明](#启动脚本使用说明)
- [故障排除指南](#故障排除指南)

---

## 系统要求

### 硬件要求

| 组件 | 最低配置 | 推荐配置 |
|------|----------|----------|
| CPU | 2 核 | 4 核+ |
| 内存 | 4 GB | 8 GB+ |
| 磁盘 | 20 GB | 50 GB+ |

### 软件要求

| 软件 | 版本要求 |
|------|----------|
| Docker | 20.10+ |
| Docker Compose | 2.0+ |
| Node.js | 16+ (本地开发) |
| JDK | 17+ (本地开发) |
| MySQL | 8.0+ |

---

## Docker 部署

### 快速开始

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd Fitness
   ```

2. **使用 Docker Compose 启动**
   ```bash
   # 生产环境（使用 MySQL）
   docker-compose up -d
   
   # 开发环境（使用 H2 内存数据库）
   docker-compose -f docker-compose.dev.yml up -d
   ```

3. **验证服务状态**
   ```bash
   docker-compose ps
   ```

### docker-compose.yml 配置说明

```yaml
version: '3.8'

services:
  # 后端服务配置
  backend:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: fitness-backend
    ports:
      - "8080:8080"              # 后端 API 端口
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/fitness?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
      - SPRING_DATASOURCE_USERNAME=fitness
      - SPRING_DATASOURCE_PASSWORD=fitness123
      - JWT_SECRET=fitness-jwt-secret-key-for-development-only
    depends_on:
      mysql:
        condition: service_healthy  # 等待 MySQL 健康检查通过
    networks:
      - fitness-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  # 前端服务配置
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: fitness-frontend
    ports:
      - "80:80"                   # 前端访问端口
    depends_on:
      - backend
    networks:
      - fitness-network
    restart: unless-stopped

  # MySQL 数据库配置
  mysql:
    image: mysql:8.0
    container_name: fitness-mysql
    ports:
      - "3306:3306"               # 数据库端口
    environment:
      - MYSQL_ROOT_PASSWORD=root123
      - MYSQL_DATABASE=fitness
      - MYSQL_USER=fitness
      - MYSQL_PASSWORD=fitness123
      - LANG=C.UTF-8
    volumes:
      - mysql-data:/var/lib/mysql                                    # 数据持久化
      - ./scripts/sql/mysql_database_init.sql:/docker-entrypoint-initdb.d/init.sql:ro  # 初始化脚本
      - ./docker/mysql/my.cnf:/etc/mysql/conf.d/my.cnf:ro           # MySQL 配置
    networks:
      - fitness-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-proot123"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s
    command: >
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci
      --init-connect='SET NAMES utf8mb4'
      --skip-character-set-client-handshake

networks:
  fitness-network:
    driver: bridge

volumes:
  mysql-data:
    driver: local
```

### 服务说明

| 服务 | 端口 | 说明 |
|------|------|------|
| backend | 8080 | Spring Boot 后端 API |
| frontend | 80 | Vue 3 用户端前端 |
| mysql | 3306 | MySQL 8.0 数据库 |

### 常用 Docker 命令

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看服务日志
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql

# 停止所有服务
docker-compose down

# 停止并删除数据卷（清除所有数据）
docker-compose down -v

# 重新构建镜像
docker-compose build --no-cache

# 重启单个服务
docker-compose restart backend
```

---

## 环境变量配置

### 后端环境变量

| 变量名 | 说明 | 默认值 | 必填 |
|--------|------|--------|------|
| `SPRING_PROFILES_ACTIVE` | 激活的配置文件 | `dev` | 否 |
| `SPRING_DATASOURCE_URL` | 数据库连接 URL | - | 是 |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户名 | - | 是 |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码 | - | 是 |
| `JWT_SECRET` | JWT 签名密钥（至少 64 字符） | - | 是 |
| `JWT_EXPIRATION` | JWT 过期时间（秒） | `86400` | 否 |
| `SERVER_PORT` | 服务端口 | `8080` | 否 |

### MySQL 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `MYSQL_ROOT_PASSWORD` | Root 用户密码 | - |
| `MYSQL_DATABASE` | 默认数据库名 | `fitness` |
| `MYSQL_USER` | 应用用户名 | `fitness` |
| `MYSQL_PASSWORD` | 应用用户密码 | - |

### 使用 .env 文件

创建 `.env` 文件管理环境变量：

```bash
# .env 文件示例
# 数据库配置
MYSQL_ROOT_PASSWORD=your_secure_root_password
MYSQL_DATABASE=fitness
MYSQL_USER=fitness
MYSQL_PASSWORD=your_secure_password

# 后端配置
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=your-very-long-and-secure-jwt-secret-key-at-least-64-characters-long

# Redis 配置（可选）
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password
```

然后在 docker-compose.yml 中引用：

```yaml
services:
  backend:
    environment:
      - SPRING_DATASOURCE_PASSWORD=${MYSQL_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
```

---

## 生产环境配置清单

### 数据库配置

```properties
# 生产环境数据库配置
spring.datasource.url=jdbc:mysql://your-db-host:3306/fitness_db?useSSL=true&serverTimezone=Asia/Shanghai
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# 连接池优化
spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
```

### 安全配置

```properties
# JWT 配置（生产环境必须使用强密钥）
jwt.secret=${JWT_SECRET}
jwt.expiration=86400
jwt.refresh-expiration=604800

# CORS 配置（限制允许的域名）
spring.mvc.cors.allowed-origins=https://your-domain.com
spring.mvc.cors.allowed-methods=GET,POST,PUT,DELETE
spring.mvc.cors.allow-credentials=true
```

### 缓存配置

```properties
# Redis 缓存（推荐生产环境使用）
spring.data.redis.host=${REDIS_HOST}
spring.data.redis.port=${REDIS_PORT}
spring.data.redis.password=${REDIS_PASSWORD}
spring.data.redis.database=0
```

### 日志配置

```properties
# 生产环境日志级别
logging.level.root=WARN
logging.level.com.wzl.fitness=INFO
logging.level.org.hibernate.SQL=WARN

# 日志文件配置
logging.file.name=/var/log/fitness/application.log
logging.file.max-size=100MB
logging.file.max-history=30
```

### 监控配置

```properties
# Actuator 端点配置
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when-authorized
management.prometheus.metrics.export.enabled=true
```

---


## 启动脚本使用说明

系统提供 PowerShell 脚本用于快速启动和停止前端服务。

### start-all.ps1 - 启动脚本

启动用户端前端（端口 3001）和管理端前端（端口 3002）。

**使用方法：**

```powershell
# 在 Fitness 目录下执行
.\start-all.ps1
```

**脚本功能：**

1. 检查 Node.js 和 npm 环境
2. 验证前端目录存在
3. 启动用户端前端服务（http://localhost:3001）
4. 启动管理端前端服务（http://localhost:3002）
5. 检查服务启动状态
6. 显示服务日志

**输出示例：**

```
========================================
     健身管理系统前端启动脚本
========================================
项目目录: C:\Projects\Fitness
检查环境依赖...
Node.js版本: v18.17.0
NPM版本: 9.6.7
========================================
正在启动用户端前端 (Port 3001)...
正在启动管理端前端 (Port 3002)...
========================================
访问地址:
用户端前端: http://localhost:3001
管理端前端: http://localhost:3002
========================================
默认账户:
普通用户: user / user123
管理员: admin / admin123
========================================
```

### stop-all.ps1 - 停止脚本

停止所有前端服务并释放端口。

**使用方法：**

```powershell
# 在 Fitness 目录下执行
.\stop-all.ps1
```

**脚本功能：**

1. 查找并停止所有 Vite 开发服务器进程
2. 检查端口 3001 和 3002 占用情况
3. 强制释放被占用的端口
4. 显示停止状态

**输出示例：**

```
========================================
     停止健身管理系统前端服务
========================================
正在停止前端服务...
停止Node.js/Vite进程 (PID: 12345)
前端服务已停止
检查端口占用情况...
端口3001已释放 (用户端前端)
端口3002已释放 (管理端前端)
========================================
前端服务停止完成!
========================================
```

### 后端启动脚本

后端服务可通过以下方式启动：

**开发环境：**

```powershell
# 使用 Maven
cd Fitness
mvn spring-boot:run

# 或使用 Maven Wrapper
.\mvnw spring-boot:run
```

**生产环境：**

```powershell
# 构建 JAR 包
mvn clean package -DskipTests

# 运行 JAR 包
java -jar target/fitness-*.jar --spring.profiles.active=prod
```

### 完整系统启动流程

1. **启动数据库**
   ```powershell
   # 使用 Docker
   docker-compose up -d mysql
   
   # 或启动本地 MySQL 服务
   net start mysql
   ```

2. **启动后端服务**
   ```powershell
   cd Fitness
   mvn spring-boot:run
   ```

3. **启动前端服务**
   ```powershell
   cd Fitness
   .\start-all.ps1
   ```

4. **验证服务**
   - 后端 API: http://localhost:8080/actuator/health
   - 用户端前端: http://localhost:3001
   - 管理端前端: http://localhost:3002
   - Swagger 文档: http://localhost:8080/swagger-ui/index.html

---

## 故障排除指南

### 数据库连接问题

#### 问题：无法连接到 MySQL 数据库

**症状：**
```
com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
```

**解决方案：**

1. **检查 MySQL 服务状态**
   ```powershell
   # Windows
   Get-Service -Name "MySQL*"
   
   # Docker
   docker-compose ps mysql
   ```

2. **验证连接参数**
   ```powershell
   # 测试数据库连接
   mysql -h localhost -P 3306 -u fitness -p
   ```

3. **检查防火墙设置**
   ```powershell
   # 检查端口是否开放
   Test-NetConnection -ComputerName localhost -Port 3306
   ```

4. **检查 Docker 网络**
   ```bash
   docker network inspect fitness_fitness-network
   ```

#### 问题：数据库字符集乱码

**解决方案：**

确保 MySQL 配置使用 UTF-8：

```sql
-- 检查字符集
SHOW VARIABLES LIKE 'character%';
SHOW VARIABLES LIKE 'collation%';

-- 修改数据库字符集
ALTER DATABASE fitness_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 端口冲突问题

#### 问题：端口已被占用

**症状：**
```
Error: listen EADDRINUSE: address already in use :::3001
```

**解决方案：**

1. **查找占用端口的进程**
   ```powershell
   # Windows
   netstat -ano | findstr :3001
   Get-NetTCPConnection -LocalPort 3001
   
   # Linux/Mac
   lsof -i :3001
   ```

2. **终止占用进程**
   ```powershell
   # Windows - 使用 PID
   Stop-Process -Id <PID> -Force
   
   # 或使用停止脚本
   .\stop-all.ps1
   ```

3. **修改服务端口**
   
   前端端口配置（vite.config.js）：
   ```javascript
   export default defineConfig({
     server: {
       port: 3001,  // 修改为其他端口
     }
   })
   ```

### JWT 认证问题

#### 问题：登录返回 500 错误

**症状：**
```
io.jsonwebtoken.security.WeakKeyException: The signing key's size is 328 bits
```

**解决方案：**

JWT 密钥长度必须至少 512 位（64 字符）：

```properties
# application.properties
jwt.secret=mySecretKey123456789012345678901234567890mySecretKey123456789012345678901234567890
```

#### 问题：Token 过期

**解决方案：**

调整 Token 过期时间：

```properties
# 24 小时（秒）
jwt.expiration=86400

# 7 天刷新 Token
jwt.refresh-expiration=604800
```

### Docker 相关问题

#### 问题：容器启动失败

**诊断步骤：**

```bash
# 查看容器日志
docker-compose logs backend
docker-compose logs mysql

# 检查容器状态
docker-compose ps

# 进入容器调试
docker exec -it fitness-backend /bin/sh
```

#### 问题：镜像构建失败

**解决方案：**

```bash
# 清理 Docker 缓存
docker system prune -a

# 重新构建
docker-compose build --no-cache
```

#### 问题：数据卷权限问题

**解决方案：**

```bash
# 检查卷权限
docker volume inspect fitness_mysql-data

# 重置卷
docker-compose down -v
docker-compose up -d
```

### 前端构建问题

#### 问题：npm install 失败

**解决方案：**

```powershell
# 清理缓存
npm cache clean --force

# 删除 node_modules 重新安装
Remove-Item -Recurse -Force node_modules
Remove-Item package-lock.json
npm install
```

#### 问题：Vite 构建内存不足

**解决方案：**

```powershell
# 增加 Node.js 内存限制
$env:NODE_OPTIONS="--max-old-space-size=4096"
npm run build
```

### 性能问题

#### 问题：API 响应缓慢

**诊断步骤：**

1. **检查数据库连接池**
   ```
   GET http://localhost:8080/actuator/metrics/hikaricp.connections.active
   ```

2. **检查 JVM 内存**
   ```
   GET http://localhost:8080/actuator/metrics/jvm.memory.used
   ```

3. **启用慢查询日志**
   ```properties
   logging.level.org.hibernate.SQL=DEBUG
   logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
   ```

#### 问题：内存使用过高

**解决方案：**

调整 JVM 参数：

```bash
# Dockerfile 或启动命令
java -XX:+UseContainerSupport \
     -XX:MaxRAMPercentage=75.0 \
     -XX:InitialRAMPercentage=50.0 \
     -jar app.jar
```

### 日志查看

#### 后端日志位置

```
Fitness/logs/
├── fitness-system.log          # 主日志
├── fitness-system-error.log    # 错误日志
├── fitness-system-json.log     # JSON 格式日志
└── fitness-system-audit.log    # 审计日志
```

#### 实时查看日志

```powershell
# Windows PowerShell
Get-Content -Path "logs/fitness-system.log" -Wait -Tail 100

# Docker
docker-compose logs -f backend
```

---

## 健康检查端点

系统提供以下健康检查端点：

| 端点 | 说明 |
|------|------|
| `/actuator/health` | 整体健康状态 |
| `/actuator/health/db` | 数据库连接状态 |
| `/actuator/health/diskSpace` | 磁盘空间状态 |
| `/actuator/info` | 应用信息 |
| `/actuator/metrics` | 性能指标 |
| `/actuator/prometheus` | Prometheus 格式指标 |

**示例请求：**

```bash
curl http://localhost:8080/actuator/health
```

**响应示例：**

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 107374182400,
        "free": 53687091200,
        "threshold": 10485760
      }
    }
  }
}
```

---

## 联系支持

如遇到本文档未涵盖的问题，请：

1. 查看项目 README.md 获取更多信息
2. 检查 GitHub Issues 是否有类似问题
3. 提交新的 Issue 并附上详细的错误日志
