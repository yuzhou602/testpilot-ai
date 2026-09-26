package com.testpilot.agent.memory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentMemoryService {

    private final AgentMemoryRepository memoryRepository;

    public AgentMemory save(Long projectId, String memoryType, String content, String metadata) {
        AgentMemory memory = AgentMemory.builder()
                .projectId(projectId)
                .memoryType(memoryType)
                .content(content)
                .metadata(metadata)
                .build();
        return memoryRepository.save(memory);
    }

    public List<AgentMemory> getProjectMemory(Long projectId) {
        return memoryRepository.findByProjectIdOrderByRelevanceScoreDescCreatedAtDesc(projectId);
    }

    public List<AgentMemory> getByType(Long projectId, String memoryType) {
        return memoryRepository.findByProjectIdAndMemoryTypeOrderByRelevanceScoreDescCreatedAtDesc(projectId, memoryType);
    }

    public String getMemoryContext(Long projectId) {
        List<AgentMemory> memories = getProjectMemory(projectId);
        if (memories.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("## Project Memory\n");
        for (AgentMemory m : memories.stream().limit(20).toList()) {
            sb.append("- [").append(m.getMemoryType()).append("] ").append(m.getContent()).append("\n");
        }
        return sb.toString();
    }
}
