package com.testpilot.agent.multi;

import com.testpilot.agent.AgentTask;
import com.testpilot.agent.planner.AgentPlan;
import com.testpilot.agent.planner.AgentPlanner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlannerAgent {

    private final AgentPlanner agentPlanner;

    public AgentPlan plan(AgentTask task, AgentContext context) {
        log.info("PlannerAgent: Creating plan for task {}", task.getId());

        AgentPlan plan = agentPlanner.createPlan(task);

        log.info("PlannerAgent: Created plan with {} steps for task {}",
                plan.getSteps().size(), task.getId());

        return plan;
    }

    public AgentPlan replan(AgentTask task, AgentContext context, String failureReason) {
        log.info("PlannerAgent: Replanning for task {} due to: {}", task.getId(), failureReason);

        // Modify the task goal to include failure context
        AgentTask modifiedTask = AgentTask.builder()
                .id(task.getId())
                .projectId(task.getProjectId())
                .userId(task.getUserId())
                .goal(task.getGoal() + "\n\nPrevious attempt failed: " + failureReason + "\nPlease create a new plan avoiding this issue.")
                .context(task.getContext())
                .status(task.getStatus())
                .build();

        AgentPlan plan = agentPlanner.createPlan(modifiedTask);

        log.info("PlannerAgent: Replanned with {} steps for task {}",
                plan.getSteps().size(), task.getId());

        return plan;
    }
}
