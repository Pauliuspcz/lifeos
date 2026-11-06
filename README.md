# LifeOS

LifeOS is a goal tracker. Users pick a goal from a catalog of templates (or create their own),
check in when they do it, and follow their progress over time with streaks,
completion rate and history.

It was built by a team of two as the portfolio project for PG3402 Microservices at Kristiania Universiry College (autumn 2026). 
The system is split into four Spring Boot microservices, each with its own PostgreSQL database,
behind a Spring Cloud Gateway with consul for service discovery and configuration. Services talk 
synchronously over REST and asynchronously through RabbitMQ.

**What a user can do:**
- Register and log in with email/password or Google
- Create custom goals or choose from premade, edit and archive them
- check in on today's checkins and log progress for earlier days.
- See insights per habit: current streak, completion rate, last 30 days vs the 30 before, and history per period.

## Contents

- [Quick start](#quick-start)
    - [Other ways to run](#other-ways-to-run)
    - [Ports](#ports)
- [Test credentials and seed data](#test-credentials-and-seed-data)
- [Architecture](#architecture)
    - [Services](#services)
    - [Communication](#communication)
    - [Gateway, discovery, config, scaling](#gateway-discovery-config-scaling)
- [User stories (how to test them)](#user-stories-how-to-test-them)
- [API (Postman)](#api-postman)
- [Decisions, assumptions and simplifications](#decisions-assumptions-and-simplifications)
- [Team contributions](#team-contributions)

## Quick start

**Requirements:** Docker Desktop (with Docker Compose v2). Nothing else needs to be installed:
Java, Maven and Node all run inside the containers.

1. Unzip the project and open a terminal in the project root (the folder with `docker-compose.yaml`).
2. Copy `.env.example` to a new file named `.env` (in the same folder). It contains dev-only values that work as-is. (NOT WORKING NOW)


3. Build and start everything:
```bash
docker compose up -d --build
```
The first build takes a few minutes (Maven and npm download dependencies).

4. Wait until all services show as passing in the Consul UI at: http://localhost:8500
(about 30–60 seconds after the containers start). Until then the gateway may answer `503`.

5. Open http://localhost:5173 and log in with the test user (see [Test credentials](#test-credentials-and-seed-data)). (MISSING)

**Stop:** `docker compose down`
**Reset all data (wipes the databases):** `docker compose down -v`

### Other ways to run

**A. Backend in Docker, frontend locally (for frontend development)**

**Required node version?**

```bash
docker compose up -d --build
docker compose stop frontend
cd frontend
npm install
npm run dev
```

Open http://localhost:5173. The Vite dev server proxies all API calls to the gateway on `localhost:8100`
(override with the `GATEWAY_URL` environment variable).

**B. Infrastructure in Docker, services from IntelliJ (for backend development)**

Requires JDK 21.

1. Start only the databases, RabbitMQ and Consul:
   ```bash
   docker compose up -d user-db goal-db progress-db report-db rabbitmq consul consul-importer
   ```
2. In IntelliJ, run `GatewayApplication` and the `...ServiceApplication` class of each service you need.
   Each service reads `.env` from its working directory, so set the run configuration's
   working directory to the project root.
3. Run the frontend as in A.

### Ports

| What        | URL/PORT                                       |
|-------------|------------------------------------------------|
| Frontend    | http://localhost:5173                          |
| API gateway | http://localhost:8100                          |
| Consul UI   | http://localhost:8500                          |
| RabbitMQ UI | http://localhost:15672 (login creds in `.env`) |
| PostgreSQL  |user 5432, goal 5433, progress 5434, report 5435|

## Test credentials and seed data

## Test credentials and seed data

All seed data is inserted automatically by Flyway migrations on first startup. No manual setup is needed.

**Test user**

| Email               | Password    |
|---------------------|-------------|
| `TODO`              | `TODO`      |

You can also register a new user on the login page.

**Seeded data**
- Goal catalog: 6 categories, 7 directions and 17 habit templates (`goal-service/.../V005__seed_catelog.sql`).
- TODO: demo habits and check-ins for the test user.

To start over with a clean database: `docker compose down -v`, then `docker compose up -d --build`.

## Architecture
LifeOS consists of four Spring Boot microservices, each owning its own PostgreSQL database.
The React frontend talks only to the API gateway, which routes requests to the services it finds in Consul.
Progress-service calls goal-service synchronously to check a goal before logging progress, and publishes
events to RabbitMQ that report-service consumes asynchronously to build its statistics.

**MISSING:** Diagram

### Services
| Service          | Container Port | Responsibility                                                        | Database      | Instances | Docs                                   |
|------------------|----------------|-----------------------------------------------------------------------|---------------|-----------|----------------------------------------|
| user-service     | 8080           | Register, login (email/password + Google), issues JWT, user profile   | `user_db`     | 1         | [README](user-service/README.md)       |
| goal-service     | 8081           | User goals and habits, template catalog                               | `goal_db`     | 1         | [README](goal-service/README.md)       |
| progress-service | 8082           | Check-ins / logged progress, publishes progress events                | `progress_db` | 2         | —                                      |
| report-service   | 8083           | Consumes progress events, computes streak, completion rate, history   | `report_db`   | 2         | —                                      |
| gateway          | 8100           | Single entry point, routes `/auth`, `/profile`, `/api/goal`, `/api/catalog`, `/api/progress`, `/api/reports` | —  | 1 | — |

Every service validates the JWT itself with the public key, so user-service is only needed to log in, not on every request.

### Communication
### Communication

**Synchronous (REST)**
- **Frontend -> gateway -> services:** every request carries the user's JWT (`Authorization: Bearer`).
  The gateway looks up the service in Consul and load-balances between instances.
- **progress-service -> goal-service:** before saving a check-in, progress calls `GET /api/goal/{id}` with the
  user's token, to check that the goal exists and belongs to the user and to read its period and target.
  This call goes directly to `goal-service:8081` on the Docker network, not through the gateway.
  If goal-service is down, logging fails with `503` (consistency over availability).


  **Asynchronous (RabbitMQ)**
- progress-service publishes `progress.created` and `progress.deleted` events to the topic exchange `lifeos.progress`.
- report-service consumes them from its own durable queues (`report.progress.created`, `report.progress.deleted`)
  and keeps its own copy of the data, which it uses to compute statistics. It never calls the other services.
- Events carry a unique `eventId`, so a redelivered event is not counted twice.
- With two report instances, each event is processed by only one of them (competing consumers).

## Gateway, discovery, config, scaling

- **API gateway** (Spring Cloud Gateway, port 8100): the single entry point. Routes by path
  (`/auth/**`, `/profile/**` -> user, `/api/goal/**`, `/api/catalog/**`-> goal, `/api/progress/**`-> progress,
  `/api/reports/**`-> report) to `lb://<service-name>`, load-balancing between instances.
  The gateway does no authentication itself; each service validates the JWT.
- **Service discovery and health checks** (Consul, UI on http://localhost:8500): every service registers on startup,
  and Consul checks each instance's `/actuator/health`. The gateway only routes to instances that are passing.
- **Central configuration** (Consul KV): shared settings are stored under `config/application/data`, loaded into
  Consul at startup by the `consul-importer` container, and read by all services (`spring.config.import: optional:consul:`).
  Today this holds the discovery settings, including a random instance id per instance.
- **Scaling:** progress-service and report-service run with two instances each (`deploy: replicas: 2`).
  The random instance id lets both instances register separately, and the gateway spreads requests across them.
  More instances: `docker compose up -d --scale report-service=3`.

## User stories (how to test them)

**MISSING**

## API (postman)

**Expected changes:** add later

## Decisions, assumptions and simplifications

Missing

## Scope changes

**Write at the end**

## Team contributions

**Write at the end**