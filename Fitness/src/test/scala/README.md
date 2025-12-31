# Gatling 性能测试

本目录包含健身管理系统的API性能测试脚本，使用Gatling框架编写。

## 目录结构

```
src/test/scala/
├── gatling.conf                    # Gatling配置文件
├── com/wzl/fitness/gatling/
│   ├── config/
│   │   └── GatlingConfig.scala     # 通用配置（URL、阈值等）
│   └── simulations/
│       ├── AuthApiSimulation.scala           # 认证API测试
│       ├── DashboardApiSimulation.scala      # 仪表盘API测试
│       ├── TrainingApiSimulation.scala       # 训练记录API测试
│       ├── FullApiPerformanceSimulation.scala # 综合性能测试
│       └── ApiResponseTimePropertySimulation.scala # Property 1测试
```

## 运行测试

### 前置条件

1. 确保后端服务已启动并运行在 `http://localhost:8080`
2. 确保测试用户 `admin/admin123` 存在

### 运行所有测试

```bash
cd Fitness
mvn gatling:test
```

### 运行特定测试

```bash
# 运行认证API测试
mvn gatling:test -Dgatling.simulationClass=com.wzl.fitness.gatling.simulations.AuthApiSimulation

# 运行仪表盘API测试
mvn gatling:test -Dgatling.simulationClass=com.wzl.fitness.gatling.simulations.DashboardApiSimulation

# 运行训练记录API测试
mvn gatling:test -Dgatling.simulationClass=com.wzl.fitness.gatling.simulations.TrainingApiSimulation

# 运行综合性能测试
mvn gatling:test -Dgatling.simulationClass=com.wzl.fitness.gatling.simulations.FullApiPerformanceSimulation

# 运行Property 1测试（100并发响应时间<500ms）
mvn gatling:test -Dgatling.simulationClass=com.wzl.fitness.gatling.simulations.ApiResponseTimePropertySimulation
```

### 自定义基础URL

```bash
# 测试其他环境
GATLING_BASE_URL=http://your-server:8080 mvn gatling:test
```

## 测试报告

测试完成后，报告将生成在 `target/gatling/` 目录下。

打开 `target/gatling/<simulation-name>-<timestamp>/index.html` 查看详细报告。

## 性能指标

### Property 1: API响应时间性能

**验证条件：** 在100并发用户负载下，平均响应时间应低于500毫秒

**测试断言：**
- 平均响应时间 < 500ms
- 95%请求响应时间 < 500ms
- 成功率 > 95%

### 测试覆盖的API端点

| 端点 | 方法 | 描述 |
|------|------|------|
| /api/v1/auth/login | POST | 用户登录 |
| /api/v1/auth/me | GET | 获取当前用户信息 |
| /api/v1/dashboard/metrics-overview | GET | 仪表盘指标概览 |
| /api/v1/dashboard/user-stats-overview | GET | 用户统计概览 |
| /api/v1/dashboard/analytics | GET | 分析数据 |
| /api/v1/strength-training | GET | 训练记录列表 |
| /api/v1/strength-training | POST | 添加训练记录 |
| /api/v1/strength-training/stats/max-weight | GET | 最大重量统计 |

## 配置说明

### GatlingConfig.scala

```scala
object Thresholds {
  val maxResponseTime = 500      // 最大响应时间(ms)
  val meanResponseTime = 200     // 平均响应时间(ms)
  val successRate = 95.0         // 成功率(%)
  val requestsPerSecond = 100    // 每秒请求数
}

object LoadConfig {
  val rampUpUsers = 100          // 递增用户数
  val rampUpDuration = 60        // 递增时间(秒)
  val constantUsers = 50         // 恒定用户数
  val constantDuration = 120     // 恒定时间(秒)
}
```

## 故障排除

### 测试失败

1. 检查后端服务是否正常运行
2. 检查测试用户是否存在
3. 检查网络连接
4. 查看Gatling报告中的错误详情

### 响应时间超标

1. 检查数据库连接池配置
2. 检查服务器资源使用情况
3. 考虑添加缓存
4. 优化慢查询
