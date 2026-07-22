# ==========================
# Stage 1: Build the app
# ==========================
FROM maven:3.9.12-eclipse-temurin-25 AS builder

WORKDIR /app

# Copy Maven wrapper and project descriptor first to improve build cache usage.
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Download dependencies before copying source files.
RUN ./mvnw -B -DskipTests dependency:go-offline

COPY docs docs
COPY src src

# Build the executable Spring Boot JAR.
RUN ./mvnw -B clean package -DskipTests


# ==========================
# Stage 2: Runtime
# ==========================
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copy the built JAR from the build stage.
COPY --from=builder /app/target/example-service-*.jar /app/app.jar

# download opentelemetry-javaagent
RUN apt-get update && apt-get install -y wget && \
    mkdir -p /var/opentelemetry && \
    wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar -O /var/opentelemetry/opentelemetry-javaagent.jar

# Set environment variables for UTF-8 encoding and UTC timezone.
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 -Duser.timezone=UTC"

# Supports optional runtime flags through JAVA_OPTS.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
