package com.empresa.tasks.application.port.out;

import com.empresa.tasks.domain.model.User;

public interface TokenGeneratorPort {
    String generateToken(User user);
    boolean validate(String token);
}