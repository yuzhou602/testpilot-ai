package com.testpilot.agent.memory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ChatMemoryServiceTest {

    @InjectMocks
    private ChatMemoryService chatMemoryService;

    @Mock
    private AgentMemoryRepository memoryRepository;

    @BeforeEach
    void setUp() {
        chatMemoryService.clearAll();
    }

    @Test
    void shouldAddMessage() {
        chatMemoryService.addMessage(1L, "user", "Hello");

        List<ChatMemoryService.ChatMessage> messages = chatMemoryService.getConversationHistory(1L);

        assertEquals(1, messages.size());
        assertEquals("user", messages.get(0).getRole());
        assertEquals("Hello", messages.get(0).getContent());
    }

    @Test
    void shouldMaintainMultipleMessages() {
        chatMemoryService.addMessage(1L, "user", "Hello");
        chatMemoryService.addMessage(1L, "assistant", "Hi there!");
        chatMemoryService.addMessage(1L, "user", "How are you?");

        List<ChatMemoryService.ChatMessage> messages = chatMemoryService.getConversationHistory(1L);

        assertEquals(3, messages.size());
    }

    @Test
    void shouldClearConversation() {
        chatMemoryService.addMessage(1L, "user", "Hello");
        chatMemoryService.clearConversation(1L);

        List<ChatMemoryService.ChatMessage> messages = chatMemoryService.getConversationHistory(1L);

        assertTrue(messages.isEmpty());
    }

    @Test
    void shouldGetConversationContext() {
        chatMemoryService.addMessage(1L, "user", "Test login");
        chatMemoryService.addMessage(1L, "assistant", "Testing login...");

        String context = chatMemoryService.getConversationContext(1L);

        assertNotNull(context);
        assertTrue(context.contains("Conversation History"));
        assertTrue(context.contains("user: Test login"));
    }

    @Test
    void shouldReturnEmptyContextForNoMessages() {
        String context = chatMemoryService.getConversationContext(1L);

        assertEquals("", context);
    }

    @Test
    void shouldBuildPromptWithMemory() {
        chatMemoryService.addMessage(1L, "user", "Previous step completed");

        String prompt = chatMemoryService.buildPromptWithMemory(1L, 1L, "System", "Current request");

        assertNotNull(prompt);
        assertTrue(prompt.contains("Current request"));
    }
}
