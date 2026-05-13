package com.empresa.tasks.application.port.out;

import com.empresa.tasks.domain.model.Task;
import com.empresa.tasks.domain.model.TaskStatus;
import java.util.List;
import java.util.Optional;

public interface TaskRepositoryPort {
    Task save(Task task);
    Optional<Task> findById(Long id);
    List<Task> findAllByUserId(Long userId);
    List<Task> findAllByUserIdAndStatus(Long userId, TaskStatus status);
    void deleteById(Long id);
}