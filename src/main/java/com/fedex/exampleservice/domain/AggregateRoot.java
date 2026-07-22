package com.fedex.exampleservice.domain;

import com.fedex.exampleservice.domain.event.DomainEvent;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Base for aggregate roots: behavior methods register the domain events they raise;
 * infrastructure listeners convert them to outbox messages in the same transaction.
 */
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AggregateRoot<ID> extends BaseEntity<ID> {

    @Transient
    private final transient List<DomainEvent<?>> domainEvents = new ArrayList<>();

    protected <T extends DomainEvent<?>> T registerEvent(@NonNull final T event) {
        domainEvents.add(event);
        return event;
    }

    public Collection<DomainEvent<?>> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    @DomainEvents
    protected Collection<DomainEvent<?>> domainEvents() {
        return getDomainEvents();
    }

    @AfterDomainEventPublication
    protected void clearDomainEvents() {
        domainEvents.clear();
    }
}
