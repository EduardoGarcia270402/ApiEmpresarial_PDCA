# TICKET-005: Puertos y Casos de Uso

**Asignado:** Backend Lead - Eduardo Garcia
**Tipo:** Backend  
**Estado:** ✅ Completado  
**Tiempo estimado:** 3 horas  
**Dependencias:** TICKET-004

---

## ¿Qué se hizo?

Se implementó la capa `application/` de Clean Architecture. Esta capa define **qué puede hacer el sistema** (casos de uso) y **qué necesita del exterior** (puertos), sin saber cómo se implementa esa comunicación exterior.

---

## Puertos creados (`application/port/out/`)

Los puertos son **interfaces Java puras** que definen contratos. La implementación real vive en `infrastructure/` y será desarrollada por Backend 2.

| Puerto | Propósito |
|--------|-----------|
| `TaskRepositoryPort` | CRUD de tareas en la BD |
| `UserRepositoryPort` | Buscar y guardar usuarios |
| `TokenGeneratorPort` | Generar JWT para un usuario |
| `PasswordEncoderPort` | Hashear y verificar contraseñas |

### Ejemplo — `TaskRepositoryPort`:
```java
public interface TaskRepositoryPort {
    Task save(Task task);
    Optional<Task> findById(Long id);
    List<Task> findAllByUserId(Long userId);
    List<Task> findAllByUserIdAndStatus(Long userId, TaskStatus status);
    void deleteById(Long id);
}
```

---

## Excepciones de aplicación creadas (`application/exception/`)

| Excepción | Cuándo se lanza |
|-----------|----------------|
| `UserAlreadyExistsException` | Al registrar un email que ya existe |
| `TaskNotFoundException` | Al buscar una tarea con ID inexistente |
| `UnauthorizedTaskAccessException` | Al intentar modificar una tarea ajena |

---

## Casos de Uso creados (`application/usecase/`)

Cada caso de uso recibe sus dependencias **por constructor** (inyección explícita, sin anotaciones de Spring en esta capa).

### `RegisterUserUseCase`
- Verifica que el email no esté registrado
- Hashea la contraseña usando `PasswordEncoderPort`
- Guarda el usuario con `UserRepositoryPort`

### `LoginUseCase`
- Busca el usuario por email
- Verifica la contraseña con `PasswordEncoderPort`
- Genera y retorna el JWT con `TokenGeneratorPort`

### `CreateTaskUseCase`
- Crea una nueva tarea con estado `PENDING`
- La asocia al `userId` del token (nunca del request body)

### `ChangeTaskStatusUseCase` ⭐ (más importante)
Implementa la validación de **ownership**:

```
1. Buscar tarea por ID → si no existe: TaskNotFoundException
2. Verificar que task.userId == requestingUserId → si no: UnauthorizedTaskAccessException  
3. Delegar el cambio de estado a task.markAsCompleted() o task.cancel()
4. Persistir el resultado
```

> El backend **nunca confía** en que el frontend envíe el userId correcto. Siempre se extrae del JWT.

---

## Tests unitarios creados

**Archivo:** `src/test/java/com/empresa/tasks/application/ChangeTaskStatusUseCaseTest.java`

Usa **Mockito** para simular el `TaskRepositoryPort` sin necesitar base de datos real.

| Test | Qué valida |
|------|-----------|
| `deberiaCompletarTareaExitosamente` | Flujo feliz: tarea existe, usuario es dueño |
| `deberiaLanzarExcepcionSiTareaNoExiste` | ID inexistente lanza `TaskNotFoundException` |
| `deberiaLanzarExcepcionSiUsuarioNoEsDueno` | userId diferente lanza `UnauthorizedTaskAccessException` |

## Cómo se verificó

```bash
.\mvnw test -Dtest=ChangeTaskStatusUseCaseTest
```

**Resultado:** `Tests run: 3, Failures: 0, Errors: 0, Skipped: 0` ✅

---

## Factor McCall evaluado

**Reusabilidad** — Los puertos permiten intercambiar implementaciones (PostgreSQL, H2, mock) sin tocar la lógica de negocio. **Integridad** — La validación de ownership garantiza que ningún usuario pueda modificar datos ajenos.