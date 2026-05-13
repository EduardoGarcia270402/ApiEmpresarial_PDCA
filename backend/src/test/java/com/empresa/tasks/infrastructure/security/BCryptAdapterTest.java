package com.empresa.tasks.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios del BCryptAdapter.
 * Verifica encode y matches sin levantar Spring.
 */
class BCryptAdapterTest {

    private final BCryptAdapter adapter = new BCryptAdapter();

    @Test
    void shouldEncodePassword() {
        String raw = "miPassword123";
        String hashed = adapter.encode(raw);

        assertNotNull(hashed);
        assertNotEquals(raw, hashed);
        // BCrypt hashes siempre empiezan con $2a$ o $2b$
        assertTrue(hashed.startsWith("$2"));
    }

    @Test
    void shouldMatchCorrectPassword() {
        String raw = "securePass!";
        String hashed = adapter.encode(raw);

        assertTrue(adapter.matches(raw, hashed));
    }

    @Test
    void shouldNotMatchWrongPassword() {
        String hashed = adapter.encode("correctPassword");

        assertFalse(adapter.matches("wrongPassword", hashed));
    }

    @Test
    void shouldGenerateDifferentHashesForSamePassword() {
        String raw = "samePassword";
        String hash1 = adapter.encode(raw);
        String hash2 = adapter.encode(raw);

        // BCrypt genera salt aleatorio → mismo input, distinto hash
        assertNotEquals(hash1, hash2);
        // Pero ambos coinciden con el original
        assertTrue(adapter.matches(raw, hash1));
        assertTrue(adapter.matches(raw, hash2));
    }
}
