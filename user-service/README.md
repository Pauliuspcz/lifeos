# user-service

Authentication and user-profile service for **LifeOS**, a habit and goal tracker built as part of the PG3402 Microservices coursework.

`user-service` is the only service that creates accounts and issues tokens. Other services (Goal, Progress, Report) never see passwords or Google tokens; they only need to verify the JWT access token this service signs.

## Recap: what this service does

- Registers users with email and password (BCrypt-hashed).
- Logs users in with email/password or with a Google account (Google ID token).
- Issues a short-lived **access token** (JWT, RS256) and a long-lived, rotating **refresh token**.
- Supports logout by revoking the refresh token.
- Lets a logged-in user read and update their own profile.
- Owns its own PostgreSQL database (`user_db`), managed with Flyway migrations.

### Design decisions

| Decision                                                    | Why                                                                                                                                                          |
| ----------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| JWT signed with RS256 (key pair) instead of a shared secret | Only `user-service` holds the private key. Other services get the public key and can verify tokens but cannot forge them.                                    |
| Opaque refresh tokens, stored only as SHA-256 hashes        | A database leak does not expose usable refresh tokens.                                                                                                       |
| Refresh token rotation with reuse detection                 | Each refresh token works once. If a token that was rotated a while ago is presented again, all of that user's tokens are revoked.                            |
| Short grace period for reuse (10 seconds)                   | Two parallel refresh calls from a well-meaning client must not destroy every session. A token rotated just now is rejected, but the other tokens stay valid. |
| Pessimistic lock when consuming a refresh token             | Two simultaneous refresh calls with the same token cannot both succeed.                                                                                      |
| Refresh runs in one transaction                             | The old token is consumed and the new pair is issued together. If issuing fails, nothing is committed and the client can retry with the old token.           |
| Google login via ID-token verification                      | The backend verifies the token against Google's public keys. No client secret is needed.                                                                     |
| Stateless sessions, CSRF disabled                           | Authentication uses the `Authorization: Bearer` header only, no cookies.                                                                                     |
| Flyway migrations + `ddl-auto: validate`                    | The schema is versioned in SQL; Hibernate only checks that entities match it.                                                                                |

## Project structure

```
src/main/java/no/kristiania/lifeos
├── UserServiceApplication
├── auth
│   ├── controller   AuthController
│   ├── service      AuthService, TokenIssuer
│   ├── google       GoogleAuthService, GoogleTokenVerifier, GoogleUser
│   ├── token        JwtService, RefreshToken, RefreshTokenRepository, RefreshTokenService
│   ├── security     SecurityConfig, JwtAuthFilter
│   └── dto          request/response records
├── profile
│   ├── controller   ProfileController
│   ├── service      ProfileService
│   └── dto          ProfileResponse, UpdateProfileRequest
├── user             User (entity), UserRepository, Emails
└── exception        ApiException and one small exception per error case
src/main/resources
├── application.yaml
└── db/migration     V001__create_user_structure.sql, V002__create_refresh_tokens.sql,
                     V003__add_revoked_at.sql
src/test/java/no/kristiania/lifeos
├── UserServiceApplicationTests
├── auth
│   ├── security     JwtAuthFilterTest (unit)
│   ├── service      AuthServiceTest (unit)
│   └── token        JwtServiceTest, RefreshTokenServiceTest (unit)
├── profile/service  ProfileServiceTest (unit)
└── integration
    ├── AbstractIntegrationTest, TestcontainersConfiguration
    ├── AuthRegisterTest, AuthLoginTest, AuthRefreshLogoutTest, AuthGoogleTest
    └── ProfileTest
src/test/resources
└── application-test.yaml
```

## API reference

Base URL when running locally: `http://localhost:8080`

