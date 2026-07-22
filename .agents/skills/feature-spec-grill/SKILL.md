---
name: feature-spec-grill
description: Interview the user with focused questions before generating feature documentation under docs/features. Use when asked to create, refine, or scaffold a feature SPEC.md and optional DATABASE.md, API.md, MESSAGING.md, or OBSERVABILITY.md for this project.
---

# Feature Spec Grill

Run a spec-grilling session before writing feature docs. This skill adapts the
`grill-with-docs` idea: ask hard, specific questions until the feature is clear enough to
document, then write the docs.

## Required Context

Read these files before generating docs:

1. `references/feature-docs.md` — local feature folder rules.
2. `assets/SPEC.md` — canonical `SPEC.md` template.
3. Existing `docs/features/featureNN-*` folders — to pick the next feature number.

If this skill is copied to another project, prefer that project's `docs/features/README.md`
and `docs/features/_template/SPEC.md` when present.

## Workflow

1. Identify the feature name and next `featureNN-kebab-name` folder.
2. Ask questions before writing files. Do not invent business behavior.
3. Keep questions pointed and grouped; stop when answers are sufficient for a useful draft.
4. Generate `SPEC.md` first from `assets/SPEC.md`.
5. Generate companion docs only when answers require them:
   - `DATABASE.md` for schema, Liquibase, seed or migration changes.
   - `API.md` for REST endpoints, request/response contracts or OpenAPI changes.
   - `MESSAGING.md` for queues, events, commands or payloads.
   - `OBSERVABILITY.md` for feature-specific metrics, logs, alerts or SLOs.
6. Keep the feature business-agnostic unless the user explicitly gives domain rules.
7. Do not implement production code from this skill. Stop after docs unless the user asks
   for implementation in a separate step.

## Grilling Questions

Ask only what is missing. Prefer 3-7 questions per round.

Core:

- What problem does this feature solve?
- Who or what triggers the feature?
- What is the successful flow, step by step?
- What is in scope?
- What is explicitly out of scope?
- What are the negative/error cases?
- What should prove the feature works in production?

Contracts and data:

- Does this add or change a REST endpoint?
- Does this add or change database tables/columns/indexes?
- Does this publish or consume messages/events?
- What fields are required, optional, unique, immutable or generated?
- What status/state transitions exist?

Delivery:

- What acceptance criteria should become Cucumber scenarios?
- What rollout or rollback behavior is needed?
- What questions remain unresolved?

## Output Rules

- Directory: `docs/features/featureNN-kebab-name/`.
- `SPEC.md` is required.
- Acceptance criteria must be checklist items written in Given/When/Then style where possible.
- Include explicit out-of-scope bullets.
- Keep open questions in the spec instead of guessing.
- Keep companion docs concise and factual.

## Report readiness

End with one readiness state:

- `ready-for-review`;
- `blocked-by-open-questions`;
- `ready-for-implementation`.

Do not mark a feature `ready-for-implementation` while material open questions remain.