package com.testpilot.agent.planner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testpilot.agent.AgentTask;
import com.testpilot.agent.memory.AgentMemoryService;
import com.testpilot.agent.tool.AgentToolRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentPlanner {

    private final ChatClient.Builder chatClientBuilder;
    private final AgentMemoryService memoryService;
    private final AgentToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;

    public AgentPlan createPlan(AgentTask task) {
        try {
            String memoryContext = memoryService.getMemoryContext(task.getProjectId());
            String toolDescriptions = objectMapper.writeValueAsString(toolRegistry.getToolDescriptions());

            String prompt = buildPlanningPrompt(task, memoryContext, toolDescriptions);

            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt()
                    .system("""
                        You are TestPilot AI, an expert software test engineer agent.
                        Your job is to analyze test goals and create detailed test plans.
                        Always respond with valid JSON only, no markdown.
                    """)
                    .user(prompt)
                    .call()
                    .content();

            // Parse the JSON response
            String json = extractJson(response);
            Map<String, Object> planData = objectMapper.readValue(json, new TypeReference<>() {});

            AgentPlan plan = new AgentPlan();
            plan.setGoal(task.getGoal());
            plan.setStrategy((String) planData.getOrDefault("strategy", ""));
            plan.setScenarios((List<Map<String, Object>>) planData.getOrDefault("scenarios", List.of()));
            plan.setSteps((List<Map<String, Object>>) planData.getOrDefault("steps", List.of()));
            plan.setEstimatedDuration((String) planData.getOrDefault("estimatedDuration", ""));
            plan.setRiskAreas((List<String>) planData.getOrDefault("riskAreas", List.of()));

            log.info("Plan created for task {}: {} steps, {} scenarios",
                    task.getId(), plan.getSteps().size(), plan.getScenarios().size());

            // Save to memory
            memoryService.save(task.getProjectId(), "plan",
                    "Goal: " + task.getGoal() + " | Steps: " + plan.getSteps().size(),
                    null);

            return plan;

        } catch (Exception e) {
            log.error("Failed to create plan", e);
            throw new RuntimeException("Failed to create plan: " + e.getMessage());
        }
    }

    public AgentPlan replan(AgentTask task, AgentPlan currentPlan, List<Map<String, Object>> completedResults) {
        try {
            String completedSummary = objectMapper.writeValueAsString(completedResults);

            String prompt = buildReplanPrompt(task, currentPlan, completedSummary);

            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt()
                    .system("You are TestPilot AI replanning after partial execution. Respond with valid JSON only.")
                    .user(prompt)
                    .call()
                    .content();

            String json = extractJson(response);
            Map<String, Object> planData = objectMapper.readValue(json, new TypeReference<>() {});

            AgentPlan newPlan = new AgentPlan();
            newPlan.setGoal(task.getGoal());
            newPlan.setStrategy((String) planData.getOrDefault("strategy", currentPlan.getStrategy()));
            newPlan.setScenarios((List<Map<String, Object>>) planData.getOrDefault("scenarios", currentPlan.getScenarios()));
            newPlan.setSteps((List<Map<String, Object>>) planData.getOrDefault("steps", List.of()));
            newPlan.setEstimatedDuration((String) planData.getOrDefault("estimatedDuration", ""));
            newPlan.setRiskAreas((List<String>) planData.getOrDefault("riskAreas", List.of()));

            log.info("Replanned task {}: {} new steps", task.getId(), newPlan.getSteps().size());
            return newPlan;

        } catch (Exception e) {
            log.error("Failed to replan", e);
            return currentPlan;
        }
    }

    private String buildPlanningPrompt(AgentTask task, String memoryContext, String toolDescriptions) {
        return """
            ## Test Goal
            %s

            ## Context
            %s

            ## Available Tools
            %s

            ## Task
            Create a comprehensive test plan. Output JSON with this structure:
            {
                "strategy": "Overall testing strategy description",
                "scenarios": [
                    {
                        "title": "Scenario title",
                        "description": "What this scenario tests",
                        "testType": "API/UI",
                        "priority": 1-5,
                        "riskLevel": "LOW/MEDIUM/HIGH/CRITICAL"
                    }
                ],
                "steps": [
                    {
                        "stepName": "Step name",
                        "stepType": "analyze/plan/execute/assert/failure_analysis",
                        "tool": "tool name to use or null",
                        "input": "what to pass to the tool",
                        "assertion": "what to verify",
                        "expectedOutcome": "PASS/FAIL description"
                    }
                ],
                "estimatedDuration": "estimated time",
                "riskAreas": ["areas that need special attention"]
            }

            Make the plan thorough but practical. Include both happy path and edge cases.
            For each step, specify which tool to use if applicable.
            """.formatted(task.getGoal(), memoryContext, toolDescriptions);
    }

    private String buildReplanPrompt(AgentTask task, AgentPlan currentPlan, String completedResults) {
        return """
            ## Original Goal
            %s

            ## Current Plan Progress
            Strategy: %s

            ## Completed Steps Results
            %s

            ## Task
            Based on completed results, create a NEW plan with remaining steps only.
            If a step failed, suggest alternative approaches.
            Output the same JSON structure as the original plan.
            """.formatted(task.getGoal(), currentPlan.getStrategy(), completedResults);
    }

    private String extractJson(String response) {
        // Extract JSON from possible markdown code blocks
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }
}
