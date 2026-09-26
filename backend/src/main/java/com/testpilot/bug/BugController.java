package com.testpilot.bug;

import com.testpilot.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bugs")
@RequiredArgsConstructor
@Tag(name = "Bugs", description = "Bug management and tracking")
public class BugController {

    private final BugService bugService;

    @PostMapping
    @Operation(summary = "Create a new bug")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bug created")
    })
    public ApiResponse<TestBug> createBug(@RequestBody TestBug bug) {
        return ApiResponse.ok(bugService.createBug(bug));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get project bugs")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bugs retrieved")
    })
    public ApiResponse<List<TestBug>> getProjectBugs(@PathVariable Long projectId) {
        return ApiResponse.ok(bugService.getProjectBugs(projectId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bug by ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bug found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Bug not found", content = @Content)
    })
    public ApiResponse<TestBug> getBug(@PathVariable Long id) {
        return ApiResponse.ok(bugService.getBug(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a bug")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bug updated")
    })
    public ApiResponse<TestBug> updateBug(@PathVariable Long id, @RequestBody TestBug bug) {
        return ApiResponse.ok(bugService.updateBug(id, bug));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a bug")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bug deleted")
    })
    public ApiResponse<Void> deleteBug(@PathVariable Long id) {
        bugService.deleteBug(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/project/{projectId}/stats")
    @Operation(summary = "Get bug statistics")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stats retrieved")
    })
    public ApiResponse<Map<String, Object>> getBugStats(@PathVariable Long projectId) {
        return ApiResponse.ok(bugService.getBugStats(projectId));
    }
}
