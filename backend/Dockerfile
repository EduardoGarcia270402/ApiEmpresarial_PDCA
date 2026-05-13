# ─────────────────────────────────────────
# STAGE 1: Build con Maven
# ─────────────────────────────────────────
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

# Copiar wrapper y pom primero (cache de dependencias)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Descargar dependencias (cacheado si pom.xml no cambia)
RUN ./mvnw dependency:go-offline -q

# Copiar código fuente y compilar
COPY src/ src/
RUN ./mvnw clean package -DskipTests -q

# ─────────────────────────────────────────
# STAGE 2: Imagen final liviana
# ─────────────────────────────────────────
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copiar el JAR generado
COPY --from=builder /app/target/*.jar app.jar

# Puerto expuesto
EXPOSE 8080

# Perfil de producción
ENV SPRING_PROFILES_ACTIVE=prod

# Arrancar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]