package com.empresa.tasks.application.usecase;

import com.empresa.tasks.application.exception.TaskNotFoundException;
import com.empresa.tasks.application.exception.UnauthorizedTaskAccessException;
import com.empresa.tasks.application.port.out.TaskRepositoryPort;
import com.empresa.tasks.domain.model.Task;

public class DeleteTaskUseCase {

    private final TaskRepositoryPort taskRepository;

    public DeleteTaskUseCase(TaskRepositoryPort taskRepository) {
        this.taskRepository = taskRepository;
    }

    public void execute(Long taskId, Long requestingUserId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        if (!task.getUserId().equals(requestingUserId)) {
            throw new UnauthorizedTaskAccessException();
        }

        taskRepository.deleteById(taskId);
    }
}
