package com.testpilot.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.testpilot.common.exception.BusinessException;
import com.testpilot.project.TestProject;
import com.testpilot.project.TestProjectRepository;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.parser.core.models.ParseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenApiService {

    private final ApiDefinitionRepository apiDefinitionRepository;
    private final TestProjectRepository projectRepository;
    private final ObjectMapper objectMapper;

    public List<ApiDefinition> importOpenApi(Long projectId, String specContent) {
        try {
            ParseResult result = new OpenAPIParser().readContents(specContent, null, null);
            OpenAPI openAPI = result.getOpenAPI();

            if (openAPI == null) {
                throw new BusinessException("Failed to parse OpenAPI specification");
            }

            // Save spec to project
            TestProject project = projectRepository.findById(projectId).orElseThrow();
            project.setOpenApiSpec(specContent);
            projectRepository.save(project);

            List<ApiDefinition> definitions = new ArrayList<>();

            if (openAPI.getPaths() != null) {
                for (Map.Entry<String, PathItem> entry : openAPI.getPaths().entrySet()) {
                    String path = entry.getKey();
                    PathItem pathItem = entry.getValue();

                    processOperation(projectId, path, "GET", pathItem.getGet(), definitions);
                    processOperation(projectId, path, "POST", pathItem.getPost(), definitions);
                    processOperation(projectId, path, "PUT", pathItem.getPut(), definitions);
                    processOperation(projectId, path, "PATCH", pathItem.getPatch(), definitions);
                    processOperation(projectId, path, "DELETE", pathItem.getDelete(), definitions);
                }
            }

            return apiDefinitionRepository.saveAll(definitions);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse OpenAPI", e);
            throw new BusinessException("Invalid OpenAPI specification: " + e.getMessage());
        }
    }

    private void processOperation(Long projectId, String path, String method,
                                   Operation operation,
                                   List<ApiDefinition> definitions) {
        if (operation == null) return;

        ApiDefinition api = ApiDefinition.builder()
                .projectId(projectId)
                .method(method)
                .path(path)
                .summary(operation.getSummary())
                .description(operation.getDescription())
                .tag(operation.getTags() != null && !operation.getTags().isEmpty()
                        ? operation.getTags().get(0) : null)
                .build();

        // Parse request body
        if (operation.getRequestBody() != null) {
            RequestBody rb = operation.getRequestBody();
            if (rb.getContent() != null) {
                MediaType mediaType = rb.getContent().get("application/json");
                if (mediaType != null && mediaType.getSchema() != null) {
                    api.setRequestSchema(schemaToJson(mediaType.getSchema()).toString());
                }
            }
        }

        // Parse response
        if (operation.getResponses() != null) {
            ApiResponse response = operation.getResponses().get("200");
            if (response == null) response = operation.getResponses().get("201");
            if (response != null && response.getContent() != null) {
                MediaType mediaType = response.getContent().get("application/json");
                if (mediaType != null && mediaType.getSchema() != null) {
                    api.setResponseSchema(schemaToJson(mediaType.getSchema()).toString());
                }
            }
        }

        definitions.add(api);
    }

    private Object schemaToJson(Schema<?> schema) {
        Map<String, Object> json = new HashMap<>();
        if (schema.getType() != null) json.put("type", schema.getType());
        if (schema.getFormat() != null) json.put("format", schema.getFormat());
        if (schema.getTitle() != null) json.put("title", schema.getTitle());
        if (schema.getEnum() != null) json.put("enum", schema.getEnum());
        if (schema.getExample() != null) json.put("example", schema.getExample());

        Map<String, Object> properties = new HashMap<>();
        if (schema.getProperties() != null) {
            for (Map.Entry<String, Schema<?>> prop : schema.getProperties().entrySet()) {
                properties.put(prop.getKey(), schemaToJson(prop.getValue()));
            }
        }
        if (!properties.isEmpty()) json.put("properties", properties);

        List<String> required = schema.getRequired();
        if (required != null && !required.isEmpty()) json.put("required", required);

        return json;
    }

    public List<ApiDefinition> getProjectApis(Long projectId) {
        return apiDefinitionRepository.findByProjectIdOrderByPath(projectId);
    }
}
