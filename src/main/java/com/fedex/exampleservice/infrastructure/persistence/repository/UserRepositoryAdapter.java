package com.fedex.exampleservice.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import com.fedex.exampleservice.application.usecase.user.UserRepository;
import com.fedex.exampleservice.domain.model.User;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

	private final JpaUserRepository jpaUserRepository;

	@Override
	public Optional<User> findByEmail(@NonNull final String email) {
		return this.jpaUserRepository.findByEmail(email);
	}

	@Override
	public User save(@NonNull final User user) {
		return this.jpaUserRepository.save(user);
	}

	@Override
	public Optional<User> findById(@NonNull final UUID id) {
		return this.jpaUserRepository.findById(id);
	}

}
