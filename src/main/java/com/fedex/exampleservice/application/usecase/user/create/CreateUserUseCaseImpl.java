package com.fedex.exampleservice.application.usecase.user.create;

import com.fedex.exampleservice.application.usecase.user.UserRepository;
import com.fedex.exampleservice.domain.model.User;
import com.fedex.exampleservice.infrastructure.web.dto.CreateUserRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Log4j2
public class CreateUserUseCaseImpl implements CreateUserUseCase {

	private final UserRepository userRepository;

	@Override
	@Transactional
	public CreatedUserResult create(@NonNull final CreateUserRequest request) {
		this.userRepository.findByEmail(request.getEmail()).ifPresent(_ -> {
			throw new UserEmailAlreadyExistsException(request.getEmail());
		});

		final User user = User.create(request.getName(), request.getEmail());
		final User savedUser = this.userRepository.save(user);
		final Instant createdDate = Instant.now();
		return new CreatedUserResult(savedUser.getId(), request.getName(), request.getEmail(), createdDate);
	}

}
