package com.empresa.tasks.application;

import com.empresa.tasks.application.port.out.PasswordEncoderPort;
import com.empresa.tasks.application.port.out.TokenGeneratorPort;
import com.empresa.tasks.application.port.out.UserRepositoryPort;
import com.empresa.tasks.application.usecase.LoginUseCase;
import com.empresa.tasks.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock private UserRepositoryPort userRepo;
    @Mock private PasswordEncoderPort passwordEncoder;
    @Mock private TokenGeneratorPort tokenGenerator;

    private LoginUseCase loginUseCase;

    @BeforeEach
    void setUp() {
        loginUseCase = new LoginUseCase(userRepo, passwordEncoder, tokenGenerator);
    }

    @Test
    void shouldReturnTokenWhenCredentialsAreValid() {
        User user = new User(1L, "test@test.com", "hashed", "Test");
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(tokenGenerator.generateToken(user)).thenReturn("jwt-token-abc");

        LoginUseCase.LoginResult result = loginUseCase.execute("test@test.com", "password123");

        assertEquals("jwt-token-abc", result.token());
        assertEquals(7200, result.expiresIn());
        verify(tokenGenerator).generateToken(user);
    }

    @Test
    void shouldThrowWhenEmailNotFound() {
        when(userRepo.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> loginUseCase.execute("noexiste@test.com", "password"));

        assertEquals("Credenciales inválidas", ex.getMessage());
        verify(tokenGenerator, never()).generateToken(any());
    }

    @Test
    void shouldThrowWhenPasswordDoesNotMatch() {
        User user = new User(1L, "test@test.com", "hashed", "Test");
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "hashed")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> loginUseCase.execute("test@test.com", "wrongpassword"));

        assertEquals("Credenciales inválidas", ex.getMessage());
        verify(tokenGenerator, never()).generateToken(any());
    }
}
