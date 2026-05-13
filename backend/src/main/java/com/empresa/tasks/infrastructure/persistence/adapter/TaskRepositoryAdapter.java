package com.empresa.tasks.infrastructure.persistence.adapter;

import com.empresa.tasks.application.port.out.TaskRepositoryPort;
import com.empresa.tasks.domain.model.Task;
import com.empresa.tasks.domain.model.TaskStatus;
import com.empresa.tasks.infrastructure.persistence.mapper.TaskPersistenceMapper;
import com.empresa.tasks.infrastructure.persistence.repository.TaskJpaRepository;
import com.empresa.tasks.infrastructure.persistence.repository.UserJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class TaskRepositoryAdapter implements TaskRepositoryPort {

    private final TaskJpaRepository taskJpaRepository;
    private final UserJpaRepository userJpaRepository;

    public TaskRepositoryAdapter(TaskJpaRepository taskJpaRepository,
                                 UserJpaRepository userJpaRepository) {
        this.taskJpaRepository = taskJpaRepository;
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public Task save(Task task) {
        return TaskPersistenceMapper.toDomain(
                taskJpaRepository.save(
                        TaskPersistenceMapper.toEntity(task, userJpaRepository.getReferenceById(task.getUserId()))
                )
        );
    }

    @Override
    public Optional<Task> findById(Long id) {
        return taskJpaRepository.findById(id)
                .map(TaskPersistenceMapper::toDomain);
    }

    @Override
    public List<Task> findAllByUserId(Long userId) {
        return taskJpaRepository.findAllByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(TaskPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Task> findAllByUserIdAndStatus(Long userId, TaskStatus status) {
        return taskJpaRepository.findAllByUser_IdAndStatusOrderByCreatedAtDesc(userId, status)
                .stream()
                .map(TaskPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        taskJpaRepository.deleteById(id);
    }
}
