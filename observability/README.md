# observability

The log stack for LifeOS. Every service writes its logs as JSON to the console. Filebeat collects them from the Docker containers, Elasticsearch stores them, and Kibana is where you search and read them.

```
service (stdout, JSON) -> Docker log file -> Filebeat -> Elasticsearch -> Kibana
```

The rules for what the services log are in `docs/LOGGING.md`.

## What is in the stack

| Container       | Role                                                            | Port   |
| --------------- | --------------------------------------------------------------- | ------ |
| `elasticsearch` | Stores the logs. Single node, data kept in the volume `esdata`. | `9200` |
| `kibana`        | The web UI for searching logs (Discover).                       | `5601` |
| `filebeat`      | Reads the container logs and sends them to Elasticsearch.       | -      |

The stack is defined in `observability/compose.logging.yaml`. The main `docker-compose.yaml` includes it (`include: - observability/compose.logging.yaml`), so a normal `docker compose up -d --build` starts it together with the services.

**Dev setup only.** Security is switched off in Elasticsearch (`xpack.security.enabled=false`), port `9200` is open on the host, and the Java heap is limited to 512 MB. Do not use this as it is in production.

## Configuration

| Setting                          | Where                           | Meaning                                                                                     |
| -------------------------------- | ------------------------------- | ------------------------------------------------------------------------------------------- |
| `ELASTIC_VERSION`                | `.env`                          | Version of the three Elastic images (we run `8.19.15`). Use the same version for all three. |
| `discovery.type=single-node`     | `compose.logging.yaml`          | One Elasticsearch node, no cluster.                                                         |
| `ES_JAVA_OPTS=-Xms512m -Xmx512m` | `compose.logging.yaml`          | Heap size for Elasticsearch.                                                                |
| `filebeat/filebeat.yml`          | mounted read-only into Filebeat | What to collect and how to read it (below).                                                 |

Filebeat runs as root and reads `/var/lib/docker/containers` and the Docker socket (both read-only), because that is where Docker keeps the container logs.

## What Filebeat collects

Filebeat uses Docker autodiscover with hints. The default is **not** to collect anything, so only containers with this label are read:

```yaml
labels:
  co.elastic.logs/enabled: "true"
```

Today the label is on user-service, goal-service, progress-service, report-service, recommendation-service and the gateway. The databases, RabbitMQ and Consul do not have it, so their logs are not in Kibana.

**A new service is invisible in Kibana until it has this label in `docker-compose.yaml`.**

For each labelled container Filebeat:

1. reads `/var/lib/docker/containers/<id>/*.log`,
2. unwraps Docker's own log format (`container` parser),
3. parses each line as JSON (`ndjson` parser) and puts the fields at the top level of the event, so `service.name` and `log.level` become real fields,
4. sends the result to `http://elasticsearch:9200`, into the data stream `filebeat-<version>`.

A line that is not JSON (for example output from before logging is configured) is still stored, with an `error` field that says the JSON could not be parsed.

## Read the logs in Kibana

1. Open http://localhost:5601 and choose **Discover** in the menu.
2. Pick the data view `All logs`. Its index pattern is `logs-*-*,logs-*,filebeat-*` with `@timestamp` as the time field, which covers the Filebeat data. If it does not exist, create it with **Create a data view** and the same settings.
3. Set the time range top right (for example the last 15 minutes).
4. Search with KQL. A wildcard (`*`) only works when the value is **not** in quotes.

```
service.name : "goal-service" and (log.level : "WARN" or log.level : "ERROR")
log.logger : no.kristiania.*
```

Useful fields in a log line:

| Field                 | Content                                                                                                                           |
| --------------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| `@timestamp`          | When the line was written                                                                                                         |
| `service.name`        | The service (`user-service`, `gateway`, ...)                                                                                      |
| `service.version`     | The version from the build                                                                                                        |
| `log.level`           | `INFO`, `WARN`, `ERROR`, `DEBUG`                                                                                                  |
| `log.logger`          | The class that logged. Our own code starts with `no.kristiania.`                                                                  |
| `message`             | The log message                                                                                                                   |
| `process.thread.name` | The thread, for example `http-nio-8084-exec-4`                                                                                    |
| `container.name`      | The container, for example `lifeosfelles-recommendation-service-1`                                                                |
| trace id and span id  | Ids that follow one request. They are only set on lines logged inside a request (check the exact field name by opening one line). |

progress-service and report-service run two instances each. Their lines have the same `service.name`, so use `container.name` to see which instance wrote a line.

## Start and stop

```bash
# everything, including the log stack
docker compose up -d --build

# only the log stack
docker compose up -d elasticsearch kibana filebeat

# stop the log stack
docker compose stop filebeat kibana elasticsearch
```

Logs are kept in the volume `esdata`. `docker compose down -v` deletes it, and with it all stored logs.

## Check that it works

```bash
docker compose logs filebeat                      # errors from the collector
curl http://localhost:9200                        # Elasticsearch answers
curl "http://localhost:9200/_cat/indices?v"       # what Filebeat has created
```

## Troubleshooting

| Problem                                            | Check                                                                                                                                                                                                                                   |
| -------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Kibana says it is not ready                        | Elasticsearch is still starting. Wait a minute and reload.                                                                                                                                                                              |
| Discover shows nothing                             | Time range, the data view, and that the container has the `co.elastic.logs/enabled` label. Then `docker compose logs filebeat`.                                                                                                         |
| A service is missing                               | The label is missing on that service in `docker-compose.yaml`, or the container has not logged anything yet.                                                                                                                            |
| Everything is in one `message` field               | The service does not log JSON. Structured logging (`logging.structured.format.console: ecs`) comes from the shared Consul config (the gateway gets it as environment variables). Check that the service reads Consul and was restarted. |
| No trace id on a line                              | Lines written outside a request (startup, Tomcat, listeners) have none. Look at a line from our own code inside a request, for example `Goal created`.                                                                                  |
| A Tomcat warning about an invalid `g_state` cookie | Harmless. It comes from the Google login cookie in the browser and is logged once per service. Filter with `log.logger : no.kristiania.*`.                                                                                              |
| A wildcard search finds nothing                    | Remove the quotes around the value that has the `*`.                                                                                                                                                                                    |
| Elasticsearch keeps stopping                       | It needs a few GB of memory. Give Docker more memory.                                                                                                                                                                                   |

## Project structure

```
observability/
  compose.logging.yaml      Elasticsearch, Kibana and Filebeat
  filebeat/
    filebeat.yml            What Filebeat collects and how it reads the lines
  README.md
```

## What is not here

- No alerts and no dashboards are set up. Discover is enough for the project; useful dashboards would be WARN and ERROR per service, failed logins per hour, and `Refresh token reuse detected`.
- No log retention policy: logs stay until the volume is deleted.
- No trace backend. Tracing is sampled at 100%, but OTLP export is off, so trace ids only exist in the log lines.
