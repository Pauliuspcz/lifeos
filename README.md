# LifeOS

LifeOS is a goal tracker. Users pick a goal from a catalog of templates (or create their own),
check in when they do it, and follow their progress over time with streaks,
completion rate and history.

It was built by a team of two as the portfolio project for PG3402 Microservices at Kristiania University College (autumn 2026).
The system is split into five Spring Boot microservices, each with its own PostgreSQL database,
behind a Spring Cloud Gateway with Consul for service discovery and configuration. Services talk
synchronously over REST and asynchronously through RabbitMQ.

**What a user can do:**

- Register and log in with email/password or Google
- Create custom goals or choose from premade templates, then archive or delete them (editing is available through the API)
- Track two kinds of goals: check-in goals (daily, weekly or monthly, e.g. "run 3× a week") and amount goals
  (e.g. "read 300 pages a month") with a running total and a pace marker
- Check in on today's goals and fix forgotten days (up to 2 days back, weekly and monthly goals back to the start of the period)
- See insights per goal: current streak, completion rate, last 30 days vs the 30 before, history per period,
  and an activity calendar of all check-ins
- See the current weather in the top bar, plus a weather card with a GOOD/OK/POOR rating for outdoor training when they have an outdoor goal (OUTDATED - CHANGES)
- Get suggestions for goal categories that complement the ones they already have (API only, see [API](#api-postman)) (OUTDATED - CHANGES)

## Contents

- [Quick start](#quick-start)
  - [Other ways to run](#other-ways-to-run)
  - [Ports](#ports)
- [Test credentials and seed data](#test-credentials-and-seed-data)
- [Architecture](#architecture)
  - [Services](#services)
  - [Communication](#communication)
  - [Gateway, discovery, config, scaling](#gateway-discovery-config-scaling)
- [Tests and CI](#tests-and-ci)
- [User stories (how to test them)](#user-stories-how-to-test-them)
- [API (Postman)](#api-postman)
- [Decisions, assumptions and simplifications](#decisions-assumptions-and-simplifications)
- [Scope changes](#scope-changes)
- [Team contributions](#team-contributions)

## Quick start

**Requirements:** Docker Desktop (with Docker Compose v2). Nothing else needs to be installed:
Java, Maven and Node all run inside the containers.

1. Unzip the project and open a terminal in the project root (the folder with `docker-compose.yaml`).
2. Copy `.env.example` to a new file named `.env` (in the same folder). It contains dev-only values
   (database password, RabbitMQ login, an RSA key pair for JWT) that work as-is. Never reuse them in a real deployment!
   Google login needs your own Google client ID and is optional; email/password login works without it.

3. Build and start everything:

```bash
docker compose up -d --build
```

The first build takes a few minutes (Maven and npm download dependencies).

4. Wait until all services show as passing in the Consul UI at: http://localhost:8500
   (about 30–60 seconds after the containers start). Until then the gateway may answer `503`.

5. Open http://localhost:5173 and log in with `demo@lifeos.no` / `demo1234` (see [Test credentials](#test-credentials-and-seed-data)).

**Stop:** `docker compose down`
**Reset all data (wipes the databases):** `docker compose down -v`

### Other ways to run

**A. Backend in Docker, frontend locally (for frontend development)**

Requires Node.js 22 or newer.

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
   docker compose up -d user-db goal-db progress-db report-db recommendation-db rabbitmq consul consul-importer
   ```
2. In IntelliJ, run `GatewayApplication` and the `...ServiceApplication` class of each service you need.
   Each service reads `.env` from its working directory, so set the run configuration's
   working directory to the project root.
3. Run the frontend as in A.

### Ports

| What        | URL/PORT                                                              |
| ----------- | --------------------------------------------------------------------- |
| Frontend    | http://localhost:5173                                                 |
| API gateway | http://localhost:8100                                                 |
| Consul UI   | http://localhost:8500                                                 |
| RabbitMQ UI | http://localhost:15672 (login creds (`lifeos` / `lifeos_dev`))        |
| PostgreSQL  | user 5432, goal 5433, progress 5434, report 5435, recommendation 5436 |

## Test credentials and seed data

All seed data is inserted automatically by Flyway migrations on first startup. No manual setup is needed.

**Test user**

| Email            | Password   |
| ---------------- | ---------- |
| `demo@lifeos.no` | `demo1234` |

You can also register a new user on the login page.

**Seeded data**

- Test user: `user-service/.../V004__seed_demo_user.sql` (password stored as a BCrypt hash).
- Goal catalog: 4 categories, 12 directions and 34 templates (`goal-service/.../V002__seeding_goal_templates.sql`).

**Dev-only values:** `.env.example`, the test user and the RSA key pair are public on purpose so the project
runs from a clean clone. Any real deployment must use its own passwords and generate its own key pair
(see [user-service README](user-service/README.md#generating-the-key-pair)).

To start over with a clean database: `docker compose down -v`, then `docker compose up -d --build`.

## Architecture

LifeOS consists of five Spring Boot microservices, each owning its own PostgreSQL database.
The React frontend talks only to the API gateway, which routes requests to the service instances it finds in Consul.
Progress-service calls goal-service synchronously to check a goal before logging progress, and publishes
events to RabbitMQ that report-service consumes asynchronously to build its statistics.
Goal-service publishes an event when a goal is marked as outdoor, which recommendation-service uses to decide
who gets a weather recommendation. Recommendation-service fetches the weather from the external Open-Meteo API.

![LifeOS Microservices Architecture](docs/architecture.png)

### Services

| Service                | Container Port | Responsibility                                                                                                                       | Database            | Instances | Docs                               |
| ---------------------- | -------------- | ------------------------------------------------------------------------------------------------------------------------------------ | ------------------- | --------- | ---------------------------------- |
| user-service           | 8080           | Register, login (email/password + Google), issues JWT, user profile                                                                  | `user_db`           | 1         | [README](user-service/README.md)   |
| goal-service           | 8081           | User goals, template catalog, publishes outdoor-goal events                                                                          | `goal_db`           | 1         | [README](goal-service/README.md)   |
| progress-service       | 8082           | Check-ins / logged progress, publishes progress events                                                                               | `progress_db`       | 2         | -                                  |
| report-service         | 8083           | Consumes progress events, computes streak, completion rate, history                                                                  | `report_db`         | 2         | [README](report-service/README.md) |
| recommendation-service | 8084           | Weather rating for outdoor goals (Open-Meteo, cached), complementary goal suggestions, consumes outdoor-goal events                  | `recommendation_db` | 1         | -                                  |
| gateway                | 8100           | Single entry point, routes `/auth`, `/profile`, `/api/goal`, `/api/catalog`, `/api/progress`, `/api/reports`, `/api/recommendations` | —                   | 1         | -                                  |

User-service signs the JWT with the private key. Every other service validates it itself with the public key,
so user-service is only needed to log in, not on every request.

### Communication

**Synchronous (REST)**

- **Frontend -> gateway -> services:** every request carries the user's JWT (`Authorization: Bearer`).
  The gateway looks up the service in Consul and load-balances between instances.
- **progress-service -> goal-service:** before saving a check-in, progress calls `GET /api/goal/{id}` and forwards the
  user's token, to check that the goal exists and belongs to the user and to read its period and target.
  This call goes directly to `goal-service:8081` on the Docker network, not through the gateway.
  If goal-service is down or rejects the call, logging fails with `503` (consistency over availability).
- **recommendation-service -> Open-Meteo (external):** the current weather is fetched from the free Open-Meteo API
  (no API key). Timeouts are short (connect 2 s, read 3 s) and there are no retries. Responses are cached per location
  (rounded to 1 decimal): fresh for 10 minutes, and if Open-Meteo is down a stale value up to 3 hours old is returned
  with `stale: true`. With nothing usable in the cache the endpoint answers `503`.

**Asynchronous (RabbitMQ)**

_Progress -> report_

- progress-service publishes `progress.created` and `progress.deleted` events to the topic exchange `lifeos.progress`.
- report-service consumes them from its own durable queues (`report.progress.created`, `report.progress.deleted`)
  and keeps its own copy of the data, which it uses to compute statistics. It never calls the other services.
- Events carry a unique `eventId`, so a redelivered event is not counted twice.
- With two report instances, each event is processed by only one of them (competing consumers).

_Goal -> recommendation_

- goal-service publishes `goal.outdoor.changed` (`goalId`, `userId`, `outdoor`) to the topic exchange `lifeos.goal`
  when an outdoor goal is created, when the outdoor flag is changed, and when an outdoor goal is archived, unarchived or deleted.
- The event is sent only after the database transaction has committed, so a change that is rolled back never produces an event.
- recommendation-service consumes it from the durable queue `recommendation.goal.outdoor.changed` and keeps its own
  table of outdoor goals (insert or update on `outdoor: true`, delete on `false`), so handling the same event twice is harmless.
- `GET /api/recommendations/weather/me` only checks this table, using the user id from the JWT: no outdoor goal gives `204`
  and no weather call. Recommendation-service never calls goal-service, so weather keeps working when goal-service is down.

### Gateway, discovery, config, scaling

- **API gateway** (Spring Cloud Gateway, port 8100): the single entry point. Routes by path
  (`/auth/**`, `/profile/**` -> user, `/api/goal/**`, `/api/catalog/**` -> goal, `/api/progress/**` -> progress,
  `/api/reports/**` -> report, `/api/recommendations/**` -> recommendation) to `lb://<service-name>`,
  load-balancing between instances. The gateway does no authentication itself; each service validates the JWT.
- **Service discovery and health checks** (Consul, UI on http://localhost:8500): every service registers on startup,
  and Consul checks each instance's `/actuator/health`. When a container is recreated, its old registration stays
  in Consul as critical for about a minute before it is removed. The gateway only routes to passing instances, so it skips the old one (see [known limitations](#simplifications-and-known-limitations)).
- **Central configuration** (Consul KV): shared settings are stored under `config/application/data`, loaded into
  Consul at startup by the `consul-importer` container, and read by all services (`spring.config.import: optional:consul:`).
  It holds the discovery settings (including a random instance id per instance), the log format
  (`logging.structured.format.console: ecs`, so every service logs JSON) and the tracing settings
  (sampling 100%, OTLP export off, so trace ids appear in the logs only).- **Scaling:** progress-service and report-service run with two instances each (`deploy: replicas: 2`).
  The random instance id lets both instances register separately, and the gateway spreads requests across them.
  More instances: `docker compose up -d --scale report-service=3`.

## Tests and CI

Each service has its own tests and its own GitHub Actions workflow (`.github/workflows/<service>-tests.yaml`)
that runs on every pull request and on push to `main`.

- **Backend services:** unit tests for the business logic, and integration tests that start the whole service with
  real PostgreSQL and RabbitMQ in Docker (Testcontainers). They cover the API, security (JWT), ownership
  (users can only see their own data), messaging (events sent and received) and error handling.
- **Frontend:** type check, formatting check and unit tests (Vitest).

**Run the tests** (Docker must be running for the backend tests)

Each service is tested on its own. Open a terminal in the service's folder (e.g. `progress-service`) and run:

- Windows: `mvnw verify`
- macOS / Linux: `./mvnw verify`

Frontend: open a terminal in `frontend`, run `npm install`, then `npm test`.

Or in IntelliJ: right-click a service's `src/test/java` folder and choose **Run 'All Tests'**.

## User stories (how to test them)

Log in with `demo@lifeos.no` / `demo1234`, or register a new user. The same flows are in the Postman collection (see [API](#api-postman)).

**US-1: As a user, I want to register and log in, so that my goals are private to me.**

1. On the login page, click **No account? Register** and create a user (password at least 8 characters).
2. Log in. You land on **Today**.
3. Create a goal, log out on **Profile**, and log in as `demo@lifeos.no`: the goal from the other user is not visible.

**US-2: As a user, I want to create goals from templates or from scratch, and log my progress.**

1. On **Today**, click **+ New goal**. Pick an area, a focus and a template (e.g. _Walk 10,000 steps a day_), adjust it and save.
2. Create a weekly goal (e.g. _Run 2× a week_) and an amount goal (e.g. _Read 300 pages a month_).
3. On **Today**, tick a check-in goal. On **Log**, tick a day for the weekly goal and add an amount to the amount goal:
   the bar shows the total for the current period and a marker for where you should be by now.
4. Fix a forgotten day: on **Log**, tick yesterday.
5. On **Goals**, archive a goal and find it under **Archived**, where it can be restored or deleted
   (a goal must be archived before it can be deleted). Editing a goal is available through the API.

**US-3: As a user, I want to see statistics for my goals, so that I can follow my progress.**

1. Check in on a goal (US-2), then open **Insights**.
2. Each goal shows its current streak, completion rate, last 30 days vs the 30 before, and a history per period.
3. The activity calendar shows all check-ins per day.
4. The statistics are calculated by report-service from events, so they can take a second to appear after a check-in.

**US-4: Weather for outdoor goals**

TODO (recommendation-service)

## API (postman)

**Expected changes:** add later

## Decisions, assumptions and simplifications

| Decision                                                                                                     | Why                                                                                                                                 |
| ------------------------------------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------- |
| Database per service, no shared tables                                                                       | Services can change their schema and be deployed independently.                                                                     |
| JWT (RS256) validated in every service, not in the gateway                                                   | Only user-service holds the private key; services trust nothing they haven't checked themselves.                                    |
| Progress -> report via events, report keeps its own copy                                                     | Report keeps working when progress is down, and statistics don't slow down check-ins.                                               |
| Goal settings travel inside progress events                                                                  | Report never calls goal-service. Downside: a goal edit only reaches report with the next check-in.                                  |
| Progress -> goal is synchronous and fail-closed (`503`)                                                      | A check-in needs the goal's ownership and target at write time (consistency over availability).                                     |
| Outdoor goals sent as events to recommendation-service                                                       | The weather endpoint never calls goal-service.                                                                                      |
| Goal events published after commit                                                                           | A rolled-back change never produces an event.                                                                                       |
| Open-Meteo instead of OpenWeather                                                                            | No API key, so the feature works from a clean clone.                                                                                |
| Stale weather (max 3 h) instead of an error                                                                  | Availability over freshness when Open-Meteo is down.                                                                                |
| Weather endpoint requires JWT                                                                                | Open-Meteo's free quota is per server IP; an open endpoint could burn it.                                                           |
| Optimistic locking on goals (`@Version`)                                                                     | Two parallel edits can't silently overwrite each other.                                                                             |
| Amount goals reset per rolling period from the start date; check goals use calendar weeks/months             | A goal created mid-month still gets a full month for its amount. Check goals match report's statistics, which use calendar periods. |
| Check-ins can be fixed up to 2 days back (weekly/monthly goals: back to the start of the period, max 7 days) | Forgotten days can be logged, but never into a period that has already ended.                                                       |
| One check-in per goal per day; sending it again returns the existing entry                                   | A double click doesn't count twice, and no duplicate event goes to report.                                                          |
| A goal must be archived before it can be deleted                                                             | Avoids deleting by mistake; archived goals keep their history.                                                                      |

### Assumptions

- All users are in Norway: containers run with `TZ=Europe/Oslo`, and check-in dates come from the browser's local date.
- Location for weather comes from the browser. If the user denies it, the weather is simply not shown.
- Dev-only secrets in `.env.example` are never used in a real deployment (see [Test credentials](#test-credentials-and-seed-data)).

### Simplifications and known limitations

- No outbox or dead-letter queue: if a service crashes between saving and publishing, the event is lost.
- Progress events go on two queues and report runs two instances, so a very fast check-in + undo can be processed in the wrong order.
- The progress -> goal call has no timeout or circuit breaker yet.
- A recreated container leaves its old instance in Consul as critical for about 1 minute before Consul removes it. The gateway skips critical instances (`query-passing`). Consul checks each instance every 10 seconds, so for up to 10 seconds after an instance dies, the gateway may still send requests to it.
- Ice risk is estimated from current temperature and precipitation only; existing ice is not detected.
- Complement suggestions are fixed rules in configuration and only available through the API.
- Amount goals start from zero each period, and earlier periods aren't shown (no history yet).
- Amount goals have no streak or completion rate (report counts days, not amounts), so the frontend hides them.
- Check goals can be daily, weekly or monthly. Yearly isn't supported.
- A check goal's first period can be partial: a monthly goal created on the 28th only has 3 days in its first month.
- Goals can only be edited through the API; the frontend has no edit view yet.
- Google login needs your own Google client ID.

## Scope changes

**Changed**

- **Weather integration:** planned as a Python service using OpenWeather. Built as a Java recommendation-service using Open-Meteo,
  because Open-Meteo needs no API key (it runs from a clean clone) and the team could reuse the patterns from the other Spring services.
- **Goal suggestions:** lecturer feedback asked for something unique. We built complement suggestions (e.g. training -> sleep, food)
  as fixed rules in recommendation-service. Conflict warnings were left out.

**Added beyond the plan**

- API gateway, Consul discovery and central config, and two instances of progress and report (exam requirements for C–A).
- Outdoor goals: goal-service publishes an event, and recommendation-service shows a weather rating only to users with an outdoor goal.
- Amount goals with a running total per period and a pace marker, and weekly/monthly check-in goals.
- Activity calendar on Insights.

**Reduced or left out**

- Duration and time targets ("run 5 km in 25 min"): only check-in and amount goals are supported.
- Separate dashboard views for habits and one-time goals.
- "Best case" items from the plan: photo proof, PWA, Garmin/Google Fit, cloud deployment.

## Team contributions

**Write at the end**
