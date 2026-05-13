package com.empresa.tasks.interfaces.rest.dto;

import com.empresa.tasks.domain.model.Task;
import com.empresa.tasks.domain.model.TaskStatus;
import java.time.LocalDateTime;

public record TaskResponse(
    Long id,
    Long userId,
    String title,
    String description,
    TaskStatus status,
    LocalDateTime createdAt,
    LocalDateTime completedAt
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getUserId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getCreatedAt(),
            task.getCompletedAt()
        );
    }
}
