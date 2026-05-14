package com.empresa.tasks.interfaces.rest.dto;

import com.empresa.tasks.domain.model.Task;
import com.empresa.tasks.domain.model.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskResponseTest {

    @Test
    void deberiaCrearDesdeTask() {
        Task task = new Task();
        task.setId(1L);
        task.setUserId(10L);
        task.setTitle("Test");
        task.setDescription("Desc");
        task.setStatus(TaskStatus.COMPLETED);
        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setCompletedAt(now.plusHours(1));

        TaskResponse response = TaskResponse.from(task);

        assertEquals(1L, response.id());
        assertEquals(10L, response.userId());
        assertEquals("Test", response.title());
        assertEquals("Desc", response.description());
        assertEquals(TaskStatus.COMPLETED, response.status());
        assertEquals(now, response.createdAt());
        assertEquals(now.plusHours(1), response.completedAt());
    }

    @Test
    void deberiaCrearConCompletedAtNull() {
        Task task = new Task(1L, 10L, "T", "D");

        TaskResponse response = TaskResponse.from(task);

        assertEquals(TaskStatus.PENDING, response.status());
        assertNull(response.completedAt());
    }
}
