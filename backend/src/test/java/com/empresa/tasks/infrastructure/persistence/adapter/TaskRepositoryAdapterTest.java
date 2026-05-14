package com.empresa.tasks.infrastructure.persistence.adapter;

import com.empresa.tasks.domain.model.Task;
import com.empresa.tasks.domain.model.TaskStatus;
import com.empresa.tasks.infrastructure.persistence.entity.TaskJpaEntity;
import com.empresa.tasks.infrastructure.persistence.entity.UserJpaEntity;
import com.empresa.tasks.infrastructure.persistence.repository.TaskJpaRepository;
import com.empresa.tasks.infrastructure.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskRepositoryAdapterTest {

    @Mock
    private TaskJpaRepository taskJpaRepository;

    @Mock
    private UserJpaRepository userJpaRepository;

    private TaskRepositoryAdapter adapter;

    private UserJpaEntity userEntity;

    @BeforeEach
    void setUp() {
        adapter = new TaskRepositoryAdapter(taskJpaRepository, userJpaRepository);
        userEntity = new UserJpaEntity(10L, "u@t.com", "h", "U");
    }

    private TaskJpaEntity crearTaskEntity(Long id, String title) {
        TaskJpaEntity entity = new TaskJpaEntity();
        entity.setId(id);
        entity.setUser(userEntity);
        entity.setTitle(title);
        entity.setDescription("Desc");
        entity.setStatus(TaskStatus.PENDING);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    @Test
    void deberiaGuardarTarea() {
        Task task = new Task(null, 10L, "Tarea", "Desc");
        TaskJpaEntity savedEntity = crearTaskEntity(1L, "Tarea");

        when(userJpaRepository.getReferenceById(10L)).thenReturn(userEntity);
        when(taskJpaRepository.save(any(TaskJpaEntity.class))).thenReturn(savedEntity);

        Task result = adapter.save(task);

        assertEquals(1L, result.getId());
        assertEquals("Tarea", result.getTitle());
    }

    @Test
    void deberiaBuscarTareaPorId() {
        TaskJpaEntity entity = crearTaskEntity(1L, "Tarea");
        when(taskJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

        Optional<Task> result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void deberiaRetornarVacioSiTareaNoExiste() {
        when(taskJpaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Task> result = adapter.findById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void deberiaListarTareasPorUsuario() {
        List<TaskJpaEntity> entities = List.of(
            crearTaskEntity(1L, "T1"),
            crearTaskEntity(2L, "T2")
        );
        when(taskJpaRepository.findAllByUser_IdOrderByCreatedAtDesc(10L)).thenReturn(entities);

        List<Task> result = adapter.findAllByUserId(10L);

        assertEquals(2, result.size());
    }

    @Test
    void deberiaListarTareasPorUsuarioYEstado() {
        List<TaskJpaEntity> entities = List.of(crearTaskEntity(1L, "T1"));
        when(taskJpaRepository.findAllByUser_IdAndStatusOrderByCreatedAtDesc(10L, TaskStatus.PENDING))
            .thenReturn(entities);

        List<Task> result = adapter.findAllByUserIdAndStatus(10L, TaskStatus.PENDING);

        assertEquals(1, result.size());
    }

    @Test
    void deberiaEliminarTareaPorId() {
        adapter.deleteById(1L);

        verify(taskJpaRepository).deleteById(1L);
    }
}
