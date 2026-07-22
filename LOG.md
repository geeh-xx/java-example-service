# LOG.md - Session History

Read this file at the start of each session after `AGENTS.md`. Append a structured entry at
the end of each session. When this file grows past roughly 30 entries, move the oldest entries
to `docs/log-archive/LOG-MM-YYYY.md`.

---

## 2026-07-22 - Implement Create User Feature

Summary:
- Created `docs/features/feature01-create-user/` with `SPEC.md`, `DATABASE.md` and `API.md`.
- Added a Gherkin feature file for create-user acceptance criteria.
- Added OpenAPI contract for `POST /v1/users`.
- Implemented user domain model, create-user input, use case, repository port, JPA adapter,
  controller and conflict exception handler.
- Added Liquibase changelog for the `users` table.
- Fixed `WebSecurityConfig` compilation so the project can build.

Decisions:
- Used `UUID` as the user id because the id is exposed through the API.
- Kept the feature business-agnostic: user creation stores only `name` and `email`.
- Used explicit constructors/getters in new classes because Lombok annotation processing was
  not producing methods during compilation.

Verification:
- `make unit-test` passed: 4 tests, 0 failures.
- `mvn verify` passed.
- `make it-test` passed, but Maven warned that the requested `cucumber` profile does not
  exist, so no real Cucumber integration profile was activated.

Open problems:
- The Gherkin file exists as required by the workflow, but there are no Cucumber dependencies,
  profile or step definitions configured in the project yet.
- Some copied old references and inconsistencies remain outside this feature work and need
  review.

Suggested next step:
- Review the remaining old references/inconsistencies and decide which documentation or build
  metadata should be normalized next.

---

## 2026-07-22 - Align Management Paths And README Module Description

Summary:
- Updated `docs/openapi.yaml` operational paths from `/actuator/...` to `/management/...`
  to match `management.endpoints.web.base-path`.
- Updated `MetricsController` metadata to report `/management/prometheus`.
- Updated `README.md` to describe the project as a Maven single module instead of a
  multi-module reactor with a separate test module.
- Updated README integration-test paths from `test/...` module paths to current root-module
  paths and documented that the `cucumber` profile is not configured yet.

Decisions:
- Left remaining old references untouched for separate review.

Verification:
- `make unit-test` passed: 4 tests, 0 failures.
- Searched for `/actuator` references after the change; only a permissive security matcher
  remains.

Open problems:
- Several copied template references remain in `AGENTS.md`, `.agents/knowledge/*`,
  `.agents/production-readiness.md`, `.agents/skills/development-flow/SKILL.md`, README,
  and configuration metadata.

Suggested next step:
- Normalize the remaining copied references and decide whether messaging/Cucumber-related
  rules should be kept as template expectations or documented as optional future additions.

---

## 2026-07-22 - Configure IT Test Profile And Cucumber Reports

Summary:
- Added Cucumber, JUnit Platform Suite and Testcontainers dependencies.
- Added Maven profile `it-test` with Failsafe execution.
- Updated `make it-test` to run `mvn clean verify -P it-test`.
- Added `CucumberIT` runner configured to generate HTML, JSON and JUnit XML reports under
  `target/cucumber-reports/`.
- Added Cucumber Spring configuration with a real MySQL 8 Testcontainers database.
- Implemented create-user Cucumber steps that exercise the running Spring Boot app over HTTP
  and assert persistence in the Testcontainers MySQL database.
- Updated README and agent docs to use `it-test` instead of the old `cucumber` profile name.

Decisions:
- Kept `application.yaml` unchanged; the Cucumber context activates `it-test` through test
  properties.
- Used Java `HttpClient` in Cucumber steps instead of `TestRestTemplate`, which is not
  available in this Spring Boot 4 setup.
- Fixed the create-user Liquibase SQL so it applies cleanly as the raw included changelog.

Verification:
- `make unit-test` passed: 4 tests, 0 failures.
- `make it-test` passed: unit tests plus 2 Cucumber scenarios, 0 failures.
- Confirmed generated reports:
  - `target/cucumber-reports/cucumber.html`
  - `target/cucumber-reports/cucumber.json`
  - `target/cucumber-reports/cucumber.xml`
- Searched docs/build/test files for `-P cucumber`, `when configured` and `when needed`;
  no matches remain in the checked paths.

Open problems:
- Some broader copied template references may still remain outside the specific profile/report
  inconsistency fixed here.

Suggested next step:
- Continue normalizing remaining template references that are not part of the configured
  example-service stack.

---

## 2026-07-22 - Document Lombok Null Contracts And Aggregate Root Pattern

Summary:
- Updated `AGENTS.md` with global rules for Lombok `@NonNull`, Java `final`, `BaseEntity`
  and `AggregateRoot`.
- Updated `.agents/knowledge/java.md` to require Lombok `@NonNull` on required method
  parameters and Java `final` on read-only parameters, local variables and fields.
- Updated `.agents/knowledge/ddd.md` to require aggregate roots to extend
  `AggregateRoot<ID>` and documented the standard JPA entity annotation block:
  `@Getter`, `@Entity`, `@Table(name = "<table_name>")`,
  `@NoArgsConstructor(access = AccessLevel.PROTECTED)`.
- Updated `.agents/knowledge/architecture.md` to reference `AggregateRoot<ID>` and the
  `@NonNull`/`final` style rule.

Decisions:
- Kept the table annotation example generic with `<table_name>` instead of a domain-specific
  table name so the project remains a reusable skeleton.

Verification:
- Searched `AGENTS.md` and the Java/DDD/architecture guides for `NonNull`, `final`,
  `AggregateRoot`, `NoArgsConstructor`, `AccessLevel.PROTECTED`, `@Getter`, `@Entity` and
  `@Table`; all requested rules are now present.

