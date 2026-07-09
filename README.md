# Shopping cart backend

Spring Boot **REST API** and **Neon PostgreSQL** for the shopping cart app. JWT auth, Stripe checkout, and Cloudinary product images.

## Requirements

- Java 17
- Maven 3.9+
- [Neon](https://neon.tech) PostgreSQL database (free tier works)

## Configuration

1. Create a Neon project and copy the connection string from **Connect**.
2. Copy env template and fill in values:

```bash
cp .env.example .env
# Edit .env — set DATABASE_URL to your Neon postgresql://… string
```

Key settings:

| Variable | Purpose |
|----------|---------|
| `DATABASE_URL` | Neon connection string (`postgresql://…?sslmode=require`) |
| `JWT_SECRET` | JWT signing key (32+ chars) |
| `STRIPE_API_KEY` | Stripe secret key |
| `STRIPE_WEBHOOK_SECRET` | Stripe webhook signing secret |
| `APP_FRONTEND_BASE_URL` | Flutter web URL for Stripe redirects |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary cloud name |
| `CLOUDINARY_API_KEY` | Cloudinary API key |
| `CLOUDINARY_API_SECRET` | Cloudinary API secret |
| `ADMIN_SEED_USERNAME` | Optional first admin username (created on startup if missing) |
| `ADMIN_SEED_PASSWORD` | Password for the seeded admin |

See **[DEPLOY.md](DEPLOY.md)** for the full production checklist (Google Cloud Run, Stripe webhook).

## Run locally

**Option A — Maven (recommended for development)**

```bash
export $(grep -v '^#' .env | xargs)   # load DATABASE_URL from .env
./mvnw spring-boot:run
```

**Option B — Docker**

```bash
docker compose --env-file .env up --build
```

API base: **http://localhost:8080/api**

## Google Cloud Run deploy

Deploy files for this service:

| File | Location |
|------|----------|
| `Dockerfile`, `.dockerignore`, `cloudbuild.yaml` | Repo root (this repository) |
| `scripts/deploy-cloud-run.sh` | One-command deploy helper |

Set `DATABASE_URL` and other env vars per [DEPLOY.md](DEPLOY.md).

| Setting | Value |
|---------|--------|
| Runtime | Docker on Cloud Run |
| Health check | `/actuator/health/liveness` |
| Region | e.g. `asia-southeast1` |

Quick deploy:

```bash
export GCP_PROJECT_ID=your-project-id
./scripts/deploy-cloud-run.sh
```

## Tests

```bash
./mvnw test
```

Integration tests use **Testcontainers** with PostgreSQL 16 (real Postgres, not H2).

## Tech stack

- Spring Boot 3.5 (Web, Data JPA, Actuator)
- PostgreSQL / Neon
- JWT (jjwt), Stripe Java SDK, Cloudinary, Lombok

## License

MIT — see [LICENSE](LICENSE).
