package com.empresa.tasks.infrastructure.persistence.mapper;

import com.empresa.tasks.domain.model.User;
import com.empresa.tasks.infrastructure.persistence.entity.UserJpaEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserPersistenceMapperTest {

    @Test
    void deberiaConvertirDominioAEntidad() {
        User user = new User(1L, "test@test.com", "hash123", "Test User");

        UserJpaEntity entity = UserPersistenceMapper.toEntity(user);

        assertEquals(1L, entity.getId());
        assertEquals("test@test.com", entity.getEmail());
        assertEquals("hash123", entity.getPasswordHash());
        assertEquals("Test User", entity.getName());
    }

    @Test
    void deberiaConvertirEntidadADominio() {
        UserJpaEntity entity = new UserJpaEntity(1L, "test@test.com", "hash123", "Test User");

        User user = UserPersistenceMapper.toDomain(entity);

        assertEquals(1L, user.getId());
        assertEquals("test@test.com", user.getEmail());
        assertEquals("hash123", user.getPasswordHash());
        assertEquals("Test User", user.getName());
    }
}
