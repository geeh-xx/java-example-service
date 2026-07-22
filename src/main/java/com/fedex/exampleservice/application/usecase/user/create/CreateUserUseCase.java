package com.fedex.exampleservice.application.usecase.user.create;

import com.fedex.exampleservice.infrastructure.web.dto.CreateUserRequest;
import lombok.NonNull;

public interface CreateUserUseCase {

	CreatedUserResult create(@NonNull final CreateUserRequest request);

}
