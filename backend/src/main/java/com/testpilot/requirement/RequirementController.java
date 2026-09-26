package com.testpilot.requirement;

import com.testpilot.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requirements")
@RequiredArgsConstructor
@Tag(name = "Requirements", description = "Test requirement management")
public class RequirementController {

    private final RequirementService requirementService;

    @PostMapping
    @Operation(summary = "Create a new requirement")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Requirement created")
    })
    public ApiResponse<Requirement> createRequirement(@RequestBody Requirement requirement) {
        return ApiResponse.ok(requirementService.createRequirement(requirement));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get project requirements")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Requirements retrieved")
    })
    public ApiResponse<List<Requirement>> getProjectRequirements(@PathVariable Long projectId) {
        return ApiResponse.ok(requirementService.getProjectRequirements(projectId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get requirement by ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Requirement found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requirement not found", content = @Content)
    })
    public ApiResponse<Requirement> getRequirement(@PathVariable Long id) {
        return ApiResponse.ok(requirementService.getRequirement(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a requirement")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Requirement updated")
    })
    public ApiResponse<Requirement> updateRequirement(@PathVariable Long id, @RequestBody Requirement requirement) {
        return ApiResponse.ok(requirementService.updateRequirement(id, requirement));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a requirement")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Requirement deleted")
    })
    public ApiResponse<Void> deleteRequirement(@PathVariable Long id) {
        requirementService.deleteRequirement(id);
        return ApiResponse.ok(null);
    }
}
