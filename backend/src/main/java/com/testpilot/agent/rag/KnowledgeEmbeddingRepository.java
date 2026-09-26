package com.testpilot.agent.rag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeEmbeddingRepository extends JpaRepository<KnowledgeEmbedding, Long> {

    @Query(value = "SELECT * FROM knowledge_embeddings " +
            "WHERE project_id = :projectId " +
            "ORDER BY embedding <=> :embedding::vector " +
            "LIMIT :limit", nativeQuery = true)
    List<KnowledgeEmbedding> findSimilar(@Param("projectId") Long projectId,
                                         @Param("embedding") float[] embedding,
                                         @Param("limit") int limit);

    @Query(value = "SELECT * FROM knowledge_embeddings " +
            "WHERE project_id = :projectId AND category = :category " +
            "ORDER BY embedding <=> :embedding::vector " +
            "LIMIT :limit", nativeQuery = true)
    List<KnowledgeEmbedding> findSimilarByCategory(@Param("projectId") Long projectId,
                                                   @Param("category") String category,
                                                   @Param("embedding") float[] embedding,
                                                   @Param("limit") int limit);

    List<KnowledgeEmbedding> findByProjectIdAndCategory(Long projectId, String category);
}
