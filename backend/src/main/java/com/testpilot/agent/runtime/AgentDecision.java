package com.testpilot.agent.runtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentDecision {

    private DecisionAction action;

    private String reasoning;

    private Integer nextStepIndex;

    private Map<String, Object> toolInput;

    private boolean shouldRetry;

    private boolean createBug;

    private Map<String, Object> bugData;

    public enum DecisionAction {
        EXECUTE_STEP,
        SKIP_STEP,
        REPLAN,
        CREATE_BUG,
        WAIT_USER,
        COMPLETE,
        FAIL
    }
}
