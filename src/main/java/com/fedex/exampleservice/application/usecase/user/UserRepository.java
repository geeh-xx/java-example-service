package com.fedex.exampleservice.application.usecase.user;

import java.util.Optional;
import java.util.UUID;

import com.fedex.exampleservice.domain.model.User;
import lombok.NonNull;

public interface UserRepository {

	Optional<User> findByEmail(@NonNull final String email);

	User save(@NonNull final User user);

	Optional<User> findById(@NonNull final UUID id);

}
