# Feature Docs Reference

Spec-first development: every feature starts with a folder under `docs/features/`, before
code changes. The SPEC is the source of truth an agent or human reads before touching the
feature.

## Structure

```text
docs/features/featureNN-<feature-name>/
├── SPEC.md            # required: problem, scope, acceptance criteria, rollout
├── DATABASE.md        # optional: tables, Liquibase changelogs, data migration notes
├── API.md             # optional: REST endpoints exposed/changed
├── MESSAGING.md       # optional: queues, CloudEvents types, payload schemas
├── OBSERVABILITY.md   # optional: feature-specific metrics, alerts, SLOs
└── notes/             # optional: investigation notes, external docs excerpts, decisions
```

## Rules

- Feature directories are prefixed with implementation order:
  `feature01-create-user`, `feature02-delete-user`, etc.
- Feature names after the numeric prefix are kebab-case and stable.
- Start from `assets/SPEC.md` or the project-local `docs/features/_template/SPEC.md`.
- A feature is more than its spec: schema changes go to `DATABASE.md`, contracts to
  `API.md` or `MESSAGING.md`, and feature-specific operations concerns go to
  `OBSERVABILITY.md`.
- Keep SPECs updated when scope changes during implementation.
- Gherkin `.feature` files in `src/test/resources/features/` must trace back to the
  acceptance criteria of a SPEC.
