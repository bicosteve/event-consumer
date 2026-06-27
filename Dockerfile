# Multi-stage build:
#   STAGE 1 (builder) compiles & packages the Spring Boot fat jar.
#   STAGE 2 (runtime) ships only the JRE + jar on a slim, non-root image.

# STAGE 01 :: Build & dependency cache
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -B

COPY src ./src

# Build the app
RUN ./mvnw clean package -DskipTests -B


# STAGE 02 :: Runtime (slim, non-root, production hardened)
FROM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app

RUN useradd -r -u 1001 appusr \
    && mkdir -p /app/logs \
    && chown -R appusr:appusr /app


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


EXPOSE 5003

USER appusr

ENTRYPOINT ["java", "-jar", "event-consumer.jar"]

