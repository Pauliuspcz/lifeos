# goal-service

Goal management service for **LifeOS**, a habit and goal tracker built as part of the PG3402 Microservices coursework.

`goal-service` stores each user's goals (for example "run 3 times a week"). It never handles passwords or logins: it only verifies the JWT access token issued by `user-service` and uses the user ID from the token to decide whose goals a request may touch.

## What this service does

- Creates, lists, reads, updates, archives and deletes goals.
- Scopes every operation to the authenticated user. A user can never see or change another user's goals.
- Owns its own PostgreSQL database (`goal_db`), managed with Flyway migrations.

### Design decisions

| Decision                                                          | Why                                                                                                        |
| ----------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| Verify RS256 tokens with the public key only                      | `goal-service` can check tokens but never forge them. Only `user-service` holds the private key.           |
| User ID comes from the token (`sub`), never from the request body | A client cannot create or access goals on behalf of someone else. A `userId` in a request body is ignored. |
| Another user's goal gives `404`, not `403`                        | The API does not reveal that a goal with that ID exists.                                                   |
| Request and response DTOs, entity never exposed                   | The API contract is independent of the database model. `userId` is not part of any response.               |
| `PUT` replaces all editable fields                                | The client sends the complete goal. Omitting `description` clears it.                                      |
| Archiving is idempotent and keeps the first `archivedAt`          | Repeating the call is safe and does not rewrite history.                                                   |
| Optimistic locking (`@Version`)                                   | Concurrent modifications give `409` instead of silently overwriting each other.                            |
| Stateless sessions, CSRF disabled                                 | Authentication uses the `Authorization: Bearer` header only, no cookies.                                   |
| Flyway migrations + `ddl-auto: validate`                          | The schema is versioned in SQL; Hibernate only checks that entities match it.                              |

## Project structure

```
src/main/java/no/kristiania/goalservice
├── GoalServiceApplication
├── config       SecurityConfig, JsonAuthenticationEntryPoint
├── exception    ApiException, ApiError, GlobalExceptionHandler
└── goal
    ├── Goal (entity), GoalRepo, GoalService, GoalController
    ├── dto          CreateGoalRequest, UpdateGoalRequest, GoalResponse
    ├── enums        GoalPeriod, GoalStatus
    └── exceptions   GoalNotFoundException
src/main/resources
├── application.yaml
└── db/migration     V001 (goals table)
src/test/java/no/kristiania/goalservice
├── GoalServiceApplicationTests
├── goal/GoalServiceTest (unit)
└── integration
    ├── AbstractIntegrationTest, AbstractGoalApiTest, TestcontainersConfiguration
    ├── SecurityTest, GoalApiTest, GoalOwnershipTest
src/test/resources
└── application-test.yaml
```

## API reference

Base URL when running locally: `http://localhost:8081`

Every endpoint requires `Authorization: Bearer <accessToken>`. A missing or invalid token gives `401`.

| Method | Path                     | Purpose                                                                      | Success                                   |
| ------ | ------------------------ | ---------------------------------------------------------------------------- | ----------------------------------------- |
| POST   | `/api/goal`              | Create a goal                                                                | `201` + `GoalResponse`, `Location` header |
| GET    | `/api/goal`              | List the user's goals, newest first. Optional `?status=ACTIVE` or `ARCHIVED` | `200` + list                              |
| GET    | `/api/goal/{id}`         | Get one goal                                                                 | `200` + `GoalResponse`                    |
| PUT    | `/api/goal/{id}`         | Replace the editable fields                                                  | `200` + `GoalResponse`                    |
| PATCH  | `/api/goal/{id}/archive` | Archive a goal                                                               | `200` + `GoalResponse`                    |
| DELETE | `/api/goal/{id}`         | Delete a goal                                                                | `204`                                     |

### POST /api/goal and PUT /api/goal/{id}

Request (the same shape for both):

```json
{
  "title": "Run",
  "description": "Morning runs",
  "period": "WEEKLY",
  "timesPerPeriod": 3,
  "amountPerSession": 5.0,
  "unit": "km"
}
```

Rules: `title` required, not blank, max 255. `description` max 255. `period` required (`GoalPeriod`). `timesPerPeriod` required, at least 1. `amountPerSession` positive, at most 8 integer and 2 decimal digits. `unit` max 50.

### GoalResponse

