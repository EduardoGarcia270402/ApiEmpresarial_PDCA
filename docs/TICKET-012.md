# TICKET-012: Coleccion Postman y Smoke Test Backend

**Asignado:** DevOps & QA  
**Tipo:** Testing Manual  
**Estado:** Completado  
**Tiempo estimado:** 2 horas  
**Dependencias:** TICKET-008

---

## Que se hizo

Se preparo una coleccion de Postman para validar el flujo principal del backend con un solo clic desde el Collection Runner.

El smoke test ejecuta la secuencia completa:

```text
Registro -> Login -> Crear Tarea -> Completar Tarea -> Borrar Tarea
```

La evidencia del Runner se guardo en:

```text
docs/postman/API_GESTION_TAREAS.postman_test_run.json
```

---

## Variables de entorno usadas en Postman

| Variable | Valor de ejemplo | Uso |
|----------|------------------|-----|
| `base_url` | `http://localhost:8080/api/v1` | URL base del backend |
| `test_email` | `smoke010@test.com` | Correo del usuario de prueba |
| `test_password` | `Password123` | Password del usuario de prueba |
| `jwt_token` | Se llena automaticamente | Token JWT obtenido en Login |
| `task_id` | Se llena automaticamente | ID de la tarea creada |

`jwt_token` y `task_id` se dejan vacios antes de correr la coleccion, porque Postman los completa durante el Runner.

---

## Peticiones configuradas

| Orden | Peticion | Metodo | Endpoint | Resultado esperado |
|-------|----------|--------|----------|--------------------|
| 1 | RegistrarCorreo | POST | `{{base_url}}/auth/register` | `201 Created` |
| 2 | Login | POST | `{{base_url}}/auth/login` | `200 OK` |
| 3 | CrearTarea | POST | `{{base_url}}/tasks` | `201 Created` |
| 4 | ActualizarTarea | PATCH | `{{base_url}}/tasks/{{task_id}}/status` | `200 OK` |
| 5 | EliminarTarea | DELETE | `{{base_url}}/tasks/{{task_id}}` | `204 No Content` |

La peticion `ObtenerTareas` queda como consulta auxiliar, pero no es obligatoria para el criterio principal del ticket.

---

## Script de Login

En la pestana **Scripts** de la peticion `Login`, se agrego un test que valida la respuesta y guarda el token JWT:

```javascript
const response = pm.response.json();

pm.test("Login exitoso", function () {
    pm.response.to.have.status(200);
});

pm.test("Token recibido", function () {
    pm.expect(response.token).to.exist;
});

pm.environment.set("jwt_token", response.token);
```

Las peticiones protegidas usan el token con el header:

```text
Authorization: Bearer {{jwt_token}}
```

---

## Script de CrearTarea

La peticion `CrearTarea` guarda el ID de la tarea para que las siguientes peticiones puedan completarla y eliminarla:

```javascript
const response = pm.response.json();

pm.test("Tarea creada", function () {
    pm.response.to.have.status(201);
});

pm.test("ID de tarea recibido", function () {
    pm.expect(response.id).to.exist;
});

pm.environment.set("task_id", response.id);
```

---

## Scripts de cierre

### ActualizarTarea

```javascript
const response = pm.response.json();

pm.test("Tarea completada", function () {
    pm.response.to.have.status(200);
});

pm.test("Estado COMPLETED", function () {
    pm.expect(response.status).to.eql("COMPLETED");
});
```

### EliminarTarea

```javascript
pm.test("Tarea eliminada", function () {
    pm.response.to.have.status(204);
});
```

---

## Como se prueba

1. Levantar el backend y la base de datos con Docker Compose.
2. Abrir Postman.
3. Seleccionar el environment `New Environment`.
4. Verificar que existan las variables `base_url`, `test_email`, `test_password`, `jwt_token` y `task_id`.
5. Abrir el Runner de la coleccion `API_GESTION_TAREAS`.
6. Ejecutar en este orden:

```text
RegistrarCorreo
Login
CrearTarea
ActualizarTarea
EliminarTarea
```

7. Confirmar que no existan tests fallidos.

---

## Resultado obtenido

La ejecucion exportada en `docs/postman/API_GESTION_TAREAS.postman_test_run.json` termino correctamente:

| Metrica | Resultado |
|---------|-----------|
| Estado del Runner | `finished` |
| Tests exitosos | `8` |
| Tests fallidos | `0` |
| Tiempo total | `1164 ms` |

Resultados por peticion:

| Peticion | Codigo |
|----------|--------|
| RegistrarCorreo | `201 Created` |
| Login | `200 OK` |
| CrearTarea | `201 Created` |
| ActualizarTarea | `200 OK` |
| EliminarTarea | `204 No Content` |

---

## Criterios de aceptacion

| Criterio | Estado |
|----------|--------|
| Coleccion Postman completa | Completado |
| Login captura el token JWT | Completado |
| Token se guarda en `{{jwt_token}}` | Completado |
| Peticiones posteriores usan el token automaticamente | Completado |
| Runner ejecuta Registro -> Login -> Crear -> Completar -> Borrar | Completado |
| Flujo termina sin fallos | Completado |

Con este smoke test exitoso, el backend queda validado para iniciar la integracion con frontend.
