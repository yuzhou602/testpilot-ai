package com.testpilot.agent.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class TestDataTool implements AgentTool {

    @Override
    public String getName() {
        return "generateTestData";
    }

    @Override
    public String getDescription() {
        return "Generate test data for a given schema: normal, empty, boundary, oversized, special characters, illegal format, duplicate values.";
    }

    @Override
    public String getParametersSchema() {
        return """
        {
            "type": "object",
            "properties": {
                "schema": {"type": "object"},
                "types": {"type": "array", "items": {"type": "string"}},
                "count": {"type": "integer"}
            },
            "required": ["schema"]
        }""";
    }

    @Override
    public ToolResult execute(ToolContext context, Map<String, Object> args) {
        Map<String, Object> schema = (Map<String, Object>) args.get("schema");
        List<String> types = (List<String>) args.getOrDefault("types",
                List.of("normal", "empty", "boundary", "oversized", "special_chars", "duplicate"));
        int count = args.get("count") != null ? ((Number) args.get("count")).intValue() : 1;

        List<Map<String, Object>> testDataList = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Map<String, Object> record = new HashMap<>();
            for (Map.Entry<String, Object> field : schema.entrySet()) {
                String fieldName = field.getKey();
                Object fieldDef = field.getValue();
                Map<String, Object> fieldMap = fieldDef instanceof Map ? (Map<String, Object>) fieldDef : Map.of("type", "string");

                String type = (String) fieldMap.getOrDefault("type", "string");
                int maxLen = fieldMap.get("maxLength") != null ? ((Number) fieldMap.get("maxLength")).intValue() : 255;

                String chosenType = types.get(i % types.size());
                record.put(fieldName, generateValue(type, maxLen, chosenType));
            }
            testDataList.add(record);
        }

        return ToolResult.builder()
                .success(true)
                .output("Generated " + testDataList.size() + " test records")
                .data(testDataList)
                .build();
    }

    private Object generateValue(String type, int maxLen, String testType) {
        return switch (testType) {
            case "empty" -> "";
            case "boundary" -> switch (type) {
                case "integer" -> maxLen;
                case "string" -> "a".repeat(Math.min(maxLen, 255));
                default -> "";
            };
            case "oversized" -> switch (type) {
                case "string" -> "X".repeat(maxLen + 100);
                case "integer" -> 999999999;
                default -> "oversized_value";
            };
            case "special_chars" -> "<script>alert('xss')</script>@#$%^&*()";
            case "duplicate" -> "duplicate_test_value";
            default -> switch (type) {
                case "integer" -> 1;
                case "email" -> "test@example.com";
                case "string" -> "test_value";
                case "boolean" -> true;
                default -> "test";
            };
        };
    }
}
