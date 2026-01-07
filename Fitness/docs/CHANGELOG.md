# 变更记录 (Changelog)

本文档记录了 AFitness 健身管理系统的所有重要变更。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [1.0.0] - 2026-01-07

### 新增
- 完整的用户认证系统（注册、登录、JWT令牌刷新）
- 训练记录管理功能（CRUD操作）
- 营养记录追踪功能
- 1RM计算器（支持多种计算公式：Epley、Brzycki、Lombardi、OConner、Mayhew）
- 恢复状态评估功能
- 训练建议生成功能
- 仪表盘数据概览
- 用户资料管理
- 身体数据记录
- 训练计划管理
- 管理员后台功能
- Caffeine 本地缓存支持
- 系统监控仪表盘
- 数据导出功能（Excel格式）
- 完整的 Swagger/OpenAPI 文档

### 安全
- JWT Bearer Token 认证
- 密码加密存储（BCrypt）
- XSS 防护过滤器
- 输入参数验证
- 敏感数据脱敏
- 审计日志记录

### 技术栈
- 后端：Spring Boot 3.x + JPA + MySQL
- 前端：Vue 3 + Element Plus + ECharts
- 缓存：Caffeine
- 文档：SpringDoc OpenAPI

---

## API 版本历史

### v1 (当前版本)

基础URL: `/api/v1`

#### 认证接口
| 方法 | 路径 | 描述 | 版本 |
|------|------|------|------|
| POST | /auth/login | 用户登录 | v1.0.0 |
| POST | /auth/register | 用户注册 | v1.0.0 |
| POST | /auth/refresh | 刷新令牌 | v1.0.0 |
| POST | /auth/logout | 用户登出 | v1.0.0 |
| GET | /auth/me | 获取当前用户 | v1.0.0 |
| GET | /auth/check-username | 检查用户名 | v1.0.0 |
| GET | /auth/check-email | 检查邮箱 | v1.0.0 |

#### 训练接口
| 方法 | 路径 | 描述 | 版本 |
|------|------|------|------|
| POST | /training/record | 创建训练记录 | v1.0.0 |
| GET | /training/records | 获取训练记录 | v1.0.0 |
| GET | /training/records/page | 分页获取训练记录 | v1.0.0 |
| POST | /training/recovery | 提交恢复指标 | v1.0.0 |
| GET | /training/analysis/{userId} | 获取训练分析 | v1.0.0 |

#### 负荷恢复接口
| 方法 | 路径 | 描述 | 版本 |
|------|------|------|------|
| GET | /load-recovery/one-rep-max | 计算1RM | v1.0.0 |
| GET | /load-recovery/one-rep-max/models | 获取支持的模型 | v1.0.0 |
| POST | /load-recovery/one-rep-max/record | 保存1RM记录 | v1.0.0 |
| POST | /load-recovery/training-data | 保存训练数据 | v1.0.0 |
| POST | /load-recovery/recovery-assessment | 恢复评估 | v1.0.0 |
| GET | /load-recovery/training-suggestions | 训练建议 | v1.0.0 |
| GET | /load-recovery/load-trend | 负荷趋势 | v1.0.0 |
| GET | /load-recovery/my-data | 我的健身数据 | v1.0.0 |

#### 营养接口
| 方法 | 路径 | 描述 | 版本 |
|------|------|------|------|
| GET | /nutrition/records/{date} | 获取营养记录 | v1.0.0 |
| POST | /nutrition/records | 添加营养记录 | v1.0.0 |
| PUT | /nutrition/records/{id} | 更新营养记录 | v1.0.0 |
| DELETE | /nutrition/records/{id} | 删除营养记录 | v1.0.0 |
| GET | /nutrition/stats/{date} | 营养统计 | v1.0.0 |
| GET | /nutrition/recommendation | 营养建议 | v1.0.0 |
| GET | /nutrition/goals | 获取营养目标 | v1.0.0 |
| POST | /nutrition/goals | 设置营养目标 | v1.0.0 |

#### 仪表盘接口
| 方法 | 路径 | 描述 | 版本 |
|------|------|------|------|
| GET | /dashboard/metrics-overview | 指标概览 | v1.0.0 |
| GET | /dashboard/user-stats-overview | 用户统计 | v1.0.0 |
| GET | /dashboard/analytics | 分析数据 | v1.0.0 |
| GET | /dashboard/recent-training-records | 最近训练 | v1.0.0 |

#### 用户接口
| 方法 | 路径 | 描述 | 版本 |
|------|------|------|------|
| GET | /user/profile | 获取用户资料 | v1.0.0 |
| PUT | /user/profile | 更新用户资料 | v1.0.0 |
| POST | /user/change-password | 修改密码 | v1.0.0 |
| GET | /user/settings | 获取设置 | v1.0.0 |
| PUT | /user/settings | 更新设置 | v1.0.0 |
| GET | /user/body-records | 身体数据 | v1.0.0 |
| POST | /user/body-records | 添加身体数据 | v1.0.0 |
| GET | /user/achievements | 用户成就 | v1.0.0 |
| GET | /user/export | 导出数据 | v1.0.0 |

#### 训练计划接口
| 方法 | 路径 | 描述 | 版本 |
|------|------|------|------|
| GET | /training-plans | 获取所有计划 | v1.0.0 |
| GET | /training-plans/page | 分页获取计划 | v1.0.0 |
| GET | /training-plans/{id} | 获取计划详情 | v1.0.0 |
| POST | /training-plans | 创建计划 | v1.0.0 |
| PUT | /training-plans/{id} | 更新计划 | v1.0.0 |
| DELETE | /training-plans/{id} | 删除计划 | v1.0.0 |
| POST | /training-plans/weekly | 保存周计划 | v1.0.0 |

#### 管理员接口
| 方法 | 路径 | 描述 | 版本 |
|------|------|------|------|
| GET | /admin/monitor/system-info | 系统信息 | v1.0.0 |
| GET | /admin/monitor/jvm-metrics | JVM指标 | v1.0.0 |
| GET | /admin/monitor/database-stats | 数据库统计 | v1.0.0 |
| GET | /admin/monitor/user-activity | 用户活跃度 | v1.0.0 |
| GET | /admin/cache/stats | 缓存统计 | v1.0.0 |
| POST | /admin/cache/evict/{name} | 清除缓存 | v1.0.0 |
| POST | /admin/cache/evict-all | 清除所有缓存 | v1.0.0 |
| GET | /admin/export/users | 导出用户 | v1.0.0 |
| GET | /admin/export/training-records/{userId} | 导出训练记录 | v1.0.0 |
| GET | /admin/export/nutrition-records/{userId} | 导出营养记录 | v1.0.0 |
| GET | /admin/export/system-stats | 导出系统统计 | v1.0.0 |

#### 健康检查接口
| 方法 | 路径 | 描述 | 版本 |
|------|------|------|------|
| GET | /health | 健康检查 | v1.0.0 |
| GET | /health/live | 存活检查 | v1.0.0 |
| GET | /health/ready | 就绪检查 | v1.0.0 |

---

## 废弃说明

目前没有废弃的 API。

---

## 迁移指南

### 从旧版本迁移

如果您从早期开发版本迁移，请注意以下变更：

1. 所有 API 路径现在统一使用 `/api/v1` 前缀
2. 认证方式统一使用 JWT Bearer Token
3. 响应格式统一为 `ApiResponse` 结构

---

*最后更新: 2026-01-07*
