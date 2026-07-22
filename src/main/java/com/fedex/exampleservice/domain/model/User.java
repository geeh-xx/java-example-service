package com.fedex.exampleservice.domain.model;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.uuid.Generators;
import com.fedex.exampleservice.domain.AggregateRoot;
import com.fedex.exampleservice.domain.event.UserCreatedEvent;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Access(AccessType.FIELD)
public class User extends AggregateRoot<UUID> {

	@Id
	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "name", nullable = false, length = 120)
	private String name;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	public static User create(@NonNull final String name, @NonNull final String email) {
		final var user = new User();
		user.id = Generators.timeBasedEpochGenerator().generate();
		user.name = name;
		user.email = email;

		user.registerEvent(new UserCreatedEvent(user.id, user.name, user.email, Instant.now()));
		return user;
	}

	@Override
	public UUID getId() {
		return this.id;
	}

}
