# Create User - Database

## Tables

### `users`

| Column | Type | Notes |
|---|---|---|
| `id` | `BINARY(16)` | Primary key, application-generated UUID |
| `name` | `VARCHAR(120)` | Required |
| `email` | `VARCHAR(255)` | Required, unique |
| `version` | `BIGINT` | Optimistic locking |
| `created_date` | `DATETIME(6)` | Created timestamp |
| `created_by` | `VARCHAR(64)` | Optional audit actor that created the row |
| `last_modified_date` | `DATETIME(6)` | Last modified timestamp |
| `updated_by` | `VARCHAR(64)` | Optional audit actor that last changed the row |

## Changelog

- `src/main/resources/db/changelog/user/2026-07-22-create-users-table.sql`
- `src/main/resources/db/changelog/user/2026-07-22-add-users-audit-actor-columns.sql`
