# Spending Insight

A full-stack spend analytics platform for capturing customer purchase and interaction events, persisting them securely, and visualizing spend trends through a modern dashboard.

Built for local development with a production-shaped architecture: JWT-secured APIs, PostgreSQL persistence, OpenAPI documentation, automated tests, and one-command Docker Compose deployment.

---

## Overview

**Spending Insight** helps you:

- Register and authenticate users with JWT-based security
- Ingest spend and interaction events (purchases, refunds, subscriptions, and more)
- Query event history with filtering and pagination
- Explore analytics — daily net spend trends and category breakdowns — in a React dashboard

Ideal as a portfolio project or a starting point for customer spend / fintech-style analytics services.

---

## Tech Stack

| Layer | Technologies |
|-------|----------------|
| **Backend** | Java 21, Spring Boot, Spring Security, JWT, Flyway, Hibernate/JPA |
| **Frontend** | React, Vite, Recharts |
| **Database** | PostgreSQL 16 |
| **API docs** | OpenAPI 3 / Swagger UI |
| **Testing** | JUnit, Testcontainers |
| **Ops** | Docker, Docker Compose, Actuator health checks |

---

## Features

- **Authentication** — Register / login with BCrypt password hashing and stateless JWT
- **Event ingestion** — Create and query spend/interaction events with validation
- **Analytics** — Spend summary with trend points and category breakdown
- **Dashboard** — Seed sample data, browse events, and view charts
- **Schema management** — Flyway migrations with `ddl-auto: validate`
- **API explorer** — Swagger UI for interactive endpoint testing
- **Containerized run** — Full stack (API + UI + Postgres) via Compose

---

## Architecture

```text
┌─────────────────┐     JWT + REST      ┌──────────────────────┐
│  React Dashboard│ ──────────────────► │  Spring Boot API     │
│  (nginx :3000)  │                     │  (:8080)             │
└─────────────────┘                     │  Auth · Events ·     │
                                        │  Analytics           │
                                        └──────────┬───────────┘
                                                   │ JDBC
                                                   ▼
                                        ┌──────────────────────┐
                                        │  PostgreSQL 16       │
                                        └──────────────────────┘
```

---

## Quick Start

### Prerequisites

- Docker and Docker Compose (Docker Desktop or OrbStack)
- For local (non-Docker) development: Java 21, Node.js 20+, and Gradle wrapper (included)

### Run with Docker

```bash
git clone <your-repo-url>
cd spend-insights   # or your cloned folder name

cp .env.example .env   # optional; defaults work for local use

make up
```

This builds the backend JAR and frontend assets, then starts Postgres, the API, and the dashboard.

| Service | URL |
|---------|-----|
| **Dashboard** | http://localhost:3000 |
| **API** | http://localhost:8080 |
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **Health** | http://localhost:8080/actuator/health |

**First-time walkthrough**

1. Open the dashboard and create an account.
2. Click **Seed sample events** (or ingest events manually).
3. Review daily net spend and the category breakdown.

**Stop the stack**

```bash
make down
# or: docker compose down
# add -v to also remove the Postgres volume
```

**Logs**

```bash
make logs
```

---

## API Reference

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/v1/auth/register` | Public | Create account and receive JWT |
| `POST` | `/api/v1/auth/login` | Public | Login and receive JWT |
| `POST` | `/api/v1/events` | Bearer | Ingest a spend/interaction event |
| `GET` | `/api/v1/events` | Bearer | List events (`customerId`, paging) |
| `GET` | `/api/v1/events/{id}` | Bearer | Fetch a single event |
| `GET` | `/api/v1/analytics/spend-summary` | Bearer | Trends + category breakdown |

### Example: ingest an event

```http
POST /api/v1/events
Authorization: Bearer <token>
Content-Type: application/json
```

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

**Supported `eventType` values:** `PURCHASE`, `REFUND`, `INTERACTION`, `SUBSCRIPTION`

---

## Local Development (without full Compose)

### 1. Database only

```bash
docker compose up -d db
```

### 2. Backend

```bash
cd backend
./gradlew bootRun
```

Defaults (see `backend/src/main/resources/application.yml`):

| Setting | Default |
|---------|---------|
| JDBC URL | `jdbc:postgresql://localhost:5432/spend_insights` |
| Username / password | `spend` / `spend_secret` |
| JWT | Override with `JWT_SECRET` for anything beyond local use |

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Vite serves the app at http://localhost:5173 and proxies `/api` to `http://localhost:8080`.

---

## Testing

Backend tests use Testcontainers (Docker required for Postgres):

```bash
cd backend
./gradlew test
```

---

## Project Structure

```text
spend-insights/
├── backend/                 # Spring Boot API, Flyway, JWT, tests
│   ├── src/main/java/...
│   └── src/test/java/...
├── frontend/                # React (Vite) dashboard
│   └── src/
├── docker-compose.yml
├── Makefile
├── .env.example
└── README.md
```

---

## Configuration

Copy `.env.example` to `.env` to override Compose defaults:

| Variable | Purpose |
|----------|---------|
| `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` | Database credentials |
| `JWT_SECRET` | Signing key (use a long random secret outside local demos) |
| `JWT_EXPIRATION_MS` | Token lifetime |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins |

---

## Security Notes

- Passwords are hashed with BCrypt; APIs are protected with JWT (except auth endpoints).
- CORS is allow-listed; Actuator exposes health for orchestration.
- Defaults are for **local development only**. Change `JWT_SECRET` and database credentials before any shared or production deployment.

---

## License

This project is provided for educational and portfolio use. Add a license file if you publish it under an open-source license (e.g. MIT).
