# ========================================
# Maven
# ========================================
clean:
	mvn clean

install: clean
	mvn install -DskipTests

unit-test: clean
	mvn test

coverage: clean
	mvn verify
	open target/site/jacoco/index.html

it-test:
	mvn clean verify -P cucumber

run: install
	mvn spring-boot:run

# ========================================
# Infrastructure
# ========================================
infra-up:
	docker-compose up -d

infra-down:
	docker-compose down

infra-reset:
	docker compose down --volumes && docker compose up -d --build