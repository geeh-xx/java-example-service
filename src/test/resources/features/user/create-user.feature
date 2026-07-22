Feature: Create user

  Scenario: Create a user with a valid request
    Given a create user request with name "Example User" and email "user@example.com"
    When the client creates the user
    Then the user is persisted
    And the response contains the created user

  Scenario: Reject a duplicated email
    Given a user already exists with email "user@example.com"
    When the client creates another user with email "user@example.com"
    Then the create user request is rejected as a conflict
