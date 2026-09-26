# Changelog

All notable changes to TestPilot AI will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Build & Repository
- Reduced the largest frontend JavaScript chunk from 833.94 kB to 191.05 kB by replacing the remaining package-level Element Plus import and separating the icon cache chunk.
- Added GitHub Actions CI for the Vue production build, ten-route browser smoke test, and Java unit tests.
- Added Dependabot coverage for npm, Maven, and GitHub Actions dependencies.
- Added repository line-ending and binary file normalization rules.

### UI Quality
- Fixed the AI workspace execution summary at 1366×768 and the responsive action selector at 1024px.
- Added the TestPilot AI browser icon and removed the initial missing-resource request.
- Added a rendered workspace design-audit report with before/after evidence.

### Added
- Multi-Agent architecture (Planner, Executor, Reviewer)
- RAG (Retrieval-Augmented Generation) for knowledge search
- Git Diff analysis and regression test generation
- Self-Healing browser locator
- Chat Memory for multi-round conversations
- SSE heartbeat and reconnection
- Docker Compose deployment
- SpringDoc OpenAPI documentation
- Unit tests for core services

### Changed
- Refactored AgentExecutor with multi-round LLM loop
- Improved failure analysis with FailureAnalyzer
- Enhanced BrowserTool with Self-Healing capabilities
- Updated KnowledgeTool to use RAG

### Fixed
- Missing demo.ts for frontend
- Missing application_logs and knowledge_base tables
- SSE connection stability issues

## [1.0.0] - 2026-09-05

### Added
- Initial release of TestPilot AI
- AI Agent-driven testing platform
- 12-state Agent state machine
- 8+ Agent tools (HTTP, Browser, Database, Log, etc.)
- OpenAPI import and API testing
- UI testing with Playwright
- Test case management
- Bug tracking with root cause analysis
- Test reports and analytics
- Real-time SSE streaming
- Dark theme UI

### Backend
- Spring Boot 3.3.5
- Spring AI integration
- JWT authentication
- JPA/Hibernate
- Redis caching
- MySQL database

### Frontend
- Vue 3 + TypeScript
- Vite build tool
- Element Plus UI
- Tailwind CSS
- Pinia state management
- Vue Flow for trace visualization

## [0.9.0] - 2026-09-01

### Added
- Beta release for testing
- Core Agent functionality
- Basic UI components

## [0.8.0] - 2026-08-25

### Added
- Alpha release for internal testing
- Project setup and architecture

---

## Version History

| Version | Date | Description |
|---------|------|-------------|
| 1.0.0 | 2026-09-05 | First stable release |
| 0.9.0 | 2026-09-01 | Beta release |
| 0.8.0 | 2026-08-25 | Alpha release |

## Upgrade Guide

### Upgrading to 1.0.0

1. Backup your database
2. Update dependencies
3. Run database migrations
4. Update configuration files
5. Restart the application

```bash
# Backup database
mysqldump -u root -p testpilot > backup.sql

# Update backend
cd backend
mvn clean install

# Update frontend
cd frontend
npm install
npm run build

# Restart services
docker-compose down
docker-compose up -d
```

## Support

- **Architecture**: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
- **Demo guide**: [docs/DEMO_GUIDE.md](docs/DEMO_GUIDE.md)
- **API reference**: [docs/API.md](docs/API.md)
- **Issues and discussions**: available after the GitHub remote is created
