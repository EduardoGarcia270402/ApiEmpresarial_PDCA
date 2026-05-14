package com.empresa.tasks.domain;

import com.empresa.tasks.domain.exception.InvalidStatusTransitionException;
import com.empresa.tasks.domain.model.Task;
import com.empresa.tasks.domain.model.TaskStatus;
import java.time.LocalDateTime;
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
    void deberiaIniciarProgresoDesdePendiente() {
        Task task = new Task(1L, 1L, "Estudiar", "Cap 1");

        task.startProgress();

        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
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
    void deberiaLanzarExcepcionAlCompletarTareaYaCompletada() {
        Task task = new Task(1L, 1L, "Estudiar", "Cap 1");
        task.markAsCompleted();

        assertThrows(InvalidStatusTransitionException.class, task::markAsCompleted);
    }

    @Test
    void deberiaLanzarExcepcionAlCancelarTareaCompletada() {
        Task task = new Task(1L, 1L, "Estudiar", "Cap 1");
        task.markAsCompleted();

        assertThrows(InvalidStatusTransitionException.class, () -> {
            task.cancel();
        });
    }

    @Test
    void deberiaLanzarExcepcionAlCancelarTareaYaCancelada() {
        Task task = new Task(1L, 1L, "Estudiar", "Cap 1");
        task.cancel();

        assertThrows(InvalidStatusTransitionException.class, task::cancel);
    }

    @Test
    void deberiaLanzarExcepcionAlIniciarProgresoSiNoEstaPendiente() {
        Task task = new Task(1L, 1L, "Estudiar", "Cap 1");
        task.startProgress();

        assertThrows(InvalidStatusTransitionException.class, task::startProgress);
    }

    @Test
    void deberiaActualizarCamposConSetters() {
        Task task = new Task();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime completedAt = LocalDateTime.now();

        task.setId(10L);
        task.setUserId(20L);
        task.setTitle("Nueva tarea");
        task.setDescription("Descripcion");
        task.setStatus(TaskStatus.COMPLETED);
        task.setCreatedAt(createdAt);
        task.setCompletedAt(completedAt);

        assertEquals(10L, task.getId());
        assertEquals(20L, task.getUserId());
        assertEquals("Nueva tarea", task.getTitle());
        assertEquals("Descripcion", task.getDescription());
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertEquals(createdAt, task.getCreatedAt());
        assertEquals(completedAt, task.getCompletedAt());
    }
}