| Method | Path             | Auth                       | Purpose                                       | Success                    |
| ------ | ---------------- | -------------------------- | --------------------------------------------- | -------------------------- |
| POST   | `/auth/register` | No                         | Create an account with email and password     | `201` + `RegisterResponse` |
| POST   | `/auth/login`    | No                         | Log in with email and password                | `200` + `AuthResponse`     |
| POST   | `/auth/google`   | No                         | Log in or sign up with a Google ID token      | `200` + `AuthResponse`     |
| POST   | `/auth/refresh`  | No (refresh token in body) | Exchange a refresh token for a new token pair | `200` + `AuthResponse`     |
| POST   | `/auth/logout`   | No (refresh token in body) | Revoke the given refresh token                | `204`                      |
| GET    | `/profile/me`    | Bearer token               | Get the logged-in user's profile              | `200` + `ProfileResponse`  |
| PATCH  | `/profile/me`    | Bearer token               | Update name and/or profile picture            | `200` + `ProfileResponse`  |

All `/auth/**` endpoints are public. Every other endpoint requires `Authorization: Bearer <accessToken>`; a missing or invalid token gives `401`.

### POST /auth/register

Request:

```json
{ "email": "kari@example.com", "password": "at-least-8-chars", "name": "Kari" }
```

Rules: `email` must be a valid address, `password` 8 to 72 characters, `name` not blank. The email is trimmed and lower-cased before it is stored.

Response `201`:

```json
{ "id": 1, "email": "kari@example.com", "name": "Kari" }
```

Errors: `400` validation failed, `409` email already registered. Registering does not log the user in; call `/auth/login` afterwards.

### POST /auth/login

Request: `{ "email": "...", "password": "..." }`

Response `200` (`AuthResponse`, shared by login, Google login and refresh):

```json
{
  "accessToken": "<jwt>",
  "refreshToken": "<opaque string>",
  "tokenType": "Bearer",
  "expiresInSeconds": 3600
}
```

Errors: `400` validation failed, `401` invalid credentials. Accounts created only through Google have no password and cannot log in here. A wrong password and an unknown email give the same response, so the API does not reveal which emails are registered.

### POST /auth/google

Request: `{ "idToken": "<Google ID token from the frontend>" }`

Behaviour:

1. The token's signature, expiry, issuer and audience (`app.google.client-id`) are verified, and `email_verified` must be `true`.
2. If a user with this Google ID exists, they are logged in.
3. Otherwise, if a user with the same email exists, the Google account is linked to it.
4. Otherwise a new user is created from the Google profile (name, email, picture).

Response `200`: `AuthResponse`. Errors: `401` invalid Google token, `409` the email belongs to an account already linked to a different Google user.

### POST /auth/refresh

Request: `{ "refreshToken": "<token from the last login or refresh>" }`

Response `200`: a new `AuthResponse`. The old refresh token is revoked immediately (rotation), so the client must replace both tokens.

Error `401` when the token is unknown, expired or revoked. What happens to the user's other tokens depends on when the revoked token is presented again:

- **Within the grace period** (10 seconds after it was rotated): the call gets `401`, but the other tokens stay valid. This covers the case where a client accidentally sends two refresh calls at once. One of them succeeds, and the session survives.
- **After the grace period**, or after the token was revoked by logout: the call gets `401` and **all refresh tokens for that user are revoked**, which forces a new login on every device.

Concurrent calls with the same token are serialised by a database lock, so exactly one of them succeeds.

### POST /auth/logout

Request: `{ "refreshToken": "..." }`. Response `204` with no body. The call is idempotent: an unknown token also gives `204`. The access token stays valid until it expires (at most 60 minutes), which is normal for stateless JWTs.

### GET /profile/me

Response `200`:

```json
{
  "id": 1,
  "email": "kari@example.com",
  "name": "Kari",
  "profilePictureUrl": null
}
```

Errors: `401` no or invalid token, `404` the user no longer exists.

### PATCH /profile/me

Partial update. Fields that are omitted (or `null`) stay unchanged.

```json
{ "name": "Kari Nordmann", "profilePictureUrl": "https://example.com/me.png" }
```

Rules: `name` must not be blank, max 255 characters. `profilePictureUrl` max 500 characters; an empty string removes the picture. Response `200`: updated `ProfileResponse`. Errors: `400`, `401`, `404`.

## Authentication details

### Access token

