# Messaging — RabbitMQ, CloudEvents, Outbox, ShedLock

## CloudEvents envelope

All messages follow [CloudEvents v1.0 JSON](https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/formats/json-format.md):

```json
{
  "specversion": "1.0",
  "type": "com.{your-domain}.integration.connection.status-changed",
  "source": "/example-service",
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "time": "2026-07-14T10:00:00Z",
  "datacontenttype": "application/json",
  "data": { }
}
```

- `type` identifies the message — never route by queue name alone.
- Commands (inbound): `com.{your-domain}.integration.cmd.<domain>.<action>` — carry `correlationid`;
  results come back as events (no request/reply RPC over Rabbit).
- Events (outbound): `com.{your-domain}.integration.<domain>.<what-happened>`.

## Consumers (`infrastructure/messaging/consumer`)

- One handler class per domain area; each `@RabbitListener` method delegates immediately to a
  use case — zero business logic in the handler.
- Queue names always from properties: `@RabbitListener(queues = "${queue.connection.commands}")`.
- Throw a domain exception on failure so the message NACKs; poison messages go to the DLQ /
  parking-lot.
- Do not hardcode queues, exchanges or bindings in Java configuration classes. RabbitMQ
  topology is versioned in `src/main/resources/messaging/messaging-topology-changelog.yml` and applied
  on startup by the topology initializer using `RabbitAdmin`, similar to how Liquibase
  versions database schema.
- New/changed message topology must be a new YAML changeset with a stable `id`; `author`
  defaults to `system` when omitted. Applied changesets are recorded in
  `message_topology_changelog`; changing an already-applied changeset checksum must fail
  startup.
- Keep `spring.rabbitmq.listener.simple.missing-queues-fatal=true` so listener startup fails
  if topology creation did not happen correctly.
- **Idempotent consumption**: dedup by CloudEvents `id` (inbox table) — consumers must
  tolerate redelivery. Persist the consumed queue name in `inbox_dedup.destiny_queue`
  for audit/debugging.
- Log `type` and `id` on entry; put `eventId`/`source` into MDC (see `observability.md`).

## Outbox (mandatory for every outbound message)

```
Use case (@Transactional)
   └─ aggregate.registerEvent(...)
      └─ DomainEventListener<T>.handle(event)
         └─ MessagePublisher.publish(IntegrationMessage) ──► outbox table (PENDING, same transaction)
OutboxProcessor (@Scheduled + @SchedulerLock, separate transaction)
   └─ reads PENDING in small batches ──► RabbitMQ ──► marks PROCESSED / FAILED per row
```

- `MessagePublisher` is an application interface; the impl
  (`infrastructure/messaging/producer`) only writes to the DB — no broker knowledge.
- Use cases must never call `MessagePublisher` directly. Aggregates raise domain events and
  each domain event has its own `DomainEventListener<T extends DomainEvent<?>>`.
- `MessagePublisher.publish(...)` receives an application message object, not a `DomainEvent`,
  because some messages are not domain events.
- Outbox rows persist `destiny_queue` explicitly. `CloudEvents.type` is the semantic event
  contract, not the RabbitMQ destination; `OutboxProcessor` dispatches to the stored
  `destiny_queue`.
- Outbox rows persist `message_event_type` explicitly: `INTERNAL` for internal application
  messages such as domain-event/inbox-derived messages, `EXTERNAL` for messages destined to
  systems outside the platform boundary. This classification is separate from
  `CloudEvents.type`.
- Always called inside a `@Transactional` method; if the business operation rolls back, the
  message is never persisted.
- Never call `RabbitTemplate` from a use case.
- `lockAtLeastFor` ≥ expected processing time; `lockAtMostFor` as safety ceiling; batch ≤ 100.

## ShedLock

Every `@Scheduled` task uses `@SchedulerLock` (lock rows in the `shedlock` table, managed by
Liquibase). No exceptions — this service runs multi-replica.
