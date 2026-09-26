package com.testpilot.agent.rag;

import com.testpilot.agent.knowledge.KnowledgeBase;
import com.testpilot.agent.knowledge.KnowledgeBaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RagServiceTest {

    @InjectMocks
    private RagService ragService;

    @Mock
    private KnowledgeEmbeddingRepository embeddingRepository;

    @Mock
    private KnowledgeBaseService knowledgeBaseService;

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
    }

    @Test
    void shouldSearchKnowledgeBase() {
        String query = "SQL injection testing";

        KnowledgeEmbedding embedding = KnowledgeEmbedding.builder()
                .id(1L)
                .projectId(1L)
                .category("test_method")
                .content("SQL Injection Testing: Use parameterized queries")
                .embedding(new float[1536])
                .build();

        when(embeddingRepository.findSimilar(any(), any(), any())).thenReturn(Arrays.asList(embedding));

        List<RagService.RagResult> results = ragService.search(1L, query, null, 5);

        assertNotNull(results);
    }

    @Test
    void shouldIndexKnowledge() {
        KnowledgeBase knowledge = KnowledgeBase.builder()
                .id(1L)
                .projectId(1L)
                .category("test_method")
                .title("SQL Injection Testing")
                .content("Use parameterized queries to prevent SQL injection")
                .build();

        when(knowledgeBaseService.findByCategory(1L, null)).thenReturn(Arrays.asList(knowledge));
        when(chatClient.prompt()).thenReturn(chatClient);
        when(chatClient.system(any())).thenReturn(chatClient);
        when(chatClient.user(any())).thenReturn(chatClient);
        when(chatClient.call()).thenReturn(chatClient);
        when(chatClient.content()).thenReturn("0.1,0.2,0.3");

        assertDoesNotThrow(() -> ragService.indexKnowledge(1L));
    }

    @Test
    void shouldAddDocument() {
        when(chatClient.prompt()).thenReturn(chatClient);
        when(chatClient.system(any())).thenReturn(chatClient);
        when(chatClient.user(any())).thenReturn(chatClient);
        when(chatClient.call()).thenReturn(chatClient);
        when(chatClient.content()).thenReturn("0.1,0.2,0.3");

        assertDoesNotThrow(() -> ragService.addDocument(1L, "test_method", "Test content", "manual"));
    }

    @Test
    void shouldBuildRagContext() {
        String query = "login testing";

        KnowledgeEmbedding embedding = KnowledgeEmbedding.builder()
                .id(1L)
                .projectId(1L)
                .category("test_method")
                .content("Login Testing: Test with valid and invalid credentials")
                .embedding(new float[1536])
                .build();

        when(embeddingRepository.findSimilar(any(), any(), any())).thenReturn(Arrays.asList(embedding));

        String context = ragService.buildRagContext(1L, query, null);

        assertNotNull(context);
        assertTrue(context.contains("Relevant Knowledge"));
    }
}
