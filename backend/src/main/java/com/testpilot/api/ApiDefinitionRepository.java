package com.testpilot.api;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiDefinitionRepository extends JpaRepository<ApiDefinition, Long> {
    List<ApiDefinition> findByProjectIdOrderByPath(Long projectId);
    List<ApiDefinition> findByProjectIdAndTag(Long projectId, String tag);
}
