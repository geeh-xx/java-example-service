# AGENTS.md — example-service

> Primary entry point for AI coding agents (Claude Code, Copilot, Cursor, Aider, …) working in
> this repository. Agent-specific files (`CLAUDE.md`, `.cursorrules`, `.github/copilot-instructions.md`)
> defer to this document.

---

## Rule #0 — History Tracking with LOG.md (read this before anything else)

- Chat sessions lose track of details as they get longer. Use `LOG.md` to track every change
  so the project never has to be explained again in a new window.
- **LOG.md**: The History Tracking File, at the repo root — the session memory of this project.
- **Purpose**: Maintain a detailed log of all completed tasks to prevent duplicate work and
  provide context for future sessions.
- **Protocol**: At the end of each work session, summarize your achievements and add them to
  `LOG.md`. Keep it in a structured Markdown format for easy reading.
- **Content**: Include features implemented, bugs fixed, refactoring performed, decisions
  taken, significant challenges overcome, open problems — and always end the entry with a
  **"Suggested next step"** field.

Three practices make the LOG sustainable:

1. **Read first, write last.** Start every session by reading the recent entries of `LOG.md`.
   End every work session by appending an entry as described above.
2. **Rotation.** When `LOG.md` grows past **~30 entries**, move the oldest entries to
   `docs/log-archive/LOG-MM-YYYY.md` (by year), keeping only the recent ones at the root.
   The archive is NOT read by default — only when investigating history — so the LOG never
   eats the agent's context window.
3. **"Continue" has a defined meaning.** When the user's request is just "continue" (or
   equivalent), start from the **"Suggested next step"** field of the LAST entry in `LOG.md`.

These same rules are mirrored in the header of `LOG.md` itself.

---

## Reading order

Before making changes, read the relevant files in this order:

