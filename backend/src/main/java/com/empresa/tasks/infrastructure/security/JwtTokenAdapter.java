package com.empresa.tasks.infrastructure.security;

import com.empresa.tasks.application.port.out.TokenGeneratorPort;
import com.empresa.tasks.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenAdapter implements TokenGeneratorPort {

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenAdapter(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:7200000}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    @Override
    public String generateToken(User user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Extrae el userId (subject) del token.
     * Retorna null si el token es inválido o expirado.
     */
    public Long extractUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.valueOf(claims.getSubject());
        } catch (JwtException | NumberFormatException e) {
            return null;
        }
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
