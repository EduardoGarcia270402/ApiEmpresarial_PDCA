package com.empresa.tasks.interfaces.rest;

import com.empresa.tasks.application.usecase.LoginUseCase;
import com.empresa.tasks.application.usecase.RegisterUserUseCase;
import com.empresa.tasks.domain.model.User;
import com.empresa.tasks.interfaces.rest.dto.AuthResponse;
import com.empresa.tasks.interfaces.rest.dto.LoginRequest;
import com.empresa.tasks.interfaces.rest.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Endpoints de autenticación y registro")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase,
                          LoginUseCase loginUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar usuario", description = "Crea un nuevo usuario en el sistema")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = registerUserUseCase.execute(request.email(), request.password(), request.name());
        LoginUseCase.LoginResult loginResult = loginUseCase.execute(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(loginResult.token(), loginResult.expiresIn(), loginResult.userId(), user.getEmail()));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario y retorna un token JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginUseCase.LoginResult loginResult = loginUseCase.execute(request.email(), request.password());
        return ResponseEntity.ok(new AuthResponse(
            loginResult.token(), loginResult.expiresIn(), loginResult.userId(), request.email()));
    }
}
