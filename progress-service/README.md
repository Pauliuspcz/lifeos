# progress-service

Progress service for LifeOS. It stores the users' check-ins and logged amounts, enforces which dates can be logged, and publishes an event to RabbitMQ every time progress is created or deleted. It also gives the frontend the data for the Log page and the pace of amount goals.

- Port: `8082` (reached through the gateway on `8100`)
- Stack: Java 21, Spring Boot 4.1, Spring Security (resource server), Spring Data JPA, PostgreSQL, Flyway, RabbitMQ, Resilience4j
- Registers itself in Consul and reads shared config from Consul
- Runs with two instances in Docker Compose (`replicas: 2`)
- Database: `progress_db` (host port `5434`)

## Endpoints

Through the gateway the route is `/api/progress/**`. All endpoints need a Bearer token, and a user only ever sees their own entries.

| Method | Path                            | Purpose                                                                                                                 |
| ------ | ------------------------------- | ----------------------------------------------------------------------------------------------------------------------- |
| POST   | `/api/progress`                 | Log a check-in or an amount. Body: `goalId`, `date`, `amount` (amount goals only). Returns 201.                         |
| GET    | `/api/progress`                 | Own entries, newest first. Optional `?goalId=`.                                                                         |
| DELETE | `/api/progress/{id}`            | Delete an entry (only inside the log window). Returns 204.                                                              |
| GET    | `/api/progress/window`          | Entries for the Log page: current week and month, with the editable date range per tracking model. Optional `?goalId=`. |
| GET    | `/api/progress/summary?goalId=` | Total, target, expected pace and delta for an amount goal in its current period.                                        |

Logging a check-in twice for the same goal and day returns the existing entry (still 201), so a double click counts once.

## Logging rules

The service reads the goal from goal-service before saving, and the goal decides the tracking model.

| Goal           | Tracking model   | One entry per day | Target                                            | Dates that can be logged                                     |
| -------------- | ---------------- | ----------------- | ------------------------------------------------- | ------------------------------------------------------------ |
| CHECK, DAILY   | `DAILY_CHECK`    | yes               | 1 per day                                         | today and the 2 days before                                  |
| CHECK, WEEKLY  | `WEEKLY_COUNT`   | yes               | `timesPerPeriod` per calendar week (Monday start) | from the start of this week to today                         |
| CHECK, MONTHLY | `MONTHLY_COUNT`  | yes               | `timesPerPeriod` per calendar month               | from the start of this month, but at most 7 days back        |
| AMOUNT         | `CUMULATIVE_SUM` | no, many per day  | `targetAmount` per period or until `targetDate`   | today and the 2 days before, never before the current period |

- Future dates are rejected. The goal must exist, belong to the user and be `ACTIVE`.
- AMOUNT goals need an `amount`. CHECK goals ignore it.
- A weekly or monthly goal that has reached its target for the period rejects more entries.
- Delete follows the same date window as logging.
- Amount goals use rolling periods from the goal's start date (week, month or year). The summary compares the total with the expected pace for the days that have passed.

### Errors

All errors are JSON: `status`, `error`, `message`, `timestamp`, plus `fieldErrors` for validation problems.

