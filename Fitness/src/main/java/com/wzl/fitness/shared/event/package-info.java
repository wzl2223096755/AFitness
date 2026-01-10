/**
 * 事件基础设施包
 * 
 * 提供领域事件的基础类和发布机制，用于模块间的松耦合通信。
 * 
 * 主要组件：
 * - {@link com.wzl.fitness.shared.event.DomainEvent} - 领域事件基类
 * - {@link com.wzl.fitness.shared.event.EventPublisher} - 事件发布器
 * 
 * 使用示例：
 * <pre>
 * // 定义领域事件
 * public class TrainingCompletedEvent extends DomainEvent {
 *     private final Long trainingRecordId;
 *     
 *     public TrainingCompletedEvent(Long userId, Long trainingRecordId) {
 *         super(userId);
 *         this.trainingRecordId = trainingRecordId;
 *     }
 * }
 * 
 * // 发布事件
 * eventPublisher.publish(new TrainingCompletedEvent(userId, recordId));
 * 
 * // 监听事件
 * {@literal @}EventListener
 * public void handleTrainingCompleted(TrainingCompletedEvent event) {
 *     // 处理事件
 * }
 * </pre>
 */
package com.wzl.fitness.shared.event;
