# 力量训练负荷与恢复监控系统

基于 Spring Boot 3.2.5 + Vue 3 的智能健身管理平台，专注于力量训练的负荷计算与恢复状态监控。

## 项目简介

本系统是一个全栈式的健身管理应用，帮助用户科学地记录训练数据、计算训练负荷、评估恢复状态，并提供个性化的训练建议。系统采用前后端分离架构，支持PWA离线使用，适配移动端和桌面端。

## 功能特性

### 用户管理
- 用户注册、登录、登出
- JWT令牌认证
- 基于角色的权限控制（RBAC）
- 登录失败锁定机制

### 训练数据管理
- 力量训练记录（动作、重量、组数、次数、RPE）
- 有氧训练记录（类型、时长、距离、心率）
- 训练历史查询与统计
- 训练计划制定与管理

### 负荷计算
- 训练量计算（重量 × 组数 × 次数）
- 1RM估算（Epley、Brzycki、Lombardi公式）
- 周/月训练负荷趋势分析

### 恢复监控
- 多维度恢复评估（睡眠质量、肌肉酸痛、疲劳程度）
- 恢复评分计算（0-100分）
- 基于恢复状态的训练建议

### 营养管理
- 每日营养记录（蛋白质、碳水、脂肪、热量）
- 营养目标设置
- 营养摄入趋势分析

### 数据可视化
- 训练数据仪表盘
- ECharts图表展示
- 进步趋势追踪

### 其他特性
- PWA离线支持
- 移动端手势操作
- 主题切换（深色/浅色）
- API限流保护
- 操作审计日志

## 技术栈

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.5 | 核心框架 |
| Spring Security | - | 安全认证 |
| Spring Data JPA | - | 数据访问 |
| MySQL | 8.0 | 生产数据库 |
| H2 | - | 开发数据库 |
| JWT | 0.11.5 | 令牌认证 |
| Micrometer | - | 监控指标 |
| Swagger | 2.2.0 | API文档 |

### 前端
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.x | 核心框架 |
| Vite | - | 构建工具 |
| Pinia | - | 状态管理 |
| Vue Router | - | 路由管理 |
| Element Plus | - | 桌面端UI |
| Vant | - | 移动端UI |
| ECharts | - | 数据可视化 |
| Axios | - | HTTP客户端 |

### 测试
| 技术 | 说明 |
|------|------|
| JUnit 5 | 后端单元测试 |
| jqwik | 属性测试 |
| Gatling | 性能测试 |
| Vitest | 前端单元测试 |
| Playwright | E2E测试 |

## 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0（生产环境）

## 快速开始

### 1. 克隆项目
```bash
git clone <repository-url>
cd AFitness
```

### 2. 数据库配置

#### 方式一：使用 H2 内存数据库（推荐新手）
无需安装任何数据库，直接启动即可：
```bash
cd Fitness
.\start-h2.ps1
# 或者
java -jar target/fitness-0.0.1-SNAPSHOT.jar --spring.profiles.active=h2
```

#### 方式二：使用 MySQL 数据库（生产环境）

1. 安装并启动 MySQL 8.0+
2. 创建数据库：
```sql
CREATE DATABASE fitness_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
3. 修改配置文件 `src/main/resources/application-mysql.properties`：
```properties
spring.datasource.username=你的用户名
spring.datasource.password=你的密码
```
4. 启动应用：
```bash
.\start-mysql.ps1
# 或者
java -jar target/fitness-0.0.1-SNAPSHOT.jar --spring.profiles.active=mysql
```

#### 方式三：使用 Docker Compose（完整环境）
```bash
cd Fitness
docker-compose up -d
```

详细配置请参考：[MySQL 配置指南](docs/MYSQL_SETUP.md)

### 3. 启动后端
```bash
cd Fitness

# 使用H2数据库（开发模式）
mvn spring-boot:run -Dspring-boot.run.profiles=h2

# 使用MySQL数据库（生产模式）
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

### 4. 启动前端
```bash
cd Fitness/frontend
npm install
npm run dev
```

### 5. 访问应用
| 服务 | 地址 |
|------|------|
| 前端应用 | http://localhost:3000 |
| 后端API | http://localhost:8080 |
| Swagger文档 | http://localhost:8080/swagger-ui.html |
| 健康检查 | http://localhost:8080/actuator/health |
| Prometheus指标 | http://localhost:8080/actuator/prometheus |
| H2控制台 (仅H2模式) | http://localhost:8080/h2-console |

