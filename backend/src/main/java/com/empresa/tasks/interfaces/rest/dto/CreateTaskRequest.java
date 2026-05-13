package com.empresa.tasks.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
    @NotBlank(message = "El título es obligatorio")
    @Size(min = 3, max = 200, message = "El título debe tener entre 3 y 200 caracteres")
    String title,

    @Size(max = 2000, message = "La descripción no puede exceder los 2000 caracteres")
    String description
) {}
