package com.empresa.tasks.interfaces.rest.dto;

import com.empresa.tasks.domain.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(
    @NotNull(message = "El estado es obligatorio")
    TaskStatus status
) {}
