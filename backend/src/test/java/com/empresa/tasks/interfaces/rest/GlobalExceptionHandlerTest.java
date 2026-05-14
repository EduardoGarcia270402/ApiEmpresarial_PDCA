package com.empresa.tasks.interfaces.rest;

import com.empresa.tasks.application.exception.TaskNotFoundException;
import com.empresa.tasks.application.exception.UnauthorizedTaskAccessException;
import com.empresa.tasks.application.exception.UserAlreadyExistsException;
import com.empresa.tasks.domain.exception.InvalidStatusTransitionException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void deberiaRetornarConflictParaUsuarioExistente() {
        var ex = new UserAlreadyExistsException("test@test.com");
        ResponseEntity<ProblemDetail> response = handler.handleUserAlreadyExists(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Conflicto de registro", response.getBody().getTitle());
    }

    @Test
    void deberiaRetornarConflictParaTransicionInvalida() {
        var ex = new InvalidStatusTransitionException("PENDING", "COMPLETED");
        ResponseEntity<ProblemDetail> response = handler.handleInvalidTransition(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Transición de estado inválida", response.getBody().getTitle());
    }

    @Test
    void deberiaRetornarNotFoundParaTareaNoEncontrada() {
        var ex = new TaskNotFoundException(99L);
        ResponseEntity<ProblemDetail> response = handler.handleTaskNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Tarea no encontrada", response.getBody().getTitle());
    }

    @Test
    void deberiaRetornarForbiddenParaAccesoNoAutorizado() {
        var ex = new UnauthorizedTaskAccessException();
        ResponseEntity<ProblemDetail> response = handler.handleUnauthorizedAccess(ex);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Acceso no autorizado", response.getBody().getTitle());
    }

    @Test
    void deberiaRetornarForbiddenParaAccessDenied() {
        var ex = new AccessDeniedException("denied");
        ResponseEntity<ProblemDetail> response = handler.handleAccessDenied(ex);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Acceso denegado", response.getBody().getTitle());
    }

    @Test
    void deberiaRetornarUnauthorizedParaIllegalArgument() {
        var ex = new IllegalArgumentException("Credenciales inválidas");
        ResponseEntity<ProblemDetail> response = handler.handleBadCredentials(ex);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Autenticación fallida", response.getBody().getTitle());
    }

    @Test
    void deberiaRetornarBadRequestParaValidacion() {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "es obligatorio"));
        var ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ProblemDetail> response = handler.handleValidation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error de validación", response.getBody().getTitle());
        assertTrue(response.getBody().getDetail().contains("email"));
    }
}
