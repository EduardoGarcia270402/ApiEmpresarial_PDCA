package com.empresa.tasks.application;

import com.empresa.tasks.application.exception.UserAlreadyExistsException;
import com.empresa.tasks.application.port.out.PasswordEncoderPort;
import com.empresa.tasks.application.port.out.TaskRepositoryPort;
import com.empresa.tasks.application.port.out.UserRepositoryPort;
import com.empresa.tasks.application.usecase.CreateTaskUseCase;
import com.empresa.tasks.application.usecase.RegisterUserUseCase;
import com.empresa.tasks.domain.model.Task;
import com.empresa.tasks.domain.model.TaskStatus;
import com.empresa.tasks.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserAndCreateTaskUseCaseTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private PasswordEncoderPort passwordEncoder;
    @Mock private TaskRepositoryPort taskRepository;

    private RegisterUserUseCase registerUserUseCase;
    private CreateTaskUseCase createTaskUseCase;

    @BeforeEach
    void setUp() {
        registerUserUseCase = new RegisterUserUseCase(userRepository, passwordEncoder);
        createTaskUseCase = new CreateTaskUseCase(taskRepository);
    }

    @Test
    void deberiaRegistrarUsuarioExitosamente() {
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("hashedPass");
        User savedUser = new User(1L, "test@test.com", "hashedPass", "Juan");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = registerUserUseCase.execute("test@test.com", "pass123", "Juan");

        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
        verify(passwordEncoder).encode("pass123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deberiaLanzarExcepcionSiEmailYaExiste() {
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () ->
            registerUserUseCase.execute("test@test.com", "pass123", "Juan")
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    void deberiaCrearTareaExitosamente() {
        Task savedTask = new Task(1L, 10L, "Estudiar", "Cap 1");
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        Task result = createTaskUseCase.execute(10L, "Estudiar", "Cap 1");

        assertNotNull(result);
        assertEquals(TaskStatus.PENDING, result.getStatus());
        assertEquals(10L, result.getUserId());
        verify(taskRepository).save(any(Task.class));
    }
}