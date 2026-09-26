package com.testpilot.agent.knowledge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {

    @Query("SELECT k FROM KnowledgeBase k WHERE k.projectId = :projectId AND (k.title LIKE %:query% OR k.content LIKE %:query% OR k.tags LIKE %:query%) ORDER BY k.createdAt DESC")
    List<KnowledgeBase> searchByProject(@Param("projectId") Long projectId, @Param("query") String query);

    @Query("SELECT k FROM KnowledgeBase k WHERE k.projectId = :projectId AND k.category = :category AND (k.title LIKE %:query% OR k.content LIKE %:query%) ORDER BY k.createdAt DESC")
    List<KnowledgeBase> searchByProjectAndCategory(@Param("projectId") Long projectId,
                                                   @Param("category") String category,
                                                   @Param("query") String query);

    List<KnowledgeBase> findByProjectIdAndCategory(Long projectId, String category);
}
