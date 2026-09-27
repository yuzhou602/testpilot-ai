package com.testpilot.agent.multi;

import com.testpilot.agent.AgentTask;
import com.testpilot.agent.AgentTaskRepository;
import com.testpilot.agent.memory.ChatMemoryService;
import com.testpilot.agent.planner.AgentPlan;
import com.testpilot.common.enums.AgentTaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiAgentOrchestratorTest {

    @InjectMocks
    private MultiAgentOrchestrator orchestrator;

    @Mock
    private PlannerAgent plannerAgent;

    @Mock
    private ExecutorAgent executorAgent;

    @Mock
    private ReviewerAgent reviewerAgent;

    @Mock
    private AgentTaskRepository taskRepository;

    @Mock
    private ChatMemoryService chatMemoryService;

    private AgentTask testTask;
    private AgentPlan testPlan;

    @BeforeEach
    void setUp() {
        testTask = AgentTask.builder()
                .id(1L)
                .projectId(1L)
                .userId(1L)
                .goal("Test user login")
                .status(AgentTaskStatus.RUNNING)
                .build();

        testPlan = AgentPlan.builder()
                .strategy("Test login functionality")
                .scenarios(List.of())
                .steps(List.of(
                        Map.of("stepName", "Step 1", "stepType", "execute", "tool", "executeHttpRequest"),
                        Map.of("stepName", "Step 2", "stepType", "execute", "tool", "executeHttpRequest")
                ))
                .build();
    }

    @Test
    void shouldExecuteSuccessfully() {
        when(plannerAgent.plan(any(), any())).thenReturn(testPlan);

        when(executorAgent.executeStep(any(), any(), any(), any()))
                .thenReturn(ExecutorAgent.StepResult.builder()
                        .success(true)
                        .output("Success")
                        .build());

        when(reviewerAgent.review(any(), any(), any(), any()))
                .thenReturn(ReviewerAgent.ReviewResult.builder()
                        .passed(2)
                        .failed(0)
                        .total(2)
                        .passRate(1.0)
                        .shouldContinue(true)
                        .summary("2/2 passed")
                        .build());

        MultiAgentOrchestrator.OrchestratorResult result = orchestrator.execute(testTask);

        assertNotNull(result);
        assertEquals(2, result.getPassed());
        assertEquals(0, result.getFailed());
    }

    @Test
    void shouldHandleFailureAndReplan() {
        when(plannerAgent.plan(any(), any())).thenReturn(testPlan);
        when(plannerAgent.replan(any(), any(), any())).thenReturn(testPlan);

        when(executorAgent.executeStep(any(), any(), any(), any()))
                .thenReturn(ExecutorAgent.StepResult.builder()
                        .success(false)
                        .error("Test failed")
                        .build());

        when(reviewerAgent.review(any(), any(), any(), any()))
                .thenReturn(ReviewerAgent.ReviewResult.builder()
                        .passed(0)
                        .failed(1)
                        .total(1)
                        .passRate(0.0)
                        .shouldContinue(false)
                        .shouldReplan(true)
                        .summary("0/1 passed")
                        .build());

        when(reviewerAgent.analyzeFailure(any(), any(), any(), any(), any()))
                .thenReturn(com.testpilot.agent.runtime.FailureAnalyzer.FailureAnalysis.builder()
                        .failureType("TEST_ERROR")
                        .rootCause("Test error")
                        .evidence("Evidence")
                        .confidence(0.8)
                        .shouldRetry(false)
                        .createBug(false)
                        .build());

        MultiAgentOrchestrator.OrchestratorResult result = orchestrator.execute(testTask);

        assertNotNull(result);
        assertTrue(result.getReplanAttempts() > 0);
    }
}
