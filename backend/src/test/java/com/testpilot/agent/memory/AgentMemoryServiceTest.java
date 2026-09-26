package com.testpilot.agent.memory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentMemoryServiceTest {

    @InjectMocks
    private AgentMemoryService memoryService;

    @Mock
    private AgentMemoryRepository memoryRepository;

    private AgentMemory testMemory;

    @BeforeEach
    void setUp() {
        testMemory = AgentMemory.builder()
                .id(1L)
                .projectId(1L)
                .memoryType("task_result")
                .content("Test completed successfully")
                .metadata("{\"passed\": 10, \"failed\": 2}")
                .relevanceScore(0.8)
                .build();
    }

    @Test
    void shouldSaveMemory() {
        when(memoryRepository.save(any())).thenReturn(testMemory);

        AgentMemory result = memoryService.save(1L, "task_result", "Test completed", "{}");

        assertNotNull(result);
        assertEquals("task_result", result.getMemoryType());
    }

    @Test
    void shouldGetProjectMemory() {
        List<AgentMemory> memories = Arrays.asList(testMemory);
        when(memoryRepository.findByProjectIdOrderByRelevanceScoreDescCreatedAtDesc(1L)).thenReturn(memories);

        List<AgentMemory> result = memoryService.getProjectMemory(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetMemoryByType() {
        List<AgentMemory> memories = Arrays.asList(testMemory);
        when(memoryRepository.findByProjectIdAndMemoryTypeOrderByRelevanceScoreDescCreatedAtDesc(1L, "task_result")).thenReturn(memories);

        List<AgentMemory> result = memoryService.getByType(1L, "task_result");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetMemoryContext() {
        List<AgentMemory> memories = Arrays.asList(testMemory);
        when(memoryRepository.findByProjectIdOrderByRelevanceScoreDescCreatedAtDesc(1L)).thenReturn(memories);

        String context = memoryService.getMemoryContext(1L);

        assertNotNull(context);
        assertTrue(context.contains("Project Memory"));
    }

    @Test
    void shouldReturnEmptyContextForNoMemory() {
        when(memoryRepository.findByProjectIdOrderByRelevanceScoreDescCreatedAtDesc(1L)).thenReturn(List.of());

        String context = memoryService.getMemoryContext(1L);

        assertEquals("", context);
    }
}
