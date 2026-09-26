package com.testpilot.agent.git;

import com.testpilot.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/git")
@RequiredArgsConstructor
@Tag(name = "Git Integration", description = "Git diff analysis and regression test generation")
public class GitDiffController {

    private final GitDiffService gitDiffService;

    @PostMapping("/analyze-diff")
    @Operation(summary = "Analyze Git diff", description = "Analyze a Git diff and identify regression test needs")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Analysis completed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid diff", content = @Content)
    })
    public ApiResponse<GitDiffService.GitDiffAnalysis> analyzeDiff(@RequestBody DiffRequest request) {
        return ApiResponse.ok(gitDiffService.analyzeDiff(request.getDiffContent()));
    }

    @PostMapping("/generate-tests")
    @Operation(summary = "Generate regression tests", description = "Generate regression test cases based on diff analysis")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tests generated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid analysis", content = @Content)
    })
    public ApiResponse<String> generateTests(@RequestBody DiffRequest request) {
        GitDiffService.GitDiffAnalysis analysis = gitDiffService.analyzeDiff(request.getDiffContent());
        return ApiResponse.ok(gitDiffService.generateRegressionTests(analysis));
    }

    @Data
    public static class DiffRequest {
        private String diffContent;
    }
}
