# LOG.md - Session History

Read this file at the start of each session after `AGENTS.md`. Append a structured entry at
the end of each session. When this file grows past roughly 30 entries, move the oldest entries
to `docs/log-archive/LOG-YYYY.md`.

---

## 2026-07-22 - Make Agent Docs Business-Agnostic

Summary:
- Rewrote `AGENTS.md` as generic `example-service` guidance for a reusable Spring Boot
  service template.
- Rewrote all `.agents/*.md` guides to remove domain-specific copied rules and align them
  with the current template shape.
- Rewrote `docs/features/README.md` and `docs/features/_template/SPEC.md` as generic
  feature-spec guidance.

Decisions:
- Kept the package root as `com.fedex.exampleservice` because that is the current source
  layout.
- Documented messaging, outbox and external integrations as optional patterns, not mandatory
  capabilities, because the current `pom.xml` does not configure a broker stack.
- Documented Cucumber/Testcontainers as not currently configured instead of requiring them.

Verification:
- Searched the requested docs for copied references to `f360`, `spend_api`, `Xero`, `xero`,
  `Revolut`, and `com.f360`; none remain.
- Did not run Java tests because this was a documentation-only change.
- Could not use `git diff` because `/Users/rangel/Downloads/example-service` is not detected
  as a Git repository by `git`.

Open problems:
- `README.md`, `pom.xml`, and application configuration still contain some service naming
  from the previous template state. They were outside the requested document set.

Suggested next step:
- Normalize `README.md`, Maven metadata and `spring.application.name` so the whole repository
  consistently presents itself as a generic `example-service` template.
