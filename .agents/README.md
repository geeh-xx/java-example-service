# .agents — topic guides for AI agents

Entry point is the repo-root `AGENTS.md` (read it first, including Rule #0 about `LOG.md`).
Each file here is a focused rulebook; load only what the task needs.

| File | Content |
|---|---|
| `agent-workflow.md` | BDD → TDD workflow, session protocol, LOG.md discipline |
| `knowledge/architecture.md` | Package layout, use-case pattern, dependency rules |
| `knowledge/java.md` | Java 25 / Spring style, Lombok, nullability, code smells |
| `knowledge/ddd.md` | Domain model, events, input types, JPA mapping rules |
| `knowledge/messaging.md` | RabbitMQ consumers, CloudEvents envelope, outbox, ShedLock |
| `knowledge/database.md` | Liquibase conventions, MySQL naming, datasources |
| `knowledge/api.md` | OpenAPI-First REST, Springwolf AsyncAPI, HTTP semantics |
| `knowledge/security.md` | JWT resource server, secrets handling, CORS |
| `knowledge/testing.md` | Unit tests, Cucumber/Testcontainers, coverage gates |
| `knowledge/observability.md` | Logback/MDC, OpenTelemetry, metrics, never-log list |
| `production-readiness.md` | Pre-deploy checklist |

## Local skills

| Skill | Use when |
|---|---|
| `skills/development-flow/SKILL.md` | The user wants to follow the repository development workflow as a reusable skill |
| `skills/feature-spec-grill/SKILL.md` | The user wants to create or refine a feature spec by answering questions first |

Feature specs live in `docs/features/<feature>/` — read the SPEC before coding a feature.
