package com.testpilot.execution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestExecutionRepository extends JpaRepository<TestExecution, Long> {
    List<TestExecution> findByTestCaseIdOrderByExecutedAtDesc(Long testCaseId);
    List<TestExecution> findByAgentTaskIdOrderByExecutedAtDesc(Long agentTaskId);
    long countByProjectIdAndStatus(Long projectId, String status);
}
