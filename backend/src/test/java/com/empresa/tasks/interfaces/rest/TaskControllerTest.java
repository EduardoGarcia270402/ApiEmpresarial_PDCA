package com.empresa.tasks.interfaces.rest;

import com.empresa.tasks.application.port.out.TaskRepositoryPort;
import com.empresa.tasks.application.usecase.ChangeTaskStatusUseCase;
import com.empresa.tasks.application.usecase.CreateTaskUseCase;
import com.empresa.tasks.application.usecase.DeleteTaskUseCase;
import com.empresa.tasks.domain.model.Task;
import com.empresa.tasks.domain.model.TaskStatus;
import com.empresa.tasks.interfaces.rest.dto.CreateTaskRequest;
import com.empresa.tasks.interfaces.rest.dto.TaskResponse;
import com.empresa.tasks.interfaces.rest.dto.UpdateTaskStatusRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private CreateTaskUseCase createTaskUseCase;

    @Mock
    private ChangeTaskStatusUseCase changeTaskStatusUseCase;

    @Mock
    private DeleteTaskUseCase deleteTaskUseCase;

    @Mock
    private TaskRepositoryPort taskRepository;

    private TaskController controller;

    @BeforeEach
    void setUp() {
        controller = new TaskController(createTaskUseCase, changeTaskStatusUseCase,
            deleteTaskUseCase, taskRepository);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(42L, null, List.of())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deberiaListarTareas() {
        Task task = new Task(1L, 42L, "T1", "D1");
        when(taskRepository.findAllByUserId(42L)).thenReturn(List.of(task));

        ResponseEntity<List<TaskResponse>> response = controller.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("T1", response.getBody().get(0).title());
    }

    @Test
    void deberiaCrearTarea() {
        CreateTaskRequest request = new CreateTaskRequest("Nueva", "Desc");
        Task task = new Task(1L, 42L, "Nueva", "Desc");
        when(createTaskUseCase.execute(42L, "Nueva", "Desc")).thenReturn(task);

        ResponseEntity<TaskResponse> response = controller.create(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Nueva", response.getBody().title());
    }

    @Test
    void deberiaCrearTareaSinDescripcion() {
        CreateTaskRequest request = new CreateTaskRequest("Nueva", null);
        Task task = new Task(1L, 42L, "Nueva", "");
        when(createTaskUseCase.execute(42L, "Nueva", "")).thenReturn(task);

        ResponseEntity<TaskResponse> response = controller.create(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void deberiaCambiarEstadoDeTarea() {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(TaskStatus.COMPLETED);
        Task task = new Task(1L, 42L, "T", "D");
        task.markAsCompleted();
        when(changeTaskStatusUseCase.execute(1L, 42L, TaskStatus.COMPLETED)).thenReturn(task);

        ResponseEntity<TaskResponse> response = controller.updateStatus(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(TaskStatus.COMPLETED, response.getBody().status());
    }

    @Test
    void deberiaEliminarTarea() {
        ResponseEntity<Void> response = controller.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(deleteTaskUseCase).execute(1L, 42L);
    }
}
