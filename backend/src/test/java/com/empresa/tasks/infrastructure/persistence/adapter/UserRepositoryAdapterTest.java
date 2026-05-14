package com.empresa.tasks.infrastructure.persistence.adapter;

import com.empresa.tasks.domain.model.User;
import com.empresa.tasks.infrastructure.persistence.entity.UserJpaEntity;
import com.empresa.tasks.infrastructure.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    private UserRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new UserRepositoryAdapter(userJpaRepository);
    }

    @Test
    void deberiaGuardarUsuario() {
        User user = new User(null, "test@test.com", "hash", "Test");
        UserJpaEntity savedEntity = new UserJpaEntity(1L, "test@test.com", "hash", "Test");
        when(userJpaRepository.save(any(UserJpaEntity.class))).thenReturn(savedEntity);

        User result = adapter.save(user);

        assertEquals(1L, result.getId());
        assertEquals("test@test.com", result.getEmail());
        verify(userJpaRepository).save(any(UserJpaEntity.class));
    }

    @Test
    void deberiaEncontrarUsuarioPorEmail() {
        UserJpaEntity entity = new UserJpaEntity(1L, "test@test.com", "hash", "Test");
        when(userJpaRepository.findByEmail("test@test.com")).thenReturn(Optional.of(entity));

        Optional<User> result = adapter.findByEmail("test@test.com");

        assertTrue(result.isPresent());
        assertEquals("test@test.com", result.get().getEmail());
    }

    @Test
    void deberiaRetornarVacioSiNoExisteEmail() {
        when(userJpaRepository.findByEmail("no@existe.com")).thenReturn(Optional.empty());

        Optional<User> result = adapter.findByEmail("no@existe.com");

        assertFalse(result.isPresent());
    }

    @Test
    void deberiaVerificarSiExisteEmail() {
        when(userJpaRepository.existsByEmail("test@test.com")).thenReturn(true);
        when(userJpaRepository.existsByEmail("no@existe.com")).thenReturn(false);

        assertTrue(adapter.existsByEmail("test@test.com"));
        assertFalse(adapter.existsByEmail("no@existe.com"));
    }
}
