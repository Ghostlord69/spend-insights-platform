# Customer Spend Insights & Event Collection Platform

Local, production-shaped PoC for ingesting customer spend/interaction events and exploring analytics-ready spend trends.

**Stack:** Java 21 ù Spring Boot ù React ù PostgreSQL ù JWT ù Docker ù OpenAPI ù JUnit

## What you get

- JWT-secured Spring Boot REST APIs (`register` / `login`, event create/query, spend-summary analytics)
- React dashboard for event ingestion and spend-trend charts
- PostgreSQL persistence with Flyway migrations
- OpenAPI/Swagger UI
- JUnit + Testcontainers integration coverage
- Docker Compose packaging for one-command local run

## Quick start (Docker)

Build artifacts on the host, then package with Compose:

```bash
cd spend-insights
make up
# equivalent:
#   cd backend && ./gradlew bootJar -x test
#   cd frontend && npm ci && npm run build
#   docker compose up --build -d
```

| Service   | URL |
|-----------|-----|
| Dashboard | http://localhost:3000 |
| API       | http://localhost:8080 |
| Swagger   | http://localhost:8080/swagger-ui.html |
| Health    | http://localhost:8080/actuator/health |

1. Open the dashboard and create an account.
2. Click **Seed sample events** (or ingest manually).
3. Review daily net spend and category breakdown.

Stop with `docker compose down`. Add `-v` to also drop the Postgres volume.

## API overview

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| `POST` | `/api/v1/auth/register` | No | Create account + JWT |
| `POST` | `/api/v1/auth/login` | No | Login + JWT |
| `POST` | `/api/v1/events` | Bearer | Ingest spend/interaction event |
| `GET` | `/api/v1/events` | Bearer | Query events (`customerId`, paging) |
| `GET` | `/api/v1/events/{id}` | Bearer | Fetch one event |
| `GET` | `/api/v1/analytics/spend-summary` | Bearer | Trends + category breakdown |

Example ingest payload:

```json
{
  "customerId": "cust-1001",
  "eventType": "PURCHASE",
  "category": "Travel",
  "amount": 249.5,
  "currency": "USD",
  "channel": "web",
  "description": "Hotel booking",
  "occurredAt": "2026-03-20T10:15:00Z",
  "metadata": { "city": "Bangkok" }
}
```

`eventType` values: `PURCHASE`, `REFUND`, `INTERACTION`, `SUBSCRIPTION`.

## Local development (without full Compose)

### Database

```bash
docker compose up -d db
```

### Backend

```bash
cd backend
./gradlew bootRun
```

Defaults (see `src/main/resources/application.yml`):

- JDBC: `jdbc:postgresql://localhost:5432/spend_insights`
- User/pass: `spend` / `spend_secret`
- JWT secret via `JWT_SECRET` (override for anything beyond local use)

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Vite proxies `/api` to `http://localhost:8080`. App: http://localhost:5173

## Tests

Requires Docker (Testcontainers pulls Postgres):

```bash
cd backend
./gradlew test
```

## Project layout

```
spend-insights/
??? backend/          Spring Boot API, Flyway, JWT, tests
??? frontend/         React (Vite) dashboard
??? docker-compose.yml
??? README.md
```

## Production-minded local defaults

- Stateless JWT security, BCrypt password hashing, CORS allow-list
- Flyway-owned schema (`ddl-auto: validate`)
- HikariCP pooling, Actuator health/info/metrics
- Structured API error responses + Bean Validation
- Multi-stage Docker images (JDK build ? JRE runtime; Node build ? nginx)
- Non-root backend container user, healthchecks on all services

This is intended for **local** use. Change `JWT_SECRET` and DB credentials before any shared deployment.
