# Makefile for Spring Auth Application
# 
# This Makefile provides a comprehensive set of commands for managing the Spring Boot authentication application.
# It includes commands for building, running, and testing the application, as well as managing the PostgreSQL database,
# Docker containers, and other utilities.
#
# Usage: make [command]
#
# For a list of available commands, run: make help
#
# Author: AI Assistant
# Date: 2023

# Variables
APP_NAME = spring-auth
JAVA_VERSION = 21
MAVEN = ./mvnw
DOCKER_COMPOSE = docker-compose
DB_CONTAINER = postgres_advocacia
JAR_FILE = target/$(APP_NAME)-0.0.1-SNAPSHOT.jar
DEFAULT_PORT = 8080

# Help command
.PHONY: help
help:
	@echo "Available commands:"
	@echo ""
	@echo "Application commands:"
	@echo "  help                 - Show this help message"
	@echo "  build                - Build the application"
	@echo "  run                  - Run the application"
	@echo "  run-profile          - Run the application with a specific profile (usage: make run-profile profile=dev)"
	@echo "  run-jar              - Run the application from the JAR file"
	@echo "  test                 - Run all tests"
	@echo "  clean                - Clean the project (remove target directory)"
	@echo "  package              - Package the application as a JAR file"
	@echo "  install              - Install the package into local repository"
	@echo ""
	@echo "Database commands:"
	@echo "  db-start             - Start the PostgreSQL database container"
	@echo "  db-stop              - Stop the PostgreSQL database container"
	@echo "  db-logs              - Show database container logs"
	@echo "  db-shell             - Open a psql shell in the database container"
	@echo "  db-backup            - Create a backup of the database"
	@echo "  db-restore           - Restore the database from a backup (usage: make db-restore file=backup_file.sql)"
	@echo ""
	@echo "Docker commands:"
	@echo "  docker-build         - Build Docker image for the application"
	@echo "  docker-run           - Run the application in a Docker container"
	@echo "  docker-compose-up    - Start all services defined in docker-compose.yml"
	@echo "  docker-compose-down  - Stop all services defined in docker-compose.yml"
	@echo "  update-dockerfile    - Create a proper Dockerfile for the Spring Boot application"
	@echo ""
	@echo "Combined commands:"
	@echo "  start-all            - Start the database and run the application"
	@echo ""
	@echo "Utility commands:"
	@echo "  status               - Check if the application and database are running"
	@echo "  release              - Create a release version of the application"
	@echo "  dependency-updates   - Check for dependency updates"
	@echo "  add-api-docs         - Instructions for adding API documentation"

# Build the application
.PHONY: build
build:
	$(MAVEN) clean compile

# Run the application
.PHONY: run
run:
	$(MAVEN) spring-boot:run

# Run all tests
.PHONY: test
test:
	$(MAVEN) test

# Clean the project
.PHONY: clean
clean:
	$(MAVEN) clean

# Start the PostgreSQL database container
.PHONY: db-start
db-start:
	docker start postgres_advocacia

# Stop the PostgreSQL database container
.PHONY: db-stop
db-stop:
	docker stop postgres_advocacia

# Show database container logs
.PHONY: db-logs
db-logs:
	docker logs -f $(DB_CONTAINER)

# Open a psql shell in the database container
.PHONY: db-shell
db-shell:
	docker exec -it $(DB_CONTAINER) psql -U postgres -d advocacia

# Build Docker image for the application
.PHONY: docker-build
docker-build:
	docker build -t $(APP_NAME) .

# Run the application in a Docker container
.PHONY: docker-run
docker-run:
	docker run -p 8080:8080 $(APP_NAME)

# Start all services defined in docker-compose.yml
.PHONY: docker-compose-up
docker-compose-up:
	$(DOCKER_COMPOSE) up -d

# Stop all services defined in docker-compose.yml
.PHONY: docker-compose-down
docker-compose-down:
	$(DOCKER_COMPOSE) down

# Package the application as a JAR file
.PHONY: package
package:
	$(MAVEN) package -DskipTests

