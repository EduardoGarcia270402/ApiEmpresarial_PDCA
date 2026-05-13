package com.empresa.tasks.infrastructure.persistence.repository;

import com.empresa.tasks.domain.model.TaskStatus;
import com.empresa.tasks.infrastructure.persistence.entity.TaskJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskJpaRepository extends JpaRepository<TaskJpaEntity, Long> {
    List<TaskJpaEntity> findAllByUser_IdOrderByCreatedAtDesc(Long userId);
    List<TaskJpaEntity> findAllByUser_IdAndStatusOrderByCreatedAtDesc(Long userId, TaskStatus status);
}
