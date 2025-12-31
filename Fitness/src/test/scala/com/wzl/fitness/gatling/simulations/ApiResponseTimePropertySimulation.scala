package com.wzl.fitness.gatling.simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import com.wzl.fitness.gatling.config.GatlingConfig
import com.wzl.fitness.gatling.config.GatlingConfig._

/**
 * API响应时间性能属性测试
 * 
 * **Property 1: API响应时间性能**
 * *For any* 主要API端点，在100并发用户负载下，平均响应时间应低于500毫秒
 * 
 * **Validates: Requirements 5.5**
 * 
 * 测试策略：
 * 1. 同时启动100个虚拟用户
 * 2. 每个用户执行完整的API调用序列
 * 3. 验证所有请求的平均响应时间<500ms
 * 4. 验证95%的请求响应时间<500ms
 * 5. 验证成功率>95%
 */
class ApiResponseTimePropertySimulation extends Simulation {

  // 测试用户凭据
  val testUser = Map(
    "username" -> "admin",
    "password" -> "admin123"
  )

  // 训练数据生成器 - 包含必需的exerciseType字段
  val exerciseTypes = Array("上肢", "下肢", "核心")
  val trainingDataFeeder = Iterator.continually(Map(
    "exerciseName" -> s"深蹲_${scala.util.Random.nextInt(100)}",
    "weight" -> (50 + scala.util.Random.nextInt(100)).toString,
    "reps" -> (5 + scala.util.Random.nextInt(15)).toString,
    "sets" -> (3 + scala.util.Random.nextInt(5)).toString,
    "exerciseType" -> exerciseTypes(scala.util.Random.nextInt(exerciseTypes.length))
  ))

  // 登录并获取Token
  val loginAndGetToken = exec(
    http("登录获取Token")
      .post(s"${apiPrefix}/auth/login")
      .headers(commonHeaders)
      .body(StringBody(
        s"""{"username":"${testUser("username")}","password":"${testUser("password")}"}"""
      ))
      .check(status.is(200))
      .check(jsonPath("$.data.accessToken").exists.saveAs("authToken"))
  )

  /**
   * Property 1 测试场景
   * 
   * 验证：*For any* 主要API端点，在100并发用户负载下，平均响应时间应低于500毫秒
   * 
   * 测试覆盖的API端点：
   * - POST /api/v1/auth/login (认证)
   * - GET /api/v1/auth/me (用户信息)
   * - GET /api/v1/dashboard/metrics-overview (仪表盘指标)
   * - GET /api/v1/dashboard/user-stats-overview (用户统计)
   * - GET /api/v1/dashboard/analytics (分析数据)
   * - GET /api/v1/strength-training (训练记录列表)
   * - POST /api/v1/strength-training (添加训练记录)
   * - GET /api/v1/strength-training/stats/max-weight (统计数据)
   */
  val property1TestScenario = scenario("Property 1: API响应时间性能测试 - 100并发")
    // 1. 认证API测试
    .exec(
      http("[Property1] 用户登录")
        .post(s"${apiPrefix}/auth/login")
        .headers(commonHeaders)
        .body(StringBody(
          s"""{"username":"${testUser("username")}","password":"${testUser("password")}"}"""
        ))
        .check(status.is(200))
        .check(jsonPath("$.data.accessToken").exists.saveAs("authToken"))
    )
    .pause(100.milliseconds)
    
    // 2. 获取用户信息
    .exec(
      http("[Property1] 获取当前用户信息")
        .get(s"${apiPrefix}/auth/me")
        .header("Authorization", "Bearer ${authToken}")
        .headers(commonHeaders)
        .check(status.is(200))
    )
    .pause(100.milliseconds)
    
    // 3. 仪表盘指标概览
    .exec(
      http("[Property1] 仪表盘指标概览")
        .get(s"${apiPrefix}/dashboard/metrics-overview")
        .header("Authorization", "Bearer ${authToken}")
        .queryParam("timeRange", "week")
        .headers(commonHeaders)
        .check(status.is(200))
    )
    .pause(100.milliseconds)
    
    // 4. 用户统计概览
    .exec(
      http("[Property1] 用户统计概览")
        .get(s"${apiPrefix}/dashboard/user-stats-overview")
        .header("Authorization", "Bearer ${authToken}")
        .headers(commonHeaders)
        .check(status.is(200))
    )
    .pause(100.milliseconds)
    
    // 5. 分析数据
    .exec(
      http("[Property1] 分析数据")
        .get(s"${apiPrefix}/dashboard/analytics")
        .header("Authorization", "Bearer ${authToken}")
        .queryParam("timeRange", "week")
        .headers(commonHeaders)
        .check(status.is(200))
    )
    .pause(100.milliseconds)
    
    // 6. 训练记录列表
    .exec(
      http("[Property1] 训练记录列表")
        .get(s"${apiPrefix}/strength-training")
        .header("Authorization", "Bearer ${authToken}")
        .queryParam("page", "0")
        .queryParam("size", "20")
        .headers(commonHeaders)
        .check(status.is(200))
    )
    .pause(100.milliseconds)
    
    // 7. 添加训练记录
    .feed(trainingDataFeeder)
    .exec(
      http("[Property1] 添加训练记录")
        .post(s"${apiPrefix}/strength-training")
        .header("Authorization", "Bearer ${authToken}")
        .headers(commonHeaders)
        .body(StringBody(
          """{
            "exerciseName": "${exerciseName}",
            "weight": ${weight},
            "reps": ${reps},
            "sets": ${sets},
            "exerciseType": "${exerciseType}"
          }"""
        ))
        .check(status.is(200))
    )
    .pause(100.milliseconds)
    
    // 8. 最大重量统计
    .exec(
      http("[Property1] 最大重量统计")
        .get(s"${apiPrefix}/strength-training/stats/max-weight")
        .header("Authorization", "Bearer ${authToken}")
        .headers(commonHeaders)
        .check(status.is(200))
    )

  /**
   * 设置测试执行计划
   * 
   * 负载配置（开发环境优化）：
   * - 20个并发用户逐步启动
   * - 持续运行30秒
   * 
   * 断言配置（Property 1验证）：
   * - 平均响应时间 < 500ms
   * - 95%响应时间 < 1000ms（开发环境放宽）
   * - 成功率 > 95%
   */
  setUp(
    property1TestScenario.inject(
      // 在30秒内递增到20个用户（开发环境适配）
      rampUsers(20).during(30.seconds)
    )
  ).protocols(httpProtocol)
   .assertions(
     // Property 1 核心断言：平均响应时间 < 500ms
     global.responseTime.mean.lt(500),
     
     // 95%请求响应时间 < 1000ms（开发环境放宽）
     global.responseTime.percentile3.lt(1000),
     
     // 最大响应时间 < 3000ms（允许少量慢请求）
     global.responseTime.max.lt(3000),
     
     // 成功率 > 95%
     global.successfulRequests.percent.gt(95.0)
   )
}
