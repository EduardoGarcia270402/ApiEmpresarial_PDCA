package com.empresa.tasks.interfaces.rest;

import com.empresa.tasks.application.usecase.LoginUseCase;
import com.empresa.tasks.application.usecase.RegisterUserUseCase;
import com.empresa.tasks.domain.model.User;
import com.empresa.tasks.interfaces.rest.dto.AuthResponse;
import com.empresa.tasks.interfaces.rest.dto.LoginRequest;
import com.empresa.tasks.interfaces.rest.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private RegisterUserUseCase registerUserUseCase;

    @Mock
    private LoginUseCase loginUseCase;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(registerUserUseCase, loginUseCase);
    }

    @Test
    void deberiaRegistrarUsuarioExitosamente() {
        RegisterRequest request = new RegisterRequest("test@test.com", "password123", "Test User");
        User user = new User(1L, "test@test.com", "hash", "Test User");
        LoginUseCase.LoginResult loginResult = new LoginUseCase.LoginResult("jwt-token", 3600L, 1L);

        when(registerUserUseCase.execute("test@test.com", "password123", "Test User")).thenReturn(user);
        when(loginUseCase.execute("test@test.com", "password123")).thenReturn(loginResult);

        ResponseEntity<AuthResponse> response = controller.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("jwt-token", response.getBody().token());
        assertEquals(1L, response.getBody().userId());
        assertEquals("test@test.com", response.getBody().email());
    }

    @Test
    void deberiaHacerLoginExitosamente() {
        LoginRequest request = new LoginRequest("test@test.com", "password123");
        LoginUseCase.LoginResult loginResult = new LoginUseCase.LoginResult("jwt-token", 3600L, 1L);

        when(loginUseCase.execute("test@test.com", "password123")).thenReturn(loginResult);

        ResponseEntity<AuthResponse> response = controller.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt-token", response.getBody().token());
        assertEquals("test@test.com", response.getBody().email());
    }
}
