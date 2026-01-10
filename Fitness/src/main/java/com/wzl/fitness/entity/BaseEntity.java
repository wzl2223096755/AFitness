package com.wzl.fitness.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 基础实体类，提供软删除和审计字段支持
 * 
 * <p>所有需要软删除功能的实体应继承此类</p>
 * 
 * <p>软删除机制说明：</p>
 * <ul>
 *   <li>deleted 字段标记记录是否已删除（false=未删除，true=已删除）</li>
 *   <li>deletedAt 字段记录删除时间</li>
 *   <li>deletedBy 字段记录执行删除操作的用户ID</li>
 * </ul>
 */
@MappedSuperclass
public abstract class BaseEntity {

    /**
     * 软删除标记
     * false = 未删除（默认）
     * true = 已删除
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    /**
     * 删除时间
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 执行删除操作的用户ID
     */
    @Column(name = "deleted_by")
    private Long deletedBy;

    /**
     * 创建时间
     */
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public Long getDeletedBy() { return deletedBy; }
    public void setDeletedBy(Long deletedBy) { this.deletedBy = deletedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /**
     * 执行软删除
     * 
     * @param userId 执行删除操作的用户ID
     */
    public void softDelete(Long userId) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = userId;
    }

    /**
     * 恢复已删除的记录
     */
    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }

    /**
     * 检查记录是否已被删除
     * 
     * @return true 如果记录已被删除
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(deleted);
    }
}
