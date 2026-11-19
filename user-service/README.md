# user-service

Auth and user service for LifeOS. It registers users, logs them in (email/password or Google), issues JWT access tokens and rotating refresh tokens, and serves the user's profile.

- Port: `8080` (inside Docker; reached through the gateway on `8100`)
- Stack: Java 21, Spring Boot 4.1, Spring Security, Spring Data JPA, PostgreSQL, Flyway
- Registers itself in Consul and reads shared config from Consul

## Endpoints

Through the gateway the routes are `/auth/**` and `/profile/**`.

| Method | Path             | Auth         | Purpose                                                        |
| ------ | ---------------- | ------------ | -------------------------------------------------------------- |
| POST   | `/auth/register` | public       | Create a user (email, password 8-72 chars, name). Returns 201. |
| POST   | `/auth/login`    | public       | Email + password. Returns access + refresh token.              |
| POST   | `/auth/refresh`  | public       | Swap a refresh token for a new access + refresh token.         |
| POST   | `/auth/google`   | public       | Log in with a Google ID token. Creates or links the user.      |
| POST   | `/auth/logout`   | public       | Revoke a refresh token. Returns 204.                           |
| GET    | `/profile/me`    | Bearer token | Read own profile.                                              |
| PATCH  | `/profile/me`    | Bearer token | Update `name` and/or `profilePictureUrl`.                      |

Login, refresh and Google return `accessToken`, `refreshToken`, `tokenType` (`Bearer`) and `expiresInSeconds`.

### Errors

All errors are JSON: `status`, `error`, `message`, `timestamp`, plus `fieldErrors` for validation problems.

| Status | When                                                                                           |
| ------ | ---------------------------------------------------------------------------------------------- |
| 400    | Validation failed or malformed body                                                            |
| 401    | Invalid credentials, invalid refresh token, invalid Google token, missing/invalid access token |
| 404    | User not found                                                                                 |
| 409    | Email already registered, Google account linked to another user, unique value already exists   |
| 500    | Unexpected error (details only in the logs)                                                    |

## How auth works

- **Access token**: JWT signed with the private RSA key (`JWT_PRIVATE_KEY`), valid 60 minutes. `sub` is the user id. Other services only need the public key (`JWT_PUBLIC_KEY`) to validate it.
- **Refresh token**: random 32 bytes, stored only as a SHA-256 hash, valid 30 days.
- **Rotation**: every refresh revokes the old token and issues a new one.
- **Reuse detection**: using an already revoked token revokes all of that user's refresh tokens, unless it was rotated within the grace period (`app.jwt.refresh-reuse-grace-seconds`, default 10 s).
- **Google**: the ID token is verified against Google's public keys (issuer, audience = `GOOGLE_CLIENT_ID`, `email_verified`). An existing user with the same email is linked.

## Configuration

Set in `.env` at the repo root (read by Docker Compose) or as environment variables.

| Variable                             | Used for                            |
| ------------------------------------ | ----------------------------------- |
| `POSTGRES_USER`, `POSTGRES_PASSWORD` | Database login                      |
| `USER_DB_NAME`, `USER_DB_PORT`       | Database name and port              |
| `JWT_PRIVATE_KEY`, `JWT_PUBLIC_KEY`  | RSA key pair, Base64-encoded PEM    |
| `JWT_ISSUER`                         | Token issuer, default `lifeos-auth` |
| `GOOGLE_CLIENT_ID`                   | Audience check for Google ID tokens |

Allowed CORS origins are set with `app.cors.allowed-origins`. Never commit `.env` or the private key.

## Database

PostgreSQL, schema managed by Flyway (`src/main/resources/db/migration`). Hibernate only validates the schema (`ddl-auto: validate`). Tables: `users`, `refresh_tokens`. A demo user (`demo@lifeos.no`) is seeded by a migration for development.

## Run

From the repo root, with the whole system:

```bash
docker compose up -d --build user-service
```

Locally (needs Postgres and a `.env`):

```bash
cd user-service
./mvnw spring-boot:run
```

Tests (Testcontainers needs Docker running):

```bash
./mvnw verify
```

## Project structure

```
user-service/
  pom.xml
  Dockerfile
  src/main/java/no/kristiania/lifeos/
    auth/
      controller/   AuthController
      service/      AuthService, TokenIssuer
      token/        JwtService, RefreshTokenService, RefreshToken, RefreshTokenRepository
      google/       GoogleAuthService, GoogleTokenVerifier, GoogleUser
      security/     SecurityConfig, JsonAuthenticationEntryPoint
      dto/          request/response records
    profile/        controller, service, dto
    user/           User, UserRepository, Emails
    exception/      GlobalExceptionHandler, ErrorResponse, ApiException + subclasses
  src/main/resources/
    application.yml
    db/migration/   Flyway migrations
```

## Logging

Logs go to stdout as JSON in ECS format. This is set for all services in the shared Consul config (`config/application/data`: `logging.structured.format.console: ecs`). Each line carries `log.level`, `service.name` and, inside a request, `traceId` and `spanId`, so one request can be followed across services. Tracing is sampled at 100%, but OTLP export is off, so trace ids exist in the logs only (no trace backend).

The log stack (Elasticsearch/Kibana) is described in `observability/README.md`.

| Where                                      | Level                | What                                                                                                             |
| ------------------------------------------ | -------------------- | ---------------------------------------------------------------------------------------------------------------- |
| `GlobalExceptionHandler`                   | WARN / ERROR / DEBUG | Rejected requests (status + message), unexpected errors with stack trace, validation problems (field names only) |
| `AuthService`                              | INFO                 | Register, login, token refresh, with `userId`                                                                    |
| `RefreshTokenService`                      | WARN / INFO          | Unknown token, reuse detected (all tokens revoked), expired token, logout                                        |
| `GoogleAuthService`, `GoogleTokenVerifier` | INFO / WARN          | Google login, account linking, rejected Google tokens                                                            |
| `ProfileService`                           | INFO                 | Profile updated                                                                                                  |

Never logged: passwords, tokens, email addresses, request bodies. Users are identified by `userId`.

Default level is INFO. To see DEBUG lines, set `logging.level.no.kristiania.lifeos=DEBUG`.
