# 简历项目描述模板 - AFitness 健身管理系统

> 本文档提供简历撰写参考，请根据实际情况修改个人信息和具体数据

---

## 一、项目简介（简历用）

### 简短版（1-2行）

```
AFitness 健身管理系统 | 全栈开发
基于 Spring Boot 3 + Vue 3 的健身数据管理平台，支持训练记录、营养追踪、恢复评估等功能
```

### 标准版（简历项目经历）

```
AFitness 健身管理系统                                    2025.01 - 2026.01
技术栈：Spring Boot 3.2 / Vue 3 / MySQL 8.0 / Redis / Docker

项目描述：
一款面向健身爱好者的全栈 Web 应用，提供训练数据记录、营养摄入追踪、
恢复状态评估、数据可视化分析等功能，包含用户端和管理后台两个子系统。

主要职责：
• 负责系统架构设计，采用前后端分离架构，实现 RESTful API 规范
• 设计并实现 JWT 无状态认证机制，支持 Token 自动刷新
• 实现 Caffeine + Redis 多级缓存架构，接口响应时间降低 60%
• 开发数据库连接重试机制（指数退避算法），系统可用性提升至 99.5%
• 编写单元测试、集成测试、属性测试，代码覆盖率达 80%+
• 使用 Docker 容器化部署，编写 CI/CD 流水线配置
```

---

## 二、技术亮点描述

### 后端技术亮点

```
【认证授权】
• 基于 Spring Security + JWT 实现无状态认证
• 设计 Access Token + Refresh Token 双令牌机制，兼顾安全性与用户体验
• 实现细粒度的 RBAC 权限控制（USER/ADMIN 角色）

【缓存架构】
• 设计 L1(Caffeine) + L2(Redis) 多级缓存架构
• 针对不同业务场景配置差异化 TTL 策略（2分钟~24小时）
• 实现缓存统计监控，命中率稳定在 85% 以上

【高可用设计】
• 基于 AOP 实现数据库操作自动重试（指数退避算法）
• 集成 HikariCP 连接池，配置连接泄漏检测
• 实现优雅关闭机制，确保请求不丢失

【安全防护】
• 实现 XSS 过滤器，防止跨站脚本攻击
• 使用 BCrypt 加密存储密码
• 敏感数据脱敏处理（日志、API 响应）
```

### 前端技术亮点

```
【架构设计】
• 基于 Vue 3 Composition API 构建，代码复用性提升 40%
• 使用 Pinia 进行状态管理，模块化设计
• Axios 封装统一请求处理，支持请求重试、Token 自动刷新

【用户体验】
• 实现网络状态检测与离线提示
• 全局错误边界组件，优雅处理异常
• ECharts 数据可视化，支持训练趋势分析

【工程化】
• Vite 构建，开发环境秒级热更新
• 组件按需加载，首屏加载时间 < 2s
```

---

## 三、可量化成果

```
性能优化：
• 接口平均响应时间从 200ms 降至 80ms（降低 60%）
• 缓存命中率达 85%，数据库查询减少 70%
• 首屏加载时间 < 2s

质量保障：
• 单元测试覆盖率 80%+
• 集成测试覆盖核心业务流程
• 属性测试发现 3 个边界条件 Bug

可用性：
• 系统可用性 99.5%+
• 支持优雅关闭，零请求丢失
• 数据库连接异常自动恢复
```

---

## 四、面试常见问题准备

### 1. 项目介绍（30秒版）

```
这是我独立开发的一个健身管理系统，采用 Spring Boot + Vue 3 前后端分离架构。
主要功能包括训练记录管理、营养追踪、恢复评估和数据分析。
技术上我重点实现了 JWT 认证、多级缓存、数据库重试机制等，
通过这些优化，接口响应时间降低了 60%，系统可用性达到 99.5%。
```

### 2. 为什么选择这个技术栈？

