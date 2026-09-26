package com.testpilot.agent.log;

import com.testpilot.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Tag(name = "Application Logs", description = "Application log management")
public class ApplicationLogController {

    private final ApplicationLogService logService;

    @PostMapping
    @Operation(summary = "Create a new log entry")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Log entry created")
    })
    public ApiResponse<ApplicationLog> createLog(@RequestBody ApplicationLog log) {
        return ApiResponse.ok(logService.save(log));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get project logs by level")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logs retrieved")
    })
    public ApiResponse<List<ApplicationLog>> getProjectLogs(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "ERROR") String level,
            @RequestParam(defaultValue = "30") int sinceMinutes) {
        return ApiResponse.ok(logService.findByProjectAndLevel(projectId, level, sinceMinutes));
    }

    @GetMapping("/project/{projectId}/search")
    @Operation(summary = "Search logs by keyword")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search results retrieved")
    })
    public ApiResponse<List<ApplicationLog>> searchLogs(
            @PathVariable Long projectId,
            @RequestParam String keyword) {
        return ApiResponse.ok(logService.findByProjectAndKeyword(projectId, keyword));
    }
}
