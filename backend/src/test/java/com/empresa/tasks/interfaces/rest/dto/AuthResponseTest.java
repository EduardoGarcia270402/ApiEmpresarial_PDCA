package com.empresa.tasks.interfaces.rest.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseTest {

    @Test
    void deberiaCrearAuthResponse() {
        AuthResponse response = new AuthResponse("token123", 3600L, 1L, "test@test.com");

        assertEquals("token123", response.token());
        assertEquals(3600L, response.expiresIn());
        assertEquals(1L, response.userId());
        assertEquals("test@test.com", response.email());
    }
}
