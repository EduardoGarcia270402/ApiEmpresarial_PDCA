# TICKET-002: Setup Docker Compose y Base de Datos Local

**Asignado:** DevOps & QA (completado por Backend Lead)  
**Tipo:** Infraestructura  
**Estado:** ✅ Completado  
**Tiempo estimado:** 2 horas  
**Dependencias:** TICKET-001

---

## ¿Qué se hizo?

Se configuró la infraestructura completa de contenedores Docker para levantar los 3 servicios del proyecto (PostgreSQL, Backend y Frontend) con un solo comando. Se resolvieron problemas de compatibilidad con Java 25, CORS, Flyway y proxy de Nginx.

---

## Archivos creados

### `docker-compose.yml` (raíz del proyecto)
Orquesta los 3 servicios con dependencias y healthchecks:

```
postgres  ← BD PostgreSQL 16
    ↑
backend   ← Spring Boot (espera que postgres esté healthy)
    ↑
frontend  ← React + Nginx (proxy hacia backend)
```

### `backend/Dockerfile`
Build multi-stage:
- **Stage 1 (builder):** `eclipse-temurin:25-jdk` — descarga dependencias Maven y compila el JAR
- **Stage 2 (runtime):** `eclipse-temurin:25-jre` — imagen liviana, solo corre el JAR

### `frontend/Dockerfile`
Build multi-stage:
- **Stage 1 (builder):** `node:22-alpine` — instala dependencias y ejecuta `npm run build`
- **Stage 2 (runtime):** `nginx:alpine` — sirve el build estático de Vite

---

## Configuración del `docker-compose.yml`

### Servicio PostgreSQL
```yaml
postgres:
  image: postgres:16-alpine
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U root -d tasksdb"]
    interval: 5s
    retries: 5
```
El healthcheck garantiza que el backend no arranque hasta que la BD esté lista.

### Servicio Backend
```yaml
backend:
  environment:
    SPRING_PROFILES_ACTIVE: prod
    DB_HOST: postgres
    DB_PORT: 5432
  depends_on:
    postgres:
      condition: service_healthy
```

### Servicio Frontend
```yaml
frontend:
  build:
    args:
      VITE_API_URL: /api/v1   ← proxy relativo via Nginx
  ports:
    - "80:80"
```

---

## Configuración Nginx (dentro del Dockerfile frontend)

```nginx
server {
    listen 80;
    root /usr/share/nginx/html;
    
    # SPA: todas las rutas van al index.html
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # Proxy de /api hacia el backend
    location /api {
        proxy_pass http://backend:8080;
    }
}
```

Esto resuelve dos cosas:
1. React Router funciona correctamente (rutas como `/register`, `/login` no dan 404)
2. Las llamadas a `/api/v1/...` del frontend son proxeadas al backend internamente

---

## Problemas encontrados y soluciones

### ❌ Error: `DockerFile` con D mayúscula
Docker es case-sensitive en Linux. El archivo debe llamarse exactamente `Dockerfile`.

**Solución:** Renombrar el archivo a `Dockerfile` (D mayúscula, resto minúscula).

### ❌ Error: `405 Not Allowed` al registrarse
El frontend llamaba a `:8080/api/v1/auth/register` saltándose Nginx.

**Solución:** Configurar `VITE_API_URL=/api/v1` como ARG en el Dockerfile del frontend para que Vite use rutas relativas en el build de producción.

### ❌ Error: `403 Forbidden` en `/api/v1/auth/register`
Spring Security bloqueaba la petición porque la tabla `users` no existía, lanzando una excepción que Spring Security interpretaba como error de autorización.

**Solución:** Crear las tablas manualmente y agregar configuración de Flyway en `application-prod.properties`.

### ❌ Flyway no ejecutaba migraciones en producción
Faltaban propiedades de Flyway en el perfil `prod`.

**Solución:** Agregar al `application-prod.properties`:
```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

### ❌ CORS bloqueaba peticiones desde `http://localhost`
`SecurityConfig.java` solo permitía `localhost:5173` y `localhost:3000`.

**Solución:** Agregar `http://localhost` y `http://localhost:80` a los orígenes permitidos.

---

## Cómo levantar el proyecto

### Primera vez (build completo)
```bash
docker compose up --build
```

### Arranque normal (imágenes ya construidas)
```bash
docker compose up
```

### Detener todo
```bash
docker compose down
```

### Reconstruir sin caché (si hay cambios en dependencias)
```bash
docker compose down
docker compose build --no-cache
docker compose up
```

---

## Verificación del sistema

Una vez levantado, verificar:

| Servicio | URL | Esperado |
|---------|-----|----------|
| Frontend | `http://localhost` | Pantalla de login |
| Backend API | `http://localhost:8080/swagger-ui.html` | Swagger UI |
| Quality Score | `http://localhost:8080/api/v1/quality/score` | JSON con score |
| PostgreSQL | `localhost:5432` | Accesible con credenciales del `.env` |

### Verificar tablas en BD
```bash
docker exec -it tasks_postgres psql -U root -d tasksdb -c "\dt"
```

Debe mostrar las tablas `users` y `tasks`.

---

## Variables de entorno requeridas

El sistema usa las variables del archivo `.env` en la raíz:

```env
DB_NAME=tasksdb
DB_USER=root
DB_PASSWORD=changeme
JWT_SECRET=super_secret_key_minimum_256_bits_for_hs256_algorithm_00
JWT_EXPIRATION_MS=7200000
```

> ⚠️ El archivo `.env` está en `.gitignore` y nunca debe subirse al repositorio.

---

## Factor McCall evaluado

**Portabilidad** — El sistema corre de forma idéntica en cualquier máquina con Docker instalado, independientemente del OS. **Interoperabilidad** — Los 3 servicios se comunican a través de una red interna Docker (`tasks_network`), con el frontend proxy-eando correctamente al backend.