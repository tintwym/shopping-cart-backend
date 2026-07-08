# syntax=docker/dockerfile:1

# Build stage — must match java.version in pom.xml (21)
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw \
    && ./mvnw -B -q dependency:resolve dependency:resolve-plugins -DskipTests

COPY src src
RUN ./mvnw -B -q -DskipTests package \
    && cp target/shopping-cart-backend-*.jar /app/app.jar

# Runtime stage
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring

COPY --from=build --chown=spring:spring /app/app.jar app.jar

USER spring:spring

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
