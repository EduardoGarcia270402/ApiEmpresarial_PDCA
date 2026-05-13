package com.empresa.tasks.interfaces.rest.dto;

public record AuthResponse(
    String token,
    long expiresIn,
    Long userId,
    String email
) {}
