# Integrations (providers) 

These rules exist for coordinate the integration for external systems

## One door per provider

- Every outbound call goes through the provider's gateway implementation in
  `infrastructure/web/client/<provider>/` — never from use cases, consumers or schedulers
  directly.
- The gateway implementation is wrapped by the **connection manager**, which enforces the
  rules below. Code cannot opt out.

## Connection lifecycle (state machine)

```
CREATED ──connect──► CONNECTED ──auth error──► FAILED ──refresh ok──► CONNECTED
                                                  │
                                                  └─refresh rejected──► DISCONNECTED
                                                     (terminal until the user reconnects)
```

1. Tokens are checked for expiry before every call; refresh proactively; persist rotated
   refresh tokens **immediately** (Xero refresh tokens are single-use).
2. First 401/403-consent error → refresh once → retry once.
3. Refresh rejected → `DISCONNECTED`; **all unattended polling for that connection stops**.
   Only a user-driven reconnect revives it.
4. **Auth errors are never in a blind retry list.** Transient codes (5xx, 424, timeouts) may
   retry with exponential backoff; 401/403 never.
5. Every status transition persists the trace id that caused it
   (`status_changed_by_trace_id`).

There is no standalone token-refresh scheduler. Scheduled jobs may exist for business
capabilities such as sync, but refresh happens only because a provider call is about to run.

## Rate limits and call budgets

- Per provider+tenant: rate limiter (e.g. Xero 60 calls/min, 80% headroom) and **daily call
  budget** (e.g.some api 5.000/day, alert at 70%) — Resilience4j + a `call_budget` counter.
- On 429: honor `Retry-After`; never burst.
- Budget exhausted → the sync job re-queues the remainder for the next window; it does not
  fail and does not burst.
- Unattended sync frequency respects the provider's rules (Open Banking-style: max 4
  unattended accesses/day per account when applicable).

## Data and idempotency

- External ids map to internal refs via the `entity_link` table — never scatter external ids
  across business tables.
- Sync uses persisted cursors (`sync_state`); pushes require an idempotency key and replay
  the stored result on duplicates.
- Raw provider payloads may be stored in DB columns for debugging — never logged.

## Adding a new provider (checklist)

1. `docs/features/<provider>-connection/SPEC.md` first.
2. Gateway interface in `application` (no SDK types in signatures).
3. Implementation under `infrastructure/web/client/<provider>/` + configuration class.
4. Register limits/budget values in configuration (never hardcoded).
5. Cucumber features: connect happy path, token rotation/revocation → DISCONNECTED,
   budget exhaustion, 429 handling.
