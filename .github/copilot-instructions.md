# Copilot Review Instructions

When reviewing this repository, prioritize correctness and architectural regressions over
style-only comments.

Check pull requests against these project rules:

- Use cases must not call RabbitMQ directly and must not publish domain events directly.
- Domain behavior should register domain events on aggregates; listeners convert those
  events to application messages for the outbox.
- Provider access must stay behind provider-agnostic gateways/factories. Do not allow Xero
  SDK or provider-specific HTTP details to leak into domain or use-case code.
- Queue, exchange, routing and credential values must come from configuration, not hardcoded
  constants.
- Database changes require a new Liquibase changelog; do not edit already-applied changesets.
- Domain classes and use cases should remain fully unit covered; overall coverage should stay
  above 80%.
- Never log tokens, secrets or raw provider payloads.
- Prefer small, actionable review comments with file/line references.
