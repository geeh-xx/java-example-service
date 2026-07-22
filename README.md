# integration-service - README

project for integration external sistem to f360

# 1) Technologies and Architecture


- **Languages & Build**: Java 25, Maven (multi-module reactor), Using BDD and domain events
- **Dependencies**:
    - Spring Boot 4 (AMQP, Data JPA, spring-boot-starter-liquibase, Test)
    - Lombok (boilerplate reduction)
    - None
- **Database**: MySQL (managed schema via Liquibase)
- **Observability**:
    - Spring Boot Actuator (health checks, metrics, endpoints)
    - OpenTelemetry SDK (traces, metrics, logs)
    - OpenTelemetry OTLP Exporter (telemetry data export)
    - OpenTelemetry Spring Boot Starter (auto-instrumentation)
- **Testing**:
    - Cucumber 7 (JUnit Platform engine)
    - Testcontainers (MySQL and RabbitMQ for integration tests)
    - JUnit 5
- **Modules**:
    - `domain`: Entities and core domain models
    - `application`: Use cases and commands (e.g., `RegisterEmailReceivedCommand`)
    - `infrastructure`: Adapters (AMQP consumers/producers, persistence, Liquibase), application wiring and configuration
    - `test`: Cucumber integration tests and supportive test configuration

### Project structure:

```text
com.fedex.exampleservice
 ├── domain
 │    ├── model
 │    ├── event
 │
 ├── application
 │    ├── usecase
 │    ├── dto
 │    ├── mapper
 │
 ├── infrastructure
 │    ├── exception
 │    ├── configuration
 │    ├── persistence
 │    │     ├── repository
 │    ├── messaging
 │    │     ├── producer
 │    │     ├── consumer
 │    ├── web
 │    │     ├── controller
 │    │     ├── request
 │    │     ├── response
 │
 ├── shared
 │    ├── util
 │    ├── constants
```

Responsibilities:
- domain: domain entities(jpa entities), interfaces, rules, events
- application: use cases + DTOs + mappers
- infrastructure: Spring configs, web controllers, persistence repos, integrations, exceptions
- shared: utils and constants

### High-level flow:
- Messages follow a predefined cloud event schema ([Cloud Event Docs](https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/formats/json-format.md))
- Payloads are mapped  into application commands and persisted (JPA repositories), with outbox pattern records created.
- Liquibase manages the database schema; MySQL is used both locally and in tests (via Testcontainers).

# 3) Observability

### OpenTelemetry and Actuator:
- OpenTelemetry is configured to auto-instrument Spring components and export telemetry data via OTLP.
- Spring Boot Actuator provides health checks and metrics endpoints.

### MDC (Mapped Diagnostic Context) for messages:
- Each incoming message is assigned a unique eventId (UUID) and source stored in MDC for consistent logging.
- This allows tracing logs related to a specific message processing flow.

## Logging:
- The project uses SLF4J with Logback for logging.
- Logs include MDC values (eventId, source) for better traceability.
- Logback configuration can be found in `src/main/resources/logback-spring.xml`.

# 3) Prerequisites

### Install/configure the following on your machine:
- Java 25 (JDK)
- Maven 
- Docker Desktop (or Docker Engine) to run Testcontainers
- Make (optional but recommended for convenience)

### Optional (for local infrastructure via Docker Compose):
- Docker Compose

# 4) Import into IntelliJ IDEA and run (local profile)

- Import the project:
    - IntelliJ IDEA > New > Project from Existing Sources… > select the repository root (`pom.xml`).
    - Choose Maven model import; keep default profiles unchecked for now.
- Configure JDK/annotation processors:
    - Project SDK: set to JDK 25 (File > Project Structure > Project).
    - Enable annotation processing (MapStruct/Lombok): Settings > Build, Execution, Deployment > Compiler > Annotation Processors > Enable.
- Install extra plugins:
    - Lombok plugin (for Lombok annotations support).
    - Cucumber for Java (for BDD feature files support).
- Select main application class:
    - Run/Debug Configurations > + Spring Boot > Main class
