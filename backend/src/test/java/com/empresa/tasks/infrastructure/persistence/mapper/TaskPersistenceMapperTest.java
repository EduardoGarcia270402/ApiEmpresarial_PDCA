package com.empresa.tasks.infrastructure.persistence.mapper;

import com.empresa.tasks.domain.model.Task;
import com.empresa.tasks.domain.model.TaskStatus;
import com.empresa.tasks.infrastructure.persistence.entity.TaskJpaEntity;
import com.empresa.tasks.infrastructure.persistence.entity.UserJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskPersistenceMapperTest {

    @Test
    void deberiaConvertirDominioAEntidad() {
        Task task = new Task();
        task.setId(1L);
        task.setUserId(10L);
        task.setTitle("Tarea");
        task.setDescription("Desc");
        task.setStatus(TaskStatus.PENDING);
        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setCompletedAt(null);

        UserJpaEntity user = new UserJpaEntity(10L, "u@t.com", "h", "U");

        TaskJpaEntity entity = TaskPersistenceMapper.toEntity(task, user);

        assertEquals(1L, entity.getId());
        assertEquals(user, entity.getUser());
        assertEquals("Tarea", entity.getTitle());
        assertEquals("Desc", entity.getDescription());
        assertEquals(TaskStatus.PENDING, entity.getStatus());
        assertEquals(now, entity.getCreatedAt());
        assertNull(entity.getCompletedAt());
    }

    @Test
    void deberiaConvertirEntidadADominio() {
        UserJpaEntity user = new UserJpaEntity(10L, "u@t.com", "h", "U");
        LocalDateTime now = LocalDateTime.now();

        TaskJpaEntity entity = new TaskJpaEntity();
        entity.setId(1L);
        entity.setUser(user);
        entity.setTitle("Tarea");
        entity.setDescription("Desc");
        entity.setStatus(TaskStatus.IN_PROGRESS);
        entity.setCreatedAt(now);
        entity.setCompletedAt(now.plusHours(1));

        Task task = TaskPersistenceMapper.toDomain(entity);

        assertEquals(1L, task.getId());
        assertEquals(10L, task.getUserId());
        assertEquals("Tarea", task.getTitle());
        assertEquals("Desc", task.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertEquals(now, task.getCreatedAt());
        assertEquals(now.plusHours(1), task.getCompletedAt());
    }
}
