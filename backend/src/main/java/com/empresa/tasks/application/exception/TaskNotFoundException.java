package com.empresa.tasks.application.exception;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(Long id) {
        super("Tarea con id " + id + " no encontrada");
    }
}