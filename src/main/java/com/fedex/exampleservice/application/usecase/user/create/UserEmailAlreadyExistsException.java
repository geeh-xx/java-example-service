package com.fedex.exampleservice.application.usecase.user.create;

import lombok.NonNull;

public class UserEmailAlreadyExistsException extends RuntimeException {

	public UserEmailAlreadyExistsException(@NonNull final String email) {
		super("User email already exists: " + email);
	}

}
