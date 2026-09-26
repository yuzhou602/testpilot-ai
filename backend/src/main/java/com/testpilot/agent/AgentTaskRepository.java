package com.testpilot.agent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentTaskRepository extends JpaRepository<AgentTask, Long> {
    List<AgentTask> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    List<AgentTask> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<AgentTask> findByStatus(String status);
    long countByProjectId(Long projectId);
}
