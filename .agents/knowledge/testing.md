# Testing

## Unit tests (`mvn test` / `make unit-test`)

- JUnit 5 + Mockito + Instancio + AssertJ.
- Every production class has a mirroring test class; use cases are tested against mocked
  interfaces (repositories, gateways, publisher).
- Coverage gate: **JaCoCo ≥ 80%** (`make coverage` opens the HTML report).
- Instancio generates test data; avoid hand-built object mothers unless the scenario needs
  specific values.
- No Spring context in unit tests — plain constructors and mocks.

## BDD integration tests (`make it-test` → `mvn clean verify -P it-test`)

**Cucumber BDD tests are REAL integration tests. This is non-negotiable.**

The full Spring Boot application boots and every scenario exercises the real stack
end-to-end on **Testcontainers**:

- **Real MySQL container** — Liquibase migrations run from scratch on it (that IS the
  migration test); scenarios assert against real persisted rows.
- **Real RabbitMQ container** — commands are published to real queues, consumers really
  consume, the outbox processor really dispatches; scenarios assert on real messages, not
  on publisher mocks.
- **Nothing internal is mocked**: no mocked repositories, no mocked use cases, no mocked
  `MessagePublisher`, no Spring context slices, no in-memory H2, no embedded broker.
- The **only permitted stub** is the external provider's HTTP API (WireMock), because CI
  cannot call the real Xero — and even then WireMock replays realistic payloads including
  error semantics (401, 403, 429 with `Retry-After`, 5xx).

Practicalities:

- Docker is required (`make it-test`); Cucumber 7 runs under the `it-test` Maven profile,
  isolated from unit tests.
- Features in `src/test/resources/features/<domain>/`, Gherkin syntax
  ([better-gherkin](https://cucumber.io/docs/bdd/better-gherkin)); step definitions in
  `src/test/java/...`.
- **Every use case has at least one Cucumber scenario** — happy path and the negative
  scenarios from the feature SPEC.
- A scenario that passes only because something real was replaced by a mock is a failed
  scenario — fix the test, not the rule.

## Reports

- Cucumber HTML/JSON/JUnit reports under `target/cucumber-reports/`.
- CI runs unit tests on every push and the `cucumber` profile before merge.
