package com.testpilot.agent.multi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testpilot.agent.AgentTask;
import com.testpilot.agent.AgentStep;
import com.testpilot.agent.AgentStepRepository;
import com.testpilot.agent.planner.AgentPlan;
import com.testpilot.agent.runtime.StepExecutor;
import com.testpilot.agent.tool.AgentTool;
import com.testpilot.common.enums.StepStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutorAgent {

    private final StepExecutor stepExecutor;
    private final AgentStepRepository stepRepository;
    private final ObjectMapper objectMapper;

    public StepResult executeStep(AgentTask task, AgentPlan plan, int stepIndex, AgentContext context) {
        log.info("ExecutorAgent: Executing step {} for task {}", stepIndex, task.getId());

        Map<String, Object> stepDef = plan.getSteps().get(stepIndex);
        String stepName = (String) stepDef.getOrDefault("stepName", "Step " + (stepIndex + 1));
        String toolName = (String) stepDef.get("tool");

        AgentStep step = AgentStep.builder()
                .taskId(task.getId())
                .stepIndex(stepIndex)
                .stepName(stepName)
                .stepType((String) stepDef.getOrDefault("stepType", "execute"))
                .toolName(toolName)
                .status(StepStatus.RUNNING)
                .input(toJson(stepDef))
                .build();
        step = stepRepository.save(step);

        try {
            Map<String, Object> toolInput = parseToolInput(stepDef);
            AgentTool.ToolContext ctx = AgentTool.ToolContext.builder()
                    .taskId(task.getId())
                    .projectId(task.getProjectId())
                    .taskStatus(task.getStatus())
                    .build();

            StepExecutor.StepResult result = stepExecutor.execute(toolName, toolInput, ctx);

            step.setStatus(StepStatus.COMPLETED);
            step.setOutput(toJson(result));
            step.setLatencyMs(result.getLatencyMs());
            step.setCompletedAt(java.time.LocalDateTime.now());
            stepRepository.save(step);

            log.info("ExecutorAgent: Step {} completed for task {}", stepIndex, task.getId());

            return StepResult.builder()
                    .success(result.isSuccess())
                    .output(result.getOutput())
                    .data(result.getData())
                    .error(result.getError())
                    .latencyMs(result.getLatencyMs())
                    .build();

        } catch (Exception e) {
            step.setStatus(StepStatus.FAILED);
            step.setErrorMessage(e.getMessage());
            stepRepository.save(step);

            log.error("ExecutorAgent: Step {} failed for task {}: {}",
                    stepIndex, task.getId(), e.getMessage());

            return StepResult.builder()
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }

    private Map<String, Object> parseToolInput(Map<String, Object> stepDef) {
        Object input = stepDef.get("input");
        if (input instanceof Map) {
            return (Map<String, Object>) input;
        }
        return Map.of();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to serialize Agent step data", e);
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class StepResult {
        private boolean success;
        private String output;
        private Object data;
        private String error;
        private Long latencyMs;
    }
}
