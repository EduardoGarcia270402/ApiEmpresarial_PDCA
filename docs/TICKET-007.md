# TICKET-007: Seguridad JWT y Filtros (Factor Integrity)

**Asignado:** Frontend Developer (apoyo al Backend 2)  
**Tipo:** Backend — Seguridad  
**Estado:** ✅ Completado  
**Tiempo estimado:** 4 horas  
**Dependencias:** TICKET-005 (Puertos), TICKET-006 (en paralelo)

---

## ¿Qué se hizo?

Se implementó toda la capa de seguridad del backend siguiendo la especificación §12 (Seguridad) y los principios de Clean Architecture. Esto incluye:

1. **Autenticación JWT stateless** con JJWT 0.12.6
2. **Hashing de contraseñas** con BCrypt (cost 10)
3. **Filtro de autenticación** que puebla el SecurityContext
4. **Configuración de Spring Security** con rutas públicas y protegidas
5. **Implementación del LoginUseCase** (que estaba vacío)
6. **Registro de UseCases como beans de Spring** (ApplicationConfig)

---

## Archivos creados / modificados

### Nuevos archivos:

| Archivo | Capa | Propósito |
|---------|------|-----------|
| `BCryptAdapter.java` | infrastructure/security | Implementa `PasswordEncoderPort` — BCrypt cost 10 |
| `JwtTokenAdapter.java` | infrastructure/security | Implementa `TokenGeneratorPort` + `extractUserId()` — HS256 |
| `JwtAuthenticationFilter.java` | infrastructure/security | `OncePerRequestFilter` — extrae Bearer token y puebla SecurityContext |
| `SecurityConfig.java` | infrastructure/security | Spring Security config: stateless, CORS, rutas públicas/protegidas |
| `ApplicationConfig.java` | infrastructure/security | Registra UseCases como beans inyectando puertos |
| `LoginUseCaseTest.java` | test/application | 3 tests unitarios del LoginUseCase |
| `JwtTokenAdapterTest.java` | test/infrastructure/security | 4 tests: generación, extracción, token inválido, token expirado |
| `BCryptAdapterTest.java` | test/infrastructure/security | 4 tests: encode, match correcto, match incorrecto, salt aleatorio |

### Archivos modificados:

| Archivo | Cambio |
|---------|--------|
| `pom.xml` | +`spring-boot-starter-security`, +`jjwt-api/impl/jackson` 0.12.6, +`spring-security-test` |
| `application.properties` | +`jwt.secret`, +`jwt.expiration-ms` (con defaults seguros) |
| `LoginUseCase.java` | Implementación completa (estaba vacío) |

---

## Decisiones técnicas y justificaciones

### 1. JWT firmado con HS256 (JJWT 0.12.6)

```java
Jwts.builder()
    .subject(String.valueOf(user.getId()))   // userId como subject
    .claim("email", user.getEmail())
    .issuedAt(now)
    .expiration(new Date(now.getTime() + expirationMs))
    .signWith(key)                           // HS256 automático
    .compact();
```

**Justificación:** El subject del JWT es el `userId` (no el email), lo cual permite que el filtro extraiga el ID criptográficamente sin consultar la BD en cada request. Esto cumple con §12: *"El userId se extrae criptográficamente del token JWT en el SecurityContext"*.

### 2. BCrypt con cost factor 10

```java
private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
```

**Justificación:** Cost 10 es el estándar recomendado (≈100ms por hash). Cumple §12: *"Se hashean con BCryptPasswordEncoder (Cost 10) antes de tocar la BD"*.

### 3. Filtro JWT (`OncePerRequestFilter`)

```java
String header = request.getHeader("Authorization");
if (header != null && header.startsWith("Bearer ")) {
    String token = header.substring(7);
    Long userId = jwtTokenAdapter.extractUserId(token);
    // Poblar SecurityContext con userId como principal
}
```

**Justificación:** El filtro se ejecuta antes del `UsernamePasswordAuthenticationFilter` de Spring Security. Si el token es válido, el principal del SecurityContext es el `userId` (tipo `Long`). Los controllers pueden obtenerlo con `SecurityContextHolder.getContext().getAuthentication().getPrincipal()`.

### 4. Rutas públicas vs protegidas

```java
.requestMatchers("/api/v1/auth/**").permitAll()
.requestMatchers("/api/v1/quality/**").permitAll()
.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
.anyRequest().authenticated()
```

**Justificación:** Cumple §7: Auth y Quality son públicos, todo lo demás requiere Bearer token. CORS habilitado para `localhost:5173` (frontend Vite).

