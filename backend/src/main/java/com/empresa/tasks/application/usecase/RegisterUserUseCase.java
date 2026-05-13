package com.empresa.tasks.application.usecase;

import com.empresa.tasks.application.exception.UserAlreadyExistsException;
import com.empresa.tasks.application.port.out.PasswordEncoderPort;
import com.empresa.tasks.application.port.out.UserRepositoryPort;
import com.empresa.tasks.domain.model.User;

public class RegisterUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;

    public RegisterUserUseCase(UserRepositoryPort userRepository,
                                PasswordEncoderPort passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User execute(String email, String password, String name) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(email);
        }
        User user = new User(null, email, passwordEncoder.encode(password), name);
        return userRepository.save(user);
    }
}