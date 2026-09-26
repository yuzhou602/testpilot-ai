package com.testpilot.agent;

import com.testpilot.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
@Tag(name = "Agent", description = "AI Agent for autonomous test execution and analysis")
public class AgentController {

    private final AgentService agentService;

    @PostMapping("/tasks")
    @Operation(summary = "Create a new Agent task", description = "Create a new task for the AI Agent to execute")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
    })
    public ApiResponse<AgentTask> createTask(@RequestBody CreateTaskRequest request) {
        return ApiResponse.ok(agentService.createTask(request));
    }

    @GetMapping("/tasks/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieve a specific task by its ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found", content = @Content)
    })
    public ApiResponse<AgentTask> getTask(@PathVariable Long id) {
        return ApiResponse.ok(agentService.getTask(id));
    }

    @GetMapping("/tasks/project/{projectId}")
    @Operation(summary = "Get project tasks", description = "Retrieve all tasks for a specific project")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tasks retrieved successfully")
    })
    public ApiResponse<List<AgentTask>> getProjectTasks(@PathVariable Long projectId) {
        return ApiResponse.ok(agentService.getProjectTasks(projectId));
    }

    @PostMapping("/tasks/{id}/execute")
    @Operation(summary = "Execute a task", description = "Start executing an Agent task")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task execution started"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found", content = @Content)
    })
    public ApiResponse<Void> executeTask(@PathVariable Long id) {
        agentService.executeTask(id);
        return ApiResponse.ok("Task execution started", null);
    }

    @PostMapping("/tasks/{id}/cancel")
    @Operation(summary = "Cancel a task", description = "Cancel a running Agent task")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task cancelled"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found", content = @Content)
    })
    public ApiResponse<Void> cancelTask(@PathVariable Long id) {
        agentService.cancelTask(id);
        return ApiResponse.ok("Task cancelled", null);
    }

    @GetMapping(value = "/tasks/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream task events", description = "SSE stream for real-time task updates")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "SSE stream connected")
    })
    public SseEmitter streamTaskEvents(@PathVariable Long id) {
        return agentService.createSseEmitter(id);
    }

    @GetMapping("/tasks/{id}/steps")
    @Operation(summary = "Get task steps", description = "Retrieve all steps for a specific task")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Steps retrieved successfully")
    })
    public ApiResponse<List<AgentStep>> getTaskSteps(@PathVariable Long id) {
        return ApiResponse.ok(agentService.getTaskSteps(id));
    }

    @GetMapping("/tasks/{id}/traces")
    @Operation(summary = "Get task traces", description = "Retrieve execution traces for a specific task")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Traces retrieved successfully")
    })
    public ApiResponse<?> getTaskTraces(@PathVariable Long id) {
        return ApiResponse.ok(agentService.getTaskTraces(id));
    }

    @Data
    public static class CreateTaskRequest {
        @Schema(description = "Project ID", example = "1")
        private Long projectId;

        @Schema(description = "User ID", example = "1")
        private Long userId;

        @Schema(description = "Task goal", example = "Test user login functionality")
        private String goal;

        @Schema(description = "Additional context", example = "POST /api/auth/login endpoint")
        private String context;
    }
}
