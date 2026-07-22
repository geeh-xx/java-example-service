# Database — MySQL + Liquibase

## Liquibase

- Changelogs live in `src/main/resources/db/changelog/` (master changelog includes per-area
  files; see the `example/` folder for the current structure).
- **Never modify an applied changelog** — always create a new file.
- One changelog per logical change, named `NNN-verb-subject.yaml` (or the pattern already in
  the folder — follow it), with `author` and a meaningful `id`.
- Every schema change ships in the same PR as the code using it, and runs green in the
  Cucumber suite (Testcontainers applies migrations from scratch — that is the migration test).
- Rollback blocks where feasible.

## Conventions

- Tables and columns: `snake_case`; PK column `id`.
- **PK type follows the entity's exposure** (`.agents/ddd.md`): exposed via API/events/any
  external communication → UUID stored as **`BINARY(16)`**, application-generated as
  **UUIDv7** via  `Generators.timeBasedEpochGenerator()`;
  internal-only → `BIGINT AUTO_INCREMENT`. Externally-owned identifiers are never the PK
  of an internal table: store them as a `UNIQUE` column beside the `Long` id
  (`outbox.event_id BINARY(16)`, `inbox_dedup.event_id VARCHAR(64)`).
- UUID PK mapping on the entity:

  ```java
  @Id
  @JdbcTypeCode(SqlTypes.BINARY)
  @Column(name = "id", nullable = false, columnDefinition = "BINARY(16)")
  private UUID id;
  ```

  In raw SQL (tests/ops), compare with `id = UNHEX(REPLACE(?, '-', ''))`.
- Audit columns on every table: `created_date`, `last_modified_date` — populated by Spring
  Data JPA auditing (`@EnableJpaAuditing` in `JpaAuditingConfiguration`; entities use
  `@EntityListeners(AuditingEntityListener.class)` + `@CreatedDate`/`@LastModifiedDate`,
  never `@PrePersist`/`@PreUpdate`).
- Every domain table (entity extending `BaseEntity`) also has
  `version BIGINT NOT NULL DEFAULT 0` (JPA `@Version` optimistic locking) and
  `created_by`/`updated_by VARCHAR(64)` (`@CreatedBy`/`@LastModifiedBy`). Auditor
  resolution order (`AuditorAware` in `JpaAuditingConfiguration`): 1) `user_id` JWT claim
  (`application.gateway.AuthenticationProviderGateway`, implemented by Spring Security
  infrastructure); 2) actor set in `AuditContextHolder` at the entry
  point (consumers set the CloudEvents `source`, schedulers set the job name — always
  cleared in `finally`); 3) `"SYSTEM"`. All inherited from `BaseEntity`.
- Status/state columns: `VARCHAR` with enum values, never numeric codes.
- Money: `DECIMAL(19,4)` + `currency CHAR(3)`; timestamps in UTC (`DATETIME`).
- Indexes named `idx_<table>_<cols>`; FKs `fk_<table>_<ref>`.

## Service-owned tables (core set)

| Table | Purpose |
|---|---|
| `connection` | provider, tenant id, encrypted tokens, status, scopes |
| `entity_link` | internal ref (service+type+id) ↔ external id per connection |
| `sync_state` | cursor + last run/success + error streak per connection+capability |
| `call_budget` | consumed calls per connection+day |
| `outbox` / `inbox_dedup` | messaging reliability |
| `webhook_event` | raw inbound webhooks + processing status |
| `shedlock` | scheduler locks |

## Sensitive data

- Tokens/secrets encrypted at rest (application-level crypto before persist).
- Raw provider payload columns (`source_payload`) are debugging aids — access-controlled,
  never logged, never exposed via API.
