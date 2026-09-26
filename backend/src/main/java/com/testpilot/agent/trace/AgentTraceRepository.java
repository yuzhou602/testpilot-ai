package com.testpilot.agent.trace;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentTraceRepository extends JpaRepository<AgentTrace, Long> {
    List<AgentTrace> findByTaskIdOrderByStepIndexCreatedAt(Long taskId);
}
