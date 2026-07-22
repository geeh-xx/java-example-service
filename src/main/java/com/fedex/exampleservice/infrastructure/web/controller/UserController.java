package com.fedex.exampleservice.infrastructure.web.controller;

import java.time.ZoneOffset;

import com.fedex.exampleservice.application.usecase.user.create.CreateUserUseCase;
import com.fedex.exampleservice.application.usecase.user.create.CreatedUserResult;
import com.fedex.exampleservice.infrastructure.web.api.UsersApi;
import com.fedex.exampleservice.infrastructure.web.dto.CreateUserRequest;
import com.fedex.exampleservice.infrastructure.web.dto.UserResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements UsersApi {

	private final CreateUserUseCase createUserUseCase;

	@Override
	public ResponseEntity<UserResponse> createUser(@NonNull final CreateUserRequest createUserRequest) {
		CreatedUserResult result = this.createUserUseCase.create(createUserRequest);

		UserResponse response = new UserResponse()
			.id(result.id())
			.name(result.name())
			.email(result.email())
			.createdDate(result.createdDate().atOffset(ZoneOffset.UTC));

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

}
