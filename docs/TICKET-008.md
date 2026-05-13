# TICKET-008: Controladores REST y Swagger (Factor Interoperability)

**Asignado:** Backend 2  
**Tipo:** Backend — API REST  
**Estado:** ✅ Completado  
**Tiempo estimado:** 3 horas  
**Dependencias:** TICKET-007 (Seguridad JWT)

---

## ¿Qué se hizo?

Se implementó la capa de exposición REST completa con 6 endpoints, DTOs con validación Jakarta, manejo global de excepciones mapeadas a HTTP, y documentación Swagger/OpenAPI. Se creó también el `DeleteTaskUseCase` para el endpoint DELETE con verificación de ownership. Se agregó `userId` al `LoginResult` del `LoginUseCase` para que el frontend conozca el ID del usuario autenticado.

### Flujo de una petición

```
Cliente → AuthController/TaskController → DTO (@Valid) → UseCase → Puerto → Adapter → BD
                                              ↓ excepción
                                     GlobalExceptionHandler → ProblemDetail JSON
```

---

## Archivos creados / modificados

### Nuevos archivos (11):

#### DTOs (`interfaces/rest/dto/`)

| Archivo | Validaciones |
|---------|-------------|
| `RegisterRequest.java` | `@Email`, `@NotBlank`, `@Size(min=6)` en password |
| `LoginRequest.java` | `@Email`, `@NotBlank` en email y password |
| `AuthResponse.java` | `token`, `expiresIn`, `userId`, `email` |
| `CreateTaskRequest.java` | `@NotBlank`, `@Size(min=3, max=200)` en title; `@Size(max=2000)` en description |
| `TaskResponse.java` | `from(Task)` factory method — transforma dominio a DTO |
| `UpdateTaskStatusRequest.java` | `@NotNull` en status |

#### Controladores REST (`interfaces/rest/`)

| Archivo | Endpoints | Auth |
|---------|-----------|------|
| `AuthController.java` | `POST /api/v1/auth/register`, `POST /api/v1/auth/login` | Público |
| `TaskController.java` | `GET /api/v1/tasks`, `POST /api/v1/tasks`, `PATCH /api/v1/tasks/{id}/status`, `DELETE /api/v1/tasks/{id}` | Requiere Bearer token |

#### Manejo de excepciones

| Archivo | Descripción |
|---------|------------|
| `GlobalExceptionHandler.java` | `@RestControllerAdvice` — mapea 6 tipos de excepción a `ProblemDetail` (RFC 7807) |

#### Caso de uso nuevo

| Archivo | Capa | Responsabilidad |
|---------|------|----------------|
| `DeleteTaskUseCase.java` | `application/usecase` | Verifica ownership del task, lanza `TaskNotFoundException` o `UnauthorizedTaskAccessException`, luego elimina |

#### Configuración Swagger

| Archivo | Descripción |
|---------|------------|
| `OpenApiConfig.java` | `infrastructure/config` — bean `OpenAPI` con esquema de seguridad `bearerAuth` (JWT) |

### Archivos modificados (3):

| Archivo | Cambio |
|---------|--------|
| `LoginUseCase.java` | Agregado `Long userId` al record `LoginResult` |
| `ApplicationConfig.java` | Registrado bean `DeleteTaskUseCase` |
| `pom.xml` | Agregada dependencia `springdoc-openapi-starter-webmvc-ui:3.0.3` |

---

## Decisiones técnicas y justificaciones

### 1. DTOs como Java `record`

```java
public record RegisterRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 6) String password,
    @NotBlank String name
) {}
```

**Justificación:** Los records de Java son inmutables por diseño, reducen boilerplate (sin getters/setters manuales) y son ideales para DTOs de entrada/salida. Las anotaciones `@Valid` de Jakarta se aplican directamente sobre los componentes del record.

### 2. `LoginResult` incluye `userId`

```java
public record LoginResult(String token, long expiresIn, Long userId) {}
```

**Justificación:** El frontend necesita el `userId` para construir la UI (ej. mostrar "Mis tareas"). Sin este campo, el cliente tendría que decodificar el JWT manualmente. Agregarlo al `LoginResult` es un cambio mínimo (un campo extra en el record) que evita acoplar el frontend al formato interno del token.

