# <Feature name>

> Status: draft | in-progress | done
> Owner: <who>
> Related: <links to other features, tickets, external docs>

## Problem

What is broken/missing today, and why this service must solve it. One or two paragraphs.

## Scope

- In scope: …
- Out of scope: … (be explicit — this is what keeps PRs small)

## Behavior

Describe the flows in plain language (numbered steps). State machines, diagrams and edge
cases welcome. This section is what the Gherkin scenarios are written from.

## Acceptance criteria

- [ ] Given …, when …, then …  (each item becomes at least one Cucumber scenario)
- [ ] Negative/error cases included
- [ ] Observability: which metrics/logs/alerts prove it works in production

## Companion docs (delete lines that don't apply)

- `DATABASE.md` — tables/changelogs this feature adds or alters
- `API.md` — REST endpoints exposed/changed
- `MESSAGING.md` — queues, CloudEvents types, payloads

## Rollout & rollback

How it reaches production (flags, phases, shadow mode) and how it is turned off.

## Open questions

- …
