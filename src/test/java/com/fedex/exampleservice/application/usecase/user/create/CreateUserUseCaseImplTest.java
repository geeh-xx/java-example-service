package com.fedex.exampleservice.application.usecase.user.create;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.fedex.exampleservice.application.usecase.user.UserRepository;
import com.fedex.exampleservice.domain.model.User;
import com.fedex.exampleservice.infrastructure.web.dto.CreateUserRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CreateUserUseCaseImplTest {

	private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);

	private final CreateUserUseCase useCase = new CreateUserUseCaseImpl(this.userRepository);

	@Test
	void createsUserWhenEmailIsAvailable() {
		when(this.userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
		when(this.userRepository.save(any(User.class))).thenAnswer((invocation) -> invocation.getArgument(0));

		final CreateUserRequest request = new CreateUserRequest().name("Example User").email("user@example.com");
		final CreatedUserResult result = this.useCase.create(request);

		assertEquals("Example User", result.name());
		assertEquals("user@example.com", result.email());
		final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		verify(this.userRepository).save(userCaptor.capture());
		assertEquals(result.id(), userCaptor.getValue().getId());
	}

	@Test
	void rejectsDuplicatedEmail() {
		final User existingUser = User.create("Existing User", "user@example.com");
		when(this.userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));

		final CreateUserRequest request = new CreateUserRequest().name("Example User").email("user@example.com");
		assertThrows(UserEmailAlreadyExistsException.class,
				() -> this.useCase.create(request));

		verify(this.userRepository, never()).save(any(User.class));
	}

}
