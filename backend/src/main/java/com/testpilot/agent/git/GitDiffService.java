package com.testpilot.agent.git;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitDiffService {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    public GitDiffAnalysis analyzeDiff(String diffContent) {
        try {
            List<GitFileChange> fileChanges = parseDiff(diffContent);

            String prompt = """
                Analyze this Git diff and identify what needs to be regression tested:

                %s

                Provide analysis as JSON:
                {
                    "summary": "overall change summary",
                    "affectedModules": ["module1", "module2"],
                    "riskAreas": [
                        {
                            "area": "area name",
                            "riskLevel": "HIGH/MEDIUM/LOW",
                            "reason": "why this is risky"
                        }
                    ],
                    "suggestedTests": [
                        {
                            "testName": "test name",
                            "testType": "UNIT/INTEGRATION/API/UI",
                            "description": "what to test",
                            "priority": "P0/P1/P2"
                        }
                    ],
                    "breakingChanges": ["change1", "change2"]
                }
                """.formatted(diffContent);

            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt()
                    .system("You are a Git diff analysis expert for regression testing. Respond with valid JSON only.")
                    .user(prompt)
                    .call()
                    .content();

            String json = extractJson(response);
            Map<String, Object> analysisMap = objectMapper.readValue(json, new TypeReference<>() {});

            return GitDiffAnalysis.builder()
                    .summary(analysisMap.getOrDefault("summary", "").toString())
                    .affectedModules((List<String>) analysisMap.getOrDefault("affectedModules", List.of()))
                    .riskAreas((List<Map<String, Object>>) analysisMap.getOrDefault("riskAreas", List.of()))
                    .suggestedTests((List<Map<String, Object>>) analysisMap.getOrDefault("suggestedTests", List.of()))
                    .breakingChanges((List<String>) analysisMap.getOrDefault("breakingChanges", List.of()))
                    .fileChanges(fileChanges)
                    .build();

        } catch (Exception e) {
            log.error("Git diff analysis failed: {}", e.getMessage());
            return GitDiffAnalysis.builder()
                    .summary("Analysis failed: " + e.getMessage())
                    .build();
        }
    }

    public List<GitFileChange> parseDiff(String diffContent) {
        List<GitFileChange> changes = new ArrayList<>();
        String[] lines = diffContent.split("\n");

        GitFileChange currentFile = null;
        for (String line : lines) {
            if (line.startsWith("diff --git")) {
                if (currentFile != null) {
                    changes.add(currentFile);
                }
                String[] parts = line.split(" ");
                if (parts.length >= 4) {
                    currentFile = GitFileChange.builder()
                            .oldFile(parts[2])
                            .newFile(parts[3])
                            .additions(0)
                            .deletions(0)
                            .build();
                }
            } else if (line.startsWith("+") && !line.startsWith("+++")) {
                if (currentFile != null) {
                    currentFile.setAdditions(currentFile.getAdditions() + 1);
                }
            } else if (line.startsWith("-") && !line.startsWith("---")) {
                if (currentFile != null) {
                    currentFile.setDeletions(currentFile.getDeletions() + 1);
                }
            }
        }

        if (currentFile != null) {
            changes.add(currentFile);
        }

        return changes;
    }

    public String generateRegressionTests(GitDiffAnalysis analysis) {
        try {
            String prompt = """
                Based on this Git diff analysis, generate regression test cases:

                Summary: %s
                Affected Modules: %s
                Risk Areas: %s
                Suggested Tests: %s

                Generate specific test cases as JSON:
                {
                    "testCases": [
                        {
                            "name": "test name",
                            "description": "what this test verifies",
                            "steps": ["step1", "step2"],
                            "expectedResult": "expected outcome",
                            "testType": "UNIT/INTEGRATION/API",
                            "priority": "P0/P1/P2"
                        }
                    ]
                }
                """.formatted(
                    analysis.getSummary(),
                    analysis.getAffectedModules(),
                    analysis.getRiskAreas(),
                    analysis.getSuggestedTests()
            );

            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt()
                    .system("You are a test case generation expert. Respond with valid JSON only.")
                    .user(prompt)
                    .call()
                    .content();

            return response;

        } catch (Exception e) {
            log.error("Test generation failed: {}", e.getMessage());
            return "{\"testCases\": []}";
        }
    }

    private String extractJson(String response) {
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) cleaned = cleaned.substring(7);
        else if (cleaned.startsWith("```")) cleaned = cleaned.substring(3);
        if (cleaned.endsWith("```")) cleaned = cleaned.substring(0, cleaned.length() - 3);
        return cleaned.trim();
    }

    @lombok.Data
    @lombok.Builder
    public static class GitDiffAnalysis {
        private String summary;
        private List<String> affectedModules;
        private List<Map<String, Object>> riskAreas;
        private List<Map<String, Object>> suggestedTests;
        private List<String> breakingChanges;
        private List<GitFileChange> fileChanges;
    }

    @lombok.Data
    @lombok.Builder
    public static class GitFileChange {
        private String oldFile;
        private String newFile;
        private int additions;
        private int deletions;
    }
}
