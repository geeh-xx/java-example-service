# Architecture

Single Maven module, hexagonal by **package** discipline. Package root:
`com.{your-domain}.integrationservice`.

## Layers and dependency direction

```
infrastructure  ─►  application  ─►  domain
        └──────────────►  shared  ◄──────────┘
```

- `domain` must not import anything from `infrastructure`.
- `application` generally stays independent from `infrastructure`, except use-case
  boundaries may receive request DTOs passed by controllers.
- Provider SDKs and HTTP client libraries are **infrastructure-only**
  (`infrastructure/web/client/<provider>/`); `application` sees only interfaces.
- `shared` holds cross-cutting utils/constants with no Spring beans and no state.

## Use-case pattern

- One sub-package per feature: `application/usecase/<domain>/<feature>/`
  containing `XxxUseCase` (interface) + `XxxUseCaseImpl`.
- Use cases may receive the request object passed by the controller. Create a separate
  `domain/input` type only when the request DTO is not enough for the use case.
- Use cases return an output/command-result type — never a domain model/JPA entity to the
  web layer, never a provider SDK type anywhere.
- Orchestration only: validation → domain behavior → repository/gateway calls → output.
  Domain behavior registers domain events on an `AggregateRoot<ID>`; infrastructure listeners
  convert those events to outbox messages. `@Transactional` goes on the Impl method.
- Controllers, consumers and schedulers are **thin**: map input, call use case, map output.
- Required method parameters use Lombok `@NonNull`; read-only parameters, locals and fields
  use Java `final`.

## Interfaces owned by application, implemented by infrastructure

- Repositories: interface next to the use cases that need it (or `persistence/repository`
  Spring Data interfaces when trivial); custom logic goes through an adapter impl in
  `infrastructure/persistence/repository`.
- Provider gateways: interface in `application` (no SDK/Feign/HTTP types in the signature);
  implementation in `infrastructure/web/client/<provider>/`.
- `MessagePublisher`: interface in `application`; outbox implementation in
  `infrastructure/messaging/producer`.

## Where new things go

| New thing | Location                                                                                                                                                                  |
|---|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| New external provider (e.g. Xero) | `infrastructure/web/client/<provider>/` + gateway interface in application + rules in `.agents/integrations.md`                                                                   |
| New queue consumer | `infrastructure/messaging/consumer/` (delegates to a use case) + versioned RabbitMQ topology changeset in `src/main/resources/messaging/messaging-topology-changelog.yml` |
| New REST endpoint | contract in `docs/openapi.yaml` → controller in `infrastructure/web/controller/`                                                                                          |
| New scheduled job | `infrastructure/` scheduled class with `@SchedulerLock`, calls a use case                                                                                                 |
| New entity/table | `domain/model` + Liquibase changelog; use `AggregateRoot<ID>` for aggregate roots and `BaseEntity<ID>` for non-root persistent entities                                  |
| New domain event | `domain/event`                                                                                                                                                            |

Do **not** create new top-level packages or convert the project to multi-module without
explicit approval.