- Algorithm: RS256, signed with the private key.
- Lifetime: 60 minutes (`app.jwt.access-expiration-minutes`).
- Claims: `sub` = user ID, `email`, `iat`, `exp`.

### Refresh token

- 32 random bytes, Base64-URL encoded; the database stores only the SHA-256 hash.
- Lifetime: 30 days (`app.jwt.refresh-expiration-days`).
- Single use: every refresh returns a new token and revokes the old one. The time of the rotation is stored in `revoked_at`.
- Reuse of a rotated token is treated as a possible theft once the grace period (`app.jwt.refresh-reuse-grace-seconds`, default 10) has passed.

## Error handling

All errors are handled in one place (`GlobalExceptionHandler`) and returned as JSON in the same format:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Email already registered",
  "timestamp": "2026-10-14T17:00:41.977705286Z"
}
```

Validation errors (`400`) also include a `fieldErrors` object with one entry per invalid field:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "timestamp": "2026-10-14T17:00:41.977705286Z",
  "fieldErrors": {
    "email": "must not be blank",
    "password": "must not be blank",
    "name": "must not be blank"
  }
}
```

| Status | When                                                                                                                                |
| ------ | ----------------------------------------------------------------------------------------------------------------------------------- |
| `400`  | Validation failed (`fieldErrors` lists each field and its problem) or the request body is missing or malformed                      |
| `401`  | Wrong credentials, invalid or expired refresh token, invalid Google token, or a missing or invalid access token on a protected path |
| `404`  | The user no longer exists                                                                                                           |
| `409`  | Email already registered, Google account already linked to another user, or a duplicate value in the database                       |

Each case has its own small exception in the `exception` package (for example `InvalidCredentialsException` and `EmailAlreadyRegisteredException`). All of them extend `ApiException`, which carries the HTTP status, so a new error case only needs a new class and no extra handler code.

A refresh token can only be used once. Presenting an already used token returns `401`. If the token was rotated more than the grace period ago, all refresh tokens for that user are revoked as well.

### For other services

A service that needs to authenticate users only needs the **public key**. It verifies the RS256 signature and expiry of the access token and reads the user ID from `sub`. It should never receive the private key.

### Frontend notes

- Send only one refresh request at a time. The server survives accidental parallel refreshes (one succeeds, the others get `401`, and the session stays valid), but a client that clears its tokens on that `401` would lose the new tokens it just received.
- Replace both tokens after each refresh, and call `/auth/logout` before clearing local storage on logout.
- Only clear the stored tokens on a `401` from `/auth/refresh`, not on network or server errors.

## Database

Managed by Flyway (`src/main/resources/db/migration`). Never edit a migration that has already been applied; add a new `V00N__...` file instead.

**users**

| Column              | Type            | Notes                                                       |
| ------------------- | --------------- | ----------------------------------------------------------- |
| id                  | BIGINT identity | primary key                                                 |
| email               | VARCHAR(255)    | required, unique (`uk_users_email`)                         |
| password_hash       | VARCHAR(60)     | BCrypt hash; null for Google-only users                     |
| google_id           | VARCHAR(255)    | unique (`uk_users_google_id`); null for password-only users |
| name                | VARCHAR(255)    | required                                                    |
| profile_picture_url | VARCHAR(500)    | optional                                                    |
| created_at          | TIMESTAMP       | set on insert                                               |

**refresh_tokens**

| Column     | Type            | Notes                                                                                  |
| ---------- | --------------- | -------------------------------------------------------------------------------------- |
| id         | BIGINT identity | primary key                                                                            |
| user_id    | BIGINT          | references `users(id)`, cascades on delete, indexed                                    |
| token_hash | VARCHAR(64)     | SHA-256 hex, unique                                                                    |
| expires_at | TIMESTAMP       | required                                                                               |
| revoked    | BOOLEAN         | default false                                                                          |
| revoked_at | TIMESTAMP       | set when the token is rotated (starts the grace period); null otherwise and for logout |
| created_at | TIMESTAMP       | required                                                                               |

## Configuration

