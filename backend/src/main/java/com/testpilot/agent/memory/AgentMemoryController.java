package com.testpilot.agent.memory;

import com.testpilot.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/memory")
@RequiredArgsConstructor
@Tag(name = "Agent Memory", description = "Agent memory and conversation management")
public class AgentMemoryController {

    private final AgentMemoryService memoryService;

    @PostMapping
    @Operation(summary = "Save a memory entry")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Memory saved")
    })
    public ApiResponse<AgentMemory> saveMemory(
            @RequestParam Long projectId,
            @RequestParam String memoryType,
            @RequestParam String content,
            @RequestParam(required = false) String metadata) {
        return ApiResponse.ok(memoryService.save(projectId, memoryType, content, metadata));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get project memory")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Memory retrieved")
    })
    public ApiResponse<List<AgentMemory>> getProjectMemory(@PathVariable Long projectId) {
        return ApiResponse.ok(memoryService.getProjectMemory(projectId));
    }

    @GetMapping("/project/{projectId}/type/{memoryType}")
    @Operation(summary = "Get memory by type")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Memory retrieved")
    })
    public ApiResponse<List<AgentMemory>> getMemoryByType(
            @PathVariable Long projectId,
            @PathVariable String memoryType) {
        return ApiResponse.ok(memoryService.getByType(projectId, memoryType));
    }

    @GetMapping("/project/{projectId}/context")
    @Operation(summary = "Get memory context")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Context retrieved")
    })
    public ApiResponse<String> getMemoryContext(@PathVariable Long projectId) {
        return ApiResponse.ok(memoryService.getMemoryContext(projectId));
    }
}