Open problems:
- Existing production code may still need refactoring to fully comply with the newly
  documented style rules.

Suggested next step:
- Bring the existing `User` domain model and related code into compliance with the documented
  `AggregateRoot`, Lombok annotation and `final`/`@NonNull` conventions.

---

## 2026-07-22 - Verify Test Suites After Aggregate Root Changes

Summary:
- Ran the unit test suite and confirmed it passes.
- Ran the integration/Cucumber suite with the `it-test` profile and found a schema mismatch
  introduced by the `BaseEntity` audit actor fields.
- Added a Liquibase changelog for `users.created_by` and `users.updated_by` and included it
  in `db.changelog-master.yaml`.
- Updated the create-user database feature documentation to include the audit actor columns.

Decisions:
- Added a new changelog instead of editing the existing create-table changelog, following the
  documented migration rule.
- Kept `created_by` and `updated_by` nullable because no authenticated auditor is required for
  the current example-service create-user flow.

Verification:
- `make unit-test` passed: 4 tests, 0 failures.
- `make it-test` passed: 4 unit tests plus 2 Cucumber/Testcontainers scenarios, 0 failures.
- Confirmed Cucumber report files exist under `target/cucumber-reports/`.

Open problems:
- Maven/JDK still emits warnings about restricted native access and Mockito dynamic agent
  self-attachment; they do not fail the tests.

Suggested next step:
- Review remaining copied-template references and decide which ones should be normalized next.

---

## 2026-07-22 - Remove Redundant BaseEntity Getters

Summary:
- Removed explicit getters from `BaseEntity` because the class already uses Lombok `@Getter`.
- Kept only the abstract `getId()` contract, since the id is supplied by concrete entities.
- Updated the create-user use case so the API result no longer depends on calling the
  inherited audit getter from `BaseEntity`.

Decisions:
- Preserved the documented entity style: Lombok owns regular getters in the base entity.
- Kept `createdDate` in the API result as an application result timestamp for this skeleton
  feature, while JPA auditing continues to own the persisted audit columns.

Verification:
- `make unit-test` passed: 4 tests, 0 failures.
- `make it-test` passed: 4 unit tests plus 2 Cucumber/Testcontainers scenarios, 0 failures.

Open problems:
- Maven/JDK still emits warnings about restricted native access and Mockito dynamic agent
  self-attachment; they do not fail the tests.

Suggested next step:
- Continue reviewing copied-template references and inconsistencies in the skeleton docs.

---

## 2026-07-22 - Align Service Parameter And Use Case Boundary Rules

Summary:
- Removed `@Valid` from create-user method parameters and kept Lombok `@NonNull` plus Java
  `final` on required parameters.
- Updated the create-user service to use `@Service`, `@RequiredArgsConstructor` and
  Lombok `@Log4j2`.
- Configured `maven-compiler-plugin` with Lombok annotation processing so
  `@RequiredArgsConstructor`, `@Getter` and `@NonNull` are honored by Maven builds.
- Updated agent rules to state that services do not use `@Validated`, method parameters do
  not use `@Valid`, and use cases may receive the controller request but must not return
  domain model/JPA entity classes.

Decisions:
- Used `@Log4j2` rather than Lombok `@Log4j` because `@Log4j` targets the old
  `org.apache.log4j` API, which is not on this project's classpath.
- Kept the create-user use case returning `CreatedUserResult`, not `User`, so domain classes
  do not leak back to the controller.

Verification:
- `make unit-test` passed: 4 tests, 0 failures.
- `make it-test` passed: 4 unit tests plus 2 Cucumber/Testcontainers scenarios, 0 failures.

Open problems:
- Maven/JDK still emits warnings about restricted native access and Mockito dynamic agent
  self-attachment; they do not fail the tests.

Suggested next step:
- Continue normalizing remaining skeleton documentation and code style rules as they are
  clarified.

---

## 2026-07-22 - Normalize Lombok Logging Documentation

Summary:
- Confirmed `.agents/knowledge/java.md` documents `@Log4j2` as the service logging
  annotation.
- Updated `lombok.config` documentation comment from `@Slf4j` to `@Log4j2` so the local
  Lombok guidance matches the agent rules.

Decisions:
- Kept `@Log4j2` as the standard Lombok logging annotation for services.

Verification:
- Searched `AGENTS.md`, `.agents`, `docs`, `README.md` and `lombok.config` for logging
  annotation references.

Open problems:
- README still describes the runtime logging backend as SLF4J/Logback, which is a stack
  description rather than a Lombok annotation rule.

Suggested next step:
- Continue normalizing skeleton documentation as additional project conventions are clarified.

---

## 2026-07-22 - Use Controller Request Directly In Create User Use Case

Summary:
- Removed the intermediate `CreateUserInput` record from the create-user flow.
- Changed `CreateUserUseCase` and `CreateUserUseCaseImpl` to receive
  `CreateUserRequest` directly.
- Simplified `UserController` so it passes the request object straight to the use case.
- Updated unit tests and Cucumber steps to create/use `CreateUserRequest`.
- Updated agent documentation so `domain/input` is optional and only used when the request
  DTO is not enough for the use case.

Decisions:
- Kept `CreatedUserResult` as the use-case return type so domain/JPA entities do not leak
  back to controllers.
- Documented the request DTO dependency as an allowed use-case boundary exception for this
  skeleton.

Verification:
- `make unit-test` passed: 4 tests, 0 failures.
- `make it-test` passed: 4 unit tests plus 2 Cucumber/Testcontainers scenarios, 0 failures.

Open problems:
- Maven/JDK still emits warnings about restricted native access and Mockito dynamic agent
  self-attachment; they do not fail the tests.

Suggested next step:
- Continue normalizing skeleton documentation as additional project conventions are clarified.
