# gateway

API gateway for LifeOS. It is the single entry point for the frontend: it receives every request on one port and forwards it to the right service, found through Consul. It does no authentication of its own.

- Port: `8100`
- Stack: Java 21, Spring Boot 4.1, Spring Cloud Gateway (WebFlux), Spring Cloud LoadBalancer, Consul
- Registers itself in Consul (service discovery). Unlike the other services it does not read the shared Consul config.
- No database. The only code is `GatewayApplication` and one logging filter; everything else is configuration

## Routes

Each route sends matching paths to `lb://<service-name>`. The name is looked up in Consul and requests are spread across the instances that pass their health check.

| Path                                           | Service                |
| ---------------------------------------------- | ---------------------- |
| `/auth/**`, `/profile/**`                      | user-service           |
| `/api/goal`, `/api/goal/**`, `/api/catalog/**` | goal-service           |
| `/api/progress`, `/api/progress/**`            | progress-service       |
| `/api/reports/**`                              | report-service         |
| `/api/recommendations/**`                      | recommendation-service |

A path that matches no route gets `404`. A route whose service has no healthy instance in Consul gets `503`.

## What the gateway does not do

- **No authentication.** Every service validates the JWT itself with the public key, so the gateway forwards the `Authorization` header untouched.
- **No CORS.** The frontend never calls the gateway from another origin: in development the Vite dev server proxies API calls to `localhost:8100` (override with `GATEWAY_URL`).
- **No retries or circuit breaker.** A failing instance is skipped only once Consul marks it critical (health is checked every 10 seconds, so a dead instance can still get requests for a short time).

## Configuration

Routes and discovery are in `src/main/resources/application.yml`.

| Setting                                       | Meaning                                                                                          |
| --------------------------------------------- | ------------------------------------------------------------------------------------------------ |
| `server.port`                                 | `8100`                                                                                           |
| `spring.cloud.consul.host`, `port`            | Consul address, default `localhost:8500`. Docker Compose sets `SPRING_CLOUD_CONSUL_HOST=consul`. |
| `spring.cloud.consul.discovery.query-passing` | Only route to instances that pass their health check.                                            |
| `spring.cloud.gateway.server.webflux.routes`  | The route table above.                                                                           |

To add a service: add a route with the service's Consul name and its paths.

Logging and tracing are set with environment variables in `docker-compose.yaml` (see Logging below), not through Consul.

## Run

From the repo root, with the whole system:

```bash
docker compose up -d --build gateway
```

Locally (needs Consul from Compose):

```bash
cd gateway
./mvnw spring-boot:run
```

Health check (also used by Consul): `http://localhost:8100/actuator/health`.

## Project structure

```
gateway/
  pom.xml
  Dockerfile
  src/main/java/no/kristiania/gateway/
    GatewayApplication
    filter/       GatewayErrorLoggingFilter
  src/main/resources/
    application.yml
```

## Logging

Logs go to stdout as JSON in ECS format, like all other services. The gateway does not read the shared Consul config, so the same settings are given as environment variables in `docker-compose.yaml`:

| Variable                                                                                                                               | Why                                                                                    |
| -------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------- |
| `LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs`                                                                                                | JSON logs in ECS format                                                                |
| `MANAGEMENT_TRACING_SAMPLING_PROBABILITY=1.0`                                                                                          | Trace every request, so `traceId` is always set                                        |
| `MANAGEMENT_TRACING_EXPORT_OTLP_ENABLED`, `MANAGEMENT_OTLP_METRICS_EXPORT_ENABLED`, `MANAGEMENT_LOGGING_EXPORT_OTLP_ENABLED` = `false` | No OTLP export, trace ids exist in the logs only                                       |
| `SPRING_REACTOR_CONTEXTPROPAGATION=auto`                                                                                               | Needed in a reactive app so `traceId` / `spanId` follow the request into the log lines |

If the values in the shared Consul config change, change them here too.

The gateway starts the trace, so the same `traceId` follows a request through the services it calls. A container is only picked up by the log stack if it has the label `co.elastic.logs/enabled: "true"` in `docker-compose.yaml`.

### What the gateway logs

`GatewayErrorLoggingFilter` writes one WARN line when the gateway itself fails a request, with the route, method, path (no query string) and exception type:

```
Gateway error status=503 routeId=goal-service method=GET path=/api/catalog/categories type=NotFoundException
```

| Case                                            | status | routeId   |
| ----------------------------------------------- | ------ | --------- |
| No route matches the path                       | 404    | `none`    |
| No healthy instance in Consul                   | 503    | the route |
| Connection or timeout problem towards a service | 5xx    | the route |

Successful requests are not logged, and neither are errors the services return themselves (a `401` or `409` from a service passes through; the service logs it). Spring also logs 5xx errors with a stack trace.

The rules for all services are in `docs/LOGGING.md`; the log stack is described in `observability/README.md`. To see DEBUG lines from our own code, set `logging.level.no.kristiania.gateway=DEBUG`.
