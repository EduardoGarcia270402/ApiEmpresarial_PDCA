package com.empresa.tasks.domain;

import com.empresa.tasks.domain.exception.InvalidStatusTransitionException;
import com.empresa.tasks.domain.model.Task;
import com.empresa.tasks.domain.model.TaskStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskDomainTest {

    @Test
    void deberiaCompletarUnaTareaPendiente() {
        Task task = new Task(1L, 1L, "Estudiar", "Cap 1");
        task.markAsCompleted();
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertNotNull(task.getCompletedAt());
    }

    @Test
    void deberiaCancelarUnaTareaPendiente() {
        Task task = new Task(1L, 1L, "Estudiar", "Cap 1");
        task.cancel();
        assertEquals(TaskStatus.CANCELLED, task.getStatus());
    }

    @Test
    void deberiaLanzarExcepcionAlCompletarTareaCancelada() {
        Task task = new Task(1L, 1L, "Estudiar", "Cap 1");
        task.cancel();

        assertThrows(InvalidStatusTransitionException.class, () -> {
            task.markAsCompleted();
        });
    }

    @Test
    void deberiaLanzarExcepcionAlCancelarTareaCompletada() {
        Task task = new Task(1L, 1L, "Estudiar", "Cap 1");
        task.markAsCompleted();

        assertThrows(InvalidStatusTransitionException.class, () -> {
            task.cancel();
        });
    }
}