# Production readiness checklist

Before enabling a feature/provider in production:

## Functional
- [ ] All Cucumber scenarios green (`make it-test`), unit coverage ≥ 80%
- [ ] Feature SPEC (`docs/features/<feature>/SPEC.md`) matches what was built
- [ ] Idempotency verified for every command consumer and push endpoint

## Messaging
- [ ] Outbox used for every outbound message; DLQ + parking-lot bound for every queue
- [ ] Consumers idempotent (inbox dedup) and tolerant to redelivery

## Integration discipline (`.agents/knowledge/integrations.md`)
- [ ] All provider calls go through the gateway + connection manager (no direct SDK/HTTP use)
- [ ] 401/403 → refresh once → DISCONNECTED path exercised in a test
- [ ] Rate limit + daily budget configured with real provider values; 429 honors `Retry-After`
- [ ] Unattended polling frequency reviewed against the provider's rules

## Data
- [ ] Liquibase changelogs applied cleanly from scratch (Testcontainers run proves it)
- [ ] Tokens encrypted at rest; no sensitive data in logs

## Observability (`.agents/knowledge/observability.md`)
- [ ] Metrics emitting: provider calls, budget ratio, breaker state, sync lag, outbox pending
- [ ] Alerts provisioned: 401/403 spike, budget > 70%, DLQ non-empty, backlog growth
- [ ] Dashboards updated; MDC keys present in logs; traceparent propagates through Rabbit

## Ops
- [ ] Deploy via TeamCity (`make deploy`) documented for the branch
- [ ] Rollback path: feature flag or queue binding removal — written down in the SPEC
- [ ] `LOG.md` entry with go-live decisions and the "Suggested next step"
