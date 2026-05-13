package com.empresa.tasks.infrastructure.persistence.mapper;

import com.empresa.tasks.domain.model.Task;
import com.empresa.tasks.infrastructure.persistence.entity.TaskJpaEntity;
import com.empresa.tasks.infrastructure.persistence.entity.UserJpaEntity;

public final class TaskPersistenceMapper {

    private TaskPersistenceMapper() {
    }

    public static TaskJpaEntity toEntity(Task task, UserJpaEntity user) {
        TaskJpaEntity entity = new TaskJpaEntity();
        entity.setId(task.getId());
        entity.setUser(user);
        entity.setTitle(task.getTitle());
        entity.setDescription(task.getDescription());
        entity.setStatus(task.getStatus());
        entity.setCreatedAt(task.getCreatedAt());
        entity.setCompletedAt(task.getCompletedAt());
        return entity;
    }

    public static Task toDomain(TaskJpaEntity entity) {
        Task task = new Task();
        task.setId(entity.getId());
        task.setUserId(entity.getUser().getId());
        task.setTitle(entity.getTitle());
        task.setDescription(entity.getDescription());
        task.setStatus(entity.getStatus());
        task.setCreatedAt(entity.getCreatedAt());
        task.setCompletedAt(entity.getCompletedAt());
        return task;
    }
}