| Property                                      | Environment variable                                                                | Description                                                                                                |
| --------------------------------------------- | ----------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| `app.jwt.private-key`                         | `JWT_PRIVATE_KEY`                                                                   | Base64 of the private PEM file (single line). Used for signing.                                            |
| `app.jwt.public-key`                          | `JWT_PUBLIC_KEY`                                                                    | Base64 of the public PEM file (single line). Used for verification.                                        |
| `app.jwt.access-expiration-minutes`           |                                                                                     | Default 60                                                                                                 |
| `app.jwt.refresh-expiration-days`             |                                                                                     | Default 30                                                                                                 |
| `app.jwt.refresh-reuse-grace-seconds`         |                                                                                     | Default 10. How long after a rotation a reused token is rejected without revoking the user's other tokens. |
| `app.google.client-id`                        |                                                                                     | Google OAuth client ID. It is public, not a secret. The client secret is not used.                         |
| `spring.datasource.url / username / password` | `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | Database connection. Docker Compose overrides the local defaults.                                          |

### Generating the key pair

Run once (for example in Git Bash), then keep the private key out of git:

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem
openssl rsa -in private.pem -pubout -out public.pem
base64 -w0 private.pem   # value for JWT_PRIVATE_KEY
base64 -w0 public.pem    # value for JWT_PUBLIC_KEY
```

Use separate key pairs for development and production. If the private key is replaced, all existing access tokens become invalid and clients must refresh or log in again.

### The .env file

`.env` lives next to `docker-compose.yml` in the repository root and is listed in `.gitignore`, so it is never committed. Copy `.env.example` to `.env` and fill in the values.

| Variable                        | Used by                   | Description                                                                                                                                                     |
| ------------------------------- | ------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `POSTGRES_USER`                 | Compose (all databases)   | Database username                                                                                                                                               |
| `POSTGRES_PASSWORD`             | Compose (all databases)   | Database password                                                                                                                                               |
| `USER_DB_NAME`                  | `user-db`, `user-service` | Name of the user database                                                                                                                                       |
| `USER_DB_PORT`                  | `user-db`                 | Port on your machine that maps to the database (host side only)                                                                                                 |
| `GOAL_DB_NAME` / `GOAL_DB_PORT` | `goal-db`                 | Same for the goal service. The port must differ from `USER_DB_PORT`                                                                                             |
| `JWT_PRIVATE_KEY`               | `user-service`            | Base64 of the private PEM, one line, no quotes                                                                                                                  |
| `JWT_PUBLIC_KEY`                | `user-service`            | Base64 of the public PEM, one line, no quotes                                                                                                                   |
| `GOOGLE_CLIENT_ID`              | `user-service`            | Google OAuth client ID. It is public, not a secret. The backend uses it to check that a Google ID token was issued for this app. The client secret is not used. |

Example (`.env.example`, replace every value with your own):

```
POSTGRES_USER=postgres
POSTGRES_PASSWORD=change-me
USER_DB_NAME=user_db
USER_DB_PORT=5432
GOAL_DB_NAME=goal_db
GOAL_DB_PORT=5433
JWT_PRIVATE_KEY=<base64 of private.pem, one line>
JWT_PUBLIC_KEY=<base64 of public.pem, one line>
GOOGLE_CLIENT_ID=<your-client-id>.apps.googleusercontent.com
```

## Running the service

### With Docker Compose (from the repository root)

```bash
docker compose up --build user-service
docker compose logs -f user-service
```

This starts `user-db` (PostgreSQL) and the service on port 8080. Flyway creates the tables on first start. Data is kept in the `user-db-data` volume, so `docker compose down -v` deletes all users.

### Locally from IntelliJ

1. Start only the database: `docker compose up -d user-db`.
2. Run `UserServiceApplication`. `application.yaml` imports `.env` (`optional:file:.env[.properties]`), so the working directory must be the folder that contains `.env`, or the keys must be set as environment variables in the run configuration.
3. Datasource defaults point to `localhost:5432/user_db`.

## Testing

Run all tests from the `user-service` folder:

