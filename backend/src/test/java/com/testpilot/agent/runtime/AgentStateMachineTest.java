package com.testpilot.agent.runtime;

import com.testpilot.common.enums.AgentTaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgentStateMachineTest {

    private AgentStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new AgentStateMachine();
    }

    @Test
    void shouldAllowValidTransition() {
        assertTrue(stateMachine.canTransition(AgentTaskStatus.CREATED, AgentTaskStatus.ANALYZING));
    }

    @Test
    void shouldAllowPlanningTransition() {
        assertTrue(stateMachine.canTransition(AgentTaskStatus.ANALYZING, AgentTaskStatus.PLANNING));
    }

    @Test
    void shouldAllowRunningTransition() {
        assertTrue(stateMachine.canTransition(AgentTaskStatus.PLANNING, AgentTaskStatus.RUNNING));
    }

    @Test
    void shouldNotAllowInvalidTransition() {
        assertFalse(stateMachine.canTransition(AgentTaskStatus.CREATED, AgentTaskStatus.COMPLETED));
    }

    @Test
    void shouldAllowFailureAnalysisTransition() {
        assertTrue(stateMachine.canTransition(AgentTaskStatus.RUNNING, AgentTaskStatus.ANALYZING_FAILURE));
    }

    @Test
    void shouldAllowWaitingUserTransition() {
        assertTrue(stateMachine.canTransition(AgentTaskStatus.RUNNING, AgentTaskStatus.WAITING_USER));
    }
}
