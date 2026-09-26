package com.testpilot.agent.knowledge;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final KnowledgeBaseRepository repository;

    public KnowledgeBase save(KnowledgeBase knowledge) {
        return repository.save(knowledge);
    }

    public List<KnowledgeBase> search(Long projectId, String query, String category, int limit) {
        List<KnowledgeBase> results;
        if (category != null && !category.isEmpty()) {
            results = repository.searchByProjectAndCategory(projectId, category, query);
        } else {
            results = repository.searchByProject(projectId, query);
        }
        if (results.size() > limit) {
            return results.subList(0, limit);
        }
        return results;
    }

    public List<KnowledgeBase> findByCategory(Long projectId, String category) {
        return repository.findByProjectIdAndCategory(projectId, category);
    }
}
