package com.empresa.tasks.application.usecase;

import com.empresa.tasks.application.port.out.PasswordEncoderPort;
import com.empresa.tasks.application.port.out.TokenGeneratorPort;
import com.empresa.tasks.application.port.out.UserRepositoryPort;
import com.empresa.tasks.domain.model.User;

public class LoginUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenGeneratorPort tokenGenerator;

    public LoginUseCase(UserRepositoryPort userRepository,
                        PasswordEncoderPort passwordEncoder,
                        TokenGeneratorPort tokenGenerator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenGenerator = tokenGenerator;
    }

    public record LoginResult(String token, long expiresIn) {}

    /**
     * Autentica al usuario por email/password.
     * Retorna el JWT si las credenciales son válidas.
     * Lanza IllegalArgumentException si las credenciales son incorrectas.
     */
    public LoginResult execute(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        String token = tokenGenerator.generateToken(user);
        return new LoginResult(token, 7200);
    }
}
