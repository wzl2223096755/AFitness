package com.wzl.fitness.gatling.simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import com.wzl.fitness.gatling.config.GatlingConfig
import com.wzl.fitness.gatling.config.GatlingConfig._

/**
 * 仪表盘API性能测试模拟
 * 测试仪表盘相关的数据获取接口
 * 
 * Requirements: 5.2 - 测试主要API端点的响应时间
 */
class DashboardApiSimulation extends Simulation {

  // 测试用户凭据
  val testUser = Map(
    "username" -> "admin",
    "password" -> "admin123"
  )

  // 时间范围参数
  val timeRanges = Array("day", "week", "month", "year")
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

  // 仪表盘指标概览场景
  val metricsOverviewScenario = scenario("仪表盘指标概览性能测试")
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
        .check(jsonPath("$.code").is("200"))
        .check(responseTimeInMillis.lt(Thresholds.maxResponseTime))
    )

  // 用户统计概览场景
  val userStatsScenario = scenario("用户统计概览性能测试")
    .exec(loginAndGetToken)
    .pause(500.milliseconds)
    .exec(
      http("获取用户统计概览")
        .get(s"${apiPrefix}/dashboard/user-stats-overview")
        .header("Authorization", "Bearer ${authToken}")
        .headers(commonHeaders)
        .check(status.is(200))
        .check(jsonPath("$.code").is("200"))
        .check(responseTimeInMillis.lt(Thresholds.maxResponseTime))
    )

  // 分析数据场景
  val analyticsScenario = scenario("分析数据性能测试")
    .exec(loginAndGetToken)
    .pause(500.milliseconds)
    .feed(timeRangeFeeder)
    .exec(
      http("获取分析数据")
        .get(s"${apiPrefix}/dashboard/analytics")
        .header("Authorization", "Bearer ${authToken}")
        .queryParam("timeRange", "${timeRange}")
        .headers(commonHeaders)
        .check(status.is(200))
        .check(jsonPath("$.code").is("200"))
        .check(responseTimeInMillis.lt(Thresholds.maxResponseTime))
    )

  // 最近训练记录场景
  val recentRecordsScenario = scenario("最近训练记录性能测试")
    .exec(loginAndGetToken)
    .pause(500.milliseconds)
    .exec(
      http("获取最近训练记录")
        .get(s"${apiPrefix}/dashboard/recent-training-records")
        .header("Authorization", "Bearer ${authToken}")
        .headers(commonHeaders)
        .check(status.is(200))
        .check(jsonPath("$.code").is("200"))
        .check(responseTimeInMillis.lt(Thresholds.maxResponseTime))
    )

  // 综合仪表盘场景 - 模拟用户打开仪表盘页面
  val fullDashboardScenario = scenario("完整仪表盘加载性能测试")
    .exec(loginAndGetToken)
    .pause(500.milliseconds)
    .exec(
      http("获取仪表盘指标概览")
        .get(s"${apiPrefix}/dashboard/metrics-overview")
        .header("Authorization", "Bearer ${authToken}")
        .queryParam("timeRange", "week")
        .headers(commonHeaders)
        .check(status.is(200))
    )
    .exec(
      http("获取用户统计概览")
        .get(s"${apiPrefix}/dashboard/user-stats-overview")
        .header("Authorization", "Bearer ${authToken}")
        .headers(commonHeaders)
        .check(status.is(200))
    )
    .exec(
      http("获取分析数据")
        .get(s"${apiPrefix}/dashboard/analytics")
        .header("Authorization", "Bearer ${authToken}")
        .queryParam("timeRange", "week")
        .headers(commonHeaders)
        .check(status.is(200))
    )
    .exec(
      http("获取最近训练记录")
        .get(s"${apiPrefix}/dashboard/recent-training-records")
        .header("Authorization", "Bearer ${authToken}")
        .headers(commonHeaders)
        .check(status.is(200))
    )

  // 设置测试执行计划
  setUp(
    // 完整仪表盘加载测试 - 主要负载
    fullDashboardScenario.inject(
      rampUsers(50).during(30.seconds),
      constantUsersPerSec(5).during(60.seconds)
    ),
    // 单独接口测试
    metricsOverviewScenario.inject(
      rampUsers(30).during(30.seconds)
    ),
    userStatsScenario.inject(
      rampUsers(30).during(30.seconds)
    )
  ).protocols(httpProtocol)
   .assertions(
     // 全局断言
     global.responseTime.mean.lt(Thresholds.meanResponseTime),
     global.responseTime.max.lt(Thresholds.maxResponseTime * 2),
     global.successfulRequests.percent.gt(Thresholds.successRate),
     // 仪表盘特定断言
     details("获取仪表盘指标概览").responseTime.mean.lt(300),
     details("获取用户统计概览").responseTime.mean.lt(300)
   )
}
