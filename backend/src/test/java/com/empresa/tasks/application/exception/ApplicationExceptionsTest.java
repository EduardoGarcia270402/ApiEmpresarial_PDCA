package com.empresa.tasks.application.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationExceptionsTest {

    @Test
    void taskNotFoundDeberiaIncluirId() {
        TaskNotFoundException ex = new TaskNotFoundException(42L);
        assertTrue(ex.getMessage().contains("42"));
    }

    @Test
    void unauthorizedDeberiaRetornarMensaje() {
        UnauthorizedTaskAccessException ex = new UnauthorizedTaskAccessException();
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("permiso"));
    }

    @Test
    void userAlreadyExistsDeberiaIncluirEmail() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("test@test.com");
        assertTrue(ex.getMessage().contains("test@test.com"));
    }
}
