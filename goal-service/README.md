# goal-service

Goal service for LifeOS. It owns the template catalog (areas, focuses, goal templates) and the users' goals. It lets users create, edit, archive, restore and delete goals, and it publishes an event when an outdoor goal changes.

- Port: `8081` (Docker and local; reached through the gateway on `8100`)
- Stack: Java 21, Spring Boot 4.1, Spring Security (resource server), Spring Data JPA, PostgreSQL, Flyway, RabbitMQ
- Registers itself in Consul and reads shared config from Consul
- Database: `goal_db` (host port `5433`)

## Endpoints

Through the gateway the routes are `/api/goal/**` and `/api/catalog/**`.

### Catalog (public, read only)

| Method | Path                                      | Purpose                                                   |
| ------ | ----------------------------------------- | --------------------------------------------------------- |
| GET    | `/api/catalog/categories`                 | The areas (Body, Mind, Growth, Life) with icon and color. |
| GET    | `/api/catalog/categories/{id}/directions` | Focuses in one area.                                      |
| GET    | `/api/catalog/categories/{id}/templates`  | Active templates in one area.                             |
| GET    | `/api/catalog/directions`                 | All focuses.                                              |
| GET    | `/api/catalog/directions/{id}/templates`  | Active templates for one focus.                           |

### Goals (Bearer token required)

A user only ever sees their own goals. Another user's goal answers `404`.

| Method | Path                       | Purpose                                                                            |
| ------ | -------------------------- | ---------------------------------------------------------------------------------- |
| POST   | `/api/goal`                | Create a goal. Returns 201 with a `Location` header.                               |
| GET    | `/api/goal`                | List own goals, newest first. Optional `?status=ACTIVE` or `ARCHIVED`.             |
| GET    | `/api/goal/{id}`           | Read one goal.                                                                     |
| PUT    | `/api/goal/{id}`           | Update a goal (not archived ones). Send `version` to guard against parallel edits. |
| PATCH  | `/api/goal/{id}/archive`   | Archive a goal.                                                                    |
| PATCH  | `/api/goal/{id}/unarchive` | Restore an archived goal.                                                          |
| DELETE | `/api/goal/{id}`           | Delete a goal. Only archived goals can be deleted. Returns 204.                    |

Goal responses include `templateCode` and `directionCode` for goals created from a template (the frontend uses them for relation hints). They are `null` for custom goals.

### Goal rules

- `trackingType` is `CHECK` (default) or `AMOUNT`.
- **CHECK**: `period` defaults to `DAILY` and `timesPerPeriod` to `1`.
- **AMOUNT**: needs `targetAmount` and `unit`, and either a `period` or a `targetDate`. The period cannot be `DAILY`. `timesPerPeriod` is ignored.
- `categoryId` is required. If `templateId` is given, the template must exist and belong to the same category.
- `startDate` defaults to today. `targetDate` must be in the future when set or changed, and not before `startDate`.
- The same rules are repeated as check constraints in the database.

### Errors

All errors are JSON: `status`, `error`, `message`, `timestamp`, plus `fieldErrors` for validation problems.

| Status    | When                                                                                                                                                   |
| --------- | ------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 400       | Validation failed, malformed body, invalid goal (for example unknown category, AMOUNT without unit), bad parameter value, database constraint violated |
| 401       | Missing or invalid access token                                                                                                                        |
| 404       | Goal or catalog item not found (also for other users' goals)                                                                                           |
| 405 / 415 | Wrong HTTP method or content type                                                                                                                      |
| 409       | Goal is archived (cannot be updated), goal is not archived (cannot be deleted), goal was changed by another request                                    |
| 500       | Unexpected error (details only in the logs)                                                                                                            |

## Events

goal-service publishes `goal.outdoor.changed` to the topic exchange `lifeos.goal`. Payload: `eventId`, `goalId`, `userId`, `outdoor`, `occurredAt`.

It is sent when an outdoor goal is created, when the outdoor flag changes, and when an outdoor goal is archived, restored or deleted. The event goes out only after the database transaction has committed, so a rolled-back change never produces an event. recommendation-service consumes it.

There is no outbox: if RabbitMQ is unreachable, the failure is logged as an error and the event is lost.

## Used by

- **progress-service** calls `GET /api/goal/{id}` directly (not through the gateway) and forwards the user's token, to check that a goal exists, belongs to the user and to read its period and target.
- **recommendation-service** consumes the outdoor event.
- **frontend** uses the catalog for the goal wizard and the goal endpoints for the goal list.

## Security

The service validates the JWT itself with the public key (`JWT_PUBLIC_KEY`) and checks the issuer. It never creates tokens. The user id is the token's `sub` claim. `GET /api/catalog/**` and `/actuator/health` are public; everything else needs a valid token.

## Configuration

Set in `.env` at the repo root (read by Docker Compose) or as environment variables.

| Variable                             | Used for                                                 |
| ------------------------------------ | -------------------------------------------------------- |
| `POSTGRES_USER`, `POSTGRES_PASSWORD` | Database login                                           |
| `GOAL_DB_NAME`, `GOAL_DB_PORT`       | Database name and port (defaults `goal_db`, `5433`)      |
| `JWT_PUBLIC_KEY`                     | RSA public key, Base64-encoded PEM                       |
| `JWT_ISSUER`                         | Expected issuer, default `lifeos-auth`                   |
| `RABBITMQ_USER`, `RABBITMQ_PASSWORD` | RabbitMQ login (defaults `lifeos` / `lifeos_dev`)        |
| `CORS_ALLOWED_ORIGINS`               | Allowed browser origins, default `http://localhost:5173` |

## Database

PostgreSQL, schema managed by Flyway (`src/main/resources/db/migration`). Hibernate only validates the schema (`ddl-auto: validate`).

Tables: `categories`, `directions`, `goal_templates`, `goals`. The catalog tables are read only in the service (`@Immutable`). The seed adds 4 areas, 12 focuses and 34 templates.

Goals use optimistic locking (`version`), so two parallel edits cannot silently overwrite each other.

## Run

From the repo root, with the whole system:

```bash
docker compose up -d --build goal-service
```

Locally (needs Postgres and RabbitMQ from Compose, and a `.env`):

```bash
cd goal-service
./mvnw spring-boot:run
```

Tests (Testcontainers needs Docker running):

```bash
./mvnw verify
```

## Project structure

```
no/kristiania/goalservice/
  GoalServiceApplication
  config/        SecurityConfig, JsonAuthenticationEntryPoint, RabbitConfig
  exception/     GlobalExceptionHandler, ApiError, ApiException + alle exceptions
  shared/
    enums/       TrackingType, GoalPeriod
  catalog/
    controller/  CatalogController
    service/     CatalogService
    repository/  CategoryRepository, DirectionRepository,
                 GoalTemplateRepository, TemplateCodeRepository
    entity/      Category, Direction, GoalTemplate, Difficulty
    dto/         CategoryResponse, DirectionResponse, TemplateResponse
  goal/
    controller/  GoalController
    service/     GoalService, GoalResponseMapper
    repository/  GoalRepository
    entity/      Goal, GoalStatus
    dto/         CreateGoalRequest, UpdateGoalRequest, GoalResponse
    events/      GoalEventPublisher, GoalOutdoorChangedEvent
```

## Logging

Logs go to stdout as JSON in ECS format, set for all services in the shared Consul config. Each line has `log.level`, `service.name` and, inside a request, `traceId` and `spanId`.
