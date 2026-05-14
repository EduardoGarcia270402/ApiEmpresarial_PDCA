# TICKET-016: Despliegue Produccion en Render

## Objetivo

Desplegar PostgreSQL, backend Spring Boot y frontend React/Vite en Render para que la aplicacion quede disponible publicamente.

## 1. Crear PostgreSQL

En Render:

1. New > Postgres.
2. Nombre sugerido: `tasks-postgres`.
3. Database: `tasksdb`, si Render permite definirlo.
4. User: `tasks_user`, si Render permite definirlo.
5. Region: la misma que usara el backend.
6. Plan: Free si esta disponible.
7. Crear la base y esperar estado `Available`.

Guardar los datos internos:

```env
DB_HOST=<internal-host>
DB_PORT=5432
DB_NAME=<database>
DB_USER=<user>
DB_PASSWORD=<password>
```

Usar el host interno para conectar desde el backend desplegado en Render.

## 2. Crear Backend

En Render:

1. New > Web Service.
2. Conectar el repositorio.
3. Branch: `main` o la rama configurada para produccion.
4. Root Directory: `backend`.
5. Runtime: Docker.
6. Dockerfile Path: `Dockerfile`.
7. Plan: Free si esta disponible.

Variables de entorno:

```env
SPRING_PROFILES_ACTIVE=prod
DB_HOST=<internal-host>
DB_PORT=5432
DB_NAME=<database>
DB_USER=<user>
DB_PASSWORD=<password>
JWT_SECRET=<clave-larga-minimo-256-bits>
JWT_EXPIRATION_MS=7200000
FRONTEND_URL=https://<frontend>.onrender.com
```

Si el backend se crea antes del frontend, dejar `FRONTEND_URL` vacia temporalmente y actualizarla despues.

URLs esperadas:

- API: `https://<backend>.onrender.com`
- Swagger: `https://<backend>.onrender.com/swagger-ui.html`

## 3. Crear Frontend

En Render:

1. New > Static Site.
2. Conectar el mismo repositorio.
3. Branch: `main` o la rama configurada para produccion.
4. Root Directory: `frontend`.
5. Build Command: `npm ci && npm run build`.
6. Publish Directory: `dist`.

Variable de entorno:

```env
VITE_API_URL=https://<backend>.onrender.com/api/v1
```

## 4. Actualizar CORS

Cuando Render entregue la URL publica del frontend:

1. Entrar al servicio backend.
2. Environment.
3. Configurar:

```env
FRONTEND_URL=https://<frontend>.onrender.com
```

4. Ejecutar `Manual Deploy > Redeploy latest commit`.

## 5. Prueba de aceptacion

Desde navegador o celular:

1. Abrir `https://<frontend>.onrender.com`.
2. Registrar usuario.
3. Iniciar sesion.
4. Crear una tarea.
5. Confirmar que aparece en el dashboard.

Si Swagger funciona pero el frontend no puede iniciar sesion, revisar que `FRONTEND_URL` coincida exactamente con la URL publica del frontend.

