package com.testpilot.api;

import com.testpilot.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/openapi")
@RequiredArgsConstructor
public class OpenApiController {

    private final OpenApiService openApiService;

    @PostMapping("/import/{projectId}")
    public ApiResponse<List<ApiDefinition>> importOpenApi(
            @PathVariable Long projectId,
            @RequestBody String specContent) {
        return ApiResponse.ok(openApiService.importOpenApi(projectId, specContent));
    }

    @GetMapping("/list/{projectId}")
    public ApiResponse<List<ApiDefinition>> getApis(@PathVariable Long projectId) {
        return ApiResponse.ok(openApiService.getProjectApis(projectId));
    }
}