# Install the package into local repository
.PHONY: install
install:
	$(MAVEN) install -DskipTests

# Check for dependency updates
.PHONY: dependency-updates
dependency-updates:
	$(MAVEN) versions:display-dependency-updates

# Run the application with a specific profile
.PHONY: run-profile
run-profile:
	@echo "Running with profile: $(profile)"
	$(MAVEN) spring-boot:run -Dspring-boot.run.profiles=$(profile)

# Run the application from the JAR file
.PHONY: run-jar
run-jar: package
	java -jar $(JAR_FILE)

# Start the database and run the application
.PHONY: start-all
start-all: db-start
	@echo "Starting database and application..."
	@echo "Database should be available at localhost:5432"
	@echo "Application will start on port $(DEFAULT_PORT)"
	$(MAVEN) spring-boot:run

# Create a proper Dockerfile for the Spring Boot application
.PHONY: update-dockerfile
update-dockerfile:
	@echo "Updating Dockerfile for Spring Boot application..."
	@echo "FROM eclipse-temurin:$(JAVA_VERSION)-jdk-alpine" > Dockerfile
	@echo "WORKDIR /app" >> Dockerfile
	@echo "COPY $(JAR_FILE) app.jar" >> Dockerfile
	@echo "EXPOSE $(DEFAULT_PORT)" >> Dockerfile
	@echo "ENTRYPOINT [\"java\", \"-jar\", \"/app/app.jar\"]" >> Dockerfile
	@echo "Dockerfile updated successfully."

# Generate API documentation (if Springdoc is added to the project)
.PHONY: add-api-docs
add-api-docs:
	@echo "Adding Springdoc OpenAPI dependency to pom.xml..."
	@echo "Please add the following dependency to your pom.xml:"
	@echo "<dependency>"
	@echo "    <groupId>org.springdoc</groupId>"
	@echo "    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>"
	@echo "    <version>2.0.2</version>"
	@echo "</dependency>"
	@echo "After adding, rebuild the application and access the API docs at: http://localhost:$(DEFAULT_PORT)/swagger-ui.html"

# Check if the application is running
.PHONY: status
status:
	@echo "Checking if the application is running..."
	@if pgrep -f "$(APP_NAME)" > /dev/null; then \
		echo "Application is running."; \
		echo "Process info:"; \
		ps -ef | grep "$(APP_NAME)" | grep -v grep; \
	else \
		echo "Application is not running."; \
	fi
	@echo "Checking database container status..."
	@if docker ps | grep $(DB_CONTAINER) > /dev/null; then \
		echo "Database container is running."; \
	else \
		echo "Database container is not running."; \
	fi

# Create a release version
.PHONY: release
release:
	@echo "Creating release version..."
	@read -p "Enter release version (e.g., 1.0.0): " version; \
	$(MAVEN) versions:set -DnewVersion=$$version -DgenerateBackupPoms=false && \
	$(MAVEN) clean package -DskipTests && \
	echo "Release version $$version created successfully. JAR file available at $(JAR_FILE)"

# Backup the database
.PHONY: db-backup
db-backup:
	@echo "Creating database backup..."
	@timestamp=$$(date +%Y%m%d_%H%M%S); \
	backup_file="advocacia_backup_$$timestamp.sql"; \
	docker exec $(DB_CONTAINER) pg_dump -U postgres -d advocacia > $$backup_file && \
	echo "Database backup created: $$backup_file"

# Restore database from backup
.PHONY: db-restore
db-restore:
	@echo "Restoring database from backup..."
	@if [ -z "$(file)" ]; then \
		echo "Error: No backup file specified. Usage: make db-restore file=backup_file.sql"; \
	else \
		if [ ! -f "$(file)" ]; then \
			echo "Error: Backup file $(file) not found."; \
		else \
			docker exec -i $(DB_CONTAINER) psql -U postgres -d advocacia < $(file) && \
			echo "Database restored from $(file)"; \
		fi \
	fi
