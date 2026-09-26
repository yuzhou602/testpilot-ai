# TestPilot AI API Documentation

## Overview

TestPilot AI provides a RESTful API for managing test projects, agents, and test execution. All endpoints return JSON responses with a standard format.

## Base URL

```
http://localhost:8080/api
```

## Authentication

All API requests require a JWT token in the Authorization header:

```
Authorization: Bearer <token>
```

### Get Token

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

Response:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "displayName": "Admin"
    }
  }
}
```

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | User login |
| POST | `/api/auth/register` | User registration |

### Projects

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/projects` | Create project |
| GET | `/api/projects?userId={id}` | Get user projects |
| GET | `/api/projects/{id}` | Get project by ID |
| PUT | `/api/projects/{id}` | Update project |
| DELETE | `/api/projects/{id}` | Delete project |

### Requirements

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/requirements` | Create requirement |
| GET | `/api/requirements/project/{projectId}` | Get project requirements |
| GET | `/api/requirements/{id}` | Get requirement by ID |
| PUT | `/api/requirements/{id}` | Update requirement |
| DELETE | `/api/requirements/{id}` | Delete requirement |

### Test Cases

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/testcases` | Create test case |
| GET | `/api/testcases/project/{projectId}` | Get project test cases |
| GET | `/api/testcases/{id}` | Get test case by ID |
| PUT | `/api/testcases/{id}` | Update test case |
| DELETE | `/api/testcases/{id}` | Delete test case |

### Bugs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/bugs` | Create bug |
| GET | `/api/bugs/project/{projectId}` | Get project bugs |
| GET | `/api/bugs/{id}` | Get bug by ID |
| PUT | `/api/bugs/{id}` | Update bug |
| DELETE | `/api/bugs/{id}` | Delete bug |
| GET | `/api/bugs/project/{projectId}/stats` | Get bug statistics |

### Agent

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/agent/tasks` | Create agent task |
| GET | `/api/agent/tasks/{id}` | Get task by ID |
| GET | `/api/agent/tasks/project/{projectId}` | Get project tasks |
| POST | `/api/agent/tasks/{id}/execute` | Execute task |
| POST | `/api/agent/tasks/{id}/cancel` | Cancel task |
| GET | `/api/agent/tasks/{id}/stream` | SSE stream for task events |
| GET | `/api/agent/tasks/{id}/steps` | Get task steps |
| GET | `/api/agent/tasks/{id}/traces` | Get task traces |

### Knowledge Base

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/knowledge` | Create knowledge entry |
| GET | `/api/knowledge/project/{projectId}` | Get project knowledge |
| GET | `/api/knowledge/project/{projectId}/category/{category}` | Get by category |
| GET | `/api/knowledge/project/{projectId}/search?query={query}` | Search knowledge |

### Application Logs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/logs` | Create log entry |
| GET | `/api/logs/project/{projectId}?level={level}` | Get logs by level |
| GET | `/api/logs/project/{projectId}/search?keyword={keyword}` | Search logs |

### Agent Memory

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/memory` | Save memory entry |
| GET | `/api/memory/project/{projectId}` | Get project memory |
| GET | `/api/memory/project/{projectId}/type/{type}` | Get by type |
| GET | `/api/memory/project/{projectId}/context` | Get memory context |

### Git Integration

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/git/analyze-diff` | Analyze Git diff |
| POST | `/api/git/generate-tests` | Generate regression tests |

### Evaluation

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/evaluation/project/{projectId}` | Get project evaluations |
| GET | `/api/evaluation/task/{taskId}` | Get task evaluations |

### OpenAPI Import

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/openapi/import/{projectId}` | Import OpenAPI spec for a project |
| GET | `/api/openapi/list/{projectId}` | Get project APIs |

## Response Format

All responses follow this format:

```json
{
  "code": 200,
  "message": "Success",
  "data": {}
}
```

Error responses:
```json
{
  "code": 400,
  "message": "Invalid request",
  "data": null
}
```

## SSE Events

The Agent SSE stream (`/api/agent/tasks/{id}/stream`) emits these events:

| Event | Description |
|-------|-------------|
| TASK_STARTED | Task execution started |
| PLAN_CREATED | Test plan generated |
| STEP_STARTED | Step execution started |
| STEP_COMPLETED | Step execution completed |
| STEP_FAILED | Step execution failed |
| TOOL_CALL | Tool called by agent |
| TOOL_RESULT | Tool execution result |
| ASSERTION | Test assertion result |
| FAILURE_ANALYSIS | Failure analysis started |
| BUG_CREATED | Bug created from failure |
| WAITING_USER | Waiting for user input |
| TASK_COMPLETED | Task execution completed |
| TASK_FAILED | Task execution failed |
| AGENT_THINKING | Agent is processing |

## Error Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 400 | Bad request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not found |
| 500 | Internal server error |

## CORS

CORS is enabled for development. In production, configure allowed origins in `application-prod.yml`.
