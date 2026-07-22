# Security

## Service auth (inbound)

- Spring Security with **OAuth2 Resource Server (JWT)** — configuration in
  `infrastructure/security/WebSecurityConfig`.
- Every REST endpoint (except Actuator health and the OAuth provider callback) requires a
  valid JWT from the platform issuer.
- Never bypass or disable the security filter chain in production profiles; test profiles
  may relax it only inside the Cucumber test configuration.

## Provider credentials & tokens (outbound)

- OAuth client ids/secrets per provider come from environment variables / deployment
  secrets — never from committed files.
- Access/refresh tokens are **encrypted at rest** (application-level crypto before persist)
  and decrypted only inside the provider gateway path.
- Rotated refresh tokens (single-use, e.g. Xero) are persisted immediately after refresh —
  losing a rotated token disconnects the tenant.
- Tokens never appear in logs, traces, events, or API responses (see `observability.md`).

## General

- Follow SOLID boundaries for security integrations: expose authentication/authorization
  needs as application-owned interfaces, implemented by infrastructure adapters. Do not
  inject Spring Security concrete components directly into unrelated configuration,
  use cases, controllers, or domain code.
- `.env` files are local-only and gitignored — never commit credentials.
- No secrets in Liquibase changelogs, test fixtures, or Cucumber features.
- CORS: this service has no browser frontend except OAuth redirects — keep CORS closed.
- Dependency upgrades with CVE fixes take priority; do not pin vulnerable versions.
