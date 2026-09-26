.PHONY: help install dev build test lint clean docker-build docker-up docker-down

# Default target
help: ## Show this help message
	@echo "TestPilot AI - Available Commands:"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

# =============================================================================
# Development
# =============================================================================

install: ## Install all dependencies
	cd backend && mvn clean install -DskipTests
	cd frontend && npm install

dev: ## Start development servers
	@echo "Starting backend..."
	cd backend && mvn spring-boot:run &
	@echo "Starting frontend..."
	cd frontend && npm run dev

dev-backend: ## Start backend only
	cd backend && mvn spring-boot:run

dev-frontend: ## Start frontend only
	cd frontend && npm run dev

# =============================================================================
# Build
# =============================================================================

build: ## Build all projects
	cd backend && mvn clean package -DskipTests
	cd frontend && npm run build

build-backend: ## Build backend only
	cd backend && mvn clean package -DskipTests

build-frontend: ## Build frontend only
	cd frontend && npm run build

# =============================================================================
# Testing
# =============================================================================

test: ## Run all tests
	cd backend && mvn test

test-backend: ## Run backend tests
	cd backend && mvn test

test-frontend: ## Run frontend tests
	cd frontend && npm run test

test-coverage: ## Run tests with coverage
	cd backend && mvn test jacoco:report

# =============================================================================
# Code Quality
# =============================================================================

lint: ## Run linters
	cd frontend && npm run lint

lint-backend: ## Run backend linter
	cd backend && mvn checkstyle:check

lint-fix: ## Fix linting issues
	cd frontend && npm run lint -- --fix

format: ## Format code
	cd frontend && npm run format

# =============================================================================
# Docker
# =============================================================================

docker-build: ## Build Docker images
	docker-compose build

docker-up: ## Start Docker services
	docker-compose up -d

docker-down: ## Stop Docker services
	docker-compose down

docker-logs: ## View Docker logs
	docker-compose logs -f

docker-reset: ## Reset Docker environment
	docker-compose down -v
	docker-compose up -d

# =============================================================================
# Database
# =============================================================================

db-init: ## Initialize database
	cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev

db-reset: ## Reset database
	mysql -u root -proot -e "DROP DATABASE IF EXISTS testpilot; CREATE DATABASE testpilot;"

db-seed: ## Seed database with demo data
	mysql -u root -proot testpilot < backend/src/main/resources/data.sql

# =============================================================================
# Utilities
# =============================================================================

clean: ## Clean build artifacts
	cd backend && mvn clean
	cd frontend && rm -rf dist node_modules/.vite

clean-all: ## Clean everything
	cd backend && mvn clean
	cd frontend && rm -rf dist node_modules

logs: ## View application logs
	tail -f backend/logs/testpilot.log

status: ## Show project status
	@echo "=== Backend Status ==="
	@cd backend && mvn -q validate
	@echo "=== Frontend Status ==="
	@cd frontend && npm ls --depth=0
	@echo "=== Docker Status ==="
	@docker-compose ps

# =============================================================================
# IDE
# =============================================================================

setup: ## Setup development environment
	@echo "Setting up development environment..."
	cp .env.example .env
	cd backend && mvn clean install -DskipTests
	cd frontend && npm install
	@echo "Setup complete! Run 'make dev' to start development."

idea: ## Generate IntelliJ IDEA project files
	cd backend && mvn idea:idea

vscode: ## Setup VS Code workspace
	@echo "VS Code workspace configured"