### 3. `TaskController` obtiene el `userId` del `SecurityContext`

```java
private Long getCurrentUserId() {
    return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
}
```

**Justificación:** El `JwtAuthenticationFilter` (TICKET-007) extrae el `userId` del token JWT y lo establece como principal del `SecurityContext`. Cada endpoint del `TaskController` usa este método para saber **quién** está haciendo la petición, filtrando tareas por ownership sin necesidad de pasar el `userId` como parámetro explícito. Esto previene ataques de IDOR (Insecure Direct Object Reference).

### 4. `GlobalExceptionHandler` con `ProblemDetail` (RFC 7807)

```java
@ExceptionHandler(UserAlreadyExistsException.class)
public ResponseEntity<ProblemDetail> handleUserAlreadyExists(UserAlreadyExistsException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    pd.setTitle("Conflicto de registro");
    return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
}
```

**Justificación:** `ProblemDetail` es el estándar de Spring Boot 4 para respuestas de error estructuradas. Reemplaza al antiguo mapa `Map<String, String>` y cumple con RFC 7807. Cada excepción de negocio se mapea a un código HTTP semántico:

| Excepción | HTTP Status |
|-----------|------------|
| `UserAlreadyExistsException` | 409 Conflict |
| `InvalidStatusTransitionException` | 409 Conflict |
| `TaskNotFoundException` | 404 Not Found |
| `UnauthorizedTaskAccessException` | 403 Forbidden |
| `IllegalArgumentException` (credenciales) | 401 Unauthorized |
| `MethodArgumentNotValidException` | 400 Bad Request |

### 5. Swagger con esquema Bearer JWT

```java
.components(new Components()
    .addSecuritySchemes("bearerAuth", new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT")));
```

**Justificación:** El `@SecurityRequirement(name = "bearerAuth")` en `TaskController` indica a Swagger UI que esos endpoints requieren el token. El usuario puede hacer clic en "Authorize" en Swagger UI, pegar su token JWT, y todos los endpoints protegidos lo enviarán automáticamente en el header `Authorization: Bearer <token>`.

### 6. `DeleteTaskUseCase` — ownership antes de eliminar

```java
Task task = taskRepository.findById(taskId)
    .orElseThrow(() -> new TaskNotFoundException(taskId));
if (!task.getUserId().equals(requestingUserId)) {
    throw new UnauthorizedTaskAccessException();
}
taskRepository.deleteById(taskId);
```

**Justificación:** Sigue el mismo patrón que `ChangeTaskStatusUseCase` (TICKET-005): fetch → verificar ownership → operar. La verificación de ownership **siempre** ocurre en la capa de aplicación (use case), nunca en el controller. Esto garantiza que ningún adapter o futuro consumer pueda saltarse la regla de negocio.

---

## Endpoints expuestos

| Método | Ruta | Auth | Request Body | Response | Códigos |
|--------|------|------|-------------|----------|---------|
| `POST` | `/api/v1/auth/register` | No | `RegisterRequest` | `AuthResponse` | 201, 400, 409 |
| `POST` | `/api/v1/auth/login` | No | `LoginRequest` | `AuthResponse` | 200, 401 |
| `GET` | `/api/v1/tasks` | Sí | — | `List<TaskResponse>` | 200, 401 |
| `POST` | `/api/v1/tasks` | Sí | `CreateTaskRequest` | `TaskResponse` | 201, 400, 401 |
| `PATCH` | `/api/v1/tasks/{id}/status` | Sí | `UpdateTaskStatusRequest` | `TaskResponse` | 200, 400, 401, 403, 404, 409 |
| `DELETE` | `/api/v1/tasks/{id}` | Sí | — | 204 No Content | 204, 401, 403, 404 |

---

## Resultados de ejecución

```
Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

- `mvnw clean compile`: ✅ 45 source files, 0 errors
- Tests existentes: 25/25 ✅ sin regresiones
- Swagger UI: Configurado (accesible en `http://localhost:8080/swagger-ui.html`)

---

