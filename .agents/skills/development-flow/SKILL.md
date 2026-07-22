# Agent Workflow

## Session protocol

1. **Start**: read `AGENTS.md`, then the recent entries of `LOG.md` (Rule #0), then
   `CONTEXT.md` for the project's canonical vocabulary.
   If the request is just **"continue"**, start from the **"Suggested next step"** field
   of the last LOG entry.
2. **Scope**: find or create the feature spec at `docs/features/<feature>/SPEC.md`.
   No feature work without a spec. If the plan or the domain language is still fuzzy at this
   point, reach for `/grill-with-docs` first — it settles vocabulary into `CONTEXT.md` and
   hard-to-reverse decisions into `docs/adr/` before the spec gets written, so the spec
   inherits settled terms instead of re-litigating them.
3. **Work**: BDD → TDD → implement (below). Stay inside the package layout.
4. **End**: run `make unit-test`, verify coverage, and run `make it-test`; when the change
   touches anything reflection-sensitive (new DTO, mapper, client), also confirm
   `mvn package -Dnative` still builds; append a `LOG.md` entry with date, what was done,
   decisions, open problems and **"Suggested next step"**.
   If `LOG.md` passed ~30 entries, rotate the oldest into `docs/log-archive/LOG-YYYY.md`.

## BDD → TDD → Implement

```
SPEC.md  →  .feature (Gherkin)  →  failing unit test  →  minimum code  →  refactor  →  all green
```

- The `.feature` file comes **before** production code — it encodes the acceptance criteria
  from the SPEC.
- Cucumber scenarios are **real integration tests**: full Quarkus app boot + real
  MySQL/RabbitMQ via Dev Services (Testcontainers under the hood), nothing internal mocked
  (see `.agents/testing.md`).
- Unit tests mirror each production class (`XxxUseCaseImpl` → `XxxUseCaseImplTest`).
- A feature is not done until all coverage gates are verified:
  - every domain class has 100% coverage;
  - every use-case implementation has 100% coverage;
  - the whole application has at least 80% coverage.
- A feature is not done until `make unit-test`, coverage verification and `make it-test`
  pass locally.

## Scaffold order

See `AGENTS.md` §Core workflow — spec → feature file → contract → domain → use case →
mapper → Liquibase → infrastructure adapters → steps → unit tests.

## When blocked

- Ambiguity about product behavior → ask, don't guess; record the question in `LOG.md`.
- Missing credentials/infra → record in `LOG.md` with what was attempted.
- Never commit/push without explicit approval from the user.
