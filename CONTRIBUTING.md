# Contributing to TestPilot AI

Thank you for your interest in contributing to TestPilot AI! This document provides guidelines and information for contributors.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Contributing Guidelines](#contributing-guidelines)
- [Pull Request Process](#pull-request-process)
- [Coding Standards](#coding-standards)
- [Testing](#testing)
- [Documentation](#documentation)

## Code of Conduct

We expect all contributors to follow these standards:

- Be respectful and inclusive
- Focus on constructive feedback
- Help create a welcoming environment
- Follow the project's coding standards

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+
- Redis 7+
- Git

### Fork and Clone

```bash
# Fork the repository on GitHub
git clone https://github.com/your-username/testpilot-ai.git
cd testpilot-ai
git remote add upstream https://github.com/yuzhou602/testpilot-ai.git
```

## Development Setup

### Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

### Docker

```bash
docker-compose up -d
```

## Contributing Guidelines

### Branch Naming

- `feature/description` - New features
- `bugfix/description` - Bug fixes
- `hotfix/description` - Critical fixes
- `docs/description` - Documentation changes
- `refactor/description` - Code refactoring

### Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add new test execution endpoint
fix: resolve agent state machine issue
docs: update API documentation
refactor: improve error handling
test: add unit tests for BugService
```

### Code Style

#### Java

- Use 4 spaces for indentation
- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use Lombok for boilerplate reduction
- Add Javadoc for public methods

#### TypeScript/Vue

- Use 2 spaces for indentation
- Follow [Vue Style Guide](https://vuejs.org/style-guide/)
- Use Composition API with `<script setup>`
- Add TypeScript types for all props and events

## Pull Request Process

### 1. Create a Branch

```bash
git checkout -b feature/your-feature-name
```

### 2. Make Changes

- Write clean, maintainable code
- Add or update tests as needed
- Update documentation if necessary

### 3. Test Your Changes

```bash
# Backend tests
cd backend
mvn test

# Frontend tests
cd frontend
npm run test
```

### 4. Commit Your Changes

```bash
git add .
git commit -m "feat: add your feature description"
```

### 5. Push and Create PR

```bash
git push origin feature/your-feature-name
```

Then create a Pull Request on GitHub with:

- Clear title and description
- Reference any related issues
- Include screenshots for UI changes
- Ensure CI passes

## Coding Standards

### Java Code Style

```java
// Use descriptive names
public class AgentExecutor {

    // Inject dependencies via constructor
    private final AgentToolRegistry toolRegistry;

    // Use proper access modifiers
    public void executeTask(Long taskId) {
        // Implementation
    }

    // Add Javadoc for public methods
    /**
     * Executes an agent task asynchronously.
     *
     * @param taskId the ID of the task to execute
     */
    private void doExecute(Long taskId) {
        // Implementation
    }
}
```

### Vue Component Style

```vue
<template>
  <div class="component">
    <!-- Use semantic HTML -->
  </div>
</template>

<script setup lang="ts">
// Use Composition API
import { ref, computed } from 'vue'

// Define props with types
interface Props {
  taskId: number
  status: string
}

const props = defineProps<Props>()

// Use computed for derived state
const statusColor = computed(() => {
  return props.status === 'COMPLETED' ? 'green' : 'red'
})
</script>

<style scoped>
/* Use scoped styles */
.component {
  padding: 16px;
}
</style>
```

## Testing

### Unit Tests

- Write tests for all new functionality
- Maintain >80% code coverage
- Use descriptive test names
- Follow AAA pattern (Arrange, Act, Assert)

```java
@Test
void shouldCreateBugSuccessfully() {
    // Arrange
    TestBug bug = createTestBug();
    when(bugRepository.save(any())).thenReturn(bug);

    // Act
    TestBug result = bugService.createBug(bug);

    // Assert
    assertNotNull(result);
    assertEquals("Test Bug", result.getTitle());
}
```

### Integration Tests

- Test API endpoints
- Test database operations
- Test external integrations

### E2E Tests

- Test critical user flows
- Use Playwright for browser testing

## Documentation

### API Documentation

- Use OpenAPI annotations
- Keep documentation up to date
- Include examples for all endpoints

### Code Documentation

- Add Javadoc for public methods
- Explain complex algorithms
- Document configuration options

### User Documentation

- Update README for new features
- Add usage examples
- Include troubleshooting guides

## Getting Help

- Open an issue for bugs
- Start a discussion for questions
- Join our community chat

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
