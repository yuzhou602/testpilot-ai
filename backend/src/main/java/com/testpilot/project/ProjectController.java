package com.testpilot.project;

import com.testpilot.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Test project management")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Create a new project", description = "Create a new test project")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
    })
    public ApiResponse<TestProject> createProject(@RequestBody TestProject project) {
        return ApiResponse.ok(projectService.createProject(project));
    }

    @GetMapping
    @Operation(summary = "Get user projects", description = "Retrieve all projects for a specific user")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Projects retrieved successfully")
    })
    public ApiResponse<List<TestProject>> getProjects(@RequestParam Long userId) {
        return ApiResponse.ok(projectService.getUserProjects(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID", description = "Retrieve a specific project by its ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found", content = @Content)
    })
    public ApiResponse<TestProject> getProject(@PathVariable Long id) {
        return ApiResponse.ok(projectService.getProject(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a project", description = "Update an existing project")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found", content = @Content)
    })
    public ApiResponse<TestProject> updateProject(@PathVariable Long id, @RequestBody TestProject project) {
        return ApiResponse.ok(projectService.updateProject(id, project));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a project", description = "Delete a specific project")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found", content = @Content)
    })
    public ApiResponse<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ApiResponse.ok(null);
    }
}
