package com.testpilot.agent.rag;

import com.testpilot.agent.knowledge.KnowledgeBase;
import com.testpilot.agent.knowledge.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final KnowledgeEmbeddingRepository embeddingRepository;
    private final KnowledgeBaseService knowledgeBaseService;
    private final ChatClient.Builder chatClientBuilder;

    private static final int DEFAULT_LIMIT = 5;
    private static final double MIN_RELEVANCE_THRESHOLD = 0.7;

    public List<RagResult> search(Long projectId, String query, String category, int limit) {
        try {
            float[] queryEmbedding = generateEmbedding(query);

            List<KnowledgeEmbedding> embeddings;
            if (category != null && !category.isEmpty()) {
                embeddings = embeddingRepository.findSimilarByCategory(projectId, category, queryEmbedding, limit);
            } else {
                embeddings = embeddingRepository.findSimilar(projectId, queryEmbedding, limit);
            }

            return embeddings.stream()
                    .map(e -> RagResult.builder()
                            .id(e.getId())
                            .content(e.getContent())
                            .category(e.getCategory())
                            .source(e.getSource())
                            .relevanceScore(calculateRelevance(queryEmbedding, e.getEmbedding()))
                            .build())
                    .filter(r -> r.getRelevanceScore() >= MIN_RELEVANCE_THRESHOLD)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("RAG search failed: {}", e.getMessage());
            return List.of();
        }
    }

    public void indexKnowledge(Long projectId) {
        List<KnowledgeBase> knowledgeList = knowledgeBaseService.findByCategory(projectId, null);

        for (KnowledgeBase knowledge : knowledgeList) {
            try {
                float[] embedding = generateEmbedding(knowledge.getTitle() + " " + knowledge.getContent());

                KnowledgeEmbedding embeddingEntity = KnowledgeEmbedding.builder()
                        .projectId(projectId)
                        .category(knowledge.getCategory())
                        .content(knowledge.getTitle() + ": " + knowledge.getContent())
                        .embedding(embedding)
                        .source("knowledge_base")
                        .relevanceScore(0.0)
                        .build();

                embeddingRepository.save(embeddingEntity);

            } catch (Exception e) {
                log.error("Failed to index knowledge {}: {}", knowledge.getId(), e.getMessage());
            }
        }
    }

    public void addDocument(Long projectId, String category, String content, String source) {
        try {
            float[] embedding = generateEmbedding(content);

            KnowledgeEmbedding embeddingEntity = KnowledgeEmbedding.builder()
                    .projectId(projectId)
                    .category(category)
                    .content(content)
                    .embedding(embedding)
                    .source(source)
                    .relevanceScore(0.0)
                    .build();

            embeddingRepository.save(embeddingEntity);

        } catch (Exception e) {
            log.error("Failed to add document: {}", e.getMessage());
        }
    }

    public String buildRagContext(Long projectId, String query, String category) {
        List<RagResult> results = search(projectId, query, category, DEFAULT_LIMIT);

        if (results.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("## Relevant Knowledge (RAG)\n");
        for (RagResult result : results) {
            sb.append("- [").append(result.getCategory()).append("] ")
                    .append(result.getContent())
                    .append(" (relevance: ").append(String.format("%.2f", result.getRelevanceScore())).append(")\n");
        }
        return sb.toString();
    }

    private float[] generateEmbedding(String text) {
        try {
            String prompt = "Generate embedding for: " + text;

            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt()
                    .system("You are an embedding generator. Return only the embedding vector as a comma-separated list of numbers.")
                    .user(prompt)
                    .call()
                    .content();

            String[] parts = response.trim().split(",");
            float[] embedding = new float[parts.length];
            for (int i = 0; i < parts.length; i++) {
                embedding[i] = Float.parseFloat(parts[i].trim());
            }

            // Pad or truncate to 1536 dimensions
            if (embedding.length < 1536) {
                float[] padded = new float[1536];
                System.arraycopy(embedding, 0, padded, 0, embedding.length);
                return padded;
            } else if (embedding.length > 1536) {
                float[] truncated = new float[1536];
                System.arraycopy(embedding, 0, truncated, 0, 1536);
                return truncated;
            }

            return embedding;

        } catch (Exception e) {
            log.error("Embedding generation failed: {}", e.getMessage());
            return new float[1536];
        }
    }

    private double calculateRelevance(float[] a, float[] b) {
        if (a.length != b.length) return 0.0;

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    @lombok.Data
    @lombok.Builder
    public static class RagResult {
        private Long id;
        private String content;
        private String category;
        private String source;
        private double relevanceScore;
    }
}
