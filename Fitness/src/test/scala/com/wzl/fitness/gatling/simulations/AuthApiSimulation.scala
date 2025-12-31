package com.wzl.fitness.gatling.simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import com.wzl.fitness.gatling.config.GatlingConfig
import com.wzl.fitness.gatling.config.GatlingConfig._

/**
 * 认证API性能测试模拟
 * 测试登录、注册、token刷新等认证相关接口
 * 
 * Requirements: 5.2 - 测试主要API端点的响应时间
 */
class AuthApiSimulation extends Simulation {

  // 测试用户数据
  val testUsers = Iterator.continually(Map(
    "username" -> s"testuser_${System.currentTimeMillis()}_${scala.util.Random.nextInt(10000)}",
    "password" -> "Test123456!",
    "email" -> s"test_${System.currentTimeMillis()}_${scala.util.Random.nextInt(10000)}@test.com"
  ))

  // 已存在的测试用户（用于登录测试）
  val existingUser = Map(
    "username" -> "admin",
    "password" -> "admin123"
  )

  // 登录场景
  val loginScenario = scenario("用户登录性能测试")
    .exec(
      http("用户登录")
        .post(s"${apiPrefix}/auth/login")
        .headers(commonHeaders)
        .body(StringBody(
          s"""{"username":"${existingUser("username")}","password":"${existingUser("password")}"}"""
        ))
        .check(status.is(200))
        .check(jsonPath("$.code").is("200"))
        .check(jsonPath("$.data.accessToken").exists.saveAs("authToken"))
    )
    .pause(1.second)
    .exec(
      http("获取当前用户信息")
        .get(s"${apiPrefix}/auth/me")
        .header("Authorization", "Bearer ${authToken}")
        .headers(commonHeaders)
        .check(status.is(200))
        .check(jsonPath("$.code").is("200"))
    )

  // 注册场景
  val registerScenario = scenario("用户注册性能测试")
    .feed(testUsers)
    .exec(
      http("检查用户名是否存在")
        .get(s"${apiPrefix}/auth/check-username")
        .queryParam("username", "${username}")
        .headers(commonHeaders)
        .check(status.is(200))
    )
    .pause(500.milliseconds)
    .exec(
      http("检查邮箱是否存在")
        .get(s"${apiPrefix}/auth/check-email")
        .queryParam("email", "${email}")
        .headers(commonHeaders)
        .check(status.is(200))
    )
    .pause(500.milliseconds)
    .exec(
      http("用户注册")
        .post(s"${apiPrefix}/auth/register")
        .headers(commonHeaders)
        .body(StringBody(
          """{"username":"${username}","password":"${password}","email":"${email}"}"""
        ))
        .check(status.in(200, 400)) // 400可能是用户已存在
    )

  // Token刷新场景
  val tokenRefreshScenario = scenario("Token刷新性能测试")
    .exec(
      http("登录获取Token")
        .post(s"${apiPrefix}/auth/login")
        .headers(commonHeaders)
        .body(StringBody(
          s"""{"username":"${existingUser("username")}","password":"${existingUser("password")}"}"""
        ))
        .check(status.is(200))
        .check(jsonPath("$.data.accessToken").exists.saveAs("authToken"))
        .check(jsonPath("$.data.refreshToken").optional.saveAs("refreshToken"))
    )
    .pause(1.second)
    .doIf(session => session.contains("refreshToken")) {
      exec(
        http("刷新Token")
          .post(s"${apiPrefix}/auth/refresh")
          .headers(commonHeaders)
          .body(StringBody("""{"refreshToken":"${refreshToken}"}"""))
          .check(status.in(200, 400))
      )
    }

  // 综合认证场景
  val mixedAuthScenario = scenario("综合认证性能测试")
    .randomSwitch(
      70.0 -> exec(loginScenario),
      20.0 -> exec(registerScenario),
      10.0 -> exec(tokenRefreshScenario)
    )

  // 设置测试执行计划
  setUp(
    // 登录测试 - 主要负载
    loginScenario.inject(
      rampUsers(50).during(30.seconds),
      constantUsersPerSec(10).during(60.seconds)
    ),
    // 注册测试 - 较低负载
    registerScenario.inject(
      rampUsers(20).during(30.seconds),
      constantUsersPerSec(2).during(60.seconds)
    )
  ).protocols(httpProtocol)
   .assertions(
     // 全局断言
     global.responseTime.mean.lt(Thresholds.meanResponseTime),
     global.responseTime.max.lt(Thresholds.maxResponseTime * 2),
     global.successfulRequests.percent.gt(Thresholds.successRate)
   )
}
