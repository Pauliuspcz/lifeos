# report-service

Report service for LifeOS. It listens to the progress events from progress-service, keeps its own copy of every check-in, and computes the statistics the frontend shows on Insights: streaks, completion rate, history per period, an activity calendar and suggestions for weekly and monthly goals. It also stores a small daily wellbeing log (mood and sleep).

- Port: `8083` (reached through the gateway on `8100`)
- Stack: Java 21, Spring Boot 4.1, Spring Security (resource server), Spring Data JPA, PostgreSQL, Flyway, RabbitMQ
- Registers itself in Consul and reads shared config from Consul
- Runs with two instances in Docker Compose (`replicas: 2`)
- Database: `report_db` (host port `5435`)
- Never calls other services: everything is computed from its own copy of the events

## Endpoints

Through the gateway the route is `/api/reports/**`. All endpoints need a Bearer token, and a user only ever sees their own data.

| Method | Path                           | Purpose                                                                                  |
| ------ | ------------------------------ | ---------------------------------------------------------------------------------------- |
| GET    | `/api/reports/goals/{goalId}`  | Statistics for one goal. A goal without check-ins gives an empty report (never `404`).   |
| GET    | `/api/reports/activity`        | Activity calendar for the last 53 weeks and totals. Supports `ETag` / `304`.             |
| PUT    | `/api/reports/day-log`         | Save today's mood (1-5) and/or sleep hours (0-24). Only the fields you send are changed. |
| GET    | `/api/reports/day-log/today`   | Today's mood and sleep (empty values if nothing is logged).                              |
| GET    | `/api/reports/day-log/summary` | Average mood and sleep for the last 30 days and the number of days logged.               |

### Goal report

`goalId`, `totalSessions`, `lastDate`, `last30Days`, `previous30Days`, `currentStreak`, `successRate` (0-100), `history` (the last 12 periods, each with `periodStart`, `count` and `reached`) and `suggestion` (`recommendation` and `suggestedTarget`).

### Activity report

`totalCheckins`, `activeDays`, `last30Days`, `activeStreaks` and `days`: one entry per day with `count` and a `level` from 0 to 4 for the calendar colors. The response has `Cache-Control: no-cache, private` and an `ETag`, so the browser only downloads it again when something changed.

## How the numbers are computed

- Only one check-in per goal and day counts.
- The period (`DAILY`, `WEEKLY`, `MONTHLY`, `YEARLY`) and the target per period come from the latest check-in for the goal. Weeks start on Monday.
- **Streak**: the number of periods in a row that reached the target. The current period only counts when it has reached the target; otherwise the streak counts back from the previous period.
- **Success rate**: share of periods since the first check-in that reached the target. The current period counts only if it has reached the target.
- **Suggestion** (weekly and monthly goals only): looks at the last 4 finished weeks or 3 finished months with the same target.
  - `RAISE` (target + 1, at most 7 per week or 28 per month) when every period reached the target.
  - `LOWER` when the typical count is at least 2 below the target.
  - `KEEP` otherwise, `INACTIVE` when the last check-in is more than one period old, and `NONE` when there is not enough history.
- Amount goals arrive as daily events, so they count as days with progress. They are left out of `activeStreaks`.

### Errors

All errors are JSON: `status`, `error`, `message`, `timestamp`.

| Status | When                                                            |
| ------ | --------------------------------------------------------------- |
| 400    | Mood or sleep out of range, malformed body, bad parameter value |
| 401    | Missing or invalid access token                                 |
| 404    | Unknown path                                                    |
| 500    | Unexpected error (details only in the logs)                     |

## Events

report-service consumes the topic exchange `lifeos.progress` from two durable queues:

| Queue                     | Routing key        | Effect                                                                                              |
| ------------------------- | ------------------ | --------------------------------------------------------------------------------------------------- |
| `report.progress.created` | `progress.created` | Saves a record. An `eventId` that is already stored is skipped, so a redelivered event counts once. |
| `report.progress.deleted` | `progress.deleted` | Deletes the record with the same `progressId`.                                                      |

- The service declares the exchange, queues and bindings itself, so it works even if progress-service has not started yet.
- With two instances the queues are shared (competing consumers): each event is handled by one instance.
- Failed messages are retried (Spring AMQP retry), then rejected without requeue. There is no dead-letter queue, so a message that keeps failing is dropped.

## Known limitations

