# API — REST (OpenAPI-First) + AsyncAPI

## Scope

This service is **messaging-first** — the REST surface is intentionally small:

- OAuth flows that require a browser (authorize redirect / callback)
- ops/admin endpoints (trigger sync manually, inspect a connection)
- Actuator (health/metrics)

Everything else (commands, sync results, webhook fan-out) is RabbitMQ + CloudEvents
(see `messaging.md`).

## OpenAPI-First

- The contract lives in `docs/openapi.yaml`. Spec first, then implementation.
- If code generation is configured, never edit generated interfaces/DTOs — edit the spec and
  regenerate. If not, controllers must match the spec exactly (reviewed in PR).
- Version the API under `/v1/...`; breaking changes require a new version.

## Controller rules

- Controllers live in `infrastructure/web/controller` and are thin: pass the request to the
  use case directly or map request → input record, then map output → response. No logic, no
  repository access.
- Use cases may receive the controller request, but must not return domain model/JPA entity
  classes to controllers; return a command/result DTO instead.
- Validation via `jakarta.validation` on request records; violations → 400 handled by the
  global exception handler (`infrastructure/exception`).
- HTTP semantics: 200/201/202 (202 for accepted async commands), 400 validation,
  401/403 auth, 404 unknown resource, 409 idempotency/state conflicts, 5xx never for
  business outcomes.
- Idempotency: mutating endpoints accept an `Idempotency-Key` header where retries are
  possible.
