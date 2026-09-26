package com.testpilot.testcase;

import com.testpilot.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/testcases")
@RequiredArgsConstructor
@Tag(name = "Test Cases", description = "Test case management")
public class TestCaseController {

    private final TestCaseService testCaseService;

    @PostMapping
    @Operation(summary = "Create a new test case")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Test case created")
    })
    public ApiResponse<TestCase> createTestCase(@RequestBody TestCase testCase) {
        return ApiResponse.ok(testCaseService.createTestCase(testCase));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get project test cases")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Test cases retrieved")
    })
    public ApiResponse<List<TestCase>> getProjectTestCases(@PathVariable Long projectId) {
        return ApiResponse.ok(testCaseService.getProjectTestCases(projectId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get test case by ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Test case found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Test case not found", content = @Content)
    })
    public ApiResponse<TestCase> getTestCase(@PathVariable Long id) {
        return ApiResponse.ok(testCaseService.getTestCase(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a test case")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Test case updated")
    })
    public ApiResponse<TestCase> updateTestCase(@PathVariable Long id, @RequestBody TestCase testCase) {
        return ApiResponse.ok(testCaseService.updateTestCase(id, testCase));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a test case")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Test case deleted")
    })
    public ApiResponse<Void> deleteTestCase(@PathVariable Long id) {
        testCaseService.deleteTestCase(id);
        return ApiResponse.ok(null);
    }
}
