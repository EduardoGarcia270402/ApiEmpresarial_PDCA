package com.empresa.tasks.application.usecase;

import com.empresa.tasks.application.port.out.TaskRepositoryPort;
import com.empresa.tasks.domain.model.Task;

public class CreateTaskUseCase {

    private final TaskRepositoryPort taskRepository;

    public CreateTaskUseCase(TaskRepositoryPort taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task execute(Long userId, String title, String description) {
        Task task = new Task(null, userId, title, description);
        return taskRepository.save(task);
    }
}