```json
{
  "id": 1,
  "title": "Run",
  "description": "Morning runs",
  "period": "WEEKLY",
  "timesPerPeriod": 3,
  "amountPerSession": 5.0,
  "unit": "km",
  "status": "ACTIVE",
  "templateId": null,
  "version": 0,
  "createdAt": "2026-10-19T10:00:00Z",
  "updatedAt": "2026-10-19T10:00:00Z",
  "archivedAt": null
}
```

## Error handling

All errors are returned as JSON in the same format as `user-service`:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Goal with id 42 not found",
  "timestamp": "2026-10-19T10:00:00.000000000Z"
}
```

Validation errors (`400`) also include a `fieldErrors` object with one entry per invalid field.

| Status | When                                                                                                                                                  |
| ------ | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| `400`  | Validation failed, the body is missing or malformed (including an unknown `period`), or a parameter has an invalid value (for example `?status=NOPE`) |
| `401`  | Missing, invalid, expired or wrongly signed access token, or a token whose `sub` is not a user ID                                                     |
| `404`  | The goal does not exist or belongs to another user                                                                                                    |
| `409`  | The goal was modified by another request at the same time                                                                                             |

## Authentication

`goal-service` is an OAuth2 resource server. It verifies the signature (RS256) and expiry of the token with the public key, and uses `sub` as the user ID. It never receives the private key.

## Database

Managed by Flyway (`src/main/resources/db/migration`). Never edit a migration that has already been applied; add a new `V00N__...` file instead.

**goals**: `id` (identity), `user_id` (required, never updated), `title`, `description`, `period`, `times_per_period`, `amount_per_session`, `unit`, `status` (default `ACTIVE`), `template_id`, `version`, `created_at`, `updated_at`, `archived_at`. Column types are defined in `V001`.

## Configuration

| Property              | Environment variable                                                 | Description                                                                                     |
| --------------------- | -------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------- |
| `app.jwt.public-key`  | `JWT_PUBLIC_KEY`                                                     | Base64 of the public PEM file (single line). Must match the private key used by `user-service`. |
| `spring.datasource.*` | `POSTGRES_USER`, `POSTGRES_PASSWORD`, `GOAL_DB_NAME`, `GOAL_DB_PORT` | Database connection. Docker Compose overrides the local defaults.                               |

The `.env` file lives in the repository root, is listed in `.gitignore`, and is shared with the other services. See the `user-service` README for how to generate the key pair.

## Running the service

### With Docker Compose (from the repository root)

```bash
docker compose up --build goal-service
```

This starts `goal-db` and the service on port 8081. Stop any copy started from IntelliJ first, since both use port 8081.

### Locally from IntelliJ

1. Start the database: `docker compose up -d goal-db`.
2. Run `GoalServiceApplication` with the repository root as working directory, so `.env` is found.

## Testing

```bash
./mvnw verify                        # all tests
./mvnw test -Dtest=GoalServiceTest   # one class
```

**Docker must be running for the integration tests**, because they start PostgreSQL through Testcontainers. The unit tests need neither Spring nor Docker.

- **Integration tests** run the full application against a real PostgreSQL 15 container. All classes extend `AbstractIntegrationTest`, which shares one container and Spring context, empties the `goals` table before each test, and generates an RSA key pair so tests can sign their own tokens.
- The test profile needs no `.env` file.

| Test class          | Covers                                                                                                                                                           |
| ------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `SecurityTest`      | No token, garbage token, expired token, wrong key, unsigned token and non-numeric `sub` all give `401` with a JSON body; a valid token is accepted               |
| `GoalApiTest`       | Every endpoint: create, validation, unreadable bodies, list and status filter, get, update and version bump, archive, delete                                     |
| `GoalOwnershipTest` | Another user gets `404` on read, update, archive and delete, and the goal is unchanged afterwards; lists contain only own goals; `userId` in the body is ignored |
| `GoalServiceTest`   | Unit: owner comes from the argument, query selection, not-found, update, idempotent archive, delete                                                              |

### Continuous integration

`.github/workflows/goal-service-tests.yml` runs `./mvnw -B verify` on every pull request and on pushes to `main`. GitHub-hosted Ubuntu runners have Docker installed, so Testcontainers needs no extra setup.

## Known limitations and next steps

- Add CORS configuration before the frontend calls the service directly.
- Deleting a goal does not yet notify `progress-service`.
- No pagination on `GET /api/goal`.
- Archived goals can still be edited.
- Optimistic locking only protects concurrent transactions. The client does not send `version` back for conflict detection.
