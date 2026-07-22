# Observability

## Logging (SLF4J + Logback — `logback-spring.xml`)

- Structured and purposeful. Every message-processing flow carries MDC keys **`eventId`**
  and **`source`**; provider flows add `connectionId`, `provider`, `capability`, `companyId`.
- Log at the boundaries: what was received (ids, not payloads), which use case ran, which
  aggregate was affected, success/failure, duration, why it failed.
- Prefix log messages with the method context: `log.info("[completeConnection] ...")` —
  same convention as the other f360 services.

**Allowed in logs:** event/aggregate/connection ids, tenant/company ids, CloudEvent ids,
correlation ids, statuses, durations, HTTP status codes.

**Never log:** access/refresh tokens, JWTs, client secrets, passwords, raw provider payloads
(invoices/bank data may contain personal data). Raw payloads belong in DB columns
(access-controlled), never in stdout/ELK.

## Tracing (OpenTelemetry, OTLP exporter)

- Auto-instrumentation via the OTel Spring Boot starter; add `@WithSpan` +
  span attributes (`connectionId`, `provider`, `capability`) on use cases and provider gateways.
- Propagate context through RabbitMQ: `traceparent` as a CloudEvents extension attribute,
  restored by consumers — a command on spend_api must be traceable to the provider call here.
- Persist the trace id on connection status transitions (`status_changed_by_trace_id`)
  so production forensics never depends on log retention.

## Metrics (Micrometer → Prometheus, Actuator)

Minimum set per provider+tenant:

- `integration_provider_calls_total{provider,capability,status}`
- `integration_budget_consumed_ratio{provider,tenant}`
- `integration_breaker_state{provider,tenant}`
- `integration_sync_lag_seconds{capability}`
- outbox pending count, DLQ depth, retry count, processing duration

## Alerts (provisioned with the service, reviewed in PRs)

- 401/403 spike per provider (a dead connection being polled should be impossible — alert ≥ 1)
- call budget > 70%
- connection flip-flopping CONNECTED↔FAILED
- outbox/webhook backlog growth, DLQ non-empty
- sync lag SLO breach (p95 < 2× the capability's scheduled interval)

These alerts exist because their absence let the Jul/2026 Xero 401 loop and the Revolut
over-polling run for months unnoticed.
