package com.empresa.tasks.application.exception;

public class UnauthorizedTaskAccessException extends RuntimeException {
    public UnauthorizedTaskAccessException() {
        super("No tienes permiso para modificar esta tarea");
    }
}