package com.empresa.tasks.infrastructure.persistence.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserJpaEntityTest {

    @Test
    void deberiaCrearConConstructorAllArgs() {
        UserJpaEntity entity = new UserJpaEntity(1L, "test@test.com", "hash", "Name");

        assertEquals(1L, entity.getId());
        assertEquals("test@test.com", entity.getEmail());
        assertEquals("hash", entity.getPasswordHash());
        assertEquals("Name", entity.getName());
    }

    @Test
    void deberiaCrearConConstructorNoArgs() {
        UserJpaEntity entity = new UserJpaEntity();

        assertNull(entity.getId());
        assertNull(entity.getEmail());
    }

    @Test
    void deberiaFuncionarSetters() {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(5L);
        entity.setEmail("new@test.com");
        entity.setPasswordHash("newHash");
        entity.setName("New Name");

        assertEquals(5L, entity.getId());
        assertEquals("new@test.com", entity.getEmail());
        assertEquals("newHash", entity.getPasswordHash());
        assertEquals("New Name", entity.getName());
    }
}
