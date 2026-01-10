/**
 * AFitness模块化架构 - 业务模块包
 * 
 * 本包包含按业务领域划分的独立模块：
 * - user: 用户模块 - 用户认证、个人资料、设置
 * - training: 训练模块 - 训练记录、训练计划、统计
 * - nutrition: 营养模块 - 营养记录、营养目标、统计
 * - recovery: 恢复评估模块 - 恢复状态评估、训练建议
 * - admin: 管理模块 - 系统监控、数据导出、缓存管理
 * 
 * 每个模块包含以下子包：
 * - api: 模块对外接口定义
 * - controller: REST控制器
 * - service: 业务服务
 * - repository: 数据访问
 * - entity: 领域实体
 * - dto: 数据传输对象
 * - event: 领域事件
 * 
 * 模块间通信规则：
 * 1. 模块间依赖必须通过api包中的接口进行
 * 2. 禁止直接依赖其他模块的实现类
 * 3. 数据传输必须使用DTO，禁止暴露Entity
 * 4. 异步通信通过事件总线进行
 * 
 * @see com.wzl.fitness.shared 共享内核
 * @see com.wzl.fitness.shared.event 事件基础设施
 */
package com.wzl.fitness.modules;
