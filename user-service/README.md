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

| Decision                                                    | Why                                                                                                                       |
| ----------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------- |
| JWT signed with RS256 (key pair) instead of a shared secret | Only `user-service` holds the private key. Other services get the public key and can verify tokens but cannot forge them. |
| Opaque refresh tokens, stored only as SHA-256 hashes        | A database leak does not expose usable refresh tokens.                                                                    |
| Refresh token rotation with reuse detection                 | Each refresh token works once. If a revoked token is presented again, all of that user's tokens are revoked.              |
| Pessimistic lock when consuming a refresh token             | Two simultaneous refresh calls with the same token cannot both succeed.                                                   |
| Google login via ID-token verification                      | The backend verifies the token against Google's public keys. No client secret is needed.                                  |
| Stateless sessions, CSRF disabled                           | Authentication uses the `Authorization: Bearer` header only, no cookies.                                                  |
| Flyway migrations + `ddl-auto: validate`                    | The schema is versioned in SQL; Hibernate only checks that entities match it.                                             |

## Tech stack

- Java 21, Spring Boot 4.1.1 (Web MVC, Data JPA, Security, Validation)
- PostgreSQL 15, Flyway
- jjwt 0.13.0 (signing and parsing of access tokens)
- Spring Security OAuth2 JOSE (Nimbus) for Google ID-token verification
- Lombok
- Docker / Docker Compose

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
└── exceptions       NotFoundException
src/main/resources
├── application.yaml
└── db/migration     V001__create_user_structure.sql, V002__create_refresh_tokens.sql
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

Errors: `400` validation failed, `401` invalid credentials. Accounts created only through Google have no password and cannot log in here.

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

Error `401` when the token is unknown, expired or revoked. If a token that was already used is presented again, **all refresh tokens for that user are revoked**, which forces a new login on every device.

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
- Single use: every refresh returns a new token and revokes the old one.

### For other services

A service that needs to authenticate users only needs the **public key**. It verifies the RS256 signature and expiry of the access token and reads the user ID from `sub`. It should never receive the private key.

### Frontend notes

- Send only one refresh request at a time. Two parallel refreshes with the same token look like token reuse and revoke every session for the user.
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

| Column     | Type            | Notes                                               |
| ---------- | --------------- | --------------------------------------------------- |
| id         | BIGINT identity | primary key                                         |
| user_id    | BIGINT          | references `users(id)`, cascades on delete, indexed |
| token_hash | VARCHAR(64)     | SHA-256 hex, unique                                 |
| expires_at | TIMESTAMP       | required                                            |
| revoked    | BOOLEAN         | default false                                       |
| created_at | TIMESTAMP       | required                                            |

## Configuration

| Property                                      | Environment variable                                                                | Description                                                                        |
| --------------------------------------------- | ----------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------- |
| `app.jwt.private-key`                         | `JWT_PRIVATE_KEY`                                                                   | Base64 of the private PEM file (single line). Used for signing.                    |
| `app.jwt.public-key`                          | `JWT_PUBLIC_KEY`                                                                    | Base64 of the public PEM file (single line). Used for verification.                |
| `app.jwt.access-expiration-minutes`           |                                                                                     | Default 60                                                                         |
| `app.jwt.refresh-expiration-days`             |                                                                                     | Default 30                                                                         |
| `app.google.client-id`                        |                                                                                     | Google OAuth client ID. It is public, not a secret. The client secret is not used. |
| `spring.datasource.url / username / password` | `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | Database connection. Docker Compose overrides the local defaults.                  |

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

`.env` lives next to `docker-compose.yml` in the repository root and is listed in `.gitignore`. It contains the database variables used by Compose plus:

```
JWT_PRIVATE_KEY=<base64, one line, no quotes>
JWT_PUBLIC_KEY=<base64, one line, no quotes>
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

## Known limitations and next steps

- Add automated tests (registration, login, refresh rotation, logout, Google login with a mocked verifier).
- Add a `GlobalExceptionHandler` so all errors share one JSON format, and handle duplicate-email races as `409`.
- Delete expired and revoked refresh tokens periodically (scheduled job).
- Move the CORS origin (`http://localhost:5173`) and the Google client ID to environment variables before deploying.
- Optional: publish the public key through a JWKS endpoint so other services can fetch it and support key rotation.
