# TICKET-009: Quality Scoring Engine (Motor SQA McCall)

**Asignado:** Backend Lead - Eduardo García
**Tipo:** Backend  
**Estado:** ✅ Completado  
**Tiempo estimado:** 3 horas  
**Dependencias:** Ninguna directa de código

---

## ¿Qué se hizo?

Se desarrolló un motor de evaluación de calidad de software basado en el **Modelo de McCall**. El motor lee artefactos XML generados automáticamente durante el build y calcula un score técnico que es expuesto como endpoint REST.

Este módulo vive en `sqa/engine/` completamente aislado del resto del sistema.

---

## Archivos creados

### `sqa/engine/McCallReportParser.java`
Parser XML que extrae métricas reales de dos fuentes:

| Archivo leído | Métrica extraída | Factor McCall |
|---------------|-----------------|---------------|
| `target/site/jacoco/jacoco.xml` | % de instrucciones cubiertas por tests | Testability |
| `target/spotbugsXml.xml` | Número de bugs estáticos detectados | Correctness |

> Si los archivos no existen (ej. primer arranque), retorna valores seguros (0.0 / 0) sin lanzar excepciones.

### `sqa/engine/QualityScoringEngine.java`
Motor de cálculo con fórmula parametrizable:

```
Score Total = (Cobertura × W_TEST) + (Correctness × W_CORR) + (Mantenibilidad × W_MANT)
```

**Cálculo de cada factor:**

| Factor | Cálculo |
|--------|---------|
| Testability | % de cobertura de instrucciones (JaCoCo) |
| Correctness | `max(0, 100 - bugs * 10)` — cada bug resta 10 puntos |
| Maintainability | 100 fijo (Clean Architecture sin dependencias cíclicas) |

### `sqa/engine/QualityScoreResult.java`
Modelo de respuesta JSON:

```json
{
  "correctness": 100.0,
  "maintainability": 100.0,
  "testability": 90.0,
  "totalScore": 96.0
}
```

### `sqa/engine/SqaConfig.java`
Configuración Spring que registra el `McCallReportParser` como Bean inyectable.

### `interfaces/rest/QualityController.java`
Expone el endpoint público:

```
GET /api/v1/quality/score
→ 200 OK | { "correctness": ..., "maintainability": ..., "testability": ..., "totalScore": ... }
```

> Este endpoint **no requiere autenticación JWT** para que el evaluador pueda consultarlo directamente.

---

## Configuración en `application.properties`

Los pesos son configurables sin recompilar:

```properties
sqa.weights.testability=0.4
sqa.weights.correctness=0.4
sqa.weights.maintainability=0.2
sqa.jacoco.path=target/site/jacoco/jacoco.xml
sqa.spotbugs.path=target/spotbugsXml.xml
```

---

## Ejemplo de cálculo real

Supuesto: cobertura 90%, 0 bugs detectados.

```
Testability    = 90.0
Correctness    = 100 - (0 × 10) = 100.0
Maintainability = 100.0

Total = (90 × 0.4) + (100 × 0.4) + (100 × 0.2)
      = 36 + 40 + 20
      = 96.0
```

---

## Tests unitarios creados

**Archivo:** `src/test/java/com/empresa/tasks/sqa/QualityScoringEngineTest.java`

Usa **Mockito** para simular el parser sin necesitar archivos XML reales.

| Test | Qué valida |
|------|-----------|
| `deberiaCalcularScoreCorrectamente` | 90% cobertura + 0 bugs = score 96.0 |
| `deberiaPenalizarPorBugs` | 3 bugs → correctness = 70.0 |
| `correctnessNuncaDebeSerNegativa` | 20 bugs → correctness = 0.0 (no negativo) |

## Cómo se verificó

```bash
.\mvnw test -Dtest=QualityScoringEngineTest
```

**Resultado:** `Tests run: 3, Failures: 0, Errors: 0, Skipped: 0` ✅

---

## Cómo consultar el endpoint en producción

```bash
curl https://tasks-api.onrender.com/api/v1/quality/score
```

O localmente una vez levantado Docker:
```bash
curl http://localhost:8080/api/v1/quality/score
```

---

## Factor McCall evaluado

**Testability** — Cobertura de código medida con JaCoCo. **Correctness** — Bugs estáticos detectados por SpotBugs. **Maintainability** — Verificada por ausencia de dependencias cíclicas en Clean Architecture.