```
Spring Boot：
- 企业级应用的事实标准，生态成熟
- 自动配置减少样板代码，开发效率高
- 内置 Actuator 监控，生产级特性完善

Vue 3：
- Composition API 提供更好的代码组织和复用
- 相比 React，学习曲线更平缓
- Element Plus 组件库成熟，开发效率高

MySQL + Redis：
- MySQL 稳定可靠，适合结构化数据
- Redis 作为缓存层，显著提升读取性能
```

### 3. 多级缓存是怎么设计的？

```
采用 L1(Caffeine) + L2(Redis) 架构：

L1 Caffeine（本地缓存）：
- 存储热点数据，延迟约 1μs
- 容量 1000 条，使用 Window TinyLFU 驱逐策略
- 写后 30 分钟过期，访问后 15 分钟过期

L2 Redis（分布式缓存）：
- 存储共享数据，延迟约 1ms
- 不同数据类型使用不同 TTL（用户信息 30 分钟，训练记录 5 分钟）

查询流程：先查 L1 → 未命中查 L2 → 未命中查 DB → 回写缓存
```

### 4. JWT 认证流程是怎样的？

```
登录流程：
1. 用户提交用户名密码
2. 后端验证凭据（BCrypt 比对）
3. 生成 Access Token（24小时）+ Refresh Token（7天）
4. 返回给前端存储

请求流程：
1. 前端在 Header 中携带 Authorization: Bearer <token>
2. JwtAuthFilter 拦截请求，验证 Token 签名和有效期
3. 解析用户信息，设置 SecurityContext
4. 后续可通过 @AuthenticationPrincipal 获取当前用户

Token 刷新：
1. Access Token 过期时，前端用 Refresh Token 请求刷新
2. 后端验证 Refresh Token，签发新的 Access Token
3. 前端更新本地存储，重试原请求
```

### 5. 数据库重试机制怎么实现的？

```
基于 AOP 实现，使用指数退避算法：

@Aspect
public class DatabaseRetryAspect {
    @Around("@annotation(DatabaseRetryable)")
    public Object retry(ProceedingJoinPoint joinPoint) {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                return joinPoint.proceed();
            } catch (TransientDataAccessException e) {
                attempts++;
                long delay = BASE_DELAY * (long) Math.pow(2, attempts - 1);
                Thread.sleep(delay);  // 1s → 2s → 4s
            }
        }
        throw lastException;
    }
}

为什么用指数退避？
- 避免立即重试造成资源浪费
- 给数据库恢复时间
- 减少对故障服务的压力
```

### 6. 遇到的最大挑战是什么？

```
挑战：缓存一致性问题

场景：用户更新数据后，缓存中仍是旧数据

解决方案：
1. 采用 Cache-Aside 模式：先更新 DB，再删除缓存
2. 为什么不是先删缓存？
   - 先删缓存可能导致：删除后、更新前，另一个请求读取旧数据并写入缓存
   - 先更新 DB 的不一致窗口更短，且缓存很快会被删除

3. 使用 @CacheEvict 注解自动处理
4. 关联数据使用 @Caching 组合注解，同时清除多个缓存
```

---

## 五、技能清单（简历用）

```
后端：Java 17, Spring Boot 3, Spring Security, Spring Data JPA, MyBatis
前端：Vue 3, Vite, Element Plus, Pinia, Axios, ECharts
数据库：MySQL 8.0, Redis, HikariCP
中间件：Caffeine Cache, JWT (jjwt)
测试：JUnit 5, jqwik (属性测试), Gatling (性能测试)
DevOps：Docker, Docker Compose, Nginx, Git
监控：Spring Actuator, Micrometer, Prometheus
```

---

## 六、注意事项

1. **数据要真实**：面试官可能深挖细节，确保你能解释每个数字的来源
2. **突出个人贡献**：如果是团队项目，明确说明你负责的部分
3. **准备代码演示**：最好能现场演示项目，或准备好 GitHub 链接
4. **理解原理**：不只是会用，要能解释为什么这样设计
5. **准备失败案例**：面试官喜欢问"遇到什么困难"，准备 1-2 个真实案例

---

> 祝求职顺利！🎉
