package com.empresa.tasks.domain;

import com.empresa.tasks.domain.model.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserDomainTest {

    @Test
    void deberiaCrearUsuarioCorrectamente() {
        User user = new User(1L, "test@test.com", "hashedPass", "Juan");

        assertEquals(1L, user.getId());
        assertEquals("test@test.com", user.getEmail());
        assertEquals("hashedPass", user.getPasswordHash());
        assertEquals("Juan", user.getName());
    }

    @Test
    void deberiaPermitirModificarCampos() {
        User user = new User();
        user.setId(2L);
        user.setEmail("nuevo@test.com");
        user.setPasswordHash("newHash");
        user.setName("Maria");

        assertEquals(2L, user.getId());
        assertEquals("nuevo@test.com", user.getEmail());
        assertEquals("newHash", user.getPasswordHash());
        assertEquals("Maria", user.getName());
    }
}