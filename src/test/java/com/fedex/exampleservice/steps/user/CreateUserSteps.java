package com.fedex.exampleservice.steps.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fedex.exampleservice.application.usecase.user.create.CreateUserUseCase;
import com.fedex.exampleservice.infrastructure.web.dto.CreateUserRequest;
import lombok.NonNull;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

public class CreateUserSteps {

	private final JdbcTemplate jdbcTemplate;

	private final CreateUserUseCase createUserUseCase;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private final HttpClient httpClient = HttpClient.newHttpClient();

	@LocalServerPort
	private int port;

	private Map<String, String> request;

	private int responseStatus;

	private Map<String, Object> responseBody;

	public CreateUserSteps(@NonNull final JdbcTemplate jdbcTemplate,
			@NonNull final CreateUserUseCase createUserUseCase) {
		this.jdbcTemplate = jdbcTemplate;
		this.createUserUseCase = createUserUseCase;
	}

	@Before
	public void cleanDatabase() {
		this.jdbcTemplate.update("DELETE FROM users");
	}

	@Given("a create user request with name {string} and email {string}")
	public void aCreateUserRequestWithNameAndEmail(@NonNull final String name, @NonNull final String email) {
		this.request = Map.of("name", name, "email", email);
	}

	@Given("a user already exists with email {string}")
	public void aUserAlreadyExistsWithEmail(@NonNull final String email) {
		final CreateUserRequest request = new CreateUserRequest().name("Existing User").email(email);
		this.createUserUseCase.create(request);
	}

	@When("the client creates the user")
	public void theClientCreatesTheUser() throws Exception {
		postUser(this.request);
	}

	@When("the client creates another user with email {string}")
	public void theClientCreatesAnotherUserWithEmail(@NonNull final String email) throws Exception {
		postUser(Map.of("name", "Another User", "email", email));
	}

	@Then("the user is persisted")
	public void theUserIsPersisted() {
		Integer count = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE email = ?", Integer.class,
				this.request.get("email"));
		assertEquals(1, count);
	}

	@Then("the response contains the created user")
	public void theResponseContainsTheCreatedUser() {
		assertEquals(201, this.responseStatus);
		assertNotNull(this.responseBody);
		assertNotNull(this.responseBody.get("id"));
		assertEquals(this.request.get("name"), this.responseBody.get("name"));
		assertEquals(this.request.get("email"), this.responseBody.get("email"));
		assertNotNull(this.responseBody.get("createdDate"));
	}

	@Then("the create user request is rejected as a conflict")
	public void theCreateUserRequestIsRejectedAsAConflict() {
		assertEquals(409, this.responseStatus);
		Integer count = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE email = ?", Integer.class,
				"user@example.com");
		assertEquals(1, count);
		assertTrue(this.responseBody != null && !this.responseBody.isEmpty());
	}

	private void postUser(Map<String, String> body) throws Exception {
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create("http://localhost:" + this.port + "/v1/users"))
			.header("Content-Type", "application/json")
			.header("Authorization", basicAuthHeader())
			.POST(HttpRequest.BodyPublishers.ofString(this.objectMapper.writeValueAsString(body)))
			.build();

		HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		this.responseStatus = response.statusCode();
		this.responseBody = readBody(response.body());
	}

	private Map<String, Object> readBody(String body) throws JsonProcessingException {
		if (body == null || body.isBlank()) {
			return Map.of();
		}
		return this.objectMapper.readValue(body, new TypeReference<>() {
		});
	}

	private String basicAuthHeader() {
		String value = Base64.getEncoder().encodeToString("test:test".getBytes(StandardCharsets.UTF_8));
		return "Basic " + value;
	}

}
