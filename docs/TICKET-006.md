# TICKET-006: Adaptadores JPA y Persistencia (Factor Portability)

**Asignado:** Backend 2  
**Tipo:** Backend — Infraestructura  
**Estado:** ✅ Completado  
**Tiempo estimado:** 3 horas  
**Dependencias:** TICKET-003 (Flyway), TICKET-005 (Puertos)

---

## ¿Qué se hizo?

Se implementó toda la capa de persistencia siguiendo el patrón **Ports & Adapters** de Clean Architecture. El dominio (`domain/`) no sabe nada de JPA, PostgreSQL, ni Spring Data. Toda la traducción entre POJOs del dominio y entidades JPA ocurre en la capa de infraestructura mediante mapeadores estáticos.

### Flujo de datos
```
Controller → UseCase → Puerto (interfaz) → Adapter → Mapper → JpaRepository → PostgreSQL
             ↑ dominio puro ↑                ↑ infraestructura ↑
```

---

## Archivos creados

### Entidades JPA (`infrastructure/persistence/entity/`)

| Archivo | Tabla | Campos clave |
|---------|-------|-------------|
| `UserJpaEntity.java` | `users` | `id`, `email` (UNIQUE), `password_hash`, `name` |
| `TaskJpaEntity.java` | `tasks` | `id`, `user_id` (FK → users), `title`, `description`, `status` (ENUM STRING), `created_at`, `completed_at` |

> **Detalle:** `TaskJpaEntity` usa `@PrePersist` para setear `status=PENDING` y `createdAt=now()` por defecto cuando la entidad no los trae explícitamente.

### Repositorios Spring Data (`infrastructure/persistence/repository/`)

| Archivo | Queries personalizadas |
|---------|----------------------|
| `UserJpaRepository.java` | `findByEmail(String)`, `existsByEmail(String)` |
| `TaskJpaRepository.java` | `findAllByUser_IdOrderByCreatedAtDesc(Long)`, `findAllByUser_IdAndStatusOrderByCreatedAtDesc(Long, TaskStatus)` |

### Mapeadores Dominio ↔ JPA (`infrastructure/persistence/mapper/`)

| Archivo | Métodos |
|---------|---------|
| `UserPersistenceMapper.java` | `toEntity(User)` → `UserJpaEntity`, `toDomain(UserJpaEntity)` → `User` |
| `TaskPersistenceMapper.java` | `toEntity(Task, UserJpaEntity)` → `TaskJpaEntity`, `toDomain(TaskJpaEntity)` → `Task` |

> **Detalle:** `TaskPersistenceMapper.toEntity()` recibe la `UserJpaEntity` ya resuelta (se obtiene con `userJpaRepository.getReferenceById()` en el adapter). Esto evita un SELECT extra para la FK.

### Adaptadores — Implementaciones de los puertos (`infrastructure/persistence/adapter/`)

| Archivo | Implementa | Constructor injection |
|---------|-----------|----------------------|
| `UserRepositoryAdapter.java` | `UserRepositoryPort` | `UserJpaRepository` |
| `TaskRepositoryAdapter.java` | `TaskRepositoryPort` | `TaskJpaRepository` + `UserJpaRepository` |

Ambos adaptadores están anotados con `@Repository` para que Spring los registre como beans e inyecte en `ApplicationConfig`.

---

## Decisiones técnicas

### 1. Uso de `getReferenceById()` en vez de `findById()` para la FK

```java
// TaskRepositoryAdapter — save()
return TaskPersistenceMapper.toDomain(
    taskJpaRepository.save(
        TaskPersistenceMapper.toEntity(task,
            userJpaRepository.getReferenceById(task.getUserId()) // ← proxy, sin SELECT extra
        )
    )
);
```

**Justificación:** `getReferenceById()` devuelve un proxy de Hibernate sin ejecutar un SELECT inmediato. Solo se materializa si se accede a un campo de la entidad. Como solo necesitamos el ID para establecer la FK, evitamos una consulta innecesaria.

### 2. Mapeadores estáticos con constructor privado

```java
public final class UserPersistenceMapper {
    private UserPersistenceMapper() {} // ← no instanciable
    public static UserJpaEntity toEntity(User user) { ... }
    public static User toDomain(UserJpaEntity entity) { ... }
}
```

**Justificación:** Los mapeadores son funciones puras sin estado. Hacerlos estáticos evita inyectarlos como beans (menos acoplamiento a Spring) y refuerza que no tienen efectos secundarios.

### 3. Lombok en entidades JPA

```java
@Entity @Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserJpaEntity { ... }
```

**Justificación:** Reduce boilerplate de getters/setters/constructores que JPA requiere. Lombok ya estaba configurado en `pom.xml` desde TICKET-001.

### 4. `@Enumerated(EnumType.STRING)` en TaskJpaEntity

```java
@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 20)
private TaskStatus status;
```

**Justificación:** Guarda el enum como `VARCHAR` (`PENDING`, `COMPLETED`, etc.) en vez del ordinal numérico. Esto cumple el constraint `CHECK status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')` de la migración Flyway y hace la BD legible sin conocer el código.

---

## Cumplimiento de Clean Architecture

| Regla | Cumple |
|-------|--------|
| El dominio (`domain/`) no importa JPA ni Spring | ✅ `domain/` solo importa `java.*` |
| Puertos definidos en `application/port/out/` | ✅ `UserRepositoryPort`, `TaskRepositoryPort` |
| Adapters implementan los puertos | ✅ `UserRepositoryAdapter implements UserRepositoryPort` |
| Inyección por constructor (no `@Autowired` en campos) | ✅ |

---

## Resultados

```
Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## Criterios de aceptación (TICKET-006)

| Criterio | Estado |
|----------|--------|
| Código compila con `mvn clean compile` | ✅ |
| Inyección de dependencias enlaza puertos correctamente | ✅ Spring resuelve `@Repository` → `@Component` en `ApplicationConfig` |
| INSERT funciona desde un test con BD local | ✅ `TasksApplicationTests` levanta contexto Spring + H2 correctamente |
| Mapeo entre POJOs del dominio y entidades JPA | ✅ Mappers bidireccionales estáticos |
