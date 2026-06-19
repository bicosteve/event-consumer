# Multi-stage build:
#   STAGE 1 (builder) compiles & packages the Spring Boot fat jar.
#   STAGE 2 (runtime) ships only the JRE + jar on a slim, non-root image.

# STAGE 01 :: Build & dependency cache
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy only the files needed to resolve dependencies first so this layer is
# cached and re-used whenever the source changes but dependencies do not.
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -B

# Copy the source and build the application.
COPY src ./src

RUN ./mvnw clean package -DskipTests -B


# STAGE 02 :: Runtime (slim, non-root, production hardened)
FROM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app

# Create an unprivileged user and a writable logs directory.
RUN useradd -r -u 1001 appusr \
    && mkdir -p /app/logs \
    && chown -R appusr:appusr /app

# Copy only the built artifact from the builder stage.
COPY --from=builder /app/target/*.jar event-consumer.jar

# Production runtime defaults.
#   - Activate the prod profile via SPRING_PROFILES_ACTIVE.
#   - Container-aware JVM memory settings (picked up automatically by the JVM
#     via JAVA_TOOL_OPTIONS, so no wrapper shell is needed in the entrypoint).
#   - APP_PORT is the local fallback; Render injects $PORT at runtime and
#     application-prod.yaml resolves the listening port via ${PORT:${APP_PORT:5003}}.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0" \
    SPRING_PROFILES_ACTIVE=prod \
    APP_PORT=5003

# Render injects the listening port via $PORT; expose the documented default.
EXPOSE 5003

USER appusr

# Clean exec-form entrypoint: java runs as PID 1 (receives signals for graceful
# shutdown), no shell, no unquoted variable expansion. JVM flags come from
# JAVA_TOOL_OPTIONS and the server port is resolved by Spring from $PORT.
ENTRYPOINT ["java", "-jar", "event-consumer.jar"]