### 6. 默认账户
| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | Test123! | 管理员 |

## 项目结构

```
AFitness/
├── Fitness/                          # 主项目目录
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/wzl/fitness/
│   │   │   │   ├── controller/       # REST控制器
│   │   │   │   ├── service/          # 业务逻辑层
│   │   │   │   ├── repository/       # 数据访问层
│   │   │   │   ├── entity/           # JPA实体类
│   │   │   │   ├── dto/              # 数据传输对象
│   │   │   │   ├── config/           # 配置类
│   │   │   │   ├── security/         # 安全相关
│   │   │   │   ├── exception/        # 异常处理
│   │   │   │   └── util/             # 工具类
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── application-*.properties
│   │   └── test/                     # 测试代码
│   │       ├── java/                 # Java测试
│   │       └── scala/                # Gatling性能测试
│   ├── frontend/                     # 前端项目
│   │   ├── src/
│   │   │   ├── views/                # 页面组件
│   │   │   ├── components/           # 通用组件
│   │   │   ├── stores/               # Pinia状态
│   │   │   ├── api/                  # API调用
│   │   │   ├── composables/          # 组合式函数
│   │   │   ├── utils/                # 工具函数
│   │   │   └── router/               # 路由配置
│   │   ├── e2e/                      # E2E测试
│   │   └── public/                   # 静态资源
│   ├── docs/                         # 项目文档
│   │   ├── API_DOC.md               # API文档
│   │   ├── Usage_Guide.md           # 使用指南
│   │   └── Graduation_Design.md     # 毕业设计文档
│   ├── scripts/sql/                  # SQL脚本
│   ├── database_design.md            # 数据库设计
│   ├── system_detailed_design.md     # 系统设计
│   ├── Fitness_PRD.md               # 产品需求文档
│   ├── Dockerfile                    # Docker配置
│   └── docker-compose.yml            # Docker Compose配置
├── .github/workflows/                # CI/CD配置
├── GraduationDesignDoc.md           # 毕业设计总文档
└── README.md                         # 项目说明
```

## API接口

### 认证接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/auth/register | 用户注册 |
| POST | /api/v1/auth/login | 用户登录 |
| POST | /api/v1/auth/refresh | 刷新令牌 |
| POST | /api/v1/auth/logout | 用户登出 |

### 训练接口
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/training/records | 获取训练记录 |
| POST | /api/v1/training/records | 创建训练记录 |
| GET | /api/v1/strength-training | 获取力量训练数据 |
| POST | /api/v1/strength-training | 创建力量训练数据 |

### 负荷与恢复接口
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/load-recovery/load | 获取训练负荷 |
| GET | /api/v1/load-recovery/recovery | 获取恢复状态 |
| GET | /api/v1/load-recovery/one-rep-max | 计算1RM |

### 营养接口
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/nutrition | 获取营养记录 |
| POST | /api/v1/nutrition | 创建营养记录 |
| GET | /api/v1/nutrition/goals | 获取营养目标 |

详细API文档请参考 [API_DOC.md](Fitness/docs/API_DOC.md) 或访问 Swagger UI。

## 运行测试

### 后端测试
```bash
cd Fitness

# 运行所有测试
mvn test

# 运行单元测试
mvn test -Dtest=*Test

# 运行属性测试
mvn test -Dtest=*PropertyTest

# 运行性能测试
mvn gatling:test
```

### 前端测试
```bash
cd Fitness/frontend

# 运行单元测试
npm run test:unit

# 运行测试并生成覆盖率报告
npm run test:coverage

# 运行E2E测试
npm run test:e2e
```

## Docker部署

### 使用Docker Compose
```bash
cd Fitness
docker-compose up -d
```

### 单独构建镜像
```bash
# 构建后端镜像
docker build -t fitness-backend .

# 构建前端镜像
cd frontend
docker build -t fitness-frontend .
```

## 文档索引

| 文档 | 说明 |
|------|------|
| [API文档](Fitness/docs/API_DOC.md) | REST API接口说明 |
| [使用指南](Fitness/docs/Usage_Guide.md) | 系统使用说明 |
| [数据库设计](Fitness/database_design.md) | 数据库表结构设计 |
| [系统设计](Fitness/system_detailed_design.md) | 系统架构与详细设计 |
| [产品需求](Fitness/Fitness_PRD.md) | 产品需求文档 |
| [启动指南](Fitness/启动指南.md) | 中文启动说明 |

## 许可证

本项目仅供学习和研究使用。
