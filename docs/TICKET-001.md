# TICKET-001: Setup del Repositorio y Arquitectura Limpia

**Asignado:** Backend Lead - Eduardo Garcia 
**Tipo:** DevOps / Backend  
**Estado:** ✅ Completado  
**Tiempo estimado:** 2 horas  
**Dependencias:** Ninguna

---

## ¿Qué se hizo?

Se inicializó el proyecto base de Spring Boot 4.0 con Java 25 y se estableció la estructura de carpetas siguiendo los principios de **Clean Architecture**. Este ticket es la base sobre la que se construye todo el sistema.

### Proyecto generado con:
- **Spring Initializr** (start.spring.io)
- Spring Boot 4.0.6
- Java 25
- Maven como gestor de dependencias

### Dependencias configuradas en `pom.xml`:

| Dependencia | Propósito |
|-------------|-----------|
| `spring-boot-starter-web` | API REST |
| `spring-boot-starter-data-jpa` | Persistencia con JPA |
| `spring-boot-starter-validation` | Validación de DTOs |
| `postgresql` | Driver de base de datos |
| `lombok` | Reducción de código boilerplate |
| `spring-boot-starter-test` | JUnit 5 + Mockito |

---

## Estructura Clean Architecture creada

```
backend/src/main/java/com/empresa/tasks/
├── domain/                  ← Capa 1: Entidades y reglas de negocio puras
│   ├── model/
│   └── exception/
├── application/             ← Capa 2: Casos de uso y puertos
│   ├── usecase/
│   └── port/
│       └── out/
├── infrastructure/          ← Capa 3: Adaptadores JPA, JWT, Security
│   ├── persistence/
│   └── security/
├── interfaces/              ← Capa 4: Controllers REST y DTOs
│   ├── rest/
│   ├── dto/
│   └── exception/
└── sqa/                     ← Motor de calidad McCall (aislado)
    └── engine/
```

> La regla principal: **las capas internas no importan las externas**. `domain/` no tiene ningún import de Spring o Jakarta.

---

## Archivos clave creados

- `backend/pom.xml` — Configuración Maven con todas las dependencias
- `.env.example` — Plantilla de variables de entorno (sin valores reales)
- `.gitignore` — Excluye `target/`, `.env`, `.idea/`
- `backend/src/main/java/com/empresa/tasks/TasksApplication.java` — Entry point

---

## Cómo se verificó

```bash
cd backend
.\mvnw clean install -DskipTests
```

**Resultado:** `BUILD SUCCESS` — 5 carpetas principales compiladas correctamente.

---

## Factor McCall evaluado

**Mantenibilidad y Flexibilidad** — La arquitectura limpia garantiza cero dependencias cíclicas entre capas y facilita el cambio de tecnologías sin afectar la lógica de negocio.