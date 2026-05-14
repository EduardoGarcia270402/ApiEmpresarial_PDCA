# TICKET-015: TaskDetailPage — Vista de detalle y navegación /task/:id

**Asignado:** Frontend  
**Tipo:** Feature (Frontend + Bug-fix Backend)  
**Estado:** ✅ Completado  
**Tiempo estimado:** 2 horas  
**Dependencias:** TICKET-004 (CRUD tareas), TICKET-005 (Autenticación JWT)

---

## ¿Qué se hizo?

Se implementó la página de detalle de tarea (`/task/:id`) con navegación desde el Dashboard, acciones de cambio de estado (Iniciar, Completar, Cancelar) y eliminación con confirmación. También se corrigieron dos bugs en el backend: la configuración CORS que bloqueaba el frontend y la transición de estado `PENDING → IN_PROGRESS` que no existía.

---

## Archivos creados

### `frontend/src/pages/TaskDetailPage.tsx`

Componente principal de la vista de detalle. Incluye:

| Sección | Descripción |
|---------|-------------|
| Encabezado | Título, badge de estado con color e ícono, ID de tarea |
| Descripción | Cuerpo de la tarea con soporte multi-línea |
| Línea de tiempo | Fecha de creación y fecha de completado (si aplica) |
| Acciones | Botones de transición de estado según el estado actual |
| Eliminar | Botón con diálogo de confirmación inline |
| Skeleton | Estado de carga animado mientras se obtiene la tarea |

**Navegación desde Dashboard (zero-request):** Cuando el usuario hace clic en el título de una tarea desde el Dashboard, la tarea se pasa via `router state`, evitando una petición adicional al backend.

**Fallback por URL directa:** Si se accede directamente a `/task/3`, se obtiene la tarea desde la API.

---

## Archivos modificados

### `frontend/src/pages/DashboardPage.tsx`

Se hizo el título de cada tarea clickeable:

```tsx
<h3
  className="text-indigo-600 cursor-pointer hover:underline font-semibold"
  onClick={() => navigate(`/task/${task.id}`, { state: { task } })}
>
  {task.title}
</h3>
```

### `frontend/src/router/index.tsx`

Se agregó la ruta protegida:

```tsx
{ path: '/task/:id', element: <TaskDetailPage /> }
```

### `frontend/src/pages/index.ts`

Se agregó el export:

```ts
export { default as TaskDetailPage } from './TaskDetailPage';
```

### `frontend/src/api/tasks.ts`

Se agregó el método `getById` como fallback para acceso directo por URL:

```ts
async getById(id: number): Promise<Task | undefined> {
  const tasks = await this.getAll();
  return tasks.find((t) => t.id === id);
}
```

---

## Bug-fix Backend #1 — Transición `IN_PROGRESS` no implementada

### Problema

Al dar clic en "Iniciar tarea", el backend respondía `200 OK` pero devolvía la tarea con `status: "PENDING"` sin cambios. El `ChangeTaskStatusUseCase` solo manejaba `COMPLETED` y `CANCELLED`; el caso `IN_PROGRESS` era ignorado silenciosamente.

### Solución

**`backend/.../domain/model/Task.java`** — Se agregó el método de dominio:

```java
public void startProgress() {
    if (this.status != TaskStatus.PENDING) {
        throw new InvalidStatusTransitionException(
            this.status.name(), TaskStatus.IN_PROGRESS.name()
        );
    }
    this.status = TaskStatus.IN_PROGRESS;
}
```

**`backend/.../application/usecase/ChangeTaskStatusUseCase.java`** — Se agregó el caso:

```java
if (newStatus == TaskStatus.IN_PROGRESS) {
    task.startProgress();
} else if (newStatus == TaskStatus.COMPLETED) {
    task.markAsCompleted();
} else if (newStatus == TaskStatus.CANCELLED) {
    task.cancel();
}
```

### Máquina de estados resultante

```
PENDING ──────► IN_PROGRESS ──────► COMPLETED
   │                │
   │                ▼
   └──────────► CANCELLED
```

| Transición | Método | Válida desde |
|-----------|--------|-------------|
| `→ IN_PROGRESS` | `startProgress()` | `PENDING` |
| `→ COMPLETED` | `markAsCompleted()` | `PENDING`, `IN_PROGRESS` |
| `→ CANCELLED` | `cancel()` | `PENDING`, `IN_PROGRESS` |

---

## Bug-fix Backend #2 — CORS bloqueaba el frontend

### Problema

El frontend corría en `localhost:5174` pero `SecurityConfig.java` solo permitía orígenes `5173`, `3000`, `80`. Las peticiones OPTIONS preflight retornaban `403 Forbidden`.

### Solución

**`backend/.../infrastructure/security/SecurityConfig.java`** — Se agregó el origen:

```java
config.setAllowedOrigins(List.of(
    "http://localhost:5173",
    "http://localhost:5174",  // ← agregado
    "http://localhost:3000",
    "http://localhost:80",
    "http://localhost"
));
```

---

## Cómo ejecutar

### 1. Levantar la base de datos

```bash
cd ApiEmpresarial_PDCA
docker compose up -d postgres
```

### 2. Levantar el backend

```bash
cd backend
./mvnw spring-boot:run
```

El backend arranca en `http://localhost:8080`.

### 3. Levantar el frontend

```bash
cd frontend
npm install
npm run dev
```

El frontend arranca en `http://localhost:5173` (o `5174` si el puerto está ocupado).

### 4. Probar la funcionalidad

1. Registrarse o iniciar sesión en `/login`
2. Crear una tarea desde el Dashboard
3. Hacer clic en el título de la tarea → se abre `/task/:id`
4. Probar los botones:
   - **Iniciar tarea** → cambia a `IN_PROGRESS`
   - **Marcar como completada** → cambia a `COMPLETED`
   - **Cancelar tarea** → cambia a `CANCELLED`
   - **Eliminar** → pide confirmación y borra la tarea
5. Verificar que al volver al Dashboard el estado se refleja

### 5. Verificar por API (curl)

```bash
# Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"123456"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# Crear tarea
curl -s -X POST http://localhost:8080/api/v1/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test","description":"Prueba"}'

# Iniciar tarea (PENDING → IN_PROGRESS)
curl -s -X PATCH http://localhost:8080/api/v1/tasks/1/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"IN_PROGRESS"}'

# Completar tarea (IN_PROGRESS → COMPLETED)
curl -s -X PATCH http://localhost:8080/api/v1/tasks/1/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"COMPLETED"}'
```

---

## Criterios de aceptación

| # | Criterio | Estado |
|---|----------|--------|
| 1 | Existe la ruta `/task/:id` protegida por autenticación | ✅ |
| 2 | Se muestra título, descripción, estado y fechas de la tarea | ✅ |
| 3 | Los botones de cambio de estado funcionan (Iniciar, Completar, Cancelar) | ✅ |
| 4 | La eliminación requiere confirmación antes de ejecutarse | ✅ |
| 5 | La navegación desde el Dashboard pasa la tarea sin request adicional | ✅ |
| 6 | El acceso directo por URL carga la tarea desde la API | ✅ |
| 7 | La transición `PENDING → IN_PROGRESS` funciona en el backend | ✅ |
| 8 | El frontend se comunica correctamente con el backend (CORS) | ✅ |
