####
# Multi-stage build for Quarkus application
####

## Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn package -DskipTests -B

## Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create non-root user
RUN groupadd -r quarkus && useradd -r -g quarkus quarkus

# Copy built artifacts from build stage
COPY --from=build --chown=quarkus:quarkus /app/target/quarkus-app/lib/ /app/lib/
COPY --from=build --chown=quarkus:quarkus /app/target/quarkus-app/*.jar /app/
COPY --from=build --chown=quarkus:quarkus /app/target/quarkus-app/app/ /app/app/
COPY --from=build --chown=quarkus:quarkus /app/target/quarkus-app/quarkus/ /app/quarkus/

# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Switch to non-root user
USER quarkus

EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "/app/quarkus-run.jar"]