- Start the docker containers for local infra (MySQL, RabbitMQ):
    - Execute the following command in a terminal: `docker compose up -d`
    - From repo root: `make infra-up` (starts only MySQL as defined in `docker-compose.yml`).
- Activate the local profile and run:
    - In the same Run configuration, set VM options (or Environment):
    - `-Dspring.profiles.active=local`
    - Ensure local dependencies are available:
        - Option A (recommended): run local infra via Docker Compose
            - From repo root: `make infra-up` (starts MySQL and RabbitMQ as defined in `docker-compose.yml`).
        - Option B: point `application-local.yml` to your own MySQL/RabbitMQ endpoints.
    - Click Run. Liquibase will apply migrations automatically on the configured database.

### Tips:
- If Lombok annotations are not recognized, install the Lombok plugin and keep annotation processing enabled.

# 5) Documentation

### Async API (QUEUES) :
Queues documentation (AsyncAPI) is auto-generated via Springwolf:
- Access the docs at [local documentation](http://localhost:8080/springwolf/asyncapi-ui.html) when the app is running locally.

### Health Checks:
- Access health checks at [local health](http://localhost:8080/actuator/)

# 6) Tests
The project includes unit tests and BDD-style integration tests.

### Unit Tests:
- The minimum coverage should be 80% for unit tests.
- Use Junit 5, Mockito for mocking dependencies, and Instancio for generating test data.
- Run unit tests with:
    - `mvn test` or `make unit-test`
- Run make coverage to generate a JaCoCo report:
    -  `make coverage`

### Integration Tests:
- Every use case should have at least minimum one Cucumber scenario.
- The scenarios are located in `test/src/test/resources/features/` and need to be written in Gherkin syntax.
    - [Gherkin reference](https://cucumber.io/docs/bdd/better-gherkin)
- Step definitions and supportive test code are in `test/src/test/java/`.
- Integration tests use Testcontainers to spin up MySQL and RabbitMQ instances.
- Run integration tests with:
    - `mvn verify -P cucumber` or `make it-test`
- Reports are generated in `test/target/cucumber-reports/` (HTML, JSON, JUnit XML).

### Notes:
- Integration tests use Testcontainers; Docker must be running.
- Integration tests are isolated under the `cucumber` Maven profile with Failsafe and do not run with unit tests.

### Reports and artifacts:
- Cucumber HTML: `test/target/cucumber-reports/ExtentReports.html`
- Cucumber JSON/JUnit (if configured by plugins): under `test/target/cucumber-reports/`


# 7) Make targets and how to run integration tests

### The project ships a `Makefile` with common tasks:

- `make clean`
    - Runs `mvn clean` across the reactor.

- `make install`
    - Cleans and builds all modules without running tests.

- `make unit-test`
    - Runs unit tests (`mvn test`).

- `make coverage`
    - Runs unit tests and produces a JaCoCo report.

- `make run`
    - Builds and starts the Spring Boot application (`spring-boot:run`).

- `make infra-up` / `make infra-down` / `make infra-reset`
    - Manage local infra via `docker-compose` (if you choose to run MySQL/RabbitMQ locally).

- `make integration-test`
    - Execute Cucumber integration tests with Testcontainers:
    - Internally runs: `mvn clean verify -P integration-test`
    - Opens the HTML report at `test/target/cucumber-reports/ExtentReports.html` (macOS `open`).

# 8) Deploy in Dev environment

Be sure you are connected to the VPN and have access to the TeamCity server.

Go to the [team city profile page](https://teamcity.ppsystem.net/profile.html?item=accessTokens) and generate a new token.

Add token to your .env file:

```bash
TEAM_CITY_TOKEN=your_generated_token_here
```

Commit and push your changes.

Then run the following command to trigger the deployment:

- `make deploy`
    - Start a deployment for the current branch.

- `make deploy BRANCH=branch_name`
    - Start a deployment for the specified branch (e.g., `main` or `develop`).

- `make deploy BRANCH=branch_name TOKEN=xxxx`
    - Start a deployment for the specified branch and token.

### Manual Maven commands (without Make):
- Run integration tests only:
    - From repo root: `mvn -q -P integration-test verify`
    - Or from `test/` module: `../mvnw -q -P integration-test verify`
