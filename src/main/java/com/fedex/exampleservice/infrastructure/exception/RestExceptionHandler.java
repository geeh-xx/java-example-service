package com.fedex.exampleservice.infrastructure.exception;

import java.util.Map;

import com.fedex.exampleservice.application.usecase.user.create.UserEmailAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

	@ExceptionHandler(UserEmailAlreadyExistsException.class)
	ResponseEntity<Map<String, String>> handleUserEmailAlreadyExists(UserEmailAlreadyExistsException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", exception.getMessage()));
	}

}
