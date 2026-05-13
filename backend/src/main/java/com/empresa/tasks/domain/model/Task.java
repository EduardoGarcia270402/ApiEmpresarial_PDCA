package com.empresa.tasks.domain.model;

import com.empresa.tasks.domain.exception.InvalidStatusTransitionException;
import java.time.LocalDateTime;

public class Task {

    private Long id;
    private Long userId;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public Task() {}

    public Task(Long id, Long userId, String title, String description) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.status = TaskStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // ── Reglas de negocio ──────────────────────────────────

    public void markAsCompleted() {
        if (this.status == TaskStatus.CANCELLED) {
            throw new InvalidStatusTransitionException(
                this.status.name(), TaskStatus.COMPLETED.name()
            );
        }
        if (this.status == TaskStatus.COMPLETED) {
            throw new InvalidStatusTransitionException(
                this.status.name(), TaskStatus.COMPLETED.name()
            );
        }
        this.status = TaskStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status == TaskStatus.COMPLETED) {
            throw new InvalidStatusTransitionException(
                this.status.name(), TaskStatus.CANCELLED.name()
            );
        }
        if (this.status == TaskStatus.CANCELLED) {
            throw new InvalidStatusTransitionException(
                this.status.name(), TaskStatus.CANCELLED.name()
            );
        }
        this.status = TaskStatus.CANCELLED;
    }

    // ── Getters y Setters ──────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}