package com.empresa.tasks.infrastructure.persistence.entity;

import com.empresa.tasks.domain.model.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskJpaEntityTest {

    @Test
    void deberiaEstablecerDefaultsEnPrePersist() {
        TaskJpaEntity entity = new TaskJpaEntity();
        assertNull(entity.getStatus());
        assertNull(entity.getCreatedAt());

        entity.ensureDefaults();

        assertEquals(TaskStatus.PENDING, entity.getStatus());
        assertNotNull(entity.getCreatedAt());
    }

    @Test
    void noDeberiaOverrideValoresExistentesEnPrePersist() {
        TaskJpaEntity entity = new TaskJpaEntity();
        LocalDateTime fixedTime = LocalDateTime.of(2026, 1, 1, 0, 0);
        entity.setStatus(TaskStatus.COMPLETED);
        entity.setCreatedAt(fixedTime);

        entity.ensureDefaults();

        assertEquals(TaskStatus.COMPLETED, entity.getStatus());
        assertEquals(fixedTime, entity.getCreatedAt());
    }

    @Test
    void deberiaUsarConstructorAllArgs() {
        UserJpaEntity user = new UserJpaEntity(1L, "e@e.com", "h", "N");
        LocalDateTime now = LocalDateTime.now();
        TaskJpaEntity entity = new TaskJpaEntity(1L, user, "T", "D", TaskStatus.PENDING, now, null);

        assertEquals(1L, entity.getId());
        assertEquals(user, entity.getUser());
        assertEquals("T", entity.getTitle());
        assertEquals("D", entity.getDescription());
        assertEquals(TaskStatus.PENDING, entity.getStatus());
        assertEquals(now, entity.getCreatedAt());
        assertNull(entity.getCompletedAt());
    }

    @Test
    void deberiaFuncionarGettersYSetters() {
        TaskJpaEntity entity = new TaskJpaEntity();
        UserJpaEntity user = new UserJpaEntity(1L, "e@e.com", "h", "N");
        LocalDateTime now = LocalDateTime.now();

        entity.setId(5L);
        entity.setUser(user);
        entity.setTitle("Titulo");
        entity.setDescription("Desc");
        entity.setStatus(TaskStatus.IN_PROGRESS);
        entity.setCreatedAt(now);
        entity.setCompletedAt(now.plusDays(1));

        assertEquals(5L, entity.getId());
        assertEquals(user, entity.getUser());
        assertEquals("Titulo", entity.getTitle());
        assertEquals("Desc", entity.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, entity.getStatus());
        assertEquals(now, entity.getCreatedAt());
        assertEquals(now.plusDays(1), entity.getCompletedAt());
    }
}
