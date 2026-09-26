package com.testpilot.agent.runtime;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testpilot.agent.AgentTask;
import com.testpilot.agent.AgentStep;
import com.testpilot.agent.AgentStepRepository;
import com.testpilot.agent.AgentTaskRepository;
import com.testpilot.agent.memory.AgentMemoryService;
import com.testpilot.agent.memory.ChatMemoryService;
import com.testpilot.agent.planner.AgentPlan;
import com.testpilot.agent.planner.AgentPlanner;
import com.testpilot.agent.tool.AgentTool;
import com.testpilot.agent.tool.AgentToolRegistry;
import com.testpilot.agent.trace.AgentTraceRecorder;
import com.testpilot.common.enums.AgentTaskStatus;
import com.testpilot.common.enums.SseEventType;
import com.testpilot.common.enums.StepStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentExecutor {

    private final AgentTaskRepository taskRepository;
    private final AgentStepRepository stepRepository;
    private final AgentPlanner planner;
    private final AgentToolRegistry toolRegistry;
    private final AgentTraceRecorder traceRecorder;
    private final AgentMemoryService memoryService;
    private final AgentStateMachine stateMachine;
    private final StepExecutor stepExecutor;
    private final FailureAnalyzer failureAnalyzer;
    private final ChatMemoryService chatMemoryService;
    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> runningTasks = new ConcurrentHashMap<>();

    private static final int MAX_REPLAN_ATTEMPTS = 3;

    public void registerEmitter(Long taskId, SseEmitter emitter) {
        emitters.put(taskId, emitter);
        emitter.onCompletion(() -> emitters.remove(taskId));
        emitter.onTimeout(() -> emitters.remove(taskId));
        emitter.onError(e -> emitters.remove(taskId));
    }

    public AgentTask createTask(Long projectId, Long userId, String goal, String context) {
        AgentTask task = AgentTask.builder()
                .projectId(projectId)
                .userId(userId)
                .goal(goal)
                .context(context)
                .status(AgentTaskStatus.CREATED)
                .currentStepIndex(0)
                .totalSteps(0)
                .currentRetry(0)
                .totalTokens(0L)
                .totalLatencyMs(0L)
                .waitingForUser(false)
                .build();
        return taskRepository.save(task);
    }

    public void executeTask(Long taskId) {
        executor.submit(() -> doExecute(taskId));
    }

    private void doExecute(Long taskId) {
        AgentTask task = taskRepository.findById(taskId).orElseThrow();
        runningTasks.put(taskId, true);

        try {
            chatMemoryService.addMessage(taskId, "system", "Task started: " + task.getGoal());

            // Phase 1: ANALYZING
            transition(task, AgentTaskStatus.ANALYZING);
            sendEvent(taskId, SseEventType.TASK_STARTED, Map.of("taskId", taskId, "goal", task.getGoal()));
            traceRecorder.record(taskId, 0, "TASK_STARTED", task.getGoal(), null, null, "STARTED", null, null, null, null);

            // Phase 2: PLANNING
            transition(task, AgentTaskStatus.PLANNING);
            AgentPlan plan = planner.createPlan(task);
            task.setPlanJson(objectMapper.writeValueAsString(plan));
            task.setTotalSteps(plan.getSteps().size());
            transition(task, AgentTaskStatus.READY);
            sendEvent(taskId, SseEventType.PLAN_CREATED, Map.of(
                    "strategy", plan.getStrategy(),
                    "scenarios", plan.getScenarios().size(),
                    "steps", plan.getSteps().size()
            ));
            traceRecorder.record(taskId, 1, "PLAN_CREATED",
                    plan.getStrategy(), objectMapper.writeValueAsString(plan.getSteps()),
                    null, "COMPLETED", null, null, null, null);

            // Phase 3: Execute steps with multi-round loop
            transition(task, AgentTaskStatus.RUNNING);
            List<Map<String, Object>> completedResults = new ArrayList<>();
            boolean allPassed = true;
            int failedCount = 0;
            int replanAttempts = 0;

            int i = 0;
            while (i < plan.getSteps().size() && runningTasks.getOrDefault(taskId, false)) {
                Map<String, Object> stepDef = plan.getSteps().get(i);
                task.setCurrentStepIndex(i);
                taskRepository.save(task);

                AgentStep step = AgentStep.builder()
                        .taskId(taskId)
                        .stepIndex(i)
                        .stepName((String) stepDef.getOrDefault("stepName", "Step " + (i + 1)))
                        .stepType((String) stepDef.getOrDefault("stepType", "execute"))
                        .toolName((String) stepDef.get("tool"))
                        .status(StepStatus.RUNNING)
                        .input(objectMapper.writeValueAsString(stepDef))
                        .build();
                step = stepRepository.save(step);

                sendEvent(taskId, SseEventType.STEP_STARTED, Map.of(
                        "stepIndex", i, "stepName", step.getStepName(), "tool", step.getToolName()));

                try {
                    // Execute step
                    Map<String, Object> toolInput = parseToolInput(stepDef);
                    AgentTool.ToolContext ctx = AgentTool.ToolContext.builder()
                            .taskId(taskId)
                            .projectId(task.getProjectId())
                            .taskStatus(task.getStatus())
                            .build();

                    StepExecutor.StepResult stepResult = stepExecutor.execute(step.getToolName(), toolInput, ctx);

                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("success", stepResult.isSuccess());
                    resultMap.put("output", stepResult.getOutput());
                    resultMap.put("data", stepResult.getData());
                    resultMap.put("error", stepResult.getError());
                    resultMap.put("latencyMs", stepResult.getLatencyMs());

                    chatMemoryService.addMessage(taskId, "assistant",
                            "Step " + i + ": " + step.getStepName() + " - " +
                                    (stepResult.isSuccess() ? "SUCCESS" : "FAILED") +
                                    (stepResult.getOutput() != null ? " | " + stepResult.getOutput() : ""));

                    step.setStatus(StepStatus.COMPLETED);
                    step.setOutput(objectMapper.writeValueAsString(resultMap));
                    step.setLatencyMs(stepResult.getLatencyMs());
                    step.setCompletedAt(java.time.LocalDateTime.now());
                    stepRepository.save(step);

                    completedResults.add(resultMap);
                    sendEvent(taskId, SseEventType.STEP_COMPLETED, Map.of(
                            "stepIndex", i, "result", resultMap));

                    traceRecorder.record(taskId, i + 2, "STEP_COMPLETED",
                            step.getInput(), step.getOutput(),
                            step.getToolName(), "COMPLETED",
                            step.getLatencyMs(), step.getTokensUsed(),
                            step.getModel(), null);

                    // Check if this step found a failure
                    if (!stepResult.isSuccess() || (stepResult.getOutput() != null && stepResult.getOutput().contains("FAIL"))) {
                        failedCount++;
                        allPassed = false;

                        // Enter failure analysis
                        transition(task, AgentTaskStatus.ANALYZING_FAILURE);
                        sendEvent(taskId, SseEventType.FAILURE_ANALYSIS, Map.of("stepIndex", i));

                        FailureAnalyzer.FailureAnalysis analysis = failureAnalyzer.analyze(
                                step.getStepName(),
                                step.getInput(),
                                step.getOutput(),
                                stepResult.getError()
                        );

                        chatMemoryService.addMessage(taskId, "system",
                                "Failure analysis: " + analysis.getFailureType() +
                                        " - " + analysis.getRootCause() +
                                        " (confidence: " + analysis.getConfidence() + ")");

                        // Create bug if needed
                        if (analysis.isCreateBug() && analysis.getBugData() != null) {
                            Map<String, Object> bugResult = executeTool(task, "createBug", analysis.getBugData());
                            sendEvent(taskId, SseEventType.BUG_CREATED, bugResult);
                        }

                        // Decide: retry or replan
                        if (analysis.isShouldRetry() && task.getCurrentRetry() < task.getMaxRetry()) {
                            task.setCurrentRetry(task.getCurrentRetry() + 1);
                            taskRepository.save(task);
                            sendEvent(taskId, SseEventType.AGENT_THINKING, Map.of(
                                    "message", "Retrying step " + i + " (attempt " + task.getCurrentRetry() + ")"));
                            transition(task, AgentTaskStatus.RUNNING);
                            continue; // Retry same step
                        } else if (replanAttempts < MAX_REPLAN_ATTEMPTS) {
                            replanAttempts++;
                            sendEvent(taskId, SseEventType.AGENT_THINKING, Map.of(
                                    "message", "Replanning after failure at step " + i));

                            // Ask LLM to replan
                            AgentDecision decision = makeReplanDecision(task, completedResults, analysis);
                            if (decision.getAction() == AgentDecision.DecisionAction.REPLAN) {
                                // Update plan from decision
                                transition(task, AgentTaskStatus.PLANNING);
                                plan = planner.createPlan(task);
                                task.setPlanJson(objectMapper.writeValueAsString(plan));
                                task.setTotalSteps(plan.getSteps().size());
                                task.setCurrentRetry(0);
                                taskRepository.save(task);
                                transition(task, AgentTaskStatus.RUNNING);
                                i = 0;
                                continue;
                            }
                        }

                        transition(task, AgentTaskStatus.RUNNING);
                    }

                    // Check for waiting user
                    if (stepDef.containsKey("waitForUser") && Boolean.TRUE.equals(stepDef.get("waitForUser"))) {
                        transition(task, AgentTaskStatus.WAITING_USER);
                        task.setWaitingForUser(true);
                        taskRepository.save(task);
                        sendEvent(taskId, SseEventType.WAITING_USER, resultMap);
                        return; // Wait for user response
                    }

                } catch (Exception e) {
                    step.setStatus(StepStatus.FAILED);
                    step.setErrorMessage(e.getMessage());
                    stepRepository.save(step);

                    sendEvent(taskId, SseEventType.STEP_FAILED, Map.of(
                            "stepIndex", i, "error", e.getMessage()));
                    traceRecorder.record(taskId, i + 2, "STEP_FAILED",
                            step.getInput(), null, step.getToolName(),
                            "FAILED", null, null, null, e.getMessage());

                    chatMemoryService.addMessage(taskId, "system",
                            "Step " + i + " failed: " + e.getMessage());

                    failedCount++;
                    allPassed = false;
                }

                i++;
            }

            // Phase 4: Complete
            task.setTotalTokens(calculateTotalTokens(taskId));
            task.setTotalLatencyMs(calculateTotalLatency(taskId));
            task.setResultSummary(String.format("Completed %d steps. Passed: %d, Failed: %d",
                    plan.getSteps().size(), plan.getSteps().size() - failedCount, failedCount));
            task.setCompletedAt(java.time.LocalDateTime.now());
            transition(task, AgentTaskStatus.COMPLETED);
            taskRepository.save(task);

            sendEvent(taskId, SseEventType.TASK_COMPLETED, Map.of(
                    "summary", task.getResultSummary(),
                    "totalTokens", task.getTotalTokens(),
                    "totalLatencyMs", task.getTotalLatencyMs()
            ));
            traceRecorder.record(taskId, plan.getSteps().size() + 2, "TASK_COMPLETED",
                    null, task.getResultSummary(), null, "COMPLETED",
                    task.getTotalLatencyMs(), task.getTotalTokens(), null, null);

            // Save to memory
            memoryService.save(task.getProjectId(), "task_result",
                    "Goal: " + task.getGoal() + " | Result: " + task.getResultSummary(),
                    objectMapper.writeValueAsString(Map.of("failedCount", failedCount, "allPassed", allPassed)));

            // Save conversation to memory
            String conversationContext = chatMemoryService.getConversationContext(taskId);
            if (!conversationContext.isEmpty()) {
                memoryService.save(task.getProjectId(), "conversation",
                        "Task " + taskId + " conversation: " + conversationContext,
                        objectMapper.writeValueAsString(Map.of("taskId", taskId, "steps", plan.getSteps().size())));
            }

            chatMemoryService.clearConversation(taskId);

        } catch (Exception e) {
            log.error("Agent execution failed for task {}", taskId, e);
            try {
                transition(task, AgentTaskStatus.FAILED);
                task.setResultSummary("Execution failed: " + e.getMessage());
                taskRepository.save(task);
                sendEvent(taskId, SseEventType.TASK_FAILED, Map.of("error", e.getMessage()));
            } catch (Exception ex) {
                log.error("Failed to update task status", ex);
            }
        } finally {
            runningTasks.remove(taskId);
            emitters.remove(taskId);
        }
    }

    private AgentDecision makeReplanDecision(AgentTask task, List<Map<String, Object>> results,
                                              FailureAnalyzer.FailureAnalysis analysis) {
        try {
            String prompt = """
                Current task goal: %s
                Completed steps: %d
                Last failure analysis:
                  - Type: %s
                  - Root cause: %s
                  - Confidence: %.2f

                Based on this failure, should we:
                1. Continue with the current plan (EXECUTE_STEP)
                2. Skip this step and move on (SKIP_STEP)
                3. Create a completely new plan (REPLAN)

                Respond with JSON:
                {"action": "EXECUTE_STEP|SKIP_STEP|REPLAN", "reasoning": "why"}
                """.formatted(
                    task.getGoal(),
                    results.size(),
                    analysis.getFailureType(),
                    analysis.getRootCause(),
                    analysis.getConfidence()
            );

            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt()
                    .system("You are TestPilot AI deciding next actions. Respond with valid JSON only.")
                    .user(prompt)
                    .call()
                    .content();

            String json = extractJson(response);
            Map<String, Object> decisionMap = objectMapper.readValue(json, new TypeReference<>() {});

            String actionStr = decisionMap.getOrDefault("action", "EXECUTE_STEP").toString();
            AgentDecision.DecisionAction action;
            try {
                action = AgentDecision.DecisionAction.valueOf(actionStr);
            } catch (Exception e) {
                action = AgentDecision.DecisionAction.EXECUTE_STEP;
            }

            return AgentDecision.builder()
                    .action(action)
                    .reasoning(decisionMap.getOrDefault("reasoning", "").toString())
                    .build();

        } catch (Exception e) {
            return AgentDecision.builder()
                    .action(AgentDecision.DecisionAction.EXECUTE_STEP)
                    .reasoning("Replan decision failed, continuing: " + e.getMessage())
                    .build();
        }
    }

    private Map<String, Object> executeTool(AgentTask task, String toolName, Map<String, Object> toolInput) {
        try {
            AgentTool tool = toolRegistry.getTool(toolName);
            AgentTool.ToolContext ctx = AgentTool.ToolContext.builder()
                    .taskId(task.getId())
                    .projectId(task.getProjectId())
                    .taskStatus(task.getStatus())
                    .build();

            long start = System.currentTimeMillis();
            AgentTool.ToolResult result = tool.execute(ctx, toolInput);
            long latency = System.currentTimeMillis() - start;

            Map<String, Object> output = new HashMap<>();
            output.put("success", result.isSuccess());
            output.put("output", result.getOutput());
            output.put("data", result.getData());
            output.put("error", result.getError());
            output.put("latencyMs", latency);

            return output;
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    private void transition(AgentTask task, AgentTaskStatus next) {
        AgentTaskStatus current = task.getStatus();
        if (stateMachine.canTransition(current, next)) {
            task.setStatus(next);
            taskRepository.save(task);
        } else {
            log.warn("Skipping invalid transition {} -> {}", current, next);
            task.setStatus(next);
            taskRepository.save(task);
        }
    }

    private void sendEvent(Long taskId, SseEventType eventType, Object data) {
        SseEmitter emitter = emitters.get(taskId);
        if (emitter != null) {
            try {
                Map<String, Object> event = Map.of(
                        "type", eventType.name(),
                        "data", data,
                        "timestamp", System.currentTimeMillis()
                );
                emitter.send(SseEmitter.event()
                        .name(eventType.name())
                        .data(objectMapper.writeValueAsString(event)));
            } catch (IOException e) {
                log.warn("Failed to send SSE event to task {}: {}", taskId, e.getMessage());
                emitters.remove(taskId);
            }
        }
    }

    private Map<String, Object> parseToolInput(Map<String, Object> stepDef) {
        Object input = stepDef.get("input");
        if (input instanceof Map) {
            return (Map<String, Object>) input;
        }
        if (input instanceof String) {
            try {
                return objectMapper.readValue((String) input, new TypeReference<>() {});
            } catch (Exception e) {
                return Map.of("query", input);
            }
        }
        return Map.of();
    }

    private String extractJson(String response) {
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) cleaned = cleaned.substring(7);
        else if (cleaned.startsWith("```")) cleaned = cleaned.substring(3);
        if (cleaned.endsWith("```")) cleaned = cleaned.substring(0, cleaned.length() - 3);
        return cleaned.trim();
    }

    private Long calculateTotalTokens(Long taskId) {
        return stepRepository.findByTaskIdOrderByStepIndex(taskId).stream()
                .filter(s -> s.getTokensUsed() != null)
                .mapToLong(AgentStep::getTokensUsed)
                .sum();
    }

    private Long calculateTotalLatency(Long taskId) {
        return stepRepository.findByTaskIdOrderByStepIndex(taskId).stream()
                .filter(s -> s.getLatencyMs() != null)
                .mapToLong(AgentStep::getLatencyMs)
                .sum();
    }

    public void cancelTask(Long taskId) {
        runningTasks.put(taskId, false);
        AgentTask task = taskRepository.findById(taskId).orElseThrow();
        task.setStatus(AgentTaskStatus.CANCELLED);
        taskRepository.save(task);
        sendEvent(taskId, SseEventType.TASK_FAILED, Map.of("reason", "Cancelled by user"));
    }
}