- Report only knows a goal through the check-ins. If a goal's period or target is edited (for example from 3 to 5 times a week), report uses the new values after the next check-in, and the history is then recalculated with the new target. goal-service sends no event for edits.
- Archiving or deleting a goal sends no event either, so the check-ins stay in the report (and in the activity calendar).
- If a delete event is processed before the create event for the same entry, the entry stays in the report.

## Security

The service validates the JWT itself with the public key (`JWT_PUBLIC_KEY`) and checks the issuer. The user id is the token's `sub` claim. Only `/actuator/health` and `/error` are public.

## Configuration

Set in `.env` at the repo root (read by Docker Compose) or as environment variables.

| Variable                             | Used for                                                 |
| ------------------------------------ | -------------------------------------------------------- |
| `POSTGRES_USER`, `POSTGRES_PASSWORD` | Database login                                           |
| `REPORT_DB_NAME`, `REPORT_DB_PORT`   | Database name and port (defaults `report_db`, `5435`)    |
| `JWT_PUBLIC_KEY`                     | RSA public key, Base64-encoded PEM                       |
| `JWT_ISSUER`                         | Expected issuer, default `lifeos-auth`                   |
| `RABBITMQ_USER`, `RABBITMQ_PASSWORD` | RabbitMQ login (defaults `lifeos` / `lifeos_dev`)        |
| `CORS_ALLOWED_ORIGINS`               | Allowed browser origins, default `http://localhost:5173` |

"Today" is taken from the JVM's time zone. Docker Compose sets `TZ=Europe/Oslo`.

## Database

PostgreSQL, schema managed by Flyway (`src/main/resources/db/migration`). Hibernate only validates the schema (`ddl-auto: validate`).

| Table             | Content                                                                            |
| ----------------- | ---------------------------------------------------------------------------------- |
| `progress_record` | Copy of each check-in. `event_id` and `progress_id` are unique.                    |
| `day_log`         | One row per user and day (unique on `user_id` + `date`) with mood and sleep hours. |

## Run

From the repo root, with the whole system:

```bash
docker compose up -d --build report-service
```

Locally (needs Postgres and RabbitMQ from Compose, and a `.env`):

```bash
cd report-service
./mvnw spring-boot:run
```

Tests (Testcontainers needs Docker running):

```bash
./mvnw verify
```

## Project structure

```
report-service/
  pom.xml
  Dockerfile
  src/main/java/no/kristiania/reportservice/
    ReportServiceApplication
    config/        SecurityConfig, JsonAuthenticationEntryPoint, RabbitConfig, EtagConfig
    exceptions/    GlobalExceptionHandler, ApiError, ApiException + InvalidDayLogException
    report/
      controller/  ReportController
      service/     ReportService
      listener/    ProgressEventListener
      repository/  ProgressRecordRepository
      entity/      ProgressRecord
      dto/         GoalReport, ActivityReport, ActivityDay, PeriodCount, Suggestion
      enums/       TrackingModel, Recommendation
      events/      ProgressCreatedEvent, ProgressDeletedEvent
      utils/       PeriodStats, ActivityStats
    daylog/
      controller/  DayLogController
      service/     DayLogService
      repository/  DayLogRepository
      entity/      DayLog
      dto/         DayLogRequest, DayLogView, Wellbeing
      utils/       WellbeingStats
  src/main/resources/
    application.yml
    db/migration/  Flyway migrations
```

## Logging

Logs go to stdout as JSON in ECS format, set for all services in the shared Consul config. Each line has `log.level`, `service.name` and, inside a request, `traceId` and `spanId`. With two instances, the same message can come from either one.

| Where                    | Level                | What                                                                                           |
| ------------------------ | -------------------- | ---------------------------------------------------------------------------------------------- |
| `GlobalExceptionHandler` | WARN / ERROR / DEBUG | Rejected requests (status + message), unexpected errors with stack trace, bad parameter values |
| `ProgressEventListener`  | INFO                 | Progress record saved, duplicate event skipped, delete event handled, with ids                 |
| `DayLogService`          | INFO                 | Day log saved (`userId` only, never the mood or sleep values)                                  |

When all retries for a message are used up, Spring AMQP logs a WARN with the message properties. The reports themselves are read-only and not logged.

Never logged: tokens, request bodies, mood and sleep values. Users are identified by `userId`.

To see DEBUG lines from our own code, set `logging.level.no.kristiania.reportservice=DEBUG`.
