package com.empresa.tasks.infrastructure.security;

import com.empresa.tasks.domain.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios del JwtTokenAdapter.
 * Verifica generación y extracción de tokens sin levantar Spring.
 */
class JwtTokenAdapterTest {

    // Clave de al menos 256 bits (32 chars) para HS256
    private static final String SECRET = "test_secret_key_minimum_256_bits_for_hs256!";
    private static final long EXPIRATION_MS = 7200000; // 2h

    private final JwtTokenAdapter adapter = new JwtTokenAdapter(SECRET, EXPIRATION_MS);

    @Test
    void shouldGenerateTokenAndExtractUserId() {
        User user = new User(42L, "test@mail.com", "hash", "Test User");

        String token = adapter.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(42L, adapter.extractUserId(token));
    }

    @Test
    void shouldReturnNullForInvalidToken() {
        assertNull(adapter.extractUserId("invalid.token.here"));
    }

    @Test
    void shouldReturnNullForTamperedToken() {
        User user = new User(1L, "a@b.com", "hash", "A");
        String token = adapter.generateToken(user);

        // Tamper: cambiar el último carácter
        String tampered = token.substring(0, token.length() - 1) + "X";

        assertNull(adapter.extractUserId(tampered));
    }

    @Test
    void shouldReturnNullForExpiredToken() {
        // Adapter con expiración de 0ms → token expira inmediatamente
        JwtTokenAdapter expiredAdapter = new JwtTokenAdapter(SECRET, 0);
        User user = new User(1L, "a@b.com", "hash", "A");

        String token = expiredAdapter.generateToken(user);

        // El token ya expiró
        assertNull(expiredAdapter.extractUserId(token));
    }
}
