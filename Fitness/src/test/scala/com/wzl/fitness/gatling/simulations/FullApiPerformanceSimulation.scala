package com.wzl.fitness.gatling.simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import com.wzl.fitness.gatling.config.GatlingConfig
import com.wzl.fitness.gatling.config.GatlingConfig._

/**
 * 完整API性能测试模拟
 * 综合测试所有主要API端点，验证100并发用户下的性能表现
 * 
 * Requirements: 5.3, 5.5 - 测试系统在并发用户下的表现，100并发下响应时间<500ms
 * Property 1: API响应时间性能 - 验证100并发下响应时间<500ms
 */
class FullApiPerformanceSimulation extends Simulation {

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

  // 时间范围参数
  val timeRanges = Array("day", "week", "month")
  val timeRangeFeeder = timeRanges.map(tr => Map("timeRange" -> tr)).circular

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

  // 认证API场景
  val authScenario = scenario("认证API性能测试")
    .exec(
      http("用户登录")
        .post(s"${apiPrefix}/auth/login")
        .headers(commonHeaders)
        .body(StringBody(
          s"""{"username":"${testUser("username")}","password":"${testUser("password")}"}"""
        ))
        .check(status.is(200))
        .check(jsonPath("$.data.accessToken").exists.saveAs("authToken"))
        .check(responseTimeInMillis.lt(Thresholds.maxResponseTime))
    )
    .pause(500.milliseconds)
    .exec(
      http("获取当前用户信息")
        .get(s"${apiPrefix}/auth/me")
        .header("Authorization", "Bearer ${authToken}")
        .headers(commonHeaders)
        .check(status.is(200))
        .check(responseTimeInMillis.lt(Thresholds.maxResponseTime))
    )

  // 仪表盘API场景
  val dashboardScenario = scenario("仪表盘API性能测试")
    .exec(loginAndGetToken)
    .pause(500.milliseconds)
    .feed(timeRangeFeeder)
    .exec(
      http("获取仪表盘指标概览")
        .get(s"${apiPrefix}/dashboard/metrics-overview")
        .header("Authorization", "Bearer ${authToken}")
        .queryParam("timeRange", "${timeRange}")
        .headers(commonHeaders)
        .check(status.is(200))
        .check(responseTimeInMillis.lt(Thresholds.maxResponseTime))
    )
    .exec(
      http("获取用户统计概览")
        .get(s"${apiPrefix}/dashboard/user-stats-overview")
        .header("Authorization", "Bearer ${authToken}")
        .headers(commonHeaders)
        .check(status.is(200))
        .check(responseTimeInMillis.lt(Thresholds.maxResponseTime))
    )
    .exec(
      http("获取分析数据")
        .get(s"${apiPrefix}/dashboard/analytics")
        .header("Authorization", "Bearer ${authToken}")
        .queryParam("timeRange", "${timeRange}")
        .headers(commonHeaders)
        .check(status.is(200))
        .check(responseTimeInMillis.lt(Thresholds.maxResponseTime))
    )
    .exec(
      http("获取最近训练记录")
        .get(s"${apiPrefix}/dashboard/recent-training-records")
        .header("Authorization", "Bearer ${authToken}")
        .headers(commonHeaders)
        .check(status.is(200))
        .check(responseTimeInMillis.lt(Thresholds.maxResponseTime))
    )

  // 训练记录API场景
  val trainingScenario = scenario("训练记录API性能测试")
    .exec(loginAndGetToken)
    .pause(500.milliseconds)
    .exec(
      http("获取训练记录列表")
        .get(s"${apiPrefix}/strength-training")
        .header("Authorization", "Bearer ${authToken}")
        .queryParam("page", "0")
        .queryParam("size", "20")
        .headers(commonHeaders)
        .check(status.is(200))
        .check(responseTimeInMillis.lt(Thresholds.maxResponseTime))
    )
    .pause(500.milliseconds)
    .feed(trainingDataFeeder)
    .exec(
      http("添加训练记录")
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
        .check(responseTimeInMillis.lt(Thresholds.maxResponseTime))
    )
    .exec(
      http("获取最大重量统计")
        .get(s"${apiPrefix}/strength-training/stats/max-weight")
        .header("Authorization", "Bearer ${authToken}")
        .headers(commonHeaders)
        .check(status.is(200))
        .check(responseTimeInMillis.lt(Thresholds.maxResponseTime))
    )

  // 综合场景 - 模拟真实用户行为
  val mixedScenario = scenario("综合API性能测试")
    .exec(loginAndGetToken)
    .pause(1.second)
    .randomSwitch(
      40.0 -> exec(
        // 仪表盘操作
        http("仪表盘-指标概览")
          .get(s"${apiPrefix}/dashboard/metrics-overview")
          .header("Authorization", "Bearer ${authToken}")
          .queryParam("timeRange", "week")
          .headers(commonHeaders)
          .check(status.is(200))
      ),
      30.0 -> exec(
        // 训练记录查询
        http("训练-记录列表")
          .get(s"${apiPrefix}/strength-training")
          .header("Authorization", "Bearer ${authToken}")
          .queryParam("page", "0")
          .queryParam("size", "20")
          .headers(commonHeaders)
          .check(status.is(200))
      ),
      20.0 -> exec(
        // 用户信息
        http("用户-当前信息")
          .get(s"${apiPrefix}/auth/me")
          .header("Authorization", "Bearer ${authToken}")
          .headers(commonHeaders)
          .check(status.is(200))
      ),
      10.0 -> feed(trainingDataFeeder).exec(
        // 添加训练记录
        http("训练-添加记录")
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
    )

  // 100并发用户负载测试 - Property 1验证
  setUp(
    // 认证场景 - 20%负载
    authScenario.inject(
      rampUsers(20).during(30.seconds),
      constantUsersPerSec(2).during(90.seconds)
    ),
    // 仪表盘场景 - 40%负载
    dashboardScenario.inject(
      rampUsers(40).during(30.seconds),
      constantUsersPerSec(4).during(90.seconds)
    ),
    // 训练记录场景 - 40%负载
    trainingScenario.inject(
      rampUsers(40).during(30.seconds),
      constantUsersPerSec(4).during(90.seconds)
    )
  ).protocols(httpProtocol)
   .assertions(
     // Property 1: API响应时间性能 - 100并发下平均响应时间<500ms
     global.responseTime.mean.lt(Thresholds.maxResponseTime),
     // 最大响应时间不超过1秒
     global.responseTime.max.lt(1000),
     // 95%请求响应时间<500ms
     global.responseTime.percentile3.lt(Thresholds.maxResponseTime),
     // 成功率>95%
     global.successfulRequests.percent.gt(Thresholds.successRate),
     // 每秒请求数
     global.requestsPerSec.gt(50.0)
   )
}
