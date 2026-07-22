package com.fedex.exampleservice.domain.event;

import java.time.Instant;
import java.util.UUID;

public record UserCreatedEvent(UUID userId, String name,
                               String email, Instant occurredOn) implements DomainEvent<UUID> {

    @Override
    public String eventType() {
        return "";
    }

    @Override
    public UUID eventId() {
        return userId;
    }

    @Override
    public String correlationId() {
        return "";
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }
}
