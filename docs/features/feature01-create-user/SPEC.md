# Create User

> Status: in-progress
> Owner: template
> Related: `docs/openapi.yaml`

## Problem

The template needs a small, business-agnostic example feature that demonstrates the expected
service structure from specification through REST contract, domain model, use case,
persistence, and tests.

Creating a user is intentionally generic: it gives future projects a simple reference for
adding a persisted resource without embedding project-specific business rules.

## Scope

- In scope: create a user with `name` and unique `email`.
- In scope: persist the user in MySQL through JPA and Liquibase.
- In scope: expose `POST /v1/users` through OpenAPI and a thin controller.
- In scope: return the created user id, name, email and creation timestamp.
- Out of scope: authentication/authorization policy beyond existing service security.
- Out of scope: update, delete, list or retrieve user operations.
- Out of scope: password management and user login.

## Behavior

1. A client submits a create-user request with `name` and `email`.
2. The controller delegates to the create-user use case.
3. The use case validates that no existing user has the same email.
4. The domain model creates a new user with a generated id.
5. The repository persists the user.
6. The API returns `201 Created` with the created user representation.
7. If the email is already used, the request fails with a conflict.
8. If the request payload is invalid, validation fails before persistence.

## Acceptance criteria

- [ ] Given a valid name and unused email, when the user is created, then the response is
      `201 Created` and contains id, name, email and created date.
- [ ] Given an email that already exists, when the user is created, then the operation fails
      with a conflict and no duplicate row is created.
- [ ] Given an invalid name or email, when the user is created, then validation rejects the
      request.
- [ ] Observability: normal request logging and existing HTTP/Actuator metrics are enough
      for this template feature.

## Companion docs

- `DATABASE.md` — user table and changelog.
- `API.md` — create-user REST endpoint.

## Rollout & rollback

Rollout is the normal application deployment with the Liquibase changelog. Rollback for the
template is to remove the endpoint/use case and drop the `users` table only in environments
where no real data depends on it.

## Open questions

- None.
