package com.testpilot.testcase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    List<TestCase> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    List<TestCase> findByScenarioId(Long scenarioId);
    long countByProjectId(Long projectId);
}
