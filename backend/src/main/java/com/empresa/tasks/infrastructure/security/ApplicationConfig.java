package com.empresa.tasks.infrastructure.security;

import com.empresa.tasks.application.port.out.PasswordEncoderPort;
import com.empresa.tasks.application.port.out.TaskRepositoryPort;
import com.empresa.tasks.application.port.out.TokenGeneratorPort;
import com.empresa.tasks.application.port.out.UserRepositoryPort;
import com.empresa.tasks.application.usecase.ChangeTaskStatusUseCase;
import com.empresa.tasks.application.usecase.CreateTaskUseCase;
import com.empresa.tasks.application.usecase.DeleteTaskUseCase;
import com.empresa.tasks.application.usecase.LoginUseCase;
import com.empresa.tasks.application.usecase.RegisterUserUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra los casos de uso como beans de Spring.
 * Los UseCases son POJOs puros (sin @Component) para respetar Clean Architecture,
 * por lo que se instancian manualmente inyectando sus puertos.
 */
@Configuration
public class ApplicationConfig {

    @Bean
    public LoginUseCase loginUseCase(UserRepositoryPort userRepo,
                                     PasswordEncoderPort encoder,
                                     TokenGeneratorPort tokenGenerator) {
        return new LoginUseCase(userRepo, encoder, tokenGenerator);
    }

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserRepositoryPort userRepo,
                                                    PasswordEncoderPort encoder) {
        return new RegisterUserUseCase(userRepo, encoder);
    }

    @Bean
    public CreateTaskUseCase createTaskUseCase(TaskRepositoryPort taskRepo) {
        return new CreateTaskUseCase(taskRepo);
    }

    @Bean
    public ChangeTaskStatusUseCase changeTaskStatusUseCase(TaskRepositoryPort taskRepo) {
        return new ChangeTaskStatusUseCase(taskRepo);
    }

    @Bean
    public DeleteTaskUseCase deleteTaskUseCase(TaskRepositoryPort taskRepo) {
        return new DeleteTaskUseCase(taskRepo);
    }
}
