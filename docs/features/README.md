# Feature specs

Spec-first development: **every feature starts with a folder here, before any code.**
The SPEC is the source of truth an agent (or human) reads before touching the feature.

## Structure

```
docs/features/featureNN-<feature-name>/
├── SPEC.md            # required — problem, scope, acceptance criteria, rollout
├── DATABASE.md        # optional — tables, Liquibase changelogs, data migration notes
├── API.md             # optional — REST endpoints (OpenAPI fragments) exposed/consumed
├── MESSAGING.md       # optional — queues, CloudEvents types, payload schemas
├── OBSERVABILITY.md   # optional — feature-specific metrics, alerts, SLOs
└── notes/             # optional — investigation notes, provider docs excerpts, decisions
```

- Feature directories are prefixed with implementation order:
  `feature01-create-user`, `feature02-delete-user`, etc.
- Feature names after the numeric prefix are kebab-case and stable.
- Start from `_template/SPEC.md`.
- A feature is more than its spec: schema changes go to `DATABASE.md`, contracts to
  `API.md`/`MESSAGING.md` — keep them in the folder so everything about the feature lives
  in one place.
- Keep SPECs updated when scope changes during implementation; the PR that changes behavior
  updates the SPEC in the same commit.
- Gherkin `.feature` files in `src/test/resources/features/` must trace back to the
  acceptance criteria of a SPEC.

## Current features

| Order | Feature | Status | Summary |
|---|---|---|---|
