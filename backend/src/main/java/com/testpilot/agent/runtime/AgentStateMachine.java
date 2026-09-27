package com.testpilot.agent.runtime;

import com.testpilot.common.enums.AgentTaskStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class AgentStateMachine {

    private static final Map<AgentTaskStatus, Set<AgentTaskStatus>> TRANSITIONS = Map.ofEntries(
            Map.entry(AgentTaskStatus.CREATED, Set.of(AgentTaskStatus.ANALYZING, AgentTaskStatus.CANCELLED)),
            Map.entry(AgentTaskStatus.ANALYZING, Set.of(AgentTaskStatus.PLANNING, AgentTaskStatus.FAILED, AgentTaskStatus.CANCELLED)),
            Map.entry(AgentTaskStatus.PLANNING, Set.of(AgentTaskStatus.READY, AgentTaskStatus.FAILED, AgentTaskStatus.CANCELLED)),
            Map.entry(AgentTaskStatus.READY, Set.of(AgentTaskStatus.RUNNING, AgentTaskStatus.CANCELLED)),
            Map.entry(AgentTaskStatus.RUNNING, Set.of(
                    AgentTaskStatus.WAITING_TOOL, AgentTaskStatus.WAITING_USER,
                    AgentTaskStatus.ANALYZING_FAILURE, AgentTaskStatus.COMPLETED,
                    AgentTaskStatus.REPLANNING, AgentTaskStatus.FAILED
            )),
            Map.entry(AgentTaskStatus.WAITING_TOOL, Set.of(AgentTaskStatus.RUNNING, AgentTaskStatus.FAILED)),
            Map.entry(AgentTaskStatus.WAITING_USER, Set.of(AgentTaskStatus.RUNNING, AgentTaskStatus.CANCELLED)),
            Map.entry(AgentTaskStatus.REPLANNING, Set.of(AgentTaskStatus.RUNNING, AgentTaskStatus.FAILED)),
            Map.entry(AgentTaskStatus.ANALYZING_FAILURE, Set.of(AgentTaskStatus.REPLANNING, AgentTaskStatus.COMPLETED, AgentTaskStatus.FAILED)),
            Map.entry(AgentTaskStatus.COMPLETED, Set.of()),
            Map.entry(AgentTaskStatus.FAILED, Set.of(AgentTaskStatus.REPLANNING, AgentTaskStatus.CANCELLED)),
            Map.entry(AgentTaskStatus.CANCELLED, Set.of())
    );

    public boolean canTransition(AgentTaskStatus from, AgentTaskStatus to) {
        Set<AgentTaskStatus> allowed = TRANSITIONS.getOrDefault(from, Set.of());
        boolean valid = allowed.contains(to);
        if (!valid) {
            log.warn("Invalid state transition: {} -> {}", from, to);
        }
        return valid;
    }

    public AgentTaskStatus transition(AgentTaskStatus current, AgentTaskStatus next) {
        if (!canTransition(current, next)) {
            throw new IllegalStateException("Cannot transition from " + current + " to " + next);
        }
        log.info("Agent state: {} -> {}", current, next);
        return next;
    }
}