| Status | When                                                                                        |
| ------ | ------------------------------------------------------------------------------------------- |
| 400    | Validation failed, malformed body, amount missing for an amount goal                        |
| 401    | Missing or invalid access token                                                             |
| 404    | Entry or goal not found (also for other users' goals)                                       |
| 409    | Goal is not active, target for the period already reached, entry changed by another request |
| 422    | Date outside the log window, unsupported tracking type or period                            |
| 503    | goal-service unavailable (down, timeout, circuit open)                                      |
| 500    | Unexpected error (details only in the logs)                                                 |

## Events

progress-service publishes to the topic exchange `lifeos.progress`:

| Routing key        | When                | Payload                                                                                                               |
| ------------------ | ------------------- | --------------------------------------------------------------------------------------------------------------------- |
| `progress.created` | An entry is saved   | `eventId`, `progressId`, `userId`, `goalId`, `date`, `amount`, `trackingModel`, `targetCount`, `period`, `occurredAt` |
| `progress.deleted` | An entry is deleted | `eventId`, `progressId`, `userId`, `goalId`, `date`, `amount`, `trackingModel`, `occurredAt`                          |

Amount goals are sent with period `DAILY`, so report-service counts them as days with progress. report-service consumes the events from its own queues and ignores an `eventId` it has already seen.

The event is sent right after the entry is saved or deleted. There is no outbox: if RabbitMQ is unreachable, the failure is logged as an error and the event is lost.

## Calls to goal-service

Before saving, and for the summary, the service calls `GET /api/goal/{id}` on goal-service. The call goes directly to `GOAL_SERVICE_URL` (not through the gateway) and forwards the user's `Authorization` header, so goal-service checks that the goal belongs to the user.

- Timeouts: connect 1 s, read 2 s.
- Circuit breaker `goal` (Resilience4j): count-based window of 10 calls, opens at 50% failures after at least 5 calls, stays open for 10 s, then allows 2 trial calls. `4xx` answers do not count as failures.
- Fail closed: if goal-service cannot be reached or the circuit is open, the request fails with `503`. A goal-service answer of `404` becomes `404`.

## Security

The service validates the JWT itself with the public key (`JWT_PUBLIC_KEY`) and checks the issuer. The user id is the token's `sub` claim. Only `/actuator/health` and `/error` are public.

## Configuration

Set in `.env` at the repo root (read by Docker Compose) or as environment variables.

| Variable                               | Used for                                                                                         |
| -------------------------------------- | ------------------------------------------------------------------------------------------------ |
| `POSTGRES_USER`, `POSTGRES_PASSWORD`   | Database login                                                                                   |
| `PROGRESS_DB_NAME`, `PROGRESS_DB_PORT` | Database name and port (defaults `progress_db`, `5434`)                                          |
| `JWT_PUBLIC_KEY`                       | RSA public key, Base64-encoded PEM                                                               |
| `JWT_ISSUER`                           | Expected issuer, default `lifeos-auth`                                                           |
| `RABBITMQ_USER`, `RABBITMQ_PASSWORD`   | RabbitMQ login (defaults `lifeos` / `lifeos_dev`)                                                |
| `GOAL_SERVICE_URL`                     | Base URL of goal-service, default `http://localhost:8081` (`http://goal-service:8081` in Docker) |
| `CORS_ALLOWED_ORIGINS`                 | Allowed browser origins, default `http://localhost:5173`                                         |
| `app.time-zone`                        | Time zone for "today", default `Europe/Oslo`                                                     |

## Database

PostgreSQL, schema managed by Flyway (`src/main/resources/db/migration`). Hibernate only validates the schema (`ddl-auto: validate`). Table: `progress`.

A partial unique index allows only one entry per user, goal and day for check goals. Amount goals are excluded, so they can have many entries per day. Two instances can therefore save at the same time without creating duplicates.

## Run

From the repo root, with the whole system:

```bash
docker compose up -d --build progress-service
```

Locally (needs Postgres, RabbitMQ and goal-service, and a `.env`):

```bash
cd progress-service
./mvnw spring-boot:run
```

Tests (Testcontainers needs Docker running):

```bash
./mvnw verify
```

## Project structure

```
progress-service/
  pom.xml
  Dockerfile
  src/main/java/no/kristiania/progressservice/
    ProgressServiceApplication
    config/        SecurityConfig, JsonAuthenticationEntryPoint, RabbitConfig,
                   RestClientConfig, TimeConfig
    exceptions/    GlobalExceptionHandler, ApiError, ApiException + subclasses
    client/        GoalClient, GoalSummary
    progress/
      controller/  ProgressController
      service/     ProgressService
      repository/  ProgressRepository
      entity/      Progress, TrackingModel
      dto/         request/response records
      events/      ProgressCreatedEvent, ProgressDeletedEvent
      utils/       LogWindow, AmountPeriod, Pace
  src/main/resources/
    application.yml
    db/migration/  Flyway migrations
```

## Logging

Logs go to stdout as JSON in ECS format, set for all services in the shared Consul config. Each line has `log.level`, `service.name` and, inside a request, `traceId` and `spanId`. With two instances, the same message can come from either one.

| Where                    | Level                | What                                                                                                             |
| ------------------------ | -------------------- | ---------------------------------------------------------------------------------------------------------------- |
| `GlobalExceptionHandler` | WARN / ERROR / DEBUG | Rejected requests (status + message), unexpected errors with stack trace, validation problems (field names only) |
| `ProgressService`        | INFO                 | Progress created and deleted, with `progressId`, `userId`, `goalId` and tracking model                           |
| `ProgressService`        | WARN / ERROR         | goal-service unavailable or circuit open, unexpected answers from goal-service, failed event publishing          |
| `RestClientConfig`       | WARN / INFO          | Circuit breaker `goal` changes state (WARN when it opens or half-opens, INFO when it closes)                     |

Never logged: tokens, request bodies. Users are identified by `userId`.

To see DEBUG lines from our own code, set `logging.level.no.kristiania.progressservice=DEBUG`.
