# Shopping cart backend

Spring Boot API and server-side views for the shopping cart app. It exposes REST endpoints (consumed by the React frontend), Thymeleaf templates for some flows, JWT auth, MySQL persistence, and Stripe checkout.

## Requirements

- Java 17
- Maven 3.9+
- MySQL 8 (local instance with a database you create)

## Configuration

Copy the example properties file and fill in real values:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Edit `src/main/resources/application.properties`:

- **Database** — JDBC URL, username, and password for your MySQL database.
- **JWT** — `jwt.secret` (use a long random string in production) and optional `jwt.expiration` (milliseconds).
- **Stripe** — `stripe.api.key` and `stripe.webhook.secret` from the Stripe dashboard.
- **Frontend** — `app.frontend.base-url` should match where the React app runs (default in the example is `http://localhost:5173`).

See `application.properties.example` for all keys and comments.

## Run

From this directory:

```bash
./mvnw spring-boot:run
```

If you do not use the Maven wrapper:

```bash
mvn spring-boot:run
```

The API is served under the default Spring Boot port **8080** unless you set `server.port`. REST paths used by the frontend are under `/api` (see the frontend `.env.example` for the full base URL).

## Tests

Tests use an in-memory H2 database (`src/test/resources/application.properties`). Run:

```bash
./mvnw test
```

## Tech stack

- Spring Boot 3.5 (Web, Data JPA, Data REST, Thymeleaf)
- MySQL (runtime), H2 (tests)
- JWT (jjwt), Stripe Java SDK, Spring Session, Lombok
