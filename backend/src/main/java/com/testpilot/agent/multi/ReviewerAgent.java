package com.testpilot.agent.multi;

import com.testpilot.agent.AgentTask;
import com.testpilot.agent.planner.AgentPlan;
import com.testpilot.agent.runtime.FailureAnalyzer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewerAgent {

    private final FailureAnalyzer failureAnalyzer;

    public ReviewResult review(AgentTask task, AgentPlan plan, List<Map<String, Object>> results, AgentContext context) {
        log.info("ReviewerAgent: Reviewing results for task {}", task.getId());

        int passed = 0;
        int failed = 0;
        int total = results.size();

        for (Map<String, Object> result : results) {
            if (Boolean.TRUE.equals(result.get("success"))) {
                passed++;
            } else {
                failed++;
            }
        }

        double passRate = total > 0 ? (double) passed / total : 0;

        boolean shouldContinue = passRate >= 0.8;
        boolean shouldReplan = failed > 0 && passRate < 0.8;

        String summary = String.format("Passed: %d/%d (%.1f%%)", passed, total, passRate * 100);

        log.info("ReviewerAgent: Review complete for task {}: {}", task.getId(), summary);

        return ReviewResult.builder()
                .passed(passed)
                .failed(failed)
                .total(total)
                .passRate(passRate)
                .shouldContinue(shouldContinue)
                .shouldReplan(shouldReplan)
                .summary(summary)
                .build();
    }

    public FailureAnalyzer.FailureAnalysis analyzeFailure(AgentTask task, String stepName,
                                                           String stepInput, String stepOutput,
                                                           String errorMessage) {
        log.info("ReviewerAgent: Analyzing failure for task {} at step {}", task.getId(), stepName);

        FailureAnalyzer.FailureAnalysis analysis = failureAnalyzer.analyze(
                stepName, stepInput, stepOutput, errorMessage);

        log.info("ReviewerAgent: Failure analysis complete - type: {}, confidence: {:.2f}",
                analysis.getFailureType(), analysis.getConfidence());

        return analysis;
    }

    @lombok.Data
    @lombok.Builder
    public static class ReviewResult {
        private int passed;
        private int failed;
        private int total;
        private double passRate;
        private boolean shouldContinue;
        private boolean shouldReplan;
        private String summary;
    }
}
