package com.wzl.fitness.gatling.simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import com.wzl.fitness.gatling.config.GatlingConfig
import com.wzl.fitness.gatling.config.GatlingConfig._

/**
 * 训练记录API性能测试模拟
 * 测试力量训练数据的CRUD操作
 * 
 * Requirements: 5.2, 5.3 - 测试主要API端点的响应时间和并发用户表现
 */
class TrainingApiSimulation extends Simulation {

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
    "notes" -> s"性能测试数据_${System.currentTimeMillis()}",
    "exerciseType" -> exerciseTypes(scala.util.Random.nextInt(exerciseTypes.length))
  ))

  // 分页参数
  val pageFeeder = Iterator.continually(Map(
    "page" -> scala.util.Random.nextInt(5).toString,
    "size" -> (10 + scala.util.Random.nextInt(20)).toString
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

  // 获取训练记录列表场景
  val listTrainingRecordsScenario = scenario("获取训练记录列表性能测试")
    .exec(loginAndGetToken)
    .pause(500.milliseconds)
    .feed(pageFeeder)
    .exec(
      http("获取力量训练数据列表")
        .get(s"${apiPrefix}/strength-training")
        .header("Authorization", "Bearer ${authToken}")
        .queryParam("page", "${page}")
        .queryParam("size", "${size}")
        .queryParam("sortBy", "timestamp")
        .queryParam("sortDir", "desc")
        .headers(commonHeaders)
        .check(status.is(200))
        .check(responseTimeInMillis.lt(Thresholds.maxResponseTime))
    )

  // 创建训练记录场景
  val createTrainingRecordScenario = scenario("创建训练记录性能测试")
    .exec(loginAndGetToken)
    .pause(500.milliseconds)
    .feed(trainingDataFeeder)
    .exec(
      http("添加力量训练数据")
        .post(s"${apiPrefix}/strength-training")
        .header("Authorization", "Bearer ${authToken}")
        .headers(commonHeaders)
        .body(StringBody(
          """{
            "exerciseName": "${exerciseName}",
            "weight": ${weight},
            "reps": ${reps},
            "sets": ${sets},
            "notes": "${notes}",
            "exerciseType": "${exerciseType}"
          }"""
        ))
        .check(status.is(200))
        .check(jsonPath("$.data.id").optional.saveAs("createdRecordId"))
        .check(responseTimeInMillis.lt(Thresholds.maxResponseTime))
    )

  // 查询训练记录详情场景
  val getTrainingRecordScenario = scenario("查询训练记录详情性能测试")
    .exec(loginAndGetToken)
    .pause(500.milliseconds)
    // 先获取列表以获取有效ID
    .exec(
      http("获取训练记录列表")
        .get(s"${apiPrefix}/strength-training")
        .header("Authorization", "Bearer ${authToken}")
        .queryParam("page", "0")
        .queryParam("size", "10")
        .headers(commonHeaders)
        .check(status.is(200))
        .check(jsonPath("$.data.content[0].id").optional.saveAs("recordId"))
    )
    .doIf(session => session.contains("recordId")) {
      exec(
        http("获取训练记录详情")
          .get(s"${apiPrefix}/strength-training/$${recordId}")
          .header("Authorization", "Bearer ${authToken}")
          .headers(commonHeaders)
          .check(status.is(200))
          .check(responseTimeInMillis.lt(Thresholds.maxResponseTime))
      )
    }

  // 按动作名称查询场景
  val queryByExerciseScenario = scenario("按动作名称查询性能测试")
    .exec(loginAndGetToken)
    .pause(500.milliseconds)
    .exec(
      http("按动作名称查询")
        .get(s"${apiPrefix}/strength-training/exercise/深蹲")
        .header("Authorization", "Bearer ${authToken}")
        .headers(commonHeaders)
        .check(status.in(200, 404))
        .check(responseTimeInMillis.lt(Thresholds.maxResponseTime))
    )

  // 获取最大重量统计场景
  val maxWeightStatsScenario = scenario("最大重量统计性能测试")
    .exec(loginAndGetToken)
    .pause(500.milliseconds)
    .exec(
      http("获取最大重量统计")
        .get(s"${apiPrefix}/strength-training/stats/max-weight")
        .header("Authorization", "Bearer ${authToken}")
        .headers(commonHeaders)
        .check(status.is(200))
        .check(responseTimeInMillis.lt(Thresholds.maxResponseTime))
    )

  // 综合训练数据操作场景 - 模拟真实用户行为
  val mixedTrainingScenario = scenario("综合训练数据操作性能测试")
    .exec(loginAndGetToken)
    .pause(500.milliseconds)
    // 查看列表
    .exec(
      http("查看训练记录列表")
        .get(s"${apiPrefix}/strength-training")
        .header("Authorization", "Bearer ${authToken}")
        .queryParam("page", "0")
        .queryParam("size", "20")
        .headers(commonHeaders)
        .check(status.is(200))
    )
    .pause(1.second)
    // 添加新记录
    .feed(trainingDataFeeder)
    .exec(
      http("添加新训练记录")
        .post(s"${apiPrefix}/strength-training")
        .header("Authorization", "Bearer ${authToken}")
        .headers(commonHeaders)
        .body(StringBody(
          """{
            "exerciseName": "${exerciseName}",
            "weight": ${weight},
            "reps": ${reps},
            "sets": ${sets},
            "notes": "${notes}",
            "exerciseType": "${exerciseType}"
          }"""
        ))
        .check(status.is(200))
    )
    .pause(500.milliseconds)
    // 查看统计
    .exec(
      http("查看最大重量统计")
        .get(s"${apiPrefix}/strength-training/stats/max-weight")
        .header("Authorization", "Bearer ${authToken}")
        .headers(commonHeaders)
        .check(status.is(200))
    )

  // 设置测试执行计划
  setUp(
    // 列表查询 - 高频操作
    listTrainingRecordsScenario.inject(
      rampUsers(50).during(30.seconds),
      constantUsersPerSec(10).during(60.seconds)
    ),
    // 创建记录 - 中频操作
    createTrainingRecordScenario.inject(
      rampUsers(30).during(30.seconds),
      constantUsersPerSec(5).during(60.seconds)
    ),
    // 综合操作
    mixedTrainingScenario.inject(
      rampUsers(20).during(30.seconds)
    )
  ).protocols(httpProtocol)
   .assertions(
     // 全局断言
     global.responseTime.mean.lt(Thresholds.meanResponseTime),
     global.responseTime.max.lt(Thresholds.maxResponseTime * 2),
     global.successfulRequests.percent.gt(Thresholds.successRate),
     // 训练记录特定断言
     details("获取力量训练数据列表").responseTime.mean.lt(300),
     details("添加力量训练数据").responseTime.mean.lt(400)
   )
}
