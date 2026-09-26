package com.testpilot.agent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentStepRepository extends JpaRepository<AgentStep, Long> {
    List<AgentStep> findByTaskIdOrderByStepIndex(Long taskId);
    long countByTaskIdAndStatus(Long taskId, String status);
}