### 5. LoginUseCase — Mensaje de error genérico

```java
User user = userRepository.findByEmail(email)
    .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));
if (!passwordEncoder.matches(password, user.getPasswordHash())) {
    throw new IllegalArgumentException("Credenciales inválidas");
}
```

**Justificación de seguridad:** Se usa el mismo mensaje ("Credenciales inválidas") tanto si el email no existe como si la contraseña es incorrecta. Esto previene **user enumeration attacks** (OWASP A07:2021).

### 6. JWT secret desde variable de entorno

```properties
jwt.secret=${JWT_SECRET:super_secret_key_minimum_256_bits_for_hs256_algorithm_00}
```

**Justificación:** En producción se lee de la variable `JWT_SECRET`. El default solo aplica en desarrollo local. Cumple §12 y §14.

---

## Tests implementados (11 tests nuevos)

### LoginUseCaseTest (3 tests)

| Test | Escenario | Resultado esperado |
|------|-----------|--------------------|
| `shouldReturnTokenWhenCredentialsAreValid` | Email y password correctos | Retorna JWT + expiresIn |
| `shouldThrowWhenEmailNotFound` | Email no registrado | `IllegalArgumentException` |
| `shouldThrowWhenPasswordDoesNotMatch` | Password incorrecto | `IllegalArgumentException` |

### JwtTokenAdapterTest (4 tests)

| Test | Escenario | Resultado esperado |
|------|-----------|--------------------|
| `shouldGenerateTokenAndExtractUserId` | Token válido | Extrae userId=42 |
| `shouldReturnNullForInvalidToken` | Token basura | `null` |
| `shouldReturnNullForTamperedToken` | Token alterado | `null` |
| `shouldReturnNullForExpiredToken` | Token expirado (0ms TTL) | `null` |

### BCryptAdapterTest (4 tests)

| Test | Escenario | Resultado esperado |
|------|-----------|--------------------|
| `shouldEncodePassword` | Encode raw password | Hash ≠ raw, empieza con `$2` |
| `shouldMatchCorrectPassword` | Password correcto | `true` |
| `shouldNotMatchWrongPassword` | Password incorrecto | `false` |
| `shouldGenerateDifferentHashesForSamePassword` | Mismo input 2 veces | Hashes distintos (salt), ambos matchean |

---

## Resultados de ejecución

```
Tests run: 21, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Desglose:
- LoginUseCaseTest: 3/3 ✅
- JwtTokenAdapterTest: 4/4 ✅
- BCryptAdapterTest: 4/4 ✅
- ChangeTaskStatusUseCaseTest: 3/3 ✅ (no roto)
- TaskDomainTest: 4/4 ✅ (no roto)
- QualityScoringEngineTest: 3/3 ✅ (no roto)

---

## Estado del arranque del backend

Al intentar `./mvnw spring-boot:run`, el filtro JWT se registra correctamente:

```
Filter 'jwtAuthenticationFilter' configured for use
```

El backend falla al arrancar únicamente porque **TICKET-006 (JPA Adapters)** no existe aún — no hay beans que implementen `UserRepositoryPort` ni `TaskRepositoryPort`:

```
No qualifying bean of type 'com.empresa.tasks.application.port.out.UserRepositoryPort' available
```

**Esto es esperado.** Una vez que TICKET-006 proporcione los adapters de persistencia, el backend arrancará completo.

---

## Criterios de aceptación (TICKET-007)

| Criterio | Estado |
|----------|--------|
| Rutas públicas (`/api/v1/auth/**`, `/swagger-ui/**`) accesibles sin token | ✅ Configurado |
| Rutas privadas rechazan sin token → HTTP 401 | ✅ Configurado |
| BCrypt con cost 10 para hasheo | ✅ Implementado + testeado |
| JWT firmado con HS256, clave ≥256 bits desde env var | ✅ Implementado + testeado |
| SecurityContext poblado con userId del token | ✅ Implementado en filtro |
| LoginUseCase funcional | ✅ Implementado + 3 tests |

---

## Bloqueantes identificados

- **TICKET-006** (JPA Adapters): Debe implementar `UserRepositoryPort` y `TaskRepositoryPort` como `@Component` para que Spring pueda inyectarlos en los beans del `ApplicationConfig`.
- **TICKET-008** (Controllers REST): Debe crear `AuthController` y `TaskController` que usen los UseCases y extraigan el `userId` del `SecurityContext`.
