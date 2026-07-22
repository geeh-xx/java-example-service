# Create User - API

## Endpoint

`POST /v1/users`

Request:

```json
{
  "name": "Example User",
  "email": "user@example.com"
}
```

Responses:

- `201 Created` with the created user.
- `400 Bad Request` for validation errors.
- `409 Conflict` when the email is already registered.
