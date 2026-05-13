package com.empresa.tasks.application;

import com.empresa.tasks.application.exception.TaskNotFoundException;
import com.empresa.tasks.application.exception.UnauthorizedTaskAccessException;
import com.empresa.tasks.application.port.out.TaskRepositoryPort;
import com.empresa.tasks.application.usecase.ChangeTaskStatusUseCase;
import com.empresa.tasks.domain.model.Task;
import com.empresa.tasks.domain.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeTaskStatusUseCaseTest {

    @Mock
    private TaskRepositoryPort taskRepository;

    private ChangeTaskStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ChangeTaskStatusUseCase(taskRepository);
    }

    @Test
    void deberiaCompletarTareaExitosamente() {
        Task task = new Task(1L, 10L, "Estudiar", "Cap 1");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task result = useCase.execute(1L, 10L, TaskStatus.COMPLETED);

        assertEquals(TaskStatus.COMPLETED, result.getStatus());
        verify(taskRepository).save(task);
    }

    @Test
    void deberiaLanzarExcepcionSiTareaNoExiste() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () ->
            useCase.execute(99L, 10L, TaskStatus.COMPLETED)
        );
    }

    @Test
    void deberiaLanzarExcepcionSiUsuarioNoEsDueno() {
        Task task = new Task(1L, 10L, "Estudiar", "Cap 1");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(UnauthorizedTaskAccessException.class, () ->
            useCase.execute(1L, 99L, TaskStatus.COMPLETED)
        );
    }
}