package com.fedex.exampleservice.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import com.fedex.exampleservice.domain.model.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaUserRepository extends JpaRepository<User, UUID> {

	Optional<User> findByEmail(@NonNull final String email);

}
