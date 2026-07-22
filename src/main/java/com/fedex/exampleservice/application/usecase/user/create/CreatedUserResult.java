package com.fedex.exampleservice.application.usecase.user.create;

import java.time.Instant;
import java.util.UUID;

public record CreatedUserResult(UUID id, String name, String email, Instant createdDate) {
}
