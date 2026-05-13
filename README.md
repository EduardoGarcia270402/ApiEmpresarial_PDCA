# 📋 Task Management System — SQA Evaluation

![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-blue)
![Quality Gate](https://img.shields.io/badge/Quality%20Gate-Passed-brightgreen)
![Coverage](https://img.shields.io/badge/Coverage-%3E85%25-brightgreen)
![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.x-green)
![React](https://img.shields.io/badge/React-18-blue)

Sistema full-stack de gestión de tareas con enfoque en **calidad de software**. Implementa el **Modelo de Calidad de McCall** y el **Ciclo PDCA**, incluyendo un motor propio de evaluación SQA que analiza métricas reales de cobertura y bugs.

---

## 📑 Tabla de Contenidos

- [Descripción](#-descripción)
- [Stack Tecnológico](#-stack-tecnológico)
- [Arquitectura](#-arquitectura)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación y Ejecución Local](#-instalación-y-ejecución-local)
- [Variables de Entorno](#-variables-de-entorno)
- [Endpoints de la API](#-endpoints-de-la-api)
- [Motor de Calidad SQA](#-motor-de-calidad-sqa)
- [Pruebas](#-pruebas)
- [Pipeline CI/CD](#-pipeline-cicd)
- [Despliegue en Producción](#-despliegue-en-producción)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Equipo](#-equipo)

---

## 📌 Descripción

Este proyecto es un **Task Manager** donde cada usuario puede registrarse, iniciar sesión y gestionar sus propias tareas (crear, listar, cambiar estado y eliminar). Lo que lo diferencia es su capa de **evaluación de calidad automatizada**:

- Analiza reportes XML generados por **JaCoCo** (cobertura) y **SpotBugs** (bugs estáticos).
- Calcula un score técnico basado en la fórmula de **McCall**.
- Expone ese score como un endpoint REST para que el evaluador lo consulte en tiempo real.

### Flujo principal del usuario

```
Registro → Login (recibe JWT) → Crear Tareas → Cambiar Estado → Eliminar
```

### Estados de una tarea

```
PENDING → IN_PROGRESS → COMPLETED
                      ↘ CANCELLED
```

---

## 🛠 Stack Tecnológico

| Capa | Tecnología |
|------|-----------|
| Backend | Java 25 + Spring Boot 4.0.x |
| Persistencia | PostgreSQL 16 + Spring Data JPA + Flyway |
| Frontend | React 18 + TypeScript + Vite + TailwindCSS |
| Seguridad | Spring Security + JJWT 0.12.x (HS256) |
| Testing | JUnit 5 + Mockito + JaCoCo |
| Calidad | SonarCloud + SpotBugs + OWASP Dependency Check |
| Contenedores | Docker + Docker Compose |
| CI/CD | GitHub Actions |
| Despliegue | Render.com |

---

## 🏗 Arquitectura

El backend implementa **Clean Architecture** con separación estricta de capas:

```
┌─────────────────────────────────────────┐
│           interfaces/  (REST)           │  ← Controllers, DTOs
├─────────────────────────────────────────┤
│         infrastructure/                 │  ← JPA, Security, JWT
├─────────────────────────────────────────┤
│           application/                  │  ← Use Cases, Ports
├─────────────────────────────────────────┤
│              domain/                    │  ← Entities, Business Rules
└─────────────────────────────────────────┘
         sqa/  (Motor McCall aislado)
```

> La capa `domain/` es Java puro — **cero dependencias de Spring o Jakarta**.

---

## ✅ Requisitos Previos

Antes de correr el proyecto necesitas tener instalado:

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (incluye Docker Compose)
- [Java 25 JDK](https://openjdk.org/)
- [Node.js 20+](https://nodejs.org/) (para el frontend)
- Git

---

## 🚀 Instalación y Ejecución Local

### 1. Clonar el repositorio

```bash
git clone https://github.com/TU_USUARIO/ApiEmpresarial_PDCA.git
cd ApiEmpresarial_PDCA
```

### 2. Configurar variables de entorno

```bash
# Copiar la plantilla
cp .env.example .env

# Editar .env con tus valores locales
```

### 3. Levantar PostgreSQL con Docker Compose

```bash
docker compose up -d postgres
```

Esto levanta PostgreSQL en `localhost:5432`.

### 4. Ejecutar el backend con perfil de desarrollo

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

> **Perfiles disponibles:**
> - `dev` — SQL verbose, logs DEBUG, Flyway con `clean-disabled=false` (para desarrollo)
> - `prod` — SQL oculto, logs mínimos, Flyway con `clean-disabled=true` (para producción)

### 5. Verificar que todo funciona

```bash
# Health check del backend
curl http://localhost:8080/api/v1/quality/score

# Abrir el frontend (si está configurado)
open http://localhost:5173
```

### Ejecución manual completa (sin Docker)

```bash
# Levantar PostgreSQL manualmente
docker run -d -p 5432:5432 -e POSTGRES_DB=tasksdb -e POSTGRES_USER=root -e POSTGRES_PASSWORD=changeme postgres:16-alpine

# Backend
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Frontend (en otra terminal)
cd frontend
npm install
npm run dev
```

---

## 🔐 Variables de Entorno

Copia `.env.example` como `.env` y completa los valores:

```env
DB_HOST=postgres
DB_PORT=5432
DB_NAME=tasksdb
DB_USER=root
DB_PASSWORD=tu_password_seguro

# Mínimo 256 bits para HS256
JWT_SECRET=tu_clave_secreta_de_al_menos_256_bits
JWT_EXPIRATION_MS=7200000        # 2 horas

# Frontend
VITE_API_URL=http://localhost:8080/api/v1
```

> ⚠️ **Nunca subas el archivo `.env` al repositorio.** Está en `.gitignore`.

---

## 📡 Endpoints de la API

La documentación interactiva completa está en **Swagger UI**:
```
http://localhost:8080/swagger-ui.html
```

### Autenticación

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/auth/register` | Registrar nuevo usuario | ❌ |
| POST | `/api/v1/auth/login` | Login — retorna JWT | ❌ |

**Registro:**
```json
POST /api/v1/auth/register
{
  "email": "usuario@example.com",
  "password": "miPassword123",
  "name": "Juan Pérez"
}
```

**Login:**
```json
POST /api/v1/auth/login
{
  "email": "usuario@example.com",
  "password": "miPassword123"
}

// Respuesta:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 3600
}
```

### Tareas (requieren JWT)

Incluye el header en todas las peticiones:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/tasks` | Listar mis tareas |
| GET | `/api/v1/tasks?status=PENDING` | Filtrar por estado |
| POST | `/api/v1/tasks` | Crear tarea |
| PATCH | `/api/v1/tasks/{id}/status` | Cambiar estado |
| DELETE | `/api/v1/tasks/{id}` | Eliminar tarea |

**Crear tarea:**
```json
POST /api/v1/tasks
{
  "title": "Estudiar Clean Architecture",
  "description": "Capítulos 1 al 3"
}
```

**Cambiar estado:**
```json
PATCH /api/v1/tasks/1/status
{
  "status": "COMPLETED"
}
```

### Motor SQA

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/quality/score` | Score McCall en tiempo real | ❌ |

```json
// Respuesta ejemplo:
{
  "correctness": 95.0,
  "maintainability": 100.0,
  "totalScore": 97.5
}
```

---

## 🔬 Motor de Calidad SQA

El **Quality Scoring Engine** implementa el Modelo de McCall leyendo artefactos reales del build:

| Fuente | Métrica | Factor McCall |
|--------|---------|---------------|
| `target/site/jacoco/jacoco.xml` | % Cobertura de código | Testability |
| `target/spotbugsXml.xml` | Número de bugs detectados | Correctness |
| SonarCloud | Vulnerabilidades | Integrity |

**Fórmula:**
```
Score = (Coverage × W_TEST) + (Bugs × W_CORR) + (Vulns × W_INT)
```

Los pesos son configurables en `application.yml`.

---

## 🧪 Pruebas

```bash
cd backend

# Ejecutar todos los tests
./mvnw test

# Solo tests unitarios de dominio
./mvnw test -Dtest=TaskDomainTest

# Solo tests de casos de uso (con Mockito)
./mvnw test -Dtest=ChangeTaskStatusUseCaseTest

# Generar reporte de cobertura JaCoCo
./mvnw clean verify
# Reporte disponible en: target/site/jacoco/index.html
```

### Umbrales requeridos

| Tipo | Herramienta | Umbral |
|------|-------------|--------|
| Cobertura | JaCoCo | > 85% |
| Bugs estáticos | SpotBugs | 0 críticos |
| Vulnerabilidades | OWASP | 0 altas |
| Quality Gate | SonarCloud | Passed ✅ |

---

## ⚙️ Pipeline CI/CD

El pipeline se dispara automáticamente en cada **PR hacia `develop` o `main`**:

```
Push / PR
    │
    ▼
┌─────────────┐    ┌──────────────┐    ┌─────────────┐    ┌──────────┐
│  Build &    │ → │   SonarCloud  │ → │    OWASP    │ → │  Docker  │
│  JUnit Test │    │  Quality Gate │    │ Dep. Check  │    │  Build   │
└─────────────┘    └──────────────┘    └─────────────┘    └──────────┘
                                                                │
                                                                ▼
                                                         ┌──────────┐
                                                         │  Deploy  │
                                                         │  Render  │
                                                         └──────────┘
```

Si cualquier etapa falla, el pipeline se pone en ❌ y el deploy no ocurre.

---

## 🌐 Despliegue en Producción

El sistema está desplegado en **Render.com**:

| Servicio | URL |
|---------|-----|
| Frontend | `https://tasks-frontend.onrender.com` |
| Backend API | `https://tasks-api.onrender.com` |
| Swagger UI | `https://tasks-api.onrender.com/swagger-ui.html` |

---

## �️ Migraciones de Base de Datos (Flyway)

El proyecto utiliza **Flyway** para gestión de migraciones SQL. Las migraciones se encuentran en:

```
backend/src/main/resources/db/migration/
├── V1__create_users.sql
└── V2__create_tasks.sql
```

Las migraciones se ejecutan automáticamente al iniciar la aplicación (Spring Boot auto-detecta Flyway).

---

##  Estructura del Proyecto

```
ApiEmpresarial_PDCA/
├── backend/
│   ├── src/main/java/com/empresa/tasks/
│   │   ├── domain/              # Entidades y reglas de negocio puras
│   │   │   ├── model/           # Task, User, TaskStatus
│   │   │   └── exception/       # InvalidStatusTransitionException
│   │   ├── application/         # Casos de uso y puertos
│   │   │   ├── usecase/         # CreateTask, Login, Register...
│   │   │   └── port/out/        # Interfaces de repositorio y servicios
│   │   ├── infrastructure/      # Adaptadores JPA, JWT, Security
│   │   │   ├── persistence/     # Entidades JPA y repositorios Spring
│   │   │   └── security/        # JwtFilter, BCrypt, SecurityConfig
│   │   ├── interfaces/          # Controllers REST y DTOs
│   │   │   ├── rest/            # AuthController, TaskController
│   │   │   ├── dto/             # Request/Response DTOs
│   │   │   └── exception/       # GlobalExceptionHandler
│   │   └── sqa/                 # Motor de calidad McCall
│   │       └── engine/          # QualityScoringEngine, ReportParser
│   ├── src/main/resources/
│   │   ├── application.properties          # Configuración base
│   │   ├── application-dev.properties       # Perfil desarrollo
│   │   ├── application-prod.properties      # Perfil producción
│   │   └── db/migration/                   # Migraciones Flyway SQL
│   │       ├── V1__create_users.sql
│   │       └── V2__create_tasks.sql
│   ├── src/test/                # Tests unitarios y de integración
│   ├── pom.xml
│   └── Dockerfile
├── frontend/
│   ├── src/
│   │   ├── pages/               # Login, Register, Dashboard, TaskDetail
│   │   ├── components/          # Componentes reutilizables
│   │   ├── services/            # Llamadas Axios a la API
│   │   └── types/               # Interfaces TypeScript
│   └── package.json
├── .env.example                 # Plantilla de variables de entorno
├── docker-compose.yml           # Docker Compose para desarrollo local
└── README.md
```

---

## 👥 Equipo

| Rol | Responsabilidad |
|-----|----------------|
| Backend Lead | Domain, Application, Motor SQA, Tests unitarios |
| Backend 2 | Flyway, JPA, Spring Security, JWT, Controllers REST |
| Frontend | React UI, Axios, Tailwind, Consumo de APIs |
| DevOps & QA | Docker, GitHub Actions, Postman, Despliegue, Informe PDF |

---

## 📄 Licencia

Proyecto académico — Universidad. Todos los derechos reservados.