package com.fedex.exampleservice.domain.event;

import java.time.Instant;

public interface DomainEvent<T> {

    String eventType();

    T eventId();

    String correlationId();

    Instant occurredOn();
}
