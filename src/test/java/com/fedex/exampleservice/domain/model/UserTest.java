package com.fedex.exampleservice.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class UserTest {

	@Test
	void createsUserWithRequiredFields() {
		final User user = User.create("Example User", "user@example.com");

		assertNotNull(user.getId());
		assertEquals("Example User", user.getName());
		assertEquals("user@example.com", user.getEmail());
		assertNotNull(user);
	}

}