```bash
./mvnw test                         # all tests
./mvnw test -Dtest=AuthLoginTest    # one class
```

In IntelliJ, right-click `src/test/java` and choose _Run 'All Tests'_.

**Docker must be running for the integration tests**, because they start a PostgreSQL container through Testcontainers. On Windows, Docker Desktop keeps running in the background after its window is closed, so check the tray icon if you are unsure. You do not need to start `user-db` or anything from `docker-compose.yml`; the tests create and remove their own container.

The unit tests need neither Spring nor Docker. To run only them:

```bash
./mvnw test -Dtest='RefreshTokenServiceTest,JwtServiceTest,AuthServiceTest,ProfileServiceTest,JwtAuthFilterTest'
```

### How the tests are set up

- **Integration tests** (`src/test/java/.../integration`) start the full application against a real PostgreSQL 15 container through Testcontainers. Flyway runs the real migrations, so the schema is tested too.
- All integration test classes extend `AbstractIntegrationTest`, so they share one container and one Spring context, and the whole suite starts the database only once. The base class also empties the tables before each test and provides helpers such as `register`, `registerAndLogin`, `postJson`, `profileOf` and `userCount`.
- The test profile (`src/test/resources/application-test.yaml`) needs no `.env` file. An RSA key pair is generated when the tests start, so no private key is stored in the repository.
- `GoogleTokenVerifier` is replaced with a mock, so the tests never call Google. This means the Google token verification itself (signature, issuer, audience) is not covered, only what the service does with a verified user.
- **Unit tests** use Mockito (or plain objects) and run in milliseconds. `RefreshTokenServiceTest` sits in the same package as the service because it uses the package-private `hash` method.

### What the tests cover

| Test class                | Covers                                                                                                                                                                                                                                              |
| ------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `AuthRegisterTest`        | Validation, lower-cased email, duplicate email (`409`), BCrypt hash stored instead of the password                                                                                                                                                  |
| `AuthLoginTest`           | Token pair, wrong password and unknown email give the same `401`, validation                                                                                                                                                                        |
| `AuthRefreshLogoutTest`   | Rotation, reuse inside and after the grace period, expired and unknown tokens, only the hash is stored, logout is idempotent, and concurrent refresh with the same token (repeated 10 times: exactly one `200`, the session of the winner survives) |
| `AuthGoogleTest`          | New user, existing Google ID, linking by email, `409` on conflict, invalid token, Google-only accounts cannot use a password                                                                                                                        |
| `ProfileTest`             | `401` without a valid token, read profile, partial update, removing the picture, length and blank validation                                                                                                                                        |
| `RefreshTokenServiceTest` | Unit: hashing (known SHA-256 value), token creation, rotation, reuse detection with and without grace period, expiry, revoke                                                                                                                        |
| `JwtServiceTest`          | Unit: claims and expiry, RS256 header, rejects expired, foreign-key, tampered, unsigned and malformed tokens                                                                                                                                        |
| `AuthServiceTest`         | Unit: register (normalised email, trimmed name, encoded password, duplicate), every login branch, refresh and logout                                                                                                                                |
| `ProfileServiceTest`      | Unit: trimming, omitted fields stay unchanged, empty or blank picture URL removes the picture, unknown user                                                                                                                                         |
| `JwtAuthFilterTest`       | Unit: a valid token sets the user ID as principal; a missing, non-bearer or invalid token, or a non-numeric subject, leaves the request unauthenticated and the chain continues                                                                     |

### Continuous integration

A GitHub Actions workflow (`.github/workflows/user-service-tests.yml`) runs the tests on every pull request and on pushes to `main`. GitHub-hosted Ubuntu runners have Docker installed, so Testcontainers needs no extra setup. The workflow is pinned to `ubuntu-24.04` so a change of the `ubuntu-latest` label cannot affect the build unexpectedly.

## Known limitations and next steps

- Delete expired and revoked refresh tokens periodically (scheduled job).
- Move the CORS origin (`http://localhost:5173`) to environment variables before deploying.
- Optional: publish the public key through a JWKS endpoint so other services can fetch it and support key rotation.
