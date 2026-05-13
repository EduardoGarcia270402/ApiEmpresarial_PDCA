package com.empresa.tasks.infrastructure.persistence.adapter;

import com.empresa.tasks.application.port.out.UserRepositoryPort;
import com.empresa.tasks.domain.model.User;
import com.empresa.tasks.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.empresa.tasks.infrastructure.persistence.repository.UserJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public User save(User user) {
        return UserPersistenceMapper.toDomain(
                userJpaRepository.save(UserPersistenceMapper.toEntity(user))
        );
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }
}
