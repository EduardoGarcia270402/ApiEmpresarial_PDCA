package com.empresa.tasks.domain.exception;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(String currentStatus, String targetStatus) {
        super("No se puede cambiar el estado de " + currentStatus + " a " + targetStatus);
    }
}