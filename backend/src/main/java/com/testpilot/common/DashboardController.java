package com.testpilot.common;

import com.testpilot.agent.AgentTaskRepository;
import com.testpilot.agent.AgentStepRepository;
import com.testpilot.bug.TestBugRepository;
import com.testpilot.execution.TestExecutionRepository;
import com.testpilot.testcase.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final AgentTaskRepository taskRepository;
    private final AgentStepRepository stepRepository;
    private final TestBugRepository bugRepository;
    private final TestExecutionRepository executionRepository;
    private final TestCaseRepository testCaseRepository;

    @GetMapping("/stats/{projectId}")
    public Map<String, Object> getProjectStats(@PathVariable Long projectId) {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalTasks", taskRepository.countByProjectId(projectId));
        stats.put("totalTestCases", testCaseRepository.countByProjectId(projectId));
        stats.put("totalBugs", bugRepository.countByProjectId(projectId));
        stats.put("blockerBugs", bugRepository.countByProjectIdAndSeverity(projectId, "BLOCKER"));
        stats.put("criticalBugs", bugRepository.countByProjectIdAndSeverity(projectId, "CRITICAL"));
        stats.put("majorBugs", bugRepository.countByProjectIdAndSeverity(projectId, "MAJOR"));
        stats.put("passedTests", executionRepository.countByProjectIdAndStatus(projectId, "PASSED"));
        stats.put("failedTests", executionRepository.countByProjectIdAndStatus(projectId, "FAILED"));
        stats.put("errorTests", executionRepository.countByProjectIdAndStatus(projectId, "ERROR"));

        return stats;
    }
}
