package com.empresa.tasks.application;

import com.empresa.tasks.application.exception.TaskNotFoundException;
import com.empresa.tasks.application.exception.UnauthorizedTaskAccessException;
import com.empresa.tasks.application.port.out.TaskRepositoryPort;
import com.empresa.tasks.application.usecase.DeleteTaskUseCase;
import com.empresa.tasks.domain.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteTaskUseCaseTest {

    @Mock
    private TaskRepositoryPort taskRepository;

    private DeleteTaskUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteTaskUseCase(taskRepository);
    }

    @Test
    void deberiaEliminarTareaExitosamente() {
        Task task = new Task(1L, 10L, "Tarea", "Desc");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        useCase.execute(1L, 10L);

        verify(taskRepository).deleteById(1L);
    }

    @Test
    void deberiaLanzarExcepcionSiTareaNoExiste() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () ->
            useCase.execute(99L, 10L)
        );
        verify(taskRepository, never()).deleteById(anyLong());
    }

    @Test
    void deberiaLanzarExcepcionSiUsuarioNoEsDueno() {
        Task task = new Task(1L, 10L, "Tarea", "Desc");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(UnauthorizedTaskAccessException.class, () ->
            useCase.execute(1L, 99L)
        );
        verify(taskRepository, never()).deleteById(anyLong());
    }
}