## Criterios de aceptación (TICKET-008)

| Criterio | Estado |
|----------|--------|
| Swagger UI disponible y documentando todos los endpoints | ✅ `OpenApiConfig` + anotaciones en controllers |
| Endpoints retornan JSON con códigos HTTP correctos | ✅ `ResponseEntity` + `GlobalExceptionHandler` |
| `POST /api/v1/auth/register` | ✅ Implementado |
| `POST /api/v1/auth/login` | ✅ Implementado |
| `GET /api/v1/tasks` | ✅ Implementado (filtrado por userId del token) |
| `POST /api/v1/tasks` | ✅ Implementado |
| `PATCH /api/v1/tasks/{id}/status` | ✅ Implementado |
| `DELETE /api/v1/tasks/{id}` | ✅ Implementado con `DeleteTaskUseCase` |
| DTOs con `@Valid` | ✅ Records con Jakarta Validation |
| `GlobalExceptionHandler` mapea excepciones a HTTP | ✅ 6 handlers con `ProblemDetail` |
| Código compila sin errores | ✅ `mvnw clean compile` exitoso |
| Tests existentes no rotos | ✅ 25/25 pasan |

---

## Corrección post-implementación: Swagger UI 403

Al intentar acceder a `http://localhost:8080/swagger-ui.html`, Spring Security retornaba HTTP 403. **Causa raíz:** el patrón AntPathMatcher `/swagger-ui/**` no matchea `/swagger-ui.html` porque no hay `/` entre `swagger-ui` y `html`. La URL pública de entrada a Swagger es `/swagger-ui.html` (que redirige a `/swagger-ui/index.html`), por lo que era bloqueada por `anyRequest().authenticated()`.

### Cambio realizado

| Archivo | Cambio |
|---------|--------|
| `SecurityConfig.java` | `.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()` |

Agregado `/swagger-ui.html` explícitamente antes del wildcard.

## Corrección post-implementación: Quality y manejo de 403

Se detectaron tres carencias en la documentación Swagger y el manejo de errores 403:

1. **`QualityController`** — sin `@SecurityRequirement`, `@Tag`, ni `@Operation`. Swagger no mostraba candado ni descripción.
2. **`SecurityConfig`** — `/api/v1/quality/**` estaba como ruta pública. Ahora requiere autenticación.
3. **`GlobalExceptionHandler`** — no capturaba `AccessDeniedException` de Spring Security. Si el filtro rechazaba la petición, el 403 se escapaba sin `ProblemDetail`.

### Cambios realizados

| Archivo | Cambio |
|---------|--------|
| `QualityController.java` | +`@Tag`, +`@Operation`, +`@SecurityRequirement(name = "bearerAuth")` |
| `SecurityConfig.java` | Eliminado `.requestMatchers("/api/v1/quality/**").permitAll()` — ahora requiere token |
| `GlobalExceptionHandler.java` | +`@ExceptionHandler(AccessDeniedException.class)` → 403 con `ProblemDetail` |

### Matriz de excepciones → HTTP (completa)

| Excepción | HTTP | RFC 7807 Title |
|-----------|------|----------------|
| `UserAlreadyExistsException` | 409 Conflict | Conflicto de registro |
| `InvalidStatusTransitionException` | 409 Conflict | Transición de estado inválida |
| `TaskNotFoundException` | 404 Not Found | Tarea no encontrada |
| `UnauthorizedTaskAccessException` | 403 Forbidden | Acceso no autorizado |
| `AccessDeniedException` | 403 Forbidden | Acceso denegado |
| `IllegalArgumentException` (credenciales) | 401 Unauthorized | Autenticación fallida |
| `MethodArgumentNotValidException` | 400 Bad Request | Error de validación |

---

## Próximos pasos

- **TICKET-009** (Quality Scoring Engine): Ya está implementado (`QualityScoringEngine` + `QualityController` en `/api/v1/quality/score`)
- **TICKET-010** (Pipeline CI/CD): GitHub Actions workflow
- **TICKET-011** (Pruebas Unitarias >85%): Ampliar cobertura de tests
- **TICKET-012** (Colección Postman): Smoke test del backend
