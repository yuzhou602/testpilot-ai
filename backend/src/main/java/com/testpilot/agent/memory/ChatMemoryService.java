package com.testpilot.agent.memory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMemoryService {

    private final AgentMemoryRepository memoryRepository;
    private final ObjectMapper objectMapper;

    private static final int MAX_CONTEXT_MESSAGES = 50;
    private static final int MAX_CONTEXT_TOKENS = 12000;

    private final Map<Long, List<ChatMessage>> conversationCache = new ConcurrentHashMap<>();

    public void addMessage(Long taskId, String role, String content) {
        List<ChatMessage> messages = conversationCache.computeIfAbsent(taskId, k -> new ArrayList<>());
        messages.add(ChatMessage.builder()
                .role(role)
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build());

        if (messages.size() > MAX_CONTEXT_MESSAGES) {
            messages.subList(0, messages.size() - MAX_CONTEXT_MESSAGES).clear();
        }
    }

    public List<ChatMessage> getConversationHistory(Long taskId) {
        return conversationCache.getOrDefault(taskId, new ArrayList<>());
    }

    public String getConversationContext(Long taskId) {
        List<ChatMessage> messages = conversationCache.getOrDefault(taskId, new ArrayList<>());
        if (messages.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("## Conversation History\n");
        for (ChatMessage msg : messages) {
            sb.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
        }
        return sb.toString();
    }

    public String buildPromptWithMemory(Long taskId, Long projectId, String systemPrompt, String userPrompt) {
        StringBuilder prompt = new StringBuilder();

        String memoryContext = getMemoryContext(projectId);
        if (!memoryContext.isEmpty()) {
            prompt.append(memoryContext).append("\n\n");
        }

        String conversationContext = getConversationContext(taskId);
        if (!conversationContext.isEmpty()) {
            prompt.append(conversationContext).append("\n\n");
        }

        prompt.append("## Current Request\n").append(userPrompt);
        return prompt.toString();
    }

    public void save(Long projectId, String memoryType, String content, String metadata) {
        AgentMemory memory = AgentMemory.builder()
                .projectId(projectId)
                .memoryType(memoryType)
                .content(content)
                .metadata(metadata)
                .build();
        memoryRepository.save(memory);
    }

    public List<AgentMemory> getProjectMemory(Long projectId) {
        return memoryRepository.findByProjectIdOrderByRelevanceScoreDescCreatedAtDesc(projectId);
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

    public void clearConversation(Long taskId) {
        conversationCache.remove(taskId);
    }

    public void clearAll() {
        conversationCache.clear();
    }

    @lombok.Data
    @lombok.Builder
    public static class ChatMessage {
        private String role;
        private String content;
        private long timestamp;
    }
}
