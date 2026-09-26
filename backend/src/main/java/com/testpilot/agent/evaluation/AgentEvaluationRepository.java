package com.testpilot.agent.evaluation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentEvaluationRepository extends JpaRepository<AgentEvaluation, Long> {
    List<AgentEvaluation> findByProjectIdOrderByEvaluatedAtDesc(Long projectId);
    List<AgentEvaluation> findByTaskId(Long taskId);
}
