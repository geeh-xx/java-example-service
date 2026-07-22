CREATE TABLE users (
    id BINARY(16) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(255) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);
