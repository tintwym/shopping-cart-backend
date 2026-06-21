# Shopping cart backend

Spring Boot **REST API** and **PostgreSQL** (Neon) for the shopping cart app. JWT auth, Stripe checkout, and product image static files. No server-side HTML UI.

## Requirements

- Java 17
- Maven 3.9+
- PostgreSQL (Neon for production, or local Postgres via Docker Compose)

## Configuration

Copy the example properties file and fill in real values:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Key settings:

- **Database** — `SPRING_DATASOURCE_*` or `DATABASE_URL` (Neon connection string on Render)
- **JWT** — `JWT_SECRET`
- **Stripe** — `STRIPE_API_KEY`, `STRIPE_WEBHOOK_SECRET`
- **Flutter client** — `APP_FRONTEND_BASE_URL` (default `http://localhost:8081`)

See `application.properties.example` and `.env.example` for all keys.

## Run locally

```bash
./mvnw spring-boot:run
```

API base: **http://localhost:8080/api**

## Docker (pre-deploy smoke test)

```bash
docker compose up --build
```

Starts Postgres 16 + API on port 8080.

## Render deploy

Use `render.yaml` with `DATABASE_URL` pointing at Neon.

## Tests

```bash
./mvnw test
```

Tests use in-memory H2 in PostgreSQL compatibility mode.

## Tech stack

- Spring Boot 3.5 (Web, Data JPA, Actuator)
- PostgreSQL / Neon (runtime), H2 (tests)
- JWT (jjwt), Stripe Java SDK, Lombok
