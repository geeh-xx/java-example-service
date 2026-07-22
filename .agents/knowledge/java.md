# Java / Spring style

## Language — Java 25

- Records for DTOs, inputs, outputs, CloudEvent payloads and value objects.
- Sealed interfaces for closed hierarchies (domain events, provider results, statuses).
- **Inner/nested classes are PROHIBITED** — every class, enum, record and interface lives
  in its own top-level file. Sealed hierarchies list their variants in `permits` and each
  variant is a top-level type in the same package (e.g. `RefreshResult` permits
  `TokenRefreshed`, `RefreshRejected`). Enums owned by an entity are top-level too
  (e.g. `OutboxMessageStatus`, not `OutboxMessage.Status`).
- Pattern matching (`switch` over sealed types) instead of `instanceof` chains.
- `Optional<T>` or validated `@NonNull` instead of raw `null` returns.
- Required method parameters MUST be annotated with Lombok `@NonNull`.
- Parameters, local variables and fields that are not reassigned MUST be declared `final`.
- Virtual threads are enabled for provider IO — never synchronize on shared monitors inside
  provider calls (pinning); prefer `ReentrantLock` when locking is unavoidable.

## Spring

- Constructor injection only (`@RequiredArgsConstructor`); no field `@Autowired`.
- Service classes use `@Service`, `@RequiredArgsConstructor` and `@Log4j2`.
- Do not use `@Validated` on services.
- `@Transactional` on implementation methods, never on interfaces.
- Do not use `@Valid` on method parameters. Validate semantic input constraints in records
  and enforce required references with Lombok `@NonNull`.
- One concern per `@Configuration` class (`infrastructure/configuration/...`).
- Properties bound via `@ConfigurationProperties` records — no `@Value` scattering.
- No Spring types in `domain` or `application` (except stereotypes explicitly allowed by
  the existing codebase — follow what is already there).

## Lombok (`lombok.config` applies)

- Allowed: `@RequiredArgsConstructor`, `@Getter`, `@Builder`, `@Log4j2`, `@NonNull`,
  `@NoArgsConstructor(access = AccessLevel.PROTECTED)` for JPA entities.
- Forbidden: `@Data` on JPA entities, `@Setter` on aggregates (mutate via behavior methods),
  `@SneakyThrows` in production code.

## General

- Follow SOLID: depend on narrow interfaces/ports at layer boundaries, keep implementations
  behind infrastructure adapters, and avoid injecting concrete components when a role-based
  contract is the dependency the caller actually needs.
- No business logic in mappers (pure transformation), controllers or consumers.
- Fail fast: validate inputs at the use-case boundary (`jakarta.validation` on input records).
- Exceptions live under `infrastructure/exception`, organized by functional domain
  (`connection`, `xero`, `token`, …) and by level (`level/NotFoundException`,
  `level/ConflictException`, `level/BadGatewayException`, …). Concrete exceptions extend
  the level base class; no raw `RuntimeException`.
- No dead code, no commented-out code, no TODO without a ticket/spec reference.
- Use Lombok `@NonNull` on required method arguments and Java `final` for read-only
  parameters, local variables and fields.
- Do not use `@NonNull` as a substitute for domain validation; keep semantic validation in
  input records/use cases and use `@NonNull` for null-contract enforcement.
