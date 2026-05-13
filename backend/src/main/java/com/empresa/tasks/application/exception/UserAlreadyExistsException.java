package com.empresa.tasks.application.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super("El usuario con email " + email + " ya existe");
    }
}