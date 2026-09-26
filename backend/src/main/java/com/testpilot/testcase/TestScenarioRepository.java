package com.testpilot.testcase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestScenarioRepository extends JpaRepository<TestScenario, Long> {
    List<TestScenario> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    List<TestScenario> findByRequirementId(Long requirementId);
}
