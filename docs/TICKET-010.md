# TICKET-010: Pipeline DevSecOps CI/CD

**Asignado:** DevOps & QA  
**Tipo:** DevSecOps  
**Estado:** Completado  
**Tiempo estimado:** 3 horas  
**Dependencias:** TICKET-008

---

## Que se hizo

Se implemento el pipeline DevSecOps del backend usando GitHub Actions. El workflow ejecuta compilacion, pruebas, cobertura, analisis estatico, escaneo de dependencias y Quality Gate de SonarCloud.

El archivo principal creado es:

```text
.github/workflows/main.yml
```

---

## Flujo del pipeline

```text
Pull Request a develop / Push a develop o main
    |
    v
Checkout
    |
    v
Setup JDK 25
    |
    v
Maven clean verify
    |
    +--> Tests JUnit
    +--> JaCoCo Coverage > 85%
    +--> SpotBugs High bugs check
    |
    v
OWASP Dependency Check
    |
    v
SonarCloud Scan + Quality Gate
```

Si cualquiera de estas etapas falla, GitHub Actions marca el pipeline en rojo.

---

## Triggers configurados

```yaml
on:
  pull_request:
    branches:
      - develop
  push:
    branches:
      - develop
      - main
```

Esto cumple el criterio principal del ticket: la accion se dispara al abrir o actualizar un PR hacia `develop`.

---

## Cambios en Maven

Se agregaron propiedades de version al `pom.xml`:

```xml
<spotbugs.maven.plugin.version>4.9.8.3</spotbugs.maven.plugin.version>
<dependency.check.maven.version>12.2.2</dependency.check.maven.version>
```

### JaCoCo

Ya estaba configurado y se mantiene como parte de `mvn clean verify`:

- Genera `target/site/jacoco/jacoco.xml`.
- Verifica cobertura minima de `0.85`.
- Si la cobertura baja del 85%, el build falla.

### SpotBugs

Se agrego `spotbugs-maven-plugin`:

- Corre en fase `verify`.
- Genera `target/spotbugsXml.xml`.
- Falla el build con bugs de severidad alta.
- Alimenta el motor SQA McCall, que lee ese XML para calcular Correctness.

### OWASP Dependency Check

Se agrego `dependency-check-maven`:

- Se ejecuta como etapa separada del workflow.
- Genera reportes HTML, XML y SARIF.
- Falla el pipeline si detecta vulnerabilidades con CVSS >= 7.

---

## Configuracion requerida en GitHub

Para que SonarCloud funcione, configurar en el repositorio:

### Secrets

| Nombre | Uso |
|--------|-----|
| `SONAR_TOKEN` | Token generado desde SonarCloud |
| `NVD_API_KEY` | Opcional, mejora la estabilidad del escaneo OWASP |

### Variables

| Nombre | Uso |
|--------|-----|
| `SONAR_PROJECT_KEY` | Project key del proyecto en SonarCloud |
| `SONAR_ORGANIZATION` | Organization key de SonarCloud |

Si falta cualquiera de los datos obligatorios de SonarCloud, el pipeline falla con un mensaje explicito para evitar falsos verdes.

---

## Artefactos generados por el workflow

| Artefacto | Ruta |
|-----------|------|
| JaCoCo coverage report | `backend/target/site/jacoco/` |
| SpotBugs XML report | `backend/target/spotbugsXml.xml` |
| OWASP Dependency Check | `backend/target/dependency-check-report.*` |

Estos artefactos sirven como evidencia para el informe final.

---

## Como se prueba

1. Crear una rama desde `develop`:

```bash
git checkout develop
git checkout -b feature/TICKET-010-pipeline-devsecops
```

2. Hacer commit de los cambios:

```bash
git add .github/workflows/main.yml backend/pom.xml docs/TICKET-010.md
git commit -m "Implement TICKET-010 DevSecOps pipeline"
```

3. Subir la rama:

```bash
git push origin feature/TICKET-010-pipeline-devsecops
```

4. Abrir Pull Request hacia `develop`.

5. Entrar a la pestana **Actions** de GitHub y verificar:

- El workflow se ejecuta automaticamente.
- El job termina en verde si build, tests, cobertura y scanners pasan.
- El job termina en rojo si hay errores de compilacion, tests fallidos, cobertura insuficiente, bugs altos, vulnerabilidades altas o Quality Gate fallido.

---

## Criterios de aceptacion

| Criterio | Estado |
|----------|--------|
| Action se dispara al hacer PR a develop | Completado |
| Pipeline instala JDK 25 | Completado |
| Maven build y tests automaticos | Completado |
| JaCoCo Coverage en CI | Completado |
| SonarCloud Scan | Completado, requiere `SONAR_TOKEN` y variables |
| OWASP Dependency Check | Completado |
| SpotBugs | Completado |
| Si hay fallos, pipeline rojo | Completado |
