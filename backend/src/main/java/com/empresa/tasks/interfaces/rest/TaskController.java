package com.empresa.tasks.interfaces.rest;

import com.empresa.tasks.application.usecase.ChangeTaskStatusUseCase;
import com.empresa.tasks.application.usecase.CreateTaskUseCase;
import com.empresa.tasks.application.usecase.DeleteTaskUseCase;
import com.empresa.tasks.application.port.out.TaskRepositoryPort;
import com.empresa.tasks.domain.model.Task;
import com.empresa.tasks.interfaces.rest.dto.CreateTaskRequest;
import com.empresa.tasks.interfaces.rest.dto.TaskResponse;
import com.empresa.tasks.interfaces.rest.dto.UpdateTaskStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Tasks", description = "Gestión de tareas (requiere autenticación)")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final CreateTaskUseCase createTaskUseCase;
    private final ChangeTaskStatusUseCase changeTaskStatusUseCase;
    private final DeleteTaskUseCase deleteTaskUseCase;
    private final TaskRepositoryPort taskRepository;

    public TaskController(CreateTaskUseCase createTaskUseCase,
                          ChangeTaskStatusUseCase changeTaskStatusUseCase,
                          DeleteTaskUseCase deleteTaskUseCase,
                          TaskRepositoryPort taskRepository) {
        this.createTaskUseCase = createTaskUseCase;
        this.changeTaskStatusUseCase = changeTaskStatusUseCase;
        this.deleteTaskUseCase = deleteTaskUseCase;
        this.taskRepository = taskRepository;
    }

    @GetMapping
    @Operation(summary = "Listar tareas", description = "Obtiene todas las tareas del usuario autenticado")
    public ResponseEntity<List<TaskResponse>> findAll() {
        Long userId = getCurrentUserId();
        List<Task> tasks = taskRepository.findAllByUserId(userId);
        return ResponseEntity.ok(tasks.stream().map(TaskResponse::from).toList());
    }

    @PostMapping
    @Operation(summary = "Crear tarea", description = "Crea una nueva tarea para el usuario autenticado")
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        Long userId = getCurrentUserId();
        Task task = createTaskUseCase.execute(userId, request.title(),
                request.description() != null ? request.description() : "");
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(task));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cambiar estado", description = "Cambia el estado de una tarea (COMPLETED, CANCELLED)")
    public ResponseEntity<TaskResponse> updateStatus(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateTaskStatusRequest request) {
        Long userId = getCurrentUserId();
        Task task = changeTaskStatusUseCase.execute(id, userId, request.status());
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tarea", description = "Elimina una tarea del usuario autenticado")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        deleteTaskUseCase.execute(id, userId);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
