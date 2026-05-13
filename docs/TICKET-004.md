# TICKET-004: Modelo de Dominio y Excepciones

**Asignado:** Backend Lead  - Eduardo Garcia
**Tipo:** Backend  
**Estado:** ✅ Completado  
**Tiempo estimado:** 2.5 horas  
**Dependencias:** TICKET-001

---

## ¿Qué se hizo?

Se implementó el núcleo del sistema en la capa `domain/` — la más interna de Clean Architecture. Esta capa contiene las entidades del negocio y sus reglas, completamente aislada de frameworks externos.

**Regla estricta cumplida:** Cero imports de `org.springframework` o `jakarta.persistence` en toda la carpeta `domain/`.

---

## Archivos creados

### `domain/model/TaskStatus.java`
Enum con los 4 estados posibles de una tarea:

```java
public enum TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
```

### `domain/model/User.java`
POJO puro que representa un usuario del sistema. Campos: `id`, `email`, `passwordHash`, `name`.

### `domain/model/Task.java`
POJO con las **reglas de negocio** embebidas como métodos:

| Método | Comportamiento |
|--------|---------------|
| `markAsCompleted()` | Cambia estado a `COMPLETED`. Lanza excepción si ya está `CANCELLED` o `COMPLETED` |
| `cancel()` | Cambia estado a `CANCELLED`. Lanza excepción si ya está `COMPLETED` o `CANCELLED` |

### `domain/exception/InvalidStatusTransitionException.java`
Excepción de negocio que se lanza cuando se intenta una transición de estado inválida.

```java
// Ejemplo de uso interno en Task:
throw new InvalidStatusTransitionException("CANCELLED", "COMPLETED");
// Mensaje: "No se puede cambiar el estado de CANCELLED a COMPLETED"
```

---

## Diagrama de transición de estados

```
              ┌─────────────┐
    inicio───►│   PENDING   │
              └──────┬──────┘
                     │
              ┌──────▼──────┐
              │ IN_PROGRESS │
              └──────┬──────┘
                     │
         ┌───────────┼───────────┐
         │                       │
  ┌──────▼──────┐         ┌──────▼──────┐
  │  COMPLETED  │         │  CANCELLED  │
  └─────────────┘         └─────────────┘
  
  ❌ COMPLETED → CANCELLED  (lanza excepción)
  ❌ CANCELLED → COMPLETED  (lanza excepción)
```

---

## Tests unitarios creados

**Archivo:** `src/test/java/com/empresa/tasks/domain/TaskDomainTest.java`

| Test | Qué valida |
|------|-----------|
| `deberiaCompletarUnaTareaPendiente` | Estado cambia a COMPLETED y se registra `completedAt` |
| `deberiaCancelarUnaTareaPendiente` | Estado cambia a CANCELLED correctamente |
| `deberiaLanzarExcepcionAlCompletarTareaCancelada` | `markAsCompleted()` en tarea CANCELLED lanza excepción |
| `deberiaLanzarExcepcionAlCancelarTareaCompletada` | `cancel()` en tarea COMPLETED lanza excepción |

## Cómo se verificó

```bash
.\mvnw test -Dtest=TaskDomainTest
```

**Resultado:** `Tests run: 4, Failures: 0, Errors: 0, Skipped: 0` ✅

---

## Factor McCall evaluado

**Correctness (Corrección)** — Las reglas de negocio están encapsuladas en el dominio y son verificables de forma aislada, sin necesidad de levantar Spring ni conectarse a una base de datos.