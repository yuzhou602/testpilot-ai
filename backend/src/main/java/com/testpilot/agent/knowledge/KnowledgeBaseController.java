package com.testpilot.agent.knowledge;

import com.testpilot.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Tag(name = "Knowledge Base", description = "Testing knowledge base management")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    @PostMapping
    @Operation(summary = "Create a new knowledge entry")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Knowledge entry created")
    })
    public ApiResponse<KnowledgeBase> createKnowledge(@RequestBody KnowledgeBase knowledge) {
        return ApiResponse.ok(knowledgeBaseService.save(knowledge));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get project knowledge entries")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Knowledge entries retrieved")
    })
    public ApiResponse<List<KnowledgeBase>> getProjectKnowledge(@PathVariable Long projectId) {
        return ApiResponse.ok(knowledgeBaseService.findByCategory(projectId, null));
    }

    @GetMapping("/project/{projectId}/category/{category}")
    @Operation(summary = "Get knowledge by category")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Knowledge entries retrieved")
    })
    public ApiResponse<List<KnowledgeBase>> getKnowledgeByCategory(
            @PathVariable Long projectId,
            @PathVariable String category) {
        return ApiResponse.ok(knowledgeBaseService.findByCategory(projectId, category));
    }

    @GetMapping("/project/{projectId}/search")
    @Operation(summary = "Search knowledge base")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search results retrieved")
    })
    public ApiResponse<List<KnowledgeBase>> searchKnowledge(
            @PathVariable Long projectId,
            @RequestParam String query,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.ok(knowledgeBaseService.search(projectId, query, category, limit));
    }
}
