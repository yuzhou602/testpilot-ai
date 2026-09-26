package com.testpilot.agent.multi;

import com.testpilot.agent.AgentTask;
import com.testpilot.agent.AgentTaskRepository;
import com.testpilot.agent.memory.ChatMemoryService;
import com.testpilot.agent.planner.AgentPlan;
import com.testpilot.agent.runtime.FailureAnalyzer;
import com.testpilot.common.enums.AgentTaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MultiAgentOrchestrator {

    private final PlannerAgent plannerAgent;
    private final ExecutorAgent executorAgent;
    private final ReviewerAgent reviewerAgent;
    private final AgentTaskRepository taskRepository;
    private final ChatMemoryService chatMemoryService;

    private static final int MAX_REPLAN_ATTEMPTS = 3;

    public OrchestratorResult execute(AgentTask task) {
        log.info("MultiAgentOrchestrator: Starting execution for task {}", task.getId());

        AgentContext context = AgentContext.create(task.getProjectId(), task.getId());
        List<Map<String, Object>> allResults = new ArrayList<>();
        int replanAttempts = 0;

        chatMemoryService.addMessage(task.getId(), "system",
                "Multi-Agent execution started for: " + task.getGoal());

        // Phase 1: Planning
        AgentPlan plan = plannerAgent.plan(task, context);

        chatMemoryService.addMessage(task.getId(), "assistant",
                "Plan created with " + plan.getSteps().size() + " steps");

        // Phase 2: Execution loop
        boolean shouldContinue = true;
        int stepIndex = 0;

        while (shouldContinue && stepIndex < plan.getSteps().size()) {
            // Execute step
            ExecutorAgent.StepResult result = executorAgent.executeStep(task, plan, stepIndex, context);
            allResults.add(Map.of(
                    "stepIndex", stepIndex,
                    "success", result.isSuccess(),
                    "output", result.getOutput() != null ? result.getOutput() : "",
                    "error", result.getError() != null ? result.getError() : ""
            ));

            chatMemoryService.addMessage(task.getId(), "assistant",
                    "Step " + stepIndex + ": " + (result.isSuccess() ? "SUCCESS" : "FAILED"));

            // Review results
            ReviewerAgent.ReviewResult review = reviewerAgent.review(task, plan, allResults, context);

            if (!result.isSuccess()) {
                // Analyze failure
                FailureAnalyzer.FailureAnalysis analysis = reviewerAgent.analyzeFailure(
                        task,
                        (String) plan.getSteps().get(stepIndex).get("stepName"),
                        plan.getSteps().get(stepIndex).toString(),
                        result.getOutput(),
                        result.getError()
                );

                chatMemoryService.addMessage(task.getId(), "system",
                        "Failure analysis: " + analysis.getFailureType() +
                                " - " + analysis.getRootCause());

                // Decide: replan or continue
                if (review.isShouldReplan() && replanAttempts < MAX_REPLAN_ATTEMPTS) {
                    replanAttempts++;
                    log.info("Replanning attempt {} for task {}", replanAttempts, task.getId());

                    chatMemoryService.addMessage(task.getId(), "system",
                            "Replanning attempt " + replanAttempts);

                    plan = plannerAgent.replan(task, context, analysis.getRootCause());
                    stepIndex = 0;
                    allResults.clear();
                    continue;
                }
            }

            stepIndex++;
            shouldContinue = review.isShouldContinue();
        }

        // Phase 3: Final review
        ReviewerAgent.ReviewResult finalReview = reviewerAgent.review(task, plan, allResults, context);

        chatMemoryService.addMessage(task.getId(), "assistant",
                "Execution complete: " + finalReview.getSummary());

        log.info("MultiAgentOrchestrator: Execution complete for task {}: {}",
                task.getId(), finalReview.getSummary());

        return OrchestratorResult.builder()
                .plan(plan)
                .results(allResults)
                .passed(finalReview.getPassed())
                .failed(finalReview.getFailed())
                .total(finalReview.getTotal())
                .passRate(finalReview.getPassRate())
                .replanAttempts(replanAttempts)
                .summary(finalReview.getSummary())
                .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class OrchestratorResult {
        private AgentPlan plan;
        private List<Map<String, Object>> results;
        private int passed;
        private int failed;
        private int total;
        private double passRate;
        private int replanAttempts;
        private String summary;
    }
}
