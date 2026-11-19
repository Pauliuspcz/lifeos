# recommendation-service

Recommendation service for LifeOS. It rates the weather for outdoor training, and it gives hints about how a new goal fits together with the goals a user already has (goals that support each other, goals that compete, goals that overlap, and too many daily goals).

- Port: `8084` (reached through the gateway on `8100`)
- Stack: Java 21, Spring Boot 4.1, Spring Security (resource server), Spring Data JPA, PostgreSQL, Flyway, RabbitMQ, Caffeine
- Registers itself in Consul and reads shared config from Consul
- External API: [Open-Meteo](https://open-meteo.com) (no API key needed)
- Database: `recommendation_db` (host port `5436`)
- Never calls goal-service: it keeps its own list of outdoor goals, filled from events

## Endpoints

Through the gateway the route is `/api/recommendations/**`. All endpoints need a Bearer token.

| Method | Path                                        | Purpose                                                                                                 |
| ------ | ------------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| GET    | `/api/recommendations/weather?lat=&lon=`    | Weather rating for a position. `lat` is -90 to 90, `lon` is -180 to 180.                                |
| GET    | `/api/recommendations/weather/me?lat=&lon=` | The same, but only for users with an active outdoor goal. Returns `204` with no weather call otherwise. |
| POST   | `/api/recommendations/goals`                | Relation hints for a user's goals and an optional new goal.                                             |

### Weather recommendation

The response has `weather` (temperature, feels-like, precipitation, recent precipitation, wind, condition, day or night, `stale`), `rating` (`GOOD`, `OK` or `POOR`), `reason`, `advice` and `bestTime`.

- **POOR**: thunderstorm, risk of ice (freezing rain, precipitation near 0 degrees, or wet ground at or below 2 degrees), heavy precipitation (1 mm or more), strong wind (40 km/h or more), or feels-like at or below -10 or at or above 32.
- **OK**: below freezing (advice about grip), light precipitation, wind from 25 km/h, or feels-like below 0 or above 27.
- **GOOD**: everything else.
- A `POOR` rating comes with the advice to try an indoor session, which still counts for the streak.
- `bestTime` is the first stretch of better hours later today (between 06:00 and 22:00), and it is only set when it beats the current rating. The advice is left out when a best time exists.
- All thresholds are configuration (`app.weather.rating.*`).

### Relation hints

Request body: `goals` (the user's active goals, each with `templateCode`, `directionCode` and `period`) and an optional `candidate` (the goal being created). The response is a list of hints with `type`, `involved`, `suggestion` and `reason`.

| Type        | Meaning                                                                                                  |
| ----------- | -------------------------------------------------------------------------------------------------------- |
| `SUPPORTS`  | One goal makes another worth more. If the other goal is missing, `suggestion` names the template to add. |
| `TENSION`   | The goals work, but mind timing and load.                                                                |
| `REDUNDANT` | The goals overlap so much that one may be enough.                                                        |
| `OVERLOAD`  | More daily goals than `app.relations.overload.max-daily-goals` (default 8), the candidate included.      |

A rule matches a goal when the goal's template code or direction code is in the rule. With a `candidate`, only hints that involve the candidate are returned (overload is always checked). Equal hints are merged.

The rules are in `src/main/resources/relations.yml` (type, `a`, `b` and a reason). To add one, add an entry there. Keep `b` to one code per `SUPPORTS` rule so the suggestion is a single template.

### Errors

All errors are JSON: `status`, `error`, `message`, `timestamp`, plus `fieldErrors` for validation problems.

| Status | When                                                                             |
| ------ | -------------------------------------------------------------------------------- |
| 400    | `lat` or `lon` missing or out of range, invalid relation request, malformed body |
| 401    | Missing or invalid access token                                                  |
| 503    | Open-Meteo unavailable and nothing in the cache                                  |
| 500    | Unexpected error (details only in the logs)                                      |

## Weather data

The weather comes from Open-Meteo (`/v1/forecast`): current weather plus the hourly values for the last 12 and the next 12 hours, in the local time zone of the position.

- Positions are rounded to 1 decimal (about 11 km) for the cache key.
- Cache (Caffeine): fresh for 10 minutes, kept for up to 3 hours, at most 1000 positions.
- If Open-Meteo fails and a value up to 3 hours old exists, it is returned with `stale: true`. With nothing in the cache the endpoint answers `503`.
- Timeouts: connect 2 s, read 3 s. No retries.
- The weather endpoints need a token because Open-Meteo's free quota is per server IP.

## Events

recommendation-service consumes `goal.outdoor.changed` from the topic exchange `lifeos.goal` through the durable queue `recommendation.goal.outdoor.changed`, which it declares itself.

- `outdoor: true` saves `goalId` and `userId` in `outdoor_goal` (insert or update).
- `outdoor: false` deletes the row for `goalId`.
- Handling the same event twice is harmless.
- Failed messages are retried (Spring AMQP retry), then rejected without requeue. There is no dead-letter queue.

`/weather/me` only checks whether the user has at least one row in `outdoor_goal`.

## Security

The service validates the JWT itself with the public key (`JWT_PUBLIC_KEY`) and checks the issuer. The user id is the token's `sub` claim. Only `/actuator/health` and `/error` are public.

## Configuration

Set in `.env` at the repo root (read by Docker Compose) or as environment variables.

| Variable                                           | Used for                                                      |
| -------------------------------------------------- | ------------------------------------------------------------- |
| `POSTGRES_USER`, `POSTGRES_PASSWORD`               | Database login                                                |
| `RECOMMENDATION_DB_NAME`, `RECOMMENDATION_DB_PORT` | Database name and port (defaults `recommendation_db`, `5436`) |
| `JWT_PUBLIC_KEY`                                   | RSA public key, Base64-encoded PEM                            |
| `JWT_ISSUER`                                       | Expected issuer, default `lifeos-auth`                        |
| `RABBITMQ_USER`, `RABBITMQ_PASSWORD`               | RabbitMQ login (defaults `lifeos` / `lifeos_dev`)             |
| `OPEN_METEO_URL`                                   | Open-Meteo base URL, default `https://api.open-meteo.com`     |
| `CORS_ALLOWED_ORIGINS`                             | Allowed browser origins, default `http://localhost:5173`      |

Cache, rating thresholds and the overload limit are set under `app.weather.*` and `app.relations.*` in `application.yml`.

## Database

PostgreSQL, schema managed by Flyway (`src/main/resources/db/migration`). Hibernate only validates the schema (`ddl-auto: validate`). Table: `outdoor_goal` (`goal_id` primary key, `user_id` with an index).

## Run

From the repo root, with the whole system:

```bash
docker compose up -d --build recommendation-service
```

Locally (needs Postgres and RabbitMQ from Compose, and a `.env`):

```bash
cd recommendation-service
./mvnw spring-boot:run
```

Tests (Testcontainers needs Docker running; Open-Meteo is mocked with WireMock):

```bash
./mvnw verify
```

## Project structure

```
recommendation-service/
  pom.xml
  Dockerfile
  src/main/java/no/kristiania/recommendationservice/
    RecommendationServiceApplication
    config/        SecurityConfig, JsonAuthenticationEntryPoint, RabbitConfig,
                   RestClientConfig, CacheConfig
    exceptions/    GlobalExceptionHandler, ApiError, ApiException + WeatherUnavailableException
    outdoor/
      entity/      OutdoorGoal
      repository/  OutdoorGoalRepository
      listener/    OutdoorGoalListener
      events/      GoalOutdoorChangedEvent
    relations/
      controller/  RelationController
      service/     RelationService
      dto/         RelationRequest, RelationHint, GoalRef
      props/       RelationProperties, RelationRule
      enums/       RelationType
      utils/       RelationFinder
    weather/
      controller/  WeatherController
      service/     WeatherService
      model/       CachedWeather, Location
      dto/         WeatherResponse, WeatherRecommendation, BestTime
      enums/       Rating, WeatherCondition
      openmeteo/   OpenMeteoClient, OpenMeteoMapper, OpenMeteoResponse
      props/       WeatherCacheProperties, WeatherRatingProperties
      rules/       WeatherRating, BestWindow
  src/main/resources/
    application.yml
    relations.yml
    db/migration/  Flyway migrations
```

## Logging

Logs go to stdout as JSON in ECS format, set for all services in the shared Consul config. Each line has `log.level`, `service.name` and, inside a request, `traceId` and `spanId`.

| Where                    | Level                | What                                                                                                    |
| ------------------------ | -------------------- | ------------------------------------------------------------------------------------------------------- |
| `GlobalExceptionHandler` | WARN / ERROR / DEBUG | Rejected requests (status + message), unexpected errors with stack trace, invalid parameters            |
| `OutdoorGoalListener`    | INFO                 | Outdoor goal saved or removed, with `goalId` and `userId`                                               |
| `WeatherService`         | WARN / DEBUG         | Open-Meteo failed or returned nothing, stale weather served. A successful fetch is only logged at DEBUG |

The weather and relation endpoints only read, so they are not logged on success. Never logged: tokens, request bodies and positions (`lat` and `lon`). Users are identified by `userId`.

To see DEBUG lines from our own code, set `logging.level.no.kristiania.recommendationservice=DEBUG`.
