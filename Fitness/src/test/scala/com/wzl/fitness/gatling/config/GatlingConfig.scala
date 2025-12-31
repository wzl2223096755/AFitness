package com.wzl.fitness.gatling.config

import io.gatling.core.Predef._
import io.gatling.http.Predef._

/**
 * Gatling性能测试基础配置
 * 包含HTTP协议配置、通用请求头、基础URL等
 */
object GatlingConfig {
  
  // 基础URL配置 - 可通过环境变量覆盖
  val baseUrl: String = sys.env.getOrElse("GATLING_BASE_URL", "http://localhost:8080")
  
  // API版本前缀
  val apiPrefix: String = "/api/v1"
  
  // HTTP协议配置
  val httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("application/json")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("zh-CN,zh;q=0.9,en;q=0.8")
    .contentTypeHeader("application/json")
    .userAgentHeader("Gatling/Performance-Test")
    .disableFollowRedirect
    .disableAutoReferer
    .shareConnections
  
  // 通用请求头
  val commonHeaders = Map(
    "Accept" -> "application/json",
    "Content-Type" -> "application/json"
  )
  
  // 带认证的请求头
  def authHeaders(token: String) = Map(
    "Accept" -> "application/json",
    "Content-Type" -> "application/json",
    "Authorization" -> s"Bearer $token"
  )
  
  // 性能测试阈值配置
  object Thresholds {
    val maxResponseTime = 500      // 最大响应时间(ms)
    val meanResponseTime = 200     // 平均响应时间(ms)
    val successRate = 95.0         // 成功率(%)
    val requestsPerSecond = 100    // 每秒请求数
  }
  
  // 负载配置
  object LoadConfig {
    val rampUpUsers = 100          // 递增用户数
    val rampUpDuration = 60        // 递增时间(秒)
    val constantUsers = 50         // 恒定用户数
    val constantDuration = 120     // 恒定时间(秒)
  }
}
