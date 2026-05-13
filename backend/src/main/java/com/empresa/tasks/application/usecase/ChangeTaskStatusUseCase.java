package com.empresa.tasks.application.usecase;

import com.empresa.tasks.application.exception.TaskNotFoundException;
import com.empresa.tasks.application.exception.UnauthorizedTaskAccessException;
import com.empresa.tasks.application.port.out.TaskRepositoryPort;
import com.empresa.tasks.domain.model.Task;
import com.empresa.tasks.domain.model.TaskStatus;

public class ChangeTaskStatusUseCase {

    private final TaskRepositoryPort taskRepository;

    public ChangeTaskStatusUseCase(TaskRepositoryPort taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task execute(Long taskId, Long requestingUserId, TaskStatus newStatus) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        if (!task.getUserId().equals(requestingUserId)) {
            throw new UnauthorizedTaskAccessException();
        }

        if (newStatus == TaskStatus.COMPLETED) {
            task.markAsCompleted();
        } else if (newStatus == TaskStatus.CANCELLED) {
            task.cancel();
        }

        return taskRepository.save(task);
    }
}