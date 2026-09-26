package com.testpilot.agent.memory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentMemoryRepository extends JpaRepository<AgentMemory, Long> {
    List<AgentMemory> findByProjectIdAndMemoryTypeOrderByRelevanceScoreDescCreatedAtDesc(Long projectId, String memoryType);
    List<AgentMemory> findByProjectIdOrderByRelevanceScoreDescCreatedAtDesc(Long projectId);
}
