# MySQL 数据库配置指南

本文档介绍如何配置 MySQL 数据库以运行 AFitness 健身管理系统。

## 目录

1. [环境要求](#环境要求)
2. [MySQL 安装](#mysql-安装)
3. [数据库初始化](#数据库初始化)
4. [应用配置](#应用配置)
5. [启动应用](#启动应用)
6. [常见问题](#常见问题)

---

## 环境要求

- MySQL 8.0 或更高版本
- Java 17 或更高版本
- Maven 3.6+

---

## MySQL 安装

### Windows 安装

1. 下载 MySQL Installer: https://dev.mysql.com/downloads/installer/
2. 运行安装程序，选择 "Developer Default" 或 "Server only"
3. 设置 root 密码（记住这个密码）
4. 完成安装

### 使用 Docker 安装（推荐）

```bash
# 启动 MySQL 容器
docker run -d \
  --name fitness-mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_DATABASE=fitness_db \
  -e MYSQL_USER=fitness \
  -e MYSQL_PASSWORD=fitness123 \
  mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci
```

### 使用 Docker Compose（完整环境）

```bash
cd Fitness
docker-compose up -d mysql
```

---

## 数据库初始化

### 方式一：手动创建数据库

连接到 MySQL 并执行以下命令：

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS fitness_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 创建用户（可选）
CREATE USER IF NOT EXISTS 'fitness'@'%' IDENTIFIED BY 'fitness123';
GRANT ALL PRIVILEGES ON fitness_db.* TO 'fitness'@'%';
FLUSH PRIVILEGES;
```

### 方式二：使用初始化脚本

```bash
# 使用 MySQL 命令行
mysql -u root -p < scripts/sql/mysql_database_init.sql

# 或者在 MySQL 客户端中执行
source scripts/sql/mysql_database_init.sql
```

### 方式三：让 JPA 自动创建表

应用配置中设置 `spring.jpa.hibernate.ddl-auto=update`，首次启动时会自动创建所有表。

---

## 应用配置

### 配置文件位置

- 主配置: `src/main/resources/application.properties`
- MySQL 配置: `src/main/resources/application-mysql.properties`

### 修改数据库连接信息

编辑 `application-mysql.properties` 或 `application.properties`：

```properties
# 数据库连接URL
spring.datasource.url=jdbc:mysql://localhost:3306/fitness_db?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true

# 数据库用户名和密码
spring.datasource.username=root
spring.datasource.password=你的密码
```

### 配置参数说明

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `spring.datasource.url` | 数据库连接URL | localhost:3306/fitness_db |
| `spring.datasource.username` | 数据库用户名 | root |
| `spring.datasource.password` | 数据库密码 | 123456 |
| `spring.jpa.hibernate.ddl-auto` | 表结构管理策略 | update |

### ddl-auto 选项说明

- `create`: 每次启动删除并重建所有表（会丢失数据）
- `update`: 自动更新表结构，保留数据（推荐开发环境）
- `validate`: 仅验证表结构，不做修改（推荐生产环境）
- `none`: 不做任何操作

---

## 启动应用

### 方式一：使用 Maven 启动

```bash
cd Fitness

# 使用 MySQL 配置启动
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

### 方式二：使用 JAR 包启动

```bash
# 打包
mvn clean package -DskipTests

# 启动（使用 MySQL）
java -jar target/fitness-0.0.1-SNAPSHOT.jar --spring.profiles.active=mysql

# 或者指定数据库参数
java -jar target/fitness-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=mysql \
  --spring.datasource.url=jdbc:mysql://localhost:3306/fitness_db \
  --spring.datasource.username=root \
  --spring.datasource.password=你的密码
```

### 方式三：使用 Docker Compose 启动完整环境

```bash
cd Fitness
docker-compose up -d
```

---

## 验证部署

### 1. 检查健康状态

```bash
curl http://localhost:8080/actuator/health
```

预期响应：
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL"
      }
    }
  }
}
```

### 2. 测试登录

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Test123!"}'
```

### 3. 访问 Swagger UI

打开浏览器访问: http://localhost:8080/swagger-ui.html

---

## 默认账户

系统启动时会自动创建管理员账户：

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | Test123! | ADMIN |

**注意**: 首次登录后请修改默认密码！

---

## 常见问题

### 1. 连接被拒绝

**错误**: `Communications link failure`

**解决方案**:
- 确认 MySQL 服务已启动
- 检查端口 3306 是否被占用
- 确认防火墙允许 3306 端口

```bash
# Windows 检查 MySQL 服务
sc query mysql

# 检查端口
netstat -an | findstr 3306
```

### 2. 访问被拒绝

**错误**: `Access denied for user 'root'@'localhost'`

**解决方案**:
- 确认用户名和密码正确
- 检查用户权限

```sql
-- 重置密码
ALTER USER 'root'@'localhost' IDENTIFIED BY '新密码';
FLUSH PRIVILEGES;
```

### 3. 中文乱码

**解决方案**:
- 确保数据库使用 utf8mb4 字符集
- 连接URL包含 `useUnicode=true&characterEncoding=UTF-8`

```sql
-- 检查字符集
SHOW VARIABLES LIKE 'character%';

-- 修改数据库字符集
ALTER DATABASE fitness_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 4. 时区问题

**错误**: `The server time zone value 'xxx' is unrecognized`

**解决方案**:
- 连接URL添加 `serverTimezone=Asia/Shanghai`
- 或在 MySQL 中设置时区

```sql
SET GLOBAL time_zone = '+8:00';
```

### 5. Public Key Retrieval 错误

**错误**: `Public Key Retrieval is not allowed`

**解决方案**:
- 连接URL添加 `allowPublicKeyRetrieval=true`

---

## 生产环境建议

1. **修改默认密码**: 使用强密码替换默认的 `123456`
2. **限制访问**: 不要使用 root 用户，创建专用数据库用户
3. **启用 SSL**: 生产环境建议启用 SSL 连接
4. **定期备份**: 配置自动备份策略
5. **监控**: 启用数据库监控和告警

```sql
-- 创建专用用户
CREATE USER 'fitness_app'@'%' IDENTIFIED BY '强密码';
GRANT SELECT, INSERT, UPDATE, DELETE ON fitness_db.* TO 'fitness_app'@'%';
FLUSH PRIVILEGES;
```

---

## 相关文件

- `application.properties` - 主配置文件
- `application-mysql.properties` - MySQL 专用配置
- `application-h2.properties` - H2 内存数据库配置（测试用）
- `scripts/sql/mysql_database_init.sql` - 数据库初始化脚本
- `docker/mysql/my.cnf` - MySQL 配置文件
- `docker-compose.yml` - Docker 部署配置
