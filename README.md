# parallax-sports-ktor-microservices

Multi-project Gradle monorepo with **5 Ktor microservices** + a shared `common`
module. Each service is a Redis-stream-driven worker that delivers user
notifications through one channel (Discord, Telegram, Email) or supports the
pipeline (Cloudinary asset uploads, Playwright HTML→PNG rendering).

**Project DOCS:** https://tfg-avellaneda-25-26.github.io/parallax-sports-docs/

## Services

| Service        | Port  | Role                                                                |
| -------------- | ----- | ------------------------------------------------------------------- |
| ms-discord     | 8082  | Consumes `alerts:discord` stream, posts via JDA (bot + webhooks)    |
| ms-telegram    | 8083  | Consumes `alerts:telegram` stream, posts via kotlin-telegram-bot    |
| ms-email       | 8084  | Consumes `alerts:email` stream, sends via Gmail API                 |
| ms-cloudinary  | 8085  | Uploads generated images to Cloudinary, returns URLs                |
| ms-playwright  | 8087  | Renders event-card HTML → PNG via headless Chromium                 |

Ports come from each service's `src/main/resources/application.conf`
(`ktor.deployment.port`). They must stay in sync with the compose mappings in
[parallax-sports-infra/docker-compose.yml](https://github.com/TFG-Avellaneda-25-26/parallax-sports-infra)
and the Alloy scrape targets in `alloy/config.alloy`.

All services expose:

- `GET /metrics` — Prometheus metrics (JVM + custom stream-consumer meters)
- `GET /health` — liveness probe (200 OK = process is up)

## Local development

Build everything once to populate the Gradle cache:

```bash
./gradlew build -x test
```

Run a single service (auto-reloads via Ktor dev mode is off in this setup —
restart manually after edits):

```bash
./gradlew :ms-discord:run
```

The service expects:

- Redis reachable at `REDIS_HOST:REDIS_PORT` (env vars, default `localhost:6379`).
- Spring API reachable at `SPRING_BASE_URL` (default `http://localhost:8080`).
- `application-secrets.conf` decrypted via git-crypt (see Secrets below).

## Secrets

Five files are git-crypt encrypted in this repo:

- `common/src/main/resources/shared-secrets.conf` (shared base secrets)
- `ms-discord/src/main/resources/application-secrets.conf`
- `ms-telegram/src/main/resources/application-secrets.conf`
- `ms-cloudinary/src/main/resources/application-secrets.conf`
- `ms-email/src/main/resources/application-secrets.conf`

To unlock locally:

```bash
git-crypt unlock         # uses your GPG key if you're added as a user
# or
git-crypt unlock /path/to/parallax-ktor.key   # symmetric key fallback
```

This repo's git-crypt key is **separate** from the parallax-sports-spring and
parallax-sports-infra repos. Store it in your password manager.

## CI / CD (Jenkins)

The [Jenkinsfile](Jenkinsfile) at the repo root drives a 5-service pipeline:

1. **Checkout** the branch.
2. **Decrypt secrets** — `git-crypt unlock` using the `gitcrypt-ktor` Jenkins
   credential (Secret File kind, holds the symmetric key).
3. **Build distributions** — `./gradlew clean installDist --no-daemon -x test`
   produces a runnable distribution at `<service>/build/install/<service>/` for
   each of the 5 services.
4. **Build & push images** — 5 docker builds in sequence:
   - ms-discord / ms-email / ms-cloudinary / ms-telegram share
     [Dockerfile](Dockerfile) — tiny JRE-alpine image that copies the install
     dist and starts via `bin/run`. Built with `--build-arg SERVICE=<name>`.
   - ms-playwright uses [Dockerfile.playwright](Dockerfile.playwright) — based
     on `mcr.microsoft.com/playwright/java:v1.58.0-noble`, includes Chromium
     and friends.
   - All images push to `localhost:5000/parallax-<service>:latest` and
     `:${BUILD_NUMBER}`.
5. **Deploy** — `docker compose up -d --no-deps <all 5 services>` on the host.

## Adding a new microservice

1. `mkdir ms-foo && cd ms-foo` then mirror an existing service's structure
   (`build.gradle.kts`, `src/main/kotlin/...`, `src/main/resources/application.conf`).
2. Add `include(":ms-foo")` to `settings.gradle.kts`.
3. Pick a free port; set it in `application.conf` (`ktor.deployment.port`).
4. Add the service to `Jenkinsfile`'s `SERVICES` env var (or the playwright
   slot if it needs browsers).
5. Add a service block to `parallax-sports-infra/docker-compose.yml` with the
   same port number.
6. Add a scrape target in `parallax-sports-infra/alloy/config.alloy`.
7. If it needs secrets: create `application-secrets.conf`, add it to
   `.gitattributes` with the `git-crypt` filter, commit.

## Observability primitives

All five services use the shared helpers in
[common/src/main/kotlin/es/daw/parallaxbot/common/observability/](common/src/main/kotlin/es/daw/parallaxbot/common/observability/):

- `installMetrics(serviceName)` — registers Prometheus registry + JVM binders, exposes `/metrics`.
- `installHealth()` — exposes `/health` and `/health/ready`.
- `StreamConsumerMetrics` — drop-in meter set for `RedisStreamConsumer` subclasses (messages consumed, processed, retried, dropped, provider-send timer, etc.).
- `MdcContext` (alertMdc helper) — coroutine MDC propagation so every log line tied to an alert carries `alertId`, `channel`, `workerId`, `traceId`.

The base [RedisStreamConsumer](common/src/main/kotlin/es/daw/parallaxbot/common/RedisStreamConsumer.kt)
already wires those for you — just pass a `StreamConsumerMetrics` instance into the constructor in each service's Koin module.
