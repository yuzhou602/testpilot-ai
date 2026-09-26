package com.testpilot.agent.runtime;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FailureAnalyzer {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    public FailureAnalysis analyze(String stepName, String stepInput, String stepOutput, String errorMessage) {
        try {
            String prompt = """
                Analyze this test failure:
                Step: %s
                Input: %s
                Result: %s
                Error: %s

                Provide analysis as JSON:
                {
                    "failureType": "PRODUCT_BUG/TEST_ERROR/ENVIRONMENT_ERROR/DATA_ERROR/TOOL_ERROR",
                    "rootCause": "description",
                    "evidence": "what supports this conclusion",
                    "confidence": 0.0-1.0,
                    "shouldRetry": true/false,
                    "createBug": true/false,
                    "bugData": {
                        "title": "bug title",
                        "description": "description",
                        "severity": "CRITICAL/MAJOR/MINOR/COSMETIC",
                        "stepsToReproduce": "steps",
                        "expectedResult": "expected",
                        "actualResult": "actual"
                    }
                }
                """.formatted(stepName, stepInput, stepOutput, errorMessage);

            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt()
                    .system("You are TestPilot AI analyzing test failures. Respond with valid JSON only. Be precise and evidence-based.")
                    .user(prompt)
                    .call()
                    .content();

            String json = extractJson(response);
            Map<String, Object> analysisMap = objectMapper.readValue(json, new TypeReference<>() {});

            return FailureAnalysis.builder()
                    .failureType(analysisMap.getOrDefault("failureType", "UNKNOWN").toString())
                    .rootCause(analysisMap.getOrDefault("rootCause", "Unknown").toString())
                    .evidence(analysisMap.getOrDefault("evidence", "").toString())
                    .confidence(toDouble(analysisMap.get("confidence")))
                    .shouldRetry(toBoolean(analysisMap.get("shouldRetry")))
                    .createBug(toBoolean(analysisMap.get("createBug")))
                    .bugData((Map<String, Object>) analysisMap.get("bugData"))
                    .build();

        } catch (Exception e) {
            log.error("Failure analysis error", e);
            return FailureAnalysis.builder()
                    .failureType("UNKNOWN")
                    .rootCause("Analysis failed: " + e.getMessage())
                    .confidence(0.0)
                    .shouldRetry(false)
                    .createBug(false)
                    .build();
        }
    }

    private String extractJson(String response) {
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) cleaned = cleaned.substring(7);
        else if (cleaned.startsWith("```")) cleaned = cleaned.substring(3);
        if (cleaned.endsWith("```")) cleaned = cleaned.substring(0, cleaned.length() - 3);
        return cleaned.trim();
    }

    private double toDouble(Object value) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        try { return Double.parseDouble(value.toString()); } catch (Exception e) { return 0.0; }
    }

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }

    @lombok.Data
    @lombok.Builder
    public static class FailureAnalysis {
        private String failureType;
        private String rootCause;
        private String evidence;
        private double confidence;
        private boolean shouldRetry;
        private boolean createBug;
        private Map<String, Object> bugData;
    }
}