1. `AGENTS.md` (this file)
2. `LOG.md` (recent entries — Rule #0)
3. `.agents/README.md`
4. `.agents/agent-workflow.md`
5. `.agents/knowledge/architecture.md`
6. `docs/features/<feature>/SPEC.md` — when working on a feature that has one (create it if not)
7. `.agents/<task-specific>.md` (table below)

| Task | Required file |
|---|---|
| Java/Spring implementation | `.agents/knowledge/java.md` |
| Architecture/package decision | `.agents/knowledge/architecture.md` |
| Domain modeling | `.agents/knowledge/ddd.md` |
| External system integration (providers, connections) | `.agents/knowledge/integrations.md` |
| Database or Liquibase change | `.agents/knowledge/database.md` |
| REST/OpenAPI change | `.agents/knowledge/api.md` |
| Security/auth change | `.agents/knowledge/security.md` |
| RabbitMQ/CloudEvents/Outbox change | `.agents/knowledge/messaging.md` |
| Testing or BDD | `.agents/knowledge/testing.md` |
| Observability/logging/tracing | `.agents/knowledge/observability.md` |
| Production readiness / deploy | `.agents/production-readiness.md` |

---

## What this service is

`<Enter with a description about this project and technologies>`

Business logic (what an evidence or a spend item *means*) stays in the consuming services.
This service knows providers, connections and sync — nothing else.

Feature specs live in `docs/features/<feature>/` (see `docs/features/README.md`).


---

## Global non-negotiable rules

- Follow the existing package architecture (see §Module structure) — do not invent new
  top-level packages or modules without explicit approval.
- Follow **BDD → TDD → implementation** (see `.agents/knowledge/agent-workflow.md`).
- **Every external call goes through a provider adapter honoring the connection status,
  rate limit and call budget** — never call a provider SDK/HTTP API directly from a use case,
  consumer or scheduler (`.agents/knowledge/integrations.md`).
- Do not put business logic in controllers, queue consumers or infrastructure adapters.
- A use case may receive the request object passed by the controller, but it must not return
  a domain model/JPA entity to the controller. Return an output/result DTO instead.
- Do not call RabbitMQ directly from a use case — outbox pattern only.
- No database schema change without a new Liquibase changelog.
- Do not hardcode secrets, queue names or provider credentials.
- Never log tokens, secrets or raw provider payloads (`.agents/knowledge/observability.md`).
- Do not create duplicate abstractions when the project already has a pattern.
- Required method parameters use Lombok `@NonNull`; read-only parameters, locals and fields
  use Java `final`.
- Persistent domain entities extend `BaseEntity<ID>`; aggregate roots extend
  `AggregateRoot<ID>` and expose mutation only through behavior methods.
- JPA entities use `@Getter`, `@Entity`, `@Table(name = "<table_name>")` and
  `@NoArgsConstructor(access = AccessLevel.PROTECTED)`.
- Add or update tests for every behavior change (unit + Cucumber scenario).
- **Cucumber BDD tests are REAL integration tests running on Testcontainers**: the full
  Spring application boots against real MySQL and RabbitMQ containers. Never mock the
  database, the broker, repositories or use cases in a Cucumber scenario — the only
  permitted stub is the external provider's HTTP API (WireMock), because we cannot call
  the real externals api from CI (`.agents/knowledge/testing.md`).
- Update `LOG.md` at the end of the session (Rule #0).

---

## Tech stack

| Concern | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.1 (Web, Data JPA, AMQP, Security OAuth2 Resource Server, Validation, Actuator) |
| Build tool | Maven (single module) — `make` wraps common tasks |
| Messaging | RabbitMQ (Spring AMQP) + CloudEvents v1.0 JSON envelope |
| Database | MySQL 8 (Liquibase migrations, `src/main/resources/db/changelog/`) |
| Resilience | Retry/circuit-breaker policies defined by the concrete service |
| Mappers | MapStruct + Lombok (see `lombok.config`) |
| Unit testing | JUnit 5 · Mockito · Instancio · AssertJ (JaCoCo ≥ 80%) |
| BDD testing | Cucumber 7 · Testcontainers (MySQL) — Maven profile `it-test` |
| API contract | OpenAPI (`docs/openapi.yaml`) for REST · AsyncAPI via Springwolf for queues |
| Observability | SLF4J/Logback + MDC (`eventId`, `source`) · OpenTelemetry OTLP · Micrometer/Prometheus |
| Scheduling | ShedLock on every `@Scheduled` |
| Deploy | TeamCity (`make deploy`) |

---

## Core workflow — BDD → TDD → Implement

**Every feature MUST follow this exact order. No exceptions.**

```
0. Read/write the feature spec       docs/features/<feature>/SPEC.md
1. Write .feature file               src/test/resources/features/<domain>/   (Gherkin)
2. Write failing unit test           (Red)
3. Minimum production code           (Green)
4. Refactor without breaking tests   (Refactor)
5. make unit-test && make it-test    all green before committing
```

### Scaffold order for a new feature

1. `docs/features/<feature>/SPEC.md` (+ `DATABASE.md`, `API.md`, `MESSAGING.md` as needed)
2. Gherkin `.feature` → `src/test/resources/features/<domain>/`
3. Contract update → `docs/openapi.yaml` (REST) and/or CloudEvents payload (messaging)
4. Domain model/event → `src/main/java/com/{your-domain}/integrationservice/domain/{model,event}/`
5. Use case (interface + Impl) → `application/usecase/<domain>/<feature>/`
6. MapStruct mapper → `application/mapper/`
7. Liquibase changelog → `src/main/resources/db/changelog/`
8. Infrastructure adapters → `infrastructure/{messaging,persistence,web}/…`
9. Cucumber step definitions → `src/test/java/…`
10. Unit tests mirroring each production class

---

## Definition of Done

- [ ] `docs/features/<feature>/SPEC.md` exists/updated
- [ ] Gherkin `.feature` covers happy path and negative scenarios; all Cucumber scenarios pass
      **end-to-end against real MySQL/RabbitMQ (Testcontainers)** — no mocked infrastructure
- [ ] Unit tests cover domain and use-case layers; JaCoCo ≥ 80%
- [ ] OpenAPI/AsyncAPI contract updated when endpoints/queues changed
- [ ] Liquibase changelog added for every schema change (never edit an applied one)
- [ ] No `@Autowired` field injection — constructor injection only
- [ ] No secrets/queue names/credentials hardcoded
- [ ] `make unit-test` and `make it-test` pass locally
- [ ] `LOG.md` entry appended (Rule #0)

---

## Module structure

Single Maven module, package root `com.{your-domain}.exampleservice`:

```text
com.{your-domain}.exampleservice
 ├── domain
 │    ├── model            # entities and core domain models (JPA entities)
 │    ├── event            # domain events
 │    ├── input            # optional use-case input/command types, only when a request DTO is not enough
 ├── application
 │    ├── usecase          # one sub-package per domain feature (interface + Impl)
 │    ├── dto
 │    ├── mapper           # MapStruct mappers
 ├── infrastructure
 │    ├── configuration    # Spring @Configuration (one concern per class)
 │    ├── exception        # global handlers + exception taxonomy
 │    ├── messaging
 │    │    ├── consumer    # @RabbitListener handlers (delegate to use cases, no logic)
 │    │    ├── producer    # outbox MessagePublisher implementation
 │    ├── persistence
 │    │    ├── repository  # Spring Data repositories (+ impl adapters)
 │    ├── security         # WebSecurityConfig, JWT resource-server config
 │    ├── web
 │    │    ├── controller  # REST controllers (thin)
 │    │    ├── client      # HTTP clients for providers/internal services
 ├── shared
 │    ├── util
 │    ├── constants
```

**Do not change this layout.** New integrations add packages *inside* the existing ones
(e.g. `infrastructure/web/client/<domain>/`, `application/usecase/connection/…`) — see
`.agents/knowledge/architecture.md` and `.agents/knowledge/integrations.md`.

---

## Version control

Branches: `feature/<desc>` · `fix/<desc>` · `chore/<desc>` · `refactor/<desc>` · `test/<desc>`

Conventional Commits — types `feat|fix|test|refactor|chore|docs|ci`, scopes
`connection | sync | push | webhook | outbox | <domain> | infra | db`:

```
feat(connection): add <domain>  oauth callback handling
fix(outbox): prevent duplicate dispatch on retry
test(sync): add cucumber scenario for budget exhaustion
```

PRs: all tests green, rebase strategy (no merge commits), description references the
feature spec (`docs/features/<feature>/SPEC.md`) or ticket.

---

## Local development

```bash
make infra-up        # MySQL + RabbitMQ via docker-compose
make unit-test       # mvn test
make coverage        # mvn verify + JaCoCo HTML report
make it-test         # mvn clean verify -P it-test (Testcontainers; Docker required)
make run             # build + spring-boot:run
make deploy          # TeamCity deploy (needs TEAM_CITY_TOKEN in .env, VPN)
```

- Local profile: `-Dspring.profiles.active=local` (`application-local.yaml`)
- Actuator: `http://localhost:8080/actuator/`
- Never commit `.env` or credentials.

---

## Anti-patterns

| Never do this | Do this instead |
|---|---|
| Call a provider SDK/HTTP API from a use case or scheduler | Go through the provider adapter + connection manager (`.agents/knowledge/integrations.md`) |
| Retry auth errors (401/403) blindly | Refresh once, then mark the connection DISCONNECTED and stop polling |
| Poll a DISCONNECTED connection | Reconnection is a user action; unattended polling stops |
| `@Autowired` on fields | Constructor injection |
| `@Data` on JPA entities | `@Getter` + behavior methods for mutation |
| Public no-arg constructor on JPA entity | `@NoArgsConstructor(access = AccessLevel.PROTECTED)` |
| JPA entity without explicit table mapping | `@Table(name = "<table_name>")` |
| Required method parameter without null contract | Lombok `@NonNull` on the parameter |
| Reassigning read-only values | Java `final` on parameters, locals and fields |
| Aggregate root as a plain entity | Extend `AggregateRoot<ID>` |
| Business logic in consumers/controllers/mappers | Delegate to use cases |
| Calling RabbitMQ from a use case | Outbox table via `MessagePublisher`; `OutboxProcessor` dispatches |
| `@Scheduled` without `@SchedulerLock` | Always pair them |
| Hardcoded queue names | `${queue.*}` properties |
| Modifying an applied Liquibase changelog | New changelog file |
| Logging tokens or raw provider payloads | Log ids, statuses, durations (`.agents/knowledge/observability.md`) |
| Skipping the `.feature` file or the SPEC | Spec + Gherkin first |
| Mocking the database, broker, repositories or use cases in Cucumber tests | Real MySQL + RabbitMQ via Testcontainers; full app boots; only provider HTTP is stubbed (WireMock) |
| Ending a session without updating `LOG.md` | Rule #0 |
